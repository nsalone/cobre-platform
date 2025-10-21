package com.cobre.movements.infrastructure.config

import io.r2dbc.spi.ConnectionFactory
import net.javacrumbs.shedlock.core.LockProvider
import net.javacrumbs.shedlock.provider.r2dbc.R2dbcLockProvider
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
@EnableSchedulerLock(defaultLockAtMostFor = "PT60S")
class ShedLockConfig {

    @Bean
    fun lockProvider(connectionFactory: ConnectionFactory?): LockProvider {
        return R2dbcLockProvider(connectionFactory!!)
    }

}

