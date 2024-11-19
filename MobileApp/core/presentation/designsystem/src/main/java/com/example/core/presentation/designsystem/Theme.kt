package com.example.core.presentation.designsystem

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val DarkColorScheme = darkColorScheme(
  primary = RFGreen,
  background = RFBlack,
  surface = RFDarkGray,
  secondary = RFWhite,
  tertiary = RFWhite,
  primaryContainer = RFGreen30,
  onPrimary = RFBlack,
  onBackground = RFWhite,
  onSurface = RFWhite,
  onSurfaceVariant = RFGray,
  error = RFDarkRed
)

val LightColorScheme = lightColorScheme(
  primary = RFAccent,
  background = RFWhite,
  surface = RFShadow,
  secondary = RFBlack,
  secondaryContainer = RFAccent,
  tertiary = RFTertiary,
  primaryContainer = RFGreen30,
  onPrimary = RFDarkGray,
  onBackground = RFBlack,
  onSurface = RFBlack,
  onSecondary = RFWhite,
  onSurfaceVariant = RFLightGray,
  error = RFDarkRed
)

@Composable
fun RFTheme(
  content: @Composable () -> Unit
) {
  val colorScheme = LightColorScheme

  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as Activity).window
      WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
    }
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}