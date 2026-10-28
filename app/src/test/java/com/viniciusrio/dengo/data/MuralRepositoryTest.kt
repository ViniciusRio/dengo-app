package com.viniciusrio.dengo.data

import com.viniciusrio.dengo.model.PartnerId
import com.viniciusrio.dengo.model.MuralContent
import com.viniciusrio.dengo.model.DrawingPoint
import com.viniciusrio.dengo.model.DrawingStroke
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlinx.coroutines.test.runTest

class MuralRepositoryTest {
    private val now = Instant.parse("2026-09-29T14:32:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)

    @Test fun initialMuralIsEmpty() = runTest {
        assertTrue(FakeCoupleRepository(clock).snapshot().muralNotes.isEmpty())
    }

    @Test fun drawingPublishesOnceWithAuthorTimeAndNoHistoryEvent() = runTest {
        val repository = FakeCoupleRepository(clock)
        val stroke = DrawingStroke(0xFFA63854.toInt(), listOf(DrawingPoint(0.2f, 0.3f)))
        val note = repository.createMuralDrawing(PartnerId.VINICIUS, 1.5f, listOf(stroke)).confirmed()

        assertEquals(1L, note.id)
        assertEquals(PartnerId.VINICIUS, note.authorId)
        assertEquals(now, note.createdAt)
        assertEquals(MuralContent.Drawing(1.5f, listOf(stroke)), note.content)
        assertEquals(listOf(note), repository.snapshot().muralNotes)
        assertTrue(repository.snapshot().history.isEmpty())
    }

    @Test fun emptyDrawingDoesNotPublishOrConsumeId() = runTest {
        val repository = FakeCoupleRepository(clock)
        assertTrue(repository.createMuralDrawing(PartnerId.LIDIANNE, 1f, emptyList()) is WriteResult.Reverted)
        assertTrue(repository.snapshot().muralNotes.isEmpty())
        assertEquals(1L, repository.createMuralNote(PartnerId.LIDIANNE, "Oi").confirmed().id)
    }

    @Test fun publishingStoresTrimmedTextAuthorSequentialIdAndFixedTime() = runTest {
        val repository = FakeCoupleRepository(clock)
        val first = repository.createMuralNote(PartnerId.LIDIANNE, "  Oi, meu amor ❤️  ").confirmed()
        val second = repository.createMuralNote(PartnerId.VINICIUS, "  Também te amo  ").confirmed()

        assertEquals(1L, first.id)
        assertEquals(2L, second.id)
        assertEquals(PartnerId.LIDIANNE, first.authorId)
        assertEquals(PartnerId.VINICIUS, second.authorId)
        assertEquals(MuralContent.Text("Oi, meu amor ❤️"), first.content)
        assertEquals(MuralContent.Text("Também te amo"), second.content)
        assertEquals(now, first.createdAt)
        assertEquals(listOf(first, second), repository.snapshot().muralNotes)
        assertTrue(repository.snapshot().history.isEmpty())
    }

    @Test fun emptyAndWhitespaceOnlyTextDoNotChangeStateOrConsumeId() = runTest {
        val repository = FakeCoupleRepository(clock)
        assertTrue(repository.createMuralNote(PartnerId.LIDIANNE, "") is WriteResult.Reverted)
        assertTrue(repository.createMuralNote(PartnerId.LIDIANNE, "  \n  ") is WriteResult.Reverted)
        assertTrue(repository.snapshot().muralNotes.isEmpty())
        assertEquals(1L, repository.createMuralNote(PartnerId.LIDIANNE, "Oi").confirmed().id)
    }

    @Test fun accepts160CodePointsIncludingEmojiAndRejects161WithoutTruncation() = runTest {
        val repository = FakeCoupleRepository(clock)
        val accepted = "a".repeat(159) + "❤️" // two code points, so this is 161
        assertTrue(repository.createMuralNote(PartnerId.LIDIANNE, accepted) is WriteResult.Reverted)
        val exact = "a".repeat(158) + "❤️"
        val note = repository.createMuralNote(PartnerId.LIDIANNE, exact).confirmed()

        assertEquals(MuralContent.Text(exact), note.content)
        assertEquals(160, (note.content as MuralContent.Text).value.codePointCount(0, exact.length))
        assertEquals(1L, note.id)
        assertEquals(listOf(note), repository.snapshot().muralNotes)

        val supplementaryEmoji = "💗".repeat(160)
        val emojiNote = repository.createMuralNote(PartnerId.VINICIUS, supplementaryEmoji).confirmed()
        assertEquals(MuralContent.Text(supplementaryEmoji), emojiNote.content)
        assertEquals(160, (emojiNote.content as MuralContent.Text).value.codePointCount(0, supplementaryEmoji.length))
    }

    @Test fun personalSpaceDoesNotBlockMuralOrCreateHistoryEvent() = runTest {
        val repository = FakeCoupleRepository(clock)
        repository.activatePersonalSpace()
        val historyBefore = repository.snapshot().history

        val note = repository.createMuralNote(PartnerId.VINICIUS, "Estou por aqui").confirmed()

        assertEquals(listOf(note), repository.snapshot().muralNotes)
        assertEquals(historyBefore, repository.snapshot().history)
    }

    @Test fun drawingDuringPersonalSpaceDoesNotAlterRequestsOrSpace() = runTest {
        val repository = FakeCoupleRepository(clock)
        repository.activatePersonalSpace()
        val before = repository.snapshot()
        val note = repository.createMuralDrawing(
            PartnerId.LIDIANNE, 1f,
            listOf(DrawingStroke(0xFF292426.toInt(), listOf(DrawingPoint(0.5f, 0.5f)))),
        ).confirmed()

        assertEquals(before.personalSpace, repository.snapshot().personalSpace)
        assertEquals(before.requests, repository.snapshot().requests)
        assertEquals(before.history, repository.snapshot().history)
        assertEquals(listOf(note), repository.snapshot().muralNotes)
    }
}
