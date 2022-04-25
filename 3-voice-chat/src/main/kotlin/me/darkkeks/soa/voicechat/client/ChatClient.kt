package me.darkkeks.soa.voicechat.client

import me.darkkeks.soa.voicechat.JsonUtils
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import me.darkkeks.soa.voicechat.protocol.ChatConnection
import me.darkkeks.soa.voicechat.protocol.Packet
import me.darkkeks.soa.voicechat.protocol.PacketType
import me.darkkeks.soa.voicechat.protocol.RoomListData
import me.darkkeks.soa.voicechat.protocol.RoomUpdate
import me.darkkeks.soa.voicechat.protocol.RoomUpdateType
import java.nio.ByteBuffer

class ChatClient(
    private val login: String,
) {
    private val logger = LoggerFactory.getLogger(ChatClient::class.java)

    private val selector = ActorSelectorManager(Dispatchers.IO)
    private var connection: ChatConnection? = null

    private val audioClient = AudioClient()

    private val clientData = ClientData()

    suspend fun start(hostname: String = "0.0.0.0", port: Int = 3456) = coroutineScope {
        logger.info("Connecting to $hostname:$port")

        val socket = aSocket(selector)
            .tcp()
            .connect(hostname = hostname, port = port)

        logger.info("Connected")

        val connection = ChatConnection(socket)
        this@ChatClient.connection = connection

        launch {
            audioClient.voiceChannel().consumeEach { voicePacket ->
                if (clientData.isTalking()) {
                    connection.sendPacket(Packet(PacketType.VOICE, voicePacket))
                }
            }
        }

        connection.sendPacket(Packet.fromString(PacketType.CONNECT, login))

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
            PacketType.ROOM_LIST -> handleRoomList(packet)
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

    private suspend fun handleRoomList(packet: Packet) {
        val rooms: RoomListData = JsonUtils.fromJson(packet.asString())
        logger.info("[$login] Room list: {}", rooms)
        clientData.update(rooms)
    }

    private suspend fun handleRoomUpdate(packet: Packet) {
        val roomUpdate: RoomUpdate = JsonUtils.fromJson(packet.asString())

        clientData.update(roomUpdate)

        if (roomUpdate.login != login) {
            when (roomUpdate.type) {
                RoomUpdateType.JOIN ->
                    logger.info("[$login] User ${roomUpdate.login} joined room ${roomUpdate.name}")
                RoomUpdateType.LEAVE ->
                    logger.info("[$login] User ${roomUpdate.login} left room ${roomUpdate.name}")
                RoomUpdateType.START_TALKING ->
                    logger.info("[$login] User ${roomUpdate.login} started talking in the room ${roomUpdate.name}")
                RoomUpdateType.STOP_TALKING ->
                    logger.info("[$login] User ${roomUpdate.login} stopped talking in the room ${roomUpdate.name}")
            }
        } else {
            when (roomUpdate.type) {
                RoomUpdateType.JOIN -> {
                    logger.info("[$login] Joined room ${roomUpdate.name}")
                    audioClient.start()
                }
                RoomUpdateType.LEAVE -> {
                    logger.info("[$login] Left room ${roomUpdate.name}")
                    audioClient.stop()
                }
                RoomUpdateType.START_TALKING ->
                    logger.info("[$login] Started talking in room ${roomUpdate.name}")
                RoomUpdateType.STOP_TALKING ->
                    logger.info("[$login] Stopped talking in room ${roomUpdate.name}")
            }
        }
    }

    private suspend fun handleVoice(packet: Packet) {
        audioClient.accept(packet.buffer)
    }

    fun getRooms() = clientData.getRooms()

    suspend fun joinRoom(room: String) {
        connection?.sendPacket(Packet.fromString(PacketType.JOIN_ROOM, room))
    }

    suspend fun toggleTalking() {
        val talking: Boolean = clientData.toggleTalking()
        val buffer = ByteBuffer.allocate(1)
        buffer.put(if (talking) 1 else 0)
        buffer.rewind()
        connection?.sendPacket(Packet(PacketType.TALKING_STATE, buffer))
    }

    suspend fun leaveRoom() {
        connection?.sendPacket(Packet.fromString(PacketType.LEAVE_ROOM))
        clientData.leaveRoom()
        if (clientData.isTalking()) {
            clientData.toggleTalking()
        }
    }

    fun stop() {
        connection?.close()
    }
}
