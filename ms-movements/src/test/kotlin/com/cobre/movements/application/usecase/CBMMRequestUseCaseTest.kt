package com.cobre.movements.application.usecase

import com.cobre.movements.domain.model.CBMMSaga
import com.cobre.movements.domain.model.FxQuote
import com.cobre.movements.domain.port.out.cache.FxQuoteCachePort
import com.cobre.movements.domain.port.out.event.CBMMEventPublisherPort
import com.cobre.movements.domain.port.out.repository.CBMMSagaRepositoryPort
import com.cobre.movements.infrastructure.controllers.dto.CBMMRequest
import com.cobre.movements.infrastructure.exceptions.BusinessException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.math.BigDecimal
import java.time.Instant
import java.util.*

class CBMMRequestUseCaseTest {

    private val sagaPort: CBMMSagaRepositoryPort = mockk()
    private val eventPublisher: CBMMEventPublisherPort = mockk()
    private val fxQuoteCachePort: FxQuoteCachePort = mockk()
    private val objectMapper: ObjectMapper = spyk(ObjectMapper())

    private lateinit var useCase: CBMMRequestUseCase

    @BeforeEach
    fun setup() {
        objectMapper.registerModule(JavaTimeModule()) // 👈 agrega soporte para Instant y LocalDateTime
        useCase = CBMMRequestUseCase(sagaPort, eventPublisher, fxQuoteCachePort, objectMapper)
    }

    @Test
    fun `should save saga and publish event successfully`() {
        // Given
        val quoteId = "quote-123"
        val request = CBMMRequest(
            quoteId = quoteId,
            originAccount = "ACC001",
            destinationAccount = "ACC999",
            amount = BigDecimal("100.00")
        )

        val quote = FxQuote(
            id = quoteId,
            rate = BigDecimal("1500.00"),
            from = "USD",
            to = "ARS",
            fetchedAt = Instant.now(),
            expiresAt = Instant.now().plusSeconds(60)
        )

        every { fxQuoteCachePort.getQuoteById(quoteId) } returns Mono.just(quote)
        every { sagaPort.save(any()) } returns Mono.just(CBMMSaga(UUID.randomUUID().toString(), "cross_border_money_movement", "STARTED", "{}"))
        every { eventPublisher.publishRequestedEvent(any()) } returns Mono.empty()

        // When & Then
        StepVerifier.create(useCase.requestTransfer(request))
            .verifyComplete()

        verify(exactly = 1) { fxQuoteCachePort.getQuoteById(quoteId) }
        verify(exactly = 1) { sagaPort.save(match {
            it.cbmmStatus == "STARTED" && it.payload.contains("cross_border_money_movement")
        }) }
        verify(exactly = 1) { eventPublisher.publishRequestedEvent(any()) }
    }

    @Test
    fun `should fail when quote not found`() {
        // Given
        val quoteId = "invalid-quote"
        val request = CBMMRequest(
            quoteId = quoteId,
            originAccount = "ACC001",
            destinationAccount = "ACC999",
            amount = BigDecimal("100.00")
        )

        every { fxQuoteCachePort.getQuoteById(quoteId) } returns Mono.empty()

        // When & Then
        StepVerifier.create(useCase.requestTransfer(request))
            .expectErrorSatisfies { ex ->
                assertTrue(ex is BusinessException)
                val businessEx = ex as BusinessException
                assertEquals("QUOTE_NOT_FOUND", businessEx.code)
                assertEquals("Quote not found or expired", businessEx.message)
            }
            .verify()

        verify(exactly = 1) { fxQuoteCachePort.getQuoteById(quoteId) }
        verify(exactly = 0) { sagaPort.save(any()) }
        verify(exactly = 0) { eventPublisher.publishRequestedEvent(any()) }
    }
}
