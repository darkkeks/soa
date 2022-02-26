import com.fasterxml.jackson.databind.ObjectMapper

class BenchmarkClock {
    private val iterations: MutableList<Long> = mutableListOf()

    private var currentStart: Long? = null

    fun run(block: () -> Unit) {
        start()
        block()
        end()
    }

    private fun start() {
        check(currentStart == null) { "clock is already started" }
        currentStart = System.currentTimeMillis()
    }

    private fun end() {
        val start = checkNotNull(currentStart) { "clock was not started" }
        iterations += System.currentTimeMillis() - start
        currentStart = null
    }

    fun mean(): Double = iterations.average()
}

data class BenchmarkResult(
    val name: String,
    val dataSize: Int,
    val meanSerializationTimeMs: Double,
    val meanDeserializationTimeMs: Double,
)

fun benchmark(
    data: ClientsData,
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
    val data = Generate.generateClientData()
    val results = benchmark(data)

    results.forEach { result ->
        println(result)
    }
}
