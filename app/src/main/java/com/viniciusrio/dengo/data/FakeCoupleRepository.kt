package com.viniciusrio.dengo.data

import com.viniciusrio.dengo.model.*
import java.time.Clock
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeCoupleRepository(
    private val clock: Clock = Clock.systemDefaultZone(),
    private var actor: PartnerId = PartnerId.LIDIANNE,
) : CoupleRepository {
    /** Só para testes: troca a pessoa da sessão, simulando o outro aparelho sobre o mesmo estado. */
    fun actingAs(person: PartnerId): FakeCoupleRepository = apply { actor = person }

    private val nextRequestId = AtomicLong(1)
    private val nextMuralNoteId = AtomicLong(1)
    private val mutableState = MutableStateFlow(CoupleState(partners = listOf(
        Partner(PartnerId.LIDIANNE, "Lidianne"), Partner(PartnerId.VINICIUS, "Vinícius"),
    )))
    private val mutableReadState = MutableStateFlow<RepositoryReadState>(RepositoryReadState.Ready(mutableState.value))
    override val state: StateFlow<RepositoryReadState> = mutableReadState.asStateFlow()

    override fun retryRead() { mutableReadState.value = RepositoryReadState.Ready(mutableState.value) }

    private fun updateState(transform: (CoupleState) -> CoupleState) {
        val current = mutableState.value
        val next = transform(current)
        if (next != current) {
            mutableState.value = next
            mutableReadState.value = RepositoryReadState.Ready(next)
        }
    }

    override suspend fun createMuralNote(text: String): WriteResult<MuralNote> = synchronized(this) {
        val normalized = text.trim()
        if (normalized.isEmpty() || normalized.muralCodePointCount() > MURAL_NOTE_MAX_CODE_POINTS) {
            return@synchronized WriteResult.Reverted("Invalid mural text")
        }
        WriteResult.Confirmed(appendMuralNote(actor, MuralContent.Text(normalized)), true)
    }

    override suspend fun createMuralDrawing(aspectRatio: Float, strokes: List<DrawingStroke>): WriteResult<MuralNote> = synchronized(this) {
        if (!aspectRatio.isFinite() || aspectRatio <= 0f || strokes.isEmpty() || !strokes.all { stroke ->
            (stroke.aspectRatio == null || (stroke.aspectRatio.isFinite() && stroke.aspectRatio > 0f)) &&
                stroke.points.isNotEmpty() && stroke.points.all { point ->
                    point.x.isFinite() && point.y.isFinite() && point.x in 0f..1f && point.y in 0f..1f
                }
        }) return@synchronized WriteResult.Reverted("Invalid mural drawing")
        val content = MuralContent.Drawing(aspectRatio, strokes.map { it.copy(points = it.points.toList()) })
        WriteResult.Confirmed(appendMuralNote(actor, content), true)
    }

    private fun appendMuralNote(authorId: PartnerId, content: MuralContent): MuralNote {
        val note = MuralNote(nextMuralNoteId.getAndIncrement(), authorId, content, clock.instant())
        updateState { it.copy(muralNotes = it.muralNotes + note) }
        return note
    }

    override suspend fun createRequest(type: CareRequestType, message: String?): WriteResult<CareRequest> = synchronized(this) {
        if (type != CareRequestType.OTHER && message != null) return@synchronized WriteResult.Reverted("Only OTHER accepts a message")
        val normalized = message?.trim()?.takeIf(String::isNotEmpty)
        if (type == CareRequestType.OTHER && normalized == null) return@synchronized WriteResult.Reverted("OTHER requires a message")
        val active = mutableState.value.requests.firstOrNull {
            type != CareRequestType.OTHER && it.type == type &&
                it.status in setOf(CareRequestStatus.PENDING, CareRequestStatus.ACCEPTED)
        }
        if (active != null) return@synchronized WriteResult.Confirmed(active, false)
        val request = CareRequest(
            nextRequestId.getAndIncrement(), type, PartnerId.LIDIANNE, PartnerId.VINICIUS,
            CareRequestStatus.PENDING, clock.instant(), normalized,
        )
        updateState { current -> current.copy(
            requests = current.requests + request,
            history = current.history + HistoryEvent.RequestCreated(request.id, request.requesterId, request.createdAt),
        ) }
        WriteResult.Confirmed(request, true)
    }

    override suspend fun acceptRequest(requestId: Long): WriteResult<Unit> = synchronized(this) {
        respondToRequest(requestId, CareRequestStatus.ACCEPTED)
    }

    override suspend fun declineRequest(requestId: Long): WriteResult<Unit> = synchronized(this) {
        respondToRequest(requestId, CareRequestStatus.DECLINED)
    }

    private fun respondToRequest(requestId: Long, status: CareRequestStatus): WriteResult<Unit> {
        val current = mutableState.value
        val request = current.requests.firstOrNull { it.id == requestId }
        if (current.personalSpace.status == PersonalSpace.Status.ACTIVE ||
            request?.recipientId != PartnerId.VINICIUS || request.status != CareRequestStatus.PENDING
        ) return WriteResult.Reverted("Request cannot be answered")
        val now = clock.instant()
        val event = when (status) {
            CareRequestStatus.ACCEPTED -> HistoryEvent.RequestAccepted(requestId, PartnerId.VINICIUS, now)
            CareRequestStatus.DECLINED -> HistoryEvent.RequestDeclined(requestId, PartnerId.VINICIUS, now)
            else -> error("Unsupported response status")
        }
        updateState { it.copy(
            requests = it.requests.map { existing -> if (existing.id == requestId) existing.copy(status = status) else existing },
            history = it.history + event,
        ) }
        return WriteResult.Confirmed(Unit, true)
    }

    override suspend fun acknowledgeRequest(requestId: Long): WriteResult<Unit> = synchronized(this) {
        val request = mutableState.value.requests.firstOrNull { it.id == requestId }
        if (actor != PartnerId.LIDIANNE || request?.requesterId != actor ||
            request.status != CareRequestStatus.ACCEPTED
        ) return@synchronized WriteResult.Reverted("Request cannot be acknowledged")
        val now = clock.instant()
        updateState { it.copy(
            requests = it.requests.map { existing -> if (existing.id == requestId) existing.copy(status = CareRequestStatus.ACKNOWLEDGED) else existing },
            history = it.history + HistoryEvent.RequestAcknowledged(requestId, actor, now),
        ) }
        WriteResult.Confirmed(Unit, true)
    }

    override suspend fun setMood(option: MoodOption): WriteResult<Unit> = synchronized(this) {
        val now = clock.instant()
        val mood = Mood(PartnerId.LIDIANNE, option, LocalDate.ofInstant(now, clock.zone))
        if (mutableState.value.mood == mood) return@synchronized WriteResult.Confirmed(Unit, false)
        updateState { it.copy(mood = mood, history = it.history + HistoryEvent.MoodChanged(option, mood.partnerId, now)) }
        WriteResult.Confirmed(Unit, true)
    }

    override suspend fun activatePersonalSpace(): WriteResult<Unit> = synchronized(this) {
        if (mutableState.value.personalSpace.status == PersonalSpace.Status.ACTIVE) return@synchronized WriteResult.Confirmed(Unit, false)
        val now = clock.instant()
        updateState { it.copy(
            personalSpace = PersonalSpace(PersonalSpace.Status.ACTIVE, now),
            history = it.history + HistoryEvent.PersonalSpaceActivated(PartnerId.LIDIANNE, now),
        ) }
        WriteResult.Confirmed(Unit, true)
    }

    override suspend fun endPersonalSpace(): WriteResult<Unit> = synchronized(this) {
        if (mutableState.value.personalSpace.status == PersonalSpace.Status.INACTIVE) return@synchronized WriteResult.Confirmed(Unit, false)
        val now = clock.instant()
        updateState { it.copy(
            personalSpace = PersonalSpace(),
            history = it.history + HistoryEvent.PersonalSpaceEnded(PartnerId.LIDIANNE, now),
        ) }
        WriteResult.Confirmed(Unit, true)
    }
}
