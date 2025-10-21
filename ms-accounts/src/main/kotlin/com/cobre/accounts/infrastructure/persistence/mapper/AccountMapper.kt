package com.cobre.accounts.infrastructure.persistence.mapper

import com.cobre.accounts.domain.model.Account
import com.cobre.accounts.infrastructure.persistence.entity.AccountEntity

fun AccountEntity.toDomain() = Account(
    id = id,
    accountId = accountId,
    currency = currency,
    balance = balance,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Account.toEntity() = AccountEntity(
    id = id,
    accountId = accountId,
    currency = currency,
    balance = balance,
    createdAt = createdAt,
    updatedAt = updatedAt
)
