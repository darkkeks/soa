package me.darkkeks.soa.graphql.server.model

import io.leangen.graphql.annotations.GraphQLNonNull

data class GqScoreboardUpdate(
    val updates: @GraphQLNonNull List<@GraphQLNonNull GqGamePlayer>,
)
