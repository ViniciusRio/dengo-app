package com.viniciusrio.dengo.data

import com.viniciusrio.dengo.model.CareRequest
import com.viniciusrio.dengo.model.CareRequestStatus
import com.viniciusrio.dengo.model.CareRequestType
import com.viniciusrio.dengo.model.CoupleState
import com.viniciusrio.dengo.model.HistoryEvent
import com.viniciusrio.dengo.model.Mood
import com.viniciusrio.dengo.model.MoodOption
import com.viniciusrio.dengo.model.MuralNote
import com.viniciusrio.dengo.model.MURAL_NOTE_MAX_CODE_POINTS
import com.viniciusrio.dengo.model.Partner
import com.viniciusrio.dengo.model.PartnerId
import com.viniciusrio.dengo.model.PersonalSpace
import com.viniciusrio.dengo.model.muralCodePointCount
import java.time.Clock
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FakeCoupleRepository(private val clock: Clock = Clock.systemDefaultZone()) {
    private val nextRequestId = AtomicLong(1)
    private val nextMuralNoteId = AtomicLong(1)
    private val mutableState = MutableStateFlow(
        CoupleState(
            partners = listOf(
                Partner(PartnerId.LIDIANNE, "Lidianne"),
                Partner(PartnerId.VINICIUS, "Vinícius"),
            ),
        ),
    )

    val state: StateFlow<CoupleState> = mutableState.asStateFlow()

    fun createMuralNote(authorId: PartnerId, text: String): MuralNote {
        val normalized = text.trim()
        require(normalized.isNotEmpty() && normalized.muralCodePointCount() <= MURAL_NOTE_MAX_CODE_POINTS)
        val note = MuralNote(
            id = nextMuralNoteId.getAndIncrement(),
            authorId = authorId,
            text = normalized,
            createdAt = clock.instant(),
        )
        mutableState.update { current -> current.copy(muralNotes = current.muralNotes + note) }
        return note
    }

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

    fun acceptRequest(requestId: Long) = respondToRequest(requestId, CareRequestStatus.ACCEPTED)

    fun declineRequest(requestId: Long) = respondToRequest(requestId, CareRequestStatus.DECLINED)

    private fun respondToRequest(requestId: Long, status: CareRequestStatus) {
        val now = clock.instant()
        mutableState.update { current ->
            val request = current.requests.firstOrNull { it.id == requestId }
            if (current.personalSpace.status == PersonalSpace.Status.ACTIVE ||
                request?.recipientId != PartnerId.VINICIUS ||
                request.status != CareRequestStatus.PENDING
            ) {
                current
            } else {
                current.copy(
                    requests = current.requests.map { if (it.id == requestId) it.copy(status = status) else it },
                    history = current.history + when (status) {
                        CareRequestStatus.ACCEPTED -> HistoryEvent.RequestAccepted(requestId, PartnerId.VINICIUS, now)
                        CareRequestStatus.DECLINED -> HistoryEvent.RequestDeclined(requestId, PartnerId.VINICIUS, now)
                        else -> error("Unsupported response status")
                    },
                )
            }
        }
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
