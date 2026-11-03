package com.viniciusrio.dengo.backend

import org.assertj.core.api.Assertions.assertThat
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class BootstrapTest : BackendTest() {
    @Autowired
    private lateinit var flyway: Flyway

    @Test
    fun `server starts against PostgreSQL with migrations applied`() {
        assertThat(flyway.info().applied()).isNotEmpty
    }
}
