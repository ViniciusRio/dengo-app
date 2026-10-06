package com.viniciusrio.dengo.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
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
import com.viniciusrio.dengo.ui.theme.AppBackground
import com.viniciusrio.dengo.ui.theme.AppSpacing
import com.viniciusrio.dengo.ui.theme.LidianneAction
import com.viniciusrio.dengo.ui.theme.LidiannePinkSoft
import com.viniciusrio.dengo.ui.theme.ViniciusAction
import com.viniciusrio.dengo.ui.theme.ViniciusBlueSoft

private val ProfilePanelShape = GenericShape { size, _ ->
    val edgeRise = size.width * 0.07f
    moveTo(0f, edgeRise)
    cubicTo(size.width * 0.10f, 0f, size.width * 0.30f, 0f, size.width * 0.46f, 0f)
    cubicTo(size.width * 0.71f, 0f, size.width * 0.91f, 0f, size.width, edgeRise)
    lineTo(size.width, size.height)
    lineTo(0f, size.height)
    close()
}

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
            .background(AppBackground)
            .verticalScroll(rememberScrollState()),
    ) {
        Text(
            text = stringResource(R.string.nav_profile),
            modifier = Modifier
                .padding(start = AppSpacing.Large, end = AppSpacing.Large, top = AppSpacing.Base)
                .semantics { heading() },
            style = MaterialTheme.typography.titleLarge,
        )
        ProfileComposition(perspective, onPerspectiveSelected)
    }
}

@Composable
private fun ProfileComposition(
    perspective: PrototypePerspective,
    onPerspectiveSelected: (PrototypePerspective) -> Unit,
) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        // Only the illustration uses a width-based dimension. The panel and controls grow with text.
        val heroHeight = maxWidth * 0.86f
        val showTextLettering = maxWidth < 350.dp || LocalConfiguration.current.fontScale >= 1.3f

        Canvas(Modifier.fillMaxWidth().height(heroHeight)) {
            drawOval(
                color = LidiannePinkSoft.copy(alpha = 0.76f),
                topLeft = Offset(-size.width * 0.13f, size.height * 0.11f),
                size = Size(size.width * 0.92f, size.height * 0.79f),
            )
            drawOval(
                color = ViniciusBlueSoft.copy(alpha = 0.62f),
                topLeft = Offset(size.width * 0.68f, size.height * 0.17f),
                size = Size(size.width * 0.52f, size.height * 0.66f),
            )
            drawHeart(Offset(size.width * 0.17f, size.height * 0.23f), size.width * 0.055f)
            drawHeart(Offset(size.width * 0.85f, size.height * 0.35f), size.width * 0.043f)
        }
        Image(
            painter = painterResource(R.drawable.dengo_profile_couple_cutout),
            contentDescription = stringResource(R.string.profile_couple_illustration),
            modifier = Modifier.fillMaxWidth().height(heroHeight),
            contentScale = ContentScale.Crop,
            alignment = Alignment.TopCenter,
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = heroHeight - 36.dp)
                .clip(ProfilePanelShape)
                .background(AppBackground)
                .padding(start = AppSpacing.Large, end = AppSpacing.Large, top = AppSpacing.ExtraLarge),
        ) {
            CoupleIdentity(showTextLettering)
            Spacer(Modifier.height(AppSpacing.Large))
            Text(
                text = stringResource(R.string.profile_perspective_heading),
                modifier = Modifier.semantics { heading() },
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(AppSpacing.Base))
                    .background(LidiannePinkSoft.copy(alpha = 0.36f))
                    .padding(AppSpacing.Medium),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.Small),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(R.string.perspective_explanation),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(AppSpacing.Large))
        }
    }
}

@Composable
private fun CoupleIdentity(showTextLettering: Boolean) {
    val lidianne = stringResource(R.string.perspective_lidianne)
    val vinicius = stringResource(R.string.perspective_vinicius)
    val name = "$lidianne e $vinicius"
    if (showTextLettering) {
        val coloredName = buildAnnotatedString {
            withStyle(SpanStyle(color = LidianneAction)) { append(lidianne) }
            append(" e ")
            withStyle(SpanStyle(color = ViniciusAction)) { append(vinicius) }
        }
        Text(
            text = coloredName,
            modifier = Modifier.fillMaxWidth().semantics { heading() }.testTag("profile-lettering-text"),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
            textAlign = TextAlign.Center,
        )
    } else {
        Image(
            painter = painterResource(R.drawable.dengo_profile_couple_lettering),
            contentDescription = name,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(6.1f)
                .semantics { heading() }
                .testTag("profile-lettering-image"),
            contentScale = ContentScale.Crop,
        )
    }
    Spacer(Modifier.height(AppSpacing.Small))
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
    val shape = RoundedCornerShape(28.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 88.dp)
            .clip(shape)
            .background(Brush.horizontalGradient(listOf(soft, soft.copy(alpha = 0.68f))))
            .border(if (selected) 1.5.dp else 1.dp, if (selected) accent.copy(alpha = 0.8f) else Color.White, shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = AppSpacing.Medium, vertical = AppSpacing.Small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.Medium),
    ) {
        Image(
            painter = painterResource(avatar),
            contentDescription = null,
            modifier = Modifier.size(72.dp).clip(CircleShape),
            contentScale = ContentScale.Crop,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (selected) {
                Spacer(Modifier.height(AppSpacing.ExtraSmall))
                Text(
                    text = stringResource(R.string.profile_in_use),
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.12f))
                        .padding(horizontal = AppSpacing.Medium, vertical = AppSpacing.ExtraSmall),
                    style = MaterialTheme.typography.labelMedium,
                    color = accent,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

private fun DrawScope.drawHeart(center: Offset, radius: Float) {
    val path = Path().apply {
        moveTo(center.x, center.y + radius)
        cubicTo(center.x - radius * 1.9f, center.y - radius * 0.2f, center.x - radius, center.y - radius * 1.5f, center.x, center.y - radius * 0.55f)
        cubicTo(center.x + radius, center.y - radius * 1.5f, center.x + radius * 1.9f, center.y - radius * 0.2f, center.x, center.y + radius)
    }
    drawPath(path, color = LidianneAction.copy(alpha = 0.64f), style = Stroke(width = radius * 0.16f))
}
