package com.lidiannevinicius.tamagotchi.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.lidiannevinicius.tamagotchi.R
import com.lidiannevinicius.tamagotchi.navigation.AppDestination
import com.lidiannevinicius.tamagotchi.ui.theme.AppSpacing

@Composable
fun PlaceholderScreen(destination: AppDestination, padding: PaddingValues = PaddingValues()) {
    val title = if (destination == AppDestination.Home) {
        stringResource(R.string.home_greeting)
    } else {
        stringResource(destination.labelRes)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = AppSpacing.Large, vertical = AppSpacing.ExtraLarge),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(AppSpacing.Medium))
        Text(
            text = stringResource(destination.descriptionRes),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
