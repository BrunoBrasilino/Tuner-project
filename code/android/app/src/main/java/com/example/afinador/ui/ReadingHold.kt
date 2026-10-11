package com.example.afinador.ui

import com.example.afinador.audio.NoteReading

/** Keeps the last valid reading briefly, while making its stale state explicit to the UI. */
class ReadingHold(private val durationMs: Long = 5_000) {
    var reading: NoteReading? = null
        private set
    var isHeld: Boolean = false
        private set
    private var expiresAtMs: Long = 0

    fun accept(next: NoteReading?, nowMs: Long): Boolean {
        if (next != null) {
            reading = next
            isHeld = false
            expiresAtMs = 0
            return true
        }
        if (reading != null && !isHeld) {
            isHeld = true
            expiresAtMs = nowMs + durationMs
            return true
        }
        return false
    }

    fun expire(nowMs: Long): Boolean {
        if (!isHeld || nowMs < expiresAtMs) return false
        reset()
        return true
    }

    fun reset() {
        reading = null
        isHeld = false
        expiresAtMs = 0
    }
}
