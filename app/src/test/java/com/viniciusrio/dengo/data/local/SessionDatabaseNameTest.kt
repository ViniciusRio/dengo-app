package com.viniciusrio.dengo.data.local

import com.viniciusrio.dengo.model.PartnerId
import org.junit.Assert.assertEquals
import org.junit.Test

class SessionDatabaseNameTest {
    @Test fun eachPersonHasAnOwnFileAndNoneIsTheOldLocalDatabase() {
        assertEquals("dengo-session-lidianne.db", sessionDatabaseName(PartnerId.LIDIANNE))
        assertEquals("dengo-session-vinicius.db", sessionDatabaseName(PartnerId.VINICIUS))
    }
}
