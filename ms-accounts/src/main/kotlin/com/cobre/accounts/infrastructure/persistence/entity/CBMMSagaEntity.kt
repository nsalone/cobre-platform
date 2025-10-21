package com.cobre.accounts.infrastructure.persistence.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("cbmm_saga")
data class CBMMSagaEntity(
    @Id val id: String? = null,
    val eventId: String,
    val cbmmStatus: String,
    val payload: String,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
)