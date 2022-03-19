package me.darkkeks.soa.mafia.server

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import io.grpc.ServerBuilder
import io.grpc.Status
import io.grpc.StatusRuntimeException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import me.darkkeks.soa.mafia.model.ChatRequest
import me.darkkeks.soa.mafia.model.ChatResponse
import me.darkkeks.soa.mafia.model.CheckRequest
import me.darkkeks.soa.mafia.model.CheckResponse
import me.darkkeks.soa.mafia.model.EndDayRequest
import me.darkkeks.soa.mafia.model.EndDayResponse
import me.darkkeks.soa.mafia.model.GameEndUpdate
import me.darkkeks.soa.mafia.model.GameRole
import me.darkkeks.soa.mafia.model.GameUpdate
import me.darkkeks.soa.mafia.model.LoginInfo
import me.darkkeks.soa.mafia.model.MafiaServerGrpcKt
import me.darkkeks.soa.mafia.model.PublishRequest
import me.darkkeks.soa.mafia.model.PublishResponse
import me.darkkeks.soa.mafia.model.StateUpdate
import me.darkkeks.soa.mafia.model.VoteRequest
import me.darkkeks.soa.mafia.model.VoteResponse
import me.darkkeks.soa.mafia.model.chatResponse
import me.darkkeks.soa.mafia.model.chatUpdate
import me.darkkeks.soa.mafia.model.checkResponse
import me.darkkeks.soa.mafia.model.endDayResponse
import me.darkkeks.soa.mafia.model.gameEndUpdate
import me.darkkeks.soa.mafia.model.gameUpdate
import me.darkkeks.soa.mafia.model.initUpdate
import me.darkkeks.soa.mafia.model.publishResponse
import me.darkkeks.soa.mafia.model.sessionUpdate
import me.darkkeks.soa.mafia.model.stateUpdate
import me.darkkeks.soa.mafia.model.voteResponse
import org.slf4j.LoggerFactory
import java.util.UUID


data class Player(
    val login: String,
    val authToken: String = UUID.randomUUID().toString(),

    val flow: MutableSharedFlow<GameUpdate>,

    var role: GameRole? = null,
    var dead: Boolean = false,
)

data class GameConfig(
    val playersPerSession: Int = 4,
    val mafiaCount: Int = 1,
    val detectiveCount: Int = 1,
)

enum class SessionState {
    DAY, NIGHT,
}

class MafiaSession(
    val gameConfig: GameConfig,
    val players: List<Player>,
) {
    private val endVotes: MutableSet<String> = mutableSetOf()
    private val currentVotes: MutableMap<String, String> = mutableMapOf()
    private var detectiveChecked: String? = null

    private var state: SessionState = SessionState.DAY
    private var day: Int = 0

    suspend fun start() {
        val logins = players.map { it.login }

        pickRoles()

        players.forEach { player ->
            player.flow.emit(gameUpdate {
                sessionUpdate = sessionUpdate {
                    role = player.role!!
                    players.addAll(logins)
                }
            })
        }
    }

    private fun pickRoles() {
        val shuffledPlayers = players.shuffled()

        val detectives = shuffledPlayers.take(gameConfig.detectiveCount)
        val mafia = shuffledPlayers.take(gameConfig.mafiaCount)

        detectives.forEach { it.role = GameRole.DETECTIVE }
        mafia.forEach { it.role = GameRole.MAFIA }
        shuffledPlayers.forEach { it.role = GameRole.CIVILIAN }
    }

    private suspend fun endDay() {
        val toKill: String? = getVictim()

        currentVotes.clear()
        endVotes.clear()
        detectiveChecked = null
        state = SessionState.NIGHT

        if (toKill != null) {
            players.forEach { player ->
                if (player.login == toKill) {
                    player.dead = true
                }
            }
        }

        if (checkWin()) {
            return
        }

        val gameUpdate = gameUpdate {
            stateUpdate {
                state = StateUpdate.GameState.NIGHT
                if (toKill != null) {
                    killed = killed
                }
            }
        }

        players.forEach { player ->
            player.flow.emit(gameUpdate)
        }
    }

    private suspend fun checkWin(): Boolean {
        val mafiaCount = players
            .count { it.role == GameRole.MAFIA && !it.dead }
        val civiliansCount = players
            .count { it.role != GameRole.MAFIA && !it.dead }

        if (mafiaCount == 0) {
            val gameUpdate = gameUpdate {
                gameEndUpdate = gameEndUpdate {
                    winner = GameEndUpdate.Winner.CIVILIANS
                }
            }
            players.forEach { player ->
                player.flow.emit(gameUpdate)
            }
            return true
        }

        if (mafiaCount == civiliansCount) {
            val gameUpdate = gameUpdate {
                gameEndUpdate = gameEndUpdate {
                    winner = GameEndUpdate.Winner.MAFIA
                }
            }
            players.forEach { player ->
                player.flow.emit(gameUpdate)
            }
            return true
        }

        return false
    }

    private fun getVictim(): String? {
        val votes = currentVotes.values.groupingBy { it }.eachCount()
        val maxVotes = votes.values.maxOrNull()
        val maxVoteCount = votes.values.count { it == maxVotes }

        return if (maxVoteCount == 1) {
            votes.filterValues { it == maxVotes }.keys.first()
        } else {
            null
        }
    }

    private suspend fun checkEndNight() {
        val shouldEndNight = players.all { player ->
            when (player.role) {
                GameRole.MAFIA -> player.dead || player.login in currentVotes
                GameRole.DETECTIVE -> player.dead || detectiveChecked != null
                else -> true
            }
        }

        if (!shouldEndNight) {
            return
        }

        val toKill: String? = getVictim()

        currentVotes.clear()
        ++day
        state = SessionState.DAY

        if (toKill != null) {
            players.forEach { player ->
                if (player.login == toKill) {
                    player.dead = true
                }
            }
        }

        if (checkWin()) {
            return
        }

        val gameUpdate = gameUpdate {
            stateUpdate {
                state = StateUpdate.GameState.DAY
                if (toKill != null) {
                    killed = killed
                }
            }
        }

        players.forEach { player ->
            player.flow.emit(gameUpdate)
        }
    }

    suspend fun endDay(player: Player): EndDayResponse {
        if (player.login in endVotes) {
            return endDayResponse { error = EndDayResponse.EndDayErrorType.NOT_ALLOWED }
        }
        endVotes += player.login
        if (endVotes.size == players.size) {
            endDay()
        }
        return endDayResponse { }
    }

    suspend fun publishMafia(player: Player): PublishResponse {
        if (player.role != GameRole.DETECTIVE) {
            return publishResponse { error = PublishResponse.PublishErrorType.NOT_ALLOWED }
        }
        if (detectiveChecked == null || state != SessionState.DAY) {
            return publishResponse { error = PublishResponse.PublishErrorType.NOT_ALLOWED }
        }
        val targetPlayer = players.first { it.login == detectiveChecked }
        players.forEach { other ->
            other.flow.emit(gameUpdate {
                chatUpdate {
                    from = player.login
                    text = "Player $detectiveChecked has role ${targetPlayer.role}"
                }
            })
        }
        return publishResponse { }
    }

    suspend fun sendMessage(player: Player, toSend: String): ChatResponse {
        if (state != SessionState.DAY) {
            return chatResponse { error = ChatResponse.ChatErrorType.NOT_ALLOWED }
        }
        players.forEach { other ->
            other.flow.emit(gameUpdate {
                chatUpdate = chatUpdate {
                    from = player.login
                    text = toSend
                }
            })
        }
        return chatResponse { }
    }

    suspend fun check(player: Player, target: String): CheckResponse {
        if (player.role != GameRole.DETECTIVE) {
            return checkResponse { error = CheckResponse.CheckErrorType.NOT_IN_A_GAME }
        }
        if (players.none { it.login == target }) {
            return checkResponse { error = CheckResponse.CheckErrorType.NOT_IN_A_GAME }
        }
        if (state != SessionState.NIGHT || detectiveChecked != null) {
            return checkResponse { error = CheckResponse.CheckErrorType.NOT_IN_A_GAME }
        }
        detectiveChecked = target
        val targetPlayer = players.first { it.login == target }
        return checkResponse { role = targetPlayer.role!! }
    }

    suspend fun vote(player: Player, target: String): VoteResponse {
        if (players.none { it.login == target }) {
            return voteResponse { error = VoteResponse.VoteErrorType.INVALID_TARGET }
        }
        if (state == SessionState.DAY) {
            if (day == 0) {
                return voteResponse { error = VoteResponse.VoteErrorType.NOT_IN_A_GAME }
            }
            if (player.login in currentVotes) {
                return voteResponse { error = VoteResponse.VoteErrorType.ALREADY_VOTED }
            }
        } else {
            if (player.role != GameRole.MAFIA) {
                return voteResponse { error = VoteResponse.VoteErrorType.NOT_IN_A_GAME }
            }
            val targetPlayer = players.first { it.login == target }
            if (targetPlayer.role == GameRole.MAFIA) {
                return voteResponse { error = VoteResponse.VoteErrorType.NOT_IN_A_GAME }
            }
        }
        currentVotes[player.login] = target
        if (state == SessionState.NIGHT) {
            checkEndNight()
        }
        return voteResponse { }
    }
}

class MafiaServer(
    private val gameConfig: GameConfig = GameConfig(),
) {
    private val logger = LoggerFactory.getLogger(MafiaServer::class.java)

    private val players: MutableSet<Player> = mutableSetOf()
    private val queue: MutableList<Player> = mutableListOf()
    private val sessions: MutableSet<MafiaSession> = mutableSetOf()

    private val serverScope = CoroutineScope(CoroutineExceptionHandler { _, throwable ->
        logger.error("Unhandled exception in server coroutine scope", throwable)
    })

    fun connect(login: String): Flow<GameUpdate> {
        val exists = players.any { it.login == login }
        if (exists) {
            throw StatusRuntimeException(Status.INVALID_ARGUMENT)
        }

        val player = Player(
            login = login,
            flow = MutableSharedFlow(),
        )

        players += player
        queue += player

        logger.info("[${player.login}] Connected")

        serverScope.launch {
            sendInitUpdate(player)

            while (queue.size >= gameConfig.playersPerSession) {
                startSession()
            }
        }

        return player.flow
    }

    private suspend fun sendInitUpdate(player: Player) {
        val logins: List<String> = players.map { it.login }

        player.flow.emit(gameUpdate {
            initUpdate = initUpdate {
                players.addAll(logins)
                token = player.authToken
            }
        })
    }

    private suspend fun startSession() {
        val session = MafiaSession(
            gameConfig = gameConfig,
            players = queue.take(gameConfig.playersPerSession),
        )

        sessions += session
        session.start()
    }

    private fun currentPlayer(token: String): Player? {
        return players.firstOrNull { it.authToken == token }
    }

    private fun sessionForPlayer(player: Player): MafiaSession? {
        return sessions.firstOrNull { session ->
            session.players.any { it.login == player.login }
        }
    }

    suspend fun endDay(request: EndDayRequest): EndDayResponse {
        val player = currentPlayer(request.token) ?: return endDayResponse {
            error = EndDayResponse.EndDayErrorType.NOT_AUTHORIZED
        }
        val session =
            sessionForPlayer(player) ?: return endDayResponse { error = EndDayResponse.EndDayErrorType.NOT_IN_A_GAME }
        return session.endDay(player)
    }

    suspend fun publishMafia(request: PublishRequest): PublishResponse {
        val player = currentPlayer(request.token) ?: return publishResponse {
            error = PublishResponse.PublishErrorType.NOT_AUTHORIZED
        }
        val session = sessionForPlayer(player) ?: return publishResponse {
            error = PublishResponse.PublishErrorType.NOT_IN_A_GAME
        }
        return session.publishMafia(player)
    }

    suspend fun sendMessage(request: ChatRequest): ChatResponse {
        val player =
            currentPlayer(request.token) ?: return chatResponse { error = ChatResponse.ChatErrorType.NOT_AUTHORIZED }
        val session =
            sessionForPlayer(player) ?: return chatResponse { error = ChatResponse.ChatErrorType.NOT_IN_A_GAME }
        return session.sendMessage(player, request.text)
    }

    suspend fun check(request: CheckRequest): CheckResponse {
        val player =
            currentPlayer(request.token) ?: return checkResponse { error = CheckResponse.CheckErrorType.NOT_AUTHORIZED }
        val session =
            sessionForPlayer(player) ?: return checkResponse { error = CheckResponse.CheckErrorType.NOT_IN_A_GAME }
        return session.check(player, request.target)
    }

    suspend fun vote(request: VoteRequest): VoteResponse {
        val player =
            currentPlayer(request.token) ?: return voteResponse { error = VoteResponse.VoteErrorType.NOT_AUTHORIZED }
        val session =
            sessionForPlayer(player) ?: return voteResponse { error = VoteResponse.VoteErrorType.NOT_IN_A_GAME }
        return session.vote(player, request.target)
    }
}

class MafiaServerImpl(
    private val mafiaServer: MafiaServer,
) : MafiaServerGrpcKt.MafiaServerCoroutineImplBase() {
    override fun connect(request: LoginInfo): Flow<GameUpdate> = mafiaServer.connect(request.login)
    override suspend fun endDay(request: EndDayRequest): EndDayResponse = mafiaServer.endDay(request)
    override suspend fun publishMafia(request: PublishRequest): PublishResponse = mafiaServer.publishMafia(request)
    override suspend fun sendMessage(request: ChatRequest): ChatResponse = mafiaServer.sendMessage(request)
    override suspend fun check(request: CheckRequest): CheckResponse = mafiaServer.check(request)
    override suspend fun vote(request: VoteRequest): VoteResponse = mafiaServer.vote(request)
}

class ServerApp : CliktCommand() {
    private val port: Int by option("-p", help = "Server port").int().default(1234)

    override fun run() {
        val mafiaServer = MafiaServer()
        val mafiaServerImpl = MafiaServerImpl(mafiaServer)

        val server = ServerBuilder.forPort(port).addService(mafiaServerImpl).build()

        server.start()
        println("Started mafia server on 0.0.0.0:$port")

        server.awaitTermination()
    }
}

fun main(vararg args: String) = ServerApp().main(args)
