package com.viniciusrio.dengo.data.sync

import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class SyncDatabaseFilesTest {
    @get:Rule val folder = TemporaryFolder()

    @Test fun theCopyHasItsOwnFileApartFromTheOldLocalDatabase() {
        assertEquals("dengo-sync.db", SYNC_DATABASE_NAME)
    }

    @Test fun onlyTheStageThreePerPersonDatabasesAreDeleted() {
        val kept = listOf("dengo-local.db", "dengo-local.db-wal", "dengo-sync.db", "dengo-session-other.db")
        val obsolete = listOf("dengo-session-lidianne.db", "dengo-session-vinicius.db", "dengo-session-vinicius.db-wal", "dengo-session-vinicius.db-shm")
        (kept + obsolete).forEach { folder.newFile(it).writeText("x") }

        deleteStageThreeSessionDatabases(folder.root)

        assertEquals(kept.sorted(), folder.root.list()!!.sorted())
    }
}
