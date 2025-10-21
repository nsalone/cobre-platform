package com.cobre.movements.infrastructure.controllers

import com.cobre.movements.application.usecase.CBMMRequestUseCase
import com.cobre.movements.infrastructure.controllers.dto.CBMMRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/cbmm")
class CBMMController(
    private val cbmmRequestUseCase: CBMMRequestUseCase
) {

    @PostMapping("/request")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun requestTransfer(
        @RequestBody request: CBMMRequest
    ): Mono<ResponseEntity<Void>> {
        return cbmmRequestUseCase.requestTransfer(request)
            .thenReturn(ResponseEntity.accepted().build())
    }
}
