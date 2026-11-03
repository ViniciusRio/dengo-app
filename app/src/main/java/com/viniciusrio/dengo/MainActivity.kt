package com.viniciusrio.dengo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.viniciusrio.dengo.data.CoupleRepository
import com.viniciusrio.dengo.ui.home.HomeViewModel
import com.viniciusrio.dengo.ui.home.ViniciusHomeViewModel
import com.viniciusrio.dengo.ui.history.HistoryViewModel
import com.viniciusrio.dengo.ui.mural.MuralViewModel
import com.viniciusrio.dengo.ui.pairing.PairingViewModel

class MainActivity : ComponentActivity() {
    private val prototypeState by viewModels<PrototypeStateViewModel>()

    private fun sessionRepository(): CoupleRepository =
        prototypeState.repositoryFor(checkNotNull(pairingViewModel.session.value).person)

    private val homeFactory by lazy {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = when (modelClass) {
                PairingViewModel::class.java -> prototypeState.newPairingViewModel() as T
                HomeViewModel::class.java -> HomeViewModel(sessionRepository()) as T
                ViniciusHomeViewModel::class.java -> ViniciusHomeViewModel(sessionRepository()) as T
                HistoryViewModel::class.java -> HistoryViewModel(sessionRepository()) as T
                MuralViewModel::class.java -> MuralViewModel(sessionRepository()) as T
                else -> throw IllegalArgumentException("Unknown ViewModel: $modelClass")
            }
        }
    }

    private val pairingViewModel by viewModels<PairingViewModel> { homeFactory }
    // Criados só depois do vínculo, ao abrir o app da pessoa.
    private val homeViewModel by viewModels<HomeViewModel> {
        homeFactory
    }
    private val viniciusHomeViewModel by viewModels<ViniciusHomeViewModel> { homeFactory }
    private val historyViewModel by viewModels<HistoryViewModel> { homeFactory }
    private val muralViewModel by viewModels<MuralViewModel> { homeFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DengoRoot(pairingViewModel) { person ->
                DengoApp(person, homeViewModel, viniciusHomeViewModel, historyViewModel, muralViewModel)
            }
        }
    }
}
