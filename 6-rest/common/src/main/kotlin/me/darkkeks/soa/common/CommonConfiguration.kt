package me.darkkeks.soa.common

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories

@Configuration
@ComponentScan
@EnableJdbcRepositories
class CommonConfiguration
