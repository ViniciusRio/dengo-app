package com.viniciusrio.dengo.data.sync

import com.viniciusrio.dengo.model.CareRequest
import com.viniciusrio.dengo.model.CareRequestStatus
import com.viniciusrio.dengo.model.CareRequestType
import com.viniciusrio.dengo.model.Mood
import com.viniciusrio.dengo.model.MoodOption
import com.viniciusrio.dengo.model.PartnerId
import com.viniciusrio.dengo.model.PersonalSpace
import java.io.IOException
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.UnknownHostException
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.viniciusrio.dengo.data.net.dengoHttpClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject

/** O que se sabe de uma chamada. Só [Revoked] autoriza apagar dados. */
sealed interface ApiOutcome<out T> {
    data class Success<T>(val value: T) : ApiOutcome<T>

    /** O servidor recusou pela regra (4xx), sem alterar nada. */
    data class Refused(val code: String) : ApiOutcome<Nothing>

    /** 401 com o código próprio de sessão revogada ou inválida. */
    data object Revoked : ApiOutcome<Nothing>

    /** Nada chegou ao servidor. */
    data object NoConnection : ApiOutcome<Nothing>

    /** Tempo esgotado, conexão perdida depois de enviar, 5xx ou resposta ilegível. */
    data class Unknown(val cause: Throwable) : ApiOutcome<Nothing>
}

/** Resultado de um comando confirmado. `request` só vem na criação de pedido. */
data class CommandReply(val changed: Boolean, val revision: Long, val request: CareRequest?)

enum class DiscardReply { CANCELLED, CONFIRMED, REFUSED }
data class DiscardResponse(val outcome: DiscardReply, val revision: Long?)

private val JSON = "application/json".toMediaType()
private val SESSION_CODES = setOf("SESSION_REVOKED", "AUTH_REQUIRED")

/** Só o conhecimento do protocolo HTTP do backend. Nenhuma chamada é repetida aqui. */
class HttpCoupleApi(
    private val baseUrl: String,
    private val token: String,
    private val timeoutMillis: Int = 15_000,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    suspend fun send(key: String, command: PendingCommand): ApiOutcome<CommandReply> {
        val (method, path, body) = when (command) {
            is PendingCommand.CreateRequest -> Triple("POST", "/v1/requests", JSONObject().put("type", command.type.name).putOpt("message", command.message))
            is PendingCommand.AcceptRequest -> Triple("POST", "/v1/requests/${command.requestId}/accept", JSONObject())
            is PendingCommand.DeclineRequest -> Triple("POST", "/v1/requests/${command.requestId}/decline", JSONObject())
            is PendingCommand.AcknowledgeRequest -> Triple("POST", "/v1/requests/${command.requestId}/acknowledge", JSONObject())
            is PendingCommand.SetMood -> Triple("PUT", "/v1/mood", JSONObject().put("option", command.option.name))
            PendingCommand.ActivatePersonalSpace -> Triple("POST", "/v1/personal-space", JSONObject())
            PendingCommand.EndPersonalSpace -> Triple("DELETE", "/v1/personal-space", JSONObject())
        }
        return call(method, path, key, body) { json ->
            CommandReply(json.getBoolean("changed"), json.getLong("revision"), json.optJSONObject("request")?.toCareRequest())
        }
    }

    suspend fun sync(after: Long, limit: Int = 100): ApiOutcome<SyncPage> =
        call("GET", "/v1/sync?after=$after&limit=$limit", null, null) { it.toSyncPage() }

    suspend fun discard(key: String): ApiOutcome<DiscardResponse> = call("DELETE", "/v1/operations/$key", null, null) { json ->
        DiscardResponse(DiscardReply.valueOf(json.getString("outcome")), if (json.isNull("revision")) null else json.getLong("revision"))
    }

    private val client = dengoHttpClient(timeoutMillis)

    private suspend fun <T> call(method: String, path: String, key: String?, body: JSONObject?, parse: (JSONObject) -> T): ApiOutcome<T> =
        withContext(dispatcher) {
            val request = Request.Builder()
                .url(baseUrl + path)
                .header("Authorization", "Bearer $token")
                .apply { key?.let { header("Idempotency-Key", it) } }
                .method(method, body?.toString()?.toByteArray()?.toRequestBody(JSON))
                .build()
            try {
                client.newCall(request).execute().use { response ->
                    val text = response.body?.string().orEmpty()
                    val code = errorCode(text)
                    when {
                        response.isSuccessful -> ApiOutcome.Success(parse(JSONObject(text)))
                        response.code == 401 && code in SESSION_CODES -> ApiOutcome.Revoked
                        response.code in 400..499 && response.code != 401 -> ApiOutcome.Refused(code ?: "HTTP_${response.code}")
                        else -> ApiOutcome.Unknown(IOException("HTTP ${response.code}"))
                    }
                }
            } catch (failure: IOException) {
                // Só falhas de conexão garantem que nada foi enviado.
                if (failure.isNoConnection()) ApiOutcome.NoConnection else ApiOutcome.Unknown(failure)
            } catch (failure: JSONException) {
                ApiOutcome.Unknown(failure)
            } catch (failure: IllegalArgumentException) {
                ApiOutcome.Unknown(failure)
            }
        }

    private fun IOException.isNoConnection() = this is ConnectException || this is UnknownHostException || this is NoRouteToHostException

    private fun errorCode(text: String): String? = try {
        JSONObject(text).optString("code").takeIf(String::isNotEmpty)
    } catch (_: JSONException) {
        null
    }
}

private fun JSONObject.optText(name: String): String? = if (isNull(name)) null else getString(name)

private fun JSONObject.toCareRequest() = CareRequest(
    getLong("id"),
    CareRequestType.valueOf(getString("type")),
    PartnerId.valueOf(getString("requesterId")),
    PartnerId.valueOf(getString("recipientId")),
    CareRequestStatus.valueOf(getString("status")),
    Instant.parse(getString("createdAt")),
    optText("message"),
)

private fun JSONObject.toSyncPage(): SyncPage {
    val requests = getJSONArray("requests").let { array -> (0 until array.length()).map { array.getJSONObject(it).toCareRequest() } }
    val history = getJSONArray("history").let { array ->
        (0 until array.length()).map { index ->
            val json = array.getJSONObject(index)
            val event = historyEvent(
                json.getString("type"),
                PartnerId.valueOf(json.getString("actorId")),
                Instant.parse(json.getString("occurredAt")),
                if (json.isNull("requestId")) null else json.getLong("requestId"),
                json.optText("option"),
            )
            SyncedHistoryEvent(json.getLong("id"), event)
        }
    }
    val mood = optJSONObject("mood")?.let {
        Mood(PartnerId.valueOf(it.getString("partnerId")), MoodOption.valueOf(it.getString("option")), LocalDate.parse(it.getString("date")))
    }
    val personalSpace = optJSONObject("personalSpace")?.let {
        PersonalSpace(PersonalSpace.Status.valueOf(it.getString("status")), it.optText("activatedAt")?.let(Instant::parse))
    }
    return SyncPage(getLong("revision"), getBoolean("hasMore"), requests, history, mood, personalSpace)
}
