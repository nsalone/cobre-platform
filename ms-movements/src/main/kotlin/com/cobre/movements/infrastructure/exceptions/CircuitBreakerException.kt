package com.cobre.movements.infrastructure.exceptions


class CircuitBreakerException : RuntimeException {
    override val message: String
    val api: String

    /**
     * Constructor
     *
     * @param message the exception message
     * @param api the api call
     * @param cause the original exception cause
     */
    constructor(message: String, api: String, cause: Throwable?) : super(cause) {
        this.message = message
        this.api = api
    }

    /**
     * Constructor
     *
     * @param message the exception message
     * @param api the api call
     */
    constructor(message: String, api: String) : super() {
        this.message = message
        this.api = api
    }

    companion object {
        private const val serialVersionUID = 4459483198203092926L
    }
}