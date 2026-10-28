package com.viniciusrio.dengo.ui

import com.viniciusrio.dengo.data.RepositoryReadState
import com.viniciusrio.dengo.data.WriteResult
import com.viniciusrio.dengo.model.CoupleState
import kotlinx.coroutines.CancellationException

sealed interface ScreenReadState<out T> {
    data object Loading : ScreenReadState<Nothing>
    data class Ready<T>(val value: T) : ScreenReadState<T>
    data class ReadError<T>(val cause: Throwable, val lastKnown: T?) : ScreenReadState<T>
}

enum class ReadNotice { Loading, Error }

data class ReadPresentation<T>(val value: T?, val notice: ReadNotice?)

fun <T> ScreenReadState<T>.present(lastReady: T?): ReadPresentation<T> = when (this) {
    ScreenReadState.Loading -> ReadPresentation(lastReady, ReadNotice.Loading)
    is ScreenReadState.Ready -> ReadPresentation(value, null)
    is ScreenReadState.ReadError -> ReadPresentation(lastKnown ?: lastReady, ReadNotice.Error)
}

fun <T> RepositoryReadState.project(transform: (CoupleState) -> T): ScreenReadState<T> = when (this) {
    RepositoryReadState.Loading -> ScreenReadState.Loading
    is RepositoryReadState.Ready -> ScreenReadState.Ready(transform(value))
    is RepositoryReadState.ReadError -> ScreenReadState.ReadError(cause, lastKnownState?.let(transform))
}

sealed interface SubmissionState {
    data object Idle : SubmissionState
    data object Sending : SubmissionState
    data object Confirmed : SubmissionState
    data class Reverted(val reason: String) : SubmissionState
    data class Uncertain(val cause: Throwable) : SubmissionState
}

fun WriteResult<*>.toSubmissionState(): SubmissionState = when (this) {
    is WriteResult.Confirmed -> SubmissionState.Confirmed
    is WriteResult.Reverted -> SubmissionState.Reverted(reason)
    is WriteResult.Uncertain -> SubmissionState.Uncertain(cause)
}

suspend fun submitWrite(block: suspend () -> WriteResult<*>): SubmissionState = try {
    block().toSubmissionState()
} catch (cancelled: CancellationException) {
    throw cancelled
} catch (failure: Exception) {
    SubmissionState.Uncertain(failure)
}
