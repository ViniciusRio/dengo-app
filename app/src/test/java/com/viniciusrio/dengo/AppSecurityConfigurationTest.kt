package com.viniciusrio.dengo

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Confere os arquivos de configuração que valem no build de uso real. */
class AppSecurityConfigurationTest {
    private val manifest = File("src/main/AndroidManifest.xml").readText()

    @Test fun `the app asks for internet`() {
        assertTrue(manifest.contains("android.permission.INTERNET"))
    }

    @Test fun `traffic without https is refused outside the debug build`() {
        assertTrue(manifest.contains("android:usesCleartextTraffic=\"false\""))
        assertTrue(manifest.contains("android:networkSecurityConfig=\"@xml/network_security_config\""))
        val config = File("src/main/res/xml/network_security_config.xml").readText()
        assertTrue(config.contains("cleartextTrafficPermitted=\"false\""))
        assertFalse(config.contains("cleartextTrafficPermitted=\"true\""))
        assertFalse(config.contains("<domain"))
    }

    @Test fun `the debug exception covers only the local machine`() {
        val config = File("src/debug/res/xml/network_security_config.xml").readText()
        val domains = Regex("<domain[^>]*>([^<]+)</domain>").findAll(config).map { it.groupValues[1] }.toSet()
        assertEquals(setOf("localhost", "127.0.0.1"), domains)
        assertTrue(config.contains("<base-config cleartextTrafficPermitted=\"false\""))
    }

    @Test fun `backup and transfer stay off`() {
        assertTrue(manifest.contains("android:allowBackup=\"false\""))
        assertTrue(manifest.contains("android:fullBackupContent=\"false\""))
        val rules = File("src/main/res/xml/data_extraction_rules.xml").readText()
        listOf("root", "file", "database", "sharedpref", "external").forEach { domain ->
            assertEquals(domain, 2, Regex("<exclude domain=\"$domain\" path=\"\\.\" />").findAll(rules).count())
        }
    }
}
