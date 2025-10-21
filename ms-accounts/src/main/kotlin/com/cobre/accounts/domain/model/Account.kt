package com.cobre.accounts.domain.model

import java.math.BigDecimal
import java.time.Instant
import java.util.*

data class Account(
    val id: String? = null,
    val accountId: String,
    val currency: String,
    val balance: BigDecimal = BigDecimal.ZERO,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    fun debit(amount: BigDecimal): Account {
        require(amount >= BigDecimal.ZERO) { "amount must be >= 0" }
        require(balance >= amount) { "Insufficient funds" }
        return copy(balance = balance.subtract(amount), updatedAt = Instant.now())
    }

    fun credit(amount: BigDecimal): Account {
        require(amount >= BigDecimal.ZERO) { "amount must be >= 0" }
        return copy(balance = balance.add(amount), updatedAt = Instant.now())
    }

    companion object {
        fun new(accountId: String, currency: String) =
            Account(
                id = UUID.randomUUID().toString(),
                accountId = accountId,
                currency = currency,
                balance = BigDecimal.ZERO,
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
    }
}
