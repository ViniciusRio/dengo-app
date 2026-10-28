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
import kotlinx.coroutines.test.runTest

class FakeCoupleRepositoryTest {
    private val now = Instant.parse("2026-09-29T14:32:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)

    @Test
    fun initialStateHasPartnersAndNoInteractions() = runTest {
        val state = FakeCoupleRepository(clock).snapshot()

        assertEquals(listOf(PartnerId.LIDIANNE, PartnerId.VINICIUS), state.partners.map { it.id })
        assertEquals(listOf("Lidianne", "Vinícius"), state.partners.map { it.name })
        assertTrue(state.requests.isEmpty())
        assertNull(state.mood)
        assertEquals(PersonalSpace(), state.personalSpace)
        assertTrue(state.history.isEmpty())
    }

    @Test
    fun creatingRequestPublishesPendingRequestAndMatchingHistoryEventTogether() = runTest {
        val repository = FakeCoupleRepository(clock)

        val request = repository.createRequest(CareRequestType.DENGO).confirmed()
        val state = repository.snapshot()

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
    fun requestsReceiveSequentialIdsAndOtherTextIsTrimmed() = runTest {
        val repository = FakeCoupleRepository(clock)

        repository.createRequest(CareRequestType.HOT_WATER_BAG).confirmed()
        val other = repository.createRequest(CareRequestType.OTHER, "  Pode trazer água?  ").confirmed()

        assertEquals(2L, other.id)
        assertEquals("Pode trazer água?", other.message)
        assertEquals(2, repository.snapshot().requests.size)
        assertEquals(2, repository.snapshot().history.size)
    }

    @Test
    fun newRepositoryStartsWithFirstIdAndEmptyHistory() = runTest {
        FakeCoupleRepository(clock).createRequest(CareRequestType.MEDICINE).confirmed()

        val freshRepository = FakeCoupleRepository(clock)

        assertTrue(freshRepository.snapshot().history.isEmpty())
        assertEquals(1L, freshRepository.createRequest(CareRequestType.MEDICINE).confirmed().id)
    }

    @Test
    fun changingMoodStoresPersonAndDateAndRecordsEachChange() = runTest {
        val repository = FakeCoupleRepository(clock)

        repository.setMood(MoodOption.TIRED)
        repository.setMood(MoodOption.NEEDY)
        repository.setMood(MoodOption.NEEDY)

        assertEquals(
            Mood(PartnerId.LIDIANNE, MoodOption.NEEDY, LocalDate.of(2026, 9, 29)),
            repository.snapshot().mood,
        )
        assertEquals(
            listOf(
                HistoryEvent.MoodChanged(MoodOption.TIRED, PartnerId.LIDIANNE, now),
                HistoryEvent.MoodChanged(MoodOption.NEEDY, PartnerId.LIDIANNE, now),
            ),
            repository.snapshot().history,
        )
    }

    @Test
    fun activatingPersonalSpaceRecordsOneEventAndNoRequest() = runTest {
        val repository = FakeCoupleRepository(clock)

        repository.activatePersonalSpace()
        repository.activatePersonalSpace()
        val state = repository.snapshot()

        assertEquals(PersonalSpace(PersonalSpace.Status.ACTIVE, now), state.personalSpace)
        assertTrue(state.requests.isEmpty())
        assertEquals(
            listOf(HistoryEvent.PersonalSpaceActivated(PartnerId.LIDIANNE, now)),
            state.history,
        )
    }

    @Test
    fun endingPersonalSpaceRecordsOneEventAndClearsStartTime() = runTest {
        val repository = FakeCoupleRepository(clock)
        repository.activatePersonalSpace()

        repository.endPersonalSpace()
        repository.endPersonalSpace()
        val state = repository.snapshot()

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
    fun endingInactivePersonalSpaceDoesNotCreateEvent() = runTest {
        val repository = FakeCoupleRepository(clock)

        repository.endPersonalSpace()

        assertEquals(PersonalSpace(), repository.snapshot().personalSpace)
        assertTrue(repository.snapshot().history.isEmpty())
    }

    @Test
    fun acceptingPendingRequestUpdatesStatusAndHistoryTogether() = runTest {
        val repository = FakeCoupleRepository(clock)
        val request = repository.createRequest(CareRequestType.DENGO).confirmed()

        repository.acceptRequest(request.id)

        assertEquals(CareRequestStatus.ACCEPTED, repository.snapshot().requests.single().status)
        assertEquals(
            HistoryEvent.RequestAccepted(request.id, PartnerId.VINICIUS, now),
            repository.snapshot().history.last(),
        )
    }

    @Test
    fun decliningPendingRequestUpdatesStatusAndHistoryTogether() = runTest {
        val repository = FakeCoupleRepository(clock)
        val request = repository.createRequest(CareRequestType.MEDICINE).confirmed()

        repository.declineRequest(request.id)

        assertEquals(CareRequestStatus.DECLINED, repository.snapshot().requests.single().status)
        assertEquals(
            HistoryEvent.RequestDeclined(request.id, PartnerId.VINICIUS, now),
            repository.snapshot().history.last(),
        )
    }

    @Test
    fun responsesToDifferentRequestsPreserveEachIdStatusAndHistory() = runTest {
        val repository = FakeCoupleRepository(clock)
        val first = repository.createRequest(CareRequestType.DENGO).confirmed()
        val second = repository.createRequest(CareRequestType.MEDICINE).confirmed()

        repository.acceptRequest(first.id)
        val firstAfterAcceptance = repository.snapshot().requests.single { it.id == first.id }
        repository.declineRequest(second.id)
        val state = repository.snapshot()

        assertEquals(listOf(first.id, second.id), state.requests.map { it.id })
        assertEquals(firstAfterAcceptance, state.requests.single { it.id == first.id })
        assertEquals(CareRequestStatus.ACCEPTED, state.requests.single { it.id == first.id }.status)
        assertEquals(CareRequestStatus.DECLINED, state.requests.single { it.id == second.id }.status)
        assertEquals(
            listOf(
                HistoryEvent.RequestCreated(first.id, PartnerId.LIDIANNE, now),
                HistoryEvent.RequestCreated(second.id, PartnerId.LIDIANNE, now),
                HistoryEvent.RequestAccepted(first.id, PartnerId.VINICIUS, now),
                HistoryEvent.RequestDeclined(second.id, PartnerId.VINICIUS, now),
            ),
            state.history,
        )

        repository.declineRequest(first.id)
        repository.acceptRequest(second.id)
        assertEquals(state, repository.snapshot())
    }

    @Test
    fun invalidTransitionsKeepStateAndHistoryUnchanged() = runTest {
        val repository = FakeCoupleRepository(clock)
        val request = repository.createRequest(CareRequestType.DENGO).confirmed()
        repository.acceptRequest(request.id)
        val before = repository.snapshot()

        repository.declineRequest(request.id)
        repository.acceptRequest(999L)

        assertEquals(before, repository.snapshot())
    }

    @Test
    fun personalSpaceBlocksResponsesWithoutChangingExistingRequests() = runTest {
        val repository = FakeCoupleRepository(clock)
        val request = repository.createRequest(CareRequestType.DENGO).confirmed()
        repository.activatePersonalSpace()
        val before = repository.snapshot()

        repository.acceptRequest(request.id)
        repository.declineRequest(request.id)
        assertEquals(before, repository.snapshot())

        repository.endPersonalSpace()
        repository.acceptRequest(request.id)
        assertEquals(CareRequestStatus.ACCEPTED, repository.snapshot().requests.single().status)
    }

    @Test
    fun predefinedRequestCannotBeRepeatedWhilePendingOrAcceptedAndDoesNotConsumeIdOrHistory() = runTest {
        val repository = FakeCoupleRepository(clock)
        val first = repository.createRequest(CareRequestType.DENGO).confirmed()
        val pending = repository.snapshot()
        repository.createRequest(CareRequestType.DENGO).confirmed()
        assertEquals(pending, repository.snapshot())
        repository.acceptRequest(first.id)
        val accepted = repository.snapshot()
        repository.createRequest(CareRequestType.DENGO).confirmed()
        assertEquals(accepted, repository.snapshot())
        assertEquals(2L, repository.createRequest(CareRequestType.MEDICINE).confirmed().id)
    }

    @Test
    fun decliningOrAcknowledgingFreesPredefinedTypeImmediately() = runTest {
        val repository = FakeCoupleRepository(clock)
        val declined = repository.createRequest(CareRequestType.DENGO).confirmed()
        repository.declineRequest(declined.id)
        val replacement = repository.createRequest(CareRequestType.DENGO).confirmed()
        assertEquals(2L, replacement.id)
        repository.acceptRequest(replacement.id)
        repository.acknowledgeRequest(replacement.id, PartnerId.LIDIANNE)
        assertEquals(CareRequestStatus.ACKNOWLEDGED, repository.snapshot().requests.last().status)
        assertEquals(3L, repository.createRequest(CareRequestType.DENGO).confirmed().id)
    }

    @Test
    fun otherRequestsWithIdenticalTextStayIndependent() = runTest {
        val repository = FakeCoupleRepository(clock)
        val first = repository.createRequest(CareRequestType.OTHER, " Mesmo texto ").confirmed()
        val second = repository.createRequest(CareRequestType.OTHER, "Mesmo texto").confirmed()
        assertEquals(listOf(1L, 2L), repository.snapshot().requests.map { it.id })
        repository.acceptRequest(second.id)
        repository.acknowledgeRequest(first.id, PartnerId.LIDIANNE)
        assertEquals(CareRequestStatus.PENDING, repository.snapshot().requests.first().status)
        repository.acknowledgeRequest(second.id, PartnerId.LIDIANNE)
        assertEquals(CareRequestStatus.ACKNOWLEDGED, repository.snapshot().requests.last().status)
    }

    @Test
    fun acknowledgementIsAtomicAndOnlyLidianneCanAcknowledgeAcceptedRequest() = runTest {
        val repository = FakeCoupleRepository(clock)
        val request = repository.createRequest(CareRequestType.HOT_WATER_BAG).confirmed()
        val pending = repository.snapshot()
        repository.acknowledgeRequest(request.id, PartnerId.LIDIANNE)
        repository.acknowledgeRequest(999L, PartnerId.LIDIANNE)
        assertEquals(pending, repository.snapshot())
        repository.acceptRequest(request.id)
        val accepted = repository.snapshot()
        repository.acknowledgeRequest(request.id, PartnerId.VINICIUS)
        assertEquals(accepted, repository.snapshot())
        repository.acknowledgeRequest(request.id, PartnerId.LIDIANNE)
        val acknowledged = repository.snapshot()
        assertEquals(CareRequestStatus.ACKNOWLEDGED, acknowledged.requests.single().status)
        assertEquals(accepted.history + HistoryEvent.RequestAcknowledged(request.id, PartnerId.LIDIANNE, now), acknowledged.history)
        repository.acknowledgeRequest(request.id, PartnerId.LIDIANNE)
        repository.declineRequest(request.id)
        assertEquals(acknowledged, repository.snapshot())
    }

    @Test
    fun onlyApprovedStatusesRemainAndBlankOtherIsRejectedAtRepositoryBoundary() = runTest {
        assertEquals(
            setOf(CareRequestStatus.PENDING, CareRequestStatus.ACCEPTED, CareRequestStatus.DECLINED, CareRequestStatus.ACKNOWLEDGED),
            CareRequestStatus.entries.toSet(),
        )
        val repository = FakeCoupleRepository(clock)
        assertTrue(repository.createRequest(CareRequestType.OTHER, "   ") is WriteResult.Reverted)
        assertTrue(repository.snapshot().requests.isEmpty())
        assertTrue(repository.snapshot().history.isEmpty())
        assertEquals(1L, repository.createRequest(CareRequestType.OTHER, "Um abraço").confirmed().id)
    }

    @Test
    fun acknowledgingDoesNotChangePersonalSpaceAndDoesNotUnblockResponsesDuringIt() = runTest {
        val repository = FakeCoupleRepository(clock)
        val accepted = repository.createRequest(CareRequestType.DENGO).confirmed()
        val pending = repository.createRequest(CareRequestType.MEDICINE).confirmed()
        repository.acceptRequest(accepted.id)
        repository.activatePersonalSpace()
        repository.acknowledgeRequest(accepted.id, PartnerId.LIDIANNE)
        val afterAcknowledgement = repository.snapshot()
        assertEquals(PersonalSpace.Status.ACTIVE, afterAcknowledgement.personalSpace.status)
        assertEquals(CareRequestStatus.PENDING, afterAcknowledgement.requests.last().status)
        repository.acceptRequest(pending.id)
        repository.declineRequest(pending.id)
        assertEquals(afterAcknowledgement, repository.snapshot())
    }
}
