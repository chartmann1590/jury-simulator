package com.charles.jurysim.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = CourtBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF3B82F6),
    onPrimaryContainer = Color.White,

    secondary = CourtGold,
    onSecondary = DarkGray,
    secondaryContainer = CourtGoldDark,
    onSecondaryContainer = Color.White,

    tertiary = SuccessGreen,
    onTertiary = Color.White,

    error = ErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF991B1B),

    background = LightGray,
    onBackground = DarkGray,

    surface = Color.White,
    onSurface = DarkGray,
    surfaceVariant = MediumGray,
    onSurfaceVariant = Color(0xFF64748B),

    outline = Color(0xFFCBD5E1)
)

@Composable
fun JurySimulatorTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
