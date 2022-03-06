import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import java.nio.ByteBuffer

open class ChatConnection(
    private val socket: Socket,
    private val input: ByteReadChannel,
    private val output: ByteWriteChannel,
) {
    suspend fun readPacket(): Packet {
        val typeId = input.readByte()
        val type = PacketType.byId(typeId)

        val length = input.readInt()
        val data = ByteBuffer.allocateDirect(length)
        input.readFully(data)

        data.rewind()
        return Packet(type, data)
    }

    suspend fun sendPacket(packet: Packet) {
        output.writePacket {
            writeByte(packet.type.id)
            writeInt(packet.buffer.limit())
            writeFully(packet.buffer)
        }
        output.flush()
    }

    fun close() {
        socket.close()
    }
}
