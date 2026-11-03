package com.viniciusrio.dengo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.viniciusrio.dengo.data.CoupleRepository
import com.viniciusrio.dengo.data.local.DengoDatabase
import com.viniciusrio.dengo.data.local.RoomCoupleRepository
import com.viniciusrio.dengo.data.session.HttpPairingClient
import com.viniciusrio.dengo.data.session.KeystoreSessionStore
import com.viniciusrio.dengo.model.PartnerId
import com.viniciusrio.dengo.ui.pairing.PairingViewModel
import java.io.File

class PrototypeStateViewModel(application: Application) : AndroidViewModel(application) {
    private var database: DengoDatabase? = null
    private var repository: CoupleRepository? = null

    fun newPairingViewModel() = PairingViewModel(
        HttpPairingClient(BuildConfig.DENGO_API_URL),
        // noBackupFilesDir fica fora de backup e de transferência entre aparelhos.
        KeystoreSessionStore(File(getApplication<Application>().noBackupFilesDir, "device-session.bin"), "dengo-session-key"),
    )

    /** O banco só abre com a pessoa conhecida: sem vínculo, nenhum dado do casal é lido. */
    fun repositoryFor(person: PartnerId): CoupleRepository = repository ?: run {
        val opened = DengoDatabase.open(getApplication()).also { database = it }
        RoomCoupleRepository(person, opened, viewModelScope).also { repository = it }
    }

    override fun onCleared() {
        database?.close()
        super.onCleared()
    }
}
