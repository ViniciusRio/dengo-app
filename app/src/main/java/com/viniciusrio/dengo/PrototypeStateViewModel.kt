package com.viniciusrio.dengo

import androidx.lifecycle.ViewModel
import com.viniciusrio.dengo.data.FakeCoupleRepository

class PrototypeStateViewModel : ViewModel() {
    val repository = FakeCoupleRepository()
}
