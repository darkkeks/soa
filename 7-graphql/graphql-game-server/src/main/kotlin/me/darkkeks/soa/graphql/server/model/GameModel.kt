package me.darkkeks.soa.graphql.server.model

import io.leangen.graphql.annotations.GraphQLNonNull
import me.darkkeks.soa.common.model.GameRole
import me.darkkeks.soa.common.model.GameStatus
import java.time.LocalDateTime

data class GqAddGame(
    val status: @GraphQLNonNull GameStatus,
    val players: @GraphQLNonNull List<GqGamePlayer>,
)

data class GqGameSelector(
    val ids: List<Long>? = null,
    val status: GameStatus? = null,
)

data class GqGame(
    val id: @GraphQLNonNull Long,
    val status: @GraphQLNonNull GameStatus,
)

data class GqGamePlayer(
    val name: @GraphQLNonNull String,
    val role: @GraphQLNonNull GameRole,
    val score: @GraphQLNonNull Long,
)

data class GqGameComment(
    val author: @GraphQLNonNull String,
    val content: @GraphQLNonNull String,
    val createdAt: @GraphQLNonNull LocalDateTime,
)

data class GqAddComment(
    val gameId: @GraphQLNonNull Long,
    val author: @GraphQLNonNull String,
    val content: @GraphQLNonNull String,
)

data class GqUpdateScore(
    val gameId: @GraphQLNonNull Long,
    val name: @GraphQLNonNull String,
    val score: @GraphQLNonNull Long,
)
