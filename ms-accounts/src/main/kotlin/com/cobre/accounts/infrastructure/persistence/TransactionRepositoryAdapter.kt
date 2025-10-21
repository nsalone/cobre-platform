package com.cobre.accounts.infrastructure.persistence

import com.cobre.accounts.domain.model.Transaction
import com.cobre.accounts.domain.port.repository.TransactionRepositoryPort
import com.cobre.accounts.infrastructure.persistence.mapper.toDomain
import com.cobre.accounts.infrastructure.persistence.mapper.toEntity
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class TransactionRepositoryAdapter(
    private val repo: TransactionR2dbcRepository
) : TransactionRepositoryPort {

    override fun findByIdempotencyKey(idempotencyKey: String): Mono<Transaction> =
        repo.findByIdempotencyKey(idempotencyKey).map { it.toDomain() }

    override fun findByAccountId(accountId: String): Flux<Transaction> =
        repo.findByAccountId(accountId).map { it.toDomain() }

    override fun save(tx: Transaction): Mono<Transaction> =
        repo.save(tx.toEntity()).map { it.toDomain() }

    override fun saveAll(transactions: List<Transaction>): Mono<Void> {
        return repo.saveAll(transactions.map { it.toEntity() })
            .map { it.toDomain() }
            .then()
    }

    override fun findAll(): Flux<Transaction> =
        repo.findAll().map { it.toDomain() }
}
