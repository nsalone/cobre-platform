package com.cobre.accounts.application.usecase

import com.cobre.accounts.domain.model.Account
import com.cobre.accounts.domain.model.TransactionEvent
import com.cobre.accounts.domain.model.enums.TransactionType
import com.cobre.accounts.domain.port.repository.AccountRepositoryPort
import com.cobre.accounts.domain.port.repository.TransactionRepositoryPort
import com.cobre.accounts.infrastructure.controllers.dto.request.AccountProcessResult
import io.mockk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.math.BigDecimal
import java.time.Instant

class FinalBalanceCalculationUseCaseTest {

    private val accountRepositoryPort: AccountRepositoryPort = mockk()
    private val transactionRepositoryPort: TransactionRepositoryPort = mockk()
    private lateinit var useCase: FinalBalanceCalculationUseCase

    @BeforeEach
    fun setup() {
        useCase = FinalBalanceCalculationUseCase(accountRepositoryPort, transactionRepositoryPort)
    }

    @Test
    fun `should process events and update balance successfully`() {
        val now = Instant.now()
        val account = Account(
            id = "1",
            accountId = "ACC1",
            currency = "USD",
            balance = BigDecimal("100.00"),
            createdAt = now,
            updatedAt = now
        )

        val events = listOf(
            TransactionEvent("asd", "ACC1", BigDecimal("30.00"), "USD", TransactionType.DEBIT, now),
            TransactionEvent("asd2", "ACC1", BigDecimal("10.00"), "USD", TransactionType.CREDIT, now)
        )

        every { accountRepositoryPort.findByAccountIdForUpdate("ACC1") } returns Mono.just(account)
        every { accountRepositoryPort.save(any()) } returns Mono.just(account.copy(balance = BigDecimal("80.00")))
        every { transactionRepositoryPort.saveAll(any()) } returns Mono.empty()

        StepVerifier.create(useCase.process(Flux.fromIterable(events)))
            .verifyComplete()

        verify(exactly = 1) { accountRepositoryPort.save(any()) }
        verify(exactly = 1) { transactionRepositoryPort.saveAll(any()) }
    }

    @Test
    fun `should fail when account not found`() {
        val now = Instant.now()
        val events = listOf(
            TransactionEvent("asd", "ACC2", BigDecimal("30.00"), "USD", TransactionType.DEBIT, now)
        )

        every { accountRepositoryPort.findByAccountIdForUpdate("ACC2") } returns Mono.empty()

        StepVerifier.create(useCase.process(Flux.fromIterable(events)))
            .recordWith { mutableListOf<AccountProcessResult>() }
            .expectNextCount(1)
            .consumeRecordedWith { results ->
                val result = results.first()
                assertEquals("ACC2", result.accountId)
                assertEquals("FAILED", result.status)
                assert(result.message.contains("Account ACC2 not found"))
            }
            .verifyComplete()
    }

    @Test
    fun `should fail when currency mismatch`() {
        val now = Instant.now()
        val account = Account("1", "ACC3", "ARS", BigDecimal("100.00"), now, now)

        val events = listOf(
            TransactionEvent("asd", "ACC3", BigDecimal("30.00"), "USD", TransactionType.DEBIT, now)
        )
        every { accountRepositoryPort.findByAccountIdForUpdate("ACC3") } returns Mono.just(account)


        StepVerifier.create(useCase.process(Flux.fromIterable(events)))
            .recordWith { mutableListOf<AccountProcessResult>() }
            .expectNextCount(1)
            .consumeRecordedWith { results ->
                val result = results.first()
                assertEquals("ACC3", result.accountId)
                assertEquals("FAILED", result.status)
                assert(result.message.contains("Currency mismatch"))
            }
            .verifyComplete()
    }

    @Test
    fun `should fail when balance would go negative`() {
        val now = Instant.now()
        val account = Account("1", "ACC4", "USD", BigDecimal("20.00"), now, now)

        val events = listOf(
            TransactionEvent("asd", "ACC4", BigDecimal("30.00"), "USD", TransactionType.DEBIT, now)
        )
        every { accountRepositoryPort.findByAccountIdForUpdate("ACC4") } returns Mono.just(account)


        StepVerifier.create(useCase.process(Flux.fromIterable(events)))
            .recordWith { mutableListOf<AccountProcessResult>() }
            .expectNextCount(1)
            .consumeRecordedWith { results ->
                val result = results.first()
                assertEquals("ACC4", result.accountId)
                assertEquals("FAILED", result.status)
                assert(result.message.contains("Negative balance"))
            }
            .verifyComplete()
    }
}
