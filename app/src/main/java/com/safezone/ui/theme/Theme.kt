package com.safezone.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ─── SafeZone Color Palette ──────────────────────────────────────────────────
object SafeZoneColors {
    val OrangeCore      = Color(0xFFFF8C00)
    val OrangeLight     = Color(0xFFFFAA33)
    val OrangeDim       = Color(0xFF8B4800)
    val OrangeGlow      = Color(0x33FF8C00)

    val BackgroundDark  = Color(0xFF0A0A0A)
    val SurfaceDark     = Color(0xFF121212)
    val SurfaceCard     = Color(0xFF1A1A1A)
    val SurfaceElevated = Color(0xFF222222)

    val TextPrimary     = Color(0xFFFFFFFF)
    val TextSecondary   = Color(0xFFAAAAAA)
    val TextDim         = Color(0xFF666666)
    val TextOrange      = Color(0xFFFF8C00)

    val StatusGreen     = Color(0xFF00FF88)
    val StatusBlue      = Color(0xFF4488FF)
    val StatusOrange    = Color(0xFFFF8C00)
    val StatusRed       = Color(0xFFFF3333)

    val DangerRed       = Color(0xFFCC0000)
    val DangerSurface   = Color(0xFF1A0000)

    val Divider         = Color(0xFF2A2A2A)
    val OverlayDark     = Color(0xCC000000)
}

// ─── Dark Color Scheme ───────────────────────────────────────────────────────
private val SafeZoneDarkColorScheme = darkColorScheme(
    primary          = SafeZoneColors.OrangeCore,
    onPrimary        = Color.Black,
    primaryContainer = SafeZoneColors.OrangeDim,
    secondary        = SafeZoneColors.OrangeLight,
    onSecondary      = Color.Black,
    background       = SafeZoneColors.BackgroundDark,
    onBackground     = SafeZoneColors.TextPrimary,
    surface          = SafeZoneColors.SurfaceDark,
    onSurface        = SafeZoneColors.TextPrimary,
    surfaceVariant   = SafeZoneColors.SurfaceCard,
    onSurfaceVariant = SafeZoneColors.TextSecondary,
    error            = SafeZoneColors.StatusRed,
    onError          = Color.White,
    outline          = SafeZoneColors.Divider
)

// ─── Typography ───────────────────────────────────────────────────────────────
val SafeZoneTypography = Typography(
    displayLarge = TextStyle(
        fontWeight = FontWeight.Black,
        fontSize   = 48.sp,
        letterSpacing = (-1).sp
    ),
    displayMedium = TextStyle(
        fontWeight = FontWeight.Black,
        fontSize   = 36.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize   = 28.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize   = 22.sp,
        letterSpacing = 0.5.sp
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize   = 18.sp,
        letterSpacing = 1.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize   = 16.sp,
        letterSpacing = 2.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize   = 14.sp,
        letterSpacing = 1.5.sp
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize   = 12.sp,
        letterSpacing = 2.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize   = 16.sp,
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize   = 14.sp,
        letterSpacing = 0.sp
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize   = 12.sp,
        letterSpacing = 0.25.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize   = 13.sp,
        letterSpacing = 2.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize   = 11.sp,
        letterSpacing = 1.5.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize   = 10.sp,
        letterSpacing = 1.sp
    )
)

// ─── Theme Composable ─────────────────────────────────────────────────────────
@Composable
fun SafeZoneTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SafeZoneDarkColorScheme,
        typography  = SafeZoneTypography,
        content     = content
    )
}
