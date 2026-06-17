package com.example.ramcpurestorer.theme

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
    primary = ElectricCyan,
    onPrimary = ObsidianBlack,
    secondary = SolarOrange,
    onSecondary = Color.White,
    background = ObsidianBlack,
    surface = SlateNavy,
    onBackground = Color.White,
    onSurface = Color.White,
    tertiary = HealthyGreen
)

private val LightColorScheme = lightColorScheme(
    primary = ElectricCyan,
    onPrimary = ObsidianBlack,
    secondary = SolarOrange,
    onSecondary = Color.White,
    background = ObsidianBlack, // Keep it dark for premium look
    surface = SlateNavy,
    onBackground = Color.White,
    onSurface = Color.White,
    tertiary = HealthyGreen
)

@Composable
fun RamCpuRestorerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set to false to enforce our customized premium palette
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
