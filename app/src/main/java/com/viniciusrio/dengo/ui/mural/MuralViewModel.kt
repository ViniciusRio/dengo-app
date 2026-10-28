package com.viniciusrio.dengo.ui.mural

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viniciusrio.dengo.data.CoupleRepository
import com.viniciusrio.dengo.ui.ScreenReadState
import com.viniciusrio.dengo.ui.SubmissionState
import com.viniciusrio.dengo.ui.project
import com.viniciusrio.dengo.ui.submitWrite
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException

data class MuralUiState(val notes: List<MuralNote> = emptyList())

enum class ComposerMode { CLOSED, CHOOSER, TEXT, DRAWING }

data class MuralComposerState(
    val mode: ComposerMode = ComposerMode.CLOSED,
    val textDraft: String = "",
    val strokes: List<DrawingStroke> = emptyList(),
    val aspectRatio: Float = 1f,
    val selectedArgb: Int = 0xFFA63854.toInt(),
    val confirmDiscard: Boolean = false,
    val submission: SubmissionState = SubmissionState.Idle,
)

class MuralViewModel(private val repository: CoupleRepository) : ViewModel() {
    private val mutableComposer = MutableStateFlow(MuralComposerState())
    val composer: StateFlow<MuralComposerState> = mutableComposer.asStateFlow()
    private val mutableTextSubmission = MutableStateFlow<SubmissionState>(SubmissionState.Idle)
    val textSubmission: StateFlow<SubmissionState> = mutableTextSubmission.asStateFlow()
    private val isPublishing: Boolean
        get() = mutableTextSubmission.value == SubmissionState.Sending ||
            mutableComposer.value.submission == SubmissionState.Sending
    val state: StateFlow<ScreenReadState<MuralUiState>> = repository.state
        .map { it.project(CoupleState::toMuralUiState) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = repository.state.value.project(CoupleState::toMuralUiState),
        )

    fun retryRead() = repository.retryRead()

    fun leaveNote(authorId: PartnerId, text: String) {
        if (mutableComposer.value.mode != ComposerMode.TEXT || isPublishing) return
        mutableTextSubmission.value = SubmissionState.Sending
        viewModelScope.launch {
            try {
                when (val result = submitWrite { repository.createMuralNote(authorId, text) }) {
                    SubmissionState.Confirmed -> {
                        mutableTextSubmission.value = result
                        mutableComposer.value = MuralComposerState()
                    }
                    else -> mutableTextSubmission.value = result
                }
            } catch (cancelled: CancellationException) {
                mutableTextSubmission.value = SubmissionState.Uncertain(cancelled)
                throw cancelled
            }
        }
    }

    fun openChooser() {
        if (isPublishing) return
        mutableComposer.value = MuralComposerState(mode = ComposerMode.CHOOSER)
    }
    fun openText() {
        if (isPublishing) return
        mutableTextSubmission.value = SubmissionState.Idle
        mutableComposer.value = MuralComposerState(mode = ComposerMode.TEXT)
    }
    fun updateTextDraft(text: String) {
        if (!isPublishing) mutableComposer.value = mutableComposer.value.copy(textDraft = text)
    }
    fun openDrawing() {
        if (!isPublishing) mutableComposer.value = MuralComposerState(mode = ComposerMode.DRAWING)
    }
    fun selectColor(argb: Int) {
        if (mutableComposer.value.submission != SubmissionState.Sending) mutableComposer.value = mutableComposer.value.copy(selectedArgb = argb)
    }

    fun addStroke(points: List<DrawingPoint>, aspectRatio: Float, argb: Int = mutableComposer.value.selectedArgb) {
        val current = mutableComposer.value
        if (current.mode != ComposerMode.DRAWING || current.submission == SubmissionState.Sending || points.isEmpty()) return
        mutableComposer.value = current.copy(
            strokes = current.strokes + DrawingStroke(argb, points.toList(), aspectRatio),
            aspectRatio = if (current.strokes.isEmpty()) aspectRatio else current.aspectRatio,
        )
    }

    fun undoStroke() {
        val current = mutableComposer.value
        if (current.submission == SubmissionState.Sending) return
        mutableComposer.value = current.copy(strokes = current.strokes.dropLast(1))
    }

    fun clearDrawing() {
        if (mutableComposer.value.submission != SubmissionState.Sending) mutableComposer.value = mutableComposer.value.copy(strokes = emptyList())
    }

    /** Returns true when closing is complete; otherwise a discard choice is required. */
    fun requestExit(): Boolean {
        val current = mutableComposer.value
        if (isPublishing) return false
        if (current.mode == ComposerMode.DRAWING && current.strokes.isNotEmpty()) {
            mutableComposer.value = current.copy(confirmDiscard = true)
            return false
        }
        mutableComposer.value = MuralComposerState()
        return true
    }

    fun continueDrawing() { mutableComposer.value = mutableComposer.value.copy(confirmDiscard = false) }
    fun discardDraft() {
        if (!isPublishing) mutableComposer.value = MuralComposerState()
    }

    fun publishDrawing(authorId: PartnerId): Boolean {
        val current = mutableComposer.value
        if (current.mode != ComposerMode.DRAWING || current.strokes.isEmpty() || isPublishing) return false
        mutableComposer.value = current.copy(submission = SubmissionState.Sending)
        viewModelScope.launch {
            try {
                when (val result = submitWrite { repository.createMuralDrawing(authorId, current.aspectRatio, current.strokes) }) {
                    SubmissionState.Confirmed -> mutableComposer.value = MuralComposerState()
                    else -> mutableComposer.value = mutableComposer.value.copy(submission = result)
                }
            } catch (cancelled: CancellationException) {
                mutableComposer.value = mutableComposer.value.copy(submission = SubmissionState.Uncertain(cancelled))
                throw cancelled
            }
        }
        return true
    }
}

internal fun CoupleState.toMuralUiState() = MuralUiState(
    notes = muralNotes.sortedWith(compareByDescending<MuralNote> { it.createdAt }.thenByDescending { it.id }),
)
