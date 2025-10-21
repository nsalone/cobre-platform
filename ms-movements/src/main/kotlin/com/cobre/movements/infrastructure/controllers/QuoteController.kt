package com.cobre.movements.infrastructure.controllers

import com.cobre.movements.application.usecase.FxQuoteUseCase
import com.cobre.movements.infrastructure.controllers.dto.request.FxQuoteRequest
import com.cobre.movements.infrastructure.controllers.dto.response.FxQuoteResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/fx")
class QuoteController(private val fxQuoteUseCase: FxQuoteUseCase) {

    @PostMapping("/quote")
    fun getQuote(@RequestBody request: FxQuoteRequest): Mono<FxQuoteResponse> {
        return fxQuoteUseCase.getQuote(request)
    }
}