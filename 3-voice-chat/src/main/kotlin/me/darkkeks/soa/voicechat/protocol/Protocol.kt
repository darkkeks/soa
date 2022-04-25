package me.darkkeks.soa.voicechat.protocol

import java.nio.ByteBuffer

object ProtocolConstants {
    const val defaultPort: Int = 8457
}

enum class PacketType(val id: Byte) {
    // serverbound
    CONNECT(0x00),
    JOIN_ROOM(0x01),
    LEAVE_ROOM(0x04),
    TALKING_STATE(0x02),
    VOICE(0x03),

    // clientbound
    ROOM_LIST(0x10),
    ROOM_UPDATE(0x11),
    SERVER_VOICE(0x12),

    // both ways
    ERROR(0x20),
    ;

    companion object {
        private val packetsByType = values()
            .associateBy { it.id }

        fun byId(id: Byte) = packetsByType[id]
            ?: throw IllegalArgumentException("Invalid packet type id $id")
    }
}

data class Packet(
    val type: PacketType,
    val buffer: ByteBuffer,
) {
    fun asString(): String {
        val array = if (buffer.hasArray()) {
            buffer.array()
        } else {
            ByteArray(buffer.remaining())
                .also { buffer.get(it) }
        }
        return String(array)
    }

    companion object {
        fun fromString(type: PacketType, string: String? = null): Packet {
            val array = string?.toByteArray() ?: byteArrayOf()
            return Packet(type, ByteBuffer.wrap(array))
        }
    }
}

// Room list
data class RoomListData(
    val rooms: List<RoomData>,
)

data class RoomData(
    val name: String,
    val users: List<RoomUserData>,
)

data class RoomUserData(
    val login: String,
    val talking: Boolean,
)

// Room update
data class RoomUpdate(
    val type: RoomUpdateType,
    val name: String,
    val login: String,
)

enum class RoomUpdateType {
    JOIN,
    LEAVE,

    START_TALKING,
    STOP_TALKING,
}
