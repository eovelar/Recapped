package com.recapped.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val Display = FontFamily.SansSerif // En el final podés agregar Unbounded vía downloadable fonts
private val Heading = FontFamily.SansSerif
private val Body    = FontFamily.SansSerif

val RecappedTypography = Typography(
    displayLarge  = TextStyle(fontFamily = Display, fontWeight = FontWeight.Bold, fontSize = 30.sp, letterSpacing = (-1.2).sp),
    headlineLarge = TextStyle(fontFamily = Heading, fontWeight = FontWeight.Bold, fontSize = 24.sp, letterSpacing = (-0.6).sp),
    headlineMedium = TextStyle(fontFamily = Heading, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, letterSpacing = (-0.5).sp),
    titleLarge    = TextStyle(fontFamily = Heading, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, letterSpacing = (-0.3).sp),
    titleMedium   = TextStyle(fontFamily = Body, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    bodyLarge     = TextStyle(fontFamily = Body, fontWeight = FontWeight.Normal, fontSize = 14.sp),
    bodyMedium    = TextStyle(fontFamily = Body, fontWeight = FontWeight.Normal, fontSize = 13.sp),
    bodySmall     = TextStyle(fontFamily = Body, fontWeight = FontWeight.Normal, fontSize = 11.sp),
    labelLarge    = TextStyle(fontFamily = Body, fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
    labelSmall    = TextStyle(fontFamily = Body, fontWeight = FontWeight.SemiBold, fontSize = 10.sp, letterSpacing = 1.sp)
)
