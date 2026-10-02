package com.viniciusrio.dengo.ui.mural

import com.viniciusrio.dengo.data.FakeCoupleRepository
import com.viniciusrio.dengo.model.CoupleState
import com.viniciusrio.dengo.model.MuralNote
import com.viniciusrio.dengo.model.MuralContent
import com.viniciusrio.dengo.model.DrawingPoint
import com.viniciusrio.dengo.model.Partner
import com.viniciusrio.dengo.model.PartnerId
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MuralViewModelTest {
    private val now = Instant.parse("2026-09-29T14:32:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)

    @Before fun setUp() = Dispatchers.setMain(StandardTestDispatcher())
    @After fun tearDown() = Dispatchers.resetMain()

    @Test fun initialStateIsEmpty() = runTest {
        val viewModel = MuralViewModel(FakeCoupleRepository(clock))
        advanceUntilIdle()
        assertTrue(viewModel.state.value.notes.isEmpty())
    }

    @Test fun drawingDraftKeepsStrokeColorsUndoClearAndDiscard() = runTest {
        val repository = FakeCoupleRepository(clock)
        val viewModel = MuralViewModel(repository)
        viewModel.openDrawing()
        viewModel.selectColor(0xFFA63854.toInt())
        viewModel.addStroke(listOf(DrawingPoint(0.1f, 0.2f)), 1.5f)
        viewModel.selectColor(0xFF315F91.toInt())
        viewModel.addStroke(listOf(DrawingPoint(0.3f, 0.4f)), 2f)
        viewModel.addStroke(listOf(DrawingPoint(0.5f, 0.6f)), 2f, 0xFFA63854.toInt())
        assertEquals(listOf(0xFFA63854.toInt(), 0xFF315F91.toInt(), 0xFFA63854.toInt()), viewModel.composer.value.strokes.map { it.argb })
        assertEquals(listOf(1.5f, 2f, 2f), viewModel.composer.value.strokes.map { it.aspectRatio })
        assertEquals(false, viewModel.requestExit())
        assertTrue(viewModel.composer.value.confirmDiscard)
        viewModel.continueDrawing()
        viewModel.undoStroke()
        assertEquals(2, viewModel.composer.value.strokes.size)
        viewModel.clearDrawing()
        assertTrue(viewModel.composer.value.strokes.isEmpty())
        assertEquals(false, viewModel.publishDrawing(PartnerId.LIDIANNE))
        assertEquals(true, viewModel.requestExit())
        assertTrue(repository.state.value.muralNotes.isEmpty())
    }

    @Test fun discardingNonEmptyDrawingNeverPublishes() = runTest {
        val repository = FakeCoupleRepository(clock)
        val viewModel = MuralViewModel(repository)
        viewModel.openDrawing()
        viewModel.addStroke(listOf(DrawingPoint(0.5f, 0.5f)), 1f)
        assertEquals(false, viewModel.requestExit())
        viewModel.discardDraft()

        assertEquals(ComposerMode.CLOSED, viewModel.composer.value.mode)
        assertTrue(repository.state.value.muralNotes.isEmpty())
    }

    @Test fun drawingPublishesOnlyOnceAndClosesComposer() = runTest {
        val repository = FakeCoupleRepository(clock)
        val viewModel = MuralViewModel(repository)
        viewModel.openDrawing()
        assertEquals(false, viewModel.publishDrawing(PartnerId.LIDIANNE))
        viewModel.addStroke(listOf(DrawingPoint(0.5f, 0.5f)), 1f)
        assertEquals(true, viewModel.publishDrawing(PartnerId.LIDIANNE))
        assertEquals(false, viewModel.publishDrawing(PartnerId.LIDIANNE))
        assertEquals(1, repository.state.value.muralNotes.size)
        assertEquals(ComposerMode.CLOSED, viewModel.composer.value.mode)
    }

    @Test fun bothPerspectivesPublishIntoAndObserveOneSharedList() = runTest {
        val repository = FakeCoupleRepository(clock)
        val viewModel = MuralViewModel(repository)
        viewModel.leaveNote(PartnerId.LIDIANNE, "Bom dia 💗")
        viewModel.leaveNote(PartnerId.VINICIUS, "Bom dia 💙")
        advanceUntilIdle()

        assertEquals(listOf(PartnerId.VINICIUS, PartnerId.LIDIANNE), viewModel.state.value.notes.map { it.authorId })
        assertEquals(repository.state.value.muralNotes.reversed(), viewModel.state.value.notes)
    }

    @Test fun newestTimestampWinsAndIdBreaksTies() {
        val early = Instant.parse("2026-09-29T12:00:00Z")
        val later = Instant.parse("2026-09-29T13:00:00Z")
        val notes = listOf(
            MuralNote(3, PartnerId.VINICIUS, MuralContent.Text("Antigo"), early),
            MuralNote(2, PartnerId.LIDIANNE, MuralContent.Text("Novo"), later),
            MuralNote(1, PartnerId.VINICIUS, MuralContent.Text("Mesmo horário"), later),
        )
        val state = CoupleState(partners = listOf(Partner(PartnerId.LIDIANNE, "Lidianne")), muralNotes = notes)

        assertEquals(listOf(2L, 1L, 3L), state.toMuralUiState().notes.map { it.id })
    }

    @Test fun timestampUsesConsultationDayAndLocalZone() {
        val zone = ZoneId.of("America/Fortaleza")
        val today = LocalDate.of(2026, 9, 30)
        assertEquals("Hoje, 00:15", formatMuralTimestamp(Instant.parse("2026-09-30T03:15:00Z"), today, zone))
        assertEquals("Ontem, 23:45", formatMuralTimestamp(Instant.parse("2026-09-30T02:45:00Z"), today, zone))
        assertEquals("28/09/2026, 15:00", formatMuralTimestamp(Instant.parse("2026-09-28T18:00:00Z"), today, zone))
    }
}
