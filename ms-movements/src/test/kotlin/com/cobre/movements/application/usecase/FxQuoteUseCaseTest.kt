package com.cobre.movements.application.usecase

import com.cobre.movements.domain.model.FxQuote
import com.cobre.movements.domain.port.out.api.FxQuoteProviderPort
import com.cobre.movements.domain.port.out.cache.FxQuoteCachePort
import com.cobre.movements.infrastructure.controllers.dto.request.FxQuoteRequest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import io.mockk.*
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.math.BigDecimal
import java.time.Instant

class FxQuoteUseCaseTest {

    private val fxQuoteCachePort: FxQuoteCachePort = mockk()
    private val fxQuoteProviderPort: FxQuoteProviderPort = mockk()
    private lateinit var fxQuoteUseCase: FxQuoteUseCase

    @BeforeEach
    fun setup() {
        fxQuoteUseCase = FxQuoteUseCase(
            expire = "60", // segundos
            fxQuoteProviderPort = fxQuoteProviderPort,
            fxQuoteCachePort = fxQuoteCachePort
        )
    }

    @Test
    fun `should return quote from cache successfully`() {
        // Given
        val ori = "USD"
        val dest = "ARS"
        val request = FxQuoteRequest(
            originCurrency = ori,
            destinationCurrency = dest,
            amount = BigDecimal.valueOf(100.0)
        )

        val cachedQuote = FxQuote(
            id = "quote-123",
            rate = BigDecimal("1477.47"),
            from = ori,
            to = dest,
            fetchedAt = Instant.now(),
            expiresAt = Instant.now().plusSeconds(60)
        )

        every { fxQuoteCachePort.getQuoteByPair(ori, dest) } returns Mono.just(cachedQuote)

        // When & Then
        StepVerifier.create(fxQuoteUseCase.getQuote(request))
            .assertNext { response ->
                Assertions.assertEquals("quote-123", response.quoteId)
                Assertions.assertEquals(BigDecimal("1477.47"), response.rate)
                Assertions.assertEquals(
                    BigDecimal("147747.00").setScale(2), // 100 * 1477.47
                    response.amountConverted.setScale(2)
                )
                Assertions.assertEquals(dest, response.destinationCurrency)
                Assertions.assertEquals(ori, response.originCurrency)
            }
            .verifyComplete()

        verify(exactly = 1) { fxQuoteCachePort.getQuoteByPair(ori, dest) }
        verify(exactly = 0) { fxQuoteProviderPort.getQuote(any(), any()) }
    }

    @Test
    fun `should refresh quote when cache is empty`() {
        // Given
        val ori = "USD"
        val dest = "ARS"
        val request = FxQuoteRequest(
            originCurrency = ori,
            destinationCurrency = dest,
            amount = BigDecimal.valueOf(50.0)
        )

        val freshQuote = FxQuote(
            id = "fresh-999",
            rate = BigDecimal("1000.00"),
            from = ori,
            to = dest,
            fetchedAt = Instant.now(),
            expiresAt = Instant.now().plusSeconds(60)
        )

        every { fxQuoteCachePort.getQuoteByPair(ori, dest) } returns Mono.empty()
        every { fxQuoteProviderPort.getQuote(ori, dest) } returns Mono.just(freshQuote)
        every { fxQuoteCachePort.saveQuote(any()) } returns Mono.empty()

        // When & Then
        StepVerifier.create(fxQuoteUseCase.getQuote(request))
            .assertNext { response ->
                Assertions.assertEquals("fresh-999", response.quoteId)
                Assertions.assertEquals(BigDecimal("1000.00"), response.rate)
                Assertions.assertEquals(
                    BigDecimal("50000.00").setScale(2), // 50 * 1000
                    response.amountConverted.setScale(2)
                )
            }
            .verifyComplete()

        verify(exactly = 1) { fxQuoteCachePort.getQuoteByPair(ori, dest) }
        verify(exactly = 1) { fxQuoteProviderPort.getQuote(ori, dest) }
        verify(exactly = 1) { fxQuoteCachePort.saveQuote(any()) }
    }
}
