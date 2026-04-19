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

// ── Semantic health colors (stable across themes) ──────────────────────────
val HeartRed      = Color(0xFFE53935)
val OxygenBlue    = Color(0xFF1565C0)
val StepsGreen    = Color(0xFF2E7D32)
val AlertOrange   = Color(0xFFE65100)
val CriticalRed   = Color(0xFFB71C1C)

// ── Light palette — Material Design 3, Teal-Cyan base ──────────────────────
// Primary: deep teal  |  Secondary: indigo  |  Tertiary: green
private val md3LightPrimary          = Color(0xFF006874)  // Deep teal
private val md3LightOnPrimary        = Color(0xFFFFFFFF)
private val md3LightPrimaryContainer = Color(0xFF97F0FF)  // Pale cyan
private val md3LightOnPrimaryContainer = Color(0xFF001F24)

private val md3LightSecondary          = Color(0xFF4A6267)
private val md3LightOnSecondary        = Color(0xFFFFFFFF)
private val md3LightSecondaryContainer = Color(0xFFCCE8ED)
private val md3LightOnSecondaryContainer = Color(0xFF051F23)

private val md3LightTertiary          = Color(0xFF525E7D)  // Muted indigo accent
private val md3LightOnTertiary        = Color(0xFFFFFFFF)
private val md3LightTertiaryContainer = Color(0xFFDAE2FF)
private val md3LightOnTertiaryContainer = Color(0xFF0E1B37)

private val md3LightBackground  = Color(0xFFF5FAFB)   // Very light cyan-white
private val md3LightSurface     = Color(0xFFFFFFFF)
private val md3LightSurfaceVariant = Color(0xFFDBE4E6)
private val md3LightOnSurfaceVariant = Color(0xFF3F484A)
private val md3LightOnBackground = Color(0xFF191C1D)
private val md3LightOnSurface    = Color(0xFF191C1D)
private val md3LightOutline      = Color(0xFF6F797A)
private val md3LightError        = Color(0xFFBA1A1A)
private val md3LightOnError      = Color(0xFFFFFFFF)
private val md3LightErrorContainer = Color(0xFFFFDAD6)

// ── Dark palette — deep navy/teal, crisp & accessible ──────────────────────
private val md3DarkPrimary          = Color(0xFF4FD8EB)  // Bright cyan
private val md3DarkOnPrimary        = Color(0xFF00363D)
private val md3DarkPrimaryContainer = Color(0xFF004F58)  // Dark teal container
private val md3DarkOnPrimaryContainer = Color(0xFF97F0FF)

private val md3DarkSecondary          = Color(0xFFB1CBD0)
private val md3DarkOnSecondary        = Color(0xFF1C3438)
private val md3DarkSecondaryContainer = Color(0xFF334B4F)
private val md3DarkOnSecondaryContainer = Color(0xFFCCE8ED)

private val md3DarkTertiary          = Color(0xFFBAC6EA)
private val md3DarkOnTertiary        = Color(0xFF24304D)
private val md3DarkTertiaryContainer = Color(0xFF3B4664)
private val md3DarkOnTertiaryContainer = Color(0xFFDAE2FF)

private val md3DarkBackground   = Color(0xFF0F1416)   // Almost-black dark navy
private val md3DarkSurface      = Color(0xFF191C1D)
private val md3DarkSurfaceVariant = Color(0xFF3F484A)
private val md3DarkOnSurfaceVariant = Color(0xFFBFC8CA)
private val md3DarkOnBackground  = Color(0xFFE1E3E3)
private val md3DarkOnSurface     = Color(0xFFE1E3E3)
private val md3DarkOutline       = Color(0xFF899294)
private val md3DarkError         = Color(0xFFFFB4AB)
private val md3DarkOnError       = Color(0xFF690005)
private val md3DarkErrorContainer = Color(0xFF93000A)

// card surface shorthands still used in screens
val CardSurface   = md3LightSurfaceVariant
val CardSurfaceDk = md3DarkSurfaceVariant
val PrimaryTeal   = md3LightPrimary

private val LightColors = lightColorScheme(
    primary                 = md3LightPrimary,
    onPrimary               = md3LightOnPrimary,
    primaryContainer        = md3LightPrimaryContainer,
    onPrimaryContainer      = md3LightOnPrimaryContainer,
    secondary               = md3LightSecondary,
    onSecondary             = md3LightOnSecondary,
    secondaryContainer      = md3LightSecondaryContainer,
    onSecondaryContainer    = md3LightOnSecondaryContainer,
    tertiary                = md3LightTertiary,
    onTertiary              = md3LightOnTertiary,
    tertiaryContainer       = md3LightTertiaryContainer,
    onTertiaryContainer     = md3LightOnTertiaryContainer,
    background              = md3LightBackground,
    onBackground            = md3LightOnBackground,
    surface                 = md3LightSurface,
    onSurface               = md3LightOnSurface,
    surfaceVariant          = md3LightSurfaceVariant,
    onSurfaceVariant        = md3LightOnSurfaceVariant,
    outline                 = md3LightOutline,
    error                   = md3LightError,
    onError                 = md3LightOnError,
    errorContainer          = md3LightErrorContainer
)

private val DarkColors = darkColorScheme(
    primary                 = md3DarkPrimary,
    onPrimary               = md3DarkOnPrimary,
    primaryContainer        = md3DarkPrimaryContainer,
    onPrimaryContainer      = md3DarkOnPrimaryContainer,
    secondary               = md3DarkSecondary,
    onSecondary             = md3DarkOnSecondary,
    secondaryContainer      = md3DarkSecondaryContainer,
    onSecondaryContainer    = md3DarkOnSecondaryContainer,
    tertiary                = md3DarkTertiary,
    onTertiary              = md3DarkOnTertiary,
    tertiaryContainer       = md3DarkTertiaryContainer,
    onTertiaryContainer     = md3DarkOnTertiaryContainer,
    background              = md3DarkBackground,
    onBackground            = md3DarkOnBackground,
    surface                 = md3DarkSurface,
    onSurface               = md3DarkOnSurface,
    surfaceVariant          = md3DarkSurfaceVariant,
    onSurfaceVariant        = md3DarkOnSurfaceVariant,
    outline                 = md3DarkOutline,
    error                   = md3DarkError,
    onError                 = md3DarkOnError,
    errorContainer          = md3DarkErrorContainer
)

val AppTypography = Typography(
    displayMedium  = TextStyle(fontWeight = FontWeight.Bold,    fontSize = 42.sp, letterSpacing = (-1).sp),
    headlineLarge  = TextStyle(fontWeight = FontWeight.Bold,    fontSize = 32.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 24.sp),
    headlineSmall  = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 20.sp),
    titleLarge     = TextStyle(fontWeight = FontWeight.Medium,  fontSize = 18.sp),
    titleMedium    = TextStyle(fontWeight = FontWeight.Medium,  fontSize = 16.sp),
    bodyLarge      = TextStyle(fontWeight = FontWeight.Normal,  fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium     = TextStyle(fontWeight = FontWeight.Normal,  fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge     = TextStyle(fontWeight = FontWeight.Medium,  fontSize = 14.sp),
    labelMedium    = TextStyle(fontWeight = FontWeight.Medium,  fontSize = 12.sp)
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
