package com.cobre.accounts.infrastructure.persistence

import com.cobre.accounts.infrastructure.persistence.entity.OutboxEventEntity
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface OutboxEventRepository : ReactiveCrudRepository<OutboxEventEntity, String> {

    @Query("SELECT * FROM event_outbox WHERE status = 'PENDING' OR (status = 'FAILED' AND retries < 5)")
    fun findPending(): Flux<OutboxEventEntity>

    @Query("UPDATE event_outbox SET status = :status WHERE id = :id")
    fun updateStatus(id: String, status: String): Mono<Void>

    @Query("UPDATE event_outbox SET retries = retries + 1 WHERE id = :id")
    fun incrementRetry(id: String): Mono<Void>
}