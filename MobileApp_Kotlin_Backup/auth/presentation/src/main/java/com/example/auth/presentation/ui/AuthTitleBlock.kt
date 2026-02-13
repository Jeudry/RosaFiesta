package com.example.auth.presentation.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.presentation.designsystem.RFColors

@Composable
fun AuthTitleBlock(
  title: String,
  subtitle: String,
  modifier: Modifier = Modifier
) {
  Column(modifier = modifier.fillMaxWidth()) {
    Text(
      text = title,
      fontSize = 28.sp,
      fontWeight = FontWeight.Bold,
      color = RFColors.Title,
      modifier = Modifier.fillMaxWidth()
    )
    Text(
      text = subtitle,
      fontSize = 14.sp,
      color = RFColors.Subtitle,
      lineHeight = 20.sp,
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 8.dp, bottom = 30.dp)
    )
  }
}
