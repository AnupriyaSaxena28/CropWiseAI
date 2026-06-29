package com.cropwise.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Green,
    onPrimary = Color.White,
    primaryContainer = GreenLight,
    secondary = Amber,
    tertiary = Blue,
    background = Surface,
    surface = Color.White,
    error = Rose,
)

private val DarkColors = darkColorScheme(
    primary = GreenLight,
    onPrimary = Color.Black,
    primaryContainer = GreenDark,
    secondary = Amber,
    tertiary = Blue,
    background = Color(0xFF0F1A0F),
    surface = Color(0xFF16241A),
    error = Rose,
)

@Composable
fun CropWiseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography(),
        content = content,
    )
}
