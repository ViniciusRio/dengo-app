package com.viniciusrio.dengo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.viniciusrio.dengo.model.PartnerId
import com.viniciusrio.dengo.ui.pairing.PairingScreen
import com.viniciusrio.dengo.ui.pairing.PairingViewModel
import com.viniciusrio.dengo.ui.theme.DengoTheme

/** Sem vínculo, só a tela de vínculo existe; o app nasce com a pessoa da sessão. */
@Composable
fun DengoRoot(pairing: PairingViewModel, app: @Composable (PartnerId) -> Unit) {
    val session by pairing.session.collectAsState()
    val state by pairing.state.collectAsState()
    val person = session?.person
    if (person == null) {
        DengoTheme { PairingScreen(state, pairing::updateCode, pairing::submit) }
    } else {
        app(person)
    }
}
