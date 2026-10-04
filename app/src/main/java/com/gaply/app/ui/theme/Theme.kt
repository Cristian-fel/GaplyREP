package com.gaply.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val GaplyLightColors = lightColorScheme(
    primary = BrandGreen,
    onPrimary = SurfaceWhite,
    primaryContainer = Mint,
    onPrimaryContainer = BrandGreenDark,
    secondary = Sage,
    onSecondary = BrandGreenDark,
    secondaryContainer = Mint,
    onSecondaryContainer = BrandGreenDark,
    tertiary = BrandGreenLight,
    onTertiary = SurfaceWhite,
    background = BackgroundApp,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = Mint,
    onSurfaceVariant = TextSecondary,
    outline = OutlineGray,
    error = ErrorRed,
    onError = SurfaceWhite,
)

@Composable
fun GaplyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = GaplyLightColors,
        typography = GaplyTypography,
        content = content,
    )
}
