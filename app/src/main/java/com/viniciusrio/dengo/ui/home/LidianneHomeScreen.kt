package com.viniciusrio.dengo.ui.home

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
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
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.viniciusrio.dengo.R
import com.viniciusrio.dengo.model.CareRequestStatus
import com.viniciusrio.dengo.model.CareRequestType
import com.viniciusrio.dengo.model.Mood
import com.viniciusrio.dengo.model.MoodOption
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
    onMoodSelected: (MoodOption) -> Unit,
    onPersonalSpaceActivated: () -> Unit,
    onPersonalSpaceEnded: () -> Unit,
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
        MuralPreview()
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
private fun QuickRequestTile(choice: RequestChoice, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
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
            Text(
                text = stringResource(choice.labelRes),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
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
private fun MuralPreview() {
    Column {
        SectionHeading(stringResource(R.string.home_mural_title))
        Spacer(Modifier.height(AppSpacing.Small))
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.Small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.EditNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = stringResource(R.string.home_mural_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
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
    CareRequestStatus.COMPLETED -> R.string.request_status_completed
    CareRequestStatus.DECLINED -> R.string.request_status_declined
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
