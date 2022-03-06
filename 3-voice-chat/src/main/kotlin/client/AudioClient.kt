package client

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.nio.ByteBuffer

class AudioClient {
    private val logger = LoggerFactory.getLogger(AudioClient::class.java)

    private var captureChannel: Channel<ByteBuffer>
    private var playbackChannel: Channel<ByteBuffer>

    private val outputChannel: Channel<ByteBuffer> = Channel()

    private val scope = CoroutineScope(CoroutineExceptionHandler { _, exception ->
        logger.info("Uncaught exception", exception)
    })

    init {
        captureChannel = Channel(capacity = Channel.UNLIMITED)
        playbackChannel = Channel(capacity = Channel.UNLIMITED)
    }

    private fun resetChannels() {
        captureChannel = Channel(capacity = Channel.UNLIMITED)
        playbackChannel = Channel(capacity = Channel.UNLIMITED)
    }

    fun voiceChannel(): ReceiveChannel<ByteBuffer> {
        return outputChannel
    }

    fun start() {
        scope.launch {
            AudioUtils.startCapture(captureChannel)
        }

        scope.launch {
            AudioUtils.startPlayback(playbackChannel)
        }

        scope.launch {
            captureChannel.consumeEach { buffer ->
                outputChannel.send(buffer)
            }
        }
    }

    suspend fun accept(buffer: ByteBuffer) {
        playbackChannel.send(buffer)
    }

    fun stop() {
        captureChannel.close()
        playbackChannel.close()
        resetChannels()
    }
}
