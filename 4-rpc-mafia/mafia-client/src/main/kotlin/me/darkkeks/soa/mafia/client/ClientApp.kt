package me.darkkeks.soa.mafia.client

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.output.TermUi.prompt
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.darkkeks.soa.mafia.model.ChatUpdate
import me.darkkeks.soa.mafia.model.GameEndUpdate
import me.darkkeks.soa.mafia.model.GameUpdate
import me.darkkeks.soa.mafia.model.InitUpdate
import me.darkkeks.soa.mafia.model.MafiaServerGrpcKt
import me.darkkeks.soa.mafia.model.PlayerUpdate
import me.darkkeks.soa.mafia.model.SessionUpdate
import me.darkkeks.soa.mafia.model.StateUpdate
import me.darkkeks.soa.mafia.model.chatRequest
import me.darkkeks.soa.mafia.model.checkRequest
import me.darkkeks.soa.mafia.model.endDayRequest
import me.darkkeks.soa.mafia.model.loginInfo
import me.darkkeks.soa.mafia.model.publishRequest
import me.darkkeks.soa.mafia.model.voteRequest
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

class MafiaClient(
    private val mafiaServerStub: MafiaServerGrpcKt.MafiaServerCoroutineStub,
) {
    private val logger = LoggerFactory.getLogger(MafiaClient::class.java)

    private var token: String? = null
    private var isDead: Boolean = false

    suspend fun start() = coroutineScope {
        val loginToUse: String = prompt("Enter login")
            ?: throw IllegalStateException("Login was not entered")

        val updateFlow = mafiaServerStub.connect(loginInfo {
            login = loginToUse
        })


        launch {
            updateFlow.collect { update ->
                handleUpdate(update)
            }
        }

        while (true) {
            val command = prompt("")
                ?: continue
            when (command) {
                "end" -> {
                    mafiaServerStub.endDay(endDayRequest {
                        token = this@MafiaClient.token!!
                    })
                }
                "vote" -> {
                    val voteTarget = prompt("Who to vote for")
                        ?: continue

                    mafiaServerStub.vote(voteRequest {
                        token = this@MafiaClient.token!!
                        target = voteTarget
                    })
                }
                "check" -> {
                    val checkTarget = prompt("Who to check")
                        ?: continue

                    mafiaServerStub.check(checkRequest {
                        token = this@MafiaClient.token!!
                        target = checkTarget
                    })
                }
                "publish" -> {
                    mafiaServerStub.publishMafia(publishRequest {
                        token = this@MafiaClient.token!!
                    })
                }
                else -> {
                    mafiaServerStub.sendMessage(chatRequest {
                        token = this@MafiaClient.token!!
                        text = command
                    })
                }
            }

        }
    }

    private fun handleUpdate(update: GameUpdate) {
        logger.info("Got update: $update")
        when (update.updateCase) {
            GameUpdate.UpdateCase.INIT_UPDATE -> handleInit(update.initUpdate)
            GameUpdate.UpdateCase.PLAYER_UPDATE -> handlePlayer(update.playerUpdate)
            GameUpdate.UpdateCase.CHAT_UPDATE -> handleChat(update.chatUpdate)
            GameUpdate.UpdateCase.STATE_UPDATE -> handleState(update.stateUpdate)
            GameUpdate.UpdateCase.GAME_END_UPDATE -> handleGameEnd(update.gameEndUpdate)
            GameUpdate.UpdateCase.SESSION_UPDATE -> handleSession(update.sessionUpdate)
            else -> throw IllegalStateException("Unknown update type: $update")
        }
    }

    private fun handleInit(initUpdate: InitUpdate) {
        token = initUpdate.token

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

    private fun handleChat(chatUpdate: ChatUpdate) {
        logger.info("[${chatUpdate.from}] ${chatUpdate.text}")
    }

    private fun handleState(stateUpdate: StateUpdate) {
        if (stateUpdate.state == StateUpdate.GameState.DAY) {
            logger.info("It is day now, you can chat")
        } else {
            logger.info("It is night now, mafia and detective should cast their votes")
        }
        if (stateUpdate.killed != null) {
            logger.info("[${stateUpdate.killed}] Was killed")
        } else {
            logger.info("No one was killed")
        }
    }

    private fun handleGameEnd(gameEndUpdate: GameEndUpdate) {
        logger.info("Game ended! Winner: ${gameEndUpdate.winner}")
        exitProcess(0)
    }

    private fun handleSession(sessionUpdate: SessionUpdate) {
        logger.info("Starting a new session as a ${sessionUpdate.role}, players:")
        sessionUpdate.playersList.forEach { login ->
            logger.info("- $login")
        }

        logger.info("It is day now, you can chat")
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
