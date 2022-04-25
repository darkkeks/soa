package me.darkkeks.soa.serialization.formats

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import me.darkkeks.soa.serialization.ClientsData

class XmlFormat : Format {
    private val mapper = XmlMapper()
        .configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, false)
        .registerModule(KotlinModule.Builder().build())

    override val name = "xml"
    override fun serialize(data: ClientsData): ByteArray = mapper.writeValueAsBytes(data)
    override fun deserialize(data: ByteArray) = mapper.readValue<ClientsData>(data)
}
