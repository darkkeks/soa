package me.darkkeks.soa.graphql.server.service

import me.darkkeks.soa.graphql.server.model.GqGamePlayer
import me.darkkeks.soa.graphql.server.model.GqGameSelector
import me.darkkeks.soa.graphql.server.model.GqScoreboardUpdate
import org.springframework.stereotype.Component
import reactor.core.publisher.FluxSink
import java.util.concurrent.ConcurrentHashMap

@Component
class ScoreboardService(
    private val gameService: GameService,
) {
    private val subscribers: MutableMap<Long, MutableSet<FluxSink<GqScoreboardUpdate>>> = ConcurrentHashMap()

    fun addSubscription(gameId: Long, sink: FluxSink<GqScoreboardUpdate>) {
        sink.onDispose { removeFlux(gameId, sink) }
        addFlux(gameId, sink)

        val initialUpdate = getInitialUpdate(gameId)
            ?: throw IllegalArgumentException("Could not find the game")

        sink.next(initialUpdate)
    }

    private fun getInitialUpdate(gameId: Long): GqScoreboardUpdate? {
        val game = gameService.getGames(GqGameSelector(ids = listOf(gameId))).firstOrNull()
            ?: return null

        val players = gameService.getPlayers(game)

        return GqScoreboardUpdate(updates = players)
    }

    fun onScoreChange(gameId: Long, gamePlayer: GqGamePlayer) {
        val toNotify = subscribers[gameId]
            ?: return

        val update = GqScoreboardUpdate(updates = listOf(gamePlayer))
        toNotify.forEach { it.next(update) }
    }

    private fun addFlux(gameId: Long, sink: FluxSink<GqScoreboardUpdate>) {
        subscribers
            .computeIfAbsent(gameId) { ConcurrentHashMap.newKeySet() }
            .add(sink)
    }

    private fun removeFlux(gameId: Long, sink: FluxSink<GqScoreboardUpdate>) {
        subscribers[gameId]?.remove(sink)
    }
}
