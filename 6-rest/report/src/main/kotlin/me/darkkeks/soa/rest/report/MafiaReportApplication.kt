package me.darkkeks.soa.rest.report

import me.darkkeks.soa.common.CommonConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import

@SpringBootApplication
@Import(CommonConfiguration::class)
class MafiaReportApplication

fun main(args: Array<String>) {
    runApplication<MafiaReportApplication>(*args)
}
