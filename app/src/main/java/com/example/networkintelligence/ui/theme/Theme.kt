package com.example.networkintelligence.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val KineticSignalDarkColorScheme = darkColorScheme(
    primary = KS_Primary,
    onPrimary = KS_OnPrimary,
    primaryContainer = KS_PrimaryContainer,
    onPrimaryContainer = KS_OnPrimaryContainer,
    secondary = KS_Secondary,
    onSecondary = KS_OnSecondary,
    secondaryContainer = KS_SecondaryContainer,
    onSecondaryContainer = KS_OnSecondaryContainer,
    tertiary = KS_Tertiary,
    onTertiary = KS_OnTertiary,
    tertiaryContainer = KS_TertiaryContainer,
    onTertiaryContainer = KS_OnTertiaryContainer,
    error = KS_Error,
    onError = KS_OnError,
    errorContainer = KS_ErrorContainer,
    onErrorContainer = KS_OnErrorContainer,
    background = KS_Background,
    onBackground = KS_OnBackground,
    surface = KS_Surface,
    onSurface = KS_OnSurface,
    surfaceVariant = KS_SurfaceVariant,
    onSurfaceVariant = KS_OnSurfaceVariant,
    surfaceTint = KS_SurfaceTint,
    surfaceBright = KS_SurfaceBright,
    surfaceDim = KS_SurfaceDim,
    surfaceContainer = KS_SurfaceContainer,
    surfaceContainerHigh = KS_SurfaceContainerHigh,
    surfaceContainerHighest = KS_SurfaceContainerHighest,
    surfaceContainerLow = KS_SurfaceContainerLow,
    surfaceContainerLowest = KS_SurfaceContainerLowest,
    inverseSurface = KS_InverseSurface,
    inverseOnSurface = KS_InverseOnSurface,
    inversePrimary = KS_InversePrimary,
    outline = KS_Outline,
    outlineVariant = KS_OutlineVariant,
    scrim = KS_Scrim,
)

@Composable
fun NetworkIntelligenceTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = KineticSignalDarkColorScheme,
        typography = Typography,
        content = content,
    )
}
