package com.dokonhisob.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PrimaryGreen = Color(0xFF1B5E20)
private val PrimaryGreenLight = Color(0xFF4C8C4A)
private val AccentAmber = Color(0xFFFFC107)
private val ErrorRed = Color(0xFFB3261E)

private val LightColors = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = Color.White,
    secondary = AccentAmber,
    onSecondary = Color.Black,
    error = ErrorRed,
    background = Color(0xFFF7F8F5),
    surface = Color.White
)

private val DarkColors = darkColorScheme(
    primary = PrimaryGreenLight,
    onPrimary = Color.White,
    secondary = AccentAmber,
    onSecondary = Color.Black,
    error = Color(0xFFFFB4AB),
    background = Color(0xFF121412),
    surface = Color(0xFF1C1F1C)
)

@Composable
fun DokonHisobKitobTheme(
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
