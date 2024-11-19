package com.example.core.presentation.designsystem.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.core.presentation.designsystem.R

@Composable
fun RFFloatingAddBtn(
  onClick: () -> Unit
) {
  FloatingActionButton(
    containerColor =  MaterialTheme.colorScheme.primary,
    contentColor =  MaterialTheme.colorScheme.onPrimary,
    modifier = Modifier.padding(end = 8.dp, bottom = 16.dp).size(
      75.dp
    ),
    onClick = onClick
  ) {
    Icon(Icons.Filled.Add, stringResource(R.string.add_btn),
      Modifier.size(40.dp),
      tint = MaterialTheme.colorScheme.onSecondary
      )
  }
}