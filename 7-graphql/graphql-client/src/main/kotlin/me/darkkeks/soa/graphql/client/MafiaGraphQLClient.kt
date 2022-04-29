package me.darkkeks.soa.graphql.client

import com.apollographql.apollo3.ApolloClient
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import me.darkkeks.soa.graphql.client.command.Comment
import me.darkkeks.soa.graphql.client.command.Games
import me.darkkeks.soa.graphql.client.command.Scoreboard

class MafiaGraphQLClientApp : CliktCommand() {
    override fun run() = Unit
}

fun main(args: Array<String>) {
    val client = ApolloClient.Builder()
        .serverUrl("http://localhost:8080/graphql")
        .build()

    try {
        MafiaGraphQLClientApp()
            .subcommands(
                Games(client),
                Comment(client),
                Scoreboard(client),
            )
            .main(args)
    } finally {
        client.dispose()
    }
}
