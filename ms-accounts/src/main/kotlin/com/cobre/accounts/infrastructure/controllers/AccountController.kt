package com.cobre.accounts.infrastructure.controllers

import com.cobre.accounts.application.usecase.ApplyTransferUseCase
import com.cobre.accounts.application.usecase.FinalBalanceCalculationUseCase
import com.cobre.accounts.application.usecase.FinalBalancePreviewUseCase
import com.cobre.accounts.domain.model.TransactionEvent
import com.cobre.accounts.infrastructure.controllers.dto.request.AccountBalanceResult
import com.cobre.accounts.infrastructure.controllers.dto.request.AccountProcessResult
import com.cobre.accounts.infrastructure.controllers.dto.request.TransferRequest
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/accounts")
class AccountController(
    private val applyTransferUseCase: ApplyTransferUseCase,
    private val finalBalanceCalculationUseCase: FinalBalanceCalculationUseCase,
    private val finalBalancePreviewUseCase: FinalBalancePreviewUseCase
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
    fun processEvents(@RequestBody events: Flux<TransactionEvent>): Flux<AccountProcessResult> {
        return finalBalanceCalculationUseCase.process(events)
    }

    @GetMapping("/preview")
    fun getPreview(): Flux<AccountBalanceResult> {
        return finalBalancePreviewUseCase.calculateFinalBalances()
    }

}