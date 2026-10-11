package com.example.afinador.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val scheme = darkColorScheme(
    primary = Color(0xFF45D5CF),
    onPrimary = Color(0xFF003735),
    background = Color(0xFF101316),
    onBackground = Color(0xFFF0F3F4),
    surface = Color(0xFF181D21),
    onSurface = Color(0xFFF0F3F4),
    onSurfaceVariant = Color(0xFFB8C3C7),
    secondaryContainer = Color(0xFF1C4B4A),
    onSecondaryContainer = Color(0xFFBDF4F1),
    outline = Color(0xFF8A979D),
    outlineVariant = Color(0xFF374247),
)

@Composable
fun TunerTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = scheme, content = content)
}
