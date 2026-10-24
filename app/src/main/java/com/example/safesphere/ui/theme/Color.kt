package com.example.safesphere.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val PrimaryPurple = Color(0xFF7B61FF)
val ElectricBlue = Color(0xFF00E5FF)
val SoftPink = Color(0xFFFF61D6)
val DeepPurple = Color(0xFF1A0B2E)
val DarkNavy = Color(0xFF0A0E21)
val DarkBackground = Color(0xFF0F0F0F)
val SurfaceGrey = Color(0xFF1E1E1E)
val LightGrey = Color(0xFFBDBDBD)
val White = Color(0xFFFFFFFF)
val ErrorRed = Color(0xFFFF5252)
val SafeGreen = Color(0xFF00C853)
val AlertYellow = Color(0xFFFFD600)

val BackgroundGradient = Brush.verticalGradient(
    colors = listOf(DarkBackground, DeepPurple, DarkNavy)
)

val PrimaryGradient = Brush.horizontalGradient(
    colors = listOf(PrimaryPurple, SoftPink)
)

val SOSGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFFFF416C), Color(0xFFFF4B2B))
)
