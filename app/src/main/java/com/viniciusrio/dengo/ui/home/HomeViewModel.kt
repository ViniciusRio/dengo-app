package com.viniciusrio.dengo.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viniciusrio.dengo.data.CoupleRepository
import com.viniciusrio.dengo.ui.ScreenReadState
import com.viniciusrio.dengo.ui.SubmissionState
import com.viniciusrio.dengo.ui.project
import com.viniciusrio.dengo.ui.submitWrite
import com.viniciusrio.dengo.model.CareRequest
import com.viniciusrio.dengo.model.CareRequestStatus
import com.viniciusrio.dengo.model.CareRequestType
import com.viniciusrio.dengo.model.CoupleState
import com.viniciusrio.dengo.model.HistoryEvent
import com.viniciusrio.dengo.model.MoodOption
import com.viniciusrio.dengo.model.PartnerId
import com.viniciusrio.dengo.model.latestMuralNote
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException

class HomeViewModel(private val repository: CoupleRepository) : ViewModel() {
    val state: StateFlow<ScreenReadState<LidianneHomeState>> = repository.state
        .map { it.project(CoupleState::toLidianneHomeState) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = repository.state.value.project(CoupleState::toLidianneHomeState),
        )
    private val mutableOtherSubmission = MutableStateFlow<SubmissionState>(SubmissionState.Idle)
    val otherSubmission: StateFlow<SubmissionState> = mutableOtherSubmission.asStateFlow()
    private val quickWrites = QuickWriteCoordinator(viewModelScope)
    val quickSubmission: StateFlow<SubmissionState> = quickWrites.submission
    private val mutableOtherDraft = MutableStateFlow("")
    val otherDraft: StateFlow<String> = mutableOtherDraft.asStateFlow()
    private val mutableOtherOpen = MutableStateFlow(false)
    val otherOpen: StateFlow<Boolean> = mutableOtherOpen.asStateFlow()

    fun retryRead() = repository.retryRead()
    fun resetOtherSubmission() { if (mutableOtherSubmission.value != SubmissionState.Sending) mutableOtherSubmission.value = SubmissionState.Idle }
    fun openOtherRequest() {
        resetOtherSubmission()
        mutableOtherOpen.value = true
    }
    fun dismissOtherRequest() {
        if (mutableOtherSubmission.value == SubmissionState.Sending) return
        mutableOtherOpen.value = false
        mutableOtherDraft.value = ""
        resetOtherSubmission()
    }
    fun updateOtherDraft(text: String) {
        if (mutableOtherSubmission.value != SubmissionState.Sending) mutableOtherDraft.value = text
    }

    fun createQuickRequest(type: CareRequestType) {
        require(type != CareRequestType.OTHER)
        quickWrites.independent(type) { repository.createRequest(type) }
    }

    fun createOtherRequest(text: String) {
        require(text.trim().isNotEmpty())
        if (mutableOtherSubmission.value == SubmissionState.Sending) return
        mutableOtherSubmission.value = SubmissionState.Sending
        viewModelScope.launch {
            try {
                val result = submitWrite { repository.createRequest(CareRequestType.OTHER, text) }
                mutableOtherSubmission.value = result
                if (result == SubmissionState.Confirmed) {
                    mutableOtherOpen.value = false
                    mutableOtherDraft.value = ""
                }
            } catch (cancelled: CancellationException) {
                mutableOtherSubmission.value = SubmissionState.Uncertain(cancelled)
                throw cancelled
            }
        }
    }

    fun acknowledgeRequest(requestId: Long) {
        quickWrites.independent("ack" to requestId) { repository.acknowledgeRequest(requestId) }
    }

    fun setMood(option: MoodOption) {
        quickWrites.ordered("mood", option) { repository.setMood(option) }
    }

    fun activatePersonalSpace() {
        quickWrites.ordered("space", true) { repository.activatePersonalSpace() }
    }

    fun endPersonalSpace() {
        quickWrites.ordered("space", false) { repository.endPersonalSpace() }
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
        latestMuralNote = muralNotes.latestMuralNote(),
        activeRequests = ownRequests
            .filter { it.status == CareRequestStatus.PENDING || it.status == CareRequestStatus.ACCEPTED }
            .sortedWith(compareByDescending<CareRequest> { it.createdAt }.thenByDescending { it.id }),
        acknowledgedRequest = (history.lastOrNull() as? HistoryEvent.RequestAcknowledged)
            ?.let { event -> ownRequests.firstOrNull { it.id == event.requestId } },
    )
}
