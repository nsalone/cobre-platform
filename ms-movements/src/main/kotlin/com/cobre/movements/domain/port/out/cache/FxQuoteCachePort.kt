package com.cobre.movements.domain.port.out.cache

import com.cobre.movements.domain.model.FxQuote
import reactor.core.publisher.Mono

interface FxQuoteCachePort {
    fun saveQuote(quote: FxQuote): Mono<Void>
    fun getQuoteById(quoteId: String): Mono<FxQuote>
    fun getQuoteByPair(from: String, to: String): Mono<FxQuote>
}