package com.cobre.accounts.application.usecase

import com.cobre.accounts.domain.model.enums.TransactionType
import com.cobre.accounts.domain.port.repository.TransactionRepositoryPort
import com.cobre.accounts.infrastructure.controllers.dto.request.AccountBalanceResult
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import java.math.BigDecimal

@Service
class FinalBalancePreviewUseCase(
    private val transactionRepositoryPort: TransactionRepositoryPort
) {

    fun calculateFinalBalances(): Flux<AccountBalanceResult> {
        return transactionRepositoryPort.findAll()
            .groupBy { it.accountId }
            .flatMap { groupedFlux ->
                groupedFlux.collectList().map { transactions ->
                    val accountId = groupedFlux.key()
                    val total = transactions.fold(BigDecimal.ZERO) { acc, tx ->
                        when (tx.operationType) {
                            TransactionType.CREDIT -> acc + tx.amount
                            TransactionType.DEBIT -> acc - tx.amount
                        }
                    }

                    AccountBalanceResult(
                        accountId = accountId,
                        currency = transactions.firstOrNull()?.currency ?: "N/A",
                        totalMovements = transactions.size,
                        netBalance = total
                    )
                }
            }
    }
}
