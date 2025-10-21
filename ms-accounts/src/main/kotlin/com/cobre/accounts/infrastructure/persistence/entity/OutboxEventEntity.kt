package com.cobre.accounts.infrastructure.persistence.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("event_outbox")
data class OutboxEventEntity(
    @Id val id: String? = null,
    val eventType: String,
    val aggregateId: String,
    val payload: String,
    val status: String = "PENDING",
    val retries: Int = 0,
    val createdAt: Instant = Instant.now(),
    val publishedAt: Instant? = null
)