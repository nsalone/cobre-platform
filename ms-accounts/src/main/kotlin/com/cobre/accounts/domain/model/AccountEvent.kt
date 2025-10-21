package com.cobre.accounts.domain.model

import java.math.BigDecimal

data class AccountEvent(
    val accountId: String,
    val currency: String,
    val amount: BigDecimal
)