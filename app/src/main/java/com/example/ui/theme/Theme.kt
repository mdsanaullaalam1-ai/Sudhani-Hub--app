package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class SudhaniColors(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val cardBackground: Color,
    val cardBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val primaryButton: Color,
    val onPrimaryButton: Color,
    val secondaryButton: Color,
    val onSecondaryButton: Color,
    val goldAccent: Color,
    val goldLight: Color,
    val navyAccent: Color,
    val divider: Color,
    val inputBackground: Color,
    val inputBorder: Color,
    val chipBackground: Color,
    val chipBorder: Color,
    val bottomBarBackground: Color,
    val bottomBarSelected: Color,
    val bottomBarUnselected: Color,
    val bottomBarIndicator: Color
)

val LightSudhaniColors = SudhaniColors(
    isDark = false,
    background = Color(0xFFF8FAFC),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF1F5F9), // very light gray cards and sections
    cardBackground = Color(0xFFFFFFFF),
    cardBorder = Color(0xFFE2E8F0),
    textPrimary = Color(0xFF070E4E), // Dark navy text
    textSecondary = Color(0xFF475569), // Slate 600
    textMuted = Color(0xFF94A3B8), // Slate 400
    primaryButton = Color(0xFFF5A623), // Sudhanihub gold/yellow
    onPrimaryButton = Color(0xFF070E4E), // Dark navy on gold
    secondaryButton = Color(0xFF070E4E),
    onSecondaryButton = Color.White,
    goldAccent = Color(0xFFF5A623),
    goldLight = Color(0xFFFFF8EA),
    navyAccent = Color(0xFF070E4E),
    divider = Color(0xFFF1F5F9),
    inputBackground = Color(0xFFF1F5F9),
    inputBorder = Color(0xFFE2E8F0),
    chipBackground = Color(0xFFEEF1FA),
    chipBorder = Color(0xFFDFE4F7),
    bottomBarBackground = Color(0xFFFFFFFF),
    bottomBarSelected = Color(0xFF070E4E),
    bottomBarUnselected = Color(0xFF94A3B8),
    bottomBarIndicator = Color(0xFFFFF8EA)
)

val DarkSudhaniColors = SudhaniColors(
    isDark = true,
    background = Color(0xFF060914), // Deep black / dark navy background
    surface = Color(0xFF0F162E), // Cards slightly lighter than main background
    surfaceVariant = Color(0xFF16203D), // Slightly lighter section/chip
    cardBackground = Color(0xFF0F162E),
    cardBorder = Color(0xFF1E2A4B),
    textPrimary = Color(0xFFFFFFFF), // White text
    textSecondary = Color(0xFF94A3B8), // Light gray secondary text
    textMuted = Color(0xFF64748B),
    primaryButton = Color(0xFFF5A623), // Sudhanihub gold/yellow
    onPrimaryButton = Color(0xFF070E4E), // Dark navy on gold
    secondaryButton = Color(0xFF16203D),
    onSecondaryButton = Color(0xFFFFFFFF),
    goldAccent = Color(0xFFF5A623),
    goldLight = Color(0xFF2A1F08),
    navyAccent = Color(0xFFF5A623), // Gold highlights in dark mode
    divider = Color(0xFF1B243E),
    inputBackground = Color(0xFF16203D),
    inputBorder = Color(0xFF253358),
    chipBackground = Color(0xFF16203D),
    chipBorder = Color(0xFF253358),
    bottomBarBackground = Color(0xFF090E20),
    bottomBarSelected = Color(0xFFF5A623), // Active gold icon in dark mode
    bottomBarUnselected = Color(0xFF64748B),
    bottomBarIndicator = Color(0xFF242E4C)
)

val LocalSudhaniColors = staticCompositionLocalOf { LightSudhaniColors }

object SudhaniTheme {
    val colors: SudhaniColors
        @Composable
        @ReadOnlyComposable
        get() = LocalSudhaniColors.current

    val isDark: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalSudhaniColors.current.isDark
}

private val LightColorScheme = lightColorScheme(
    primary = SudhaniGoldPrimary,
    onPrimary = SudhaniNavyPrimary,
    primaryContainer = SudhaniGoldLight,
    onPrimaryContainer = SudhaniGoldDark,
    secondary = SudhaniNavyPrimary,
    onSecondary = Color.White,
    secondaryContainer = SudhaniNavyLight,
    onSecondaryContainer = SudhaniNavyDark,
    tertiary = HighDensityEmerald,
    onTertiary = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = SudhaniNavyPrimary,
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFE2E8F0),
    outlineVariant = Color(0xFFCBD5E1),
    error = SudhaniRedDiscount,
    errorContainer = SudhaniRedLight,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = SudhaniGoldPrimary,
    onPrimary = SudhaniNavyPrimary,
    primaryContainer = Color(0xFF2A1F08),
    onPrimaryContainer = SudhaniGoldPrimary,
    secondary = SudhaniGoldPrimary,
    onSecondary = SudhaniNavyPrimary,
    secondaryContainer = Color(0xFF16203D),
    onSecondaryContainer = SudhaniGoldPrimary,
    tertiary = HighDensityEmerald,
    onTertiary = Color.White,
    background = Color(0xFF060914), // Deep black / dark navy background
    onBackground = Color(0xFFFFFFFF), // White text
    surface = Color(0xFF0F162E), // Cards slightly lighter than main background
    onSurface = Color(0xFFFFFFFF), // White text
    surfaceVariant = Color(0xFF16203D), // Slightly lighter section
    onSurfaceVariant = Color(0xFF94A3B8), // Light gray secondary text
    outline = Color(0xFF1E2A4B),
    outlineVariant = Color(0xFF253358),
    error = SudhaniRedDiscount,
    errorContainer = Color(0xFF3B1212),
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val sudhaniColors = if (darkTheme) DarkSudhaniColors else LightSudhaniColors
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(
        LocalSudhaniColors provides sudhaniColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
