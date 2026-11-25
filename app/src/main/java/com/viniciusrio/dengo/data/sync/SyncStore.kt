package com.viniciusrio.dengo.data.sync

import com.viniciusrio.dengo.model.CareRequest
import com.viniciusrio.dengo.model.CareRequestType
import com.viniciusrio.dengo.model.CoupleState
import com.viniciusrio.dengo.model.HistoryEvent
import com.viniciusrio.dengo.model.Mood
import com.viniciusrio.dengo.model.MoodOption
import com.viniciusrio.dengo.model.PersonalSpace

/** Um acontecimento com o ID do servidor, para a cópia regravar sem duplicar. */
data class SyncedHistoryEvent(val id: Long, val event: HistoryEvent)

/** Uma página de `GET /v1/sync`. `revision` é o cursor seguinte, nunca a revisão de uma linha. */
data class SyncPage(
    val revision: Long,
    val hasMore: Boolean,
    val requests: List<CareRequest> = emptyList(),
    val history: List<SyncedHistoryEvent> = emptyList(),
    val mood: Mood? = null,
    val personalSpace: PersonalSpace? = null,
)

/** Um comando que a pessoa mandou publicar, guardado com a chave até ter resposta definitiva. */
sealed interface PendingCommand {
    data class CreateRequest(val type: CareRequestType, val message: String?) : PendingCommand
    data class AcceptRequest(val requestId: Long) : PendingCommand
    data class DeclineRequest(val requestId: Long) : PendingCommand
    data class AcknowledgeRequest(val requestId: Long) : PendingCommand
    data class SetMood(val option: MoodOption) : PendingCommand
    data object ActivatePersonalSpace : PendingCommand
    data object EndPersonalSpace : PendingCommand
}

/**
 * `confirmedRevision` existe quando o descarte encontrou a escrita já confirmada: o registro só sai
 * quando a cópia alcança essa revisão.
 */
data class PendingOperation(val key: String, val command: PendingCommand, val confirmedRevision: Long? = null)

data class LocalCopy(val state: CoupleState, val revision: Long, val pending: List<PendingOperation>)

/**
 * A cópia local do servidor e as tentativas pendentes. Cada método é uma transação.
 * Nenhum método apaga dados por falha: só [clear], chamado na revogação ou no vínculo de outra pessoa.
 */
interface SyncStore {
    suspend fun read(): LocalCopy

    /**
     * Grava a página e o cursor juntos. `stillActive` roda dentro da transação: se a sessão que pediu a
     * página não for mais a ativa, nada é gravado e o retorno é `false`.
     */
    suspend fun apply(page: SyncPage, stillActive: () -> Boolean = { true }): Boolean

    suspend fun savePending(operation: PendingOperation)
    suspend fun deletePending(key: String)
    suspend fun clear()
}
