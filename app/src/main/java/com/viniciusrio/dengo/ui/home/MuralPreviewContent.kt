package com.viniciusrio.dengo.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.viniciusrio.dengo.R
import com.viniciusrio.dengo.model.MuralNote
import com.viniciusrio.dengo.model.MuralContent
import com.viniciusrio.dengo.model.PartnerId
import com.viniciusrio.dengo.ui.theme.AppSpacing
import com.viniciusrio.dengo.ui.mural.MuralDrawing
import com.viniciusrio.dengo.ui.mural.formatMuralTimestamp
import java.time.LocalDate
import java.time.ZoneId

@Composable
internal fun MuralPreviewContent(note: MuralNote?) {
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
        if (note == null) {
            Text(
                text = stringResource(R.string.home_mural_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Column {
                Text(
                    text = stringResource(if (note.authorId == PartnerId.LIDIANNE) R.string.perspective_lidianne else R.string.perspective_vinicius),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                when (val content = note.content) {
                    is MuralContent.Text -> Text(
                        text = content.value,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    is MuralContent.Drawing -> MuralDrawing(
                        drawing = content,
                        description = stringResource(
                            R.string.mural_drawing_description,
                            stringResource(if (note.authorId == PartnerId.LIDIANNE) R.string.perspective_lidianne else R.string.perspective_vinicius),
                            formatMuralTimestamp(note.createdAt, LocalDate.now(ZoneId.systemDefault()), ZoneId.systemDefault()),
                        ),
                        modifier = Modifier.fillMaxWidth().height(96.dp),
                    )
                }
            }
        }
    }
}
