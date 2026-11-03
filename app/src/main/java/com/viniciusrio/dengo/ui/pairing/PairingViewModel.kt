package com.viniciusrio.dengo.ui.pairing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viniciusrio.dengo.data.session.DeviceSession
import com.viniciusrio.dengo.data.session.HttpPairingClient
import com.viniciusrio.dengo.data.session.PairingResult
import com.viniciusrio.dengo.data.session.SessionStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PairingStatus { Idle, Sending, InvalidCode, Locked, NoConnection, Unavailable, StorageFailed }

data class PairingUiState(val code: String = "", val status: PairingStatus = PairingStatus.Idle)

class PairingViewModel(
    private val client: HttpPairingClient,
    private val store: SessionStore,
) : ViewModel() {
    private val mutableSession = MutableStateFlow(store.load())
    val session: StateFlow<DeviceSession?> = mutableSession.asStateFlow()

    private val mutableState = MutableStateFlow(PairingUiState())
    val state: StateFlow<PairingUiState> = mutableState.asStateFlow()

    fun updateCode(code: String) {
        mutableState.update { current ->
            if (current.status == PairingStatus.Sending) current else PairingUiState(code = code)
        }
    }

    fun submit() {
        val current = mutableState.value
        val code = current.code.trim().uppercase()
        if (code.isEmpty() || current.status == PairingStatus.Sending) return
        mutableState.value = current.copy(status = PairingStatus.Sending)
        viewModelScope.launch {
            val status = when (val result = client.pair(code)) {
                is PairingResult.Paired -> store(result.session)
                PairingResult.InvalidCode -> PairingStatus.InvalidCode
                PairingResult.Locked -> PairingStatus.Locked
                PairingResult.NoConnection -> PairingStatus.NoConnection
                PairingResult.Unavailable -> PairingStatus.Unavailable
            }
            mutableState.update { it.copy(status = status) }
        }
    }

    private fun store(session: DeviceSession): PairingStatus = try {
        store.save(session)
        mutableSession.value = session
        PairingStatus.Idle
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (_: Exception) {
        PairingStatus.StorageFailed
    }
}
