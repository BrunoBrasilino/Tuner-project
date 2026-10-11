package com.example.afinador

import android.os.Bundle
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.afinador.audio.AudioLevel
import com.example.afinador.audio.NoteMath
import com.example.afinador.ui.TunerScreen
import com.example.afinador.ui.TunerStatus
import com.example.afinador.ui.TunerTheme
import java.util.Locale

/** Stable UI states for emulator screenshots. Included only in debug builds. */
open class LayoutPreviewActivity : ComponentActivity() {
    private var previewState by mutableStateOf("empty-auto")
    private var automatic by mutableStateOf(true)
    private var threshold by mutableIntStateOf(-53)

    override fun attachBaseContext(newBase: Context) {
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(Locale.forLanguageTag(previewLocaleTag()))
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.BLACK),
        )
        applyPreviewIntent(intent)
        setContent {
            val hasSignal = previewState.startsWith("signal-")
            val aboveThreshold = previewState.startsWith("signal-high")
            val hasNote = previewState.startsWith("note") || previewState.startsWith("held") || aboveThreshold
            val reading = if (hasNote) {
                NoteMath.fromFrequency(
                    if (hasSignal) 131.5 else 122.3,
                    440,
                    if (hasSignal) -68.0 else -42.0,
                    0.95,
                )
            } else null
            val level = if (hasSignal) AudioLevel(if (aboveThreshold) -68.0 else -83.0, -75.0)
                else AudioLevel(-42.0, -53.0)
            TunerTheme {
                TunerScreen(
                    reading = reading,
                    readingIsHeld = previewState.startsWith("held"),
                    audioLevel = level,
                    tunerStatus = TunerStatus.LISTENING,
                    permissionDeniedPermanently = false,
                    referenceA4Hz = 440,
                    presets = listOf(440),
                    automaticSensitivity = automatic,
                    manualThresholdDbfs = threshold,
                    language = previewLocaleTag(),
                    onRetryMicrophone = {},
                    onReferenceChange = {},
                    onSavePreset = {},
                    onRemovePreset = {},
                    onLanguageChange = {},
                    onAutomaticSensitivityChange = { automatic = it },
                    onManualThresholdChange = { threshold = it },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        applyPreviewIntent(intent)
    }

    private fun applyPreviewIntent(intent: Intent) {
        previewState = intent.getStringExtra("state") ?: "empty-auto"
        automatic = !previewState.endsWith("manual")
        threshold = if (previewState.startsWith("signal-")) -75 else -53
    }

    protected open fun previewLocaleTag() = "pt"
}

class LayoutPreviewEnglishActivity : LayoutPreviewActivity() {
    override fun previewLocaleTag() = "en"
}

class LayoutPreviewFrenchActivity : LayoutPreviewActivity() {
    override fun previewLocaleTag() = "fr"
}
