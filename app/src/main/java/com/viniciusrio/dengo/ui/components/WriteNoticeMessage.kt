package com.viniciusrio.dengo.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import com.viniciusrio.dengo.R
import com.viniciusrio.dengo.ui.SubmissionState
import com.viniciusrio.dengo.ui.theme.AppSpacing

@Composable
fun WriteNoticeMessage(submission: SubmissionState, modifier: Modifier = Modifier) {
    val message = when (submission) {
        SubmissionState.Sending -> R.string.quick_write_sending
        is SubmissionState.Reverted -> R.string.quick_write_reverted
        is SubmissionState.Uncertain -> R.string.quick_write_uncertain
        else -> return
    }
    Surface(
        modifier = modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = MaterialTheme.shapes.medium,
    ) {
        Text(
            text = stringResource(message),
            modifier = Modifier.padding(AppSpacing.Base),
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
