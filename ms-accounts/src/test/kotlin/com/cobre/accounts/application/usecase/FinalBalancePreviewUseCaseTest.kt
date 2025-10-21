package com.cobre.accounts.application.usecase

import com.cobre.accounts.domain.model.Transaction
import com.cobre.accounts.domain.model.enums.TransactionType
import com.cobre.accounts.domain.port.repository.TransactionRepositoryPort
import com.cobre.accounts.infrastructure.controllers.dto.request.AccountBalanceResult
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import java.math.BigDecimal
import java.time.Instant

class FinalBalancePreviewUseCaseTest {

    private val transactionRepositoryPort: TransactionRepositoryPort = mockk()
    private lateinit var useCase: FinalBalancePreviewUseCase

    @BeforeEach
    fun setup() {
        useCase = FinalBalancePreviewUseCase(transactionRepositoryPort)
    }

    @Test
    fun `should calculate final balances per account correctly`() {
        val now = Instant.now()

        val txs = listOf(
            Transaction(
                id = "1",
                accountId = "ACC1",
                eventId = "E1",
                idempotencyKey = "K1",
                operationType = TransactionType.CREDIT,
                amount = BigDecimal("100.00"),
                currency = "USD",
                balanceAfter = BigDecimal("100.00"),
                operationDate = now,
                createdAt = now
            ),
            Transaction(
                id = "2",
                accountId = "ACC1",
                eventId = "E2",
                idempotencyKey = "K2",
                operationType = TransactionType.DEBIT,
                amount = BigDecimal("25.00"),
                currency = "USD",
                balanceAfter = BigDecimal("75.00"),
                operationDate = now,
                createdAt = now
            ),
            Transaction(
                id = "3",
                accountId = "ACC2",
                eventId = "E3",
                idempotencyKey = "K3",
                operationType = TransactionType.CREDIT,
                amount = BigDecimal("500.00"),
                currency = "ARS",
                balanceAfter = BigDecimal("500.00"),
                operationDate = now,
                createdAt = now
            )
        )

        every { transactionRepositoryPort.findAll() } returns Flux.fromIterable(txs)

        StepVerifier.create(useCase.calculateFinalBalances())
            .recordWith { mutableListOf<AccountBalanceResult>() }
            .expectNextCount(2)
            .consumeRecordedWith { results ->
                val acc1 = results.first { it.accountId == "ACC1" }
                val acc2 = results.first { it.accountId == "ACC2" }

                assertEquals(BigDecimal("75.00"), acc1.netBalance)
                assertEquals("USD", acc1.currency)
                assertEquals(2, acc1.totalMovements)

                assertEquals(BigDecimal("500.00"), acc2.netBalance)
                assertEquals("ARS", acc2.currency)
                assertEquals(1, acc2.totalMovements)
            }
            .verifyComplete()
    }

    @Test
    fun `should return empty when there are no transactions`() {
        every { transactionRepositoryPort.findAll() } returns Flux.empty()

        StepVerifier.create(useCase.calculateFinalBalances())
            .expectNextCount(0)
            .verifyComplete()
    }
}
