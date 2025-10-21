package com.cobre.movements.domain.model.event

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

data class CrossBorderMoneyMovementEvent(
    @JsonProperty("event_id")
    val eventId: String,

    @JsonProperty("event_type")
    val eventType: String = "cross_border_money_movement",

    @JsonProperty("operation_date")
    val operationDate: Instant,

    @JsonProperty("origin")
    val origin: MovementEndpoint,

    @JsonProperty("destination")
    val destination: MovementEndpoint
)