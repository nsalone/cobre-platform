package com.cobre.accounts.domain.port.repository

import com.cobre.accounts.domain.model.Account
import reactor.core.publisher.Mono
import java.math.BigDecimal

interface AccountRepositoryPort {
    fun findByAccountId(accountId: String): Mono<Account>
    fun save(account: Account): Mono<Account>
    fun findByAccountIdForUpdate(accountId: String): Mono<Account>
    fun addToBalance(accountId: String, amount: BigDecimal): Mono<Void>
    fun subtractFromBalance(accountId: String, amount: BigDecimal): Mono<Void>
}