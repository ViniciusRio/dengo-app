package com.viniciusrio.dengo.ui.home

import com.viniciusrio.dengo.data.FakeCoupleRepository
import com.viniciusrio.dengo.data.confirmed
import com.viniciusrio.dengo.data.snapshot
import com.viniciusrio.dengo.data.ready
import com.viniciusrio.dengo.model.CareRequestStatus
import com.viniciusrio.dengo.model.CareRequestType
import com.viniciusrio.dengo.model.HistoryEvent
import com.viniciusrio.dengo.model.MoodOption
import com.viniciusrio.dengo.model.PartnerId
import com.viniciusrio.dengo.ui.history.HistoryViewModel
import com.viniciusrio.dengo.ui.history.RequestStory
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

        assertEquals(listOf(2L, 1L), vinicius.state.value.ready().requests.map { it.id })
        vinicius.acceptRequest(2L)
        advanceUntilIdle()
        assertEquals(CareRequestStatus.ACCEPTED, vinicius.state.value.ready().requests.first().status)
        assertEquals(CareRequestStatus.ACCEPTED, lidianne.state.value.ready().latestRequest?.status)
    }

    @Test
    fun currentMoodIsMarkedAsToday() = runTest {
        val repository = FakeCoupleRepository(clock)
        val viewModel = ViniciusHomeViewModel(repository, clock)
        repository.setMood(MoodOption.HAPPY)
        advanceUntilIdle()

        assertEquals(MoodOption.HAPPY, viewModel.state.value.ready().mood?.option)
        assertTrue(viewModel.state.value.ready().isMoodToday)
    }

    @Test
    fun olderMoodKeepsItsDateAndIsNotMarkedAsToday() = runTest {
        val repository = FakeCoupleRepository(clock)
        val tomorrow = Clock.fixed(now.plusSeconds(86400), ZoneOffset.UTC)
        val viewModel = ViniciusHomeViewModel(repository, tomorrow)
        repository.setMood(MoodOption.TIRED)
        advanceUntilIdle()

        assertEquals("2026-09-29", viewModel.state.value.ready().mood?.date.toString())
        assertFalse(viewModel.state.value.ready().isMoodToday)
    }

    @Test
    fun personalSpaceIsReflectedAndRequestsRemainVisible() = runTest {
        val repository = FakeCoupleRepository(clock)
        val viewModel = ViniciusHomeViewModel(repository, clock)
        val request = repository.createRequest(CareRequestType.DENGO).confirmed()
        repository.activatePersonalSpace()
        advanceUntilIdle()

        assertTrue(viewModel.state.value.ready().isPersonalSpaceActive)
        assertEquals(listOf(request), viewModel.state.value.ready().requests)
        repository.endPersonalSpace()
        advanceUntilIdle()
        assertFalse(viewModel.state.value.ready().isPersonalSpaceActive)
    }

    @Test
    fun emptyMoodHasNoCurrentDayClaim() = runTest {
        val viewModel = ViniciusHomeViewModel(FakeCoupleRepository(clock), clock)
        advanceUntilIdle()

        assertNull(viewModel.state.value.ready().mood)
        assertFalse(viewModel.state.value.ready().isMoodToday)
    }

    @Test
    fun muralPreviewUsesSameLatestNoteAsLidianneHome() = runTest {
        val repository = FakeCoupleRepository(clock)
        val lidianne = HomeViewModel(repository)
        val vinicius = ViniciusHomeViewModel(repository, clock)
        repository.actingAs(PartnerId.LIDIANNE).createMuralNote("Um recado").confirmed()
        val latest = repository.actingAs(PartnerId.VINICIUS).createMuralNote("Outro recado").confirmed()
        advanceUntilIdle()

        assertEquals(latest, lidianne.state.value.ready().latestMuralNote)
        assertEquals(latest, vinicius.state.value.ready().latestMuralNote)
    }

    @Test
    fun drawingBecomesLatestPreviewInBothHomes() = runTest {
        val repository = FakeCoupleRepository(clock)
        val lidianne = HomeViewModel(repository)
        val vinicius = ViniciusHomeViewModel(repository, clock)
        repository.actingAs(PartnerId.LIDIANNE).createMuralNote("Texto").confirmed()
        val latest = repository.actingAs(PartnerId.VINICIUS).createMuralDrawing(1.5f,
            listOf(com.viniciusrio.dengo.model.DrawingStroke(
                0xFF315F91.toInt(),
                listOf(com.viniciusrio.dengo.model.DrawingPoint(0.4f, 0.5f)),
            )),
        ).confirmed()
        advanceUntilIdle()

        assertEquals(latest, lidianne.state.value.ready().latestMuralNote)
        assertEquals(latest, vinicius.state.value.ready().latestMuralNote)
    }

    @Test
    fun mainListOnlyContainsPendingAndAcceptedAndLatestAcknowledgementGivesFeedback() = runTest {
        val repository = FakeCoupleRepository(clock)
        val viewModel = ViniciusHomeViewModel(repository, clock)
        val pending = repository.createRequest(CareRequestType.DENGO).confirmed()
        val accepted = repository.createRequest(CareRequestType.MEDICINE).confirmed()
        val declined = repository.createRequest(CareRequestType.HOT_WATER_BAG).confirmed()
        repository.acceptRequest(accepted.id)
        repository.declineRequest(declined.id)
        advanceUntilIdle()
        assertEquals(listOf(accepted.id, pending.id), viewModel.state.value.ready().requests.map { it.id })
        assertNull(viewModel.state.value.ready().acknowledgedRequest)

        repository.actingAs(PartnerId.LIDIANNE).acknowledgeRequest(accepted.id)
        advanceUntilIdle()
        assertEquals(listOf(pending.id), viewModel.state.value.ready().requests.map { it.id })
        assertEquals(accepted.id, viewModel.state.value.ready().acknowledgedRequest?.id)
        repository.createRequest(CareRequestType.SPEND_TIME_TOGETHER).confirmed()
        advanceUntilIdle()
        assertEquals(accepted.id, viewModel.state.value.ready().acknowledgedRequest?.id)
    }

    @Test
    fun latestAcknowledgementReplacesEarlierFeedbackWhileHistoryKeepsBoth() = runTest {
        val repository = FakeCoupleRepository(clock)
        val lidianne = HomeViewModel(repository)
        val vinicius = ViniciusHomeViewModel(repository, clock)
        val history = HistoryViewModel(repository, clock)
        val dengo = repository.createRequest(CareRequestType.DENGO).confirmed()
        val bag = repository.createRequest(CareRequestType.HOT_WATER_BAG).confirmed()
        vinicius.acceptRequest(dengo.id)
        vinicius.acceptRequest(bag.id)

        lidianne.acknowledgeRequest(dengo.id)
        advanceUntilIdle()
        assertEquals(dengo.id, vinicius.state.value.ready().acknowledgedRequest?.id)

        lidianne.acknowledgeRequest(bag.id)
        advanceUntilIdle()
        assertEquals(bag.id, vinicius.state.value.ready().acknowledgedRequest?.id)
        assertEquals(
            listOf(bag.id, dengo.id),
            history.state.value.ready().days.single().entries.filterIsInstance<RequestStory>()
                .filter { story -> story.steps.any { it.event is HistoryEvent.RequestAcknowledged } }
                .map { it.requestId },
        )

        lidianne.setMood(MoodOption.HAPPY)
        advanceUntilIdle()
        assertEquals(bag.id, vinicius.state.value.ready().acknowledgedRequest?.id)
        assertEquals(2, repository.snapshot().history.count { it is HistoryEvent.RequestAcknowledged })
    }
}
