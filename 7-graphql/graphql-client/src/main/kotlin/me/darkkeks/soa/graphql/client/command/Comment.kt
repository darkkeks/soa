package me.darkkeks.soa.graphql.client.command

import com.apollographql.apollo3.ApolloClient
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import kotlinx.coroutines.runBlocking
import me.darkkeks.soa.graphql.client.model.PostCommentMutation
import me.darkkeks.soa.graphql.client.model.type.GqAddCommentInput

class Comment(
    private val client: ApolloClient,
) : CliktCommand(help = "Post game comment") {
    private val id by option(help = "Id of a game to add comment to").required()
    private val author by option(help = "Name to post comment as").required()
    private val comment by option(help = "Comment content").required()

    override fun run() = runBlocking {
        val result = client.mutation(
            PostCommentMutation(
                input = GqAddCommentInput(
                    gameId = id,
                    author = author,
                    content = comment,
                )
            )
        ).execute()

        if (result.hasErrors()) {
            val errors = result.errors
            println(errors)
            return@runBlocking
        }

        println("Comment id: ${result.data!!.postComment}")
    }
}
