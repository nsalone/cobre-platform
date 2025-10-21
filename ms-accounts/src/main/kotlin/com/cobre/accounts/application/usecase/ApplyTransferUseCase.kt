package com.cobre.accounts.application.usecase

import com.cobre.accounts.domain.model.Account
import com.cobre.accounts.domain.model.Transaction
import com.cobre.accounts.domain.port.repository.AccountRepositoryPort
import com.cobre.accounts.domain.port.repository.TransactionRepositoryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Mono
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2
import java.math.BigDecimal

@Service
class ApplyTransferUseCase(
    private val accountRepositoryPort: AccountRepositoryPort,
    private val transactionRepositoryPort: TransactionRepositoryPort,
    private val txOperator: TransactionalOperator
) {
    fun execute(
        originAccountId: String,
        destinationAccountId: String,
        originAmount: BigDecimal,
        destinationAmount: BigDecimal,
        currencyOrigin: String,
        currencyDestination: String,
        eventId: String
    ):  Mono<Void> {
        val debitKey = "$eventId:$originAccountId:DEBIT"
        val creditKey = "$eventId:$destinationAccountId:CREDIT"

        val pipeline =
            Mono.zip(
                transactionRepositoryPort.findByIdempotencyKey(debitKey).hasElement(),
                transactionRepositoryPort.findByIdempotencyKey(creditKey).hasElement()
            )
                .filter { (debitExists, creditExists) -> !debitExists && !creditExists }
                .flatMap {
                    getDebitFlow(originAccountId, currencyOrigin, originAmount, eventId)
                        .then(getCreditFlow(destinationAccountId, currencyDestination, eventId, destinationAmount))
                        .then()
                }

        return txOperator.transactional(pipeline)
    }

    private fun getDebitFlow(
        originAccountId: String,
        currencyOrigin: String,
        originAmount: BigDecimal,
        eventId: String
    ): Mono<Void> {
        return accountRepositoryPort.findByAccountIdForUpdate(originAccountId)
            .switchIfEmpty(Mono.error(IllegalStateException("Origin account not found")))
            .flatMap { origin ->
                validateDebitTx(origin, currencyOrigin, originAmount)
                val debitTx = Transaction.debit(
                    accountId = origin.accountId,
                    eventId = eventId,
                    amount = originAmount,
                    currency = currencyOrigin,
                    balanceBefore = origin.balance
                )
                val updatedOrigin = origin.debit(originAmount)
                transactionRepositoryPort.save(debitTx)
                    .then(accountRepositoryPort.save(updatedOrigin))
            }.then()
    }

    private fun getCreditFlow(
        destinationAccountId: String,
        currencyDestination: String,
        eventId: String,
        destinationAmount: BigDecimal
    ): Mono<Void> {
        return accountRepositoryPort.findByAccountIdForUpdate(destinationAccountId)
            .switchIfEmpty(
                Mono.defer {
                    Mono.just(Account.new(destinationAccountId, currencyDestination))
                        .flatMap { accountRepositoryPort.save(it) }
                }
            )
            .flatMap { dest ->
                validateCreditTx(dest, currencyDestination)
                val creditTx = Transaction.credit(
                    accountId = dest.accountId,
                    eventId = eventId,
                    amount = destinationAmount,
                    currency = currencyDestination,
                    balanceBefore = dest.balance
                )
                val updatedDest = dest.credit(destinationAmount)
                transactionRepositoryPort.save(creditTx)
                    .then(accountRepositoryPort.save(updatedDest))
            }.then()
    }

    private fun validateCreditTx(dest: Account?, currencyDestination: String) {
        require(dest?.currency == currencyDestination) {
            "Destination currency mismatch: expected=$currencyDestination actual=${dest?.currency}"
        }
    }

    private fun validateDebitTx(
        origin: Account?,
        currencyOrigin: String,
        originAmount: BigDecimal
    ) {
        requireNotNull(origin) { "Origin account not found" }
        require(origin.currency == currencyOrigin) {
            "Origin currency mismatch: expected=$currencyOrigin actual=${origin.currency}"
        }
        require(origin.balance >= originAmount) {
            "Insufficient funds"
        }
    }

}
