package com.cobre.movements.infrastructure.adapter.out.message

import com.cobre.movements.domain.model.event.CrossBorderMoneyMovementEvent
import com.cobre.movements.domain.port.out.event.CBMMEventPublisherPort
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class CBMMKafkaPublisherAdapter(
    @Value("\${topic.cross_border_money_movement.v1}") private val topic: String,
    private val kafkaTemplate: ReactiveKafkaProducerTemplate<String, Any>
) : CBMMEventPublisherPort {

    override fun publishRequestedEvent(event: CrossBorderMoneyMovementEvent): Mono<Void> {
        return kafkaTemplate.send(topic, event.eventId, event)
            .doOnNext { log.info("➡Send message to Kafka: ${event.eventId}") }
            .doOnError { ex -> log.error("Error send message: ${ex.message}") }
            .then()
    }

    companion object {
        private val log = LoggerFactory.getLogger(CBMMKafkaPublisherAdapter::class.java)
    }
}
