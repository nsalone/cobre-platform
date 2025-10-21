package com.cobre.accounts.infrastructure.persistence

import com.cobre.accounts.infrastructure.persistence.entity.TransactionEntity
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface TransactionR2dbcRepository : ReactiveCrudRepository<TransactionEntity, String> {
    fun findByIdempotencyKey(idempotencyKey: String): Mono<TransactionEntity>
    fun findByAccountId(accountId: String): Flux<TransactionEntity>
}