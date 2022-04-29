package me.darkkeks.soa.graphql.client.command

import com.apollographql.apollo3.ApolloClient
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.int
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import me.darkkeks.soa.graphql.client.model.GetScoreboardSubscription

class Scoreboard(
    private val client: ApolloClient,
) : CliktCommand(help = "View game scoreboard") {

    val id by option(help = "Game id").int().required()

    override fun run() = runBlocking {
        val flow = client.subscription(GetScoreboardSubscription(gameId = id)).toFlow()
        flow.collect { response ->
            if (response.hasErrors()) {
                println(response.errors)
                throw IllegalStateException()
            }

            val updates = response.data!!.scoreboard.updates
            updates.forEach { update ->
                println(" - Name: ${update.name}, role: ${update.role}, score: ${update.score}")
            }
        }
    }
}
