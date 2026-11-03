package com.viniciusrio.dengo.data.local

import androidx.room.withTransaction
import com.viniciusrio.dengo.data.CoupleRepository
import com.viniciusrio.dengo.data.RepositoryReadState
import com.viniciusrio.dengo.data.WriteResult
import com.viniciusrio.dengo.model.CareRequest
import com.viniciusrio.dengo.model.CareRequestStatus
import com.viniciusrio.dengo.model.CareRequestType
import com.viniciusrio.dengo.model.CoupleState
import com.viniciusrio.dengo.model.DrawingStroke
import com.viniciusrio.dengo.model.HistoryEvent
import com.viniciusrio.dengo.model.Mood
import com.viniciusrio.dengo.model.MoodOption
import com.viniciusrio.dengo.model.MuralContent
import com.viniciusrio.dengo.model.MuralNote
import com.viniciusrio.dengo.model.PartnerId
import com.viniciusrio.dengo.model.PersonalSpace
import com.viniciusrio.dengo.model.MURAL_NOTE_MAX_CODE_POINTS
import com.viniciusrio.dengo.model.muralCodePointCount
import java.time.Clock
import java.time.LocalDate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class RoomCoupleRepository(
    private val actor: PartnerId,
    private val database: DengoDatabase,
    private val scope: CoroutineScope,
    private val clock: Clock = Clock.systemDefaultZone(),
) : CoupleRepository {
    private val dao = database.dao()
    private val mutex = Mutex()
    private val mutableState = MutableStateFlow<RepositoryReadState>(RepositoryReadState.Loading)
    override val state: StateFlow<RepositoryReadState> = mutableState.asStateFlow()

    init { retryRead() }

    override fun retryRead() {
        scope.launch {
            mutex.withLock {
                val lastKnown = when (val current = mutableState.value) {
                    is RepositoryReadState.Ready -> current.value
                    is RepositoryReadState.ReadError -> current.lastKnownState
                    RepositoryReadState.Loading -> null
                }
                mutableState.value = RepositoryReadState.Loading
                try {
                    mutableState.value = RepositoryReadState.Ready(database.withTransaction { dao.loadCoupleState() })
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (failure: Exception) {
                    mutableState.value = RepositoryReadState.ReadError(failure, lastKnown)
                }
            }
        }
    }

    private suspend fun <T> write(block: suspend (CoupleState) -> WriteResult<T>): WriteResult<T> = mutex.withLock {
        if (mutableState.value !is RepositoryReadState.Ready) return@withLock WriteResult.Reverted("Read unavailable")
        val result = try {
            database.withTransaction { block(dao.loadCoupleState()) }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: Exception) {
            val lastKnown = (mutableState.value as? RepositoryReadState.Ready)?.value
            try {
                mutableState.value = RepositoryReadState.Ready(database.withTransaction { dao.loadCoupleState() })
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (readFailure: Exception) {
                mutableState.value = RepositoryReadState.ReadError(readFailure, lastKnown)
            }
            return@withLock WriteResult.Uncertain(failure)
        }
        if (result is WriteResult.Confirmed && result.changed) {
            try {
                mutableState.value = RepositoryReadState.Ready(database.withTransaction { dao.loadCoupleState() })
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                mutableState.value = RepositoryReadState.ReadError(failure, (mutableState.value as? RepositoryReadState.Ready)?.value)
            }
        }
        result
    }

    override suspend fun createRequest(type: CareRequestType, message: String?): WriteResult<CareRequest> = write { current ->
        if (type != CareRequestType.OTHER && message != null) return@write WriteResult.Reverted("Only OTHER accepts a message")
        val normalized = message?.trim()?.takeIf(String::isNotEmpty)
        if (type == CareRequestType.OTHER && normalized == null) return@write WriteResult.Reverted("OTHER requires a message")
        val active = current.requests.firstOrNull {
            type != CareRequestType.OTHER && it.type == type &&
                it.status in setOf(CareRequestStatus.PENDING, CareRequestStatus.ACCEPTED)
        }
        if (active != null) return@write WriteResult.Confirmed(active, false)
        val now = clock.instant()
        val id = dao.insertRequest(CareRequestRow(
            type = type.name, requesterId = PartnerId.LIDIANNE.name, recipientId = PartnerId.VINICIUS.name,
            status = CareRequestStatus.PENDING.name, createdAtIso = now.toString(), message = normalized,
        ))
        val request = CareRequest(id, type, PartnerId.LIDIANNE, PartnerId.VINICIUS, CareRequestStatus.PENDING, now, normalized)
        dao.insertHistory(HistoryEvent.RequestCreated(id, PartnerId.LIDIANNE, now).toRow())
        WriteResult.Confirmed(request, true)
    }

    override suspend fun acceptRequest(requestId: Long): WriteResult<Unit> = respond(requestId, CareRequestStatus.ACCEPTED)
    override suspend fun declineRequest(requestId: Long): WriteResult<Unit> = respond(requestId, CareRequestStatus.DECLINED)

    private suspend fun respond(requestId: Long, status: CareRequestStatus): WriteResult<Unit> = write { current ->
        val request = current.requests.firstOrNull { it.id == requestId }
        if (current.personalSpace.status == PersonalSpace.Status.ACTIVE ||
            request?.recipientId != PartnerId.VINICIUS || request.status != CareRequestStatus.PENDING
        ) return@write WriteResult.Reverted("Request cannot be answered")
        val now = clock.instant()
        dao.updateRequestStatus(requestId, status.name)
        val event = when (status) {
            CareRequestStatus.ACCEPTED -> HistoryEvent.RequestAccepted(requestId, PartnerId.VINICIUS, now)
            CareRequestStatus.DECLINED -> HistoryEvent.RequestDeclined(requestId, PartnerId.VINICIUS, now)
            else -> error("Unsupported response status")
        }
        dao.insertHistory(event.toRow())
        WriteResult.Confirmed(Unit, true)
    }

    override suspend fun acknowledgeRequest(requestId: Long): WriteResult<Unit> = write { current ->
        val request = current.requests.firstOrNull { it.id == requestId }
        if (actor != PartnerId.LIDIANNE || request?.requesterId != actor ||
            request.status != CareRequestStatus.ACCEPTED
        ) return@write WriteResult.Reverted("Request cannot be acknowledged")
        val now = clock.instant()
        dao.updateRequestStatus(requestId, CareRequestStatus.ACKNOWLEDGED.name)
        dao.insertHistory(HistoryEvent.RequestAcknowledged(requestId, actor, now).toRow())
        WriteResult.Confirmed(Unit, true)
    }

    override suspend fun setMood(option: MoodOption): WriteResult<Unit> = write { current ->
        val now = clock.instant()
        val mood = Mood(PartnerId.LIDIANNE, option, LocalDate.ofInstant(now, clock.zone))
        if (current.mood == mood) return@write WriteResult.Confirmed(Unit, false)
        dao.putMood(MoodRow(partnerId = mood.partnerId.name, option = option.name, dateEpochDay = mood.date.toEpochDay()))
        dao.insertHistory(HistoryEvent.MoodChanged(option, mood.partnerId, now).toRow())
        WriteResult.Confirmed(Unit, true)
    }

    override suspend fun activatePersonalSpace(): WriteResult<Unit> = write { current ->
        if (current.personalSpace.status == PersonalSpace.Status.ACTIVE) return@write WriteResult.Confirmed(Unit, false)
        val now = clock.instant()
        dao.putPersonalSpace(PersonalSpaceRow(status = PersonalSpace.Status.ACTIVE.name, activatedAtIso = now.toString()))
        dao.insertHistory(HistoryEvent.PersonalSpaceActivated(PartnerId.LIDIANNE, now).toRow())
        WriteResult.Confirmed(Unit, true)
    }

    override suspend fun endPersonalSpace(): WriteResult<Unit> = write { current ->
        if (current.personalSpace.status == PersonalSpace.Status.INACTIVE) return@write WriteResult.Confirmed(Unit, false)
        val now = clock.instant()
        dao.putPersonalSpace(PersonalSpaceRow(status = PersonalSpace.Status.INACTIVE.name, activatedAtIso = null))
        dao.insertHistory(HistoryEvent.PersonalSpaceEnded(PartnerId.LIDIANNE, now).toRow())
        WriteResult.Confirmed(Unit, true)
    }

    override suspend fun createMuralNote(text: String): WriteResult<MuralNote> = write {
        val normalized = text.trim()
        if (normalized.isEmpty() || normalized.muralCodePointCount() > MURAL_NOTE_MAX_CODE_POINTS) {
            return@write WriteResult.Reverted("Invalid mural text")
        }
        val now = clock.instant()
        val id = dao.insertMuralNote(MuralNoteRow(
            authorId = actor.name, kind = "TEXT", text = normalized, aspectRatio = null,
            createdAtIso = now.toString(),
        ))
        WriteResult.Confirmed(MuralNote(id, actor, MuralContent.Text(normalized), now), true)
    }

    override suspend fun createMuralDrawing(aspectRatio: Float, strokes: List<DrawingStroke>): WriteResult<MuralNote> = write {
        if (!aspectRatio.isFinite() || aspectRatio <= 0f || strokes.isEmpty() || !strokes.all { stroke ->
            (stroke.aspectRatio == null || (stroke.aspectRatio.isFinite() && stroke.aspectRatio > 0f)) &&
                stroke.points.isNotEmpty() && stroke.points.all { point ->
                    point.x.isFinite() && point.y.isFinite() && point.x in 0f..1f && point.y in 0f..1f
                }
        }) return@write WriteResult.Reverted("Invalid mural drawing")
        val now = clock.instant()
        val copy = strokes.map { it.copy(points = it.points.toList()) }
        val id = dao.insertMuralNote(MuralNoteRow(
            authorId = actor.name, kind = "DRAWING", text = null, aspectRatio = aspectRatio,
            createdAtIso = now.toString(),
        ))
        dao.insertStrokes(copy.mapIndexed { index, stroke -> DrawingStrokeRow(id, index, stroke.argb, stroke.aspectRatio) })
        dao.insertPoints(copy.flatMapIndexed { strokeIndex, stroke ->
            stroke.points.mapIndexed { pointIndex, point -> DrawingPointRow(id, strokeIndex, pointIndex, point.x, point.y) }
        })
        WriteResult.Confirmed(MuralNote(id, actor, MuralContent.Drawing(aspectRatio, copy), now), true)
    }
}
