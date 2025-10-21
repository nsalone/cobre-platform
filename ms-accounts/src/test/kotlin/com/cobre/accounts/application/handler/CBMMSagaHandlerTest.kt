package com.cobre.accounts.application.handler

import com.cobre.accounts.application.usecase.ApplyTransferUseCase
import com.cobre.accounts.domain.model.AccountEvent
import com.cobre.accounts.domain.model.CrossBorderMoneyMovementEvent
import com.cobre.accounts.infrastructure.persistence.CBMMSagaRepository
import com.cobre.accounts.infrastructure.persistence.OutboxEventRepository
import com.cobre.accounts.infrastructure.persistence.entity.OutboxEventEntity
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.math.BigDecimal
import java.time.Instant

class CBMMSagaHandlerTest {

    private val applyTransferUseCase: ApplyTransferUseCase = mockk()
    private val sagaRepo: CBMMSagaRepository = mockk()
    private val outboxRepo: OutboxEventRepository = mockk()
    private val objectMapper: ObjectMapper = ObjectMapper().registerModule(JavaTimeModule())

    private lateinit var handler: CBMMSagaHandler

    @BeforeEach
    fun setup() {
        handler = CBMMSagaHandler(
            complete = "cbmm.complete.v1",
            failed = "cbmm.failed.v1",
            applyTransferUseCase = applyTransferUseCase,
            sagaRepo = sagaRepo,
            outboxRepo = outboxRepo,
            objectMapper = objectMapper
        )
    }

    private fun buildEvent() = CrossBorderMoneyMovementEvent(
        eventId = "event-123",
        eventType = "cross_border_money_movement",
        operationDate = Instant.now(),
        origin = AccountEvent(
            accountId = "A1",
            currency = "USD",
            amount = BigDecimal("10")
        ),
        destination = AccountEvent(
            accountId = "A2",
            currency = "ARS",
            amount = BigDecimal("15000")
        )
    )

    @Test
    fun `should complete saga successfully`() {
        val event = buildEvent()

        every {
            applyTransferUseCase.execute(
                event.origin.accountId,
                event.destination.accountId,
                event.origin.amount,
                event.destination.amount,
                event.origin.currency,
                event.destination.currency,
                event.eventId
            )
        } returns Mono.empty()

        every { sagaRepo.updateStatus(event.eventId, "COMPLETED") } returns Mono.empty()
        every { outboxRepo.save(any<OutboxEventEntity>()) } returns Mono.just(mockk())

        StepVerifier.create(handler.handle(event))
            .verifyComplete()

        verify(exactly = 1) { sagaRepo.updateStatus(event.eventId, "COMPLETED") }
        verify(exactly = 1) {
            outboxRepo.save(match {
                it.eventType == "cbmm.complete.v1" &&
                        it.aggregateId == event.eventId &&
                        it.payload.contains("cross_border_money_movement")
            })
        }
        verify(exactly = 1) { applyTransferUseCase.execute(any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `should mark saga as failed when applyTransferUseCase throws error`() {
        val event = buildEvent()

        every {
            applyTransferUseCase.execute(
                any(), any(), any(), any(), any(), any(), any()
            )
        } returns Mono.error(RuntimeException("Transfer failed"))

        every { sagaRepo.updateStatus(event.eventId, any()) } returns Mono.empty()
        every { outboxRepo.save(any<OutboxEventEntity>()) } returns Mono.just(mockk())

        StepVerifier.create(handler.handle(event))
            .verifyComplete()

        verify(exactly = 1) { sagaRepo.updateStatus(event.eventId, "FAILED") }
        verify(exactly = 1) {
            outboxRepo.save(match {
                it.eventType == "cbmm.failed.v1" &&
                        it.aggregateId == event.eventId &&
                        it.payload.contains("Transfer failed")
            })
        }
    }

}
