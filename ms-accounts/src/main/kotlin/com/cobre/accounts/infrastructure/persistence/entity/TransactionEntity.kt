package com.cobre.accounts.infrastructure.persistence.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.Instant

@Table("transactions")
data class TransactionEntity(
    @Id val id: String? = null,
    val accountId: String,
    val eventId: String,
    val idempotencyKey: String,
    val operationType: String,
    val amount: BigDecimal,
    val currency: String,
    val balanceAfter: BigDecimal,
    val operationDate: Instant,
    val createdAt: Instant
)