package com.cobre.movements.infrastructure.adapter.out.repository

import com.cobre.movements.domain.model.CBMMSaga
import com.cobre.movements.domain.port.out.repository.CBMMSagaRepositoryPort
import com.cobre.movements.infrastructure.persistence.CBMMSagaRepository
import com.cobre.movements.infrastructure.persistence.mapper.toDomain
import com.cobre.movements.infrastructure.persistence.mapper.toEntity
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class CBMMSagaRepositoryAdapter(
    private val repository: CBMMSagaRepository
) : CBMMSagaRepositoryPort {

    override fun save(entity: CBMMSaga): Mono<CBMMSaga> =
        repository.save(entity.toEntity()).map { it.toDomain() }

    override fun findByEventId(eventId: String): Mono<CBMMSaga> =
        repository.findByEventId(eventId).map { it.toDomain() }

    override fun existsByEventId(eventId: String): Mono<Boolean> =
        repository.existsByEventId(eventId)
}
