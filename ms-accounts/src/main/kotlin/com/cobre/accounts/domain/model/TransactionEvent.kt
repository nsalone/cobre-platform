package com.cobre.accounts.domain.model

import com.cobre.accounts.domain.model.enums.TransactionType
import java.math.BigDecimal
import java.time.Instant
import java.util.*

data class TransactionEvent(
    val eventId: String = UUID.randomUUID().toString(),
    val accountId: String,
    val amount: BigDecimal,
    val currency: String,
    val operationType: TransactionType,
    val operationDate: Instant = Instant.now()
)