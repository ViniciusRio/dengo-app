package com.viniciusrio.dengo.data

import com.viniciusrio.dengo.model.CareRequest
import com.viniciusrio.dengo.model.CareRequestType
import com.viniciusrio.dengo.model.CoupleState
import com.viniciusrio.dengo.model.DrawingStroke
import com.viniciusrio.dengo.model.MoodOption
import com.viniciusrio.dengo.model.MuralNote
import kotlinx.coroutines.flow.StateFlow

sealed interface RepositoryReadState {
    data object Loading : RepositoryReadState
    data class Ready(val value: CoupleState) : RepositoryReadState
    data class ReadError(val cause: Throwable, val lastKnownState: CoupleState? = null) : RepositoryReadState
}

sealed interface WriteResult<out T> {
    data class Confirmed<T>(val value: T, val changed: Boolean) : WriteResult<T>
    data class Reverted(val reason: String) : WriteResult<Nothing>
    data class Uncertain(val cause: Throwable) : WriteResult<Nothing>
}

/** Quem age vem da sessão, definida na construção da implementação; nenhum comando informa autor. */
interface CoupleRepository {
    val state: StateFlow<RepositoryReadState>
    fun retryRead()

    suspend fun createRequest(type: CareRequestType, message: String? = null): WriteResult<CareRequest>
    suspend fun acceptRequest(requestId: Long): WriteResult<Unit>
    suspend fun declineRequest(requestId: Long): WriteResult<Unit>
    suspend fun acknowledgeRequest(requestId: Long): WriteResult<Unit>
    suspend fun setMood(option: MoodOption): WriteResult<Unit>
    suspend fun activatePersonalSpace(): WriteResult<Unit>
    suspend fun endPersonalSpace(): WriteResult<Unit>
    suspend fun createMuralNote(text: String): WriteResult<MuralNote>
    suspend fun createMuralDrawing(aspectRatio: Float, strokes: List<DrawingStroke>): WriteResult<MuralNote>
}
