package me.darkkeks.soa.serialization.formats

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import me.darkkeks.soa.serialization.ClientsData

class JsonFormat : Format {
    private val mapper = ObjectMapper()
        .registerModule(KotlinModule.Builder().build())

    override val name = "json"
    override fun serialize(data: ClientsData): ByteArray = mapper.writeValueAsBytes(data)
    override fun deserialize(data: ByteArray) = mapper.readValue<ClientsData>(data)
}
