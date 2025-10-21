package com.cobre.accounts.infrastructure.persistence.mapper

import com.cobre.accounts.domain.model.Transaction
import com.cobre.accounts.domain.model.enums.TransactionType
import com.cobre.accounts.infrastructure.persistence.entity.TransactionEntity

fun TransactionEntity.toDomain() = Transaction(
    id = id,
    accountId = accountId,
    eventId = eventId,
    idempotencyKey = idempotencyKey,
    operationType = TransactionType.valueOf(operationType),
    amount = amount,
    currency = currency,
    balanceAfter = balanceAfter,
    operationDate = operationDate,
    createdAt = createdAt
)

fun Transaction.toEntity() = TransactionEntity(
    id = id,
    accountId = accountId,
    eventId = eventId,
    idempotencyKey = idempotencyKey,
    operationType = operationType.name,
    amount = amount,
    currency = currency,
    balanceAfter = balanceAfter,
    operationDate = operationDate,
    createdAt = createdAt
)
