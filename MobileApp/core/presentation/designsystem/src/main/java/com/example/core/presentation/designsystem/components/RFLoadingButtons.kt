package com.example.core.presentation.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.presentation.designsystem.RFButtonDefaults
import com.example.core.presentation.designsystem.RFColors
import com.example.core.presentation.designsystem.RFShapes
import androidx.compose.material3.LocalContentColor

@Composable
fun RFLoadingContentButton(
  text: String,
  loading: Boolean,
  enabled: Boolean = true,
  modifier: Modifier = Modifier,
  colors: ButtonColors = ButtonDefaults.buttonColors(
    containerColor = MaterialTheme.colorScheme.primary,
    contentColor = MaterialTheme.colorScheme.onPrimary,
    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
    disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
  ),
  progressColor: Color = MaterialTheme.colorScheme.onPrimary,
  onClick: () -> Unit,
  shape: RoundedCornerShape = RoundedCornerShape(25.dp)
) {
  Button(
    onClick = onClick,
    enabled = enabled && !loading,
    modifier = modifier.height(RFButtonDefaults.PrimaryHeight),
    colors = colors,
    shape = shape
  ) {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
      if (loading) {
        CircularProgressIndicator(
          modifier = Modifier.size(22.dp),
          strokeWidth = 2.dp,
          color = progressColor
        )
      } else {
        Text(
          text = text,
          fontWeight = FontWeight.SemiBold,
          color = LocalContentColor.current
        )
      }
    }
  }
}

@Composable
fun RFPrimaryLargeButton(
  text: String,
  loading: Boolean,
  enabled: Boolean,
  modifier: Modifier = Modifier,
  activeColor: Color = RFColors.GradientEnd,
  disabledColor: Color = RFColors.GradientEnd.copy(alpha = 0.2f),
  onClick: () -> Unit
) {
  RFLoadingContentButton(
    text = text,
    loading = loading,
    enabled = enabled,
    modifier = modifier,
    onClick = onClick,
    shape = RFShapes.LargeButton,
    progressColor = Color.White,
    colors = ButtonDefaults.buttonColors(
      containerColor = if (enabled) activeColor else disabledColor,
      contentColor = Color.White,
      disabledContainerColor = disabledColor,
      disabledContentColor = Color.White.copy(alpha = 0.6f)
    )
  )
}
