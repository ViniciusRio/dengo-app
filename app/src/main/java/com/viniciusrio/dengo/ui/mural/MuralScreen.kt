package com.viniciusrio.dengo.ui.mural

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.viniciusrio.dengo.R
import com.viniciusrio.dengo.model.MURAL_NOTE_MAX_CODE_POINTS
import com.viniciusrio.dengo.model.MuralNote
import com.viniciusrio.dengo.model.PartnerId
import com.viniciusrio.dengo.model.muralCodePointCount
import com.viniciusrio.dengo.ui.theme.AppSpacing
import com.viniciusrio.dengo.ui.theme.LidianneAction
import com.viniciusrio.dengo.ui.theme.ViniciusAction
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val timeFormat = DateTimeFormatter.ofPattern("HH:mm", Locale.forLanguageTag("pt-BR"))
private val dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.forLanguageTag("pt-BR"))

internal fun formatMuralTimestamp(instant: Instant, today: LocalDate, zone: ZoneId): String {
    val local = instant.atZone(zone)
    val day = when (local.toLocalDate()) {
        today -> "Hoje"
        today.minusDays(1) -> "Ontem"
        else -> local.format(dateFormat)
    }
    return "$day, ${local.format(timeFormat)}"
}

@Composable
fun MuralScreen(
    state: MuralUiState,
    authorId: PartnerId,
    onLeaveNote: (PartnerId, String) -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
) {
    var showComposer by rememberSaveable { mutableStateOf(false) }
    val accent = if (authorId == PartnerId.LIDIANNE) LidianneAction else ViniciusAction
    val zone = ZoneId.systemDefault()
    val today = LocalDate.now(zone)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .consumeWindowInsets(contentPadding),
        contentPadding = PaddingValues(horizontal = AppSpacing.Large, vertical = AppSpacing.Base),
    ) {
        item(key = "mural_header") {
            Text(
                text = stringResource(R.string.home_mural_title),
                modifier = Modifier.semantics { heading() },
                style = MaterialTheme.typography.headlineMedium,
            )
            Spacer(Modifier.height(AppSpacing.Large))
            Button(
                onClick = { showComposer = true },
                modifier = Modifier.heightIn(min = 48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accent),
            ) {
                Icon(Icons.Outlined.EditNote, contentDescription = null)
                Text(stringResource(R.string.mural_leave_note), modifier = Modifier.padding(start = AppSpacing.Small))
            }
            Spacer(Modifier.height(AppSpacing.Large))
        }

        if (state.notes.isEmpty()) {
            item(key = "mural_empty") {
                Text(
                    text = stringResource(R.string.mural_empty),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            items(state.notes, key = MuralNote::id) { note ->
                MuralNoteRow(note, today, zone)
            }
        }
    }

    if (showComposer) {
        MuralComposer(
            accent = accent,
            onDismiss = { showComposer = false },
            onSubmit = { text ->
                onLeaveNote(authorId, text)
                showComposer = false
            },
        )
    }
}

@Composable
private fun MuralNoteRow(note: MuralNote, today: LocalDate, zone: ZoneId) {
    val author = stringResource(if (note.authorId == PartnerId.LIDIANNE) R.string.perspective_lidianne else R.string.perspective_vinicius)
    val accent = if (note.authorId == PartnerId.LIDIANNE) LidianneAction else ViniciusAction
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = AppSpacing.Base)) {
        Text(author, color = accent, style = MaterialTheme.typography.labelLarge)
        Text(
            formatMuralTimestamp(note.createdAt, today, zone),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
        )
        Spacer(Modifier.height(AppSpacing.Small))
        Text(note.text, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(AppSpacing.Base))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MuralComposer(accent: Color, onDismiss: () -> Unit, onSubmit: (String) -> Unit) {
    var draft by rememberSaveable { mutableStateOf("") }
    val count = draft.trim().muralCodePointCount()
    val tooLong = count > MURAL_NOTE_MAX_CODE_POINTS

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.Large, vertical = AppSpacing.Base),
        ) {
            Text(stringResource(R.string.mural_leave_note), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(AppSpacing.Base))
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                label = { Text(stringResource(R.string.mural_note_label)) },
                minLines = 3,
                maxLines = 5,
                isError = tooLong,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(AppSpacing.Small))
            Text(
                text = stringResource(
                    if (tooLong) R.string.mural_note_count_exceeded else R.string.mural_note_count,
                    count,
                    MURAL_NOTE_MAX_CODE_POINTS,
                ),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.labelMedium,
                color = if (tooLong) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(AppSpacing.Base))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text(stringResource(R.string.mural_cancel))
                }
                Button(
                    onClick = { onSubmit(draft) },
                    enabled = count in 1..MURAL_NOTE_MAX_CODE_POINTS,
                    modifier = Modifier.heightIn(min = 48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accent),
                ) {
                    Text(stringResource(R.string.mural_submit))
                }
            }
        }
    }
}
