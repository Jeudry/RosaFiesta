package com.jeudry.rosafiesta.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeudry.rosafiesta.designsystem.LocalRfTypography
import com.jeudry.rosafiesta.designsystem.RfIcon
import com.jeudry.rosafiesta.designsystem.RfIconKind

private val PersonalButtonGradient = Brush.horizontalGradient(
    listOf(Color(0xFFFF3366), Color(0xFFE6007A)),
)

private val RosaButtonGradient = Brush.horizontalGradient(
    listOf(Color(0xFFEA4EA9), Color(0xFFB754CB), Color(0xFF865DEC)),
)

// ==============================================================================
// s79 · NOTIFICACIONES PERSONAL
// ==============================================================================
@Composable
internal fun RenderPersonalNotificationsS79(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF6F8)),
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
                Column {
                    BasicText("Notificaciones", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = Color(0xFF1E1E24)))
                    BasicText("3 avisos recientes", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF7A7177), fontSize = 13.sp))
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(6.dp, CircleShape, spotColor = Color(0x33FF3366))
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("s14") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1E1E24), size = 20.dp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            val notifs = listOf(
                Triple("¡Tu cotización está lista!", "Boda Real Rosa & Dorado · Revisa los detalles", "Hoy"),
                Triple("Recordatorio de pago", "Abono del 30% vence en 48 horas", "Ayer"),
                Triple("Rosa IA tiene sugerencias", "Nuevos centros florales para tu paleta", "Hace 2 días"),
            )

            notifs.forEach { (title, subtitle, time) ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFFFE3EC), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            BasicText(title, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24), fontSize = 14.sp))
                            BasicText(subtitle, style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF7A7177), fontSize = 12.sp))
                        }
                        BasicText(time, style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFFFF3366), fontSize = 11.sp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// ==============================================================================
// s80 · BÚSQUEDA
// ==============================================================================
@Composable
internal fun RenderPersonalSearchS80(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF6F8)),
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
                        .shadow(6.dp, CircleShape, spotColor = Color(0x33FF3366))
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("s18") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1E1E24), size = 22.dp)
                }

                BasicText("Buscar en Catálogo", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF1E1E24)))

                Spacer(modifier = Modifier.size(44.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFFFE3EC), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                BasicText("Resultados para: “mesa”", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF1E1E24), fontSize = 14.sp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            val results = listOf(
                Triple("Mesa Redonda 10 personas", "Para 8 a 10 personas · 1.5 m", "RD$ 25 c/u"),
                Triple("Centro de Mesa Floral", "Flores naturales con base de madera", "RD$ 35 c/u"),
                Triple("Mesa Plegable Rectangular", "Ideal para buffet y catering", "RD$ 45 c/u"),
            )

            results.forEach { (name, desc, pr) ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFFFE3EC), RoundedCornerShape(16.dp))
                        .clickable { onSelectScreenCode("s19") }
                        .padding(16.dp),
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            BasicText(name, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24), fontSize = 14.sp))
                            BasicText(desc, style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF7A7177), fontSize = 12.sp))
                        }
                        BasicText(pr, style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.Bold, color = Color(0xFFFF3366), fontSize = 15.sp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// ==============================================================================
// s82 · MIS PAGOS
// ==============================================================================
@Composable
internal fun RenderPersonalPaymentsS82(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF6F8)),
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
                        .shadow(6.dp, CircleShape, spotColor = Color(0x33FF3366))
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("s54") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1E1E24), size = 22.dp)
                }

                BasicText("Mis Pagos", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF1E1E24)))

                Spacer(modifier = Modifier.size(44.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFFFE3EC), RoundedCornerShape(16.dp))
                        .padding(14.dp),
                ) {
                    Column {
                        BasicText("Total Pagado", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF7A7177), fontSize = 12.sp))
                        BasicText("RD$ 3,210", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2FB58F), fontSize = 18.sp))
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFFFE3EC), RoundedCornerShape(16.dp))
                        .padding(14.dp),
                ) {
                    Column {
                        BasicText("Saldo Pendiente", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF7A7177), fontSize = 12.sp))
                        BasicText("RD$ 7,490", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFF3366), fontSize = 18.sp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            BasicText("Historial de Recibos", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24), fontSize = 16.sp))

            Spacer(modifier = Modifier.height(10.dp))

            val receipts = listOf(
                Pair("Anticipo 30% · Boda Real Rosa", "RD$ 3,210 · 18 oct · Tarjeta Visa"),
            )

            receipts.forEach { (title, subtitle) ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFFFE3EC), RoundedCornerShape(16.dp))
                        .clickable { onSelectScreenCode("s83") }
                        .padding(16.dp),
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            BasicText(title, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24), fontSize = 14.sp))
                            BasicText(subtitle, style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF7A7177), fontSize = 12.sp))
                        }
                        RfIcon(kind = RfIconKind.MORE_VERT, tint = Color(0xFFFF3366), size = 18.dp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// ==============================================================================
// s86 · AJUSTES DE CUENTA
// ==============================================================================
@Composable
internal fun RenderAccountSettingsS86(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF6F8)),
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
                        .shadow(6.dp, CircleShape, spotColor = Color(0x33FF3366))
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("s54") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1E1E24), size = 22.dp)
                }

                BasicText("Ajustes", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF1E1E24)))

                Spacer(modifier = Modifier.size(44.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            val options = listOf(
                Pair("Términos y Privacidad", "s87"),
                Pair("Modo Oscuro / Claro", "s15"),
                Pair("Eliminar Cuenta", "s88"),
            )

            options.forEach { (lbl, screen) ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFFFE3EC), RoundedCornerShape(16.dp))
                        .clickable { onSelectScreenCode(screen) }
                        .padding(16.dp),
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        BasicText(lbl, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24), fontSize = 15.sp))
                        RfIcon(kind = RfIconKind.MORE_VERT, tint = Color(0xFFFF3366), size = 18.dp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// ==============================================================================
// s87 · TÉRMINOS Y PRIVACIDAD
// ==============================================================================
@Composable
internal fun RenderTermsS87(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF6F8)),
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
                        .shadow(6.dp, CircleShape, spotColor = Color(0x33FF3366))
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("s86") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1E1E24), size = 22.dp)
                }

                BasicText("Términos y Privacidad", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF1E1E24)))

                Spacer(modifier = Modifier.size(44.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFFFE3EC), RoundedCornerShape(18.dp))
                    .padding(18.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    BasicText("Políticas de Reserva", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24), fontSize = 16.sp))
                    BasicText("Tu fecha queda apartada cuando Rosa Fiesta confirma la cotización y recibe el anticipo del 30%. Las cotizaciones tienen validez de 7 días.", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF7A7177), fontSize = 13.sp))
                    BasicText("El saldo restante se liquida a más tardar un día antes del montaje.", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF7A7177), fontSize = 13.sp))
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// ==============================================================================
// s88 · ELIMINAR CUENTA
// ==============================================================================
@Composable
internal fun RenderDeleteAccountS88(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF6F8)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFEE2E2)),
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.WARNING, tint = Color(0xFFDC2626), size = 32.dp)
            }

            BasicText("¿Eliminar tu cuenta?", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Color(0xFF1E1E24)))
            BasicText("Esta acción es permanente y eliminará tu historial de eventos y favoritos.", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF7A7177), fontSize = 14.sp))

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFDC2626))
                    .clickable { onSelectScreenCode("s07") },
                contentAlignment = Alignment.Center,
            ) {
                BasicText("Eliminar Cuenta", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFFFE3EC), RoundedCornerShape(16.dp))
                    .clickable { onSelectScreenCode("s86") },
                contentAlignment = Alignment.Center,
            ) {
                BasicText("Mantener mi cuenta", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24), fontSize = 15.sp))
            }
        }
    }
}

// ==============================================================================
// s89 · ESTADO VACÍO PERSONAL
// ==============================================================================
@Composable
internal fun RenderPersonalEmptyStateS89(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF6F8)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFE3EC)),
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.SPARK, tint = Color(0xFFFF3366), size = 32.dp)
            }

            BasicText("Aún no tienes eventos", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Color(0xFF1E1E24)))
            BasicText("Cuéntale a Rosa qué celebras y te armamos la lista en 2 minutos.", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF7A7177), fontSize = 14.sp))

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(PersonalButtonGradient)
                    .clickable { onSelectScreenCode("s26") },
                contentAlignment = Alignment.Center,
            ) {
                BasicText("Planificar con Rosa IA", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp))
            }
        }
    }
}

// ==============================================================================
// s90 · ESTADO CARGANDO PERSONAL
// ==============================================================================
@Composable
internal fun RenderPersonalLoadingStateS90(
    onSelectScreenCode: (String) -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF6F8)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Spacer(modifier = Modifier.height(56.dp))
            Box(modifier = Modifier.fillMaxWidth().height(40.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFFFE3EC)))
            repeat(4) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFFFE3EC), RoundedCornerShape(16.dp)),
                )
            }
        }
    }
}

// ==============================================================================
// s81 · MODAL FILTROS DE CATÁLOGO
// ==============================================================================
@Composable
internal fun RenderCatalogFiltersModalS81(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFC)),
    ) {
        // Background search results (s80)
        RenderPersonalSearchS80(onSelectScreenCode = onSelectScreenCode)

        // Scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x731E1432))
                .clickable { onSelectScreenCode("s80") },
        )

        // Bottom sheet dialog
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Color.White)
                .padding(top = 12.dp, start = 20.dp, end = 20.dp, bottom = 34.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
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

                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BasicText(
                        "Filtros",
                        style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF1A1A2E)),
                    )
                    BasicText(
                        "Limpiar",
                        modifier = Modifier.clickable { },
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFC2338F)),
                    )
                }

                // Categoría
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BasicText("Categoría", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF3B3550)))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        listOf("Todas", "Mobiliario", "Floral", "Iluminación", "Globos", "Mantelería").forEachIndexed { i, cat ->
                            val isSelected = i == 0
                            Box(
                                modifier = Modifier
                                    .height(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9)))
                                        else SolidColor(Color(0xFFF1EFF5))
                                    )
                                    .padding(horizontal = 14.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                BasicText(
                                    cat,
                                    style = TextStyle(
                                        fontFamily = typo.bodyFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = if (isSelected) Color.White else Color(0xFF1A1A2E),
                                    ),
                                )
                            }
                        }
                    }
                }

                // Precio por día
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        BasicText("Precio por día", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF3B3550)))
                        BasicText("RD$ 0 – RD$ 200", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1A1A2E)))
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        // Inactive track
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFFF1EFF5)),
                        )
                        // Active gradient track
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.58f)
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(RosaButtonGradient),
                        )
                        // Left thumb
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .shadow(4.dp, CircleShape, spotColor = Color(0x80C846B4))
                                .border(2.dp, Color(0xFFEA4EA9), CircleShape)
                                .clip(CircleShape)
                                .background(Color.White),
                        )
                        // Right thumb
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .offset(x = 180.dp)
                                .size(24.dp)
                                .shadow(4.dp, CircleShape, spotColor = Color(0x80865DEC))
                                .border(2.dp, Color(0xFF865DEC), CircleShape)
                                .clip(CircleShape)
                                .background(Color.White),
                        )
                    }
                }

                // Disponible para
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BasicText("Disponible para", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF3B3550)))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .shadow(2.dp, RoundedCornerShape(16.dp), spotColor = Color(0x20B450B4))
                                .border(1.dp, Color(0xFFF0EBF4), RoundedCornerShape(16.dp))
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White)
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                RfIcon(kind = RfIconKind.CALENDAR, tint = Color(0xFF1A1A2E), size = 18.dp)
                                BasicText("Vie 23 oct", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF1A1A2E)))
                            }
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .shadow(2.dp, RoundedCornerShape(16.dp), spotColor = Color(0x20B450B4))
                                .border(1.dp, Color(0xFFF0EBF4), RoundedCornerShape(16.dp))
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White)
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                RfIcon(kind = RfIconKind.PARTY, tint = Color(0xFF1A1A2E), size = 18.dp)
                                BasicText("Mi boda", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF1A1A2E)))
                            }
                        }
                    }

                    // Switch Solo disponibles
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        BasicText("Solo artículos disponibles", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF1A1A2E)))
                        Box(
                            modifier = Modifier
                                .width(46.dp)
                                .height(28.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(Color(0xFFEA4EA9), Color(0xFF865DEC))))
                                .padding(3.dp),
                            contentAlignment = Alignment.CenterEnd,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .shadow(2.dp, CircleShape)
                                    .clip(CircleShape)
                                    .background(Color.White),
                            )
                        }
                    }
                }

                // Ordenar por
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BasicText("Ordenar por", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF3B3550)))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFF1EFF5))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        listOf("Populares", "Precio ↑", "Precio ↓", "Nuevos").forEachIndexed { i, opt ->
                            val isSel = i == 0
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSel) Color.White else Color.Transparent)
                                    .then(if (isSel) Modifier.shadow(4.dp, RoundedCornerShape(12.dp), spotColor = Color(0x33B450B4)) else Modifier)
                                    .clickable { },
                                contentAlignment = Alignment.Center,
                            ) {
                                BasicText(
                                    opt,
                                    style = TextStyle(
                                        fontFamily = typo.bodyFamily,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 13.sp,
                                        color = if (isSel) Color(0xFF1A1A2E) else Color(0xFF62627A),
                                    ),
                                )
                            }
                        }
                    }
                }

                // CTA
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = Color(0xB3C846B4))
                        .clip(RoundedCornerShape(16.dp))
                        .background(RosaButtonGradient)
                        .clickable { onSelectScreenCode("s80") },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        "Ver 4 resultados",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White),
                    )
                }
            }
        }
    }
}

// ==============================================================================
// s84 · CAMBIAR FECHA DEL EVENTO
// ==============================================================================
@Composable
internal fun RenderChangeDateModalS84(
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
                .padding(top = 54.dp, start = 20.dp, end = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(8.dp, CircleShape, spotColor = Color(0x33B450B4))
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("s40") },
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1A1A2E), size = 22.dp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    BasicText("Cambiar o cancelar", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF1A1A2E)))
                    BasicText("Boda Real Rosa & Dorado", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF62627A)))
                }
            }

            // Segmented tabs: Cambiar fecha / Cancelar evento
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF1EFF5))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .shadow(4.dp, RoundedCornerShape(12.dp), spotColor = Color(0x33B450B4))
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText("Cambiar fecha", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1A1A2E)))
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelectScreenCode("s85") },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText("Cancelar evento", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color(0xFF62627A)))
                }
            }

            // Calendar Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(14.dp, RoundedCornerShape(20.dp), spotColor = Color(0x33C850AA))
                    .border(1.dp, Color(0xFFF2EDF5), RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .padding(14.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Month header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF1EFF5)),
                        ) {
                            RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1A1A2E), size = 16.dp)
                        }
                        BasicText("Octubre 2026", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF1A1A2E)))
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF1EFF5)),
                        ) {
                            RfIcon(kind = RfIconKind.ARROW_RIGHT, tint = Color(0xFF1A1A2E), size = 16.dp)
                        }
                    }

                    // Weekday headers
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        listOf("L", "M", "X", "J", "V", "S", "D").forEach { d ->
                            BasicText(d, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF62627A)))
                        }
                    }

                    // Days grid
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        val weeks = listOf(
                            listOf("", "", "", "1", "2", "3", "4"),
                            listOf("5", "6", "7", "8", "9", "10", "11"),
                            listOf("12", "13", "14", "15", "16", "17", "18"),
                            listOf("19", "20", "21", "22", "23", "24", "25"),
                            listOf("26", "27", "28", "29", "30", "31", ""),
                        )
                        weeks.forEach { week ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                                week.forEach { day ->
                                    val isPast = day.toIntOrNull() != null && day.toInt() < 20
                                    val isCurrent = day == "23"
                                    val isSelected = day == "30"
                                    val hasDot = day == "24" || day == "31"

                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .then(
                                                if (isSelected) Modifier
                                                    .shadow(8.dp, RoundedCornerShape(12.dp), spotColor = Color(0xB3865DEC))
                                                    .background(Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9))))
                                                else if (isCurrent) Modifier.border(1.5.dp, Color(0xFFE3DCE8), RoundedCornerShape(12.dp))
                                                else Modifier
                                            ),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        if (day.isNotEmpty()) {
                                            BasicText(
                                                day,
                                                style = TextStyle(
                                                    fontFamily = typo.bodyFamily,
                                                    fontWeight = if (isSelected || day == "20") FontWeight.ExtraBold else FontWeight.SemiBold,
                                                    fontSize = 14.sp,
                                                    color = if (isSelected) Color.White
                                                    else if (day == "20") Color(0xFFC2338F)
                                                    else if (isPast || isCurrent) Color(0xFFC9C3D1)
                                                    else Color(0xFF1A1A2E),
                                                ),
                                            )
                                            if (hasDot) {
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.BottomCenter)
                                                        .offset(y = (-4).dp)
                                                        .size(5.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(0xFFF7B500)),
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Legend
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.padding(top = 6.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).border(1.5.dp, Color(0xFFE3DCE8), RoundedCornerShape(3.dp)))
                            BasicText("Fecha actual", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF62627A)))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFFF7B500)))
                            BasicText("Poca disponibilidad", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF62627A)))
                        }
                    }
                }
            }

            // Input fields row
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .shadow(2.dp, RoundedCornerShape(16.dp), spotColor = Color(0x20B450B4))
                        .border(1.dp, Color(0xFFF0EBF4), RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        RfIcon(kind = RfIconKind.CALENDAR, tint = Color(0xFF1A1A2E), size = 18.dp)
                        BasicText("Vie 30 oct", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1A1A2E)))
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .shadow(2.dp, RoundedCornerShape(16.dp), spotColor = Color(0x20B450B4))
                        .border(1.dp, Color(0xFFF0EBF4), RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        RfIcon(kind = RfIconKind.ORDERS, tint = Color(0xFF1A1A2E), size = 18.dp)
                        BasicText("2:00 p. m.", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1A1A2E)))
                    }
                }
            }

            // Green banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFEEF6D6))
                    .padding(12.dp, 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top,
            ) {
                RfIcon(kind = RfIconKind.CHECK, tint = Color(0xFF3F560C), size = 18.dp)
                BasicText(
                    "Tus 145 artículos están disponibles el vie 30 oct. Tu primer cambio de fecha es gratis.",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.5.sp, lineHeight = 19.sp, color = Color(0xFF3F560C)),
                )
            }
        }

        // Bottom action button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(start = 20.dp, end = 20.dp, bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BasicText(
                "Rosa Fiesta confirmará el cambio en menos de 24 h.",
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.5.sp, color = Color(0xFF62627A)),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = Color(0xB3C846B4))
                    .clip(RoundedCornerShape(16.dp))
                    .background(RosaButtonGradient)
                    .clickable { onSelectScreenCode("s40") },
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    "Solicitar cambio de fecha",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White),
                )
            }
        }
    }
}

// ==============================================================================
// s85 · CANCELAR EVENTO
// ==============================================================================
@Composable
internal fun RenderCancelEventModalS85(
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
                .padding(top = 54.dp, start = 20.dp, end = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(8.dp, CircleShape, spotColor = Color(0x33B450B4))
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("s40") },
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1A1A2E), size = 22.dp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    BasicText("Cambiar o cancelar", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF1A1A2E)))
                    BasicText("Quinceañera Valentina Crystal · Lun 2 nov", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF62627A)))
                }
            }

            // Segmented tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF1EFF5))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelectScreenCode("s84") },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText("Cambiar fecha", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color(0xFF62627A)))
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .shadow(4.dp, RoundedCornerShape(12.dp), spotColor = Color(0x33B450B4))
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText("Cancelar evento", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1A1A2E)))
                }
            }

            // Card 1: Política de cancelación
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(14.dp, RoundedCornerShape(20.dp), spotColor = Color(0x33C850AA))
                    .border(1.dp, Color(0xFFF2EDF5), RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .padding(14.dp, 16.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row {
                        BasicText("Política de cancelación ", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1A1A2E)))
                        BasicText("· faltan 13 días", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF62627A)))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .shadow(4.dp, CircleShape, spotColor = Color(0x80FDE6F2))
                                .border(4.dp, Color(0xFFFDE6F2), CircleShape)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF6664E9)))),
                        )
                        BasicText("Más de 7 días antes", modifier = Modifier.weight(1f), style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1A1A2E)))
                        BasicText("90%", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color(0xFF1A1A2E)))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.size(18.dp).border(2.dp, Color(0xFFE3DCE8), CircleShape))
                        BasicText("Entre 3 y 7 días", modifier = Modifier.weight(1f), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF9A95A6)))
                        BasicText("50%", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color(0xFF9A95A6)))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.size(18.dp).border(2.dp, Color(0xFFE3DCE8), CircleShape))
                        BasicText("Menos de 3 días", modifier = Modifier.weight(1f), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF9A95A6)))
                        BasicText("Sin reembolso", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color(0xFF9A95A6)))
                    }
                }
            }

            // Card 2: Desglose financiero
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(14.dp, RoundedCornerShape(20.dp), spotColor = Color(0x33C850AA))
                    .border(1.dp, Color(0xFFF2EDF5), RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 6.dp),
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        BasicText("Anticipo pagado", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF62627A)))
                        BasicText("RD$ 1,860", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF1A1A2E)))
                    }
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF2F0F5)))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        BasicText("Cargo de gestión (10%)", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF62627A)))
                        BasicText("− RD$ 186", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF1A1A2E)))
                    }
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF2F0F5)))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        BasicText("Te devolvemos", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1A1A2E)))
                        BasicText("RD$ 1,674", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Color(0xFF4E6A10)))
                    }
                }
            }

            // ¿Por qué cancelas?
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                BasicText("¿Por qué cancelas?", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF3B3550)))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.height(38.dp)) {
                    Box(modifier = Modifier.size(20.dp).border(6.dp, Color(0xFFC2338F), CircleShape))
                    BasicText("Cambio de planes", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.5.sp, color = Color(0xFF1A1A2E)))
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.height(38.dp)) {
                    Box(modifier = Modifier.size(20.dp).border(2.dp, Color(0xFFE3DCE8), CircleShape))
                    BasicText("El presupuesto no me alcanza", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.5.sp, color = Color(0xFF1A1A2E)))
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.height(38.dp)) {
                    Box(modifier = Modifier.size(20.dp).border(2.dp, Color(0xFFE3DCE8), CircleShape))
                    BasicText("Encontré otra opción", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.5.sp, color = Color(0xFF1A1A2E)))
                }
            }

            // Alternative banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFFFCE6F3), Color(0xFFECE4FD))))
                    .clickable { onSelectScreenCode("s84") }
                    .padding(10.dp, 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RfIcon(kind = RfIconKind.CALENDAR, tint = Color(0xFF865DEC), size = 18.dp)
                BasicText(
                    "¿Mejor otra fecha? Cambiarla es gratis.",
                    modifier = Modifier.weight(1f),
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.5.sp, color = Color(0xFF1A1A2E)),
                )
                RfIcon(kind = RfIconKind.ARROW_RIGHT, tint = Color(0xFF865DEC), size = 16.dp)
            }
        }

        // Bottom actions
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(start = 20.dp, end = 20.dp, bottom = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = Color(0x99B42345))
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFB42345))
                    .clickable { onSelectScreenCode("s40") },
                contentAlignment = Alignment.Center,
            ) {
                BasicText("Cancelar evento", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White))
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clickable { onSelectScreenCode("s40") },
                contentAlignment = Alignment.Center,
            ) {
                BasicText("Mantener mi evento", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF62627A)))
            }
        }
    }
}

// ==============================================================================
// s91 · ESTADO SIN CONEXIÓN PERSONAL
// ==============================================================================
@Composable
internal fun RenderPersonalOfflineStateS91(
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

        // Top dark banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(92.dp)
                .background(Color(0xFF1A1A2E))
                .padding(horizontal = 20.dp, vertical = 12.dp),
            contentAlignment = Alignment.BottomStart,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                RfIcon(kind = RfIconKind.WIFI_OFF, tint = Color.White, size = 18.dp)
                BasicText(
                    "Sin conexión · tus cambios se guardan aquí",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp, color = Color.White),
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 130.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .shadow(12.dp, CircleShape, spotColor = Color(0x80FDE6F2))
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFFFDE6F2), Color(0xFFEFE8FD)))),
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.WIFI_OFF, tint = Color(0xFFB754CB), size = 52.dp)
            }

            Spacer(Modifier.height(4.dp))

            BasicText("No hay conexión", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Color(0xFF1A1A2E)))
            BasicText(
                "Revisa tu internet. Tu lista de Mi evento está guardada y la enviaremos cuando vuelvas a conectarte.",
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, lineHeight = 22.sp, color = Color(0xFF62627A), textAlign = androidx.compose.ui.text.style.TextAlign.Center),
            )

            Spacer(Modifier.height(6.dp))

            // Reintentar button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = Color(0xB3C846B4))
                    .clip(RoundedCornerShape(16.dp))
                    .background(RosaButtonGradient)
                    .clickable { onSelectScreenCode("s91") },
                contentAlignment = Alignment.Center,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    RfIcon(kind = RfIconKind.REFRESH, tint = Color.White, size = 18.dp)
                    BasicText("Reintentar", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White))
                }
            }

            // Ver mis favoritos guardados button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = Color(0x33B450B4))
                    .border(1.dp, Color(0xFFEEE9F2), RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .clickable { onSelectScreenCode("s24") },
                contentAlignment = Alignment.Center,
            ) {
                BasicText("Ver mis favoritos guardados", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1A1A2E)))
            }

            // Variante: error del servidor
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .shadow(8.dp, RoundedCornerShape(20.dp), spotColor = Color(0x20AA46A0))
                    .border(1.dp, Color(0xFFF2EDF5), RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .padding(12.dp, 14.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFCE3E8)),
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.WARNING, tint = Color(0xFFB42345), size = 20.dp)
                    }
                    Column {
                        BasicText("Variante: error del servidor", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1A1A2E)))
                        BasicText("“Algo salió mal de nuestro lado. Intenta de nuevo en un momento.”", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF62627A)))
                    }
                }
            }
        }
    }
}

// ==============================================================================
// s92 · ESTADO ÉXITO PERSONAL (CON TOAST)
// ==============================================================================
@Composable
internal fun RenderPersonalSuccessStateS92(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFC)),
    ) {
        // Base screen: Catálogo (s18)
        RenderPersonalCatalogS18(onSelectScreenCode = onSelectScreenCode)

        // Floating Toast overlay above bottom bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(start = 16.dp, end = 16.dp, bottom = 112.dp)
                .shadow(18.dp, RoundedCornerShape(18.dp), spotColor = Color(0x991A1A2E))
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF1A1A2E))
                .padding(12.dp, 12.dp, 12.dp, 14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Green checkmark icon
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFB9D84A)),
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.CHECK, tint = Color(0xFF2E3A08), size = 18.dp)
                }

                // Text
                Column(modifier = Modifier.weight(1f)) {
                    BasicText("Agregado a Mi evento", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White))
                    BasicText("Arco de globos · 17 en total", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color.White.copy(alpha = 0.75f)))
                }

                // Deshacer button
                Box(
                    modifier = Modifier
                        .clickable { onSelectScreenCode("s18") }
                        .padding(horizontal = 6.dp),
                ) {
                    BasicText(
                        "Deshacer",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color(0xFFFFD45C)),
                    )
                }
            }
        }
    }
}
