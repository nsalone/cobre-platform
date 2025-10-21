package com.cobre.movements.infrastructure.adapter.out.message

import com.cobre.movements.infrastructure.persistence.CBMMSagaRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaHandler
import reactor.core.publisher.Mono

/**
 * Clase abstracta base para listeners de Kafka.
 */
abstract class Listener<T>(
    private val sagaRepo: CBMMSagaRepository,
    private val objectMapper: ObjectMapper
) {

    private val log = LoggerFactory.getLogger(Listener::class.java)

    @KafkaHandler(isDefault = true)
    fun onMessage(message: String, record: ConsumerRecord<String, String>): Mono<Void> {
        log.info("Received message: {}", message)
        val eventId = record.key()

        return sagaRepo.existsByEventId(eventId)
            .flatMap { exists ->
                if (!exists) {
                    log.info("Event $eventId not found, skipping")
                    Mono.empty()
                } else {
                    Mono.fromCallable {
                        val content: T = objectMapper.readValue(message, contentClass())
                        process(record.topic(), message, content)
                    }.then()
                }
            }
            .onErrorResume { ex ->
                log.error("Error processing message Kafka $eventId: ${ex.message}", ex)
                sagaRepo.updateStatus(eventId, "FAILED").then()
            }
    }

    protected open fun extractEventIdFromRaw(raw: String): String {
        return try {
            val node = objectMapper.readTree(raw)
            node["event_id"]?.asText() ?: "unknown"
        } catch (e: Exception) {
            "unknown"
        }
    }

    protected abstract fun process(topic: String, message: String, content: T)
    protected abstract fun contentClass(): Class<T>
}
