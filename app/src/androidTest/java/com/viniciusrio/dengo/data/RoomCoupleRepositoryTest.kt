package com.viniciusrio.dengo.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.viniciusrio.dengo.data.local.DengoDatabase
import com.viniciusrio.dengo.data.local.RoomCoupleRepository
import com.viniciusrio.dengo.model.CareRequest
import com.viniciusrio.dengo.model.CareRequestStatus
import com.viniciusrio.dengo.model.CareRequestType
import com.viniciusrio.dengo.model.DrawingPoint
import com.viniciusrio.dengo.model.DrawingStroke
import com.viniciusrio.dengo.model.HistoryEvent
import com.viniciusrio.dengo.model.MoodOption
import com.viniciusrio.dengo.model.MuralContent
import com.viniciusrio.dengo.model.PartnerId
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomCoupleRepositoryTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun publishedStateSurvivesDatabaseReopen() = runBlocking {
        val name = "dengo-test-${UUID.randomUUID()}"
        val clock = Clock.fixed(Instant.parse("2026-10-29T15:00:00Z"), ZoneOffset.UTC)
        fun open() = DengoDatabase.open(context, name)
        val firstDatabase = open()
        val firstScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val first = RoomCoupleRepository(firstDatabase, firstScope, clock)
        assertTrue(first.awaitReady().requests.isEmpty())

        val request = first.createRequest(CareRequestType.DENGO) as WriteResult.Confirmed<CareRequest>
        assertTrue(request.changed)
        assertEquals(WriteResult.Confirmed(Unit, true), first.acceptRequest(request.value.id))
        assertEquals(WriteResult.Confirmed(Unit, true), first.acknowledgeRequest(request.value.id, PartnerId.LIDIANNE))
        assertEquals(WriteResult.Confirmed(Unit, true), first.setMood(MoodOption.HAPPY))
        assertEquals(WriteResult.Confirmed(Unit, true), first.activatePersonalSpace())
        assertTrue(first.createMuralNote(PartnerId.VINICIUS, "Oi ❤️") is WriteResult.Confirmed)
        val stroke = DrawingStroke(0xFFCC4466.toInt(), listOf(DrawingPoint(0.1f, 0.2f), DrawingPoint(0.8f, 0.9f)), 1.5f)
        assertTrue(first.createMuralDrawing(PartnerId.LIDIANNE, 2f, listOf(stroke)) is WriteResult.Confirmed)
        firstScope.cancel()
        firstDatabase.close()

        val secondDatabase = open()
        val secondScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        try {
            val second = RoomCoupleRepository(secondDatabase, secondScope, clock)
            val restored = second.awaitReady()
            assertEquals(CareRequestStatus.ACKNOWLEDGED, restored.requests.single().status)
            assertEquals(5, restored.history.size)
            assertTrue(restored.history[0] is HistoryEvent.RequestCreated)
            assertTrue(restored.history[2] is HistoryEvent.RequestAcknowledged)
            assertEquals(MoodOption.HAPPY, restored.mood?.option)
            assertTrue(restored.personalSpace.status.name == "ACTIVE")
            assertEquals(2, restored.muralNotes.size)
            assertEquals("Oi ❤️", (restored.muralNotes[0].content as MuralContent.Text).value)
            assertEquals(listOf(stroke), (restored.muralNotes[1].content as MuralContent.Drawing).strokes)
            assertEquals(2f, (restored.muralNotes[1].content as MuralContent.Drawing).aspectRatio)
            val repeated = second.createRequest(CareRequestType.DENGO) as WriteResult.Confirmed<CareRequest>
            assertTrue(repeated.changed)
            assertTrue(repeated.value.id > request.value.id)
            val newer = second.createRequest(CareRequestType.OTHER, "Outro") as WriteResult.Confirmed<CareRequest>
            assertTrue(newer.value.id > repeated.value.id)
        } finally {
            secondScope.cancel()
            secondDatabase.close()
        }
    }

    @Test fun failedHistoryInsertRollsBackRequestAndKeepsReadableState() = runBlocking {
        val database = DengoDatabase.open(context, "dengo-test-${UUID.randomUUID()}")
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        try {
            val repository = RoomCoupleRepository(database, scope)
            assertTrue(repository.awaitReady().requests.isEmpty())
            database.openHelper.writableDatabase.execSQL(
                "CREATE TRIGGER fail_history BEFORE INSERT ON history_events BEGIN SELECT RAISE(ABORT, 'injected'); END"
            )
            assertTrue(repository.createRequest(CareRequestType.DENGO) is WriteResult.Uncertain)
            database.openHelper.writableDatabase.execSQL("DROP TRIGGER fail_history")
            assertTrue(repository.state.value is RepositoryReadState.Ready)
            val restored = repository.awaitReady()
            assertTrue(restored.requests.isEmpty())
            assertTrue(restored.history.isEmpty())
            assertTrue(repository.createRequest(CareRequestType.DENGO) is WriteResult.Confirmed)
        } finally {
            scope.cancel()
            database.close()
        }
    }

    @Test fun failedDrawingPointInsertLeavesNoPartialNote() = runBlocking {
        val database = DengoDatabase.open(context, "dengo-test-${UUID.randomUUID()}")
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        try {
            val repository = RoomCoupleRepository(database, scope)
            repository.awaitReady()
            database.openHelper.writableDatabase.execSQL(
                "CREATE TRIGGER fail_point BEFORE INSERT ON drawing_points BEGIN SELECT RAISE(ABORT, 'injected'); END"
            )
            val stroke = DrawingStroke(0xFFCC4466.toInt(), listOf(DrawingPoint(0.3f, 0.4f)))
            assertTrue(repository.createMuralDrawing(PartnerId.LIDIANNE, 1f, listOf(stroke)) is WriteResult.Uncertain)
            database.openHelper.writableDatabase.execSQL("DROP TRIGGER fail_point")
            assertTrue(repository.state.value is RepositoryReadState.Ready)
            assertTrue(repository.awaitReady().muralNotes.isEmpty())
            assertTrue(repository.createMuralNote(PartnerId.VINICIUS, "Depois") is WriteResult.Confirmed)
        } finally {
            scope.cancel()
            database.close()
        }
    }

    @Test fun transientReadErrorCanRetryOnSameRepository() = runBlocking {
        val database = DengoDatabase.open(context, "dengo-test-${UUID.randomUUID()}")
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        try {
            val repository = RoomCoupleRepository(database, scope)
            repository.awaitReady()
            database.openHelper.writableDatabase.execSQL(
                "INSERT INTO care_requests (type, requesterId, recipientId, status, createdAtIso, message) " +
                    "VALUES ('UNKNOWN', 'LIDIANNE', 'VINICIUS', 'PENDING', '2026-10-29T15:00:00Z', NULL)"
            )
            repository.retryRead()
            withTimeout(10_000) { repository.state.first { it is RepositoryReadState.ReadError } }
            assertTrue(repository.state.value is RepositoryReadState.ReadError)
            database.openHelper.writableDatabase.execSQL("UPDATE care_requests SET type = 'DENGO' WHERE type = 'UNKNOWN'")
            repository.retryRead()
            assertEquals(CareRequestType.DENGO, repository.awaitReady().requests.single().type)
        } finally {
            scope.cancel()
            database.close()
        }
    }

    @Test fun corruptedFileIsPreservedWhenOpenFails() = runBlocking {
        val name = "dengo-test-corrupt-${UUID.randomUUID()}"
        val initialDatabase = DengoDatabase.open(context, name)
        val initialScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val initialRepository = RoomCoupleRepository(initialDatabase, initialScope)
        initialRepository.awaitReady()
        assertTrue(initialRepository.createMuralNote(PartnerId.VINICIUS, "Conteúdo anterior") is WriteResult.Confirmed)
        initialScope.cancel()
        initialDatabase.close()

        val file = context.getDatabasePath(name)
        val corrupted = file.readBytes().also { bytes ->
            require(bytes.size > 128)
            "not-a-sqlite-db".toByteArray().copyInto(bytes, destinationOffset = 0)
        }
        file.writeBytes(corrupted)
        val database = DengoDatabase.open(context, name)
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        try {
            val repository = RoomCoupleRepository(database, scope)
            withTimeout(10_000) { repository.state.first { it is RepositoryReadState.ReadError } }
            assertTrue(file.exists())
            assertArrayEquals(corrupted, file.readBytes())
        } finally {
            scope.cancel()
            database.close()
        }
    }

    @Test fun concurrentEquivalentRequestsCreateOneRowAndOneEvent() = runBlocking {
        val database = DengoDatabase.open(context, "dengo-test-${UUID.randomUUID()}")
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        try {
            val repository = RoomCoupleRepository(database, scope)
            repository.awaitReady()
            val outcomes = coroutineScope {
                val first = async { repository.createRequest(CareRequestType.MEDICINE) }
                val second = async { repository.createRequest(CareRequestType.MEDICINE) }
                listOf(first.await(), second.await())
            }
            assertEquals(1, outcomes.count { it is WriteResult.Confirmed && it.changed })
            assertEquals(1, outcomes.count { it is WriteResult.Confirmed && !it.changed })
            assertEquals(1, repository.awaitReady().requests.size)
            assertEquals(1, repository.awaitReady().history.size)
        } finally {
            scope.cancel()
            database.close()
        }
    }

    @Test fun activePresetRequestStillBlocksDuplicateAfterReopen() = runBlocking {
        val name = "dengo-test-${UUID.randomUUID()}"
        val firstDatabase = DengoDatabase.open(context, name)
        val firstScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val first = RoomCoupleRepository(firstDatabase, firstScope)
        first.awaitReady()
        val original = first.createRequest(CareRequestType.HOT_WATER_BAG) as WriteResult.Confirmed<CareRequest>
        firstScope.cancel()
        firstDatabase.close()

        val reopened = DengoDatabase.open(context, name)
        val secondScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        try {
            val second = RoomCoupleRepository(reopened, secondScope)
            second.awaitReady()
            val duplicate = second.createRequest(CareRequestType.HOT_WATER_BAG) as WriteResult.Confirmed<CareRequest>
            assertFalse(duplicate.changed)
            assertEquals(original.value.id, duplicate.value.id)
            assertEquals(1, second.awaitReady().history.size)
        } finally {
            secondScope.cancel()
            reopened.close()
        }
    }

    @Test fun domainRejectionsAndNoOpWritesDoNotAddEvents() = runBlocking {
        val database = DengoDatabase.open(context, "dengo-test-${UUID.randomUUID()}")
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        try {
            val repository = RoomCoupleRepository(database, scope)
            repository.awaitReady()
            assertTrue(repository.createRequest(CareRequestType.OTHER, " ") is WriteResult.Reverted)
            assertTrue(repository.createMuralNote(PartnerId.LIDIANNE, " ") is WriteResult.Reverted)
            assertTrue(repository.createMuralDrawing(PartnerId.LIDIANNE, 1f, emptyList()) is WriteResult.Reverted)
            assertEquals(WriteResult.Confirmed(Unit, true), repository.setMood(MoodOption.SAD))
            assertEquals(WriteResult.Confirmed(Unit, false), repository.setMood(MoodOption.SAD))
            assertEquals(WriteResult.Confirmed(Unit, true), repository.activatePersonalSpace())
            assertEquals(WriteResult.Confirmed(Unit, false), repository.activatePersonalSpace())
            val request = repository.createRequest(CareRequestType.DENGO) as WriteResult.Confirmed<CareRequest>
            assertTrue(repository.acceptRequest(request.value.id) is WriteResult.Reverted)
            assertEquals(3, repository.awaitReady().history.size)
        } finally {
            scope.cancel()
            database.close()
        }
    }

    @Test fun badReadDoesNotReplaceExistingDatabaseWithEmptyState() = runBlocking {
        val name = "dengo-test-${UUID.randomUUID()}"
        val database = DengoDatabase.open(context, name)
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val repository = RoomCoupleRepository(database, scope)
        repository.awaitReady()
        repository.createMuralNote(PartnerId.VINICIUS, "Persistido")
        database.close()
        repository.retryRead()
        withTimeout(10_000) { repository.state.first { it is RepositoryReadState.ReadError } }
        assertEquals(1, (repository.state.value as RepositoryReadState.ReadError).lastKnownState?.muralNotes?.size)
        scope.cancel()

        val reopened = DengoDatabase.open(context, name)
        val secondScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        try {
            assertEquals(1, RoomCoupleRepository(reopened, secondScope).awaitReady().muralNotes.size)
        } finally {
            secondScope.cancel()
            reopened.close()
        }
    }

    private suspend fun CoupleRepository.awaitReady() = withTimeout(10_000) {
        (state.first { it is RepositoryReadState.Ready } as RepositoryReadState.Ready).value
    }
}
