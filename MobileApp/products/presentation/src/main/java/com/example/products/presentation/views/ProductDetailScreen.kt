@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.products.presentation.views

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.domain.product.ProductId
import com.example.core.presentation.designsystem.RFTheme
import com.example.products.presentation.R
import com.example.products.presentation.actions.ProductDetailAction
import com.example.products.presentation.model.ProductUi
import com.example.products.presentation.states.ProductDetailState
import com.example.products.presentation.viewModels.ProductDetailVM
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

private val Alternative_1 = Color(0xFF6D86C4)
private val Alternative_2 = Color(0xFFA0B4B0)

private const val SIZE_38 = 38.0
private const val SIZE_39 = 39.0
private const val SIZE_40 = 40.0
private const val SIZE_41 = 41.0

private const val DURATION = 600

@Composable
fun ProductDetailSR(
  viewModel: ProductDetailVM = koinViewModel(),
  productId: ProductId,
  onProductName: (String) -> Unit
) {
  viewModel.retrieveProduct(productId)
  
  ProductDetailScreen(
    state = viewModel.state,
    onAction = viewModel::onAction,
    onProductName = onProductName
  )
}

@ExperimentalMaterial3Api
@Composable
private fun ProductDetailScreen(
  state: ProductDetailState?,
  onAction: (ProductDetailAction) -> Unit,
  onProductName: (String) -> Unit
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
    LaunchedEffect(Unit) {
      onProductName(state.product.name)
    }
    Box(
      modifier = Modifier
        .fillMaxSize()
    ){
      Column {
        ProductDetailContent(state)
      }
    }
  }
}

@Composable
fun ProductDetailContent(state: ProductDetailState) {
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
      .verticalScroll(rememberScrollState())
        .fillMaxSize()
  ) {
    Box(
      modifier = Modifier
          .offset(x = animatedXOffset.value, y = animatedYOffset.value)
          .alpha(0.3f)
          .size(400.dp)
          .background(color = state.product.color, shape = CircleShape)
    )
    Column {
      Image(
        imageVector = ImageVector.vectorResource(id = com.example.core.presentation.designsystem.R.drawable.run_outlined),
        modifier = Modifier
            .scale(animatedSneakerScale.value)
            .rotate(animatedSneakerRotate.value)
            .padding(end = 48.dp)
            .height(240.dp)
            .fillMaxWidth(),
        contentDescription = stringResource(R.string.product_image)
      )
      
      Column(
        modifier = Modifier.padding(horizontal = 22.dp)
      ){
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            modifier = Modifier
              .padding(top = 4.dp),
            text = "$${state.product.price}",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 28.sp,
            style = TextStyle(
              platformStyle = PlatformTextStyle(
                includeFontPadding = false
              )
            )
          )
        }
        
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp),
          
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Text(
            text = stringResource(R.string.sizes),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            style = TextStyle(
              platformStyle = PlatformTextStyle(
                includeFontPadding = false
              )
            )
          )
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
        
        
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = stringResource(R.string.colors),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            style = TextStyle(
              platformStyle = PlatformTextStyle(
                includeFontPadding = false
              )
            )
          )
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
        
        
        if(!state.product.description.isNullOrEmpty()){
          Text(
            modifier = Modifier
              .padding(top = 24.dp),
            text = state.product.description!!,
            color = MaterialTheme.colorScheme.onPrimary,
            lineHeight = 20.sp,
            fontSize = 16.sp,
            fontWeight = FontWeight.Light,
            textAlign = TextAlign.Justify,
            style = TextStyle(
              platformStyle = PlatformTextStyle(
                includeFontPadding = false
              )
            )
          )
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
  val borderColor = if (isSelected) color else Color.Transparent
  
  Box(
    modifier = modifier
      .border(width = 0.dp, color = if(isSelected)MaterialTheme.colorScheme.outline else Color.Transparent, shape = CircleShape)
      .border(width = 3.dp, color = borderColor, shape = CircleShape)
        .padding(7.dp)
      .border(
        width = 0.5.dp,
        color = MaterialTheme.colorScheme.outline, shape = CircleShape)
      .background(color, shape = CircleShape)
        .size(24.dp)
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
  val textColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onPrimary
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
        .padding(horizontal = 12.dp, vertical = 8.dp),
    text = size.toString(),
    fontSize = 16.sp,
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
          id = UUID.randomUUID(),
          name = "name example",
          description = "description example",
          price = 50.0,
          rentalPrice = 10.0,
          imageUrl = "",
          stock = 10,
          color = MaterialTheme.colorScheme.primary,
          size = 0.0
        )
      ),
      onAction = {},
      onProductName = {}
    )
  }
}