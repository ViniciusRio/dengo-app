package com.viniciusrio.dengo.ui.home

import com.viniciusrio.dengo.data.FakeCoupleRepository
import com.viniciusrio.dengo.model.CareRequestStatus
import com.viniciusrio.dengo.model.CareRequestType
import com.viniciusrio.dengo.model.MoodOption
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
}
