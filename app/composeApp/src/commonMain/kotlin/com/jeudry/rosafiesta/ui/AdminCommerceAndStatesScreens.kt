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
import com.jeudry.rosafiesta.designsystem.RfLogo
import com.jeudry.rosafiesta.designsystem.RfLogoSize
import com.jeudry.rosafiesta.designsystem.RfPhoto
import com.jeudry.rosafiesta.designsystem.RfPhotoKey
import com.jeudry.rosafiesta.designsystem.RosaAvatar
import com.jeudry.rosafiesta.designsystem.RosaAvatarSize

private val AdminButtonGradient = Brush.horizontalGradient(
    listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0)),
)

// ==============================================================================
// A04 · CREAR EVENTO (ELEGIR MODO)
// ==============================================================================
@Composable
internal fun RenderAdminCreateEventA04(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(modifier = Modifier.fillMaxSize()) {
        // Underlying A01 screen
        RenderAdminHomeA01(onSelectScreenCode = onSelectScreenCode)

        // Dim overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x73141632))
                .clickable { onSelectScreenCode("A01") },
        )

        // Bottom sheet modal dialog
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Color.White)
                .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 34.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Drag handle
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(40.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFFE3E4EF)),
                )

                // Header
                Column(modifier = Modifier.padding(horizontal = 4.dp)) {
                    BasicText("Nuevo evento", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)))
                    Spacer(Modifier.height(4.dp))
                    BasicText("¿Cómo quieres crearlo?", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF5F6377)))
                }

                // Option 1: Con Rosa IA
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFFE9E8FF), Color(0xFFDDF2F6))))
                        .clickable { onSelectScreenCode("A08") }
                        .padding(14.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        RosaAvatar(size = RosaAvatarSize.LARGE, customSize = 48.dp, forceAdminPalette = true)
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                BasicText("Con Rosa IA", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)))
                                Box(modifier = Modifier.height(20.dp).background(Color(0xFFFFD45C), CircleShape).padding(horizontal = 7.dp), contentAlignment = Alignment.Center) {
                                    BasicText("Más rápido", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E1A47)))
                                }
                            }
                            Spacer(Modifier.height(2.dp))
                            BasicText("Describe el evento y Rosa arma el borrador con disponibilidad real", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)))
                        }
                        RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = Color(0xFF151729), size = 20.dp)
                    }
                }

                // Option 2: Manual, paso a paso
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE9E9F2), RoundedCornerShape(20.dp))
                        .clickable { onSelectScreenCode("A05") }
                        .padding(14.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFFECEBFF)), contentAlignment = Alignment.Center) {
                            RfIcon(kind = RfIconKind.EDIT, tint = Color(0xFF4B47D6), size = 22.dp)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            BasicText("Manual, paso a paso", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)))
                            Spacer(Modifier.height(2.dp))
                            BasicText("Cliente, fecha, lugar y artículos", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)))
                        }
                        RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = Color(0xFF5F6377), size = 20.dp)
                    }
                }

                // Option 3: Duplicar un evento
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE9E9F2), RoundedCornerShape(20.dp))
                        .clickable { onSelectScreenCode("A11") }
                        .padding(14.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFFDDF4F3)), contentAlignment = Alignment.Center) {
                            RfIcon(kind = RfIconKind.BOX, tint = Color(0xFF1F8A84), size = 22.dp)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            BasicText("Duplicar un evento", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)))
                            Spacer(Modifier.height(2.dp))
                            BasicText("Parte de uno que ya hiciste", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)))
                        }
                        RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = Color(0xFF5F6377), size = 20.dp)
                    }
                }

                // Cancelar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clickable { onSelectScreenCode("A01") },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText("Cancelar", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF5F6377)))
                }
            }
        }
    }
}

// ==============================================================================
// A05 · CREAR EVENTO MANUAL (PASO 1 DE 3)
// ==============================================================================
@Composable
internal fun RenderAdminManualEventA05(
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
                .padding(top = 54.dp, start = 20.dp, end = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
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
                        .clickable { onSelectScreenCode("A04") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "Nuevo evento",
                        style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)),
                    )
                    BasicText(
                        text = "Paso 1 de 3",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)),
                    )
                }
            }

            // Segments
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.weight(1f).height(5.dp).clip(RoundedCornerShape(3.dp)).background(AdminButtonGradient))
                    Box(modifier = Modifier.weight(1f).height(5.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFFE3E4EF)))
                    Box(modifier = Modifier.weight(1f).height(5.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFFE3E4EF)))
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    BasicText("Datos", modifier = Modifier.weight(1f), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B47D6)))
                    BasicText("Artículos", modifier = Modifier.weight(1f), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF5F6377)))
                    BasicText("Resumen", modifier = Modifier.weight(1f), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF5F6377)))
                }
            }

            // Cliente
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BasicText("Cliente", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3A3D52)))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(14.dp))
                        .padding(start = 8.dp, end = 6.dp),
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
                            BasicText("L", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFC2338F)))
                        }

                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            BasicText("Laura Peña ", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF151729)))
                            BasicText("· (809) 555-0142", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF5F6377)))
                        }

                        BasicText("Cambiar", modifier = Modifier.clickable {}, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B47D6)))
                    }
                }
            }

            // Tipo de evento
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BasicText("Tipo de evento", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3A3D52)))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                            RfIcon(kind = RfIconKind.STAR, tint = Color(0xFF151729), size = 16.dp)
                            BasicText("Boda", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF151729)))
                        }
                    }

                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .clip(CircleShape)
                            .background(AdminButtonGradient)
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            RfIcon(kind = RfIconKind.SPARK, tint = Color.White, size = 16.dp)
                            BasicText("Cumpleaños", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White))
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
                        BasicText("Baby shower", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF151729)))
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE9E9F2), CircleShape)
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText("Corporativo", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF151729)))
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
                        BasicText("Otro", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF151729)))
                    }
                }
            }

            // Fecha y Hora
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(modifier = Modifier.weight(1.3f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    BasicText("Fecha", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3A3D52)))
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
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            RfIcon(kind = RfIconKind.CALENDAR, tint = Color(0xFF5F6377), size = 18.dp)
                            BasicText("Sáb 7 nov", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF151729)))
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    BasicText("Hora", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3A3D52)))
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
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            RfIcon(kind = RfIconKind.CLOCK, tint = Color(0xFF5F6377), size = 18.dp)
                            BasicText("4:00 p. m.", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF151729)))
                        }
                    }
                }
            }

            // Lugar
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BasicText("Lugar", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3A3D52)))
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
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        RfIcon(kind = RfIconKind.LOCATION, tint = Color(0xFF5F6377), size = 18.dp)
                        BasicText("Dirección o nombre del local", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF5F6377)))
                    }
                }
            }

            // Invitados
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
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
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        RfIcon(kind = RfIconKind.USER, tint = Color(0xFF5F6377), size = 18.dp)
                        BasicText("Invitados", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF151729)))
                    }

                    Box(
                        modifier = Modifier
                            .height(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF2F2F8))
                            .padding(horizontal = 3.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            Box(modifier = Modifier.size(28.dp), contentAlignment = Alignment.Center) {
                                BasicText("−", style = TextStyle(fontSize = 18.sp, color = Color(0xFF5B6CF0), fontWeight = FontWeight.Bold))
                            }
                            BasicText(
                                "40",
                                modifier = Modifier.widthIn(min = 34.dp),
                                style = TextStyle(fontFamily = typo.displayFamily, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729), textAlign = androidx.compose.ui.text.style.TextAlign.Center),
                            )
                            Box(modifier = Modifier.size(28.dp), contentAlignment = Alignment.Center) {
                                BasicText("+", style = TextStyle(fontSize = 18.sp, color = Color(0xFF5B6CF0), fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }
            }

            // Notas internas
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BasicText("Notas internas", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3A3D52)))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    contentAlignment = Alignment.TopStart,
                ) {
                    BasicText("Tema de safari, colores verde y beige.", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF151729)))
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
                    .clickable { onSelectScreenCode("A06") },
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BasicText("Siguiente: artículos", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White))
                    RfIcon(kind = RfIconKind.ARROW_RIGHT, tint = Color.White, size = 18.dp)
                }
            }
        }
    }
}

// ==============================================================================
// A06 · AGREGAR ARTÍCULOS (PASO 2 DE 3)
// ==============================================================================
@Composable
internal fun RenderAdminEventArticlesA06(
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
                        .clickable { onSelectScreenCode("A05") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "Agregar artículos",
                        style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)),
                    )
                    BasicText(
                        text = "Paso 2 de 3 · Cumpleaños de Mateo",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)),
                    )
                }
            }

            // Segments
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.weight(1f).height(5.dp).clip(RoundedCornerShape(3.dp)).background(AdminButtonGradient))
                    Box(modifier = Modifier.weight(1f).height(5.dp).clip(RoundedCornerShape(3.dp)).background(AdminButtonGradient))
                    Box(modifier = Modifier.weight(1f).height(5.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFFE3E4EF)))
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    BasicText("Datos", modifier = Modifier.weight(1f), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF5F6377)))
                    BasicText("Artículos", modifier = Modifier.weight(1f), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B47D6)))
                    BasicText("Resumen", modifier = Modifier.weight(1f), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF5F6377)))
                }
            }

            // Buscador
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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    RfIcon(kind = RfIconKind.SEARCH, tint = Color(0xFF5F6377), size = 18.dp)
                    BasicText("Buscar en el inventario", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF5F6377)))
                }
            }

            // Banner disponibilidad
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFEEF6D6))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    RfIcon(kind = RfIconKind.CALENDAR, tint = Color(0xFF4E6A10), size = 16.dp)
                    BasicText("Disponibilidad para el sábado 7 de noviembre", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF4E6A10)))
                }
            }

            // Card lista artículos
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp)),
            ) {
                Column {
                    val items = listOf(
                        Quad(RfPhotoKey.SILLA_TIFFANY, "Silla Tiffany Cristal", "80 libres de 120 · RD$ 4", "40", true),
                        Quad(RfPhotoKey.MESA_REDONDA, "Mesa redonda", "10 libres de 12 · RD$ 25", "5", true),
                        Quad(RfPhotoKey.ARCO_ROSA, "Arco de globos rosa", "2 libres de 3 · RD$ 95", "1", true),
                        Quad(RfPhotoKey.MESA_HELADOS, "Mesa dulce temática", "Última disponible · RD$ 150", "1", true),
                        Quad(RfPhotoKey.CENTRO_FLORAL, "Centro de mesa floral", "20 libres · RD$ 35", "0", false),
                    )

                    items.forEachIndexed { idx, (photo, name, desc, count, isSelected) ->
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
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                            ) {
                                RfPhoto(photoKey = photo, contentDescription = name, modifier = Modifier.fillMaxSize())
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                BasicText(name, maxLines = 1, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)))
                                val descColor = if (desc.startsWith("Última")) Color(0xFF7A5700) else Color(0xFF4E6A10)
                                BasicText(desc, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = descColor))
                            }

                            Box(
                                modifier = Modifier
                                    .height(34.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) Color(0xFFECEBFF) else Color(0xFFF2F2F8))
                                    .padding(horizontal = 3.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Box(modifier = Modifier.size(28.dp), contentAlignment = Alignment.Center) {
                                        BasicText("−", style = TextStyle(fontSize = 16.sp, color = Color(0xFF5B6CF0), fontWeight = FontWeight.Bold))
                                    }
                                    BasicText(
                                        count,
                                        modifier = Modifier.widthIn(min = 22.dp),
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729), textAlign = androidx.compose.ui.text.style.TextAlign.Center),
                                    )
                                    Box(modifier = Modifier.size(28.dp), contentAlignment = Alignment.Center) {
                                        BasicText("+", style = TextStyle(fontSize = 16.sp, color = Color(0xFF5B6CF0), fontWeight = FontWeight.Bold))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }

        // Docked Bottom Bar
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.White)
                .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(0.dp))
                .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 32.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Column {
                    BasicText("4 artículos · 47 unid.", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
                    BasicText("RD$ 530", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)))
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(AdminButtonGradient)
                        .clickable { onSelectScreenCode("A07") },
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        BasicText("Ver resumen", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White))
                        RfIcon(kind = RfIconKind.ARROW_RIGHT, tint = Color.White, size = 18.dp)
                    }
                }
            }
        }
    }
}

// Data class helper for items
private data class Quad(
    val photo: RfPhotoKey,
    val name: String,
    val desc: String,
    val count: String,
    val isSelected: Boolean,
)

// ==============================================================================
// A07 · RESUMEN Y CONFIRMACIÓN (PASO 3 DE 3)
// ==============================================================================
@Composable
internal fun RenderAdminEventSummaryA07(
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
                        .clickable { onSelectScreenCode("A06") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "Resumen",
                        style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)),
                    )
                    BasicText(
                        text = "Paso 3 de 3",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)),
                    )
                }
            }

            // Segments
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.weight(1f).height(5.dp).clip(RoundedCornerShape(3.dp)).background(AdminButtonGradient))
                    Box(modifier = Modifier.weight(1f).height(5.dp).clip(RoundedCornerShape(3.dp)).background(AdminButtonGradient))
                    Box(modifier = Modifier.weight(1f).height(5.dp).clip(RoundedCornerShape(3.dp)).background(AdminButtonGradient))
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    BasicText("Datos", modifier = Modifier.weight(1f), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF5F6377)))
                    BasicText("Artículos", modifier = Modifier.weight(1f), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF5F6377)))
                    BasicText("Resumen", modifier = Modifier.weight(1f), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B47D6)))
                }
            }

            // Hero Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0), Color(0xFF2FB8C8))))
                    .padding(16.dp),
            ) {
                // Cake canvas in top-right
                Canvas(
                    modifier = Modifier
                        .size(110.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 12.dp, y = (-10).dp),
                ) {
                    val w = size.width
                    val h = size.height
                    val sx = w / 120f
                    val sy = h / 120f

                    // Plate
                    drawOval(
                        color = Color.White,
                        topLeft = Offset((60f - 38f) * sx, (99f - 6.5f) * sy),
                        size = androidx.compose.ui.geometry.Size(76f * sx, 13f * sy),
                    )
                    // Cake body
                    drawRoundRect(
                        color = Color(0xFFFFF6EE),
                        topLeft = Offset(30f * sx, 58f * sy),
                        size = androidx.compose.ui.geometry.Size(60f * sx, 39f * sy),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f * sx, 6f * sy),
                    )
                    // Ribbon
                    drawRect(
                        color = Color(0xFFF9B6D3),
                        topLeft = Offset(30f * sx, 82f * sy),
                        size = androidx.compose.ui.geometry.Size(60f * sx, 6f * sy),
                    )
                    // Frosting
                    val path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(30f * sx, 64f * sy)
                        quadraticTo(30f * sx, 58f * sy, 36f * sx, 58f * sy)
                        lineTo(84f * sx, 58f * sy)
                        quadraticTo(90f * sx, 58f * sy, 90f * sx, 64f * sy)
                        lineTo(90f * sx, 68f * sy)
                        quadraticTo(85f * sx, 75f * sy, 80f * sx, 68f * sy)
                        quadraticTo(75f * sx, 75f * sy, 70f * sx, 68f * sy)
                        quadraticTo(65f * sx, 75f * sy, 60f * sx, 68f * sy)
                        quadraticTo(55f * sx, 75f * sy, 50f * sx, 68f * sy)
                        quadraticTo(45f * sx, 75f * sy, 40f * sx, 68f * sy)
                        quadraticTo(35f * sx, 75f * sy, 30f * sx, 68f * sy)
                        close()
                    }
                    drawPath(path, Color(0xFFEC4F97))
                    // Candles
                    drawRoundRect(Color(0xFF7650E8), Offset(42f * sx, 42f * sy), androidx.compose.ui.geometry.Size(5f * sx, 17f * sy), androidx.compose.ui.geometry.CornerRadius(2f * sx, 2f * sy))
                    drawRoundRect(Color(0xFF4AA8E0), Offset(57.5f * sx, 38f * sy), androidx.compose.ui.geometry.Size(5f * sx, 21f * sy), androidx.compose.ui.geometry.CornerRadius(2f * sx, 2f * sy))
                    drawRoundRect(Color(0xFF2FB58F), Offset(73f * sx, 42f * sy), androidx.compose.ui.geometry.Size(5f * sx, 17f * sy), androidx.compose.ui.geometry.CornerRadius(2f * sx, 2f * sy))
                    // Flames
                    drawOval(Color(0xFFF5B53B), Offset(42.5f * sx, 33f * sy), androidx.compose.ui.geometry.Size(4f * sx, 8f * sy))
                    drawOval(Color(0xFFF5B53B), Offset(58f * sx, 29f * sy), androidx.compose.ui.geometry.Size(4f * sx, 8f * sy))
                    drawOval(Color(0xFFF5B53B), Offset(73.5f * sx, 33f * sy), androidx.compose.ui.geometry.Size(4f * sx, 8f * sy))
                }

                Column(modifier = Modifier.fillMaxWidth(0.65f)) {
                    BasicText("Cumpleaños de Mateo", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White))
                    Spacer(modifier = Modifier.height(4.dp))
                    BasicText("Sáb 7 nov · 4:00 p. m. · 40 invitados", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFFE9EEFF)))
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        RfIcon(kind = RfIconKind.USER, tint = Color(0xFFE9EEFF), size = 14.dp)
                        BasicText("Laura Peña", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFFE9EEFF)))
                    }
                }
            }

            // Card artículos y total
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                    .padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 14.dp),
            ) {
                Column {
                    val summaryItems = listOf(
                        Triple(RfPhotoKey.SILLA_TIFFANY, "Silla Tiffany Cristal × 40", "RD$ 160"),
                        Triple(RfPhotoKey.MESA_REDONDA, "Mesa redonda × 5", "RD$ 125"),
                        Triple(RfPhotoKey.ARCO_ROSA, "Arco de globos rosa × 1", "RD$ 95"),
                        Triple(RfPhotoKey.MESA_HELADOS, "Mesa dulce temática × 1", "RD$ 150"),
                    )

                    summaryItems.forEachIndexed { idx, (photo, name, price) ->
                        if (idx > 0) {
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(10.dp)),
                            ) {
                                RfPhoto(photoKey = photo, contentDescription = name, modifier = Modifier.fillMaxSize())
                            }

                            BasicText(
                                text = name,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF151729)),
                            )

                            BasicText(price, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)))
                        }
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)).padding(vertical = 6.dp))

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        BasicText("Transporte e instalación", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF5F6377)))
                        BasicText("RD$ 250", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF5F6377)))
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        BasicText("Total", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)))
                        BasicText("RD$ 780", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)))
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        BasicText("Anticipo para reservar (30%)", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)))
                        BasicText("RD$ 234", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B47D6)))
                    }
                }
            }

            // WhatsApp switch row
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
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFEEF6D6)),
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.WHATSAPP, tint = Color(0xFF4E6A10), size = 18.dp)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        BasicText("Enviar cotización a Laura", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)))
                        BasicText("Por WhatsApp, al crear el evento", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
                    }

                    // Switch ON
                    Box(
                        modifier = Modifier
                            .size(width = 50.dp, height = 30.dp)
                            .clip(CircleShape)
                            .background(AdminButtonGradient)
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
                    .clickable { onSelectScreenCode("A11") },
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RfIcon(kind = RfIconKind.CHECK, tint = Color.White, size = 20.dp)
                    BasicText("Crear evento", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White))
                }
            }
        }
    }
}

// ==============================================================================
// A14 · BANDEJA DE CHATS ADMIN
// ==============================================================================
@Composable
internal fun RenderAdminChatsA14(
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
                Column {
                    BasicText("Chats", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = Color(0xFF151729)))
                    BasicText("2 conversaciones sin leer", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF5F6377), fontSize = 13.sp))
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(6.dp, CircleShape, spotColor = Color(0x334650B4))
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("A01") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 20.dp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Todos", "Sin leer · 2", "Clientes", "Equipo").forEachIndexed { idx, filter ->
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

            val chats = listOf(
                Triple("Camila Rodríguez", "¿Pueden agregar 20 sillas doradas más?", "9:08 p. m. · 1 sin leer"),
                Triple("Pedro Montaje", "Camión cargado, saliendo al Jardín Botánico", "8:45 p. m."),
                Triple("Laura Peña", "Me encantó la propuesta del cumpleaños", "Ayer"),
            )

            chats.forEach { (sender, msg, time) ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(16.dp))
                        .clickable { onSelectScreenCode("A15") }
                        .padding(16.dp),
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEFE9FE)),
                                contentAlignment = Alignment.Center,
                            ) {
                                BasicText(sender.take(1), style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.Bold, color = Color(0xFF7C5CF0), fontSize = 16.sp))
                            }
                            Column {
                                BasicText(sender, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF151729), fontSize = 15.sp))
                                BasicText(msg, style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF5F6377), fontSize = 12.sp))
                            }
                        }
                        BasicText(time, style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF7C5CF0), fontSize = 11.sp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// ==============================================================================
// A15 · CONVERSACIÓN DE CHAT CLIENTE ADMIN
// ==============================================================================
@Composable
internal fun RenderAdminChatConversationA15(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F6FB)),
    ) {
        // Chat scroll content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 118.dp, start = 16.dp, end = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Ayer
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clip(CircleShape)
                    .background(Color(0xFFEDEDF5))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                BasicText("Ayer", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5F6377)))
            }

            // Bubble 1 (client)
            Box(
                modifier = Modifier
                    .align(Alignment.Start)
                    .widthIn(max = 290.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 6.dp, bottomEnd = 20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 6.dp, bottomEnd = 20.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    BasicText(
                        text = "¡Hola equipo Rosa Fiesta! ¿Podemos incluir 20 sillas Chiavari doradas adicionales para el área del brindis?",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF151729), lineHeight = 21.sp),
                    )
                    BasicText("7:38 p. m.", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, color = Color(0xFF5F6377)))
                }
            }

            // Bubble 2 (client)
            Box(
                modifier = Modifier
                    .align(Alignment.Start)
                    .widthIn(max = 290.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 6.dp, bottomEnd = 20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 6.dp, bottomEnd = 20.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    BasicText(
                        text = "Ya revisé el presupuesto ajustado y el montaje en el Jardín Botánico nos encanta.",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF151729), lineHeight = 21.sp),
                    )
                    BasicText("9:08 p. m.", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, color = Color(0xFF5F6377)))
                }
            }

            // Hoy
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clip(CircleShape)
                    .background(Color(0xFFEDEDF5))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                BasicText("Hoy", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5F6377)))
            }

            // Admin response stack
            Column(
                modifier = Modifier
                    .align(Alignment.End)
                    .widthIn(max = 300.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 6.dp))
                        .background(AdminButtonGradient)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                ) {
                    BasicText(
                        text = "¡Hola Camila! Sí, tenemos 20 Chiavari doradas libres para el 23. ¿Te las agrego?",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color.White, lineHeight = 21.sp),
                    )
                }

                // Proposal subcard
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(18.dp))
                        .padding(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFFF2C9)),
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.BOX, tint = Color(0xFF7A5700), size = 20.dp)
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            BasicText("+20 Sillas Chiavari doradas", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)))
                            BasicText("RD$ 4.50 c/u · RD$ 90", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)))
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .height(20.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFF2C9))
                                    .padding(horizontal = 8.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                BasicText("Por aprobar", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7A5700)))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(130.dp))
        }

        // Header blanco fijo superior
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .background(Color.White)
                .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(0.dp))
                .padding(top = 52.dp, start = 12.dp, end = 12.dp, bottom = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .clickable { onSelectScreenCode("A14") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                }

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
                    BasicText("Camila Rodríguez", maxLines = 1, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)))
                    BasicText(
                        text = "Boda Real Rosa & Dorado · Vie 23 oct",
                        maxLines = 1,
                        modifier = Modifier.clickable { onSelectScreenCode("A13") },
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF4B47D6)),
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.WHATSAPP, tint = Color(0xFF4E6A10), size = 22.dp)
                }
            }
        }

        // Bottom Docked Bar
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.White)
                .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(0.dp))
                .padding(top = 10.dp, start = 12.dp, end = 12.dp, bottom = 30.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                            RfIcon(kind = RfIconKind.BOX, tint = Color(0xFF151729), size = 16.dp)
                            BasicText("Enviar cotización", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF151729)))
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
                            RfIcon(kind = RfIconKind.CALENDAR, tint = Color(0xFF151729), size = 16.dp)
                            BasicText("Confirmar hora", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF151729)))
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
                        BasicText("Gracias", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF151729)))
                    }
                }

                // Input row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                        RfIcon(kind = RfIconKind.CLOSE, tint = Color(0xFF5F6377), size = 20.dp)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF2F2F8))
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        BasicText("Escribe un mensaje…", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF5F6377)))
                    }

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(AdminButtonGradient),
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.ARROW_RIGHT, tint = Color.White, size = 20.dp)
                    }
                }
            }
        }
    }
}

// ==============================================================================
// A22 · COTIZACIONES ADMIN
// ==============================================================================
@Composable
internal fun RenderAdminQuotesA22(
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
                    text = "Cotizaciones",
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF151729)),
                )

                Spacer(modifier = Modifier.size(44.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Enviadas · 3", "Aprobadas · 2", "Vencidas · 1").forEachIndexed { idx, filter ->
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

            val quotes = listOf(
                Triple("Cumpleaños de Mateo", "Laura Peña · Enviada hoy", "RD$ 780"),
                Triple("Boda Jardín Quinta Las Flores", "Patricia Lora · En revisión", "RD$ 18,880"),
                Triple("Coctel Aniversario", "Banco BHD · Aprobada", "RD$ 24,500"),
            )

            quotes.forEach { (title, subtitle, amount) ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(16.dp))
                        .clickable { onSelectScreenCode("A03") }
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
                        BasicText(amount, style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.Bold, color = Color(0xFF7C5CF0), fontSize = 16.sp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// ==============================================================================
// A29 · NOTIFICACIONES DEL NEGOCIO
// ==============================================================================
@Composable
internal fun RenderAdminNotificationsA29(
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
                    text = "Notificaciones",
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF151729)),
                )

                Spacer(modifier = Modifier.size(44.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            val notifs = listOf(
                Triple("Nueva solicitud de cotización", "Graduación Promo 2026 · 200 invitados", "6:40 p. m."),
                Triple("Mensaje de Camila Rodríguez", "¿Pueden agregar 20 sillas doradas más?", "2:15 p. m."),
                Triple("Nueva reseña 5 estrellas", "Gala Corporativa Grupo Caribe · ¡Excelente servicio!", "Ayer"),
            )

            notifs.forEach { (title, desc, time) ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            BasicText(title, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF151729), fontSize = 14.sp))
                            BasicText(time, style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF7C5CF0), fontSize = 11.sp))
                        }
                        BasicText(desc, style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF5F6377), fontSize = 13.sp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// ==============================================================================
// A30 · ELECCIÓN DE ENTRADA STAFF
// ==============================================================================
@Composable
internal fun RenderAdminEntryChoiceA30(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    0.0f to Color(0xFFF9E3F1),
                    0.6f to Color(0xFFF8F7FC),
                    radius = 900f,
                    center = Offset(0f, 0f),
                ),
            ),
    ) {
        // Logo y saludo
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 96.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            RfLogo(size = RfLogoSize.LARGE)

            BasicText("Hola, María", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)))
            BasicText("Tu cuenta tiene acceso de administración. ¿Cómo quieres entrar?", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF5F6377), textAlign = androidx.compose.ui.text.style.TextAlign.Center))
        }

        // 2 Tarjetas de modo
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 322.dp, start = 20.dp, end = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Administración Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0), Color(0xFF2FB8C8))))
                    .clickable { onSelectScreenCode("A01") }
                    .padding(18.dp),
            ) {
                // Sparkle decoration top right
                Canvas(
                    modifier = Modifier
                        .size(60.dp)
                        .align(Alignment.TopEnd),
                ) {
                    drawCircle(color = Color(0x1AFFFFFF), radius = size.minDimension / 1.5f, center = Offset(size.width, 0f))
                    // Sparkle
                    val cx = size.width - 20f
                    val cy = 25f
                    val path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(cx, cy - 8f)
                        lineTo(cx + 3f, cy - 3f)
                        lineTo(cx + 8f, cy)
                        lineTo(cx + 3f, cy + 3f)
                        lineTo(cx, cy + 8f)
                        lineTo(cx - 3f, cy + 3f)
                        lineTo(cx - 8f, cy)
                        lineTo(cx - 3f, cy - 3f)
                        close()
                    }
                    drawPath(path, Color(0xFFFFD45C))
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0x33FFFFFF)),
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.STORE, tint = Color.White, size = 24.dp)
                    }

                    Column {
                        BasicText("Administración", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White))
                        Spacer(modifier = Modifier.height(2.dp))
                        BasicText("Eventos, inventario, pagos y clientes", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFFE9EEFF)))
                    }
                }
            }

            // Vista de cliente Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECE7F1), RoundedCornerShape(24.dp))
                    .clickable { onSelectScreenCode("s14") }
                    .padding(18.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFFEA4EA9), Color(0xFF865DEC)))),
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.STAR, tint = Color.White, size = 24.dp)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        BasicText("Vista de cliente", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)))
                        BasicText("Mira la app como la ven tus clientes", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF5F6377)))
                    }

                    RfIcon(kind = RfIconKind.ARROW_RIGHT, tint = Color(0xFF5F6377), size = 20.dp)
                }
            }
        }

        // Bottom: Recordar mi elección
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 44.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(AdminButtonGradient),
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.CHECK, tint = Color.White, size = 14.dp)
            }
            BasicText("Recordar mi elección", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF5F6377)))
        }
    }
}

// ==============================================================================
// A31 · AJUSTES DEL NEGOCIO
// ==============================================================================
@Composable
internal fun RenderAdminSettingsA31(
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
                    text = "Ajustes del Negocio",
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF151729)),
                )

                Spacer(modifier = Modifier.size(44.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            val settings = listOf(
                Pair("Nombre de la empresa", "Rosa Fiesta SRL"),
                Pair("Teléfono comercial", "+1 (829) 942-4971"),
                Pair("Horario de atención", "Lun – Sáb · 9:00 a. m. – 6:00 p. m."),
                Pair("Zona de entrega", "Distrito Nacional y Gran Santo Domingo"),
                Pair("Depósito de reserva requerido", "50% de anticipo para confirmación"),
            )

            settings.forEach { (lbl, valStr) ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        BasicText(lbl, style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF5F6377), fontSize = 12.sp))
                        BasicText(valStr, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF151729), fontSize = 15.sp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// ==============================================================================
// A32 · CANCELACIONES
// ==============================================================================
@Composable
internal fun RenderAdminCancellationsA32(
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
                    text = "Cancelaciones",
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF151729)),
                )

                Spacer(modifier = Modifier.size(44.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            BasicText("2 solicitudes por revisar", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF5F6377), fontSize = 13.sp))

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(18.dp))
                    .clickable { onSelectScreenCode("A33") }
                    .padding(16.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        BasicText("Bautizo de Lucas", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF151729), fontSize = 15.sp))
                        BasicText("Por revisar", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626), fontSize = 12.sp))
                    }
                    BasicText("Cliente: Rafael Soto · Fecha prevista: Sáb 31 oct", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF5F6377), fontSize = 13.sp))
                    BasicText("Motivo: Viaje familiar de emergencia", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF5F6377), fontSize = 12.sp))
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// ==============================================================================
// A33 · REEMBOLSO
// ==============================================================================
@Composable
internal fun RenderAdminRefundA33(
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
                        .clickable { onSelectScreenCode("A32") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText("Reembolso", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)))
                    BasicText("Rafael Soto · Bautizo de Lucas", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)))
                }
            }

            // Hero banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0), Color(0xFF2FB8C8))))
                    .padding(horizontal = 18.dp, vertical = 16.dp),
            ) {
                Column {
                    BasicText("Vas a reembolsar", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xD9FFFFFF)))
                    Spacer(modifier = Modifier.height(2.dp))
                    BasicText("RD$ 1,080", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color.White))
                    Spacer(modifier = Modifier.height(4.dp))
                    BasicText("RD$ 1,200 pagados − 10% de gestión (RD$ 120)", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xD9FFFFFF)))
                }
            }

            // Devolver por
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BasicText("Devolver por", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3A3D52)))

                // Transferencia (Selected)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(2.dp, Color(0xFF5B6CF0), RoundedCornerShape(20.dp))
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
                                .background(Color(0xFFECEBFF)),
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.BOX, tint = Color(0xFF4B47D6), size = 19.dp)
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            BasicText("Transferencia", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)))
                            BasicText("BHD · cuenta •••• 3307", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)))
                        }

                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .border(6.dp, Color(0xFF5B6CF0), CircleShape),
                        )
                    }
                }

                // Efectivo
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
                                .background(Color(0xFFEEF6D6)),
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.WALLET, tint = Color(0xFF4E6A10), size = 19.dp)
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            BasicText("Efectivo", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)))
                            BasicText("Entregar en el local", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)))
                        }

                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .border(2.dp, Color(0xFFDCDDEA), CircleShape),
                        )
                    }
                }
            }

            // Card 2 switches
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
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            BasicText("Liberar artículos del 31 oct", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF151729)))
                            BasicText("14 artículos vuelven al inventario", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)))
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

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF2F0F5)))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        BasicText("Avisar al cliente por WhatsApp", modifier = Modifier.weight(1f), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF151729)))

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
                }
            }

            // Mensaje para Rafael
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BasicText("Mensaje para Rafael", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3A3D52)))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                ) {
                    BasicText("Hola Rafael, aprobamos tu cancelación. Te enviamos RD$ 1,080 por transferencia en 3 a 5 días hábiles. ¡Ojalá celebremos pronto!", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF3A3D52), lineHeight = 21.sp))
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
                    .clickable { onSelectScreenCode("A32") },
                contentAlignment = Alignment.Center,
            ) {
                BasicText("Aprobar y reembolsar RD$ 1,080", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White))
            }
        }
    }
}

// ==============================================================================
// A34 · RESPONDER RESEÑA
// ==============================================================================
@Composable
internal fun RenderAdminReplyReviewA34(
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
                    BasicText("Responder reseña", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)))
                    BasicText("Pendiente · llegó hace 1 día", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)))
                }
            }

            // Review Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                    .padding(14.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFF2C9)),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText("P", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF7A5700)))
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            BasicText("Patricia Lora", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)))
                            BasicText("Baby shower Ana · 17 oct", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)))
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                            repeat(4) {
                                RfIcon(kind = RfIconKind.STAR, tint = Color(0xFFF7B500), size = 16.dp)
                            }
                            RfIcon(kind = RfIconKind.STAR, tint = Color(0xFFDCDDEA), size = 16.dp)
                        }
                    }

                    BasicText(
                        text = "“Todo muy bonito y llegaron puntuales al montaje. Solo faltaron dos manteles en la mesa principal y tuvimos que improvisar.”",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.5.sp, color = Color(0xFF151729), lineHeight = 21.sp),
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(width = 72.dp, height = 56.dp)
                                .clip(RoundedCornerShape(10.dp)),
                        ) {
                            RfPhoto(photoKey = RfPhotoKey.BABY_SHOWER, contentDescription = "Baby shower", modifier = Modifier.fillMaxSize())
                        }
                        Box(
                            modifier = Modifier
                                .size(width = 72.dp, height = 56.dp)
                                .clip(RoundedCornerShape(10.dp)),
                        ) {
                            RfPhoto(photoKey = RfPhotoKey.MESA_HELADOS, contentDescription = "Mesa dulces", modifier = Modifier.fillMaxSize())
                        }
                    }
                }
            }

            // Rosa helper row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                RosaAvatar(size = RosaAvatarSize.MINI, customSize = 30.dp, forceAdminPalette = true)
                BasicText("Rosa redactó una respuesta", modifier = Modifier.weight(1f), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)))
                Row(
                    modifier = Modifier.clickable {},
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    RfIcon(kind = RfIconKind.REFRESH, tint = Color(0xFF4B47D6), size = 15.dp)
                    BasicText("Otra", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B47D6)))
                }
            }

            // Chips
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .height(32.dp)
                        .clip(CircleShape)
                        .background(AdminButtonGradient)
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText("Cálida", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White))
                }

                Box(
                    modifier = Modifier
                        .height(32.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE9E9F2), CircleShape)
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText("Formal", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF151729)))
                }

                Box(
                    modifier = Modifier
                        .height(32.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE9E9F2), CircleShape)
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText("Breve", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF151729)))
                }
            }

            // Textarea Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(2.dp, Color(0xFF5B6CF0), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                BasicText(
                    text = "¡Gracias, Patricia! Nos alegra que el baby shower quedara bonito. Tienes razón con los manteles: ya los sumamos a nuestro checklist de montaje para que no vuelva a pasar. Te debemos un detalle en tu próximo evento. — María, Rosa Fiesta",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.5.sp, color = Color(0xFF151729), lineHeight = 21.sp),
                )
            }

            // Switch row
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    BasicText("Publicar y verificar la reseña", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF151729)))
                    BasicText("La respuesta será visible en la app", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.5.sp, color = Color(0xFF5F6377)))
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
                    .clickable { onSelectScreenCode("A12") },
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RfIcon(kind = RfIconKind.ARROW_RIGHT, tint = Color.White, size = 20.dp)
                    BasicText("Publicar respuesta", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White))
                }
            }
        }
    }
}

// ==============================================================================
// A35 · ESTADO VACÍO ADMIN
// ==============================================================================
@Composable
internal fun RenderAdminEmptyStateA35(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F6FB)),
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
                    .background(Color(0xFFEFE9FE)),
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.SPARK, tint = Color(0xFF7C5CF0), size = 32.dp)
            }

            BasicText("Todo al día", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Color(0xFF151729)))
            BasicText("No hay solicitudes pendientes en este momento. Te avisaremos cuando un cliente pida una cotización.", style = TextStyle(fontFamily = typo.bodyFamily, color = Color(0xFF5F6377), fontSize = 14.sp))

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AdminButtonGradient)
                    .clickable { onSelectScreenCode("A01") },
                contentAlignment = Alignment.Center,
            ) {
                BasicText("Volver al Inicio", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp))
            }
        }
    }
}

// ==============================================================================
// A36 · ESTADO CARGANDO ADMIN
// ==============================================================================
@Composable
internal fun RenderAdminLoadingStateA36(
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
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            // Skeleton Header
            Box(modifier = Modifier.fillMaxWidth().height(40.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFE5E7EB)))

            Spacer(modifier = Modifier.height(10.dp))

            // Skeleton Cards
            repeat(4) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(16.dp))
                        .padding(14.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.fillMaxWidth(0.6f).height(16.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFFECECF5)))
                        Box(modifier = Modifier.fillMaxWidth(0.4f).height(12.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFFF3F4F6)))
                    }
                }
            }
        }
    }
}

// ==============================================================================
// A37 · ESTADO ERROR ADMIN (SIN CONEXIÓN)
// ==============================================================================
@Composable
internal fun RenderAdminErrorStateA37(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F6FB)),
    ) {
        // Banner negro superior "Sin conexión"
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(92.dp)
                .background(Color(0xFF151729))
                .padding(start = 20.dp, end = 20.dp, bottom = 12.dp),
            contentAlignment = Alignment.BottomStart,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                RfIcon(kind = RfIconKind.WARNING, tint = Color.White, size = 18.dp)
                BasicText(
                    text = "Sin conexión · 3 cambios por sincronizar",
                    modifier = Modifier.weight(1f),
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = Color.White),
                )
                BasicText(
                    text = "Ver",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFFD45C)),
                )
            }
        }

        // Contenido con padding top 112dp
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 112.dp, start = 20.dp, end = 20.dp),
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
                        .clickable { onSelectScreenCode("A09") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText("Inventario", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)))
                    BasicText("86 artículos", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)))
                }
            }

            // Error Center
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFCE3E8)),
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.WARNING, tint = Color(0xFFB42345), size = 50.dp)
                }

                Spacer(modifier = Modifier.height(4.dp))

                BasicText(
                    text = "No pudimos cargar el inventario",
                    style = TextStyle(fontFamily = typo.displayFamily, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729), textAlign = androidx.compose.ui.text.style.TextAlign.Center),
                )

                BasicText(
                    text = "Revisa tu conexión. Los cambios que hiciste sin internet se enviarán solos al reconectar.",
                    modifier = Modifier.widthIn(max = 300.dp),
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF5F6377), lineHeight = 22.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center),
                )

                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(AdminButtonGradient)
                            .clickable { onSelectScreenCode("A09") },
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            RfIcon(kind = RfIconKind.REFRESH, tint = Color.White, size = 18.dp)
                            BasicText("Reintentar", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White))
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(16.dp))
                            .clickable { onSelectScreenCode("A09") },
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText("Ver la última copia guardada", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)))
                    }
                }

                BasicText("Última sincronización: hoy, 9:12 a. m.", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.5.sp, color = Color(0xFF5F6377)))
            }
        }
    }
}

// ==============================================================================
// A38 · ESTADO ÉXITO ADMIN (CON TOAST FLOTANTE SOBRE A13)
// ==============================================================================
@Composable
internal fun RenderAdminSuccessStateA38(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(modifier = Modifier.fillMaxSize()) {
        // Pantalla A13 subyacente
        RenderAdminEventDetailA13(onSelectScreenCode = onSelectScreenCode)

        // Toast de éxito flotante superior
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 52.dp, start = 16.dp, end = 16.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF151729))
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFB9D84A)),
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.CHECK, tint = Color(0xFF2E3A08), size = 18.dp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText("Pago registrado", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White))
                    BasicText("RD$ 1,350 · Camila Rodríguez · recibo enviado", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xBFFFFFFF)))
                }

                BasicText(
                    text = "Ver",
                    modifier = Modifier.clickable { onSelectScreenCode("A20") },
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFFD45C)),
                )
            }
        }
    }
}
