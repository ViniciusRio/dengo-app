package com.viniciusrio.dengo.data.sync

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Os gatilhos que dependem do ciclo de vida (design, decisão 6): voltar ao primeiro plano, receber
 * aviso e, em primeiro plano, a rede de segurança a cada [intervalMillis] caso um aviso se perca.
 * Abrir o app, escrever e `retryRead()` já sincronizam no [RemoteCoupleRepository].
 *
 * Cada sincronização roda à parte do temporizador: ir para o segundo plano só para o temporizador,
 * sem interromper uma sincronização em andamento.
 */
class SyncTriggers(
    private val scope: CoroutineScope,
    private val sync: suspend () -> Unit,
    private val intervalMillis: Long = 30_000,
) {
    private var periodic: Job? = null

    @Synchronized fun onForeground() {
        if (periodic?.isActive == true) return
        periodic = scope.launch {
            while (true) {
                scope.launch { sync() }
                delay(intervalMillis)
            }
        }
    }

    @Synchronized fun onBackground() {
        periodic?.cancel()
        periodic = null
    }

    fun onNotice() {
        scope.launch { sync() }
    }
}
