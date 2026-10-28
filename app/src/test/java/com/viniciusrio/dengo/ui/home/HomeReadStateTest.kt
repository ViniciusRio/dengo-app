package com.viniciusrio.dengo.ui.home

import com.viniciusrio.dengo.data.CoupleRepository
import com.viniciusrio.dengo.data.FakeCoupleRepository
import com.viniciusrio.dengo.data.RepositoryReadState
import com.viniciusrio.dengo.ui.ScreenReadState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeReadStateTest {
    @Before fun setUp() = Dispatchers.setMain(StandardTestDispatcher())
    @After fun tearDown() = Dispatchers.resetMain()

    @Test fun loadingAndFailureDoNotBecomeAnEmptyHome() = runTest {
        val fake = FakeCoupleRepository()
        val reads = MutableStateFlow<RepositoryReadState>(RepositoryReadState.Loading)
        val repository = object : CoupleRepository by fake { override val state = reads }
        val home = HomeViewModel(repository)
        advanceUntilIdle()
        assertEquals(ScreenReadState.Loading, home.state.value)

        val failure = IllegalStateException("read failed")
        reads.value = RepositoryReadState.ReadError(failure)
        advanceUntilIdle()
        assertTrue(home.state.value is ScreenReadState.ReadError)

        reads.value = fake.state.value
        advanceUntilIdle()
        assertTrue(home.state.value is ScreenReadState.Ready)
    }
}
