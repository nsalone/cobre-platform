package com.cobre.movements.infrastructure.exceptions

class BusinessException(message: String, val code: String) : RuntimeException(message)
