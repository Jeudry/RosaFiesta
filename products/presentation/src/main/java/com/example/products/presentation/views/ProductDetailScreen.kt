@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.products.presentation.views

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.presentation.designsystem.RFIconTint
import com.example.core.presentation.designsystem.RFLightRed
import com.example.core.presentation.designsystem.RFTheme
import com.example.core.presentation.designsystem.RFYellow
import com.example.products.presentation.actions.ProductDetailAction
import com.example.products.presentation.model.ProductUi
import com.example.products.presentation.states.ProductDetailState
import com.example.products.presentation.viewModels.ProductDetailVM
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import java.time.ZoneId
import java.time.ZonedDateTime

private val Alternative_1 = Color(0xFF6D86C4)
private val Alternative_2 = Color(0xFFA0B4B0)

private const val SIZE_38 = 38.0
private const val SIZE_39 = 39.0
private const val SIZE_40 = 40.0
private const val SIZE_41 = 41.0

private const val DURATION = 600

@Composable
fun ProductDetailSR(
  viewModel: ProductDetailVM = koinViewModel()
) {
  ProductDetailScreen(
    state = viewModel.state,
    onAction = viewModel::onAction
  )
}

@ExperimentalMaterial3Api
@Composable
private fun ProductDetailScreen(
  state: ProductDetailState?,
  onAction: (ProductDetailAction) -> Unit
) {
  ProductDetailCard(
    state
  )
}

@Composable
private fun ProductDetailCard(
  state: ProductDetailState?,
) {
  if (state == null) {
    Box(
      modifier = Modifier
        .fillMaxSize(),
      contentAlignment = Alignment.Center
    ) {
      CircularProgressIndicator()
    }
  } else {
    Box(
      modifier = Modifier
        .fillMaxSize()
    ){
      Column(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.onSurface
            )
      ) {
        ProductDetailContent(state)
      }
    }
  }
}

@Composable
fun ProductDetailContent(state: ProductDetailState) {
  var isFavorite by remember { mutableStateOf(false) }
  var selectedSize by remember { mutableDoubleStateOf(state.product.size) }
  var selectedColor by remember { mutableStateOf(state.product.color) }
  
  var xOffset by remember { mutableStateOf(800.dp) }
  var yOffset by remember { mutableStateOf(800.dp) }
  var buttonScale by remember { mutableFloatStateOf(0f) }
  var iconScale by remember { mutableFloatStateOf(0f) }
  var sneakerScale by remember { mutableFloatStateOf(0.6f) }
  var sneakerRotate by remember { mutableFloatStateOf(-60f) }
  
  val animatedXOffset = animateDpAsState(
    targetValue = xOffset,
    label = "",
    animationSpec = tween(durationMillis = DURATION, easing = FastOutLinearInEasing)
  )
  
  val animatedYOffset = animateDpAsState(
    targetValue = yOffset,
    label = "",
    animationSpec = tween(durationMillis = DURATION, easing = FastOutLinearInEasing)
  )
  
  val animatedButtonScale = animateFloatAsState(
    targetValue = buttonScale,
    label = "",
    animationSpec = tween(easing = FastOutLinearInEasing)
  )
  
  val animatedIconScale = animateFloatAsState(
    targetValue = iconScale,
    label = "",
    animationSpec = tween(easing = FastOutLinearInEasing)
  )
  
  val animatedSneakerScale = animateFloatAsState(
    targetValue = sneakerScale,
    label = "",
    animationSpec = tween(durationMillis = DURATION, easing = FastOutLinearInEasing)
  )
  
  val animatedSneakerRotate = animateFloatAsState(
    targetValue = sneakerRotate,
    label = "",
    animationSpec = tween(durationMillis = DURATION, easing = FastOutLinearInEasing)
  )
  
  LaunchedEffect(true) {
    delay(150)
    xOffset = 140.dp
    yOffset = (-130).dp
    sneakerScale = 1f
    sneakerRotate = -25f
    delay(400)
    iconScale = 1f
    delay(100)
    buttonScale = 1f
  }
  
  Box(
    modifier = Modifier
        .background(MaterialTheme.colorScheme.background)
        .fillMaxSize()
  ) {
    Box(
      modifier = Modifier
          .offset(x = animatedXOffset.value, y = animatedYOffset.value)
          .alpha(0.3f)
          .size(400.dp)
          .background(color = state.product.color, shape = CircleShape)
    )
    
    IconButton(
      modifier = Modifier
          .padding(start = 22.dp)
          .padding(top = 42.dp)
          .shadow(
              elevation = 24.dp,
              spotColor = MaterialTheme.colorScheme.surface,
              shape = RoundedCornerShape(12.dp)
          )
          .background(color = Color.White, shape = RoundedCornerShape(12.dp))
          .size(36.dp),
      onClick = {
      
      }
    ) {
      Icon(
        imageVector = Icons.Filled.KeyboardArrowLeft,
        contentDescription = "Back Icon",
        tint = MaterialTheme.colorScheme.onSurface
      )
    }
    
    Column {
      Image(
        imageVector = ImageVector.vectorResource(id = com.example.core.presentation.designsystem.R.drawable.run_outlined),
        modifier = Modifier
            .scale(animatedSneakerScale.value)
            .rotate(animatedSneakerRotate.value)
            .padding(end = 48.dp)
            .padding(top = 30.dp)
            .size(320.dp),
        contentDescription = "Product Image"
      )
      
      Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
            .padding(top = 48.dp),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column {
          Text(
            text = "Sneaker",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp,
            style = TextStyle(
              platformStyle = PlatformTextStyle(
                includeFontPadding = false
              )
            )
          )
          
          Text(
            modifier = Modifier
              .padding(top = 2.dp),
            text = "Product ${state.product.name}",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 22.sp,
            style = TextStyle(
              platformStyle = PlatformTextStyle(
                includeFontPadding = false
              )
            )
          )
          
          Row(
            modifier = Modifier
              .padding(top = 2.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              modifier = Modifier
                .size(18.dp),
              imageVector = Icons.Outlined.Star,
              contentDescription = "Rating Icon",
              tint = RFYellow
            )
            
            Text(
              modifier = Modifier
                .padding(start = 4.dp),
              textAlign = TextAlign.Center,
              text = state.product.rating.toString(),
              color = MaterialTheme.colorScheme.onSurface,
              fontSize = 12.sp,
              style = TextStyle(
                platformStyle = PlatformTextStyle(
                  includeFontPadding = false
                )
              )
            )
          }
        }
        
        Text(
          modifier = Modifier
            .padding(top = 4.dp),
          text = "$${state.product.price}",
          color = MaterialTheme.colorScheme.onPrimary,
          fontSize = 36.sp,
          style = TextStyle(
            platformStyle = PlatformTextStyle(
              includeFontPadding = false
            )
          )
        )
      }
      
      Text(
        modifier = Modifier
            .padding(horizontal = 22.dp)
            .padding(top = 24.dp),
        text = "Size",
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        style = TextStyle(
          platformStyle = PlatformTextStyle(
            includeFontPadding = false
          )
        )
      )
      
      Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp)
            .padding(horizontal = 22.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        ProductSizeCard(
          size = SIZE_38,
          isSelected = selectedSize == SIZE_38
        ) {
          selectedSize = SIZE_38
        }
        
        ProductSizeCard(
          size = SIZE_39,
          isSelected = selectedSize == SIZE_39
        ) {
          selectedSize = SIZE_39
        }
        
        ProductSizeCard(
          size = SIZE_40,
          isSelected = selectedSize == SIZE_40
        ) {
          selectedSize = SIZE_40
        }
        
        ProductSizeCard(
          size = SIZE_41,
          isSelected = selectedSize == SIZE_41
        ) {
          selectedSize = SIZE_41
        }
      }
      
      Text(
        modifier = Modifier
            .padding(horizontal = 22.dp)
            .padding(top = 24.dp),
        text = "Color",
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        style = TextStyle(
          platformStyle = PlatformTextStyle(
            includeFontPadding = false
          )
        )
      )
      
      Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp)
            .padding(horizontal = 22.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        ProductColor(
          color = state.product.color,
          isSelected = selectedColor == state.product.color
        ) {
          selectedColor = state.product.color
        }
        
        ProductColor(
          color = Alternative_1,
          isSelected = selectedColor == Alternative_1
        ) {
          selectedColor = Alternative_1
        }
        
        ProductColor(
          color = Alternative_2,
          isSelected = selectedColor == Alternative_2
        ) {
          selectedColor = Alternative_2
        }
      }
      
      Text(
        modifier = Modifier
            .padding(horizontal = 22.dp)
            .padding(top = 24.dp),
        text = "Introducing the \"Futurist Glide,\" a revolutionary sneaker designed for the modern urban explorer. Its sleek, aerodynamic silhouette is crafted from sustainable, high-performance materials, offering both unparalleled comfort and eco-conscious style. The upper features a unique, breathable mesh pattern, seamlessly integrated with adaptive lacing technology for a snug, custom fit.",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        lineHeight = 20.sp,
        fontSize = 12.sp,
        fontWeight = FontWeight.Light,
        textAlign = TextAlign.Justify,
        style = TextStyle(
          platformStyle = PlatformTextStyle(
            includeFontPadding = false
          )
        )
      )
      
      Spacer(modifier = Modifier.weight(1.0f))
      
      Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp)
      ) {
        IconButton(
          modifier = Modifier
            .scale(animatedIconScale.value),
          onClick = { isFavorite = !isFavorite }
        ) {
          Icon(
            imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
            contentDescription = "Favorite Icon",
            tint = if (isFavorite) RFLightRed else RFIconTint
          )
        }
        
        Button(
          modifier = Modifier
              .scale(animatedButtonScale.value)
              .fillMaxWidth()
              .height(48.dp)
              .padding(start = 8.dp),
          shape = RoundedCornerShape(16.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
          ),
          onClick = {}
        ) {
          Icon(
            imageVector = Icons.Rounded.ShoppingCart,
            contentDescription = null,
            tint = Color.White
          )
          Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
          Text(text = "Add to cart")
        }
      }
    }
  }
}

@Composable
fun ProductColor(
  modifier: Modifier = Modifier,
  color: Color,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
  
  Box(
    modifier = modifier
        .border(width = 0.5.dp, color = borderColor, shape = CircleShape)
        .padding(3.dp)
        .background(color, shape = CircleShape)
        .size(12.dp)
        .clickable(
            onClick = onClick
        )
  )
}


@Composable
fun ProductSizeCard(
  modifier: Modifier = Modifier,
  size: Double,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  val backgroundColor =
    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
  val textColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
  val border = if (isSelected) 0.dp else 0.8.dp
  
  Text(
    modifier = modifier
        .clip(RoundedCornerShape(12.dp))
        .border(
            width = border,
            color = MaterialTheme.colorScheme.outline,
            shape = RoundedCornerShape(12.dp)
        )
        .background(backgroundColor)
        .clickable(
            onClick = onClick
        )
        .padding(12.dp),
    text = size.toString(),
    fontSize = 12.sp,
    color = textColor,
    style = TextStyle(
      platformStyle = PlatformTextStyle(
        includeFontPadding = false
      )
    )
  )
}

@Preview
@Composable
private fun ProductDetailSP() {
  RFTheme {
    ProductDetailScreen(
      state = ProductDetailState(
        product = ProductUi(
          created = ZonedDateTime.now().withZoneSameInstant(ZoneId.of("UTC")),
          id = "123",
          name = "name example",
          description = "description example",
          price = 50.0,
          rentalPrice = 10.0,
          imageUrl = "",
          stock = 10,
          color = MaterialTheme.colorScheme.primary,
          size = 0.0,
          rating = 0.0
        )
      ),
      onAction = {}
    )
  }
}