package com.cobre.movements.infrastructure.exceptions


import org.springframework.web.reactive.function.client.ClientResponse
import reactor.core.publisher.Mono

class ApiErrorHandler {
    companion object {
        fun handle4xxScenario(response: ClientResponse, apiName: String, code: String): Mono<BusinessException> {
            return response
                .bodyToMono(String::class.java)
                .map { BusinessException("$apiName: $it", code) }
        }

        fun handle5xxScenario(
            response: ClientResponse,
            errorMsg: String
        ): Mono<ApiGatewayException> {
            return response
                .bodyToMono<String>(String::class.java)
                .map { m: String? ->
                    ApiGatewayException(
                        errorMsg,
                        response.statusCode(),
                        m!!
                    )
                }
        }

    }
}
