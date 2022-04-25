package me.darkkeks.soa.serialization.formats

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import me.darkkeks.soa.serialization.ClientsData

class YamlFormat : Format {
    private val mapper = YAMLMapper()
        .registerModule(KotlinModule.Builder().build())

    override val name = "yaml"
    override fun serialize(data: ClientsData): ByteArray = mapper.writeValueAsBytes(data)
    override fun deserialize(data: ByteArray) = mapper.readValue<ClientsData>(data)
}
