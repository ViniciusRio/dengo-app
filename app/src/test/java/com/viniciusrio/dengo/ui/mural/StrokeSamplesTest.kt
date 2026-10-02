package com.viniciusrio.dengo.ui.mural

import androidx.compose.ui.geometry.Offset
import org.junit.Assert.assertEquals
import org.junit.Test

class StrokeSamplesTest {
    @Test fun quickStrokeIncludesPositionWhereFingerLifts() {
        val samples = StrokeSamples(Offset(10f, 20f))

        samples.append(Offset(16f, 23f), pressed = false)

        assertEquals(listOf(Offset(10f, 20f), Offset(16f, 23f)), samples.completed())
    }

    @Test fun liftAddsLastShortSegmentWithoutDuplicatingAStationaryLift() {
        val samples = StrokeSamples(Offset(10f, 20f))

        samples.append(Offset(20f, 20f), pressed = true)
        samples.append(Offset(21f, 20f), pressed = false)

        assertEquals(listOf(Offset(10f, 20f), Offset(20f, 20f), Offset(21f, 20f)), samples.completed())
        assertEquals(listOf(Offset(5f, 5f)), StrokeSamples(Offset(5f, 5f)).apply {
            append(Offset(5f, 5f), pressed = false)
        }.completed())
    }
}
