package com.example.afinador.audio

import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.roundToInt

data class NoteReading(
    val name: String,
    val octave: Int,
    val frequencyHz: Double,
    val targetHz: Double,
    val cents: Double,
    val levelDbfs: Double,
    val confidence: Double,
) {
    val label: String get() = "$name$octave"
}

object NoteMath {
    private val names = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
    private const val LOWEST_MIDI = 36 // C2
    private const val HIGHEST_MIDI = 76 // E5

    fun fromFrequency(
        frequencyHz: Double,
        referenceA4Hz: Int,
        levelDbfs: Double,
        confidence: Double,
    ): NoteReading? {
        if (!frequencyHz.isFinite() || frequencyHz <= 0.0 || referenceA4Hz !in 400..480) return null
        val midi = (69 + 12 * log2(frequencyHz / referenceA4Hz)).roundToInt()
        if (midi !in LOWEST_MIDI..HIGHEST_MIDI) return null
        val target = referenceA4Hz * 2.0.pow((midi - 69) / 12.0)
        return NoteReading(
            name = names[midi % 12],
            octave = midi / 12 - 1,
            frequencyHz = frequencyHz,
            targetHz = target,
            cents = 1200 * log2(frequencyHz / target),
            levelDbfs = levelDbfs,
            confidence = confidence,
        )
    }
}
