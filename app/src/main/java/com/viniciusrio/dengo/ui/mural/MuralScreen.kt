package com.viniciusrio.dengo.ui.mural

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.viniciusrio.dengo.R
import com.viniciusrio.dengo.model.MURAL_NOTE_MAX_CODE_POINTS
import com.viniciusrio.dengo.model.MuralNote
import com.viniciusrio.dengo.model.MuralContent
import com.viniciusrio.dengo.model.DrawingPoint
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
    composer: MuralComposerState,
    authorId: PartnerId,
    onLeaveNote: (PartnerId, String) -> Unit,
    onOpenChooser: () -> Unit,
    onOpenText: () -> Unit,
    onOpenDrawing: () -> Unit,
    onSelectColor: (Int) -> Unit,
    onStroke: (List<DrawingPoint>, Float, Int) -> Unit,
    onUndo: () -> Unit,
    onClear: () -> Unit,
    onPublishDrawing: (PartnerId) -> Boolean,
    onRequestExit: () -> Boolean,
    onContinueDrawing: () -> Unit,
    onDiscard: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
) {
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
                onClick = onOpenChooser,
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

    if (composer.mode == ComposerMode.CHOOSER) {
        AlertDialog(
            onDismissRequest = { onRequestExit() },
            title = { Text(stringResource(R.string.mural_leave_note)) },
            text = {
                Column {
                    TextButton(onClick = onOpenText, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                        Text(stringResource(R.string.mural_write))
                    }
                    TextButton(onClick = onOpenDrawing, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                        Text(stringResource(R.string.mural_draw))
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { onRequestExit() }) { Text(stringResource(R.string.mural_cancel)) } },
        )
    }
    if (composer.mode == ComposerMode.TEXT) {
        MuralComposer(
            accent = accent,
            onDismiss = { onRequestExit() },
            onSubmit = { text -> onLeaveNote(authorId, text) },
        )
    }
    if (composer.mode == ComposerMode.DRAWING) {
        DrawingComposer(composer, authorId, accent, onSelectColor, onStroke, onUndo, onClear,
            onPublishDrawing, onRequestExit)
    }
    if (composer.confirmDiscard) {
        AlertDialog(
            onDismissRequest = onContinueDrawing,
            title = { Text(stringResource(R.string.mural_discard_title)) },
            text = { Text(stringResource(R.string.mural_discard_message)) },
            confirmButton = { TextButton(onClick = onDiscard) { Text(stringResource(R.string.mural_discard)) } },
            dismissButton = { TextButton(onClick = onContinueDrawing) { Text(stringResource(R.string.mural_continue)) } },
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
        when (val content = note.content) {
            is MuralContent.Text -> Text(content.value, style = MaterialTheme.typography.bodyLarge)
            is MuralContent.Drawing -> MuralDrawing(
                content,
                stringResource(R.string.mural_drawing_description, author, formatMuralTimestamp(note.createdAt, today, zone)),
                modifier = Modifier.fillMaxWidth().height(200.dp),
            )
        }
        Spacer(Modifier.height(AppSpacing.Base))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f))
    }
}

private data class InkColor(val label: Int, val argb: Int)
private val inkColors = listOf(
    InkColor(R.string.mural_color_pink, 0xFFA63854.toInt()),
    InkColor(R.string.mural_color_blue, 0xFF315F91.toInt()),
    InkColor(R.string.mural_color_dark, 0xFF292426.toInt()),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DrawingComposer(
    composer: MuralComposerState,
    authorId: PartnerId,
    accent: Color,
    onSelectColor: (Int) -> Unit,
    onStroke: (List<DrawingPoint>, Float, Int) -> Unit,
    onUndo: () -> Unit,
    onClear: () -> Unit,
    onPublish: (PartnerId) -> Boolean,
    onRequestExit: () -> Boolean,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(sheetState = sheetState, onDismissRequest = { onRequestExit() }) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.Large, vertical = AppSpacing.Base),
        ) {
            Text(stringResource(R.string.mural_draw_title), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(AppSpacing.Base))
            MuralDrawing(
                drawing = MuralContent.Drawing(composer.aspectRatio, composer.strokes),
                description = stringResource(R.string.mural_drawing_surface),
                modifier = Modifier.fillMaxWidth().aspectRatio(1.35f),
                onStroke = onStroke,
                selectedArgb = composer.selectedArgb,
            )
            Spacer(Modifier.height(AppSpacing.Base))
            Text(stringResource(R.string.mural_color_label), style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.Small)) {
                inkColors.forEach { ink ->
                    val selected = composer.selectedArgb == ink.argb
                    OutlinedButton(
                        onClick = { onSelectColor(ink.argb) },
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp).semantics { this.selected = selected },
                        contentPadding = PaddingValues(horizontal = AppSpacing.Small, vertical = AppSpacing.Small),
                    ) {
                        Box(Modifier.size(16.dp)) {
                            if (selected) Text("✓", color = Color(ink.argb))
                        }
                        Text(stringResource(ink.label), color = Color(ink.argb))
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.Small)) {
                TextButton(onClick = onUndo, enabled = composer.strokes.isNotEmpty(), modifier = Modifier.heightIn(min = 48.dp)) {
                    Text(stringResource(R.string.mural_undo))
                }
                TextButton(onClick = onClear, enabled = composer.strokes.isNotEmpty(), modifier = Modifier.heightIn(min = 48.dp)) {
                    Text(stringResource(R.string.mural_clear))
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { onRequestExit() }, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text(stringResource(R.string.mural_cancel))
                }
                Button(
                    onClick = { onPublish(authorId) },
                    enabled = composer.strokes.isNotEmpty(),
                    modifier = Modifier.heightIn(min = 48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accent),
                ) { Text(stringResource(R.string.mural_submit)) }
            }
        }
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
