package com.example.products.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.presentation.ui.ProductAction
import com.example.products.presentation.R
import com.example.products.presentation.model.ProductUi
import java.time.ZonedDateTime
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProductSmallCard(
  modifier: Modifier = Modifier,
  product: ProductUi,
  onAction: (ProductAction) -> Unit
) {
  val interactionSource = remember { MutableInteractionSource() }
  var showDropDown by remember {
    mutableStateOf(false)
  }
  
  
  Box(
    modifier = modifier
      .combinedClickable(
        onClick = {
          onAction(ProductAction.OnProductDetail(product.id))
        },
        onLongClick = {
          showDropDown = true
        },
        interactionSource = interactionSource,
        indication = rememberRipple(bounded = true)
      )
      .height(IntrinsicSize.Min)
      .fillMaxWidth()
      .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp))
      .background(Color.White)
  ) {
    Row {
      Image(
        modifier = Modifier
          .align(Alignment.CenterVertically)
          .padding(horizontal = 22.dp, vertical = 2.dp)
          .size(90.dp),
        imageVector = Icons.Rounded.ShoppingCart,
        contentDescription = stringResource(R.string.product_image)
      )
      
      Column(
        modifier = Modifier
          .align(Alignment.CenterVertically),
        verticalArrangement = Arrangement.Center
      ) {
        Text(
          modifier = Modifier
            .align(Alignment.Start),
          text = product.name,
          color = MaterialTheme.colorScheme.onPrimary,
          fontSize = 20.sp,
          fontWeight = FontWeight.Medium,
          style = TextStyle(
            platformStyle = PlatformTextStyle(
              includeFontPadding = false
            )
          )
        )
        
        product.description?.let {
          Text(
            modifier = Modifier
              .align(Alignment.Start)
              .padding(top = 2.dp)
              .padding(end = 18.dp),
            text = it,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 16.sp,
            style = TextStyle(
              platformStyle = PlatformTextStyle(
                includeFontPadding = false
              )
            )
          )
        }
        
        Text(
          modifier = Modifier
            .align(Alignment.Start)
            .padding(top = 14.dp)
            .padding(end = 18.dp),
          text = product.price.toString(),
          color = MaterialTheme.colorScheme.primary,
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          style = TextStyle(
            platformStyle = PlatformTextStyle(
              includeFontPadding = false
            )
          )
        )
      }
    }
    
    Box(
      modifier = Modifier
        .align(Alignment.CenterStart)
        .width(2.dp)
        .fillMaxHeight()
        .background(product.color)
    )
    
    DropdownMenu(
      expanded = showDropDown,
      onDismissRequest = { showDropDown = false }
    ) {
      DropdownMenuItem(
        text = {
          Text(text = stringResource(id = R.string.delete))
        },
        onClick = {
          showDropDown = false
          onAction(ProductAction.OnProductDelete(product.id))
        }
      )
    }

//    Icon(
//      modifier = Modifier
//        .clickable(onClick = { onAddToCartClick(product) })
//        .align(Alignment.BottomEnd)
//        .padding(8.dp)
//        .background(color = Accent, shape = RoundedCornerShape(12.dp))
//        .padding(10.dp)
//        .size(16.dp),
//      imageVector = Icons.Rounded.ShoppingCart,
//      contentDescription = "Cart",
//      tint = Color.White
//    )
  }
}

@Preview(showBackground = true)
@Composable
fun ProductSmallCardPreview() {
  ProductSmallCard(
    modifier = Modifier.padding(20.dp),
    product = ProductUi(
      id = UUID.randomUUID(),
      name = "Product 1",
      description = "Product 1 description",
      price = 10.0,
      color = Color.Blue,
      size = 10.0,
      stock = 0,
      created = ZonedDateTime.now()
    ),
    onAction = {}
    
  )
}