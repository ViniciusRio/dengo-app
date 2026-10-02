package com.viniciusrio.dengo.data

import com.viniciusrio.dengo.model.CareRequest
import com.viniciusrio.dengo.model.CareRequestStatus
import com.viniciusrio.dengo.model.CareRequestType
import com.viniciusrio.dengo.model.CoupleState
import com.viniciusrio.dengo.model.HistoryEvent
import com.viniciusrio.dengo.model.Mood
import com.viniciusrio.dengo.model.MoodOption
import com.viniciusrio.dengo.model.MuralNote
import com.viniciusrio.dengo.model.MuralContent
import com.viniciusrio.dengo.model.DrawingStroke
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

    @Synchronized fun createMuralNote(authorId: PartnerId, text: String): MuralNote {
        val normalized = text.trim()
        require(normalized.isNotEmpty() && normalized.muralCodePointCount() <= MURAL_NOTE_MAX_CODE_POINTS)
        return appendMuralNote(authorId, MuralContent.Text(normalized))
    }

    @Synchronized fun createMuralDrawing(authorId: PartnerId, aspectRatio: Float, strokes: List<DrawingStroke>): MuralNote {
        require(aspectRatio.isFinite() && aspectRatio > 0f)
        require(strokes.isNotEmpty() && strokes.all { stroke ->
            (stroke.aspectRatio == null || (stroke.aspectRatio.isFinite() && stroke.aspectRatio > 0f)) &&
                stroke.points.isNotEmpty() && stroke.points.all { point ->
                point.x.isFinite() && point.y.isFinite() && point.x in 0f..1f && point.y in 0f..1f
            }
        })
        return appendMuralNote(authorId, MuralContent.Drawing(aspectRatio, strokes.map { it.copy(points = it.points.toList()) }))
    }

    private fun appendMuralNote(authorId: PartnerId, content: MuralContent): MuralNote {
        val note = MuralNote(
            id = nextMuralNoteId.getAndIncrement(),
            authorId = authorId,
            content = content,
            createdAt = clock.instant(),
        )
        mutableState.update { current -> current.copy(muralNotes = current.muralNotes + note) }
        return note
    }

    @Synchronized fun createRequest(type: CareRequestType, message: String? = null): CareRequest {
        require(type == CareRequestType.OTHER || message == null)
        val normalized = message?.trim()?.takeIf(String::isNotEmpty)
        require(type != CareRequestType.OTHER || normalized != null)
        var result: CareRequest? = null
        mutableState.update { current ->
            val active = current.requests.firstOrNull {
                type != CareRequestType.OTHER && it.type == type &&
                    it.status in setOf(CareRequestStatus.PENDING, CareRequestStatus.ACCEPTED)
            }
            if (active != null) {
                result = active
                current
            } else {
                val request = CareRequest(
                    id = nextRequestId.getAndIncrement(), type = type,
                    requesterId = PartnerId.LIDIANNE, recipientId = PartnerId.VINICIUS,
                    status = CareRequestStatus.PENDING, createdAt = clock.instant(), message = normalized,
                )
                result = request
                current.copy(
                    requests = current.requests + request,
                    history = current.history + HistoryEvent.RequestCreated(request.id, request.requesterId, request.createdAt),
                )
            }
        }
        return checkNotNull(result)
    }

    fun acceptRequest(requestId: Long) = respondToRequest(requestId, CareRequestStatus.ACCEPTED)

    fun declineRequest(requestId: Long) = respondToRequest(requestId, CareRequestStatus.DECLINED)

    @Synchronized private fun respondToRequest(requestId: Long, status: CareRequestStatus) {
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

    @Synchronized fun acknowledgeRequest(requestId: Long, actorId: PartnerId) {
        val now = clock.instant()
        mutableState.update { current ->
            val request = current.requests.firstOrNull { it.id == requestId }
            if (actorId != PartnerId.LIDIANNE || request?.requesterId != actorId ||
                request.status != CareRequestStatus.ACCEPTED
            ) current else current.copy(
                requests = current.requests.map {
                    if (it.id == requestId) it.copy(status = CareRequestStatus.ACKNOWLEDGED) else it
                },
                history = current.history + HistoryEvent.RequestAcknowledged(requestId, actorId, now),
            )
        }
    }

    @Synchronized fun setMood(option: MoodOption) {
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

    @Synchronized fun activatePersonalSpace() {
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

    @Synchronized fun endPersonalSpace() {
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
