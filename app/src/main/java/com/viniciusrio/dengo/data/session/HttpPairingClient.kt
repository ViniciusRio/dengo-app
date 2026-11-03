package com.viniciusrio.dengo.data.session

import com.viniciusrio.dengo.model.PartnerId
import java.io.IOException
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.NoRouteToHostException
import java.net.URL
import java.net.UnknownHostException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject

sealed interface PairingResult {
    data class Paired(val session: DeviceSession) : PairingResult
    data object InvalidCode : PairingResult
    data object Locked : PairingResult

    /** Nada chegou ao servidor. */
    data object NoConnection : PairingResult

    /** Não dá para saber o que houve; o código pode ter sido consumido. */
    data object Unavailable : PairingResult
}

/** Só o conhecimento do protocolo de vínculo. Sem reenvio: o código é de uso único. */
class HttpPairingClient(
    private val baseUrl: String,
    private val timeoutMillis: Int = 15_000,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    suspend fun pair(code: String): PairingResult = withContext(dispatcher) {
        try {
            exchange(code)
        } catch (_: ConnectException) {
            PairingResult.NoConnection
        } catch (_: UnknownHostException) {
            PairingResult.NoConnection
        } catch (_: NoRouteToHostException) {
            PairingResult.NoConnection
        } catch (_: IOException) {
            PairingResult.Unavailable
        }
    }

    private fun exchange(code: String): PairingResult {
        val connection = URL("$baseUrl/v1/pairings").openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "POST"
            connection.connectTimeout = timeoutMillis
            connection.readTimeout = timeoutMillis
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.connect()
            connection.outputStream.use { it.write(JSONObject().put("code", code).toString().toByteArray()) }
            val status = connection.responseCode
            val body = (if (status < 400) connection.inputStream else connection.errorStream)
                ?.use { it.readBytes().decodeToString() }.orEmpty()
            return when {
                status == 200 -> parseSession(body)?.let(PairingResult::Paired) ?: PairingResult.Unavailable
                errorCode(body) == "PAIRING_CODE_INVALID" -> PairingResult.InvalidCode
                errorCode(body) == "PAIRING_LOCKED" -> PairingResult.Locked
                else -> PairingResult.Unavailable
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun parseSession(body: String): DeviceSession? = try {
        val json = JSONObject(body)
        val person = PartnerId.entries.firstOrNull { it.name == json.optString("person") }
        val token = json.optString("token")
        if (person != null && token.isNotEmpty()) DeviceSession(person, token) else null
    } catch (_: JSONException) {
        null
    }

    private fun errorCode(body: String): String? = try {
        JSONObject(body).optString("code")
    } catch (_: JSONException) {
        null
    }
}
