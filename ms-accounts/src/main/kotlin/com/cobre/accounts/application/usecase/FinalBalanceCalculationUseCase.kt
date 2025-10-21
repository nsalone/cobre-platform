package com.cobre.accounts.application.usecase

import com.cobre.accounts.domain.model.Transaction
import com.cobre.accounts.domain.model.TransactionEvent
import com.cobre.accounts.domain.model.enums.TransactionType
import com.cobre.accounts.domain.port.repository.AccountRepositoryPort
import com.cobre.accounts.domain.port.repository.TransactionRepositoryPort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import javax.security.auth.login.AccountNotFoundException

@Service
class FinalBalanceCalculationUseCase(
    private val accountRepositoryPort: AccountRepositoryPort,
    private val transactionRepositoryPort: TransactionRepositoryPort
) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun process(events: Flux<TransactionEvent>): Mono<Void> {
        return events
            .groupBy { it.accountId }
            .flatMap { groupedFlux ->
                val accountId = groupedFlux.key()
                groupedFlux
                    .collectList()
                    .flatMap { eventsForAccount ->
                        updateAccountBalance(accountId, eventsForAccount)
                            .onErrorResume { ex ->
                                log.error("Error processing account $accountId: ${ex.message}")
                                Mono.empty()
                            }
                    }
            }
            .then()
    }

    private fun updateAccountBalance(
        accountId: String,
        events: List<TransactionEvent>
    ): Mono<Void> {
        return accountRepositoryPort.findByAccountId(accountId)
            .switchIfEmpty(Mono.error(AccountNotFoundException(accountId)))
            .flatMap { account ->
                val finalBalance = events.fold(account.balance) { balance, ev ->
                    when (ev.operationType) {
                        TransactionType.CREDIT -> balance + ev.amount
                        TransactionType.DEBIT -> balance - ev.amount
                    }
                }

                val updatedAccount = account.copy(balance = finalBalance)
                val transactions = events.map { Transaction.fromEvent(it, account) }

                transactionRepositoryPort.saveAll(transactions)
                    .then(accountRepositoryPort.save(updatedAccount))
                    .then()
            }
    }
}

