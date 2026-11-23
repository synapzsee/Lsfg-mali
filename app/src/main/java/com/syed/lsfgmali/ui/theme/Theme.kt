package com.syed.lsfgmali.ui.theme

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

private val Teal = Color(0xFF00B3A4)
private val TealDark = Color(0xFF00332F)
private val Blue = Color(0xFF4C6FFF)

private val DarkScheme = darkColorScheme(
    primary = Teal,
    secondary = Blue,
    tertiary = Color(0xFF9AD9CF),
    background = Color(0xFF0B0F10),
    surface = Color(0xFF12181A),
    surfaceVariant = Color(0xFF1B2427),
)

private val LightScheme = lightColorScheme(
    primary = Color(0xFF006A61),
    secondary = Color(0xFF3A4C9B),
    tertiary = Color(0xFF00696B),
)

@Composable
fun LsfgTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colors = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> DarkScheme
        else -> LightScheme
    }
    MaterialTheme(colorScheme = colors, content = content)
}
