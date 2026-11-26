package com.viniciusrio.dengo.data.sync

import com.viniciusrio.dengo.data.CoupleRepository
import com.viniciusrio.dengo.data.RepositoryReadState
import com.viniciusrio.dengo.data.WriteResult
import com.viniciusrio.dengo.model.CareRequest
import com.viniciusrio.dengo.model.CareRequestType
import com.viniciusrio.dengo.model.DrawingStroke
import com.viniciusrio.dengo.model.MoodOption
import com.viniciusrio.dengo.model.MuralNote
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Motivos de `Reverted` que a interface distingue. */
object RevertReason {
    const val NO_CONNECTION = "NO_CONNECTION"
    const val SESSION_REVOKED = "SESSION_REVOKED"
    const val LOCAL_STORAGE = "LOCAL_STORAGE_UNAVAILABLE"

    /** O servidor só aceita recados na etapa 6 (decisão de Vinícius em 2026-11-25). */
    const val MURAL_NOT_SYNCED = "MURAL_NOT_SYNCED_YET"
}

enum class DiscardResult {
    /** Nada foi nem será confirmado com aquela chave. */
    DISCARDED,

    /** A escrita já estava confirmada; ela aparece na cópia com a sincronização. */
    ALREADY_CONFIRMED,

    /** Sem resposta definitiva; a tentativa continua pendente. */
    STILL_PENDING,
}

/**
 * O servidor é a fonte da verdade, e o [SyncStore] é a cópia. Cada escrita leva uma chave gravada
 * antes do envio; só um resultado definitivo apaga o registro. Nenhuma escrita é reenviada sozinha.
 */
class RemoteCoupleRepository(
    private val api: HttpCoupleApi,
    private val store: SyncStore,
    private val scope: CoroutineScope,
    private val onRevoked: suspend () -> Unit = {},
    private val newKey: () -> String = { UUID.randomUUID().toString() },
) : CoupleRepository {
    private val mutableState = MutableStateFlow<RepositoryReadState>(RepositoryReadState.Loading)
    override val state: StateFlow<RepositoryReadState> = mutableState.asStateFlow()

    private val mutableUncertain = MutableStateFlow<List<PendingOperation>>(emptyList())

    /** Tentativas com resultado incerto, que esperam **Tentar de novo** ou **Descartar**. */
    val uncertainOperations: StateFlow<List<PendingOperation>> = mutableUncertain.asStateFlow()

    // Escritas e descartes em ordem: um comando igual a um pendente espera o descarte terminar.
    private val writes = Mutex()
    private val reads = Mutex()

    @Volatile private var revoked = false

    init {
        scope.launch {
            publishCopy()
            sync()
        }
    }

    override fun retryRead() {
        scope.launch {
            if (mutableState.value !is RepositoryReadState.Ready) publishCopy()
            sync()
        }
    }

    /** Baixa todas as páginas desde o cursor. Devolve `false` se não alcançou o servidor. */
    suspend fun sync(): Boolean = reads.withLock {
        if (revoked) return@withLock false
        try {
            var after = store.read().revision
            while (true) {
                val page = when (val outcome = api.sync(after)) {
                    is ApiOutcome.Success -> outcome.value
                    ApiOutcome.Revoked -> {
                        revoke()
                        return@withLock false
                    }
                    else -> return@withLock false
                }
                if (!store.apply(page) { !revoked }) return@withLock false
                after = page.revision
                if (!page.hasMore) break
                publishCopy()
            }
            store.read().let { copy ->
                copy.pending.filter { it.confirmedRevision != null && it.confirmedRevision <= copy.revision }
                    .forEach { store.deletePending(it.key) }
            }
            true
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            false
        } finally {
            publishCopy()
        }
    }

    override suspend fun createRequest(type: CareRequestType, message: String?): WriteResult<CareRequest> =
        submit(PendingCommand.CreateRequest(type, message)).map { requireNotNull(it.request) }

    override suspend fun acceptRequest(requestId: Long) = submit(PendingCommand.AcceptRequest(requestId)).map { }
    override suspend fun declineRequest(requestId: Long) = submit(PendingCommand.DeclineRequest(requestId)).map { }
    override suspend fun acknowledgeRequest(requestId: Long) = submit(PendingCommand.AcknowledgeRequest(requestId)).map { }
    override suspend fun setMood(option: MoodOption) = submit(PendingCommand.SetMood(option)).map { }
    override suspend fun activatePersonalSpace() = submit(PendingCommand.ActivatePersonalSpace).map { }
    override suspend fun endPersonalSpace() = submit(PendingCommand.EndPersonalSpace).map { }

    override suspend fun createMuralNote(text: String): WriteResult<MuralNote> = WriteResult.Reverted(RevertReason.MURAL_NOT_SYNCED)

    override suspend fun createMuralDrawing(aspectRatio: Float, strokes: List<DrawingStroke>): WriteResult<MuralNote> =
        WriteResult.Reverted(RevertReason.MURAL_NOT_SYNCED)

    /** Reenvia a mesma chave com o mesmo conteúdo: o servidor devolve o resultado original, se houver. */
    suspend fun retryPending(key: String): WriteResult<Unit> = writes.withLock {
        val operation = pendingFor(key) ?: return@withLock WriteResult.Confirmed(Unit, changed = false)
        resolve(operation.key, operation.command).map { }
    }

    /** Exige conexão: só uma resposta definitiva do servidor tira a tentativa da lista. */
    suspend fun discardPending(key: String): DiscardResult = writes.withLock {
        val operation = pendingFor(key) ?: return@withLock DiscardResult.DISCARDED
        when (val outcome = api.discard(key)) {
            is ApiOutcome.Success -> when (outcome.value.outcome) {
                DiscardReply.CANCELLED, DiscardReply.REFUSED -> {
                    store.deletePending(key)
                    publishCopy()
                    DiscardResult.DISCARDED
                }
                DiscardReply.CONFIRMED -> {
                    store.savePending(operation.copy(confirmedRevision = requireNotNull(outcome.value.revision)))
                    publishCopy()
                    scope.launch { sync() }
                    DiscardResult.ALREADY_CONFIRMED
                }
            }
            ApiOutcome.Revoked -> {
                revoke()
                DiscardResult.STILL_PENDING
            }
            else -> DiscardResult.STILL_PENDING
        }
    }

    private suspend fun submit(command: PendingCommand): WriteResult<CommandReply> = writes.withLock {
        if (revoked) return@withLock WriteResult.Reverted(RevertReason.SESSION_REVOKED)
        val key = try {
            // Um comando igual ao pendente reaproveita a chave e recebe o resultado original.
            store.read().pending.firstOrNull { it.command == command }?.key
                ?: newKey().also { store.savePending(PendingOperation(it, command)) }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            return@withLock WriteResult.Reverted(RevertReason.LOCAL_STORAGE)
        }
        resolve(key, command)
    }

    private suspend fun resolve(key: String, command: PendingCommand): WriteResult<CommandReply> {
        val result = when (val outcome = api.send(key, command)) {
            is ApiOutcome.Success -> {
                store.deletePending(key)
                WriteResult.Confirmed(outcome.value, outcome.value.changed)
            }
            is ApiOutcome.Refused -> {
                store.deletePending(key)
                WriteResult.Reverted(outcome.code)
            }
            ApiOutcome.NoConnection -> {
                store.deletePending(key)
                WriteResult.Reverted(RevertReason.NO_CONNECTION)
            }
            ApiOutcome.Revoked -> {
                revoke()
                return WriteResult.Reverted(RevertReason.SESSION_REVOKED)
            }
            is ApiOutcome.Unknown -> WriteResult.Uncertain(outcome.cause)
        }
        publishCopy()
        // Confirmado sincroniza para mostrar; incerto sincroniza para conferir. Nunca reenvia.
        if (result !is WriteResult.Reverted) scope.launch { sync() }
        return result
    }

    private suspend fun pendingFor(key: String) = store.read().pending.firstOrNull { it.key == key }

    private suspend fun revoke() {
        revoked = true
        store.clear()
        mutableUncertain.value = emptyList()
        mutableState.value = RepositoryReadState.Loading
        onRevoked()
    }

    private suspend fun publishCopy() {
        if (revoked) return
        try {
            val copy = store.read()
            mutableState.value = RepositoryReadState.Ready(copy.state)
            mutableUncertain.value = copy.pending.filter { it.confirmedRevision == null }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: Exception) {
            val lastKnown = (mutableState.value as? RepositoryReadState.Ready)?.value
                ?: (mutableState.value as? RepositoryReadState.ReadError)?.lastKnownState
            mutableState.value = RepositoryReadState.ReadError(failure, lastKnown)
        }
    }
}

private inline fun <T, R> WriteResult<T>.map(transform: (T) -> R): WriteResult<R> = when (this) {
    is WriteResult.Confirmed -> WriteResult.Confirmed(transform(value), changed)
    is WriteResult.Reverted -> this
    is WriteResult.Uncertain -> this
}
