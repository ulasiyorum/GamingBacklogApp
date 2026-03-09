package com.ulasiyorum.gamingbacklogapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = ElectricPurple,
    background = MidnightBlue,
    surface = SurfaceBlue,
    onPrimary = androidx.compose.ui.graphics.Color.Black,
    onBackground = SoftWhite,
    onSurface = SoftWhite
)

@Composable
fun GamingBacklogAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}