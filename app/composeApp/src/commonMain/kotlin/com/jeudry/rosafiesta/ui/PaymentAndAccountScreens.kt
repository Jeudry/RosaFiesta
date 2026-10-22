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
    var selectedMethod by remember { mutableStateOf(0) } // 0: Tarjeta, 1: Banco Popular, 2: Efectivo

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
                        .clickable { onSelectScreenCode("s48") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1A1A2E), size = 22.dp)
                }

                BasicText(
                    text = "Método de pago",
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF1A1A2E)),
                )

                Spacer(modifier = Modifier.size(44.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Monto a pagar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(10.dp, RoundedCornerShape(22.dp), spotColor = Color(0x33B754CB))
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White)
                    .padding(20.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    BasicText("MONTO A PAGAR (ANTICIPO 50%)", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF62627A), fontSize = 11.sp))
                    BasicText("RD$ 9,778.50", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, color = Color(0xFF865DEC), fontSize = 28.sp))
                    BasicText("Boda Real Rosa & Dorado · Saldo restante el día del evento", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF62627A), fontSize = 12.sp))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            BasicText("Elige tu forma de pago:", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E), fontSize = 15.sp))

            Spacer(modifier = Modifier.height(12.dp))

            val methods = listOf(
                "Tarjeta de crédito o débito" to "Visa · Mastercard (inmediato)",
                "Transferencia bancaria" to "Banco Popular / Banreservas / BHD",
                "Efectivo contra entrega" to "Paga al momento de la entrega en sitio",
            )

            methods.forEachIndexed { idx, (title, desc) ->
                val isSel = selectedMethod == idx
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White)
                        .border(
                            width = if (isSel) 2.dp else 1.dp,
                            color = if (isSel) Color(0xFF865DEC) else Color(0xFFEBE6F2),
                            shape = RoundedCornerShape(18.dp),
                        )
                        .clickable { selectedMethod = idx }
                        .padding(16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            BasicText(title, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E), fontSize = 15.sp))
                            BasicText(desc, style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF62627A), fontSize = 12.sp))
                        }
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(if (isSel) Color(0xFF865DEC) else Color(0xFFF1EFF5)),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (isSel) RfIcon(kind = RfIconKind.CHECK, tint = Color.White, size = 14.dp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = Color(0x66C846B4))
                    .clip(RoundedCornerShape(16.dp))
                    .background(RosaButtonGradient)
                    .clickable { onSelectScreenCode("s51") },
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    text = "Confirmar pago · RD$ 9,778.50",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(100.dp))

            // Ícono turquesa de éxito
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .shadow(16.dp, CircleShape, spotColor = Color(0x885AB7E0))
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFF5CC9C0), Color(0xFF5AB7E0)))),
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.CHECK, tint = Color.White, size = 48.dp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            BasicText(
                text = "¡Pago recibido con éxito!",
                style = TextStyle(
                    fontFamily = typo.displayFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 26.sp,
                    color = Color(0xFF1A1A2E),
                ),
            )
            BasicText(
                text = "Tu reserva para la Boda Real Rosa & Dorado ha quedado asegurada.",
                style = TextStyle(
                    fontFamily = typo.bodyFamily,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = Color(0xFF62627A),
                ),
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Tarjeta de detalles del pago
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(10.dp, RoundedCornerShape(24.dp), spotColor = Color(0x22865DEC))
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .padding(20.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        BasicText("Monto abonado", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF62627A), fontSize = 14.sp))
                        BasicText("RD$ 9,778.50", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.Bold, color = Color(0xFF865DEC), fontSize = 16.sp))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        BasicText("Referencia", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF62627A), fontSize = 14.sp))
                        BasicText("PAG-2026-0891", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E), fontSize = 14.sp))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        BasicText("Fecha y hora", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF62627A), fontSize = 14.sp))
                        BasicText("22 Oct 2026 · 10:45 AM", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF1A1A2E), fontSize = 14.sp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Botón Ver Recibo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = Color(0x66C846B4))
                    .clip(RoundedCornerShape(16.dp))
                    .background(RosaButtonGradient)
                    .clickable { onSelectScreenCode("s83") },
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    text = "Ver recibo oficial",
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = Color.White,
                    ),
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botón Volver a Mis Eventos
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF1EFF5))
                    .clickable { onSelectScreenCode("s37") },
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    text = "Volver a mis eventos",
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF1A1A2E),
                    ),
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// ==============================================================================
// s52 · POST-EVENTO / RESEÑA
// ==============================================================================
@Composable
internal fun RenderPostEventReviewScreenS52(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    var rating by remember { mutableStateOf(5) }

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
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            BasicText(
                text = "¿Cómo estuvo tu evento?",
                style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = Color(0xFF1A1A2E)),
            )
            BasicText(
                text = "Tu opinión nos ayuda a seguir creando fiestas inolvidables",
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF62627A)),
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 5 estrellas interactivas
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                for (star in 1..5) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (star <= rating) Color(0xFFFFD45C) else Color(0xFFF1EFF5))
                            .clickable { rating = star },
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText("★", style = TextStyle(fontSize = 24.sp, color = if (star <= rating) Color(0xFF2E1A47) else Color(0xFF8E8B9E)))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Campo de comentario
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(16.dp))
                    .padding(14.dp),
            ) {
                BasicText("¡Todo estuvo hermoso! La puntualidad y la atención de Rosa IA fueron impecables.", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF1A1A2E), fontSize = 14.sp))
            }

            Spacer(modifier = Modifier.height(30.dp))

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
                    text = "Enviar reseña",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color.White),
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
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
