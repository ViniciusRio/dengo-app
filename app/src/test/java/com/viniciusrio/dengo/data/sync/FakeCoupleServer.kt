package com.viniciusrio.dengo.data.sync

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CopyOnWriteArrayList
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okhttp3.mockwebserver.SocketPolicy

/** Servidor HTTP real em porta local. Cada rota responde a fila programada; sem fila, um padrão. */
class FakeCoupleServer : AutoCloseable {
    data class Received(val method: String, val path: String, val key: String?, val auth: String?, val body: String)

    val requests = CopyOnWriteArrayList<Received>()
    private val queued = ConcurrentHashMap<String, ConcurrentLinkedQueue<MockResponse>>()

    /** Página padrão de `GET /v1/sync`, montada a partir do cursor pedido. */
    @Volatile var syncPage: (after: Long) -> String = { after -> page(after) }

    private val server = MockWebServer().apply {
        dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val path = request.path!!
                requests += Received(request.method!!, path, request.getHeader("Idempotency-Key"), request.getHeader("Authorization"), request.body.readUtf8())
                val route = "${request.method} ${path.substringBefore('?')}"
                queued[route]?.poll()?.let { return it }
                if (route == "GET /v1/sync") {
                    val after = Regex("after=(\\d+)").find(path)!!.groupValues[1].toLong()
                    return json(200, syncPage(after))
                }
                return json(500, """{"code":"NOT_PROGRAMMED"}""")
            }
        }
        start()
    }

    val baseUrl: String get() = server.url("/").toString().trimEnd('/')

    fun respond(route: String, status: Int, body: String) = enqueue(route, json(status, body))

    /** Recebe o pedido inteiro e derruba a conexão sem responder: o resultado fica incerto. */
    fun dropAfterRequest(route: String) = enqueue(route, MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST))

    /** Atrasa os cabeçalhos da próxima resposta programada da rota. */
    fun delayNext(route: String, millis: Long) {
        queued[route]?.peek()?.setHeadersDelay(millis, java.util.concurrent.TimeUnit.MILLISECONDS)
    }

    fun writes() = requests.filter { it.method != "GET" }

    private fun enqueue(route: String, response: MockResponse) {
        queued.getOrPut(route) { ConcurrentLinkedQueue() } += response
    }

    private fun json(status: Int, body: String) =
        MockResponse().setResponseCode(status).setHeader("Content-Type", "application/json").setBody(body)

    override fun close() = server.shutdown()

    companion object {
        fun page(
            revision: Long,
            hasMore: Boolean = false,
            requests: String = "",
            history: String = "",
            mood: String = "null",
            personalSpace: String = "null",
        ) = """{"revision":$revision,"hasMore":$hasMore,"requests":[$requests],"history":[$history],"mood":$mood,"personalSpace":$personalSpace}"""

        fun request(id: Long, status: String = "PENDING", revision: Long = 1, type: String = "DENGO", message: String? = null) =
            """{"id":$id,"type":"$type","requesterId":"LIDIANNE","recipientId":"VINICIUS","status":"$status",""" +
                """"createdAt":"2026-11-20T12:00:00.123456Z","message":${message?.let { "\"$it\"" } ?: "null"},"revision":$revision}"""

        fun event(id: Long, type: String, actor: String, requestId: Long? = null, option: String? = null, revision: Long = 1) =
            """{"id":$id,"type":"$type","actorId":"$actor","occurredAt":"2026-11-20T12:00:00Z","requestId":${requestId ?: "null"},""" +
                """"option":${option?.let { "\"$it\"" } ?: "null"},"revision":$revision}"""
    }
}
