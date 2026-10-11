package com.example.afinador.ui

import com.example.afinador.audio.NoteReading
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReadingHoldTest {
    private val c2 = NoteReading("C", 2, 65.4, 65.4, 0.0, -40.0, 0.9)
    private val d2 = NoteReading("D", 2, 73.4, 73.4, 0.0, -38.0, 0.9)

    @Test fun retainsLastReadingForFiveSecondsWithoutExtendingOnRepeatedSilence() {
        val hold = ReadingHold()
        hold.accept(c2, 1_000)
        assertTrue(hold.accept(null, 2_000))
        assertTrue(hold.isHeld)
        assertFalse(hold.accept(null, 4_000))
        assertFalse(hold.expire(6_999))
        assertEquals(c2, hold.reading)
        assertTrue(hold.expire(7_000))
        assertNull(hold.reading)
    }

    @Test fun newNoteReplacesHeldReadingImmediately() {
        val hold = ReadingHold()
        hold.accept(c2, 1_000)
        hold.accept(null, 2_000)
        hold.accept(d2, 2_300)
        assertEquals(d2, hold.reading)
        assertFalse(hold.isHeld)
        assertFalse(hold.expire(7_000))
    }
}
