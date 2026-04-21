package com.safezone.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val SafeZoneDarkColors = darkColorScheme(
    primary = SafeZoneColors.BrandRed,
    onPrimary = SafeZoneColors.BgBase,
    secondary = SafeZoneColors.AccentBlue,
    onSecondary = SafeZoneColors.TextPrimary,
    background = SafeZoneColors.BgBase,
    onBackground = SafeZoneColors.TextPrimary,
    surface = SafeZoneColors.BgCard,
    onSurface = SafeZoneColors.TextPrimary,
    surfaceVariant = SafeZoneColors.BgCardElevated,
    onSurfaceVariant = SafeZoneColors.TextSecondary,
    error = SafeZoneColors.BrandRedDeep,
    outline = SafeZoneColors.Divider
)

@Composable
fun SafeZoneTheme(
    @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // SafeZone is always dark — emergency-tech aesthetic
    MaterialTheme(
        colorScheme = SafeZoneDarkColors,
        typography = SafeZoneTypography,
        content = content
    )
}
