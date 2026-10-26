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
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
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
                    BasicText("Nuevo evento", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A)))
                    Spacer(Modifier.height(4.dp))
                    BasicText("¿Cómo quieres crearlo?", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF5F6377)))
                }

                // Option 1: Con Rosa IA
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
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
                                BasicText("Con Rosa IA", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E324A)))
                                Box(modifier = Modifier.height(20.dp).background(Color(0xFFFFD45C), CircleShape).padding(horizontal = 7.dp), contentAlignment = Alignment.Center) {
                                    BasicText("Más rápido", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E1A47)))
                                }
                            }
                            Spacer(Modifier.height(2.dp))
                            BasicText("Describe el evento y Rosa arma el borrador con disponibilidad real", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)))
                        }
                        RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = Color(0xFF5F6377), size = 20.dp)
                    }
                }

                // Option 2: Manual, paso a paso
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
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
                            BasicText("Manual, paso a paso", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E324A)))
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
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
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
                            BasicText("Duplicar un evento", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E324A)))
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
    val clientOptions = remember {
        listOf(
            Triple("L", "Laura Peña ", "· (809) 555-0142"),
            Triple("C", "Camila Rodríguez ", "· (809) 555-0188"),
            Triple("A", "Ana Gómez ", "· (829) 555-0911"),
        )
    }
    var clientIndex by remember { mutableStateOf(0) }
    val currentClient = clientOptions[clientIndex % clientOptions.size]
    var selectedType by remember { mutableStateOf("Cumpleaños") }
    var dateText by remember { mutableStateOf("Sáb 7 nov") }
    var timeText by remember { mutableStateOf("4:00 p. m.") }
    var venueText by remember { mutableStateOf("") }
    var guestCount by remember { mutableStateOf(40) }
    var notesText by remember { mutableStateOf("Tema de safari, colores verde y beige.") }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 54.dp, start = 20.dp, end = 20.dp),
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
                        .rfIconButtonShadow(CircleShape, isAdmin = true)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), CircleShape)
                        .clickable { onSelectScreenCode("BACK:A04") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "Nuevo evento",
                        style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A)),
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
                    BasicText("Datos", modifier = Modifier.weight(1f), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF4B47D6)))
                    BasicText("Artículos", modifier = Modifier.weight(1f).clickable { onSelectScreenCode("A06") }, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)))
                    BasicText("Resumen", modifier = Modifier.weight(1f).clickable { onSelectScreenCode("A07") }, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)))
                }
            }

            // Cliente
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BasicText("Cliente", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF3A3D52)))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .rfInputShadow(RoundedCornerShape(14.dp), isAdmin = true)
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
                            BasicText(currentClient.first, style = TextStyle(fontFamily = typo.displayFamily, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFC2338F)))
                        }

                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            BasicText(currentClient.second, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Normal, color = Color(0xFF2E324A)))
                            BasicText(currentClient.third, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF5F6377)))
                        }

                        BasicText(
                            "Cambiar",
                            modifier = Modifier.clickable { clientIndex += 1 },
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B47D6)),
                        )
                    }
                }
            }

            // Tipo de evento
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BasicText("Tipo de evento", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF3A3D52)))
                val typeRows = listOf(
                    listOf("Boda" to RfIconKind.STAR, "Cumpleaños" to RfIconKind.SPARK),
                    listOf("Baby shower" to RfIconKind.STAR, "Corporativo" to RfIconKind.BOX),
                    listOf("Otro" to RfIconKind.SPARK),
                )
                typeRows.forEach { rowItems ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowItems.forEach { (label, iconKind) ->
                            val isSelected = selectedType == label
                            Box(
                                modifier = Modifier
                                    .height(36.dp)
                                    .clip(CircleShape)
                                    .then(
                                        if (isSelected) {
                                            Modifier.background(AdminButtonGradient)
                                        } else {
                                            Modifier.background(Color.White).border(1.dp, Color(0xFFE9E9F2), CircleShape)
                                        },
                                    )
                                    .clickable { selectedType = label }
                                    .padding(horizontal = 14.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    RfIcon(kind = iconKind, tint = if (isSelected) Color.White else Color(0xFF5F6377), size = 16.dp)
                                    BasicText(
                                        label,
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
            }

            // Fecha y Hora
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(modifier = Modifier.weight(1.3f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    BasicText("Fecha", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF3A3D52)))
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
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            RfIcon(kind = RfIconKind.CALENDAR, tint = Color(0xFF5F6377), size = 18.dp)
                            BasicTextField(
                                value = dateText,
                                onValueChange = { updated -> dateText = updated },
                                singleLine = true,
                                textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Normal, color = Color(0xFF2E324A)),
                                cursorBrush = SolidColor(Color(0xFF5B6CF0)),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    BasicText("Hora", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF3A3D52)))
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
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            RfIcon(kind = RfIconKind.CLOCK, tint = Color(0xFF5F6377), size = 18.dp)
                            BasicTextField(
                                value = timeText,
                                onValueChange = { updated -> timeText = updated },
                                singleLine = true,
                                textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Normal, color = Color(0xFF2E324A)),
                                cursorBrush = SolidColor(Color(0xFF5B6CF0)),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }

            // Lugar
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BasicText("Lugar", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF3A3D52)))
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
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        RfIcon(kind = RfIconKind.LOCATION, tint = Color(0xFF5F6377), size = 18.dp)
                        BasicTextField(
                            value = venueText,
                            onValueChange = { updated -> venueText = updated },
                            singleLine = true,
                            textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF2E324A)),
                            cursorBrush = SolidColor(Color(0xFF5B6CF0)),
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { innerTextField ->
                                if (venueText.isEmpty()) {
                                    BasicText("Dirección o nombre del local", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF5F6377)))
                                }
                                innerTextField()
                            },
                        )
                    }
                }
            }

            // Invitados
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
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
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        RfIcon(kind = RfIconKind.USER, tint = Color(0xFF5F6377), size = 18.dp)
                        BasicText("Invitados", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Normal, color = Color(0xFF2E324A)))
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
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .clickable { guestCount = (guestCount - 5).coerceAtLeast(5) },
                                contentAlignment = Alignment.Center,
                            ) {
                                BasicText("−", style = TextStyle(fontSize = 18.sp, color = Color(0xFF5B6CF0), fontWeight = FontWeight.Bold))
                            }
                            BasicText(
                                guestCount.toString(),
                                modifier = Modifier.widthIn(min = 34.dp),
                                style = TextStyle(fontFamily = typo.displayFamily, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E324A), textAlign = androidx.compose.ui.text.style.TextAlign.Center),
                            )
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .clickable { guestCount += 5 },
                                contentAlignment = Alignment.Center,
                            ) {
                                BasicText("+", style = TextStyle(fontSize = 18.sp, color = Color(0xFF5B6CF0), fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }
            }

            // Notas internas
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BasicText("Notas internas", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF3A3D52)))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .rfInputShadow(RoundedCornerShape(14.dp), isAdmin = true)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    contentAlignment = Alignment.TopStart,
                ) {
                    BasicTextField(
                        value = notesText,
                        onValueChange = { updated -> notesText = updated },
                        textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Normal, color = Color(0xFF2E324A)),
                        cursorBrush = SolidColor(Color(0xFF5B6CF0)),
                        modifier = Modifier.fillMaxSize(),
                    )
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
                    .background(AdminButtonGradient)
                    .clickable { onSelectScreenCode("A06") },
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BasicText("Siguiente: artículos", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White))
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
    var searchQuery by remember { mutableStateOf("") }
    var itemCounts by remember {
        mutableStateOf(
            mapOf(
                "Silla Tiffany Cristal" to 40,
                "Mesa redonda" to 5,
                "Arco de globos rosa" to 1,
                "Mesa dulce temática" to 1,
                "Centro de mesa floral" to 0,
            ),
        )
    }

    val catalogDefinitions = remember {
        listOf(
            Quad(RfPhotoKey.SILLA_TIFFANY, "Silla Tiffany Cristal", "80 libres de 120 · RD$ 4", "4", true),
            Quad(RfPhotoKey.MESA_REDONDA, "Mesa redonda", "10 libres de 12 · RD$ 25", "25", true),
            Quad(RfPhotoKey.ARCO_ROSA, "Arco de globos rosa", "2 libres de 3 · RD$ 95", "95", true),
            Quad(RfPhotoKey.MESA_HELADOS, "Mesa dulce temática", "Última disponible · RD$ 150", "150", true),
            Quad(RfPhotoKey.CENTRO_FLORAL, "Centro de mesa floral", "20 libres · RD$ 35", "35", false),
        )
    }

    val visibleItems = catalogDefinitions.filter { item ->
        if (searchQuery.isBlank()) true else item.name.contains(searchQuery.trim(), ignoreCase = true)
    }
    val selectedArticlesCount = itemCounts.values.count { qty -> qty > 0 }
    val totalUnitsCount = itemCounts.values.sum()
    val totalDop = catalogDefinitions.sumOf { item ->
        val unitPrice = item.count.toIntOrNull() ?: 0
        val qty = itemCounts[item.name] ?: 0
        unitPrice * qty
    }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 54.dp, start = 20.dp, end = 20.dp),
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
                        .clickable { onSelectScreenCode("BACK:A05") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "Agregar artículos",
                        style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A)),
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
                    BasicText("Datos", modifier = Modifier.weight(1f).clickable { onSelectScreenCode("A05") }, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)))
                    BasicText("Artículos", modifier = Modifier.weight(1f), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF4B47D6)))
                    BasicText("Resumen", modifier = Modifier.weight(1f).clickable { onSelectScreenCode("A07") }, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)))
                }
            }

            // Buscador
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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    RfIcon(kind = RfIconKind.SEARCH, tint = Color(0xFF5F6377), size = 18.dp)
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { updated -> searchQuery = updated },
                        singleLine = true,
                        textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF2E324A)),
                        cursorBrush = SolidColor(Color(0xFF5B6CF0)),
                        modifier = Modifier.weight(1f),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                BasicText("Buscar en el inventario", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF5F6377)))
                            }
                            innerTextField()
                        },
                    )
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
                    BasicText("Disponibilidad para el sábado 7 de noviembre", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF4E6A10)))
                }
            }

            // Card lista artículos
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp)),
            ) {
                Column {
                    visibleItems.forEachIndexed { idx, (photo, name, desc, _, _) ->
                        val qty = itemCounts[name] ?: 0
                        val isSelected = qty > 0
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
                                BasicText(name, maxLines = 1, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Normal, color = Color(0xFF2E324A)))
                                val parts = desc.split(" · ")
                                val stockPart = parts[0]
                                val pricePart = if (parts.size > 1) " · " + parts[1] else ""
                                val descColor = if (stockPart.startsWith("Última")) Color(0xFF7A5700) else Color(0xFF4E6A10)
                                val annotatedDesc = buildAnnotatedString {
                                    withStyle(SpanStyle(color = descColor, fontWeight = FontWeight.Normal)) {
                                        append(stockPart)
                                    }
                                    if (pricePart.isNotEmpty()) {
                                        withStyle(SpanStyle(color = Color(0xFF5F6377), fontWeight = FontWeight.Normal)) {
                                            append(pricePart)
                                        }
                                    }
                                }
                                BasicText(annotatedDesc, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp))
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
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .clickable {
                                                val nextQty = (qty - 1).coerceAtLeast(0)
                                                itemCounts = itemCounts + (name to nextQty)
                                            },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        BasicText("−", style = TextStyle(fontSize = 16.sp, color = Color(0xFF5B6CF0), fontWeight = FontWeight.Normal))
                                    }
                                    BasicText(
                                        qty.toString(),
                                        modifier = Modifier.widthIn(min = 22.dp),
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Normal, color = Color(0xFF2E324A), textAlign = androidx.compose.ui.text.style.TextAlign.Center),
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .clickable {
                                                itemCounts = itemCounts + (name to (qty + 1))
                                            },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        BasicText("+", style = TextStyle(fontSize = 16.sp, color = Color(0xFF5B6CF0), fontWeight = FontWeight.Normal))
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
                    BasicText("$selectedArticlesCount artículos · $totalUnitsCount unid.", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
                    BasicText("RD$ $totalDop", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)))
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = true)
                        .clip(RoundedCornerShape(16.dp))
                        .background(AdminButtonGradient)
                        .clickable { onSelectScreenCode("A07") },
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        BasicText("Ver resumen", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White))
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
private data class ItemSummaryA07(val photo: RfPhotoKey, val name: String, val qty: String, val price: String)

@Composable
internal fun RenderAdminEventSummaryA07(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    var notifyWhatsApp by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 54.dp, start = 20.dp, end = 20.dp),
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
                        .clickable { onSelectScreenCode("BACK:A06") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "Resumen",
                        style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A)),
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
                    BasicText("Datos", modifier = Modifier.weight(1f).clickable { onSelectScreenCode("A05") }, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)))
                    BasicText("Artículos", modifier = Modifier.weight(1f).clickable { onSelectScreenCode("A06") }, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)))
                    BasicText("Resumen", modifier = Modifier.weight(1f), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF4B47D6)))
                }
            }

            // Hero Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfHeroBannerShadow(RoundedCornerShape(22.dp), isAdmin = true)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0), Color(0xFF2FB8C8)))),
            ) {
                // Cake canvas in top-right
                Canvas(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 12.dp, y = (-10).dp)
                        .size(110.dp),
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

                Column(modifier = Modifier.padding(16.dp).widthIn(max = 220.dp)) {
                    BasicText("Cumpleaños de Mateo", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White))
                    Spacer(modifier = Modifier.height(4.dp))
                    BasicText("Sáb 7 nov · 4:00 p. m. · 40 invitados", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFFE9EEFF)))
                    Spacer(modifier = Modifier.height(2.dp))
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
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                    .padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 14.dp),
            ) {
                Column {
                    val summaryItems = listOf(
                        ItemSummaryA07(RfPhotoKey.SILLA_TIFFANY, "Silla Tiffany Cristal", "× 40", "RD$ 160"),
                        ItemSummaryA07(RfPhotoKey.MESA_REDONDA, "Mesa redonda", "× 5", "RD$ 125"),
                        ItemSummaryA07(RfPhotoKey.ARCO_ROSA, "Arco de globos rosa", "× 1", "RD$ 95"),
                        ItemSummaryA07(RfPhotoKey.MESA_HELADOS, "Mesa dulce temática", "× 1", "RD$ 150"),
                    )

                    summaryItems.forEachIndexed { idx, item ->
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
                                RfPhoto(photoKey = item.photo, contentDescription = item.name, modifier = Modifier.fillMaxSize())
                            }

                            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                BasicText(
                                    text = item.name,
                                    maxLines = 1,
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Normal, color = Color(0xFF2E324A)),
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                BasicText(
                                    text = item.qty,
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)),
                                )
                            }

                            BasicText(item.price, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Normal, color = Color(0xFF2E324A)))
                        }
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))

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
                        BasicText("Total", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E324A)))
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
                    .rfCardShadow(RoundedCornerShape(16.dp), isAdmin = true)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(16.dp))
                    .clickable { notifyWhatsApp = !notifyWhatsApp }
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
                        BasicText("Enviar cotización a Laura", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E324A)))
                        BasicText("Por WhatsApp, al crear el evento", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
                    }

                    Box(
                        modifier = Modifier
                            .size(width = 50.dp, height = 30.dp)
                            .clip(CircleShape)
                            .then(
                                if (notifyWhatsApp) {
                                    Modifier.background(AdminButtonGradient)
                                } else {
                                    Modifier.background(Color(0xFFE3E4EF))
                                },
                            )
                            .padding(3.dp),
                        contentAlignment = if (notifyWhatsApp) Alignment.CenterEnd else Alignment.CenterStart,
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
                    .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = true)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AdminButtonGradient)
                    .clickable { onSelectScreenCode("A11") },
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RfIcon(kind = RfIconKind.CHECK, tint = Color.White, size = 20.dp)
                    BasicText("Crear evento", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729).let { Color.White }))
                }
            }
        }
    }
}

// ==============================================================================
// A14 · BANDEJA DE CHATS ADMIN
// ==============================================================================
private data class AdminChatItem(
    val initial: String,
    val avatarBg: Color,
    val avatarColor: Color,
    val name: String,
    val time: String,
    val eventOrRole: String,
    val snippet: String,
    val unreadCount: Int = 0,
)

@Composable
internal fun RenderAdminChatsA14(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Todos") }

    val allChats = remember {
        listOf(
            AdminChatItem("C", Color(0xFFFDE6F2), Color(0xFFC2338F), "Camila Rodríguez", "9:08 p. m.", "Boda Real Rosa & Dorado", "Ya revisé el presupuesto ajustado y el montaje…", unreadCount = 2),
            AdminChatItem("A", Color(0xFFFFF2C9), Color(0xFF7A5700), "Ana Gómez", "6:40 p. m.", "Graduación Promo 2026", "¿Tienen letreros con luces para la entrada?"),
            AdminChatItem("L", Color(0xFFEEF6D6), Color(0xFF4E6A10), "Laura Peña", "Ayer", "Cumpleaños de Mateo", "Tú: Te envié la cotización por aquí 🎈"),
            AdminChatItem("Y", Color(0xFFFDE6F2), Color(0xFFC2338F), "Yuli · Decoración", "Ayer", "Equipo", "Ya tengo las flores para el viernes."),
            AdminChatItem("P", Color(0xFFDDF4F3), Color(0xFF1F8A84), "Pedro · Logística", "12 oct", "Equipo", "Camión listo para el sábado."),
        )
    }

    val filteredByChip = allChats.filter { chat ->
        when (selectedFilter) {
            "Sin leer · 2" -> chat.unreadCount > 0
            "Clientes" -> chat.eventOrRole != "Equipo"
            "Equipo" -> chat.eventOrRole == "Equipo"
            else -> true
        }
    }
    val visibleChats = filteredByChip.filter { chat ->
        if (searchQuery.isBlank()) {
            true
        } else {
            val q = searchQuery.trim()
            val matchesName = chat.name.contains(q, ignoreCase = true)
            if (matchesName) true else chat.eventOrRole.contains(q, ignoreCase = true)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 54.dp, start = 20.dp, end = 20.dp),
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
                        .clickable { onSelectScreenCode("BACK:A01") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "Chats",
                        style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A)),
                    )
                    BasicText(
                        text = "2 sin leer",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)),
                    )
                }
            }

            // Search bar
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    RfIcon(kind = RfIconKind.SEARCH, tint = Color(0xFF5F6377), size = 18.dp)
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { updated -> searchQuery = updated },
                        singleLine = true,
                        textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF2E324A)),
                        cursorBrush = SolidColor(Color(0xFF5B6CF0)),
                        modifier = Modifier.weight(1f),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                BasicText(
                                    text = "Buscar cliente o evento",
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF5F6377)),
                                )
                            }
                            innerTextField()
                        },
                    )
                }
            }

            // Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf("Todos", "Sin leer · 2", "Clientes", "Equipo").forEach { chip ->
                    val isSelected = selectedFilter == chip
                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .then(
                                if (isSelected) {
                                    Modifier.background(AdminButtonGradient)
                                } else {
                                    Modifier.background(Color.White).border(1.dp, Color(0xFFE9E9F2), RoundedCornerShape(999.dp))
                                },
                            )
                            .clickable { selectedFilter = chip }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = chip,
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

            // Single card with chats
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp)),
            ) {
                Column {
                    visibleChats.forEachIndexed { idx, chat ->
                        if (idx > 0) {
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectScreenCode("A15") }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .rfIconButtonShadow(CircleShape, isAdmin = true)
                                    .clip(CircleShape)
                                    .background(chat.avatarBg),
                                contentAlignment = Alignment.Center,
                            ) {
                                BasicText(chat.initial, style = TextStyle(fontFamily = typo.displayFamily, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = chat.avatarColor))
                            }

                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    BasicText(
                                        text = chat.name,
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = if (chat.unreadCount > 0) FontWeight.Bold else FontWeight.Medium, color = Color(0xFF2E324A)),
                                    )
                                    BasicText(chat.time, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
                                }

                                BasicText(
                                    text = chat.eventOrRole,
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF4B47D6)),
                                )

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    BasicText(
                                        text = chat.snippet,
                                        maxLines = 1,
                                        modifier = Modifier.weight(1f),
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)),
                                    )

                                    if (chat.unreadCount > 0) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFFFD45C)),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            BasicText(
                                                text = "${chat.unreadCount}",
                                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2E1A47)),
                                            )
                                        }
                                    }
                                }
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
// A15 · CONVERSACIÓN DE CHAT CLIENTE ADMIN
// ==============================================================================
@Composable
internal fun RenderAdminChatConversationA15(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    var messageInput by remember { mutableStateOf("") }
    var sentMessages by remember { mutableStateOf(listOf<String>()) }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        // Chat scroll content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 118.dp, start = 16.dp, end = 16.dp, bottom = 156.dp),
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
                BasicText("Ayer", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF5F6377)))
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
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF2E324A), lineHeight = 21.sp),
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
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF2E324A), lineHeight = 21.sp),
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
                BasicText("Hoy", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF5F6377)))
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
                            BasicText("+20 Sillas Chiavari doradas", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E324A)))
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
                                BasicText("Por aprobar", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF7A5700)))
                            }
                        }
                    }
                }
            }

            sentMessages.forEach { msg ->
                Box(
                    modifier = Modifier
                        .align(Alignment.End)
                        .widthIn(max = 300.dp)
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 6.dp))
                        .background(AdminButtonGradient)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                ) {
                    BasicText(
                        text = msg,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color.White, lineHeight = 21.sp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
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
                        .rfIconButtonShadow(CircleShape, isAdmin = true)
                        .clip(CircleShape)
                        .clickable { onSelectScreenCode("BACK:A14") },
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
                    BasicText("Camila Rodríguez", maxLines = 1, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E324A)))
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
                        .rfIconButtonShadow(CircleShape, isAdmin = true)
                        .clip(CircleShape)
                        .clickable {
                            sentMessages = sentMessages + "Te compartí el enlace directo por WhatsApp 📲"
                        },
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE9E9F2), CircleShape)
                            .clickable {
                                sentMessages = sentMessages + "Te acabo de enviar la cotización actualizada con las 20 sillas Chiavari."
                            }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            RfIcon(kind = RfIconKind.BOX, tint = Color(0xFF2E324A), size = 16.dp)
                            BasicText("Enviar cotización", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E324A)))
                        }
                    }

                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE9E9F2), CircleShape)
                            .clickable {
                                sentMessages = sentMessages + "Confirmado el montaje para el viernes 23 a las 2:00 p. m."
                            }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            RfIcon(kind = RfIconKind.CALENDAR, tint = Color(0xFF2E324A), size = 16.dp)
                            BasicText("Confirmar hora", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E324A)))
                        }
                    }

                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE9E9F2), CircleShape)
                            .clickable {
                                sentMessages = sentMessages + "¡Gracias a ti, Camila! Cualquier ajuste adicional me avisas."
                            }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText("Gracias", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E324A)))
                    }
                }

                // Input row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .clickable { onSelectScreenCode("A03") },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.PLUS, tint = Color(0xFF5F6377), size = 20.dp)
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
                        BasicTextField(
                            value = messageInput,
                            onValueChange = { updated -> messageInput = updated },
                            singleLine = true,
                            textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF2E324A)),
                            cursorBrush = SolidColor(Color(0xFF5B6CF0)),
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { innerTextField ->
                                if (messageInput.isEmpty()) {
                                    BasicText("Escribe un mensaje…", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF5F6377)))
                                }
                                innerTextField()
                            },
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(AdminButtonGradient)
                            .clickable {
                                val trimmed = messageInput.trim()
                                if (trimmed.isNotEmpty()) {
                                    sentMessages = sentMessages + trimmed
                                    messageInput = ""
                                }
                            },
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
private data class AdminQuoteItem(
    val title: String,
    val subtitle: String,
    val statusTag: String,
    val tagBg: Color,
    val tagColor: Color,
    val amount: String,
)

@Composable
internal fun RenderAdminQuotesA22(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    var selectedFilter by remember { mutableStateOf("Enviadas · 3") }
    var resentQuotes by remember { mutableStateOf(setOf<String>()) }

    val allQuotes = remember {
        listOf(
            AdminQuoteItem("Cumpleaños de Mateo", "Laura Peña · enviada hoy", "Sin abrir", Color(0xFFFFF2C9), Color(0xFF7A5700), "RD$ 780"),
            AdminQuoteItem("Boda Real Rosa & Dorado", "Camila R. · enviada el 16 oct", "Vista", Color(0xFFDDF4F3), Color(0xFF1F8A84), "RD$ 4,500"),
            AdminQuoteItem("Graduación Promo 2026", "Ana Gómez · enviada el 12 oct", "Aprobada", Color(0xFFECEBFF), Color(0xFF4B47D6), "RD$ 2,300"),
        )
    }
    val quotes = when (selectedFilter) {
        "Aprobadas · 2" -> allQuotes.filter { quote -> quote.statusTag == "Aprobada" }
        "Rechazadas · 1" -> listOf(AdminQuoteItem("Baby Shower Sofía", "Carla M. · enviada el 5 oct", "Rechazada", Color(0xFFFDE3EA), Color(0xFFB42345), "RD$ 640"))
        "Vencidas · 1" -> listOf(AdminQuoteItem("Aniversario Bodas Oro", "Roberto P. · enviada el 1 oct", "Vencida", Color(0xFFE9E9F2), Color(0xFF5F6377), "RD$ 1,850"))
        else -> allQuotes
    }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 54.dp, start = 20.dp, end = 20.dp),
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
                    BasicText(
                        text = "Cotizaciones",
                        style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A)),
                    )
                    BasicText(
                        text = "6 este mes",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)),
                    )
                }
            }

            // Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf("Enviadas · 3", "Aprobadas · 2", "Rechazadas · 1", "Vencidas · 1").forEach { chip ->
                    val isSelected = selectedFilter == chip
                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .then(
                                if (isSelected) {
                                    Modifier.background(AdminButtonGradient)
                                } else {
                                    Modifier.background(Color.White).border(1.dp, Color(0xFFE9E9F2), RoundedCornerShape(999.dp))
                                },
                            )
                            .clickable { selectedFilter = chip }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = chip,
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

            quotes.forEach { quote ->
                val isResent = resentQuotes.contains(quote.title)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                        .padding(14.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                BasicText(quote.title, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E324A)))
                                BasicText(quote.subtitle, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
                            }
                            Box(
                                modifier = Modifier
                                    .height(24.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(quote.tagBg)
                                    .padding(horizontal = 9.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                BasicText(quote.statusTag, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = quote.tagColor))
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            BasicText(
                                text = quote.amount,
                                modifier = Modifier.weight(1f),
                                style = TextStyle(fontFamily = typo.displayFamily, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)),
                            )

                            Box(
                                modifier = Modifier
                                    .height(34.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(Color.White)
                                    .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(999.dp))
                                    .clickable { onSelectScreenCode("A03") }
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                    RfIcon(kind = RfIconKind.COPY, tint = Color(0xFF151729), size = 14.dp)
                                    BasicText("Duplicar", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)))
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .height(34.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(AdminButtonGradient)
                                    .clickable { resentQuotes = resentQuotes + quote.title }
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                    RfIcon(kind = if (isResent) RfIconKind.CHECK else RfIconKind.SEND, tint = Color.White, size = 14.dp)
                                    BasicText(
                                        text = if (isResent) "Reenviada" else "Reenviar",
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White),
                                    )
                                }
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
// A29 · NOTIFICACIONES DEL NEGOCIO
// ==============================================================================
@Composable
internal fun RenderAdminNotificationsA29(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 54.dp, start = 20.dp, end = 20.dp, bottom = 32.dp),
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

            BasicText(
                text = "Notificaciones",
                style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A)),
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
                    text = "3 nuevas",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7A5700)),
                )
            }
        }

        // Section HOY
        BasicText(
            text = "HOY",
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
                // Item 1
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF7F6FF))
                        .clickable { onSelectScreenCode("A02") }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFFFF2C9)),
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.ORDERS, tint = Color(0xFF7A5700), size = 18.dp)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        BasicText("Nueva solicitud de Ana Gómez", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)))
                        BasicText("Graduación Promo 2026 · 200 invitados", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)))
                    }
                    BasicText("6:40 p. m.", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)))
                }

                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))

                // Item 2
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF7F6FF))
                        .clickable { onSelectScreenCode("A20") }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFEEF6D6)),
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.WALLET, tint = Color(0xFF4E6A10), size = 18.dp)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        BasicText("Pago recibido · RD$ 1,350", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)))
                        BasicText("Camila Rodríguez · Boda Real", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)))
                    }
                    BasicText("2:15 p. m.", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)))
                }

                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))

                // Item 3
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF7F6FF))
                        .clickable { onSelectScreenCode("s71") }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFECEBFF)),
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.STAR, tint = Color(0xFF4B47D6), size = 18.dp)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        BasicText("Reseña nueva por aprobar", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)))
                        BasicText("5 estrellas · Gala Corporativa", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)))
                    }
                    BasicText("11:02 a. m.", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)))
                }
            }
        }

        // Section AYER
        BasicText(
            text = "AYER",
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
                // Item 4
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
                        RfIcon(kind = RfIconKind.WARNING, tint = Color(0xFFB42345), size = 18.dp)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        BasicText("Stock bajo · Mesa dulce temática", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF151729)))
                        BasicText("Quedan 2 unidades", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)))
                    }
                    BasicText("Ayer", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)))
                }

                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))

                // Item 5
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectScreenCode("A14") }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFDDF4F3)),
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.CHAT, tint = Color(0xFF1F8A84), size = 18.dp)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        BasicText("2 mensajes de Camila", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF151729)))
                        BasicText("Sobre sillas Chiavari adicionales", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)))
                    }
                    BasicText("Ayer", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)))
                }

                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))

                // Item 6
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectScreenCode("A17") }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFEEF6D6)),
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.CHECK, tint = Color(0xFF4E6A10), size = 18.dp)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        BasicText("Yuli completó una tarea", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF151729)))
                        BasicText("Confirmar paleta floral", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)))
                    }
                    BasicText("Ayer", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, color = Color(0xFF5F6377)))
                }
            }
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
            .background(Color(0xFFF8F7FC)),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(Color(0xFFF8F7FC))
            drawRect(
                Brush.radialGradient(
                    listOf(Color(0xFFF9E3F1), Color(0x00F9E3F1)),
                    center = Offset(0f, 0f),
                    radius = size.width * 1.1f,
                ),
            )
            drawRect(
                Brush.radialGradient(
                    listOf(Color(0xFFE6E7FF), Color(0x00E6E7FF)),
                    center = Offset(size.width, size.height * 0.40f),
                    radius = size.width * 0.9f,
                ),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 96.dp, start = 20.dp, end = 20.dp, bottom = 96.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Logo y saludo
            Column(
                modifier = Modifier.padding(horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                RfLogo(
                    customSize = 88.dp,
                    insetPadding = false,
                    modifier = Modifier.shadow(elevation = 6.dp, shape = CircleShape, spotColor = Color(0x59B450B4)),
                )

                BasicText("Hola, María", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A)))
                BasicText(
                    text = "Tu cuenta tiene acceso de administración.\n¿Cómo quieres entrar?",
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontSize = 15.sp,
                        color = Color(0xFF5F6377),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 21.sp,
                    ),
                )
            }

            Spacer(modifier = Modifier.height(44.dp))

            // 2 Tarjetas de modo
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Administración Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfHeroBannerShadow(RoundedCornerShape(24.dp), isAdmin = true)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0), Color(0xFF2FB8C8))))
                        .clickable { onSelectScreenCode("A01") }
                        .padding(18.dp),
                ) {
                    // Decoración oficial: círculo tenue + estrella dorada
                    Canvas(
                        modifier = Modifier
                            .size(70.dp)
                            .align(Alignment.TopEnd),
                    ) {
                        drawCircle(Color.White.copy(alpha = 0.10f), radius = 35.dp.toPx(), center = Offset(35.dp.toPx(), 15.dp.toPx()))
                        val cx = 35.dp.toPx()
                        val cy = 20.dp.toPx()
                        val sc = 0.9f
                        val star = Path().apply {
                            moveTo(cx + 0f * sc, cy - 10f * sc)
                            lineTo(cx + 2.35f * sc, cy - 3.24f * sc)
                            lineTo(cx + 9.5f * sc, cy - 3.1f * sc)
                            lineTo(cx + 3.8f * sc, cy + 1.24f * sc)
                            lineTo(cx + 5.9f * sc, cy + 8.1f * sc)
                            lineTo(cx + 0f * sc, cy + 4f * sc)
                            lineTo(cx - 5.9f * sc, cy + 8.1f * sc)
                            lineTo(cx - 3.8f * sc, cy + 1.24f * sc)
                            lineTo(cx - 9.5f * sc, cy - 3.1f * sc)
                            lineTo(cx - 2.35f * sc, cy - 3.24f * sc)
                            close()
                        }
                        drawPath(star, Color(0xFFFFD45C))
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0x33FFFFFF)),
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.BAR_CHART, tint = Color.White, size = 24.dp)
                        }

                        Column {
                            BasicText("Administración", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White))
                            Spacer(modifier = Modifier.height(2.dp))
                            BasicText("Eventos, inventario, pagos y clientes", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFFE9EEFF)))
                        }
                    }
                }

                // Vista de cliente Card con sombra rosa oficial
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 14.dp, shape = RoundedCornerShape(24.dp), spotColor = Color(0x66C850AA))
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
                            RfIcon(kind = RfIconKind.PARTY, tint = Color.White, size = 24.dp)
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            BasicText("Vista de cliente", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A)))
                            BasicText("Mira la app como la ven tus clientes", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF5F6377)))
                        }

                        RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = Color(0xFF5F6377), size = 20.dp)
                    }
                }
            }
        }

        // Bottom: Recordar mi elección
        var rememberChoice by remember { mutableStateOf(true) }
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .clickable { rememberChoice = !rememberChoice }
                .padding(bottom = 44.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .then(
                        if (rememberChoice) {
                            Modifier.background(AdminButtonGradient)
                        } else {
                            Modifier.background(Color.White).border(1.5.dp, Color(0xFFDCDDEA), RoundedCornerShape(7.dp))
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (rememberChoice) {
                    RfIcon(kind = RfIconKind.CHECK, tint = Color.White, size = 14.dp)
                }
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
    var businessName by remember { mutableStateOf("Rosa Fiesta") }
    var whatsappNumber by remember { mutableStateOf("+1 (829) 942-4971") }
    var scheduleHours by remember { mutableStateOf("Lun – Sáb · 9:00 – 6:00") }
    var depositPercent by remember { mutableStateOf(30) }
    var rncNumber by remember { mutableStateOf("RNC [número]") }
    var extraZonesCount by remember { mutableStateOf(0) }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 54.dp, start = 20.dp, end = 20.dp),
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

                BasicText(
                    text = "Ajustes del negocio",
                    style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A)),
                )
            }

            // Section 1 · Negocio
            BasicText(
                text = "NEGOCIO",
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.08.em, color = Color(0xFF5F6377)),
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp)),
            ) {
                Column {
                    val businessItems = listOf(
                        Triple(RfIconKind.STORE, "Nombre", businessName to { updated: String -> businessName = updated }),
                        Triple(RfIconKind.WHATSAPP, "WhatsApp", whatsappNumber to { updated: String -> whatsappNumber = updated }),
                        Triple(RfIconKind.CLOCK, "Horario", scheduleHours to { updated: String -> scheduleHours = updated }),
                    )

                    businessItems.forEachIndexed { idx, (icon, label, statePair) ->
                        val (value, onValueChange) = statePair
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
                            RfIcon(kind = icon, tint = Color(0xFF5F6377), size = 18.dp)
                            BasicText(label, modifier = Modifier.weight(1f), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF151729)))
                            BasicTextField(
                                value = value,
                                onValueChange = onValueChange,
                                singleLine = true,
                                textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF5F6377), textAlign = TextAlign.End),
                                cursorBrush = SolidColor(Color(0xFF5B6CF0)),
                            )
                        }
                    }
                }
            }

            // Section 2 · Entregas · costo por zona
            BasicText(
                text = "ENTREGAS · COSTO POR ZONA",
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.08.em, color = Color(0xFF5F6377)),
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp)),
            ) {
                Column {
                    val baseZones = listOf(
                        Triple(Color(0xFF5B6CF0), "Distrito Nacional", "RD$ 250"),
                        Triple(Color(0xFF2FB8C8), "Santo Domingo Este y Oeste", "RD$ 350"),
                        Triple(Color(0xFFB9D84A), "Fuera de Santo Domingo", "A cotizar"),
                    )
                    val zones = if (extraZonesCount > 0) {
                        baseZones + Triple(Color(0xFF7C5CF0), "Santo Domingo Norte", "RD$ 300")
                    } else {
                        baseZones
                    }

                    zones.forEachIndexed { idx, (dotColor, name, cost) ->
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
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(dotColor))
                            BasicText(name, modifier = Modifier.weight(1f), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF151729)))
                            if (cost == "A cotizar") {
                                BasicText(cost, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF5F6377)))
                            } else {
                                BasicText(cost, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)))
                            }
                        }
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clickable { extraZonesCount = if (extraZonesCount == 0) 1 else 0 },
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            RfIcon(kind = RfIconKind.PLUS, tint = Color(0xFF4B47D6), size = 16.dp)
                            BasicText("Agregar zona", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B47D6)))
                        }
                    }
                }
            }

            // Section 3 · Reservas
            BasicText(
                text = "RESERVAS",
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.08.em, color = Color(0xFF5F6377)),
            )

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
                        BasicText("Anticipo para reservar", modifier = Modifier.weight(1f), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF151729)))
                        Row(
                            modifier = Modifier
                                .height(34.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(Color(0xFFF2F2F8))
                                .padding(horizontal = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .clickable { depositPercent = (depositPercent - 5).coerceAtLeast(10) },
                                contentAlignment = Alignment.Center,
                            ) {
                                RfIcon(kind = RfIconKind.MINUS, tint = Color(0xFF5B6CF0), size = 14.dp)
                            }
                            BasicText("$depositPercent%", modifier = Modifier.widthIn(min = 38.dp), style = TextStyle(fontFamily = typo.displayFamily, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center, color = Color(0xFF151729)))
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .clickable { depositPercent = (depositPercent + 5).coerceAtMost(100) },
                                contentAlignment = Alignment.Center,
                            ) {
                                RfIcon(kind = RfIconKind.PLUS, tint = Color(0xFF5B6CF0), size = 14.dp)
                            }
                        }
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF0F0F6)))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        BasicText("Datos de facturación", modifier = Modifier.weight(1f), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF151729)))
                        BasicTextField(
                            value = rncNumber,
                            onValueChange = { updated -> rncNumber = updated },
                            singleLine = true,
                            textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF5F6377), textAlign = TextAlign.End),
                            cursorBrush = SolidColor(Color(0xFF5B6CF0)),
                        )
                        RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = Color(0xFF5F6377), size = 18.dp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
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
                    .background(AdminButtonGradient)
                    .clickable { onSelectScreenCode("A12") },
                contentAlignment = Alignment.Center,
            ) {
                BasicText("Guardar cambios", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White))
            }
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
    var selectedFilter by remember { mutableStateOf("Por revisar · 2") }
    var card1Decision by remember { mutableStateOf<String?>(null) }
    var card2Decision by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 54.dp, start = 20.dp, end = 20.dp),
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
                    BasicText(
                        text = "Cancelaciones",
                        style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A)),
                    )
                    BasicText(
                        text = "2 solicitudes por revisar",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)),
                    )
                }
            }

            // Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf("Por revisar · 2", "Reembolsos · 1", "Historial").forEach { chip ->
                    val isSelected = selectedFilter == chip
                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .then(
                                if (isSelected) {
                                    Modifier.background(AdminButtonGradient)
                                } else {
                                    Modifier.background(Color.White).border(1.dp, Color(0xFFE9E9F2), RoundedCornerShape(999.dp))
                                },
                            )
                            .clickable { selectedFilter = chip }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = chip,
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

            // Card 1 · Bautizo de Lucas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                    .padding(14.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDDF4F3)),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText("R", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1F8A84)))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            BasicText("Bautizo de Lucas", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E324A)))
                            BasicText("Rafael Soto · Sáb 31 oct", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)))
                        }
                        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Box(
                                modifier = Modifier
                                    .height(24.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(Color(0xFFFCE3E8))
                                    .padding(horizontal = 9.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                BasicText(card1Decision ?: "Cancelación", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal, color = Color(0xFFDC2B48)))
                            }
                            BasicText("hace 2 h", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.5.sp, color = Color(0xFF5F6377)))
                        }
                    }

                    // 3-column refund metrics
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFF6F6FB))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            BasicText("Pagado", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
                            BasicText("RD$ 1,200", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            BasicText("Política", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
                            BasicText("90%", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            BasicText("Reembolso", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF5F6377)))
                            BasicText("RD$ 1,080", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4E6A10)))
                        }
                    }

                    BasicText(
                        text = "Motivo: “Cambio de planes”. 11 días antes · 14 artículos vuelven al inventario.",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, lineHeight = 18.sp, color = Color(0xFF5F6377)),
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White)
                                .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(16.dp))
                                .clickable { card1Decision = "Rechazada" },
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText("Rechazar", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)))
                        }
                        Box(
                            modifier = Modifier
                                .weight(1.4f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(AdminButtonGradient)
                                .clickable { onSelectScreenCode("A33") },
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText("Revisar reembolso", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White))
                        }
                    }
                }
            }

            // Card 2 · Cumpleaños de Mateo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                    .padding(14.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEEF6D6)),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText("L", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4E6A10)))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            BasicText("Cumpleaños de Mateo", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E324A)))
                            BasicText("Laura Peña · 40 invitados", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)))
                        }
                        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Box(
                                modifier = Modifier
                                    .height(24.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(Color(0xFFECEBFF))
                                    .padding(horizontal = 9.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                BasicText(card2Decision ?: "Reprogramar", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF4B47D6)))
                            }
                            BasicText("hace 5 h", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.5.sp, color = Color(0xFF5F6377)))
                        }
                    }

                    // Reschedule dates
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF6F6FB))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                        ) {
                            BasicText(
                                text = "Sáb 7 nov",
                                style = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF5F6377),
                                    textDecoration = TextDecoration.LineThrough,
                                ),
                            )
                        }
                        RfIcon(kind = RfIconKind.ARROW_RIGHT, tint = Color(0xFF151729), size = 18.dp)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFECEBFF))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                        ) {
                            BasicText(
                                text = "Sáb 14 nov",
                                style = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4B47D6),
                                ),
                            )
                        }
                    }

                    // Warning banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFF2C9))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                            RfIcon(kind = RfIconKind.WARNING, tint = Color(0xFF5E4300), size = 16.dp)
                            BasicText(
                                text = "La mesa dulce temática ya está reservada el 14 nov. Sugiere otra fecha o un sustituto.",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, lineHeight = 18.sp, color = Color(0xFF5E4300)),
                            )
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White)
                                .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(16.dp))
                                .clickable { card2Decision = "21 nov propuesto" },
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText("Proponer fecha", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)))
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(AdminButtonGradient)
                                .clickable { card2Decision = "Aprobado" },
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText("Aprobar", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White))
                        }
                    }
                }
            }

            // Section caption
            BasicText(
                text = "REEMBOLSOS EN PROCESO",
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.08.em, color = Color(0xFF5F6377)),
            )

            // Card 3 · Gabriela Núñez
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                    .clickable { onSelectScreenCode("A33") }
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
                            .clip(CircleShape)
                            .background(Color(0xFFFDE6F2)),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText("G", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFC2338F)))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        BasicText("Gabriela Núñez · Baby shower", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)))
                        BasicText("Transferencia · enviado 14 oct", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)))
                    }
                    BasicText("RD$ 520", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)))
                }
            }

            Spacer(modifier = Modifier.height(110.dp))
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
    var refundMethod by remember { mutableStateOf("Transferencia") }
    var releaseArticles by remember { mutableStateOf(true) }
    var notifyClientWhatsApp by remember { mutableStateOf(true) }
    var clientMessage by remember {
        mutableStateOf("Hola Rafael, aprobamos tu cancelación. Te enviamos RD$ 1,080 por transferencia en 3 a 5 días hábiles. ¡Ojalá celebremos pronto!")
    }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 54.dp, start = 20.dp, end = 20.dp),
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
                        .clickable { onSelectScreenCode("BACK:A32") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText("Reembolso", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A)))
                    BasicText("Rafael Soto · Bautizo de Lucas", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)))
                }
            }

            // Hero banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfHeroBannerShadow(RoundedCornerShape(22.dp), isAdmin = true)
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

                val isTransfer = refundMethod == "Transferencia"
                // Transferencia
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(if (isTransfer) 2.dp else 1.dp, if (isTransfer) Color(0xFF5B6CF0) else Color(0xFFECECF5), RoundedCornerShape(20.dp))
                        .clickable { refundMethod = "Transferencia" }
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
                                .border(if (isTransfer) 6.dp else 2.dp, if (isTransfer) Color(0xFF5B6CF0) else Color(0xFFDCDDEA), CircleShape),
                        )
                    }
                }

                // Efectivo
                val isCash = refundMethod == "Efectivo"
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(if (isCash) 2.dp else 1.dp, if (isCash) Color(0xFF5B6CF0) else Color(0xFFECECF5), RoundedCornerShape(20.dp))
                        .clickable { refundMethod = "Efectivo" }
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
                                .border(if (isCash) 6.dp else 2.dp, if (isCash) Color(0xFF5B6CF0) else Color(0xFFDCDDEA), CircleShape),
                        )
                    }
                }
            }

            // Card 2 switches
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
                            .clickable { releaseArticles = !releaseArticles }
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
                                .then(
                                    if (releaseArticles) {
                                        Modifier.background(AdminButtonGradient)
                                    } else {
                                        Modifier.background(Color(0xFFE3E4EF))
                                    },
                                )
                                .padding(3.dp),
                            contentAlignment = if (releaseArticles) Alignment.CenterEnd else Alignment.CenterStart,
                        ) {
                            Box(modifier = Modifier.size(22.dp).clip(CircleShape).background(Color.White))
                        }
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF2F0F5)))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { notifyClientWhatsApp = !notifyClientWhatsApp }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        BasicText("Avisar al cliente por WhatsApp", modifier = Modifier.weight(1f), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF151729)))

                        Box(
                            modifier = Modifier
                                .size(width = 46.dp, height = 28.dp)
                                .clip(CircleShape)
                                .then(
                                    if (notifyClientWhatsApp) {
                                        Modifier.background(AdminButtonGradient)
                                    } else {
                                        Modifier.background(Color(0xFFE3E4EF))
                                    },
                                )
                                .padding(3.dp),
                            contentAlignment = if (notifyClientWhatsApp) Alignment.CenterEnd else Alignment.CenterStart,
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
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                ) {
                    BasicTextField(
                        value = clientMessage,
                        onValueChange = { updated -> clientMessage = updated },
                        textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF3A3D52), lineHeight = 21.sp),
                        cursorBrush = SolidColor(Color(0xFF5B6CF0)),
                        modifier = Modifier.fillMaxWidth(),
                    )
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
    val toneTemplates = remember {
        mapOf(
            "Cálida" to "¡Gracias, Patricia! Nos alegra que el baby shower quedara bonito. Tienes razón con los manteles: ya los sumamos a nuestro checklist de montaje para que no vuelva a pasar. Te debemos un detalle en tu próximo evento. — María, Rosa Fiesta",
            "Formal" to "Estimada Patricia, agradecemos sinceramente su calificación. Tomamos nota sobre los dos manteles faltantes y hemos actualizado nuestro protocolo de supervisión previo al montaje. Esperamos servirle nuevamente. — Equipo Rosa Fiesta",
            "Breve" to "¡Muchas gracias por tu reseña, Patricia! Ya corregimos el checklist de manteles para futuros montajes. ¡Te esperamos pronto! — María",
        )
    }
    var selectedTone by remember { mutableStateOf("Cálida") }
    var replyDraft by remember { mutableStateOf(toneTemplates["Cálida"].orEmpty()) }
    var publishAndVerify by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 54.dp, start = 20.dp, end = 20.dp),
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
                    BasicText("Responder reseña", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A)))
                    BasicText("Pendiente · llegó hace 1 día", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)))
                }
            }

            // Review Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
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
                            BasicText("Patricia Lora", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E324A)))
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
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.5.sp, color = Color(0xFF2E324A), lineHeight = 21.sp),
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
                BasicText("Rosa redactó una respuesta", modifier = Modifier.weight(1f), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E324A)))
                Row(
                    modifier = Modifier.clickable {
                        val tones = listOf("Cálida", "Formal", "Breve")
                        val nextIdx = (tones.indexOf(selectedTone) + 1) % tones.size
                        val nextTone = tones[nextIdx]
                        selectedTone = nextTone
                        replyDraft = toneTemplates[nextTone].orEmpty()
                    },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    RfIcon(kind = RfIconKind.REFRESH, tint = Color(0xFF4B47D6), size = 15.dp)
                    BasicText("Otra", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B47D6)))
                }
            }

            // Chips
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Cálida", "Formal", "Breve").forEach { tone ->
                    val isSelected = selectedTone == tone
                    Box(
                        modifier = Modifier
                            .height(32.dp)
                            .clip(CircleShape)
                            .then(
                                if (isSelected) {
                                    Modifier.background(AdminButtonGradient)
                                } else {
                                    Modifier.background(Color.White).border(1.dp, Color(0xFFE9E9F2), CircleShape)
                                },
                            )
                            .clickable {
                                selectedTone = tone
                                replyDraft = toneTemplates[tone].orEmpty()
                            }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            tone,
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                color = if (isSelected) Color.White else Color(0xFF2E324A),
                            ),
                        )
                    }
                }
            }

            // Textarea Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(2.dp, Color(0xFF5B6CF0), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                BasicTextField(
                    value = replyDraft,
                    onValueChange = { updated -> replyDraft = updated },
                    textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.5.sp, color = Color(0xFF2E324A), lineHeight = 21.sp),
                    cursorBrush = SolidColor(Color(0xFF5B6CF0)),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // Switch row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { publishAndVerify = !publishAndVerify }
                    .padding(horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    BasicText("Publicar y verificar la reseña", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E324A)))
                    BasicText("La respuesta será visible en la app", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.5.sp, color = Color(0xFF5F6377)))
                }

                Box(
                    modifier = Modifier
                        .size(width = 46.dp, height = 28.dp)
                        .clip(CircleShape)
                        .then(
                            if (publishAndVerify) {
                                Modifier.background(AdminButtonGradient)
                            } else {
                                Modifier.background(Color(0xFFE3E4EF))
                            },
                        )
                        .padding(3.dp),
                    contentAlignment = if (publishAndVerify) Alignment.CenterEnd else Alignment.CenterStart,
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
                    .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = true)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AdminButtonGradient)
                    .clickable { onSelectScreenCode("A12") },
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RfIcon(kind = RfIconKind.SEND, tint = Color.White, size = 20.dp)
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
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(Color(0xFFF6F6FB))
            drawRect(
                Brush.radialGradient(
                    listOf(Color(0xFFE6E7FF), Color(0x00E6E7FF)),
                    center = Offset(size.width, 0f),
                    radius = size.width * 1.1f,
                ),
            )
            drawRect(
                Brush.radialGradient(
                    listOf(Color(0xFFF1E9FB), Color(0x00F1E9FB)),
                    center = Offset(0f, size.height * 0.12f),
                    radius = size.width * 0.8f,
                ),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 54.dp, start = 20.dp, end = 20.dp, bottom = 116.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Header: Botón volver + Título + Subtítulo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(elevation = 6.dp, shape = CircleShape, spotColor = Color(0x334650B4), ambientColor = Color.Transparent)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), CircleShape)
                        .clickable { onSelectScreenCode("BACK:A01") },
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "Solicitudes",
                        style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)),
                    )
                    BasicText(
                        text = "Cotizaciones que piden tus clientes",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)),
                    )
                }
            }

            // Chips de filtro
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(36.dp)
                        .shadow(elevation = 8.dp, shape = RoundedCornerShape(999.dp), spotColor = Color(0xB35B6CF0), ambientColor = Color.Transparent)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
                        .padding(horizontal = 14.dp),
                ) {
                    BasicText("Nuevas · 0", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White))
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(36.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE9E9F2), RoundedCornerShape(999.dp))
                        .padding(horizontal = 14.dp),
                ) {
                    BasicText("En revisión", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF5F6377)))
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(36.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE9E9F2), RoundedCornerShape(999.dp))
                        .padding(horizontal = 14.dp),
                ) {
                    BasicText("Enviadas", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF5F6377)))
                }
            }

            // Ilustración central y textos
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 70.dp),
            ) {
                // Círculo de 120dp con anillo exterior de 12px (144dp total)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(144.dp)
                        .background(Color(0x8CECEBFF), CircleShape),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Color(0xFFECEBFF), Color(0xFFDDF4F3)))),
                    ) {
                        RfIcon(kind = RfIconKind.CHECK, tint = Color(0xFF4B47D6), size = 52.dp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                BasicText(
                    text = "Todo al día",
                    style = TextStyle(fontFamily = typo.displayFamily, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)),
                )

                Spacer(modifier = Modifier.height(10.dp))

                BasicText(
                    text = "No hay solicitudes nuevas. Te avisaremos cuando un cliente pida una cotización.",
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontSize = 15.sp,
                        color = Color(0xFF5F6377),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 22.sp,
                    ),
                    modifier = Modifier.width(290.dp),
                )

                Spacer(modifier = Modifier.height(26.dp))

                // Botones de acción
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .shadow(elevation = 12.dp, shape = RoundedCornerShape(16.dp), spotColor = Color(0xBF5B6CF0))
                            .clip(RoundedCornerShape(16.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
                            .clickable { onSelectScreenCode("A05") },
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            RfIcon(kind = RfIconKind.PLUS, tint = Color.White, size = 18.dp)
                            BasicText(
                                text = "Crear evento manual",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White),
                            )
                        }
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = true)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(16.dp))
                            .clickable { onSelectScreenCode("A08") },
                    ) {
                        BasicText(
                            text = "Crear con Rosa IA",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)),
                        )
                    }
                }
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
    val skGradient = Brush.horizontalGradient(
        listOf(Color(0xFFE4E5F0), Color(0xFFF1F1F8), Color(0xFFE4E5F0)),
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F6FB)),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(Color(0xFFF6F6FB))
            drawRect(
                Brush.radialGradient(
                    listOf(Color(0xFFE6E7FF), Color(0x00E6E7FF)),
                    center = Offset(size.width, 0f),
                    radius = size.width * 1.1f,
                ),
            )
            drawRect(
                Brush.radialGradient(
                    listOf(Color(0xFFF1E9FB), Color(0x00F1E9FB)),
                    center = Offset(0f, size.height * 0.12f),
                    radius = size.width * 0.8f,
                ),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 54.dp, start = 20.dp, end = 20.dp, bottom = 116.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Header skeleton: Logo + 2 lines + circular avatar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                RfLogo(customSize = 44.dp)
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.fillMaxWidth(0.40f).height(12.dp).clip(RoundedCornerShape(10.dp)).background(skGradient))
                    Box(modifier = Modifier.fillMaxWidth(0.60f).height(18.dp).clip(RoundedCornerShape(10.dp)).background(skGradient))
                }
                Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(skGradient))
            }

            // Big hero card skeleton (132dp)
            Box(modifier = Modifier.fillMaxWidth().height(132.dp).clip(RoundedCornerShape(24.dp)).background(skGradient))

            // 3 small metric cards (74dp)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f).height(74.dp).clip(RoundedCornerShape(18.dp)).background(skGradient))
                Box(modifier = Modifier.weight(1f).height(74.dp).clip(RoundedCornerShape(18.dp)).background(skGradient))
                Box(modifier = Modifier.weight(1f).height(74.dp).clip(RoundedCornerShape(18.dp)).background(skGradient))
            }

            // Section label skeleton
            Box(modifier = Modifier.fillMaxWidth(0.30f).height(14.dp).clip(RoundedCornerShape(10.dp)).background(skGradient))

            // 2 medium cards (64dp)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f).height(64.dp).clip(RoundedCornerShape(18.dp)).background(skGradient))
                Box(modifier = Modifier.weight(1f).height(64.dp).clip(RoundedCornerShape(18.dp)).background(skGradient))
            }

            // Section label skeleton
            Box(modifier = Modifier.fillMaxWidth(0.36f).height(14.dp).clip(RoundedCornerShape(10.dp)).background(skGradient))

            // White Card with 2 rows
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
                    .padding(vertical = 4.dp),
            ) {
                Column {
                    // Row 1
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                    ) {
                        Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(skGradient))
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(modifier = Modifier.fillMaxWidth(0.70f).height(13.dp).clip(RoundedCornerShape(10.dp)).background(skGradient))
                            Box(modifier = Modifier.fillMaxWidth(0.45f).height(11.dp).clip(RoundedCornerShape(10.dp)).background(skGradient))
                        }
                        Box(modifier = Modifier.size(width = 56.dp, height = 16.dp).clip(RoundedCornerShape(10.dp)).background(skGradient))
                    }

                    // Divider
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF2F0F5)))

                    // Row 2
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                    ) {
                        Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(skGradient))
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(modifier = Modifier.fillMaxWidth(0.70f).height(13.dp).clip(RoundedCornerShape(10.dp)).background(skGradient))
                            Box(modifier = Modifier.fillMaxWidth(0.45f).height(11.dp).clip(RoundedCornerShape(10.dp)).background(skGradient))
                        }
                        Box(modifier = Modifier.size(width = 56.dp, height = 16.dp).clip(RoundedCornerShape(10.dp)).background(skGradient))
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
        modifier = Modifier.fillMaxSize(),
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
                RfIcon(kind = RfIconKind.WIFI_OFF, tint = Color.White, size = 18.dp)
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
                .verticalScroll(rememberScrollState())
                .padding(top = 112.dp, start = 20.dp, end = 20.dp, bottom = 116.dp),
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
                        .clickable { onSelectScreenCode("BACK:A09") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 22.dp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText("Inventario", style = TextStyle(fontFamily = typo.displayFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A)))
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
                        .size(144.dp)
                        .clip(CircleShape)
                        .background(Color(0x80FCE3E8)),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFCE3E8)),
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(
                            kind = RfIconKind.WARNING,
                            tint = Color(0xFFB42345),
                            size = 50.dp,
                            strokeWidthFactor = 0.075f,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                BasicText(
                    text = "No pudimos cargar el inventario",
                    style = TextStyle(fontFamily = typo.displayFamily, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A), textAlign = androidx.compose.ui.text.style.TextAlign.Center),
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
                            .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = true)
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
                            .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = true)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(16.dp))
                            .clickable { onSelectScreenCode("A09") },
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText("Ver la última copia guardada", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Normal, color = Color(0xFF2E324A)))
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
