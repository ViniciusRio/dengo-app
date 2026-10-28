package com.viniciusrio.dengo.ui.home

import com.viniciusrio.dengo.data.CoupleRepository
import com.viniciusrio.dengo.data.FakeCoupleRepository
import com.viniciusrio.dengo.data.WriteResult
import com.viniciusrio.dengo.model.CareRequest
import com.viniciusrio.dengo.model.CareRequestType
import com.viniciusrio.dengo.model.MoodOption
import com.viniciusrio.dengo.model.PartnerId
import com.viniciusrio.dengo.ui.SubmissionState
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class QuickWriteFeedbackTest {
    @Before fun setUp() = Dispatchers.setMain(StandardTestDispatcher())
    @After fun tearDown() = Dispatchers.resetMain()

    private class Repository(private val fake: FakeCoupleRepository = FakeCoupleRepository()) : CoupleRepository by fake {
        var result = CompletableDeferred<WriteResult<*>>()
        var calls = 0
        @Suppress("UNCHECKED_CAST")
        private suspend fun <T> answer(): WriteResult<T> {
            calls++
            return result.await() as WriteResult<T>
        }
        override suspend fun createRequest(type: CareRequestType, message: String?): WriteResult<CareRequest> = answer()
        override suspend fun acknowledgeRequest(requestId: Long, actorId: PartnerId): WriteResult<Unit> = answer()
        override suspend fun setMood(option: MoodOption): WriteResult<Unit> = answer()
        override suspend fun activatePersonalSpace(): WriteResult<Unit> = answer()
        override suspend fun endPersonalSpace(): WriteResult<Unit> = answer()
        override suspend fun acceptRequest(requestId: Long): WriteResult<Unit> = answer()
        override suspend fun declineRequest(requestId: Long): WriteResult<Unit> = answer()
    }

    @Test fun allQuickActionsExposeReversalAndUncertaintyWithoutRepeatingWrites() = runTest {
        val actions: List<Pair<(HomeViewModel, ViniciusHomeViewModel) -> Unit, Boolean>> = listOf(
            ({ home: HomeViewModel, _: ViniciusHomeViewModel -> home.createQuickRequest(CareRequestType.DENGO) }) to true,
            ({ home: HomeViewModel, _: ViniciusHomeViewModel -> home.acknowledgeRequest(1) }) to true,
            ({ home: HomeViewModel, _: ViniciusHomeViewModel -> home.setMood(MoodOption.HAPPY) }) to true,
            ({ home: HomeViewModel, _: ViniciusHomeViewModel -> home.activatePersonalSpace() }) to true,
            ({ home: HomeViewModel, _: ViniciusHomeViewModel -> home.endPersonalSpace() }) to true,
            ({ _: HomeViewModel, vinicius: ViniciusHomeViewModel -> vinicius.acceptRequest(1) }) to false,
            ({ _: HomeViewModel, vinicius: ViniciusHomeViewModel -> vinicius.declineRequest(1) }) to false,
        )
        for ((action, isLidianne) in actions) {
            val repository = Repository()
            val home = HomeViewModel(repository)
            val vinicius = ViniciusHomeViewModel(repository)
            val feedback = if (isLidianne) home.quickSubmission else vinicius.quickSubmission
            action(home, vinicius)
            action(home, vinicius)
            advanceUntilIdle()
            assertEquals(1, repository.calls)
            assertEquals(SubmissionState.Sending, feedback.value)
            repository.result.complete(WriteResult.Reverted("rollback"))
            advanceUntilIdle()
            assertTrue(feedback.value is SubmissionState.Reverted)
            assertEquals(1, repository.calls)

            repository.result = CompletableDeferred()
            action(home, vinicius)
            advanceUntilIdle()
            repository.result.complete(WriteResult.Uncertain(IllegalStateException("unknown")))
            advanceUntilIdle()
            assertTrue(feedback.value is SubmissionState.Uncertain)
            assertEquals(2, repository.calls)
        }
    }

    @Test fun exceptionBecomesUncertainAndLaterConfirmationClearsFailure() = runTest {
        val repository = Repository()
        val home = HomeViewModel(repository)
        home.setMood(MoodOption.HAPPY)
        advanceUntilIdle()
        repository.result.completeExceptionally(IllegalStateException("write failed"))
        advanceUntilIdle()
        assertTrue(home.quickSubmission.value is SubmissionState.Uncertain)
        repository.result = CompletableDeferred()
        home.setMood(MoodOption.TIRED)
        advanceUntilIdle()
        repository.result.complete(WriteResult.Confirmed(Unit, true))
        advanceUntilIdle()
        assertEquals(SubmissionState.Confirmed, home.quickSubmission.value)
        assertEquals(2, repository.calls)
    }

    @Test fun cancellationKeepsUncertainFeedbackAndIsPropagated() = runTest {
        val repository = Repository()
        val vinicius = ViniciusHomeViewModel(repository)
        vinicius.acceptRequest(1)
        advanceUntilIdle()
        val cancellation = CancellationException("cancelled")
        repository.result.cancel(cancellation)
        advanceUntilIdle()
        assertTrue(vinicius.quickSubmission.value is SubmissionState.Uncertain)
        assertTrue((vinicius.quickSubmission.value as SubmissionState.Uncertain).cause is CancellationException)
        assertEquals(1, repository.calls)
    }
}
