package com.lidiannevinicius.dengo.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lidiannevinicius.dengo.data.FakeCoupleRepository
import com.lidiannevinicius.dengo.model.CareRequestType
import com.lidiannevinicius.dengo.model.CoupleState
import com.lidiannevinicius.dengo.model.MoodOption
import com.lidiannevinicius.dengo.model.PartnerId
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(private val repository: FakeCoupleRepository) : ViewModel() {
    val state: StateFlow<LidianneHomeState> = repository.state
        .map(CoupleState::toLidianneHomeState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = repository.state.value.toLidianneHomeState(),
        )

    fun createRequest(type: CareRequestType, message: String? = null) {
        repository.createRequest(type, message)
    }

    fun setMood(option: MoodOption) {
        repository.setMood(option)
    }

    fun activatePersonalSpace() {
        repository.activatePersonalSpace()
    }

    fun endPersonalSpace() {
        repository.endPersonalSpace()
    }
}

private fun CoupleState.toLidianneHomeState() = LidianneHomeState(
    latestRequest = requests.lastOrNull { it.requesterId == PartnerId.LIDIANNE },
    mood = mood,
    personalSpace = personalSpace,
)
