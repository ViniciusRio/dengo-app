package com.lidiannevinicius.dengo.ui.home

import com.lidiannevinicius.dengo.data.FakeCoupleRepository
import com.lidiannevinicius.dengo.model.CareRequestType
import com.lidiannevinicius.dengo.model.MoodOption
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
        assertNull(viewModel.state.value.mood)
        assertFalse(viewModel.state.value.personalSpace.isActive)
    }

    @Test
    fun actionsDelegateToRepositoryAndUpdateHomeState() = runTest {
        val repository = FakeCoupleRepository(clock)
        val viewModel = HomeViewModel(repository)

        viewModel.createRequest(CareRequestType.TIME_TOGETHER)
        viewModel.setMood(MoodOption.LOVING)
        viewModel.activatePersonalSpace()
        advanceUntilIdle()

        assertEquals(CareRequestType.TIME_TOGETHER, viewModel.state.value.latestRequest?.type)
        assertEquals(MoodOption.LOVING, viewModel.state.value.mood?.option)
        assertTrue(viewModel.state.value.personalSpace.isActive)
        assertEquals(1, repository.state.value.history.size)

        viewModel.endPersonalSpace()
        advanceUntilIdle()

        assertFalse(viewModel.state.value.personalSpace.isActive)
    }
}
