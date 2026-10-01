package com.viniciusrio.dengo.ui.home

import com.viniciusrio.dengo.data.FakeCoupleRepository
import com.viniciusrio.dengo.model.CareRequestStatus
import com.viniciusrio.dengo.model.CareRequestType
import com.viniciusrio.dengo.model.MoodOption
import com.viniciusrio.dengo.model.PartnerId
import com.viniciusrio.dengo.model.PersonalSpace
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
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val clock = Clock.fixed(Instant.parse("2026-09-29T14:32:00Z"), ZoneOffset.UTC)

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialStateContainsOnlyHomeData() = runTest {
        val viewModel = HomeViewModel(FakeCoupleRepository(clock))
        advanceUntilIdle()

        assertNull(viewModel.state.value.latestRequest)
        assertEquals(0, viewModel.state.value.otherRespondedRequestCount)
        assertNull(viewModel.state.value.mood)
        assertEquals(PersonalSpace(), viewModel.state.value.personalSpace)
    }

    @Test
    fun quickRequestActionUpdatesRepositoryAndHomeState() = runTest {
        val repository = FakeCoupleRepository(clock)
        val viewModel = HomeViewModel(repository)

        viewModel.createQuickRequest(CareRequestType.SPEND_TIME_TOGETHER)
        advanceUntilIdle()

        assertEquals(CareRequestType.SPEND_TIME_TOGETHER, viewModel.state.value.latestRequest?.type)
        assertEquals(repository.state.value.requests.last(), viewModel.state.value.latestRequest)
    }

    @Test
    fun otherRequestActionPassesTextToRepository() = runTest {
        val viewModel = HomeViewModel(FakeCoupleRepository(clock))

        viewModel.createOtherRequest("  Quero conversar  ")
        advanceUntilIdle()

        assertEquals(CareRequestType.OTHER, viewModel.state.value.latestRequest?.type)
        assertEquals("Quero conversar", viewModel.state.value.latestRequest?.message)
    }

    @Test
    fun moodActionUpdatesHomeState() = runTest {
        val viewModel = HomeViewModel(FakeCoupleRepository(clock))

        viewModel.setMood(MoodOption.HAPPY)
        advanceUntilIdle()

        assertEquals(MoodOption.HAPPY, viewModel.state.value.mood?.option)
    }

    @Test
    fun personalSpaceActionsUpdateHomeState() = runTest {
        val viewModel = HomeViewModel(FakeCoupleRepository(clock))

        viewModel.activatePersonalSpace()
        advanceUntilIdle()
        assertEquals(PersonalSpace.Status.ACTIVE, viewModel.state.value.personalSpace.status)

        viewModel.endPersonalSpace()
        advanceUntilIdle()
        assertEquals(PersonalSpace(), viewModel.state.value.personalSpace)
    }

    @Test
    fun stateFollowsRepositoryChangesMadeOutsideViewModel() = runTest {
        val repository = FakeCoupleRepository(clock)
        val viewModel = HomeViewModel(repository)

        repository.createRequest(CareRequestType.HOT_WATER_BAG)
        repository.setMood(MoodOption.OKAY)
        repository.activatePersonalSpace()
        advanceUntilIdle()

        assertEquals(repository.state.value.requests.last(), viewModel.state.value.latestRequest)
        assertEquals(repository.state.value.mood, viewModel.state.value.mood)
        assertEquals(repository.state.value.personalSpace, viewModel.state.value.personalSpace)
    }

    @Test
    fun multipleResponsesKeepNewestRequestAsFeedbackAndCountOnlyOtherRespondedRequests() = runTest {
        val repository = FakeCoupleRepository(clock)
        val viewModel = HomeViewModel(repository)
        val first = repository.createRequest(CareRequestType.DENGO)
        val second = repository.createRequest(CareRequestType.MEDICINE)
        repository.acceptRequest(first.id)
        repository.declineRequest(second.id)
        advanceUntilIdle()

        assertEquals(second.id, viewModel.state.value.latestRequest?.id)
        assertEquals(CareRequestStatus.DECLINED, viewModel.state.value.latestRequest?.status)
        assertEquals(1, viewModel.state.value.otherRespondedRequestCount)

        val third = repository.createRequest(CareRequestType.HOT_WATER_BAG)
        advanceUntilIdle()

        assertEquals(third.id, viewModel.state.value.latestRequest?.id)
        assertEquals(CareRequestStatus.PENDING, viewModel.state.value.latestRequest?.status)
        assertEquals(2, viewModel.state.value.otherRespondedRequestCount)

        repository.acceptRequest(third.id)
        advanceUntilIdle()
        assertEquals(third.id, viewModel.state.value.latestRequest?.id)
        assertEquals(CareRequestStatus.ACCEPTED, viewModel.state.value.latestRequest?.status)
        assertEquals(2, viewModel.state.value.otherRespondedRequestCount)

        val fourth = repository.createRequest(CareRequestType.SPEND_TIME_TOGETHER)
        advanceUntilIdle()
        assertEquals(fourth.id, viewModel.state.value.latestRequest?.id)
        assertEquals(3, viewModel.state.value.otherRespondedRequestCount)
    }

    @Test
    fun otherPendingRequestsDoNotInflateResponseCount() = runTest {
        val repository = FakeCoupleRepository(clock)
        val viewModel = HomeViewModel(repository)
        repository.createRequest(CareRequestType.DENGO)
        val latest = repository.createRequest(CareRequestType.MEDICINE)
        advanceUntilIdle()

        assertEquals(latest.id, viewModel.state.value.latestRequest?.id)
        assertEquals(0, viewModel.state.value.otherRespondedRequestCount)
    }

    @Test
    fun muralPreviewFollowsLatestNoteWithoutChangingRequestFeedback() = runTest {
        val repository = FakeCoupleRepository(clock)
        val viewModel = HomeViewModel(repository)
        val request = repository.createRequest(CareRequestType.DENGO)
        repository.createMuralNote(PartnerId.VINICIUS, "Primeiro")
        val latest = repository.createMuralNote(PartnerId.LIDIANNE, "Segundo")
        advanceUntilIdle()

        assertEquals(latest, viewModel.state.value.latestMuralNote)
        assertEquals(request, viewModel.state.value.latestRequest)
    }

    @Test
    fun pendingAndMultipleAcceptedRequestsRemainIndividuallyAvailableForAcknowledgement() = runTest {
        val repository = FakeCoupleRepository(clock)
        val viewModel = HomeViewModel(repository)
        val dengo = repository.createRequest(CareRequestType.DENGO)
        val medicine = repository.createRequest(CareRequestType.MEDICINE)
        val bag = repository.createRequest(CareRequestType.HOT_WATER_BAG)
        repository.acceptRequest(medicine.id)
        repository.acceptRequest(dengo.id)
        advanceUntilIdle()

        assertEquals(listOf(bag.id, medicine.id, dengo.id), viewModel.state.value.activeRequests.map { it.id })
        viewModel.acknowledgeRequest(dengo.id)
        advanceUntilIdle()
        assertEquals(listOf(bag.id, medicine.id), viewModel.state.value.activeRequests.map { it.id })
        viewModel.acknowledgeRequest(medicine.id)
        repository.declineRequest(bag.id)
        advanceUntilIdle()
        assertEquals(emptyList<Long>(), viewModel.state.value.activeRequests.map { it.id })
    }
}
