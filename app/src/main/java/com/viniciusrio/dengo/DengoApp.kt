package com.viniciusrio.dengo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.material3.MaterialTheme
import com.viniciusrio.dengo.navigation.AppDestination
import com.viniciusrio.dengo.navigation.PrototypePerspective
import com.viniciusrio.dengo.ui.components.AppScaffold
import com.viniciusrio.dengo.ui.home.HomeViewModel
import com.viniciusrio.dengo.ui.home.LidianneHomeScreen
import com.viniciusrio.dengo.ui.home.ViniciusHomeScreen
import com.viniciusrio.dengo.ui.home.ViniciusHomeViewModel
import com.viniciusrio.dengo.ui.history.HistoryScreen
import com.viniciusrio.dengo.ui.history.HistoryViewModel
import com.viniciusrio.dengo.ui.screens.PlaceholderScreen
import com.viniciusrio.dengo.ui.screens.PrototypePerspectiveScreen
import com.viniciusrio.dengo.ui.theme.DengoTheme
import com.viniciusrio.dengo.ui.theme.ViniciusAction
import com.viniciusrio.dengo.ui.theme.ViniciusBlueSoft

@Composable
fun DengoApp(
    homeViewModel: HomeViewModel,
    viniciusHomeViewModel: ViniciusHomeViewModel,
    historyViewModel: HistoryViewModel,
) {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestination.Home) }
    var perspective by rememberSaveable { mutableStateOf(PrototypePerspective.LIDIANNE) }

    DengoTheme {
        AppScaffold(
            currentDestination = currentDestination,
            onDestinationSelected = { currentDestination = it },
            selectedColor = if (perspective == PrototypePerspective.VINICIUS) ViniciusAction else MaterialTheme.colorScheme.primary,
            indicatorColor = if (perspective == PrototypePerspective.VINICIUS) ViniciusBlueSoft else MaterialTheme.colorScheme.primaryContainer,
        ) { padding ->
            if (currentDestination == AppDestination.Home && perspective == PrototypePerspective.LIDIANNE) {
                val homeState by homeViewModel.state.collectAsState()
                LidianneHomeScreen(
                    state = homeState,
                    onQuickRequest = homeViewModel::createQuickRequest,
                    onOtherRequest = homeViewModel::createOtherRequest,
                    onMoodSelected = homeViewModel::setMood,
                    onPersonalSpaceActivated = homeViewModel::activatePersonalSpace,
                    onPersonalSpaceEnded = homeViewModel::endPersonalSpace,
                    contentPadding = padding,
                )
            } else if (currentDestination == AppDestination.Home) {
                val homeState by viniciusHomeViewModel.state.collectAsState()
                ViniciusHomeScreen(
                    state = homeState,
                    onAcceptRequest = viniciusHomeViewModel::acceptRequest,
                    onDeclineRequest = viniciusHomeViewModel::declineRequest,
                    contentPadding = padding,
                )
            } else if (currentDestination == AppDestination.History) {
                val historyState by historyViewModel.state.collectAsState()
                HistoryScreen(state = historyState, contentPadding = padding)
            } else if (currentDestination == AppDestination.Profile) {
                PrototypePerspectiveScreen(
                    perspective = perspective,
                    onPerspectiveSelected = {
                        perspective = it
                        currentDestination = AppDestination.Home
                    },
                    contentPadding = padding,
                )
            } else {
                PlaceholderScreen(currentDestination, padding)
            }
        }
    }
}
