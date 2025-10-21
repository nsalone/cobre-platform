package com.cobre.movements.infrastructure.adapter.out.rest.dto

import java.math.BigDecimal

data class FastForexResponse(
    val base: String,
    val result: Map<String, BigDecimal>,
    val updated: String,
    val ms: Int
)
