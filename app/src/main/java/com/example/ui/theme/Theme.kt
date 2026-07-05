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

private val DarkColorScheme =
  darkColorScheme(
    primary = BrandTerracotta,
    secondary = DarkSlateText,
    tertiary = LightCoralContainer,
    background = Color(0xFF2B2524),
    surface = Color(0xFF2B2524),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = CreamWhiteBackground,
    onSurface = CreamWhiteBackground,
    primaryContainer = DarkBrown,
    onPrimaryContainer = LightCoralContainer,
    secondaryContainer = Color(0xFF473E3C),
    onSecondaryContainer = SecondaryTerracottaContainer,
    surfaceVariant = Color(0xFF473E3C),
    onSurfaceVariant = SecondaryTerracottaContainer,
    outline = Color(0xFF473E3C)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = BrandTerracotta,
    secondary = DarkSlateText,
    tertiary = DarkBrown,
    background = CreamWhiteBackground,
    surface = CreamWhiteBackground,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = NearBlackText,
    onSurface = NearBlackText,
    primaryContainer = LightCoralContainer,
    onPrimaryContainer = DarkBrown,
    secondaryContainer = SecondaryTerracottaContainer,
    onSecondaryContainer = DarkBrown,
    surfaceVariant = SecondaryTerracottaContainer,
    onSurfaceVariant = DarkSlateText,
    outline = SecondaryTerracottaContainer
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+ (disabled by default to enforce Geometric Balance theme)
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
