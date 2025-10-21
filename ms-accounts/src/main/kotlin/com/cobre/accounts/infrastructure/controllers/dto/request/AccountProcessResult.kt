package com.cobre.accounts.infrastructure.controllers.dto.request

data class AccountProcessResult(
    val accountId: String,
    val status: String,
    val message: String
)