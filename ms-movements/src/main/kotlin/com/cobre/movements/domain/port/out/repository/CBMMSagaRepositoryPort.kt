package com.cobre.movements.domain.port.out.repository

import com.cobre.movements.domain.model.CBMMSaga
import reactor.core.publisher.Mono

interface CBMMSagaRepositoryPort {
    fun save(entity: CBMMSaga): Mono<CBMMSaga>
    fun findByEventId(eventId: String): Mono<CBMMSaga>
    fun existsByEventId(eventId: String): Mono<Boolean>
}
