package com.example.home.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.domain.product.ProductId
import com.example.core.presentation.designsystem.RFColors
import com.example.core.presentation.designsystem.RFTheme
import com.example.core.presentation.designsystem.components.RFParticleBackground
import com.example.core.presentation.designsystem.components.RFScreenGradientBackground
import com.example.core.presentation.ui.BaseProductAction
import com.example.core.presentation.ui.ProductAction
import com.example.products.presentation.model.ProductUi
import org.koin.androidx.compose.koinViewModel
import java.time.ZonedDateTime
import java.util.UUID

@Composable
fun DashboardControlCenterScreenRoot(
  viewModel: DashboardVM = koinViewModel(),
  onNavigateInventory: () -> Unit,
  onNavigateStats: () -> Unit,
  onNavigateOrders: () -> Unit,
  onProductDetail: (ProductId) -> Unit,
  onViewAllOrders: () -> Unit,
) {
  DashboardControlCenterScreen(
    state = viewModel.state,
    onAction = { action: BaseProductAction ->
      when (action) {
        is ProductAction.OnProductDetail -> onProductDetail(action.productId)
        else -> Unit
      }
      viewModel.onAction(action)
    },
    onNavigateInventory = onNavigateInventory,
    onNavigateStats = onNavigateStats,
    onNavigateOrders = onNavigateOrders,
    onViewAllOrders = onViewAllOrders
  )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DashboardControlCenterScreen(
  state: DashboardState,
  onAction: (BaseProductAction) -> Unit,
  onNavigateInventory: () -> Unit,
  onNavigateStats: () -> Unit,
  onNavigateOrders: () -> Unit,
  onViewAllOrders: () -> Unit
) {
  val criticalStock = remember(state.productsList) { state.productsList.filter { it.stock <= 3 } }
  val recentOrders = remember { sampleOrders() }
  val filteredStatuses = remember { OrderStatus.entries }

  RFScreenGradientBackground {
    RFParticleBackground {
      Box(Modifier.fillMaxSize()) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp),
          verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
          Text(
            text = "Resumen",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )

            OrdersSummaryCard(
              recent = recentOrders.take(3),
              upcoming = recentOrders.filter { it.status == OrderStatus.PROXIMO }.size,
              inProgress = recentOrders.filter { it.status == OrderStatus.EN_CURSO }.size,
              onViewAll = onViewAllOrders
            )

          StatusFiltersRow(statuses = filteredStatuses.map { it }, onSelected = { /* filter */ })

          CriticalStockCard(products = criticalStock.take(5))

          ChartsRow(onNavigateStats = onNavigateStats)

          QuickActionsRow(
            onNavigateInventory = onNavigateInventory,
            onNavigateOrders = onNavigateOrders,
            onNavigateStats = onNavigateStats
          )

          if (criticalStock.isNotEmpty()) {
            ProductHorizontalList(
              title = "Stock crítico",
              products = criticalStock,
              onAction = onAction
            )
          }

          Spacer(Modifier.height(32.dp))
        }
      }
    }
  }
}

@Composable
private fun OrdersSummaryCard(
  recent: List<UiOrder>,
  upcoming: Int,
  inProgress: Int,
  onViewAll: () -> Unit
) {
  Card(
    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
    shape = RoundedCornerShape(24.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "Pedidos",
          fontSize = 20.sp,
          fontWeight = FontWeight.SemiBold,
          modifier = Modifier.weight(1f),
          color = RFColors.Title
        )
        Text(
          text = "Ver detalle",
          color = RFColors.Link,
          fontSize = 13.sp,
          modifier = Modifier.clickable { onViewAll() }
        )
      }

      Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SummaryPill(label = "Próximos", value = upcoming, color = Color(0xFF5E60CE))
        SummaryPill(label = "En curso", value = inProgress, color = Color(0xFF48BFE3))
        SummaryPill(label = "Recientes", value = recent.size, color = Color(0xFF6930C3))
      }

      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        recent.forEach { order ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(Color(0xFFF3F5F9))
              .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(Modifier.weight(1f)) {
              Text(order.title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = RFColors.Title, maxLines = 1, overflow = TextOverflow.Ellipsis)
              Text(order.status.label, fontSize = 11.sp, color = RFColors.Subtitle)
            }
            Text("$" + order.amount.toString(), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = RFColors.Title)
          }
        }
      }
    }
  }
}

@Composable
private fun SummaryPill(label: String, value: Int, color: Color) {
  Column(
    modifier = Modifier
      .weight(1f)
      .clip(RoundedCornerShape(16.dp))
      .background(color.copy(alpha = 0.1f))
      .padding(vertical = 12.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text("$value", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
    Text(label, fontSize = 11.sp, color = color, fontWeight = FontWeight.Medium)
  }
}

@Composable
private fun StatusFiltersRow(statuses: List<OrderStatus>, onSelected: (OrderStatus) -> Unit) {
  LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
    items(statuses) { status ->
      FilterChip(
        selected = false,
        onClick = { onSelected(status) },
        label = { Text(status.label) },
        shape = RoundedCornerShape(50)
      )
    }
  }
}

@Composable
private fun CriticalStockCard(products: List<ProductUi>) {
  if (products.isEmpty()) return
  Card(
    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.96f)),
    shape = RoundedCornerShape(24.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Text("Stock crítico", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = RFColors.Title)
      products.forEach { product ->
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF7F9FC))
            .padding(horizontal = 12.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(product.name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = RFColors.Title, modifier = Modifier.weight(1f))
          Text("${product.stock}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD62828))
        }
      }
    }
  }
}

@Composable
private fun ChartsRow(onNavigateStats: () -> Unit) {
  Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
    ChartPlaceholder(
      title = "Ingresos",
      gradient = listOf(Color(0xFF56CCF2), Color(0xFF2F80ED)),
      modifier = Modifier.weight(1f).height(140.dp),
      onClick = onNavigateStats
    )
    ChartPlaceholder(
      title = "Pedidos / Mes",
      gradient = listOf(Color(0xFFB24592), Color(0xFFF15F79)),
      modifier = Modifier.weight(1f).height(140.dp),
      onClick = onNavigateStats
    )
  }
}

@Composable
private fun ChartPlaceholder(title: String, gradient: List<Color>, modifier: Modifier, onClick: () -> Unit) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(22.dp))
      .background(Brush.linearGradient(gradient))
      .clickable { onClick() }
      .padding(16.dp)
  ) {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
      Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
      CompositionLocalProvider(LocalContentColor provides Color.White.copy(alpha = 0.85f)) {
        Text("(Gráfica)", fontSize = 12.sp)
      }
    }
  }
}

@Composable
private fun QuickActionsRow(
  onNavigateInventory: () -> Unit,
  onNavigateOrders: () -> Unit,
  onNavigateStats: () -> Unit
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    QuickActionButton(label = "Inventario", icon = Icons.Default.Inventory2, color = Color(0xFF5E60CE), onClick = onNavigateInventory)
    QuickActionButton(label = "Pedidos", icon = Icons.Default.ShoppingCart, color = Color(0xFF64DFDF), onClick = onNavigateOrders)
    QuickActionButton(label = "Estadísticas", icon = Icons.Default.BarChart, color = Color(0xFF6930C3), onClick = onNavigateStats)
  }
}

@Composable
private fun QuickActionButton(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, onClick: () -> Unit) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .clip(RoundedCornerShape(18.dp))
      .background(color.copy(alpha = 0.12f))
      .clickable { onClick() }
      .padding(horizontal = 14.dp, vertical = 16.dp)
      .widthIn(min = 90.dp)
  ) {
    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
    Spacer(Modifier.height(6.dp))
    Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = color, maxLines = 1, overflow = TextOverflow.Ellipsis)
  }
}

@Composable
private fun ProductHorizontalList(title: String, products: List<ProductUi>, onAction: (BaseProductAction) -> Unit) {
  Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    Text(title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
    LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
      items(products, key = { it.id }) { product ->
        Card(
          modifier = Modifier
            .size(width = 160.dp, height = 120.dp)
            .clickable { onAction(ProductAction.OnProductDetail(product.id)) },
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
          elevation = CardDefaults.cardElevation(8.dp)
        ) {
          Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Text(product.name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = RFColors.Title, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text("Stock: ${product.stock}", fontSize = 11.sp, color = RFColors.Subtitle)
          }
        }
      }
    }
  }
}

private enum class OrderStatus(val label: String) { EN_COTIZACION("En cotización"), PARA_HOY("Para hoy"), CUMPLIDO("Cumplido"), FALLADO("Fallado"), PENDIENTE("Pendiente"), PROXIMO("Próximo"), EN_CURSO("En curso"); }

private data class UiOrder(
  val id: String,
  val title: String,
  val amount: Double,
  val status: OrderStatus
)

private fun sampleOrders(): List<UiOrder> = listOf(
  UiOrder("1", "Pedido fiesta jardín", 120.0, OrderStatus.EN_CURSO),
  UiOrder("2", "Decoración corporativa", 560.0, OrderStatus.PROXIMO),
  UiOrder("3", "Boda íntima", 980.0, OrderStatus.EN_COTIZACION),
  UiOrder("4", "Cumple infantil", 210.0, OrderStatus.PENDIENTE),
  UiOrder("5", "Centro de mesa urgente", 85.0, OrderStatus.PARA_HOY)
)

@Preview
@Composable
private fun DashboardControlCenterPreview() {
  RFTheme {
    DashboardControlCenterScreen(
      state = DashboardState(
        productsList = listOf(
          ProductUi(
            color = Color(0xFFE0E0E0),
            id = UUID.randomUUID(),
            name = "Rosa roja premium",
            description = "Desc",
            price = 10.0,
            size = 10.0,
            stock = 2,
            imageUrl = null,
            created = ZonedDateTime.now(),
            rentalPrice = 2.0
          ),
          ProductUi(
            color = Color(0xFFE0E0E0),
            id = UUID.randomUUID(),
            name = "Lirio blanco",
            description = "Desc",
            price = 11.0,
            size = 10.0,
            stock = 1,
            imageUrl = null,
            created = ZonedDateTime.now(),
            rentalPrice = 2.0
          )
        )
      ),
      onAction = {},
      onNavigateInventory = {},
      onNavigateStats = {},
      onNavigateOrders = {},
      onViewAllOrders = {}
    )
  }
}
