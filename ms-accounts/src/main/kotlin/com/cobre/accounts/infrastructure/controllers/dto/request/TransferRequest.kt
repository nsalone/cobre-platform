package com.cobre.accounts.infrastructure.controllers.dto.request

import java.math.BigDecimal

data class TransferRequest(
    val originAccountId: String,
    val destinationAccountId: String,
    val originAmount: BigDecimal,
    val destinationAmount: BigDecimal,
    val currencyOrigin: String,
    val currencyDestination: String,
    val referenceEventId: String
)