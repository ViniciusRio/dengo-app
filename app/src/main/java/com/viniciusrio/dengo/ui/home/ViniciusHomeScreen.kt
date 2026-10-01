package com.viniciusrio.dengo.ui.home

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.PauseCircleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.viniciusrio.dengo.R
import com.viniciusrio.dengo.model.CareRequest
import com.viniciusrio.dengo.model.CareRequestStatus
import com.viniciusrio.dengo.model.CareRequestType
import com.viniciusrio.dengo.model.MoodOption
import com.viniciusrio.dengo.ui.theme.AppSpacing
import com.viniciusrio.dengo.ui.theme.LidianneAction
import com.viniciusrio.dengo.ui.theme.LidiannePink
import com.viniciusrio.dengo.ui.theme.LidiannePinkSoft
import com.viniciusrio.dengo.ui.theme.ViniciusAction
import com.viniciusrio.dengo.ui.theme.ViniciusBlueSoft
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ViniciusHomeScreen(
    state: ViniciusHomeState,
    onAcceptRequest: (Long) -> Unit,
    onDeclineRequest: (Long) -> Unit,
    onOpenMural: () -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(),
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .consumeWindowInsets(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AppSpacing.Large),
    ) {
        Spacer(Modifier.height(AppSpacing.Base))
        HomeIdentity(state)

        Spacer(Modifier.height(AppSpacing.Large))
        Text(
            text = stringResource(R.string.vinicius_requests_title),
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(Modifier.height(AppSpacing.Small))
        if (state.requests.isEmpty()) {
            EmptyRequests()
        } else {
            state.requests.forEach { request ->
                RequestNote(
                    request = request,
                    actionsEnabled = !state.isPersonalSpaceActive,
                    onAccept = { onAcceptRequest(request.id) },
                    onDecline = { onDeclineRequest(request.id) },
                )
                Spacer(Modifier.height(AppSpacing.Medium))
            }
        }
        state.acknowledgedRequest?.let { request ->
            Spacer(Modifier.height(AppSpacing.Small))
            AcknowledgementNote(request)
        }

        Spacer(Modifier.height(AppSpacing.Large))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f))
        Spacer(Modifier.height(AppSpacing.Base))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clickable(onClickLabel = stringResource(R.string.home_mural_open), role = Role.Button, onClick = onOpenMural),
        ) {
            Text(
                text = stringResource(R.string.home_mural_title),
                modifier = Modifier.semantics { heading() },
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(AppSpacing.Small))
            MuralPreviewContent(state.latestMuralNote)
        }
        Spacer(Modifier.height(AppSpacing.Large))
    }
}

@Composable
private fun AcknowledgementNote(request: CareRequest) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            modifier = Modifier.padding(AppSpacing.Base),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.Medium),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                imageVector = Icons.Outlined.FavoriteBorder,
                contentDescription = null,
                tint = LidianneAction,
                modifier = Modifier.size(24.dp),
            )
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.vinicius_acknowledged_feedback),
                    style = MaterialTheme.typography.labelLarge,
                    color = LidianneAction,
                )
                Spacer(Modifier.height(AppSpacing.ExtraSmall))
                Text(
                    text = stringResource(R.string.vinicius_acknowledged_message),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.height(AppSpacing.ExtraSmall))
                Text(
                    text = request.message ?: stringResource(request.type.labelRes()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun HomeIdentity(state: ViniciusHomeState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(ViniciusBlueSoft)
            .padding(AppSpacing.Large),
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.Base),
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.vinicius_greeting),
                    modifier = Modifier.semantics { heading() },
                    style = MaterialTheme.typography.headlineMedium,
                    color = ViniciusAction,
                )
                Spacer(Modifier.height(AppSpacing.ExtraSmall))
                Text(
                    text = stringResource(R.string.vinicius_intro),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
            ) {
                Icon(
                    imageVector = Icons.Outlined.FavoriteBorder,
                    contentDescription = null,
                    tint = ViniciusAction,
                    modifier = Modifier.align(Alignment.Center).size(26.dp),
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(13.dp)
                        .clip(CircleShape)
                        .background(LidiannePink),
                )
            }
        }
        Spacer(Modifier.height(AppSpacing.Base))
        HorizontalDivider(color = ViniciusAction.copy(alpha = 0.18f))
        Spacer(Modifier.height(AppSpacing.Base))
        if (state.isPersonalSpaceActive) {
            PersonalSpaceNotice()
            Spacer(Modifier.height(AppSpacing.Base))
            HorizontalDivider(color = ViniciusAction.copy(alpha = 0.18f))
            Spacer(Modifier.height(AppSpacing.Base))
        }
        MoodSummary(state)
    }
}

@Composable
private fun PersonalSpaceNotice() {
    Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.Medium)) {
        Icon(
            imageVector = Icons.Outlined.PauseCircleOutline,
            contentDescription = null,
            tint = ViniciusAction,
            modifier = Modifier.size(24.dp),
        )
        Column {
            Text(
                text = stringResource(R.string.vinicius_space_title),
                style = MaterialTheme.typography.titleMedium,
                color = ViniciusAction,
            )
            Spacer(Modifier.height(AppSpacing.ExtraSmall))
            Text(
                text = stringResource(R.string.vinicius_space_body),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun MoodSummary(state: ViniciusHomeState) {
    val mood = state.mood
    Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.Medium)) {
        Box(
            modifier = Modifier
                .padding(top = AppSpacing.Small)
                .size(10.dp)
                .clip(CircleShape)
                .background(LidiannePink),
        )
        Column {
            Text(
                text = stringResource(R.string.vinicius_mood_label),
                style = MaterialTheme.typography.labelMedium,
                color = LidianneAction,
            )
            Spacer(Modifier.height(AppSpacing.ExtraSmall))
            if (mood == null) {
                Text(
                    text = stringResource(R.string.vinicius_mood_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            } else {
                val (emoji, labelRes) = mood.option.presentation()
                Text(
                    text = if (state.isMoodToday) stringResource(R.string.vinicius_mood_today, emoji, stringResource(labelRes).lowercase(Locale.forLanguageTag("pt-BR")))
                    else stringResource(R.string.vinicius_mood_previous, emoji, stringResource(labelRes)),
                    style = MaterialTheme.typography.titleMedium,
                )
                if (!state.isMoodToday) {
                    Spacer(Modifier.height(AppSpacing.ExtraSmall))
                    Text(
                        text = mood.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyRequests() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = AppSpacing.Base),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.Medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.FavoriteBorder,
            contentDescription = null,
            tint = LidianneAction,
            modifier = Modifier
                .clip(CircleShape)
                .background(LidiannePinkSoft)
                .padding(AppSpacing.Medium)
                .size(20.dp),
        )
        Column {
            Text(stringResource(R.string.vinicius_requests_empty), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(AppSpacing.ExtraSmall))
            Text(
                stringResource(R.string.vinicius_requests_empty_detail),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun RequestNote(
    request: CareRequest,
    actionsEnabled: Boolean,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
) {
    val isPending = request.status == CareRequestStatus.PENDING
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = if (isPending) LidiannePinkSoft else MaterialTheme.colorScheme.surface,
    ) {
        Column(Modifier.padding(AppSpacing.Base)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = stringResource(request.type.labelRes()),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isPending) LidianneAction else MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = request.createdAt.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd/MM HH:mm")),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            request.message?.let { message ->
                Spacer(Modifier.height(AppSpacing.Small))
                Text(message, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(AppSpacing.Small))
            Text(
                text = stringResource(request.status.humanLabelRes()),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (isPending) {
                Spacer(Modifier.height(AppSpacing.Medium))
                Button(
                    onClick = onAccept,
                    enabled = actionsEnabled,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ViniciusAction),
                ) { Text(stringResource(R.string.vinicius_accept)) }
                Spacer(Modifier.height(AppSpacing.Small))
                OutlinedButton(
                    onClick = onDecline,
                    enabled = actionsEnabled,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ViniciusAction),
                ) { Text(stringResource(R.string.vinicius_decline)) }
            }
        }
    }
}

@StringRes
private fun CareRequestType.labelRes(): Int = when (this) {
    CareRequestType.DENGO -> R.string.request_dengo
    CareRequestType.HOT_WATER_BAG -> R.string.request_hot_water_bag
    CareRequestType.MEDICINE -> R.string.request_medicine
    CareRequestType.SPEND_TIME_TOGETHER -> R.string.request_spend_time
    CareRequestType.OTHER -> R.string.request_other
}

@StringRes
private fun CareRequestStatus.humanLabelRes(): Int = when (this) {
    CareRequestStatus.PENDING -> R.string.vinicius_request_waiting
    CareRequestStatus.ACCEPTED -> R.string.vinicius_request_accepted
    CareRequestStatus.DECLINED -> R.string.vinicius_request_declined
    CareRequestStatus.ACKNOWLEDGED -> R.string.vinicius_request_acknowledged
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
