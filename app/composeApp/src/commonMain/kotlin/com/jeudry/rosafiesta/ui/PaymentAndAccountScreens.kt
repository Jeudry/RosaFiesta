package com.jeudry.rosafiesta.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.jeudry.rosafiesta.domain.CanonicalSampleData
import com.jeudry.rosafiesta.domain.ClientEvent
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeudry.rosafiesta.designsystem.LocalRfTypography
import com.jeudry.rosafiesta.designsystem.RfIcon
import com.jeudry.rosafiesta.designsystem.RfIconKind
import com.jeudry.rosafiesta.designsystem.RfPhoto
import com.jeudry.rosafiesta.designsystem.RfPhotoKey
import com.jeudry.rosafiesta.designsystem.RfRadialBackgroundBox
import com.jeudry.rosafiesta.designsystem.rfCardShadow
import com.jeudry.rosafiesta.designsystem.rfHeroBannerShadow
import com.jeudry.rosafiesta.designsystem.rfIconButtonShadow
import com.jeudry.rosafiesta.designsystem.rfInputShadow
import com.jeudry.rosafiesta.designsystem.rfPrimaryButtonShadow

private val PaymentBackgroundGradient = Brush.radialGradient(
    0.0f to Color(0xFFF9E3F1),
    0.6f to Color(0x00F9E3F1),
    center = Offset(0f, 0f),
    radius = 450f,
)

private val RosaButtonGradient = Brush.horizontalGradient(
    listOf(Color(0xFFEA4EA9), Color(0xFFB754CB), Color(0xFF865DEC)),
)

// ==============================================================================
// s49 · HISTORIAL DE COTIZACIONES
// ==============================================================================
private data class QuoteHistoryItem49(
    val title: String,
    val dateLocation: String,
    val tagText: String,
    val tagBg: Color,
    val tagTextCol: Color,
    val stepText: String,
    val activeSteps: Int,
    val stepColor: Color,
    val guests: String,
    val price: String,
)

@Composable
internal fun RenderQuoteHistoryScreenS49(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    RfRadialBackgroundBox(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 56.dp, bottom = 30.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = false)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFF0EBF4), CircleShape)
                        .clickable { onSelectScreenCode("BACK:s37") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1A1A2E), size = 22.dp)
                }

                BasicText(
                    text = "Cotizaciones",
                    style = TextStyle(
                        fontFamily = typo.displayFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = Color(0xFF1A1A2E),
                    ),
                )
            }

            val quotes = listOf(
                QuoteHistoryItem49(
                    title = "Boda Real Rosa & Dorado",
                    dateLocation = "Vie 23 oct · Jardín Botánico",
                    tagText = "Revisada",
                    tagBg = Color(0xFFFFF2C9),
                    tagTextCol = Color(0xFF7A5700),
                    stepText = "Paso 2 de 5 · Revisada",
                    activeSteps = 2,
                    stepColor = Color(0xFFF7B500),
                    guests = "120 invitados",
                    price = "RD$ 4,500",
                ),
                QuoteHistoryItem49(
                    title = "Quinceañera Valentina Crystal",
                    dateLocation = "Lun 2 nov · Hotel El Embajador",
                    tagText = "Confirmada",
                    tagBg = Color(0xFFEEF6D6),
                    tagTextCol = Color(0xFF4E6A10),
                    stepText = "Paso 3 de 5 · Confirmada",
                    activeSteps = 3,
                    stepColor = Color(0xFF8DB52C),
                    guests = "150 invitados",
                    price = "RD$ 6,200",
                ),
                QuoteHistoryItem49(
                    title = "Gala Corporativa Grupo Caribe",
                    dateLocation = "Lun 12 oct · JW Marriott BlueMall",
                    tagText = "Completada",
                    tagBg = Color(0xFFEFE8FD),
                    tagTextCol = Color(0xFF7A4FD8),
                    stepText = "Paso 5 de 5 · Completada",
                    activeSteps = 5,
                    stepColor = Color(0xFF865DEC),
                    guests = "200 invitados",
                    price = "RD$ 8,500",
                ),
            )

            quotes.forEach { item ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(22.dp),
                            spotColor = Color(0x30505078),
                            ambientColor = Color(0x15505078),
                        )
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFF2EDF5), RoundedCornerShape(22.dp))
                        .clickable { onSelectScreenCode("s48") }
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        // Top row: Title/subtitle + Tag
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top,
                        ) {
                            Column(
                                modifier = Modifier.weight(1f).padding(end = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                BasicText(
                                    text = item.title,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(
                                        fontFamily = typo.bodyFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color(0xFF1A1A2E),
                                    ),
                                )
                                BasicText(
                                    text = item.dateLocation,
                                    style = TextStyle(
                                        fontFamily = typo.bodyFamily,
                                        fontSize = 13.sp,
                                        color = Color(0xFF62627A),
                                    ),
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .height(24.dp)
                                    .clip(CircleShape)
                                    .background(item.tagBg)
                                    .padding(horizontal = 9.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                BasicText(
                                    text = item.tagText,
                                    style = TextStyle(
                                        fontFamily = typo.bodyFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = item.tagTextCol,
                                    ),
                                )
                            }
                        }

                        // Middle: 5 progress bars + step text
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                for (i in 1..5) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(
                                                if (i <= item.activeSteps) item.stepColor
                                                else Color(0xFFF1EFF5)
                                            ),
                                    )
                                }
                            }
                            BasicText(
                                text = item.stepText,
                                style = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF62627A),
                                ),
                            )
                        }

                        // Bottom: guests + price
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                RfIcon(kind = RfIconKind.GUESTS, tint = Color(0xFF62627A), size = 16.dp)
                                BasicText(
                                    text = item.guests,
                                    style = TextStyle(
                                        fontFamily = typo.bodyFamily,
                                        fontSize = 14.sp,
                                        color = Color(0xFF62627A),
                                    ),
                                )
                            }
                            BasicText(
                                text = item.price,
                                style = TextStyle(
                                    fontFamily = typo.displayFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp,
                                    color = Color(0xFF1A1A2E),
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==============================================================================
// s50 · PAGO / CHECKOUT
// ==============================================================================
@Composable
internal fun RenderPaymentCheckoutScreenS50(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    var selectedMethod by remember { mutableStateOf(0) } // 0: Tarjeta, 1: Popular, 2: Banreservas, 3: Efectivo
    val events = viewModel?.events?.collectAsState()?.value ?: CanonicalSampleData.events
    val selectedId = viewModel?.selectedEventId?.collectAsState()?.value ?: "evt-boda-real"
    val event = events.firstOrNull { it.id == selectedId } ?: events.first()
    val depositAmount = (event.totalDop * 30 / 100).coerceAtLeast(100)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFC)),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(Brush.radialGradient(colors = listOf(Color(0xFFF9E3F1), Color(0x00F9E3F1)), center = Offset(0f, 0f), radius = size.width * 0.9f))
            drawRect(Brush.radialGradient(colors = listOf(Color(0xFFEEE7FB), Color(0x00EEE7FB)), center = Offset(size.width, 0f), radius = size.width * 0.8f))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 56.dp, start = 20.dp, end = 20.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = false)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("BACK:s48") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1A1A2E), size = 22.dp)
                }

                BasicText(
                    text = "Pago",
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF1A1A2E)),
                    modifier = Modifier.weight(1f),
                )

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    RfIcon(kind = RfIconKind.SHIELD, tint = Color(0xFF62627A), size = 14.dp)
                    BasicText("Pago seguro", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF62627A)))
                }
            }

            // Delivery section card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(18.dp), isAdmin = false)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFF2EDF5), RoundedCornerShape(18.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFEEF6D6)),
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.TRUCK, tint = Color(0xFF4E6A10), size = 20.dp)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        BasicText("Entrega para ${event.clientName}", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF62627A)))
                        BasicText(event.venue, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1A1A2E)))
                    }

                    BasicText("Cambiar", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFC2338F)))
                }
            }

            // Payment methods title
            BasicText("Método de pago", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1A1A2E)))

            // 2x2 Payment methods grid
            val methods = listOf("Tarjeta", "Banco Popular", "Banreservas", "Efectivo")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                methods.chunked(2).forEachIndexed { rowIdx, row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        row.forEachIndexed { colIdx, name ->
                            val idx = rowIdx * 2 + colIdx
                            val isSel = selectedMethod == idx
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(62.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White)
                                    .then(
                                        if (isSel) Modifier.border(2.dp, Color(0xFFB754CB), RoundedCornerShape(16.dp))
                                        else Modifier.border(1.dp, Color(0xFFEEE9F2), RoundedCornerShape(16.dp))
                                    )
                                    .clickable { selectedMethod = idx }
                                    .padding(horizontal = 14.dp),
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    RfIcon(
                                        kind = if (name == "Tarjeta") RfIconKind.WALLET else if (name == "Efectivo") RfIconKind.WALLET else RfIconKind.ORDERS,
                                        tint = Color(0xFF1A1A2E),
                                        size = 20.dp,
                                    )
                                    BasicText(
                                        text = name,
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF1A1A2E)),
                                        modifier = Modifier.weight(1f),
                                    )
                                    if (isSel) {
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .shadow(6.dp, CircleShape, spotColor = Color(0xB36664E9))
                                                .clip(CircleShape)
                                                .background(Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9)))),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            RfIcon(kind = RfIconKind.CHECK, tint = Color.White, size = 12.dp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Card details title
            BasicText("Datos de la tarjeta", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1A1A2E)))

            // Name on card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .rfInputShadow(RoundedCornerShape(14.dp), isAdmin = false)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE8E1EE), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    RfIcon(kind = RfIconKind.USER, tint = Color(0xFF62627A), size = 18.dp)
                    BasicText(event.clientName, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF1A1A2E)))
                }
            }

            // Card number
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .rfInputShadow(RoundedCornerShape(14.dp), isAdmin = false)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE8E1EE), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    RfIcon(kind = RfIconKind.WALLET, tint = Color(0xFF62627A), size = 18.dp)
                    BasicText("•••• •••• •••• 4242", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF1A1A2E)))
                }
            }

            // MM/AA and CVC
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .rfInputShadow(RoundedCornerShape(14.dp), isAdmin = false)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE8E1EE), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    BasicText("12/28", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF1A1A2E)))
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .rfInputShadow(RoundedCornerShape(14.dp), isAdmin = false)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE8E1EE), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    BasicText("•••", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF1A1A2E)))
                }
            }

            // Contact phone
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .rfInputShadow(RoundedCornerShape(14.dp), isAdmin = false)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE8E1EE), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    RfIcon(kind = RfIconKind.PHONE, tint = Color(0xFF62627A), size = 18.dp)
                    BasicText("(809) 555-0142", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF1A1A2E)))
                }
            }
        }

        // Bottom fixed payment CTA bar
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.White)
                .border(1.dp, Color(0xFFF2EDF5))
                .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 34.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = false)
                    .clip(RoundedCornerShape(16.dp))
                    .background(RosaButtonGradient)
                    .clickable {
                        viewModel?.submitPayment(eventId = event.id)
                        viewModel?.soundPlayer?.playPaymentSuccess()
                        onSelectScreenCode("s51")
                    }
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BasicText("Pagar anticipo (30%)", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White))
                    BasicText("RD$ $depositAmount", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 19.sp, color = Color.White))
                }
            }
        }
    }
}

// ==============================================================================
// s51 · CONFIRMACIÓN DE PAGO
// ==============================================================================
@Composable
internal fun RenderPaymentSuccessScreenS51(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val events = viewModel?.events?.collectAsState()?.value ?: CanonicalSampleData.events
    val selectedId = viewModel?.selectedEventId?.collectAsState()?.value ?: "evt-boda-real"
    val event = events.firstOrNull { it.id == selectedId } ?: events.first()
    val depositPaid = if (event.paidDepositDop > 0) event.paidDepositDop else (event.totalDop * 30 / 100).coerceAtLeast(100)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFC)),
    ) {
        // Decorative background garland & shapes
        Canvas(modifier = Modifier.fillMaxSize()) {
            val scaleX = size.width / 390f
            val scaleY = size.height / 844f

            // Garland wire
            val wirePath = Path().apply {
                moveTo(-10f * scaleX, 50f * scaleY)
                quadraticTo(195f * scaleX, 110f * scaleY, 400f * scaleX, 50f * scaleY)
            }
            drawPath(
                wirePath,
                color = Color(0xFF1A1A2E).copy(alpha = 0.18f),
                style = Stroke(width = 1.2f * density),
            )

            // 9 Garland flags
            val flags = listOf(
                Triple(listOf(Offset(14f, 56f), Offset(34f, 56f), Offset(24f, 80f)), Color(0xFFEC4F97), 1f),
                Triple(listOf(Offset(58f, 66f), Offset(78f, 66f), Offset(68f, 90f)), Color(0xFFFBCA3F), 1f),
                Triple(listOf(Offset(102f, 73f), Offset(122f, 73f), Offset(112f, 97f)), Color(0xFF5CC9C0), 1f),
                Triple(listOf(Offset(146f, 78f), Offset(166f, 78f), Offset(156f, 102f)), Color(0xFF865DEC), 1f),
                Triple(listOf(Offset(190f, 79f), Offset(210f, 79f), Offset(200f, 103f)), Color(0xFFB9D84A), 1f),
                Triple(listOf(Offset(234f, 77f), Offset(254f, 77f), Offset(244f, 101f)), Color(0xFFEC4F97), 1f),
                Triple(listOf(Offset(278f, 73f), Offset(298f, 73f), Offset(288f, 97f)), Color(0xFFFBCA3F), 1f),
                Triple(listOf(Offset(322f, 66f), Offset(342f, 66f), Offset(332f, 90f)), Color(0xFF5CC9C0), 1f),
                Triple(listOf(Offset(366f, 56f), Offset(386f, 56f), Offset(376f, 80f)), Color(0xFF865DEC), 1f),
            )

            flags.forEach { (pts, color, _) ->
                val p = Path().apply {
                    moveTo(pts[0].x * scaleX, pts[0].y * scaleY)
                    lineTo(pts[1].x * scaleX, pts[1].y * scaleY)
                    lineTo(pts[2].x * scaleX, pts[2].y * scaleY)
                    close()
                }
                drawPath(p, color = color)
            }

            // Hollow circles
            drawCircle(
                color = Color(0xFFF2C4DF),
                radius = 30f * density,
                center = Offset(52f * scaleX, 200f * scaleY),
                style = Stroke(width = 1.5f * density),
            )
            drawCircle(
                color = Color(0xFFCFE48A),
                radius = 24f * density,
                center = Offset(344f * scaleX, 250f * scaleY),
                style = Stroke(width = 2f * density),
            )

            // Diamond at (330, 150)
            val diamond = Path().apply {
                moveTo(330f * scaleX, 150f * scaleY)
                lineTo(338f * scaleX, 164f * scaleY)
                lineTo(330f * scaleX, 178f * scaleY)
                lineTo(322f * scaleX, 164f * scaleY)
                close()
            }
            drawPath(diamond, color = Color(0xFF9ED7DC), style = Stroke(width = 1.5f * density))

            // Sparkle 4-point at (70, 290)
            val spk1 = Path().apply {
                moveTo(70f * scaleX, 282f * scaleY)
                lineTo(73f * scaleX, 290f * scaleY)
                lineTo(81f * scaleX, 293f * scaleY)
                lineTo(73f * scaleX, 296f * scaleY)
                lineTo(70f * scaleX, 304f * scaleY)
                lineTo(67f * scaleX, 296f * scaleY)
                lineTo(59f * scaleX, 293f * scaleY)
                lineTo(67f * scaleX, 290f * scaleY)
                close()
            }
            drawPath(spk1, color = Color(0xFFF7C9DF))

            // Solid dots
            drawCircle(color = Color(0xFFB9D84A), radius = 5f * density, center = Offset(140f * scaleX, 150f * scaleY))
            drawCircle(color = Color(0xFFFBCA3F), radius = 3.5f * density, center = Offset(150f * scaleX, 220f * scaleY))

            // Star 5-point at (246, 136)
            drawCircle(color = Color(0xFFFBCA3F), radius = 6f * density, center = Offset(246f * scaleX, 136f * scaleY))

            // Bottom sparkles
            drawCircle(color = Color(0xFFFBCA3F).copy(alpha = 0.85f), radius = 5f * density, center = Offset(360f * scaleX, 700f * scaleY))
            drawCircle(color = Color(0xFFB9D84A), radius = 4f * density, center = Offset(30f * scaleX, 740f * scaleY))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 130.dp, bottom = 160.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Header at top 130dp
            Column(
                modifier = Modifier.padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .shadow(14.dp, CircleShape, spotColor = Color(0xCC50BEC8), ambientColor = Color(0x4050BEC8))
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFF5CC9C0), Color(0xFF5AB7E0)))),
                    contentAlignment = Alignment.Center,
                ) {
                    Canvas(modifier = Modifier.size(36.dp)) {
                        val p = Path().apply {
                            moveTo(5f * density, 12.5f * density)
                            lineTo(9.5f * density, 17f * density)
                            lineTo(19f * density, 7.5f * density)
                        }
                        drawPath(p, color = Color.White, style = Stroke(width = 3.5f * density, cap = StrokeCap.Round, join = StrokeJoin.Round))
                    }
                }

                BasicText(
                    text = "¡Evento confirmado!",
                    style = TextStyle(
                        fontFamily = typo.displayFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp,
                        brush = Brush.horizontalGradient(listOf(Color(0xFFEB4F9E), Color(0xFFF2894A), Color(0xFFE3B23C), Color(0xFF9CC04B))),
                    ),
                )

                BasicText("Todo listo para el 23 de octubre.", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF62627A)))
            }

            Spacer(modifier = Modifier.height(50.dp))

            // Ticket card at top 330dp
            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth(),
            ) {
                // Main white card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 14.dp,
                            shape = RoundedCornerShape(24.dp),
                            spotColor = Color(0x50C850AA),
                            ambientColor = Color(0x20C850AA),
                        )
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White),
                ) {
                    Column {
                        // Card header
                        Row(
                            modifier = Modifier.padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Brush.linearGradient(listOf(Color(0xFFEA4EA9), Color(0xFF865DEC)))),
                                contentAlignment = Alignment.Center,
                            ) {
                                Canvas(modifier = Modifier.size(26.dp)) {
                                    val s = size.width / 24f
                                    val horn = Path().apply {
                                        moveTo(4f * s, 20f * s)
                                        lineTo(8.5f * s, 8f * s)
                                        lineTo(16f * s, 15.5f * s)
                                        close()
                                    }
                                    drawPath(horn, color = Color.White, style = Stroke(width = 1.8f * s, cap = StrokeCap.Round, join = StrokeJoin.Round))
                                    val ribbons = Path().apply {
                                        moveTo(14f * s, 4f * s)
                                        quadraticTo(14f * s, 6f * s, 13f * s, 7.5f * s)
                                        moveTo(17f * s, 7.5f * s)
                                        quadraticTo(18.5f * s, 6.5f * s, 20.5f * s, 7.5f * s)
                                        moveTo(20f * s, 12f * s)
                                        quadraticTo(18.5f * s, 11.5f * s, 16.8f * s, 13f * s)
                                    }
                                    drawPath(ribbons, color = Color.White, style = Stroke(width = 1.8f * s, cap = StrokeCap.Round, join = StrokeJoin.Round))
                                    drawCircle(color = Color.White, radius = 0.9f * s, center = Offset(10f * s, 4.5f * s))
                                    drawCircle(color = Color.White, radius = 0.9f * s, center = Offset(20f * s, 4f * s))
                                    drawCircle(color = Color.White, radius = 0.9f * s, center = Offset(21f * s, 16f * s))
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                BasicText(event.title, style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 19.sp, lineHeight = 22.sp, color = Color(0xFF1A1A2E)))
                                Box(
                                    modifier = Modifier
                                        .height(24.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEEF6D6))
                                        .padding(horizontal = 9.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                        Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(Color(0xFFA9CC3A)))
                                        BasicText("Confirmado", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF4E5A12)))
                                    }
                                }
                            }
                        }

                        // Dashed line
                        Canvas(modifier = Modifier.fillMaxWidth().height(1.dp).padding(horizontal = 18.dp)) {
                            drawLine(
                                color = Color(0xFFEEE8F2),
                                start = Offset(0f, 0f),
                                end = Offset(size.width, 0f),
                                strokeWidth = 2f * density,
                            )
                        }

                        // Ticket details
                        Column(
                            modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 18.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().height(30.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                BasicText("Fecha", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF62627A)))
                                Box(
                                    modifier = Modifier
                                        .height(28.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFFF2C9))
                                        .padding(horizontal = 10.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    BasicText(event.dateLabel, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF7A5700)))
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth().height(30.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                BasicText("Artículos", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF62627A)))
                                BasicText("${event.articleCount}", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1A1A2E)))
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth().height(30.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                BasicText("Anticipo pagado", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF62627A)))
                                BasicText("RD$ $depositPaid", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Color(0xFF3FB59A)))
                            }
                        }
                    }
                }

                // Ticket Notch on Left
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .offset(x = (-12).dp, y = 80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFAFAFC)),
                )

                // Ticket Notch on Right
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(24.dp)
                        .offset(x = 12.dp, y = 80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFAFAFC)),
                )
            }
        }

        // Bottom CTA buttons at bottom 36dp
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 20.dp, end = 20.dp, bottom = 36.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = false)
                    .clip(RoundedCornerShape(16.dp))
                    .background(RosaButtonGradient)
                    .clickable { onSelectScreenCode("s37") },
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    text = "Ver mis eventos",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White),
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECE6F0), RoundedCornerShape(16.dp))
                        .clickable { onSelectScreenCode("s40") },
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        RfIcon(kind = RfIconKind.SHARE, tint = Color(0xFF1A1A2E), size = 18.dp)
                        BasicText("Compartir", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1A1A2E)))
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF1EFF5))
                        .clickable { onSelectScreenCode("s18") },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText("Ir al catálogo", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1A1A2E)))
                }
            }
        }
    }
}

// ==============================================================================
// s52 · POST-EVENTO / ANÁLISIS
// ==============================================================================
@Composable
internal fun RenderPostEventReviewScreenS52(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFC)),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(Brush.radialGradient(colors = listOf(Color(0xFFF9E3F1), Color(0x00F9E3F1)), center = Offset(0f, 0f), radius = size.width * 0.9f))
            drawRect(Brush.radialGradient(colors = listOf(Color(0xFFEEE7FB), Color(0x00EEE7FB)), center = Offset(size.width, 0f), radius = size.width * 0.8f))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 56.dp, start = 20.dp, end = 20.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = false)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("BACK:s40") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1A1A2E), size = 22.dp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "Así salió tu evento",
                        style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, lineHeight = 22.sp, color = Color(0xFF1A1A2E)),
                    )
                    BasicText(
                        text = "Gala Corporativa Grupo Caribe · 12 oct",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF62627A)),
                    )
                }
            }

            // Puntualidad card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFEFE8FD))
                    .padding(20.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    // Meter circle
                    Canvas(modifier = Modifier.size(96.dp)) {
                        val strokeW = 10f * density
                        drawCircle(color = Color.White, radius = 38f * density, center = Offset(48f * density, 48f * density), style = Stroke(width = strokeW))
                        drawCircle(color = Color(0xFF8DB52C), radius = 38f * density, center = Offset(48f * density, 48f * density), style = Stroke(width = strokeW, cap = StrokeCap.Round))
                        // Checkmark inside
                        val check = Path().apply {
                            moveTo(36f * density, 49f * density)
                            lineTo(44f * density, 57f * density)
                            lineTo(60f * density, 40f * density)
                        }
                        drawPath(check, color = Color(0xFF4E6A10), style = Stroke(width = 4.5f * density, cap = StrokeCap.Round))
                    }

                    Column {
                        BasicText("Puntualidad", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF7A4FD8)))
                        BasicText("100%", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 44.sp, lineHeight = 44.sp, color = Color(0xFF1A1A2E)))
                        BasicText("Todo llegó a tiempo", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF62627A)))
                    }
                }
            }

            // Presupuesto card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFF2EDF5), RoundedCornerShape(22.dp))
                    .padding(horizontal = 18.dp, vertical = 16.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    BasicText("Presupuesto", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1A1A2E)))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            BasicText("Estimado", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF62627A)))
                            BasicText("RD$ 8,500", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1A1A2E)))
                        }
                        Box(modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)).background(Color(0xFFE6DEEA)))
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            BasicText("Real", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF62627A)))
                            BasicText("RD$ 8,535", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1A1A2E)))
                        }
                        Box(modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)).background(Color(0xFF8DB52C)))
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFEEF6D6))
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            BasicText("Cargo por un centro dañado", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF4E6A10)))
                            BasicText("RD$ 35", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF4E6A10)))
                        }
                    }
                }
            }

            // Activities / Tasks section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFF2EDF5), RoundedCornerShape(22.dp))
                    .padding(vertical = 4.dp),
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        BasicText("Tareas completadas", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color(0xFF1A1A2E)))
                        BasicText("Sin tareas", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF62627A)))
                    }
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF2F0F5)))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        BasicText("Cronograma ejecutado", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color(0xFF1A1A2E)))
                        BasicText("Sin actividades", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF62627A)))
                    }
                }
            }
        }

        // Bottom CTA button
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 20.dp, end = 20.dp, bottom = 36.dp)
                .fillMaxWidth()
                .height(56.dp)
                .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = false)
                .clip(RoundedCornerShape(16.dp))
                .background(RosaButtonGradient)
                .clickable { onSelectScreenCode("s37") },
            contentAlignment = Alignment.Center,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RfIcon(kind = RfIconKind.STAR, tint = Color.White, size = 20.dp)
                BasicText(
                    text = "Dejar una reseña",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White),
                )
            }
        }
    }
}

// ==============================================================================
// s53 · MIS RESERVAS
// ==============================================================================
@Composable
internal fun RenderMyReservationsScreenS53(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    var selectedTab by remember { mutableStateOf("Próximas") }

    RfRadialBackgroundBox(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Spacer(modifier = Modifier.height(56.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = false)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("BACK:s54") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1A1A2E), size = 22.dp)
                }
                BasicText(
                    text = "Mis reservas",
                    modifier = Modifier.weight(1f),
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF1A1A2E)),
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF1EFF5))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                listOf("Próximas", "Pasadas", "Todas").forEach { label ->
                    val selected = label == selectedTab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .then(if (selected) Modifier.rfCardShadow(RoundedCornerShape(12.dp), isAdmin = false) else Modifier)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selected) Color.White else Color.Transparent)
                            .clickable { selectedTab = label },
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            label,
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = if (selected) Color(0xFF1A1A2E) else Color(0xFF62627A),
                            ),
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                ReservationCard53(
                    title = "Boda Real Rosa & Dorado",
                    meta = "Vie 23 oct · Jardín Botánico",
                    tag = "Cotización lista",
                    tagBackground = Color(0xFFDDF4F3),
                    tagColor = Color(0xFF1F8A84),
                    photoKey = RfPhotoKey.BODA,
                    paid = "RD$ 1,350",
                    total = "RD$ 4,500",
                    remaining = "Resta RD$ 3,150",
                    progress = 0.30f,
                    onClick = { onSelectScreenCode("s49") },
                )
                ReservationCard53(
                    title = "Quinceañera Valentina Crystal",
                    meta = "Lun 2 nov · Hotel El Embajador",
                    tag = "Confirmado",
                    tagBackground = Color(0xFFEEF6D6),
                    tagColor = Color(0xFF4E6A10),
                    photoKey = RfPhotoKey.QUINCEANERA,
                    paid = "RD$ 1,860",
                    total = "RD$ 6,200",
                    remaining = "Resta RD$ 4,340",
                    progress = 0.30f,
                    onClick = { onSelectScreenCode("s49") },
                )
                ReservationCard53(
                    title = "Baby Shower Sofía en Rosas",
                    meta = "Lun 9 nov · Terraza Cava Alta, Piantini",
                    tag = "Borrador",
                    tagBackground = Color(0xFFF1EFF5),
                    tagColor = Color(0xFF62627A),
                    photoKey = RfPhotoKey.BABY_SHOWER,
                    paid = "RD$ 555",
                    total = "RD$ 1,850",
                    remaining = "Resta RD$ 1,295",
                    progress = 0.30f,
                    onClick = { onSelectScreenCode("s49") },
                )
            }
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun ReservationCard53(
    title: String,
    meta: String,
    tag: String,
    tagBackground: Color,
    tagColor: Color,
    photoKey: RfPhotoKey,
    paid: String,
    total: String,
    remaining: String,
    progress: Float,
    onClick: () -> Unit,
) {
    val typo = LocalRfTypography.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .rfCardShadow(RoundedCornerShape(22.dp), isAdmin = false)
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
            RfPhoto(photoKey = photoKey, contentDescription = title, modifier = Modifier.size(52.dp), cornerRadius = 14.dp)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                BasicText(
                    title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1A1A2E)),
                )
                BasicText(
                    meta,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF62627A)),
                )
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .height(24.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(tagBackground)
                        .padding(horizontal = 9.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(tag, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = tagColor))
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFFAFAFC))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                BasicText(
                    buildAnnotatedString {
                        withStyle(SpanStyle(color = Color(0xFF62627A))) { append("Pagado ") }
                        withStyle(SpanStyle(color = Color(0xFF1A1A2E), fontWeight = FontWeight.Bold)) { append(paid) }
                    },
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp),
                )
                BasicText(
                    buildAnnotatedString {
                        withStyle(SpanStyle(color = Color(0xFF62627A))) { append("de ") }
                        withStyle(SpanStyle(color = Color(0xFF1A1A2E), fontWeight = FontWeight.Bold)) { append(total) }
                    },
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp),
                )
            }
            Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFFEADFE7))) {
                Box(modifier = Modifier.fillMaxWidth(progress).height(6.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFF8DB52C)))
            }
            BasicText(remaining, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF62627A)))
        }
    }
}

// ==============================================================================
// s54 · PERFIL DE USUARIO
// ==============================================================================
@Composable
internal fun RenderUserProfileScreenS54(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val profileName = viewModel?.profileName?.collectAsState()?.value ?: "Camila Rodríguez"
    val profileEmail = viewModel?.profileEmail?.collectAsState()?.value ?: "camila.rodriguez@correo.com"
    val profilePhone = viewModel?.profilePhone?.collectAsState()?.value ?: "809-555-0142"
    val initials = profileName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("").ifEmpty { "CR" }

    RfRadialBackgroundBox(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Spacer(modifier = Modifier.height(56.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = false)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("BACK:s14") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1A1A2E), size = 22.dp)
                }
                BasicText(
                    text = "Mi perfil",
                    modifier = Modifier.weight(1f),
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF1A1A2E)),
                )
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = false)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.EDIT, tint = Color(0xFF1A1A2E), size = 20.dp)
                }
            }
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 4.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    modifier = Modifier.size(84.dp).clip(CircleShape).background(Color(0xFFF9B6D3)),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier.size(80.dp).clip(CircleShape).background(Color(0xFFFAFAFC)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier.size(76.dp).clip(CircleShape).background(Color(0xFFFDE6F2)),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(
                                initials,
                                style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, color = Color(0xFFC2338F)),
                            )
                        }
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        profileName,
                        style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, lineHeight = 24.sp, color = Color(0xFF1A1A2E)),
                    )
                    BasicText(
                        profileEmail,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF62627A)),
                    )
                }
            }

            ProfileCaption54("DATOS")
            Spacer(Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFF2EDF5), RoundedCornerShape(20.dp)),
            ) {
                ProfileRow54("Teléfono", RfIconKind.PHONE) {
                    BasicText(profilePhone, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFC2338F)))
                }
                ProfileDivider54()
                ProfileRow54("Cumpleaños", RfIconKind.CAKE) {
                    BasicText("Agregar", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFC2338F)))
                }
                ProfileDivider54()
                ProfileRow54("Usuario", RfIconKind.USER) {
                    BasicText("camila", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF62627A)))
                }
            }

            Spacer(Modifier.height(14.dp))
            ProfileCaption54("PREFERENCIAS")
            Spacer(Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFF2EDF5), RoundedCornerShape(20.dp)),
            ) {
                ProfileRow54("Idioma", RfIconKind.GLOBE) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        BasicText("Español", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF62627A)))
                        RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = Color(0xFF62627A), size = 16.dp)
                    }
                }
                ProfileDivider54()
                Row(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp).padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    RfIcon(kind = RfIconKind.BELL, tint = Color(0xFF1A1A2E), size = 20.dp)
                    Column(modifier = Modifier.weight(1f)) {
                        BasicText("Recordatorios", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF1A1A2E)))
                        BasicText("Antes y después de cada evento", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF62627A)))
                    }
                    Box(
                        modifier = Modifier
                            .width(50.dp)
                            .height(30.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFFEA4EA9), Color(0xFF865DEC))))
                            .clickable { },
                        contentAlignment = Alignment.CenterEnd,
                    ) {
                        Box(modifier = Modifier.padding(end = 3.dp).size(24.dp).clip(CircleShape).background(Color.White))
                    }
                }
                ProfileDivider54()
                ProfileRow54("Tema", RfIconKind.MOON) {
                    Row(
                        modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(Color(0xFFF1EFF5)).padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .height(30.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                                .clickable { onSelectScreenCode("s14") }
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText("Claro", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1A1A2E)))
                        }
                        Box(
                            modifier = Modifier
                                .height(30.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSelectScreenCode("s15") }
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText("Oscuro", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF62627A)))
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFF2EDF5), RoundedCornerShape(20.dp)),
            ) {
                ProfileRow54("Mis pagos y recibos", RfIconKind.RECEIPT, onClick = { onSelectScreenCode("s82") }) {
                    RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = Color(0xFF62627A), size = 16.dp)
                }
                ProfileDivider54()
                ProfileRow54("Ajustes y privacidad", RfIconKind.FILTER, onClick = { onSelectScreenCode("s86") }) {
                    RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = Color(0xFF62627A), size = 16.dp)
                }
                ProfileDivider54()
                ProfileRow54("Acerca de Rosa Fiesta", RfIconKind.INFO, onClick = { onSelectScreenCode("s16") }) {
                    RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = Color(0xFF62627A), size = 16.dp)
                }
                ProfileDivider54()
                ProfileRow54(
                    "Cerrar sesión",
                    RfIconKind.LOGOUT,
                    labelColor = Color(0xFFB42345),
                    onClick = { onSelectScreenCode("s07") },
                ) {}
            }
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun ProfileCaption54(label: String) {
    val typo = LocalRfTypography.current

    BasicText(
        label,
        modifier = Modifier.padding(horizontal = 20.dp),
        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp, color = Color(0xFF62627A)),
    )
}

@Composable
private fun ProfileDivider54() {
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF2F0F5)))
}

@Composable
private fun ProfileRow54(
    label: String,
    icon: RfIconKind,
    labelColor: Color = Color(0xFF1A1A2E),
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit,
) {
    val typo = LocalRfTypography.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 54.dp)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RfIcon(kind = icon, tint = labelColor, size = 20.dp)
        BasicText(
            label,
            modifier = Modifier.weight(1f),
            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = if (labelColor == Color(0xFFB42345)) FontWeight.SemiBold else FontWeight.Normal, color = labelColor),
        )
        trailing()
    }
}

// ==============================================================================
// s55 · PROVEEDORES ASOCIADOS
// ==============================================================================
@Composable
internal fun RenderSuppliersScreenS55(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(Color(0xFFF6F6FB))
            drawRect(
                Brush.radialGradient(
                    colors = listOf(Color(0xFFE6E7FF), Color(0x00E6E7FF)),
                    center = Offset(size.width, 0f),
                    radius = size.width * 1.1f,
                ),
            )
            drawRect(
                Brush.radialGradient(
                    colors = listOf(Color(0xFFF1E9FB), Color(0x00F1E9FB)),
                    center = Offset(0f, size.height * 0.12f),
                    radius = size.width * 0.8f,
                ),
            )
        }
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Spacer(modifier = Modifier.height(56.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = false)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("BACK:s54") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        "Proveedores",
                        style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, lineHeight = 22.sp, color = Color(0xFF151729)),
                    )
                    BasicText("2 activos", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)))
                }
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(8.dp, CircleShape, spotColor = Color(0xB35B6CF0))
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
                        .clickable { onSelectScreenCode("s56") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.PLUS, tint = Color.White, size = 20.dp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                RfIcon(kind = RfIconKind.SEARCH, tint = Color(0xFF5F6377), size = 18.dp)
                BasicText("Buscar proveedor", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF5F6377)))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                SupplierCard55(
                    initial = "F",
                    initialBackground = Color(0xFFECEBFF),
                    initialColor = Color(0xFF4B47D6),
                    name = "Flores del Jarabacoa Premium",
                    contact = "Doña Carmen Polanco",
                    phone = "+1 809 555 2201",
                )
                SupplierCard55(
                    initial = "C",
                    initialBackground = Color(0xFFDDF4F3),
                    initialColor = Color(0xFF1F8A84),
                    name = "Cristalería & Eventos Caribe",
                    contact = "Ing. Marcos Tavares",
                    phone = "+1 809 555 2202",
                )
            }
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun SupplierCard55(
    initial: String,
    initialBackground: Color,
    initialColor: Color,
    name: String,
    contact: String,
    phone: String,
) {
    val typo = LocalRfTypography.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(22.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(initialBackground),
                contentAlignment = Alignment.Center,
            ) {
                BasicText(initial, style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = initialColor))
            }
            Column(modifier = Modifier.weight(1f)) {
                BasicText(
                    name,
                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 20.sp, color = Color(0xFF151729)),
                )
                BasicText(contact, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF5F6377)))
            }
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape).clickable { },
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.MORE_VERT, tint = Color(0xFF5F6377), size = 20.dp)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BasicText(
                phone,
                modifier = Modifier.weight(1f),
                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF151729)),
            )
            Box(
                modifier = Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFEDEDF5)).clickable { },
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.PHONE, tint = Color(0xFF151729), size = 18.dp)
            }
            Box(
                modifier = Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFEEF6D6)).clickable { },
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.WHATSAPP, tint = Color(0xFF4E6A10), size = 18.dp)
            }
        }
    }
}

// ==============================================================================
// s56 · NUEVO PROVEEDOR
// ==============================================================================
private val AdminPanelGradient = Brush.linearGradient(
    listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0), Color(0xFF2FB8C8)),
)

@Composable
private fun AdminPanelIconButton(icon: RfIconKind, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .rfIconButtonShadow(CircleShape, isAdmin = true)
            .clip(CircleShape)
            .background(Color.White)
            .border(1.dp, Color(0xFFECECF5), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        RfIcon(kind = icon, tint = Color(0xFF151729), size = 21.dp)
    }
}

@Composable
private fun AdminPanelCard(
    modifier: Modifier = Modifier,
    contentPadding: androidx.compose.foundation.layout.PaddingValues = androidx.compose.foundation.layout.PaddingValues(0.dp),
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(22.dp))
            .padding(contentPadding),
        content = content,
    )
}

@Composable
internal fun RenderNewSupplierScreenS56(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(modifier = Modifier.fillMaxSize()) {
        RfRadialBackgroundBox(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                                        .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
            ) {
                Spacer(modifier = Modifier.height(56.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    AdminPanelIconButton(RfIconKind.CLOSE) { onSelectScreenCode("s55") }
                    BasicText(
                        "Nuevo proveedor",
                        modifier = Modifier.weight(1f),
                        style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF151729)),
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    AdminField56(label = "Nombre de la empresa", placeholder = "Ej. Flores del Valle", icon = RfIconKind.BOX, required = true)
                    AdminField56(label = "Persona de contacto", placeholder = "Nombre y apellido", icon = RfIconKind.USER)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(Modifier.weight(1f)) { AdminField56(label = "Teléfono", placeholder = "809 000 0000") }
                        Box(Modifier.weight(1f)) { AdminField56(label = "Correo", placeholder = "correo@…") }
                    }
                    AdminField56(label = "Sitio web", placeholder = "www.ejemplo.com", icon = RfIconKind.SHARE)
                    AdminNotes56()
                }
                Spacer(modifier = Modifier.height(130.dp))
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 36.dp)
                .height(56.dp)
                .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = Color(0x995B6CF0))
                .clip(RoundedCornerShape(16.dp))
                .background(AdminPanelGradient)
                .clickable { onSelectScreenCode("s55") },
            contentAlignment = Alignment.Center,
        ) {
            BasicText("Guardar proveedor", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White))
        }
    }
}

@Composable
private fun AdminField56(
    label: String,
    placeholder: String,
    icon: RfIconKind? = null,
    required: Boolean = false,
) {
    val typo = LocalRfTypography.current

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row {
            BasicText(label, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF151729)))
            if (required) {
                BasicText(" *", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF4B47D6)))
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .shadow(8.dp, RoundedCornerShape(14.dp), spotColor = Color(0x224650B4))
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (icon != null) {
                RfIcon(kind = icon, tint = Color(0xFF5F6377), size = 18.dp)
            }
            BasicText(
                placeholder,
                maxLines = 1,
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF5F6377)),
            )
        }
    }
}

@Composable
private fun AdminNotes56() {
    val typo = LocalRfTypography.current

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        BasicText("Notas", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF151729)))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .shadow(8.dp, RoundedCornerShape(14.dp), spotColor = Color(0x224650B4))
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(14.dp))
                .padding(start = 14.dp, end = 14.dp, top = 14.dp),
        ) {
            BasicText(
                "Condiciones, horarios, lo que ofrece…",
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF5F6377)),
            )
        }
    }
}

// ==============================================================================
// s57 · ANÁLISIS
// ==============================================================================
@Composable
internal fun RenderSpendingAnalyticsScreenS57(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    RfRadialBackgroundBox(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(56.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AdminPanelIconButton(RfIconKind.BACK) { onSelectScreenCode("BACK:s54") }
                BasicText(
                    "Análisis",
                    modifier = Modifier.weight(1f),
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF151729)),
                )
                Row(
                    modifier = Modifier
                        .height(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE9E9F2), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    BasicText("Este año", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF151729)))
                    RfIcon(kind = RfIconKind.CHEVRON_DOWN, tint = Color(0xFF151729), size = 15.dp)
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(
                        modifier = Modifier
                            .weight(1.5f)
                            .clip(RoundedCornerShape(22.dp))
                            .background(AdminPanelGradient)
                            .padding(16.dp),
                    ) {
                        BasicText("Ingresos totales", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFFF6EAFB)))
                        Spacer(Modifier.height(6.dp))
                        BasicText("RD$ 22,100", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, lineHeight = 32.sp, color = Color.White))
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(22.dp))
                            .padding(16.dp),
                    ) {
                        BasicText("Eventos", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)))
                        Spacer(Modifier.height(6.dp))
                        BasicText("4", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, lineHeight = 32.sp, color = Color(0xFF151729)))
                    }
                }

                AdminPanelCard(contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                        BasicText("Tendencia de ingresos", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF151729)))
                        BasicText("Datos de ejemplo", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
                    }
                    Spacer(Modifier.height(10.dp))
                    IncomeTrend57()
                }

                AdminPanelCard(contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)) {
                    BasicText("Eventos por estado", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF151729)))
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().height(14.dp).clip(RoundedCornerShape(7.dp)),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Box(Modifier.weight(1f).fillMaxSize().background(Color(0xFF8DB52C)))
                        Box(Modifier.weight(1f).fillMaxSize().background(Color(0xFF1FBDC0)))
                        Box(Modifier.weight(1f).fillMaxSize().background(Color(0xFF865DEC)))
                        Box(Modifier.weight(1f).fillMaxSize().background(Color(0xFFCDBFCB)))
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatusLegend57("Confirmado", "1", Color(0xFF8DB52C))
                            StatusLegend57("Completado", "1", Color(0xFF865DEC))
                        }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatusLegend57("Ajustado", "1", Color(0xFF1FBDC0))
                            StatusLegend57("Borrador", "1", Color(0xFFCDBFCB))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun IncomeTrend57() {
    val typo = LocalRfTypography.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Canvas(modifier = Modifier.fillMaxWidth().height(128.dp)) {
            val sx = size.width / 326f
            listOf(30f, 75f, 120f).forEach { y ->
                drawLine(Color(0xFFEDEDF5), Offset(0f, y), Offset(size.width, y), 1.dp.toPx())
            }
            val line = Path().apply {
                moveTo(10f * sx, 40f)
                cubicTo(40f * sx, 44f, 60f * sx, 30f, 70f * sx, 34f)
                cubicTo(80f * sx, 38f, 110f * sx, 62f, 130f * sx, 58f)
                cubicTo(150f * sx, 54f, 170f * sx, 72f, 190f * sx, 80f)
                cubicTo(210f * sx, 88f, 230f * sx, 96f, 250f * sx, 98f)
                cubicTo(270f * sx, 100f, 300f * sx, 112f, 316f * sx, 116f)
            }
            val area = Path().apply {
                addPath(line)
                lineTo(316f * sx, 128f)
                lineTo(10f * sx, 128f)
                close()
            }
            drawPath(area, Color(0xFFECEBFF))
            drawPath(line, Color(0xFFD63F98), style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))
            drawCircle(Color.White, radius = 5.dp.toPx(), center = Offset(316f * sx, 116f))
            drawCircle(Color(0xFFD63F98), radius = 5.dp.toPx(), center = Offset(316f * sx, 116f), style = Stroke(width = 2.5.dp.toPx()))
        }
        Row(modifier = Modifier.fillMaxWidth().padding(top = 2.dp)) {
            listOf("May", "Jun", "Jul", "Ago", "Sep", "Oct").forEachIndexed { index, month ->
                BasicText(
                    month,
                    modifier = Modifier.weight(1f),
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, color = Color(0xFF5F6377), textAlign = TextAlign.Center),
                )
            }
        }
    }
}

@Composable
private fun StatusLegend57(label: String, count: String, color: Color) {
    val typo = LocalRfTypography.current

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.size(10.dp).clip(RoundedCornerShape(3.dp)).background(color))
        BasicText(label, modifier = Modifier.weight(1f), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF151729)))
        BasicText(count, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF151729)))
    }
}

// ==============================================================================
// s58 · PANEL RESUMEN
// ==============================================================================
@Composable
internal fun RenderFinancialDashboardScreenS58(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    RfRadialBackgroundBox(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(56.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AdminPanelIconButton(RfIconKind.BACK) { onSelectScreenCode("BACK:s14") }
                BasicText(
                    "Panel",
                    modifier = Modifier.weight(1f),
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF151729)),
                )
                BasicText("Octubre 2026", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF5F6377)))
            }
            Spacer(modifier = Modifier.height(12.dp))
            AdminTabs58(activeCode = "s58", onSelectScreenCode = onSelectScreenCode)
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(18.dp, RoundedCornerShape(24.dp), spotColor = Color(0x996E82E6))
                        .clip(RoundedCornerShape(24.dp))
                        .background(AdminPanelGradient)
                        .padding(20.dp),
                ) {
                    Canvas(modifier = Modifier.matchParentSize()) {
                        val ox = size.width - 170f
                        val oy = size.height - 110f
                        val stroke = Path().apply {
                            moveTo(ox, oy + 100f)
                            cubicTo(ox + 30f, oy + 96f, ox + 50f, oy + 70f, ox + 80f, oy + 74f)
                            cubicTo(ox + 110f, oy + 78f, ox + 120f, oy + 40f, ox + 150f, oy + 30f)
                            cubicTo(ox + 180f, oy + 20f, ox + 175f, oy + 20f, ox + 190f, oy + 10f)
                        }
                        val area = Path().apply {
                            addPath(stroke)
                            lineTo(ox + 190f, oy + 130f)
                            lineTo(ox, oy + 130f)
                            close()
                        }
                        drawPath(area, Color(0x1FFFD45C))
                        drawPath(stroke, Color(0xFFFFD45C), style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            RfIcon(kind = RfIconKind.TREND_UP, tint = Color(0xFFF6EAFB), size = 16.dp)
                            BasicText("Ingresos del mes", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFFF6EAFB)))
                        }
                        Spacer(Modifier.height(8.dp))
                        BasicText("RD$ 12,450", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 34.sp, lineHeight = 38.sp, color = Color.White))
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(22.dp)),
                ) {
                    PanelSummaryRow58(
                        icon = RfIconKind.CALENDAR,
                        iconBackground = Color(0xFFECEBFF),
                        iconTint = Color(0xFF4B47D6),
                        title = "Eventos activos",
                        value = "4",
                        divider = false,
                    )
                    PanelSummaryRow58(
                        icon = RfIconKind.WARNING,
                        iconBackground = Color(0xFFFFF2C9),
                        iconTint = Color(0xFF7A5700),
                        title = "Siniestros pendientes",
                        link = "Revisar",
                        onLinkClick = { onSelectScreenCode("s60") },
                        value = "3",
                        divider = true,
                    )
                    PanelSummaryRow58(
                        icon = RfIconKind.USER,
                        iconBackground = Color(0xFFEEF6D6),
                        iconTint = Color(0xFF4E6A10),
                        title = "Clientes nuevos",
                        value = "2",
                        divider = true,
                    )
                }
            }
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun AdminTabs58(activeCode: String, onSelectScreenCode: (String) -> Unit) {
    val typo = LocalRfTypography.current
    val tabs = listOf("Resumen" to "s58", "Finanzas" to "s59", "Seguros" to "s60", "Auditoría" to "s61", "Clientes" to "s62")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                val stroke = 1.dp.toPx()
                drawLine(Color(0xFFE9E9F2), Offset(0f, size.height - stroke / 2f), Offset(size.width, size.height - stroke / 2f), stroke)
            },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            tabs.forEach { (label, code) ->
                val active = code == activeCode
                Box(
                    modifier = Modifier
                        .height(44.dp)
                        .clickable { if (!active) onSelectScreenCode(code) }
                        .drawBehind {
                            if (active) {
                                val stroke = 2.5.dp.toPx()
                                drawLine(Color(0xFF5B6CF0), Offset(0f, size.height - stroke / 2f), Offset(size.width, size.height - stroke / 2f), stroke)
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        label,
                        maxLines = 1,
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = if (active) Color(0xFF151729) else Color(0xFF5F6377),
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun PanelSummaryRow58(
    icon: RfIconKind,
    iconBackground: Color,
    iconTint: Color,
    title: String,
    value: String,
    divider: Boolean,
    link: String? = null,
    onLinkClick: () -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                if (divider) {
                    val stroke = 1.dp.toPx()
                    drawLine(Color(0xFFF0F0F6), Offset(0f, stroke / 2f), Offset(size.width, stroke / 2f), stroke)
                }
            }
            .heightIn(min = 72.dp)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(iconBackground),
            contentAlignment = Alignment.Center,
        ) {
            RfIcon(kind = icon, tint = iconTint, size = 20.dp)
        }
        if (link != null) {
            Column(modifier = Modifier.weight(1f)) {
                BasicText(title, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color(0xFF151729)))
                BasicText(
                    link,
                    modifier = Modifier.clickable(onClick = onLinkClick),
                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF7A5700)),
                )
            }
        } else {
            BasicText(
                title,
                modifier = Modifier.weight(1f),
                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color(0xFF151729)),
            )
        }
        BasicText(value, style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp, color = Color(0xFF151729)))
    }
}

// ==============================================================================
// s83 · RECIBO OFICIAL
// ==============================================================================
@Composable
internal fun RenderOfficialReceiptScreenS83(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    // Background: radial pink top-left + lavender top-right + #FAFAFC
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFC)),
    ) {
        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        ) {
            Spacer(modifier = Modifier.height(54.dp))

            // Header: back | "Recibo" + "No. RF-2026-0412" | share
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = false)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("BACK:s51") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1A1A2E), size = 22.dp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "Recibo",
                        style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, lineHeight = (20 * 1.1f).sp, color = Color(0xFF1A1A2E)),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                    )
                    BasicText(
                        text = "No. RF-2026-0412",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF62627A)),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                    )
                }
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = false)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.SEND, tint = Color(0xFF1A1A2E), size = 19.dp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Receipt card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = false)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .padding(horizontal = 18.dp, vertical = 20.dp),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    // Logo RF circle
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .shadow(elevation = 4.dp, shape = CircleShape, spotColor = Color(0x24B450B4))
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.dp, Color(0x25EA4EA9), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = "RF",
                            style = TextStyle(
                                fontFamily = typo.displayFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = Color(0xFFEA4EA9),
                            ),
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // "Pago recibido" badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color(0xFFEEF6D6))
                            .padding(horizontal = 9.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RfIcon(kind = RfIconKind.CHECK, tint = Color(0xFF4E6A10), size = 13.dp)
                            BasicText(
                                text = "Pago recibido",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF4E6A10)),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Amount
                    BasicText(
                        text = "RD$ 1,350",
                        style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 34.sp, lineHeight = 34.sp, color = Color(0xFF1A1A2E)),
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    BasicText(
                        text = "Anticipo (30%) · Boda Real Rosa & Dorado",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF62627A), textAlign = TextAlign.Center),
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Dashed divider
                    Canvas(modifier = Modifier.fillMaxWidth().height(1.5.dp)) {
                        drawLine(
                            color = Color(0xFFE6DEEA),
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)),
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Detail rows
                    val rows = listOf(
                        "Fecha" to "18 oct 2026 · 10:24 a. m.",
                        "Método" to "Transferencia · Banco Popular",
                        "A nombre de" to "Camila Rodríguez",
                        "Evento" to "Vie 23 oct · Jardín Botánico",
                        "Total del evento" to "RD$ 4,500",
                        "Pagado hasta hoy" to "RD$ 1,350",
                    )

                    rows.forEachIndexed { idx, (label, value) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp)
                                .then(if (idx > 0) Modifier.border(width = 1.dp, color = Color(0xFFF2F0F5), shape = RoundedCornerShape(0.dp)).padding(top = 1.dp) else Modifier),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            BasicText(label, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF62627A)))
                            BasicText(value, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1A1A2E)), modifier = Modifier.padding(start = 12.dp))
                        }
                    }

                    // Saldo pendiente (highlighted amber)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        BasicText("Saldo pendiente", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF62627A)))
                        BasicText(
                            text = "RD$ 3,150",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color(0xFF7A5700)),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer legal text
            BasicText(
                text = "Rosa Fiesta · RNC 1-31-00000-0 · Santo Domingo\nEste recibo no sustituye una factura con NCF.",
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.5f.sp, color = Color(0xFF62627A), textAlign = TextAlign.Center, lineHeight = (12.5f * 1.45f).sp),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(120.dp))
        }

        // Bottom buttons: "Compartir" (outline) | "Descargar PDF" (gradient)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 20.dp, vertical = 36.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Compartir — secondary outline button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
                    .rfIconButtonShadow(RoundedCornerShape(16.dp), isAdmin = false)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFEEE9F2), RoundedCornerShape(16.dp))
                    .clickable { },
                contentAlignment = Alignment.Center,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    RfIcon(kind = RfIconKind.SEND, tint = Color(0xFF1A1A2E), size = 18.dp)
                    BasicText("Compartir", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1A1A2E)))
                }
            }
            // Descargar PDF — primary gradient
            Box(
                modifier = Modifier
                    .weight(1.3f)
                    .height(54.dp)
                    .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = false)
                    .clip(RoundedCornerShape(16.dp))
                    .background(RosaButtonGradient)
                    .clickable { },
                contentAlignment = Alignment.Center,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    RfIcon(kind = RfIconKind.DOWNLOAD, tint = Color.White, size = 18.dp)
                    BasicText("Descargar PDF", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White))
                }
            }
        }
    }
}
