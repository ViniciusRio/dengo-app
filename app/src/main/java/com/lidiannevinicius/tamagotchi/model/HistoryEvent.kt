package com.lidiannevinicius.tamagotchi.model

import java.time.Instant

sealed interface HistoryEvent {
    val actorId: PartnerId
    val occurredAt: Instant

    data class RequestCreated(
        val requestId: Long,
        override val actorId: PartnerId,
        override val occurredAt: Instant,
    ) : HistoryEvent
}
