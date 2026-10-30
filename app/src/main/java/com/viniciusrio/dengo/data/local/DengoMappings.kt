package com.viniciusrio.dengo.data.local

import com.viniciusrio.dengo.model.CareRequest
import com.viniciusrio.dengo.model.CareRequestStatus
import com.viniciusrio.dengo.model.CareRequestType
import com.viniciusrio.dengo.model.CoupleState
import com.viniciusrio.dengo.model.DrawingPoint
import com.viniciusrio.dengo.model.DrawingStroke
import com.viniciusrio.dengo.model.HistoryEvent
import com.viniciusrio.dengo.model.Mood
import com.viniciusrio.dengo.model.MoodOption
import com.viniciusrio.dengo.model.MuralContent
import com.viniciusrio.dengo.model.MuralNote
import com.viniciusrio.dengo.model.Partner
import com.viniciusrio.dengo.model.PartnerId
import com.viniciusrio.dengo.model.PersonalSpace
import java.time.Instant
import java.time.LocalDate

internal suspend fun DengoDao.loadCoupleState(): CoupleState {
    val requests = requests().map { row ->
        CareRequest(
            row.id, CareRequestType.valueOf(row.type), PartnerId.valueOf(row.requesterId),
            PartnerId.valueOf(row.recipientId), CareRequestStatus.valueOf(row.status),
            Instant.parse(row.createdAtIso), row.message,
        )
    }
    val history = history().map { row ->
        val actor = PartnerId.valueOf(row.actorId)
        val at = Instant.parse(row.occurredAtIso)
        when (row.kind) {
            "REQUEST_CREATED" -> HistoryEvent.RequestCreated(requireNotNull(row.requestId), actor, at)
            "REQUEST_ACCEPTED" -> HistoryEvent.RequestAccepted(requireNotNull(row.requestId), actor, at)
            "REQUEST_DECLINED" -> HistoryEvent.RequestDeclined(requireNotNull(row.requestId), actor, at)
            "REQUEST_ACKNOWLEDGED" -> HistoryEvent.RequestAcknowledged(requireNotNull(row.requestId), actor, at)
            "MOOD_CHANGED" -> HistoryEvent.MoodChanged(MoodOption.valueOf(requireNotNull(row.moodOption)), actor, at)
            "PERSONAL_SPACE_ACTIVATED" -> HistoryEvent.PersonalSpaceActivated(actor, at)
            "PERSONAL_SPACE_ENDED" -> HistoryEvent.PersonalSpaceEnded(actor, at)
            else -> error("Unknown history event kind: ${row.kind}")
        }
    }
    val mood = mood()?.let { Mood(PartnerId.valueOf(it.partnerId), MoodOption.valueOf(it.option), LocalDate.ofEpochDay(it.dateEpochDay)) }
    val personalSpace = personalSpace()?.let { row ->
        PersonalSpace(PersonalSpace.Status.valueOf(row.status), row.activatedAtIso?.let(Instant::parse))
    } ?: PersonalSpace()
    val strokesByNote = drawingStrokes().groupBy { it.noteId }
    val pointsByStroke = drawingPoints().groupBy { it.noteId to it.strokePosition }
    val notes = muralNotes().map { row ->
        val rows = strokesByNote[row.id].orEmpty()
        val content = when (row.kind) {
            "TEXT" -> {
                require(row.text != null && row.aspectRatio == null && rows.isEmpty())
                MuralContent.Text(row.text)
            }
            "DRAWING" -> {
                val ratio = requireNotNull(row.aspectRatio)
                require(row.text == null && ratio.isFinite() && ratio > 0f && rows.isNotEmpty())
                MuralContent.Drawing(ratio, rows.mapIndexed { index, stroke ->
                    require(stroke.position == index)
                    val points = pointsByStroke[row.id to stroke.position].orEmpty().mapIndexed { pointIndex, point ->
                        require(point.position == pointIndex && point.x.isFinite() && point.y.isFinite())
                        require(point.x in 0f..1f && point.y in 0f..1f)
                        DrawingPoint(point.x, point.y)
                    }
                    require(points.isNotEmpty())
                    require(stroke.aspectRatio == null || (stroke.aspectRatio.isFinite() && stroke.aspectRatio > 0f))
                    DrawingStroke(stroke.argb, points, stroke.aspectRatio)
                })
            }
            else -> error("Unknown mural note kind: ${row.kind}")
        }
        MuralNote(row.id, PartnerId.valueOf(row.authorId), content, Instant.parse(row.createdAtIso))
    }
    require(strokesByNote.keys.all { noteId -> notes.any { it.id == noteId && it.content is MuralContent.Drawing } })
    require(pointsByStroke.keys.all { (noteId, strokePosition) -> strokesByNote[noteId].orEmpty().any { it.position == strokePosition } })
    return CoupleState(
        partners = listOf(Partner(PartnerId.LIDIANNE, "Lidianne"), Partner(PartnerId.VINICIUS, "Vinícius")),
        requests = requests,
        mood = mood,
        personalSpace = personalSpace,
        history = history,
        muralNotes = notes,
    )
}

internal fun HistoryEvent.toRow(): HistoryEventRow = when (this) {
    is HistoryEvent.RequestCreated -> HistoryEventRow(kind = "REQUEST_CREATED", requestId = requestId, moodOption = null, actorId = actorId.name, occurredAtIso = occurredAt.toString())
    is HistoryEvent.RequestAccepted -> HistoryEventRow(kind = "REQUEST_ACCEPTED", requestId = requestId, moodOption = null, actorId = actorId.name, occurredAtIso = occurredAt.toString())
    is HistoryEvent.RequestDeclined -> HistoryEventRow(kind = "REQUEST_DECLINED", requestId = requestId, moodOption = null, actorId = actorId.name, occurredAtIso = occurredAt.toString())
    is HistoryEvent.RequestAcknowledged -> HistoryEventRow(kind = "REQUEST_ACKNOWLEDGED", requestId = requestId, moodOption = null, actorId = actorId.name, occurredAtIso = occurredAt.toString())
    is HistoryEvent.MoodChanged -> HistoryEventRow(kind = "MOOD_CHANGED", requestId = null, moodOption = option.name, actorId = actorId.name, occurredAtIso = occurredAt.toString())
    is HistoryEvent.PersonalSpaceActivated -> HistoryEventRow(kind = "PERSONAL_SPACE_ACTIVATED", requestId = null, moodOption = null, actorId = actorId.name, occurredAtIso = occurredAt.toString())
    is HistoryEvent.PersonalSpaceEnded -> HistoryEventRow(kind = "PERSONAL_SPACE_ENDED", requestId = null, moodOption = null, actorId = actorId.name, occurredAtIso = occurredAt.toString())
}
