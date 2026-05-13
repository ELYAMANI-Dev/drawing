package com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AppColorScheme = lightColorScheme(
    primary          = PrimaryBlue,
    onPrimary        = Color.White,
    primaryContainer = PrimaryBlueDim,
    onPrimaryContainer = Color(0xFF0D2444),
    background       = BackgroundLight,
    onBackground     = TextPrimary,
    surface          = SurfaceLight,
    onSurface        = TextPrimary,
    surfaceVariant   = Color(0xFFE0E0E0),
    onSurfaceVariant = TextSecondary,
    outline          = BorderStrong,
    error            = ErrorRed,
    onError          = Color.White,
)

@Composable
fun DrawingStepsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography  = Typography,
        content     = content
    )
}
