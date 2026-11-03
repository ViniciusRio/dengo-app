package com.viniciusrio.dengo.backend

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.sql.DriverManager
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.util.UUID
import java.util.concurrent.CompletableFuture
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.postgresql.PostgreSQLContainer
import tools.jackson.databind.JsonNode
import tools.jackson.databind.json.JsonMapper

/** Relógio controlado pelos testes; o único colaborador substituído é esta fronteira do sistema. */
class TestClock(private var current: Instant = START) : Clock() {
    override fun instant(): Instant = current
    override fun getZone(): ZoneId = ZoneId.of("UTC")
    override fun withZone(zone: ZoneId): Clock = this
    fun set(instant: Instant) { current = instant }
    fun advance(duration: Duration) { current = current.plus(duration) }

    companion object {
        val START: Instant = Instant.parse("2026-11-03T15:00:00Z")
    }
}

@TestConfiguration
class TestClockConfiguration {
    @Bean
    @Primary
    fun testClock(): TestClock = TestClock()
}

data class ApiResponse(val status: Int, val body: JsonNode) {
    val code: String? get() = body.get("code")?.asString()
}

class Api(private val port: Int) {
    private val http = HttpClient.newHttpClient()
    private val mapper = JsonMapper.builder().build()

    fun send(method: String, path: String, token: String? = null, key: String? = null, body: Any? = null): ApiResponse =
        sendAsync(method, path, token, key, body).join()

    fun sendAsync(method: String, path: String, token: String? = null, key: String? = null, body: Any? = null): CompletableFuture<ApiResponse> {
        val json = when (body) {
            null -> null
            is String -> body
            else -> mapper.writeValueAsString(body)
        }
        val request = HttpRequest.newBuilder(URI.create("http://localhost:$port$path"))
            .timeout(Duration.ofSeconds(30))
            .method(method, json?.let(HttpRequest.BodyPublishers::ofString) ?: HttpRequest.BodyPublishers.noBody())
            .apply {
                if (json != null) header("Content-Type", "application/json")
                token?.let { header("Authorization", "Bearer $it") }
                key?.let { header("Idempotency-Key", it) }
            }
            .build()
        return http.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply { response ->
            val text = response.body()
            ApiResponse(response.statusCode(), mapper.readTree(text.ifEmpty { "{}" }))
        }
    }
}

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestClockConfiguration::class)
abstract class BackendTest {
    @LocalServerPort
    protected var port: Int = 0

    @Autowired
    protected lateinit var jdbc: JdbcClient

    @Autowired
    protected lateinit var clock: TestClock

    @Autowired
    protected lateinit var admin: AdminCommands

    protected val api: Api by lazy { Api(port) }

    @BeforeEach
    fun resetState() {
        clock.set(TestClock.START)
        jdbc.sql(
            jdbc.sql("""
                SELECT string_agg(format('TRUNCATE %I RESTART IDENTITY CASCADE', tablename), ';')
                FROM pg_tables WHERE schemaname = 'public' AND tablename NOT IN ('flyway_schema_history')
            """).query(String::class.java).single(),
        ).update()
        jdbc.sql(SEED_SQL).update()
    }

    protected fun pair(person: Person): String {
        val code = admin.issuePairingCode(person).code
        val response = api.send("POST", "/v1/pairings", body = mapOf("code" to code))
        check(response.status == 200) { "pairing failed: ${response.status}" }
        return response.body.get("token").asString()
    }

    /** Segura um pedido no banco por outra conexão: quem tocar nele fica preso no meio da transação. */
    protected fun <T> holdingRequestRow(id: Long, block: () -> T): T = holdingLock("SELECT 1 FROM care_request WHERE id = $id FOR UPDATE", block)

    /** Segura o contador de revisão: toda escrita para logo no começo da transação. */
    protected fun <T> holdingWriteLock(block: () -> T): T = holdingLock("SELECT 1 FROM sync_counter WHERE id = 1 FOR UPDATE", block)

    private fun <T> holdingLock(sql: String, block: () -> T): T =
        DriverManager.getConnection(postgres.jdbcUrl, postgres.username, postgres.password).use { connection ->
            connection.autoCommit = false
            connection.createStatement().use { it.execute(sql) }
            try {
                block()
            } finally {
                connection.rollback()
            }
        }

    /** Espera até `count` transações da API estarem paradas aguardando um bloqueio. */
    protected fun awaitBlockedWriters(count: Int) {
        val deadline = System.nanoTime() + Duration.ofSeconds(15).toNanos()
        while (blockedBackends().size < count) {
            check(System.nanoTime() < deadline) { "expected $count blocked writers, found ${blockedBackends().size}" }
            Thread.sleep(20)
        }
    }

    /** Derruba a transação mais antiga parada em um bloqueio: uma falha real, que a desfaz por inteiro. */
    protected fun cancelOldestBlockedWriter() {
        val pid = blockedBackends().first()
        jdbc.sql("SELECT pg_cancel_backend(:pid)").param("pid", pid).query(Boolean::class.java).single()
    }

    private fun blockedBackends(): List<Int> = jdbc.sql("""
        SELECT pid FROM pg_stat_activity
        WHERE datname = current_database() AND wait_event_type = 'Lock' AND pid <> pg_backend_pid()
        ORDER BY xact_start
    """).query { rs, _ -> rs.getInt(1) }.list()

    protected fun newKey(): String = UUID.randomUUID().toString()

    companion object {
        val postgres: PostgreSQLContainer = PostgreSQLContainer("postgres:17.10").apply { start() }

        // Mesmo conteúdo inicial que as migrações gravam.
        private val SEED_SQL = BackendTest::class.java.getResource("/db/seed.sql")!!.readText()

        @JvmStatic
        @DynamicPropertySource
        fun datasource(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }
}
