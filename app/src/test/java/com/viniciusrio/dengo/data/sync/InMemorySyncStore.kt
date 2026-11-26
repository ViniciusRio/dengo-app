package com.viniciusrio.dengo.data.sync

import com.viniciusrio.dengo.model.CoupleState
import com.viniciusrio.dengo.model.Partner
import com.viniciusrio.dengo.model.PartnerId

/** A cópia em memória, com o mesmo contrato do Room: regrava pelo ID e preserva a ordem das pendentes. */
class InMemorySyncStore : SyncStore {
    private val requests = sortedMapOf<Long, com.viniciusrio.dengo.model.CareRequest>()
    private val history = sortedMapOf<Long, com.viniciusrio.dengo.model.HistoryEvent>()
    private var mood: com.viniciusrio.dengo.model.Mood? = null
    private var personalSpace = com.viniciusrio.dengo.model.PersonalSpace()
    private var revision = 0L
    private val pending = LinkedHashMap<String, PendingOperation>()
    var appliedPages = 0
        private set

    override suspend fun read() = synchronized(this) { LocalCopy(
        CoupleState(
            partners = listOf(Partner(PartnerId.LIDIANNE, "Lidianne"), Partner(PartnerId.VINICIUS, "Vinícius")),
            requests = requests.values.toList(),
            mood = mood,
            personalSpace = personalSpace,
            history = history.values.toList(),
        ),
        revision,
        pending.values.toList(),
    ) }

    override suspend fun apply(page: SyncPage, stillActive: () -> Boolean): Boolean = synchronized(this) {
        if (!stillActive()) return false
        page.requests.forEach { requests[it.id] = it }
        page.history.forEach { history[it.id] = it.event }
        page.mood?.let { mood = it }
        page.personalSpace?.let { personalSpace = it }
        revision = page.revision
        appliedPages++
        true
    }

    override suspend fun savePending(operation: PendingOperation) = synchronized(this) { pending[operation.key] = operation }
    override suspend fun deletePending(key: String) { synchronized(this) { pending.remove(key) } }

    override suspend fun clear() = synchronized(this) {
        requests.clear(); history.clear(); mood = null; personalSpace = com.viniciusrio.dengo.model.PersonalSpace()
        revision = 0; pending.clear()
    }
}
