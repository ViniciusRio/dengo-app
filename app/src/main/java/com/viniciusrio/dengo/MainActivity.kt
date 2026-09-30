package com.viniciusrio.dengo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.viniciusrio.dengo.ui.home.HomeViewModel
import com.viniciusrio.dengo.ui.home.ViniciusHomeViewModel

class MainActivity : ComponentActivity() {
    private val prototypeState by viewModels<PrototypeStateViewModel>()

    private val homeFactory by lazy {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = when (modelClass) {
                HomeViewModel::class.java -> HomeViewModel(prototypeState.repository) as T
                ViniciusHomeViewModel::class.java -> ViniciusHomeViewModel(prototypeState.repository) as T
                else -> throw IllegalArgumentException("Unknown ViewModel: $modelClass")
            }
        }
    }

    private val homeViewModel by viewModels<HomeViewModel> {
        homeFactory
    }
    private val viniciusHomeViewModel by viewModels<ViniciusHomeViewModel> { homeFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { DengoApp(homeViewModel, viniciusHomeViewModel) }
    }
}
