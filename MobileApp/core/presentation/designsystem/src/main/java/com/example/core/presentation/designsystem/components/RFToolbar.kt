@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.core.presentation.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.presentation.designsystem.AnalyticsIcon
import com.example.core.presentation.designsystem.ArrowLeftIcon
import com.example.core.presentation.designsystem.LogoIcon
import com.example.core.presentation.designsystem.Poppins
import com.example.core.presentation.designsystem.R
import com.example.core.presentation.designsystem.RFGreen
import com.example.core.presentation.designsystem.RFTheme
import com.example.core.presentation.designsystem.components.utils.DropdownItem

@Composable
fun RFToolbar(
  modifier: Modifier = Modifier,
  showBackButton: Boolean = false,
  title: String,
  menuItems: List<DropdownItem> = emptyList(),
  onMenuItemClick: (Int) -> Unit = {},
  onBackClick: () -> Unit = {},
  scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(),
  startContent: @Composable() (() -> Unit)? = null
) {
  var isDropdownOpen by rememberSaveable {
    mutableStateOf(false)
  }

  TopAppBar(
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically
      ) {
        startContent?.invoke()
        if(startContent != null) {
          Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
          text = title,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onBackground,
          fontFamily = Poppins,
          fontSize = 28.sp
        )
      }
    },
    modifier = modifier,
    scrollBehavior = scrollBehavior,
    colors = TopAppBarDefaults.topAppBarColors(
      containerColor = Color.Transparent
    ),
    navigationIcon = {
      if (showBackButton) {
        IconButton(onClick = onBackClick) {
          Icon(
            imageVector = ArrowLeftIcon,
            contentDescription = stringResource(id = R.string.go_back),
            tint = MaterialTheme.colorScheme.onBackground
          )
        }
      }
    }, actions = {
      if (menuItems.isNotEmpty()) {
        Box {
          DropdownMenu(expanded = isDropdownOpen, onDismissRequest = {
            isDropdownOpen = false
          }) {
            menuItems.forEachIndexed { index, item ->
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                  .clickable {
                    onMenuItemClick(index)
                  }
                  .fillMaxWidth()
                  .padding(16.dp)
              ) {
                Icon(
                  imageVector = item.icon,
                  contentDescription = item.title
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = item.title,

                  )
              }
            }
          }
          IconButton(
            onClick = { isDropdownOpen = true }
          ) {
            Icon(
              imageVector = Icons.Default.MoreVert,
              contentDescription = stringResource(id = R.string.open_menu),
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }
    }
  )
}

@Preview
@Composable
private fun RFToolbarPreview(
  showLogo: Boolean = true
) {
  RFTheme {
    RFToolbar(
      modifier = Modifier.fillMaxWidth(),
      title = "Title",
      menuItems = listOf(
        DropdownItem(AnalyticsIcon, "Analytics")
      ),
      startContent = {
        if(showLogo) {
          Icon(
            imageVector = LogoIcon,
            contentDescription = null,
            tint = RFGreen,
            modifier = Modifier.size(35.dp)
          )
        }
      }
    )
  }
}