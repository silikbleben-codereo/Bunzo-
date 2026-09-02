package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = BunzoPrimary,
    onPrimary = BunzoDark,
    primaryContainer = BunzoPrimaryContainer,
    onPrimaryContainer = BunzoOnPrimaryContainer,
    secondary = BunzoDark,
    onSecondary = Color.White,
    secondaryContainer = BunzoSecondaryYellow,
    onSecondaryContainer = BunzoDark,
    tertiary = BunzoPrimaryDark,
    onTertiary = BunzoDark,
    background = BunzoBackground,
    onBackground = BunzoTextPrimary,
    surface = BunzoSurface,
    onSurface = BunzoTextPrimary,
    surfaceVariant = BunzoSurfaceVariant,
    onSurfaceVariant = BunzoTextSecondary,
    outline = BunzoBorder,
    outlineVariant = BunzoBorderLight,
    error = BunzoError,
    errorContainer = BunzoErrorContainer,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = BunzoPrimary,
    onPrimary = BunzoDark,
    primaryContainer = BunzoDarkSurfaceVariant,
    onPrimaryContainer = BunzoSecondaryYellow,
    secondary = BunzoSecondaryYellow,
    onSecondary = BunzoDark,
    secondaryContainer = BunzoDarkSurface,
    onSecondaryContainer = BunzoSecondaryYellow,
    tertiary = BunzoPrimaryLight,
    onTertiary = BunzoDark,
    background = BunzoDarkBackground,
    onBackground = BunzoDarkTextPrimary,
    surface = BunzoDarkSurface,
    onSurface = BunzoDarkTextPrimary,
    surfaceVariant = BunzoDarkSurfaceVariant,
    onSurfaceVariant = BunzoDarkTextSecondary,
    outline = BunzoDarkBorder,
    outlineVariant = BunzoDarkBorder,
    error = BunzoError,
    onError = Color.White
)

@Composable
fun BunzoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    BunzoTheme(darkTheme = darkTheme, content = content)
}

