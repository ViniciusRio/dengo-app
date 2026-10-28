package com.viniciusrio.dengo.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.viniciusrio.dengo.R
import com.viniciusrio.dengo.ui.ReadNotice
import com.viniciusrio.dengo.ui.theme.AppSpacing

@Composable
fun ReadNoticeMessage(notice: ReadNotice, retry: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(Modifier.padding(AppSpacing.Base)) {
            Text(
                stringResource(if (notice == ReadNotice.Loading) R.string.read_stale_loading else R.string.read_stale_failed),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                style = MaterialTheme.typography.bodyMedium,
            )
            if (notice == ReadNotice.Error) {
                TextButton(onClick = retry) { Text(stringResource(R.string.read_retry)) }
            }
        }
    }
}
