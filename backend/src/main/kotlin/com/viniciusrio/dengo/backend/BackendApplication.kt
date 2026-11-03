package com.viniciusrio.dengo.backend

import java.time.Clock
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import kotlin.system.exitProcess
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class BackendApplication {
    @Bean
    fun clock(): Clock = Clock.systemUTC()
}

enum class Person { LIDIANNE, VINICIUS }

/** Precisão do PostgreSQL, para a resposta e a sincronização devolverem o mesmo horário. */
internal fun Clock.now(): Instant = instant().truncatedTo(ChronoUnit.MICROS)

internal fun Instant.toDb(): OffsetDateTime = atOffset(ZoneOffset.UTC)

fun main(args: Array<String>) {
    if (args.firstOrNull() == "admin") exitProcess(runAdminCli(args.drop(1), System.out))
    runApplication<BackendApplication>(*args)
}
