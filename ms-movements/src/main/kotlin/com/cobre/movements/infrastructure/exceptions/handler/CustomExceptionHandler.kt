package com.cobre.movements.infrastructure.exceptions.handler

import com.cobre.movements.infrastructure.exceptions.ApiGatewayException
import com.cobre.movements.infrastructure.exceptions.BusinessException
import com.cobre.movements.infrastructure.exceptions.CircuitBreakerException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.*
import org.springframework.validation.FieldError
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.ServerWebInputException
import reactor.core.publisher.Mono
import java.net.URI
import java.util.concurrent.TimeoutException

@ControllerAdvice
class CustomExceptionHandler : ResponseEntityExceptionHandler() {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(CustomExceptionHandler::class.java)
        const val ERROR_TECNICO = "ERROR TECNICO. CONTACTE AL ADMINISTRADOR"
        const val ERROR_TIMEOUT = "Looks like the server is taking to long to respond, please try again in sometime"
        const val URI_ERROR = "https://cobre-error-handler.herokuapp.com/error"
    }

    @ExceptionHandler(ApiGatewayException::class)
    fun handleApiGatewayException(
        ex: ApiGatewayException,
        exchange: ServerWebExchange
    ): Mono<ResponseEntity<Any>> {
        log.error("API GATEWAY EXCEPTION", ex)
        return handleExceptionInternal(
            ex,
            createProblemDetail(
                ex, "GATEWAY", HttpStatus.BAD_GATEWAY, ex.message, exchange
            ),
            HttpHeaders(),
            HttpStatus.BAD_GATEWAY,
            exchange
        )
    }

    @ExceptionHandler(CircuitBreakerException::class)
    fun handleCircuitBreakerException(
        ex: CircuitBreakerException,
        exchange: ServerWebExchange
    ): Mono<ResponseEntity<Any>> {
        return handleExceptionInternal(
            ex,
            createProblemDetail(
                ex, "[CIRCUIT BREAKER]:: [${ex.api}]", HttpStatus.SERVICE_UNAVAILABLE, ex.message, exchange
            ),
            HttpHeaders(),
            HttpStatus.SERVICE_UNAVAILABLE,
            exchange
        )
    }

    @ExceptionHandler(TimeoutException::class)
    fun handleTimeoutException(
        ex: TimeoutException,
        exchange: ServerWebExchange
    ): Mono<ResponseEntity<Any>> {
        log.error("TIMEOUT EXCEPTION", ex)
        return handleExceptionInternal(
            ex,
            createProblemDetail(
                ex, "TIMEOUT", HttpStatus.REQUEST_TIMEOUT, ERROR_TIMEOUT, exchange
            ),
            HttpHeaders(),
            HttpStatus.REQUEST_TIMEOUT,
            exchange
        )
    }

    @ExceptionHandler(BusinessException::class)
    fun handleBussinessException(
        ex: BusinessException,
        exchange: ServerWebExchange
    ): Mono<ResponseEntity<Any>> {
        log.warn("BUSINESS EXCEPTION", ex)
        return handleExceptionInternal(
            ex,
            createProblemDetail(
                ex, ex.code, HttpStatus.CONFLICT, ex.message!!, exchange
            ),
            HttpHeaders(),
            HttpStatus.CONFLICT,
            exchange
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception,
        exchange: ServerWebExchange
    ): Mono<ResponseEntity<Any>> {
        log.error(ERROR_TECNICO, ex)
        return handleExceptionInternal(
            ex,
            createProblemDetail(
                ex, "TECHNICAL", HttpStatus.INTERNAL_SERVER_ERROR, ERROR_TECNICO, exchange
            ),
            HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR,
            exchange
        )
    }

    override fun handleWebExchangeBindException(
        ex: WebExchangeBindException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        exchange: ServerWebExchange
    ): Mono<ResponseEntity<Any>> {
        return super.handleExceptionInternal(
            ex,
            createProblemDetail(
                ex,
                "VALIDATION",
                HttpStatus.valueOf(status.value()),
                ex.bindingResult.allErrors.joinToString(
                    ", ",
                    "{",
                    "}"
                ) { (it as FieldError).field + ":" + it.defaultMessage },
                exchange
            ),
            HttpHeaders(),
            status,
            exchange
        )
    }

    override fun handleServerWebInputException(
        ex: ServerWebInputException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        exchange: ServerWebExchange
    ): Mono<ResponseEntity<Any>> {
        return super.handleExceptionInternal(
            ex,
            createProblemDetail(
                ex,
                "VALIDATION",
                HttpStatus.valueOf(status.value()), ex.message, exchange
            ),
            HttpHeaders(),
            status,
            exchange
        )
    }

    private fun createProblemDetail(
        ex: Exception,
        title: String,
        status: HttpStatus,
        detail: String,
        exchange: ServerWebExchange
    ): ProblemDetail {
        val problemDetail = super.createProblemDetail(
            ex,
            status,
            detail,
            null,
            null,
            exchange
        )
        problemDetail.type = URI(URI_ERROR)
        problemDetail.title = title

        return problemDetail
    }
}
