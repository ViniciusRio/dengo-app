package com.lidiannevinicius.dengo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.lidiannevinicius.dengo.navigation.AppDestination
import com.lidiannevinicius.dengo.ui.components.AppScaffold
import com.lidiannevinicius.dengo.ui.screens.PlaceholderScreen
import com.lidiannevinicius.dengo.ui.theme.DengoTheme

@Composable
fun DengoApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestination.Home) }

    DengoTheme {
        AppScaffold(
            currentDestination = currentDestination,
            onDestinationSelected = { currentDestination = it },
        ) { padding ->
            PlaceholderScreen(currentDestination, padding)
        }
    }
}
