package com.viniciusrio.dengo.ui.mural

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viniciusrio.dengo.data.FakeCoupleRepository
import com.viniciusrio.dengo.model.CoupleState
import com.viniciusrio.dengo.model.MuralNote
import com.viniciusrio.dengo.model.PartnerId
import com.viniciusrio.dengo.model.DrawingPoint
import com.viniciusrio.dengo.model.DrawingStroke
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class MuralUiState(val notes: List<MuralNote> = emptyList())

enum class ComposerMode { CLOSED, CHOOSER, TEXT, DRAWING }

data class MuralComposerState(
    val mode: ComposerMode = ComposerMode.CLOSED,
    val strokes: List<DrawingStroke> = emptyList(),
    val aspectRatio: Float = 1f,
    val selectedArgb: Int = 0xFFA63854.toInt(),
    val confirmDiscard: Boolean = false,
)

class MuralViewModel(private val repository: FakeCoupleRepository) : ViewModel() {
    private val mutableComposer = MutableStateFlow(MuralComposerState())
    val composer: StateFlow<MuralComposerState> = mutableComposer.asStateFlow()
    val state: StateFlow<MuralUiState> = repository.state
        .map(CoupleState::toMuralUiState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = repository.state.value.toMuralUiState(),
        )

    fun leaveNote(authorId: PartnerId, text: String) {
        repository.createMuralNote(authorId, text)
        mutableComposer.value = MuralComposerState()
    }

    fun openChooser() { mutableComposer.value = MuralComposerState(mode = ComposerMode.CHOOSER) }
    fun openText() { mutableComposer.value = MuralComposerState(mode = ComposerMode.TEXT) }
    fun openDrawing() { mutableComposer.value = MuralComposerState(mode = ComposerMode.DRAWING) }
    fun selectColor(argb: Int) { mutableComposer.value = mutableComposer.value.copy(selectedArgb = argb) }

    fun addStroke(points: List<DrawingPoint>, aspectRatio: Float, argb: Int = mutableComposer.value.selectedArgb) {
        val current = mutableComposer.value
        if (current.mode != ComposerMode.DRAWING || points.isEmpty()) return
        mutableComposer.value = current.copy(
            strokes = current.strokes + DrawingStroke(argb, points.toList(), aspectRatio),
            aspectRatio = if (current.strokes.isEmpty()) aspectRatio else current.aspectRatio,
        )
    }

    fun undoStroke() {
        val current = mutableComposer.value
        mutableComposer.value = current.copy(strokes = current.strokes.dropLast(1))
    }

    fun clearDrawing() { mutableComposer.value = mutableComposer.value.copy(strokes = emptyList()) }

    /** Returns true when closing is complete; otherwise a discard choice is required. */
    fun requestExit(): Boolean {
        val current = mutableComposer.value
        if (current.mode == ComposerMode.DRAWING && current.strokes.isNotEmpty()) {
            mutableComposer.value = current.copy(confirmDiscard = true)
            return false
        }
        mutableComposer.value = MuralComposerState()
        return true
    }

    fun continueDrawing() { mutableComposer.value = mutableComposer.value.copy(confirmDiscard = false) }
    fun discardDraft() { mutableComposer.value = MuralComposerState() }

    fun publishDrawing(authorId: PartnerId): Boolean {
        val current = mutableComposer.value
        if (current.mode != ComposerMode.DRAWING || current.strokes.isEmpty()) return false
        repository.createMuralDrawing(authorId, current.aspectRatio, current.strokes)
        mutableComposer.value = MuralComposerState()
        return true
    }
}

internal fun CoupleState.toMuralUiState() = MuralUiState(
    notes = muralNotes.sortedWith(compareByDescending<MuralNote> { it.createdAt }.thenByDescending { it.id }),
)
