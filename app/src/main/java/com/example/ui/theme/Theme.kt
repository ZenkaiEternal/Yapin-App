package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val YapinColorScheme = darkColorScheme(
    primary = YapinPurpleLight,
    onPrimary = TextWhite,
    primaryContainer = YapinPurple,
    onPrimaryContainer = TextWhite,
    secondary = YapinCyan,
    onSecondary = TextWhite,
    secondaryContainer = YapinCardBg,
    onSecondaryContainer = TextPrimary,
    tertiary = YapinPink,
    onTertiary = TextWhite,
    background = YapinChatBg,
    onBackground = TextPrimary,
    surface = YapinCardBg,
    onSurface = TextPrimary,
    surfaceVariant = YapinInputBg,
    onSurfaceVariant = TextSecondary,
    outline = YapinBorder
)

@Composable
fun YapinTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = YapinColorScheme,
        typography = Typography,
        content = content
    )
}
