package com.viniciusrio.dengo.ui.mural

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viniciusrio.dengo.data.FakeCoupleRepository
import com.viniciusrio.dengo.model.CoupleState
import com.viniciusrio.dengo.model.MuralNote
import com.viniciusrio.dengo.model.PartnerId
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class MuralUiState(val notes: List<MuralNote> = emptyList())

class MuralViewModel(private val repository: FakeCoupleRepository) : ViewModel() {
    val state: StateFlow<MuralUiState> = repository.state
        .map(CoupleState::toMuralUiState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = repository.state.value.toMuralUiState(),
        )

    fun leaveNote(authorId: PartnerId, text: String) {
        repository.createMuralNote(authorId, text)
    }
}

internal fun CoupleState.toMuralUiState() = MuralUiState(
    notes = muralNotes.sortedWith(compareByDescending<MuralNote> { it.createdAt }.thenByDescending { it.id }),
)
