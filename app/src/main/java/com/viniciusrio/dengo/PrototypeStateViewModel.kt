package com.viniciusrio.dengo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.viniciusrio.dengo.data.CoupleRepository
import com.viniciusrio.dengo.data.local.DengoDatabase
import com.viniciusrio.dengo.data.local.RoomCoupleRepository

class PrototypeStateViewModel(application: Application) : AndroidViewModel(application) {
    private val database = DengoDatabase.open(application)
    val repository: CoupleRepository = RoomCoupleRepository(database, viewModelScope)

    override fun onCleared() {
        database.close()
        super.onCleared()
    }
}
