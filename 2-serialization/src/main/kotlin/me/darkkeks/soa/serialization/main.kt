package me.darkkeks.soa.serialization

import me.darkkeks.soa.serialization.formats.AvroFormat
import me.darkkeks.soa.serialization.formats.Format
import me.darkkeks.soa.serialization.formats.JavaFormat
import me.darkkeks.soa.serialization.formats.JsonFormat
import me.darkkeks.soa.serialization.formats.MessagePackFormat
import me.darkkeks.soa.serialization.formats.ProtobufFormat
import me.darkkeks.soa.serialization.formats.XmlFormat
import me.darkkeks.soa.serialization.formats.YamlFormat

data class BenchmarkResult(
    val name: String,
    val dataSize: Int,
    val meanSerializationTimeMs: Double,
    val meanDeserializationTimeMs: Double,
)

fun benchmark(
    data: ClientsData,
    formats: List<Format>,
    preheatIterations: Int = 10,
    iterations: Int = 100,
): List<BenchmarkResult> {
    val results: MutableList<BenchmarkResult> = mutableListOf()

    formats.forEach { format ->
        println("Benchmarking ${format.name}")

        val serialized = format.serialize(data)

        println("Preheating ${format.name}...")
        repeat(preheatIterations) {
            format.serialize(data)
            format.deserialize(serialized)
        }

        println("Serializing ${format.name}...")
        val serializeClock = BenchmarkClock()
        repeat(iterations) {
            serializeClock.run {
                format.serialize(data)
            }
        }

        println("Deserializing ${format.name}...")
        val deserializeClock = BenchmarkClock()
        repeat(iterations) {
            deserializeClock.run {
                format.deserialize(serialized)
            }
        }

        results += BenchmarkResult(
            name = format.name,
            dataSize = serialized.size,
            meanSerializationTimeMs = serializeClock.mean(),
            meanDeserializationTimeMs = deserializeClock.mean(),
        )
    }

    println("Finished!")

    return results
}

fun main() {
    val formats: List<Format> = listOf(
        JavaFormat(),
        XmlFormat(),
        JsonFormat(),
        AvroFormat(),
        ProtobufFormat(),
        YamlFormat(),
        MessagePackFormat(),
    )

    val data = DataGeneration.generateClientData()

    val results = benchmark(data, formats)

    results.forEach { result ->
        println(result)
    }
}
