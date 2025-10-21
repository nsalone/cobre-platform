package com.cobre.movements.infrastructure.persistence

import com.cobre.movements.infrastructure.persistence.entity.CBMMSagaEntity
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono

interface CBMMSagaRepository : ReactiveCrudRepository<CBMMSagaEntity, String> {

    fun existsByEventId(eventId: String): Mono<Boolean>

    @Query("UPDATE cbmm_saga SET cbmm_status = :status, updated_at = NOW() WHERE event_id = :eventId")
    fun updateStatus(eventId: String, status: String): Mono<Void>

    fun findByEventId(eventId: String): Mono<CBMMSagaEntity>


}