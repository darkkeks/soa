package server

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import kotlinx.coroutines.runBlocking
import protocol.ProtocolConstants

class ServerApp : CliktCommand() {

    private val hostname: String by option("-h", help = "Server hostname").default("0.0.0.0")
    private val port: Int by option("-p", help = "Server port").int().default(ProtocolConstants.defaultPort)

    override fun run() = runBlocking {
        ChatServer().start(hostname, port)
    }
}

fun main(vararg args: String): Unit = ServerApp().main(args)

