package com.viniciusrio.dengo.data

import com.viniciusrio.dengo.model.CareRequestType
import com.viniciusrio.dengo.model.HistoryEvent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CoupleRepositoryContractTest {
    @Test fun fakeStartsReadyAndReportsChangedThenUnchangedDuplicate() = runTest {
        val repository: CoupleRepository = FakeCoupleRepository()
        assertTrue(repository.state.value is RepositoryReadState.Ready)

        val created = repository.createRequest(CareRequestType.DENGO)
        val duplicate = repository.createRequest(CareRequestType.DENGO)

        assertTrue(created is WriteResult.Confirmed && created.changed)
        assertTrue(duplicate is WriteResult.Confirmed && !duplicate.changed)
        assertEquals((created as WriteResult.Confirmed).value, (duplicate as WriteResult.Confirmed).value)
        val ready = repository.state.value as RepositoryReadState.Ready
        assertEquals(1, ready.value.requests.size)
        assertEquals(1, ready.value.history.count { it is HistoryEvent.RequestCreated })
    }
}
