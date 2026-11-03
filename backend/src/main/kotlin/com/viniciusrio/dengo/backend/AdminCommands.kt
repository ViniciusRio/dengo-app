package com.viniciusrio.dengo.backend

import java.io.PrintStream
import org.springframework.boot.Banner
import org.springframework.boot.WebApplicationType
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.stereotype.Component

@Component
class AdminCommands(private val identity: Identity) {
    fun issuePairingCode(person: Person): IssuedPairingCode = identity.issuePairingCode(person)
    fun revoke(person: Person) = identity.revoke(person)
    fun invalidateAll() = identity.invalidateAll()
}

private const val USAGE = "uso: admin pairing-code <LIDIANNE|VINICIUS> | revoke <LIDIANNE|VINICIUS> | invalidate-all"

/** Fronteira administrativa, sem porta HTTP: roda no servidor com `java -jar backend.jar admin ...`. */
fun runAdminCli(args: List<String>, out: PrintStream): Int {
    val (springArgs, command) = args.partition { it.startsWith("--") }
    val action: ((AdminCommands) -> Unit) = when {
        command.size == 2 && command[0] == "pairing-code" -> {
            val person = parsePerson(command[1])
            person?.let { p -> { admin: AdminCommands ->
                val issued = admin.issuePairingCode(p)
                out.println(issued.code)
                out.println("expira em ${issued.expiresAt}")
            } }
        }
        command.size == 2 && command[0] == "revoke" -> parsePerson(command[1])?.let { p -> { admin: AdminCommands -> admin.revoke(p) } }
        command == listOf("invalidate-all") -> { admin: AdminCommands -> admin.invalidateAll() }
        else -> null
    } ?: run {
        out.println(USAGE)
        return 2
    }
    SpringApplicationBuilder(BackendApplication::class.java)
        .web(WebApplicationType.NONE)
        .bannerMode(Banner.Mode.OFF)
        .logStartupInfo(false)
        .properties("logging.level.root=WARN")
        .run(*springArgs.toTypedArray())
        .use { context -> action(context.getBean(AdminCommands::class.java)) }
    return 0
}

private fun parsePerson(value: String): Person? = Person.entries.firstOrNull { it.name == value }
