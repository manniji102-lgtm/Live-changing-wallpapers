package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val ImmersiveDarkColorScheme = darkColorScheme(
    primary = ImmersivePrimary,
    onPrimary = ImmersiveOnPrimary,
    primaryContainer = ImmersivePrimaryContainer,
    onPrimaryContainer = ImmersiveOnPrimaryContainer,
    background = ImmersiveBg,
    onBackground = ImmersiveText,
    surface = ImmersiveSurface,
    onSurface = ImmersiveText,
    surfaceVariant = ImmersiveSurface,
    onSurfaceVariant = ImmersiveTextMedium,
    outline = ImmersiveOutline
)

private val DarkColorScheme =
  darkColorScheme(primary = Purple80, secondary = PurpleGrey80, tertiary = Pink80)

private val LightColorScheme =
  lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force Dark theme for the immersive aesthetic
  dynamicColor: Boolean = false, // Disable dynamic colors to enforce the specific Immersive UI colors
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) ImmersiveDarkColorScheme else ImmersiveDarkColorScheme


  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
