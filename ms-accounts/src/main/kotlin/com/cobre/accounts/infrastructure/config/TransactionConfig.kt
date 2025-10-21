package com.cobre.accounts.infrastructure.config

import io.r2dbc.spi.ConnectionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.r2dbc.connection.R2dbcTransactionManager
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.support.DefaultTransactionDefinition

@Configuration
class TransactionConfig {

    @Bean
    fun transactionalOperator(connectionFactory: ConnectionFactory): TransactionalOperator {
        val txManager = R2dbcTransactionManager(connectionFactory)

        val definition = DefaultTransactionDefinition().apply {
            isolationLevel = Isolation.REPEATABLE_READ.value() // o READ_COMMITTED si DB no lo soporta
        }

        return TransactionalOperator.create(txManager, definition)
    }
}