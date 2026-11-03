package com.viniciusrio.dengo.data.session

import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest

/** Servidor HTTP real em porta local: a fronteira do teste é o protocolo, não uma classe interna. */
class FakePairingServer : AutoCloseable {
    data class Received(val method: String?, val path: String?, val contentType: String?, val body: String)

    val requests = CopyOnWriteArrayList<Received>()
    @Volatile var status = 200
    @Volatile var response = """{"person":"LIDIANNE","token":"token-sintetico"}"""
    @Volatile var delayMillis = 0L

    private val server = MockWebServer().apply {
        dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                requests += Received(request.method, request.path, request.getHeader("Content-Type"), request.body.readUtf8())
                return MockResponse().setResponseCode(status).setBody(response)
                    .setHeadersDelay(delayMillis, TimeUnit.MILLISECONDS)
            }
        }
        start()
    }

    val baseUrl: String get() = server.url("/").toString().trimEnd('/')

    override fun close() = server.shutdown()
}
