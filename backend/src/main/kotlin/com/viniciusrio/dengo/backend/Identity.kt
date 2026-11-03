package com.viniciusrio.dengo.backend

import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime
import java.util.Base64
import java.util.HexFormat
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

data class Session(val id: Long, val person: Person)

class SessionRefused(val code: String) : RuntimeException(code)

data class IssuedPairingCode(val code: String, val expiresAt: Instant)

sealed interface PairingOutcome {
    data class Paired(val person: Person, val token: String) : PairingOutcome
    data object InvalidCode : PairingOutcome
    data class Locked(val until: Instant) : PairingOutcome
}

private val CODE_VALIDITY: Duration = Duration.ofMinutes(15)
private val LOCK_DURATION: Duration = Duration.ofMinutes(15)
private const val MAX_INVALID_ATTEMPTS = 5
private const val CODE_LENGTH = 8

// Sem 0, O, 1, I e L, que se confundem ao digitar.
private const val CODE_ALPHABET = "23456789ABCDEFGHJKMNPQRSTUVWXYZ"

internal fun sha256(value: String): String =
    HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.toByteArray()))

/** Vínculo, sessões e revogação. Só hashes de código e de token chegam ao banco. */
@Service
class Identity(private val jdbc: JdbcClient, private val clock: Clock) {
    private val random = SecureRandom()

    fun sessionForToken(token: String): Session? =
        jdbc.sql("SELECT id, person FROM device_session WHERE token_hash = :hash AND revoked_at IS NULL")
            .param("hash", sha256(token))
            .query { rs, _ -> Session(rs.getLong("id"), Person.valueOf(rs.getString("person"))) }
            .optional()
            .orElse(null)

    /**
     * Dentro da transação de uma escrita: segura a sessão até o commit. Uma revogação em andamento
     * termina primeiro e recusa esta escrita; uma que chegue depois espera a escrita terminar.
     */
    fun holdActiveSession(session: Session) {
        val active = jdbc.sql("SELECT 1 FROM device_session WHERE id = :id AND revoked_at IS NULL FOR SHARE")
            .param("id", session.id).query(Int::class.java).optional().isPresent
        if (!active) throw SessionRefused("SESSION_REVOKED")
    }

    @Transactional
    fun issuePairingCode(person: Person): IssuedPairingCode {
        val now = clock.now()
        // Um código por pessoa: gerar outro invalida o anterior ainda não usado.
        jdbc.sql("UPDATE pairing_code SET invalidated_at = :now WHERE person = :person AND used_at IS NULL AND invalidated_at IS NULL")
            .param("now", now.toDb()).param("person", person.name).update()
        val code = (1..CODE_LENGTH).map { CODE_ALPHABET[random.nextInt(CODE_ALPHABET.length)] }.joinToString("")
        val expiresAt = now.plus(CODE_VALIDITY)
        jdbc.sql("INSERT INTO pairing_code (person, code_hash, expires_at) VALUES (:person, :hash, :expires)")
            .param("person", person.name).param("hash", sha256(code)).param("expires", expiresAt.toDb()).update()
        return IssuedPairingCode(code, expiresAt)
    }

    /** Toda tentativa passa pelo bloqueio da linha de limite, inclusive as que falham. */
    @Transactional
    fun pair(rawCode: String): PairingOutcome {
        val now = clock.now()
        val throttle = jdbc.sql("SELECT failed_attempts, locked_until FROM pairing_throttle WHERE id = 1 FOR UPDATE")
            .query { rs, _ -> rs.getInt(1) to rs.getObject(2, OffsetDateTime::class.java)?.toInstant() }
            .single()
        val lockedUntil = throttle.second
        if (lockedUntil != null && now.isBefore(lockedUntil)) return PairingOutcome.Locked(lockedUntil)
        val failedAttempts = if (lockedUntil != null) 0 else throttle.first

        val person = jdbc.sql("""
            UPDATE pairing_code SET used_at = :now
            WHERE code_hash = :hash AND used_at IS NULL AND invalidated_at IS NULL AND expires_at > :now
            RETURNING person
        """).param("now", now.toDb()).param("hash", sha256(rawCode.trim().uppercase()))
            .query(String::class.java).optional().map(Person::valueOf).orElse(null)

        if (person == null) {
            val attempts = failedAttempts + 1
            val locks = attempts >= MAX_INVALID_ATTEMPTS
            jdbc.sql("UPDATE pairing_throttle SET failed_attempts = :attempts, locked_until = :until WHERE id = 1")
                .param("attempts", if (locks) 0 else attempts)
                .param("until", if (locks) now.plus(LOCK_DURATION).toDb() else null, java.sql.Types.TIMESTAMP_WITH_TIMEZONE)
                .update()
            return PairingOutcome.InvalidCode
        }

        jdbc.sql("UPDATE pairing_throttle SET failed_attempts = 0, locked_until = NULL WHERE id = 1").update()
        revokeActiveSession(person, now)
        val token = Base64.getUrlEncoder().withoutPadding().encodeToString(ByteArray(32).also(random::nextBytes))
        jdbc.sql("INSERT INTO device_session (person, token_hash, created_at) VALUES (:person, :hash, :now)")
            .param("person", person.name).param("hash", sha256(token)).param("now", now.toDb()).update()
        return PairingOutcome.Paired(person, token)
    }

    @Transactional
    fun revoke(person: Person) = revokeActiveSession(person, clock.now())

    /** Parte do procedimento de restauração de backup: nada anterior a ela volta a autenticar. */
    @Transactional
    fun invalidateAll() {
        val now = clock.now().toDb()
        jdbc.sql("UPDATE device_session SET revoked_at = :now WHERE revoked_at IS NULL").param("now", now).update()
        jdbc.sql("UPDATE pairing_code SET invalidated_at = :now WHERE invalidated_at IS NULL").param("now", now).update()
    }

    private fun revokeActiveSession(person: Person, now: Instant) {
        jdbc.sql("UPDATE device_session SET revoked_at = :now WHERE person = :person AND revoked_at IS NULL")
            .param("now", now.toDb()).param("person", person.name).update()
    }
}
