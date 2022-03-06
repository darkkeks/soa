package server

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import protocol.ChatConnection
import protocol.RoomData
import protocol.RoomListData
import protocol.RoomUserData

class ServerData {

    private val mutex = Mutex()

    private val rooms: MutableMap<String, RoomInfo> = mutableMapOf()
    private val clients: MutableMap<String, ClientInfo> = mutableMapOf()

    data class RoomInfo(
        val name: String,
        val users: MutableSet<String> = mutableSetOf(),
    )

    data class ClientInfo(
        val login: String,
        val connection: ChatConnection,

        var talking: Boolean = false,
        var room: String? = null,
    )

    suspend fun addClient(clientInfo: ClientInfo): Boolean = mutex.withLock {
        return clients.putIfAbsent(clientInfo.login, clientInfo) == null
    }

    suspend fun removeClient(clientInfo: ClientInfo): Boolean = mutex.withLock {
        val removed = clients.remove(clientInfo.login) != null
        if (removed) {
            if (clientInfo.room != null) {
                rooms[clientInfo.room]!!.users.remove(clientInfo.login)
            }
        }
        return removed
    }

    suspend fun joinRoom(clientInfo: ClientInfo, roomName: String): Boolean = mutex.withLock {
        if (clientInfo.login !in clients) {
            return false
        }

        if (clientInfo.room != null) {
            return false
        }

        val room = rooms.computeIfAbsent(roomName) { RoomInfo(it) }

        clientInfo.room = roomName
        return room.users.add(clientInfo.login)
    }

    /**
     * @return name of a room the user was in, or null
     */
    suspend fun leaveRoom(clientInfo: ClientInfo): String? = mutex.withLock {
        if (clientInfo.login !in clients) {
            return null
        }

        if (clientInfo.room == null) {
            return null
        }

        val room = rooms[clientInfo.room]
            ?: throw IllegalStateException("Room has to exist")

        val removed = room.users.remove(clientInfo.login)
        check(removed) { "User has to be in the room" }

        clientInfo.room = null
        clientInfo.talking = false

        return room.name
    }

    suspend fun updateTalkingState(clientInfo: ClientInfo, talking: Boolean): Boolean = mutex.withLock {
        if (clientInfo.talking != talking) {
            clientInfo.talking = talking
            return true
        }
        return false
    }

    suspend fun getClients(): Collection<ClientInfo> = mutex.withLock {
        return clients.values.toList()
    }

    suspend fun getRooms(): RoomListData = mutex.withLock {
        return RoomListData(
            rooms = rooms.values.map { roomInfo ->
                RoomData(
                    name = roomInfo.name,
                    users = roomInfo.users.map { login ->
                        RoomUserData(
                            login = login,
                            talking = clients[login]!!.talking,
                        )
                    }
                )
            }
        )
    }
}

