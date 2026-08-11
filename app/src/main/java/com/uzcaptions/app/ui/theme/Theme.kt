package com.uzcaptions.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AccentOrange = Color(0xFFFF7A50)
private val AccentOrangeLight = Color(0xFFFF9E7A)

private val DarkColors = darkColorScheme(
    primary = AccentOrangeLight,
    onPrimary = Color.Black,
    secondary = Color(0xFF00E5FF),
    onSecondary = Color.Black,
    background = Color(0xFF0E0E10),
    surface = Color(0xFF1A1A1D),
    error = Color(0xFFFFB4AB)
)

private val LightColors = lightColorScheme(
    primary = AccentOrange,
    onPrimary = Color.White,
    secondary = Color(0xFF0097A7),
    onSecondary = Color.White,
    background = Color(0xFFFAFAFA),
    surface = Color.White,
    error = Color(0xFFB3261E)
)

@Composable
fun UzCaptionsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}
