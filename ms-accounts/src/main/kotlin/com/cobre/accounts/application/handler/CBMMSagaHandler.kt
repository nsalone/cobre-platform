package com.cobre.accounts.application.handler

import com.cobre.accounts.application.usecase.ApplyTransferUseCase
import com.cobre.accounts.domain.model.CrossBorderMoneyMovementEvent
import com.cobre.accounts.infrastructure.persistence.CBMMSagaRepository
import com.cobre.accounts.infrastructure.persistence.OutboxEventRepository
import com.cobre.accounts.infrastructure.persistence.entity.OutboxEventEntity
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class CBMMSagaHandler(
    @Value("\${topic.cross_border_money_movement.complete.v1}") private val complete: String,
    @Value("\${topic.cross_border_money_movement.failed.v1}") private val failed: String,
    private val applyTransferUseCase: ApplyTransferUseCase,
    private val sagaRepo: CBMMSagaRepository,
    private val outboxRepo: OutboxEventRepository,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun handle(event: CrossBorderMoneyMovementEvent): Mono<Void> {
        return applyTransferUseCase.execute(
            originAccountId = event.origin.accountId,
            destinationAccountId = event.destination.accountId,
            originAmount = event.origin.amount,
            destinationAmount = event.destination.amount,
            currencyOrigin = event.origin.currency,
            currencyDestination = event.destination.currency,
            eventId = event.eventId
        ).then(
            sagaRepo.updateStatus(event.eventId, "COMPLETED")
                .then(
                    outboxRepo.save(
                        OutboxEventEntity(
                            eventType = complete,
                            aggregateId = event.eventId,
                            payload = objectMapper.writeValueAsString(event)
                        )
                    ).then()
                )
        ).doOnSuccess {
            log.info("Saga local transfer completed on eventId={}", event.eventId)
        }.onErrorResume { ex ->
            log.error("Error on transfer/register saga", ex)
            sagaRepo.updateStatus(event.eventId, "FAILED")
                .then(
                    outboxRepo.save(
                        OutboxEventEntity(
                            eventType = failed,
                            aggregateId = event.eventId,
                            payload = objectMapper.writeValueAsString(
                                mapOf(
                                    "event_id" to event.eventId,
                                    "reason" to (ex.message ?: "unknown")
                                )
                            )
                        )
                    ).then()
                )
        }
    }
}