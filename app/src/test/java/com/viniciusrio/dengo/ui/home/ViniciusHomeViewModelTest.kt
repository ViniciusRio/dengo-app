package com.viniciusrio.dengo.ui.home

import com.viniciusrio.dengo.data.FakeCoupleRepository
import com.viniciusrio.dengo.model.CareRequestStatus
import com.viniciusrio.dengo.model.CareRequestType
import com.viniciusrio.dengo.model.HistoryEvent
import com.viniciusrio.dengo.model.MoodOption
import com.viniciusrio.dengo.model.PartnerId
import com.viniciusrio.dengo.ui.history.HistoryViewModel
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ViniciusHomeViewModelTest {
    private val now = Instant.parse("2026-09-29T14:32:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)

    @Before fun setUp() = Dispatchers.setMain(StandardTestDispatcher())
    @After fun tearDown() = Dispatchers.resetMain()

    @Test
    fun requestsFollowSharedRepositoryAndSortNewestFirst() = runTest {
        val repository = FakeCoupleRepository(clock)
        val lidianne = HomeViewModel(repository)
        val vinicius = ViniciusHomeViewModel(repository, clock)

        lidianne.createQuickRequest(CareRequestType.DENGO)
        lidianne.createQuickRequest(CareRequestType.MEDICINE)
        advanceUntilIdle()

        assertEquals(listOf(2L, 1L), vinicius.state.value.requests.map { it.id })
        vinicius.acceptRequest(2L)
        advanceUntilIdle()
        assertEquals(CareRequestStatus.ACCEPTED, vinicius.state.value.requests.first().status)
        assertEquals(CareRequestStatus.ACCEPTED, lidianne.state.value.latestRequest?.status)
    }

    @Test
    fun currentMoodIsMarkedAsToday() = runTest {
        val repository = FakeCoupleRepository(clock)
        val viewModel = ViniciusHomeViewModel(repository, clock)
        repository.setMood(MoodOption.HAPPY)
        advanceUntilIdle()

        assertEquals(MoodOption.HAPPY, viewModel.state.value.mood?.option)
        assertTrue(viewModel.state.value.isMoodToday)
    }

    @Test
    fun olderMoodKeepsItsDateAndIsNotMarkedAsToday() = runTest {
        val repository = FakeCoupleRepository(clock)
        val tomorrow = Clock.fixed(now.plusSeconds(86400), ZoneOffset.UTC)
        val viewModel = ViniciusHomeViewModel(repository, tomorrow)
        repository.setMood(MoodOption.TIRED)
        advanceUntilIdle()

        assertEquals("2026-09-29", viewModel.state.value.mood?.date.toString())
        assertFalse(viewModel.state.value.isMoodToday)
    }

    @Test
    fun personalSpaceIsReflectedAndRequestsRemainVisible() = runTest {
        val repository = FakeCoupleRepository(clock)
        val viewModel = ViniciusHomeViewModel(repository, clock)
        val request = repository.createRequest(CareRequestType.DENGO)
        repository.activatePersonalSpace()
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isPersonalSpaceActive)
        assertEquals(listOf(request), viewModel.state.value.requests)
        repository.endPersonalSpace()
        advanceUntilIdle()
        assertFalse(viewModel.state.value.isPersonalSpaceActive)
    }

    @Test
    fun emptyMoodHasNoCurrentDayClaim() = runTest {
        val viewModel = ViniciusHomeViewModel(FakeCoupleRepository(clock), clock)
        advanceUntilIdle()

        assertNull(viewModel.state.value.mood)
        assertFalse(viewModel.state.value.isMoodToday)
    }

    @Test
    fun muralPreviewUsesSameLatestNoteAsLidianneHome() = runTest {
        val repository = FakeCoupleRepository(clock)
        val lidianne = HomeViewModel(repository)
        val vinicius = ViniciusHomeViewModel(repository, clock)
        repository.createMuralNote(PartnerId.LIDIANNE, "Um recado")
        val latest = repository.createMuralNote(PartnerId.VINICIUS, "Outro recado")
        advanceUntilIdle()

        assertEquals(latest, lidianne.state.value.latestMuralNote)
        assertEquals(latest, vinicius.state.value.latestMuralNote)
    }

    @Test
    fun mainListOnlyContainsPendingAndAcceptedAndLatestAcknowledgementGivesFeedback() = runTest {
        val repository = FakeCoupleRepository(clock)
        val viewModel = ViniciusHomeViewModel(repository, clock)
        val pending = repository.createRequest(CareRequestType.DENGO)
        val accepted = repository.createRequest(CareRequestType.MEDICINE)
        val declined = repository.createRequest(CareRequestType.HOT_WATER_BAG)
        repository.acceptRequest(accepted.id)
        repository.declineRequest(declined.id)
        advanceUntilIdle()
        assertEquals(listOf(accepted.id, pending.id), viewModel.state.value.requests.map { it.id })
        assertNull(viewModel.state.value.acknowledgedRequest)

        repository.acknowledgeRequest(accepted.id, PartnerId.LIDIANNE)
        advanceUntilIdle()
        assertEquals(listOf(pending.id), viewModel.state.value.requests.map { it.id })
        assertEquals(accepted.id, viewModel.state.value.acknowledgedRequest?.id)
        repository.createRequest(CareRequestType.SPEND_TIME_TOGETHER)
        advanceUntilIdle()
        assertEquals(accepted.id, viewModel.state.value.acknowledgedRequest?.id)
    }

    @Test
    fun latestAcknowledgementReplacesEarlierFeedbackWhileHistoryKeepsBoth() = runTest {
        val repository = FakeCoupleRepository(clock)
        val lidianne = HomeViewModel(repository)
        val vinicius = ViniciusHomeViewModel(repository, clock)
        val history = HistoryViewModel(repository, clock)
        val dengo = repository.createRequest(CareRequestType.DENGO)
        val bag = repository.createRequest(CareRequestType.HOT_WATER_BAG)
        vinicius.acceptRequest(dengo.id)
        vinicius.acceptRequest(bag.id)

        lidianne.acknowledgeRequest(dengo.id)
        advanceUntilIdle()
        assertEquals(dengo.id, vinicius.state.value.acknowledgedRequest?.id)

        lidianne.acknowledgeRequest(bag.id)
        advanceUntilIdle()
        assertEquals(bag.id, vinicius.state.value.acknowledgedRequest?.id)
        assertEquals(
            listOf(bag.id, dengo.id),
            history.state.value.days.single().items.mapNotNull { (it.event as? HistoryEvent.RequestAcknowledged)?.requestId },
        )

        lidianne.setMood(MoodOption.HAPPY)
        advanceUntilIdle()
        assertEquals(bag.id, vinicius.state.value.acknowledgedRequest?.id)
        assertEquals(2, repository.state.value.history.count { it is HistoryEvent.RequestAcknowledged })
    }
}
