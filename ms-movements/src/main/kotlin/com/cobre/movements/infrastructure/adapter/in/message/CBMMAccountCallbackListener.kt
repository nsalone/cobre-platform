package com.cobre.movements.infrastructure.adapter.`in`.message

import com.cobre.movements.domain.model.event.CrossBorderMoneyMovementEvent
import com.cobre.movements.infrastructure.adapter.out.message.Listener
import com.cobre.movements.infrastructure.persistence.CBMMSagaRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
@KafkaListener(topics = ["\${topic.cross_border_money_movement.consumer.complete.v1}",
    "\${topic.cross_border_money_movement.consumer.failed.v1}"]
    , groupId = "movements-cbmm-group")
class CBMMAccountCallbackListener(
    val sagaRepo: CBMMSagaRepository,
    objectMapper: ObjectMapper
) : Listener<CrossBorderMoneyMovementEvent>(sagaRepo, objectMapper) {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun process(topic: String, message: String, content: CrossBorderMoneyMovementEvent) {
        when (topic) {
            "cross_border_money_movement.completed.v1" -> {
                log.info("Transfer completed: {}", content.eventId)
                sagaRepo.updateStatus(content.eventId, "COMPLETED").subscribe()
            }
            "cross_border_money_movement.failed.v1" -> {
                log.warn("Transfer fail: {}", content.eventId)
                sagaRepo.updateStatus(content.eventId, "FAILED").subscribe()
            }
            else -> log.debug("Event unknown: {}", content.eventType)
        }
    }

    override fun contentClass(): Class<CrossBorderMoneyMovementEvent> =
        CrossBorderMoneyMovementEvent::class.java
}