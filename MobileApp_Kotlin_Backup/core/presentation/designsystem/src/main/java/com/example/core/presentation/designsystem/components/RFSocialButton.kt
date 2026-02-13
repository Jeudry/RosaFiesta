package com.example.core.presentation.designsystem.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun RFSocialButton(
  backgroundColor: Color,
  icon: ImageVector,
  modifier: Modifier = Modifier,
  contentDescription: String? = null,
  onClick: () -> Unit
) {
  Button(
    onClick = onClick,
    modifier = modifier.size(45.dp),
    shape = CircleShape,
    colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
    contentPadding = PaddingValues(0.dp)
  ) {
    Icon(
      imageVector = icon,
      contentDescription = contentDescription,
      tint = Color.White,
      modifier = Modifier.size(18.dp)
    )
  }
}
