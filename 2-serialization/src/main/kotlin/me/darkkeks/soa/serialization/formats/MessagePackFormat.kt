package me.darkkeks.soa.serialization.formats

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import me.darkkeks.soa.serialization.ClientsData
import org.msgpack.jackson.dataformat.MessagePackFactory

class MessagePackFormat : Format {
    private val mapper = ObjectMapper(MessagePackFactory())
        .registerModule(KotlinModule.Builder().build())

    override val name = "msgpack"
    override fun serialize(data: ClientsData): ByteArray = mapper.writeValueAsBytes(data)
    override fun deserialize(data: ByteArray) = mapper.readValue<ClientsData>(data)
}
