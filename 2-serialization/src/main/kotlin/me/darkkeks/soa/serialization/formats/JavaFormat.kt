package me.darkkeks.soa.serialization.formats

import me.darkkeks.soa.serialization.ClientsData
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class JavaFormat : Format {
    override val name = "java"

    override fun serialize(data: ClientsData): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        ObjectOutputStream(byteArrayOutputStream).writeObject(data)
        return byteArrayOutputStream.toByteArray()
    }

    override fun deserialize(data: ByteArray): ClientsData {
        return ObjectInputStream(ByteArrayInputStream(data)).readObject() as ClientsData
    }
}
