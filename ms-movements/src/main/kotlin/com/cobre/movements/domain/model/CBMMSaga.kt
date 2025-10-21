package com.cobre.movements.domain.model

import java.time.Instant

data class CBMMSaga(
    val id: String? = null,
    val eventId: String,
    val cbmmStatus: String,
    val payload: String,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)