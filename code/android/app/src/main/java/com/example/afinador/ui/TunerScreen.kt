package com.example.afinador.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import com.example.afinador.R
import com.example.afinador.audio.AudioLevel
import com.example.afinador.audio.NoteReading
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val Amber = Color(0xFFFFAF00)
private val Card = Color(0xFF191E23)
private val MainContentHorizontalPadding = 18.dp
private val CentsScaleEdgeInset = 3.dp

enum class TunerStatus { IDLE, LISTENING, PERMISSION_DENIED, ERROR }

@Composable
fun TunerScreen(
    reading: NoteReading?,
    readingIsHeld: Boolean,
    audioLevel: AudioLevel?,
    tunerStatus: TunerStatus,
    permissionDeniedPermanently: Boolean,
    referenceA4Hz: Int,
    presets: List<Int>,
    automaticSensitivity: Boolean,
    manualThresholdDbfs: Int,
    language: String,
    onRetryMicrophone: () -> Unit,
    onReferenceChange: (Int) -> Unit,
    onSavePreset: () -> Unit,
    onRemovePreset: (Int) -> Unit,
    onLanguageChange: (String) -> Unit,
    onAutomaticSensitivityChange: (Boolean) -> Unit,
    onManualThresholdChange: (Int) -> Unit,
) {
    var showSettings by rememberSaveable { mutableStateOf(false) }
    BackHandler(showSettings) { showSettings = false }
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        if (showSettings) {
            SettingsScreen(
                referenceA4Hz = referenceA4Hz,
                presets = presets,
                language = language,
                onBack = { showSettings = false },
                onReferenceChange = onReferenceChange,
                onSavePreset = onSavePreset,
                onRemovePreset = onRemovePreset,
                onLanguageChange = onLanguageChange,
            )
        } else {
            MainTunerScreen(
                reading = reading,
                readingIsHeld = readingIsHeld,
                audioLevel = audioLevel,
                tunerStatus = tunerStatus,
                permissionDeniedPermanently = permissionDeniedPermanently,
                automaticSensitivity = automaticSensitivity,
                manualThresholdDbfs = manualThresholdDbfs,
                onOpenSettings = { showSettings = true },
                onRetryMicrophone = onRetryMicrophone,
                onAutomaticSensitivityChange = onAutomaticSensitivityChange,
                onManualThresholdChange = onManualThresholdChange,
            )
        }
    }
}

@Composable
private fun MainTunerScreen(
    reading: NoteReading?,
    readingIsHeld: Boolean,
    audioLevel: AudioLevel?,
    tunerStatus: TunerStatus,
    permissionDeniedPermanently: Boolean,
    automaticSensitivity: Boolean,
    manualThresholdDbfs: Int,
    onOpenSettings: () -> Unit,
    onRetryMicrophone: () -> Unit,
    onAutomaticSensitivityChange: (Boolean) -> Unit,
    onManualThresholdChange: (Int) -> Unit,
) {
    val context = LocalContext.current
    val locale = context.resources.configuration.locales[0] ?: Locale.getDefault()
    val direction = when {
        reading == null -> ""
        abs(reading.cents) <= 2 -> stringResource(R.string.note_tuned)
        reading.cents < 0 -> stringResource(R.string.note_flat)
        else -> stringResource(R.string.note_sharp)
    }
    val spokenReading = if (reading != null) {
        stringResource(R.string.spoken_reading, reading.label, direction, abs(reading.cents).roundToInt())
    } else ""
    var spokenState by remember { mutableStateOf("") }
    LaunchedEffect(reading?.label, direction, tunerStatus, readingIsHeld) {
        if (reading == null || readingIsHeld) spokenState = ""
        else {
            delay(300)
            spokenState = spokenReading
        }
    }

    BoxWithConstraints(
        Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()
    ) {
        val safeHeight = maxHeight
        val compact = safeHeight < 700.dp || LocalDensity.current.fontScale >= 1.3f
        Column(
            modifier = Modifier.fillMaxSize().clipToBounds().verticalScroll(rememberScrollState())
                .padding(horizontal = MainContentHorizontalPadding),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().heightIn(min = safeHeight).imePadding(),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                    TextButton(onClick = onOpenSettings, modifier = Modifier.semantics {
                        contentDescription = context.getString(R.string.settings)
                    }) {
                        Text("⚙", fontSize = 32.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Column(Modifier.fillMaxWidth()) {
                    TunerReadout(
                        reading = reading,
                        readingIsHeld = readingIsHeld,
                        tunerStatus = tunerStatus,
                        permissionDeniedPermanently = permissionDeniedPermanently,
                        locale = locale,
                    )
                    Spacer(Modifier.height(if (compact) 18.dp else 36.dp))
                    Box(Modifier.alpha(if (readingIsHeld) 0.65f else 1f).semantics {
                        liveRegion = LiveRegionMode.Polite
                        stateDescription = spokenState
                    }) {
                        CentsMeter(reading?.cents)
                    }

                    if (tunerStatus == TunerStatus.PERMISSION_DENIED || tunerStatus == TunerStatus.ERROR) {
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = onRetryMicrophone, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(if (tunerStatus == TunerStatus.ERROR) R.string.retry else R.string.allow_microphone))
                        }
                    }
                }

                Column(Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                    Spacer(Modifier.height(if (compact) 16.dp else 24.dp))
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                    SensitivityControls(
                        automatic = automaticSensitivity,
                        manualThreshold = manualThresholdDbfs,
                        audioLevel = audioLevel,
                        onAutomaticChange = onAutomaticSensitivityChange,
                        onThresholdChange = onManualThresholdChange,
                    )
                }
            }
        }
    }
}

@Composable
private fun TunerReadout(
    reading: NoteReading?,
    readingIsHeld: Boolean,
    tunerStatus: TunerStatus,
    permissionDeniedPermanently: Boolean,
    locale: Locale,
) {
        val noteSlotHeight = 132.dp * LocalDensity.current.fontScale
        val noteColor = when {
            reading == null -> MaterialTheme.colorScheme.onSurfaceVariant
            abs(reading.cents) > 2 -> Amber
            else -> MaterialTheme.colorScheme.primary
        }.copy(alpha = if (readingIsHeld) 0.62f else 1f)
        Row(
            modifier = Modifier.fillMaxWidth().height(noteSlotHeight),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = reading?.name?.removeSuffix("#") ?: "—",
                color = noteColor,
                fontSize = 108.sp,
                lineHeight = 112.sp,
                fontWeight = FontWeight.Bold,
            )
            if (reading != null) {
                Column(Modifier.height(noteSlotHeight), verticalArrangement = Arrangement.SpaceBetween) {
                    if (reading.name.endsWith("#")) {
                        Text("#", color = noteColor, fontSize = 44.sp, lineHeight = 48.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Spacer(Modifier.height(48.dp))
                    }
                    Text(
                        text = reading.octave.toString(),
                        color = noteColor,
                        fontSize = 44.sp,
                        lineHeight = 52.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )
                }
            }
        }
        Text(
            text = reading?.frequencyHz?.let { formatHz(it, locale) } ?: "— Hz",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 29.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (readingIsHeld) 0.62f else 1f),
        )
        val secondaryText = when {
            reading != null -> stringResource(R.string.target_frequency, formatHz(reading.targetHz, locale))
            tunerStatus == TunerStatus.PERMISSION_DENIED -> stringResource(
                if (permissionDeniedPermanently) R.string.microphone_settings else R.string.microphone_permission
            )
            tunerStatus == TunerStatus.ERROR -> stringResource(R.string.microphone_error)
            else -> ""
        }
        Text(
            text = secondaryText,
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (readingIsHeld) 0.62f else 1f),
            style = MaterialTheme.typography.bodyLarge,
            minLines = 1,
        )

}

@Composable
private fun CentsMeter(cents: Double?) {
    val primary = MaterialTheme.colorScheme.primary
    val accent = when {
        cents == null -> MaterialTheme.colorScheme.onSurfaceVariant
        abs(cents) <= 2 -> primary
        else -> Amber
    }
    val tickColor = MaterialTheme.colorScheme.onSurfaceVariant
    Column(Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Text(
            text = cents?.let { "${if (it >= 0) "+" else ""}${it.roundToInt()} cents" } ?: "— cents",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = accent,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(12.dp))
        Canvas(Modifier.fillMaxWidth().height(68.dp)) {
            val centerY = size.height / 2
            val inset = CentsScaleEdgeInset.toPx().coerceAtMost(size.width / 2)
            val centSpacing = (size.width - 2 * inset) / 100f
            drawLine(tickColor.copy(alpha = 0.32f), Offset(inset, centerY), Offset(size.width - inset, centerY), 1.dp.toPx())
            for (step in -50..50) {
                val x = inset + (step + 50) * centSpacing
                val mainMark = step % 25 == 0
                val fiveCentMark = step % 5 == 0
                val length = when {
                    mainMark -> 24.dp.toPx()
                    fiveCentMark -> 11.dp.toPx()
                    else -> 4.dp.toPx()
                }
                drawLine(
                    color = when {
                        step == 0 -> primary
                        mainMark -> tickColor.copy(alpha = 0.9f)
                        fiveCentMark -> tickColor.copy(alpha = 0.68f)
                        else -> tickColor.copy(alpha = 0.38f)
                    },
                    start = Offset(x, centerY - length),
                    end = Offset(x, centerY + length),
                    strokeWidth = if (mainMark) 2.dp.toPx() else 1.dp.toPx(),
                )
            }
            if (cents != null) {
                val x = inset + ((cents.coerceIn(-50.0, 50.0) + 50.0) * centSpacing).toFloat()
                drawLine(accent, Offset(x, 0f), Offset(x, size.height), 4.dp.toPx(), StrokeCap.Round)
            }
        }
        Layout(
            content = {
                listOf("−50", "−25", "0", "+25", "+50").forEach { label ->
                    Text(label, fontSize = 12.sp, color = tickColor)
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) { measurables, constraints ->
            val labels = measurables.map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) }
            val width = constraints.maxWidth
            val inset = CentsScaleEdgeInset.roundToPx().coerceAtMost(width / 2)
            val scaleWidth = width - 2 * inset
            val labelOverhang = MainContentHorizontalPadding.roundToPx()
            layout(width, labels.maxOfOrNull { it.height } ?: 0) {
                labels.forEachIndexed { index, label ->
                    val markX = inset + index * scaleWidth / 4f
                    val left = (markX - label.width / 2f).roundToInt()
                        .coerceIn(-labelOverhang, (width + labelOverhang - label.width).coerceAtLeast(-labelOverhang))
                    label.placeRelative(left, 0)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun SensitivityControls(
    automatic: Boolean,
    manualThreshold: Int,
    audioLevel: AudioLevel?,
    onAutomaticChange: (Boolean) -> Unit,
    onThresholdChange: (Int) -> Unit,
) {
    val effectiveThreshold = if (automatic) {
        audioLevel?.thresholdDbfs?.roundToInt()?.coerceIn(-80, -20) ?: manualThreshold
    } else manualThreshold
    var text by remember(manualThreshold) { mutableStateOf(manualThreshold.toString()) }
    val fontScale = LocalDensity.current.fontScale
    val invalidThreshold = !automatic && (text.toIntOrNull()?.let { it !in -80..-20 } ?: true)
    val levelText = stringResource(
        R.string.input_level,
        audioLevel?.inputDbfs?.roundToInt()?.toString() ?: "—",
    )
    val bottomBringIntoView = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()

    BoxWithConstraints(Modifier.fillMaxWidth().padding(top = 16.dp)) {
        val stacked = maxWidth < 355.dp || fontScale >= 1.3f
        Column {
            if (stacked) {
                Text(stringResource(R.string.sensitivity), style = MaterialTheme.typography.titleMedium)
                SensitivityModeSelector(
                    automatic = automatic,
                    onAutomaticChange = onAutomaticChange,
                    modifier = Modifier.align(Alignment.End),
                )
            } else {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(stringResource(R.string.sensitivity), style = MaterialTheme.typography.titleMedium)
                    SensitivityModeSelector(
                        automatic = automatic,
                        onAutomaticChange = onAutomaticChange,
                        modifier = Modifier,
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            SensitivitySlider(
                effectiveThreshold = effectiveThreshold,
                audioLevel = audioLevel,
                automatic = automatic,
                onAutomaticChange = onAutomaticChange,
                onThresholdChange = onThresholdChange,
                modifier = Modifier.fillMaxWidth().height(54.dp),
            )
            Spacer(Modifier.height(4.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (!stacked) {
                    Text(
                        stringResource(R.string.threshold_label),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(Modifier.width(8.dp))
                }
                ThresholdInput(
                    value = if (automatic) effectiveThreshold.toString() else text,
                    invalid = invalidThreshold,
                    wide = stacked,
                    onValueChange = { typed ->
                        if (typed.length <= 3 && (typed.isEmpty() || typed == "-" || typed.toIntOrNull() != null)) {
                            if (automatic) onAutomaticChange(false)
                            text = typed
                            typed.toIntOrNull()?.takeIf { it in -80..-20 }?.let(onThresholdChange)
                        }
                    },
                    onFocus = {
                        if (automatic) {
                            text = effectiveThreshold.toString()
                            onThresholdChange(effectiveThreshold)
                            onAutomaticChange(false)
                        }
                        scope.launch {
                            delay(350)
                            bottomBringIntoView.bringIntoView()
                        }
                    },
                )
            }
            if (stacked) {
                Text(
                    stringResource(R.string.threshold_label),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            if (invalidThreshold) {
                Text(
                    stringResource(R.string.threshold_range_error),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    textAlign = TextAlign.Center,
                )
            }
            Column(Modifier.fillMaxWidth().bringIntoViewRequester(bottomBringIntoView)) {
                Text(
                    levelText,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun SensitivityModeSelector(
    automatic: Boolean,
    onAutomaticChange: (Boolean) -> Unit,
    modifier: Modifier,
) {
    val autoLabel = stringResource(R.string.automatic)
    val manualLabel = stringResource(R.string.manual)
    val modeDescription = stringResource(R.string.sensitivity)
    val switchAction = stringResource(if (automatic) R.string.switch_to_manual else R.string.switch_to_auto)
    val textStyle = MaterialTheme.typography.bodyLarge
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val labelWidth = maxOf(
        textMeasurer.measure(autoLabel, style = textStyle, maxLines = 1).size.width,
        textMeasurer.measure(manualLabel, style = textStyle, maxLines = 1).size.width,
    )
    val halfWidth = with(density) { labelWidth.toDp() } + 24.dp
    val selectedOffset by animateDpAsState(
        targetValue = (if (automatic) 0.dp else halfWidth) + 2.dp,
        animationSpec = tween(durationMillis = 180),
        label = "Sensitivity mode selection",
    )
    val capsuleShape = RoundedCornerShape(50)
    val selectedShape = RoundedCornerShape(50)

    Box(
        modifier = modifier.width(halfWidth * 2).height(48.dp)
            .clearAndSetSemantics {
                role = Role.Switch
                toggleableState = ToggleableState(automatic)
                contentDescription = modeDescription
                stateDescription = if (automatic) autoLabel else manualLabel
                onClick(label = switchAction) {
                    onAutomaticChange(!automatic)
                    true
                }
            }
            .clip(capsuleShape)
            .clickable { onAutomaticChange(!automatic) },
    ) {
        Box(Modifier.matchParentSize().background(Card, capsuleShape))
        Box(
            Modifier.offset(x = selectedOffset, y = 2.dp)
                .width(halfWidth - 4.dp).height(44.dp)
                .background(MaterialTheme.colorScheme.secondaryContainer, selectedShape)
                .border(BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary), selectedShape),
        )
        Row(Modifier.matchParentSize()) {
            listOf(true to autoLabel, false to manualLabel).forEach { (mode, label) ->
                Box(Modifier.width(halfWidth).fillMaxHeight(), contentAlignment = Alignment.Center) {
                    Text(
                        label,
                        modifier = Modifier.clearAndSetSemantics { },
                        style = textStyle,
                        fontWeight = if (automatic == mode) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (automatic == mode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
        }
        Box(Modifier.matchParentSize().border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), capsuleShape))
    }
}

@Composable
private fun SensitivitySlider(
    effectiveThreshold: Int,
    audioLevel: AudioLevel?,
    automatic: Boolean,
    onAutomaticChange: (Boolean) -> Unit,
    onThresholdChange: (Int) -> Unit,
    modifier: Modifier,
) {
    val thresholdDescription = stringResource(R.string.threshold_label)
    Box(modifier) {
        Slider(
            value = effectiveThreshold.toFloat(),
            onValueChange = {
                if (automatic) onAutomaticChange(false)
                onThresholdChange(it.roundToInt())
            },
            valueRange = -80f..-20f,
            modifier = Modifier.fillMaxWidth().align(Alignment.Center).semantics {
                contentDescription = thresholdDescription
            },
        )
        audioLevel?.let { level ->
            Canvas(Modifier.fillMaxSize()) {
                val inset = 12.dp.toPx()
                val x = when {
                    level.inputDbfs < -80 -> 4.dp.toPx()
                    level.inputDbfs > -20 -> size.width - 4.dp.toPx()
                    else -> inset + ((level.inputDbfs + 80.0) / 60.0).toFloat() * (size.width - inset * 2)
                }
                drawLine(
                    color = Color(0xFFF0F3F4),
                    start = Offset(x, size.height / 2 - 9.dp.toPx()),
                    end = Offset(x, size.height / 2 + 9.dp.toPx()),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
        }
    }
}

@Composable
private fun ThresholdInput(
    value: String,
    invalid: Boolean,
    wide: Boolean,
    onValueChange: (String) -> Unit,
    onFocus: () -> Unit,
) {
    val fieldDescription = stringResource(R.string.threshold_label)
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.width(if (wide) 94.dp else 70.dp)
                .onFocusChanged { if (it.isFocused) onFocus() }
                .semantics { contentDescription = fieldDescription },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = invalid,
        )
        Text(
            "dBFS",
            modifier = Modifier.padding(start = 6.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun SettingsScreen(
    referenceA4Hz: Int,
    presets: List<Int>,
    language: String,
    onBack: () -> Unit,
    onReferenceChange: (Int) -> Unit,
    onSavePreset: () -> Unit,
    onRemovePreset: (Int) -> Unit,
    onLanguageChange: (String) -> Unit,
) {
    val fontScale = LocalDensity.current.fontScale
    val pagerState = rememberPagerState(initialPage = referenceA4Hz - 400, pageCount = { 81 })
    val scope = rememberCoroutineScope()
    val latestReference by rememberUpdatedState(referenceA4Hz)
    var showReferenceDialog by remember { mutableStateOf(false) }
    var referenceText by remember { mutableStateOf(referenceA4Hz.toString()) }
    LaunchedEffect(pagerState.settledPage) {
        val value = pagerState.settledPage + 400
        if (value != latestReference) onReferenceChange(value)
    }
    LaunchedEffect(referenceA4Hz) {
        val page = referenceA4Hz - 400
        if (pagerState.currentPage != page) pagerState.scrollToPage(page)
    }

    Column(
        Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()
            .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
    ) {
        Row(Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            val backDescription = stringResource(R.string.back)
            TextButton(onClick = onBack, modifier = Modifier.semantics { contentDescription = backDescription }) {
                Text("‹", fontSize = 36.sp)
            }
            Text(stringResource(R.string.settings), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(28.dp))
        Text(stringResource(R.string.reference_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Text(stringResource(R.string.reference_hint), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
        Spacer(Modifier.height(18.dp))
        VerticalPager(
            state = pagerState,
            pageSize = PageSize.Fixed(if (fontScale >= 1.5f) 72.dp else if (fontScale >= 1.3f) 62.dp else 54.dp),
            contentPadding = PaddingValues(vertical = if (fontScale >= 1.5f) 80.dp else if (fontScale >= 1.3f) 70.dp else 64.dp),
            pageSpacing = 8.dp,
            flingBehavior = PagerDefaults.flingBehavior(
                state = pagerState,
                pagerSnapDistance = PagerSnapDistance.atMost(3),
            ),
            modifier = Modifier.fillMaxWidth().height(if (fontScale >= 1.5f) 232.dp else if (fontScale >= 1.3f) 202.dp else 182.dp),
        ) { page ->
            val selected = pagerState.currentPage == page
            val referenceDescription = stringResource(
                if (selected) R.string.reference_selected_description else R.string.reference_option_description,
                page + 400,
            )
            Surface(
                onClick = {
                    if (selected) {
                        referenceText = (page + 400).toString()
                        showReferenceDialog = true
                    } else onReferenceChange(page + 400)
                },
                shape = RoundedCornerShape(12.dp),
                color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else Card,
                modifier = Modifier.fillMaxSize().semantics {
                    contentDescription = referenceDescription
                    this.selected = selected
                },
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "${page + 400} Hz",
                        fontSize = if (selected) 24.sp else 18.sp,
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            val previousDescription = stringResource(R.string.previous_frequency)
            val nextDescription = stringResource(R.string.next_frequency)
            TextButton(onClick = { scope.launch { pagerState.animateScrollToPage((pagerState.currentPage - 1).coerceAtLeast(0)) } }, enabled = referenceA4Hz > 400,
                modifier = Modifier.widthIn(min = 96.dp).heightIn(min = 48.dp).semantics { contentDescription = previousDescription }) {
                Text("− 1 Hz")
            }
            Spacer(Modifier.width(24.dp))
            TextButton(onClick = { scope.launch { pagerState.animateScrollToPage((pagerState.currentPage + 1).coerceAtMost(80)) } }, enabled = referenceA4Hz < 480,
                modifier = Modifier.widthIn(min = 96.dp).heightIn(min = 48.dp).semantics { contentDescription = nextDescription }) {
                Text("+ 1 Hz")
            }
        }
        Spacer(Modifier.height(20.dp))
        Text(stringResource(R.string.saved_presets, presets.size), style = MaterialTheme.typography.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
            presets.forEach { preset ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    FilterChip(
                        selected = referenceA4Hz == preset,
                        onClick = { onReferenceChange(preset) },
                        label = { Text("$preset Hz") },
                    )
                    if (preset != 440) {
                        val removeDescription = stringResource(R.string.remove_preset, preset)
                        TextButton(onClick = { onRemovePreset(preset) }, modifier = Modifier.heightIn(min = 48.dp).semantics { contentDescription = removeDescription }) {
                            Text("×", fontSize = 20.sp)
                        }
                    }
                }
            }
        }
        Button(
            onClick = onSavePreset,
            enabled = referenceA4Hz !in presets && presets.size < 3,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        ) { Text(stringResource(R.string.save_preset)) }
        if (referenceA4Hz in presets) {
            Text(stringResource(R.string.preset_already_saved), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
        }
        if (presets.size == 3 && referenceA4Hz !in presets) {
            Text(stringResource(R.string.preset_limit), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
        }
        Spacer(Modifier.height(36.dp))
        Text(stringResource(R.string.language), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Text(stringResource(R.string.language_hint), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 12.dp)) {
            listOf("pt" to "Português", "en" to "English", "fr" to "Français").forEach { (code, label) ->
                FilterChip(selected = language == code, onClick = { onLanguageChange(code) }, label = { Text(label) })
            }
        }
        Spacer(Modifier.height(32.dp))
    }
    if (showReferenceDialog) {
        val typedValue = referenceText.toIntOrNull()
        val focusRequester = remember { FocusRequester() }
        val keyboardController = LocalSoftwareKeyboardController.current
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
        AlertDialog(
            onDismissRequest = { showReferenceDialog = false },
            title = { Text(stringResource(R.string.reference_edit)) },
            text = {
                OutlinedTextField(
                    value = referenceText,
                    onValueChange = { typed ->
                        if (typed.length <= 3 && typed.all(Char::isDigit)) referenceText = typed
                    },
                    label = { Text("400–480 Hz") },
                    modifier = Modifier.focusRequester(focusRequester),
                    suffix = { Text("Hz") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = typedValue !in 400..480,
                )
            },
            confirmButton = {
                TextButton(
                    enabled = typedValue in 400..480,
                    onClick = {
                        typedValue?.let(onReferenceChange)
                        showReferenceDialog = false
                    },
                ) { Text(stringResource(R.string.apply)) }
            },
            dismissButton = {
                TextButton(onClick = { showReferenceDialog = false }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }
}

private fun formatHz(value: Double, locale: Locale): String = String.format(locale, "%.1f Hz", value)
