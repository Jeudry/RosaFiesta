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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeudry.rosafiesta.designsystem.LocalRfTypography
import com.jeudry.rosafiesta.designsystem.RfIcon
import com.jeudry.rosafiesta.designsystem.RfIconKind

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
@Composable
internal fun RenderQuoteHistoryScreenS49(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFC)),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(PaymentBackgroundGradient)
        }

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
                        .clickable { onSelectScreenCode("s37") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1A1A2E), size = 22.dp)
                }

                BasicText(
                    text = "Cotizaciones",
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF1A1A2E)),
                )

                Spacer(modifier = Modifier.size(44.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            val quotes = listOf(
                Triple("Boda Real Rosa & Dorado", "Vie 23 oct · Jardín Botánico", "Revisada" to Color(0xFFFFF2C9)),
                Triple("Baby Shower Sofía", "Sáb 2 nov · Salón Los Almendros", "Pendiente" to Color(0xFFF1EFF5)),
                Triple("Cumpleaños Mateo", "Sáb 12 dic · Club Bella Vista", "Aprobada" to Color(0xFFDDF4F3)),
            )

            quotes.forEach { (name, date, status) ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .shadow(6.dp, RoundedCornerShape(22.dp), spotColor = Color(0x22865DEC))
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFF2EDF5), RoundedCornerShape(22.dp))
                        .clickable { onSelectScreenCode("s48") }
                        .padding(16.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                BasicText(name, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E), fontSize = 16.sp))
                                BasicText(date, style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF62627A), fontSize = 13.sp))
                            }
                            Box(
                                modifier = Modifier
                                    .height(26.dp)
                                    .clip(CircleShape)
                                    .background(status.second)
                                    .padding(horizontal = 10.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                BasicText(status.first, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E), fontSize = 12.sp))
                            }
                        }

                        // Progreso de 5 pasos
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            for (i in 1..5) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(if (i <= 2) Color(0xFFF7B500) else Color(0xFFF1EFF5)),
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
// s50 · PAGO / CHECKOUT
// ==============================================================================
@Composable
internal fun RenderPaymentCheckoutScreenS50(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    var selectedMethod by remember { mutableStateOf(0) } // 0: Tarjeta, 1: Popular, 2: Banreservas, 3: Efectivo

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
                        .clickable { onSelectScreenCode("s48") },
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
                        BasicText("Entrega para Camila Rodríguez", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF62627A)))
                        BasicText("Jardín Botánico Santo Domingo", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1A1A2E)))
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
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE8E1EE), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    RfIcon(kind = RfIconKind.USER, tint = Color(0xFF62627A), size = 18.dp)
                    BasicText("Olivia Rhye", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF1A1A2E)))
                }
            }

            // Card number
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE8E1EE), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    RfIcon(kind = RfIconKind.WALLET, tint = Color(0xFF62627A), size = 18.dp)
                    BasicText("Número de tarjeta", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF62627A)))
                }
            }

            // MM/AA and CVC
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE8E1EE), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    BasicText("MM/AA", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF62627A)))
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE8E1EE), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    BasicText("CVC", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF62627A)))
                }
            }

            // Contact phone
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE8E1EE), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    RfIcon(kind = RfIconKind.PHONE, tint = Color(0xFF62627A), size = 18.dp)
                    BasicText("(809) 555-1234", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF1A1A2E)))
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
                    .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = Color(0xB3C846B4))
                    .clip(RoundedCornerShape(16.dp))
                    .background(RosaButtonGradient)
                    .clickable { onSelectScreenCode("s51") }
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BasicText("Pagar anticipo (30%)", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White))
                    BasicText("RD$ 1,350", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 19.sp, color = Color.White))
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
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFC)),
    ) {
        // Decorative background garland & shapes
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(color = Color(0xFFB9D84A), radius = 5f * density, center = Offset(140f * density, 150f * density))
            drawCircle(color = Color(0xFFFBCA3F), radius = 3.5f * density, center = Offset(150f * density, 220f * density))
            drawCircle(color = Color(0x33F2C4DF), radius = 30f * density, center = Offset(52f * density, 200f * density), style = Stroke(width = 1.5f * density))
            drawCircle(color = Color(0x33CFE48A), radius = 24f * density, center = Offset(344f * density, 250f * density), style = Stroke(width = 2f * density))
        }

        // Header at top 130dp
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 130.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .shadow(14.dp, CircleShape, spotColor = Color(0xCC50BEC8))
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFF5CC9C0), Color(0xFF5AB7E0)))),
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.CHECK, tint = Color.White, size = 36.dp)
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

        // Ticket card at top 330dp
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 330.dp, start = 20.dp, end = 20.dp)
                .fillMaxWidth()
                .shadow(18.dp, RoundedCornerShape(24.dp), spotColor = Color(0x50C850AA))
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
                        RfIcon(kind = RfIconKind.SPARK, tint = Color.White, size = 26.dp)
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        BasicText("Boda Real Rosa & Dorado", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 19.sp, lineHeight = 22.sp, color = Color(0xFF1A1A2E)))
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
                                BasicText("Cotización lista", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF4E5A12)))
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
                            BasicText("Vie 23 oct 2026", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF7A5700)))
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().height(30.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        BasicText("Artículos", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF62627A)))
                        BasicText("145", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1A1A2E)))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().height(30.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        BasicText("Anticipo pagado", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF62627A)))
                        BasicText("RD$ 1,350", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Color(0xFF3FB59A)))
                    }
                }
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
                    .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = Color(0xB3C846B4))
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
                .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = Color(0xB3C846B4))
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
                        .clickable { onSelectScreenCode("s54") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1A1A2E), size = 22.dp)
                }

                BasicText(
                    text = "Mis reservas",
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF1A1A2E)),
                )

                Spacer(modifier = Modifier.size(44.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            val reservations = listOf(
                Pair("Reserva #RES-2026-104", "Boda Real Rosa & Dorado · Confirmada"),
                Pair("Reserva #RES-2026-092", "Baby Shower Sofía · Pendiente de pago"),
            )

            reservations.forEach { (code, desc) ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFF2EDF5), RoundedCornerShape(18.dp))
                        .clickable { onSelectScreenCode("s40") }
                        .padding(16.dp),
                ) {
                    Column {
                        BasicText(code, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF865DEC), fontSize = 15.sp))
                        BasicText(desc, style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF1A1A2E), fontSize = 13.sp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// ==============================================================================
// s54 · PERFIL DE USUARIO
// ==============================================================================
@Composable
internal fun RenderUserProfileScreenS54(
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

            BasicText(
                text = "Mi Perfil",
                style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp, color = Color(0xFF1A1A2E)),
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Tarjeta de perfil
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(22.dp), spotColor = Color(0x22865DEC))
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White)
                    .padding(18.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(RosaButtonGradient),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText("JM", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 22.sp))
                    }
                    Column {
                        BasicText("Jeudry Martínez", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E), fontSize = 18.sp))
                        BasicText("jeudry@rosafiesta.do · +1 (809) 555-0192", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF62627A), fontSize = 13.sp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            val profileLinks = listOf(
                Pair("Mis reservas activas", "s53"),
                Pair("Historial de cotizaciones", "s49"),
                Pair("Proveedores asociados", "s55"),
                Pair("Análisis de gastos", "s57"),
                Pair("Panel financiero", "s58"),
            )

            profileLinks.forEach { (title, screen) ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFF0EDF3), RoundedCornerShape(16.dp))
                        .clickable { onSelectScreenCode(screen) }
                        .padding(16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        BasicText(title, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A2E), fontSize = 15.sp))
                        RfIcon(kind = RfIconKind.MORE_VERTICAL, tint = Color(0xFF865DEC), size = 18.dp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(110.dp))
        }
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
                        .clickable { onSelectScreenCode("s54") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1A1A2E), size = 22.dp)
                }

                BasicText(
                    text = "Proveedores",
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF1A1A2E)),
                )

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(6.dp, CircleShape, spotColor = Color(0x33B450B4))
                        .clip(CircleShape)
                        .background(RosaButtonGradient)
                        .clickable { onSelectScreenCode("s56") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.PLUS, tint = Color.White, size = 20.dp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            val suppliers = listOf(
                "Floristería Las Rosas" to "Centros de mesa y arcos florales",
                "Repostería Mágica" to "Mesas de dulces y pasteles temáticos",
                "Transporte y Logística RD" to "Entregas puntuales en Santo Domingo",
            )

            suppliers.forEach { (name, service) ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFF0EDF3), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                ) {
                    Column {
                        BasicText(name, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E), fontSize = 15.sp))
                        BasicText(service, style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF62627A), fontSize = 13.sp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// ==============================================================================
// s56 · NUEVO PROVEEDOR
// ==============================================================================
@Composable
internal fun RenderNewSupplierScreenS56(
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
                        .clickable { onSelectScreenCode("s55") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1A1A2E), size = 22.dp)
                }

                BasicText(
                    text = "Registrar proveedor",
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF1A1A2E)),
                )

                Spacer(modifier = Modifier.size(44.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    BasicText("Nombre comercial", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E), fontSize = 14.sp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        BasicText("Globos & Diseños Creativos", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF1A1A2E), fontSize = 15.sp))
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    BasicText("Servicio ofrecido", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E), fontSize = 14.sp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        BasicText("Arcos orgánicos de globos", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF1A1A2E), fontSize = 15.sp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = Color(0x66C846B4))
                    .clip(RoundedCornerShape(16.dp))
                    .background(RosaButtonGradient)
                    .clickable { onSelectScreenCode("s55") },
                contentAlignment = Alignment.Center,
            ) {
                BasicText("Guardar proveedor", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color.White))
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// ==============================================================================
// s57 · ANÁLISIS DE GASTOS
// ==============================================================================
@Composable
internal fun RenderSpendingAnalyticsScreenS57(
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
                        .clickable { onSelectScreenCode("s54") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1A1A2E), size = 22.dp)
                }

                BasicText(
                    text = "Análisis de gastos",
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF1A1A2E)),
                )

                Spacer(modifier = Modifier.size(44.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Tarjeta de distribución de presupuesto
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(10.dp, RoundedCornerShape(22.dp), spotColor = Color(0x22865DEC))
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White)
                    .padding(18.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    BasicText("Distribución por categoría", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E), fontSize = 16.sp))

                    val categories = listOf(
                        Triple("Mobiliario (Sillas y Mesas)", "RD$ 11,200 · 57%", Color(0xFF865DEC)),
                        Triple("Decoración Floral y Arcos", "RD$ 7,250 · 37%", Color(0xFFEA4EA9)),
                        Triple("Otros y Logística", "RD$ 1,107 · 6%", Color(0xFFFFD45C)),
                    )

                    categories.forEach { (cat, valStr, color) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color))
                                BasicText(cat, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF1A1A2E)))
                            }
                            BasicText(valStr, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF62627A)))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
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
                        .clickable { onSelectScreenCode("s54") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1A1A2E), size = 22.dp)
                }

                BasicText(
                    text = "Panel Financiero",
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF1A1A2E)),
                )

                Spacer(modifier = Modifier.size(44.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // KPIs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFF0EDF3), RoundedCornerShape(18.dp))
                        .padding(14.dp),
                ) {
                    Column {
                        BasicText("Total Invertido", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF62627A), fontSize = 12.sp))
                        BasicText("RD$ 28,457", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.Bold, color = Color(0xFF865DEC), fontSize = 18.sp))
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFF0EDF3), RoundedCornerShape(18.dp))
                        .padding(14.dp),
                ) {
                    Column {
                        BasicText("Ahorro Rosa IA", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF62627A), fontSize = 12.sp))
                        BasicText("RD$ 4,800", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.Bold, color = Color(0xFF2FB58F), fontSize = 18.sp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
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
            Spacer(modifier = Modifier.height(54.dp))

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
                        .clickable { onSelectScreenCode("s51") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1A1A2E), size = 22.dp)
                }

                BasicText(
                    text = "Recibo oficial",
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF1A1A2E)),
                )

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(6.dp, CircleShape, spotColor = Color(0x33B450B4))
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.MORE_VERTICAL, tint = Color(0xFF865DEC), size = 20.dp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Recibo formal
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(22.dp), spotColor = Color(0x22865DEC))
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFF2EDF5), RoundedCornerShape(22.dp))
                    .padding(20.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column {
                        BasicText("ROSA FIESTA SRL", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1A2E), fontSize = 18.sp))
                        BasicText("RNC 1-32-45678-9 · Santo Domingo, RD", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF62627A), fontSize = 12.sp))
                        BasicText("NCF: B0200001234 (Consumidor Final)", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF865DEC), fontSize = 12.sp))
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0EDF3)))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        BasicText("Anticipo de reserva (50%)", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF1A1A2E), fontSize = 14.sp))
                        BasicText("RD$ 8,286.86", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E), fontSize = 14.sp))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        BasicText("ITBIS facturado (18%)", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF62627A), fontSize = 14.sp))
                        BasicText("RD$ 1,491.64", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF62627A), fontSize = 14.sp))
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0EDF3)))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        BasicText("TOTAL PAGADO", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1A2E), fontSize = 16.sp))
                        BasicText("RD$ 9,778.50", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2FB58F), fontSize = 24.sp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón Compartir / Descargar PDF
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = Color(0x66C846B4))
                    .clip(RoundedCornerShape(16.dp))
                    .background(RosaButtonGradient)
                    .clickable { onSelectScreenCode("s37") },
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    text = "Descargar recibo en PDF",
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = Color.White,
                    ),
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
