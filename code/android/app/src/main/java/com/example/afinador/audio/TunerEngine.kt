package com.example.afinador.audio

import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.SystemClock
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.sqrt

/** Owns the microphone only while the tuning screen is active. Callbacks run on its worker thread. */
data class AudioLevel(val inputDbfs: Double, val thresholdDbfs: Double)

class TunerEngine(
    private val context: Context,
    private val onReading: (NoteReading?) -> Unit,
    private val onLevel: (AudioLevel) -> Unit,
    private val onError: (String) -> Unit,
) {
    @Volatile var referenceA4Hz: Int = 440
    @Volatile var automaticSensitivity: Boolean = true
    @Volatile var manualThresholdDbfs: Int = -55

    @Volatile private var running = false
    @Volatile private var recording: AudioRecord? = null
    private var worker: Thread? = null

    fun start() {
        if (running) return
        running = true
        worker = Thread({ captureLoop() }, "tuner-audio").apply { isDaemon = true; start() }
    }

    fun stop() {
        running = false
        try {
            recording?.stop()
        } catch (_: IllegalStateException) {
            // The capture thread will release it.
        }
    }

    private fun captureLoop() {
        var audio: AudioRecord? = null
        try {
            val configured = createAudioRecord()
            audio = configured.first
            recording = audio
            val sampleRate = configured.second
            val detector = YinPitchDetector(sampleRate)
            val samples = ShortArray(2048)
            var noiseFloor = -85.0
            var displayed: NoteReading? = null
            var pendingLabel: String? = null
            var pendingCount = 0
            var visualLevelDbfs = -120.0
            var levelHoldUntilMs = 0L
            var lastLevelTimeMs = SystemClock.elapsedRealtime()
            var lastLevelEmissionMs = 0L
            audio.startRecording()

            while (running) {
                val count = audio.read(samples, 0, samples.size, AudioRecord.READ_BLOCKING)
                if (count <= 0) {
                    if (running) throw IllegalStateException("Falha ao ler o microfone ($count).")
                    break
                }
                var energy = 0.0
                for (i in 0 until count) {
                    val normalized = samples[i] / 32768.0
                    energy += normalized * normalized
                }
                val rms = sqrt(energy / count)
                val levelDbfs = if (rms > 0.0) 20 * log10(rms) else -120.0
                val estimate = detector.push(samples, count)
                val threshold = if (automaticSensitivity) (noiseFloor + 12).coerceIn(-75.0, -35.0)
                    else manualThresholdDbfs.toDouble()
                val nowMs = SystemClock.elapsedRealtime()
                if (levelDbfs >= visualLevelDbfs) {
                    visualLevelDbfs = levelDbfs
                    levelHoldUntilMs = nowMs + 220
                } else if (nowMs > levelHoldUntilMs) {
                    val seconds = (nowMs - lastLevelTimeMs).coerceAtLeast(0) / 1000.0
                    visualLevelDbfs = max(levelDbfs, visualLevelDbfs - 42.0 * seconds)
                }
                lastLevelTimeMs = nowMs
                if (nowMs - lastLevelEmissionMs >= 100) {
                    onLevel(AudioLevel(visualLevelDbfs, threshold))
                    lastLevelEmissionMs = nowMs
                }
                val valid = estimate != null && levelDbfs >= threshold && estimate.confidence >= 0.78

                if (!valid) {
                    if ((estimate == null || estimate.confidence < 0.78) && levelDbfs < -35.0) {
                        noiseFloor = noiseFloor * 0.95 + levelDbfs * 0.05
                    }
                    displayed = null
                    pendingLabel = null
                    pendingCount = 0
                    onReading(null)
                    continue
                }

                val next = NoteMath.fromFrequency(
                    frequencyHz = estimate!!.frequencyHz,
                    referenceA4Hz = referenceA4Hz,
                    levelDbfs = levelDbfs,
                    confidence = estimate.confidence,
                )
                if (next == null) {
                    displayed = null
                    onReading(null)
                    continue
                }
                if (displayed != null && next.label != displayed.label) {
                    pendingCount = if (pendingLabel == next.label) pendingCount + 1 else 1
                    pendingLabel = next.label
                    if (pendingCount < 2) {
                        onReading(displayed)
                        continue
                    }
                }
                pendingLabel = null
                pendingCount = 0
                val smoothHz = if (displayed?.label == next.label) {
                    displayed.frequencyHz * 0.65 + next.frequencyHz * 0.35
                } else next.frequencyHz
                displayed = NoteMath.fromFrequency(smoothHz, referenceA4Hz, levelDbfs, next.confidence)
                onReading(displayed)
            }
        } catch (error: SecurityException) {
            if (running) onError("Permissão do microfone indisponível.")
        } catch (error: Exception) {
            if (running) onError(error.message ?: "Não foi possível iniciar o microfone.")
        } finally {
            running = false
            recording = null
            try { audio?.stop() } catch (_: IllegalStateException) { }
            audio?.release()
        }
    }

    private fun createAudioRecord(): Pair<AudioRecord, Int> {
        val manager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val rawSupported = manager.getProperty(AudioManager.PROPERTY_SUPPORT_AUDIO_SOURCE_UNPROCESSED) == "true"
        val sources = if (rawSupported) {
            intArrayOf(MediaRecorder.AudioSource.UNPROCESSED, MediaRecorder.AudioSource.MIC)
        } else intArrayOf(MediaRecorder.AudioSource.MIC)

        for (rate in intArrayOf(48_000, 44_100)) {
            val minimum = AudioRecord.getMinBufferSize(
                rate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
            )
            if (minimum <= 0) continue
            for (source in sources) {
                try {
                    val format = AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(rate)
                        .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                        .build()
                    val record = AudioRecord.Builder()
                        .setAudioSource(source)
                        .setAudioFormat(format)
                        .setBufferSizeInBytes(max(minimum * 2, 8192))
                        .build()
                    if (record.state == AudioRecord.STATE_INITIALIZED) return record to rate
                    record.release()
                } catch (_: IllegalArgumentException) {
                    // Try another source or sample rate.
                } catch (_: UnsupportedOperationException) {
                    // Try another source or sample rate.
                }
            }
        }
        throw IllegalStateException("Nenhuma configuração de microfone compatível foi encontrada.")
    }
}
