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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeudry.rosafiesta.designsystem.LocalRfTypography
import com.jeudry.rosafiesta.designsystem.RfIcon
import com.jeudry.rosafiesta.designsystem.RfIconKind
import com.jeudry.rosafiesta.designsystem.RfPhoto
import com.jeudry.rosafiesta.designsystem.RfPhotoKey
import com.jeudry.rosafiesta.domain.CanonicalSampleData

private val AdminBackgroundGradient = Brush.radialGradient(
    0.0f to Color(0xFFE6E7FF),
    0.6f to Color(0x00E6E7FF),
    center = Offset(400f, 0f),
    radius = 450f,
)

private val AdminButtonGradient = Brush.horizontalGradient(
    listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0)),
)

// ==============================================================================
// A01 · INICIO (ADMIN DASHBOARD MÓVIL)
// ==============================================================================
@Composable
internal fun RenderAdminHomeA01(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F6FB)),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(AdminBackgroundGradient)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            // Header Admin: Saludo + Notificaciones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    BasicText(
                        text = "Panel Administrativo",
                        style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = Color(0xFF151729)),
                    )
                    BasicText(
                        text = "Hola Jeudry · Operaciones Rosa Fiesta",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)),
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(6.dp, CircleShape, spotColor = Color(0x334650B4))
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("A29") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BELL, tint = Color(0xFF151729), size = 20.dp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Tarjeta Rosa IA Copiloto
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(10.dp, RoundedCornerShape(22.dp), spotColor = Color(0x337C5CF0))
                    .clip(RoundedCornerShape(22.dp))
                    .background(AdminButtonGradient)
                    .clickable { onSelectScreenCode("A08") }
                    .padding(18.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.SPARK, tint = Color.White, size = 24.dp)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        BasicText(
                            text = "Rosa IA Copiloto",
                            style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp),
                        )
                        BasicText(
                            text = "3 solicitudes pendientes de cotización",
                            style = TextStyle(fontFamily = typo.bodyFamily, color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp),
                        )
                    }

                    RfIcon(kind = RfIconKind.MORE_VERTICAL, tint = Color.White, size = 18.dp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // KPIs rápidos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(18.dp))
                        .padding(14.dp),
                ) {
                    Column {
                        BasicText("Ventas Octubre", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF5F6377), fontSize = 12.sp))
                        BasicText("RD$ 142,500", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, color = Color(0xFF7C5CF0), fontSize = 17.sp))
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(18.dp))
                        .padding(14.dp),
                ) {
                    Column {
                        BasicText("Eventos Activos", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF5F6377), fontSize = 12.sp))
                        BasicText("8 en curso", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2FB58F), fontSize = 17.sp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Acciones Rápidas
            BasicText(
                text = "Acciones Operativas",
                style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.Bold, color = Color(0xFF151729), fontSize = 16.sp),
            )

            Spacer(modifier = Modifier.height(10.dp))

            val quickActions = listOf(
                Triple("Ver Solicitudes Entrantes", "3 clientes esperando cotización", "A02"),
                Triple("Control de Operaciones Día D", "Boda Real · Montaje en curso", "A16"),
                Triple("Registrar Cobro o Pago", "Asentar abono de cliente", "A21"),
                Triple("Inventario & Almacén", "98% disponible para el fin de semana", "A09"),
            )

            quickActions.forEach { (title, subtitle, screenCode) ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(16.dp))
                        .clickable { onSelectScreenCode(screenCode) }
                        .padding(16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            BasicText(title, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF151729), fontSize = 15.sp))
                            BasicText(subtitle, style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF5F6377), fontSize = 12.sp))
                        }
                        RfIcon(kind = RfIconKind.MORE_VERTICAL, tint = Color(0xFF7C5CF0), size = 18.dp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(110.dp))
        }
    }
}

// ==============================================================================
// A02 · SOLICITUDES ENTRANTES
// ==============================================================================
@Composable
internal fun RenderAdminRequestsA02(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F6FB)),
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
                        .shadow(6.dp, CircleShape, spotColor = Color(0x334650B4))
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("A01") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                }

                BasicText(
                    text = "Solicitudes",
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF151729)),
                )

                Spacer(modifier = Modifier.size(44.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            val requests = listOf(
                Triple("Patricia Lora", "Boda Jardín · 100 personas · Vie 20 Nov", "RD$ 32,500"),
                Triple("Marcos Tavárez", "Cumpleaños 50 · 60 personas · Sáb 28 Nov", "RD$ 18,200"),
                Triple("Empresas Baní", "Coctel corporativo · 150 personas · Jue 3 Dic", "RD$ 45,000"),
            )

            requests.forEach { (client, desc, est) ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .shadow(6.dp, RoundedCornerShape(20.dp), spotColor = Color(0x224650B4))
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                        .padding(16.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                BasicText(client, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF151729), fontSize = 16.sp))
                                BasicText(desc, style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF5F6377), fontSize = 13.sp))
                            }
                            BasicText(est, style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.Bold, color = Color(0xFF7C5CF0), fontSize = 15.sp))
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(AdminButtonGradient)
                                .clickable { onSelectScreenCode("A03") },
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText("Preparar Cotización", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// ==============================================================================
// A03 · COTIZADOR RÁPIDO ADMIN
// ==============================================================================
@Composable
internal fun RenderAdminQuoteBuilderA03(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F6FB)),
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
                        .shadow(6.dp, CircleShape, spotColor = Color(0x334650B4))
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("A02") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                }

                BasicText(
                    text = "Armar Cotización",
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF151729)),
                )

                Spacer(modifier = Modifier.size(44.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Datos del cliente
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(18.dp))
                    .padding(16.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    BasicText("Cliente: Patricia Lora", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF151729), fontSize = 15.sp))
                    BasicText("Evento: Boda Jardín · Viernes 20 Noviembre", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF5F6377), fontSize = 13.sp))
                    BasicText("Lugar: Quinta Las Flores, Santo Domingo Norte", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF5F6377), fontSize = 13.sp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Ítems cotizados
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(18.dp))
                    .padding(16.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BasicText("Artículos asignados:", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF151729), fontSize = 14.sp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        BasicText("100 Sillas Tiffany Doradas", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF151729)))
                        BasicText("RD$ 4,000", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF151729)))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        BasicText("10 Mesas Redondas 10 pax", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF151729)))
                        BasicText("RD$ 3,500", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF151729)))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        BasicText("1 Arco Floral Rosa Nupcial", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF151729)))
                        BasicText("RD$ 8,500", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF151729)))
                    }
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFECECF5)))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        BasicText("Total con ITBIS", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.Bold, color = Color(0xFF151729), fontSize = 15.sp))
                        BasicText("RD$ 18,880", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, color = Color(0xFF7C5CF0), fontSize = 18.sp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AdminButtonGradient)
                    .clickable { onSelectScreenCode("A01") },
                contentAlignment = Alignment.Center,
            ) {
                BasicText("Enviar cotización al cliente", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp))
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// ==============================================================================
// A09 · INVENTARIO
// ==============================================================================
@Composable
internal fun RenderAdminInventoryA09(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val articles = viewModel?.articles?.collectAsState()?.value ?: CanonicalSampleData.articles

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F6FB)),
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
                BasicText(
                    text = "Inventario",
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = Color(0xFF151729)),
                )

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(6.dp, CircleShape, spotColor = Color(0x334650B4))
                        .clip(CircleShape)
                        .background(AdminButtonGradient)
                        .clickable { onSelectScreenCode("A10") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.PLUS, tint = Color.White, size = 20.dp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            articles.forEach { art ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(18.dp))
                        .clickable { onSelectScreenCode("A23") }
                        .padding(16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            BasicText(art.name, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF151729), fontSize = 15.sp))
                            BasicText("Código: ${art.code} · ${art.category}", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF5F6377), fontSize = 12.sp))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            BasicText("${art.totalStock} unidades", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.Bold, color = Color(0xFF7C5CF0), fontSize = 15.sp))
                            BasicText("RD$ ${art.pricePerDayDop} c/u", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF5F6377), fontSize = 12.sp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(110.dp))
        }
    }
}

// ==============================================================================
// A11 · EVENTOS ADMIN
// ==============================================================================
@Composable
internal fun RenderAdminEventsA11(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val events = viewModel?.events?.collectAsState()?.value ?: CanonicalSampleData.events

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F6FB)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            BasicText(
                text = "Eventos Programados",
                style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = Color(0xFF151729)),
            )

            Spacer(modifier = Modifier.height(20.dp))

            events.forEach { evt ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(18.dp))
                        .clickable { onSelectScreenCode("A13") }
                        .padding(16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            BasicText(evt.title, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF151729), fontSize = 15.sp))
                            BasicText("${evt.dateLabel} · ${evt.venue}", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF5F6377), fontSize = 13.sp))
                            BasicText("Cliente: ${evt.clientName} (${evt.guests} pax)", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF7C5CF0), fontSize = 12.sp))
                        }
                        RfIcon(kind = RfIconKind.MORE_VERTICAL, tint = Color(0xFF7C5CF0), size = 18.dp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(110.dp))
        }
    }
}

// ==============================================================================
// A12 · MÁS ADMIN
// ==============================================================================
@Composable
internal fun RenderAdminMoreA12(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F6FB)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            BasicText(
                text = "Administración",
                style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = Color(0xFF151729)),
            )

            Spacer(modifier = Modifier.height(20.dp))

            val menu = listOf(
                Pair("Directorio de Clientes", "A26"),
                Pair("Equipo & Permisos", "A28"),
                Pair("Pagos y Finanzas", "A20"),
                Pair("Control de Cancelaciones", "A32"),
                Pair("Ajustes del Sistema", "A31"),
                Pair("Cambiar a App Personal", "s14"),
            )

            menu.forEach { (label, screen) ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(16.dp))
                        .clickable { onSelectScreenCode(screen) }
                        .padding(16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        BasicText(label, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF151729), fontSize = 15.sp))
                        RfIcon(kind = RfIconKind.MORE_VERTICAL, tint = Color(0xFF7C5CF0), size = 18.dp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(110.dp))
        }
    }
}

// ==============================================================================
// A20 · PAGOS ADMIN
// ==============================================================================
@Composable
internal fun RenderAdminPaymentsA20(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val payments = viewModel?.payments?.collectAsState()?.value ?: CanonicalSampleData.payments

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F6FB)),
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
                        .shadow(6.dp, CircleShape, spotColor = Color(0x334650B4))
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("A12") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                }

                BasicText(
                    text = "Cobros y Pagos",
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF151729)),
                )

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(6.dp, CircleShape, spotColor = Color(0x334650B4))
                        .clip(CircleShape)
                        .background(AdminButtonGradient)
                        .clickable { onSelectScreenCode("A21") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.PLUS, tint = Color.White, size = 20.dp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            payments.forEach { pay ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            BasicText("Ref: ${pay.referenceCode}", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF151729), fontSize = 14.sp))
                            BasicText("${pay.methodLabel} · ${pay.dateLabel}", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF5F6377), fontSize = 12.sp))
                        }
                        BasicText("RD$ ${pay.amountDop}", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2FB58F), fontSize = 16.sp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// ==============================================================================
// A21 · REGISTRAR PAGO ADMIN
// ==============================================================================
@Composable
internal fun RenderAdminRecordPaymentA21(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F6FB)),
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
                        .shadow(6.dp, CircleShape, spotColor = Color(0x334650B4))
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("A20") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                }

                BasicText(
                    text = "Registrar Pago",
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF151729)),
                )

                Spacer(modifier = Modifier.size(44.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    BasicText("Monto en Pesos Dominicanos (DOP)", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF151729), fontSize = 14.sp))
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
                        BasicText("RD$ 9,778.50", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF151729), fontSize = 15.sp))
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    BasicText("Método utilizado", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF151729), fontSize = 14.sp))
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
                        BasicText("Transferencia Banco Popular", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF151729), fontSize = 15.sp))
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    BasicText("Número de referencia bancaria", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF151729), fontSize = 14.sp))
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
                        BasicText("POP-2026-99214", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF151729), fontSize = 15.sp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AdminButtonGradient)
                    .clickable { onSelectScreenCode("A20") },
                contentAlignment = Alignment.Center,
            ) {
                BasicText("Asentar pago", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp))
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// ==============================================================================
// A13 · DETALLE DEL EVENTO (ADMIN)
// ==============================================================================
@Composable
internal fun RenderAdminEventDetailA13(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F6FB)),
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
                        .shadow(6.dp, CircleShape, spotColor = Color(0x334650B4))
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("A11") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                }

                BasicText(
                    text = "Detalle del Evento",
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF151729)),
                )

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(6.dp, CircleShape, spotColor = Color(0x334650B4))
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("A18") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.SPARK, tint = Color(0xFF7C5CF0), size = 20.dp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Tarjeta principal del evento
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                    .padding(18.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        BasicText(
                            text = "Boda Real Rosa & Dorado",
                            style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color(0xFF151729)),
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFEFE9FE))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                        ) {
                            BasicText("En 3 días", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF7C5CF0), fontSize = 12.sp))
                        }
                    }

                    BasicText("Vie 23 oct · Jardín Botánico Nacional", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF5F6377), fontSize = 13.sp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF7C5CF0)),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText("C", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp))
                        }
                        Column {
                            BasicText("Camila Rodríguez", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF151729), fontSize = 14.sp))
                            BasicText("120 invitados · Ceremonia 2:00 p. m.", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF5F6377), fontSize = 12.sp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sub-módulos operativos
            val subModules = listOf(
                Triple("Estado de Pago", "Pagado RD$ 3,210 · Saldo RD$ 0", "A20"),
                Triple("Artículos Asignados", "2 tipos · 120 unidades reservadas", "A09"),
                Triple("Checklist y Tareas", "3 de 8 tareas hechas · Pedro y Yuli", "A17"),
                Triple("Plano de Distribución", "Boceto aprobado por Camila", "A18"),
            )

            subModules.forEach { (title, subtitle, screen) ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(16.dp))
                        .clickable { onSelectScreenCode(screen) }
                        .padding(16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            BasicText(title, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF151729), fontSize = 15.sp))
                            BasicText(subtitle, style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF5F6377), fontSize = 12.sp))
                        }
                        RfIcon(kind = RfIconKind.MORE_VERTICAL, tint = Color(0xFF7C5CF0), size = 18.dp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AdminButtonGradient)
                    .clickable { onSelectScreenCode("A16") },
                contentAlignment = Alignment.Center,
            ) {
                BasicText("Iniciar Modo Ejecución Día D", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp))
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// ==============================================================================
// A16 · EJECUCIÓN OPERATIVA DÍA D
// ==============================================================================
@Composable
internal fun RenderAdminExecutionA16(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    var selectedStage by remember { mutableStateOf("Montaje") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F6FB)),
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
                        .shadow(6.dp, CircleShape, spotColor = Color(0x334650B4))
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("A13") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                }

                BasicText(
                    text = "Modo Día D",
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF151729)),
                )

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(6.dp, CircleShape, spotColor = Color(0x334650B4))
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("A18") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.SPARK, tint = Color(0xFF7C5CF0), size = 20.dp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            BasicText("Boda Real Rosa & Dorado", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729), fontSize = 20.sp))
            BasicText("Jardín Botánico · Entrada por puerta 2", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF5F6377), fontSize = 13.sp))

            Spacer(modifier = Modifier.height(16.dp))

            // Segmented control de etapas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(14.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                listOf("Carga · 3/3", "Montaje · 2/4", "Desmontaje").forEach { stage ->
                    val isSel = selectedStage in stage
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSel) Color(0xFF7C5CF0) else Color.Transparent)
                            .clickable {
                                selectedStage = if ("Carga" in stage) {
                                    "Carga"
                                } else if ("Montaje" in stage) {
                                    "Montaje"
                                } else {
                                    "Desmontaje"
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = stage,
                            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium, color = if (isSel) Color.White else Color(0xFF5F6377), fontSize = 12.sp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            val tasks = listOf(
                Triple("Montar arco floral nupcial en entrada", "Completado por Yuli · 11:30 a. m.", true),
                Triple("Colocar 120 sillas Tiffany en jardín", "Completado por Pedro · 12:15 p. m.", true),
                Triple("12 centros florales con mantel blanco", "En progreso · Yuli · Mesa 8 de 12", false),
                Triple("Inspección final de montaje con la novia", "Pendiente · Mateo · Programada 1:30 p. m.", false),
            )

            tasks.forEach { (title, subtitle, done) ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(16.dp))
                        .padding(14.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(if (done) Color(0xFF2FB58F) else Color(0xFFE5E7EB)),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (done) {
                                BasicText("✓", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp))
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            BasicText(title, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF151729), fontSize = 14.sp))
                            BasicText(subtitle, style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF5F6377), fontSize = 12.sp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AdminButtonGradient)
                    .clickable { onSelectScreenCode("A19") },
                contentAlignment = Alignment.Center,
            ) {
                BasicText("Finalizar y Pasar a Cierre", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp))
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// ==============================================================================
// A17 · TAREAS DEL EQUIPO
// ==============================================================================
@Composable
internal fun RenderAdminTasksA17(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F6FB)),
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
                        .shadow(6.dp, CircleShape, spotColor = Color(0x334650B4))
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("A13") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                }

                BasicText(
                    text = "Tareas del Equipo",
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF151729)),
                )

                Spacer(modifier = Modifier.size(44.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            BasicText("Boda Real Rosa & Dorado", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.Bold, color = Color(0xFF151729), fontSize = 18.sp))

            Spacer(modifier = Modifier.height(12.dp))

            // Chips de filtrado por miembro
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf("Todos", "Mateo", "Pedro", "Yuli", "Alberto").forEachIndexed { idx, name ->
                    val isSel = idx == 0
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSel) Color(0xFF7C5CF0) else Color.White)
                            .border(1.dp, if (isSel) Color(0xFF7C5CF0) else Color(0xFFECECF5), RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        BasicText(
                            text = name,
                            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = if (isSel) Color.White else Color(0xFF5F6377), fontSize = 12.sp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Sección 1: Hoy
            BasicText("Hoy · Martes 20", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.Bold, color = Color(0xFF151729), fontSize = 15.sp))
            Spacer(modifier = Modifier.height(8.dp))

            val todayTasks = listOf(
                Pair("Confirmar paleta floral con suplidor", "10:00 a. m. · Asignado a Yuli"),
                Pair("Comprar cintas doradas y accesorios", "5:00 p. m. · Asignado a Mateo · RD$ 3,150"),
            )

            todayTasks.forEach { (task, detail) ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(14.dp))
                        .padding(14.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        BasicText(task, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF151729), fontSize = 14.sp))
                        BasicText(detail, style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF5F6377), fontSize = 12.sp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sección 2: Jueves 22
            BasicText("Jueves 22 · Víspera", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.Bold, color = Color(0xFF151729), fontSize = 15.sp))
            Spacer(modifier = Modifier.height(8.dp))

            val eveTasks = listOf(
                Pair("Limpieza y lustre de 120 sillas Tiffany", "3:00 p. m. · En almacén · Yuli"),
                Pair("Carga e inventario en camión Ford", "4:00 p. m. · Pedro y Alberto"),
            )

            eveTasks.forEach { (task, detail) ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(14.dp))
                        .padding(14.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        BasicText(task, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF151729), fontSize = 14.sp))
                        BasicText(detail, style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF5F6377), fontSize = 12.sp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// ==============================================================================
// A18 · PLANO Y DISTRIBUCIÓN
// ==============================================================================
@Composable
internal fun RenderAdminSketchA18(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F6FB)),
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
                        .shadow(6.dp, CircleShape, spotColor = Color(0x334650B4))
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("A13") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                }

                BasicText(
                    text = "Plano de Montaje",
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF151729)),
                )

                Spacer(modifier = Modifier.size(44.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            BasicText("Boceto de Camila", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.Bold, color = Color(0xFF151729), fontSize = 18.sp))
            BasicText("Boda Real Rosa & Dorado · Distribución en jardín", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF5F6377), fontSize = 13.sp))

            Spacer(modifier = Modifier.height(16.dp))

            // Canvas interactivo / visual de distribución
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                    .padding(16.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFEFE9FE))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                        ) {
                            BasicText("6 mesas redondas", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF7C5CF0), fontSize = 12.sp))
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFEFE9FE))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                        ) {
                            BasicText("60 sillas", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF7C5CF0), fontSize = 12.sp))
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF6F6FB)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            RfIcon(kind = RfIconKind.SPARK, tint = Color(0xFF7C5CF0), size = 32.dp)
                            Spacer(modifier = Modifier.height(6.dp))
                            BasicText("Arco Floral en Cabecera Nupcial", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF151729), fontSize = 13.sp))
                            BasicText("Pista de baile al centro · Mesas a los laterales", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF5F6377), fontSize = 12.sp))
                        }
                    }

                    BasicText("Aprobado por el cliente el 18 de octubre", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF2FB58F), fontSize = 12.sp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AdminButtonGradient)
                    .clickable { onSelectScreenCode("A13") },
                contentAlignment = Alignment.Center,
            ) {
                BasicText("Confirmar Distribución", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp))
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// ==============================================================================
// A19 · CIERRE DE EVENTO
// ==============================================================================
@Composable
internal fun RenderAdminCloseoutA19(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F6FB)),
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
                        .shadow(6.dp, CircleShape, spotColor = Color(0x334650B4))
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("A13") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                }

                BasicText(
                    text = "Cierre de Evento",
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF151729)),
                )

                Spacer(modifier = Modifier.size(44.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            BasicText("Gala Corporativa Grupo Caribe · 12 oct", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF5F6377), fontSize = 13.sp))

            Spacer(modifier = Modifier.height(16.dp))

            // Retorno de mobiliario
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(18.dp))
                    .padding(16.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BasicText("Devolución de Artículos", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.Bold, color = Color(0xFF151729), fontSize = 15.sp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        BasicText("120 Sillas Tiffany Cristal", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF151729)))
                        BasicText("120 / 120 recibidas", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF2FB58F), fontSize = 13.sp))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        BasicText("12 Centros florales", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF151729)))
                        BasicText("12 / 12 recibidos", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF2FB58F), fontSize = 13.sp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Daños o faltantes
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(18.dp))
                    .padding(16.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    BasicText("Daños y Faltantes", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.Bold, color = Color(0xFF151729), fontSize = 15.sp))
                    BasicText("1 Silla Tiffany Cristal · Pata fisurada en desmontaje", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF5F6377), fontSize = 13.sp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        BasicText("Cargo de reparación", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF5F6377), fontSize = 12.sp))
                        BasicText("RD$ 35", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626), fontSize = 14.sp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Liquidación final
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(18.dp))
                    .padding(16.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    BasicText("Cobro Final", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.Bold, color = Color(0xFF151729), fontSize = 15.sp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        BasicText("Total del evento", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF5F6377), fontSize = 13.sp))
                        BasicText("RD$ 14,500", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF151729)))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        BasicText("Pagado previamente", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF5F6377), fontSize = 13.sp))
                        BasicText("RD$ 14,500", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF151729)))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        BasicText("Cargo por daño", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFFDC2626), fontSize = 13.sp))
                        BasicText("RD$ 35", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626), fontSize = 13.sp))
                    }
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFECECF5)))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        BasicText("Saldo pendiente", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.Bold, color = Color(0xFF151729), fontSize = 15.sp))
                        BasicText("RD$ 35", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, color = Color(0xFF7C5CF0), fontSize = 18.sp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AdminButtonGradient)
                    .clickable { onSelectScreenCode("A01") },
                contentAlignment = Alignment.Center,
            ) {
                BasicText("Completar Cierre de Evento", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp))
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// ==============================================================================
// A10 · ALTA DE NUEVO ARTÍCULO
// ==============================================================================
@Composable
internal fun RenderAdminNewArticleA10(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F6FB)),
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
                        .shadow(6.dp, CircleShape, spotColor = Color(0x334650B4))
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("A09") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                }

                BasicText(
                    text = "Nuevo Artículo",
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF151729)),
                )

                Spacer(modifier = Modifier.size(44.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            BasicText("Se agrega al catálogo y al inventario", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF5F6377), fontSize = 13.sp))

            Spacer(modifier = Modifier.height(16.dp))

            // Placeholder de fotos
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    RfIcon(kind = RfIconKind.PLUS, tint = Color(0xFF7C5CF0), size = 28.dp)
                    Spacer(modifier = Modifier.height(6.dp))
                    BasicText("Hasta 6 fotos. La primera es la portada.", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF5F6377), fontSize = 13.sp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val fields = listOf(
                Pair("Nombre", "Mesa Plegable Rectangular"),
                Pair("Categoría", "Mobiliario"),
                Pair("Precio por día (DOP)", "RD$ 450"),
                Pair("Cantidad en stock", "40"),
                Pair("Descripción", "Mesa resistente para catering y buffets"),
            )

            fields.forEach { (label, value) ->
                Column(
                    modifier = Modifier.padding(vertical = 5.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    BasicText(label, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF151729), fontSize = 13.sp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        BasicText(value, style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF151729), fontSize = 14.sp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AdminButtonGradient)
                    .clickable { onSelectScreenCode("A09") },
                contentAlignment = Alignment.Center,
            ) {
                BasicText("Guardar Artículo", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp))
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// ==============================================================================
// A23 · FICHA DE ARTÍCULO
// ==============================================================================
@Composable
internal fun RenderAdminArticleDetailA23(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F6FB)),
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
                        .shadow(6.dp, CircleShape, spotColor = Color(0x334650B4))
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("A09") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                }

                BasicText(
                    text = "Ficha de Artículo",
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF151729)),
                )

                Spacer(modifier = Modifier.size(44.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Foto y cabecera
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(20.dp)),
            ) {
                RfPhoto(photoKey = RfPhotoKey.SILLA_TIFFANY, contentDescription = "Silla Tiffany Cristal", modifier = Modifier.fillMaxSize())
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    BasicText("Silla Tiffany Cristal", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729), fontSize = 20.sp))
                    BasicText("Mobiliario · Visible en el catálogo", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF5F6377), fontSize = 13.sp))
                }
                BasicText("RD$ 40 / día", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.Bold, color = Color(0xFF7C5CF0), fontSize = 16.sp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Kardex de unidades
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                listOf(
                    Pair("120", "En total"),
                    Pair("116", "En buen estado"),
                    Pair("4", "En reparación"),
                ).forEach { (qty, label) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(16.dp))
                            .padding(14.dp),
                    ) {
                        Column {
                            BasicText(qty, style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729), fontSize = 18.sp))
                            BasicText(label, style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF5F6377), fontSize = 11.sp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Actividad próxima
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(18.dp))
                    .padding(16.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    BasicText("Actividad Programada", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.Bold, color = Color(0xFF151729), fontSize = 15.sp))
                    BasicText("3 eventos reservados para el fin de semana del 23 al 25 de octubre", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF5F6377), fontSize = 13.sp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AdminButtonGradient)
                    .clickable { onSelectScreenCode("A09") },
                contentAlignment = Alignment.Center,
            ) {
                BasicText("Editar Artículo", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp))
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// ==============================================================================
// A24 · PAQUETES COMERCIALES
// ==============================================================================
@Composable
internal fun RenderAdminPackagesA24(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F6FB)),
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
                        .shadow(6.dp, CircleShape, spotColor = Color(0x334650B4))
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("A12") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                }

                BasicText(
                    text = "Paquetes",
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF151729)),
                )

                Spacer(modifier = Modifier.size(44.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            BasicText("4 activos en el catálogo", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF5F6377), fontSize = 13.sp))

            Spacer(modifier = Modifier.height(16.dp))

            val pkgs = listOf(
                Pair("Bodas Rosa", "RD$ 850 · Ahorro RD$ 150"),
                Pair("Quinceañera Premium", "RD$ 1,200 · Ahorro RD$ 250"),
                Pair("Cumpleaños Infantil", "RD$ 450 · Ahorro RD$ 80"),
                Pair("Evento Corporativo", "RD$ 600 · Ahorro RD$ 100"),
            )

            pkgs.forEach { (name, price) ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(18.dp))
                        .clickable { onSelectScreenCode("A25") }
                        .padding(16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            BasicText(name, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF151729), fontSize = 15.sp))
                            BasicText(price, style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF5F6377), fontSize = 12.sp))
                        }
                        RfIcon(kind = RfIconKind.MORE_VERTICAL, tint = Color(0xFF7C5CF0), size = 18.dp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// ==============================================================================
// A25 · EDITAR PAQUETE
// ==============================================================================
@Composable
internal fun RenderAdminPackageEditA25(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F6FB)),
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
                        .shadow(6.dp, CircleShape, spotColor = Color(0x334650B4))
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("A24") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                }

                BasicText(
                    text = "Editar Paquete",
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF151729)),
                )

                Spacer(modifier = Modifier.size(44.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            BasicText("Bodas Rosa", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.Bold, color = Color(0xFF151729), fontSize = 18.sp))

            Spacer(modifier = Modifier.height(16.dp))

            // Ítems incluidos
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(18.dp))
                    .padding(16.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BasicText("Artículos Incluidos", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.Bold, color = Color(0xFF151729), fontSize = 15.sp))

                    val items = listOf(
                        Pair("Arco de globos rosa", "RD$ 95 c/u"),
                        Pair("Centro de mesa floral", "RD$ 35 c/u"),
                        Pair("Silla Tiffany Cristal", "RD$ 4 c/u"),
                        Pair("Mesa redonda", "RD$ 25 c/u"),
                    )

                    items.forEach { (item, pr) ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            BasicText(item, style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF151729), fontSize = 13.sp))
                            BasicText(pr, style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF5F6377), fontSize = 12.sp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Descuento
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(18.dp))
                    .padding(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        BasicText("Descuento del paquete", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF151729), fontSize = 14.sp))
                        BasicText("Aplicado sobre la suma individual", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF5F6377), fontSize = 12.sp))
                    }
                    BasicText("15%", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, color = Color(0xFF7C5CF0), fontSize = 18.sp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AdminButtonGradient)
                    .clickable { onSelectScreenCode("A24") },
                contentAlignment = Alignment.Center,
            ) {
                BasicText("Guardar Paquete", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp))
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// ==============================================================================
// A26 · DIRECTORIO DE CLIENTES
// ==============================================================================
@Composable
internal fun RenderAdminClientsA26(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F6FB)),
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
                        .shadow(6.dp, CircleShape, spotColor = Color(0x334650B4))
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("A12") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                }

                BasicText(
                    text = "Clientes",
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF151729)),
                )

                Spacer(modifier = Modifier.size(44.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf("Todos · 4", "Con evento próximo · 2", "Empresas · 1").forEachIndexed { idx, filter ->
                    val isSel = idx == 0
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSel) Color(0xFF7C5CF0) else Color.White)
                            .border(1.dp, if (isSel) Color(0xFF7C5CF0) else Color(0xFFECECF5), RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        BasicText(
                            text = filter,
                            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = if (isSel) Color.White else Color(0xFF5F6377), fontSize = 12.sp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            val clients = listOf(
                Triple("Camila Rodríguez", "3 eventos · Pagó RD$ 3,210", "Vie 23 oct"),
                Triple("Laura Peña", "Cotización enviada · Boda Jardín", "Sáb 7 nov"),
                Triple("Ana Gómez", "1 evento · Cumpleaños infantil", "Sáb 14 nov"),
                Triple("Grupo Caribe", "Empresa · 2 eventos realizados", "Cerrado"),
            )

            clients.forEach { (name, desc, date) ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(18.dp))
                        .clickable { onSelectScreenCode("A27") }
                        .padding(16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEFE9FE)),
                                contentAlignment = Alignment.Center,
                            ) {
                                BasicText(name.take(1), style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.Bold, color = Color(0xFF7C5CF0), fontSize = 16.sp))
                            }
                            Column {
                                BasicText(name, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF151729), fontSize = 15.sp))
                                BasicText(desc, style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF5F6377), fontSize = 12.sp))
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF6F6FB))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            BasicText(date, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Medium, color = Color(0xFF5F6377), fontSize = 11.sp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// ==============================================================================
// A27 · FICHA DE CLIENTE
// ==============================================================================
@Composable
internal fun RenderAdminClientProfileA27(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F6FB)),
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
                        .shadow(6.dp, CircleShape, spotColor = Color(0x334650B4))
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("A26") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                }

                BasicText(
                    text = "Ficha de Cliente",
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF151729)),
                )

                Spacer(modifier = Modifier.size(44.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Cabecera Cliente
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF7C5CF0)),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText("C", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 26.sp))
                }
                Spacer(modifier = Modifier.height(10.dp))
                BasicText("Camila Rodríguez", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729), fontSize = 20.sp))
                BasicText("camila.rodriguez@correo.com · (809) 555-0198", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF5F6377), fontSize = 13.sp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Métricas del cliente
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(16.dp))
                        .padding(14.dp),
                ) {
                    Column {
                        BasicText("Eventos", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF5F6377), fontSize = 12.sp))
                        BasicText("3", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729), fontSize = 20.sp))
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(16.dp))
                        .padding(14.dp),
                ) {
                    Column {
                        BasicText("Facturado", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF5F6377), fontSize = 12.sp))
                        BasicText("RD$ 3,210", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, color = Color(0xFF7C5CF0), fontSize = 18.sp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            BasicText("Historial de Eventos", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.Bold, color = Color(0xFF151729), fontSize = 16.sp))

            Spacer(modifier = Modifier.height(10.dp))

            val events = listOf(
                Pair("Boda Real Rosa & Dorado", "Vie 23 oct · Confirmado"),
                Pair("Quinceañera Valentina Crystal", "Lun 2 nov · Confirmado"),
                Pair("Baby Shower Sofía en Rosas", "Lun 9 nov · Borrador"),
            )

            events.forEach { (title, status) ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                ) {
                    Column {
                        BasicText(title, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF151729), fontSize = 14.sp))
                        BasicText(status, style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF5F6377), fontSize = 12.sp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// ==============================================================================
// A28 · EQUIPO Y PERMISOS
// ==============================================================================
@Composable
internal fun RenderAdminTeamA28(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F6FB)),
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
                        .shadow(6.dp, CircleShape, spotColor = Color(0x334650B4))
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("A12") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                }

                BasicText(
                    text = "Equipo",
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF151729)),
                )

                Spacer(modifier = Modifier.size(44.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            BasicText("4 colaboradores activos", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF5F6377), fontSize = 13.sp))

            Spacer(modifier = Modifier.height(16.dp))

            val team = listOf(
                Triple("Mateo", "Dueña · Acceso total al sistema", "Admin"),
                Triple("Pedro", "Montaje · Eventos y tareas", "Equipo"),
                Triple("Yuli", "Decoración · Eventos e inventario", "Equipo"),
                Triple("Alberto", "Chofer · Solo modo ejecución Día D", "Limitado"),
            )

            team.forEach { (name, role, tag) ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(18.dp))
                        .padding(16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEFE9FE)),
                                contentAlignment = Alignment.Center,
                            ) {
                                BasicText(name.take(1), style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.Bold, color = Color(0xFF7C5CF0), fontSize = 16.sp))
                            }
                            Column {
                                BasicText(name, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF151729), fontSize = 15.sp))
                                BasicText(role, style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF5F6377), fontSize = 12.sp))
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (tag == "Admin") Color(0xFFEFE9FE) else Color(0xFFF6F6FB))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            BasicText(tag, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = if (tag == "Admin") Color(0xFF7C5CF0) else Color(0xFF5F6377), fontSize = 11.sp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
