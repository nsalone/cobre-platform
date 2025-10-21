package com.cobre.movements.infrastructure.controllers.dto.request

import java.math.BigDecimal

data class FxQuoteRequest(
    val originCurrency: String,
    val destinationCurrency: String,
    val amount: BigDecimal
)