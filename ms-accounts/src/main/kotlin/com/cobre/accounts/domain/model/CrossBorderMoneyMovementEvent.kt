package com.cobre.accounts.domain.model

import java.time.Instant

data class CrossBorderMoneyMovementEvent(
    val eventId: String,
    val eventType: String,
    val operationDate: Instant,
    val origin: AccountEvent,
    val destination: AccountEvent
)