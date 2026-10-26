package com.jeudry.rosafiesta.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
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
import com.jeudry.rosafiesta.domain.CanonicalSampleData
import com.jeudry.rosafiesta.domain.EventStatus
import com.jeudry.rosafiesta.domain.PhotoAsset
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val EventBackgroundGradient = Brush.radialGradient(
    0.0f to Color(0xFFF9E3F1),
    0.6f to Color(0x00F9E3F1),
    center = Offset(0f, 0f),
    radius = 450f,
)

private val RosaButtonGradient = Brush.horizontalGradient(
    listOf(Color(0xFFEA4EA9), Color(0xFFB754CB), Color(0xFF865DEC)),
)

// ==============================================================================
// s37 · MIS EVENTOS (TAB EVENTOS EN PERSONAL)
// ==============================================================================
// ==============================================================================
// s37 · MIS EVENTOS
// ==============================================================================
@Composable
internal fun RenderMyEventsScreenS37(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    var selectedTab by remember { mutableStateOf(0) } // 0: Próximos, 1: Pasados, 2: Todos
    val allEvents = viewModel?.events?.collectAsState()?.value ?: CanonicalSampleData.events
    val proximos = remember(allEvents) { allEvents.filter { it.status != EventStatus.COMPLETED && it.status != EventStatus.CANCELLED } }
    val pasados = remember(allEvents) { allEvents.filter { it.status == EventStatus.COMPLETED || it.status == EventStatus.CANCELLED } }

    val displayedEvents = when (selectedTab) {
        0 -> proximos
        1 -> pasados
        else -> allEvents
    }

    RfRadialBackgroundBox(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            // Header: Título + Calendario + Botón Nuevo (+)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicText(
                    text = "Mis eventos",
                    modifier = Modifier.weight(1f),
                    style = TextStyle(
                        fontFamily = typo.displayFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 26.sp,
                        color = Color(0xFF1A1A2E),
                    ),
                )

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = false)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("s39") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.CALENDAR, tint = Color(0xFF1A1A2E), size = 20.dp)
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfPrimaryButtonShadow(CircleShape, isAdmin = false)
                        .clip(CircleShape)
                        .background(RosaButtonGradient)
                        .clickable { onSelectScreenCode("s38") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.PLUS, tint = Color.White, size = 22.dp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Segmented control: Próximos · N / Pasados · N / Todos
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF1EFF5))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                listOf("Próximos · ${proximos.size}", "Pasados · ${pasados.size}", "Todos").forEachIndexed { index, label ->
                    val isSelected = selectedTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .then(
                                if (isSelected) {
                                    Modifier.shadow(
                                        6.dp,
                                        RoundedCornerShape(12.dp),
                                        spotColor = Color(0x33AA46A0),
                                        ambientColor = Color.Transparent,
                                    )
                                } else {
                                    Modifier
                                },
                            )
                            .background(if (isSelected) Color.White else Color.Transparent)
                            .clickable { selectedTab = index },
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = label,
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = if (isSelected) Color(0xFF1A1A2E) else Color(0xFF62627A),
                            ),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de eventos mostrados
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                displayedEvents.forEach { ev ->
                    val photoKey = when (ev.photo) {
                        PhotoAsset.BODA -> RfPhotoKey.BODA
                        PhotoAsset.QUINCEANERA -> RfPhotoKey.QUINCEANERA
                        PhotoAsset.CUMPLE_INFANTIL -> RfPhotoKey.CUMPLE_INFANTIL
                        PhotoAsset.BABY_SHOWER -> RfPhotoKey.BABY_SHOWER
                        PhotoAsset.GRADUACION -> RfPhotoKey.GRADUACION
                        PhotoAsset.CORPORATIVO -> RfPhotoKey.CORPORATIVO
                        else -> RfPhotoKey.BODA
                    }
                    val (statusBg, statusColor) = when (ev.status) {
                        EventStatus.CONFIRMED -> Color(0xFFEEF6D6) to Color(0xFF4E6A10)
                        EventStatus.IN_SETUP -> Color(0xFFDDF4F3) to Color(0xFF1F8A84)
                        EventStatus.PENDING_QUOTE -> Color(0xFFF1EFF5) to Color(0xFF62627A)
                        EventStatus.COMPLETED -> Color(0xFFEFE8FD) to Color(0xFF7A4FD8)
                        EventStatus.CANCELLED -> Color(0xFFFCE8E8) to Color(0xFFB42345)
                    }

                    MyEventRow37(
                        title = ev.title,
                        date = ev.dateLabel,
                        status = ev.statusLabel,
                        statusBg = statusBg,
                        statusColor = statusColor,
                        photoKey = photoKey,
                        showReview = ev.status == EventStatus.COMPLETED,
                        onReviewClick = { onSelectScreenCode("s52") },
                        onClick = {
                            viewModel?.selectEvent(ev.id)
                            onSelectScreenCode("s40")
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.height(110.dp))
        }
    }
}

@Composable
private fun MyEventRow37(
    title: String,
    date: String,
    status: String,
    statusBg: Color,
    statusColor: Color,
    photoKey: RfPhotoKey,
    showReview: Boolean = false,
    onReviewClick: () -> Unit = {},
    onClick: () -> Unit,
) {
    val typo = LocalRfTypography.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFF2EDF5), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF3EEF6)),
        ) {
            RfPhoto(photoKey = photoKey, contentDescription = title)
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            BasicText(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    fontFamily = typo.bodyFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1A1A2E),
                ),
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                BasicText(
                    text = date,
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF62627A)),
                )
                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(statusBg)
                        .padding(horizontal = 9.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = status,
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = statusColor,
                        ),
                    )
                }
            }
        }

        if (showReview) {
            Row(
                modifier = Modifier
                    .height(36.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE8E1EE), RoundedCornerShape(999.dp))
                    .clickable(onClick = onReviewClick)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                RfIcon(kind = RfIconKind.STAR, tint = Color(0xFF1A1A2E), size = 14.dp)
                BasicText(
                    text = "Reseñar",
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF1A1A2E),
                    ),
                )
            }
        } else {
            RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = Color(0xFF62627A), size = 20.dp)
        }
    }
}

// ==============================================================================
// s38 · NUEVO EVENTO
// ==============================================================================
private data class EventTypeCard(val name: String, val bg: Color, val illus: EventTypeIllus)
private enum class EventTypeIllus { CUMPLE, BODA, QUINCEANERA, BABY_SHOWER, GRADUACION, CORPORATIVO }

@Composable
internal fun RenderNewEventScreenS38(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    var selectedType by remember { mutableStateOf("Boda") }

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
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // Header: back + title + step
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
                        .clickable { onSelectScreenCode("BACK:s37") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1A1A2E), size = 22.dp)
                }
                BasicText(
                    text = "Nuevo evento",
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, letterSpacing = (-0.40).sp, color = Color(0xFF1A1A2E)),
                    modifier = Modifier.weight(1f),
                )
                BasicText("1 de 5", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF62627A)))
            }

            // Progress bar 5 steps
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                    listOf(true, false, false, false, false).forEach { active ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (active) Color(0xFFEA4EA9) else Color(0xFFE6DEEA)),
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                    listOf("Tipo" to true, "Fecha" to false, "Lugar" to false, "Invitados" to false, "Listo" to false).forEach { (label, active) ->
                        BasicText(
                            label,
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontWeight = if (active) FontWeight.Bold else FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = if (active) Color(0xFFC2338F) else Color(0xFF62627A),
                            ),
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            // Title + subtitle
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BasicText(
                    "¿Qué tipo de evento planeas?",
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, letterSpacing = (-0.48).sp, lineHeight = 28.sp, color = Color(0xFF1A1A2E)),
                )
                BasicText("Elige la categoría que mejor lo describa.", style = typo.body.copy(fontSize = 15.sp, color = Color(0xFF62627A)))
            }

            // Event type grid 2x3
            // Event type cards — order: Cumpleaños/Boda | Quinceañera/Baby shower | Graduación/Corporativo
            val eventTypes = listOf(
                EventTypeCard("Cumpleaños", Color(0xFFFFF2C9), EventTypeIllus.CUMPLE),
                EventTypeCard("Boda", Color(0xFFFDE6F2), EventTypeIllus.BODA),
                EventTypeCard("Quinceañera", Color(0xFFEFE8FD), EventTypeIllus.QUINCEANERA),
                EventTypeCard("Baby shower", Color(0xFFDDF4F3), EventTypeIllus.BABY_SHOWER),
                EventTypeCard("Graduación", Color(0xFFF1EFF5), EventTypeIllus.GRADUACION),
                EventTypeCard("Corporativo", Color(0xFFEEF6D6), EventTypeIllus.CORPORATIVO),
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                eventTypes.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        row.forEach { card ->
                            val isSelected = card.name == selectedType
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(112.dp)
                                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = false)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(card.bg)
                                    .then(if (isSelected) Modifier.border(2.5.dp, Color(0xFFB754CB), RoundedCornerShape(20.dp)) else Modifier)
                                    .clickable { selectedType = card.name },
                            ) {
                                // Selected checkmark badge
                                if (isSelected) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(26.dp)
                                            .align(Alignment.TopStart)
                                            .offset(x = 12.dp, y = 12.dp)
                                            .shadow(8.dp, CircleShape, spotColor = Color(0xB36664E9))
                                            .clip(CircleShape)
                                            .background(Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9)))),
                                    ) {
                                        RfIcon(kind = RfIconKind.CHECK, tint = Color.White, size = 16.dp)
                                    }
                                }
                                // Illustration canvas — top-end, 84dp
                                Canvas(
                                    modifier = Modifier
                                        .size(84.dp)
                                        .align(Alignment.TopEnd)
                                        .offset(x = 4.dp, y = 2.dp),
                                ) {
                                    val w = size.width
                                    val h = size.height
                                    when (card.illus) {
                                        EventTypeIllus.CUMPLE -> {
                                            // Birthday cake
                                            drawRoundRect(color = Color(0xFFFFF6EE), topLeft = androidx.compose.ui.geometry.Offset(w * 0.25f, h * 0.48f), size = androidx.compose.ui.geometry.Size(w * 0.50f, h * 0.33f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f * density))
                                            drawRect(color = Color(0xFFF9B6D3), topLeft = androidx.compose.ui.geometry.Offset(w * 0.25f, h * 0.68f), size = androidx.compose.ui.geometry.Size(w * 0.50f, h * 0.05f))
                                            // Candles
                                            drawRoundRect(color = Color(0xFF7650E8), topLeft = androidx.compose.ui.geometry.Offset(w * 0.35f, h * 0.34f), size = androidx.compose.ui.geometry.Size(w * 0.04f, h * 0.14f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f * density))
                                            drawRoundRect(color = Color(0xFF4AA8E0), topLeft = androidx.compose.ui.geometry.Offset(w * 0.48f, h * 0.32f), size = androidx.compose.ui.geometry.Size(w * 0.04f, h * 0.17f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f * density))
                                            drawRoundRect(color = Color(0xFF2FB58F), topLeft = androidx.compose.ui.geometry.Offset(w * 0.61f, h * 0.34f), size = androidx.compose.ui.geometry.Size(w * 0.04f, h * 0.14f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f * density))
                                            // Flames
                                            drawCircle(color = Color(0xFFF5B53B), radius = 4f * density, center = androidx.compose.ui.geometry.Offset(w * 0.37f, h * 0.31f))
                                            drawCircle(color = Color(0xFFF5B53B), radius = 4f * density, center = androidx.compose.ui.geometry.Offset(w * 0.50f, h * 0.29f))
                                            drawCircle(color = Color(0xFFF5B53B), radius = 4f * density, center = androidx.compose.ui.geometry.Offset(w * 0.63f, h * 0.31f))
                                        }

                                        EventTypeIllus.BODA -> {
                                            // Two interlocked rings
                                            val strokeW = 6.5f * density
                                            drawCircle(color = Color(0xFFD9A441), radius = w * 0.18f, center = androidx.compose.ui.geometry.Offset(w * 0.40f, h * 0.58f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeW))
                                            drawCircle(color = Color(0xFFF0CC7A), radius = w * 0.18f, center = androidx.compose.ui.geometry.Offset(w * 0.60f, h * 0.60f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeW))
                                            // Diamond
                                            val path = androidx.compose.ui.graphics.Path()
                                            path.moveTo(w * 0.35f, h * 0.36f)
                                            path.lineTo(w * 0.47f, h * 0.30f)
                                            path.lineTo(w * 0.47f, h * 0.37f)
                                            path.lineTo(w * 0.41f, h * 0.44f)
                                            path.close()
                                            drawPath(path, color = Color(0xFFA8D8F5))
                                        }

                                        EventTypeIllus.QUINCEANERA -> {
                                            // Crown with sparkles
                                            val crownColor = Color(0xFFD9A441)
                                            val crownPath = androidx.compose.ui.graphics.Path()
                                            crownPath.moveTo(w * 0.20f, h * 0.75f)
                                            crownPath.lineTo(w * 0.20f, h * 0.50f)
                                            crownPath.lineTo(w * 0.35f, h * 0.62f)
                                            crownPath.lineTo(w * 0.50f, h * 0.40f)
                                            crownPath.lineTo(w * 0.65f, h * 0.62f)
                                            crownPath.lineTo(w * 0.80f, h * 0.50f)
                                            crownPath.lineTo(w * 0.80f, h * 0.75f)
                                            crownPath.close()
                                            drawPath(crownPath, color = crownColor)
                                            drawCircle(color = Color(0xFFEC4F97), radius = 5f * density, center = androidx.compose.ui.geometry.Offset(w * 0.50f, h * 0.38f))
                                            drawCircle(color = Color(0xFF7650E8), radius = 3.5f * density, center = androidx.compose.ui.geometry.Offset(w * 0.30f, h * 0.58f))
                                            drawCircle(color = Color(0xFF2FB58F), radius = 3.5f * density, center = androidx.compose.ui.geometry.Offset(w * 0.70f, h * 0.56f))
                                        }

                                        EventTypeIllus.BABY_SHOWER -> {
                                            // Clothesline with baby onesie
                                            drawPath(
                                                path = androidx.compose.ui.graphics.Path().apply {
                                                    moveTo(w * 0.05f, h * 0.25f)
                                                    cubicTo(w * 0.50f, h * 0.38f, w * 0.95f, h * 0.25f, w * 0.95f, h * 0.25f)
                                                },
                                                color = Color(0xFF6E5A72).copy(alpha = 0.45f),
                                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f * density),
                                            )
                                            // Onesie body
                                            drawRoundRect(color = Color(0xFF9BE3C9), topLeft = androidx.compose.ui.geometry.Offset(w * 0.33f, h * 0.35f), size = androidx.compose.ui.geometry.Size(w * 0.34f, h * 0.38f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f * density))
                                            // Sleeves
                                            drawRoundRect(color = Color(0xFF9BE3C9), topLeft = androidx.compose.ui.geometry.Offset(w * 0.18f, h * 0.35f), size = androidx.compose.ui.geometry.Size(w * 0.17f, h * 0.16f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f * density))
                                            drawRoundRect(color = Color(0xFF9BE3C9), topLeft = androidx.compose.ui.geometry.Offset(w * 0.65f, h * 0.35f), size = androidx.compose.ui.geometry.Size(w * 0.17f, h * 0.16f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f * density))
                                            // Star detail
                                            drawCircle(color = Color(0xFFF5B53B), radius = 4f * density, center = androidx.compose.ui.geometry.Offset(w * 0.50f, h * 0.54f))
                                        }

                                        EventTypeIllus.GRADUACION -> {
                                            // Graduation cap
                                            val capColor = Color(0xFF1A1A2E)
                                            drawRoundRect(color = capColor, topLeft = androidx.compose.ui.geometry.Offset(w * 0.20f, h * 0.45f), size = androidx.compose.ui.geometry.Size(w * 0.60f, h * 0.14f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f * density))
                                            val hatPath = androidx.compose.ui.graphics.Path()
                                            hatPath.moveTo(w * 0.50f, h * 0.28f)
                                            hatPath.lineTo(w * 0.80f, h * 0.45f)
                                            hatPath.lineTo(w * 0.50f, h * 0.60f)
                                            hatPath.lineTo(w * 0.20f, h * 0.45f)
                                            hatPath.close()
                                            drawPath(hatPath, color = capColor)
                                            // Tassel
                                            drawLine(color = Color(0xFFF5B53B), start = androidx.compose.ui.geometry.Offset(w * 0.76f, h * 0.45f), end = androidx.compose.ui.geometry.Offset(w * 0.76f, h * 0.72f), strokeWidth = 2.5f * density)
                                            drawCircle(color = Color(0xFFF5B53B), radius = 5f * density, center = androidx.compose.ui.geometry.Offset(w * 0.76f, h * 0.75f))
                                        }

                                        EventTypeIllus.CORPORATIVO -> {
                                            // Champagne glasses clinking
                                            val glassColor = Color(0xFFD9A441)
                                            // Left glass
                                            val lp = androidx.compose.ui.graphics.Path()
                                            lp.moveTo(w * 0.28f, h * 0.25f)
                                            lp.lineTo(w * 0.38f, h * 0.25f)
                                            lp.lineTo(w * 0.34f, h * 0.50f)
                                            lp.lineTo(w * 0.32f, h * 0.50f)
                                            lp.close()
                                            drawPath(lp, color = glassColor)
                                            drawLine(color = glassColor, start = androidx.compose.ui.geometry.Offset(w * 0.33f, h * 0.50f), end = androidx.compose.ui.geometry.Offset(w * 0.33f, h * 0.72f), strokeWidth = 2.5f * density)
                                            // Right glass
                                            val rp = androidx.compose.ui.graphics.Path()
                                            rp.moveTo(w * 0.62f, h * 0.25f)
                                            rp.lineTo(w * 0.72f, h * 0.25f)
                                            rp.lineTo(w * 0.68f, h * 0.50f)
                                            rp.lineTo(w * 0.66f, h * 0.50f)
                                            rp.close()
                                            drawPath(rp, color = glassColor)
                                            drawLine(color = glassColor, start = androidx.compose.ui.geometry.Offset(w * 0.67f, h * 0.50f), end = androidx.compose.ui.geometry.Offset(w * 0.67f, h * 0.72f), strokeWidth = 2.5f * density)
                                            // Bubbles
                                            drawCircle(color = Color(0xFFF5B53B).copy(alpha = 0.6f), radius = 3f * density, center = androidx.compose.ui.geometry.Offset(w * 0.32f, h * 0.35f))
                                            drawCircle(color = Color(0xFFF5B53B).copy(alpha = 0.6f), radius = 2f * density, center = androidx.compose.ui.geometry.Offset(w * 0.36f, h * 0.28f))
                                            drawCircle(color = Color(0xFFF5B53B).copy(alpha = 0.6f), radius = 3f * density, center = androidx.compose.ui.geometry.Offset(w * 0.68f, h * 0.33f))
                                        }
                                    }
                                }
                                BasicText(
                                    card.name,
                                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, letterSpacing = (-0.34).sp, color = Color(0xFF1A1A2E)),
                                    modifier = Modifier.align(Alignment.BottomStart).padding(start = 14.dp, bottom = 12.dp),
                                )
                            }
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 20.dp, end = 20.dp, bottom = 40.dp)
                .fillMaxWidth()
                .height(56.dp)
                .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = false)
                .clip(RoundedCornerShape(16.dp))
                .background(RosaButtonGradient)
                .clickable { onSelectScreenCode("s26") },
            contentAlignment = Alignment.Center,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                BasicText(
                    text = "Siguiente: fecha",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White),
                )
                RfIcon(kind = RfIconKind.ARROW_RIGHT, tint = Color.White, size = 20.dp)
            }
        }
    }
}

// ==============================================================================
// s39 · CALENDARIO
// ==============================================================================
@Composable
internal fun RenderCalendarScreenS39(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFC)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = false)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("BACK:s37") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1A1A2E), size = 22.dp)
                }

                BasicText(
                    text = "Calendario",
                    style = TextStyle(
                        fontFamily = typo.displayFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = Color(0xFF1A1A2E),
                    ),
                )

                Spacer(modifier = Modifier.size(44.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Tarjeta Calendario Octubre 2026
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(22.dp), isAdmin = false)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White)
                    .padding(16.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        BasicText("Octubre 2026", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color(0xFF1A1A2E)))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            BasicText("Mes", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF865DEC), fontSize = 13.sp))
                        }
                    }

                    // Días de semana
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                    ) {
                        listOf("L", "M", "X", "J", "V", "S", "D").forEach { d ->
                            BasicText(d, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF62627A), fontSize = 12.sp))
                        }
                    }

                    // Cuadrícula simple de días
                    for (row in 0..4) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround,
                        ) {
                            for (col in 1..7) {
                                val dayNum = row * 7 + col - 3
                                if (dayNum in 1..31) {
                                    val isEventDay = dayNum in setOf(23, 28)
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(if (isEventDay) Color(0xFFEA4EA9) else Color.Transparent),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        BasicText(
                                            text = "$dayNum",
                                            style = TextStyle(
                                                fontFamily = typo.bodyFamily,
                                                fontWeight = if (isEventDay) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isEventDay) Color.White else Color(0xFF1A1A2E),
                                                fontSize = 13.sp,
                                            ),
                                        )
                                    }
                                } else {
                                    Spacer(modifier = Modifier.size(32.dp))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            BasicText("Eventos de este mes:", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E), fontSize = 15.sp))

            Spacer(modifier = Modifier.height(10.dp))

            // Evento seleccionado
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFF2EDF5), RoundedCornerShape(16.dp))
                    .clickable { onSelectScreenCode("s40") }
                    .padding(14.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEA4EA9)),
                    )
                    Column {
                        BasicText("Viernes 23 Octubre", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E), fontSize = 14.sp))
                        BasicText("Boda Real Rosa & Dorado · Jardín Botánico", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF62627A), fontSize = 13.sp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(110.dp))
        }
    }
}

// ==============================================================================
// s40 · EVENTO · DETALLES
// ==============================================================================
@Composable
internal fun RenderEventDetailScreenS40(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    RfRadialBackgroundBox(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 54.dp, start = 20.dp, end = 20.dp, bottom = 30.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = false)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFEEEAF2), CircleShape)
                        .clickable { onSelectScreenCode("BACK:s37") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1A1A2E), size = 22.dp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "Boda Real Rosa & Dorado",
                        style = TextStyle(
                            fontFamily = typo.displayFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 19.sp,
                            color = Color(0xFF1A1A2E),
                        ),
                        maxLines = 1,
                    )
                    BasicText(
                        text = "Vie 23 oct · Jardín Botánico",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 13.sp,
                            color = Color(0xFF62627A),
                        ),
                        maxLines = 1,
                    )
                }

                // Download PDF
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = false)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFEEEAF2), CircleShape)
                        .clickable { onSelectScreenCode("s83") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.DOWNLOAD, tint = Color(0xFF1A1A2E), size = 20.dp)
                }

                // More Options (3 dots)
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = false)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFEEEAF2), CircleShape)
                        .clickable { onSelectScreenCode("s84") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.MORE_VERTICAL, tint = Color(0xFF1A1A2E), size = 20.dp)
                }
            }

            // Tabs: Detalles / Chat
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .height(44.dp)
                            .width(IntrinsicSize.Max),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(
                                text = "Detalles",
                                style = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD63F98),
                                ),
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.5.dp)
                                .background(Color(0xFFEA4EA9)),
                        )
                    }

                    Row(
                        modifier = Modifier
                            .height(44.dp)
                            .clickable { onSelectScreenCode("s41") },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        BasicText(
                            text = "Chat",
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF62627A),
                            ),
                        )
                        Box(
                            modifier = Modifier
                                .height(20.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFD45C))
                                .padding(horizontal = 6.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(
                                text = "2",
                                style = TextStyle(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF2E1A47),
                                ),
                            )
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFFE9E6EE)),
                )
            }

            // Countdown Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .rfCardShadow(RoundedCornerShape(22.dp), isAdmin = false)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFF0EDF3), RoundedCornerShape(22.dp)),
            ) {
                // Confetti + rings illustration
                Canvas(modifier = Modifier.matchParentSize()) {
                    val s = size.width / 350f
                    fun at(x: Float, y: Float) = Offset(x * s, y * s)

                    // Rings
                    drawCircle(
                        color = Color(0xFFD9A441),
                        radius = 21f * s,
                        center = at(295f, 57f),
                        style = Stroke(width = 5f * s),
                    )
                    drawCircle(
                        color = Color(0xFFF0CC7A),
                        radius = 21f * s,
                        center = at(312f, 59f),
                        style = Stroke(width = 5f * s),
                    )

                    // Gold star
                    drawPath(
                        path = fivePointStar(at(222f, 25f), 11f * s, 4.4f * s),
                        color = Color(0xFFFFCE47),
                    )
                    // Pink sparkle
                    drawPath(
                        path = fourPointSparkle(at(274f, 28f), 7f * s),
                        color = Color(0xFFE86FC0),
                    )
                    // Blue diamond
                    drawPath(
                        path = fourPointSparkle(at(294f, 27f), 5f * s),
                        color = Color(0xFF8FD3F0),
                    )
                    // Pink heart
                    drawPath(
                        path = heartPath(at(335f, 81f), 6f * s),
                        color = Color(0xFFF6C3DE),
                    )
                    // Green dot
                    drawCircle(Color(0xFFB9D84A), radius = 3.5f * s, center = at(238f, 87f))
                }

                Column(modifier = Modifier.padding(start = 18.dp, top = 14.dp, end = 18.dp)) {
                    BasicText(
                        text = "FALTAN PARA TU EVENTO",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = Color(0xFF62627A),
                        ),
                    )
                    Spacer(Modifier.height(2.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                        CountdownUnit(value = "03", label = "días", color = Color(0xFFEB4F9E))
                        CountdownUnit(value = "07", label = "horas", color = Color(0xFFF2894A))
                        CountdownUnit(value = "53", label = "min", color = Color(0xFFE8B53A))
                    }
                }
            }

            // Modo Ejecución banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(22.dp),
                        spotColor = Color(0x60463C8C),
                        ambientColor = Color(0x20463C8C),
                    )
                    .clip(RoundedCornerShape(22.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9))))
                    .clickable { onSelectScreenCode("s43") }
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "Modo ejecución",
                        style = TextStyle(
                            fontFamily = typo.displayFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = Color.White,
                        ),
                    )
                    BasicText(
                        text = "Checklist para el día del evento",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 13.sp,
                            color = Color(0xFFE4E2F2),
                        ),
                    )
                }
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = false)
                        .clip(CircleShape)
                        .background(Color(0xFFFFD45C)),
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.PLAY, tint = Color.Black, size = 20.dp, filled = true)
                }
            }

            // Action Items Card (Tareas, Cronograma, Sincronizar)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(22.dp), isAdmin = false)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFF0EDF3), RoundedCornerShape(22.dp))
                    .padding(vertical = 4.dp),
            ) {
                // Tareas
                ActionMenuRow(
                    title = "Tareas",
                    subtitle = "2 de 4 completadas",
                    icon = RfIconKind.CHECKLIST,
                    iconBg = Color(0xFFDDF4F3),
                    iconTint = Color(0xFF2E9C96),
                    onClick = { onSelectScreenCode("s45") },
                )
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF2F0F5)))

                // Cronograma
                ActionMenuRow(
                    title = "Cronograma",
                    subtitle = "4 actividades por hora",
                    icon = RfIconKind.CLOCK,
                    iconBg = Color(0xFFFFF2C9),
                    iconTint = Color(0xFFB07D00),
                    onClick = { onSelectScreenCode("s44") },
                )
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF2F0F5)))

                // Sincronizar calendario
                ActionMenuRow(
                    title = "Sincronizar calendario",
                    subtitle = "Google o Apple Calendar",
                    icon = RfIconKind.CALENDAR,
                    iconBg = Color(0xFFEEF6D6),
                    iconTint = Color(0xFF5E7A12),
                    onClick = { onSelectScreenCode("s39") },
                )
            }

            // Pagos Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(22.dp), isAdmin = false)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFF0EDF3), RoundedCornerShape(22.dp))
                    .clickable { onSelectScreenCode("s48") }
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BasicText(
                        text = "Pagos",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A2E),
                        ),
                    )
                    Box(
                        modifier = Modifier
                            .height(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEEF6D6))
                            .padding(horizontal = 9.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = "30% pagado",
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4E5A12),
                            ),
                        )
                    }
                }

                // Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFEEEDF2)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.30f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(Brush.horizontalGradient(listOf(Color(0xFFCFE48A), Color(0xFFA9CC3A)))),
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        BasicText("Pagado", style = TextStyle(fontSize = 14.sp, color = Color(0xFF62627A)))
                        BasicText("RD$ 1,350", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5E7A12)))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        BasicText("Total", style = TextStyle(fontSize = 14.sp, color = Color(0xFF62627A)))
                        BasicText("RD$ 4,500", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E)))
                    }
                }
            }
        }
    }
}

private fun fivePointStar(center: Offset, outer: Float, inner: Float): Path {
    val path = Path()
    for (i in 0 until 10) {
        val radius = if (i % 2 == 0) outer else inner
        val angle = (-90.0 + i * 36.0) * PI / 180.0
        val x = center.x + (radius * cos(angle)).toFloat()
        val y = center.y + (radius * sin(angle)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}

private fun fourPointSparkle(center: Offset, radius: Float): Path {
    val path = Path()
    val thin = radius * 0.28f
    path.moveTo(center.x, center.y - radius)
    path.lineTo(center.x + thin, center.y - thin)
    path.lineTo(center.x + radius, center.y)
    path.lineTo(center.x + thin, center.y + thin)
    path.lineTo(center.x, center.y + radius)
    path.lineTo(center.x - thin, center.y + thin)
    path.lineTo(center.x - radius, center.y)
    path.lineTo(center.x - thin, center.y - thin)
    path.close()
    return path
}

private fun heartPath(center: Offset, size: Float): Path {
    val path = Path()
    val s = size * 0.1f
    path.moveTo(center.x, center.y + 9f * s)
    path.cubicTo(
        center.x - 12f * s,
        center.y + 1f * s,
        center.x - 9f * s,
        center.y - 10f * s,
        center.x,
        center.y - 3f * s,
    )
    path.cubicTo(
        center.x + 9f * s,
        center.y - 10f * s,
        center.x + 12f * s,
        center.y + 1f * s,
        center.x,
        center.y + 9f * s,
    )
    path.close()
    return path
}

@Composable
private fun CountdownUnit(
    value: String,
    label: String,
    color: Color,
) {
    val typo = LocalRfTypography.current
    Column(horizontalAlignment = Alignment.Start) {
        BasicText(
            text = value,
            style = TextStyle(
                fontFamily = typo.displayFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 40.sp,
                color = color,
                lineHeight = 40.sp,
            ),
        )
        BasicText(
            text = label,
            style = TextStyle(
                fontFamily = typo.bodyFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF62627A),
            ),
        )
    }
}

@Composable
private fun ActionMenuRow(
    title: String,
    subtitle: String,
    icon: RfIconKind,
    iconBg: Color,
    iconTint: Color,
    onClick: () -> Unit,
) {
    val typo = LocalRfTypography.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center,
        ) {
            RfIcon(kind = icon, tint = iconTint, size = 20.dp)
        }

        Column(modifier = Modifier.weight(1f)) {
            BasicText(
                text = title,
                style = TextStyle(
                    fontFamily = typo.bodyFamily,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A2E),
                ),
            )
            BasicText(
                text = subtitle,
                style = TextStyle(
                    fontFamily = typo.bodyFamily,
                    fontSize = 13.sp,
                    color = Color(0xFF62627A),
                ),
            )
        }

        RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = Color(0xFF62627A), size = 18.dp)
    }
}

// ==============================================================================
// s43 · MODO EJECUCIÓN (DÍA D)
// ==============================================================================
@Composable
internal fun RenderExecutionModeScreenS43(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    var task1Done by remember { mutableStateOf(true) }
    var task2Done by remember { mutableStateOf(true) }
    var task3Done by remember { mutableStateOf(false) }
    var task4Done by remember { mutableStateOf(false) }
    val completedBase = listOf(task1Done, task2Done, task3Done, task4Done).count { isDone -> isDone } + 1
    val progressFraction = (completedBase.toFloat() / 8f).coerceIn(0f, 1f)
    val progressPercent = (progressFraction * 100).toInt()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFC)),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfHeroBannerShadow(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp), isAdmin = false)
                    .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                    .background(RosaButtonGradient)
                    .padding(top = 52.dp, start = 20.dp, end = 20.dp, bottom = 28.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.18f))
                                .clickable { onSelectScreenCode("BACK:s40") },
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.CLOSE, tint = Color.White, size = 22.dp)
                        }

                        Row(
                            modifier = Modifier
                                .height(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFD45C))
                                .padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2E1A47)),
                            )
                            BasicText(
                                text = "En curso",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF2E1A47), fontSize = 12.sp),
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(18.dp),
                    ) {
                        Box(
                            modifier = Modifier.size(84.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val stroke = 8.dp.toPx()
                                val radius = 34.dp.toPx()
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.22f),
                                    radius = radius,
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke),
                                )
                                drawArc(
                                    color = Color(0xFFFFD45C),
                                    startAngle = -90f,
                                    sweepAngle = 360f * progressFraction,
                                    useCenter = false,
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                                        width = stroke,
                                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                                    ),
                                    topLeft = androidx.compose.ui.geometry.Offset(
                                        center.x - radius,
                                        center.y - radius,
                                    ),
                                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                                )
                            }
                            BasicText(
                                text = "$progressPercent%",
                                style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 18.sp),
                            )
                        }

                        Column {
                            BasicText(
                                text = "Modo ejecución",
                                style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 24.sp, lineHeight = 25.sp, letterSpacing = (-0.5).sp),
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            BasicText(
                                text = "Boda Real Rosa & Dorado · $completedBase de 8 tareas",
                                style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFFF6EAFB), fontSize = 14.sp),
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp, top = 18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ExecutionTaskRow43(
                    title = "Confirmar paleta floral blanco y rosa pastel",
                    subtitle = "Revisión final con la florista principal",
                    completed = task1Done,
                    onClick = { task1Done = !task1Done },
                )
                ExecutionTaskRow43(
                    title = "Aprobar plano de mesas imperial y sillas Tiffany",
                    subtitle = "Distribución para 120 invitados en el jardín central",
                    completed = task2Done,
                    onClick = { task2Done = !task2Done },
                )
                ExecutionTaskRow43(
                    title = "Realizar pago inicial de reserva (30%)",
                    subtitle = "Transferencia o tarjeta desde el portal Rosa Fiesta",
                    completed = task3Done,
                    onClick = { task3Done = !task3Done },
                )
                ExecutionTaskRow43(
                    title = "Inspección técnica de iluminación exterior",
                    subtitle = "Coordinar generador y guirnaldas cálidas",
                    completed = task4Done,
                    onClick = { task4Done = !task4Done },
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun ExecutionTaskRow43(
    title: String,
    subtitle: String,
    completed: Boolean,
    onClick: () -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = false)
            .clip(RoundedCornerShape(20.dp))
            .background(if (completed) Color(0xFFF6F4F9) else Color.White)
            .border(1.dp, Color(0xFFF2EDF5), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (completed) Color(0xFF8DB52C) else Color.White)
                .then(
                    if (completed) {
                        Modifier
                    } else {
                        Modifier.border(2.dp, Color(0xFFCDBFCB), RoundedCornerShape(10.dp))
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (completed) {
                RfIcon(kind = RfIconKind.CHECK, tint = Color.White, size = 18.dp)
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            BasicText(
                text = title,
                style = TextStyle(
                    fontFamily = typo.bodyFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    lineHeight = 21.sp,
                    color = if (completed) Color(0xFF62627A) else Color(0xFF1A1A2E),
                    textDecoration = if (completed) TextDecoration.LineThrough else TextDecoration.None,
                ),
            )
            BasicText(
                text = subtitle,
                style = TextStyle(
                    fontFamily = typo.bodyFamily,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = Color(0xFF62627A),
                ),
            )
            Row(
                modifier = Modifier.padding(top = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                RfIcon(kind = RfIconKind.CLOCK, tint = Color(0xFF62627A), size = 14.dp)
                BasicText(
                    text = "9:38 p. m.",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFF62627A)),
                )
            }
        }
    }
}

// ==============================================================================
// s44 · CRONOGRAMA
// ==============================================================================
private data class TimelineItem44(
    val start: String,
    val end: String,
    val dotColor: Color,
    val ringColor: Color,
    val title: String,
    val subtitle: String,
)

@Composable
internal fun RenderTimelineScreenS44(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    var extraTimelineItems by remember { mutableStateOf(emptyList<TimelineItem44>()) }

    RfRadialBackgroundBox(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 56.dp, bottom = 30.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
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
                        text = "Cronograma",
                        style = TextStyle(
                            fontFamily = typo.displayFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            lineHeight = 22.sp,
                            color = Color(0xFF1A1A2E),
                        ),
                    )
                    BasicText(
                        text = "Viernes 23 de octubre",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 13.sp,
                            color = Color(0xFF62627A),
                        ),
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = false)
                        .clip(CircleShape)
                        .background(RosaButtonGradient)
                        .clickable {
                            val nextIndex = extraTimelineItems.size + 1
                            extraTimelineItems = extraTimelineItems + TimelineItem44(
                                start = "22:00",
                                end = "23:30",
                                dotColor = Color(0xFF8DB52C),
                                ringColor = Color(0xFFEEF6D6),
                                title = "Desmontaje y retorno a bodega #$nextIndex",
                                subtitle = "Conteo de 145 piezas con el equipo de Pedro",
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.PLUS, tint = Color.White, size = 22.dp)
                }
            }

            val timelineItems = listOf(
                TimelineItem44(
                    start = "06:38",
                    end = "09:08",
                    dotColor = Color(0xFFF7B500),
                    ringColor = Color(0xFFFFF2C9),
                    title = "Descarga y montaje de mobiliario",
                    subtitle = "Llega el camión con 120 sillas Tiffany y mesas",
                ),
                TimelineItem44(
                    start = "09:38",
                    end = "12:38",
                    dotColor = Color(0xFFEA4EA9),
                    ringColor = Color(0xFFFDE6F2),
                    title = "Arco orgánico y flores",
                    subtitle = "Montaje del backdrop principal y centros de mesa",
                ),
                TimelineItem44(
                    start = "13:08",
                    end = "14:38",
                    dotColor = Color(0xFF1FBDC0),
                    ringColor = Color(0xFFDDF4F3),
                    title = "Pruebas de sonido e iluminación",
                    subtitle = "Microfonía, luces ambientales y playlist lista",
                ),
                TimelineItem44(
                    start = "15:38",
                    end = "16:38",
                    dotColor = Color(0xFF865DEC),
                    ringColor = Color(0xFFEFE8FD),
                    title = "Recepción de invitados",
                    subtitle = "Cóctel de bienvenida en el área de la terraza",
                ),
            ) + extraTimelineItems

            Column(modifier = Modifier.fillMaxWidth()) {
                timelineItems.forEachIndexed { index, item ->
                    TimelineRow44(item = item, isLast = index == timelineItems.lastIndex)
                }
            }
        }
    }
}

@Composable
private fun TimelineRow44(item: TimelineItem44, isLast: Boolean) {
    val typo = LocalRfTypography.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Time column on left (48 dp width)
        Column(
            modifier = Modifier.width(48.dp).padding(top = 14.dp),
            horizontalAlignment = Alignment.End,
        ) {
            BasicText(
                text = item.start,
                style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF1A1A2E)),
            )
            BasicText(
                text = item.end,
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF62627A)),
            )
        }

        // Timeline line & dot in center
        Box(
            modifier = Modifier.width(16.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .padding(top = 28.dp)
                        .width(2.dp)
                        .height(110.dp)
                        .background(Color(0xFFEADFE7)),
                )
            }
            Box(
                modifier = Modifier
                    .padding(top = 18.dp)
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(item.dotColor)
                    .border(4.dp, item.ringColor, CircleShape),
            )
        }

        // Card on right
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 12.dp)
                .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = false)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFF2EDF5), RoundedCornerShape(20.dp))
                .padding(start = 16.dp, top = 14.dp, end = 8.dp, bottom = 14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    BasicText(
                        text = item.title,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 21.sp, color = Color(0xFF1A1A2E)),
                    )
                    BasicText(
                        text = item.subtitle,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, lineHeight = 19.sp, color = Color(0xFF62627A)),
                    )
                }
                Box(
                    modifier = Modifier.size(36.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.MORE_VERTICAL, tint = Color(0xFF62627A), size = 20.dp)
                }
            }
        }
    }
}

// ==============================================================================
// s45 · TAREAS
// ==============================================================================
private data class EventTaskRowState(
    val title: String,
    val badgeText: String,
    val isUrgent: Boolean,
    val completed: Boolean,
)

@Composable
internal fun RenderTasksScreenS45(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    var taskItems by remember {
        mutableStateOf(
            listOf(
                EventTaskRowState("Realizar pago inicial de reserva (30%)", "Vence hoy", true, false),
                EventTaskRowState("Inspección técnica de iluminación exterior", "Mañana", false, false),
                EventTaskRowState("Confirmar paleta floral blanco y rosa pastel", "18 oct", false, true),
                EventTaskRowState("Aprobar plano de mesas imperial y sillas Tiffany", "15 oct", false, true),
            ),
        )
    }
    val completedCount = taskItems.count { taskItem -> taskItem.completed }
    val totalCount = taskItems.size.coerceAtLeast(1)
    val completionFraction = completedCount.toFloat() / totalCount.toFloat()
    val completionPercent = (completionFraction * 100f).toInt()
    val pendingTasks = taskItems.filter { taskItem -> !taskItem.completed }
    val completedTasks = taskItems.filter { taskItem -> taskItem.completed }

    RfRadialBackgroundBox(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 56.dp, bottom = 30.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
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
                        .background(Color.White)
                        .clickable { onSelectScreenCode("BACK:s40") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1A1A2E), size = 22.dp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "Tareas",
                        style = TextStyle(
                            fontFamily = typo.displayFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            lineHeight = 22.sp,
                            color = Color(0xFF1A1A2E),
                        ),
                    )
                    BasicText(
                        text = "Boda Real Rosa & Dorado",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 13.sp,
                            color = Color(0xFF62627A),
                        ),
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = false)
                        .clip(CircleShape)
                        .background(RosaButtonGradient)
                        .clickable {
                            val nextIndex = taskItems.size + 1
                            taskItems = listOf(
                                EventTaskRowState(
                                    title = "Nueva tarea de coordinación #$nextIndex",
                                    badgeText = "Pendiente",
                                    isUrgent = false,
                                    completed = false,
                                ),
                            ) + taskItems
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.PLUS, tint = Color.White, size = 22.dp)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(22.dp), isAdmin = false)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFF2EDF5), RoundedCornerShape(22.dp))
                    .padding(horizontal = 18.dp, vertical = 16.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            BasicText(
                                text = "$completedCount",
                                style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp, color = Color(0xFF1A1A2E)),
                            )
                            BasicText(
                                text = "de ${taskItems.size} hechas",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color(0xFF62627A)),
                            )
                        }
                        BasicText(
                            text = "$completionPercent%",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF4E6A10)),
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFF1EFF5)),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(completionFraction)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF8DB52C)),
                        )
                    }
                }
            }

            BasicText(
                text = "PENDIENTES",
                style = TextStyle(
                    fontFamily = typo.bodyFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Color(0xFF62627A),
                ),
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 0.dp)
                    .rfCardShadow(RoundedCornerShape(22.dp), isAdmin = false)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFF2EDF5), RoundedCornerShape(22.dp)),
            ) {
                Column {
                    pendingTasks.forEachIndexed { index, taskItem ->
                        if (index > 0) {
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF2F0F5)))
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    taskItems = taskItems.map { current ->
                                        if (current.title == taskItem.title) current.copy(completed = true) else current
                                    }
                                }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 1.dp)
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(2.dp, Color(0xFFCDBFCB), CircleShape),
                            )
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                BasicText(
                                    text = taskItem.title,
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 21.sp, color = Color(0xFF1A1A2E)),
                                )
                                Box(
                                    modifier = Modifier
                                        .height(24.dp)
                                        .clip(CircleShape)
                                        .background(if (taskItem.isUrgent) Color(0xFFFFF2C9) else Color(0xFFF1EFF5))
                                        .padding(horizontal = 9.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    BasicText(
                                        taskItem.badgeText,
                                        style = TextStyle(
                                            fontFamily = typo.bodyFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (taskItem.isUrgent) Color(0xFF7A5700) else Color(0xFF62627A),
                                        ),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            BasicText(
                text = "COMPLETADAS",
                style = TextStyle(
                    fontFamily = typo.bodyFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Color(0xFF62627A),
                ),
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(22.dp), isAdmin = false)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFFF7F3F6))
                    .border(1.dp, Color(0xFFEDE4EA), RoundedCornerShape(22.dp)),
            ) {
                Column {
                    completedTasks.forEachIndexed { index, taskItem ->
                        if (index > 0) {
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFEDE4EA)))
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    taskItems = taskItems.map { current ->
                                        if (current.title == taskItem.title) current.copy(completed = false) else current
                                    }
                                }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 1.dp)
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF8DB52C)),
                                contentAlignment = Alignment.Center,
                            ) {
                                RfIcon(kind = RfIconKind.CHECK, tint = Color.White, size = 16.dp)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                BasicText(
                                    text = taskItem.title,
                                    style = TextStyle(
                                        fontFamily = typo.bodyFamily,
                                        fontSize = 16.sp,
                                        lineHeight = 21.sp,
                                        color = Color(0xFF62627A),
                                        textDecoration = TextDecoration.LineThrough,
                                    ),
                                )
                                Spacer(Modifier.height(4.dp))
                                BasicText(
                                    text = taskItem.badgeText,
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF62627A)),
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

// ==============================================================================
// s46 · CHECKLIST
// ==============================================================================
@Composable
internal fun RenderChecklistScreenS46(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val checklist = viewModel?.checklist?.collectAsState()?.value ?: CanonicalSampleData.bodaChecklist
    val totalSteps = checklist.size.coerceAtLeast(1)
    val completedCount = checklist.count { it.completed }

    RfRadialBackgroundBox(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 56.dp),
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
                        .clickable { onSelectScreenCode("BACK:s40") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1A1A2E), size = 22.dp)
                }

                BasicText(
                    text = "Antes del gran día",
                    modifier = Modifier.weight(1f),
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF1A1A2E)),
                )
            }

            // Event summary card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(22.dp), isAdmin = false)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFFFDE6F2))
                    .clickable { onSelectScreenCode("s40") }
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF3EEF6)),
                ) {
                    RfPhoto(photoKey = RfPhotoKey.BODA, contentDescription = "Boda Real Rosa & Dorado")
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "Boda Real Rosa & Dorado",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1A1A2E)),
                    )
                    BasicText(
                        text = "Vie 23 oct · en 3 días",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFFC2338F)),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xCCFFFFFF)),
                        ) {
                            if (completedCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(completedCount.toFloat() / totalSteps)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(RosaButtonGradient),
                                )
                            }
                        }
                        BasicText(
                            text = "$completedCount de $totalSteps",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1A1A2E)),
                        )
                    }
                }
            }

            // Checklist rows
            val items = listOf(
                ChecklistItem46("chk-1", "Confirmar invitados (RSVP)", null, RfIconKind.GUESTS, Color(0xFFEFE8FD), Color(0xFF7A4FD8)),
                ChecklistItem46("chk-2", "Revisar artículos reservados", null, RfIconKind.BOX, Color(0xFFFDE6F2), Color(0xFFC2338F)),
                ChecklistItem46("chk-3", "Verificar dirección de entrega", "Jardín Botánico Santo Domingo", RfIconKind.LOCATION, Color(0xFFDDF4F3), Color(0xFF1F8A84)),
                ChecklistItem46("chk-4", "Preparar espacio para montaje", null, RfIconKind.CHECKLIST, Color(0xFFEEF6D6), Color(0xFF4E6A10)),
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items.forEach { item ->
                    val isChecked = checklist.firstOrNull { it.id == item.id }?.completed ?: false
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = false)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFF2EDF5), RoundedCornerShape(20.dp))
                            .clickable {
                                viewModel?.toggleChecklistStep(item.id)
                            }
                            .testTag("checklist-step-${item.id}")
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ChecklistBox46(checked = isChecked, ringColor = Color(0xFFCDBFCB))

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(item.iconBg),
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = item.icon, tint = item.iconTint, size = 20.dp)
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            BasicText(
                                text = item.title,
                                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1A1A2E)),
                            )
                            item.subtitle?.let {
                                BasicText(
                                    text = it,
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF62627A)),
                                )
                            }
                        }

                        RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = Color(0xFF62627A), size = 18.dp)
                    }
                }

                // Pending payment row (5th checklist item)
                val paid = checklist.firstOrNull { it.id == "chk-5" }?.completed ?: false
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFFFF2C9))
                        .clickable {
                            viewModel?.toggleChecklistStep("chk-5")
                        }
                        .testTag("checklist-step-chk-5")
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ChecklistBox46(checked = paid, ringColor = Color(0xFFD9B26A))

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White),
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.WALLET, tint = Color(0xFF7A5700), size = 20.dp)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        BasicText(
                            text = "Pago restante",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1A1A2E)),
                        )
                        BasicText(
                            text = "RD$ 3,150",
                            style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = Color(0xFF7A5700)),
                        )
                    }

                    Box(
                        modifier = Modifier
                            .height(40.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(RosaButtonGradient)
                            .clickable { onSelectScreenCode("s48") }
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = "Pagar",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

private data class ChecklistItem46(
    val id: String,
    val title: String,
    val subtitle: String?,
    val icon: RfIconKind,
    val iconBg: Color,
    val iconTint: Color,
)

@Composable
private fun ChecklistBox46(checked: Boolean, ringColor: Color) {
    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(CircleShape)
            .background(if (checked) Color(0xFF8DB52C) else Color.Transparent)
            .border(2.dp, if (checked) Color(0xFF8DB52C) else ringColor, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            RfIcon(kind = RfIconKind.CHECK, tint = Color.White, size = 16.dp)
        }
    }
}

// ==============================================================================
// s47 · INVITADOS
// ==============================================================================
@Composable
internal fun RenderGuestsScreenS47(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("Todos") }
    var guestStatuses by remember {
        mutableStateOf(
            listOf(
                Triple("Familia Pérez Martínez", "Confirmados · Mesa 2 · 4 personas", "Confirmado"),
                Triple("Carlos & Andrea Sánchez", "Confirmados · Mesa 1 · 2 personas", "Confirmado"),
                Triple("Dra. Laura Gómez", "Pendiente respuesta · 1 persona", "Pendiente"),
                Triple("Lic. Roberto Fernández", "Confirmado · Mesa 4 · 2 personas", "Confirmado"),
            ),
        )
    }

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
                        .clickable { onSelectScreenCode("BACK:s40") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1A1A2E), size = 22.dp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "Invitados",
                        style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, lineHeight = 22.sp, color = Color(0xFF1A1A2E)),
                    )
                    BasicText(
                        text = "${116 + guestStatuses.size} invitados · Boda Real",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF62627A)),
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = false)
                        .clip(CircleShape)
                        .background(RosaButtonGradient)
                        .clickable {
                            val nextGuestNum = guestStatuses.size + 1
                            guestStatuses = guestStatuses + Triple(
                                "Invitado VIP #$nextGuestNum",
                                "Confirmado · Mesa ${nextGuestNum + 3} · 2 personas",
                                "Confirmado",
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.PLUS, tint = Color.White, size = 22.dp)
                }
            }

            // Stat pills: Confirmados (84), Pendientes (26), Rechazados (10)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .rfCardShadow(RoundedCornerShape(18.dp), isAdmin = false)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFFEEF6D6))
                        .clickable {
                            selectedStatusFilter = if (selectedStatusFilter == "Confirmado") "Todos" else "Confirmado"
                        }
                        .padding(12.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        BasicText("Confirmados", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF62627A), fontSize = 12.sp))
                        BasicText("${81 + guestStatuses.count { guest -> guest.third == "Confirmado" }}", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4E6A10), fontSize = 24.sp))
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .rfCardShadow(RoundedCornerShape(18.dp), isAdmin = false)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFFFFF2C9))
                        .clickable {
                            selectedStatusFilter = if (selectedStatusFilter == "Pendiente") "Todos" else "Pendiente"
                        }
                        .padding(12.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        BasicText("Pendientes", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF62627A), fontSize = 12.sp))
                        BasicText("${25 + guestStatuses.count { guest -> guest.third == "Pendiente" }}", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, color = Color(0xFF7A5700), fontSize = 24.sp))
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .rfCardShadow(RoundedCornerShape(18.dp), isAdmin = false)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFFFDE6F2))
                        .clickable {
                            selectedStatusFilter = "Todos"
                        }
                        .padding(12.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        BasicText("Rechazados", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF62627A), fontSize = 12.sp))
                        BasicText("10", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, color = Color(0xFFB42345), fontSize = 24.sp))
                    }
                }
            }

            // Search bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .rfInputShadow(RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFF0EBF4), RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                RfIcon(kind = RfIconKind.SEARCH, tint = Color(0xFF62627A), size = 18.dp)
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { updatedText -> searchQuery = updatedText },
                    singleLine = true,
                    textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF1A1A2E)),
                    cursorBrush = SolidColor(Color(0xFFC2338F)),
                    modifier = Modifier.weight(1f),
                    decorationBox = { innerTextField ->
                        if (searchQuery.isEmpty()) {
                            BasicText(
                                text = "Buscar invitado por nombre o familia",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF62627A)),
                            )
                        }
                        innerTextField()
                    },
                )
            }

            val filteredGuests = guestStatuses.filter { guestTriple ->
                val matchesStatus = if (selectedStatusFilter == "Todos") true else guestTriple.third == selectedStatusFilter
                val trimmedSearch = searchQuery.trim()
                val matchesSearch = if (trimmedSearch.isEmpty()) {
                    true
                } else {
                    "${guestTriple.first} ${guestTriple.second}".contains(trimmedSearch, ignoreCase = true)
                }
                if (matchesStatus) matchesSearch else false
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                filteredGuests.forEach { (name, desc, badge) ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = false)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFF2EDF5), RoundedCornerShape(20.dp))
                            .clickable {
                                guestStatuses = guestStatuses.map { existingGuest ->
                                    if (existingGuest.first == name) {
                                        val nextStatus = if (existingGuest.third == "Confirmado") "Pendiente" else "Confirmado"
                                        Triple(existingGuest.first, existingGuest.second, nextStatus)
                                    } else {
                                        existingGuest
                                    }
                                }
                            }
                            .padding(14.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                BasicText(name, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E), fontSize = 15.sp))
                                BasicText(desc, style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF62627A), fontSize = 13.sp))
                            }
                            Box(
                                modifier = Modifier
                                    .height(24.dp)
                                    .clip(CircleShape)
                                    .background(if (badge == "Confirmado") Color(0xFFEEF6D6) else Color(0xFFFFF2C9))
                                    .padding(horizontal = 9.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                BasicText(
                                    text = badge,
                                    style = TextStyle(
                                        fontFamily = typo.bodyFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (badge == "Confirmado") Color(0xFF4E6A10) else Color(0xFF7A5700),
                                    ),
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// ==============================================================================
// s48 · COTIZACIÓN
// ==============================================================================
@Composable
internal fun RenderQuoteScreenS48(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val events = viewModel?.events?.collectAsState()?.value ?: CanonicalSampleData.events
    val selectedId = viewModel?.selectedEventId?.collectAsState()?.value ?: "evt-boda-real"
    val event = events.firstOrNull { it.id == selectedId } ?: events.first()

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

                BasicText(
                    text = "Tu cotización",
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF1A1A2E)),
                    modifier = Modifier.weight(1f),
                )

                Box(
                    modifier = Modifier
                        .height(26.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFDDF4F3))
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText("Lista para aprobar", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF1F8A84), fontSize = 12.sp))
                }
            }

            // Quotation breakdown section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(22.dp), isAdmin = false)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFF2EDF5), RoundedCornerShape(22.dp)),
            ) {
                Column {
                    // Header info
                    Column(
                        modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        BasicText(
                            text = event.title,
                            style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF1A1A2E)),
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            RfIcon(kind = RfIconKind.CALENDAR, tint = Color(0xFF62627A), size = 16.dp)
                            BasicText(event.dateLabel, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF62627A)))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            RfIcon(kind = RfIconKind.LOCATION, tint = Color(0xFF62627A), size = 16.dp)
                            BasicText(event.venue, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF62627A)))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            RfIcon(kind = RfIconKind.USER, tint = Color(0xFF62627A), size = 16.dp)
                            BasicText("${event.guests} invitados", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF62627A)))
                        }
                    }

                    // Dashed separator
                    Canvas(modifier = Modifier.fillMaxWidth().height(1.dp).padding(horizontal = 18.dp)) {
                        drawLine(
                            color = Color(0xFFEEE9F2),
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            strokeWidth = 2f * density,
                        )
                    }

                    // Line items
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        if (event.items.isNotEmpty()) {
                            event.items.forEach { item ->
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column {
                                        BasicText(item.name, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color(0xFF1A1A2E)))
                                        BasicText("${item.quantity} unidades · RD$ ${item.unitPriceDop} c/u", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF62627A)))
                                    }
                                    BasicText("RD$ ${item.subtotalDop}", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color(0xFF1A1A2E)))
                                }
                            }
                        } else {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    BasicText("Mobiliario y ambientación", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color(0xFF1A1A2E)))
                                    BasicText("${event.articleCount} artículos de catálogo", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF62627A)))
                                }
                                BasicText("RD$ ${event.totalDop * 85 / 100}", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color(0xFF1A1A2E)))
                            }
                        }

                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF2F0F5)))

                        val subtotal = event.totalDop * 94 / 100
                        val delivery = event.totalDop - subtotal

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            BasicText("Subtotal", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF62627A)))
                            BasicText("RD$ $subtotal", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF62627A)))
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            BasicText("Transporte e instalación", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF62627A)))
                            BasicText("RD$ $delivery", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF62627A)))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            BasicText("Total", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1A1A2E)))
                            BasicText("RD$ ${event.totalDop}", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, color = Color(0xFF1A1A2E)))
                        }
                    }
                }
            }

            // Note card from María
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = false)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFFFF2C9))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText("M", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF7A5700)))
                    }
                    Column {
                        BasicText("Nota de María · Rosa Fiesta", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF7A5700)))
                        BasicText("Incluye transporte e instalación VIP con arco orgánico doble.", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, lineHeight = 20.sp, color = Color(0xFF1A1A2E)))
                    }
                }
            }
        }

        // Bottom CTA buttons
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 20.dp, end = 20.dp, bottom = 36.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = false)
                    .clip(RoundedCornerShape(16.dp))
                    .background(RosaButtonGradient)
                    .clickable { onSelectScreenCode("s50") },
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    text = "Aprobar cotización",
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White,
                    ),
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clickable { onSelectScreenCode("BACK:s40") },
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    text = "Rechazar",
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFFB42345),
                    ),
                )
            }
        }
    }
}
