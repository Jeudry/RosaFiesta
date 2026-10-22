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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 54.dp, start = 20.dp, end = 20.dp),
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
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    BasicText("Ingresos oct", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF5F6377)))
                    BasicText("RD$ 12,450", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E3048)))
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    BasicText("Eventos activos", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF5F6377)))
                    BasicText("4", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E3048)))
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFFFF2C9))
                    .clickable { onSelectScreenCode("A02") }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    BasicText("Solicitudes", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF7A5700)))
                    BasicText("2 nuevas", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7A5700)))
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
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFFE9E8FF), Color(0xFFDDF2F6))))
                        .clickable { onSelectScreenCode("A08") }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        RosaAvatar(size = RosaAvatarSize.MEDIUM, customSize = 36.dp, forceAdminPalette = true)
                        Column(modifier = Modifier.weight(1f)) {
                            BasicText("Crear con\nRosa IA", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E324A), lineHeight = 17.sp))
                            BasicText("Arma el borrador", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)))
                        }
                    }
                }

                // Crear manual
                Box(
                    modifier = Modifier
                        .weight(1f)
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
                        BasicText("Solicitudes", modifier = Modifier.weight(1f, fill = false), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E324A)))
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
            .padding(top = 54.dp, start = 20.dp, end = 20.dp),
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
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), CircleShape)
                    .clickable { onSelectScreenCode("A01") },
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

        // Segments
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFEDEDF5), RoundedCornerShape(14.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center,
            ) {
                BasicText("Nuevas · 2", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Normal, color = Color(0xFF2E324A)))
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp),
                contentAlignment = Alignment.Center,
            ) {
                BasicText("En revisión · 1", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)))
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp),
                contentAlignment = Alignment.Center,
            ) {
                BasicText("Enviadas", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)))
            }
        }

        // Card 1: Cumpleaños de Mateo
        Box(
            modifier = Modifier
                .fillMaxWidth()
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
                            .background(Color(0xFFEEF6D6)),
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.WHATSAPP, tint = Color(0xFF4E6A10), size = 18.dp)
                    }

                    Box(
                        modifier = Modifier
                            .height(40.dp)
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

        // Card 2: Graduación Promo 2026
        Box(
            modifier = Modifier
                .fillMaxWidth()
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
                            .background(Color(0xFFEEF6D6)),
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.WHATSAPP, tint = Color(0xFF4E6A10), size = 18.dp)
                    }

                    Box(
                        modifier = Modifier
                            .height(40.dp)
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

        // Info footer
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

// ==============================================================================
// A03 · COTIZADOR RÁPIDO ADMIN
// ==============================================================================
@Composable
internal fun RenderAdminQuoteBuilderA03(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 54.dp, start = 20.dp, end = 20.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
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
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), CircleShape)
                        .clickable { onSelectScreenCode("A02") },
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

            // Client Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
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
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.dp, Color(0xFFECECF5), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.WHATSAPP, tint = Color(0xFF4E6A10), size = 20.dp)
                    }
                }
            }

            // Section Label
            BasicText(
                text = "ARTÍCULOS",
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.96.sp, color = Color(0xFF5F6377)),
            )

            // Articles Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-4).dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp)),
            ) {
                Column {
                    val quoteArticles = listOf(
                        Triple(RfPhotoKey.ARCO_ROSA, "Arco de globos rosa", "RD$ 95" to ("Disponible" to 1)),
                        Triple(RfPhotoKey.MESA_HELADOS, "Mesa dulce temática", "RD$ 150" to ("1 libre de 2" to 1)),
                        Triple(RfPhotoKey.SILLA_TIFFANY, "Silla Tiffany Cristal", "RD$ 4" to ("80 libres" to 40)),
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

                            // Stepper [-] qty [+]
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
                                    modifier = Modifier.size(28.dp).clip(CircleShape),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    RfIcon(kind = RfIconKind.CLOSE, tint = Color(0xFF5B6CF0), size = 14.dp)
                                }
                                BasicText(
                                    text = "$qty",
                                    modifier = Modifier.defaultMinSize(minWidth = 22.dp),
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E324A)),
                                )
                                Box(
                                    modifier = Modifier.size(28.dp).clip(CircleShape),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    RfIcon(kind = RfIconKind.PLUS, tint = Color(0xFF5B6CF0), size = 14.dp)
                                }
                            }
                        }
                    }

                    // Bottom CTA: Agregar del inventario
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

            // Transporte e instalación field
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
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
                    BasicText("RD$ 250", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Normal, color = Color(0xFF2E324A)))
                }
            }

            // Nota field
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 70.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                BasicText(
                    text = "Incluye montaje 2 horas antes. El arco va en la entrada principal.",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF151729), lineHeight = 19.sp),
                )
            }
        }

        // Docked Bottom Bar
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
                    BasicText("Total · 3 artículos", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
                    BasicText("RD$ 655", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)))
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 54.dp, start = 20.dp, end = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Header
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
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
                    .clickable { onSelectScreenCode("A10") },
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.PLUS, tint = Color.White, size = 22.dp)
            }
        }

        // Search Field
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
                BasicText(
                    text = "Buscar artículo",
                    modifier = Modifier.weight(1f),
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF5F6377)),
                )
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFECEBFF)),
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.FILTER, tint = Color(0xFF4B47D6), size = 18.dp)
                }
            }
        }

        // Category Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .height(36.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                BasicText("Todos", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White))
            }
            listOf("Mobiliario", "Floral", "Globos", "Mesa dulce").forEach { cat ->
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE9E9F2), CircleShape)
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(cat, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Normal, color = Color(0xFF2E324A)))
                }
            }
        }

        // 3 Stat Boxes
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
                    BasicText("12", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF3F560C)))
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
                    BasicText("2", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4B47D6)))
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
                    BasicText("1", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFDC2B48)))
                    BasicText("Stock bajo", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, color = Color(0xFFDC2B48)))
                }
            }
        }

        // Articles Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp)),
        ) {
            Column {
                val inventoryItems = listOf(
                    Triple(RfPhotoKey.SILLA_TIFFANY, "Silla Tiffany Cristal", "120 unid. · RD$ 4/día") to ("Activo" to (Color(0xFFEEF6D6) to Color(0xFF4E6A10))),
                    Triple(RfPhotoKey.MESA_REDONDA, "Mesa redonda", "12 unid. · RD$ 25/día") to ("Activo" to (Color(0xFFEEF6D6) to Color(0xFF4E6A10))),
                    Triple(RfPhotoKey.CENTRO_FLORAL, "Centro de mesa floral", "20 unid. · RD$ 35/día") to ("Activo" to (Color(0xFFEEF6D6) to Color(0xFF4E6A10))),
                    Triple(RfPhotoKey.ARCO_ROSA, "Arco de globos rosa", "3 unid. · RD$ 95/día") to ("Activo" to (Color(0xFFEEF6D6) to Color(0xFF4E6A10))),
                    Triple(RfPhotoKey.MESA_HELADOS, "Mesa dulce temática", "2 unid. · RD$ 150/día") to ("Stock bajo" to (Color(0xFFFDE3EA) to Color(0xFFDC2B48))),
                )

                inventoryItems.forEachIndexed { index, (item, tagInfo) ->
                    if (index > 0) {
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectScreenCode("A23") }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(12.dp)),
                        ) {
                            RfPhoto(photoKey = item.first, contentDescription = item.second, modifier = Modifier.fillMaxSize())
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(3.dp),
                        ) {
                            BasicText(item.second, maxLines = 1, overflow = TextOverflow.Ellipsis, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E324A)))
                            BasicText(item.third, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
                        }

                        Box(
                            modifier = Modifier
                                .height(24.dp)
                                .background(tagInfo.second.first, CircleShape)
                                .padding(horizontal = 9.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(tagInfo.first, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = tagInfo.second.second))
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 54.dp, start = 20.dp, end = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Header
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
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), CircleShape)
                    .clickable { onSelectScreenCode("s39") },
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.CALENDAR, tint = Color(0xFF151729), size = 20.dp)
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
                    .clickable { onSelectScreenCode("A04") },
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.PLUS, tint = Color.White, size = 22.dp)
            }
        }

        // Week Calendar Strip
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            val days = listOf(
                Triple("L", "19", false to null),
                Triple("M", "20", true to null),
                Triple("X", "21", false to null),
                Triple("J", "22", false to Color(0xFFB9D84A)),
                Triple("V", "23", false to Color(0xFFFFD45C)),
                Triple("S", "24", false to null),
                Triple("D", "25", false to null),
            )

            days.forEach { (day, num, state) ->
                val (isSelected, dotColor) = state
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
                        ),
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

        // Segments
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFEDEDF5), RoundedCornerShape(14.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center,
            ) {
                BasicText("Próximos · 4", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Normal, color = Color(0xFF2E324A)))
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp),
                contentAlignment = Alignment.Center,
            ) {
                BasicText("Por cobrar · 2", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)))
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp),
                contentAlignment = Alignment.Center,
            ) {
                BasicText("Pasados", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)))
            }
        }

        // 4 Event Cards
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            val eventList = listOf(
                Quadruple(RfPhotoKey.BODA, "Boda Real Rosa & Dorado", "Vie 23 oct · Camila R.", Triple("Ajustado", Color(0xFFDDF4F3), Color(0xFF1F8A84)) to 0.3f),
                Quadruple(RfPhotoKey.QUINCEANERA, "Quinceañera Valentina Crystal", "Lun 2 nov · Camila R.", Triple("Confirmado", Color(0xFFEEF6D6), Color(0xFF4E6A10)) to 0.3f),
                Quadruple(RfPhotoKey.CUMPLE_INFANTIL, "Cumpleaños de Mateo", "Sáb 7 nov · Laura Peña", Triple("Cotización enviada", Color(0xFFFFF2C9), Color(0xFF7A5700)) to 0.0f),
                Quadruple(RfPhotoKey.BABY_SHOWER, "Baby Shower Sofía en Rosas", "Dom 15 nov · Rosa Gómez", Triple("Borrador", Color(0xFFF2F2F8), Color(0xFF5F6377)) to 0.0f),
            )

            eventList.forEach { (photo, title, subtitle, meta) ->
                val (tagTriple, progress) = meta
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
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
            .padding(top = 54.dp, start = 20.dp, end = 20.dp),
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
                        .clickable { onSelectScreenCode("A00") }
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
                .padding(top = 54.dp, start = 20.dp, end = 20.dp)
                .verticalScroll(rememberScrollState()),
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
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), CircleShape)
                        .clickable { onSelectScreenCode("A12") },
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

            // Segments
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
                            .background(Color.White),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText("Por cobrar · 3", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Normal, color = Color(0xFF2E324A)))
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText("Recibidos", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)))
                    }
                }
            }

            // Por cobrar list
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp)),
            ) {
                Column {
                    val pending = listOf(
                        Triple("C" to Color(0xFFFDE6F2) to Color(0xFFC2338F), "Boda Real · Camila R." to ("Vence el jue 22 oct" to Color(0xFFB42345)), "RD$ 3,150"),
                        Triple("C" to Color(0xFFFDE6F2) to Color(0xFFC2338F), "Quinceañera · Camila R." to ("Vence el vie 30 oct" to Color(0xFF7A5700)), "RD$ 4,340"),
                        Triple("L" to Color(0xFFEEF6D6) to Color(0xFF4E6A10), "Cumpleaños de Mateo · Laura P." to ("Anticipo para reservar" to Color(0xFF5F6377)), "RD$ 234"),
                    )

                    pending.forEachIndexed { idx, (avatar, info, amount) ->
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
                                    modifier = Modifier.clickable {},
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    RfIcon(kind = RfIconKind.WHATSAPP, tint = Color(0xFF4B47D6), size = 13.dp)
                                    BasicText("Recordar", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF4B47D6)))
                                }
                            }
                        }
                    }
                }
            }

            // Recibidos recientes
            BasicText("RECIBIDOS RECIENTES", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5F6377), letterSpacing = 1.sp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
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
) {
    val typo = LocalRfTypography.current

    Box(modifier = Modifier.fillMaxSize()) {
        // Fondo A20 subyacente
        RenderAdminPaymentsA20(onSelectScreenCode = onSelectScreenCode)

        // Overlay oscuro
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x73141632))
                .clickable { onSelectScreenCode("A20") },
        )

        // Bottom sheet dialog
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Color.White)
                .padding(top = 12.dp, start = 20.dp, end = 20.dp, bottom = 34.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Drag handle
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

                // Selector evento
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(14.dp))
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
                            BasicText("C", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFC2338F)))
                        }

                        BasicText("Boda Real · Camila R.", modifier = Modifier.weight(1f), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Normal, color = Color(0xFF2E324A)))

                        RfIcon(kind = RfIconKind.ARROW_RIGHT, tint = Color(0xFF5F6377), size = 18.dp)
                    }
                }

                // Monto
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    BasicText("Monto", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)))
                    BasicText("RD$ 3,150", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 40.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729), lineHeight = 44.sp))
                    BasicText("Salda el evento", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4E6A10)))
                }

                // Chips método
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .clip(CircleShape)
                            .background(AdminButtonGradient)
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText("Transferencia", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White))
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
                        BasicText("Efectivo", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Normal, color = Color(0xFF2E324A)))
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
                        BasicText("Tarjeta", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Normal, color = Color(0xFF2E324A)))
                    }
                }

                // 2 fields
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
                        BasicText("Banco Popular", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Normal, color = Color(0xFF2E324A)))
                    }

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
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            RfIcon(kind = RfIconKind.CALENDAR, tint = Color(0xFF5F6377), size = 18.dp)
                            BasicText("Hoy, 20 oct", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Normal, color = Color(0xFF2E324A)))
                        }
                    }
                }

                // Dashed comprobante
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(2.dp, Color(0xFFC9CAE6), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText("Adjuntar comprobante", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B47D6)))
                }

                // Switch WhatsApp
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
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
                            .background(AdminButtonGradient)
                            .padding(3.dp),
                        contentAlignment = Alignment.CenterEnd,
                    ) {
                        Box(modifier = Modifier.size(22.dp).clip(CircleShape).background(Color.White))
                    }
                }

                // CTA
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(AdminButtonGradient)
                        .clickable { onSelectScreenCode("A20") },
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
                .padding(top = 54.dp, start = 20.dp, end = 20.dp)
                .verticalScroll(rememberScrollState()),
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
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), CircleShape)
                        .clickable { onSelectScreenCode("A11") },
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

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            // Header curved banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0), Color(0xFF2FB8C8))))
                    .padding(top = 52.dp, start = 20.dp, end = 20.dp, bottom = 22.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Top row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0x2EFFFFFF))
                                .clickable { onSelectScreenCode("A13") },
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

                    // Gauge + title
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        // Circular gauge 5/9
                        Box(
                            modifier = Modifier.size(78.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawCircle(color = Color(0x38FFFFFF), style = Stroke(width = 8.dp.toPx()))
                                drawArc(
                                    color = Color(0xFFFFD45C),
                                    startAngle = -90f,
                                    sweepAngle = (5f / 9f) * 360f,
                                    useCenter = false,
                                    style = Stroke(width = 8.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round),
                                )
                            }
                            BasicText("5/9", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White))
                        }

                        Column {
                            BasicText("Boda Real Rosa & Dorado", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White))
                            Spacer(modifier = Modifier.height(4.dp))
                            BasicText("Montaje 2:00 p. m. · Jardín Botánico", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFFE9EEFF)))
                        }
                    }
                }
            }

            // Body
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp, start = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Segment
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFEDEDF5))
                        .padding(4.dp),
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.weight(1f).height(38.dp), contentAlignment = Alignment.Center) {
                            BasicText("Carga · 3/3", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)))
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText("Montaje · 2/4", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Normal, color = Color(0xFF2E324A)))
                        }
                        Box(modifier = Modifier.weight(1f).height(38.dp), contentAlignment = Alignment.Center) {
                            BasicText("Desmontaje", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)))
                        }
                    }
                }

                // Venue card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
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

                // Tasks list
                val executionTasks = listOf(
                    QuadExecution(true, "Colocar 12 mesas redondas", "Según el plano aprobado", "P", Color(0xFFDDF4F3) to Color(0xFF1F8A84)),
                    QuadExecution(true, "Vestir mesas y sillas Tiffany", "120 sillas · mantel blanco", "Y", Color(0xFFFDE6F2) to Color(0xFFC2338F)),
                    QuadExecution(false, "Montar el arco de globos", "Entrada principal · rosa y dorado", "Y", Color(0xFFFDE6F2) to Color(0xFFC2338F)),
                    QuadExecution(false, "Centros de mesa florales", "12 centros, uno por mesa", "M", Color(0xFFECEBFF) to Color(0xFF4B47D6)),
                )

                executionTasks.forEach { task ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (task.done) Color(0xFFF6F6FB) else Color.White)
                            .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
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

        // Bottom CTA
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 54.dp, start = 20.dp, end = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
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
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), CircleShape)
                    .clickable { onSelectScreenCode("A13") },
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
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
                    .clickable { /* Nueva tarea */ },
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.PLUS, tint = Color.White, size = 22.dp)
            }
        }

        // Chips row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-2).dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Chip Todos (selected)
            Box(
                modifier = Modifier
                    .height(36.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                BasicText("Todos", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White))
            }

            val members = listOf(
                Triple("M", "María", Pair(Color(0xFFECEBFF), Color(0xFF4B47D6))),
                Triple("P", "Pedro", Pair(Color(0xFFDDF4F3), Color(0xFF1F8A84))),
                Triple("Y", "Yuli", Pair(Color(0xFFFDE6F2), Color(0xFFC2338F))),
                Triple("A", "Andrés", Pair(Color(0xFFFFF2C9), Color(0xFF7A5700))),
            )

            members.forEach { (initial, name, colors) ->
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE9E9F2), CircleShape)
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
                        BasicText(name, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Normal, color = Color(0xFF2E324A)))
                    }
                }
            }
        }

        // Section HOY · MARTES 20
        BasicText(
            text = "HOY · MARTES 20",
            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = Color(0xFF5F6377)),
            modifier = Modifier.padding(top = 2.dp),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-4).dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp)),
        ) {
            Column {
                // Task 1 (Done)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF8DB52C)),
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.CHECK, tint = Color.White, size = 14.dp)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        BasicText("Confirmar paleta floral", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF5F6377), textDecoration = TextDecoration.LineThrough))
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

                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))

                // Task 2 (Pending)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .border(2.dp, Color(0xFFC9CAE6), CircleShape),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        BasicText("Recordar pago restante a Camila", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E324A)))
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

        // Section JUEVES 22 · VÍSPERA
        BasicText(
            text = "JUEVES 22 · VÍSPERA",
            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = Color(0xFF5F6377)),
            modifier = Modifier.padding(top = 2.dp),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-4).dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp)),
        ) {
            Column {
                // Task 3
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .border(2.dp, Color(0xFFC9CAE6), CircleShape),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        BasicText("Inflar globos del arco", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF151729)))
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

                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))

                // Task 4
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .border(2.dp, Color(0xFFC9CAE6), CircleShape),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        BasicText("Revisar y limpiar 120 sillas", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF151729)))
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

        // Section VIERNES 23 · DÍA DEL EVENTO
        BasicText(
            text = "VIERNES 23 · DÍA DEL EVENTO",
            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = Color(0xFF5F6377)),
            modifier = Modifier.padding(top = 2.dp),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-4).dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp)),
        ) {
            Column {
                // Task 5
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .border(2.dp, Color(0xFFC9CAE6), CircleShape),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        BasicText("Cargar el camión", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF151729)))
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

                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))

                // Task 6
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .border(2.dp, Color(0xFFC9CAE6), CircleShape),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        BasicText("Montaje completo", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF151729)))
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

// ==============================================================================
// A18 · PLANO Y DISTRIBUCIÓN
// ==============================================================================
@Composable
internal fun RenderAdminSketchA18(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 54.dp, start = 20.dp, end = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
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
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), CircleShape)
                        .clickable { onSelectScreenCode("A13") },
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

            // Segmented
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFEDEDF5))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText("Boceto original", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)))
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .shadow(2.dp, RoundedCornerShape(12.dp), spotColor = Color(0x334650B4))
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText("Plano", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Normal, color = Color(0xFF2E324A)))
                }
            }

            // Card with interactive floor plan
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(430.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp)),
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val scaleX = size.width / 358f
                    val scaleY = size.height / 440f
                    scale(scaleX, scaleY, Offset.Zero) {
                        // Tarima
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

                        // Pista
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

                        // 6 Mesas redondas con 10 sillas cada una
                        val tables = listOf(
                            Offset(110f, 190f),
                            Offset(200f, 190f),
                            Offset(70f, 270f),
                            Offset(290f, 270f),
                            Offset(70f, 360f),
                            Offset(290f, 360f),
                        )

                        tables.forEach { tableCenter ->
                            drawCircle(color = Color(0xFFECEBFF), radius = 22f, center = tableCenter)
                            drawCircle(color = Color(0xFF5B6CF0), radius = 22f, center = tableCenter, style = Stroke(width = 2f))
                            for (i in 0 until 10) {
                                val angle = i * (kotlin.math.PI * 2 / 10)
                                val chairX = tableCenter.x + kotlin.math.cos(angle).toFloat() * 31f
                                val chairY = tableCenter.y + kotlin.math.sin(angle).toFloat() * 31f
                                drawCircle(color = Color(0xFFFDE6F2), radius = 3.6f, center = Offset(chairX, chairY))
                                drawCircle(color = Color(0xFFEA4EA9), radius = 3.6f, center = Offset(chairX, chairY), style = Stroke(width = 1.2f))
                            }
                        }

                        // Entrada chevron
                        val entrancePath = PathParser().parsePathString("M150 428L179 404L208 428").toPath()
                        drawPath(entrancePath, color = Color(0xFF151729), style = Stroke(width = 2.4f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                    }
                }

                // Labels over canvas
                Box(modifier = Modifier.fillMaxSize()) {
                    BasicText("Tarima", modifier = Modifier.align(Alignment.TopCenter).offset(y = 52.dp), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5F6377)))
                    BasicText("Pista", modifier = Modifier.align(Alignment.Center).offset(y = 45.dp), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5F6377)))
                    BasicText("Entrada", modifier = Modifier.align(Alignment.BottomCenter).offset(y = (-45).dp), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5F6377)))

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
                            BasicText("6 mesas redondas", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B47D6)))
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
                            BasicText("60 sillas", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC2338F)))
                        }
                    }
                }
            }

            // Rosa AI banner
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
                    text = "Caben 6 mesas más sin tapar la pista, para llegar a los 120 invitados.",
                    modifier = Modifier.weight(1f),
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF151729), lineHeight = 18.sp),
                )
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText("Aplicar", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White))
                }
            }
        }

        // Bottom floating button
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

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 54.dp, start = 20.dp, end = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
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
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), CircleShape)
                        .clickable { onSelectScreenCode("A13") },
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

            // Section 1: Devolución de artículos
            BasicText(
                text = "DEVOLUCIÓN DE ARTÍCULOS",
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = Color(0xFF5F6377)),
                modifier = Modifier.padding(top = 2.dp),
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-4).dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp)),
            ) {
                Column {
                    // Item 1: Silla Tiffany
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

                    // Item 2: Mesa redonda
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

                    // Item 3: Centro de mesa floral (incomplete)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFF8E5))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        RfPhoto(photoKey = RfPhotoKey.CENTRO_FLORAL, contentDescription = "Centro floral", modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)))
                        BasicText("Centro de mesa floral", modifier = Modifier.weight(1f), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF151729)))
                        BasicText("9/10", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7A5700)))
                        RfIcon(kind = RfIconKind.WARNING, tint = Color(0xFFE0A100), size = 22.dp)
                    }
                }
            }

            // Section 2: Daños y faltantes
            BasicText(
                text = "DAÑOS Y FALTANTES",
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = Color(0xFF5F6377)),
                modifier = Modifier.padding(top = 2.dp),
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-4).dp)
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
                            .size(64.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(2.dp, Color(0xFFC9CAE6), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.CAMERA, tint = Color(0xFF4B47D6), size = 22.dp)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        BasicText("1 centro de mesa con la base rota", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)))
                        BasicText("Agrega una foto para el registro", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)))
                    }
                    BasicText("RD$ 35", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFB42345)))
                }
            }

            // Section 3: Cobro final
            BasicText(
                text = "COBRO FINAL",
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = Color(0xFF5F6377)),
                modifier = Modifier.padding(top = 2.dp),
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-4).dp)
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

        // Bottom floating action button
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
) {
    val typo = LocalRfTypography.current

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 54.dp, start = 20.dp, end = 20.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
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
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), CircleShape)
                        .clickable { onSelectScreenCode("A09") },
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

            // Photos Row
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
                        .border(2.dp, Color(0xFFC9CAE6), RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        RfIcon(kind = RfIconKind.CAMERA, tint = Color(0xFF4B47D6), size = 22.dp)
                        BasicText("Agregar foto", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF4B47D6)))
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

            // Field Nombre
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
                    BasicText("Mesa plegable 1.8 m", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Normal, color = Color(0xFF2E324A)))
                }
            }

            // Categoría
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BasicText("Categoría", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF3A3D52)))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            RfIcon(kind = RfIconKind.BOX, tint = Color.White, size = 16.dp)
                            BasicText("Mobiliario", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White))
                        }
                    }

                    listOf("Floral", "Globos", "Iluminación").forEach { cat ->
                        Box(
                            modifier = Modifier
                                .height(36.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(1.dp, Color(0xFFE9E9F2), CircleShape)
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(cat, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Normal, color = Color(0xFF2E324A)))
                        }
                    }
                }
            }

            // Precio y Cantidad
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
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            BasicText("RD$", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Normal, color = Color(0xFF2E324A)))
                            BasicText("20", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Normal, color = Color(0xFF2E324A)))
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    BasicText("Cantidad", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF3A3D52)))
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            BasicText("30", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E324A)))
                            BasicText("unid.", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF5F6377)))
                        }
                    }
                }
            }

            // Descripción
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BasicText("Descripción", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF3A3D52)))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                ) {
                    BasicText("Resina blanca, para 8 personas. Ideal para exteriores.", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF2E324A)))
                }
            }

            // Visible en el catálogo Switch
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(16.dp))
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

                    // Switch ON
                    Box(
                        modifier = Modifier
                            .width(50.dp)
                            .height(30.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
                            .padding(3.dp),
                        contentAlignment = Alignment.CenterEnd,
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

        // Docked Guardar artículo button
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
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
                    .clickable { onSelectScreenCode("A09") },
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
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        // Foto superior 250dp
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
        ) {
            RfPhoto(photoKey = RfPhotoKey.SILLA_TIFFANY, contentDescription = "Silla Tiffany Cristal", modifier = Modifier.fillMaxSize())
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
                .padding(top = 54.dp, start = 20.dp, end = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), CircleShape)
                    .clickable { onSelectScreenCode("A09") },
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
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
                .fillMaxWidth()
                .padding(top = 226.dp)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Color(0xFFF6F6FB))
                .padding(top = 20.dp, start = 20.dp, end = 20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Título y precio
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        BasicText("Silla Tiffany Cristal", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A)))
                        BasicText("Mobiliario · Visible en el catálogo", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)))
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        BasicText("RD$ 4", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)))
                        BasicText("por día", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
                    }
                }

                // 3 stat cards
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                    ) {
                        Column {
                            BasicText("120", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)))
                            BasicText("En total", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                    ) {
                        Column {
                            BasicText("116", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4E6A10)))
                            BasicText("En buen estado", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                    ) {
                        Column {
                            BasicText("4", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF7A5700)))
                            BasicText("En reparación", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
                        }
                    }
                }

                // Calendario reservas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 54.dp, start = 20.dp, end = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
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
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), CircleShape)
                    .clickable { onSelectScreenCode("A12") },
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
                    text = "4 activos en el catálogo",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)),
                )
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
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

        pkgs.forEach { item ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
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

                    // Switch (active)
                    Box(
                        modifier = Modifier
                            .size(width = 46.dp, height = 28.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
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
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 54.dp, start = 20.dp, end = 20.dp)
                .verticalScroll(rememberScrollState()),
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
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), CircleShape)
                        .clickable { onSelectScreenCode("A24") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText("Editar paquete", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A)))
                    BasicText("Bodas Rosa", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)))
                }
            }

            // Foto + Nombre
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
                        BasicText("Bodas Rosa", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Normal, color = Color(0xFF2E324A)))
                    }
                }
            }

            BasicText("INCLUYE", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5F6377), letterSpacing = 1.sp))

            // Card artículos incluidos
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp)),
            ) {
                Column {
                    val packageItems = listOf(
                        Triple(RfPhotoKey.ARCO_ROSA, "Arco de globos rosa", "RD$ 95 c/u" to "1"),
                        Triple(RfPhotoKey.CENTRO_FLORAL, "Centro de mesa floral", "RD$ 35 c/u" to "10"),
                        Triple(RfPhotoKey.SILLA_TIFFANY, "Silla Tiffany Cristal", "RD$ 4 c/u" to "120"),
                        Triple(RfPhotoKey.MESA_REDONDA, "Mesa redonda", "RD$ 25 c/u" to "3"),
                    )

                    packageItems.forEachIndexed { idx, (photo, name, info) ->
                        if (idx > 0) {
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
                                    Box(modifier = Modifier.size(28.dp), contentAlignment = Alignment.Center) {
                                        BasicText("−", style = TextStyle(fontSize = 16.sp, color = Color(0xFF5B6CF0), fontWeight = FontWeight.Normal))
                                    }
                                    BasicText(
                                        info.second,
                                        modifier = Modifier.widthIn(min = 28.dp),
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Normal, color = Color(0xFF2E324A), textAlign = androidx.compose.ui.text.style.TextAlign.Center),
                                    )
                                    Box(modifier = Modifier.size(28.dp), contentAlignment = Alignment.Center) {
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
                            .clickable {},
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            RfIcon(kind = RfIconKind.PLUS, tint = Color(0xFF4B47D6), size = 16.dp)
                            BasicText("Agregar artículo", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B47D6)))
                        }
                    }
                }
            }

            // Descuento Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        BasicText("Descuento del paquete", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3A3D52)))
                        BasicText("15%", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4B47D6)))
                    }

                    // Track slider 30% fill
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
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            BasicText("Precio normal ", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF5F6377)))
                            BasicText("RD$ 1,000", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF5F6377), textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough))
                        }
                        BasicText("RD$ 850", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)))
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 54.dp, start = 20.dp, end = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
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
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), CircleShape)
                    .clickable { onSelectScreenCode("A12") },
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
                    text = "4 clientes",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)),
                )
            }
        }

        // Search Field
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
            BasicText("Buscar por nombre o teléfono", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF5F6377)))
        }

        // Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .height(36.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                BasicText("Todos · 4", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White))
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
                BasicText("Con evento próximo · 2", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Normal, color = Color(0xFF2E324A)))
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
                BasicText("Empresas · 1", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Normal, color = Color(0xFF2E324A)))
            }
        }

        // Card with 4 clients
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp)),
        ) {
            Column {
                val clientList = listOf(
                    ClientItemData("C", "Camila Rodríguez", "3 eventos · pagó RD$ 3,210", "Vie 23 oct", Pair(Color(0xFFFDE6F2), Color(0xFFC2338F)), Pair(Color(0xFFFFF2C9), Color(0xFF7A5700))),
                    ClientItemData("L", "Laura Peña", "Cotización enviada", "Sáb 7 nov", Pair(Color(0xFFEEF6D6), Color(0xFF4E6A10)), Pair(Color(0xFFECEBFF), Color(0xFF4B47D6))),
                    ClientItemData("A", "Ana Gómez", "Solicitud nueva", "Nueva", Pair(Color(0xFFFFF2C9), Color(0xFF7A5700)), Pair(Color(0xFFFFD45C), Color(0xFF2E1A47))),
                    ClientItemData("G", "Grupo Caribe", "Empresa · 1 evento", "Cerrado", Pair(Color(0xFFF2F2F8), Color(0xFF5F6377)), Pair(Color(0xFFF2F2F8), Color(0xFF5F6377))),
                )

                clientList.forEachIndexed { idx, client ->
                    if (idx > 0) {
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

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 54.dp, start = 20.dp, end = 20.dp)
                .verticalScroll(rememberScrollState()),
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
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), CircleShape)
                        .clickable { onSelectScreenCode("A26") },
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
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.EDIT, tint = Color(0xFF151729), size = 20.dp)
                }
            }

            // Cabecera Cliente
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

            // 3 Action Buttons: Llamar, WhatsApp, Chat
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                        .clickable {},
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
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                        .clickable {},
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

            // Métricas del cliente
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

            // Event list card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-4).dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp)),
            ) {
                Column {
                    val clientEvents = listOf(
                        Triple(RfPhotoKey.BODA, "Boda Real Rosa & Dorado", "Vie 23 oct · Ajustado") to "A13",
                        Triple(RfPhotoKey.QUINCEANERA, "Quinceañera Valentina Crystal", "Lun 2 nov · Confirmado") to "",
                        Triple(RfPhotoKey.BABY_SHOWER, "Baby Shower Sofía en Rosas", "Lun 9 nov · Borrador") to "",
                    )

                    clientEvents.forEachIndexed { idx, (item, targetCode) ->
                        if (idx > 0) {
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

            // Nota interna
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFFFFF8E5))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    RfIcon(kind = RfIconKind.EDIT, tint = Color(0xFF7A5700), size = 18.dp)
                    BasicText(
                        text = "Nota interna: prefiere tonos rosa y dorado. Escribirle por WhatsApp después de las 6 p. m.",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, lineHeight = 19.sp, color = Color(0xFF151729)),
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 54.dp, start = 20.dp, end = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
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
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), CircleShape)
                    .clickable { onSelectScreenCode("A12") },
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
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    RfIcon(kind = RfIconKind.USER, tint = Color.White, size = 18.dp)
                    BasicText("Invitar", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White))
                }
            }
        }

        // Card 1: Team members
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp)),
        ) {
            Column {
                val teamMembers = listOf(
                    TeamMemberData("M", "María", "Dueña · acceso total", "Admin", Pair(Color(0xFFECEBFF), Color(0xFF4B47D6)), Pair(Color(0xFFECEBFF), Color(0xFF4B47D6)), false),
                    TeamMemberData("P", "Pedro", "Montaje · eventos y tareas", "Equipo", Pair(Color(0xFFDDF4F3), Color(0xFF1F8A84)), Pair(Color(0xFFDDF4F3), Color(0xFF1F8A84)), false),
                    TeamMemberData("Y", "Yuli", "Decoración · eventos e inventario", "Equipo", Pair(Color(0xFFFDE6F2), Color(0xFFC2338F)), Pair(Color(0xFFDDF4F3), Color(0xFF1F8A84)), true),
                    TeamMemberData("A", "Andrés", "Chofer · solo modo ejecución", "Limitado", Pair(Color(0xFFFFF2C9), Color(0xFF7A5700)), Pair(Color(0xFFF2F2F8), Color(0xFF5F6377)), false),
                )

                teamMembers.forEachIndexed { idx, member ->
                    if (idx > 0) {
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (member.isHighlighted) Color(0xFFF7F6FF) else Color.White)
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

        // Section 2: Permisos de Yuli
        BasicText(
            text = "PERMISOS DE YULI",
            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = Color(0xFF5F6377)),
            modifier = Modifier.padding(top = 2.dp),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-4).dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp)),
        ) {
            Column {
                val perms = listOf(
                    Pair("Ver y editar eventos", true),
                    Pair("Inventario y artículos", true),
                    Pair("Pagos y cobros", false),
                    Pair("Reportes del negocio", false),
                    Pair("Usar Rosa IA para crear eventos", true),
                )

                perms.forEachIndexed { idx, (label, active) ->
                    if (idx > 0) {
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        BasicText(label, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF151729)))
                        Box(
                            modifier = Modifier
                                .size(width = 46.dp, height = 28.dp)
                                .clip(CircleShape)
                                .background(if (active) Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))) else SolidColor(Color(0xFFDCDDEA)))
                                .padding(3.dp),
                            contentAlignment = if (active) Alignment.CenterEnd else Alignment.CenterStart,
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
