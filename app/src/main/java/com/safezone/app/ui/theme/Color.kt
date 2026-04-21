package com.safezone.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object SafeZoneColors {
    val BrandRed = Color(0xFFFF6B6B)
    val BrandRedDeep = Color(0xFFFF3B30)
    val BrandRedSoft = Color(0xFFFFB4B4)
    val BrandRedGlow = Color(0x33FF3B30)

    val BgBase = Color(0xFF0A0A0F)
    val BgElevated = Color(0xFF15151C)
    val BgCard = Color(0xFF1C1C24)
    val BgCardElevated = Color(0xFF232330)

    val TextPrimary = Color(0xFFF5F5F7)
    val TextSecondary = Color(0xFFA8A8B3)
    val TextMuted = Color(0xFF6B6B75)

    val AccentBlue = Color(0xFF6BA4FF)
    val AccentSafe = Color(0xFF4ADE80)
    val AccentWarn = Color(0xFFFFB020)
    val Divider = Color(0xFF2A2A35)

    val SosButtonGradient = Brush.radialGradient(
        colors = listOf(BrandRedSoft, BrandRed, BrandRedDeep),
        radius = 400f
    )
    val EngageGradient = Brush.horizontalGradient(
        colors = listOf(BrandRedSoft, BrandRed, BrandRedDeep)
    )
}
