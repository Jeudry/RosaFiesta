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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeudry.rosafiesta.designsystem.LocalRfTypography
import com.jeudry.rosafiesta.designsystem.RfIcon
import com.jeudry.rosafiesta.designsystem.RfIconKind
import com.jeudry.rosafiesta.designsystem.RfPhoto
import com.jeudry.rosafiesta.designsystem.RfPhotoKey
import com.jeudry.rosafiesta.designsystem.RosaAvatar
import com.jeudry.rosafiesta.designsystem.RosaAvatarSize

// ==============================================================================
// UTILIDADES COMPARTIDAS DE ROSA IA
// ==============================================================================

private val RosaBackgroundGradient = Brush.verticalGradient(
    0.0f to Color(0xFFDED5F7),
    0.4f to Color(0xFFF2EAF7),
    0.6f to Color(0xFFF5EEF7),
    1.0f to Color(0xFFD9D0F2),
)

private val AdminRosaBackgroundGradient = Brush.verticalGradient(
    0.0f to Color(0xFFE3E1FB),
    0.4f to Color(0xFFF1EEFA),
    0.6f to Color(0xFFF4F1FA),
    1.0f to Color(0xFFDCDDF8),
)

private val RosaButtonGradient = Brush.horizontalGradient(
    listOf(Color(0xFFEA4EA9), Color(0xFFB754CB), Color(0xFF865DEC)),
)

private val AdminButtonGradient = Brush.horizontalGradient(
    listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0)),
)

/**
 * Cabecera estándar del wizard de Rosa IA:
 * [Botón atrás/cerrar] -- Paso X de 6 (con 6 segmentos) -- [Botón Mi Evento (badge)]
 */
@Composable
private fun RosaWizardHeader(
    step: Int,
    totalSteps: Int = 6,
    cartCount: Int = 16,
    onBack: () -> Unit = {},
    onCart: () -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 54.dp, start = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Botón atrás / cerrar circular blanco
        Box(
            modifier = Modifier
                .size(44.dp)
                .shadow(6.dp, CircleShape, spotColor = Color(0x335A3CA0))
                .clip(CircleShape)
                .background(Color.White)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            RfIcon(
                kind = if (step == 1) RfIconKind.CLOSE else RfIconKind.BACK,
                tint = Color(0xFF1A1A2E),
                size = 22.dp,
            )
        }

        // Progreso central
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            BasicText(
                text = "Paso $step de $totalSteps",
                style = TextStyle(
                    fontFamily = typo.bodyFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF1A1A2E),
                ),
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.width(150.dp),
            ) {
                for (i in 1..totalSteps) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                if (i <= step) {
                                    Brush.horizontalGradient(listOf(Color(0xFFEA4EA9), Color(0xFFB754CB)))
                                } else {
                                    SolidColor(Color(0xFFDDD3F3))
                                },
                            ),
                    )
                }
            }
        }

        // Botón carrito / mi evento circular blanco con badge
        Box(
            modifier = Modifier
                .size(44.dp)
                .shadow(6.dp, CircleShape, spotColor = Color(0x335A3CA0))
                .clip(CircleShape)
                .background(Color.White)
                .clickable(onClick = onCart),
            contentAlignment = Alignment.Center,
        ) {
            RfIcon(
                kind = RfIconKind.ORDERS,
                tint = Color(0xFF1A1A2E),
                size = 22.dp,
            )
            if (cartCount > 0) {
                Box(
                    modifier = Modifier
                        .offset(x = 14.dp, y = (-14).dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFD45C))
                        .padding(horizontal = 5.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = "$cartCount",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            color = Color(0xFF2E1A47),
                        ),
                    )
                }
            }
        }
    }
}

// ==============================================================================
// s25 · MI EVENTO (BORRADOR / CARRITO)
// ==============================================================================
@Composable
internal fun RenderMyEventDraftS25(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFC)),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                Brush.radialGradient(
                    0.0f to Color(0xFFF9E3F1),
                    0.6f to Color(0x00F9E3F1),
                    center = Offset(0f, 0f),
                    radius = 400f,
                ),
            )
            drawRect(
                Brush.radialGradient(
                    0.0f to Color(0xFFEEE7FB),
                    0.6f to Color(0x00EEE7FB),
                    center = Offset(Float.MAX_VALUE, 0f),
                    radius = 350f,
                ),
            )
        }

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
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(6.dp, CircleShape, spotColor = Color(0x33B450B4))
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("s18") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1A1A2E), size = 22.dp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "Mi evento",
                        style = TextStyle(
                            fontFamily = typo.displayFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            letterSpacing = (-0.44).sp,
                            color = Color(0xFF1A1A2E),
                        ),
                    )
                    BasicText(
                        text = "Baby Shower Sofía en Rosas",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF62627A)),
                    )
                }

                Box(
                    modifier = Modifier
                        .height(26.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFFF1EFF5))
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = "Borrador",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF62627A)),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Card categoría "Sillas" expandida con artículos
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(22.dp), spotColor = Color(0x26C850AA))
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFF2EDF5), RoundedCornerShape(22.dp)),
            ) {
                Column {
                    // Header de la categoría
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { }
                            .padding(10.dp, 10.dp, 10.dp, 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFDE6F2)),
                        ) {
                            RfIcon(kind = RfIconKind.ORDERS, tint = Color(0xFFC2338F), size = 20.dp)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            BasicText(
                                "Sillas",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1A1A2E)),
                            )
                            BasicText(
                                "2 artículos · 120 unidades",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF62627A)),
                            )
                        }
                        BasicText(
                            "RD$ 420",
                            style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, letterSpacing = (-0.34).sp, color = Color(0xFF1A1A2E)),
                        )
                        RfIcon(kind = RfIconKind.CHEVRON_DOWN, tint = Color(0xFF62627A), size = 18.dp)
                    }

                    // Item 1
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF2F0F5)))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(14.dp))) {
                            RfPhoto(photoKey = RfPhotoKey.SILLA_TIFFANY, contentDescription = "Silla Tiffany", cornerRadius = 14.dp, modifier = Modifier.fillMaxSize())
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            BasicText("Silla Tiffany Cristal", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1A1A2E)))
                            BasicText("Transparente · RD$ 3.50 c/u", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF62627A)))
                            BasicText("RD$ 280", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, letterSpacing = (-0.32).sp, color = Color(0xFF1A1A2E)))
                        }
                        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.clip(RoundedCornerShape(12.dp)).border(1.dp, Color(0xFFE8E1EE), RoundedCornerShape(12.dp)).height(36.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(modifier = Modifier.size(32.dp, 36.dp), contentAlignment = Alignment.Center) { RfIcon(kind = RfIconKind.MINUS, tint = Color(0xFF1A1A2E), size = 16.dp) }
                                BasicText("80", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1A1A2E)))
                                Box(modifier = Modifier.size(32.dp, 36.dp), contentAlignment = Alignment.Center) { RfIcon(kind = RfIconKind.PLUS, tint = Color(0xFF1A1A2E), size = 16.dp) }
                            }
                            BasicText("Quitar", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF62627A)))
                        }
                    }

                    // Item 2
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF2F0F5)))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(14.dp))) {
                            RfPhoto(photoKey = RfPhotoKey.SILLA_TIFFANY, contentDescription = "Silla Tiffany Rosa", cornerRadius = 14.dp, modifier = Modifier.fillMaxSize())
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            BasicText("Silla Tiffany Cristal", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1A1A2E)))
                            BasicText("Rosa · RD$ 3.50 c/u", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF62627A)))
                            BasicText("RD$ 140", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, letterSpacing = (-0.32).sp, color = Color(0xFF1A1A2E)))
                        }
                        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.clip(RoundedCornerShape(12.dp)).border(1.dp, Color(0xFFE8E1EE), RoundedCornerShape(12.dp)).height(36.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(modifier = Modifier.size(32.dp, 36.dp), contentAlignment = Alignment.Center) { RfIcon(kind = RfIconKind.MINUS, tint = Color(0xFF1A1A2E), size = 16.dp) }
                                BasicText("40", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1A1A2E)))
                                Box(modifier = Modifier.size(32.dp, 36.dp), contentAlignment = Alignment.Center) { RfIcon(kind = RfIconKind.PLUS, tint = Color(0xFF1A1A2E), size = 16.dp) }
                            }
                            BasicText("Quitar", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF62627A)))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botón dashed "Agregar más artículos"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.5.dp, Color(0xFFE3DCE8), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    RfIcon(kind = RfIconKind.PLUS, tint = Color(0xFF1A1A2E), size = 18.dp)
                    BasicText(
                        "Agregar más artículos",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1A1A2E)),
                    )
                }
            }

            Spacer(modifier = Modifier.height(180.dp))
        }

        // Panel inferior fijo con precio + CTA
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFF2EDF5)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .padding(bottom = 18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    BasicText(
                        "120 artículos · 1 categoría",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF62627A)),
                    )
                    BasicText(
                        "RD$ 420",
                        style = TextStyle(
                            fontFamily = typo.displayFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp,
                            letterSpacing = (-0.48).sp,
                            color = Color(0xFF1A1A2E),
                        ),
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = Color(0xB3C846B4))
                        .clip(RoundedCornerShape(16.dp))
                        .background(RosaButtonGradient)
                        .clickable { onSelectScreenCode("s26") },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = "Ver detalle del evento",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White),
                    )
                }
            }
        }
    }
}


// ==============================================================================
// ==============================================================================
// s26 · ROSA · TIPO DE EVENTO (PASO 1 DE 6)
// ==============================================================================
@Composable
internal fun RenderRosaEventTypeS26(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RosaBackgroundGradient),
    ) {
        // Decorative background stars & confetti
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(color = Color(0xFFB9D84A), radius = 4.5f * density, center = Offset(318f * density, 168f * density))
            drawCircle(color = Color(0x33CFE48A), radius = 24f * density, center = Offset(40f * density, 700f * density))
        }

        // Header
        RosaWizardHeader(
            step = 1,
            totalSteps = 6,
            cartCount = 16,
            onBack = { onSelectScreenCode("s18") },
            onCart = { onSelectScreenCode("s25") },
        )

        // Main content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 128.dp, start = 20.dp, end = 20.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Saludo Rosa
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    RosaAvatar(size = RosaAvatarSize.MEDIUM, forceAdminPalette = false)
                    Row {
                        BasicText(
                            text = "¡Hola! Soy ",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF5A5672)),
                        )
                        BasicText(
                            text = "Rosa",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF9B45C8)),
                        )
                        BasicText(
                            text = ", tu asistente.",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF5A5672)),
                        )
                    }
                }

                // Título con subrayado 'celebrar'
                BasicText(
                    text = "¿Qué evento quieres celebrar?",
                    style = TextStyle(
                        fontFamily = typo.displayFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 26.sp,
                        lineHeight = 28.sp,
                        color = Color(0xFF1A1A2E),
                    ),
                )
            }

            // 6 event types in 2 columns
            data class EventTypeItem(
                val name: String,
                val bg: Color,
                val isGradient: Boolean = false,
                val textColor: Color = Color(0xFF1A1A2E),
            )

            val items = listOf(
                EventTypeItem("Boda", Color(0xFFFDE6F2)),
                EventTypeItem("Cumpleaños", Color(0xFFFFF2C9)),
                EventTypeItem("Baby shower", Color(0xFFDDF4F3)),
                EventTypeItem("Gender reveal", Color(0xFFEFE8FD)),
                EventTypeItem("Corporativo", Color(0xFFEEF6D6)),
                EventTypeItem("Otro", Color(0xFF865DEC), isGradient = true, textColor = Color.White),
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items.chunked(2).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        row.forEach { item ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(116.dp)
                                    .shadow(12.dp, RoundedCornerShape(20.dp), spotColor = Color(0x336E46B4))
                                    .clip(RoundedCornerShape(20.dp))
                                    .then(
                                        if (item.isGradient) {
                                            Modifier.background(Brush.linearGradient(listOf(Color(0xFFEA4EA9), Color(0xFF865DEC))))
                                        } else {
                                            Modifier.background(item.bg)
                                        }
                                    )
                                    .border(1.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(20.dp))
                                    .clickable { onSelectScreenCode("s27") }
                                    .padding(start = 14.dp, bottom = 12.dp, end = 14.dp),
                                contentAlignment = Alignment.BottomStart,
                            ) {
                                // SVG illustration at top-end
                                Canvas(
                                    modifier = Modifier
                                        .size(88.dp)
                                        .align(Alignment.TopEnd)
                                        .offset(x = 4.dp, y = (-2).dp),
                                ) {
                                    val w = size.width; val h = size.height
                                    when (item.name) {
                                        "Boda" -> {
                                            val gold = Color(0xFFD9A441)
                                            val lightGold = Color(0xFFF0CC7A)
                                            drawCircle(color = gold, radius = 17f * density, center = Offset(w * 0.45f, h * 0.65f), style = Stroke(width = 5f * density))
                                            drawCircle(color = lightGold, radius = 17f * density, center = Offset(w * 0.65f, h * 0.67f), style = Stroke(width = 5f * density))
                                            // Diamond
                                            val diamond = Path().apply {
                                                moveTo(w * 0.40f, h * 0.42f)
                                                lineTo(w * 0.45f, h * 0.35f)
                                                lineTo(w * 0.50f, h * 0.42f)
                                                lineTo(w * 0.45f, h * 0.50f)
                                                close()
                                            }
                                            drawPath(diamond, color = Color(0xFFA8D8F5))
                                        }
                                        "Cumpleaños" -> {
                                            // Cake base
                                            drawRoundRect(color = Color(0xFFFFF6EE), topLeft = Offset(w * 0.28f, h * 0.50f), size = Size(w * 0.52f, h * 0.34f), cornerRadius = CornerRadius(5f * density))
                                            // Pink frosting
                                            drawRoundRect(color = Color(0xFFEC4F97), topLeft = Offset(w * 0.28f, h * 0.50f), size = Size(w * 0.52f, h * 0.12f), cornerRadius = CornerRadius(4f * density))
                                            // Candles
                                            drawLine(color = Color(0xFF7650E8), start = Offset(w * 0.38f, h * 0.50f), end = Offset(w * 0.38f, h * 0.36f), strokeWidth = 4f * density, cap = StrokeCap.Round)
                                            drawLine(color = Color(0xFF4AA8E0), start = Offset(w * 0.52f, h * 0.50f), end = Offset(w * 0.52f, h * 0.32f), strokeWidth = 4f * density, cap = StrokeCap.Round)
                                            drawLine(color = Color(0xFF2FB58F), start = Offset(w * 0.66f, h * 0.50f), end = Offset(w * 0.66f, h * 0.36f), strokeWidth = 4f * density, cap = StrokeCap.Round)
                                            // Flames
                                            drawCircle(color = Color(0xFFF5B53B), radius = 3.5f * density, center = Offset(w * 0.38f, h * 0.31f))
                                            drawCircle(color = Color(0xFFF5B53B), radius = 4f * density, center = Offset(w * 0.52f, h * 0.27f))
                                            drawCircle(color = Color(0xFFF5B53B), radius = 3.5f * density, center = Offset(w * 0.66f, h * 0.31f))
                                        }
                                        "Baby shower" -> {
                                            // Onesie body
                                            val onesie = Path().apply {
                                                moveTo(w * 0.40f, h * 0.36f)
                                                lineTo(w * 0.32f, h * 0.44f)
                                                lineTo(w * 0.38f, h * 0.52f)
                                                lineTo(w * 0.42f, h * 0.48f)
                                                lineTo(w * 0.42f, h * 0.78f)
                                                lineTo(w * 0.62f, h * 0.78f)
                                                lineTo(w * 0.62f, h * 0.48f)
                                                lineTo(w * 0.66f, h * 0.52f)
                                                lineTo(w * 0.72f, h * 0.44f)
                                                lineTo(w * 0.64f, h * 0.36f)
                                                close()
                                            }
                                            drawPath(onesie, color = Color(0xFFA8D8F5))
                                            // Star center
                                            drawCircle(color = Color(0xFFF5B53B), radius = 5f * density, center = Offset(w * 0.52f, h * 0.55f))
                                        }
                                        "Gender reveal" -> {
                                            // Pink balloon
                                            drawOval(color = Color(0xFFF9B6D3), topLeft = Offset(w * 0.26f, h * 0.30f), size = Size(w * 0.32f, h * 0.38f))
                                            // Blue balloon
                                            drawOval(color = Color(0xFFA8D8F5), topLeft = Offset(w * 0.52f, h * 0.34f), size = Size(w * 0.32f, h * 0.38f))
                                        }
                                        "Corporativo" -> {
                                            val glassColor = Color(0xFFD9A441)
                                            drawLine(color = glassColor, start = Offset(w * 0.38f, h * 0.40f), end = Offset(w * 0.38f, h * 0.76f), strokeWidth = 2.5f * density)
                                            drawLine(color = glassColor, start = Offset(w * 0.30f, h * 0.76f), end = Offset(w * 0.46f, h * 0.76f), strokeWidth = 2.5f * density)
                                            drawLine(color = glassColor, start = Offset(w * 0.62f, h * 0.40f), end = Offset(w * 0.62f, h * 0.76f), strokeWidth = 2.5f * density)
                                            drawLine(color = glassColor, start = Offset(w * 0.54f, h * 0.76f), end = Offset(w * 0.70f, h * 0.76f), strokeWidth = 2.5f * density)
                                            drawCircle(color = Color(0xFFF5B53B), radius = 3f * density, center = Offset(w * 0.50f, h * 0.30f))
                                        }
                                        "Otro" -> {
                                            // Starburst
                                            val star = Path().apply {
                                                moveTo(w * 0.50f, h * 0.28f)
                                                lineTo(w * 0.58f, h * 0.48f)
                                                lineTo(w * 0.78f, h * 0.52f)
                                                lineTo(w * 0.58f, h * 0.56f)
                                                lineTo(w * 0.50f, h * 0.76f)
                                                lineTo(w * 0.42f, h * 0.56f)
                                                lineTo(w * 0.22f, h * 0.52f)
                                                lineTo(w * 0.42f, h * 0.48f)
                                                close()
                                            }
                                            drawPath(star, color = Color.White.copy(alpha = 0.85f))
                                        }
                                    }
                                }

                                BasicText(
                                    text = item.name,
                                    style = TextStyle(
                                        fontFamily = typo.displayFamily,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 17.sp,
                                        color = item.textColor,
                                    ),
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bottom input bar
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 16.dp, end = 16.dp, bottom = 34.dp)
                .fillMaxWidth()
                .height(58.dp)
                .shadow(14.dp, CircleShape, spotColor = Color(0x405A3CA0))
                .clip(CircleShape)
                .background(Color.White)
                .padding(start = 20.dp, end = 6.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                BasicText(
                    text = "Escríbele a Rosa o habla…",
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontSize = 15.sp,
                        color = Color(0xFF62627A),
                    ),
                    modifier = Modifier.weight(1f),
                )
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFFEA4EA9), Color(0xFF865DEC)))),
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.MIC, tint = Color.White, size = 22.dp)
                }
            }
        }
    }
}

// ==============================================================================
// s27 · ROSA · CÓMO HABLARME / TUTORIAL
// ==============================================================================
@Composable
internal fun RenderRosaTutorialS27(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RosaBackgroundGradient),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            RosaWizardHeader(
                step = 1,
                onBack = { onSelectScreenCode("s26") },
                onCart = { onSelectScreenCode("s25") },
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                // Burbuja usuario a la derecha
                Box(
                    modifier = Modifier
                        .align(Alignment.End)
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 6.dp))
                        .background(RosaButtonGradient)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        RfIcon(kind = RfIconKind.SPARK, tint = Color.White, size = 18.dp)
                        BasicText(
                            text = "Boda",
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White,
                            ),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Respuesta Rosa
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    RosaAvatar(size = RosaAvatarSize.MEDIUM, forceAdminPalette = false)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .shadow(6.dp, RoundedCornerShape(topStart = 6.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp), spotColor = Color(0x22865DEC))
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp))
                            .background(Color.White)
                            .padding(16.dp),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            BasicText(
                                text = "¡Felicidades por tu boda! 💍✨",
                                style = TextStyle(
                                    fontFamily = typo.displayFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF1A1A2E),
                                ),
                            )
                            BasicText(
                                text = "Puedo ayudarte a armar todo: desde sillas y mesas hasta centros de mesa florales y arcos espectaculares.",
                                style = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp,
                                    color = Color(0xFF5A5672),
                                ),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Sugerencias rápidas en chips
                BasicText(
                    text = "Respuestas rápidas:",
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF62627A),
                    ),
                )
                Spacer(modifier = Modifier.height(10.dp))

                val quickReplies = listOf(
                    "Quiero un paquete para 100 personas",
                    "Estilo boho chic en jardín",
                    "Presupuesto moderado con arco de flores",
                    "Ver catálogo de sillas y manteles",
                )

                quickReplies.forEach { reply ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.9f))
                            .border(1.dp, Color(0xFFE5DFEC), RoundedCornerShape(14.dp))
                            .clickable { onSelectScreenCode("s28") }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                    ) {
                        BasicText(
                            text = reply,
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = Color(0xFF1A1A2E),
                            ),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Barra inferior de entrada simulada
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    BasicText(
                        text = "Escribe o usa tu voz...",
                        modifier = Modifier.weight(1f),
                        style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF8E8B9E), fontSize = 14.sp),
                    )
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1EFF8))
                            .clickable { onSelectScreenCode("s35") },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.MIC, tint = Color(0xFF865DEC), size = 18.dp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// ==============================================================================
// s28 · ROSA · ¿TE AYUDO? (PASO 2 DE 6)
// ==============================================================================
@Composable
internal fun RenderRosaHelpS28(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFDED5F7),
                        Color(0xFFF2EAF7),
                        Color(0xFFF5EEF7),
                        Color(0xFFD9D0F2),
                    ),
                ),
            ),
    ) {
        // Header
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 54.dp, start = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Color(0xFFF0EBF4), CircleShape)
                    .clickable { onSelectScreenCode("s26") },
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.CHEVRON_DOWN, tint = Color(0xFF1A1A2E), size = 22.dp)
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                BasicText(
                    text = "Paso 2 de 6",
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A2E),
                    ),
                )
                Row(
                    modifier = Modifier.width(150.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    repeat(2) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Brush.horizontalGradient(listOf(Color(0xFFEA4EA9), Color(0xFFB754CB)))),
                        )
                    }
                    repeat(4) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFFDDD3F3)),
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Color(0xFFF0EBF4), CircleShape)
                    .clickable { onSelectScreenCode("s25") },
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.ORDERS, tint = Color(0xFF1A1A2E), size = 22.dp)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp)
                        .height(20.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFD45C))
                        .padding(horizontal = 5.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = "16",
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF2E1A47),
                        ),
                    )
                }
            }
        }

        // Conversation bubble + Rosa heading
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .padding(top = 128.dp, start = 20.dp, end = 20.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.End)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 6.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9))))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    RfIcon(kind = RfIconKind.RING, tint = Color.White, size = 18.dp)
                    BasicText(
                        text = "Boda",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                        ),
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                RosaAvatar(size = RosaAvatarSize.MEDIUM, customSize = 40.dp)

                BasicText(
                    text = "¿Te ayudo a elegir los artículos de alquiler?",
                    style = TextStyle(
                        fontFamily = typo.displayFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 26.sp,
                        color = Color(0xFF1A1A2E),
                        lineHeight = 28.sp,
                    ),
                )
                BasicText(
                    text = "Te recomiendo según tu boda. Puedes cambiar todo después.",
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontSize = 16.sp,
                        color = Color(0xFF62627A),
                        lineHeight = 24.sp,
                    ),
                )
            }
        }

        // Options Buttons
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 116.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Option 1
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFFEA4EA9), Color(0xFF865DEC))))
                    .clickable { onSelectScreenCode("s29") }
                    .padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                RfIcon(kind = RfIconKind.SPARKLE, tint = Color.White, size = 22.dp)
                BasicText(
                    text = "Sí, busquemos juntas",
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    ),
                    modifier = Modifier.weight(1f),
                )
                RfIcon(kind = RfIconKind.ARROW_RIGHT, tint = Color.White, size = 20.dp)
            }

            // Option 2
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .clickable { onSelectScreenCode("s18") }
                    .padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                RfIcon(kind = RfIconKind.CATALOG, tint = Color(0xFF1A1A2E), size = 22.dp)
                BasicText(
                    text = "No, prefiero ver el catálogo",
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A2E),
                    ),
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // Bottom Input Bar
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 34.dp)
                .height(58.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(1.dp, Color(0xFFE7DEF5), CircleShape)
                .padding(start = 20.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            BasicText(
                text = "Escríbele a Rosa o habla…",
                style = TextStyle(
                    fontFamily = typo.bodyFamily,
                    fontSize = 15.sp,
                    color = Color(0xFF62627A),
                ),
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9)))),
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.MIC, tint = Color.White, size = 22.dp)
            }
        }
    }
}

// ==============================================================================
// s29 · ROSA · SUGERENCIAS DE PAQUETES (PASO 3 DE 6)
// ==============================================================================
@Composable
internal fun RenderRosaSuggestionsS29(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RosaBackgroundGradient),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            RosaWizardHeader(
                step = 3,
                cartCount = 17,
                onBack = { onSelectScreenCode("s28") },
                onCart = { onSelectScreenCode("s25") },
            )

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Rosa Avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9))
                            )
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    RosaAvatar(RosaAvatarSize.MEDIUM)
                }

                BasicText(
                    text = "Para tu boda te recomiendo estos artículos",
                    style = TextStyle(
                        fontFamily = typo.displayFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 26.sp,
                        letterSpacing = (-0.52).sp,
                        lineHeight = 30.sp,
                        color = Color(0xFF1A1A2E),
                    ),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Horizontal card row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Card 1: Centro de mesa floral
                Box(
                    modifier = Modifier
                        .width(208.dp)
                        .shadow(14.dp, RoundedCornerShape(22.dp), spotColor = Color(0x33AA46A0))
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.White),
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .background(Color(0xFFFFF2C9)),
                        ) {
                            RfPhoto(photoKey = RfPhotoKey.CENTRO_FLORAL, contentDescription = "Centro floral", cornerRadius = 0.dp, modifier = Modifier.fillMaxSize())
                        }
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            BasicText("Centro de mesa floral", maxLines = 1, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1A1A2E)))
                            BasicText("Flores naturales, base de madera", maxLines = 1, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF62627A)))
                            Spacer(Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                BasicText("RD$ 35", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 19.sp, color = Color(0xFF1A1A2E)))
                                Row(
                                    modifier = Modifier
                                        .height(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEEF6D6))
                                        .padding(horizontal = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    RfIcon(kind = RfIconKind.CHECK, tint = Color(0xFF4E6A10), size = 16.dp)
                                    BasicText("Agregado", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF4E6A10)))
                                }
                            }
                        }
                    }
                }

                // Card 2: Mesa dulce temática
                Box(
                    modifier = Modifier
                        .width(208.dp)
                        .shadow(14.dp, RoundedCornerShape(22.dp), spotColor = Color(0x33AA46A0))
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.White),
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .background(Color(0xFFEEF6D6)),
                        ) {
                            RfPhoto(photoKey = RfPhotoKey.MESA_HELADOS, contentDescription = "Mesa dulce", cornerRadius = 0.dp, modifier = Modifier.fillMaxSize())
                        }
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            BasicText("Mesa dulce temática", maxLines = 1, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1A1A2E)))
                            BasicText("Decoración completa para dulces", maxLines = 1, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF62627A)))
                            Spacer(Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                BasicText("RD$ 150", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 19.sp, color = Color(0xFF1A1A2E)))
                                Row(
                                    modifier = Modifier
                                        .height(36.dp)
                                        .shadow(8.dp, CircleShape, spotColor = Color(0xB3865DEC))
                                        .clip(CircleShape)
                                        .background(Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9))))
                                        .padding(horizontal = 14.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    RfIcon(kind = RfIconKind.PLUS, tint = Color.White, size = 16.dp)
                                    BasicText("Agregar", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White))
                                }
                            }
                        }
                    }
                }

                // Card 3: Arco de globos
                Box(
                    modifier = Modifier
                        .width(208.dp)
                        .shadow(14.dp, RoundedCornerShape(22.dp), spotColor = Color(0x33AA46A0))
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.White),
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .background(Color(0xFFFDE6F2)),
                        ) {
                            RfPhoto(photoKey = RfPhotoKey.ARCO_ROSA, contentDescription = "Arco de globos", cornerRadius = 0.dp, modifier = Modifier.fillMaxSize())
                        }
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            BasicText("Arco de globos", maxLines = 1, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1A1A2E)))
                            BasicText("Rosa y nude, para la entrada", maxLines = 1, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF62627A)))
                            Spacer(Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                BasicText("RD$ 95", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 19.sp, color = Color(0xFF1A1A2E)))
                                Row(
                                    modifier = Modifier
                                        .height(36.dp)
                                        .shadow(8.dp, CircleShape, spotColor = Color(0xB3865DEC))
                                        .clip(CircleShape)
                                        .background(Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9))))
                                        .padding(horizontal = 14.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    RfIcon(kind = RfIconKind.PLUS, tint = Color.White, size = 16.dp)
                                    BasicText("Agregar", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom action buttons (top of bottom bar)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(start = 20.dp, end = 20.dp, bottom = 110.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .clickable { /* refresh suggestions */ },
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    "Otras sugerencias",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1A1A2E)),
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
                    .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = Color(0xB3C846B4))
                    .clip(RoundedCornerShape(16.dp))
                    .background(RosaButtonGradient)
                    .clickable { onSelectScreenCode("s30") },
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    "Continuar",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White),
                )
            }
        }

        // Bottom input pill
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(start = 16.dp, end = 16.dp, bottom = 34.dp)
                .height(58.dp)
                .shadow(12.dp, CircleShape, spotColor = Color(0x26AA46A0))
                .border(1.dp, Color(0xFFE7DEF5), CircleShape)
                .clip(CircleShape)
                .background(Color.White)
                .padding(start = 20.dp, end = 6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicText(
                    text = "Pide algo distinto…",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF62627A)),
                )
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .shadow(8.dp, CircleShape, spotColor = Color(0xB3865DEC))
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9))))
                        .clickable { onSelectScreenCode("s35") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.MIC, tint = Color.White, size = 22.dp)
                }
            }
        }
    }
}

// ==============================================================================
// s30 · ROSA · CATEGORÍAS (PASO 4 DE 6)
// ==============================================================================
@Composable
internal fun RenderRosaCategoriesS30(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFDED5F7),
                        Color(0xFFF2EAF7),
                        Color(0xFFF5EEF7),
                        Color(0xFFD9D0F2),
                    ),
                ),
            ),
    ) {
        // Header
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 54.dp, start = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Color(0xFFF0EBF4), CircleShape)
                    .clickable { onSelectScreenCode("s29") },
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1A1A2E), size = 22.dp)
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                BasicText(
                    text = "Paso 4 de 6",
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A2E),
                    ),
                )
                Row(
                    modifier = Modifier.width(150.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    repeat(4) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Brush.horizontalGradient(listOf(Color(0xFFEA4EA9), Color(0xFFB754CB)))),
                        )
                    }
                    repeat(2) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFFDDD3F3)),
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Color(0xFFF0EBF4), CircleShape)
                    .clickable { onSelectScreenCode("s25") },
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.ORDERS, tint = Color(0xFF1A1A2E), size = 22.dp)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp)
                        .height(20.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFD45C))
                        .padding(horizontal = 5.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = "17",
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF2E1A47),
                        ),
                    )
                }
            }
        }

        // Content
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .padding(top = 128.dp, start = 20.dp, end = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                RosaAvatar(size = RosaAvatarSize.MEDIUM, customSize = 40.dp)
                BasicText(
                    text = "¿Te falta algo? Revisemos por categoría",
                    style = TextStyle(
                        fontFamily = typo.displayFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 26.sp,
                        color = Color(0xFF1A1A2E),
                        lineHeight = 28.sp,
                    ),
                )
            }

            // Categories list card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .padding(vertical = 6.dp),
            ) {
                // Sillas
                CategoryReviewRow(
                    title = "Sillas",
                    subtitle = "2 elegidas",
                    iconKind = RfIconKind.STORE,
                    iconBg = Color(0xFFFFF2C9),
                    iconTint = Color(0xFF7A5700),
                    isChosen = true,
                )
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF2F0F5)))

                // Mesas
                CategoryReviewRow(
                    title = "Mesas",
                    subtitle = "1 elegida",
                    iconKind = RfIconKind.BOX,
                    iconBg = Color(0xFFEEF6D6),
                    iconTint = Color(0xFF4E6A10),
                    isChosen = true,
                )
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF2F0F5)))

                // Iluminación
                CategoryReviewRow(
                    title = "Iluminación",
                    subtitle = null,
                    iconKind = RfIconKind.LIGHTBULB,
                    iconBg = Color(0xFFDDF4F3),
                    iconTint = Color(0xFF1F8A84),
                    isChosen = false,
                )
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF2F0F5)))

                // Mantelería
                CategoryReviewRow(
                    title = "Mantelería",
                    subtitle = null,
                    iconKind = RfIconKind.GRID,
                    iconBg = Color(0xFFEFE8FD),
                    iconTint = Color(0xFF7A4FD8),
                    isChosen = false,
                )
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF2F0F5)))

                // Centros de mesa
                CategoryReviewRow(
                    title = "Centros de mesa",
                    subtitle = null,
                    iconKind = RfIconKind.SPARK,
                    iconBg = Color(0xFFFDE6F2),
                    iconTint = Color(0xFFC2338F),
                    isChosen = false,
                )
            }
        }

        // Bottom CTA Buttons
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 110.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .clickable { },
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    text = "Más ideas",
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A2E),
                    ),
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFFEA4EA9), Color(0xFFB754CB), Color(0xFF865DEC))))
                    .clickable { onSelectScreenCode("s31") },
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    text = "Siguiente",
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    ),
                )
            }
        }

        // Bottom Input Bar
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 34.dp)
                .height(58.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(1.dp, Color(0xFFE7DEF5), CircleShape)
                .padding(start = 20.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            BasicText(
                text = "Escríbele a Rosa o habla…",
                style = TextStyle(
                    fontFamily = typo.bodyFamily,
                    fontSize = 15.sp,
                    color = Color(0xFF62627A),
                ),
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9)))),
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.MIC, tint = Color.White, size = 22.dp)
            }
        }
    }
}

@Composable
private fun CategoryReviewRow(
    title: String,
    subtitle: String?,
    iconKind: RfIconKind,
    iconBg: Color,
    iconTint: Color,
    isChosen: Boolean,
) {
    val typo = LocalRfTypography.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center,
        ) {
            RfIcon(kind = iconKind, tint = iconTint, size = 20.dp)
        }

        Column(modifier = Modifier.weight(1f)) {
            BasicText(
                text = title,
                style = TextStyle(
                    fontFamily = typo.bodyFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A2E),
                ),
            )
            if (subtitle != null) {
                BasicText(
                    text = subtitle,
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF4E6A10),
                    ),
                )
            }
        }

        if (isChosen) {
            RfIcon(kind = RfIconKind.CHECK, tint = Color(0xFF8DB52C), size = 24.dp)
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .clickable { }
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = "Omitir",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF62627A),
                        ),
                    )
                }

                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9))))
                        .clickable { }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = "Elegir",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        ),
                    )
                }
            }
        }
    }
}

// ==============================================================================
// s31 · ROSA · DETALLES DEL EVENTO (PASO 5 DE 6)
// ==============================================================================
@Composable
internal fun RenderRosaDetailsS31(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    var guestCount by remember { mutableStateOf(100) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RosaBackgroundGradient),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            RosaWizardHeader(
                step = 5,
                onBack = { onSelectScreenCode("s30") },
                onCart = { onSelectScreenCode("s25") },
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                BasicText(
                    text = "Cuéntame los detalles",
                    style = TextStyle(
                        fontFamily = typo.displayFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        color = Color(0xFF1A1A2E),
                    ),
                )
                BasicText(
                    text = "Para afinar cantidades y disponibilidad en tu fecha",
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontSize = 14.sp,
                        color = Color(0xFF62627A),
                    ),
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Campo Fecha
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    BasicText("Fecha estimada", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E), fontSize = 14.sp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White)
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        BasicText("Sábado, 24 de Noviembre 2026", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF1A1A2E), fontSize = 14.sp))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stepper Invitados
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    BasicText("Cantidad de invitados", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E), fontSize = 14.sp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White)
                            .padding(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        BasicText(
                            text = "$guestCount personas",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E), fontSize = 15.sp),
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF1EFF5))
                                    .clickable { if (guestCount > 10) guestCount -= 10 },
                                contentAlignment = Alignment.Center,
                            ) {
                                BasicText("-", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF865DEC), fontSize = 16.sp))
                            }
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF1EFF5))
                                    .clickable { guestCount += 10 },
                                contentAlignment = Alignment.Center,
                            ) {
                                BasicText("+", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF865DEC), fontSize = 16.sp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Campo Lugar / Ciudad
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    BasicText("Ciudad o municipio", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E), fontSize = 14.sp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White)
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        BasicText("Santo Domingo Este", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF1A1A2E), fontSize = 14.sp))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = Color(0x66C846B4))
                        .clip(RoundedCornerShape(16.dp))
                        .background(RosaButtonGradient)
                        .clickable { onSelectScreenCode("s32") },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = "Ver resumen de propuesta",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = Color.White,
                        ),
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// ==============================================================================
// ==============================================================================
// s32 · ROSA · RESUMEN (PASO 6 DE 6)
// ==============================================================================
@Composable
internal fun RenderRosaSummaryS32(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RosaBackgroundGradient),
    ) {
        // Header
        RosaWizardHeader(
            step = 6,
            totalSteps = 6,
            cartCount = 17,
            onBack = { onSelectScreenCode("s31") },
            onCart = { onSelectScreenCode("s25") },
        )

        // Main content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 128.dp, start = 20.dp, end = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                RosaAvatar(size = RosaAvatarSize.MEDIUM, forceAdminPalette = false)
                BasicText(
                    text = "Listo. Revisa que todo esté bien",
                    style = TextStyle(
                        fontFamily = typo.displayFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 26.sp,
                        lineHeight = 28.sp,
                        color = Color(0xFF1A1A2E),
                    ),
                )
            }

            // Summary Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(14.dp, RoundedCornerShape(24.dp), spotColor = Color(0x40AA46A0))
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .padding(vertical = 4.dp),
            ) {
                Column {
                    // Row 1: Evento · Boda
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFFDE6F2)),
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.RING, tint = Color(0xFFC2338F), size = 18.dp)
                        }
                        BasicText(
                            text = "Evento",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF62627A)),
                            modifier = Modifier.weight(1f),
                        )
                        BasicText(
                            text = "Boda",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1A1A2E)),
                        )
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF2F0F5)))

                    // Row 2: Fecha · Por confirmar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFDDF4F3)),
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.CALENDAR, tint = Color(0xFF1F8A84), size = 18.dp)
                        }
                        BasicText(
                            text = "Fecha",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF62627A)),
                            modifier = Modifier.weight(1f),
                        )
                        Box(
                            modifier = Modifier
                                .height(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFF2C9))
                                .padding(horizontal = 9.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(
                                text = "Por confirmar",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF7A5700)),
                            )
                        }
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF2F0F5)))

                    // Row 3: Ubicación · Por confirmar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFEEF6D6)),
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.LOCATION, tint = Color(0xFF4E6A10), size = 18.dp)
                        }
                        BasicText(
                            text = "Ubicación",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF62627A)),
                            modifier = Modifier.weight(1f),
                        )
                        Box(
                            modifier = Modifier
                                .height(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFF2C9))
                                .padding(horizontal = 9.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(
                                text = "Por confirmar",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF7A5700)),
                            )
                        }
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF2F0F5)))

                    // Row 4: Artículos · 160
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectScreenCode("s25") }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFFFF2C9)),
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.BOX, tint = Color(0xFF7A5700), size = 18.dp)
                        }
                        BasicText(
                            text = "Artículos",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF62627A)),
                            modifier = Modifier.weight(1f),
                        )
                        BasicText(
                            text = "160",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1A1A2E)),
                        )
                        RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = Color(0xFF62627A), size = 18.dp)
                    }
                }
            }

            // Info hint
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Box(modifier = Modifier.padding(top = 2.dp)) {
                    RfIcon(kind = RfIconKind.INFO, tint = Color(0xFF7A4FD8), size = 18.dp)
                }
                BasicText(
                    text = "El equipo de Rosa Fiesta revisa tu evento y te contacta con el precio final. No pagas nada todavía.",
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = Color(0xFF3B3550),
                    ),
                )
            }
        }

        // Bottom CTAs
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 20.dp, end = 20.dp, bottom = 40.dp)
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
                    .clickable { onSelectScreenCode("s33") },
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    text = "Solicitar cotización",
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
                    .clickable { onSelectScreenCode("s30") },
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    text = "Agregar algo más",
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF1A1A2E),
                    ),
                )
            }
        }
    }
}

// ==============================================================================
// s33 · ROSA · SOLICITUD ENVIADA
// ==============================================================================
@Composable
internal fun RenderRosaSubmittedS33(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RosaBackgroundGradient),
    ) {
        // Botón cerrar en la esquina
        Box(
            modifier = Modifier
                .padding(top = 54.dp, start = 16.dp)
                .size(44.dp)
                .shadow(6.dp, CircleShape, spotColor = Color(0x335A3CA0))
                .clip(CircleShape)
                .background(Color.White)
                .clickable { onSelectScreenCode("s14") },
            contentAlignment = Alignment.Center,
        ) {
            RfIcon(kind = RfIconKind.CLOSE, tint = Color(0xFF1A1A2E), size = 22.dp)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(110.dp))

            // Ilustración éxito verde con confeti
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFDDF5EC)),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2FB58F)),
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.CHECK, tint = Color.White, size = 48.dp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            BasicText(
                text = "¡Solicitud enviada!",
                style = TextStyle(
                    fontFamily = typo.displayFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 28.sp,
                    color = Color(0xFF1A1A2E),
                ),
            )
            BasicText(
                text = "Gracias por confiar en nosotros. Te avisamos por correo y WhatsApp en menos de 2 horas.",
                style = TextStyle(
                    fontFamily = typo.bodyFamily,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = Color(0xFF62627A),
                ),
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Tarjeta Qué Sigue
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(10.dp, RoundedCornerShape(24.dp), spotColor = Color(0x22AA46A0))
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .padding(20.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    BasicText(
                        text = "QUÉ SIGUE",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp,
                            color = Color(0xFF62627A),
                        ),
                    )

                    val steps = listOf(
                        Triple("1", "Revisamos tu evento", "El equipo de Rosa Fiesta ya lo tiene"),
                        Triple("2", "Recibes tu cotización", "Por correo y WhatsApp"),
                        Triple("3", "Apruebas y reservas", "Desde la app, en un toque"),
                    )

                    steps.forEach { (num, title, desc) ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(if (num == "1") Color(0xFF865DEC) else Color(0xFFF1EFF5)),
                                contentAlignment = Alignment.Center,
                            ) {
                                BasicText(
                                    text = num,
                                    style = TextStyle(
                                        fontFamily = typo.bodyFamily,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp,
                                        color = if (num == "1") Color.White else Color(0xFF62627A),
                                    ),
                                )
                            }
                            Column {
                                BasicText(text = title, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E), fontSize = 14.sp))
                                BasicText(text = desc, style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF62627A), fontSize = 12.sp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Botón Ver mi evento
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = Color(0x66C846B4))
                    .clip(RoundedCornerShape(16.dp))
                    .background(RosaButtonGradient)
                    .clickable { onSelectScreenCode("s25") },
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    text = "Ver mi evento",
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = Color.White,
                    ),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ==============================================================================
// s34 · ROSA · MODO CHAT
// ==============================================================================
@Composable
internal fun RenderRosaChatS34(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RosaBackgroundGradient),
    ) {
        // Base screen: s26 (event type selection)
        RenderRosaEventTypeS26(onSelectScreenCode = onSelectScreenCode)

        // Semi-transparent scrim / overlay is NOT present in design HTML,
        // but bottom sheet sits directly at the bottom with height 300dp
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .height(300.dp)
                .shadow(24.dp, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp), spotColor = Color(0x73AA46A0))
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Color.White)
                .padding(top = 12.dp, start = 20.dp, end = 20.dp, bottom = 34.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // Grab handle
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(40.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFFE3DCE8)),
                )

                // Chips row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf("Es para 120 personas", "En un jardín", "Rosa y dorado").forEach { chipText ->
                        Box(
                            modifier = Modifier
                                .height(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF3F0FA))
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(
                                text = chipText,
                                style = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF3B3550),
                                ),
                            )
                        }
                    }
                }

                // Text message
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    BasicText(
                        text = "Queremos algo íntimo en un jardín, con flores blancas y rosadas",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 18.sp,
                            lineHeight = 26.sp,
                            color = Color(0xFF1A1A2E),
                        ),
                    )
                }

                // Bottom actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Image attachment icon
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.SHARE, tint = Color(0xFF62627A), size = 22.dp)
                    }

                    // Mic icon
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onSelectScreenCode("s35") },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.MIC, tint = Color(0xFF62627A), size = 22.dp)
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Descartar text
                    Box(
                        modifier = Modifier
                            .height(44.dp)
                            .clickable { onSelectScreenCode("s26") }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = "Descartar",
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = Color(0xFF62627A),
                            ),
                        )
                    }

                    // Send circular button
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .shadow(12.dp, CircleShape, spotColor = Color(0xB3C846B4))
                            .clip(CircleShape)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFEA4EA9), Color(0xFFB754CB), Color(0xFF865DEC))
                                )
                            )
                            .clickable { onSelectScreenCode("s29") },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.SEND, tint = Color.White, size = 22.dp)
                    }
                }
            }
        }
    }
}

// ==============================================================================
// s35 · ROSA · MODO VOZ
// ==============================================================================
@Composable
internal fun RenderRosaVoiceS35(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RosaBackgroundGradient),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 54.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(6.dp, CircleShape, spotColor = Color(0x335A3CA0))
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("s34") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.CLOSE, tint = Color(0xFF1A1A2E), size = 22.dp)
                }

                BasicText(
                    text = "Modo voz con Rosa",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF5A5672), fontSize = 15.sp),
                )

                Spacer(modifier = Modifier.size(44.dp))
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Avatar gigante 124dp con halos concéntricos
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(124.dp)
                            .shadow(16.dp, CircleShape, spotColor = Color(0x667650E8))
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9)))),
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.SPARK, tint = Color.White, size = 64.dp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            BasicText(
                text = "Escuchando...",
                style = TextStyle(
                    fontFamily = typo.displayFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    color = Color(0xFF1A1A2E),
                ),
            )
            BasicText(
                text = "«Necesito 80 sillas tiffany blancas y 8 mesas redondas...»",
                style = TextStyle(
                    fontFamily = typo.bodyFamily,
                    fontSize = 15.sp,
                    color = Color(0xFF62627A),
                ),
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Barras de visualización de audio
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(40.dp),
            ) {
                val heights = listOf(14.dp, 28.dp, 36.dp, 22.dp, 40.dp, 30.dp, 18.dp)
                heights.forEach { h ->
                    Box(
                        modifier = Modifier
                            .width(6.dp)
                            .height(h)
                            .clip(RoundedCornerShape(3.dp))
                            .background(RosaButtonGradient),
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Botón terminar voz
            Box(
                modifier = Modifier
                    .padding(bottom = 40.dp)
                    .size(64.dp)
                    .shadow(12.dp, CircleShape, spotColor = Color(0x66C846B4))
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable { onSelectScreenCode("s34") },
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.MIC, tint = Color(0xFFEA4EA9), size = 28.dp)
            }
        }
    }
}

// ==============================================================================
// s36 · BOCETO DE DISTRIBUCIÓN
// ==============================================================================
@Composable
internal fun RenderRosaSketchS36(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFC)),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 54.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(6.dp, CircleShape, spotColor = Color(0x33B450B4))
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("s32") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1A1A2E), size = 22.dp)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    BasicText(
                        text = "Boceto de distribución",
                        style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1A2E), fontSize = 16.sp),
                    )
                    BasicText(
                        text = "Plano del evento sugerido por Rosa",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF62627A)),
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(6.dp, CircleShape, spotColor = Color(0x33B450B4))
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("s78") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.CHECK, tint = Color(0xFF865DEC), size = 20.dp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Canvas del plano visual
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFF2EDF5), RoundedCornerShape(24.dp)),
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Dibujar cuadrícula
                    val step = 22.dp.toPx()
                    for (x in 0..size.width.toInt() step step.toInt()) {
                        drawLine(Color(0xFFF3ECF4), Offset(x.toFloat(), 0f), Offset(x.toFloat(), size.height), 1f)
                    }
                    for (y in 0..size.height.toInt() step step.toInt()) {
                        drawLine(Color(0xFFF3ECF4), Offset(0f, y.toFloat()), Offset(size.width, y.toFloat()), 1f)
                    }

                    // Tarima principal
                    drawRoundRect(
                        color = Color(0xFF865DEC),
                        topLeft = Offset(40f, 40f),
                        size = androidx.compose.ui.geometry.Size(size.width - 80f, 70f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f),
                        style = Stroke(width = 3.dp.toPx()),
                    )

                    // 6 Mesas redondas
                    val tableOffsets = listOf(
                        Offset(size.width * 0.25f, size.height * 0.35f),
                        Offset(size.width * 0.75f, size.height * 0.35f),
                        Offset(size.width * 0.25f, size.height * 0.55f),
                        Offset(size.width * 0.75f, size.height * 0.55f),
                        Offset(size.width * 0.25f, size.height * 0.75f),
                        Offset(size.width * 0.75f, size.height * 0.75f),
                    )

                    tableOffsets.forEach { center ->
                        drawCircle(
                            color = Color(0xFFEA4EA9),
                            radius = 32.dp.toPx(),
                            center = center,
                            style = Stroke(width = 3.dp.toPx()),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Barra inferior con botón revisar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .height(54.dp)
                    .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = Color(0x66C846B4))
                    .clip(RoundedCornerShape(16.dp))
                    .background(RosaButtonGradient)
                    .clickable { onSelectScreenCode("s78") },
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    text = "Revisar y confirmar distribución",
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = Color.White,
                    ),
                )
            }
        }
    }
}

// ==============================================================================
// s78 · BOCETO · CONFIRMAR
// ==============================================================================
@Composable
internal fun RenderRosaSketchConfirmS78(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFC)),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(Brush.radialGradient(colors = listOf(Color(0xFFF9E3F1), Color(0x00F9E3F1)), center = Offset(0f, 0f), radius = size.width * 1.1f))
            drawRect(Brush.radialGradient(colors = listOf(Color(0xFFEEE7FB), Color(0x00EEE7FB)), center = Offset(size.width, size.height * 0.15f), radius = size.width * 0.8f))
        }

        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 54.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Color(0xFFF0EBF4), CircleShape)
                    .clickable { onSelectScreenCode("s36") },
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1A1A2E), size = 22.dp)
            }

            Column(modifier = Modifier.weight(1f)) {
                BasicText(
                    text = "Revisa tu boceto",
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 19.sp, color = Color(0xFF1A1A2E)),
                )
                BasicText(
                    text = "Boda Real Rosa & Dorado",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF62627A)),
                )
            }
        }

        // Main content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 114.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Sketch preview card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(236.dp)
                    .shadow(14.dp, RoundedCornerShape(24.dp), spotColor = Color(0x73AA46A0), ambientColor = Color.Transparent)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFF2EDF5), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center,
            ) {
                // Sketch Canvas
                Canvas(
                    modifier = Modifier
                        .size(width = 184.dp, height = 226.dp),
                ) {
                    val scaleX = size.width / 358f
                    val scaleY = size.height / 440f

                    // Grid pattern dots/lines
                    val gridStep = 22f * scaleX
                    var gx = 0f
                    while (gx <= size.width) {
                        drawLine(Color(0xFFF3ECF4), Offset(gx, 0f), Offset(gx, size.height), 1f)
                        gx += gridStep
                    }
                    var gy = 0f
                    while (gy <= size.height) {
                        drawLine(Color(0xFFF3ECF4), Offset(0f, gy), Offset(size.width, gy), 1f)
                        gy += 22f * scaleY
                    }

                    // Tarima rect at top
                    drawRoundRect(
                        color = Color(0xFF865DEC),
                        topLeft = Offset(70f * scaleX, 36f * scaleY),
                        size = androidx.compose.ui.geometry.Size(218f * scaleX, 50f * scaleY),
                        cornerRadius = CornerRadius(6f * scaleX, 6f * scaleY),
                        style = Stroke(width = 3f * scaleX),
                    )

                    // 6 tables (circles)
                    val tableRadius = 24f * scaleX
                    val tables = listOf(
                        Offset(134f * scaleX, 190f * scaleY),
                        Offset(224f * scaleX, 190f * scaleY),
                        Offset(70f * scaleX, 268f * scaleY),
                        Offset(288f * scaleX, 268f * scaleY),
                        Offset(70f * scaleX, 360f * scaleY),
                        Offset(288f * scaleX, 360f * scaleY),
                    )
                    tables.forEach { center ->
                        drawCircle(
                            color = Color(0xFFEA4EA9),
                            radius = tableRadius,
                            center = center,
                            style = Stroke(width = 3f * scaleX),
                        )
                    }

                    // Pista de baile (dashed rect)
                    drawRoundRect(
                        color = Color(0xFF8DB52C),
                        topLeft = Offset(128f * scaleX, 250f * scaleY),
                        size = androidx.compose.ui.geometry.Size(102f * scaleX, 90f * scaleY),
                        cornerRadius = CornerRadius(4f * scaleX, 4f * scaleY),
                        style = Stroke(
                            width = 2.6f * scaleX,
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(7f * scaleX, 7f * scaleX)),
                        ),
                    )

                    // Entrada arrow
                    drawLine(
                        color = Color(0xFF1A1A2E),
                        start = Offset(179f * scaleX, 436f * scaleY),
                        end = Offset(179f * scaleX, 404f * scaleY),
                        strokeWidth = 2.6f * scaleX,
                        cap = StrokeCap.Round,
                    )
                    drawLine(
                        color = Color(0xFF1A1A2E),
                        start = Offset(150f * scaleX, 428f * scaleY),
                        end = Offset(179f * scaleX, 404f * scaleY),
                        strokeWidth = 2.6f * scaleX,
                        cap = StrokeCap.Round,
                    )
                    drawLine(
                        color = Color(0xFF1A1A2E),
                        start = Offset(208f * scaleX, 428f * scaleY),
                        end = Offset(179f * scaleX, 404f * scaleY),
                        strokeWidth = 2.6f * scaleX,
                        cap = StrokeCap.Round,
                    )
                }

                // Edit button top-right
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .height(34.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFFF5EAF9))
                        .clickable { onSelectScreenCode("s36") }
                        .padding(horizontal = 12.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        RfIcon(kind = RfIconKind.EDIT, tint = Color(0xFFB54BD6), size = 15.dp)
                        BasicText("Editar", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFB54BD6)))
                    }
                }
            }

            // Lo que dibujaste chips
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                BasicText(
                    text = "LO QUE DIBUJASTE",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF62627A), letterSpacing = 1.sp),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    val chips = listOf(
                        Color(0xFF865DEC) to "Tarima",
                        Color(0xFFEA4EA9) to "6 mesas redondas",
                        Color(0xFF8DB52C) to "Pista de baile",
                        Color(0xFF1A1A2E) to "Entrada",
                    )
                    chips.forEach { (dotColor, label) ->
                        Box(
                            modifier = Modifier
                                .height(34.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(Color.White)
                                .border(1.dp, Color(0xFFEEE9F2), RoundedCornerShape(999.dp))
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(dotColor))
                                BasicText(label, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF1A1A2E)))
                            }
                        }
                    }
                }
            }

            // Rosa IA Assistant Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(18.dp, RoundedCornerShape(22.dp), spotColor = Color(0xCC9650DC), ambientColor = Color.Transparent)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9))))
                    .padding(16.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0x38FFFFFF)),
                        ) {
                            RosaAvatar(size = RosaAvatarSize.MEDIUM)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            BasicText(
                                text = "¿Te ayudo a completarlo?",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White),
                            )
                            Spacer(Modifier.height(2.dp))
                            BasicText(
                                text = "Ordeno tu boceto, calculo cuántas sillas caben y te propongo ejemplos en 3D.",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFFF6EAFB), lineHeight = 20.sp),
                            )
                        }
                    }

                    // Button Ayúdame con IA
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color.White)
                            .clickable { onSelectScreenCode("s32") },
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            RfIcon(kind = RfIconKind.SPARKLE, tint = Color(0xFFA23FB8), size = 18.dp)
                            BasicText(
                                "Ayúdame con IA",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFFA23FB8)),
                            )
                        }
                    }
                }
            }
        }

        // Bottom CTAs
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 34.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = Color(0xB2C846B4), ambientColor = Color.Transparent)
                    .clip(RoundedCornerShape(16.dp))
                    .background(RosaButtonGradient)
                    .clickable { onSelectScreenCode("s25") },
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RfIcon(kind = RfIconKind.CHECK, tint = Color.White, size = 20.dp)
                    BasicText(
                        "Guardar en mi evento",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White),
                    )
                }
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clickable { onSelectScreenCode("s36") },
            ) {
                BasicText(
                    "Seguir editando",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color(0xFF62627A)),
                )
            }
        }
    }
}

// ==============================================================================
// A08 · ADMIN · ROSA IA
// ==============================================================================
@Composable
internal fun RenderAdminRosaA08(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(modifier = Modifier.fillMaxSize().background(AdminRosaBackgroundGradient)) {
        // Header
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .padding(top = 54.dp, start = 16.dp, end = 16.dp)
                .height(44.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), CircleShape)
                    .clickable { onSelectScreenCode("A01") },
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
            }

            RosaAvatar(size = RosaAvatarSize.MEDIUM, customSize = 40.dp, forceAdminPalette = true)

            Column(modifier = Modifier.weight(1f)) {
                BasicText(
                    text = "Rosa",
                    style = TextStyle(fontFamily = typo.displayFamily, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729), lineHeight = 20.sp),
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    BasicText("Asistente", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
                    Box(
                        modifier = Modifier
                            .height(20.dp)
                            .background(Color(0xFFECEBFF), CircleShape)
                            .padding(horizontal = 7.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText("Admin", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF4B47D6)))
                    }
                }
            }
        }

        // Message stack (scrollable)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 118.dp, bottom = 150.dp, start = 16.dp, end = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // User bubble
            Box(
                modifier = Modifier
                    .align(Alignment.End)
                    .widthIn(max = 290.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 6.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                BasicText(
                    text = "Arma una boda para 120 personas el sábado 14 de noviembre en un jardín, blanco y dorado.",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color.White, lineHeight = 21.sp),
                )
            }

            // Rosa proposal
            Column(
                modifier = Modifier.widthIn(max = 330.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Bubble with bold date
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 6.dp, bottomEnd = 20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 6.dp, bottomEnd = 20.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                ) {
                    val bubbleText = buildAnnotatedString {
                        append("Listo. Revisé el inventario para el ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("14 de noviembre")
                        }
                        append(" y te armé este borrador:")
                    }
                    BasicText(
                        text = bubbleText,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF151729), lineHeight = 21.sp),
                    )
                }

                // Card proposal
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE9E9F2), RoundedCornerShape(20.dp)),
                ) {
                    Column {
                        // Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF7F6FF))
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                RfIcon(kind = RfIconKind.RING, tint = Color(0xFF2E324A), size = 16.dp)
                                BasicText("Boda · Sáb 14 nov", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E324A)))
                            }
                            Box(
                                modifier = Modifier
                                    .height(24.dp)
                                    .background(Color(0xFFECEBFF), CircleShape)
                                    .padding(horizontal = 9.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                BasicText("120 invitados", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF4B47D6)))
                            }
                        }

                        // Items with typography weights matching official HTML
                        val proposalItems = listOf(
                            Triple(RfPhotoKey.SILLA_TIFFANY, "Silla Tiffany Cristal" to "120", "Disponible" to (Color(0xFFEEF6D6) to Color(0xFF4E6A10))),
                            Triple(RfPhotoKey.MESA_REDONDA, "Mesa redonda" to "12", "Disponible" to (Color(0xFFEEF6D6) to Color(0xFF4E6A10))),
                            Triple(RfPhotoKey.CENTRO_FLORAL, "Centro de mesa floral" to "12", "Disponible" to (Color(0xFFEEF6D6) to Color(0xFF4E6A10))),
                            Triple(RfPhotoKey.ARCO_ROSA, "Arco de globos rosa" to "1", "Queda 1" to (Color(0xFFFFF2C9) to Color(0xFF7A5700))),
                        )

                        proposalItems.forEach { (photo, itemData, tagInfo) ->
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp)),
                                ) {
                                    RfPhoto(photoKey = photo, contentDescription = itemData.first, modifier = Modifier.fillMaxSize())
                                }
                                val itemTitle = buildAnnotatedString {
                                    append(itemData.first)
                                    append(" ")
                                    withStyle(SpanStyle(fontWeight = FontWeight.Medium, color = Color(0xFF5F6377))) {
                                        append("× ${itemData.second}")
                                    }
                                }
                                BasicText(
                                    text = itemTitle,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E324A)),
                                )
                                Box(
                                    modifier = Modifier
                                        .height(22.dp)
                                        .background(tagInfo.second.first, CircleShape)
                                        .padding(horizontal = 8.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    BasicText(tagInfo.first, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = tagInfo.second.second))
                                }
                            }
                        }

                        // Footer
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 14.dp, end = 14.dp, top = 10.dp, bottom = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                BasicText("Estimado", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)))
                                BasicText("RD$ 1,295", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)))
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
                                        .clickable { onSelectScreenCode("A07") },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        RfIcon(kind = RfIconKind.CHECK, tint = Color.White, size = 16.dp)
                                        BasicText("Usar borrador", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White))
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White)
                                        .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(12.dp))
                                        .clickable { onSelectScreenCode("A06") },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        RfIcon(kind = RfIconKind.EDIT, tint = Color(0xFF151729), size = 16.dp)
                                        BasicText("Ajustar", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)))
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Chips at bottom: 104dp
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 104.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .height(36.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE9E9F2), CircleShape)
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    RfIcon(kind = RfIconKind.USER, tint = Color(0xFF151729), size = 16.dp)
                    BasicText("Asignar a un cliente", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF151729)))
                }
            }

            Box(
                modifier = Modifier
                    .height(36.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE9E9F2), CircleShape)
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    RfIcon(kind = RfIconKind.LIGHTBULB, tint = Color(0xFF151729), size = 16.dp)
                    BasicText("Agregar iluminación", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF151729)))
                }
            }

            Box(
                modifier = Modifier
                    .height(36.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE9E9F2), CircleShape)
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                BasicText("Cambiar fecha", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF151729)))
            }
        }

        // Bottom Input at bottom: 34dp
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 34.dp)
                .height(58.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(1.dp, Color(0xFFE3E4EF), CircleShape)
                .padding(start = 20.dp, end = 6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                BasicText(
                    text = "Pídele algo más a Rosa…",
                    modifier = Modifier.weight(1f),
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF5F6377)),
                )

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0)))),
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.MIC, tint = Color.White, size = 22.dp)
                }
            }
        }
    }
}
