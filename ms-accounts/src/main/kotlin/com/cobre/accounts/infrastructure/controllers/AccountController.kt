package com.cobre.accounts.infrastructure.controllers

import com.cobre.accounts.application.usecase.ApplyTransferUseCase
import com.cobre.accounts.application.usecase.FinalBalanceCalculationUseCase
import com.cobre.accounts.domain.model.TransactionEvent
import com.cobre.accounts.infrastructure.controllers.dto.request.TransferRequest
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/accounts")
class AccountController(
    private val applyTransferUseCase: ApplyTransferUseCase,
    private val finalBalanceCalculationUseCase: FinalBalanceCalculationUseCase
) {

    @PostMapping("/transfer")
    fun transfer(@RequestBody req: TransferRequest): Mono<Void> =
        applyTransferUseCase.execute(
            req.originAccountId,
            req.destinationAccountId,
            req.originAmount,
            req.destinationAmount,
            req.currencyOrigin,
            req.currencyDestination,
            req.referenceEventId
        )

    @PostMapping("/final-balance")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun processTransactions(@RequestBody events: List<TransactionEvent>): Mono<Void> {
        return finalBalanceCalculationUseCase.process(Flux.fromIterable(events))
    }
}