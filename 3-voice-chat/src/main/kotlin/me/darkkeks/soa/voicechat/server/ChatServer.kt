package me.darkkeks.soa.voicechat.server

import me.darkkeks.soa.voicechat.protocol.Packet
import me.darkkeks.soa.voicechat.protocol.PacketType
import me.darkkeks.soa.voicechat.protocol.RoomUpdate
import me.darkkeks.soa.voicechat.protocol.RoomUpdateType
import me.darkkeks.soa.voicechat.JsonUtils
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import me.darkkeks.soa.voicechat.protocol.ChatConnection
import me.darkkeks.soa.voicechat.protocol.ProtocolConstants

class ChatServer {
    private val logger = LoggerFactory.getLogger(ChatServer::class.java)

    private val selector = ActorSelectorManager(Dispatchers.IO)

    private val serverData = ServerData()

    suspend fun start(hostname: String = "0.0.0.0", port: Int = ProtocolConstants.defaultPort) = coroutineScope {
        val serverSocket = aSocket(selector)
            .tcp()
            .bind(hostname = hostname, port = port)

        logger.info("server.ChatServer listening on $hostname:$port")
        while (true) {
            val clientSocket = serverSocket.accept()
            launch { handleClient(clientSocket) }
        }
    }

    private suspend fun handleClient(socket: Socket) {
        logger.info("Accepted a connection from ${socket.remoteAddress}")

        val connection = ChatConnection(socket)

        try {
            val connectPacket = connection.readPacket()
            if (connectPacket.type != PacketType.CONNECT) {
                connection.sendPacket(Packet.fromString(PacketType.ERROR, "Expected connect packet"))
                connection.close()
                return
            }

            val clientInfo = handleConnect(connection, connectPacket)
            try {
                sendRoomList(connection)
                while (true) {
                    val packet = connection.readPacket()
                    handlePacket(clientInfo, packet)
                }
            } finally {
                removeClient(clientInfo)
            }
        } catch (e: Exception) {
            logger.info("Connection failed", e)
        } finally {
            connection.close()
        }
    }

    private suspend fun handlePacket(clientInfo: ServerData.ClientInfo, packet: Packet) = coroutineScope {
        when (packet.type) {
            PacketType.JOIN_ROOM -> handleJoinRoom(clientInfo, packet)
            PacketType.LEAVE_ROOM -> handleLeaveRoom(clientInfo)
            PacketType.TALKING_STATE -> handleTalkingState(clientInfo, packet)
            PacketType.VOICE -> handleVoice(clientInfo, packet)

            PacketType.ERROR -> {
                logger.info("Received error from server: {}", packet.asString())
                clientInfo.connection.close()
            }

            else -> {
                clientInfo.connection.sendPacket(Packet.fromString(PacketType.ERROR, "Invalid packet type"))
                clientInfo.connection.close()
            }
        }
    }

    private suspend fun handleConnect(connection: ChatConnection, packet: Packet): ServerData.ClientInfo {
        val login = packet.asString()
        logger.info("[$login] Client connected")

        val clientInfo = ServerData.ClientInfo(
            login = login,
            connection = connection,
        )

        if (!serverData.addClient(clientInfo)) {
            connection.sendPacket(Packet.fromString(PacketType.ERROR, "Login already used"))
            throw RuntimeException("Login already exists")
        }

        return clientInfo
    }

    private suspend fun handleJoinRoom(clientInfo: ServerData.ClientInfo, packet: Packet) = coroutineScope {
        val roomName = packet.asString()
        if (serverData.joinRoom(clientInfo, roomName)) {
            logger.info("[${clientInfo.login}] Joined room $roomName")
            broadcastRoomUpdate(RoomUpdate(RoomUpdateType.JOIN, roomName, clientInfo.login))
        } else {
            clientInfo.connection.sendPacket(Packet.fromString(PacketType.ERROR, "Failed to join room"))
        }
    }

    private suspend fun handleLeaveRoom(clientInfo: ServerData.ClientInfo) = coroutineScope {
        val room = serverData.leaveRoom(clientInfo)
        if (room != null) {
            logger.info("[${clientInfo.login}] Left room $room")
            broadcastRoomUpdate(RoomUpdate(RoomUpdateType.LEAVE, room, clientInfo.login))
        } else {
            clientInfo.connection.sendPacket(Packet.fromString(PacketType.ERROR, "Failed to leave room"))
        }
    }

    private suspend fun handleTalkingState(clientInfo: ServerData.ClientInfo, packet: Packet) = coroutineScope {
        val talking: Boolean = packet.buffer.get() != 0.toByte()
        if (serverData.updateTalkingState(clientInfo, talking)) {
            val type = if (talking) {
                RoomUpdateType.START_TALKING
            } else {
                RoomUpdateType.STOP_TALKING
            }
            broadcastRoomUpdate(RoomUpdate(type, clientInfo.room!!, clientInfo.login))
        }
    }

    private suspend fun handleVoice(clientInfo: ServerData.ClientInfo, packet: Packet) = coroutineScope {
        if (clientInfo.room != null) {
            serverData.getClients()
                .filter { it.room == clientInfo.room }
                .filter { it.login != clientInfo.login }
                .forEach { other ->
                    launch {
                        other.connection.sendPacket(Packet(PacketType.VOICE, packet.buffer.duplicate()))
                    }
                }
        }
    }

    private suspend fun sendRoomList(connection: ChatConnection) {
        connection.sendPacket(Packet.fromString(PacketType.ROOM_LIST, JsonUtils.toJson(serverData.getRooms())))
    }

    private suspend fun removeClient(clientInfo: ServerData.ClientInfo) = coroutineScope {
        if (serverData.removeClient(clientInfo)) {
            val room = clientInfo.room
                ?: return@coroutineScope
            logger.info("[${clientInfo.login}] Left room $room")
            broadcastRoomUpdate(RoomUpdate(RoomUpdateType.LEAVE, room, clientInfo.login))
        }
    }

    private suspend fun broadcastRoomUpdate(update: RoomUpdate) = coroutineScope {
        serverData.getClients().forEach { client ->
            launch {
                client.connection.sendPacket(Packet.fromString(PacketType.ROOM_UPDATE, JsonUtils.toJson(update)))
            }
        }
    }
}
