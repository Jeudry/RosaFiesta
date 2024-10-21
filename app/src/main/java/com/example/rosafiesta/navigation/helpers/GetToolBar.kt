@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.rosafiesta.navigation.helpers

import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.core.presentation.designsystem.LogoIcon
import com.example.core.presentation.designsystem.components.RFToolbar
import com.example.rosafiesta.navigation.models.NavState

@Composable
fun GetToolBar(navState: NavState, scrollBehavior: TopAppBarScrollBehavior, onBackClick: () -> Unit) {
  return RFToolbar(
    showBackButton = navState.showBackBtn,
    title = navState.title,
    scrollBehavior = scrollBehavior,
    startContent = if(navState.showLogo) {
        {
          Icon(
            imageVector = LogoIcon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(30.dp)
          )
        }
      } else null,
    onBackClick = onBackClick
  )
}