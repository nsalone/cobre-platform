package com.cobre.movements.infrastructure.controllers.dto

import java.math.BigDecimal

data class CBMMRequest(
    val quoteId: String,
    val originAccount: String,
    val destinationAccount: String,
    val amount: BigDecimal
)