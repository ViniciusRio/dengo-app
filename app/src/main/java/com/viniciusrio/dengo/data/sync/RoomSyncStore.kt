package com.viniciusrio.dengo.data.sync

import androidx.room.withTransaction
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
import java.time.Instant
import java.time.LocalDate

internal class RoomSyncStore(private val database: SyncDatabase) : SyncStore {
    private val dao = database.dao()

    override suspend fun read(): LocalCopy = database.withTransaction {
        val requests = dao.requests().map { row ->
            CareRequest(
                row.id, CareRequestType.valueOf(row.type), PartnerId.valueOf(row.requesterId),
                PartnerId.valueOf(row.recipientId), CareRequestStatus.valueOf(row.status),
                Instant.parse(row.createdAtIso), row.message,
            )
        }
        val history = dao.history().map { row ->
            historyEvent(row.kind, PartnerId.valueOf(row.actorId), Instant.parse(row.occurredAtIso), row.requestId, row.moodOption)
        }
        val mood = dao.mood()?.let { Mood(PartnerId.valueOf(it.partnerId), MoodOption.valueOf(it.option), LocalDate.ofEpochDay(it.dateEpochDay)) }
        val personalSpace = dao.personalSpace()?.let {
            PersonalSpace(PersonalSpace.Status.valueOf(it.status), it.activatedAtIso?.let(Instant::parse))
        } ?: PersonalSpace()
        LocalCopy(
            state = CoupleState(
                partners = listOf(Partner(PartnerId.LIDIANNE, "Lidianne"), Partner(PartnerId.VINICIUS, "Vinícius")),
                requests = requests,
                mood = mood,
                personalSpace = personalSpace,
                history = history,
            ),
            revision = dao.cursor() ?: 0,
            pending = dao.pending().map { PendingOperation(it.opKey, decodePendingCommand(it.command), it.confirmedRevision) },
        )
    }

    override suspend fun apply(page: SyncPage, stillActive: () -> Boolean): Boolean = database.withTransaction {
        if (!stillActive()) return@withTransaction false
        dao.putRequests(page.requests.map {
            SyncRequestRow(it.id, it.type.name, it.requesterId.name, it.recipientId.name, it.status.name, it.createdAt.toString(), it.message)
        })
        dao.putHistory(page.history.map { (id, event) ->
            SyncHistoryRow(id, event.kind, event.requestId, (event as? HistoryEvent.MoodChanged)?.option?.name, event.actorId.name, event.occurredAt.toString())
        })
        page.mood?.let { dao.putMood(SyncMoodRow(partnerId = it.partnerId.name, option = it.option.name, dateEpochDay = it.date.toEpochDay())) }
        page.personalSpace?.let { dao.putPersonalSpace(SyncPersonalSpaceRow(status = it.status.name, activatedAtIso = it.activatedAt?.toString())) }
        dao.putCursor(SyncCursorRow(revision = page.revision))
        true
    }

    override suspend fun savePending(operation: PendingOperation) =
        dao.putPending(PendingOperationRow(operation.key, operation.command.encode(), operation.confirmedRevision))

    override suspend fun deletePending(key: String) = dao.deletePending(key)

    override suspend fun clear() = database.withTransaction { dao.deleteEverything() }
}
