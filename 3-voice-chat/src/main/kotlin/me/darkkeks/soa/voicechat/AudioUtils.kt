package me.darkkeks.soa.voicechat

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.lwjgl.BufferUtils
import org.lwjgl.openal.AL
import org.lwjgl.openal.AL10.AL_BUFFERS_PROCESSED
import org.lwjgl.openal.AL10.AL_FORMAT_MONO16
import org.lwjgl.openal.AL10.AL_INITIAL
import org.lwjgl.openal.AL10.AL_NO_ERROR
import org.lwjgl.openal.AL10.AL_SOURCE_STATE
import org.lwjgl.openal.AL10.AL_STOPPED
import org.lwjgl.openal.AL10.alBufferData
import org.lwjgl.openal.AL10.alGenBuffers
import org.lwjgl.openal.AL10.alGenSources
import org.lwjgl.openal.AL10.alGetError
import org.lwjgl.openal.AL10.alGetSourcei
import org.lwjgl.openal.AL10.alGetString
import org.lwjgl.openal.AL10.alSourcePlay
import org.lwjgl.openal.AL10.alSourceQueueBuffers
import org.lwjgl.openal.AL10.alSourceUnqueueBuffers
import org.lwjgl.openal.ALC
import org.lwjgl.openal.ALC10.ALC_DEVICE_SPECIFIER
import org.lwjgl.openal.ALC10.ALC_NO_ERROR
import org.lwjgl.openal.ALC10.alcCloseDevice
import org.lwjgl.openal.ALC10.alcCreateContext
import org.lwjgl.openal.ALC10.alcDestroyContext
import org.lwjgl.openal.ALC10.alcGetError
import org.lwjgl.openal.ALC10.alcGetInteger
import org.lwjgl.openal.ALC10.alcMakeContextCurrent
import org.lwjgl.openal.ALC10.alcOpenDevice
import org.lwjgl.openal.ALC11.ALC_CAPTURE_DEVICE_SPECIFIER
import org.lwjgl.openal.ALC11.ALC_CAPTURE_SAMPLES
import org.lwjgl.openal.ALC11.alcCaptureCloseDevice
import org.lwjgl.openal.ALC11.alcCaptureOpenDevice
import org.lwjgl.openal.ALC11.alcCaptureSamples
import org.lwjgl.openal.ALC11.alcCaptureStart
import org.lwjgl.openal.ALC11.alcCaptureStop
import org.lwjgl.openal.ALUtil.getStringList
import org.lwjgl.system.MemoryUtil.NULL
import org.slf4j.LoggerFactory
import java.nio.ByteBuffer

object AudioUtils {

    private val logger = LoggerFactory.getLogger(AudioUtils::class.java)

    private const val frequency = 48000  // 48kHz
    private const val samplesBufferSize = frequency * 5
    private const val samplesPerPacket = 2048

    private const val captureSamplesDelayMillis = 50L
    private const val playbackQueueDelayMillis = 50L

    private const val playbackBufferCount = 5

    private const val inputFormat = AL_FORMAT_MONO16
    private const val outputFormat = AL_FORMAT_MONO16

    suspend fun loopbackAudio() = coroutineScope {
        val channel = Channel<ByteBuffer>(capacity = Channel.UNLIMITED)
        launch { startCapture(channel) }
        launch { startPlayback(channel) }
    }

    suspend fun startCapture(channel: Channel<ByteBuffer>) {
        // select input device
        val inputDevices: List<String> = getStringList(0, ALC_CAPTURE_DEVICE_SPECIFIER)!!
        val inputDeviceName = inputDevices.first()

        logger.debug("Opening capture device '{}'", inputDeviceName)
        val inputDeviceHandle = alcCaptureOpenDevice(inputDeviceName, frequency, inputFormat, samplesBufferSize)
        if (inputDeviceHandle == NULL) {
            throw RuntimeException("Failed to open device '$inputDeviceName'")
        }

        logger.debug("Starting capture")
        alcCaptureStart(inputDeviceHandle)
        alcCheckError(inputDeviceHandle)

        try {
            // expecting a cancellation
            while (true) {
                while (alcGetInteger(inputDeviceHandle, ALC_CAPTURE_SAMPLES) >= samplesPerPacket) {
                    val buffer = BufferUtils.createByteBuffer(Short.SIZE_BYTES * samplesPerPacket)
                    alcCaptureSamples(inputDeviceHandle, buffer, samplesPerPacket)
                    alcCheckError(inputDeviceHandle)

                    try {
                        channel.send(buffer)
                    } catch (e: ClosedSendChannelException) {
                        // output is closed, stopping capture
                        return
                    }
                }

                delay(captureSamplesDelayMillis)
            }
        } finally {
            logger.debug("Closing capture device '{}'", inputDeviceName)
            alcCaptureStop(inputDeviceHandle)
            alcCheckError(inputDeviceHandle)

            if (!alcCaptureCloseDevice(inputDeviceHandle)) {
                throw RuntimeException("Failed to close capture device")
            }
        }
    }

    suspend fun startPlayback(channel: ReceiveChannel<ByteBuffer>) {
        // select output device
        val outputDevices: List<String> = getStringList(0, ALC_DEVICE_SPECIFIER)!!
        val outputDeviceName = outputDevices.first()

        logger.debug("Opening playback device '{}'", outputDeviceName)
        val outputDeviceHandle: Long = alcOpenDevice(outputDeviceName)
        if (outputDeviceHandle == NULL) {
            throw RuntimeException("Failed to open device '$outputDeviceName'")
        }

        val context: Long = alcCreateContext(outputDeviceHandle, null as IntArray?)
        alcCheckError(outputDeviceHandle)
        alcMakeContextCurrent(context)
        alcCheckError(outputDeviceHandle)

        val aclCapabilities = ALC.createCapabilities(outputDeviceHandle)
        AL.createCapabilities(aclCapabilities)

        val source: Int = alGenSources()
        alCheckError()

        val playbackBuffers = IntArray(playbackBufferCount)
        alGenBuffers(playbackBuffers)
        alCheckError()

        // index of next unused buffer
        var currentBuffer = 0

        try {
            while (true) {
                val buffer = try {
                    channel.receive()
                } catch (e: ClosedReceiveChannelException) {
                    // input is closed, stopping playback
                    return
                }

                check(buffer.isDirect) { "Expecting a direct buffer" }

                val bufferName = if (currentBuffer < playbackBuffers.size) {
                    playbackBuffers[currentBuffer++]
                } else {
                    // wait for a buffer to be ready for use
                    while (alGetSourcei(source, AL_BUFFERS_PROCESSED) == 0) {
                        alCheckError()
                        delay(playbackQueueDelayMillis)
                    }

                    alSourceUnqueueBuffers(source)
                        .also { alCheckError() }
                }

                alBufferData(bufferName, outputFormat, buffer, frequency)
                alCheckError()

                alSourceQueueBuffers(source, bufferName)
                alCheckError()

                if (alGetSourcei(source, AL_SOURCE_STATE) in listOf(AL_INITIAL, AL_STOPPED)) {
                    alSourcePlay(source)
                    alCheckError()
                }
            }
        } finally {
            logger.debug("Closing playback device '{}'", outputDeviceName)
            alcMakeContextCurrent(0)
            alcCheckError(outputDeviceHandle)
            alcDestroyContext(context)
            alcCheckError(outputDeviceHandle)
            if (!alcCloseDevice(outputDeviceHandle)) {
                throw RuntimeException("Failed to close device")
            }
            alcCheckError(0)
        }
    }

    private fun alcCheckError(deviceHandle: Long) {
        val error = alcGetError(deviceHandle)
        if (error != ALC_NO_ERROR) {
            throw RuntimeException("Alc error: ${alGetString(error)} ($error)")
        }
    }

    private fun alCheckError() {
        val error = alGetError()
        if (error != AL_NO_ERROR) {
            throw RuntimeException("Alc error: ${alGetString(error)} ($error)")
        }
    }
}
