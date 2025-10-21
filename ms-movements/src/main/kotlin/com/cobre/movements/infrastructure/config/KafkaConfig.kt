package com.cobre.movements.infrastructure.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import reactor.kafka.sender.SenderOptions

@Configuration
class KafkaConfig(var producerFactory: ProducerFactory<String, Any>) {

    @Bean
    fun reactiveKafkaProducerTemplate(): ReactiveKafkaProducerTemplate<String, Any> {
        val producerProperties = producerFactory.configurationProperties
        val senderOptions = SenderOptions.create<String, Any>(producerProperties)
        return ReactiveKafkaProducerTemplate(senderOptions)
    }
}