package com.cobre.accounts.infrastructure.persistence

import com.cobre.accounts.infrastructure.persistence.entity.AccountEntity
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono
import java.math.BigDecimal

interface AccountR2dbcRepository : ReactiveCrudRepository<AccountEntity, String> {
    fun findByAccountId(accountId: String): Mono<AccountEntity>

    @Query("SELECT * FROM accounts WHERE account_id = :accountId FOR UPDATE")
    fun findByAccountIdForUpdate(accountId: String): Mono<AccountEntity>

    @Modifying
    @Query("UPDATE accounts SET balance = balance + :amount WHERE account_id = :accountId")
    fun addToBalance(accountId: String, amount: BigDecimal): Mono<Int>

    @Modifying
    @Query("UPDATE accounts SET balance = balance - :amount WHERE account_id = :accountId")
    fun subtractFromBalance(accountId: String, amount: BigDecimal): Mono<Int>

}