package com.cobre.movements.domain.port.out.api

import com.cobre.movements.domain.model.FxQuote
import reactor.core.publisher.Mono

interface FxQuoteProviderPort {
    fun getQuote(from: String, to: String): Mono<FxQuote>
}