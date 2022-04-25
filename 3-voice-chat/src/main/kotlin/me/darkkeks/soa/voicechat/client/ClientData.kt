package me.darkkeks.soa.voicechat.client

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.darkkeks.soa.voicechat.protocol.RoomData
import me.darkkeks.soa.voicechat.protocol.RoomListData
import me.darkkeks.soa.voicechat.protocol.RoomUpdate
import me.darkkeks.soa.voicechat.protocol.RoomUpdateType
import me.darkkeks.soa.voicechat.protocol.RoomUserData

class ClientData {
    private val mutex = Mutex()

    private var talking: Boolean = false
    private var rooms: RoomListData = RoomListData(listOf())

    fun getRooms(): RoomListData {
        return rooms
    }

    fun isTalking(): Boolean = talking

    suspend fun toggleTalking(): Boolean = mutex.withLock {
        talking = !talking
        return talking
    }


    suspend fun leaveRoom() = mutex.withLock {
        if (talking) {
            talking = false
        }
    }

    suspend fun update(roomListData: RoomListData): Unit = mutex.withLock {
        rooms = roomListData
    }

    suspend fun update(update: RoomUpdate): Unit = mutex.withLock {
        val roomList = rooms.rooms.toMutableList()

        if (roomList.none { it.name == update.name }) {
            roomList += RoomData(
                name = update.name,
                users = listOf(),
            )
        }

        rooms = rooms.copy(
            rooms = roomList.map { room ->
                if (room.name == update.name) {
                    when (update.type) {
                        RoomUpdateType.JOIN -> room.copy(
                            users = room.users.filter { it.login != update.login } + RoomUserData(
                                login = update.login,
                                talking = false,
                            )
                        )
                        RoomUpdateType.LEAVE -> room.copy(
                            users = room.users.filter { it.login != update.login }
                        )
                        RoomUpdateType.START_TALKING -> room.copy(
                            users = room.users.map { userInfo ->
                                if (userInfo.login == update.login) {
                                    userInfo.copy(talking = true)
                                } else {
                                    userInfo
                                }
                            }
                        )
                        RoomUpdateType.STOP_TALKING -> room.copy(
                            users = room.users.map { userInfo ->
                                if (userInfo.login == update.login) {
                                    userInfo.copy(talking = false)
                                } else {
                                    userInfo
                                }
                            }
                        )
                    }
                } else {
                    room
                }
            },
        )
    }
}
