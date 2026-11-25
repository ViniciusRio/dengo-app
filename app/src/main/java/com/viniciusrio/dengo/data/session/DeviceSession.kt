package com.viniciusrio.dengo.data.session

import com.viniciusrio.dengo.model.PartnerId

/** A pessoa deste aparelho, definida pelo vínculo com o servidor. */
data class DeviceSession(val person: PartnerId, val token: String) {
    override fun toString() = "DeviceSession(person=$person)" // o token nunca vai para logs
}

interface SessionStore {
    fun load(): DeviceSession?
    fun save(session: DeviceSession)
}
