package com.cobre.movements.infrastructure.adapter.out.cache

import com.cobre.movements.domain.model.FxQuote
import com.cobre.movements.domain.port.out.cache.FxQuoteCachePort
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.time.Duration

@Component
class FxQuoteRedisAdapter(
    @Value("\${quotation-api.expire-at}") private val expire: String,
    private val redis: ReactiveRedisTemplate<String, String>,
    private val objectMapper: ObjectMapper
) : FxQuoteCachePort {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun saveQuote(quote: FxQuote): Mono<Void> {
        val pairKey = "fx:${quote.from}:${quote.to}"
        val idKey = "fx:id:${quote.id}"
        val ttl = Duration.ofSeconds(expire.toLong())
        val json = objectMapper.writeValueAsString(quote)

        return redis.opsForValue().set(pairKey, json, ttl)
            .then(redis.opsForValue().set(idKey, json, ttl))
            .doOnSuccess { log.debug("Saved quotation con TTL=${ttl.seconds}s -> $pairKey y $idKey") }
            .then()
    }

    override fun getQuoteById(quoteId: String): Mono<FxQuote> =
        redis.opsForValue().get("fx:id:$quoteId")
            .doOnNext { log.debug("Get fx:id:$quoteId") }
            .map { json -> objectMapper.readValue(json, FxQuote::class.java) }

    override fun getQuoteByPair(from: String, to: String): Mono<FxQuote> =
        redis.opsForValue().get("fx:$from:$to")
            .doOnNext { log.debug("Get fx:$from:$to") }
            .map { json -> objectMapper.readValue(json, FxQuote::class.java) }

}