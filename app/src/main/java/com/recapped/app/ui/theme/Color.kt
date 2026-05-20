package com.recapped.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object RecappedColors {
    val Background    = Color(0xFF080808)
    val Surface       = Color(0x0DFFFFFF)   // 5% white
    val SurfaceBright = Color(0x14FFFFFF)   // 8% white
    val Border        = Color(0x17FFFFFF)
    val BorderBright  = Color(0x29FFFFFF)
    val OnSurface     = Color(0xFFFFFFFF)
    val Muted         = Color(0x73FFFFFF)
    val Dim           = Color(0x38FFFFFF)

    val BrandRed    = Color(0xFFCC1500)
    val BrandOrange = Color(0xFFE85A00)
    val BrandGold   = Color(0xFFD4A017)
    val Spotify     = Color(0xFF1DB954)
    val Error       = Color(0xFFCC1500)
    val Success     = Color(0xFF4CAF50)
}

val BrandGradient: Brush
    get() = Brush.linearGradient(
        0.0f to RecappedColors.BrandRed,
        0.55f to RecappedColors.BrandOrange,
        1.0f to RecappedColors.BrandGold
    )
