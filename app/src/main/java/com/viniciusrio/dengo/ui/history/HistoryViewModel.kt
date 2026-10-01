package com.viniciusrio.dengo.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viniciusrio.dengo.data.FakeCoupleRepository
import com.viniciusrio.dengo.model.CareRequestType
import com.viniciusrio.dengo.model.CoupleState
import com.viniciusrio.dengo.model.HistoryEvent
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class HistoryUiState(val days: List<HistoryDay> = emptyList())

data class HistoryDay(val date: LocalDate, val items: List<HistoryItem>)

data class HistoryItem(
    val originalIndex: Int,
    val event: HistoryEvent,
    val time: LocalTime,
    val requestType: CareRequestType? = null,
    val requestMessage: String? = null,
)

internal enum class HistoryDayLabel { TODAY, YESTERDAY, DATE }

internal fun historyDayLabel(date: LocalDate, today: LocalDate): HistoryDayLabel = when (date) {
    today -> HistoryDayLabel.TODAY
    today.minusDays(1) -> HistoryDayLabel.YESTERDAY
    else -> HistoryDayLabel.DATE
}

class HistoryViewModel(
    repository: FakeCoupleRepository,
    clock: Clock = Clock.systemDefaultZone(),
) : ViewModel() {
    val state: StateFlow<HistoryUiState> = repository.state
        .map { it.toHistoryUiState(clock.zone) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = repository.state.value.toHistoryUiState(clock.zone),
        )
}

internal fun CoupleState.toHistoryUiState(zone: ZoneId): HistoryUiState {
    val requestsById = requests.associateBy { it.id }
    val items = history.withIndex()
        .sortedWith(
            compareByDescending<IndexedValue<HistoryEvent>> { it.value.occurredAt }
                .thenByDescending { it.index },
        )
        .map { (index, event) ->
            val request = when (event) {
                is HistoryEvent.RequestCreated -> requestsById[event.requestId]
                is HistoryEvent.RequestAccepted -> requestsById[event.requestId]
                is HistoryEvent.RequestDeclined -> requestsById[event.requestId]
                else -> null
            }
            HistoryItem(
                originalIndex = index,
                event = event,
                time = event.occurredAt.atZone(zone).toLocalTime(),
                requestType = request?.type,
                requestMessage = request?.message?.takeIf { request.type == CareRequestType.OTHER },
            )
        }
    return HistoryUiState(
        days = items.groupBy { it.event.occurredAt.atZone(zone).toLocalDate() }
            .map { (date, dayItems) -> HistoryDay(date, dayItems) },
    )
}
