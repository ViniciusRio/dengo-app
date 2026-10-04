package com.viniciusrio.dengo.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.viniciusrio.dengo.R
import com.viniciusrio.dengo.navigation.PrototypePerspective
import com.viniciusrio.dengo.ui.theme.AppOutline
import com.viniciusrio.dengo.ui.theme.AppSpacing
import com.viniciusrio.dengo.ui.theme.AppSurfaceSoft
import com.viniciusrio.dengo.ui.theme.LidianneAction
import com.viniciusrio.dengo.ui.theme.LidiannePinkSoft
import com.viniciusrio.dengo.ui.theme.ViniciusAction
import com.viniciusrio.dengo.ui.theme.ViniciusBlueSoft

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
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AppSpacing.Large, vertical = AppSpacing.Base),
    ) {
        Text(
            stringResource(R.string.nav_profile),
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(Modifier.height(AppSpacing.Base))
        CoupleIdentity()
        Spacer(Modifier.height(AppSpacing.Large))
        Text(
            stringResource(R.string.profile_perspective_heading),
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(Modifier.height(AppSpacing.Medium))
        Column(
            modifier = Modifier.selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.Small),
        ) {
            PrototypePerspective.entries.forEach { option ->
                PerspectiveChoice(
                    option = option,
                    selected = perspective == option,
                    onClick = { onPerspectiveSelected(option) },
                )
            }
        }
        Spacer(Modifier.height(AppSpacing.Base))
        Text(
            stringResource(R.string.perspective_explanation),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(AppSpacing.Base))
    }
}

@Composable
private fun CoupleIdentity() {
    val lidianne = stringResource(R.string.perspective_lidianne)
    val vinicius = stringResource(R.string.perspective_vinicius)
    val coupleName = buildAnnotatedString {
        withStyle(SpanStyle(color = LidianneAction)) { append(lidianne) }
        append(" e ")
        withStyle(SpanStyle(color = ViniciusAction)) { append(vinicius) }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(
                Brush.horizontalGradient(
                    listOf(LidiannePinkSoft, AppSurfaceSoft, ViniciusBlueSoft),
                ),
            ),
    ) {
        Image(
            painter = painterResource(R.drawable.dengo_couple_hero),
            contentDescription = stringResource(R.string.home_hero_content_description),
            modifier = Modifier.fillMaxWidth().aspectRatio(1765f / 891f),
            contentScale = ContentScale.Fit,
        )
    }
    Spacer(Modifier.height(AppSpacing.Medium))
    Text(
        text = coupleName,
        modifier = Modifier.fillMaxWidth().semantics { heading() },
        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(AppSpacing.ExtraSmall))
    Text(
        text = stringResource(R.string.profile_couple_subtitle),
        modifier = Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun PerspectiveChoice(
    option: PrototypePerspective,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val isLidianne = option == PrototypePerspective.LIDIANNE
    val name = stringResource(if (isLidianne) R.string.perspective_lidianne else R.string.perspective_vinicius)
    val accent = if (isLidianne) LidianneAction else ViniciusAction
    val soft = if (isLidianne) LidiannePinkSoft else ViniciusBlueSoft
    val avatar = if (isLidianne) R.drawable.dengo_lidianne_avatar else R.drawable.dengo_vinicius_avatar

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = if (selected) soft else soft.copy(alpha = 0.42f),
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) accent else AppOutline),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 88.dp)
                .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
                .padding(horizontal = AppSpacing.Medium, vertical = AppSpacing.Small),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.Base),
        ) {
            Image(
                painter = painterResource(avatar),
                contentDescription = null,
                modifier = Modifier.size(64.dp).clip(CircleShape),
                contentScale = ContentScale.Crop,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (selected) {
                    Spacer(Modifier.height(AppSpacing.ExtraSmall))
                    Text(
                        text = stringResource(R.string.profile_in_use),
                        style = MaterialTheme.typography.labelMedium,
                        color = accent,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}
