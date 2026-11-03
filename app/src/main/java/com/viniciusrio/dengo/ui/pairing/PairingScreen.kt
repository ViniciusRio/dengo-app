package com.viniciusrio.dengo.ui.pairing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.viniciusrio.dengo.R
import com.viniciusrio.dengo.ui.theme.AppBackground
import com.viniciusrio.dengo.ui.theme.AppSpacing

@Composable
fun PairingScreen(state: PairingUiState, onCodeChanged: (String) -> Unit, onSubmit: () -> Unit) {
    val sending = state.status == PairingStatus.Sending
    val canSubmit = !sending && state.code.isNotBlank()
    val error = when (state.status) {
        PairingStatus.InvalidCode -> R.string.pairing_error_invalid
        PairingStatus.Locked -> R.string.pairing_error_locked
        PairingStatus.NoConnection -> R.string.pairing_error_no_connection
        PairingStatus.Unavailable -> R.string.pairing_error_unavailable
        PairingStatus.StorageFailed -> R.string.pairing_error_storage
        PairingStatus.Idle, PairingStatus.Sending -> null
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(AppSpacing.Large),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.Base),
    ) {
        Text(
            text = stringResource(R.string.pairing_title),
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = stringResource(R.string.pairing_explanation),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(
            value = state.code,
            onValueChange = onCodeChanged,
            enabled = !sending,
            isError = error != null,
            singleLine = true,
            label = { Text(stringResource(R.string.pairing_code_label)) },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                autoCorrectEnabled = false,
                keyboardType = KeyboardType.Ascii,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { if (canSubmit) onSubmit() }),
            modifier = Modifier.fillMaxWidth(),
        )
        if (error != null) {
            Text(
                text = stringResource(error),
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Button(
            onClick = onSubmit,
            enabled = canSubmit,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        ) {
            Text(stringResource(if (sending) R.string.pairing_sending else R.string.pairing_submit))
        }
    }
}
