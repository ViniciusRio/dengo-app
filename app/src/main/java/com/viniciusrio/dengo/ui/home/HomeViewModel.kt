package com.viniciusrio.dengo.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viniciusrio.dengo.data.FakeCoupleRepository
import com.viniciusrio.dengo.model.CareRequest
import com.viniciusrio.dengo.model.CareRequestStatus
import com.viniciusrio.dengo.model.CareRequestType
import com.viniciusrio.dengo.model.CoupleState
import com.viniciusrio.dengo.model.MoodOption
import com.viniciusrio.dengo.model.PartnerId
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

    fun createQuickRequest(type: CareRequestType) {
        require(type != CareRequestType.OTHER)
        repository.createRequest(type)
    }

    fun createOtherRequest(text: String) {
        require(text.isNotBlank())
        repository.createRequest(CareRequestType.OTHER, text)
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

private fun CoupleState.toLidianneHomeState(): LidianneHomeState {
    val ownRequests = requests.filter { it.requesterId == PartnerId.LIDIANNE }
    val latestRequest = ownRequests.maxWithOrNull(
        compareBy<CareRequest> { it.createdAt }.thenBy { it.id },
    )
    return LidianneHomeState(
        latestRequest = latestRequest,
        otherRespondedRequestCount = ownRequests.count {
            it.id != latestRequest?.id && it.status != CareRequestStatus.PENDING
        },
        mood = mood,
        personalSpace = personalSpace,
    )
}
