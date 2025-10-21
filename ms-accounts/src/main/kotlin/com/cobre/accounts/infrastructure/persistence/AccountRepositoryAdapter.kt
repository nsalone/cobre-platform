package com.cobre.accounts.infrastructure.persistence

import com.cobre.accounts.domain.model.Account
import com.cobre.accounts.domain.port.repository.AccountRepositoryPort
import com.cobre.accounts.infrastructure.persistence.mapper.toDomain
import com.cobre.accounts.infrastructure.persistence.mapper.toEntity
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.math.BigDecimal

@Component
class AccountRepositoryAdapter(
    private val repo: AccountR2dbcRepository
) : AccountRepositoryPort {

    override fun findByAccountId(accountId: String): Mono<Account> =
        repo.findByAccountId(accountId).map { it.toDomain() }

    override fun save(account: Account): Mono<Account> =
        repo.save(account.toEntity()).map { it.toDomain() }

    override fun findByAccountIdForUpdate(accountId: String): Mono<Account> =
        repo.findByAccountIdForUpdate(accountId).map { it.toDomain() }

    override fun addToBalance(accountId: String, amount: BigDecimal): Mono<Void> =
        repo.addToBalance(accountId, amount).then()

    override fun subtractFromBalance(accountId: String, amount: BigDecimal): Mono<Void> =
        repo.subtractFromBalance(accountId, amount).then()

}
