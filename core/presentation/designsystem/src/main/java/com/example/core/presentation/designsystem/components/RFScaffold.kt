@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package com.example.core.presentation.designsystem.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.core.presentation.designsystem.RFTheme

@Composable
fun RFScaffold(
  modifier: Modifier = Modifier,
  withGradient: Boolean = true,
  topAppBar: @Composable () -> Unit = {},
  floatingActionButton: @Composable () -> Unit = {},
  floatingBtnPosition: FabPosition = FabPosition.End,
  bottomBar:@Composable () -> Unit = {},
  content: @Composable (PaddingValues) -> Unit = {},
  ) {
  Scaffold(
    topBar = topAppBar,
    floatingActionButton = floatingActionButton,
    modifier = modifier,
    floatingActionButtonPosition = floatingBtnPosition,
    bottomBar = bottomBar,
  ) { padding ->
    if (withGradient) {
      GradientBackground {
        content(padding)
      }
    } else {
      content(padding)
    }
  }
}

@Preview
@Composable
private fun RFScaffoldPreview() {
  RFTheme {
    RFScaffold(
      modifier = Modifier,
      withGradient = true,
      topAppBar = {},
      floatingActionButton = {},
      content = {},
      bottomBar = {}
    )
  }
}