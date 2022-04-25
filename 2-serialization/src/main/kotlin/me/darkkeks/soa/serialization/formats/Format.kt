package me.darkkeks.soa.serialization.formats

import me.darkkeks.soa.serialization.ClientsData

interface Format {
    val name: String
    fun serialize(data: ClientsData): ByteArray
    fun deserialize(data: ByteArray): ClientsData
}
