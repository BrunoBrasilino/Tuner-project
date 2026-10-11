package com.example.afinador.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.log2
import kotlin.math.sin

class PitchDetectorTest {
    @Test
    fun detectsLowerAndUpperNotesInInitialRange() {
        for (frequency in listOf(65.406, 82.407, 329.628, 659.255)) {
            val estimate = estimate(frequency, harmonic = false)
            assertNotNull("No pitch for $frequency Hz", estimate)
            val cents = 1200 * log2(estimate!!.frequencyHz / frequency)
            assertTrue("Error at $frequency Hz: $cents cents", abs(cents) < 5.0)
        }
    }

    @Test
    fun detectsFundamentalWhenSecondHarmonicIsLouder() {
        val estimate = estimate(82.407, harmonic = true)
        assertNotNull(estimate)
        assertTrue(abs(1200 * log2(estimate!!.frequencyHz / 82.407)) < 5.0)
    }

    @Test
    fun rejectsSilence() {
        val detector = YinPitchDetector(48_000)
        repeat(6) { assertNull(detector.push(ShortArray(2048), 2048)) }
    }

    @Test
    fun mapsReferenceAndOctave() {
        val a4 = NoteMath.fromFrequency(432.0, 432, -40.0, 0.95)
        assertEquals("A4", a4?.label)
        assertEquals(0.0, a4!!.cents, 0.001)
        assertNull(NoteMath.fromFrequency(41.2, 440, -40.0, 0.95))
    }

    private fun estimate(frequency: Double, harmonic: Boolean): PitchEstimate? {
        val detector = YinPitchDetector(48_000)
        var result: PitchEstimate? = null
        repeat(6) { chunk ->
            val samples = ShortArray(2048) { index ->
                val time = (chunk * 2048 + index) / 48_000.0
                val fundamental = sin(2 * PI * frequency * time)
                val value = if (harmonic) 0.25 * fundamental + 0.65 * sin(4 * PI * frequency * time)
                    else 0.8 * fundamental
                (value * 20_000).toInt().toShort()
            }
            result = detector.push(samples, samples.size)
        }
        return result
    }
}
