import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

class ChatServer {
    private val logger = LoggerFactory.getLogger(ChatServer::class.java)

    private val selector = ActorSelectorManager(Dispatchers.IO)

    private val rooms: MutableMap<String, RoomInfo> = mutableMapOf()
    private val clients: MutableMap<String, ClientInfo> = mutableMapOf()

    data class RoomInfo(
        val name: String,
        val users: MutableSet<String> = mutableSetOf(),
    )

    data class ClientInfo(
        val login: String,
        val connection: ChatConnection,
    )

    suspend fun start(hostname: String = "0.0.0.0", port: Int = 3456) = coroutineScope {
        val serverSocket = aSocket(selector)
            .tcp()
            .bind(hostname = hostname, port = port)

        logger.info("ChatServer listening on $hostname:$port")
        while (true) {
            val clientSocket = serverSocket.accept()
            launch { handleClient(clientSocket) }
        }
    }

    private suspend fun handleClient(socket: Socket) {
        logger.info("Accepted a connection from ${socket.remoteAddress}")

        val input = socket.openReadChannel()
        val output = socket.openWriteChannel(autoFlush = true)

        val connection = ChatConnection(socket, input, output)

        val connectPacket = connection.readPacket()
        if (connectPacket.type != PacketType.CONNECT) {
            connection.sendPacket(Packet.fromString(PacketType.ERROR, "Expected connect packet"))
            connection.close()
            return
        }

        val clientInfo = handleConnect(connection, connectPacket)

        try {
            while (true) {
                val packet = connection.readPacket()
                handlePacket(clientInfo, packet)
            }
        } catch (e: Exception) {
            logger.info("Connection failed", e)
        } finally {
            connection.close()
        }
    }

    private suspend fun handlePacket(clientInfo: ClientInfo, packet: Packet) = coroutineScope {
        when (packet.type) {
            PacketType.JOIN_ROOM -> handleJoinRoom(clientInfo, packet)
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

    private suspend fun handleConnect(connection: ChatConnection, packet: Packet): ClientInfo {
        val login = packet.asString()
        logger.info("[$login] Client connected")

        if (login in clients) {
            connection.sendPacket(Packet.fromString(PacketType.ERROR, "Login already used"))
            throw RuntimeException("")
        }

        clients[login] = ClientInfo(
            login,
            connection,
        )

        sendRoomList(connection)

        return clients[login]!!
    }

    private suspend fun handleJoinRoom(clientInfo: ClientInfo, packet: Packet) = coroutineScope {
        val roomName = packet.asString()
        logger.info("[${clientInfo.login}] Joining room $roomName")

        val room = rooms.computeIfAbsent(roomName) { RoomInfo(it) }

        room.users.add(clientInfo.login)

        room.users
            .mapNotNull { login -> clients[login] }
            .forEach { client ->
                launch {
                    client.connection.sendPacket(
                        Packet.fromString(
                            PacketType.ROOM_UPDATE,
                            Utils.toJson(
                                RoomUpdate(
                                    RoomUpdateType.JOIN,
                                    room.name,
                                    clientInfo.login,
                                )
                            )
                        )
                    )
                }
            }
    }

    private suspend fun handleVoice(clientInfo: ClientInfo, packet: Packet) {
        val room = rooms.values
            .firstOrNull { clientInfo.login in it.users }
            ?: return

        room.users
            .filter { login -> login != clientInfo.login }
            .mapNotNull { login -> clients[login] }
            .forEach { client ->
                client.connection.sendPacket(Packet(PacketType.VOICE, packet.buffer.duplicate()))
            }
    }

    private suspend fun sendRoomList(connection: ChatConnection) {
        connection.sendPacket(
            Packet.fromString(PacketType.ROOM_LIST, Utils.toJson(
                RoomListData(
                    rooms = rooms.values.map { room ->
                        RoomData(
                            name = room.name,
                            users = room.users.toList(),
                        )
                    },
                )
            ))
        )
    }
}
