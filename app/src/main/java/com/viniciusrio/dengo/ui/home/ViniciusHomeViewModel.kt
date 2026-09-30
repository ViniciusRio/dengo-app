package com.viniciusrio.dengo.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viniciusrio.dengo.data.FakeCoupleRepository
import com.viniciusrio.dengo.model.CareRequest
import com.viniciusrio.dengo.model.CoupleState
import com.viniciusrio.dengo.model.Mood
import com.viniciusrio.dengo.model.PartnerId
import com.viniciusrio.dengo.model.PersonalSpace
import java.time.Clock
import java.time.LocalDate
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class ViniciusHomeState(
    val requests: List<CareRequest>,
    val mood: Mood?,
    val isMoodToday: Boolean,
    val isPersonalSpaceActive: Boolean,
)

class ViniciusHomeViewModel(
    private val repository: FakeCoupleRepository,
    private val clock: Clock = Clock.systemDefaultZone(),
) : ViewModel() {
    val state: StateFlow<ViniciusHomeState> = repository.state
        .map(::toHomeState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = toHomeState(repository.state.value),
        )

    fun acceptRequest(requestId: Long) {
        repository.acceptRequest(requestId)
    }

    fun declineRequest(requestId: Long) {
        repository.declineRequest(requestId)
    }

    private fun toHomeState(couple: CoupleState): ViniciusHomeState {
        val lidianneMood = couple.mood?.takeIf { it.partnerId == PartnerId.LIDIANNE }
        return ViniciusHomeState(
            requests = couple.requests
                .filter { it.recipientId == PartnerId.VINICIUS }
                .sortedWith(compareByDescending<CareRequest> { it.createdAt }.thenByDescending { it.id }),
            mood = lidianneMood,
            isMoodToday = lidianneMood?.date == LocalDate.now(clock),
            isPersonalSpaceActive = couple.personalSpace.status == PersonalSpace.Status.ACTIVE,
        )
    }
}
