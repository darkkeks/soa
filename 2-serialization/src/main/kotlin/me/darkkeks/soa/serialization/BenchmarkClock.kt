package me.darkkeks.soa.serialization

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
