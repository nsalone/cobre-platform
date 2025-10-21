package com.cobre.accounts.application.usecase

import com.cobre.accounts.domain.model.Account
import com.cobre.accounts.domain.model.Transaction
import com.cobre.accounts.domain.port.repository.AccountRepositoryPort
import com.cobre.accounts.domain.port.repository.TransactionRepositoryPort
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.math.BigDecimal
import java.time.Instant

class ApplyTransferUseCaseTest {

    private val accountRepositoryPort: AccountRepositoryPort = mockk()
    private val transactionRepositoryPort: TransactionRepositoryPort = mockk()
    private val txOperator: TransactionalOperator = mockk()

    private lateinit var useCase: ApplyTransferUseCase

    @BeforeEach
    fun setup() {
        useCase = ApplyTransferUseCase(accountRepositoryPort, transactionRepositoryPort, txOperator)
        every { txOperator.transactional(any<Mono<Void>>()) } answers { firstArg() }
    }

    @Test
    fun `should execute transfer successfully`() {
        // Given
        val now = Instant.now()
        val origin = Account(accountId = "A1", balance = BigDecimal("1000"), currency = "USD", createdAt = now, updatedAt = now)
        val destination = Account(accountId = "A2", balance = BigDecimal("0"), currency = "ARS", createdAt = now, updatedAt = now)

        every { transactionRepositoryPort.findByIdempotencyKey(any()) } returns Mono.empty()
        every { accountRepositoryPort.findByAccountIdForUpdate("A1") } returns Mono.just(origin)
        every { accountRepositoryPort.findByAccountIdForUpdate("A2") } returns Mono.just(destination)
        every { transactionRepositoryPort.save(any()) } returns Mono.just(Transaction.debit("A1", "E1", BigDecimal.TEN, "USD", origin.balance))
        every { accountRepositoryPort.save(any()) } returns Mono.just(origin)

        // When & Then
        StepVerifier.create(
            useCase.execute(
                originAccountId = "A1",
                destinationAccountId = "A2",
                originAmount = BigDecimal.TEN,
                destinationAmount = BigDecimal("15000"),
                currencyOrigin = "USD",
                currencyDestination = "ARS",
                eventId = "E1"
            )
        ).verifyComplete()

        verify(exactly = 1) { accountRepositoryPort.findByAccountIdForUpdate("A1") }
        verify(exactly = 1) { accountRepositoryPort.findByAccountIdForUpdate("A2") }
        verify(atLeast = 2) { transactionRepositoryPort.save(any()) }
    }


    @Test
    fun `should fail when origin account not found`() {
        val now = Instant.now()
        // Given
        every { transactionRepositoryPort.findByIdempotencyKey(any()) } returns Mono.empty()
        every { accountRepositoryPort.findByAccountIdForUpdate("A1") } returns Mono.empty()
        every { accountRepositoryPort.findByAccountIdForUpdate("A2") } returns Mono.just(
            Account("asd", "A2", "ARS", BigDecimal.ZERO, now, now)
        )

        // When & Then
        StepVerifier.create(
            useCase.execute(
                originAccountId = "A1",
                destinationAccountId = "A2",
                originAmount = BigDecimal.TEN,
                destinationAmount = BigDecimal.TEN,
                currencyOrigin = "USD",
                currencyDestination = "ARS",
                eventId = "E1"
            )
        )
            .expectErrorMatches { ex ->
                ex is IllegalStateException && ex.message == "Origin account not found"
            }
            .verify()

        verify(exactly = 1) { accountRepositoryPort.findByAccountIdForUpdate("A1") }
    }

    @Test
    fun `should fail when origin currency mismatch`() {
        val now = Instant.now()
        val origin = Account("asd", "A1", currency = "EUR", balance = BigDecimal("100"), createdAt = now, updatedAt = now)
        val destination = Account("asd", "A2", currency = "ARS", balance = BigDecimal("0"), createdAt = now, updatedAt = now)

        every { transactionRepositoryPort.findByIdempotencyKey(any()) } returns Mono.empty()
        every { accountRepositoryPort.findByAccountIdForUpdate("A1") } returns Mono.just(origin)
        every { accountRepositoryPort.findByAccountIdForUpdate("A2") } returns Mono.just(destination)

        StepVerifier.create(
            useCase.execute(
                originAccountId = "A1",
                destinationAccountId = "A2",
                originAmount = BigDecimal("10"),
                destinationAmount = BigDecimal("100"),
                currencyOrigin = "USD", // Esperado: USD
                currencyDestination = "ARS",
                eventId = "E1"
            )
        )
            .expectErrorMatches { it is IllegalArgumentException && it.message!!.contains("Origin currency mismatch") }
            .verify()

        verify(exactly = 1) { accountRepositoryPort.findByAccountIdForUpdate("A1") }
    }

    @Test
    fun `should skip when idempotent transactions already exist`() {
        every { transactionRepositoryPort.findByIdempotencyKey(any()) } returnsMany listOf(Mono.just(mockk()), Mono.just(mockk()))

        StepVerifier.create(
            useCase.execute(
                originAccountId = "A1",
                destinationAccountId = "A2",
                originAmount = BigDecimal("10"),
                destinationAmount = BigDecimal("100"),
                currencyOrigin = "USD",
                currencyDestination = "ARS",
                eventId = "E1"
            )
        ).verifyComplete() // no error, solo no ejecuta flujos

        verify(exactly = 0) { accountRepositoryPort.findByAccountId(any()) }
        verify(exactly = 0) { transactionRepositoryPort.save(any()) }
    }
}
