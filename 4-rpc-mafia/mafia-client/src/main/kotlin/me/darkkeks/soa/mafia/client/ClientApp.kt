package me.darkkeks.soa.mafia.client

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.output.TermUi.prompt
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.runBlocking
import me.darkkeks.soa.mafia.model.GameUpdate
import me.darkkeks.soa.mafia.model.InitUpdate
import me.darkkeks.soa.mafia.model.MafiaServerGrpcKt
import me.darkkeks.soa.mafia.model.PlayerUpdate
import me.darkkeks.soa.mafia.model.loginInfo
import org.slf4j.LoggerFactory

class MafiaClient(
    private val mafiaServerStub: MafiaServerGrpcKt.MafiaServerCoroutineStub,
) {
    private val logger = LoggerFactory.getLogger(MafiaClient::class.java)

    suspend fun start() {
        val loginToUse: String = prompt("Enter login")
            ?: throw IllegalStateException("Login was not entered")

        val updateFlow = mafiaServerStub.connect(loginInfo {
            login = loginToUse
        })

        updateFlow.collect { update ->
            handleUpdate(update)
        }
    }

    private fun handleUpdate(update: GameUpdate) {
        logger.info("Got update: $update")
        when (update.updateCase) {
            GameUpdate.UpdateCase.INIT_UPDATE -> handleInit(update.initUpdate)
            GameUpdate.UpdateCase.PLAYER_UPDATE -> handlePlayer(update.playerUpdate)
            else -> throw IllegalStateException("Unknown update type: $update")
        }
    }

    private fun handleInit(initUpdate: InitUpdate) {
        logger.info("Players:")
        initUpdate.playersList.forEach { login ->
            logger.info("- $login")
        }
    }

    private fun handlePlayer(playerUpdate: PlayerUpdate) {
        if (playerUpdate.updateType == PlayerUpdate.Type.JOIN) {
            logger.info("[${playerUpdate.login}] Joined")
        } else {
            logger.info("[${playerUpdate.login}] Left")
        }
    }

}

class ClientApp : CliktCommand() {
    private val logger = LoggerFactory.getLogger(ClientApp::class.java)

    private val hostname: String by option("-h", help = "Server hostname").default("127.0.0.1")
    private val port: Int by option("-p", help = "Server port").int().default(1234)

    override fun run() {
        logger.info("Connecting to $hostname:$port")

        val channel = ManagedChannelBuilder
            .forAddress(hostname, port)
            .usePlaintext()
            .build()

        val mafiaServerStub = MafiaServerGrpcKt.MafiaServerCoroutineStub(channel)
        val client = MafiaClient(mafiaServerStub)

        runBlocking {
            client.start()
        }
    }
}

fun main(vararg args: String) = ClientApp().main(args)
