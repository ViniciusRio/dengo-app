package com.viniciusrio.dengo.data

import com.viniciusrio.dengo.model.PartnerId
import com.viniciusrio.dengo.model.MuralContent
import com.viniciusrio.dengo.model.DrawingPoint
import com.viniciusrio.dengo.model.DrawingStroke
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class MuralRepositoryTest {
    private val now = Instant.parse("2026-09-29T14:32:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)

    @Test fun initialMuralIsEmpty() {
        assertTrue(FakeCoupleRepository(clock).state.value.muralNotes.isEmpty())
    }

    @Test fun drawingPublishesOnceWithAuthorTimeAndNoHistoryEvent() {
        val repository = FakeCoupleRepository(clock)
        val stroke = DrawingStroke(0xFFA63854.toInt(), listOf(DrawingPoint(0.2f, 0.3f)))
        val note = repository.createMuralDrawing(PartnerId.VINICIUS, 1.5f, listOf(stroke))

        assertEquals(1L, note.id)
        assertEquals(PartnerId.VINICIUS, note.authorId)
        assertEquals(now, note.createdAt)
        assertEquals(MuralContent.Drawing(1.5f, listOf(stroke)), note.content)
        assertEquals(listOf(note), repository.state.value.muralNotes)
        assertTrue(repository.state.value.history.isEmpty())
    }

    @Test fun emptyDrawingDoesNotPublishOrConsumeId() {
        val repository = FakeCoupleRepository(clock)
        assertThrows(IllegalArgumentException::class.java) {
            repository.createMuralDrawing(PartnerId.LIDIANNE, 1f, emptyList())
        }
        assertTrue(repository.state.value.muralNotes.isEmpty())
        assertEquals(1L, repository.createMuralNote(PartnerId.LIDIANNE, "Oi").id)
    }

    @Test fun publishingStoresTrimmedTextAuthorSequentialIdAndFixedTime() {
        val repository = FakeCoupleRepository(clock)
        val first = repository.createMuralNote(PartnerId.LIDIANNE, "  Oi, meu amor ❤️  ")
        val second = repository.createMuralNote(PartnerId.VINICIUS, "  Também te amo  ")

        assertEquals(1L, first.id)
        assertEquals(2L, second.id)
        assertEquals(PartnerId.LIDIANNE, first.authorId)
        assertEquals(PartnerId.VINICIUS, second.authorId)
        assertEquals(MuralContent.Text("Oi, meu amor ❤️"), first.content)
        assertEquals(MuralContent.Text("Também te amo"), second.content)
        assertEquals(now, first.createdAt)
        assertEquals(listOf(first, second), repository.state.value.muralNotes)
        assertTrue(repository.state.value.history.isEmpty())
    }

    @Test fun emptyAndWhitespaceOnlyTextDoNotChangeStateOrConsumeId() {
        val repository = FakeCoupleRepository(clock)
        assertThrows(IllegalArgumentException::class.java) { repository.createMuralNote(PartnerId.LIDIANNE, "") }
        assertThrows(IllegalArgumentException::class.java) { repository.createMuralNote(PartnerId.LIDIANNE, "  \n  ") }
        assertTrue(repository.state.value.muralNotes.isEmpty())
        assertEquals(1L, repository.createMuralNote(PartnerId.LIDIANNE, "Oi").id)
    }

    @Test fun accepts160CodePointsIncludingEmojiAndRejects161WithoutTruncation() {
        val repository = FakeCoupleRepository(clock)
        val accepted = "a".repeat(159) + "❤️" // two code points, so this is 161
        assertThrows(IllegalArgumentException::class.java) {
            repository.createMuralNote(PartnerId.LIDIANNE, accepted)
        }
        val exact = "a".repeat(158) + "❤️"
        val note = repository.createMuralNote(PartnerId.LIDIANNE, exact)

        assertEquals(MuralContent.Text(exact), note.content)
        assertEquals(160, (note.content as MuralContent.Text).value.codePointCount(0, exact.length))
        assertEquals(1L, note.id)
        assertEquals(listOf(note), repository.state.value.muralNotes)

        val supplementaryEmoji = "💗".repeat(160)
        val emojiNote = repository.createMuralNote(PartnerId.VINICIUS, supplementaryEmoji)
        assertEquals(MuralContent.Text(supplementaryEmoji), emojiNote.content)
        assertEquals(160, (emojiNote.content as MuralContent.Text).value.codePointCount(0, supplementaryEmoji.length))
    }

    @Test fun personalSpaceDoesNotBlockMuralOrCreateHistoryEvent() {
        val repository = FakeCoupleRepository(clock)
        repository.activatePersonalSpace()
        val historyBefore = repository.state.value.history

        val note = repository.createMuralNote(PartnerId.VINICIUS, "Estou por aqui")

        assertEquals(listOf(note), repository.state.value.muralNotes)
        assertEquals(historyBefore, repository.state.value.history)
    }

    @Test fun drawingDuringPersonalSpaceDoesNotAlterRequestsOrSpace() {
        val repository = FakeCoupleRepository(clock)
        repository.activatePersonalSpace()
        val before = repository.state.value
        val note = repository.createMuralDrawing(
            PartnerId.LIDIANNE, 1f,
            listOf(DrawingStroke(0xFF292426.toInt(), listOf(DrawingPoint(0.5f, 0.5f)))),
        )

        assertEquals(before.personalSpace, repository.state.value.personalSpace)
        assertEquals(before.requests, repository.state.value.requests)
        assertEquals(before.history, repository.state.value.history)
        assertEquals(listOf(note), repository.state.value.muralNotes)
    }
}
