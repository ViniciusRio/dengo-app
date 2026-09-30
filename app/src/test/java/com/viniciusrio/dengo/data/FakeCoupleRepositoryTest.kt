package com.viniciusrio.dengo.data

import com.viniciusrio.dengo.model.CareRequestStatus
import com.viniciusrio.dengo.model.CareRequestType
import com.viniciusrio.dengo.model.HistoryEvent
import com.viniciusrio.dengo.model.Mood
import com.viniciusrio.dengo.model.MoodOption
import com.viniciusrio.dengo.model.PartnerId
import com.viniciusrio.dengo.model.PersonalSpace
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
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
    fun creatingRequestPublishesPendingRequestAndMatchingHistoryEventTogether() {
        val repository = FakeCoupleRepository(clock)

        val request = repository.createRequest(CareRequestType.DENGO)
        val state = repository.state.value

        assertEquals(1L, request.id)
        assertEquals(CareRequestType.DENGO, request.type)
        assertEquals(PartnerId.LIDIANNE, request.requesterId)
        assertEquals(PartnerId.VINICIUS, request.recipientId)
        assertEquals(CareRequestStatus.PENDING, request.status)
        assertEquals(now, request.createdAt)
        assertNull(request.message)
        assertEquals(listOf(request), state.requests)
        assertEquals(
            listOf(HistoryEvent.RequestCreated(request.id, PartnerId.LIDIANNE, now)),
            state.history,
        )
    }

    @Test
    fun requestsReceiveSequentialIdsAndOtherTextIsTrimmed() {
        val repository = FakeCoupleRepository(clock)

        repository.createRequest(CareRequestType.HOT_WATER_BAG)
        val other = repository.createRequest(CareRequestType.OTHER, "  Pode trazer água?  ")

        assertEquals(2L, other.id)
        assertEquals("Pode trazer água?", other.message)
        assertEquals(2, repository.state.value.requests.size)
        assertEquals(2, repository.state.value.history.size)
    }

    @Test
    fun newRepositoryStartsWithFirstIdAndEmptyHistory() {
        FakeCoupleRepository(clock).createRequest(CareRequestType.MEDICINE)

        val freshRepository = FakeCoupleRepository(clock)

        assertTrue(freshRepository.state.value.history.isEmpty())
        assertEquals(1L, freshRepository.createRequest(CareRequestType.MEDICINE).id)
    }

    @Test
    fun changingMoodStoresPersonAndDateAndRecordsEachChange() {
        val repository = FakeCoupleRepository(clock)

        repository.setMood(MoodOption.TIRED)
        repository.setMood(MoodOption.NEEDY)
        repository.setMood(MoodOption.NEEDY)

        assertEquals(
            Mood(PartnerId.LIDIANNE, MoodOption.NEEDY, LocalDate.of(2026, 9, 29)),
            repository.state.value.mood,
        )
        assertEquals(
            listOf(
                HistoryEvent.MoodChanged(MoodOption.TIRED, PartnerId.LIDIANNE, now),
                HistoryEvent.MoodChanged(MoodOption.NEEDY, PartnerId.LIDIANNE, now),
            ),
            repository.state.value.history,
        )
    }

    @Test
    fun activatingPersonalSpaceRecordsOneEventAndNoRequest() {
        val repository = FakeCoupleRepository(clock)

        repository.activatePersonalSpace()
        repository.activatePersonalSpace()
        val state = repository.state.value

        assertEquals(PersonalSpace(PersonalSpace.Status.ACTIVE, now), state.personalSpace)
        assertTrue(state.requests.isEmpty())
        assertEquals(
            listOf(HistoryEvent.PersonalSpaceActivated(PartnerId.LIDIANNE, now)),
            state.history,
        )
    }

    @Test
    fun endingPersonalSpaceRecordsOneEventAndClearsStartTime() {
        val repository = FakeCoupleRepository(clock)
        repository.activatePersonalSpace()

        repository.endPersonalSpace()
        repository.endPersonalSpace()
        val state = repository.state.value

        assertEquals(PersonalSpace(), state.personalSpace)
        assertTrue(state.requests.isEmpty())
        assertEquals(
            listOf(
                HistoryEvent.PersonalSpaceActivated(PartnerId.LIDIANNE, now),
                HistoryEvent.PersonalSpaceEnded(PartnerId.LIDIANNE, now),
            ),
            state.history,
        )
    }

    @Test
    fun endingInactivePersonalSpaceDoesNotCreateEvent() {
        val repository = FakeCoupleRepository(clock)

        repository.endPersonalSpace()

        assertEquals(PersonalSpace(), repository.state.value.personalSpace)
        assertTrue(repository.state.value.history.isEmpty())
    }

    @Test
    fun acceptingPendingRequestUpdatesStatusAndHistoryTogether() {
        val repository = FakeCoupleRepository(clock)
        val request = repository.createRequest(CareRequestType.DENGO)

        repository.acceptRequest(request.id)

        assertEquals(CareRequestStatus.ACCEPTED, repository.state.value.requests.single().status)
        assertEquals(
            HistoryEvent.RequestAccepted(request.id, PartnerId.VINICIUS, now),
            repository.state.value.history.last(),
        )
    }

    @Test
    fun decliningPendingRequestUpdatesStatusAndHistoryTogether() {
        val repository = FakeCoupleRepository(clock)
        val request = repository.createRequest(CareRequestType.MEDICINE)

        repository.declineRequest(request.id)

        assertEquals(CareRequestStatus.DECLINED, repository.state.value.requests.single().status)
        assertEquals(
            HistoryEvent.RequestDeclined(request.id, PartnerId.VINICIUS, now),
            repository.state.value.history.last(),
        )
    }

    @Test
    fun invalidTransitionsKeepStateAndHistoryUnchanged() {
        val repository = FakeCoupleRepository(clock)
        val request = repository.createRequest(CareRequestType.DENGO)
        repository.acceptRequest(request.id)
        val before = repository.state.value

        repository.declineRequest(request.id)
        repository.acceptRequest(999L)

        assertEquals(before, repository.state.value)
    }

    @Test
    fun personalSpaceBlocksResponsesWithoutChangingExistingRequests() {
        val repository = FakeCoupleRepository(clock)
        val request = repository.createRequest(CareRequestType.DENGO)
        repository.activatePersonalSpace()
        val before = repository.state.value

        repository.acceptRequest(request.id)
        repository.declineRequest(request.id)
        assertEquals(before, repository.state.value)

        repository.endPersonalSpace()
        repository.acceptRequest(request.id)
        assertEquals(CareRequestStatus.ACCEPTED, repository.state.value.requests.single().status)
    }
}
