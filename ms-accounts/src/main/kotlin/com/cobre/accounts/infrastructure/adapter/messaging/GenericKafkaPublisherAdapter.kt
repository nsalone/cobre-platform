package com.cobre.accounts.infrastructure.adapter.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class GenericKafkaPublisherAdapter(
    private val kafkaTemplate: ReactiveKafkaProducerTemplate<String, Any>,
    private val objectMapper: ObjectMapper
) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun publish(topic: String, key: String, payload: String): Mono<Void> {
        val node = objectMapper.readTree(payload)
        return kafkaTemplate.send(topic, key, node)
            .doOnNext {
                log.info("[Kafka] Published message on topic=$topic key=$key")
            }
            .doOnError { ex ->
                log.error("Error published message to Kafka: ${ex.message}")
            }
            .then()
    }
}
