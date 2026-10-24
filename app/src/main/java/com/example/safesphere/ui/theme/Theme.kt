package com.example.safesphere.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SafeSphereDarkColorScheme = darkColorScheme(
    primary = PrimaryPurple,
    secondary = SoftPink,
    background = DarkBackground,
    surface = SurfaceGrey,
    onPrimary = White,
    onSecondary = DarkBackground,
    onBackground = White,
    onSurface = White,
    error = ErrorRed
)

private val SafeSphereLightColorScheme = lightColorScheme(
    primary = PrimaryPurple,
    secondary = SoftPink,
    background = Color(0xFFF8F9FF), // Soft bluish-white for better engagement
    surface = Color.White,
    onPrimary = White,
    onSecondary = Color.Black,
    onBackground = Color(0xFF1A0B2E), // Deep purple-black for contrast
    onSurface = Color(0xFF1A0B2E),
    error = ErrorRed
)

@Composable
fun SAfeSphereTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) SafeSphereDarkColorScheme else SafeSphereLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
