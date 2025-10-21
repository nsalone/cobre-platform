package com.cobre.movements.application.usecase

import com.cobre.movements.domain.model.CBMMSaga
import com.cobre.movements.domain.model.event.CrossBorderMoneyMovementEvent
import com.cobre.movements.domain.model.event.MovementEndpoint
import com.cobre.movements.domain.port.out.cache.FxQuoteCachePort
import com.cobre.movements.domain.port.out.event.CBMMEventPublisherPort
import com.cobre.movements.domain.port.out.repository.CBMMSagaRepositoryPort
import com.cobre.movements.infrastructure.controllers.dto.CBMMRequest
import com.cobre.movements.infrastructure.exceptions.BusinessException
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

@Service
class CBMMRequestUseCase(
    private val sagaPort: CBMMSagaRepositoryPort,
    private val eventPublisher: CBMMEventPublisherPort,
    private val fxQuoteCachePort: FxQuoteCachePort,
    private val objectMapper: ObjectMapper
) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun requestTransfer(request: CBMMRequest): Mono<Void> {
        log.info("[requestTransfer]:: $request")
        return fxQuoteCachePort.getQuoteById(request.quoteId)
            .switchIfEmpty(Mono.error(BusinessException("Quote not found or expired", "QUOTE_NOT_FOUND")))
            .flatMap { quote ->
                val destinationAmount = request.amount.multiply(quote.rate)
                val event = CrossBorderMoneyMovementEvent(
                    eventId = UUID.randomUUID().toString(),
                    eventType = "cross_border_money_movement",
                    operationDate = Instant.now(),
                    origin = MovementEndpoint(
                        accountId = request.originAccount,
                        currency = quote.to,
                        amount = destinationAmount
                    ),
                    destination = MovementEndpoint(
                        accountId = request.destinationAccount,
                        currency = quote.from,
                        amount = request.amount
                    )
                )
                sagaPort.save(
                    CBMMSaga(
                        eventId = event.eventId,
                        cbmmStatus = "STARTED",
                        payload = objectMapper.writeValueAsString(event)
                    )
                ).then(eventPublisher.publishRequestedEvent(event))

            }
    }
}