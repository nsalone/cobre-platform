package com.cobre.movements.infrastructure.persistence.mapper

import com.cobre.movements.domain.model.CBMMSaga
import com.cobre.movements.infrastructure.persistence.entity.CBMMSagaEntity

fun CBMMSagaEntity.toDomain() = CBMMSaga(
    id = id,
    eventId = eventId,
    cbmmStatus = cbmmStatus,
    payload = payload,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun CBMMSaga.toEntity() = CBMMSagaEntity(
    id = id,
    eventId = eventId,
    cbmmStatus = cbmmStatus,
    payload = payload,
    createdAt = createdAt,
    updatedAt = updatedAt
)