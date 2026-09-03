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

private val NeonDarkColorScheme = darkColorScheme(
    primary = NeonGreenPrimaryDark,
    onPrimary = NeonGreenOnPrimaryDark,
    primaryContainer = NeonGreenPrimaryContainerDark,
    onPrimaryContainer = NeonGreenOnPrimaryContainerDark,
    secondary = CyanSecondaryDark,
    onSecondary = CyanOnSecondaryDark,
    secondaryContainer = CyanSecondaryContainerDark,
    onSecondaryContainer = CyanOnSecondaryContainerDark,
    tertiary = VioletTertiaryDark,
    onTertiary = VioletOnTertiaryDark,
    tertiaryContainer = VioletTertiaryContainerDark,
    onTertiaryContainer = VioletOnTertiaryContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark
)

private val NeonLightColorScheme = lightColorScheme(
    primary = NeonGreenPrimaryLight,
    onPrimary = NeonGreenOnPrimaryLight,
    primaryContainer = NeonGreenPrimaryContainerLight,
    onPrimaryContainer = NeonGreenOnPrimaryContainerLight,
    secondary = CyanSecondaryLight,
    onSecondary = CyanOnSecondaryLight,
    secondaryContainer = CyanSecondaryContainerLight,
    onSecondaryContainer = CyanOnSecondaryContainerLight,
    tertiary = VioletTertiaryLight,
    onTertiary = VioletOnTertiaryLight,
    tertiaryContainer = VioletTertiaryContainerLight,
    onTertiaryContainer = VioletOnTertiaryContainerLight,
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
    // Default to false so our vibrant Modern Neon Fitness color scheme is active
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> NeonDarkColorScheme
        else -> NeonLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
