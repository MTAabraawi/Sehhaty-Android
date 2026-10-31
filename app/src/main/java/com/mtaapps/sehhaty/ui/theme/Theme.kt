package com.mtaapps.sehhaty.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = BrandTeal,
    onPrimary = Color.White,
    primaryContainer = BgContainerTeal,
    onPrimaryContainer = BrandNavy,
    secondary = BrandCyan,
    onSecondary = Color.White,
    tertiary = BrandAmber,
    onTertiary = Color.White,
    background = BgCanvas,
    onBackground = TextDark,
    surface = BgCard,
    onSurface = TextDark,
    surfaceVariant = BgSubtle,
    onSurfaceVariant = TextMuted,
    error = StatusDanger
)

@Composable
fun SehhatyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography(),
        content = content
    )
}
