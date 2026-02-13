package com.example.core.presentation.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.core.presentation.designsystem.RFColors

@Composable
fun RFLabeledDivider(
  text: String,
  modifier: Modifier = Modifier,
  dividerColor: Color = RFColors.Divider,
  textColor: Color = RFColors.Subtitle,
  thickness: Dp = 1.dp,
  textPaddingHorizontal: Dp = 15.dp,
  contentAlignment: Alignment = Alignment.Center
) {
  Box(modifier = modifier, contentAlignment = contentAlignment) {
    HorizontalDivider(
      color = dividerColor,
      thickness = thickness,
      modifier = Modifier.fillMaxWidth()
    )
    Text(
      text = text,
      style = MaterialTheme.typography.bodyMedium,
      color = textColor,
      textAlign = TextAlign.Center,
      modifier = Modifier
        .background(MaterialTheme.colorScheme.onPrimary)
        .padding(horizontal = textPaddingHorizontal)
    )
  }
}
