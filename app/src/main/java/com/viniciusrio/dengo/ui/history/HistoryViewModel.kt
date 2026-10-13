package com.viniciusrio.dengo.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viniciusrio.dengo.data.FakeCoupleRepository
import com.viniciusrio.dengo.model.CareRequestType
import com.viniciusrio.dengo.model.CoupleState
import com.viniciusrio.dengo.model.HistoryEvent
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class HistoryUiState(val days: List<HistoryDay> = emptyList())

data class HistoryDay(val date: LocalDate, val entries: List<HistoryEntry>)

sealed interface HistoryEntry {
    val occurredAt: Instant
    val originalIndex: Int
    val date: LocalDate
}

data class RequestStory(
    val requestId: Long,
    val requestType: CareRequestType?,
    val requestMessage: String?,
    val steps: List<RequestStoryStep>,
    override val occurredAt: Instant,
    override val originalIndex: Int,
    override val date: LocalDate,
) : HistoryEntry

data class RequestStoryStep(
    val event: HistoryEvent,
    val date: LocalDate,
    val time: LocalTime,
)

data class IndependentHistoryEntry(
    val event: HistoryEvent,
    val time: LocalTime,
    override val occurredAt: Instant,
    override val originalIndex: Int,
    override val date: LocalDate,
) : HistoryEntry

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
    val indexedEvents = history.withIndex().toList()
    val requestEvents = indexedEvents.filter { it.value.requestIdOrNull() != null }
    val stories = requestEvents.groupBy { requireNotNull(it.value.requestIdOrNull()) }
        .map { (requestId, events) ->
            val request = requestsById[requestId]
            val latest = events.maxWith(compareBy<IndexedValue<HistoryEvent>> { it.value.occurredAt }.thenBy { it.index })
            RequestStory(
                requestId = requestId,
                requestType = request?.type,
                requestMessage = request?.message?.takeIf { request.type == CareRequestType.OTHER },
                steps = events.sortedBy { it.index }.map { (_, event) ->
                    val local = event.occurredAt.atZone(zone)
                    RequestStoryStep(event, local.toLocalDate(), local.toLocalTime())
                },
                occurredAt = latest.value.occurredAt,
                originalIndex = latest.index,
                date = latest.value.occurredAt.atZone(zone).toLocalDate(),
            )
        }
    val independent = indexedEvents.filter { it.value.requestIdOrNull() == null }
        .map { (index, event) ->
            val local = event.occurredAt.atZone(zone)
            IndependentHistoryEntry(event, local.toLocalTime(), event.occurredAt, index, local.toLocalDate())
        }
    val entries = (stories + independent).sortedWith(
        compareByDescending<HistoryEntry> { it.occurredAt }.thenByDescending { it.originalIndex },
    )
    return HistoryUiState(
        days = entries.groupBy { it.date }.map { (date, dayEntries) -> HistoryDay(date, dayEntries) },
    )
}

private fun HistoryEvent.requestIdOrNull(): Long? = when (this) {
    is HistoryEvent.RequestCreated -> requestId
    is HistoryEvent.RequestAccepted -> requestId
    is HistoryEvent.RequestDeclined -> requestId
    is HistoryEvent.RequestAcknowledged -> requestId
    else -> null
}
