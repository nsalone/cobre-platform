package com.cobre.movements.infrastructure.adapter.out.rest

import com.cobre.movements.domain.model.FxQuote
import com.cobre.movements.domain.port.out.api.FxQuoteProviderPort
import com.cobre.movements.infrastructure.adapter.out.rest.dto.FastForexResponse
import com.cobre.movements.infrastructure.exceptions.ApiErrorHandler
import com.cobre.movements.infrastructure.exceptions.CircuitBreakerException
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.Instant
import java.util.*

@Component
class QuoteExternalApiAdapter(
    private val webClient: WebClient,
    @Value("\${quotation-api.base-url}") private val baseUrl: String,
    @Value("\${quotation-api.fetch-one}") private val fetchOne: String,
    @Value("\${quotation-api.api-key}") private val apiKey: String
) : FxQuoteProviderPort {

    @CircuitBreaker(name = "quotation-service", fallbackMethod = "fallbackGetQuote")
    override fun getQuote(from: String, to: String): Mono<FxQuote> {
        val uri = UriComponentsBuilder
            .fromUriString("$baseUrl$fetchOne")
            .queryParam("from", from)
            .queryParam("to", to)
            .toUriString()

        return webClient
            .get()
            .uri(uri)
            .header("X-API-Key", apiKey)
            .retrieve()
            .onStatus({ httpStatus -> httpStatus.is4xxClientError }) {
                ApiErrorHandler.handle4xxScenario(it, "QUOTE_API", "QUOTE_API_ERROR")
            }
            .onStatus(HttpStatusCode::is5xxServerError) {
                ApiErrorHandler.handle5xxScenario(
                    it,
                    "QUOTATION_API Error when getQuote"
                )
            }
            .bodyToMono(FastForexResponse::class.java)
            .map { response ->
                val rate = response.result[to] ?: BigDecimal.ZERO
                val fetchedAt = Instant.now()
                FxQuote(
                    id = UUID.randomUUID().toString(),
                    from = from,
                    to = to,
                    rate = rate,
                    fetchedAt = fetchedAt,
                    expiresAt = fetchedAt.plusSeconds(300)
                )
            }
    }

    fun fallbackGetQuote(from: String, to: String, ex: Throwable): Mono<FxQuote> {
        return Mono.error(CircuitBreakerException("Open Circuit Breaker on QUOTATION_API", "QUOTATION_API", ex))
    }

}

