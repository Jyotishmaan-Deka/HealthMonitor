package com.healthmonitor.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Typography

// ── Colors ──────────────────────────────────────────────────────────────
val HeartRed      = Color(0xFFE53935)
val OxygenBlue    = Color(0xFF1E88E5)
val StepsGreen    = Color(0xFF43A047)
val AlertOrange   = Color(0xFFFF8F00)
val CriticalRed   = Color(0xFFB71C1C)
val CardSurface   = Color(0xFFF5F7FA)
val CardSurfaceDk = Color(0xFF1E2430)
val PrimaryPurple = Color(0xFF5C6BC0)

private val LightColors = lightColorScheme(
    primary          = PrimaryPurple,
    onPrimary        = Color.White,
    primaryContainer = Color(0xFFE8EAF6),
    secondary        = OxygenBlue,
    onSecondary      = Color.White,
    tertiary         = StepsGreen,
    background       = Color(0xFFF0F2F5),
    surface          = Color.White,
    surfaceVariant   = CardSurface,
    error            = HeartRed,
    onBackground     = Color(0xFF1A1C2E),
    onSurface        = Color(0xFF1A1C2E)
)

private val DarkColors = darkColorScheme(
    primary          = Color(0xFF9FA8DA),
    onPrimary        = Color(0xFF1A237E),
    primaryContainer = Color(0xFF283593),
    secondary        = Color(0xFF64B5F6),
    onSecondary      = Color(0xFF0D47A1),
    tertiary         = Color(0xFF81C784),
    background       = Color(0xFF0F1218),
    surface          = Color(0xFF161B25),
    surfaceVariant   = CardSurfaceDk,
    error            = Color(0xFFEF9A9A),
    onBackground     = Color(0xFFE8EAF6),
    onSurface        = Color(0xFFE8EAF6)
)

val AppTypography = Typography(
    displayMedium = TextStyle(fontWeight = FontWeight.Bold,   fontSize = 42.sp, letterSpacing = (-1).sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 24.sp),
    headlineSmall  = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 20.sp),
    titleLarge     = TextStyle(fontWeight = FontWeight.Medium, fontSize = 18.sp),
    titleMedium    = TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp),
    bodyLarge      = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium     = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge     = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp),
    labelMedium    = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp)
)

@Composable
fun HealthMonitorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography  = AppTypography,
        content     = content
    )
}
