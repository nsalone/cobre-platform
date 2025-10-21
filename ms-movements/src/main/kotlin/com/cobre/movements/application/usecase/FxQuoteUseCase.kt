package com.cobre.movements.application.usecase

import com.cobre.movements.domain.model.FxQuote
import com.cobre.movements.domain.port.out.api.FxQuoteProviderPort
import com.cobre.movements.domain.port.out.cache.FxQuoteCachePort
import com.cobre.movements.infrastructure.controllers.dto.request.FxQuoteRequest
import com.cobre.movements.infrastructure.controllers.dto.response.FxQuoteResponse
import lombok.extern.slf4j.Slf4j
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.Instant

@Service
@Slf4j
class FxQuoteUseCase(
    @Value("\${quotation-api.expire-at}") private val expire: String,
    private val fxQuoteProviderPort: FxQuoteProviderPort,
    private val fxQuoteCachePort: FxQuoteCachePort
) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun getQuote(request: FxQuoteRequest): Mono<FxQuoteResponse> {
        log.debug("[getQuote]:: {}", request)
        return fetchOrRefresh(request.originCurrency, request.destinationCurrency)
            .map { quote ->
                FxQuoteResponse(
                    quoteId = quote.id,
                    originCurrency = request.originCurrency,
                    destinationCurrency = request.destinationCurrency,
                    rate = quote.rate,
                    amountConverted = request.amount.multiply(quote.rate),
                    expiresAt = quote.expiresAt
                )
            }
    }

    private fun fetchOrRefresh(ori: String, dest: String): Mono<FxQuote> =
        fxQuoteCachePort.getQuoteByPair(ori, dest)
            .doOnNext { log.info("💾 Cache hit para $ori->$dest") }
            .switchIfEmpty(
                Mono.defer {
                    log.info("🚨 Cache miss para $ori->$dest")
                    refreshQuote(ori, dest)
                }
            )

    private fun refreshQuote(from: String, to: String): Mono<FxQuote> {
        log.debug("[refreshQuote]:: cache miss")
        return fxQuoteProviderPort.getQuote(from, to)
            .flatMap { fresh ->
                val now = Instant.now()
                val updated = fresh.copy(
                    fetchedAt = now,
                    expiresAt = now.plusSeconds(expire.toLong())
                )
                fxQuoteCachePort.saveQuote(updated)
                    .thenReturn(updated)
            }
    }
}

