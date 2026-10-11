package com.example.afinador.audio

import kotlin.math.max
import kotlin.math.min

data class PitchEstimate(val frequencyHz: Double, val confidence: Double)

/** Estimates one fundamental at a time from recent mono PCM. No Android dependencies. */
class YinPitchDetector(sampleRate: Int) {
    private val decimation = if (sampleRate >= 32_000) 4 else 2
    private val analysisRate = sampleRate.toDouble() / decimation
    private val frame = DoubleArray(2048)
    private val ordered = DoubleArray(frame.size)
    private val difference = DoubleArray(256)
    private var next = 0
    private var filled = 0
    private var partialSum = 0.0
    private var partialCount = 0

    fun push(samples: ShortArray, count: Int): PitchEstimate? {
        for (i in 0 until count.coerceAtMost(samples.size)) {
            partialSum += samples[i] / 32768.0
            partialCount++
            if (partialCount == decimation) {
                frame[next] = partialSum / decimation
                next = (next + 1) % frame.size
                filled = min(frame.size, filled + 1)
                partialSum = 0.0
                partialCount = 0
            }
        }
        if (filled < frame.size) return null

        var mean = 0.0
        for (i in ordered.indices) {
            ordered[i] = frame[(next + i) % frame.size]
            mean += ordered[i]
        }
        mean /= ordered.size
        for (i in ordered.indices) ordered[i] -= mean

        val minLag = max(2, (analysisRate / 800.0).toInt())
        val maxLag = min(difference.lastIndex - 1, (analysisRate / 55.0).toInt())
        if (minLag >= maxLag) return null

        val overlap = ordered.size - maxLag
        for (lag in 1..maxLag) {
            var sum = 0.0
            for (i in 0 until overlap) {
                val delta = ordered[i] - ordered[i + lag]
                sum += delta * delta
            }
            difference[lag] = sum
        }

        var cumulative = 0.0
        for (lag in 1..maxLag) {
            cumulative += difference[lag]
            val normalized = if (cumulative > 1e-12) difference[lag] * lag / cumulative else 1.0
            difference[lag] = normalized
        }

        var bestLag = 0
        var bestValue = Double.POSITIVE_INFINITY
        for (lag in minLag..maxLag) {
            val normalized = difference[lag]
            if (normalized < bestValue) {
                bestValue = normalized
                bestLag = lag
            }
            if (normalized < 0.12 && normalized <= difference[lag - 1]) {
                var minimum = lag
                while (minimum < maxLag && difference[minimum + 1] < difference[minimum]) minimum++
                bestLag = minimum
                bestValue = difference[minimum]
                break
            }
        }
        if (bestLag == 0 || bestValue > 0.22) return null

        val left = difference[bestLag - 1]
        val center = difference[bestLag]
        val right = if (bestLag < maxLag) difference[bestLag + 1] else center
        val denominator = left - 2 * center + right
        val correction = if (kotlin.math.abs(denominator) > 1e-12) {
            (0.5 * (left - right) / denominator).coerceIn(-1.0, 1.0)
        } else 0.0
        val frequency = analysisRate / (bestLag + correction)
        return PitchEstimate(frequency, 1.0 - bestValue)
    }
}
