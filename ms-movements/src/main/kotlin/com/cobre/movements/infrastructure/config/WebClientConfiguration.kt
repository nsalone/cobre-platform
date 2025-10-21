package com.cobre.movements.infrastructure.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import java.time.Duration

@Configuration
class WebClientConfiguration() {

    @Bean
    fun webClient(): WebClient {
        val provider = ConnectionProvider.builder("quote-api")
            .maxConnections(50)
            .metrics(true)
            .build()
        val client = HttpClient.create(provider)
            .responseTimeout(Duration.ofSeconds(60))
            .keepAlive(false)
        return WebClient.builder()
            .clientConnector(ReactorClientHttpConnector(client))
            .build()
    }

}