package com.cobre.accounts.infrastructure.controllers.dto.request

import java.math.BigDecimal

data class AccountBalanceResult(
    val accountId: String,
    val currency: String,
    val totalMovements: Int,
    val netBalance: BigDecimal
)
