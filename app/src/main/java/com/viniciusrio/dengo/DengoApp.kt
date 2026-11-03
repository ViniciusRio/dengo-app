package com.viniciusrio.dengo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.unit.dp
import com.viniciusrio.dengo.ui.ReadNotice
import com.viniciusrio.dengo.ui.ReadPresentation
import com.viniciusrio.dengo.ui.ScreenReadState
import com.viniciusrio.dengo.ui.SubmissionState
import com.viniciusrio.dengo.ui.present
import com.viniciusrio.dengo.navigation.AppDestination
import com.viniciusrio.dengo.model.PartnerId
import com.viniciusrio.dengo.ui.components.AppScaffold
import com.viniciusrio.dengo.ui.components.ReadNoticeMessage
import com.viniciusrio.dengo.ui.components.WriteNoticeMessage
import com.viniciusrio.dengo.ui.home.HomeViewModel
import com.viniciusrio.dengo.ui.home.LidianneHomeScreen
import com.viniciusrio.dengo.ui.home.ViniciusHomeScreen
import com.viniciusrio.dengo.ui.home.ViniciusHomeViewModel
import com.viniciusrio.dengo.ui.history.HistoryScreen
import com.viniciusrio.dengo.ui.history.HistoryViewModel
import com.viniciusrio.dengo.ui.mural.MuralScreen
import com.viniciusrio.dengo.ui.mural.MuralViewModel
import com.viniciusrio.dengo.ui.mural.ComposerMode
import com.viniciusrio.dengo.ui.screens.PlaceholderScreen
import com.viniciusrio.dengo.ui.screens.ProfileScreen
import com.viniciusrio.dengo.ui.theme.DengoTheme
import com.viniciusrio.dengo.ui.theme.ViniciusAction
import com.viniciusrio.dengo.ui.theme.ViniciusBlueSoft

@Composable
fun DengoApp(
    person: PartnerId,
    homeViewModel: HomeViewModel,
    viniciusHomeViewModel: ViniciusHomeViewModel,
    historyViewModel: HistoryViewModel,
    muralViewModel: MuralViewModel,
) {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestination.Home) }
    var pendingDestination by rememberSaveable { mutableStateOf<AppDestination?>(null) }
    val composer by muralViewModel.composer.collectAsState()
    val otherSubmission by homeViewModel.otherSubmission.collectAsState()
    val lidianneQuickSubmission by homeViewModel.quickSubmission.collectAsState()
    val viniciusQuickSubmission by viniciusHomeViewModel.quickSubmission.collectAsState()
    val otherOpen by homeViewModel.otherOpen.collectAsState()
    val otherDraft by homeViewModel.otherDraft.collectAsState()
    val textSubmission by muralViewModel.textSubmission.collectAsState()
    val homeState by homeViewModel.state.collectAsState()
    val viniciusState by viniciusHomeViewModel.state.collectAsState()
    val muralState by muralViewModel.state.collectAsState()
    val historyState by historyViewModel.state.collectAsState()
    val homeRead = rememberReadPresentation(homeState)
    val viniciusRead = rememberReadPresentation(viniciusState)
    val muralRead = rememberReadPresentation(muralState)
    val historyRead = rememberReadPresentation(historyState)

    DengoTheme {
        AppScaffold(
            currentDestination = currentDestination,
            onDestinationSelected = { destination ->
                if (currentDestination == AppDestination.Mural && composer.mode != ComposerMode.CLOSED) {
                    if (muralViewModel.requestExit()) currentDestination = destination
                    else if (muralViewModel.composer.value.confirmDiscard) pendingDestination = destination
                } else currentDestination = destination
            },
            selectedColor = if (person == PartnerId.VINICIUS) ViniciusAction else MaterialTheme.colorScheme.primary,
            indicatorColor = if (person == PartnerId.VINICIUS) ViniciusBlueSoft else MaterialTheme.colorScheme.primaryContainer,
        ) { padding ->
            if (currentDestination == AppDestination.Home && person == PartnerId.LIDIANNE) {
                ReadContent(homeRead, homeViewModel::retryRead, showTopNotice = !otherOpen, writeSubmission = lidianneQuickSubmission) { ready, notice -> LidianneHomeScreen(
                    state = ready,
                    readNotice = notice,
                    onRetryRead = homeViewModel::retryRead,
                    otherSubmission = otherSubmission,
                    otherOpen = otherOpen,
                    otherDraft = otherDraft,
                    onOpenOther = homeViewModel::openOtherRequest,
                    onDismissOther = homeViewModel::dismissOtherRequest,
                    onOtherDraftChanged = homeViewModel::updateOtherDraft,
                    onQuickRequest = homeViewModel::createQuickRequest,
                    onOtherRequest = homeViewModel::createOtherRequest,
                    onAcknowledgeRequest = homeViewModel::acknowledgeRequest,
                    onMoodSelected = homeViewModel::setMood,
                    onPersonalSpaceActivated = homeViewModel::activatePersonalSpace,
                    onPersonalSpaceEnded = homeViewModel::endPersonalSpace,
                    onOpenMural = { currentDestination = AppDestination.Mural },
                    contentPadding = padding,
                ) }
            } else if (currentDestination == AppDestination.Home) {
                ReadContent(viniciusRead, viniciusHomeViewModel::retryRead, writeSubmission = viniciusQuickSubmission) { ready, _ -> ViniciusHomeScreen(
                    state = ready,
                    onAcceptRequest = viniciusHomeViewModel::acceptRequest,
                    onDeclineRequest = viniciusHomeViewModel::declineRequest,
                    onOpenMural = { currentDestination = AppDestination.Mural },
                    contentPadding = padding,
                ) }
            } else if (currentDestination == AppDestination.Mural) {
                ReadContent(muralRead, muralViewModel::retryRead, showTopNotice = composer.mode == ComposerMode.CLOSED) { ready, notice -> MuralScreen(
                    state = ready,
                    readNotice = notice,
                    onRetryRead = muralViewModel::retryRead,
                    composer = composer,
                    textSubmission = textSubmission,
                    authorId = person,
                    onLeaveNote = muralViewModel::leaveNote,
                    onTextDraftChanged = muralViewModel::updateTextDraft,
                    onOpenChooser = muralViewModel::openChooser,
                    onOpenText = muralViewModel::openText,
                    onOpenDrawing = muralViewModel::openDrawing,
                    onSelectColor = muralViewModel::selectColor,
                    onStroke = muralViewModel::addStroke,
                    onUndo = muralViewModel::undoStroke,
                    onClear = muralViewModel::clearDrawing,
                    onPublishDrawing = muralViewModel::publishDrawing,
                    onRequestExit = muralViewModel::requestExit,
                    onContinueDrawing = {
                        muralViewModel.continueDrawing()
                        pendingDestination = null
                    },
                    onDiscard = {
                        muralViewModel.discardDraft()
                        pendingDestination?.let { currentDestination = it }
                        pendingDestination = null
                    },
                    contentPadding = padding,
                ) }
            } else if (currentDestination == AppDestination.History) {
                ReadContent(historyRead, historyViewModel::retryRead) { ready, _ ->
                    HistoryScreen(state = ready, person = person, contentPadding = padding)
                }
            } else if (currentDestination == AppDestination.Profile) {
                ProfileScreen(person = person, contentPadding = padding)
            } else {
                PlaceholderScreen(currentDestination, padding)
            }
        }
    }
}

@Composable
private fun <T> rememberReadPresentation(state: ScreenReadState<T>): ReadPresentation<T> {
    var lastReady by remember { mutableStateOf<T?>(null) }
    SideEffect { if (state is ScreenReadState.Ready) lastReady = state.value }
    return state.present(lastReady)
}

@Composable
private fun <T> ReadContent(
    presentation: ReadPresentation<T>,
    retry: () -> Unit,
    showTopNotice: Boolean = true,
    writeSubmission: SubmissionState = SubmissionState.Idle,
    content: @Composable (T, ReadNotice?) -> Unit,
) {
    val value = presentation.value
    if (value != null) {
        Box(Modifier.fillMaxSize()) {
            content(value, presentation.notice)
            if (showTopNotice) Column(Modifier.align(Alignment.TopCenter).padding(8.dp)) {
                presentation.notice?.let { notice -> ReadNoticeMessage(notice, retry) }
                WriteNoticeMessage(writeSubmission)
            }
        }
    } else {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            WriteNoticeMessage(writeSubmission, Modifier.align(Alignment.TopCenter).padding(8.dp))
            if (presentation.notice == ReadNotice.Error) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.read_failed))
                    Button(onClick = retry) { Text(stringResource(R.string.read_retry)) }
                }
            } else Text(stringResource(R.string.read_loading))
        }
    }
}
