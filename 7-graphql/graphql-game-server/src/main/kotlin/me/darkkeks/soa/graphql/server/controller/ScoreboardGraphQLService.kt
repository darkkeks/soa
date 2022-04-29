package me.darkkeks.soa.graphql.server.controller

import io.leangen.graphql.annotations.GraphQLNonNull
import io.leangen.graphql.annotations.GraphQLSubscription
import io.leangen.graphql.spqr.spring.annotations.GraphQLApi
import me.darkkeks.soa.graphql.server.model.GqScoreboardUpdate
import me.darkkeks.soa.graphql.server.service.ScoreboardService
import org.reactivestreams.Publisher
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux

@Component
@GraphQLApi
class ScoreboardGraphQLService(
    private val scoreboardService: ScoreboardService,
) {
    @GraphQLSubscription(name = "scoreboard")
    fun scoreboard(gameId: @GraphQLNonNull Long): Publisher<@GraphQLNonNull GqScoreboardUpdate> {
        return Flux.create { sink ->
            scoreboardService.addSubscription(gameId, sink)
        }
    }
}

