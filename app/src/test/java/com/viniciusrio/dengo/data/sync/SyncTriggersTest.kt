package com.viniciusrio.dengo.data.sync

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SyncTriggersTest {
    private var syncs = 0

    private fun TestScope.triggers(sync: suspend () -> Unit = { syncs++ }) =
        SyncTriggers(backgroundScope, sync = { sync() }, intervalMillis = 30_000)

    @Test fun `returning to the foreground syncs at once and then every 30 seconds`() = runTest {
        val triggers = triggers()

        triggers.onForeground()
        runCurrent()
        assertEquals(1, syncs)

        advanceTimeBy(29_999)
        runCurrent()
        assertEquals(1, syncs)
        advanceTimeBy(1)
        runCurrent()
        assertEquals(2, syncs)
        advanceTimeBy(60_000)
        runCurrent()
        assertEquals(4, syncs)
    }

    @Test fun `the background stops the periodic sync until the next return`() = runTest {
        val triggers = triggers()
        triggers.onForeground()
        runCurrent()

        triggers.onBackground()
        advanceTimeBy(120_000)
        runCurrent()
        assertEquals(1, syncs)

        triggers.onForeground()
        runCurrent()
        assertEquals(2, syncs)
    }

    @Test fun `a second foreground signal does not start a second timer`() = runTest {
        val triggers = triggers()
        triggers.onForeground()
        triggers.onForeground()
        runCurrent()

        advanceTimeBy(30_000)
        runCurrent()
        assertEquals(2, syncs)
    }

    @Test fun `a notice syncs even in the background`() = runTest {
        val triggers = triggers()

        triggers.onNotice()
        runCurrent()

        assertEquals(1, syncs)
    }

    @Test fun `going to the background lets a sync in progress finish`() = runTest {
        val release = CompletableDeferred<Unit>()
        var finished = 0
        val triggers = triggers { release.await(); finished++ }
        triggers.onForeground()
        runCurrent()

        triggers.onBackground()
        release.complete(Unit)
        runCurrent()

        assertEquals(1, finished)
    }
}
