package com.jeudry.rosafiesta.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeudry.rosafiesta.designsystem.LocalRfTypography
import com.jeudry.rosafiesta.designsystem.RfIcon
import com.jeudry.rosafiesta.designsystem.RfIconKind
import com.jeudry.rosafiesta.designsystem.RfPhoto
import com.jeudry.rosafiesta.designsystem.RfPhotoKey
import com.jeudry.rosafiesta.designsystem.rfCardShadow
import com.jeudry.rosafiesta.designsystem.rfIconButtonShadow
import com.jeudry.rosafiesta.domain.CanonicalSampleData

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
@Composable
internal fun RenderMyEventsScreenS37(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    var selectedTab by remember { mutableStateOf(0) } // 0: Próximos, 1: Pasados

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFC)),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(EventBackgroundGradient)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            // Header: Título + Calendario + Botón Nuevo (+)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicText(
                    text = "Mis eventos",
                    style = TextStyle(
                        fontFamily = typo.displayFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 26.sp,
                        color = Color(0xFF1A1A2E),
                    ),
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .shadow(6.dp, CircleShape, spotColor = Color(0x33B450B4))
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
                            .shadow(8.dp, CircleShape, spotColor = Color(0x66A050D2))
                            .clip(CircleShape)
                            .background(RosaButtonGradient)
                            .clickable { onSelectScreenCode("s38") },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.PLUS, tint = Color.White, size = 22.dp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Segmented control: Próximos (3) vs Pasados (1)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF1EFF5))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedTab == 0) Color.White else Color.Transparent)
                        .clickable { selectedTab = 0 },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = "Próximos · 3",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp,
                            color = if (selectedTab == 0) Color(0xFF1A1A2E) else Color(0xFF62627A),
                        ),
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedTab == 1) Color.White else Color.Transparent)
                        .clickable { selectedTab = 1 },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = "Pasados · 1",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp,
                            color = if (selectedTab == 1) Color(0xFF1A1A2E) else Color(0xFF62627A),
                        ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Tarjetas de eventos
            val events = listOf(
                Triple("Boda Real Rosa & Dorado", "Vie 23 oct · en 3 días", RfPhotoKey.BODA),
                Triple("Baby Shower Sofía", "Sáb 2 nov · en 13 días", RfPhotoKey.BABY_SHOWER),
                Triple("Quinceañera Isabella", "Dom 9 nov · en 20 días", RfPhotoKey.QUINCEANERA),
            )

            events.forEach { (name, date, photo) ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .shadow(6.dp, RoundedCornerShape(22.dp), spotColor = Color(0x22865DEC))
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFF2EDF5), RoundedCornerShape(22.dp))
                        .clickable { onSelectScreenCode("s40") }
                        .padding(14.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFFDE6F2)),
                            contentAlignment = Alignment.Center,
                        ) {
                            RfPhoto(photoKey = photo, contentDescription = name)
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            BasicText(
                                text = name,
                                style = TextStyle(
                                    fontFamily = typo.displayFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF1A1A2E),
                                ),
                            )
                            BasicText(
                                text = date,
                                style = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = Color(0xFFC2338F),
                                ),
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(Color(0xFFEFE8FD)),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.6f)
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(RosaButtonGradient),
                                    )
                                }
                                BasicText(
                                    text = "3 de 5",
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, color = Color(0xFF62627A)),
                                )
                            }
                        }

                        RfIcon(kind = RfIconKind.MORE_VERTICAL, tint = Color(0xFF865DEC), size = 18.dp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(110.dp))
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
                .fillMaxWidth()
                .padding(top = 56.dp, start = 20.dp, end = 20.dp),
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
                        .shadow(6.dp, CircleShape, spotColor = Color(0x33B450B4))
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("s37") },
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
                                    val w = size.width; val h = size.height
                                    when (card.illus) {
                                        EventTypeIllus.CUMPLE -> {
                                            // Birthday cake
                                            drawRoundRect(color = Color(0xFFFFF6EE), topLeft = androidx.compose.ui.geometry.Offset(w*0.25f, h*0.48f), size = androidx.compose.ui.geometry.Size(w*0.50f, h*0.33f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f*density))
                                            drawRect(color = Color(0xFFF9B6D3), topLeft = androidx.compose.ui.geometry.Offset(w*0.25f, h*0.68f), size = androidx.compose.ui.geometry.Size(w*0.50f, h*0.05f))
                                            // Candles
                                            drawRoundRect(color = Color(0xFF7650E8), topLeft = androidx.compose.ui.geometry.Offset(w*0.35f, h*0.34f), size = androidx.compose.ui.geometry.Size(w*0.04f, h*0.14f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f*density))
                                            drawRoundRect(color = Color(0xFF4AA8E0), topLeft = androidx.compose.ui.geometry.Offset(w*0.48f, h*0.32f), size = androidx.compose.ui.geometry.Size(w*0.04f, h*0.17f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f*density))
                                            drawRoundRect(color = Color(0xFF2FB58F), topLeft = androidx.compose.ui.geometry.Offset(w*0.61f, h*0.34f), size = androidx.compose.ui.geometry.Size(w*0.04f, h*0.14f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f*density))
                                            // Flames
                                            drawCircle(color = Color(0xFFF5B53B), radius = 4f*density, center = androidx.compose.ui.geometry.Offset(w*0.37f, h*0.31f))
                                            drawCircle(color = Color(0xFFF5B53B), radius = 4f*density, center = androidx.compose.ui.geometry.Offset(w*0.50f, h*0.29f))
                                            drawCircle(color = Color(0xFFF5B53B), radius = 4f*density, center = androidx.compose.ui.geometry.Offset(w*0.63f, h*0.31f))
                                        }
                                        EventTypeIllus.BODA -> {
                                            // Two interlocked rings
                                            val strokeW = 6.5f * density
                                            drawCircle(color = Color(0xFFD9A441), radius = w*0.18f, center = androidx.compose.ui.geometry.Offset(w*0.40f, h*0.58f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeW))
                                            drawCircle(color = Color(0xFFF0CC7A), radius = w*0.18f, center = androidx.compose.ui.geometry.Offset(w*0.60f, h*0.60f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeW))
                                            // Diamond
                                            val path = androidx.compose.ui.graphics.Path()
                                            path.moveTo(w*0.35f, h*0.36f); path.lineTo(w*0.47f, h*0.30f); path.lineTo(w*0.47f, h*0.37f); path.lineTo(w*0.41f, h*0.44f); path.close()
                                            drawPath(path, color = Color(0xFFA8D8F5))
                                        }
                                        EventTypeIllus.QUINCEANERA -> {
                                            // Crown with sparkles
                                            val crownColor = Color(0xFFD9A441)
                                            val crownPath = androidx.compose.ui.graphics.Path()
                                            crownPath.moveTo(w*0.20f, h*0.75f); crownPath.lineTo(w*0.20f, h*0.50f); crownPath.lineTo(w*0.35f, h*0.62f); crownPath.lineTo(w*0.50f, h*0.40f); crownPath.lineTo(w*0.65f, h*0.62f); crownPath.lineTo(w*0.80f, h*0.50f); crownPath.lineTo(w*0.80f, h*0.75f); crownPath.close()
                                            drawPath(crownPath, color = crownColor)
                                            drawCircle(color = Color(0xFFEC4F97), radius = 5f*density, center = androidx.compose.ui.geometry.Offset(w*0.50f, h*0.38f))
                                            drawCircle(color = Color(0xFF7650E8), radius = 3.5f*density, center = androidx.compose.ui.geometry.Offset(w*0.30f, h*0.58f))
                                            drawCircle(color = Color(0xFF2FB58F), radius = 3.5f*density, center = androidx.compose.ui.geometry.Offset(w*0.70f, h*0.56f))
                                        }
                                        EventTypeIllus.BABY_SHOWER -> {
                                            // Clothesline with baby onesie
                                            drawPath(
                                                path = androidx.compose.ui.graphics.Path().apply {
                                                    moveTo(w*0.05f, h*0.25f); cubicTo(w*0.50f, h*0.38f, w*0.95f, h*0.25f, w*0.95f, h*0.25f)
                                                },
                                                color = Color(0xFF6E5A72).copy(alpha = 0.45f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f*density)
                                            )
                                            // Onesie body
                                            drawRoundRect(color = Color(0xFF9BE3C9), topLeft = androidx.compose.ui.geometry.Offset(w*0.33f, h*0.35f), size = androidx.compose.ui.geometry.Size(w*0.34f, h*0.38f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f*density))
                                            // Sleeves
                                            drawRoundRect(color = Color(0xFF9BE3C9), topLeft = androidx.compose.ui.geometry.Offset(w*0.18f, h*0.35f), size = androidx.compose.ui.geometry.Size(w*0.17f, h*0.16f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f*density))
                                            drawRoundRect(color = Color(0xFF9BE3C9), topLeft = androidx.compose.ui.geometry.Offset(w*0.65f, h*0.35f), size = androidx.compose.ui.geometry.Size(w*0.17f, h*0.16f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f*density))
                                            // Star detail
                                            drawCircle(color = Color(0xFFF5B53B), radius = 4f*density, center = androidx.compose.ui.geometry.Offset(w*0.50f, h*0.54f))
                                        }
                                        EventTypeIllus.GRADUACION -> {
                                            // Graduation cap
                                            val capColor = Color(0xFF1A1A2E)
                                            drawRoundRect(color = capColor, topLeft = androidx.compose.ui.geometry.Offset(w*0.20f, h*0.45f), size = androidx.compose.ui.geometry.Size(w*0.60f, h*0.14f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f*density))
                                            val hatPath = androidx.compose.ui.graphics.Path()
                                            hatPath.moveTo(w*0.50f, h*0.28f); hatPath.lineTo(w*0.80f, h*0.45f); hatPath.lineTo(w*0.50f, h*0.60f); hatPath.lineTo(w*0.20f, h*0.45f); hatPath.close()
                                            drawPath(hatPath, color = capColor)
                                            // Tassel
                                            drawLine(color = Color(0xFFF5B53B), start = androidx.compose.ui.geometry.Offset(w*0.76f, h*0.45f), end = androidx.compose.ui.geometry.Offset(w*0.76f, h*0.72f), strokeWidth = 2.5f*density)
                                            drawCircle(color = Color(0xFFF5B53B), radius = 5f*density, center = androidx.compose.ui.geometry.Offset(w*0.76f, h*0.75f))
                                        }
                                        EventTypeIllus.CORPORATIVO -> {
                                            // Champagne glasses clinking
                                            val glassColor = Color(0xFFD9A441)
                                            // Left glass
                                            val lp = androidx.compose.ui.graphics.Path()
                                            lp.moveTo(w*0.28f, h*0.25f); lp.lineTo(w*0.38f, h*0.25f); lp.lineTo(w*0.34f, h*0.50f); lp.lineTo(w*0.32f, h*0.50f); lp.close()
                                            drawPath(lp, color = glassColor)
                                            drawLine(color = glassColor, start = androidx.compose.ui.geometry.Offset(w*0.33f, h*0.50f), end = androidx.compose.ui.geometry.Offset(w*0.33f, h*0.72f), strokeWidth = 2.5f*density)
                                            // Right glass
                                            val rp = androidx.compose.ui.graphics.Path()
                                            rp.moveTo(w*0.62f, h*0.25f); rp.lineTo(w*0.72f, h*0.25f); rp.lineTo(w*0.68f, h*0.50f); rp.lineTo(w*0.66f, h*0.50f); rp.close()
                                            drawPath(rp, color = glassColor)
                                            drawLine(color = glassColor, start = androidx.compose.ui.geometry.Offset(w*0.67f, h*0.50f), end = androidx.compose.ui.geometry.Offset(w*0.67f, h*0.72f), strokeWidth = 2.5f*density)
                                            // Bubbles
                                            drawCircle(color = Color(0xFFF5B53B).copy(alpha = 0.6f), radius = 3f*density, center = androidx.compose.ui.geometry.Offset(w*0.32f, h*0.35f))
                                            drawCircle(color = Color(0xFFF5B53B).copy(alpha = 0.6f), radius = 2f*density, center = androidx.compose.ui.geometry.Offset(w*0.36f, h*0.28f))
                                            drawCircle(color = Color(0xFFF5B53B).copy(alpha = 0.6f), radius = 3f*density, center = androidx.compose.ui.geometry.Offset(w*0.68f, h*0.33f))
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
                .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = Color(0xB3C846B4))
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
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
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
                        .shadow(6.dp, CircleShape, spotColor = Color(0x33B450B4))
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("s37") },
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
                    .shadow(10.dp, RoundedCornerShape(22.dp), spotColor = Color(0x22865DEC))
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FB)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 56.dp, start = 20.dp, end = 20.dp, bottom = 30.dp)
                .verticalScroll(rememberScrollState()),
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
                        .clickable { onSelectScreenCode("s37") },
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
                        .border(1.dp, Color(0xFFEEEAF2), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.RECEIPT, tint = Color(0xFF1A1A2E), size = 20.dp)
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
                        modifier = Modifier.height(44.dp),
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
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFF0EDF3), RoundedCornerShape(22.dp))
                    .padding(horizontal = 18.dp, vertical = 16.dp),
            ) {
                // Background rings illustration at top right
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 10.dp, y = (-8).dp),
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val s = size.width / 120f
                        drawCircle(
                            color = Color(0xFFD9A441),
                            radius = 21f * s,
                            center = Offset(49f * s, 70f * s),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6.5f * s),
                        )
                        drawCircle(
                            color = Color(0xFFF0CC7A),
                            radius = 21f * s,
                            center = Offset(72f * s, 72f * s),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6.5f * s),
                        )
                        drawCircle(Color(0xFFB9D84A), radius = 4f * s, center = Offset(28f * s, 26f * s))
                    }
                }

                Column {
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
                    Spacer(Modifier.height(8.dp))
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
                    RfIcon(kind = RfIconKind.PLAY, tint = Color(0xFF2E1A47), size = 20.dp)
                }
            }

            // Action Items Card (Tareas, Cronograma, Sincronizar)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFC)),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header llamativo con degradado morado-rosa
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                    .background(RosaButtonGradient)
                    .padding(top = 54.dp, start = 20.dp, end = 20.dp, bottom = 24.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
                                .background(Color.White.copy(alpha = 0.2f))
                                .clickable { onSelectScreenCode("s40") },
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.CLOSE, tint = Color.White, size = 22.dp)
                        }

                        Box(
                            modifier = Modifier
                                .height(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFD45C))
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText("En curso", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF2E1A47), fontSize = 12.sp))
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        // Círculo de progreso
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText("37%", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 20.sp))
                        }

                        Column {
                            BasicText("Modo ejecución", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 22.sp))
                            BasicText("Boda Real · 3 de 8 tareas listas", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFFF6EAFB), fontSize = 13.sp))
                        }
                    }
                }
            }

            // Lista de tareas en ejecución
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Spacer(modifier = Modifier.height(18.dp))

                val liveTasks = listOf(
                    Triple("Confirmar paleta floral blanco y rosa", "Completada a las 09:15", true),
                    Triple("Camión de transporte en ruta al Botánico", "Llegada estimada 10:30", true),
                    Triple("Montaje de 10 mesas redondas", "En progreso · 60%", false),
                    Triple("Instalación del arco de flores nupcial", "Pendiente para las 12:00", false),
                    Triple("Prueba de sonido e iluminación", "Pendiente para las 14:00", false),
                )

                liveTasks.forEach { (title, subtitle, completed) ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (completed) Color(0xFFF6F4F9) else Color.White)
                            .border(1.dp, Color(0xFFF2EDF5), RoundedCornerShape(18.dp))
                            .padding(14.dp),
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (completed) Color(0xFF8DB52C) else Color(0xFFF1EFF5)),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (completed) {
                                    RfIcon(kind = RfIconKind.CHECK, tint = Color.White, size = 18.dp)
                                }
                            }
                            Column {
                                BasicText(
                                    text = title,
                                    style = TextStyle(
                                        fontFamily = typo.bodyFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = if (completed) Color(0xFF62627A) else Color(0xFF1A1A2E),
                                        fontSize = 14.sp,
                                    ),
                                )
                                BasicText(subtitle, style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF8E8B9E), fontSize = 12.sp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

// ==============================================================================
// s44 · CRONOGRAMA
// ==============================================================================
@Composable
internal fun RenderTimelineScreenS44(
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
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(6.dp, CircleShape, spotColor = Color(0x33B450B4))
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("s40") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1A1A2E), size = 22.dp)
                }

                BasicText(
                    text = "Cronograma",
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF1A1A2E)),
                )

                Spacer(modifier = Modifier.size(44.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            val timeline = listOf(
                Pair("08:00 AM", "Llegada del equipo de logística al lugar"),
                Pair("09:30 AM", "Descarga y montaje de mobiliario"),
                Pair("11:30 AM", "Instalación de centros de mesa y manteles"),
                Pair("01:00 PM", "Montaje del arco floral nupcial"),
                Pair("03:00 PM", "Revisión final y entrega al cliente"),
                Pair("05:00 PM", "Inicio de la recepción"),
            )

            timeline.forEach { (time, desc) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Box(
                        modifier = Modifier
                            .width(74.dp)
                            .height(30.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF1EFF8)),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(time, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF865DEC), fontSize = 12.sp))
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        BasicText(desc, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A2E), fontSize = 14.sp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// ==============================================================================
// s45 · TAREAS
// ==============================================================================
@Composable
internal fun RenderTasksScreenS45(
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
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(6.dp, CircleShape, spotColor = Color(0x33B450B4))
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("s40") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1A1A2E), size = 22.dp)
                }

                BasicText(
                    text = "Tareas del evento",
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF1A1A2E)),
                )

                Spacer(modifier = Modifier.size(44.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            val tasks = listOf(
                "Aprobar muestra de centro de mesa" to true,
                "Confirmar número final de invitados" to true,
                "Pagar saldo restante (50%)" to false,
                "Asignar asientos en el plano" to false,
                "Coordinar entrega de pastel" to false,
            )

            tasks.forEach { (task, done) ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFF0EDF3), RoundedCornerShape(16.dp))
                        .padding(14.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(if (done) Color(0xFF865DEC) else Color(0xFFF1EFF5)),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (done) RfIcon(kind = RfIconKind.CHECK, tint = Color.White, size = 16.dp)
                        }
                        BasicText(
                            text = task,
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontWeight = FontWeight.SemiBold,
                                color = if (done) Color(0xFF62627A) else Color(0xFF1A1A2E),
                                fontSize = 14.sp,
                            ),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFC)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(6.dp, CircleShape, spotColor = Color(0x33B450B4))
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("s40") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1A1A2E), size = 22.dp)
                }

                BasicText(
                    text = "Checklist de artículos",
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF1A1A2E)),
                )

                Spacer(modifier = Modifier.size(44.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            checklist.forEach { step ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFF0EDF3), RoundedCornerShape(16.dp))
                        .clickable { viewModel?.toggleChecklistStep(step.id) }
                        .testTag("checklist-step-${step.id}")
                        .padding(14.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            BasicText(step.title, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E), fontSize = 14.sp))
                            BasicText("${step.stageLabel} · ${step.timeLabel}", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF865DEC), fontSize = 12.sp))
                        }
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(if (step.completed) Color(0xFF8DB52C) else Color(0xFFF1EFF5)),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (step.completed) {
                                RfIcon(kind = RfIconKind.CHECK, tint = Color.White, size = 16.dp)
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
// s47 · INVITADOS
// ==============================================================================
@Composable
internal fun RenderGuestsScreenS47(
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
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(6.dp, CircleShape, spotColor = Color(0x33B450B4))
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("s40") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1A1A2E), size = 22.dp)
                }

                BasicText(
                    text = "Lista de invitados",
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF1A1A2E)),
                )

                Spacer(modifier = Modifier.size(44.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Resumen de confirmación
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFE8F6F0))
                        .padding(14.dp),
                ) {
                    Column {
                        BasicText("Confirmados", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF2E9C76), fontSize = 12.sp))
                        BasicText("100 pax", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.Bold, color = Color(0xFF2E9C76), fontSize = 18.sp))
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFFFF6E6))
                        .padding(14.dp),
                ) {
                    Column {
                        BasicText("Pendientes", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFFC78400), fontSize = 12.sp))
                        BasicText("20 pax", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.Bold, color = Color(0xFFC78400), fontSize = 18.sp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val guests = listOf(
                "Familia Pérez Martínez" to "Confirmado · 4 personas",
                "Carlos & Andrea Sánchez" to "Confirmado · 2 personas",
                "Dra. Laura Gómez" to "Pendiente · 1 persona",
                "Lic. Roberto Fernández" to "Confirmado · 2 personas",
            )

            guests.forEach { (name, status) ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFF0EDF3), RoundedCornerShape(16.dp))
                        .padding(14.dp),
                ) {
                    Column {
                        BasicText(name, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E), fontSize = 14.sp))
                        BasicText(status, style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF62627A), fontSize = 12.sp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// ==============================================================================
// ==============================================================================
// s48 · COTIZACIÓN
// ==============================================================================
@Composable
internal fun RenderQuoteScreenS48(
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
                .fillMaxWidth()
                .padding(top = 56.dp, start = 20.dp, end = 20.dp),
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
                        .shadow(6.dp, CircleShape, spotColor = Color(0x33B450B4))
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("s40") },
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
                            text = "Boda Real Rosa & Dorado",
                            style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF1A1A2E)),
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            RfIcon(kind = RfIconKind.CALENDAR, tint = Color(0xFF62627A), size = 16.dp)
                            BasicText("Viernes 23 de octubre, 2026", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF62627A)))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            RfIcon(kind = RfIconKind.LOCATION, tint = Color(0xFF62627A), size = 16.dp)
                            BasicText("Jardín Botánico Santo Domingo", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF62627A)))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            RfIcon(kind = RfIconKind.USER, tint = Color(0xFF62627A), size = 16.dp)
                            BasicText("120 invitados", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF62627A)))
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
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                BasicText("Mobiliario", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color(0xFF1A1A2E)))
                                BasicText("120 sillas Tiffany · 12 mesas redondas", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF62627A)))
                            }
                            BasicText("RD$ 780", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color(0xFF1A1A2E)))
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                BasicText("Decoración floral y arco", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color(0xFF1A1A2E)))
                                BasicText("Backdrop, centros y arco", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF62627A)))
                            }
                            BasicText("RD$ 2,865", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color(0xFF1A1A2E)))
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                BasicText("Iluminación cálida", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color(0xFF1A1A2E)))
                                BasicText("Guirnaldas para el jardín", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF62627A)))
                            }
                            BasicText("RD$ 600", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color(0xFF1A1A2E)))
                        }

                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF2F0F5)))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            BasicText("Subtotal", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF62627A)))
                            BasicText("RD$ 4,245", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF62627A)))
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            BasicText("Transporte e instalación", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF62627A)))
                            BasicText("RD$ 255", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF62627A)))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            BasicText("Total", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1A1A2E)))
                            BasicText("RD$ 4,500", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, color = Color(0xFF1A1A2E)))
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
                    .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = Color(0xB3C846B4))
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
                    .clickable { onSelectScreenCode("s40") },
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
