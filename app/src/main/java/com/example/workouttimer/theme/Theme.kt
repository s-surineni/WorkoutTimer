package com.example.workouttimer.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val CobaltDarkColorScheme = darkColorScheme(
    primary = CobaltPrimaryDark,
    onPrimary = CobaltOnPrimaryDark,
    primaryContainer = CobaltPrimaryContainerDark,
    onPrimaryContainer = CobaltOnPrimaryContainerDark,
    secondary = CoralSecondaryDark,
    onSecondary = CoralOnSecondaryDark,
    secondaryContainer = CoralSecondaryContainerDark,
    onSecondaryContainer = CoralOnSecondaryContainerDark,
    tertiary = EmeraldTertiaryDark,
    onTertiary = EmeraldOnTertiaryDark,
    tertiaryContainer = EmeraldTertiaryContainerDark,
    onTertiaryContainer = EmeraldOnTertiaryContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark
)

private val CobaltLightColorScheme = lightColorScheme(
    primary = CobaltPrimaryLight,
    onPrimary = CobaltOnPrimaryLight,
    primaryContainer = CobaltPrimaryContainerLight,
    onPrimaryContainer = CobaltOnPrimaryContainerLight,
    secondary = CoralSecondaryLight,
    onSecondary = CoralOnSecondaryLight,
    secondaryContainer = CoralSecondaryContainerLight,
    onSecondaryContainer = CoralOnSecondaryContainerLight,
    tertiary = EmeraldTertiaryLight,
    onTertiary = EmeraldOnTertiaryLight,
    tertiaryContainer = EmeraldTertiaryContainerLight,
    onTertiaryContainer = EmeraldOnTertiaryContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight
)

@Composable
fun WorkoutTimerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Default to false so our vibrant Royal Cobalt & Coral color scheme is active
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> CobaltDarkColorScheme
        else -> CobaltLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
