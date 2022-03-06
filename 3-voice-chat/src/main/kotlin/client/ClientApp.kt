package client

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.int
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import protocol.ProtocolConstants

class ClientApp : CliktCommand() {

    private val logger = LoggerFactory.getLogger(ClientApp::class.java)

    private val hostname: String by option("-h", help = "Server hostname").default("0.0.0.0")
    private val port: Int by option("-p", help = "Server port").int().default(ProtocolConstants.defaultPort)

    private val login: String by option("-l", help = "Login").required()

    override fun run() = runBlocking {
        val client = ChatClient(login)

        launch {
            client.start(hostname, port)
            cancel()
        }

        while (true) {
            val command = withContext(Dispatchers.IO) {
                prompt(">", promptSuffix = " ")
            }

            if (command == null) {
                logger.info("Failed to read command")
                break
            }

            when (command) {
                "help" -> logger.info("""
                    Commands:
                      help - Show this message
                      show - Show current rooms
                      join [room] - Join room
                      toggle - Toggle is talking (off by default)
                      leave - Leave current room
                      quit - Quit voice chat
                """.trimIndent())

                "show" -> {
                    val rooms = client.getRooms()

                    if (rooms.rooms.isEmpty()) {
                        logger.info("No rooms")
                    } else {
                        var message = ""
                        rooms.rooms.forEach { room ->
                            message += "- Room: ${room.name}\n"
                            room.users.forEach { user ->
                                message += "  - [${if (user.talking) "x" else " "}] ${user.login}\n"
                            }
                        }
                        logger.info("Rooms:\n$message")
                    }
                }

                "join" -> {
                    val room: String? = withContext(Dispatchers.IO) {
                        prompt("Room name")
                    }

                    if (room == null) {
                        logger.info("Failed to read room name")
                        break
                    }

                    client.joinRoom(room)
                }

                "toggle" -> {
                    client.toggleTalking()
                }

                "leave" -> {
                    client.leaveRoom()
                }

                "quit" -> {
                    client.stop()
                    break
                }

                else -> logger.info("Invalid command: '$command'")
            }
        }
    }
}

fun main(argv: Array<String>): Unit = ClientApp().main(argv)
