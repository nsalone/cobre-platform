package com.cobre.movements.infrastructure.controllers.dto.response

import java.math.BigDecimal
import java.time.Instant

data class FxQuoteResponse(
    val quoteId: String,
    val originCurrency: String,
    val destinationCurrency: String,
    val rate: BigDecimal,
    val amountConverted: BigDecimal,
    val expiresAt: Instant
)