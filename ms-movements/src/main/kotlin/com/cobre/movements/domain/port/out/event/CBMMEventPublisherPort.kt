package com.cobre.movements.domain.port.out.event

import com.cobre.movements.domain.model.event.CrossBorderMoneyMovementEvent
import reactor.core.publisher.Mono

interface CBMMEventPublisherPort {
    fun publishRequestedEvent(event: CrossBorderMoneyMovementEvent): Mono<Void>
}