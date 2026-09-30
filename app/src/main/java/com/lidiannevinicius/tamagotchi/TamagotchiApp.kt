package com.lidiannevinicius.tamagotchi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.lidiannevinicius.tamagotchi.navigation.AppDestination
import com.lidiannevinicius.tamagotchi.ui.components.AppScaffold
import com.lidiannevinicius.tamagotchi.ui.screens.PlaceholderScreen
import com.lidiannevinicius.tamagotchi.ui.theme.TamagotchiTheme

@Composable
fun TamagotchiApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestination.Home) }

    TamagotchiTheme {
        AppScaffold(
            currentDestination = currentDestination,
            onDestinationSelected = { currentDestination = it },
        ) { padding ->
            PlaceholderScreen(currentDestination, padding)
        }
    }
}
