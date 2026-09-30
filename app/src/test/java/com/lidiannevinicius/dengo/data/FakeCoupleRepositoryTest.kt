package com.lidiannevinicius.dengo.data

import com.lidiannevinicius.dengo.model.CareRequestStatus
import com.lidiannevinicius.dengo.model.CareRequestType
import com.lidiannevinicius.dengo.model.HistoryEvent
import com.lidiannevinicius.dengo.model.Mood
import com.lidiannevinicius.dengo.model.MoodOption
import com.lidiannevinicius.dengo.model.PartnerId
import com.lidiannevinicius.dengo.model.PersonalSpace
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeCoupleRepositoryTest {
    private val now = Instant.parse("2026-09-29T14:32:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)

    @Test
    fun initialStateHasPartnersAndNoInteractions() {
        val state = FakeCoupleRepository(clock).state.value

        assertEquals(listOf(PartnerId.LIDIANNE, PartnerId.VINICIUS), state.partners.map { it.id })
        assertEquals(listOf("Lidianne", "Vinícius"), state.partners.map { it.name })
        assertTrue(state.requests.isEmpty())
        assertNull(state.mood)
        assertEquals(PersonalSpace(), state.personalSpace)
        assertTrue(state.history.isEmpty())
    }

    @Test
    fun creatingRequestPublishesRequestedState() {
        val repository = FakeCoupleRepository(clock)

        val request = repository.createRequest(CareRequestType.AFFECTION)

        assertEquals(1L, request.id)
        assertEquals(CareRequestType.AFFECTION, request.type)
        assertEquals(PartnerId.LIDIANNE, request.requesterId)
        assertEquals(PartnerId.VINICIUS, request.recipientId)
        assertEquals(CareRequestStatus.REQUESTED, request.status)
        assertEquals(now, request.createdAt)
        assertNull(request.message)
        assertEquals(listOf(request), repository.state.value.requests)
    }

    @Test
    fun requestsReceiveSequentialIdsAndTrimOptionalMessage() {
        val repository = FakeCoupleRepository(clock)

        repository.createRequest(CareRequestType.HOT_WATER_BAG)
        val other = repository.createRequest(CareRequestType.OTHER, "  Pode trazer água?  ")

        assertEquals(2L, other.id)
        assertEquals("Pode trazer água?", other.message)
        assertEquals(2, repository.state.value.requests.size)
    }

    @Test
    fun changingMoodReplacesCurrentDayWithoutHistoryEvent() {
        val repository = FakeCoupleRepository(clock)

        repository.setMood(MoodOption.TIRED)
        repository.setMood(MoodOption.NEEDY)

        assertEquals(Mood(MoodOption.NEEDY, LocalDate.of(2026, 9, 29)), repository.state.value.mood)
        assertTrue(repository.state.value.history.isEmpty())
    }

    @Test
    fun activatingPersonalSpaceKeepsItSeparateFromRequests() {
        val repository = FakeCoupleRepository(clock)

        repository.activatePersonalSpace()
        val activeState = repository.state.value
        repository.activatePersonalSpace()

        assertEquals(PersonalSpace(isActive = true, activatedAt = now), activeState.personalSpace)
        assertEquals(activeState, repository.state.value)
        assertTrue(activeState.requests.isEmpty())
        assertTrue(activeState.history.isEmpty())
    }

    @Test
    fun endingPersonalSpaceClearsItWithoutCreatingRequestOrHistory() {
        val repository = FakeCoupleRepository(clock)
        repository.activatePersonalSpace()

        repository.endPersonalSpace()

        assertFalse(repository.state.value.personalSpace.isActive)
        assertNull(repository.state.value.personalSpace.activatedAt)
        assertTrue(repository.state.value.requests.isEmpty())
        assertTrue(repository.state.value.history.isEmpty())
    }

    @Test
    fun creatingRequestRecordsCorrespondingHistoryEvent() {
        val repository = FakeCoupleRepository(clock)

        val request = repository.createRequest(CareRequestType.MEDICINE)

        assertEquals(
            listOf(HistoryEvent.RequestCreated(request.id, PartnerId.LIDIANNE, now)),
            repository.state.value.history,
        )
    }
}
