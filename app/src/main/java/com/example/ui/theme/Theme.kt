package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = AstraIndigoLight,
    onPrimary = Color(0xFF0F172A),
    primaryContainer = AstraIndigoContainer,
    onPrimaryContainer = AstraOnIndigoContainer,
    secondary = AstraCyanLight,
    onSecondary = Color(0xFF0F172A),
    secondaryContainer = AstraCyanContainer,
    onSecondaryContainer = AstraOnCyanContainer,
    tertiary = AstraRoseLight,
    background = AstraDeepNavy,
    onBackground = AstraTextPrimaryDark,
    surface = AstraDarkSurface,
    onSurface = AstraTextPrimaryDark,
    surfaceVariant = AstraDarkSurfaceVariant,
    onSurfaceVariant = AstraTextSecondaryDark,
    outline = AstraCardBorder
)

private val LightColorScheme = lightColorScheme(
    primary = AstraIndigoPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEEF2FF),
    onPrimaryContainer = AstraIndigoPrimary,
    secondary = AstraCyanSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF0369A1),
    tertiary = AstraRoseTertiary,
    background = AstraLightBackground,
    onBackground = AstraTextPrimaryLight,
    surface = AstraLightSurface,
    onSurface = AstraTextPrimaryLight,
    surfaceVariant = AstraLightSurfaceVariant,
    onSurfaceVariant = AstraTextSecondaryLight,
    outline = AstraLightCardBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to Astra's stunning cosmic dark theme
    dynamicColor: Boolean = false, // Keep Astra's unique celestial branding consistent
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
