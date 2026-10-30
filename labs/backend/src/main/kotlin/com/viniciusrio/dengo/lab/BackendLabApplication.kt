package com.viniciusrio.dengo.lab

import java.time.Clock
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class BackendLabApplication {
    @Bean fun clock(): Clock = Clock.systemUTC()

    @Bean fun careRequestCatalog(clock: Clock): CareRequestCatalog = CareRequestCatalog(clock)
}

fun main(args: Array<String>) {
    runApplication<BackendLabApplication>(*args)
}
