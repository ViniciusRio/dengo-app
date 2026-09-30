package com.viniciusrio.dengo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.viniciusrio.dengo.navigation.AppDestination
import com.viniciusrio.dengo.ui.components.AppScaffold
import com.viniciusrio.dengo.ui.screens.PlaceholderScreen
import com.viniciusrio.dengo.ui.theme.DengoTheme

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
