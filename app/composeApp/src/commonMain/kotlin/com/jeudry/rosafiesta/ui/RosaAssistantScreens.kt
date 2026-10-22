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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
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
    val bg = Brush.radialGradient(
        0.0f to Color(0xFFF9E3F1),
        0.6f to Color(0x00F9E3F1),
        center = Offset(0f, 0f),
        radius = 400f,
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFC)),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(bg)
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
                horizontalArrangement = Arrangement.SpaceBetween,
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

                Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                    BasicText(
                        text = "Mi evento",
                        style = TextStyle(
                            fontFamily = typo.displayFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            color = Color(0xFF1A1A2E),
                        ),
                    )
                    BasicText(
                        text = "Baby Shower Sofía en Rosas",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 13.sp,
                            color = Color(0xFF62627A),
                        ),
                    )
                }

                Box(
                    modifier = Modifier
                        .height(26.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF1EFF5))
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = "Borrador",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF62627A),
                        ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tarjetas de categorías agrupadas
            val items = listOf(
                Triple("Sillas", "2 artículos · 120 unidades", "RD$ 4,800"),
                Triple("Mesas", "1 artículo · 12 unidades", "RD$ 4,200"),
                Triple("Mantelería", "3 artículos · 24 piezas", "RD$ 3,600"),
                Triple("Decoración floral", "4 artículos · 8 piezas", "RD$ 5,850"),
            )

            items.forEach { (title, subtitle, price) ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .shadow(4.dp, RoundedCornerShape(20.dp), spotColor = Color(0x1AC850AA))
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFF2EDF5), RoundedCornerShape(20.dp))
                        .padding(14.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            BasicText(
                                text = title,
                                style = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
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
                        BasicText(
                            text = price,
                            style = TextStyle(
                                fontFamily = typo.displayFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = Color(0xFFEA4EA9),
                            ),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Desglose de totales
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFF7F5FA))
                    .padding(16.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        BasicText("Subtotal", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF62627A), fontSize = 14.sp))
                        BasicText("RD$ 18,450", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E), fontSize = 14.sp))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        BasicText("ITBIS estimado (18%)", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF62627A), fontSize = 14.sp))
                        BasicText("RD$ 1,107", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E), fontSize = 14.sp))
                    }
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE5DFEC)))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        BasicText("Total estimado", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF1A1A2E)))
                        BasicText("RD$ 19,557", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF865DEC)))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // CTA Continuar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = Color(0x66C846B4))
                    .clip(RoundedCornerShape(16.dp))
                    .background(RosaButtonGradient)
                    .clickable { onSelectScreenCode("s26") },
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    text = "Continuar con Rosa IA",
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = Color.White,
                    ),
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

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
        Column(modifier = Modifier.fillMaxSize()) {
            RosaWizardHeader(
                step = 1,
                onBack = { onSelectScreenCode("s18") },
                onCart = { onSelectScreenCode("s25") },
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Spacer(modifier = Modifier.height(24.dp))

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

                Spacer(modifier = Modifier.height(10.dp))

                // Título
                BasicText(
                    text = "¿Qué evento quieres celebrar?",
                    style = TextStyle(
                        fontFamily = typo.displayFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 26.sp,
                        color = Color(0xFF1A1A2E),
                    ),
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Cuadrícula 2 columnas de tipos de eventos
                val eventTypes = listOf(
                    Triple("Boda", "Elegante y romántica", Color(0xFFFDE6F2)),
                    Triple("Quinceañera", "Mágica y brillante", Color(0xFFEDE6FD)),
                    Triple("Cumple infantil", "Divertido y colorido", Color(0xFFFDF2E6)),
                    Triple("Baby shower", "Tierno y especial", Color(0xFFE6F8FD)),
                    Triple("Graduación", "Logros que se celebran", Color(0xFFEAF6EB)),
                    Triple("Corporativo", "Profesional e impecable", Color(0xFFF1EEF9)),
                )

                for (i in eventTypes.indices step 2) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        for (j in 0..1) {
                            val item = eventTypes.getOrNull(i + j)
                            if (item != null) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(116.dp)
                                        .shadow(8.dp, RoundedCornerShape(20.dp), spotColor = Color(0x336E46B4))
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(item.third)
                                        .border(1.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(20.dp))
                                        .clickable { onSelectScreenCode("s27") }
                                        .padding(14.dp),
                                    contentAlignment = Alignment.BottomStart,
                                ) {
                                    Column {
                                        BasicText(
                                            text = item.first,
                                            style = TextStyle(
                                                fontFamily = typo.displayFamily,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 16.sp,
                                                color = Color(0xFF1A1A2E),
                                            ),
                                        )
                                        BasicText(
                                            text = item.second,
                                            style = TextStyle(
                                                fontFamily = typo.bodyFamily,
                                                fontSize = 11.sp,
                                                color = Color(0xFF62627A),
                                            ),
                                        )
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // CTA Chatea con Rosa
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.85f))
                        .border(1.dp, Color(0xFFDDD3F3), RoundedCornerShape(16.dp))
                        .clickable { onSelectScreenCode("s34") },
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        RfIcon(kind = RfIconKind.SPARK, tint = Color(0xFF9B45C8), size = 20.dp)
                        BasicText(
                            text = "¿No estás seguro? Chatea con Rosa",
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF1A1A2E),
                            ),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
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
            .background(RosaBackgroundGradient),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            RosaWizardHeader(
                step = 2,
                onBack = { onSelectScreenCode("s27") },
                onCart = { onSelectScreenCode("s25") },
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    RosaAvatar(size = RosaAvatarSize.MEDIUM, forceAdminPalette = false)
                    BasicText(
                        text = "¿Cómo prefieres continuar?",
                        style = TextStyle(
                            fontFamily = typo.displayFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            color = Color(0xFF1A1A2E),
                        ),
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Opción 1: Paquete sugerido
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(10.dp, RoundedCornerShape(22.dp), spotColor = Color(0x33B754CB))
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.White)
                        .border(2.dp, Color(0xFFEA4EA9).copy(alpha = 0.5f), RoundedCornerShape(22.dp))
                        .clickable { onSelectScreenCode("s29") }
                        .padding(20.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFDE6F2)),
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.SPARK, tint = Color(0xFFEA4EA9), size = 22.dp)
                        }
                        BasicText(
                            text = "Recomiéndame un paquete listo",
                            style = TextStyle(
                                fontFamily = typo.displayFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp,
                                color = Color(0xFF1A1A2E),
                            ),
                        )
                        BasicText(
                            text = "Rosa IA selecciona la combinación ideal de mobiliario, mantelería y flores según la cantidad de invitados.",
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                color = Color(0xFF62627A),
                            ),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Opción 2: Elegir por categorías
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(6.dp, RoundedCornerShape(22.dp), spotColor = Color(0x22865DEC))
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.White)
                        .clickable { onSelectScreenCode("s30") }
                        .padding(20.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFEDE6FD)),
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.CATALOG, tint = Color(0xFF865DEC), size = 22.dp)
                        }
                        BasicText(
                            text = "Quiero elegir artículo por artículo",
                            style = TextStyle(
                                fontFamily = typo.displayFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp,
                                color = Color(0xFF1A1A2E),
                            ),
                        )
                        BasicText(
                            text = "Paso a paso: elige sillas, mesas, manteles y decoración a tu propio gusto.",
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                color = Color(0xFF62627A),
                            ),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
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
                onBack = { onSelectScreenCode("s28") },
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
                    text = "Paquetes recomendados para Boda",
                    style = TextStyle(
                        fontFamily = typo.displayFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        color = Color(0xFF1A1A2E),
                    ),
                )
                BasicText(
                    text = "Seleccionados por Rosa IA para lucir espectacular",
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontSize = 14.sp,
                        color = Color(0xFF62627A),
                    ),
                )

                Spacer(modifier = Modifier.height(18.dp))

                val packages = listOf(
                    Triple("Boda Rosa Diamante", "100 personas · Todo incluido", "RD$ 38,500"),
                    Triple("Boda Jardín Bohemia", "80 personas · Arco floral + Tiffany", "RD$ 29,800"),
                    Triple("Boda Íntima Romántica", "50 personas · Mesas redondas", "RD$ 18,900"),
                )

                packages.forEach { (title, desc, price) ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .shadow(8.dp, RoundedCornerShape(22.dp), spotColor = Color(0x33B754CB))
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color.White)
                            .clickable { onSelectScreenCode("s31") }
                            .padding(16.dp),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFFDE6F2)),
                                contentAlignment = Alignment.Center,
                            ) {
                                RfPhoto(photoKey = RfPhotoKey.BODA, contentDescription = title)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column {
                                    BasicText(
                                        text = title,
                                        style = TextStyle(
                                            fontFamily = typo.displayFamily,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 17.sp,
                                            color = Color(0xFF1A1A2E),
                                        ),
                                    )
                                    BasicText(
                                        text = desc,
                                        style = TextStyle(
                                            fontFamily = typo.bodyFamily,
                                            fontSize = 13.sp,
                                            color = Color(0xFF62627A),
                                        ),
                                    )
                                }
                                BasicText(
                                    text = price,
                                    style = TextStyle(
                                        fontFamily = typo.displayFamily,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        color = Color(0xFFEA4EA9),
                                    ),
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
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
    var selectedCategories by remember {
        mutableStateOf(setOf("Mobiliario", "Flores", "Mantelería"))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RosaBackgroundGradient),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            RosaWizardHeader(
                step = 4,
                onBack = { onSelectScreenCode("s28") },
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
                    text = "¿Qué más necesitas para tu evento?",
                    style = TextStyle(
                        fontFamily = typo.displayFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        color = Color(0xFF1A1A2E),
                    ),
                )
                BasicText(
                    text = "Selecciona todo lo que te gustaría incluir en la cotización",
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontSize = 14.sp,
                        color = Color(0xFF62627A),
                    ),
                )

                Spacer(modifier = Modifier.height(20.dp))

                val categories = listOf(
                    "Mobiliario" to "Sillas Tiffany, mesas redondas y rectangulares",
                    "Flores" to "Centros de mesa, arco nupcial, ramos",
                    "Mantelería" to "Manteles, caminos de mesa, servilletas de gala",
                    "Globos" to "Arcos orgánicos, bouquets personalizados",
                    "Iluminación" to "Luces de hadas, guirnaldas, reflectores cálidos",
                    "Dulces" to "Mesa de postres, bases de cristal, candy bar",
                )

                categories.forEach { (name, description) ->
                    val isChecked = selectedCategories.contains(name)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .shadow(if (isChecked) 6.dp else 2.dp, RoundedCornerShape(18.dp), spotColor = Color(0x22865DEC))
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color.White)
                            .border(
                                width = if (isChecked) 2.dp else 1.dp,
                                color = if (isChecked) Color(0xFF865DEC) else Color(0xFFEBE6F2),
                                shape = RoundedCornerShape(18.dp),
                            )
                            .clickable {
                                selectedCategories = if (isChecked) {
                                    selectedCategories - name
                                } else {
                                    selectedCategories + name
                                }
                            }
                            .padding(16.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                BasicText(
                                    text = name,
                                    style = TextStyle(
                                        fontFamily = typo.displayFamily,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        color = Color(0xFF1A1A2E),
                                    ),
                                )
                                BasicText(
                                    text = description,
                                    style = TextStyle(
                                        fontFamily = typo.bodyFamily,
                                        fontSize = 12.sp,
                                        color = Color(0xFF62627A),
                                    ),
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(if (isChecked) Color(0xFF865DEC) else Color(0xFFF1EFF5)),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (isChecked) {
                                    RfIcon(kind = RfIconKind.CHECK, tint = Color.White, size = 16.dp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = Color(0x66C846B4))
                        .clip(RoundedCornerShape(16.dp))
                        .background(RosaButtonGradient)
                        .clickable { onSelectScreenCode("s31") },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = "Continuar a detalles",
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
        Column(modifier = Modifier.fillMaxSize()) {
            RosaWizardHeader(
                step = 6,
                onBack = { onSelectScreenCode("s31") },
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
                    text = "Tu propuesta está lista 🎉",
                    style = TextStyle(
                        fontFamily = typo.displayFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        color = Color(0xFF1A1A2E),
                    ),
                )
                BasicText(
                    text = "Revisa los artículos sugeridos por Rosa IA para tu Boda",
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontSize = 14.sp,
                        color = Color(0xFF62627A),
                    ),
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Tarjeta de resumen
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(12.dp, RoundedCornerShape(22.dp), spotColor = Color(0x33B754CB))
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.White)
                        .padding(18.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            BasicText("100 Sillas Tiffany Doradas", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E), fontSize = 14.sp))
                            BasicText("RD$ 4,000", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF62627A), fontSize = 14.sp))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            BasicText("10 Mesas Redondas 10 pax", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E), fontSize = 14.sp))
                            BasicText("RD$ 3,500", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF62627A), fontSize = 14.sp))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            BasicText("10 Centros de Mesa Florales", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E), fontSize = 14.sp))
                            BasicText("RD$ 7,500", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF62627A), fontSize = 14.sp))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            BasicText("1 Arco de Boda Rosa & Blanco", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E), fontSize = 14.sp))
                            BasicText("RD$ 4,500", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF62627A), fontSize = 14.sp))
                        }

                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFEBE6F2)))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            BasicText("Total estimado", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1A2E), fontSize = 15.sp))
                            BasicText("RD$ 19,500", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF865DEC)))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Enlace a boceto
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFEFE8FD))
                        .clickable { onSelectScreenCode("s36") }
                        .padding(14.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        RfIcon(kind = RfIconKind.SPARK, tint = Color(0xFF865DEC), size = 22.dp)
                        Column {
                            BasicText("Ver boceto de distribución", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E), fontSize = 14.sp))
                            BasicText("Mira cómo quedarán las mesas y la tarima", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF62627A), fontSize = 12.sp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // CTA Enviar solicitud
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = Color(0x66C846B4))
                        .clip(RoundedCornerShape(16.dp))
                        .background(RosaButtonGradient)
                        .clickable { onSelectScreenCode("s33") },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = "Solicitar cotización oficial",
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
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Chat
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
                        .clickable { onSelectScreenCode("s26") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1A1A2E), size = 22.dp)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    RosaAvatar(size = RosaAvatarSize.MEDIUM, forceAdminPalette = false)
                    Column {
                        BasicText(
                            text = "Rosa IA",
                            style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E), fontSize = 16.sp),
                        )
                        BasicText(
                            text = "En línea · Asistente de eventos",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF2FB58F)),
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(6.dp, CircleShape, spotColor = Color(0x335A3CA0))
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("s35") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.MIC, tint = Color(0xFF865DEC), size = 20.dp)
                }
            }

            // Conversación de chat
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Spacer(modifier = Modifier.height(14.dp))

                // Mensaje Rosa
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .shadow(6.dp, RoundedCornerShape(topStart = 6.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp), spotColor = Color(0x22865DEC))
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp))
                        .background(Color.White)
                        .padding(14.dp),
                ) {
                    BasicText(
                        text = "¡Hola! Dime qué tienes en mente para tu fiesta: temática, cantidad de personas o colores preferidos.",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, lineHeight = 20.sp, color = Color(0xFF1A1A2E)),
                    )
                }

                // Mensaje Usuario
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .align(Alignment.End)
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 6.dp))
                        .background(RosaButtonGradient)
                        .padding(14.dp),
                ) {
                    BasicText(
                        text = "Quiero una mesa de dulces elegante con tonos lila y dorado para 50 personas.",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, lineHeight = 20.sp, color = Color.White),
                    )
                }

                // Respuesta Rosa con foto sugerida
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .shadow(6.dp, RoundedCornerShape(topStart = 6.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp), spotColor = Color(0x22865DEC))
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp))
                        .background(Color.White)
                        .padding(14.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        BasicText(
                            text = "¡Excelente combinación! Te recomiendo nuestro set de mesa de helados/dulces con bases doradas:",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, lineHeight = 20.sp, color = Color(0xFF1A1A2E)),
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFDE6F2)),
                            contentAlignment = Alignment.Center,
                        ) {
                            RfPhoto(photoKey = RfPhotoKey.MESA_HELADOS, contentDescription = "Mesa de dulces")
                        }
                        BasicText(
                            text = "Set completo · RD$ 4,500",
                            style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.Bold, color = Color(0xFFEA4EA9), fontSize = 14.sp),
                        )
                    }
                }
            }

            // Input inferior
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                BasicText(
                    text = "Escribe tu pregunta...",
                    modifier = Modifier.weight(1f),
                    style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF8E8B9E), fontSize = 14.sp),
                )
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(RosaButtonGradient),
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.SEND, tint = Color.White, size = 18.dp)
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
        Column(modifier = Modifier.fillMaxSize()) {
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
                        .clickable { onSelectScreenCode("s36") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1A1A2E), size = 22.dp)
                }

                Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                    BasicText(
                        text = "Revisa tu boceto",
                        style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF1A1A2E)),
                    )
                    BasicText(
                        text = "Boda Real Rosa & Dorado",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF62627A)),
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Tarjeta miniatura del boceto
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFF2EDF5), RoundedCornerShape(24.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = "1 Tarima · 6 Mesas redondas · 60 Sillas Tiffany",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF865DEC), fontSize = 14.sp),
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                BasicText(
                    text = "Artículos calculados automáticamente:",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E), fontSize = 14.sp),
                )

                Spacer(modifier = Modifier.height(10.dp))

                val items = listOf(
                    "6 Mesas redondas 10 personas" to "RD$ 2,100",
                    "60 Sillas Tiffany Blancas" to "RD$ 2,400",
                    "6 Manteles de gala lila" to "RD$ 900",
                    "6 Centros de mesa florales" to "RD$ 4,500",
                )

                items.forEach { (name, price) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White)
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        BasicText(name, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A2E), fontSize = 14.sp))
                        BasicText(price, style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.Bold, color = Color(0xFFEA4EA9), fontSize = 14.sp))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

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
                        text = "Guardar en mi evento",
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
// A08 · ADMIN · ROSA IA
// ==============================================================================
@Composable
internal fun RenderAdminRosaA08(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AdminRosaBackgroundGradient),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Admin
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
                        .shadow(6.dp, CircleShape, spotColor = Color(0x334650B4))
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("A01") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    BasicText(
                        text = "Rosa IA · Administración",
                        style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729), fontSize = 16.sp),
                    )
                    BasicText(
                        text = "Copiloto operativo",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)),
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(6.dp, CircleShape, spotColor = Color(0x334650B4))
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.SPARK, tint = Color(0xFF7C5CF0), size = 20.dp)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                // Tarjeta Rosa Admin
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(10.dp, RoundedCornerShape(22.dp), spotColor = Color(0x334650B4))
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.White)
                        .padding(18.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        RosaAvatar(size = RosaAvatarSize.MEDIUM, forceAdminPalette = true)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            BasicText(
                                text = "Hola Jeudry, tienes 3 solicitudes pendientes de cotización.",
                                style = TextStyle(
                                    fontFamily = typo.displayFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF151729),
                                ),
                            )
                            BasicText(
                                text = "Puedo generar la propuesta de inventario automáticamente para la Boda Real del 24 Nov.",
                                style = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontSize = 13.sp,
                                    color = Color(0xFF5F6377),
                                ),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                BasicText(
                    text = "Acciones automáticas sugeridas:",
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF151729),
                    ),
                )

                Spacer(modifier = Modifier.height(10.dp))

                val adminActions = listOf(
                    "Generar cotización para Boda Real (RD$ 19,557)",
                    "Verificar stock de Sillas Tiffany para el 24 Nov",
                    "Optimizar ruta de entrega Santo Domingo Este",
                )

                adminActions.forEach { act ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color(0x224650B4))
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(16.dp))
                            .clickable { onSelectScreenCode("A03") }
                            .padding(14.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            BasicText(
                                text = act,
                                modifier = Modifier.weight(1f),
                                style = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF151729),
                                ),
                            )
                            RfIcon(kind = RfIconKind.MORE_VERTICAL, tint = Color(0xFF7C5CF0), size = 18.dp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Input para consultar a Rosa Admin
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    BasicText(
                        text = "Pregúntale a Rosa sobre el inventario...",
                        modifier = Modifier.weight(1f),
                        style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF8E92A8), fontSize = 14.sp),
                    )
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(AdminButtonGradient),
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.SEND, tint = Color.White, size = 18.dp)
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}
