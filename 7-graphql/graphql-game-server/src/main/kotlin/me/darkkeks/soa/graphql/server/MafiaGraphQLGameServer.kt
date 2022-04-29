package me.darkkeks.soa.graphql.server

import me.darkkeks.soa.common.CommonConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import

@SpringBootApplication
@Import(CommonConfiguration::class)
class MafiaGraphQLGameServer

fun main(args: Array<String>) {
    runApplication<MafiaGraphQLGameServer>(*args)
}
