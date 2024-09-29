@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.core.presentation.designsystem.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
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
  scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(),
  content: @Composable (PaddingValues) -> Unit = {},
) {
  Scaffold(
    topBar = topAppBar,
    floatingActionButton = floatingActionButton,
    modifier = modifier,
    floatingActionButtonPosition = FabPosition.Center
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
fun RFScaffoldPreview() {
  RFTheme {
    RFScaffold(
      modifier = Modifier,
      withGradient = true,
      topAppBar = {},
      floatingActionButton = {},
      scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(),
      content = {}
    )
  }
}