package com.viniciusrio.dengo.ui

import com.viniciusrio.dengo.data.CoupleRepository
import com.viniciusrio.dengo.data.FakeCoupleRepository
import com.viniciusrio.dengo.data.RepositoryReadState
import com.viniciusrio.dengo.data.WriteResult
import com.viniciusrio.dengo.model.CareRequest
import com.viniciusrio.dengo.model.CareRequestStatus
import com.viniciusrio.dengo.model.CareRequestType
import com.viniciusrio.dengo.model.DrawingPoint
import com.viniciusrio.dengo.model.DrawingStroke
import com.viniciusrio.dengo.model.MuralContent
import com.viniciusrio.dengo.model.MuralNote
import com.viniciusrio.dengo.model.PartnerId
import com.viniciusrio.dengo.ui.history.HistoryViewModel
import com.viniciusrio.dengo.ui.home.HomeViewModel
import com.viniciusrio.dengo.ui.home.ViniciusHomeViewModel
import com.viniciusrio.dengo.ui.mural.ComposerMode
import com.viniciusrio.dengo.ui.mural.MuralViewModel
import java.time.Instant
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CancellationException
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
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SubmissionViewModelTest {
    @Before fun setUp() = Dispatchers.setMain(StandardTestDispatcher())
    @After fun tearDown() = Dispatchers.resetMain()

    private class ControlledRepository(private val fake: FakeCoupleRepository = FakeCoupleRepository()) : CoupleRepository by fake {
        override val state = MutableStateFlow<RepositoryReadState>(RepositoryReadState.Loading)
        var retryCalls = 0
        var requestWrites = 0
        var textWrites = 0
        var drawingWrites = 0
        var requestResult = CompletableDeferred<WriteResult<CareRequest>>()
        var textResult = CompletableDeferred<WriteResult<MuralNote>>()
        var drawingResult = CompletableDeferred<WriteResult<MuralNote>>()

        override fun retryRead() {
            retryCalls++
            state.value = fake.state.value
        }

        override suspend fun createRequest(type: CareRequestType, message: String?): WriteResult<CareRequest> {
            requestWrites++
            return requestResult.await()
        }
        override suspend fun createMuralNote(authorId: PartnerId, text: String): WriteResult<MuralNote> {
            textWrites++
            return textResult.await()
        }
        override suspend fun createMuralDrawing(authorId: PartnerId, aspectRatio: Float, strokes: List<DrawingStroke>): WriteResult<MuralNote> {
            drawingWrites++
            return drawingResult.await()
        }
    }

    @Test fun retryReadRecoversAllFourProjectionsWithoutInventingEmptyData() = runTest {
        val repository = ControlledRepository()
        val home = HomeViewModel(repository)
        val vinicius = ViniciusHomeViewModel(repository)
        val history = HistoryViewModel(repository)
        val mural = MuralViewModel(repository)
        advanceUntilIdle()
        repository.state.value = RepositoryReadState.ReadError(IllegalStateException("read"))
        advanceUntilIdle()
        assertTrue(home.state.value is ScreenReadState.ReadError)
        assertTrue(vinicius.state.value is ScreenReadState.ReadError)
        assertTrue(history.state.value is ScreenReadState.ReadError)
        assertTrue(mural.state.value is ScreenReadState.ReadError)

        home.retryRead()
        advanceUntilIdle()
        assertEquals(1, repository.retryCalls)
        assertTrue(home.state.value is ScreenReadState.Ready)
        assertTrue(vinicius.state.value is ScreenReadState.Ready)
        assertTrue(history.state.value is ScreenReadState.Ready)
        assertTrue(mural.state.value is ScreenReadState.Ready)
    }

    @Test fun cancellationIsPropagatedAndDoesNotClaimRollback() = runTest {
        val cancellation = CancellationException("operation cancelled")
        try {
            submitWrite { throw cancellation }
            fail("CancellationException was absorbed")
        } catch (caught: CancellationException) {
            assertSame(cancellation, caught)
        }
    }

    @Test fun allFourViewModelsKeepLoadingAndReadErrorDistinctFromReady() = runTest {
        val repository = ControlledRepository()
        val home = HomeViewModel(repository)
        val vinicius = ViniciusHomeViewModel(repository)
        val history = HistoryViewModel(repository)
        val mural = MuralViewModel(repository)
        advanceUntilIdle()
        assertEquals(ScreenReadState.Loading, home.state.value)
        assertEquals(ScreenReadState.Loading, vinicius.state.value)
        assertEquals(ScreenReadState.Loading, history.state.value)
        assertEquals(ScreenReadState.Loading, mural.state.value)
        repository.state.value = RepositoryReadState.ReadError(IllegalStateException("read"))
        advanceUntilIdle()
        assertTrue(home.state.value is ScreenReadState.ReadError)
        assertTrue(vinicius.state.value is ScreenReadState.ReadError)
        assertTrue(history.state.value is ScreenReadState.ReadError)
        assertTrue(mural.state.value is ScreenReadState.ReadError)
    }

    @Test fun otherRequestWaitsForConfirmationAndKeepsDraftOnReversalOrUncertainty() = runTest {
        val repository = ControlledRepository()
        val home = HomeViewModel(repository)
        home.openOtherRequest()
        home.updateOtherDraft("Um abraço")
        home.createOtherRequest("Um abraço")
        home.createOtherRequest("Um abraço")
        advanceUntilIdle()
        assertEquals(1, repository.requestWrites)
        assertEquals(SubmissionState.Sending, home.otherSubmission.value)
        home.updateOtherDraft("Texto diferente")
        assertEquals("Um abraço", home.otherDraft.value)
        repository.state.value = RepositoryReadState.ReadError(IllegalStateException("read"))
        advanceUntilIdle()
        assertEquals("Um abraço", home.otherDraft.value)
        assertTrue(home.otherOpen.value)
        repository.requestResult.complete(WriteResult.Reverted("rollback"))
        advanceUntilIdle()
        assertTrue(home.otherSubmission.value is SubmissionState.Reverted)
        assertEquals("Um abraço", home.otherDraft.value)
        assertTrue(home.otherOpen.value)

        repository.requestResult = CompletableDeferred()
        home.createOtherRequest("Um abraço")
        advanceUntilIdle()
        repository.requestResult.complete(WriteResult.Uncertain(IllegalStateException("unknown")))
        advanceUntilIdle()
        assertTrue(home.otherSubmission.value is SubmissionState.Uncertain)
        assertEquals("Um abraço", home.otherDraft.value)
        assertTrue(home.otherOpen.value)
        assertEquals(2, repository.requestWrites)

        repository.requestResult = CompletableDeferred()
        home.createOtherRequest("Um abraço")
        advanceUntilIdle()
        val request = CareRequest(1, CareRequestType.OTHER, PartnerId.LIDIANNE, PartnerId.VINICIUS,
            CareRequestStatus.PENDING, Instant.EPOCH, "Um abraço")
        repository.requestResult.complete(WriteResult.Confirmed(request, true))
        advanceUntilIdle()
        assertEquals(SubmissionState.Confirmed, home.otherSubmission.value)
        assertEquals("", home.otherDraft.value)
        assertTrue(!home.otherOpen.value)
    }

    @Test fun muralTextWaitsForConfirmationAndDoesNotRepeatPendingWrite() = runTest {
        val repository = ControlledRepository()
        val mural = MuralViewModel(repository)
        mural.openText()
        mural.updateTextDraft("Oi")
        mural.leaveNote(PartnerId.LIDIANNE, "Oi")
        mural.leaveNote(PartnerId.LIDIANNE, "Oi")
        advanceUntilIdle()
        assertEquals(1, repository.textWrites)
        assertEquals(ComposerMode.TEXT, mural.composer.value.mode)
        mural.updateTextDraft("Alterado durante envio")
        assertEquals("Oi", mural.composer.value.textDraft)
        repository.state.value = RepositoryReadState.ReadError(IllegalStateException("read"))
        advanceUntilIdle()
        assertEquals("Oi", mural.composer.value.textDraft)
        repository.textResult.complete(WriteResult.Reverted("rollback"))
        advanceUntilIdle()
        assertEquals(ComposerMode.TEXT, mural.composer.value.mode)
        assertTrue(mural.textSubmission.value is SubmissionState.Reverted)
        assertEquals("Oi", mural.composer.value.textDraft)
        repository.textResult = CompletableDeferred()
        mural.leaveNote(PartnerId.LIDIANNE, "Oi")
        advanceUntilIdle()
        repository.textResult.complete(WriteResult.Uncertain(IllegalStateException("unknown")))
        advanceUntilIdle()
        assertEquals(ComposerMode.TEXT, mural.composer.value.mode)
        assertTrue(mural.textSubmission.value is SubmissionState.Uncertain)
        assertEquals("Oi", mural.composer.value.textDraft)
        repository.textResult = CompletableDeferred()
        mural.leaveNote(PartnerId.LIDIANNE, "Oi")
        advanceUntilIdle()
        repository.textResult.complete(WriteResult.Confirmed(MuralNote(1, PartnerId.LIDIANNE, MuralContent.Text("Oi"), Instant.EPOCH), true))
        advanceUntilIdle()
        assertEquals(ComposerMode.CLOSED, mural.composer.value.mode)
    }

    @Test fun pendingTextCannotSwitchModeDiscardDraftOrStartAnotherPublication() = runTest {
        val repository = ControlledRepository()
        val mural = MuralViewModel(repository)
        mural.openText()
        mural.updateTextDraft("Ainda escrevendo")
        mural.leaveNote(PartnerId.LIDIANNE, "Ainda escrevendo")
        advanceUntilIdle()

        mural.openText()
        mural.openDrawing()
        mural.discardDraft()
        mural.leaveNote(PartnerId.LIDIANNE, "Ainda escrevendo")
        assertTrue(!mural.publishDrawing(PartnerId.LIDIANNE))
        advanceUntilIdle()

        assertEquals(ComposerMode.TEXT, mural.composer.value.mode)
        assertEquals("Ainda escrevendo", mural.composer.value.textDraft)
        assertEquals(SubmissionState.Sending, mural.textSubmission.value)
        assertEquals(1, repository.textWrites)
        assertEquals(0, repository.drawingWrites)
    }

    @Test fun pendingDrawingCannotSwitchModeDiscardStrokesOrStartAnotherPublication() = runTest {
        val repository = ControlledRepository()
        val mural = MuralViewModel(repository)
        mural.openDrawing()
        mural.addStroke(listOf(DrawingPoint(0.5f, 0.5f)), 1f)
        assertTrue(mural.publishDrawing(PartnerId.VINICIUS))
        advanceUntilIdle()

        mural.openDrawing()
        mural.openText()
        mural.discardDraft()
        mural.leaveNote(PartnerId.VINICIUS, "Não publicar")
        assertTrue(!mural.publishDrawing(PartnerId.VINICIUS))
        advanceUntilIdle()

        assertEquals(ComposerMode.DRAWING, mural.composer.value.mode)
        assertEquals(1, mural.composer.value.strokes.size)
        assertEquals(SubmissionState.Sending, mural.composer.value.submission)
        assertEquals(1, repository.drawingWrites)
        assertEquals(0, repository.textWrites)
    }

    @Test fun muralDrawingKeepsStrokesUntilConfirmedAndDoesNotRepeatPendingWrite() = runTest {
        val repository = ControlledRepository()
        val mural = MuralViewModel(repository)
        mural.openDrawing()
        mural.addStroke(listOf(DrawingPoint(0.5f, 0.5f)), 1f)
        assertTrue(mural.publishDrawing(PartnerId.VINICIUS))
        assertTrue(!mural.publishDrawing(PartnerId.VINICIUS))
        advanceUntilIdle()
        assertEquals(1, repository.drawingWrites)
        assertEquals(1, mural.composer.value.strokes.size)
        mural.clearDrawing()
        assertEquals(1, mural.composer.value.strokes.size)
        repository.drawingResult.complete(WriteResult.Reverted("rollback"))
        advanceUntilIdle()
        assertEquals(1, mural.composer.value.strokes.size)
        repository.drawingResult = CompletableDeferred()
        mural.publishDrawing(PartnerId.VINICIUS)
        advanceUntilIdle()
        repository.drawingResult.complete(WriteResult.Uncertain(IllegalStateException("unknown")))
        advanceUntilIdle()
        assertEquals(1, mural.composer.value.strokes.size)
        assertTrue(mural.composer.value.submission is SubmissionState.Uncertain)
        repository.drawingResult = CompletableDeferred()
        mural.publishDrawing(PartnerId.VINICIUS)
        advanceUntilIdle()
        repository.drawingResult.complete(WriteResult.Confirmed(MuralNote(1, PartnerId.VINICIUS,
            MuralContent.Drawing(1f, mural.composer.value.strokes), Instant.EPOCH), true))
        advanceUntilIdle()
        assertEquals(ComposerMode.CLOSED, mural.composer.value.mode)
    }

    @Test fun unexpectedWriteFailureKeepsOtherDraftAndDoesNotClaimSuccess() = runTest {
        val repository = ControlledRepository()
        val home = HomeViewModel(repository)
        home.openOtherRequest()
        home.updateOtherDraft("Preciso conversar")
        home.createOtherRequest("Preciso conversar")
        advanceUntilIdle()
        repository.requestResult.completeExceptionally(IllegalStateException("write failed"))
        advanceUntilIdle()
        assertTrue(home.otherSubmission.value is SubmissionState.Uncertain)
        assertTrue(home.otherOpen.value)
        assertEquals("Preciso conversar", home.otherDraft.value)
    }

    @Test fun cancelledWriteNeverClaimsConfirmationOrDropsDraft() = runTest {
        val repository = ControlledRepository()
        val home = HomeViewModel(repository)
        home.openOtherRequest()
        home.updateOtherDraft("Meu pedido")
        home.createOtherRequest("Meu pedido")
        advanceUntilIdle()
        repository.requestResult.cancel()
        advanceUntilIdle()
        assertTrue(home.otherSubmission.value is SubmissionState.Uncertain)
        assertEquals("Meu pedido", home.otherDraft.value)
        assertTrue(home.otherOpen.value)
        assertEquals(1, repository.requestWrites)
    }
}
