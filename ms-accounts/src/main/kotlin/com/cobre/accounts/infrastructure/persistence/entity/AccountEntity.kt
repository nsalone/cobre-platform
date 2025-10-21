package com.cobre.accounts.infrastructure.persistence.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.Instant

@Table(value = "accounts")
data class AccountEntity(
    @Id val id: String? = null,
    val accountId: String,
    val currency: String,
    val balance: BigDecimal,
    val createdAt: Instant,
    val updatedAt: Instant
)