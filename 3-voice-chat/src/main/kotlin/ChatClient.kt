import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.nio.ByteBuffer
import kotlin.time.Duration.Companion.milliseconds

class ChatClient(
    private val login: String,
) {
    private val logger = LoggerFactory.getLogger(ChatClient::class.java)

    private val selector = ActorSelectorManager(Dispatchers.IO)

    private val captureChannel: Channel<ByteBuffer> = Channel(capacity = Channel.UNLIMITED)
    private val playbackChannel: Channel<ByteBuffer> = Channel(capacity = Channel.UNLIMITED)

    private val audioScope = CoroutineScope(
        CoroutineExceptionHandler { _, throwable ->
            logger.info("[$login] Uncaught exception in AudioScope", throwable)
        }
    )

    suspend fun start(hostname: String = "0.0.0.0", port: Int = 3456) = coroutineScope {
        val socket = aSocket(selector)
            .tcp()
            .connect(hostname = hostname, port = port)

        logger.info("Connected to $hostname:$port")

        val input = socket.openReadChannel()
        val output = socket.openWriteChannel(autoFlush = false)

        val connection = ChatConnection(socket, input, output)

        connection.sendPacket(Packet.fromString(PacketType.CONNECT, login))

        launch {
            captureChannel.consumeEach { voicePacket ->
                launch {
                    connection.sendPacket(Packet(PacketType.VOICE, voicePacket))
                }
            }
        }

        try {
            while (true) {
                val packet = connection.readPacket()
                handlePacket(connection, packet)
            }
        } catch (e: Exception) {
            logger.info("Connection failed", e)
        } finally {
            connection.close()
            cancel()
        }
    }

    private suspend fun handlePacket(connection: ChatConnection, packet: Packet) {
        when (packet.type) {
            PacketType.ROOM_LIST -> handleRoomList(connection, packet)
            PacketType.ROOM_UPDATE -> handleRoomUpdate(packet)
            PacketType.VOICE -> handleVoice(packet)

            PacketType.ERROR -> {
                logger.info("[$login] Received error from server: {}", packet.asString())
                connection.close()
            }

            else -> {
                connection.sendPacket(Packet.fromString(PacketType.ERROR, "Invalid packet type"))
                connection.close()
            }
        }
    }

    private suspend fun handleRoomList(connection: ChatConnection, packet: Packet) {
        val rooms: RoomListData = Utils.fromJson(packet.asString())
        logger.info("[$login] Room list: {}", rooms)

        val roomToJoin = rooms.rooms.firstOrNull()?.name
            ?: login

        connection.sendPacket(Packet.fromString(PacketType.JOIN_ROOM, roomToJoin))
    }

    private fun handleRoomUpdate(packet: Packet) {
        val roomUpdate: RoomUpdate = Utils.fromJson(packet.asString())

        if (roomUpdate.login != login) {
            logger.info("[$login] User ${roomUpdate.login} joined room ${roomUpdate.name}")
            return
        }

        logger.info("[$login] Joined room ${roomUpdate.name}")
        startAudioCaptureAndPlayback()
    }

    private suspend fun handleVoice(packet: Packet) {
        playbackChannel.send(packet.buffer)
    }

    private fun startAudioCaptureAndPlayback() {
        audioScope.launch {
            logger.info("[$login] Starting capture")
            AudioUtils.startCapture(captureChannel)
        }

        audioScope.launch {
            logger.info("[$login] Starting playback")
            AudioUtils.startPlayback(playbackChannel)
        }
    }
}
