package com.viniciusrio.dengo.data.sync

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.viniciusrio.dengo.model.CareRequest
import com.viniciusrio.dengo.model.CareRequestStatus
import com.viniciusrio.dengo.model.CareRequestType
import com.viniciusrio.dengo.model.HistoryEvent
import com.viniciusrio.dengo.model.Mood
import com.viniciusrio.dengo.model.MoodOption
import com.viniciusrio.dengo.model.PartnerId
import com.viniciusrio.dengo.model.PersonalSpace
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomSyncStoreTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    // Nunca o arquivo do app: cada teste usa um nome próprio e o apaga no fim.
    private val name = "dengo-sync-test-${UUID.randomUUID()}"
    private val opened = mutableListOf<SyncDatabase>()

    private fun open(): RoomSyncStore = RoomSyncStore(SyncDatabase.open(context, name).also(opened::add))

    @After fun deleteDatabase() {
        opened.forEach(SyncDatabase::close)
        context.deleteDatabase(name)
    }

    private val created = Instant.parse("2026-11-20T12:00:00Z")
    private val pending = CareRequest(7, CareRequestType.DENGO, PartnerId.LIDIANNE, PartnerId.VINICIUS, CareRequestStatus.PENDING, created)

    @Test fun pageAndCursorSurviveReopenAndANewerRequestStatusReplacesTheOldOne() = runBlocking {
        val store = open()
        store.apply(
            SyncPage(
                revision = 3,
                hasMore = false,
                requests = listOf(pending),
                history = listOf(SyncedHistoryEvent(11, HistoryEvent.RequestCreated(7, PartnerId.LIDIANNE, created))),
                mood = Mood(PartnerId.LIDIANNE, MoodOption.LOVING, LocalDate.of(2026, 11, 20)),
                personalSpace = PersonalSpace(PersonalSpace.Status.ACTIVE, created),
            ),
        )
        store.apply(
            SyncPage(
                revision = 5,
                hasMore = false,
                requests = listOf(pending.copy(status = CareRequestStatus.ACCEPTED)),
                // O mesmo acontecimento reenviado não duplica.
                history = listOf(
                    SyncedHistoryEvent(11, HistoryEvent.RequestCreated(7, PartnerId.LIDIANNE, created)),
                    SyncedHistoryEvent(12, HistoryEvent.RequestAccepted(7, PartnerId.VINICIUS, created)),
                ),
            ),
        )

        val copy = open().read()

        assertEquals(5, copy.revision)
        assertEquals(listOf(pending.copy(status = CareRequestStatus.ACCEPTED)), copy.state.requests)
        assertEquals(
            listOf(HistoryEvent.RequestCreated(7, PartnerId.LIDIANNE, created), HistoryEvent.RequestAccepted(7, PartnerId.VINICIUS, created)),
            copy.state.history,
        )
        assertEquals(MoodOption.LOVING, copy.state.mood?.option)
        assertEquals(PersonalSpace.Status.ACTIVE, copy.state.personalSpace.status)
        assertTrue(copy.state.muralNotes.isEmpty())
    }

    @Test fun aPageForASessionNoLongerActiveWritesNothing() = runBlocking {
        val store = open()

        val applied = store.apply(SyncPage(revision = 3, hasMore = false, requests = listOf(pending))) { false }

        assertFalse(applied)
        val copy = store.read()
        assertEquals(0, copy.revision)
        assertTrue(copy.state.requests.isEmpty())
    }

    @Test fun pendingOperationsSurviveReopenUntilDeleted() = runBlocking {
        val commands = listOf(
            PendingCommand.CreateRequest(CareRequestType.OTHER, "Sopa"),
            PendingCommand.CreateRequest(CareRequestType.DENGO, null),
            PendingCommand.AcceptRequest(7),
            PendingCommand.DeclineRequest(7),
            PendingCommand.AcknowledgeRequest(7),
            PendingCommand.SetMood(MoodOption.TIRED),
            PendingCommand.ActivatePersonalSpace,
            PendingCommand.EndPersonalSpace,
        )
        val store = open()
        commands.forEachIndexed { index, command -> store.savePending(PendingOperation("key-$index", command)) }
        store.savePending(PendingOperation("key-1", commands[1], confirmedRevision = 9))
        store.deletePending("key-0")

        val reopened = open().read().pending

        assertEquals(
            listOf(PendingOperation("key-1", commands[1], confirmedRevision = 9)) +
                commands.drop(2).mapIndexed { index, command -> PendingOperation("key-${index + 2}", command) },
            reopened,
        )
    }

    @Test fun clearRemovesTheCopyTheCursorAndThePendingOperations() = runBlocking {
        val store = open()
        store.apply(SyncPage(revision = 3, hasMore = false, requests = listOf(pending)))
        store.savePending(PendingOperation("key", PendingCommand.EndPersonalSpace))

        store.clear()

        val copy = open().read()
        assertEquals(0, copy.revision)
        assertTrue(copy.state.requests.isEmpty())
        assertTrue(copy.pending.isEmpty())
    }
}
