package com.jeudry.rosafiesta.ui

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

// ==============================================================================
// s16 · ACERCA DE ROSA FIESTA
// ==============================================================================
@Composable
internal fun RenderAboutScreenS16(
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
                        .clickable { onSelectScreenCode("s14") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1E1E24), size = 22.dp)
                }

                BasicText(
                    text = "Acerca de",
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF1E1E24)),
                )

                Spacer(modifier = Modifier.size(44.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFFFE3EC), RoundedCornerShape(20.dp))
                    .padding(20.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BasicText("Rosa Fiesta SRL", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E1E24), fontSize = 22.sp))
                    BasicText("Decoración y planificación de eventos inolvidables en República Dominicana.", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF7A7177), fontSize = 14.sp))

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column {
                            BasicText("127", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFF3366), fontSize = 24.sp))
                            BasicText("eventos realizados", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF7A7177), fontSize = 12.sp))
                        }
                        Column {
                            BasicText("8", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFF3366), fontSize = 24.sp))
                            BasicText("años de experiencia", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF7A7177), fontSize = 12.sp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFFFE3EC), RoundedCornerShape(18.dp))
                    .clickable { onSelectScreenCode("s17") }
                    .padding(16.dp),
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        BasicText("Opiniones y Reseñas", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24), fontSize = 15.sp))
                        BasicText("Calificación promedio 5.0 ★★★★★", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF7A7177), fontSize = 12.sp))
                    }
                    RfIcon(kind = RfIconKind.MORE_VERT, tint = Color(0xFFFF3366), size = 18.dp)
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// ==============================================================================
// s17 · RESEÑAS DE LA EMPRESA
// ==============================================================================
@Composable
internal fun RenderCompanyReviewsS17(
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
                        .clickable { onSelectScreenCode("s16") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1E1E24), size = 22.dp)
                }

                BasicText(
                    text = "Reseñas",
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF1E1E24)),
                )

                Spacer(modifier = Modifier.size(44.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BasicText("5.0", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E1E24), fontSize = 36.sp))
                Column {
                    BasicText("★★★★★", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFFF59E0B), fontSize = 16.sp))
                    BasicText("Basado en eventos reales verificados", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF7A7177), fontSize = 12.sp))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            val reviews = listOf(
                Pair("Patricia Lora", "Baby shower de ensueño. Las mesas y el arco floral superaron mis expectativas. 100% recomendados."),
                Pair("Grupo Caribe", "Gala corporativa impecable. Montaje puntual y atención a cada detalle."),
            )

            reviews.forEach { (author, text) ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFFFE3EC), RoundedCornerShape(18.dp))
                        .padding(16.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            BasicText(author, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24), fontSize = 15.sp))
                            BasicText("★★★★★", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFFF59E0B), fontSize = 12.sp))
                        }
                        BasicText(text, style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF7A7177), fontSize = 13.sp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// ==============================================================================
// s41 & s42 · CHAT DEL EVENTO (CLIENTE)
// ==============================================================================
@Composable
internal fun RenderEventChatTabS41(
    onSelectScreenCode: (String) -> Unit = {},
) {
    RenderEventChatS42(onSelectScreenCode = onSelectScreenCode)
}

@Composable
internal fun RenderEventChatS42(
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
                        .clickable { onSelectScreenCode("s40") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1E1E24), size = 22.dp)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    BasicText("Equipo Rosa Fiesta", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E1E24)))
                    BasicText("Boda Real Rosa & Dorado", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF7A7177)))
                }

                Spacer(modifier = Modifier.size(44.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Conversación
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFFFE3EC), RoundedCornerShape(16.dp))
                        .padding(14.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        BasicText("¡Hola Camila! Ya tenemos tu paleta de rosas seleccionada para el arco principal.", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF1E1E24), fontSize = 14.sp))
                        BasicText("Ayer · 4:15 p. m. · Yuli", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF7A7177), fontSize = 11.sp))
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .align(Alignment.End)
                        .clip(RoundedCornerShape(16.dp))
                        .background(PersonalButtonGradient)
                        .padding(14.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        BasicText("¡Me encanta! Quedará perfecto con las mesas del jardín.", style = TextStyle(fontFamily = typo.bodyFamily, color = Color.White, fontSize = 14.sp))
                        BasicText("Ayer · 5:20 p. m.", style = TextStyle(fontFamily = typo.bodyFamily, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// ==============================================================================
// s59 · PANEL FINANZAS CLIENTE / SOCIO
// ==============================================================================
@Composable
internal fun RenderFinanceDashboardS59(
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
                    BasicText("Finanzas del Evento", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Color(0xFF1E1E24)))
                    BasicText("Octubre 2026 · Desglose presupuestario", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF7A7177), fontSize = 13.sp))
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(6.dp, CircleShape, spotColor = Color(0x33FF3366))
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("s58") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1E1E24), size = 20.dp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFFFE3EC), RoundedCornerShape(20.dp))
                    .padding(18.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    BasicText("Presupuesto Invertido", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF7A7177), fontSize = 13.sp))
                    BasicText("RD$ 10,360", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFF3366), fontSize = 24.sp))
                    BasicText("Abonado: RD$ 3,210 · Pendiente: RD$ 7,150", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF1E1E24), fontSize = 12.sp))
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// ==============================================================================
// s60 a s74 · PANELES DE ANALÍTICA, LEADS, REPORTES Y BOT WHATSAPP
// ==============================================================================
@Composable
internal fun RenderInsurancePanelS60(onSelectScreenCode: (String) -> Unit = {}) = RenderGenericPersonalSubPanel("Pólizas y Seguros", "Cobertura activa con Seguros Universal RD", onSelectScreenCode)

@Composable
internal fun RenderAuditPanelS61(onSelectScreenCode: (String) -> Unit = {}) = RenderGenericPersonalSubPanel("Auditoría de Eventos", "Registro y trazabilidad de cambios en tiempo real", onSelectScreenCode)

@Composable
internal fun RenderClientsAnalyticsS62(onSelectScreenCode: (String) -> Unit = {}) = RenderGenericPersonalSubPanel("Cartera de Clientes", "Métricas de recurrencia y satisfacción", onSelectScreenCode)

@Composable
internal fun RenderInventoryCalendarS63(onSelectScreenCode: (String) -> Unit = {}) = RenderGenericPersonalSubPanel("Disponibilidad de Mobiliario", "Calendario de ocupación por fecha", onSelectScreenCode)

@Composable
internal fun RenderLeadsPipelineS64(onSelectScreenCode: (String) -> Unit = {}) = RenderGenericPersonalSubPanel("Pipeline de Solicitudes", "3 prospectos calificados para cotización", onSelectScreenCode)

@Composable
internal fun RenderLeadsFollowupS65(onSelectScreenCode: (String) -> Unit = {}) = RenderGenericPersonalSubPanel("Seguimientos Pendientes", "2 clientes esperando llamada de confirmación", onSelectScreenCode)

@Composable
internal fun RenderLeadsStatsS66(onSelectScreenCode: (String) -> Unit = {}) = RenderGenericPersonalSubPanel("Estadísticas de Conversión", "33.3% tasa de confirmación de eventos", onSelectScreenCode)

@Composable
internal fun RenderReportsIncomeS67(onSelectScreenCode: (String) -> Unit = {}) = RenderGenericPersonalSubPanel("Reporte de Ingresos", "RD$ 32,780 generados este período", onSelectScreenCode)

@Composable
internal fun RenderReportsEventsS68(onSelectScreenCode: (String) -> Unit = {}) = RenderGenericPersonalSubPanel("Reporte de Eventos", "156 eventos realizados históricamente", onSelectScreenCode)

@Composable
internal fun RenderReportsClientsS69(onSelectScreenCode: (String) -> Unit = {}) = RenderGenericPersonalSubPanel("Reporte de Clientes", "248 clientes atendidos · +18 este mes", onSelectScreenCode)

@Composable
internal fun RenderReportsInventoryS70(onSelectScreenCode: (String) -> Unit = {}) = RenderGenericPersonalSubPanel("Rotación de Mobiliario", "1,245 artículos en inventario activo", onSelectScreenCode)

@Composable
internal fun RenderReviewModerationS71(onSelectScreenCode: (String) -> Unit = {}) = RenderGenericPersonalSubPanel("Moderación de Opiniones", "10 reseñas aprobadas con 5 estrellas", onSelectScreenCode)

@Composable
internal fun RenderBotQuestionsS72(onSelectScreenCode: (String) -> Unit = {}) = RenderGenericPersonalSubPanel("Respuestas Automáticas WhatsApp", "Preguntas frecuentes de precio y stock", onSelectScreenCode)

@Composable
internal fun RenderBotKeywordsS73(onSelectScreenCode: (String) -> Unit = {}) = RenderGenericPersonalSubPanel("Palabras Clave WhatsApp", "Filtros de reconocimiento para cotización rápida", onSelectScreenCode)

@Composable
internal fun RenderBotConversationsS74(onSelectScreenCode: (String) -> Unit = {}) = RenderGenericPersonalSubPanel("Chats del Bot WhatsApp", "Conversaciones automáticas con clientes", onSelectScreenCode)

@Composable
private fun RenderGenericPersonalSubPanel(
    title: String,
    subtitle: String,
    onSelectScreenCode: (String) -> Unit,
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
                Column(modifier = Modifier.weight(1f)) {
                    BasicText(text = title, style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Color(0xFF1E1E24)))
                    BasicText(text = subtitle, style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF7A7177), fontSize = 13.sp))
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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFFFE3EC), RoundedCornerShape(20.dp))
                    .padding(20.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        RfIcon(kind = RfIconKind.SPARK, tint = Color(0xFFFF3366), size = 24.dp)
                        BasicText("Sincronizado con Rosa IA", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E24), fontSize = 16.sp))
                    }
                    BasicText("Información actualizada en tiempo real conforme a los eventos y cotizaciones activas de Rosa Fiesta.", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF7A7177), fontSize = 13.sp))
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
