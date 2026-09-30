package com.viniciusrio.dengo.data

import com.viniciusrio.dengo.model.CareRequest
import com.viniciusrio.dengo.model.CareRequestStatus
import com.viniciusrio.dengo.model.CareRequestType
import com.viniciusrio.dengo.model.CoupleState
import com.viniciusrio.dengo.model.HistoryEvent
import com.viniciusrio.dengo.model.Mood
import com.viniciusrio.dengo.model.MoodOption
import com.viniciusrio.dengo.model.Partner
import com.viniciusrio.dengo.model.PartnerId
import com.viniciusrio.dengo.model.PersonalSpace
import java.time.Clock
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FakeCoupleRepository(private val clock: Clock = Clock.systemDefaultZone()) {
    private val nextRequestId = AtomicLong(1)
    private val mutableState = MutableStateFlow(
        CoupleState(
            partners = listOf(
                Partner(PartnerId.LIDIANNE, "Lidianne"),
                Partner(PartnerId.VINICIUS, "Vinícius"),
            ),
        ),
    )

    val state: StateFlow<CoupleState> = mutableState.asStateFlow()

    fun createRequest(type: CareRequestType, message: String? = null): CareRequest {
        require(type == CareRequestType.OTHER || message == null)
        val request = CareRequest(
            id = nextRequestId.getAndIncrement(),
            type = type,
            requesterId = PartnerId.LIDIANNE,
            recipientId = PartnerId.VINICIUS,
            status = CareRequestStatus.PENDING,
            createdAt = clock.instant(),
            message = message?.trim()?.takeIf(String::isNotEmpty),
        )
        mutableState.update { current ->
            current.copy(
                requests = current.requests + request,
                history = current.history + HistoryEvent.RequestCreated(
                    requestId = request.id,
                    actorId = request.requesterId,
                    occurredAt = request.createdAt,
                ),
            )
        }
        return request
    }

    fun setMood(option: MoodOption) {
        val now = clock.instant()
        val mood = Mood(PartnerId.LIDIANNE, option, LocalDate.ofInstant(now, clock.zone))
        mutableState.update { current ->
            if (current.mood == mood) current else current.copy(
                mood = mood,
                history = current.history + HistoryEvent.MoodChanged(
                    option = option,
                    actorId = mood.partnerId,
                    occurredAt = now,
                ),
            )
        }
    }

    fun activatePersonalSpace() {
        val now = clock.instant()
        mutableState.update { current ->
            if (current.personalSpace.status == PersonalSpace.Status.ACTIVE) current else current.copy(
                personalSpace = PersonalSpace(PersonalSpace.Status.ACTIVE, now),
                history = current.history + HistoryEvent.PersonalSpaceActivated(
                    actorId = PartnerId.LIDIANNE,
                    occurredAt = now,
                ),
            )
        }
    }

    fun endPersonalSpace() {
        val now = clock.instant()
        mutableState.update { current ->
            if (current.personalSpace.status == PersonalSpace.Status.INACTIVE) current else current.copy(
                personalSpace = PersonalSpace(),
                history = current.history + HistoryEvent.PersonalSpaceEnded(
                    actorId = PartnerId.LIDIANNE,
                    occurredAt = now,
                ),
            )
        }
    }
}
