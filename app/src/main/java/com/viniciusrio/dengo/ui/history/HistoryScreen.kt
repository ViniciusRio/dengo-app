package com.viniciusrio.dengo.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.viniciusrio.dengo.R
import com.viniciusrio.dengo.model.CareRequestType
import com.viniciusrio.dengo.model.HistoryEvent
import com.viniciusrio.dengo.model.MoodOption
import com.viniciusrio.dengo.ui.theme.AppSpacing
import com.viniciusrio.dengo.ui.theme.LidianneAction
import com.viniciusrio.dengo.ui.theme.ViniciusAction
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val timeFormat = DateTimeFormatter.ofPattern("HH:mm", Locale.forLanguageTag("pt-BR"))
private val dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.forLanguageTag("pt-BR"))

internal fun formatHistoryDate(date: LocalDate): String = date.format(dateFormat)
internal fun formatHistoryTime(time: LocalTime): String = time.format(timeFormat)

@Composable
fun HistoryScreen(
    state: HistoryUiState,
    contentPadding: PaddingValues = PaddingValues(),
    today: LocalDate = LocalDate.now(),
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .consumeWindowInsets(contentPadding),
        contentPadding = PaddingValues(horizontal = AppSpacing.Large, vertical = AppSpacing.Base),
    ) {
        item(key = "title") {
            Text(
                text = stringResource(R.string.nav_history),
                modifier = Modifier.semantics { heading() },
                style = MaterialTheme.typography.headlineMedium,
            )
            Spacer(Modifier.height(AppSpacing.Large))
        }
        if (state.days.isEmpty()) {
            item(key = "empty") { EmptyHistory() }
        } else {
            state.days.forEach { day ->
                item(key = "day_${day.date}") {
                    Text(
                        text = dayLabel(day.date, today),
                        modifier = Modifier
                            .padding(bottom = AppSpacing.Medium)
                            .semantics { heading() },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                items(day.items, key = { it.originalIndex }) { item ->
                    HistoryEventRow(item, dayLabel(day.date, today))
                }
                item(key = "space_${day.date}") { Spacer(Modifier.height(AppSpacing.Medium)) }
            }
        }
    }
}

@Composable
private fun EmptyHistory() {
    Column {
        Icon(
            imageVector = Icons.Outlined.FavoriteBorder,
            contentDescription = null,
            tint = LidianneAction,
        )
        Spacer(Modifier.height(AppSpacing.Base))
        Text(
            text = stringResource(R.string.history_empty_title),
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(Modifier.height(AppSpacing.Small))
        Text(
            text = stringResource(R.string.history_empty_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun HistoryEventRow(item: HistoryItem, dayLabel: String) {
    val title = itemTitle(item)
    val context = itemContext(item)
    val time = formatHistoryTime(item.time)
    val accent = when (item.event) {
        is HistoryEvent.RequestAccepted, is HistoryEvent.RequestDeclined -> ViniciusAction
        else -> LidianneAction
    }
    val spoken = listOfNotNull(dayLabel, time, title, context).joinToString(". ")
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { contentDescription = spoken }
            .drawBehind {
                val x = 6.dp.toPx()
                drawLine(
                    color = accent.copy(alpha = 0.24f),
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1.dp.toPx(),
                )
                drawCircle(color = accent, radius = 5.dp.toPx(), center = Offset(x, 12.dp.toPx()))
            }
            .padding(start = AppSpacing.Large, bottom = AppSpacing.Base),
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        context?.let {
            Spacer(Modifier.height(AppSpacing.ExtraSmall))
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(AppSpacing.ExtraSmall))
        Text(text = time, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun dayLabel(date: LocalDate, today: LocalDate): String = when (historyDayLabel(date, today)) {
    HistoryDayLabel.TODAY -> stringResource(R.string.history_today)
    HistoryDayLabel.YESTERDAY -> stringResource(R.string.history_yesterday)
    HistoryDayLabel.DATE -> formatHistoryDate(date)
}

@Composable
private fun itemTitle(item: HistoryItem): String = when (item.event) {
    is HistoryEvent.RequestCreated -> when (item.requestType) {
        CareRequestType.DENGO -> stringResource(R.string.history_requested_dengo)
        CareRequestType.HOT_WATER_BAG -> stringResource(R.string.history_requested_hot_water_bag)
        CareRequestType.MEDICINE -> stringResource(R.string.history_requested_medicine)
        CareRequestType.SPEND_TIME_TOGETHER -> stringResource(R.string.history_requested_spend_time)
        CareRequestType.OTHER, null -> stringResource(R.string.history_requested_generic)
    }
    is HistoryEvent.RequestAccepted -> stringResource(R.string.history_accepted)
    is HistoryEvent.RequestDeclined -> stringResource(R.string.history_declined)
    is HistoryEvent.MoodChanged -> stringResource(R.string.history_mood_changed)
    is HistoryEvent.PersonalSpaceActivated -> stringResource(R.string.history_space_activated)
    is HistoryEvent.PersonalSpaceEnded -> stringResource(R.string.history_space_ended)
}

@Composable
private fun itemContext(item: HistoryItem): String? = when (val event = item.event) {
    is HistoryEvent.RequestCreated -> item.requestMessage
    is HistoryEvent.RequestAccepted, is HistoryEvent.RequestDeclined -> when (item.requestType) {
        null -> null
        CareRequestType.OTHER -> item.requestMessage?.let { stringResource(R.string.history_request_context, it) }
            ?: stringResource(R.string.history_request_context, stringResource(R.string.request_other))
        else -> stringResource(R.string.history_request_context, stringResource(item.requestType.labelRes()))
    }
    is HistoryEvent.MoodChanged -> {
        val (emoji, labelRes) = event.option.presentation()
        "$emoji ${stringResource(labelRes)}"
    }
    is HistoryEvent.PersonalSpaceActivated, is HistoryEvent.PersonalSpaceEnded -> null
}

private fun CareRequestType.labelRes(): Int = when (this) {
    CareRequestType.DENGO -> R.string.request_dengo
    CareRequestType.HOT_WATER_BAG -> R.string.request_hot_water_bag
    CareRequestType.MEDICINE -> R.string.request_medicine
    CareRequestType.SPEND_TIME_TOGETHER -> R.string.request_spend_time
    CareRequestType.OTHER -> R.string.request_other
}

private fun MoodOption.presentation(): Pair<String, Int> = when (this) {
    MoodOption.SAD -> "😞" to R.string.mood_sad
    MoodOption.UPSET -> "😠" to R.string.mood_upset
    MoodOption.TIRED -> "😴" to R.string.mood_tired
    MoodOption.OKAY -> "😐" to R.string.mood_okay
    MoodOption.HAPPY -> "😊" to R.string.mood_happy
    MoodOption.LOVING -> "🥰" to R.string.mood_loving
    MoodOption.NEEDY -> "🥺" to R.string.mood_needy
    MoodOption.VERY_HAPPY -> "🤩" to R.string.mood_very_happy
}
