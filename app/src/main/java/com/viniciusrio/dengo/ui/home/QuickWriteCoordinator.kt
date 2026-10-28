package com.viniciusrio.dengo.ui.home

import com.viniciusrio.dengo.data.WriteResult
import com.viniciusrio.dengo.ui.SubmissionState
import com.viniciusrio.dengo.ui.submitWrite
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class QuickWriteCoordinator(private val scope: CoroutineScope) {
    private val mutableSubmission = MutableStateFlow<SubmissionState>(SubmissionState.Idle)
    val submission: StateFlow<SubmissionState> = mutableSubmission.asStateFlow()
    private val pendingIndependent = mutableSetOf<Any>()
    private data class OrderedGroup(val tail: Job, val lastKey: Any)
    private val orderedGroups = mutableMapOf<Any, OrderedGroup>()
    private var latestIntent = 0L

    fun independent(key: Any, write: suspend () -> WriteResult<*>) {
        if (!pendingIndependent.add(key)) return
        launchWrite(write, onFinished = { pendingIndependent.remove(key) })
    }

    fun ordered(group: Any, key: Any, write: suspend () -> WriteResult<*>) {
        val previous = orderedGroups[group]
        if (previous?.tail?.isActive == true && previous.lastKey == key) return
        orderedGroups[group] = OrderedGroup(
            tail = launchWrite(write, previous?.tail),
            lastKey = key,
        )
    }

    private fun launchWrite(
        write: suspend () -> WriteResult<*>,
        previous: Job? = null,
        onFinished: () -> Unit = {},
    ): Job {
        val intent = ++latestIntent
        mutableSubmission.value = SubmissionState.Sending
        return scope.launch {
            try {
                previous?.join()
                val result = submitWrite(write)
                // A conclusão de uma ação antiga não substitui o aviso da intenção mais recente.
                if (intent == latestIntent) mutableSubmission.value = result
            } catch (cancelled: CancellationException) {
                if (intent == latestIntent) mutableSubmission.value = SubmissionState.Uncertain(cancelled)
                throw cancelled
            } finally {
                onFinished()
            }
        }
    }
}
