package com.viniciusrio.dengo.ui.home

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.PeopleOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.viniciusrio.dengo.R
import com.viniciusrio.dengo.model.CareRequest
import com.viniciusrio.dengo.model.CareRequestStatus
import com.viniciusrio.dengo.model.CareRequestType
import com.viniciusrio.dengo.model.Mood
import com.viniciusrio.dengo.model.MoodOption
import com.viniciusrio.dengo.model.MuralNote
import com.viniciusrio.dengo.model.PartnerId
import com.viniciusrio.dengo.model.PersonalSpace
import com.viniciusrio.dengo.ui.theme.AppBackground
import com.viniciusrio.dengo.ui.theme.AppSpacing
import com.viniciusrio.dengo.ui.theme.DengoTheme
import com.viniciusrio.dengo.ui.theme.LidianneAction
import com.viniciusrio.dengo.ui.theme.LidiannePinkSoft
import java.time.Instant
import java.time.LocalDate

private data class RequestChoice(
    val type: CareRequestType,
    val icon: ImageVector,
    @param:StringRes val labelRes: Int,
)

private val quickRequests = listOf(
    RequestChoice(CareRequestType.HOT_WATER_BAG, Icons.Outlined.LocalFireDepartment, R.string.request_hot_water_bag),
    RequestChoice(CareRequestType.MEDICINE, Icons.Outlined.Medication, R.string.request_medicine),
    RequestChoice(CareRequestType.SPEND_TIME_TOGETHER, Icons.Outlined.PeopleOutline, R.string.request_spend_time),
    RequestChoice(CareRequestType.OTHER, Icons.Outlined.EditNote, R.string.request_other),
)

private data class MoodChoice(
    val option: MoodOption,
    val emoji: String,
    @param:StringRes val labelRes: Int,
)

private val moodChoices = listOf(
    MoodChoice(MoodOption.SAD, "😞", R.string.mood_sad),
    MoodChoice(MoodOption.UPSET, "😠", R.string.mood_upset),
    MoodChoice(MoodOption.TIRED, "😴", R.string.mood_tired),
    MoodChoice(MoodOption.OKAY, "😐", R.string.mood_okay),
    MoodChoice(MoodOption.HAPPY, "😊", R.string.mood_happy),
    MoodChoice(MoodOption.LOVING, "🥰", R.string.mood_loving),
    MoodChoice(MoodOption.NEEDY, "🥺", R.string.mood_needy),
    MoodChoice(MoodOption.VERY_HAPPY, "🤩", R.string.mood_very_happy),
)

@Composable
fun LidianneHomeScreen(
    state: LidianneHomeState,
    onQuickRequest: (CareRequestType) -> Unit,
    onOtherRequest: (String) -> Unit,
    onAcknowledgeRequest: (Long) -> Unit = {},
    onMoodSelected: (MoodOption) -> Unit,
    onPersonalSpaceActivated: () -> Unit,
    onPersonalSpaceEnded: () -> Unit,
    onOpenMural: () -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(),
) {
    var showOtherDialog by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .consumeWindowInsets(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AppSpacing.Large),
    ) {
        Spacer(Modifier.height(AppSpacing.Base))
        GreetingHeader()
        Spacer(Modifier.height(AppSpacing.Small))
        CoupleHero()
        Spacer(Modifier.height(AppSpacing.Small))
        Button(
            onClick = { onQuickRequest(CareRequestType.DENGO) },
            enabled = state.activeRequests.none { it.type == CareRequestType.DENGO },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
            shape = MaterialTheme.shapes.large,
            contentPadding = PaddingValues(AppSpacing.Base),
        ) {
            Icon(Icons.Outlined.FavoriteBorder, contentDescription = null)
            Spacer(Modifier.width(AppSpacing.Small))
            Text(stringResource(R.string.home_primary_action), style = MaterialTheme.typography.labelLarge)
        }
        state.activeRequests.firstOrNull { it.type == CareRequestType.DENGO }?.let { request ->
            ActiveRequestHint(request)
        }

        state.latestRequest?.let { request ->
            Spacer(Modifier.height(AppSpacing.Small))
            LatestRequestFeedback(
                type = request.type,
                status = request.status,
            )
            if (state.otherRespondedRequestCount > 0) {
                Spacer(Modifier.height(AppSpacing.ExtraSmall))
                Text(
                    text = stringResource(
                        if (state.otherRespondedRequestCount == 1) R.string.home_other_responded_request
                        else R.string.home_other_responded_requests,
                        state.otherRespondedRequestCount,
                    ),
                    modifier = Modifier.padding(horizontal = AppSpacing.ExtraSmall),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        val acceptedRequests = state.activeRequests.filter { it.status == CareRequestStatus.ACCEPTED }
        if (acceptedRequests.isNotEmpty()) {
            Spacer(Modifier.height(AppSpacing.Medium))
            Text(
                text = stringResource(R.string.home_accepted_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.semantics { heading() },
            )
            acceptedRequests.forEach { request ->
                Spacer(Modifier.height(AppSpacing.Small))
                AcceptedRequestRow(request, onAcknowledgeRequest)
            }
        }
        state.acknowledgedRequest?.let {
            Spacer(Modifier.height(AppSpacing.Small))
            Text(
                text = stringResource(R.string.home_acknowledged_feedback),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.height(AppSpacing.Large))
        SectionHeading(stringResource(R.string.home_requests_title))
        Spacer(Modifier.height(AppSpacing.Medium))
        quickRequests.chunked(2).forEachIndexed { index, row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.Small),
            ) {
                row.forEach { choice ->
                    QuickRequestTile(
                        choice = choice,
                        activeRequest = state.activeRequests.firstOrNull { it.type == choice.type && choice.type != CareRequestType.OTHER },
                        onClick = {
                            if (choice.type == CareRequestType.OTHER) {
                                showOtherDialog = true
                            } else {
                                onQuickRequest(choice.type)
                            }
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            if (index == 0) Spacer(Modifier.height(AppSpacing.Small))
        }

        Spacer(Modifier.height(AppSpacing.Large))
        SectionHeading(stringResource(R.string.home_mood_title))
        Spacer(Modifier.height(AppSpacing.Medium))
        MoodSelector(
            selected = state.mood?.option,
            onSelected = onMoodSelected,
        )

        Spacer(Modifier.height(AppSpacing.Large))
        PersonalSpaceSection(
            personalSpace = state.personalSpace,
            onActivate = onPersonalSpaceActivated,
            onEnd = onPersonalSpaceEnded,
        )

        Spacer(Modifier.height(AppSpacing.Large))
        MuralPreview(state.latestMuralNote, onOpenMural)
        Spacer(Modifier.height(AppSpacing.Base))
    }

    if (showOtherDialog) {
        OtherRequestDialog(
            onDismiss = { showOtherDialog = false },
            onSubmit = { text ->
                onOtherRequest(text)
                showOtherDialog = false
            },
        )
    }
}

@Composable
private fun ActiveRequestHint(request: CareRequest) {
    Text(
        text = stringResource(R.string.home_active_request, stringResource(request.type.labelRes()), stringResource(request.status.activeLabelRes())),
        modifier = Modifier.padding(top = AppSpacing.ExtraSmall, start = AppSpacing.ExtraSmall),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun AcceptedRequestRow(request: CareRequest, onAcknowledgeRequest: (Long) -> Unit) {
    val requestName = request.message ?: stringResource(request.type.labelRes())
    val requestIcon = quickRequests.firstOrNull { it.type == request.type }?.icon ?: Icons.Outlined.FavoriteBorder
    val acknowledgeDescription = stringResource(R.string.home_acknowledge_request_accessibility, requestName)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(Modifier.padding(AppSpacing.Base)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.Medium),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(
                    imageVector = requestIcon,
                    contentDescription = null,
                    tint = LidianneAction,
                    modifier = Modifier.size(24.dp),
                )
                Column(Modifier.weight(1f)) {
                    Text(requestName, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(AppSpacing.ExtraSmall))
                    Text(
                        text = stringResource(R.string.home_request_accepted),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(AppSpacing.Medium))
            FilledTonalButton(
                onClick = { onAcknowledgeRequest(request.id) },
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .semantics { contentDescription = acknowledgeDescription },
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = LidiannePinkSoft,
                    contentColor = LidianneAction,
                ),
            ) { Text(stringResource(R.string.home_acknowledge_action)) }
        }
    }
}

@Composable
private fun GreetingHeader() {
    Text(
        text = stringResource(R.string.home_greeting),
        modifier = Modifier.semantics { heading() },
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.onBackground,
    )
}

@Composable
private fun CoupleHero() {
    Image(
        painter = painterResource(R.drawable.dengo_couple_hero),
        contentDescription = stringResource(R.string.home_hero_content_description),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1765f / 891f),
        contentScale = ContentScale.Fit,
    )
}

@Composable
private fun LatestRequestFeedback(type: CareRequestType, status: CareRequestStatus) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.ExtraSmall),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.Small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.CheckCircle,
            contentDescription = null,
            tint = LidianneAction,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = stringResource(R.string.home_latest_request, stringResource(type.labelRes())),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = stringResource(status.labelRes()),
            style = MaterialTheme.typography.labelMedium,
            color = LidianneAction,
        )
    }
}

@Composable
private fun SectionHeading(title: String) {
    Text(
        text = title,
        modifier = Modifier.semantics { heading() },
        style = MaterialTheme.typography.titleLarge,
    )
}

@Composable
private fun QuickRequestTile(choice: RequestChoice, activeRequest: CareRequest?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        enabled = activeRequest == null,
        modifier = modifier.heightIn(min = 80.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier.padding(AppSpacing.Medium),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.Small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = choice.icon,
                contentDescription = null,
                tint = LidianneAction,
                modifier = Modifier.size(24.dp),
            )
            Column {
                Text(
                    text = stringResource(choice.labelRes),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                activeRequest?.let {
                    Text(
                        text = stringResource(it.status.activeLabelRes()),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun MoodSelector(selected: MoodOption?, onSelected: (MoodOption) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.Small),
    ) {
        moodChoices.forEach { choice ->
            val isSelected = selected == choice.option
            Box(
                modifier = Modifier
                    .widthIn(min = 68.dp)
                    .heightIn(min = 80.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(if (isSelected) LidiannePinkSoft else AppBackground)
                    .selectable(
                        selected = isSelected,
                        role = Role.RadioButton,
                        onClick = { onSelected(choice.option) },
                    )
                    .padding(AppSpacing.Small),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = choice.emoji,
                        fontSize = 28.sp,
                        modifier = Modifier.clearAndSetSemantics { },
                    )
                    Spacer(Modifier.height(AppSpacing.ExtraSmall))
                    Text(
                        text = stringResource(choice.labelRes),
                        style = MaterialTheme.typography.labelMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        tint = LidianneAction,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(14.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun PersonalSpaceSection(
    personalSpace: PersonalSpace,
    onActivate: () -> Unit,
    onEnd: () -> Unit,
) {
    val isActive = personalSpace.status == PersonalSpace.Status.ACTIVE
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(AppSpacing.Base),
    ) {
        Text(
            text = stringResource(R.string.home_personal_space_title),
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(Modifier.height(AppSpacing.Small))
        Text(
            text = stringResource(
                if (isActive) R.string.home_personal_space_active else R.string.home_personal_space_inactive,
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(AppSpacing.Small))
        TextButton(
            onClick = if (isActive) onEnd else onActivate,
            modifier = Modifier.heightIn(min = 48.dp),
        ) {
            Text(
                stringResource(
                    if (isActive) R.string.home_personal_space_end else R.string.home_personal_space_start,
                ),
            )
        }
    }
}

@Composable
private fun MuralPreview(note: MuralNote?, onOpenMural: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(onClickLabel = stringResource(R.string.home_mural_open), role = Role.Button, onClick = onOpenMural),
    ) {
        SectionHeading(stringResource(R.string.home_mural_title))
        Spacer(Modifier.height(AppSpacing.Small))
        MuralPreviewContent(note)
    }
}

@Composable
private fun OtherRequestDialog(onDismiss: () -> Unit, onSubmit: (String) -> Unit) {
    var text by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.request_other_dialog_title)) },
        text = {
            Column {
                Text(stringResource(R.string.request_other_dialog_description))
                Spacer(Modifier.height(AppSpacing.Medium))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text(stringResource(R.string.request_other_field_label)) },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSubmit(text.trim()) },
                enabled = text.isNotBlank(),
            ) {
                Text(stringResource(R.string.request_other_send))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.request_other_cancel))
            }
        },
    )
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
private fun CareRequestStatus.labelRes(): Int = when (this) {
    CareRequestStatus.PENDING -> R.string.request_status_pending
    CareRequestStatus.ACCEPTED -> R.string.request_status_accepted
    CareRequestStatus.ACKNOWLEDGED -> R.string.request_status_acknowledged
    CareRequestStatus.DECLINED -> R.string.request_status_declined
}

@StringRes
private fun CareRequestStatus.activeLabelRes(): Int = when (this) {
    CareRequestStatus.PENDING -> R.string.home_request_waiting
    CareRequestStatus.ACCEPTED -> R.string.home_request_accepted
    else -> error("Only active requests have active labels")
}

@Preview(name = "Home inicial", showBackground = true, widthDp = 393, heightDp = 900)
@Composable
private fun EmptyHomePreview() {
    PreviewHome(LidianneHomeState(null, null, PersonalSpace()))
}

@Preview(name = "Mood e espaço pessoal", showBackground = true, widthDp = 393, heightDp = 900)
@Composable
private fun ActiveHomePreview() {
    PreviewHome(
        LidianneHomeState(
            latestRequest = null,
            mood = Mood(PartnerId.LIDIANNE, MoodOption.NEEDY, LocalDate.of(2026, 9, 30)),
            personalSpace = PersonalSpace(PersonalSpace.Status.ACTIVE, Instant.parse("2026-09-30T12:00:00Z")),
        ),
    )
}

@Composable
private fun PreviewHome(state: LidianneHomeState) {
    DengoTheme {
        LidianneHomeScreen(
            state = state,
            onQuickRequest = {},
            onOtherRequest = {},
            onMoodSelected = {},
            onPersonalSpaceActivated = {},
            onPersonalSpaceEnded = {},
        )
    }
}
