package com.cobre.accounts.application.usecase

import com.cobre.accounts.domain.model.Transaction
import com.cobre.accounts.domain.model.TransactionEvent
import com.cobre.accounts.domain.model.enums.TransactionType
import com.cobre.accounts.domain.port.repository.AccountRepositoryPort
import com.cobre.accounts.domain.port.repository.TransactionRepositoryPort
import com.cobre.accounts.infrastructure.controllers.dto.request.AccountProcessResult
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import javax.security.auth.login.AccountNotFoundException

@Service
class FinalBalanceCalculationUseCase(
    private val accountRepositoryPort: AccountRepositoryPort,
    private val transactionRepositoryPort: TransactionRepositoryPort
) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun process(events: Flux<TransactionEvent>): Flux<AccountProcessResult> {
        return events
            .groupBy { it.accountId }
            .flatMap { groupedFlux ->
                val accountId = groupedFlux.key()
                log.info("Processing ${accountId}")
                groupedFlux.collectList()
                    .flatMap { eventsForAccount ->
                        updateAccountBalance(accountId, eventsForAccount)
                            .map { AccountProcessResult(accountId, "UPDATED", "Balance updated successfully") }
                            .onErrorResume { ex ->
                                Mono.just(
                                    AccountProcessResult(accountId, "FAILED", ex.message ?: "Unknown error")
                                )
                            }
                    }
            }
    }

    private fun updateAccountBalance(
        accountId: String,
        events: List<TransactionEvent>
    ): Mono<Void> {
        return accountRepositoryPort.findByAccountIdForUpdate(accountId)
            .switchIfEmpty(Mono.error(AccountNotFoundException("Account $accountId not found")))
            .flatMap { account ->
                val validCurrency = events.all { it.currency == account.currency }
                if (!validCurrency) {
                    return@flatMap Mono.error(
                        IllegalArgumentException("Currency mismatch for account $accountId")
                    )
                }

                val finalBalance = events.fold(account.balance) { balance, ev ->
                    when (ev.operationType) {
                        TransactionType.CREDIT -> balance + ev.amount
                        TransactionType.DEBIT -> balance - ev.amount
                    }
                }

                if (validateBalance(finalBalance)) return@flatMap Mono.error(
                    IllegalStateException("Negative balance for account $accountId")
                )

                val updatedAccount = account.copy(balance = finalBalance)
                val transactions = events.map { Transaction.fromEvent(it, account) }

                log.info("Transactions processed for account $accountId: $transactions")
                transactionRepositoryPort.saveAll(transactions)
                    .then(accountRepositoryPort.save(updatedAccount))
                    .then()
            }
    }

    private fun validateBalance(finalBalance: BigDecimal): Boolean {
        return finalBalance < BigDecimal.ZERO
    }
}

