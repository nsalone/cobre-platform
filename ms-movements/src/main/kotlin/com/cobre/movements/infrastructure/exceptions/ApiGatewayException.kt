package com.cobre.movements.infrastructure.exceptions

import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode


class ApiGatewayException : RuntimeException {
    override val message: String
    private val status: HttpStatusCode
    private val error: String

    /**
     * Constructor
     *
     * @param message the exception message
     * @param status the exception status
     * @param error the exception error
     * @param cause the original exception cause
     */
    constructor(message: String, status: HttpStatus, error: String, cause: Throwable?) : super(cause) {
        this.message = message
        this.status = status
        this.error = error
    }

    /**
     * Constructor
     *
     * @param message the exception message
     * @param status the exception status
     * @param error the exception error
     */
    constructor(message: String, status: HttpStatusCode, error: String) : super() {
        this.message = message
        this.status = status
        this.error = error
    }

    companion object {
        private const val serialVersionUID = 4459483198203092926L
    }
}