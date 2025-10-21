package com.cobre.accounts.domain.port.repository

import com.cobre.accounts.domain.model.Transaction
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface TransactionRepositoryPort {
    fun findByIdempotencyKey(idempotencyKey: String): Mono<Transaction>
    fun findByAccountId(accountId: String): Flux<Transaction>
    fun save(tx: Transaction): Mono<Transaction>
    fun saveAll(transactions: List<Transaction>): Mono<Void>
}