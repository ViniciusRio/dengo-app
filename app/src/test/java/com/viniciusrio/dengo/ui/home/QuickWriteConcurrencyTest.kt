package com.viniciusrio.dengo.ui.home

import com.viniciusrio.dengo.data.CoupleRepository
import com.viniciusrio.dengo.data.FakeCoupleRepository
import com.viniciusrio.dengo.data.WriteResult
import com.viniciusrio.dengo.data.confirmed
import com.viniciusrio.dengo.data.ready
import com.viniciusrio.dengo.data.snapshot
import com.viniciusrio.dengo.model.CareRequest
import com.viniciusrio.dengo.model.CareRequestStatus
import com.viniciusrio.dengo.model.CareRequestType
import com.viniciusrio.dengo.model.MoodOption
import com.viniciusrio.dengo.model.PersonalSpace
import com.viniciusrio.dengo.ui.SubmissionState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuickWriteConcurrencyTest {
    @Before fun setUp() = Dispatchers.setMain(StandardTestDispatcher())
    @After fun tearDown() = Dispatchers.resetMain()

    private class ControlledRepository(val fake: FakeCoupleRepository = FakeCoupleRepository()) : CoupleRepository by fake {
        data class Call<T>(val value: T, val completion: CompletableDeferred<WriteResult<Unit>> = CompletableDeferred())
        val requests = mutableListOf<Call<CareRequestType>>()
        val moods = mutableListOf<Call<MoodOption>>()
        val spaces = mutableListOf<Call<Boolean>>()
        val responses = mutableListOf<Call<Pair<Long, Boolean>>>()

        override suspend fun createRequest(type: CareRequestType, message: String?): WriteResult<CareRequest> {
            val call = Call(type).also(requests::add)
            return when (val result = call.completion.await()) {
                is WriteResult.Confirmed -> fake.createRequest(type, message)
                is WriteResult.Reverted -> result
                is WriteResult.Uncertain -> result
            }
        }
        override suspend fun setMood(option: MoodOption): WriteResult<Unit> {
            val call = Call(option).also(moods::add)
            val result = call.completion.await()
            if (result is WriteResult.Confirmed) fake.setMood(option)
            return result
        }
        override suspend fun activatePersonalSpace(): WriteResult<Unit> = space(true)
        override suspend fun endPersonalSpace(): WriteResult<Unit> = space(false)
        private suspend fun space(active: Boolean): WriteResult<Unit> {
            val call = Call(active).also(spaces::add)
            val result = call.completion.await()
            if (result is WriteResult.Confirmed) {
                if (active) fake.activatePersonalSpace() else fake.endPersonalSpace()
            }
            return result
        }
        override suspend fun acceptRequest(requestId: Long): WriteResult<Unit> = respond(requestId, true)
        override suspend fun declineRequest(requestId: Long): WriteResult<Unit> = respond(requestId, false)
        private suspend fun respond(requestId: Long, accept: Boolean): WriteResult<Unit> {
            val call = Call(requestId to accept).also(responses::add)
            val result = call.completion.await()
            if (result is WriteResult.Confirmed) {
                if (accept) fake.acceptRequest(requestId) else fake.declineRequest(requestId)
            }
            return result
        }
    }

    @Test fun olderRequestFailureCannotReplaceNewerMoodResult() = runTest {
        val repository = ControlledRepository()
        val home = HomeViewModel(repository)
        home.createQuickRequest(CareRequestType.DENGO)
        home.createQuickRequest(CareRequestType.DENGO)
        home.setMood(MoodOption.HAPPY)
        advanceUntilIdle()
        assertEquals(1, repository.requests.size)
        assertEquals(1, repository.moods.size)
        repository.moods.single().completion.complete(WriteResult.Uncertain(IllegalStateException("mood unknown")))
        advanceUntilIdle()
        repository.requests.single().completion.complete(WriteResult.Reverted("request rejected"))
        advanceUntilIdle()
        val feedback = home.quickSubmission.value as SubmissionState.Uncertain
        assertEquals("mood unknown", feedback.cause.message)
        assertEquals(null, home.state.value.ready().mood)
        assertEquals(1, repository.requests.size)
    }

    @Test fun olderCancellationCannotReplaceNewerResponseAndDoesNotDuplicateWrites() = runTest {
        val repository = ControlledRepository()
        val first = repository.fake.createRequest(CareRequestType.DENGO).confirmed()
        val second = repository.fake.createRequest(CareRequestType.MEDICINE).confirmed()
        val vinicius = ViniciusHomeViewModel(repository)
        vinicius.acceptRequest(first.id)
        vinicius.acceptRequest(first.id)
        vinicius.declineRequest(second.id)
        advanceUntilIdle()
        assertEquals(listOf(first.id to true, second.id to false), repository.responses.map { it.value })
        repository.responses[1].completion.complete(WriteResult.Confirmed(Unit, true))
        advanceUntilIdle()
        repository.responses[0].completion.cancel(CancellationException("old cancellation"))
        advanceUntilIdle()
        assertEquals(SubmissionState.Confirmed, vinicius.quickSubmission.value)
        assertEquals(listOf(first.id), vinicius.state.value.ready().requests.map { it.id })
        assertEquals(CareRequestStatus.DECLINED, repository.fake.snapshot().requests.single { it.id == second.id }.status)
    }

    @Test fun moodChoicesRunInTapOrderIncludingReturnToFirstChoice() = runTest {
        val repository = ControlledRepository()
        val home = HomeViewModel(repository)
        home.setMood(MoodOption.HAPPY)
        home.setMood(MoodOption.TIRED)
        home.setMood(MoodOption.HAPPY)
        home.setMood(MoodOption.HAPPY)
        advanceUntilIdle()
        assertEquals(listOf(MoodOption.HAPPY), repository.moods.map { it.value })
        repository.moods[0].completion.complete(WriteResult.Confirmed(Unit, true))
        advanceUntilIdle()
        assertEquals(listOf(MoodOption.HAPPY, MoodOption.TIRED), repository.moods.map { it.value })
        repository.moods[1].completion.complete(WriteResult.Confirmed(Unit, true))
        advanceUntilIdle()
        assertEquals(listOf(MoodOption.HAPPY, MoodOption.TIRED, MoodOption.HAPPY), repository.moods.map { it.value })
        repository.moods[2].completion.complete(WriteResult.Confirmed(Unit, true))
        advanceUntilIdle()
        assertEquals(MoodOption.HAPPY, home.state.value.ready().mood?.option)
        assertEquals(SubmissionState.Confirmed, home.quickSubmission.value)
    }

    @Test fun cancelledMoodWriteDoesNotBlockTheLaterChoice() = runTest {
        val repository = ControlledRepository()
        val home = HomeViewModel(repository)
        home.setMood(MoodOption.HAPPY)
        home.setMood(MoodOption.TIRED)
        advanceUntilIdle()
        assertEquals(listOf(MoodOption.HAPPY), repository.moods.map { it.value })
        repository.moods[0].completion.cancel(CancellationException("old mood cancelled"))
        advanceUntilIdle()
        assertEquals(listOf(MoodOption.HAPPY, MoodOption.TIRED), repository.moods.map { it.value })
        assertEquals(SubmissionState.Sending, home.quickSubmission.value)
        repository.moods[1].completion.complete(WriteResult.Confirmed(Unit, true))
        advanceUntilIdle()
        assertEquals(MoodOption.TIRED, home.state.value.ready().mood?.option)
        assertEquals(SubmissionState.Confirmed, home.quickSubmission.value)
    }

    @Test fun olderUncertainResponseCannotReplaceNewerReversal() = runTest {
        val repository = ControlledRepository()
        val first = repository.fake.createRequest(CareRequestType.DENGO).confirmed()
        val second = repository.fake.createRequest(CareRequestType.MEDICINE).confirmed()
        val vinicius = ViniciusHomeViewModel(repository)
        vinicius.acceptRequest(first.id)
        vinicius.declineRequest(second.id)
        advanceUntilIdle()
        repository.responses[1].completion.complete(WriteResult.Reverted("latest rejected"))
        advanceUntilIdle()
        repository.responses[0].completion.complete(WriteResult.Uncertain(IllegalStateException("old unknown")))
        advanceUntilIdle()
        assertEquals("latest rejected", (vinicius.quickSubmission.value as SubmissionState.Reverted).reason)
        assertEquals(2, vinicius.state.value.ready().requests.size)
        assertEquals(2, repository.responses.size)
    }

    @Test fun personalSpaceActionsRunInTapOrderWhileIndependentRequestCanFinish() = runTest {
        val repository = ControlledRepository()
        val home = HomeViewModel(repository)
        home.activatePersonalSpace()
        home.endPersonalSpace()
        home.createQuickRequest(CareRequestType.DENGO)
        advanceUntilIdle()
        assertEquals(listOf(true), repository.spaces.map { it.value })
        assertEquals(1, repository.requests.size)
        repository.requests.single().completion.complete(WriteResult.Confirmed(Unit, true))
        advanceUntilIdle()
        repository.spaces[0].completion.complete(WriteResult.Confirmed(Unit, true))
        advanceUntilIdle()
        assertEquals(listOf(true, false), repository.spaces.map { it.value })
        repository.spaces[1].completion.complete(WriteResult.Confirmed(Unit, true))
        advanceUntilIdle()
        assertEquals(PersonalSpace.Status.INACTIVE, home.state.value.ready().personalSpace.status)
        assertEquals(1, home.state.value.ready().activeRequests.size)
        assertEquals(SubmissionState.Confirmed, home.quickSubmission.value)
    }
}
