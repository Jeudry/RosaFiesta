package com.example.core.presentation.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.core.presentation.designsystem.RFBlack
import com.example.core.presentation.designsystem.RFGray
import com.example.core.presentation.designsystem.RFTheme

@Composable
fun RFActionButton(
  textColor: Color = MaterialTheme.colorScheme.onPrimary,
  text: String,
  isLoading: Boolean,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  onClick: () -> Unit
) {
  Button(
    onClick = onClick,
    enabled = enabled,
    colors = ButtonDefaults.buttonColors(
      containerColor = MaterialTheme.colorScheme.primary,
      contentColor = MaterialTheme.colorScheme.onPrimary,
      disabledContainerColor = RFGray,
      disabledContentColor = RFBlack
    ),
    shape = RoundedCornerShape(100f),
    modifier = modifier.height(IntrinsicSize.Min)
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp),
      contentAlignment = Alignment.Center
    ) {
      CircularProgressIndicator(
        modifier = Modifier
          .size(15.dp)
          .alpha(if (isLoading) 1f else 0f),
        strokeWidth = 1.5.dp,
        color = MaterialTheme.colorScheme.onPrimary
      )
      Text(
        text = text,
        color = textColor,
        modifier = Modifier.alpha(if (isLoading) 0f else 1f),
        fontWeight = FontWeight.Medium
      )
    }
  }
}

@Composable
fun RFOutlinedActionButton(
  text: String,
  isLoading: Boolean,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  onClick: () -> Unit
) {
  Button(
    onClick = onClick,
    enabled = enabled,
    colors = ButtonDefaults.buttonColors(
      contentColor = MaterialTheme.colorScheme.onBackground,
      containerColor = MaterialTheme.colorScheme.background,
    ),
    border = BorderStroke(
      width = 1.5.dp,
      MaterialTheme.colorScheme.onBackground
    ),
    shape = RoundedCornerShape(100f),
    modifier = modifier.height(IntrinsicSize.Min)
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp),
      contentAlignment = Alignment.Center
    ) {
      CircularProgressIndicator(
        modifier = Modifier
          .size(15.dp)
          .alpha(if (isLoading) 1f else 0f),
        strokeWidth = 1.5.dp,
        color = MaterialTheme.colorScheme.onBackground
      )
      Text(
        text = text,
        modifier = Modifier.alpha(if (isLoading) 0f else 1f),
        fontWeight = FontWeight.Medium,
      )
    }
  }
}


@Preview
@Composable
private fun RFActionButtonPreview() {
  RFTheme {
    RFOutlinedActionButton(
      text = "Sign in",
      isLoading = false,
      onClick = { }
    )
  }
}