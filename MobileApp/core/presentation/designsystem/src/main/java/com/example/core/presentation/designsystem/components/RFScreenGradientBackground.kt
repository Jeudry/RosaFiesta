package com.example.core.presentation.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.core.presentation.designsystem.RFColors

@Composable
fun RFScreenGradientBackground(
  modifier: Modifier = Modifier,
  colors: List<Color> = listOf(RFColors.GradientStart, RFColors.GradientEnd),
  start: Offset = Offset.Zero,
  end: Offset = Offset(1000f, 1000f),
  content: @Composable BoxScope.() -> Unit
) {
  Box(
    modifier = modifier
      .fillMaxSize()
      .background(
        brush = Brush.linearGradient(
          colors = colors,
          start = start,
          end = end
        )
      )
  ) { content() }
}
