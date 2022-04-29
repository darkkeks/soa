package me.darkkeks.soa.graphql.server.controller

import io.leangen.graphql.annotations.GraphQLContext
import io.leangen.graphql.annotations.GraphQLMutation
import io.leangen.graphql.annotations.GraphQLNonNull
import io.leangen.graphql.annotations.GraphQLQuery
import io.leangen.graphql.spqr.spring.annotations.GraphQLApi
import me.darkkeks.soa.graphql.server.service.GameService
import me.darkkeks.soa.graphql.server.model.GqAddComment
import me.darkkeks.soa.graphql.server.model.GqAddGame
import me.darkkeks.soa.graphql.server.model.GqGame
import me.darkkeks.soa.graphql.server.model.GqGameComment
import me.darkkeks.soa.graphql.server.model.GqGamePlayer
import me.darkkeks.soa.graphql.server.model.GqGameSelector
import me.darkkeks.soa.graphql.server.model.GqUpdateScore
import me.darkkeks.soa.graphql.server.service.ScoreboardService
import org.springframework.stereotype.Component

@Component
@GraphQLApi
class GameGraphQLService(
    private val gameService: GameService,
    private val scoreboardService: ScoreboardService,
) {
    @GraphQLMutation(name = "addGame")
    fun addGame(addGame: GqAddGame): Long {
        return gameService.addGame(addGame)
    }

    @GraphQLQuery(name = "games")
    fun games(input: @GraphQLNonNull GqGameSelector): @GraphQLNonNull List<@GraphQLNonNull GqGame> {
        return gameService.getGames(input)
    }

    @GraphQLQuery(name = "players")
    fun players(@GraphQLContext game: GqGame): @GraphQLNonNull List<@GraphQLNonNull GqGamePlayer> {
        return gameService.getPlayers(game)
    }

    @GraphQLQuery(name = "comments")
    fun comments(@GraphQLContext game: GqGame): @GraphQLNonNull List<@GraphQLNonNull GqGameComment> {
        return gameService.getComments(game)
    }

    @GraphQLMutation(name = "postComment")
    fun postComment(addComment: @GraphQLNonNull GqAddComment): Long {
        return gameService.postComment(addComment)
    }

    @GraphQLMutation(name = "updateScore")
    fun updateScore(updateScore: @GraphQLNonNull GqUpdateScore): GqGamePlayer {
        val updated = gameService.updateScore(updateScore)
        scoreboardService.onScoreChange(updateScore.gameId, updated)
        return updated
    }
}
