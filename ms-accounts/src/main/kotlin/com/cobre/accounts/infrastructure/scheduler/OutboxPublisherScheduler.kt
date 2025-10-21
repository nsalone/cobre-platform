package com.cobre.accounts.infrastructure.scheduler

import com.cobre.accounts.infrastructure.adapter.messaging.GenericKafkaPublisherAdapter
import com.cobre.accounts.infrastructure.persistence.OutboxEventRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class OutboxPublisherScheduler(
    private val outboxRepo: OutboxEventRepository,
    private val kafkaPublisher: GenericKafkaPublisherAdapter,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelay = 15000)
    fun publishPendingEvents() {
        outboxRepo.findPending()
            .flatMap { event ->
                kafkaPublisher.publish(event.eventType, event.aggregateId, event.payload)
                    .then(outboxRepo.updateStatus(event.id!!, "SENT"))
                    .doOnSuccess { log.info("Published ${event.eventType} with id=${event.id}") }
                    .onErrorResume { ex ->
                        log.error("Error on publish ${event.id}: ${ex.message}")
                        outboxRepo.incrementRetry(event.id)
                            .then(outboxRepo.updateStatus(event.id, "FAILED"))
                    }
            }
            .subscribe()
    }
}


