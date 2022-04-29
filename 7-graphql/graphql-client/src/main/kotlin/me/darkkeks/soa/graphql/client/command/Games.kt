package me.darkkeks.soa.graphql.client.command

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.int
import kotlinx.coroutines.runBlocking
import me.darkkeks.soa.graphql.client.model.GetGamesQuery
import me.darkkeks.soa.graphql.client.model.type.GameStatus
import me.darkkeks.soa.graphql.client.model.type.GqGameSelectorInput

class Games(
    private val client: ApolloClient,
) : CliktCommand(
    help = "Show games",
) {
    object Status {
        const val ALL = "all"
        const val IN_PROGRESS = "in_progress"
        const val FINISHED = "finished"
    }

    private val status by option(help = "Find games with given status")
        .choice(Status.ALL, Status.IN_PROGRESS, Status.FINISHED, ignoreCase = true)
        .default(Status.IN_PROGRESS)

    private val id by option(help = "Ids of games to show")
        .int().multiple()

    override fun run() = runBlocking {
        val selector = when {
            id.isNotEmpty() -> {
                GqGameSelectorInput(ids = Optional.Present(id))
            }
            status != Status.ALL -> {
                GqGameSelectorInput(
                    status = Optional.Present(
                        if (status == Status.IN_PROGRESS) {
                            GameStatus.IN_PROGRESS
                        } else {
                            GameStatus.FINISHED
                        }
                    )
                )
            }
            else -> GqGameSelectorInput()
        }

        val gamesResponse = client.query(GetGamesQuery(input = selector)).execute()

        if (gamesResponse.hasErrors()) {
            val errors = gamesResponse.errors
            println(errors)
            return@runBlocking
        }

        val games: List<GetGamesQuery.Game> = gamesResponse.data!!.games
        for (game in games) {
            println("Game ${game.id} (status: ${game.status}):")
            println(" Players:")
            game.players.forEach { player ->
                println("  ${player.name} - role: ${player.role}, score: ${player.score}")
            }
            if (game.comments.isNotEmpty()) {
                println(" Comments:")
                game.comments.forEach { comment ->
                    println("  ${comment.author}: ${comment.content} (${comment.createdAt})")
                }
            }

            println()
        }

        client.dispose()
    }
}
