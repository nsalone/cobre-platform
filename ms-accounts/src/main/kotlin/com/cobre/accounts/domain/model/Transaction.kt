package com.cobre.accounts.domain.model

import com.cobre.accounts.domain.model.enums.TransactionType
import java.math.BigDecimal
import java.time.Instant

data class Transaction(
    val id: String? = null,
    val accountId: String,
    val eventId: String,
    val idempotencyKey: String,
    val operationType: TransactionType,
    val amount: BigDecimal,
    val currency: String,
    val balanceAfter: BigDecimal,
    val operationDate: Instant,
    val createdAt: Instant
) {
    companion object {
        fun debit(
            accountId: String,
            eventId: String,
            amount: BigDecimal,
            currency: String,
            balanceBefore: BigDecimal,
            operationDate: Instant = Instant.now()
        ): Transaction {
            val balanceAfter = balanceBefore.subtract(amount)
            return Transaction(
                accountId = accountId,
                eventId = eventId,
                idempotencyKey = "$eventId:$accountId:DEBIT",
                operationType = TransactionType.DEBIT,
                amount = amount,
                currency = currency,
                balanceAfter = balanceAfter,
                operationDate = operationDate,
                createdAt = Instant.now()
            )
        }

        fun credit(
            accountId: String,
            eventId: String,
            amount: BigDecimal,
            currency: String,
            balanceBefore: BigDecimal,
            operationDate: Instant = Instant.now()
        ): Transaction {
            val balanceAfter = balanceBefore.add(amount)
            return Transaction(
                accountId = accountId,
                eventId = eventId,
                idempotencyKey = "$eventId:$accountId:CREDIT",
                operationType = TransactionType.CREDIT,
                amount = amount,
                currency = currency,
                balanceAfter = balanceAfter,
                operationDate = operationDate,
                createdAt = Instant.now()
            )
        }

        fun fromEvent(event: TransactionEvent, account: Account): Transaction {
            return Transaction(
                id = null,
                accountId = account.accountId,
                eventId = event.eventId,
                idempotencyKey = "${event.eventId}:${account.accountId}:${event.operationType}",
                operationType = event.operationType,
                amount = event.amount,
                currency = event.currency,
                balanceAfter = account.balance,
                operationDate = event.operationDate,
                createdAt = Instant.now()
            )
        }
    }
}
