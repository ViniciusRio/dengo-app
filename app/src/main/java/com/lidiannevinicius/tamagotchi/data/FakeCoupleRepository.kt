package com.lidiannevinicius.tamagotchi.data

import com.lidiannevinicius.tamagotchi.model.CareRequest
import com.lidiannevinicius.tamagotchi.model.CareRequestStatus
import com.lidiannevinicius.tamagotchi.model.CareRequestType
import com.lidiannevinicius.tamagotchi.model.CoupleState
import com.lidiannevinicius.tamagotchi.model.HistoryEvent
import com.lidiannevinicius.tamagotchi.model.Mood
import com.lidiannevinicius.tamagotchi.model.MoodOption
import com.lidiannevinicius.tamagotchi.model.Partner
import com.lidiannevinicius.tamagotchi.model.PartnerId
import com.lidiannevinicius.tamagotchi.model.PersonalSpace
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
        val request = CareRequest(
            id = nextRequestId.getAndIncrement(),
            type = type,
            requesterId = PartnerId.LIDIANNE,
            recipientId = PartnerId.VINICIUS,
            status = CareRequestStatus.REQUESTED,
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
        mutableState.update { current ->
            current.copy(mood = Mood(option, LocalDate.now(clock)))
        }
    }

    fun activatePersonalSpace() {
        mutableState.update { current ->
            if (current.personalSpace.isActive) current else current.copy(
                personalSpace = PersonalSpace(isActive = true, activatedAt = clock.instant()),
            )
        }
    }

    fun endPersonalSpace() {
        mutableState.update { current ->
            if (!current.personalSpace.isActive) current else current.copy(
                personalSpace = PersonalSpace(),
            )
        }
    }
}
