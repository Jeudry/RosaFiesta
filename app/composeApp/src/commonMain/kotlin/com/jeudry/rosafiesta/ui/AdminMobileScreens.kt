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
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeudry.rosafiesta.designsystem.LocalRfTypography
import com.jeudry.rosafiesta.designsystem.RfIcon
import com.jeudry.rosafiesta.designsystem.RfIconKind
import com.jeudry.rosafiesta.designsystem.RfLogo
import com.jeudry.rosafiesta.designsystem.RfLogoSize
import com.jeudry.rosafiesta.designsystem.RfPhoto
import com.jeudry.rosafiesta.designsystem.RfPhotoKey
import com.jeudry.rosafiesta.designsystem.RosaAvatar
import com.jeudry.rosafiesta.designsystem.RosaAvatarSize
import com.jeudry.rosafiesta.designsystem.rfCardShadow
import com.jeudry.rosafiesta.designsystem.rfHeroBannerShadow
import com.jeudry.rosafiesta.designsystem.rfIconButtonShadow
import com.jeudry.rosafiesta.designsystem.rfInputShadow
import com.jeudry.rosafiesta.designsystem.rfPrimaryButtonShadow
import com.jeudry.rosafiesta.domain.CanonicalSampleData
import com.jeudry.rosafiesta.domain.EventStatus

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
private fun AdminHeroWeddingRingsIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val s = size.width / 120f
        scale(s, s, Offset.Zero) {
            // Circle 1: cx="49" cy="70" r="21" stroke="#D9A441" stroke-width="6.5"
            drawCircle(
                color = Color(0xFFD9A441),
                radius = 21f,
                center = Offset(49f, 70f),
                style = Stroke(width = 6.5f),
            )
            // Circle 2: cx="72" cy="72" r="21" stroke="#F0CC7A" stroke-width="6.5"
            drawCircle(
                color = Color(0xFFF0CC7A),
                radius = 21f,
                center = Offset(72f, 72f),
                style = Stroke(width = 6.5f),
            )
            // Overlapping arc: M63.5 52.6A21 21 0 0 1 69 62
            val arcPath = PathParser().parsePathString("M63.5 52.6A21 21 0 0 1 69 62").toPath()
            drawPath(arcPath, color = Color(0xFFD9A441), style = Stroke(width = 6.5f, cap = StrokeCap.Round))

            // Diamond on ring: M42 44L49 37L56 44L49 53Z
            val diamondPath = PathParser().parsePathString("M42 44L49 37L56 44L49 53Z").toPath()
            drawPath(diamondPath, color = Color(0xFFA8D8F5))
            drawPath(diamondPath, color = Color.White, style = Stroke(width = 1.6f, join = StrokeJoin.Round))

            // Star 1: M90 30l2 5 5 2-5 2-2 5-2-5-5-2 5-2z
            val star1 = PathParser().parsePathString("M90 30l2 5 5 2-5 2-2 5-2-5-5-2 5-2z").toPath()
            drawPath(star1, color = Color(0xFFEC4F97))

            // Star 2: M27 34l1.4 3.6 3.6 1.4-3.6 1.4-1.4 3.6-1.4-3.6-3.6-1.4 3.6-1.4z
            val star2 = PathParser().parsePathString("M27 34l1.4 3.6 3.6 1.4-3.6 1.4-1.4 3.6-1.4-3.6-3.6-1.4 3.6-1.4z").toPath()
            drawPath(star2, color = Color(0xFF7650E8))

            // Sparkle: M96 96c-2-3-6-2-6 1 0 3 6 6 6 6s6-3 6-6c0-3-4-4-6-1z
            val sparkle = PathParser().parsePathString("M96 96c-2-3-6-2-6 1 0 3 6 6 6 6s6-3 6-6c0-3-4-4-6-1z").toPath()
            drawPath(sparkle, color = Color(0xFFF9B6D3))
        }
    }
}

@Composable
internal fun RenderAdminHomeA01(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val metrics = viewModel?.metrics?.collectAsState()?.value
    val allPayments = viewModel?.payments?.collectAsState()?.value ?: emptyList()
    val allEvents = viewModel?.events?.collectAsState()?.value ?: emptyList()

    val totalOctoberRevenueDop = if (allPayments.isNotEmpty()) {
        allPayments.sumOf { it.amountDop }
    } else {
        metrics?.octoberRevenueDop ?: 12450
    }

    val activeEventsCount = if (allEvents.isNotEmpty()) {
        allEvents.count { it.status != EventStatus.COMPLETED && it.status != EventStatus.CANCELLED }
    } else {
        metrics?.activeEventsCount ?: 4
    }

    val newRequestsCount = metrics?.newRequestsCount ?: 2

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 116.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().height(44.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .rfIconButtonShadow(CircleShape, isAdmin = true)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Color(0x24EA4EA9), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                RfLogo(size = RfLogoSize.MEDIUM)
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    BasicText(
                        text = "Rosa Fiesta",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)),
                    )
                    Box(
                        modifier = Modifier
                            .height(20.dp)
                            .background(Color(0xFFECEBFF), CircleShape)
                            .padding(horizontal = 7.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = "Admin",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B47D6)),
                        )
                    }
                }
                BasicText(
                    text = "Hola, María",
                    style = TextStyle(fontFamily = typo.displayFamily, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729), lineHeight = 24.sp),
                )
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clickable { onSelectScreenCode("A29") },
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .rfIconButtonShadow(CircleShape, isAdmin = true)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BELL, tint = Color(0xFF151729), size = 22.dp)
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 3.dp, y = (-3).dp)
                        .defaultMinSize(minWidth = 20.dp)
                        .height(20.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFD45C))
                        .padding(horizontal = 5.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = "3",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2E1A47)),
                    )
                }
            }
        }

        // Hero Card Boda Real
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(132.dp)
                .rfHeroBannerShadow(RoundedCornerShape(24.dp), isAdmin = true)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        0.0f to Color(0xFF7C5CF0),
                        0.55f to Color(0xFF5B6CF0),
                        1.0f to Color(0xFF2FB8C8),
                    ),
                )
                .clickable { onSelectScreenCode("A11") },
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val scale = size.width / 350f
                scale(scale, scale, Offset.Zero) {
                    drawCircle(color = Color.White.copy(alpha = 0.1f), radius = 70f, center = Offset(300f, 20f))
                    drawCircle(color = Color(0xFFD6EA8C), radius = 4f, center = Offset(190f, 52f))
                    val starPath = PathParser().parsePathString("M0 -10L2.35 -3.24L9.5 -3.1L3.8 1.24L5.9 8.1L0 4L-5.9 8.1L-3.8 1.24L-9.5 -3.1L-2.35 -3.24Z").toPath()
                    translate(214f, 30f) {
                        scale(0.8f, 0.8f, Offset.Zero) {
                            drawPath(starPath, color = Color(0xFFFFD45C))
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 16.dp, y = 18.dp)
                    .size(140.dp),
            ) {
                AdminHeroWeddingRingsIllustration(modifier = Modifier.fillMaxSize())
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.Bottom,
            ) {
                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFD45C))
                        .padding(horizontal = 9.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = "Este viernes · en 3 días",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF4A3464)),
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                BasicText(
                    text = "Boda Real Rosa & Dorado",
                    modifier = Modifier.widthIn(max = 210.dp),
                    style = TextStyle(fontFamily = typo.displayFamily, fontSize = 21.sp, fontWeight = FontWeight.Bold, color = Color.White, lineHeight = 23.sp),
                )

                Spacer(modifier = Modifier.height(2.dp))

                BasicText(
                    text = "Montaje 2:00 p. m. · Jardín Botánico",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Normal, color = Color(0xFFE9EEFF)),
                )
            }
        }

        // 3 KPI Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    BasicText("Ingresos oct", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF5F6377)))
                    BasicText("RD$ $totalOctoberRevenueDop", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E3048)))
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    BasicText("Eventos activos", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF5F6377)))
                    BasicText("$activeEventsCount", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E3048)))
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFFFF2C9))
                    .clickable { onSelectScreenCode("A02") }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    BasicText("Solicitudes", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF7A5700)))
                    BasicText("$newRequestsCount nuevas", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7A5700)))
                }
            }
        }

        // Section CREAR
        BasicText(
            text = "CREAR",
            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.96.sp, color = Color(0xFF5F6377)),
            modifier = Modifier.padding(top = 2.dp),
        )

        Column(
            modifier = Modifier.fillMaxWidth().offset(y = (-6).dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Crear con Rosa IA
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFFE9E8FF), Color(0xFFDDF2F6))))
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                        .clickable { onSelectScreenCode("A08") }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        RosaAvatar(size = RosaAvatarSize.MEDIUM, customSize = 36.dp, forceAdminPalette = true)
                        Column(modifier = Modifier.weight(1f)) {
                            BasicText("Crear con Rosa IA", maxLines = 1, softWrap = false, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E324A)))
                            BasicText("Arma el borrador", maxLines = 1, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)))
                        }
                    }
                }

                // Crear manual
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                        .clickable { onSelectScreenCode("A05") }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFECEBFF)),
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.EDIT, tint = Color(0xFF4B47D6), size = 20.dp)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            BasicText("Crear manual", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E324A), lineHeight = 17.sp))
                            BasicText("Paso a paso", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)))
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Agregar artículo
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                        .clickable { onSelectScreenCode("A10") }
                        .padding(12.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFEEF6D6)),
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.BOX, tint = Color(0xFF4E6A10), size = 20.dp)
                        }
                        BasicText("Agregar\nartículo", modifier = Modifier.weight(1f), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E324A), lineHeight = 17.sp))
                    }
                }

                // Solicitudes
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                        .clickable { onSelectScreenCode("A02") }
                        .padding(12.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFFFF2C9)),
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.ORDERS, tint = Color(0xFF7A5700), size = 20.dp)
                        }
                        BasicText(
                            text = "Solicitudes",
                            modifier = Modifier.weight(1f, fill = false),
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E324A)),
                            maxLines = 1,
                            softWrap = false,
                        )
                        Box(
                            modifier = Modifier
                                .defaultMinSize(minWidth = 22.dp)
                                .height(22.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFD45C))
                                .padding(horizontal = 6.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText("2", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2E1A47)))
                        }
                    }
                }
            }
        }

        // Section POR ATENDER
        BasicText(
            text = "POR ATENDER",
            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.96.sp, color = Color(0xFF5F6377)),
            modifier = Modifier.padding(top = 2.dp),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-6).dp)
                .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp)),
        ) {
            Column {
                // Pago restante
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectScreenCode("A20") }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFFFF2C9)),
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.WALLET, tint = Color(0xFF7A5700), size = 20.dp)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        BasicText("Pago restante · Boda Real", maxLines = 1, overflow = TextOverflow.Ellipsis, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E324A)))
                        BasicText("Vence el jueves 22 oct", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)))
                    }
                    BasicText("RD$ 3,150", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)))
                }

                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))

                // Mesa dulce temática
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectScreenCode("A09") }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFFDE3EA)),
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.WARNING, tint = Color(0xFFDC2B48), size = 20.dp)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        BasicText("Mesa dulce temática", maxLines = 1, overflow = TextOverflow.Ellipsis, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E324A)))
                        BasicText("Quedan 2 · 1 reservada el 7 nov", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)))
                    }
                    Box(
                        modifier = Modifier
                            .height(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFDE3EA))
                            .padding(horizontal = 9.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText("Stock bajo", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, color = Color(0xFFDC2B48)))
                    }
                }
            }
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 116.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().height(44.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .rfIconButtonShadow(CircleShape, isAdmin = true)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), CircleShape)
                    .clickable { onSelectScreenCode("BACK:A01") },
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
            }

            Column(modifier = Modifier.weight(1f)) {
                BasicText(
                    text = "Solicitudes",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729), lineHeight = 22.sp),
                )
                BasicText(
                    text = "Cotizaciones que piden tus clientes",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)),
                )
            }
        }

        var selectedRequestTab by remember { mutableStateOf(0) }
        val requestTabs = listOf("Nuevas · 2", "En revisión · 1", "Enviadas")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFEDEDF5), RoundedCornerShape(14.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            requestTabs.forEachIndexed { tabIndex, tabTitle ->
                val isSelectedTab = selectedRequestTab == tabIndex
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .then(
                            if (isSelectedTab) {
                                Modifier
                                    .shadow(4.dp, RoundedCornerShape(12.dp), spotColor = Color(0x334650B4), ambientColor = Color.Transparent)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White)
                            } else {
                                Modifier.clip(RoundedCornerShape(12.dp))
                            },
                        )
                        .clickable { selectedRequestTab = tabIndex },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        tabTitle,
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = if (isSelectedTab) Color(0xFF2E324A) else Color(0xFF5F6377),
                        ),
                    )
                }
            }
        }

        val showMateoCard = selectedRequestTab != 2
        if (showMateoCard) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp)),
            ) {
                Column {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(RoundedCornerShape(14.dp)),
                        ) {
                            RfPhoto(photoKey = RfPhotoKey.CUMPLE_INFANTIL, contentDescription = "Cumpleaños infantil", modifier = Modifier.fillMaxSize())
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            BasicText("Cumpleaños de Mateo", maxLines = 1, overflow = TextOverflow.Ellipsis, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E324A)))
                            BasicText("Laura Peña · Sáb 7 nov", maxLines = 1, overflow = TextOverflow.Ellipsis, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(top = 2.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .height(24.dp)
                                        .background(Color(0xFFFDE6F2), CircleShape)
                                        .padding(horizontal = 9.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    BasicText("Hecha con Rosa IA", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFFC2338F)))
                                }
                                Box(
                                    modifier = Modifier
                                        .height(24.dp)
                                        .background(Color(0xFFF2F2F8), CircleShape)
                                        .padding(horizontal = 9.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    BasicText("40 invitados", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)))
                                }
                            }
                        }
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            BasicText("3 artículos", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)))
                            Row(verticalAlignment = Alignment.Bottom) {
                                BasicText("est. ", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)))
                                BasicText("RD$ 405", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)))
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEEF6D6))
                                .border(1.dp, Color(0xFFE0EDBF), CircleShape)
                                .clickable { onSelectScreenCode("A15") },
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.WHATSAPP, tint = Color(0xFF4E6A10), size = 18.dp)
                        }

                        Box(
                            modifier = Modifier
                                .height(40.dp)
                                .shadow(8.dp, CircleShape, spotColor = Color(0x665B6CF0), ambientColor = Color.Transparent)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
                                .clickable { onSelectScreenCode("A03") }
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                BasicText("Cotizar", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White))
                                RfIcon(kind = RfIconKind.ARROW_RIGHT, tint = Color.White, size = 16.dp)
                            }
                        }
                    }
                }
            }
        }

        val showGraduacionCard = selectedRequestTab != 1
        if (showGraduacionCard) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp)),
            ) {
                Column {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(RoundedCornerShape(14.dp)),
                        ) {
                            RfPhoto(photoKey = RfPhotoKey.GRADUACION, contentDescription = "Graduación", modifier = Modifier.fillMaxSize())
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            BasicText("Graduación Promo 2026", maxLines = 1, overflow = TextOverflow.Ellipsis, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E324A)))
                            BasicText("Ana Gómez · Vie 13 nov", maxLines = 1, overflow = TextOverflow.Ellipsis, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(top = 2.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .height(24.dp)
                                        .background(Color(0xFFECEBFF), CircleShape)
                                        .padding(horizontal = 9.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    BasicText("Desde el chat", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF4B47D6)))
                                }
                                Box(
                                    modifier = Modifier
                                        .height(24.dp)
                                        .background(Color(0xFFF2F2F8), CircleShape)
                                        .padding(horizontal = 9.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    BasicText("200 invitados", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)))
                                }
                            }
                        }
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            BasicText("5 artículos", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)))
                            Row(verticalAlignment = Alignment.Bottom) {
                                BasicText("est. ", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)))
                                BasicText("RD$ 2,140", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)))
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEEF6D6))
                                .border(1.dp, Color(0xFFE0EDBF), CircleShape)
                                .clickable { onSelectScreenCode("A15") },
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.WHATSAPP, tint = Color(0xFF4E6A10), size = 18.dp)
                        }

                        Box(
                            modifier = Modifier
                                .height(40.dp)
                                .shadow(8.dp, CircleShape, spotColor = Color(0x665B6CF0), ambientColor = Color.Transparent)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
                                .clickable { onSelectScreenCode("A03") }
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                BasicText("Cotizar", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White))
                                RfIcon(kind = RfIconKind.ARROW_RIGHT, tint = Color.White, size = 16.dp)
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            RfIcon(kind = RfIconKind.INFO, tint = Color(0xFF5F6377), size = 16.dp)
            BasicText(
                text = "Las solicitudes llegan desde Rosa IA o desde el chat del evento.",
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)),
            )
        }
    }
}

private fun formatAdminDop(amount: Int): String {
    if (amount < 1000) {
        return "RD$ $amount"
    }
    val thousands = amount / 1000
    val remainder = (amount % 1000).toString().padStart(3, '0')
    return "RD$ $thousands,$remainder"
}

// ==============================================================================
// A03 · COTIZADOR RÁPIDO ADMIN
// ==============================================================================
@Composable
internal fun RenderAdminQuoteBuilderA03(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    var articleQuantities by remember { mutableStateOf(listOf(1, 1, 40)) }
    var transportDopText by remember { mutableStateOf("250") }
    var quoteNote by remember { mutableStateOf("Incluye montaje 2 horas antes. El arco va en la entrada principal.") }

    val articlesSubtotal = articleQuantities[0] * 95 + articleQuantities[1] * 150 + articleQuantities[2] * 4
    val parsedTransportDop = transportDopText.filter { char -> char.isDigit() }.toIntOrNull() ?: 0
    val totalQuoteDop = articlesSubtotal + parsedTransportDop
    val activeArticleLinesCount = articleQuantities.count { quantity -> quantity > 0 }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().height(44.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = true)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), CircleShape)
                        .clickable { onSelectScreenCode("BACK:A02") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "Cotizar",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729), lineHeight = 22.sp),
                    )
                    BasicText(
                        text = "Cumpleaños de Mateo · Sáb 7 nov",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)),
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFDE6F2)),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText("L", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFC2338F)))
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        BasicText("Laura Peña", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E324A)))
                        BasicText("40 invitados · [Lugar del evento]", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)))
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .rfIconButtonShadow(CircleShape, isAdmin = true)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.dp, Color(0xFFECECF5), CircleShape)
                            .clickable { onSelectScreenCode("A15") },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.WHATSAPP, tint = Color(0xFF4E6A10), size = 20.dp)
                    }
                }
            }

            BasicText(
                text = "ARTÍCULOS",
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.96.sp, color = Color(0xFF5F6377)),
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-4).dp)
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp)),
            ) {
                Column {
                    val quoteArticles = listOf(
                        Triple(RfPhotoKey.ARCO_ROSA, "Arco de globos rosa", "RD$ 95" to ("Disponible" to articleQuantities[0])),
                        Triple(RfPhotoKey.MESA_HELADOS, "Mesa dulce temática", "RD$ 150" to ("1 libre de 2" to articleQuantities[1])),
                        Triple(RfPhotoKey.SILLA_TIFFANY, "Silla Tiffany Cristal", "RD$ 4" to ("80 libres" to articleQuantities[2])),
                    )

                    quoteArticles.forEachIndexed { index, (photo, title, meta) ->
                        val (price, tagAndQty) = meta
                        val (tagText, qty) = tagAndQty
                        if (index > 0) {
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                            ) {
                                RfPhoto(photoKey = photo, contentDescription = title, modifier = Modifier.fillMaxSize())
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                BasicText(title, maxLines = 1, overflow = TextOverflow.Ellipsis, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E324A)))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .height(28.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        BasicText(price, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Normal, color = Color(0xFF2E324A)))
                                    }

                                    Box(
                                        modifier = Modifier
                                            .height(22.dp)
                                            .background(Color(0xFFEEF6D6), CircleShape)
                                            .padding(horizontal = 8.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        BasicText(tagText, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF4E6A10)))
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .height(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF2F2F8))
                                    .padding(horizontal = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            val updatedList = articleQuantities.toMutableList()
                                            updatedList[index] = (updatedList[index] - 1).coerceAtLeast(1)
                                            articleQuantities = updatedList
                                        },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    RfIcon(kind = RfIconKind.MINUS, tint = Color(0xFF5B6CF0), size = 14.dp)
                                }
                                BasicText(
                                    text = "$qty",
                                    modifier = Modifier.defaultMinSize(minWidth = 22.dp),
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E324A)),
                                )
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            val updatedList = articleQuantities.toMutableList()
                                            updatedList[index] = updatedList[index] + 1
                                            articleQuantities = updatedList
                                        },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    RfIcon(kind = RfIconKind.PLUS, tint = Color(0xFF5B6CF0), size = 14.dp)
                                }
                            }
                        }
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .clickable { onSelectScreenCode("A09") },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        RfIcon(kind = RfIconKind.PLUS, tint = Color(0xFF4B47D6), size = 16.dp)
                        Spacer(Modifier.width(6.dp))
                        BasicText("Agregar del inventario", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B47D6)))
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .rfInputShadow(RoundedCornerShape(14.dp), isAdmin = true)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    BasicText("Transporte e instalación", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E324A)))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        BasicText("RD$", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Normal, color = Color(0xFF2E324A)))
                        BasicTextField(
                            value = transportDopText,
                            onValueChange = { updatedText -> transportDopText = updatedText },
                            singleLine = true,
                            textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Normal, color = Color(0xFF2E324A)),
                            modifier = Modifier.widthIn(min = 32.dp, max = 64.dp),
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 70.dp)
                    .rfInputShadow(RoundedCornerShape(14.dp), isAdmin = true)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                BasicTextField(
                    value = quoteNote,
                    onValueChange = { updatedText -> quoteNote = updatedText },
                    textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF151729), lineHeight = 19.sp),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.White)
                .border(1.dp, Color(0xFFECECF5))
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Column {
                    BasicText("Total · $activeArticleLinesCount artículos", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
                    BasicText(formatAdminDop(totalQuoteDop), style = TextStyle(fontFamily = typo.displayFamily, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)))
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = true)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
                        .clickable { onSelectScreenCode("A02") },
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        RfIcon(kind = RfIconKind.SEND, tint = Color.White, size = 20.dp)
                        BasicText("Enviar cotización", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White))
                    }
                }
            }
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
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todos") }
    val categoryChips = remember { listOf("Todos", "Mobiliario", "Floral", "Globos", "Mesa dulce") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 116.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(44.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                BasicText(
                    text = "Inventario",
                    style = TextStyle(fontFamily = typo.displayFamily, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A)),
                )
                BasicText(
                    text = "15 artículos · 6 categorías",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)),
                )
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .rfIconButtonShadow(CircleShape, isAdmin = true)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), CircleShape)
                    .clickable { onSelectScreenCode("A11") },
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.CALENDAR, tint = Color(0xFF151729), size = 20.dp)
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .shadow(8.dp, CircleShape, spotColor = Color(0x665B6CF0), ambientColor = Color.Transparent)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
                    .clickable { onSelectScreenCode("A10") },
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.PLUS, tint = Color.White, size = 22.dp)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(14.dp))
                .padding(start = 14.dp, end = 6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                RfIcon(kind = RfIconKind.SEARCH, tint = Color(0xFF5F6377), size = 18.dp)
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (searchQuery.isEmpty()) {
                        BasicText(
                            text = "Buscar artículo",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF5F6377)),
                        )
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { updatedText -> searchQuery = updatedText },
                        singleLine = true,
                        textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF2E324A)),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFECEBFF))
                        .clickable {
                            searchQuery = ""
                            selectedCategory = "Todos"
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.FILTER, tint = Color(0xFF4B47D6), size = 18.dp)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            categoryChips.forEach { categoryLabel ->
                val isCategorySelected = selectedCategory == categoryLabel
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .clip(CircleShape)
                        .then(
                            if (isCategorySelected) {
                                Modifier.background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
                            } else {
                                Modifier.background(Color.White).border(1.dp, Color(0xFFE9E9F2), CircleShape)
                            },
                        )
                        .clickable { selectedCategory = categoryLabel }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        categoryLabel,
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 14.sp,
                            fontWeight = if (isCategorySelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isCategorySelected) Color.White else Color(0xFF2E324A),
                        ),
                    )
                }
            }
        }

        val articles = viewModel?.articles?.collectAsState()?.value ?: CanonicalSampleData.articles
        val filteredArticles = articles.filter { article ->
            val matchesCategory = when (selectedCategory) {
                "Todos" -> true
                "Mobiliario" -> article.category.contains("Mobiliario", ignoreCase = true) || article.category.contains("Sillas", ignoreCase = true) || article.category.contains("Mesas", ignoreCase = true)
                "Floral" -> article.category.contains("Flor", ignoreCase = true) || article.name.contains("floral", ignoreCase = true)
                "Globos" -> article.category.contains("Globo", ignoreCase = true) || article.name.contains("globo", ignoreCase = true) || article.name.contains("arco", ignoreCase = true)
                "Mesa dulce" -> article.category.contains("Dulce", ignoreCase = true) || article.category.contains("Estaciones", ignoreCase = true) || article.name.contains("helado", ignoreCase = true)
                else -> true
            }
            val matchesSearch = searchQuery.isBlank() || article.name.contains(searchQuery.trim(), ignoreCase = true) || article.category.contains(searchQuery.trim(), ignoreCase = true)
            matchesCategory && matchesSearch
        }
        val disponiblesCount = articles.count { article -> article.availableOct23 > 5 }
        val reservadosCount = articles.count { article -> article.totalStock - article.availableOct23 > 0 }
        val stockBajoCount = articles.count { article -> article.availableOct23 in 1..5 || article.totalStock <= 5 }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFEEF6D6))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                Column {
                    BasicText(disponiblesCount.toString(), style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF3F560C)))
                    BasicText("Disponibles", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF4E6A10)))
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFECEBFF))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                Column {
                    BasicText(reservadosCount.toString(), style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4B47D6)))
                    BasicText("Reservados hoy", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF4B47D6)))
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFFDE3EA))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                Column {
                    BasicText(stockBajoCount.toString(), style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFDC2B48)))
                    BasicText("Stock bajo", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, color = Color(0xFFDC2B48)))
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp)),
        ) {
            Column {
                filteredArticles.forEachIndexed { index, article ->
                    if (index > 0) {
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))
                    }
                    val photoKey = RfPhotoKey.entries.firstOrNull { entry -> entry.assetKey == article.photo.key || entry.name.equals(article.photo.key, ignoreCase = true) } ?: RfPhotoKey.SILLA_TIFFANY
                    val isStockBajo = article.availableOct23 in 1..5 || article.totalStock <= 5
                    val tagText = if (isStockBajo) "Stock bajo" else "Activo"
                    val tagBg = if (isStockBajo) Color(0xFFFDE3EA) else Color(0xFFEEF6D6)
                    val tagColor = if (isStockBajo) Color(0xFFDC2B48) else Color(0xFF4E6A10)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel?.soundPlayer?.playTap()
                                viewModel?.selectArticle(article.id)
                                onSelectScreenCode("A23")
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(12.dp)),
                        ) {
                            RfPhoto(photoKey = photoKey, contentDescription = article.name, modifier = Modifier.fillMaxSize())
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(3.dp),
                        ) {
                            BasicText(article.name, maxLines = 1, overflow = TextOverflow.Ellipsis, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E324A)))
                            BasicText("${article.totalStock} unid. · RD$ ${article.pricePerDayDop}/día", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
                        }

                        Box(
                            modifier = Modifier
                                .height(24.dp)
                                .background(tagBg, CircleShape)
                                .padding(horizontal = 9.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(tagText, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = tagColor))
                        }
                    }
                }
            }
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
    var selectedDayNum by remember { mutableStateOf("20") }
    var selectedEventSegment by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 116.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(44.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                BasicText(
                    text = "Eventos",
                    style = TextStyle(fontFamily = typo.displayFamily, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A)),
                )
                BasicText(
                    text = "Octubre 2026 · 4 próximos",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)),
                )
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .rfIconButtonShadow(CircleShape, isAdmin = true)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), CircleShape)
                    .clickable { onSelectScreenCode("s63") },
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.CALENDAR, tint = Color(0xFF151729), size = 20.dp)
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .shadow(8.dp, CircleShape, spotColor = Color(0x665B6CF0), ambientColor = Color.Transparent)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
                    .clickable { onSelectScreenCode("A04") },
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.PLUS, tint = Color.White, size = 22.dp)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            val days = listOf(
                Triple("L", "19", null),
                Triple("M", "20", null),
                Triple("X", "21", null),
                Triple("J", "22", Color(0xFFB9D84A)),
                Triple("V", "23", Color(0xFFFFD45C)),
                Triple("S", "24", null),
                Triple("D", "25", null),
            )

            days.forEach { (day, num, dotColor) ->
                val isSelected = selectedDayNum == num
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .then(
                            if (isSelected) {
                                Modifier.background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
                            } else {
                                Modifier.background(Color.White).border(1.dp, Color(0xFFECECF5), RoundedCornerShape(16.dp))
                            },
                        )
                        .clickable { selectedDayNum = num },
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        BasicText(day, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White.copy(alpha = 0.8f) else Color(0xFF5F6377)))
                        BasicText(num, style = TextStyle(fontFamily = typo.displayFamily, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = if (isSelected) Color.White else Color(0xFF151729)))
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(dotColor ?: Color.Transparent),
                        )
                    }
                }
            }
        }

        val eventSegments = listOf("Próximos · 4", "Por cobrar · 2", "Pasados")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFEDEDF5), RoundedCornerShape(14.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            eventSegments.forEachIndexed { segmentIndex, segmentTitle ->
                val isSegmentSelected = selectedEventSegment == segmentIndex
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSegmentSelected) Color.White else Color.Transparent)
                        .clickable { selectedEventSegment = segmentIndex },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        segmentTitle,
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = if (isSegmentSelected) Color(0xFF2E324A) else Color(0xFF5F6377),
                        ),
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            val allEvents = listOf(
                Quadruple(RfPhotoKey.BODA, "Boda Real Rosa & Dorado", "Vie 23 oct · Camila R.", Triple("Ajustado", Color(0xFFDDF4F3), Color(0xFF1F8A84)) to 0.3f),
                Quadruple(RfPhotoKey.QUINCEANERA, "Quinceañera Valentina Crystal", "Lun 2 nov · Camila R.", Triple("Confirmado", Color(0xFFEEF6D6), Color(0xFF4E6A10)) to 0.3f),
                Quadruple(RfPhotoKey.CUMPLE_INFANTIL, "Cumpleaños de Mateo", "Sáb 7 nov · Laura Peña", Triple("Cotización enviada", Color(0xFFFFF2C9), Color(0xFF7A5700)) to 0.0f),
                Quadruple(RfPhotoKey.BABY_SHOWER, "Baby Shower Sofía en Rosas", "Dom 15 nov · Rosa Gómez", Triple("Borrador", Color(0xFFF2F2F8), Color(0xFF5F6377)) to 0.0f),
            )
            val eventList = when (selectedEventSegment) {
                1 -> allEvents.take(2)

                2 -> listOf(
                    Quadruple(RfPhotoKey.CORPORATIVO, "Gala Corporativa Grupo Caribe", "Lun 12 oct · Grupo Caribe", Triple("Cerrado", Color(0xFFEEF6D6), Color(0xFF4E6A10)) to 1.0f),
                )

                else -> allEvents
            }

            eventList.forEach { (photo, title, subtitle, meta) ->
                val (tagTriple, progress) = meta
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                        .clickable { onSelectScreenCode("A13") }
                        .padding(10.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .clip(RoundedCornerShape(14.dp)),
                        ) {
                            RfPhoto(photoKey = photo, contentDescription = title, modifier = Modifier.fillMaxSize())
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            BasicText(title, maxLines = 1, overflow = TextOverflow.Ellipsis, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E324A)))
                            BasicText(subtitle, maxLines = 1, overflow = TextOverflow.Ellipsis, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .height(22.dp)
                                        .background(tagTriple.second, CircleShape)
                                        .padding(horizontal = 8.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    BasicText(tagTriple.first, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = tagTriple.third))
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(5.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(Color(0xFFEDEDF5)),
                                ) {
                                    if (progress > 0f) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(progress)
                                                .height(5.dp)
                                                .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0)))),
                                        )
                                    }
                                }

                                BasicText(
                                    text = "${(progress * 100).toInt()}%",
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5F6377)),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

// ==============================================================================
// A12 · MÁS ADMIN
// ==============================================================================
@Composable
internal fun RenderAdminMoreA12(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 116.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BasicText(
            text = "Más",
            style = TextStyle(fontFamily = typo.displayFamily, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A)),
        )

        // Profile Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                .clickable { onSelectScreenCode("A27") }
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0x24EA4EA9), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    RfLogo(size = RfLogoSize.MEDIUM)
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText("María · Dueña", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E324A)))
                    BasicText("admin@rosafiesta.com", maxLines = 1, overflow = TextOverflow.Ellipsis, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)))
                }

                RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = Color(0xFF5F6377), size = 20.dp)
            }
        }

        // 15 Menu Grid Tiles (3 columns)
        val tiles = listOf(
            Triple("Chats", RfIconKind.CHAT, "A14" to (Color(0xFFDDF4F3) to Color(0xFF1F8A84))),
            Triple("Pagos", RfIconKind.WALLET, "A20" to (Color(0xFFEEF6D6) to Color(0xFF4E6A10))),
            Triple("Clientes", RfIconKind.USER, "A26" to (Color(0xFFFDE6F2) to Color(0xFFC2338F))),
            Triple("Cotizaciones", RfIconKind.ORDERS, "A22" to (Color(0xFFFFF2C9) to Color(0xFF7A5700))),
            Triple("Paquetes", RfIconKind.GIFT, "A24" to (Color(0xFFECEBFF) to Color(0xFF4B47D6))),
            Triple("Panel", RfIconKind.TREND_UP, "s58" to (Color(0xFFECEBFF) to Color(0xFF4B47D6))),
            Triple("Reportes", RfIconKind.TREND_UP, "s67" to (Color(0xFFDDF4F3) to Color(0xFF1F8A84))),
            Triple("Leads", RfIconKind.LOCATION, "s64" to (Color(0xFFFDE6F2) to Color(0xFFC2338F))),
            Triple("Proveedores", RfIconKind.TRUCK, "s55" to (Color(0xFFEEF6D6) to Color(0xFF4E6A10))),
            Triple("Reseñas", RfIconKind.STAR, "s71" to (Color(0xFFFFF2C9) to Color(0xFF7A5700))),
            Triple("Bot WhatsApp", RfIconKind.WHATSAPP, "s72" to (Color(0xFFEEF6D6) to Color(0xFF4E6A10))),
            Triple("Equipo", RfIconKind.USER, "A28" to (Color(0xFFECEBFF) to Color(0xFF4B47D6))),
            Triple("Cancelaciones", RfIconKind.WARNING, "A32" to (Color(0xFFFCE3E8) to Color(0xFFB42345))),
            Triple("Calendario", RfIconKind.CALENDAR, "s63" to (Color(0xFFDDF4F3) to Color(0xFF1F8A84))),
            Triple("Versión web", RfIconKind.BOX, "A39" to (Color(0xFFF2F2F8) to Color(0xFF5F6377))),
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            tiles.chunked(3).forEach { rowTiles ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    rowTiles.forEach { (label, icon, meta) ->
                        val (screen, colors) = meta
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White)
                                .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                                .clickable { onSelectScreenCode(screen) }
                                .padding(vertical = 9.dp, horizontal = 6.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(5.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(colors.first),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    RfIcon(kind = icon, tint = colors.second, size = 18.dp)
                                }
                                BasicText(
                                    text = label,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E324A)),
                                )
                            }
                        }
                    }
                    // Fill remaining columns if not full row
                    repeat(3 - rowTiles.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Settings section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp)),
        ) {
            Column {
                // Ajustes del negocio
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectScreenCode("A31") }
                        .padding(horizontal = 14.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(modifier = Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFFF2F2F8)), contentAlignment = Alignment.Center) {
                        RfIcon(kind = RfIconKind.FILTER, tint = Color(0xFF5F6377), size = 18.dp)
                    }
                    BasicText("Ajustes del negocio", modifier = Modifier.weight(1f), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E324A)))
                    RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = Color(0xFF5F6377), size = 18.dp)
                }

                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))

                // Ver la app como cliente
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectScreenCode("s14") }
                        .padding(horizontal = 14.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(modifier = Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFFFDE6F2)), contentAlignment = Alignment.Center) {
                        RfIcon(kind = RfIconKind.LOCATION, tint = Color(0xFFC2338F), size = 18.dp)
                    }
                    BasicText("Ver la app como cliente", modifier = Modifier.weight(1f), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E324A)))
                    RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = Color(0xFF5F6377), size = 18.dp)
                }

                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))

                // Cerrar sesión
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectScreenCode("A30") }
                        .padding(horizontal = 14.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(modifier = Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFFFDE3EA)), contentAlignment = Alignment.Center) {
                        RfIcon(kind = RfIconKind.LOGOUT, tint = Color(0xFFDC2B48), size = 18.dp)
                    }
                    BasicText("Cerrar sesión", modifier = Modifier.weight(1f), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFDC2B48)))
                }
            }
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

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp, start = 20.dp, end = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = true)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), CircleShape)
                        .clickable { onSelectScreenCode("BACK:A12") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText("Pagos", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A)))
                    BasicText("Octubre 2026", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)))
                }
            }

            // 2 KPI cards
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0), Color(0xFF2FB8C8))))
                        .padding(14.dp),
                ) {
                    Column {
                        BasicText("Cobrado este mes", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFE9EEFF)))
                        Spacer(modifier = Modifier.height(4.dp))
                        BasicText("RD$ 12,450", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White))
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFFFF2C9))
                        .padding(14.dp),
                ) {
                    Column {
                        BasicText("Por cobrar", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF7A5700)))
                        Spacer(modifier = Modifier.height(4.dp))
                        BasicText("RD$ 7,724", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF7A5700)))
                    }
                }
            }

            var selectedPaymentTab by remember { mutableStateOf(0) }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFEDEDF5))
                    .padding(4.dp),
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selectedPaymentTab == 0) Color.White else Color.Transparent)
                            .clickable { selectedPaymentTab = 0 },
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText("Por cobrar · 3", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Normal, color = if (selectedPaymentTab == 0) Color(0xFF2E324A) else Color(0xFF5F6377)))
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selectedPaymentTab == 1) Color.White else Color.Transparent)
                            .clickable { selectedPaymentTab = 1 },
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText("Recibidos", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Normal, color = if (selectedPaymentTab == 1) Color(0xFF2E324A) else Color(0xFF5F6377)))
                    }
                }
            }

            if (selectedPaymentTab == 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp)),
                ) {
                    Column {
                        var remindedIndices by remember { mutableStateOf(emptySet<Int>()) }
                        val pending = listOf(
                            Triple("C" to Color(0xFFFDE6F2) to Color(0xFFC2338F), "Boda Real · Camila R." to ("Vence el jue 22 oct" to Color(0xFFB42345)), "RD$ 3,150"),
                            Triple("C" to Color(0xFFFDE6F2) to Color(0xFFC2338F), "Quinceañera · Camila R." to ("Vence el vie 30 oct" to Color(0xFF7A5700)), "RD$ 4,340"),
                            Triple("L" to Color(0xFFEEF6D6) to Color(0xFF4E6A10), "Cumpleaños de Mateo · Laura P." to ("Anticipo para reservar" to Color(0xFF5F6377)), "RD$ 234"),
                        )

                        pending.forEachIndexed { idx, (avatar, info, amount) ->
                            val isReminded = idx in remindedIndices
                            if (idx > 0) {
                                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))
                            }
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
                                        .clip(CircleShape)
                                        .background(avatar.first.second),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    BasicText(avatar.first.first, style = TextStyle(fontFamily = typo.displayFamily, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = avatar.second))
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    BasicText(info.first, maxLines = 1, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E324A)))
                                    BasicText(info.second.first, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, color = info.second.second))
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    BasicText(amount, style = TextStyle(fontFamily = typo.displayFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)))
                                    Row(
                                        modifier = Modifier.clickable {
                                            remindedIndices = if (isReminded) remindedIndices - idx else remindedIndices + idx
                                        },
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        RfIcon(kind = if (isReminded) RfIconKind.CHECK else RfIconKind.WHATSAPP, tint = if (isReminded) Color(0xFF4E6A10) else Color(0xFF4B47D6), size = 13.dp)
                                        BasicText(
                                            if (isReminded) "Recordado" else "Recordar",
                                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = if (isReminded) Color(0xFF4E6A10) else Color(0xFF4B47D6)),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            BasicText("RECIBIDOS RECIENTES", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5F6377), letterSpacing = 1.sp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp)),
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFEEF6D6)),
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.CHECK, tint = Color(0xFF4E6A10), size = 18.dp)
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            BasicText("Camila Rodríguez · anticipo", maxLines = 1, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)))
                            BasicText("Transferencia · Banco Popular · 18 oct", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
                        }

                        BasicText("+RD$ 1,350", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4E6A10)))
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFEEF6D6)),
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.CHECK, tint = Color(0xFF4E6A10), size = 18.dp)
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            BasicText("Camila R. · anticipo quinceañera", maxLines = 1, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)))
                            BasicText("Efectivo · 15 oct", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
                        }

                        BasicText("+RD$ 1,860", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4E6A10)))
                    }
                }
            }

            Spacer(modifier = Modifier.height(90.dp))
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 36.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = true)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AdminButtonGradient)
                    .clickable { onSelectScreenCode("A21") },
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RfIcon(kind = RfIconKind.PLUS, tint = Color.White, size = 20.dp)
                    BasicText("Registrar pago", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White))
                }
            }
        }
    }
}

// ==============================================================================
// A21 · REGISTRAR PAGO ADMIN (MODAL DIALOG OVER A20)
// ==============================================================================
@Composable
internal fun RenderAdminRecordPaymentA21(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val eventPaymentOptions = remember {
        listOf(
            Triple("C", "Boda Real · Camila R.", "RD$ 3,150"),
            Triple("C", "Quinceañera · Camila R.", "RD$ 4,340"),
            Triple("L", "Cumpleaños de Mateo · Laura P.", "RD$ 234"),
        )
    }
    var selectedEventIndex by remember { mutableStateOf(0) }
    var selectedMethod by remember { mutableStateOf("Transferencia") }
    var bankName by remember { mutableStateOf("Banco Popular") }
    var paymentDate by remember { mutableStateOf("Hoy, 20 oct") }
    var receiptAttached by remember { mutableStateOf(false) }
    var sendWhatsAppReceipt by remember { mutableStateOf(true) }

    val activeEventOption = eventPaymentOptions[selectedEventIndex % eventPaymentOptions.size]

    Box(modifier = Modifier.fillMaxSize()) {
        RenderAdminPaymentsA20(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x73141632))
                .clickable { onSelectScreenCode("A20") },
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Color.White),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 12.dp, start = 20.dp, end = 20.dp, bottom = 34.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(width = 40.dp, height = 5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFFE3E4EF)),
                )

                BasicText(
                    text = "Registrar pago",
                    style = TextStyle(fontFamily = typo.displayFamily, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A)),
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(14.dp))
                        .clickable { selectedEventIndex += 1 }
                        .padding(start = 8.dp, end = 14.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFDE6F2)),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(activeEventOption.first, style = TextStyle(fontFamily = typo.displayFamily, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFC2338F)))
                        }

                        BasicText(activeEventOption.second, modifier = Modifier.weight(1f), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Normal, color = Color(0xFF2E324A)))

                        RfIcon(kind = RfIconKind.ARROW_RIGHT, tint = Color(0xFF5F6377), size = 18.dp)
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    BasicText("Monto", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)))
                    BasicText(activeEventOption.third, style = TextStyle(fontFamily = typo.displayFamily, fontSize = 40.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729), lineHeight = 44.sp))
                    BasicText("Salda el evento", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4E6A10)))
                }

                val paymentMethods = listOf("Transferencia", "Efectivo", "Tarjeta")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    paymentMethods.forEach { methodLabel ->
                        val isMethodSelected = selectedMethod == methodLabel
                        Box(
                            modifier = Modifier
                                .height(36.dp)
                                .clip(CircleShape)
                                .then(
                                    if (isMethodSelected) {
                                        Modifier.background(AdminButtonGradient)
                                    } else {
                                        Modifier.background(Color.White).border(1.dp, Color(0xFFE9E9F2), CircleShape)
                                    },
                                )
                                .clickable { selectedMethod = methodLabel }
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(
                                methodLabel,
                                style = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontSize = 14.sp,
                                    fontWeight = if (isMethodSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isMethodSelected) Color.White else Color(0xFF2E324A),
                                ),
                            )
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        BasicTextField(
                            value = bankName,
                            onValueChange = { updatedText -> bankName = updatedText },
                            singleLine = true,
                            textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Normal, color = Color(0xFF2E324A)),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    var showPaymentDatePicker by remember { mutableStateOf(false) }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(14.dp))
                            .padding(start = 10.dp, end = 6.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        RfMaskedDateField(
                            value = paymentDate,
                            onValueChange = { updatedText -> paymentDate = updatedText },
                            onOpenCalendar = { showPaymentDatePicker = true },
                            placeholder = "DD/MM/AAAA",
                            isAdmin = true,
                            compactInline = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    if (showPaymentDatePicker) {
                        RfInteractiveCalendarModal(
                            title = "Fecha del pago",
                            currentDateText = paymentDate,
                            minYear = 2024,
                            maxYear = 2030,
                            defaultYear = 2026,
                            isAdmin = true,
                            onDismiss = { showPaymentDatePicker = false },
                            onSelectDate = { formattedDate ->
                                paymentDate = formattedDate
                                showPaymentDatePicker = false
                            },
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (receiptAttached) Color(0xFFECEBFF) else Color.Transparent)
                        .border(2.dp, if (receiptAttached) Color(0xFF5B6CF0) else Color(0xFFC9CAE6), RoundedCornerShape(14.dp))
                        .clickable { receiptAttached = !receiptAttached },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = if (receiptAttached) "Comprobante-20oct.jpg adjunto" else "Adjuntar comprobante",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B47D6)),
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { sendWhatsAppReceipt = !sendWhatsAppReceipt }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BasicText("Enviar recibo a Camila ", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E324A)))
                        BasicText("por WhatsApp", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF5F6377)))
                    }

                    Box(
                        modifier = Modifier
                            .size(width = 46.dp, height = 28.dp)
                            .clip(CircleShape)
                            .background(if (sendWhatsAppReceipt) AdminButtonGradient else SolidColor(Color(0xFFDCDDEA)))
                            .padding(3.dp),
                        contentAlignment = if (sendWhatsAppReceipt) Alignment.CenterEnd else Alignment.CenterStart,
                    ) {
                        Box(modifier = Modifier.size(22.dp).clip(CircleShape).background(Color.White))
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = true)
                        .clip(RoundedCornerShape(16.dp))
                        .background(AdminButtonGradient)
                        .clickable {
                            viewModel?.submitPayment()
                            onSelectScreenCode("A20")
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText("Guardar pago", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White))
                }
            }
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
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp, start = 20.dp, end = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = true)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), CircleShape)
                        .clickable { onSelectScreenCode("BACK:A11") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText("Boda Real Rosa & Dorado", maxLines = 1, style = TextStyle(fontFamily = typo.displayFamily, fontSize = 19.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp, color = Color(0xFF2E324A)))
                    BasicText("Vie 23 oct · Jardín Botánico", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)))
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = true)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.MORE_VERTICAL, tint = Color(0xFF151729), size = 20.dp)
                }
            }

            // Hero Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(118.dp)
                    .clip(RoundedCornerShape(20.dp)),
            ) {
                RfPhoto(photoKey = RfPhotoKey.BODA, contentDescription = "Boda", modifier = Modifier.fillMaxSize())
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(0.3f to Color.Transparent, 1.0f to Color(0x8C151729))),
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 12.dp, bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .height(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFD45C))
                            .padding(horizontal = 9.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText("En 3 días", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E1A47)))
                    }

                    Box(
                        modifier = Modifier
                            .height(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDDF4F3))
                            .padding(horizontal = 9.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText("Ajustado", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1F8A84)))
                    }
                }
            }

            // Cliente
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFDE6F2)),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText("C", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFC2338F)))
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        BasicText("Camila Rodríguez", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Normal, color = Color(0xFF2E324A)))
                        BasicText("120 invitados · 2:00 p. m.", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)))
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .rfIconButtonShadow(CircleShape, isAdmin = true)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.dp, Color(0xFFECECF5), CircleShape)
                            .clickable { onSelectScreenCode("A15") },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.CHAT, tint = Color(0xFF151729), size = 20.dp)
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 2.dp, y = (-2).dp)
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFD45C)),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText("2", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2E1A47)))
                        }
                    }
                }
            }

            // Pagos Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        BasicText("Pagos", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Normal, color = Color(0xFF2E324A)))
                        Row(verticalAlignment = Alignment.Bottom) {
                            BasicText("Total ", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)))
                            BasicText("RD$ 4,500", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)))
                        }
                    }

                    // Progress
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFEDEDF5)),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.3f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(AdminButtonGradient),
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Row {
                                BasicText("Pagado ", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)))
                                BasicText("RD$ 1,350", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Normal, color = Color(0xFF2E324A)))
                            }
                            BasicText("Faltan RD$ 3,150", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF7A5700)))
                        }

                        Box(
                            modifier = Modifier
                                .height(36.dp)
                                .clip(CircleShape)
                                .background(AdminButtonGradient)
                                .clickable { onSelectScreenCode("A21") }
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                RfIcon(kind = RfIconKind.PLUS, tint = Color.White, size = 14.dp)
                                BasicText("Pago", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White))
                            }
                        }
                    }
                }
            }

            // 2x2 Grid
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Artículos
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                        .clickable { onSelectScreenCode("A06") }
                        .padding(12.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFEEF6D6)),
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.BOX, tint = Color(0xFF4E6A10), size = 18.dp)
                        }
                        Column {
                            BasicText("Artículos", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Normal, color = Color(0xFF2E324A)))
                            BasicText("2 tipos · 120 unid.", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
                        }
                    }
                }

                // Tareas
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                        .clickable { onSelectScreenCode("A17") }
                        .padding(12.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFECEBFF)),
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.CHECKLIST, tint = Color(0xFF4B47D6), size = 18.dp)
                        }
                        Column {
                            BasicText("Tareas", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Normal, color = Color(0xFF2E324A)))
                            BasicText("3 de 8 hechas", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
                        }
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Boceto
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                        .clickable { onSelectScreenCode("A18") }
                        .padding(12.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFFF2C9)),
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.COPY, tint = Color(0xFF7A5700), size = 18.dp)
                        }
                        Column {
                            BasicText("Boceto", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Normal, color = Color(0xFF2E324A)))
                            BasicText("Ver plano", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
                        }
                    }
                }

                // Chat
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                        .clickable { onSelectScreenCode("A15") }
                        .padding(12.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFDDF4F3)),
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.CHAT, tint = Color(0xFF1F8A84), size = 18.dp)
                        }
                        Column {
                            BasicText("Chat", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Normal, color = Color(0xFF2E324A)))
                            BasicText("2 sin leer", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }

        // Bottom CTA
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 36.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = true)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0), Color(0xFF2FB8C8))))
                    .clickable { onSelectScreenCode("A16") },
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RfIcon(kind = RfIconKind.PLAY, tint = Color.White, size = 20.dp, filled = true)
                    BasicText("Modo ejecución", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White))
                }
            }
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
    var selectedPhaseIndex by remember { mutableStateOf(1) }
    var executionDoneStates by remember { mutableStateOf(listOf(true, true, false, false)) }
    val montageCompletedCount = executionDoneStates.count { isDone -> isDone }
    val totalCompletedTasks = 3 + montageCompletedCount

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0), Color(0xFF2FB8C8))))
                    .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 22.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .rfIconButtonShadow(CircleShape, isAdmin = true)
                                .clip(CircleShape)
                                .background(Color(0x2EFFFFFF))
                                .clickable { onSelectScreenCode("BACK:A13") },
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.CLOSE, tint = Color.White, size = 22.dp)
                        }

                        Box(
                            modifier = Modifier
                                .height(26.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFD45C))
                                .padding(horizontal = 9.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(Color(0xFF2E1A47)))
                                BasicText("Hoy · Montaje", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E1A47)))
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Box(
                            modifier = Modifier.size(78.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawCircle(color = Color(0x38FFFFFF), style = Stroke(width = 8.dp.toPx()))
                                drawArc(
                                    color = Color(0xFFFFD45C),
                                    startAngle = -90f,
                                    sweepAngle = (totalCompletedTasks.toFloat() / 9f) * 360f,
                                    useCenter = false,
                                    style = Stroke(width = 8.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round),
                                )
                            }
                            BasicText("$totalCompletedTasks/9", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White))
                        }

                        Column {
                            BasicText("Boda Real Rosa & Dorado", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White))
                            Spacer(modifier = Modifier.height(4.dp))
                            BasicText("Montaje 2:00 p. m. · Jardín Botánico", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFFE9EEFF)))
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp, start = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFEDEDF5))
                        .padding(4.dp),
                ) {
                    val phaseLabels = listOf("Carga · 3/3", "Montaje · $montageCompletedCount/4", "Desmontaje")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        phaseLabels.forEachIndexed { phaseIndex, phaseLabel ->
                            val isSelectedPhase = selectedPhaseIndex == phaseIndex
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .then(
                                        if (isSelectedPhase) {
                                            Modifier.clip(RoundedCornerShape(12.dp)).background(Color.White)
                                        } else {
                                            Modifier
                                        },
                                    )
                                    .clickable { selectedPhaseIndex = phaseIndex },
                                contentAlignment = Alignment.Center,
                            ) {
                                BasicText(
                                    phaseLabel,
                                    style = TextStyle(
                                        fontFamily = typo.bodyFamily,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = if (isSelectedPhase) Color(0xFF2E324A) else Color(0xFF5F6377),
                                    ),
                                )
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFDDF4F3)),
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.LOCATION, tint = Color(0xFF1F8A84), size = 20.dp)
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            BasicText("Jardín Botánico Santo Domingo", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E324A)))
                            BasicText("Entrada por la puerta 2 · Contacto: Camila", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
                        }

                        BasicText("Mapa", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B47D6)))
                    }
                }

                val executionTasks = listOf(
                    QuadExecution(executionDoneStates[0], "Colocar 12 mesas redondas", "Según el plano aprobado", "P", Color(0xFFDDF4F3) to Color(0xFF1F8A84)),
                    QuadExecution(executionDoneStates[1], "Vestir mesas y sillas Tiffany", "120 sillas · mantel blanco", "Y", Color(0xFFFDE6F2) to Color(0xFFC2338F)),
                    QuadExecution(executionDoneStates[2], "Montar el arco de globos", "Entrada principal · rosa y dorado", "Y", Color(0xFFFDE6F2) to Color(0xFFC2338F)),
                    QuadExecution(executionDoneStates[3], "Centros de mesa florales", "12 centros, uno por mesa", "M", Color(0xFFECEBFF) to Color(0xFF4B47D6)),
                )

                executionTasks.forEachIndexed { taskIndex, task ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (task.done) Color(0xFFF6F6FB) else Color.White)
                            .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                            .clickable {
                                val updatedStates = executionDoneStates.toMutableList()
                                updatedStates[taskIndex] = !updatedStates[taskIndex]
                                executionDoneStates = updatedStates
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            if (task.done) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF8DB52C)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    RfIcon(kind = RfIconKind.CHECK, tint = Color.White, size = 16.dp)
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color.White)
                                        .border(2.dp, Color(0xFFC9CAE6), RoundedCornerShape(10.dp)),
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                BasicText(
                                    text = task.title,
                                    style = TextStyle(
                                        fontFamily = typo.bodyFamily,
                                        fontSize = 15.sp,
                                        fontWeight = if (task.done) FontWeight.Normal else FontWeight.Medium,
                                        color = if (task.done) Color(0xFF5F6377) else Color(0xFF2E324A),
                                        textDecoration = if (task.done) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                                    ),
                                )
                                BasicText(task.subtitle, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
                            }

                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(task.avatarColor.first),
                                contentAlignment = Alignment.Center,
                            ) {
                                BasicText(task.avatarLetter, style = TextStyle(fontFamily = typo.displayFamily, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = task.avatarColor.second))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 36.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = true)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AdminButtonGradient)
                    .clickable { onSelectScreenCode("A13") },
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RfIcon(kind = RfIconKind.CAMERA, tint = Color.White, size = 20.dp)
                    BasicText("Subir fotos del montaje", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White))
                }
            }
        }
    }
}

private data class QuadExecution(
    val done: Boolean,
    val title: String,
    val subtitle: String,
    val avatarLetter: String,
    val avatarColor: Pair<Color, Color>,
)

// ==============================================================================
// A17 · TAREAS DEL EQUIPO
// ==============================================================================
@Composable
internal fun RenderAdminTasksA17(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    var selectedMemberChip by remember { mutableStateOf("Todos") }
    var taskOneCompleted by remember { mutableStateOf(true) }
    var taskTwoCompleted by remember { mutableStateOf(false) }
    var taskThreeCompleted by remember { mutableStateOf(false) }
    var taskFourCompleted by remember { mutableStateOf(false) }
    var taskFiveCompleted by remember { mutableStateOf(false) }
    var taskSixCompleted by remember { mutableStateOf(false) }

    val showYuli = selectedMemberChip == "Todos" || selectedMemberChip == "Yuli"
    val showMaria = selectedMemberChip == "Todos" || selectedMemberChip == "María"
    val showPedro = selectedMemberChip == "Todos" || selectedMemberChip == "Pedro"
    val showAndres = selectedMemberChip == "Todos" || selectedMemberChip == "Andrés"
    val showBothToday = showYuli && showMaria
    val showBothVispera = showYuli && showPedro
    val showBothEventDay = showAndres && showPedro

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 116.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(44.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .rfIconButtonShadow(CircleShape, isAdmin = true)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), CircleShape)
                    .clickable { onSelectScreenCode("BACK:A13") },
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
            }

            Column(modifier = Modifier.weight(1f)) {
                BasicText(
                    text = "Tareas del equipo",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A), lineHeight = 22.sp),
                )
                BasicText(
                    text = "Boda Real Rosa & Dorado",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)),
                )
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .shadow(8.dp, CircleShape, spotColor = Color(0x665B6CF0), ambientColor = Color.Transparent)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
                    .clickable { taskTwoCompleted = !taskTwoCompleted },
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.PLUS, tint = Color.White, size = 22.dp)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-2).dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val isAllSelected = selectedMemberChip == "Todos"
            Box(
                modifier = Modifier
                    .height(36.dp)
                    .clip(CircleShape)
                    .then(
                        if (isAllSelected) {
                            Modifier.background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
                        } else {
                            Modifier.background(Color.White).border(1.dp, Color(0xFFE9E9F2), CircleShape)
                        },
                    )
                    .clickable { selectedMemberChip = "Todos" }
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                BasicText("Todos", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = if (isAllSelected) FontWeight.SemiBold else FontWeight.Normal, color = if (isAllSelected) Color.White else Color(0xFF2E324A)))
            }

            val members = listOf(
                Triple("M", "María", Pair(Color(0xFFECEBFF), Color(0xFF4B47D6))),
                Triple("P", "Pedro", Pair(Color(0xFFDDF4F3), Color(0xFF1F8A84))),
                Triple("Y", "Yuli", Pair(Color(0xFFFDE6F2), Color(0xFFC2338F))),
                Triple("A", "Andrés", Pair(Color(0xFFFFF2C9), Color(0xFF7A5700))),
            )

            members.forEach { memberTuple ->
                val initial = memberTuple.first
                val name = memberTuple.second
                val colors = memberTuple.third
                val isMemberSelected = selectedMemberChip == name
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .clip(CircleShape)
                        .then(
                            if (isMemberSelected) {
                                Modifier.background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
                            } else {
                                Modifier.background(Color.White).border(1.dp, Color(0xFFE9E9F2), CircleShape)
                            },
                        )
                        .clickable { selectedMemberChip = name }
                        .padding(start = 5.dp, end = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(colors.first),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(initial, style = TextStyle(fontFamily = typo.displayFamily, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = colors.second))
                        }
                        BasicText(name, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = if (isMemberSelected) FontWeight.SemiBold else FontWeight.Normal, color = if (isMemberSelected) Color.White else Color(0xFF2E324A)))
                    }
                }
            }
        }

        BasicText(
            text = "HOY · MARTES 20",
            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = Color(0xFF5F6377)),
            modifier = Modifier.padding(top = 2.dp),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-4).dp)
                .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp)),
        ) {
            Column {
                if (showYuli) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { taskOneCompleted = !taskOneCompleted }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        if (taskOneCompleted) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF8DB52C)),
                                contentAlignment = Alignment.Center,
                            ) {
                                RfIcon(kind = RfIconKind.CHECK, tint = Color.White, size = 14.dp)
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .border(2.dp, Color(0xFFC9CAE6), CircleShape),
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            BasicText("Confirmar paleta floral", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = if (taskOneCompleted) Color(0xFF5F6377) else Color(0xFF2E324A), textDecoration = if (taskOneCompleted) TextDecoration.LineThrough else TextDecoration.None))
                            BasicText("10:00 a. m.", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)))
                        }
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFDE6F2)),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText("Y", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFC2338F)))
                        }
                    }
                }

                if (showBothToday) {
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))
                }

                if (showMaria) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { taskTwoCompleted = !taskTwoCompleted }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        if (taskTwoCompleted) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF8DB52C)),
                                contentAlignment = Alignment.Center,
                            ) {
                                RfIcon(kind = RfIconKind.CHECK, tint = Color.White, size = 14.dp)
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .border(2.dp, Color(0xFFC9CAE6), CircleShape),
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            BasicText("Recordar pago restante a Camila", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = if (taskTwoCompleted) Color(0xFF5F6377) else Color(0xFF2E324A), textDecoration = if (taskTwoCompleted) TextDecoration.LineThrough else TextDecoration.None))
                            BasicText("5:00 p. m. · RD$ 3,150", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)))
                        }
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFECEBFF)),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText("M", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4B47D6)))
                        }
                    }
                }
            }
        }

        BasicText(
            text = "JUEVES 22 · VÍSPERA",
            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = Color(0xFF5F6377)),
            modifier = Modifier.padding(top = 2.dp),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-4).dp)
                .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp)),
        ) {
            Column {
                if (showYuli) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { taskThreeCompleted = !taskThreeCompleted }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        if (taskThreeCompleted) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF8DB52C)),
                                contentAlignment = Alignment.Center,
                            ) {
                                RfIcon(kind = RfIconKind.CHECK, tint = Color.White, size = 14.dp)
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .border(2.dp, Color(0xFFC9CAE6), CircleShape),
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            BasicText("Inflar globos del arco", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = if (taskThreeCompleted) Color(0xFF5F6377) else Color(0xFF151729), textDecoration = if (taskThreeCompleted) TextDecoration.LineThrough else TextDecoration.None))
                            BasicText("3:00 p. m. · en el taller", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)))
                        }
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFDE6F2)),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText("Y", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFC2338F)))
                        }
                    }
                }

                if (showBothVispera) {
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))
                }

                if (showPedro) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { taskFourCompleted = !taskFourCompleted }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        if (taskFourCompleted) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF8DB52C)),
                                contentAlignment = Alignment.Center,
                            ) {
                                RfIcon(kind = RfIconKind.CHECK, tint = Color.White, size = 14.dp)
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .border(2.dp, Color(0xFFC9CAE6), CircleShape),
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            BasicText("Revisar y limpiar 120 sillas", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = if (taskFourCompleted) Color(0xFF5F6377) else Color(0xFF151729), textDecoration = if (taskFourCompleted) TextDecoration.LineThrough else TextDecoration.None))
                            BasicText("4:00 p. m.", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)))
                        }
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDDF4F3)),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText("P", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1F8A84)))
                        }
                    }
                }
            }
        }

        BasicText(
            text = "VIERNES 23 · DÍA DEL EVENTO",
            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = Color(0xFF5F6377)),
            modifier = Modifier.padding(top = 2.dp),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-4).dp)
                .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp)),
        ) {
            Column {
                if (showAndres) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { taskFiveCompleted = !taskFiveCompleted }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        if (taskFiveCompleted) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF8DB52C)),
                                contentAlignment = Alignment.Center,
                            ) {
                                RfIcon(kind = RfIconKind.CHECK, tint = Color.White, size = 14.dp)
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .border(2.dp, Color(0xFFC9CAE6), CircleShape),
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            BasicText("Cargar el camión", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = if (taskFiveCompleted) Color(0xFF5F6377) else Color(0xFF151729), textDecoration = if (taskFiveCompleted) TextDecoration.LineThrough else TextDecoration.None))
                            BasicText("12:30 p. m.", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)))
                        }
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFF2C9)),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText("A", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF7A5700)))
                        }
                    }
                }

                if (showBothEventDay) {
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))
                }

                if (showPedro) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { taskSixCompleted = !taskSixCompleted }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        if (taskSixCompleted) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF8DB52C)),
                                contentAlignment = Alignment.Center,
                            ) {
                                RfIcon(kind = RfIconKind.CHECK, tint = Color.White, size = 14.dp)
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .border(2.dp, Color(0xFFC9CAE6), CircleShape),
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            BasicText("Montaje completo", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = if (taskSixCompleted) Color(0xFF5F6377) else Color(0xFF151729), textDecoration = if (taskSixCompleted) TextDecoration.LineThrough else TextDecoration.None))
                            BasicText("2:00 – 5:00 p. m.", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)))
                        }
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDDF4F3)),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText("P", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1F8A84)))
                        }
                    }
                }
            }
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
    var selectedViewMode by remember { mutableStateOf("Plano") }
    var extraTablesApplied by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().height(44.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = true)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), CircleShape)
                        .clickable { onSelectScreenCode("BACK:A13") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "Boceto de Camila",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729), lineHeight = 22.sp),
                    )
                    BasicText(
                        text = "Boda Real Rosa & Dorado",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)),
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFEDEDF5))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                val isOriginalSelected = selectedViewMode == "Boceto original"
                val isPlanoSelected = selectedViewMode == "Plano"
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .then(
                            if (isOriginalSelected) {
                                Modifier.shadow(2.dp, RoundedCornerShape(12.dp), spotColor = Color(0x334650B4)).clip(RoundedCornerShape(12.dp)).background(Color.White)
                            } else {
                                Modifier
                            },
                        )
                        .clickable { selectedViewMode = "Boceto original" },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText("Boceto original", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Normal, color = if (isOriginalSelected) Color(0xFF2E324A) else Color(0xFF5F6377)))
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .then(
                            if (isPlanoSelected) {
                                Modifier.shadow(2.dp, RoundedCornerShape(12.dp), spotColor = Color(0x334650B4)).clip(RoundedCornerShape(12.dp)).background(Color.White)
                            } else {
                                Modifier
                            },
                        )
                        .clickable { selectedViewMode = "Plano" },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText("Plano", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Normal, color = if (isPlanoSelected) Color(0xFF2E324A) else Color(0xFF5F6377)))
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(430.dp)
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp)),
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val scaleX = size.width / 358f
                    val scaleY = size.height / 440f
                    scale(scaleX, scaleY, Offset.Zero) {
                        drawRoundRect(
                            color = Color(0xFFEFE8FD),
                            topLeft = Offset(72f, 36f),
                            size = Size(214f, 48f),
                            cornerRadius = CornerRadius(10f, 10f),
                        )
                        drawRoundRect(
                            color = Color(0xFF865DEC),
                            topLeft = Offset(72f, 36f),
                            size = Size(214f, 48f),
                            cornerRadius = CornerRadius(10f, 10f),
                            style = Stroke(width = 2f),
                        )

                        drawRoundRect(
                            color = Color(0xFFEEF6D6),
                            topLeft = Offset(128f, 240f),
                            size = Size(102f, 92f),
                            cornerRadius = CornerRadius(10f, 10f),
                        )
                        drawRoundRect(
                            color = Color(0xFF8DB52C),
                            topLeft = Offset(128f, 240f),
                            size = Size(102f, 92f),
                            cornerRadius = CornerRadius(10f, 10f),
                            style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))),
                        )

                        val baseTables = listOf(
                            Offset(110f, 190f),
                            Offset(200f, 190f),
                            Offset(70f, 270f),
                            Offset(290f, 270f),
                            Offset(70f, 360f),
                            Offset(290f, 360f),
                        )
                        val extraTables = listOf(
                            Offset(60f, 125f),
                            Offset(180f, 125f),
                            Offset(295f, 125f),
                            Offset(295f, 190f),
                            Offset(135f, 370f),
                            Offset(220f, 370f),
                        )
                        val tables = if (extraTablesApplied) baseTables + extraTables else baseTables

                        tables.forEach { tableCenter ->
                            drawCircle(color = Color(0xFFECEBFF), radius = 22f, center = tableCenter)
                            drawCircle(color = Color(0xFF5B6CF0), radius = 22f, center = tableCenter, style = Stroke(width = 2f))
                            for (chairIndex in 0 until 10) {
                                val angle = chairIndex * (kotlin.math.PI * 2 / 10)
                                val chairX = tableCenter.x + kotlin.math.cos(angle).toFloat() * 31f
                                val chairY = tableCenter.y + kotlin.math.sin(angle).toFloat() * 31f
                                drawCircle(color = Color(0xFFFDE6F2), radius = 3.6f, center = Offset(chairX, chairY))
                                drawCircle(color = Color(0xFFEA4EA9), radius = 3.6f, center = Offset(chairX, chairY), style = Stroke(width = 1.2f))
                            }
                        }

                        val entrancePath = PathParser().parsePathString("M150 428L179 404L208 428").toPath()
                        drawPath(entrancePath, color = Color(0xFF151729), style = Stroke(width = 2.4f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    BasicText("Tarima", modifier = Modifier.align(Alignment.TopCenter).offset(y = 52.dp), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5F6377)))
                    BasicText("Pista", modifier = Modifier.align(Alignment.Center).offset(y = 45.dp), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5F6377)))
                    BasicText("Entrada", modifier = Modifier.align(Alignment.BottomCenter).offset(y = (-45).dp), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5F6377)))

                    val tableLabel = if (extraTablesApplied) "12 mesas redondas" else "6 mesas redondas"
                    val chairsLabel = if (extraTablesApplied) "120 sillas" else "60 sillas"
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .height(24.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(1.dp, Color(0xFFE3E4EF), CircleShape)
                                .padding(horizontal = 9.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(tableLabel, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B47D6)))
                        }
                        Box(
                            modifier = Modifier
                                .height(24.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(1.dp, Color(0xFFE3E4EF), CircleShape)
                                .padding(horizontal = 9.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(chairsLabel, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC2338F)))
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFFE9E8FF), Color(0xFFDDF2F6))))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                RosaAvatar(size = RosaAvatarSize.MEDIUM, customSize = 36.dp, forceAdminPalette = true)
                BasicText(
                    text = if (extraTablesApplied) "Plano actualizado a 12 mesas redondas para 120 invitados." else "Caben 6 mesas más sin tapar la pista, para llegar a los 120 invitados.",
                    modifier = Modifier.weight(1f),
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF151729), lineHeight = 18.sp),
                )
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
                        .clickable { extraTablesApplied = !extraTablesApplied }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(if (extraTablesApplied) "Aplicado" else "Aplicar", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White))
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 36.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = true)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
                    .clickable { onSelectScreenCode("A13") },
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    RfIcon(kind = RfIconKind.SEND, tint = Color.White, size = 20.dp)
                    BasicText("Enviar plano a Camila", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White))
                }
            }
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
    var allFloralReturned by remember { mutableStateOf(false) }
    var damagePhotoAttached by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().height(44.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = true)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), CircleShape)
                        .clickable { onSelectScreenCode("BACK:A13") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "Cierre del evento",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729), lineHeight = 22.sp),
                    )
                    BasicText(
                        text = "Gala Corporativa Grupo Caribe · 12 oct",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)),
                    )
                }
            }

            BasicText(
                text = "DEVOLUCIÓN DE ARTÍCULOS",
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = Color(0xFF5F6377)),
                modifier = Modifier.padding(top = 2.dp),
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-4).dp)
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp)),
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        RfPhoto(photoKey = RfPhotoKey.SILLA_TIFFANY, contentDescription = "Silla Tiffany Cristal", modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)))
                        BasicText("Silla Tiffany Cristal", modifier = Modifier.weight(1f), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF151729)))
                        BasicText("100/100", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)))
                        RfIcon(kind = RfIconKind.CHECK, tint = Color(0xFF8DB52C), size = 22.dp)
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        RfPhoto(photoKey = RfPhotoKey.MESA_REDONDA, contentDescription = "Mesa redonda", modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)))
                        BasicText("Mesa redonda", modifier = Modifier.weight(1f), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF151729)))
                        BasicText("10/10", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)))
                        RfIcon(kind = RfIconKind.CHECK, tint = Color(0xFF8DB52C), size = 22.dp)
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (allFloralReturned) Color.White else Color(0xFFFFF8E5))
                            .clickable { allFloralReturned = !allFloralReturned }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        RfPhoto(photoKey = RfPhotoKey.CENTRO_FLORAL, contentDescription = "Centro floral", modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)))
                        BasicText("Centro de mesa floral", modifier = Modifier.weight(1f), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF151729)))
                        BasicText(
                            if (allFloralReturned) "10/10" else "9/10",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (allFloralReturned) Color(0xFF151729) else Color(0xFF7A5700)),
                        )
                        if (allFloralReturned) {
                            RfIcon(kind = RfIconKind.CHECK, tint = Color(0xFF8DB52C), size = 22.dp)
                        } else {
                            RfIcon(kind = RfIconKind.WARNING, tint = Color(0xFFE0A100), size = 22.dp)
                        }
                    }
                }
            }

            BasicText(
                text = "DAÑOS Y FALTANTES",
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = Color(0xFF5F6377)),
                modifier = Modifier.padding(top = 2.dp),
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-4).dp)
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                    .clickable { damagePhotoAttached = !damagePhotoAttached }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(2.dp, if (damagePhotoAttached) Color(0xFF8DB52C) else Color(0xFFC9CAE6), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (damagePhotoAttached) {
                            RfIcon(kind = RfIconKind.CHECK, tint = Color(0xFF8DB52C), size = 22.dp)
                        } else {
                            RfIcon(kind = RfIconKind.CAMERA, tint = Color(0xFF4B47D6), size = 22.dp)
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        BasicText("1 centro de mesa con la base rota", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)))
                        BasicText(if (damagePhotoAttached) "Foto-evidencia.jpg adjuntada" else "Agrega una foto para el registro", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)))
                    }
                    BasicText("RD$ 35", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFB42345)))
                }
            }

            BasicText(
                text = "COBRO FINAL",
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = Color(0xFF5F6377)),
                modifier = Modifier.padding(top = 2.dp),
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-4).dp)
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        BasicText("Total del evento", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF5F6377)))
                        BasicText("RD$ 8,500", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        BasicText("Pagado", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF5F6377)))
                        BasicText("RD$ 8,500", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4E6A10)))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        BasicText("Cargo por daño", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF5F6377)))
                        BasicText("RD$ 35", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)))
                    }
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        BasicText("Saldo a cobrar", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)))
                        BasicText("RD$ 35", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)))
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 36.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = true)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
                    .clickable { onSelectScreenCode("A01") },
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    RfIcon(kind = RfIconKind.CHECK, tint = Color.White, size = 20.dp)
                    BasicText("Cobrar y cerrar evento", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White))
                }
            }
        }
    }
}

// ==============================================================================
// A10 · NUEVO ARTÍCULO ADMIN
// ==============================================================================
@Composable
internal fun RenderAdminNewArticleA10(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val articleName = viewModel?.newArticleName?.collectAsState()?.value ?: "Mesa plegable 1.8 m"
    val articleCat = viewModel?.newArticleCategory?.collectAsState()?.value ?: "Mobiliario"
    val articlePrice = viewModel?.newArticlePrice?.collectAsState()?.value ?: "20"
    val articleStock = viewModel?.newArticleStock?.collectAsState()?.value ?: "30"
    var articleDescription by remember { mutableStateOf("Resina blanca, para 8 personas. Ideal para exteriores.") }
    var visibleInCatalog by remember { mutableStateOf(true) }
    var extraPhotoAdded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().height(44.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = true)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), CircleShape)
                        .clickable { onSelectScreenCode("BACK:A09") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "Nuevo artículo",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A), lineHeight = 22.sp),
                    )
                    BasicText(
                        text = "Se agrega al catálogo y al inventario",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)),
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(104.dp)
                        .clip(RoundedCornerShape(18.dp)),
                ) {
                    RfPhoto(photoKey = RfPhotoKey.MESA_PLEGABLE, contentDescription = "Mesa plegable", modifier = Modifier.fillMaxSize())
                }

                Box(
                    modifier = Modifier
                        .size(104.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .border(2.dp, if (extraPhotoAdded) Color(0xFF8DB52C) else Color(0xFFC9CAE6), RoundedCornerShape(18.dp))
                        .clickable { extraPhotoAdded = !extraPhotoAdded },
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        if (extraPhotoAdded) {
                            RfIcon(kind = RfIconKind.CHECK, tint = Color(0xFF8DB52C), size = 22.dp)
                            BasicText("Foto 2 lista", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF4E6A10)))
                        } else {
                            RfIcon(kind = RfIconKind.CAMERA, tint = Color(0xFF4B47D6), size = 22.dp)
                            BasicText("Agregar foto", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF4B47D6)))
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(104.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFFF2F2F8))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = "Hasta 6 fotos. La primera es la portada.",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)),
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BasicText("Nombre", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF3A3D52)))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .rfInputShadow(RoundedCornerShape(14.dp), isAdmin = true)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    BasicTextField(
                        value = articleName,
                        onValueChange = { updatedName -> viewModel?.setNewArticleName(updatedName) },
                        singleLine = true,
                        textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Normal, color = Color(0xFF2E324A)),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BasicText("Categoría", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF3A3D52)))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val categories = listOf("Mobiliario", "Floral", "Globos", "Iluminación")
                    categories.forEach { categoryLabel ->
                        val isSelected = categoryLabel.equals(articleCat, ignoreCase = true)
                        val showMobiliarioIcon = isSelected && categoryLabel == "Mobiliario"
                        Box(
                            modifier = Modifier
                                .height(36.dp)
                                .shadow(if (isSelected) 6.dp else 4.dp, CircleShape, spotColor = if (isSelected) Color(0xC05B6CF0) else Color(0x604650B4), ambientColor = if (isSelected) Color(0x405B6CF0) else Color(0x204650B4))
                                .clip(CircleShape)
                                .background(if (isSelected) Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))) else Brush.linearGradient(listOf(Color.White, Color.White)))
                                .then(if (!isSelected) Modifier.border(1.dp, Color(0xFFE9E9F2), CircleShape) else Modifier)
                                .clickable { viewModel?.setNewArticleCategory(categoryLabel) }
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (showMobiliarioIcon) {
                                    RfIcon(kind = RfIconKind.BOX, tint = Color.White, size = 16.dp)
                                }
                                BasicText(
                                    categoryLabel,
                                    style = TextStyle(
                                        fontFamily = typo.bodyFamily,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else Color(0xFF2E324A),
                                    ),
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    BasicText("Precio por día", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF3A3D52)))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .rfInputShadow(RoundedCornerShape(14.dp), isAdmin = true)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            BasicText("RD$", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Normal, color = Color(0xFF2E324A)))
                            BasicTextField(
                                value = articlePrice,
                                onValueChange = { updatedPrice -> viewModel?.setNewArticlePrice(updatedPrice) },
                                singleLine = true,
                                textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Normal, color = Color(0xFF2E324A)),
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    BasicText("Cantidad", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF3A3D52)))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .rfInputShadow(RoundedCornerShape(14.dp), isAdmin = true)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            BasicTextField(
                                value = articleStock,
                                onValueChange = { updatedStock -> viewModel?.setNewArticleStock(updatedStock) },
                                singleLine = true,
                                textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E324A)),
                                modifier = Modifier.weight(1f),
                            )
                            BasicText("unid.", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF5F6377)))
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BasicText("Descripción", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF3A3D52)))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .rfInputShadow(RoundedCornerShape(14.dp), isAdmin = true)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                ) {
                    BasicTextField(
                        value = articleDescription,
                        onValueChange = { updatedDescription -> articleDescription = updatedDescription },
                        textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF2E324A)),
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(16.dp), isAdmin = true)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(16.dp))
                    .clickable { visibleInCatalog = !visibleInCatalog }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        BasicText("Visible en el catálogo", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E324A)))
                        BasicText("Los clientes lo verán y podrán pedirlo", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
                    }

                    Box(
                        modifier = Modifier
                            .width(50.dp)
                            .height(30.dp)
                            .clip(CircleShape)
                            .background(if (visibleInCatalog) Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))) else SolidColor(Color(0xFFDCDDEA)))
                            .padding(3.dp),
                        contentAlignment = if (visibleInCatalog) Alignment.CenterEnd else Alignment.CenterStart,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 36.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = true)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
                    .clickable {
                        viewModel?.submitNewArticle()
                        onSelectScreenCode("A09")
                    },
                contentAlignment = Alignment.Center,
            ) {
                BasicText("Guardar artículo", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White))
            }
        }
    }
}

// ==============================================================================
// A23 · FICHA DE ARTÍCULO
// ==============================================================================
@Composable
internal fun RenderAdminArticleDetailA23(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val articleId = viewModel?.selectedArticleId?.collectAsState()?.value ?: "art-silla-tiffany"
    val allArticles = viewModel?.articles?.collectAsState()?.value ?: CanonicalSampleData.articles
    val article = allArticles.firstOrNull { it.id == articleId } ?: allArticles.first()

    val photoKey = when (article.id) {
        "art-silla-tiffany" -> RfPhotoKey.SILLA_TIFFANY
        "art-mesa-redonda" -> RfPhotoKey.MESA_REDONDA
        "art-centro-floral" -> RfPhotoKey.CENTRO_FLORAL
        "art-arco-rosa" -> RfPhotoKey.ARCO_ROSA
        "art-mesa-helados" -> RfPhotoKey.MESA_HELADOS
        "art-mesa-plegable" -> RfPhotoKey.MESA_PLEGABLE
        else -> RfPhotoKey.SILLA_TIFFANY
    }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        // Foto superior 250dp
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
        ) {
            RfPhoto(photoKey = photoKey, contentDescription = article.name, modifier = Modifier.fillMaxSize())
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(0.0f to Color(0x4D151729), 0.4f to Color.Transparent)),
            )
        }

        // Header botones flotantes
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 20.dp, end = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .rfIconButtonShadow(CircleShape, isAdmin = true)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), CircleShape)
                    .clickable { onSelectScreenCode("BACK:A09") },
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .rfIconButtonShadow(CircleShape, isAdmin = true)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), CircleShape)
                    .clickable { onSelectScreenCode("A10") },
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.EDIT, tint = Color(0xFF151729), size = 20.dp)
            }
        }

        // Ficha contenido
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 226.dp)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Color(0xFFF6F6FB)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 20.dp, start = 20.dp, end = 20.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Título y precio
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        BasicText(article.name, style = TextStyle(fontFamily = typo.displayFamily, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A)))
                        BasicText("${article.category} · Visible en el catálogo", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)))
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        BasicText("RD$ ${article.pricePerDayDop}", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)))
                        BasicText("por día", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
                    }
                }

                // 3 stat cards
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                    ) {
                        Column {
                            BasicText("${article.totalStock}", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)))
                            BasicText("En total", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                    ) {
                        Column {
                            BasicText("${article.availableOct23}", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4E6A10)))
                            BasicText("En buen estado", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                    ) {
                        Column {
                            val reserved = (article.totalStock - article.availableOct23).coerceAtLeast(0)
                            BasicText("$reserved", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF7A5700)))
                            BasicText("En reserva", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
                        }
                    }
                }

                // Calendario reservas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            BasicText("Reservas · octubre", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E324A)))
                            BasicText("3 eventos", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
                        }

                        // Grid 31 days
                        val days = (1..31).toList()
                        val rows = days.chunked(7)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            rows.forEach { rowDays ->
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    rowDays.forEach { day ->
                                        val isFull = (day == 23)
                                        val isPartial = (day in listOf(2, 7, 14))
                                        val isToday = (day == 20)

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(30.dp)
                                                .clip(RoundedCornerShape(9.dp))
                                                .then(
                                                    if (isFull) {
                                                        Modifier.background(AdminButtonGradient)
                                                    } else if (isPartial) {
                                                        Modifier.background(Color(0xFFECEBFF))
                                                    } else if (isToday) {
                                                        Modifier.border(1.5.dp, Color(0xFF5B6CF0), RoundedCornerShape(9.dp))
                                                    } else {
                                                        Modifier
                                                    },
                                                ),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            BasicText(
                                                text = "$day",
                                                style = TextStyle(
                                                    fontFamily = typo.bodyFamily,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = if (isFull) {
                                                        Color.White
                                                    } else if (isPartial) {
                                                        Color(0xFF4B47D6)
                                                    } else {
                                                        Color(0xFF2E324A)
                                                    },
                                                ),
                                            )
                                        }
                                    }
                                    // Filler for incomplete last row
                                    if (rowDays.size < 7) {
                                        repeat(7 - rowDays.size) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }

                        // Leyenda
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFF5B6CF0)))
                                BasicText("Todo reservado", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFFECEBFF)))
                                BasicText("Parcial", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class QuadPack(
    val photo: RfPhotoKey,
    val name: String,
    val desc: String,
    val discount: String,
    val price: String,
)

private data class ClientItemData(
    val letter: String,
    val name: String,
    val subtitle: String,
    val tagText: String,
    val avatarColors: Pair<Color, Color>,
    val tagColors: Pair<Color, Color>,
)

private data class TeamMemberData(
    val initial: String,
    val name: String,
    val role: String,
    val tagText: String,
    val avatarColors: Pair<Color, Color>,
    val tagColors: Pair<Color, Color>,
    val isHighlighted: Boolean,
)

// ==============================================================================
// A24 · PAQUETES COMERCIALES
// ==============================================================================
@Composable
internal fun RenderAdminPackagesA24(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    var packageEnabledStates by remember { mutableStateOf(listOf(true, true, true, true)) }
    val activePackageCount = packageEnabledStates.count { isEnabled -> isEnabled }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 116.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(44.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .rfIconButtonShadow(CircleShape, isAdmin = true)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), CircleShape)
                    .clickable { onSelectScreenCode("BACK:A12") },
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
            }

            Column(modifier = Modifier.weight(1f)) {
                BasicText(
                    text = "Paquetes",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A), lineHeight = 22.sp),
                )
                BasicText(
                    text = "$activePackageCount activos en el catálogo",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)),
                )
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .shadow(8.dp, CircleShape, spotColor = Color(0x665B6CF0), ambientColor = Color.Transparent)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
                    .clickable { onSelectScreenCode("A25") },
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.PLUS, tint = Color.White, size = 22.dp)
            }
        }

        val pkgs = listOf(
            QuadPack(RfPhotoKey.BODA, "Bodas Rosa", "4 artículos · ", "15% desc.", "RD$ 850"),
            QuadPack(RfPhotoKey.QUINCEANERA, "Quinceañera Premium", "5 artículos · ", "12% desc.", "RD$ 1,200"),
            QuadPack(RfPhotoKey.CUMPLE_INFANTIL, "Cumpleaños Infantil", "4 artículos · ", "10% desc.", "RD$ 450"),
            QuadPack(RfPhotoKey.CORPORATIVO, "Evento Corporativo", "3 artículos · ", "8% desc.", "RD$ 600"),
        )

        pkgs.forEachIndexed { packageIndex, item ->
            val isPackageActive = packageEnabledStates[packageIndex]
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                    .clickable { onSelectScreenCode("A25") }
                    .padding(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    RfPhoto(photoKey = item.photo, contentDescription = item.name, modifier = Modifier.size(76.dp).clip(RoundedCornerShape(14.dp)))
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        BasicText(item.name, maxLines = 1, overflow = TextOverflow.Ellipsis, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E324A)))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            BasicText(item.desc, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)))
                            BasicText(item.discount, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF4E6A10)))
                        }
                        BasicText(item.price, style = TextStyle(fontFamily = typo.displayFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)))
                    }

                    Box(
                        modifier = Modifier
                            .size(width = 46.dp, height = 28.dp)
                            .clip(CircleShape)
                            .background(if (isPackageActive) Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))) else SolidColor(Color(0xFFDCDDEA)))
                            .clickable {
                                val updatedStates = packageEnabledStates.toMutableList()
                                updatedStates[packageIndex] = !updatedStates[packageIndex]
                                packageEnabledStates = updatedStates
                            }
                            .padding(3.dp),
                        contentAlignment = if (isPackageActive) Alignment.CenterEnd else Alignment.CenterStart,
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
    var packageName by remember { mutableStateOf("Bodas Rosa") }
    var packageQuantities by remember { mutableStateOf(listOf(1, 10, 120, 3)) }
    var discountPercent by remember { mutableStateOf(15) }
    val normalPriceDop = 95 * packageQuantities[0] + 35 * packageQuantities[1] + 4 * packageQuantities[2] + 25 * packageQuantities[3]
    val discountedPriceDop = normalPriceDop * (100 - discountPercent) / 100

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp, start = 20.dp, end = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = true)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), CircleShape)
                        .clickable { onSelectScreenCode("BACK:A24") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText("Editar paquete", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A)))
                    BasicText(packageName, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)))
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .clip(RoundedCornerShape(16.dp)),
                ) {
                    RfPhoto(photoKey = RfPhotoKey.BODA, contentDescription = "Bodas Rosa", modifier = Modifier.fillMaxSize())
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    BasicText("Nombre", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF3A3D52)))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        BasicTextField(
                            value = packageName,
                            onValueChange = { updatedText -> packageName = updatedText },
                            singleLine = true,
                            textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Normal, color = Color(0xFF2E324A)),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            BasicText("INCLUYE", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5F6377), letterSpacing = 1.sp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp)),
            ) {
                Column {
                    val packageItems = listOf(
                        Triple(RfPhotoKey.ARCO_ROSA, "Arco de globos rosa", "RD$ 95 c/u" to packageQuantities[0]),
                        Triple(RfPhotoKey.CENTRO_FLORAL, "Centro de mesa floral", "RD$ 35 c/u" to packageQuantities[1]),
                        Triple(RfPhotoKey.SILLA_TIFFANY, "Silla Tiffany Cristal", "RD$ 4 c/u" to packageQuantities[2]),
                        Triple(RfPhotoKey.MESA_REDONDA, "Mesa redonda", "RD$ 25 c/u" to packageQuantities[3]),
                    )

                    packageItems.forEachIndexed { itemIndex, packageItem ->
                        val photo = packageItem.first
                        val name = packageItem.second
                        val info = packageItem.third
                        if (itemIndex > 0) {
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(10.dp)),
                            ) {
                                RfPhoto(photoKey = photo, contentDescription = name, modifier = Modifier.fillMaxSize())
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                BasicText(name, maxLines = 1, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E324A)))
                                BasicText(info.first, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
                            }

                            Box(
                                modifier = Modifier
                                    .height(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF2F2F8))
                                    .padding(horizontal = 3.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .clickable {
                                                val updatedList = packageQuantities.toMutableList()
                                                updatedList[itemIndex] = (updatedList[itemIndex] - 1).coerceAtLeast(1)
                                                packageQuantities = updatedList
                                            },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        BasicText("−", style = TextStyle(fontSize = 16.sp, color = Color(0xFF5B6CF0), fontWeight = FontWeight.Normal))
                                    }
                                    BasicText(
                                        "${info.second}",
                                        modifier = Modifier.widthIn(min = 28.dp),
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Normal, color = Color(0xFF2E324A), textAlign = androidx.compose.ui.text.style.TextAlign.Center),
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .clickable {
                                                val updatedList = packageQuantities.toMutableList()
                                                updatedList[itemIndex] = updatedList[itemIndex] + 1
                                                packageQuantities = updatedList
                                            },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        BasicText("+", style = TextStyle(fontSize = 16.sp, color = Color(0xFF5B6CF0), fontWeight = FontWeight.Normal))
                                    }
                                }
                            }
                        }
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clickable { onSelectScreenCode("A09") },
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            RfIcon(kind = RfIconKind.PLUS, tint = Color(0xFF4B47D6), size = 16.dp)
                            BasicText("Agregar artículo", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B47D6)))
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                    .clickable {
                        discountPercent = when (discountPercent) {
                            15 -> 20
                            20 -> 10
                            else -> 15
                        }
                    }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        BasicText("Descuento del paquete", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3A3D52)))
                        BasicText("$discountPercent%", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4B47D6)))
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFEDEDF5)),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth((discountPercent / 50f).coerceIn(0.1f, 1f))
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(AdminButtonGradient),
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            BasicText("Precio normal ", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF5F6377)))
                            BasicText(formatAdminDop(normalPriceDop), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF5F6377), textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough))
                        }
                        BasicText(formatAdminDop(discountedPriceDop), style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)))
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 36.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = true)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AdminButtonGradient)
                    .clickable { onSelectScreenCode("A24") },
                contentAlignment = Alignment.Center,
            ) {
                BasicText("Guardar paquete", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White))
            }
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
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterIndex by remember { mutableStateOf(0) }

    val allClients = listOf(
        ClientItemData("C", "Camila Rodríguez", "3 eventos · pagó RD$ 3,210", "Vie 23 oct", Pair(Color(0xFFFDE6F2), Color(0xFFC2338F)), Pair(Color(0xFFFFF2C9), Color(0xFF7A5700))),
        ClientItemData("L", "Laura Peña", "Cotización enviada", "Sáb 7 nov", Pair(Color(0xFFEEF6D6), Color(0xFF4E6A10)), Pair(Color(0xFFECEBFF), Color(0xFF4B47D6))),
        ClientItemData("A", "Ana Gómez", "Solicitud nueva", "Nueva", Pair(Color(0xFFFFF2C9), Color(0xFF7A5700)), Pair(Color(0xFFFFD45C), Color(0xFF2E1A47))),
        ClientItemData("G", "Grupo Caribe", "Empresa · 1 evento", "Cerrado", Pair(Color(0xFFF2F2F8), Color(0xFF5F6377)), Pair(Color(0xFFF2F2F8), Color(0xFF5F6377))),
    )
    val filteredClients = allClients.filter { client ->
        val matchesChip = when (selectedFilterIndex) {
            1 -> client.tagText == "Vie 23 oct" || client.tagText == "Sáb 7 nov"
            2 -> client.subtitle.contains("Empresa", ignoreCase = true)
            else -> true
        }
        val matchesQuery = searchQuery.isBlank() ||
            client.name.contains(searchQuery.trim(), ignoreCase = true) ||
            client.subtitle.contains(searchQuery.trim(), ignoreCase = true)
        matchesChip && matchesQuery
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 116.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(44.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .rfIconButtonShadow(CircleShape, isAdmin = true)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), CircleShape)
                    .clickable { onSelectScreenCode("BACK:A12") },
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
            }

            Column(modifier = Modifier.weight(1f)) {
                BasicText(
                    text = "Clientes",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A), lineHeight = 22.sp),
                )
                BasicText(
                    text = "${filteredClients.size} clientes",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)),
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            RfIcon(kind = RfIconKind.SEARCH, tint = Color(0xFF5F6377), size = 18.dp)
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (searchQuery.isEmpty()) {
                    BasicText("Buscar por nombre o teléfono", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF5F6377)))
                }
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { updatedText -> searchQuery = updatedText },
                    singleLine = true,
                    textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF2E324A)),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        val filterChips = listOf("Todos · 4", "Con evento próximo · 2", "Empresas · 1")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            filterChips.forEachIndexed { chipIndex, chipLabel ->
                val isSelected = selectedFilterIndex == chipIndex
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .clip(CircleShape)
                        .then(
                            if (isSelected) {
                                Modifier.background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
                            } else {
                                Modifier.background(Color.White).border(1.dp, Color(0xFFE9E9F2), CircleShape)
                            },
                        )
                        .clickable { selectedFilterIndex = chipIndex }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        chipLabel,
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) Color.White else Color(0xFF2E324A),
                        ),
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp)),
        ) {
            Column {
                filteredClients.forEachIndexed { clientIndex, client ->
                    if (clientIndex > 0) {
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectScreenCode("A27") }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .rfIconButtonShadow(CircleShape, isAdmin = true)
                                .clip(CircleShape)
                                .background(client.avatarColors.first),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(client.letter, style = TextStyle(fontFamily = typo.displayFamily, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = client.avatarColors.second))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            BasicText(client.name, maxLines = 1, overflow = TextOverflow.Ellipsis, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E324A)))
                            BasicText(client.subtitle, maxLines = 1, overflow = TextOverflow.Ellipsis, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)))
                        }
                        Box(
                            modifier = Modifier
                                .height(24.dp)
                                .clip(CircleShape)
                                .background(client.tagColors.first)
                                .padding(horizontal = 9.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(client.tagText, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = client.tagColors.second))
                        }
                    }
                }
            }
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
    var internalNote by remember { mutableStateOf("Nota interna: prefiere tonos rosa y dorado. Escribirle por WhatsApp después de las 6 p. m.") }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp, start = 20.dp, end = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = true)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), CircleShape)
                        .clickable { onSelectScreenCode("BACK:A26") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                }

                BasicText(
                    text = "Cliente",
                    modifier = Modifier.weight(1f),
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF151729)),
                )

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = true)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), CircleShape)
                        .clickable { onSelectScreenCode("A05") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.EDIT, tint = Color(0xFF151729), size = 20.dp)
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFDE6F2)),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText("C", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, color = Color(0xFFC2338F), fontSize = 28.sp))
                }
                BasicText("Camila Rodríguez", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729), fontSize = 22.sp))
                BasicText("camila.rodriguez@correo.com · (809) 555-0198", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF5F6377), fontSize = 13.sp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                        .clickable { onSelectScreenCode("A15") },
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        RfIcon(kind = RfIconKind.PHONE, tint = Color(0xFF151729), size = 18.dp)
                        BasicText("Llamar", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF151729)))
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                        .clickable { onSelectScreenCode("A15") },
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        RfIcon(kind = RfIconKind.WHATSAPP, tint = Color(0xFF4E6A10), size = 18.dp)
                        BasicText("WhatsApp", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF4E6A10)))
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                        .clickable { onSelectScreenCode("A15") },
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        RfIcon(kind = RfIconKind.CHAT, tint = Color(0xFF4B47D6), size = 18.dp)
                        BasicText("Chat", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF4B47D6)))
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFFECEBFF))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                ) {
                    Column {
                        BasicText("3", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4B47D6), fontSize = 20.sp))
                        BasicText("Eventos", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, color = Color(0xFF4B47D6), fontSize = 12.sp))
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFFEEF6D6))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                ) {
                    Column {
                        BasicText("RD$ 3,210", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, color = Color(0xFF3F560C), fontSize = 20.sp))
                        BasicText("Pagado", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, color = Color(0xFF4E6A10), fontSize = 12.sp))
                    }
                }
            }

            BasicText("EVENTOS", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.96.sp, color = Color(0xFF5F6377)))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-4).dp)
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp)),
            ) {
                Column {
                    val clientEvents = listOf(
                        Triple(RfPhotoKey.BODA, "Boda Real Rosa & Dorado", "Vie 23 oct · Ajustado") to "A13",
                        Triple(RfPhotoKey.QUINCEANERA, "Quinceañera Valentina Crystal", "Lun 2 nov · Confirmado") to "A13",
                        Triple(RfPhotoKey.BABY_SHOWER, "Baby Shower Sofía en Rosas", "Lun 9 nov · Borrador") to "A13",
                    )

                    clientEvents.forEachIndexed { eventIndex, clientEventPair ->
                        val item = clientEventPair.first
                        val targetCode = clientEventPair.second
                        if (eventIndex > 0) {
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { if (targetCode.isNotEmpty()) onSelectScreenCode(targetCode) }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                            ) {
                                RfPhoto(photoKey = item.first, contentDescription = item.second, modifier = Modifier.fillMaxSize())
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                BasicText(item.second, maxLines = 1, overflow = TextOverflow.Ellipsis, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E324A)))
                                BasicText(item.third, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
                            }
                            RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = Color(0xFF5F6377), size = 18.dp)
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFFFFF8E5))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    RfIcon(kind = RfIconKind.EDIT, tint = Color(0xFF7A5700), size = 18.dp)
                    BasicTextField(
                        value = internalNote,
                        onValueChange = { updatedNote -> internalNote = updatedNote },
                        textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, lineHeight = 19.sp, color = Color(0xFF151729)),
                        modifier = Modifier.weight(1f),
                    )
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
    var selectedMemberName by remember { mutableStateOf("Yuli") }
    var permissionStates by remember { mutableStateOf(listOf(true, true, false, false, true)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 116.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(44.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .rfIconButtonShadow(CircleShape, isAdmin = true)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), CircleShape)
                    .clickable { onSelectScreenCode("BACK:A12") },
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
            }

            Column(modifier = Modifier.weight(1f)) {
                BasicText(
                    text = "Equipo",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A), lineHeight = 22.sp),
                )
                BasicText(
                    text = "4 personas",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)),
                )
            }

            Box(
                modifier = Modifier
                    .height(44.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
                    .clickable { selectedMemberName = "María" }
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    RfIcon(kind = RfIconKind.USER, tint = Color.White, size = 18.dp)
                    BasicText("Invitar", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White))
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp)),
        ) {
            Column {
                val teamMembers = listOf(
                    TeamMemberData("M", "María", "Dueña · acceso total", "Admin", Pair(Color(0xFFECEBFF), Color(0xFF4B47D6)), Pair(Color(0xFFECEBFF), Color(0xFF4B47D6)), selectedMemberName == "María"),
                    TeamMemberData("P", "Pedro", "Montaje · eventos y tareas", "Equipo", Pair(Color(0xFFDDF4F3), Color(0xFF1F8A84)), Pair(Color(0xFFDDF4F3), Color(0xFF1F8A84)), selectedMemberName == "Pedro"),
                    TeamMemberData("Y", "Yuli", "Decoración · eventos e inventario", "Equipo", Pair(Color(0xFFFDE6F2), Color(0xFFC2338F)), Pair(Color(0xFFDDF4F3), Color(0xFF1F8A84)), selectedMemberName == "Yuli"),
                    TeamMemberData("A", "Andrés", "Chofer · solo modo ejecución", "Limitado", Pair(Color(0xFFFFF2C9), Color(0xFF7A5700)), Pair(Color(0xFFF2F2F8), Color(0xFF5F6377)), selectedMemberName == "Andrés"),
                )

                teamMembers.forEachIndexed { memberIndex, member ->
                    if (memberIndex > 0) {
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (member.isHighlighted) Color(0xFFF7F6FF) else Color.White)
                            .clickable { selectedMemberName = member.name }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(member.avatarColors.first),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(member.initial, style = TextStyle(fontFamily = typo.displayFamily, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = member.avatarColors.second))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            BasicText(member.name, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E324A)))
                            BasicText(member.role, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)))
                        }
                        Box(
                            modifier = Modifier
                                .height(24.dp)
                                .clip(CircleShape)
                                .background(member.tagColors.first)
                                .padding(horizontal = 9.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(member.tagText, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = member.tagColors.second))
                        }
                    }
                }
            }
        }

        BasicText(
            text = "PERMISOS DE ${selectedMemberName.uppercase()}",
            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = Color(0xFF5F6377)),
            modifier = Modifier.padding(top = 2.dp),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-4).dp)
                .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp)),
        ) {
            Column {
                val permissionLabels = listOf(
                    "Ver y editar eventos",
                    "Inventario y artículos",
                    "Pagos y cobros",
                    "Reportes del negocio",
                    "Usar Rosa IA para crear eventos",
                )

                permissionLabels.forEachIndexed { permIndex, label ->
                    val isPermissionActive = permissionStates[permIndex]
                    if (permIndex > 0) {
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val updatedPerms = permissionStates.toMutableList()
                                updatedPerms[permIndex] = !updatedPerms[permIndex]
                                permissionStates = updatedPerms
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        BasicText(label, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF151729)))
                        Box(
                            modifier = Modifier
                                .size(width = 46.dp, height = 28.dp)
                                .clip(CircleShape)
                                .background(if (isPermissionActive) Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))) else SolidColor(Color(0xFFDCDDEA)))
                                .padding(3.dp),
                            contentAlignment = if (isPermissionActive) Alignment.CenterEnd else Alignment.CenterStart,
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
            }
        }
    }
}
