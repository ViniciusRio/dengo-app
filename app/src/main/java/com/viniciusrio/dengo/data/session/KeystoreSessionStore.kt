package com.viniciusrio.dengo.data.session

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.viniciusrio.dengo.model.PartnerId
import java.io.File
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private const val KEY_STORE = "AndroidKeyStore"
private const val TRANSFORMATION = "AES/GCM/NoPadding"
private const val IV_BYTES = 12
private const val TAG_BITS = 128

/**
 * Guarda a sessão cifrada com uma chave do Android Keystore, que não sai do aparelho.
 * Um arquivo ilegível não é apagado: conta como sem sessão, e só um novo vínculo o substitui.
 */
class KeystoreSessionStore(private val file: File, private val keyAlias: String) : SessionStore {
    override fun load(): DeviceSession? {
        if (!file.exists()) return null
        return try {
            val bytes = file.readBytes()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(TAG_BITS, bytes, 0, IV_BYTES))
            val plain = cipher.doFinal(bytes, IV_BYTES, bytes.size - IV_BYTES).decodeToString()
            val person = PartnerId.valueOf(plain.substringBefore('\n'))
            DeviceSession(person, plain.substringAfter('\n'))
        } catch (_: GeneralSecurityException) {
            null
        } catch (_: IOException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    override fun save(session: DeviceSession) {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val encrypted = cipher.doFinal("${session.person.name}\n${session.token}".toByteArray())
        val temporary = File(file.parentFile, "${file.name}.tmp")
        temporary.writeBytes(cipher.iv + encrypted)
        if (!temporary.renameTo(file)) {
            temporary.delete()
            throw IOException("Could not store the session")
        }
    }

    private fun key(): SecretKey {
        val keyStore = KeyStore.getInstance(KEY_STORE).apply { load(null) }
        (keyStore.getKey(keyAlias, null) as SecretKey?)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEY_STORE).apply {
            init(
                KeyGenParameterSpec.Builder(keyAlias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build(),
            )
        }.generateKey()
    }
}
