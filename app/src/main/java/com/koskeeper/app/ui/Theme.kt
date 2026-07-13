package com.koskeeper.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Modern Emerald + Teal palette
private val Emerald50 = Color(0xFFECFDF5)
private val Emerald100 = Color(0xFFD1FAE5)
private val Emerald400 = Color(0xFF34D399)
private val Emerald500 = Color(0xFF10B981)
private val Emerald600 = Color(0xFF059669)
private val Emerald700 = Color(0xFF047857)
private val Emerald800 = Color(0xFF065F46)
private val Emerald900 = Color(0xFF064E3B)

private val Teal50 = Color(0xFFF0FDFA)
private val Teal400 = Color(0xFF2DD4BF)
private val Teal500 = Color(0xFF14B8A6)
private val Teal600 = Color(0xFF0D9488)

private val Slate50 = Color(0xFFF8FAFC)
private val Slate100 = Color(0xFFF1F5F9)
private val Slate600 = Color(0xFF475569)
private val Slate800 = Color(0xFF1E293B)
private val Slate900 = Color(0xFF0F172A)

private val Red400 = Color(0xFFF87171)
private val Red100 = Color(0xFFFEE2E2)

private val LightColorScheme = lightColorScheme(
    primary = Emerald600,
    onPrimary = Color.White,
    primaryContainer = Emerald100,
    onPrimaryContainer = Emerald900,
    secondary = Teal500,
    onSecondary = Color.White,
    secondaryContainer = Teal50,
    onSecondaryContainer = Slate800,
    background = Slate50,
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate600,
    error = Red400,
    onError = Color.White,
    errorContainer = Red100,
    onErrorContainer = Color(0xFF410002)
)

private val DarkColorScheme = darkColorScheme(
    primary = Emerald400,
    onPrimary = Emerald900,
    primaryContainer = Emerald800,
    onPrimaryContainer = Emerald100,
    secondary = Teal400,
    onSecondary = Slate900,
    secondaryContainer = Color(0xFF134E4A),
    onSecondaryContainer = Teal50,
    background = Slate900,
    onBackground = Slate50,
    surface = Slate800,
    onSurface = Slate50,
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFF94A3B8),
    error = Red400,
    onError = Slate900,
    errorContainer = Color(0xFF4C1D1D),
    onErrorContainer = Red100
)

@Composable
fun KosKeeperTheme(
    isDarkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (isDarkTheme) DarkColorScheme else LightColorScheme,
        content = content
    )
}
