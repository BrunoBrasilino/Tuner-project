package com.example.afinador

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.afinador.audio.AudioLevel
import com.example.afinador.audio.NoteReading
import com.example.afinador.audio.TunerEngine
import com.example.afinador.ui.ReadingHold
import com.example.afinador.ui.TunerScreen
import com.example.afinador.ui.TunerStatus
import com.example.afinador.ui.TunerTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var preferences: SharedPreferences
    private var engine: TunerEngine? = null
    private val readingHold = ReadingHold()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val clearHeldReading = Runnable {
        if (readingHold.expire(SystemClock.elapsedRealtime())) {
            reading = null
            readingIsHeld = false
        }
    }
    private var reading by mutableStateOf<NoteReading?>(null)
    private var readingIsHeld by mutableStateOf(false)
    private var audioLevel by mutableStateOf<AudioLevel?>(null)
    private var tunerStatus by mutableStateOf(TunerStatus.IDLE)
    private var permissionDeniedPermanently by mutableStateOf(false)
    private var referenceA4Hz by mutableIntStateOf(440)
    private var presets by mutableStateOf(listOf(440))
    private var automaticSensitivity by mutableStateOf(true)
    private var manualThresholdDbfs by mutableIntStateOf(-55)
    private var language by mutableStateOf("pt")

    override fun attachBaseContext(newBase: Context) {
        val savedLanguage = newBase.getSharedPreferences("afinador", MODE_PRIVATE)
            .getString("language", "pt").takeIf { it in LANGUAGES } ?: "pt"
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(Locale.forLanguageTag(savedLanguage))
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.BLACK),
        )
        preferences = getSharedPreferences("afinador", MODE_PRIVATE)
        referenceA4Hz = preferences.getInt("a4_hz", 440).coerceIn(400, 480)
        presets = parsePresets(preferences.getString("a4_presets", null))
        automaticSensitivity = preferences.getBoolean("automatic_sensitivity", true)
        manualThresholdDbfs = preferences.getInt("manual_dbfs", -55).coerceIn(-80, -20)
        language = preferences.getString("language", "pt").takeIf { it in LANGUAGES } ?: "pt"

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) startEngine()
            else {
                permissionDeniedPermanently = !shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)
                tunerStatus = TunerStatus.PERMISSION_DENIED
            }
        }

        setContent {
            TunerTheme {
                TunerScreen(
                    reading = reading,
                    readingIsHeld = readingIsHeld,
                    audioLevel = audioLevel,
                    tunerStatus = tunerStatus,
                    permissionDeniedPermanently = permissionDeniedPermanently,
                    referenceA4Hz = referenceA4Hz,
                    presets = presets,
                    automaticSensitivity = automaticSensitivity,
                    manualThresholdDbfs = manualThresholdDbfs,
                    language = language,
                    onRetryMicrophone = ::requestMicrophone,
                    onReferenceChange = { value ->
                        if (value in 400..480) {
                            referenceA4Hz = value
                            engine?.referenceA4Hz = value
                            preferences.edit().putInt("a4_hz", value).apply()
                        }
                    },
                    onSavePreset = {
                        if (referenceA4Hz !in presets && presets.size < 3) {
                            presets = presets + referenceA4Hz
                            savePresets()
                        }
                    },
                    onRemovePreset = { value ->
                        if (value != 440) {
                            presets = presets - value
                            savePresets()
                        }
                    },
                    onLanguageChange = { selected ->
                        if (selected in LANGUAGES && selected != language) {
                            preferences.edit().putString("language", selected).apply()
                            language = selected
                            recreate()
                        }
                    },
                    onAutomaticSensitivityChange = { value ->
                        automaticSensitivity = value
                        engine?.automaticSensitivity = value
                        preferences.edit().putBoolean("automatic_sensitivity", value).apply()
                    },
                    onManualThresholdChange = { value ->
                        if (value in -80..-20) {
                            manualThresholdDbfs = value
                            engine?.manualThresholdDbfs = value
                            preferences.edit().putInt("manual_dbfs", value).apply()
                        }
                    },
                )
            }
        }
    }

    private fun savePresets() {
        preferences.edit().putString("a4_presets", presets.joinToString(",")).apply()
    }

    private fun requestMicrophone() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            permissionDeniedPermanently = false
            startEngine()
        } else if (permissionDeniedPermanently) {
            startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            })
        } else {
            preferences.edit().putBoolean("microphone_permission_requested", true).apply()
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun startEngine() {
        if (engine != null) return
        lateinit var created: TunerEngine
        created = TunerEngine(
            context = applicationContext,
            onReading = { next ->
                runOnUiThread {
                    if (engine === created) {
                        val wasHeld = readingHold.isHeld
                        if (readingHold.accept(next, SystemClock.elapsedRealtime())) {
                            reading = readingHold.reading
                            readingIsHeld = readingHold.isHeld
                            if (next != null) mainHandler.removeCallbacks(clearHeldReading)
                            else if (!wasHeld) mainHandler.postDelayed(clearHeldReading, 5_000)
                        }
                    }
                }
            },
            onLevel = { next ->
                runOnUiThread {
                    if (engine === created) audioLevel = next
                }
            },
            onError = { _ ->
                runOnUiThread {
                    if (engine === created) {
                        mainHandler.removeCallbacks(clearHeldReading)
                        readingHold.reset()
                        engine = null
                        tunerStatus = TunerStatus.ERROR
                        reading = null
                        readingIsHeld = false
                        audioLevel = null
                    }
                }
            },
        )
        created.referenceA4Hz = referenceA4Hz
        created.automaticSensitivity = automaticSensitivity
        created.manualThresholdDbfs = manualThresholdDbfs
        engine = created
        tunerStatus = TunerStatus.LISTENING
        created.start()
    }

    private fun stopEngine() {
        engine?.stop()
        engine = null
        mainHandler.removeCallbacks(clearHeldReading)
        readingHold.reset()
        tunerStatus = TunerStatus.IDLE
        reading = null
        readingIsHeld = false
        audioLevel = null
    }

    override fun onStop() {
        if (engine != null) stopEngine()
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        if (!::permissionLauncher.isInitialized) return
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            permissionDeniedPermanently = false
            if (tunerStatus != TunerStatus.ERROR) startEngine()
        } else if (!preferences.getBoolean("microphone_permission_requested", false)) {
            requestMicrophone()
        } else {
            tunerStatus = TunerStatus.PERMISSION_DENIED
        }
    }

    companion object {
        private val LANGUAGES = setOf("pt", "en", "fr")

        private fun parsePresets(raw: String?): List<Int> =
            (listOf(440) + raw.orEmpty().split(',').mapNotNull(String::toIntOrNull))
                .filter { it in 400..480 }
                .distinct()
                .take(3)
    }
}
