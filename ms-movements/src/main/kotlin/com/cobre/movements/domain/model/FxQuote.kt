package com.cobre.movements.domain.model

import java.math.BigDecimal
import java.time.Instant

data class FxQuote(
    val id: String,
    val from: String,
    val to: String,
    val rate: BigDecimal,
    val fetchedAt: Instant,
    val expiresAt: Instant
)