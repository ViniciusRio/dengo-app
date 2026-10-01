package com.viniciusrio.dengo.data

import com.viniciusrio.dengo.model.PartnerId
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

    @Test fun publishingStoresTrimmedTextAuthorSequentialIdAndFixedTime() {
        val repository = FakeCoupleRepository(clock)
        val first = repository.createMuralNote(PartnerId.LIDIANNE, "  Oi, meu amor ❤️  ")
        val second = repository.createMuralNote(PartnerId.VINICIUS, "  Também te amo  ")

        assertEquals(1L, first.id)
        assertEquals(2L, second.id)
        assertEquals(PartnerId.LIDIANNE, first.authorId)
        assertEquals(PartnerId.VINICIUS, second.authorId)
        assertEquals("Oi, meu amor ❤️", first.text)
        assertEquals("Também te amo", second.text)
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

        assertEquals(exact, note.text)
        assertEquals(160, note.text.codePointCount(0, note.text.length))
        assertEquals(1L, note.id)
        assertEquals(listOf(note), repository.state.value.muralNotes)

        val supplementaryEmoji = "💗".repeat(160)
        val emojiNote = repository.createMuralNote(PartnerId.VINICIUS, supplementaryEmoji)
        assertEquals(supplementaryEmoji, emojiNote.text)
        assertEquals(160, emojiNote.text.codePointCount(0, emojiNote.text.length))
    }

    @Test fun personalSpaceDoesNotBlockMuralOrCreateHistoryEvent() {
        val repository = FakeCoupleRepository(clock)
        repository.activatePersonalSpace()
        val historyBefore = repository.state.value.history

        val note = repository.createMuralNote(PartnerId.VINICIUS, "Estou por aqui")

        assertEquals(listOf(note), repository.state.value.muralNotes)
        assertEquals(historyBefore, repository.state.value.history)
    }
}
