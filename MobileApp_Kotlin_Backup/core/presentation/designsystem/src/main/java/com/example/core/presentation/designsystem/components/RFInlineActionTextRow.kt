package com.example.core.presentation.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import com.example.core.presentation.designsystem.RFColors

@Composable
fun RFInlineActionTextRow(
  prefixText: String,
  actionText: String,
  modifier: Modifier = Modifier,
  prefixColor: Color = RFColors.Subtitle,
  actionColor: Color = RFColors.Link,
  onActionClick: () -> Unit
) {
  Row(
    horizontalArrangement = Arrangement.Center,
    modifier = modifier
  ) {
    Text(
      text = prefixText,
      style = MaterialTheme.typography.bodyMedium,
      color = prefixColor
    )
    Text(
      text = actionText,
      style = MaterialTheme.typography.bodyMedium,
      fontWeight = FontWeight.Medium,
      color = actionColor,
      modifier = Modifier.clickable { onActionClick() }
    )
  }
}
