package com.viniciusrio.dengo.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.ui.unit.dp
import com.viniciusrio.dengo.R
import com.viniciusrio.dengo.navigation.PrototypePerspective
import com.viniciusrio.dengo.ui.theme.AppSpacing

@Composable
fun PrototypePerspectiveScreen(
    perspective: PrototypePerspective,
    onPerspectiveSelected: (PrototypePerspective) -> Unit,
    contentPadding: PaddingValues,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .consumeWindowInsets(contentPadding)
            .padding(horizontal = AppSpacing.Large, vertical = AppSpacing.ExtraLarge),
    ) {
        Text(stringResource(R.string.nav_profile), modifier = Modifier.semantics { heading() }, style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(AppSpacing.Large))
        Text(stringResource(R.string.perspective_title), style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(AppSpacing.Small))
        Text(
            stringResource(R.string.perspective_explanation),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(AppSpacing.Base))
        Column(Modifier.selectableGroup()) {
            PrototypePerspective.entries.forEach { option ->
                Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp)
                            .selectable(
                                selected = perspective == option,
                                role = Role.RadioButton,
                                onClick = { onPerspectiveSelected(option) },
                            )
                            .padding(horizontal = AppSpacing.Base),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = perspective == option, onClick = null)
                        Text(
                            text = stringResource(if (option == PrototypePerspective.LIDIANNE) R.string.perspective_lidianne else R.string.perspective_vinicius),
                            modifier = Modifier.padding(start = AppSpacing.Small),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
                Spacer(Modifier.height(AppSpacing.Small))
            }
        }
    }
}
