package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = Color(0xFF00363D),
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = Color(0xFFBCE9FF),
    secondary = SecondaryTeal,
    onSecondary = Color.White,
    tertiary = TertiaryAmber,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = Color(0xFF1E293B)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryWood,
    onPrimary = OnPrimaryWood,
    primaryContainer = PrimaryContainerWood,
    onPrimaryContainer = OnPrimaryContainerWood,
    secondary = SecondaryTeal,
    onSecondary = OnSecondaryTeal,
    secondaryContainer = SecondaryContainerTeal,
    onSecondaryContainer = OnSecondaryContainerTeal,
    tertiary = TertiaryAmber,
    onTertiary = OnTertiaryAmber,
    tertiaryContainer = TertiaryContainerAmber,
    onTertiaryContainer = OnTertiaryContainerAmber,
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our handcrafted rich theme by default
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
