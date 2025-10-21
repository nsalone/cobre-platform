package com.cobre.movements.domain.model.event

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

data class MovementEndpoint(
    @JsonProperty("account_id")
    val accountId: String,

    @JsonProperty("currency")
    val currency: String,

    @JsonProperty("amount")
    val amount: BigDecimal
)