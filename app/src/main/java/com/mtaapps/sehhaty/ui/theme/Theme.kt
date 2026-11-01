package com.mtaapps.sehhaty.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF2DD4BF),       // Teal 400
    onPrimary = Color(0xFF0F172A),     // Dark Slate
    primaryContainer = Color(0xFF134E4A),
    onPrimaryContainer = Color(0xFFCCFBF1),
    secondary = Color(0xFF38BDF8),     // Sky Blue
    onSecondary = Color(0xFF0F172A),
    tertiary = Color(0xFFFBBF24),      // Amber
    onTertiary = Color(0xFF0F172A),
    background = Color(0xFF0B132B),    // Midnight Slate
    onBackground = Color(0xFFF8FAFC),  // White text
    surface = Color(0xFF1C2541),       // Elevated dark card
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF3A506B),
    onSurfaceVariant = Color(0xFFCBD5E1),
    error = Color(0xFFF87171)
)

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
    surface = Color.White,
    onSurface = TextDark,
    surfaceVariant = BgSubtle,
    onSurfaceVariant = TextMuted,
    error = StatusDanger
)

/**
 * Sehhaty Theme:
 * Dynamically switches between Light and Dark mode based on device system settings.
 */
@Composable
fun SehhatyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
