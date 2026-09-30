package com.lidiannevinicius.dengo.model

import java.time.Instant

sealed interface HistoryEvent {
    val actorId: PartnerId
    val occurredAt: Instant

    data class RequestCreated(
        val requestId: Long,
        override val actorId: PartnerId,
        override val occurredAt: Instant,
    ) : HistoryEvent

    data class MoodChanged(
        val option: MoodOption,
        override val actorId: PartnerId,
        override val occurredAt: Instant,
    ) : HistoryEvent

    data class PersonalSpaceActivated(
        override val actorId: PartnerId,
        override val occurredAt: Instant,
    ) : HistoryEvent

    data class PersonalSpaceEnded(
        override val actorId: PartnerId,
        override val occurredAt: Instant,
    ) : HistoryEvent
}
