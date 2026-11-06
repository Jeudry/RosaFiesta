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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeudry.rosafiesta.designsystem.LocalRfPalette
import androidx.compose.ui.zIndex
import com.jeudry.rosafiesta.designsystem.LocalRfTypography
import com.jeudry.rosafiesta.designsystem.RfThemeState
import com.jeudry.rosafiesta.designsystem.RfColors
import com.jeudry.rosafiesta.designsystem.RfIcon
import com.jeudry.rosafiesta.designsystem.RfIconKind
import com.jeudry.rosafiesta.designsystem.RfPhoto
import com.jeudry.rosafiesta.designsystem.RfPhotoKey
import com.jeudry.rosafiesta.designsystem.RfRadialBackgroundBox
import com.jeudry.rosafiesta.designsystem.rfCardShadow
import com.jeudry.rosafiesta.designsystem.rfHeroBannerShadow
import com.jeudry.rosafiesta.designsystem.rfIconButtonShadow
import com.jeudry.rosafiesta.domain.validation.InputValidator
import com.jeudry.rosafiesta.designsystem.rfInputShadow
import com.jeudry.rosafiesta.designsystem.rfPrimaryButtonShadow
import com.jeudry.rosafiesta.domain.CanonicalSampleData
import com.jeudry.rosafiesta.domain.ClientEvent

private val PaymentBackgroundGradient = Brush.radialGradient(
    0.0f to RfThemeState.currentPalette.radialPrimary,
    0.6f to RfThemeState.currentPalette.radialPrimary.copy(alpha = 0f),
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
    val eventId: String,
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
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val eventsState = viewModel?.events?.collectAsState()?.value ?: CanonicalSampleData.events

    RfRadialBackgroundBox(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 30.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
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
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, RfThemeState.currentPalette.line, CircleShape)
                        .clickable { onSelectScreenCode("BACK:s37") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = RfThemeState.currentPalette.ink, size = 22.dp)
                }

                BasicText(
                    text = "Cotizaciones",
                    style = TextStyle(
                        fontFamily = typo.displayFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = RfThemeState.currentPalette.ink,
                    ),
                )
            }

            val quotes = eventsState.mapIndexed { index, event ->
                val isCompleted = event.statusLabel.contains("Completad", ignoreCase = true)
                val isConfirmed = event.statusLabel.contains("Confirmad", ignoreCase = true)
                val tagText = when {
                    isCompleted -> "Completada"
                    isConfirmed -> "Confirmada"
                    else -> "Revisada"
                }
                val tagBg = when {
                    isCompleted -> Color(0xFFEFE8FD)
                    isConfirmed -> Color(0xFFEEF6D6)
                    else -> Color(0xFFFFF2C9)
                }
                val tagTextCol = when {
                    isCompleted -> Color(0xFF7A4FD8)
                    isConfirmed -> Color(0xFF4E6A10)
                    else -> Color(0xFF7A5700)
                }
                val activeSteps = when {
                    isCompleted -> 5
                    isConfirmed -> 3
                    else -> 2
                }
                val stepColor = when {
                    isCompleted -> Color(0xFF865DEC)
                    isConfirmed -> Color(0xFF8DB52C)
                    else -> Color(0xFFF7B500)
                }
                val guestCount = when (index) {
                    0 -> 120
                    1 -> 150
                    else -> 200
                }
                QuoteHistoryItem49(
                    eventId = event.id,
                    title = event.title,
                    dateLocation = "${event.dateLabel} · ${event.venue}",
                    tagText = tagText,
                    tagBg = tagBg,
                    tagTextCol = tagTextCol,
                    stepText = "Paso $activeSteps de 5 · $tagText",
                    activeSteps = activeSteps,
                    stepColor = stepColor,
                    guests = "$guestCount invitados",
                    price = "RD$ ${event.totalDop}",
                )
            }

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
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(22.dp))
                        .clickable {
                            viewModel?.selectEvent(item.eventId)
                            onSelectScreenCode("s48")
                        }
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
                                        color = RfThemeState.currentPalette.ink,
                                    ),
                                )
                                BasicText(
                                    text = item.dateLocation,
                                    style = TextStyle(
                                        fontFamily = typo.bodyFamily,
                                        fontSize = 13.sp,
                                        color = RfThemeState.currentPalette.muted,
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
                                                if (i <= item.activeSteps) {
                                                    item.stepColor
                                                } else {
                                                    RfThemeState.currentPalette.chipBg
                                                },
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
                                    color = RfThemeState.currentPalette.muted,
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
                                RfIcon(kind = RfIconKind.GUESTS, tint = RfThemeState.currentPalette.muted, size = 16.dp)
                                BasicText(
                                    text = item.guests,
                                    style = TextStyle(
                                        fontFamily = typo.bodyFamily,
                                        fontSize = 14.sp,
                                        color = RfThemeState.currentPalette.muted,
                                    ),
                                )
                            }
                            BasicText(
                                text = item.price,
                                style = TextStyle(
                                    fontFamily = typo.displayFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp,
                                    color = RfThemeState.currentPalette.ink,
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
    val event = viewModel?.activeClientEvent() ?: events.firstOrNull { currentEvent -> currentEvent.id == selectedId } ?: events.first()
    val profilePhone = viewModel?.profilePhone?.collectAsState()?.value ?: "(809) 555-0142"
    val depositAmount = (event.totalDop * 30 / 100).coerceAtLeast(100)
    var cardHolderName by remember(event.clientName) { mutableStateOf(event.clientName) }
    var cardNumberText by remember { mutableStateOf("•••• •••• •••• 4242") }
    var cardExpiryText by remember { mutableStateOf("12/28") }
    var cardCvcText by remember { mutableStateOf("•••") }
    var contactPhoneText by remember(profilePhone) { mutableStateOf(profilePhone) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RfThemeState.currentPalette.bg),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(Brush.radialGradient(colors = listOf(RfThemeState.currentPalette.radialPrimary, RfThemeState.currentPalette.radialPrimary.copy(alpha = 0f)), center = Offset(0f, 0f), radius = size.width * 0.9f))
            drawRect(Brush.radialGradient(colors = listOf(RfThemeState.currentPalette.radialSecondary, RfThemeState.currentPalette.radialSecondary.copy(alpha = 0f)), center = Offset(size.width, 0f), radius = size.width * 0.8f))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 120.dp),
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
                        .background(RfThemeState.currentPalette.surface)
                        .clickable { onSelectScreenCode("BACK:s48") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = RfThemeState.currentPalette.ink, size = 22.dp)
                }

                BasicText(
                    text = "Pago",
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = RfThemeState.currentPalette.ink),
                    modifier = Modifier.weight(1f),
                )

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    RfIcon(kind = RfIconKind.SHIELD, tint = RfThemeState.currentPalette.muted, size = 14.dp)
                    BasicText("Pago seguro", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = RfThemeState.currentPalette.muted))
                }
            }

            // Delivery section card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(18.dp), isAdmin = false)
                    .clip(RoundedCornerShape(18.dp))
                    .background(RfThemeState.currentPalette.surface)
                    .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(18.dp))
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
                        BasicText("Entrega para ${event.clientName}", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = RfThemeState.currentPalette.muted))
                        BasicText(event.venue, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = RfThemeState.currentPalette.ink))
                    }

                    BasicText(
                        "Cambiar",
                        modifier = Modifier.clickable { onSelectScreenCode("s40") },
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFC2338F)),
                    )
                }
            }

            // Payment methods title
            BasicText("Método de pago", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = RfThemeState.currentPalette.ink))

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
                                    .background(RfThemeState.currentPalette.surface)
                                    .then(
                                        if (isSel) {
                                            Modifier.border(2.dp, Color(0xFFB754CB), RoundedCornerShape(16.dp))
                                        } else {
                                            Modifier.border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(16.dp))
                                        },
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
                                        kind = if (name == "Tarjeta") {
                                            RfIconKind.WALLET
                                        } else if (name == "Efectivo") {
                                            RfIconKind.WALLET
                                        } else {
                                            RfIconKind.ORDERS
                                        },
                                        tint = RfThemeState.currentPalette.ink,
                                        size = 20.dp,
                                    )
                                    BasicText(
                                        text = name,
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = RfThemeState.currentPalette.ink),
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
            BasicText("Datos de la tarjeta", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = RfThemeState.currentPalette.ink))

            // Name on card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .rfInputShadow(RoundedCornerShape(14.dp), isAdmin = false)
                    .clip(RoundedCornerShape(14.dp))
                    .background(RfThemeState.currentPalette.surface)
                    .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    RfIcon(kind = RfIconKind.USER, tint = RfThemeState.currentPalette.muted, size = 18.dp)
                    BasicTextField(
                        value = cardHolderName,
                        onValueChange = { updatedName -> cardHolderName = updatedName },
                        singleLine = true,
                        textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = RfThemeState.currentPalette.ink),
                        cursorBrush = SolidColor(Color(0xFFC2338F)),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            // Card number
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .rfInputShadow(RoundedCornerShape(14.dp), isAdmin = false)
                    .clip(RoundedCornerShape(14.dp))
                    .background(RfThemeState.currentPalette.surface)
                    .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    RfIcon(kind = RfIconKind.WALLET, tint = RfThemeState.currentPalette.muted, size = 18.dp)
                    BasicTextField(
                        value = cardNumberText,
                        onValueChange = { updatedCard -> cardNumberText = updatedCard },
                        singleLine = true,
                        textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = RfThemeState.currentPalette.ink),
                        cursorBrush = SolidColor(Color(0xFFC2338F)),
                        modifier = Modifier.fillMaxWidth(),
                    )
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
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    BasicTextField(
                        value = cardExpiryText,
                        onValueChange = { updatedExpiry -> cardExpiryText = updatedExpiry },
                        singleLine = true,
                        textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = RfThemeState.currentPalette.ink),
                        cursorBrush = SolidColor(Color(0xFFC2338F)),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .rfInputShadow(RoundedCornerShape(14.dp), isAdmin = false)
                        .clip(RoundedCornerShape(14.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    BasicTextField(
                        value = cardCvcText,
                        onValueChange = { updatedCvc -> cardCvcText = updatedCvc },
                        singleLine = true,
                        textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = RfThemeState.currentPalette.ink),
                        cursorBrush = SolidColor(Color(0xFFC2338F)),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            // Contact phone
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .rfInputShadow(RoundedCornerShape(14.dp), isAdmin = false)
                    .clip(RoundedCornerShape(14.dp))
                    .background(RfThemeState.currentPalette.surface)
                    .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    RfIcon(kind = RfIconKind.PHONE, tint = RfThemeState.currentPalette.muted, size = 18.dp)
                    BasicTextField(
                        value = contactPhoneText,
                        onValueChange = { updatedPhone -> contactPhoneText = updatedPhone },
                        singleLine = true,
                        textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = RfThemeState.currentPalette.ink),
                        cursorBrush = SolidColor(Color(0xFFC2338F)),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        // Bottom fixed payment CTA bar
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(RfThemeState.currentPalette.surface)
                .border(1.dp, RfThemeState.currentPalette.line)
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
                        val methodLabels = listOf("Tarjeta", "Banco Popular", "Banreservas", "Efectivo")
                        val chosenMethod = methodLabels.getOrElse(selectedMethod) { "Tarjeta" }
                        viewModel?.setPaymentAmountInput(depositAmount.toString())
                        viewModel?.setPaymentMethodInput(chosenMethod)
                        viewModel?.setSavedPaymentMethod(chosenMethod)
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
    val event = viewModel?.activeClientEvent() ?: events.firstOrNull { currentEvent -> currentEvent.id == selectedId } ?: events.first()
    val depositPaid = if (event.paidDepositDop > 0) event.paidDepositDop else (event.totalDop * 30 / 100).coerceAtLeast(100)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RfThemeState.currentPalette.bg),
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
                color = RfThemeState.currentPalette.ink.copy(alpha = 0.18f),
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
                .padding(top = 48.dp, bottom = 148.dp),
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

                BasicText("Todo listo para ${event.dateLabel} · ${event.venue}.", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = RfThemeState.currentPalette.muted))
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                        .background(RfThemeState.currentPalette.surface),
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
                                BasicText(event.title, style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 19.sp, lineHeight = 22.sp, color = RfThemeState.currentPalette.ink))
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
                                BasicText("Fecha", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = RfThemeState.currentPalette.muted))
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
                                BasicText("Artículos", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = RfThemeState.currentPalette.muted))
                                BasicText("${event.articleCount}", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = RfThemeState.currentPalette.ink))
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth().height(30.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                BasicText("Anticipo pagado", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = RfThemeState.currentPalette.muted))
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
                        .background(RfThemeState.currentPalette.bg),
                )

                // Ticket Notch on Right
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(24.dp)
                        .offset(x = 12.dp, y = 80.dp)
                        .clip(CircleShape)
                        .background(RfThemeState.currentPalette.bg),
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(2f)
                .background(RfThemeState.currentPalette.bg)
                .padding(top = 10.dp, start = 20.dp, end = 20.dp, bottom = 18.dp)
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
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, Color(0xFFECE6F0), RoundedCornerShape(16.dp))
                        .clickable {
                            viewModel?.exportReceiptDocument(event)
                            onSelectScreenCode("s83")
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        RfIcon(kind = RfIconKind.SHARE, tint = RfThemeState.currentPalette.ink, size = 18.dp)
                        BasicText("Compartir", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = RfThemeState.currentPalette.ink))
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(RfThemeState.currentPalette.chipBg)
                        .clickable { onSelectScreenCode("s18") },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText("Ir al catálogo", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = RfThemeState.currentPalette.ink))
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
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val events = viewModel?.events?.collectAsState()?.value ?: CanonicalSampleData.events
    val selectedId = viewModel?.selectedEventId?.collectAsState()?.value ?: "evt-boda-real"
    val event = viewModel?.activeClientEvent() ?: events.firstOrNull { currentEvent -> currentEvent.id == selectedId } ?: events.first()
    val tasks = viewModel?.eventTasksList?.collectAsState()?.value ?: emptyList()
    val timeline = viewModel?.eventTimelineItems?.collectAsState()?.value ?: emptyList()
    val completedTaskCount = tasks.count { taskItem -> taskItem.completed }
    val taskSummaryText = if (tasks.isEmpty()) "Sin tareas" else "$completedTaskCount de ${tasks.size} listas"
    val timelineSummaryText = if (timeline.isEmpty()) "Sin actividades" else "${timeline.size} actividades"
    val estimatedBudget = event.totalDop.coerceAtLeast(1850)
    val realBudget = estimatedBudget + 35

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RfThemeState.currentPalette.bg),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(Brush.radialGradient(colors = listOf(RfThemeState.currentPalette.radialPrimary, RfThemeState.currentPalette.radialPrimary.copy(alpha = 0f)), center = Offset(0f, 0f), radius = size.width * 0.9f))
            drawRect(Brush.radialGradient(colors = listOf(RfThemeState.currentPalette.radialSecondary, RfThemeState.currentPalette.radialSecondary.copy(alpha = 0f)), center = Offset(size.width, 0f), radius = size.width * 0.8f))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 104.dp),
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
                        .background(RfThemeState.currentPalette.surface)
                        .clickable { onSelectScreenCode("BACK:s40") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = RfThemeState.currentPalette.ink, size = 22.dp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "Así salió tu evento",
                        style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, lineHeight = 22.sp, color = RfThemeState.currentPalette.ink),
                    )
                    BasicText(
                        text = "${event.title} · ${event.dateLabel}",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = RfThemeState.currentPalette.muted),
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
                        BasicText("100%", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 44.sp, lineHeight = 44.sp, color = RfThemeState.currentPalette.ink))
                        BasicText("Todo llegó a tiempo", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = RfThemeState.currentPalette.muted))
                    }
                }
            }

            // Presupuesto card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(RfThemeState.currentPalette.surface)
                    .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(22.dp))
                    .padding(horizontal = 18.dp, vertical = 16.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    BasicText("Presupuesto", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = RfThemeState.currentPalette.ink))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            BasicText("Estimado", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = RfThemeState.currentPalette.muted))
                            BasicText("RD$ $estimatedBudget", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = RfThemeState.currentPalette.ink))
                        }
                        Box(modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)).background(Color(0xFFE6DEEA)))
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            BasicText("Real", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = RfThemeState.currentPalette.muted))
                            BasicText("RD$ $realBudget", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = RfThemeState.currentPalette.ink))
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
                    .background(RfThemeState.currentPalette.surface)
                    .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(22.dp))
                    .padding(vertical = 4.dp),
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp).clickable { onSelectScreenCode("s45") },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        BasicText("Tareas completadas", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = RfThemeState.currentPalette.ink))
                        BasicText(taskSummaryText, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = RfThemeState.currentPalette.muted))
                    }
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(RfThemeState.currentPalette.lineSoft))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp).clickable { onSelectScreenCode("s44") },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        BasicText("Cronograma ejecutado", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = RfThemeState.currentPalette.ink))
                        BasicText(timelineSummaryText, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = RfThemeState.currentPalette.muted))
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
                .clickable { onSelectScreenCode("s77") },
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
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    var selectedTab by remember { mutableStateOf("Próximas") }
    val eventsState = viewModel?.events?.collectAsState()?.value ?: CanonicalSampleData.events
    val userEvents = viewModel?.clientEventsForCurrentUser() ?: eventsState

    RfRadialBackgroundBox(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Spacer(modifier = Modifier.height(16.dp))
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
                        .background(RfThemeState.currentPalette.surface)
                        .clickable { onSelectScreenCode("BACK:s54") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = RfThemeState.currentPalette.ink, size = 22.dp)
                }
                BasicText(
                    text = "Mis reservas",
                    modifier = Modifier.weight(1f),
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = RfThemeState.currentPalette.ink),
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(RfThemeState.currentPalette.chipBg)
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
                            .background(if (selected) RfThemeState.currentPalette.surface else Color.Transparent)
                            .clickable { selectedTab = label },
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            label,
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = if (selected) RfThemeState.currentPalette.ink else RfThemeState.currentPalette.muted,
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
                val filteredEvents = when (selectedTab) {
                    "Próximas" -> userEvents.filter { eventItem -> eventItem.statusLabel != "Borrador" }
                    "Pasadas" -> userEvents.filter { eventItem -> eventItem.statusLabel == "Borrador" }
                    else -> userEvents
                }.ifEmpty { userEvents }

                filteredEvents.forEach { eventItem ->
                    val totalAmount = eventItem.totalDop.coerceAtLeast(1850)
                    val paidAmount = if (eventItem.paidDepositDop > 0) eventItem.paidDepositDop else (totalAmount * 30 / 100)
                    val remainingAmount = (totalAmount - paidAmount).coerceAtLeast(0)
                    val progressFraction = (paidAmount.toFloat() / totalAmount.toFloat()).coerceIn(0.1f, 1.0f)
                    val lowerTitle = eventItem.title.lowercase()
                    val isCorporateEvent = listOf("corp", "gala").any { keyword -> lowerTitle.contains(keyword) }
                    val photoKey = if (lowerTitle.contains("boda")) {
                        RfPhotoKey.BODA
                    } else if (lowerTitle.contains("quince")) {
                        RfPhotoKey.QUINCEANERA
                    } else if (lowerTitle.contains("baby")) {
                        RfPhotoKey.BABY_SHOWER
                    } else if (isCorporateEvent) {
                        RfPhotoKey.CORPORATIVO
                    } else {
                        RfPhotoKey.CUMPLE_INFANTIL
                    }
                    val isConfirmed = eventItem.statusLabel == "Confirmado"
                    val isDraft = eventItem.statusLabel == "Borrador"
                    val tagBg = if (isConfirmed) {
                        Color(0xFFEEF6D6)
                    } else if (isDraft) {
                        RfThemeState.currentPalette.chipBg
                    } else {
                        Color(0xFFDDF4F3)
                    }
                    val tagTextColor = if (isConfirmed) {
                        Color(0xFF4E6A10)
                    } else if (isDraft) {
                        RfThemeState.currentPalette.muted
                    } else {
                        Color(0xFF1F8A84)
                    }
                    ReservationCard53(
                        title = eventItem.title,
                        meta = "${eventItem.dateLabel} · ${eventItem.venue}",
                        tag = eventItem.statusLabel,
                        tagBackground = tagBg,
                        tagColor = tagTextColor,
                        photoKey = photoKey,
                        paid = "RD$ $paidAmount",
                        total = "RD$ $totalAmount",
                        remaining = if (remainingAmount > 0) "Resta RD$ $remainingAmount" else "Saldo al día",
                        progress = progressFraction,
                        onClick = {
                            viewModel?.selectEvent(eventItem.id)
                            onSelectScreenCode("s49")
                        },
                    )
                }
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
            .background(RfThemeState.currentPalette.surface)
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
                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = RfThemeState.currentPalette.ink),
                )
                BasicText(
                    meta,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = RfThemeState.currentPalette.muted),
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
                .background(RfThemeState.currentPalette.bg)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                BasicText(
                    buildAnnotatedString {
                        withStyle(SpanStyle(color = RfThemeState.currentPalette.muted)) { append("Pagado ") }
                        withStyle(SpanStyle(color = RfThemeState.currentPalette.ink, fontWeight = FontWeight.Bold)) { append(paid) }
                    },
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp),
                )
                BasicText(
                    buildAnnotatedString {
                        withStyle(SpanStyle(color = RfThemeState.currentPalette.muted)) { append("de ") }
                        withStyle(SpanStyle(color = RfThemeState.currentPalette.ink, fontWeight = FontWeight.Bold)) { append(total) }
                    },
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp),
                )
            }
            Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFFEADFE7))) {
                Box(modifier = Modifier.fillMaxWidth(progress).height(6.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFF8DB52C)))
            }
            BasicText(remaining, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = RfThemeState.currentPalette.muted))
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
    val palette = LocalRfPalette.current
    val profileName = viewModel?.profileName?.collectAsState()?.value ?: "Invitado Rosa Fiesta"
    val profileEmail = viewModel?.profileEmail?.collectAsState()?.value ?: "cliente@rosafiesta.do"
    val profilePhone = viewModel?.profilePhone?.collectAsState()?.value ?: "809-555-0142"
    val profileUsername = viewModel?.profileUsername?.collectAsState()?.value ?: profileEmail.substringBefore("@")
    val profileBirthDate = viewModel?.profileBirthDate?.collectAsState()?.value ?: ""
    val profileAvatarKey = viewModel?.profileAvatarKey?.collectAsState()?.value
    val formError = viewModel?.formError?.collectAsState()?.value
    val formFeedback = viewModel?.formFeedback?.collectAsState()?.value

    var isEditing by remember { mutableStateOf(false) }
    var draftName by remember(profileName) { mutableStateOf(profileName) }
    var draftEmail by remember(profileEmail) { mutableStateOf(profileEmail) }
    var draftPhone by remember(profilePhone) { mutableStateOf(profilePhone) }
    var draftBirthDate by remember(profileBirthDate) { mutableStateOf(profileBirthDate.ifBlank { "22/09/1996" }) }
    var draftUsername by remember(profileUsername, profileEmail) {
        mutableStateOf(profileUsername.ifBlank { profileEmail.substringBefore("@") })
    }
    var draftAvatarKey by remember(profileAvatarKey) { mutableStateOf(profileAvatarKey) }
    var showBirthDateCalendar by remember { mutableStateOf(false) }
    var showPhotoPickerModal by remember { mutableStateOf(false) }
    var localProfileError by remember { mutableStateOf<String?>(null) }

    val liveNameError = if (isEditing) InputValidator.validateFullName(draftName) else null
    val liveEmailError = if (isEditing) InputValidator.validateEmail(draftEmail) else null
    val livePhoneError = if (isEditing) InputValidator.validatePhone(draftPhone) else null
    val liveBirthDateError = if (isEditing) InputValidator.validateBirthDate(draftBirthDate, isRequired = true) else null
    val liveUsernameError = if (isEditing) InputValidator.validateUsername(draftUsername) else null

    val displayedNameForInitials = if (isEditing) draftName else profileName
    val initials = displayedNameForInitials
        .split(" ")
        .mapNotNull { word -> word.firstOrNull()?.uppercaseChar()?.toString() }
        .take(2)
        .joinToString("")
        .ifEmpty { "RF" }

    RfRadialBackgroundBox(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = false)
                        .clip(CircleShape)
                        .background(palette.surface)
                        .border(1.dp, palette.line, CircleShape)
                        .clickable {
                            viewModel?.clearFormError()
                            onSelectScreenCode("BACK:s14")
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = palette.ink, size = 22.dp)
                }
                BasicText(
                    text = if (isEditing) "Editar perfil" else "Mi perfil",
                    modifier = Modifier.weight(1f),
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = palette.ink),
                )
                if (isEditing) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .rfIconButtonShadow(CircleShape, isAdmin = false)
                            .clip(CircleShape)
                            .background(palette.surface)
                            .border(1.dp, palette.line, CircleShape)
                            .clickable {
                                draftName = profileName
                                draftEmail = profileEmail
                                draftPhone = profilePhone
                                draftBirthDate = profileBirthDate.ifBlank { "22/09/1996" }
                                draftUsername = profileUsername.ifBlank { profileEmail.substringBefore("@") }
                                draftAvatarKey = profileAvatarKey
                                localProfileError = null
                                viewModel?.clearFormError()
                                isEditing = false
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(
                            kind = RfIconKind.CLOSE,
                            tint = palette.muted,
                            size = 18.dp,
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = false)
                        .clip(CircleShape)
                        .background(if (isEditing) Color(0xFFFDE6F2) else palette.surface)
                        .border(1.dp, palette.line, CircleShape)
                        .clickable {
                            if (!isEditing) {
                                draftName = profileName
                                draftEmail = profileEmail
                                draftPhone = profilePhone
                                draftBirthDate = profileBirthDate.ifBlank { "22/09/1996" }
                                draftUsername = profileUsername.ifBlank { profileEmail.substringBefore("@") }
                                draftAvatarKey = profileAvatarKey
                                localProfileError = null
                                viewModel?.clearFormError()
                                isEditing = true
                            } else {
                                val firstValidationError = liveNameError
                                    ?: liveEmailError
                                    ?: livePhoneError
                                    ?: liveBirthDateError
                                    ?: liveUsernameError
                                if (firstValidationError != null) {
                                    localProfileError = firstValidationError
                                    viewModel?.soundPlayer?.playAlertError()
                                } else {
                                    val savedOk = viewModel?.saveProfile(
                                        draftName = draftName,
                                        draftEmail = draftEmail,
                                        draftPhone = draftPhone,
                                        draftBirthDate = draftBirthDate,
                                        draftUsername = draftUsername,
                                        draftAvatarKey = draftAvatarKey,
                                    ) ?: true
                                    if (savedOk) {
                                        localProfileError = null
                                        isEditing = false
                                    } else {
                                        localProfileError = viewModel.formError.value
                                    }
                                }
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(
                        kind = if (isEditing) RfIconKind.CHECK else RfIconKind.EDIT,
                        tint = if (isEditing) Color(0xFFC2338F) else palette.ink,
                        size = 20.dp,
                    )
                }
            }

            val activeErrorBanner = localProfileError ?: formError
            if (!activeErrorBanner.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFFFEDF1))
                        .border(1.dp, RfColors.Error.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                ) {
                    BasicText(
                        text = activeErrorBanner,
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = RfColors.Error,
                        ),
                    )
                }
            } else {
                val hasSuccessFeedback = !formFeedback.isNullOrBlank()
                val showSuccessBanner = if (hasSuccessFeedback) !isEditing else false
                if (showSuccessBanner) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFEEF6D6))
                            .border(1.dp, Color(0xFF4E6A10).copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                    ) {
                        BasicText(
                            text = formFeedback.orEmpty(),
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = Color(0xFF4E6A10),
                            ),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 4.dp, bottom = 6.dp),
                verticalAlignment = if (isEditing) Alignment.Top else Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                RfEditableProfileAvatar(
                    initials = initials,
                    avatarKey = if (isEditing) draftAvatarKey else profileAvatarKey,
                    size = 84.dp,
                    isEditing = isEditing,
                    onClickEditPhoto = { showPhotoPickerModal = true },
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(if (isEditing) 8.dp else 2.dp),
                ) {
                    if (isEditing) {
                        ProfileHeaderEditableField54(
                            label = "Nombre completo (2–60)",
                            value = draftName,
                            placeholder = "Ej. Camila Rodríguez",
                            errorText = liveNameError,
                            maxLength = InputValidator.MAX_FULL_NAME_LENGTH,
                            isTitleStyle = true,
                            onValueChange = { nextName ->
                                draftName = nextName.take(InputValidator.MAX_FULL_NAME_LENGTH)
                                localProfileError = null
                                viewModel?.clearFormError()
                            },
                        )
                        ProfileHeaderEditableField54(
                            label = "Correo electrónico",
                            value = draftEmail,
                            placeholder = "usuario@dominio.com",
                            errorText = liveEmailError,
                            maxLength = InputValidator.MAX_EMAIL_LENGTH,
                            isTitleStyle = false,
                            onValueChange = { nextEmail ->
                                draftEmail = nextEmail.take(InputValidator.MAX_EMAIL_LENGTH)
                                localProfileError = null
                                viewModel?.clearFormError()
                            },
                        )
                    } else {
                        BasicText(
                            profileName,
                            style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, lineHeight = 24.sp, color = palette.ink),
                        )
                        BasicText(
                            profileEmail,
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = palette.muted),
                        )
                    }
                }
            }

            ProfileCaption54(if (isEditing) "DATOS (HAZ CLIC EN CADA CAMPO PARA EDITAR)" else "DATOS")
            Spacer(Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(palette.surface)
                    .border(1.dp, palette.line, RoundedCornerShape(20.dp)),
            ) {
                ProfileRow54("Teléfono", RfIconKind.PHONE) {
                    if (isEditing) {
                        ProfileInlineInputBox54(
                            value = draftPhone,
                            placeholder = "809-555-0102",
                            hasError = livePhoneError != null,
                            onValueChange = { nextPhone ->
                                draftPhone = nextPhone.take(InputValidator.MAX_PHONE_LENGTH)
                                localProfileError = null
                                viewModel?.clearFormError()
                            },
                        )
                    } else {
                        BasicText(profilePhone, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFC2338F)))
                    }
                }
                if (isEditing) {
                    ProfileInlineFieldHelper54(
                        errorText = livePhoneError,
                        hintText = "Formato: 10 a 15 dígitos (ej. 809-555-0102)",
                    )
                }
                ProfileDivider54()
                ProfileRow54(
                    label = "Cumpleaños",
                    icon = RfIconKind.CAKE,
                    onClick = if (isEditing) {
                        { showBirthDateCalendar = true }
                    } else {
                        null
                    },
                ) {
                    if (isEditing) {
                        RfMaskedDateField(
                            value = draftBirthDate,
                            placeholder = "DD/MM/AAAA",
                            hasError = liveBirthDateError != null,
                            compactInline = true,
                            modifier = Modifier.width(184.dp),
                            onValueChange = { formattedBirthDate ->
                                draftBirthDate = formattedBirthDate.take(InputValidator.MAX_BIRTH_DATE_LENGTH)
                                localProfileError = null
                                viewModel?.clearFormError()
                            },
                            onOpenCalendar = { showBirthDateCalendar = true },
                        )
                    } else {
                        BasicText(
                            profileBirthDate.ifBlank { "Agregar" },
                            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFC2338F)),
                        )
                    }
                }
                if (isEditing) {
                    ProfileInlineFieldHelper54(
                        errorText = liveBirthDateError,
                        hintText = "Máscara automática DD/MM/AAAA o toca el icono de calendario",
                    )
                }
                ProfileDivider54()
                ProfileRow54("Usuario", RfIconKind.USER) {
                    if (isEditing) {
                        ProfileInlineInputBox54(
                            value = draftUsername,
                            placeholder = "usuario",
                            hasError = liveUsernameError != null,
                            onValueChange = { nextUsername ->
                                draftUsername = nextUsername.take(InputValidator.MAX_USERNAME_LENGTH)
                                localProfileError = null
                                viewModel?.clearFormError()
                            },
                        )
                    } else {
                        BasicText(
                            profileUsername.ifBlank { profileEmail.substringBefore("@") },
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = palette.muted),
                        )
                    }
                }
                if (isEditing) {
                    ProfileInlineFieldHelper54(
                        errorText = liveUsernameError,
                        hintText = "3 a 24 caracteres (letras, números, ., -, _)",
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(palette.surface)
                    .border(1.dp, palette.line, RoundedCornerShape(20.dp)),
            ) {
                ProfileRow54(
                    "Cerrar sesión",
                    RfIconKind.LOGOUT,
                    labelColor = Color(0xFFB42345),
                    onClick = {
                        viewModel?.logoutSession()
                        onSelectScreenCode("s07")
                    },
                ) {}
            }
            Spacer(modifier = Modifier.height(30.dp))
        }

        if (showBirthDateCalendar) {
            RfInteractiveCalendarModal(
                title = "Cumpleaños",
                subtitle = "Selecciona tu día, mes y año de nacimiento",
                currentDateText = draftBirthDate,
                minYear = InputValidator.MIN_BIRTH_YEAR,
                maxYear = InputValidator.MAX_BIRTH_YEAR,
                defaultYear = 1996,
                quickYears = listOf(1985, 1990, 1995, 1996, 2000, 2004, 2008),
                isAdmin = false,
                onSelectDate = { selectedDate ->
                    draftBirthDate = selectedDate
                    localProfileError = null
                    viewModel?.clearFormError()
                    showBirthDateCalendar = false
                },
                onDismiss = { showBirthDateCalendar = false },
            )
        }

        if (showPhotoPickerModal) {
            RfProfilePhotoPickerModal(
                initials = initials,
                currentAvatarKey = if (isEditing) draftAvatarKey else profileAvatarKey,
                onSelectAvatarKey = { chosenKey ->
                    draftAvatarKey = chosenKey
                    viewModel?.setProfileAvatarKey(chosenKey)
                    showPhotoPickerModal = false
                },
                onDismiss = { showPhotoPickerModal = false },
            )
        }
    }
}

@Composable
private fun ProfileCaption54(label: String) {
    val typo = LocalRfTypography.current
    val palette = LocalRfPalette.current

    BasicText(
        label,
        modifier = Modifier.padding(horizontal = 20.dp),
        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp, color = palette.muted),
    )
}

@Composable
private fun ProfileDivider54() {
    val palette = LocalRfPalette.current
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(palette.line))
}

@Composable
private fun ProfileRow54(
    label: String,
    icon: RfIconKind,
    labelColor: Color = LocalRfPalette.current.ink,
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

@Composable
private fun ProfileHeaderEditableField54(
    label: String,
    value: String,
    placeholder: String,
    errorText: String?,
    maxLength: Int,
    isTitleStyle: Boolean,
    onValueChange: (String) -> Unit,
) {
    val typo = LocalRfTypography.current
    val palette = LocalRfPalette.current
    val hasError = errorText != null
    val borderColor = if (hasError) RfColors.Error else Color(0xFFD6B9E8)
    val bgColor = if (hasError) {
        if (palette.isDark) Color(0xFF3A1D28) else Color(0xFFFFF7F9)
    } else {
        palette.surface
    }

    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicText(
                text = label,
                style = TextStyle(
                    fontFamily = typo.bodyFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (hasError) RfColors.Error else Color(0xFF945AE1),
                ),
            )
            BasicText(
                text = "${value.length}/$maxLength",
                style = TextStyle(
                    fontFamily = typo.bodyFamily,
                    fontSize = 10.5.sp,
                    color = palette.muted,
                ),
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(bgColor)
                .border(1.4.dp, borderColor, RoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (value.isEmpty()) {
                    BasicText(
                        text = placeholder,
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = if (isTitleStyle) 15.sp else 13.5.sp,
                            color = palette.muted,
                        ),
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontFamily = if (isTitleStyle) typo.displayFamily else typo.bodyFamily,
                        fontWeight = if (isTitleStyle) FontWeight.Bold else FontWeight.Medium,
                        fontSize = if (isTitleStyle) 15.5.sp else 13.5.sp,
                        color = palette.ink,
                    ),
                    cursorBrush = SolidColor(Color(0xFFC2338F)),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            RfIcon(
                kind = RfIconKind.EDIT,
                tint = if (hasError) RfColors.Error else Color(0xFFC2338F),
                size = 14.dp,
            )
        }
        if (errorText != null) {
            BasicText(
                text = errorText,
                style = TextStyle(
                    fontFamily = typo.bodyFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = RfColors.Error,
                ),
            )
        }
    }
}

@Composable
private fun ProfileInlineInputBox54(
    value: String,
    placeholder: String,
    hasError: Boolean,
    onValueChange: (String) -> Unit,
) {
    val typo = LocalRfTypography.current
    val palette = LocalRfPalette.current
    val borderColor = if (hasError) RfColors.Error else Color(0xFFD6B9E8)
    val bgColor = if (hasError) {
        if (palette.isDark) Color(0xFF3A1D28) else Color(0xFFFFF5F7)
    } else if (palette.isDark) {
        Color(0xFF26243D)
    } else {
        RfThemeState.currentPalette.bg
    }

    Row(
        modifier = Modifier
            .width(184.dp)
            .height(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(1.4.dp, borderColor, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart,
        ) {
            if (value.isEmpty()) {
                BasicText(
                    text = placeholder,
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontSize = 13.5.sp,
                        color = palette.muted,
                    ),
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(
                    fontFamily = typo.bodyFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.5.sp,
                    color = palette.ink,
                ),
                cursorBrush = SolidColor(Color(0xFFC2338F)),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        RfIcon(
            kind = RfIconKind.EDIT,
            tint = if (hasError) RfColors.Error else Color(0xFFC2338F),
            size = 13.dp,
        )
    }
}

@Composable
private fun ProfileInlineFieldHelper54(
    errorText: String?,
    hintText: String,
) {
    val typo = LocalRfTypography.current
    val palette = LocalRfPalette.current
    val displayColor = if (errorText != null) RfColors.Error else palette.muted
    val displayWeight = if (errorText != null) FontWeight.SemiBold else FontWeight.Normal

    BasicText(
        text = errorText ?: hintText,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 48.dp, end = 16.dp, bottom = 8.dp),
        style = TextStyle(
            fontFamily = typo.bodyFamily,
            fontSize = 11.sp,
            fontWeight = displayWeight,
            color = displayColor,
        ),
    )
}

// ==============================================================================
// s55 · PROVEEDORES ASOCIADOS
// ==============================================================================
@Composable
internal fun RenderSuppliersScreenS55(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    var searchQuery by remember { mutableStateOf("") }

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
            Spacer(modifier = Modifier.height(16.dp))
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
                        .background(RfThemeState.currentPalette.surface)
                        .clickable { onSelectScreenCode("BACK:A12") },
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
                    RfIcon(kind = RfIconKind.PLUS, tint = Color(0xFFFFFFFF), size = 20.dp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(RfThemeState.currentPalette.surface)
                    .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                RfIcon(kind = RfIconKind.SEARCH, tint = Color(0xFF5F6377), size = 18.dp)
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (searchQuery.isEmpty()) {
                        BasicText("Buscar proveedor", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF5F6377)))
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { updatedText -> searchQuery = updatedText },
                        singleLine = true,
                        textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF151729)),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                val trimmedSupplierQuery = searchQuery.trim()
                val matchesFlores = if (trimmedSupplierQuery.isEmpty()) {
                    true
                } else {
                    "Flores del Jarabacoa Premium Doña Carmen Polanco".contains(trimmedSupplierQuery, ignoreCase = true)
                }
                if (matchesFlores) {
                    SupplierCard55(
                        initial = "F",
                        initialBackground = Color(0xFFECEBFF),
                        initialColor = Color(0xFF4B47D6),
                        name = "Flores del Jarabacoa Premium",
                        contact = "Doña Carmen Polanco",
                        phone = "+1 809 555 2201",
                        onEditClick = { onSelectScreenCode("s56") },
                        onChatClick = { onSelectScreenCode("A15") },
                    )
                }
                val matchesCristaleria = if (trimmedSupplierQuery.isEmpty()) {
                    true
                } else {
                    "Cristalería & Eventos Caribe Ing. Marcos Tavares".contains(trimmedSupplierQuery, ignoreCase = true)
                }
                if (matchesCristaleria) {
                    SupplierCard55(
                        initial = "C",
                        initialBackground = Color(0xFFDDF4F3),
                        initialColor = Color(0xFF1F8A84),
                        name = "Cristalería & Eventos Caribe",
                        contact = "Ing. Marcos Tavares",
                        phone = "+1 809 555 2202",
                        onEditClick = { onSelectScreenCode("s56") },
                        onChatClick = { onSelectScreenCode("A15") },
                    )
                }
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
    onEditClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(RfThemeState.currentPalette.surface)
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
                modifier = Modifier.size(36.dp).clip(CircleShape).clickable(onClick = onEditClick),
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
                modifier = Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFEDEDF5)).clickable(onClick = onChatClick),
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.PHONE, tint = Color(0xFF151729), size = 18.dp)
            }
            Box(
                modifier = Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFEEF6D6)).clickable(onClick = onChatClick),
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
            .background(RfThemeState.currentPalette.surface)
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
            .background(RfThemeState.currentPalette.surface)
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
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    AdminPanelIconButton(RfIconKind.CLOSE) { onSelectScreenCode("BACK:s55") }
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
                .clickable { onSelectScreenCode("BACK:s55") },
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
    var textValue by remember { mutableStateOf("") }

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
                .background(RfThemeState.currentPalette.surface)
                .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (icon != null) {
                RfIcon(kind = icon, tint = Color(0xFF5F6377), size = 18.dp)
            }
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (textValue.isEmpty()) {
                    BasicText(
                        placeholder,
                        maxLines = 1,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF5F6377)),
                    )
                }
                BasicTextField(
                    value = textValue,
                    onValueChange = { updatedText -> textValue = updatedText },
                    singleLine = true,
                    textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF151729)),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun AdminNotes56() {
    val typo = LocalRfTypography.current
    var notesValue by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        BasicText("Notas", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF151729)))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .shadow(8.dp, RoundedCornerShape(14.dp), spotColor = Color(0x224650B4))
                .clip(RoundedCornerShape(14.dp))
                .background(RfThemeState.currentPalette.surface)
                .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(14.dp))
                .padding(start = 14.dp, end = 14.dp, top = 14.dp),
        ) {
            if (notesValue.isEmpty()) {
                BasicText(
                    "Condiciones, horarios, lo que ofrece…",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF5F6377)),
                )
            }
            BasicTextField(
                value = notesValue,
                onValueChange = { updatedText -> notesValue = updatedText },
                textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF151729)),
                modifier = Modifier.fillMaxSize(),
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
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AdminPanelIconButton(RfIconKind.BACK) { onSelectScreenCode("BACK:A12") }
                BasicText(
                    "Análisis",
                    modifier = Modifier.weight(1f),
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF151729)),
                )
                Row(
                    modifier = Modifier
                        .height(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(RfThemeState.currentPalette.surface)
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
                            .background(RfThemeState.currentPalette.surface)
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
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AdminPanelIconButton(RfIconKind.BACK) { onSelectScreenCode("BACK:A12") }
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
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(22.dp)),
                ) {
                    PanelSummaryRow58(
                        icon = RfIconKind.CALENDAR,
                        iconBackground = Color(0xFFECEBFF),
                        iconTint = Color(0xFF4B47D6),
                        title = "Eventos activos",
                        value = "4",
                        divider = false,
                        onLinkClick = { onSelectScreenCode("A11") },
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
                        onLinkClick = { onSelectScreenCode("s62") },
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
            .clickable(onClick = onLinkClick)
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
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val profileName = viewModel?.profileName?.collectAsState()?.value ?: "Cliente Rosa Fiesta"
    val eventsState = viewModel?.events?.collectAsState()?.value ?: CanonicalSampleData.events
    val paymentsState = viewModel?.payments?.collectAsState()?.value ?: CanonicalSampleData.payments
    val event = viewModel?.activeClientEvent() ?: eventsState.first()
    val userPayments = viewModel?.clientPaymentsForCurrentUser() ?: paymentsState
    val latestPayment = userPayments.firstOrNull { paymentItem -> paymentItem.eventTitle == event.title } ?: userPayments.firstOrNull()
    val savedMethod = viewModel?.savedPaymentMethod?.collectAsState()?.value ?: "Transferencia · Banco Popular"
    val totalEventDop = event.totalDop.coerceAtLeast(1850)
    val paidDop = latestPayment?.amountDop ?: if (event.paidDepositDop > 0) event.paidDepositDop else (totalEventDop * 30 / 100)
    val remainingDop = (totalEventDop - paidDop).coerceAtLeast(0)
    val receiptCode = latestPayment?.id?.uppercase() ?: "RF-2026-0412"
    val receiptMethod = latestPayment?.methodLabel ?: savedMethod
    val receiptDate = latestPayment?.dateLabel ?: "${event.dateLabel} · 10:24 a. m."
    var receiptShared by remember { mutableStateOf(false) }
    var pdfDownloaded by remember { mutableStateOf(false) }

    // Background: radial pink top-left + lavender top-right + #FAFAFC
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RfThemeState.currentPalette.bg),
    ) {
        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Spacer(modifier = Modifier.height(16.dp))

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
                        .background(RfThemeState.currentPalette.surface)
                        .clickable { onSelectScreenCode("BACK:s51") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = RfThemeState.currentPalette.ink, size = 22.dp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "Recibo",
                        style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, lineHeight = (20 * 1.1f).sp, color = RfThemeState.currentPalette.ink),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                    )
                    BasicText(
                        text = "No. $receiptCode",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = RfThemeState.currentPalette.muted),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                    )
                }
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = false)
                        .clip(CircleShape)
                        .background(RfThemeState.currentPalette.surface)
                        .clickable { receiptShared = !receiptShared },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = if (receiptShared) RfIconKind.CHECK else RfIconKind.SEND, tint = if (receiptShared) Color(0xFF4E6A10) else Color(0xFF1A1A2E), size = 19.dp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Receipt card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = false)
                    .clip(RoundedCornerShape(20.dp))
                    .background(RfThemeState.currentPalette.surface)
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
                            .background(RfThemeState.currentPalette.surface)
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
                        text = "RD$ $paidDop",
                        style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 34.sp, lineHeight = 34.sp, color = RfThemeState.currentPalette.ink),
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    BasicText(
                        text = "Anticipo · ${event.title}",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = RfThemeState.currentPalette.muted, textAlign = TextAlign.Center),
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
                        "Fecha" to receiptDate,
                        "Método" to receiptMethod,
                        "A nombre de" to profileName,
                        "Evento" to "${event.dateLabel} · ${event.venue}",
                        "Total del evento" to "RD$ $totalEventDop",
                        "Pagado hasta hoy" to "RD$ $paidDop",
                    )

                    rows.forEachIndexed { idx, (label, value) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp)
                                .then(if (idx > 0) Modifier.border(width = 1.dp, color = RfThemeState.currentPalette.lineSoft, shape = RoundedCornerShape(0.dp)).padding(top = 1.dp) else Modifier),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            BasicText(label, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = RfThemeState.currentPalette.muted))
                            BasicText(value, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = RfThemeState.currentPalette.ink), modifier = Modifier.padding(start = 12.dp))
                        }
                    }

                    // Saldo pendiente (highlighted amber)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        BasicText("Saldo pendiente", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = RfThemeState.currentPalette.muted))
                        BasicText(
                            text = "RD$ $remainingDop",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color(0xFF7A5700)),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer legal text
            BasicText(
                text = "Rosa Fiesta · RNC 1-31-00000-0 · Santo Domingo\nEste recibo no sustituye una factura con NCF.",
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.5f.sp, color = RfThemeState.currentPalette.muted, textAlign = TextAlign.Center, lineHeight = (12.5f * 1.45f).sp),
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
                    .background(RfThemeState.currentPalette.surface)
                    .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(16.dp))
                    .clickable {
                        receiptShared = true
                        viewModel?.exportReceiptDocument(event, latestPayment)
                    },
                contentAlignment = Alignment.Center,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    RfIcon(kind = if (receiptShared) RfIconKind.CHECK else RfIconKind.SEND, tint = if (receiptShared) Color(0xFF4E6A10) else RfThemeState.currentPalette.ink, size = 18.dp)
                    BasicText(if (receiptShared) "Compartido" else "Compartir", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if (receiptShared) Color(0xFF4E6A10) else RfThemeState.currentPalette.ink))
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
                    .clickable {
                        pdfDownloaded = true
                        viewModel?.exportReceiptDocument(event, latestPayment)
                    },
                contentAlignment = Alignment.Center,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    RfIcon(kind = if (pdfDownloaded) RfIconKind.CHECK else RfIconKind.DOWNLOAD, tint = Color.White, size = 18.dp)
                    BasicText(if (pdfDownloaded) "PDF descargado" else "Descargar PDF", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White))
                }
            }
        }
    }
}
