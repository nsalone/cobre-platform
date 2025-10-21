package com.cobre.accounts.infrastructure.adapter.messaging

import com.cobre.accounts.application.handler.CBMMSagaHandler
import com.cobre.accounts.domain.model.CrossBorderMoneyMovementEvent
import com.cobre.accounts.infrastructure.persistence.CBMMSagaRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
@KafkaListener(
    topics = ["\${topic.cross_border_money_movement.v1}"],
    groupId = "\${spring.kafka.consumer.group-id}"
)
class CBMMKafkaListener(
    sagaRepo: CBMMSagaRepository,
    objectMapper: ObjectMapper,
    val sagaHandler: CBMMSagaHandler
) : Listener<CrossBorderMoneyMovementEvent>(sagaRepo, objectMapper) {

    private val log = LoggerFactory.getLogger(CBMMKafkaListener::class.java)

    override fun process(topic: String, message: String, content: CrossBorderMoneyMovementEvent) {
        log.info("Event receive: {}", content.eventId)
        sagaHandler.handle(content).subscribe()
    }

    override fun contentClass(): Class<CrossBorderMoneyMovementEvent> =
        CrossBorderMoneyMovementEvent::class.java
}
