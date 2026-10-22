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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
// s18 · CATÁLOGO (APP PERSONAL)
// ==============================================================================

@Composable
internal fun RenderPersonalCatalogS18(
    onSelectScreenCode: (String) -> Unit,
) {
    val typo = LocalRfTypography.current
    var selectedCategory by remember { mutableStateOf("Todas · 15") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 54.dp, start = 20.dp, end = 20.dp),
    ) {
        // Cabecera: Título + Botón Cuadrícula + Botón Mi Evento con badge 16
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            BasicText(
                text = "Catálogo",
                style = typo.heroTitle.copy(fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)),
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), CircleShape)
                        .clickable { onSelectScreenCode("s20") },
                ) {
                    RfIcon(kind = RfIconKind.CATALOG, tint = Color(0xFF151729), size = 20.dp)
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clickable { onSelectScreenCode("s37") },
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.dp, Color(0xFFECECF5), CircleShape),
                    ) {
                        RfIcon(kind = RfIconKind.ORDERS, tint = Color(0xFF151729), size = 20.dp)
                    }
                    // Badge 16
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 4.dp, y = (-4).dp)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFD45C)),
                    ) {
                        BasicText("16", style = typo.sectionCap.copy(fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2E1A47)))
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // Campo de búsqueda con voz
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(14.dp))
                .padding(start = 14.dp, end = 6.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    RfIcon(kind = RfIconKind.SEARCH, tint = Color(0xFF5F6377), size = 20.dp)
                    BasicText("Buscar sillas, mesas, flores…", style = typo.body.copy(fontSize = 15.sp, color = Color(0xFF5F6377)))
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF5EAF9))
                        .clickable { onSelectScreenCode("s35") },
                ) {
                    RfIcon(kind = RfIconKind.MIC, tint = Color(0xFFB54BD6), size = 20.dp)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Chips de categoría
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
        ) {
            val categories = listOf("Todas · 15", "Iluminación", "Floral", "Globos", "Mobiliario")
            categories.forEach { cat ->
                val isSelected = cat == selectedCategory
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(36.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(
                            if (isSelected) {
                                Brush.horizontalGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0)))
                            } else {
                                SolidColor(Color.White)
                            },
                        )
                        .border(1.dp, if (isSelected) Color.Transparent else Color(0xFFE9E9F2), RoundedCornerShape(999.dp))
                        .clickable { selectedCategory = cat }
                        .padding(horizontal = 14.dp),
                ) {
                    BasicText(
                        text = cat,
                        style = typo.body.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) Color.White else Color(0xFF151729),
                        ),
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Banner asistente Rosa IA
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Brush.horizontalGradient(listOf(Color(0xFFFCE6F3), Color(0xFFECE4FD))))
                .clickable { onSelectScreenCode("s26") }
                .padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                RosaAvatar(size = RosaAvatarSize.MEDIUM)
                Column(modifier = Modifier.weight(1f)) {
                    BasicText("¿No sabes por dónde empezar?", style = typo.body.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)))
                    BasicText("Rosa arma tu lista en 2 minutos.", style = typo.secondary.copy(fontSize = 13.sp, color = Color(0xFF5F6377)))
                }
                RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF865DEC), size = 18.dp, modifier = Modifier.rotate(180f))
            }
        }

        Spacer(Modifier.height(12.dp))

        // Cuadrícula 2 columnas de productos
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.verticalScroll(rememberScrollState()),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                PersonalProductCard(
                    title = "Silla Tiffany Cristal",
                    desc = "Acrílica transparente",
                    price = "4",
                    photoKey = RfPhotoKey.SILLA_TIFFANY,
                    bg = Color(0xFFEFE8FD),
                    onOpenDetail = { onSelectScreenCode("s19") },
                    modifier = Modifier.weight(1f),
                )
                PersonalProductCard(
                    title = "Mesa dulce temática",
                    desc = "Decoración completa dulces",
                    price = "150",
                    photoKey = RfPhotoKey.MESA_HELADOS,
                    bg = Color(0xFFEEF6D6),
                    onOpenDetail = { onSelectScreenCode("s19") },
                    modifier = Modifier.weight(1f),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                PersonalProductCard(
                    title = "Arco de globos",
                    desc = "Rosa y nude, entrada",
                    price = "95",
                    photoKey = RfPhotoKey.ARCO_ROSA,
                    bg = Color(0xFFFDE6F2),
                    onOpenDetail = { onSelectScreenCode("s19") },
                    modifier = Modifier.weight(1f),
                )
                PersonalProductCard(
                    title = "Centro de mesa floral",
                    desc = "Flores base madera",
                    price = "35",
                    photoKey = RfPhotoKey.CENTRO_FLORAL,
                    bg = Color(0xFFFFF2C9),
                    badge = "Nuevo",
                    onOpenDetail = { onSelectScreenCode("s19") },
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(84.dp))
        }
    }
}

@Composable
private fun PersonalProductCard(
    title: String,
    desc: String,
    price: String,
    photoKey: RfPhotoKey,
    bg: Color,
    badge: String? = null,
    onOpenDetail: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val typo = LocalRfTypography.current
    var isFavorite by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(20.dp), spotColor = Color(0x1F4650B4))
            .clickable(onClick = onOpenDetail),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(128.dp)
                    .background(bg),
            ) {
                RfPhoto(
                    photoKey = photoKey,
                    contentDescription = title,
                    cornerRadius = 0.dp,
                    modifier = Modifier.fillMaxSize(),
                )
                if (badge != null) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .height(24.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color(0xFFFFD45C))
                            .padding(horizontal = 9.dp),
                    ) {
                        BasicText(badge, style = typo.sectionCap.copy(fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2E1A47)))
                    }
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.95f))
                        .clickable { isFavorite = !isFavorite },
                ) {
                    RfIcon(
                        kind = RfIconKind.HEART,
                        tint = if (isFavorite) Color(0xFFEA4EA9) else Color(0xFF5F6377),
                        filled = isFavorite,
                        size = 18.dp,
                    )
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                BasicText(title, maxLines = 1, style = typo.body.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)))
                Spacer(Modifier.height(2.dp))
                BasicText(desc, maxLines = 1, style = typo.secondary.copy(fontSize = 13.sp, color = Color(0xFF5F6377)))
                Spacer(Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        BasicText(price, style = typo.heroTitle.copy(fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFB54BD6)))
                        BasicText(" RD$/día", style = typo.secondary.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB54BD6)))
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9))),
                            )
                            .shadow(elevation = 8.dp, shape = CircleShape, spotColor = Color(0x99865DEC)),
                    ) {
                        RfIcon(kind = RfIconKind.PLUS, tint = Color.White, size = 18.dp)
                    }
                }
            }
        }
    }
}

// ==============================================================================
// s19 · FICHA / DETALLE DE PRODUCTO
// ==============================================================================

@Composable
internal fun RenderProductDetailS19(
    onSelectScreenCode: (String) -> Unit,
) {
    val typo = LocalRfTypography.current
    var count by remember { mutableStateOf(80) }
    var selectedColor by remember { mutableStateOf("Transparente") }
    var selectedTab by remember { mutableStateOf("Descripción") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFC)),
    ) {
        // Foto de portada (alto 400dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(410.dp),
        ) {
            RfPhoto(
                photoKey = RfPhotoKey.SILLA_TIFFANY,
                contentDescription = "Silla Tiffany Cristal",
                cornerRadius = 0.dp,
                modifier = Modifier.fillMaxSize(),
            )

            // Scrim superior para botones
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Brush.verticalGradient(listOf(Color(0x501A1A2E), Color.Transparent))),
            )

            // Botones superiores: Volver, Compartir, Favorito
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 54.dp, start = 20.dp, end = 20.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("s18") },
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 20.dp)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                    ) {
                        RfIcon(kind = RfIconKind.SHARE, tint = Color(0xFF151729), size = 20.dp)
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                    ) {
                        RfIcon(kind = RfIconKind.HEART, tint = Color(0xFFEA4EA9), filled = true, size = 20.dp)
                    }
                }
            }

            // Tira de miniaturas flotante (bottom 54dp)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 20.dp, bottom = 44.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0x6628283C))
                    .padding(6.dp),
            ) {
                Box(modifier = Modifier.size(50.dp).clip(RoundedCornerShape(12.dp)).border(2.5.dp, Color(0xFFEA4EA9), RoundedCornerShape(12.dp))) {
                    RfPhoto(photoKey = RfPhotoKey.SILLA_TIFFANY, contentDescription = "View 1", cornerRadius = 12.dp, modifier = Modifier.fillMaxSize())
                }
                Box(modifier = Modifier.size(50.dp).clip(RoundedCornerShape(12.dp))) {
                    RfPhoto(photoKey = RfPhotoKey.MESA_REDONDA, contentDescription = "View 2", cornerRadius = 12.dp, modifier = Modifier.fillMaxSize())
                }
                Box(modifier = Modifier.size(50.dp).clip(RoundedCornerShape(12.dp))) {
                    RfPhoto(photoKey = RfPhotoKey.CENTRO_FLORAL, contentDescription = "View 3", cornerRadius = 12.dp, modifier = Modifier.fillMaxSize())
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(50.dp).clip(RoundedCornerShape(12.dp)).background(Color.White),
                ) {
                    BasicText("+3", style = typo.body.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF62627A)))
                }
            }
        }

        // Hoja blanca con detalles (top = 380dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = 380.dp)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Color.White)
                .padding(horizontal = 24.dp, vertical = 24.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Título y Precio
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        BasicText("Silla Tiffany Cristal", style = typo.heroTitle.copy(fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)))
                        Spacer(Modifier.height(6.dp))
                        // Chip valoración 5.0
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(Color(0xFFFFF2C9))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                BasicText("★ 5.0", style = typo.body.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7A5700)))
                                BasicText("1 reseña", style = typo.secondary.copy(fontSize = 13.sp, color = Color(0xFF7A5700)))
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        BasicText("RD$ 4", style = typo.heroTitle.copy(fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFB54BD6)))
                        BasicText("por unidad / día", style = typo.secondary.copy(fontSize = 12.sp, color = Color(0xFF5F6377)))
                    }
                }

                // Selector de color
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row {
                        BasicText("Color: ", style = typo.body.copy(fontSize = 14.sp, color = Color(0xFF5F6377)))
                        BasicText(selectedColor, style = typo.body.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        val colors = listOf("Transparente" to Color(0xFF7C5CF0), "Rosa" to Color(0xFFEA4EA9), "Verde" to Color(0xFFA9CC3A), "Dorado" to Color(0xFFF0B92E))
                        colors.forEach { (name, col) ->
                            val isSel = name == selectedColor
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isSel) col else Color.White)
                                    .border(2.5.dp, col, CircleShape)
                                    .clickable { selectedColor = name },
                            ) {
                                if (isSel) {
                                    RfIcon(kind = RfIconKind.CHECK, tint = Color.White, size = 18.dp)
                                }
                            }
                        }
                    }
                }

                // Control segmentado
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF3F2F7))
                        .padding(4.dp),
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        val tabs = listOf("Descripción", "Detalles", "Reseñas")
                        tabs.forEach { t ->
                            val isSel = t == selectedTab
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSel) {
                                            Brush.horizontalGradient(listOf(Color(0xFFE04FAF), Color(0xFF865DEC)))
                                        } else {
                                            SolidColor(Color.Transparent)
                                        },
                                    )
                                    .clickable { selectedTab = t },
                            ) {
                                BasicText(
                                    text = t,
                                    style = typo.body.copy(
                                        fontSize = 14.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.SemiBold,
                                        color = if (isSel) Color.White else Color(0xFF5F6377),
                                    ),
                                )
                            }
                        }
                    }
                }

                BasicText(
                    text = "Silla de acrílico transparente de alta resistencia para bodas y recepciones. Acabado brillante impecable con soporte hasta 150 kg.",
                    style = typo.body.copy(fontSize = 15.sp, color = Color(0xFF5F6377), lineHeight = 22.sp),
                )
            }
        }

        // Barra inferior fija con stepper y botón de agregar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color.White)
                .border(1.dp, Color(0xFFF0EBF4))
                .padding(horizontal = 20.dp, vertical = 18.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Stepper
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF3F2F7))
                        .padding(horizontal = 4.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(44.dp)
                                .clickable { if (count > 1) count -= 10 },
                        ) {
                            BasicText("-", style = typo.heroTitle.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF865DEC)))
                        }
                        BasicText(
                            text = count.toString(),
                            style = typo.heroTitle.copy(fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)),
                            modifier = Modifier.padding(horizontal = 8.dp),
                        )
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(44.dp)
                                .clickable { count += 10 },
                        ) {
                            BasicText("+", style = typo.heroTitle.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF865DEC)))
                        }
                    }
                }

                // Botón primario
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
                        .clickable { onSelectScreenCode("s37") },
                ) {
                    BasicText(
                        text = "Agregar · RD$ ${count * 4}",
                        style = typo.body.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White),
                    )
                }
            }
        }
    }
}

// ==============================================================================
// s20 · CATEGORÍAS
// ==============================================================================

@Composable
internal fun RenderCategoriesScreenS20(
    onSelectScreenCode: (String) -> Unit,
) {
    val typo = LocalRfTypography.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 54.dp, start = 20.dp, end = 20.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            BasicText("Categorías", style = typo.heroTitle.copy(fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), CircleShape)
                    .clickable { onSelectScreenCode("s18") },
            ) {
                RfIcon(kind = RfIconKind.SEARCH, tint = Color(0xFF151729), size = 20.dp)
            }
        }

        Spacer(Modifier.height(18.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                CategoryGridCard("Iluminación", Color(0xFFDDF4F3), RfPhotoKey.CENTRO_FLORAL, Modifier.weight(1f)) { onSelectScreenCode("s18") }
                CategoryGridCard("Floral", Color(0xFFFDE6F2), RfPhotoKey.ARCO_ROSA, Modifier.weight(1f)) { onSelectScreenCode("s18") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                CategoryGridCard("Globos", Color(0xFFF3EEF6), RfPhotoKey.ARCO_ROSA, Modifier.weight(1f)) { onSelectScreenCode("s23") }
                CategoryGridCard("Mobiliario", Color(0xFFEFE8FD), RfPhotoKey.SILLA_TIFFANY, Modifier.weight(1f)) { onSelectScreenCode("s18") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                CategoryGridCard("Dulces / Postres", Color(0xFFEEF6D6), RfPhotoKey.MESA_HELADOS, Modifier.weight(1f)) { onSelectScreenCode("s18") }
                CategoryGridCard("Mantelería", Color(0xFFFFF2C9), RfPhotoKey.MESA_REDONDA, Modifier.weight(1f)) { onSelectScreenCode("s18") }
            }
        }
    }
}

@Composable
private fun CategoryGridCard(
    title: String,
    bg: Color,
    photoKey: RfPhotoKey,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val typo = LocalRfTypography.current
    Box(
        modifier = modifier
            .height(142.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(14.dp),
    ) {
        RfPhoto(
            photoKey = photoKey,
            contentDescription = title,
            cornerRadius = 16.dp,
            modifier = Modifier
                .size(72.dp)
                .align(Alignment.TopEnd)
                .offset(x = 6.dp, y = (-4).dp),
        )

        BasicText(
            text = title,
            style = typo.heroTitle.copy(fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1A2E)),
            modifier = Modifier.align(Alignment.BottomStart),
        )
    }
}

// ==============================================================================
// s21 · PAQUETES
// ==============================================================================

@Composable
internal fun RenderPackagesScreenS21(
    onSelectScreenCode: (String) -> Unit,
) {
    val typo = LocalRfTypography.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 54.dp, start = 20.dp, end = 20.dp),
    ) {
        BasicText("Paquetes", style = typo.heroTitle.copy(fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)))
        Spacer(Modifier.height(4.dp))
        BasicText("Colecciones temáticas, listas para tu evento", style = typo.secondary.copy(fontSize = 14.sp, color = Color(0xFF5F6377)))

        Spacer(Modifier.height(18.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
            PackageSummaryCard("Bodas Rosa", "Paquete romántico completo para bodas, con arco floral y centros de mesa.", "RD$ 850", "Ahorra 15%", RfPhotoKey.BODA, Color(0xFFFDE6F2)) { onSelectScreenCode("s22") }
            PackageSummaryCard("Quinceañera Premium", "Paquete de lujo para quinceañeras con arco floral y mesas dulces.", "RD$ 1,200", "Ahorra 12%", RfPhotoKey.QUINCEANERA, Color(0xFFEFE8FD)) { onSelectScreenCode("s22") }
            PackageSummaryCard("Cumpleaños Infantil", "Kit completo para fiestas infantiles: globos, banner y mobiliario.", "RD$ 450", "Ahorra 10%", RfPhotoKey.CUMPLE_INFANTIL, Color(0xFFEEF6D6)) { onSelectScreenCode("s22") }
            PackageSummaryCard("Evento Corporativo", "Solución profesional para eventos empresariales y conferencias.", "RD$ 600", "Ahorra 8%", RfPhotoKey.CORPORATIVO, Color(0xFFFFF2C9)) { onSelectScreenCode("s22") }
            Spacer(Modifier.height(84.dp))
        }
    }
}

@Composable
private fun PackageSummaryCard(
    title: String,
    desc: String,
    price: String,
    discountBadge: String,
    photoKey: RfPhotoKey,
    photoBg: Color,
    onClick: () -> Unit,
) {
    val typo = LocalRfTypography.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(10.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(116.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(photoBg),
            ) {
                RfPhoto(photoKey = photoKey, contentDescription = title, cornerRadius = 16.dp, modifier = Modifier.fillMaxSize())
            }

            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFFEEF6D6))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                ) {
                    BasicText(discountBadge, style = typo.sectionCap.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4E6A10)))
                }
                Spacer(Modifier.height(4.dp))
                BasicText(title, style = typo.heroTitle.copy(fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)))
                Spacer(Modifier.height(2.dp))
                BasicText(desc, maxLines = 2, style = typo.secondary.copy(fontSize = 13.sp, color = Color(0xFF5F6377)))
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    BasicText("desde ", style = typo.secondary.copy(fontSize = 13.sp, color = Color(0xFF5F6377)))
                    BasicText(price, style = typo.heroTitle.copy(fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFC2338F)))
                }
            }
        }
    }
}

// ==============================================================================
// s22 · DETALLE DE PAQUETE
// ==============================================================================

@Composable
internal fun RenderPackageDetailScreenS22(
    onSelectScreenCode: (String) -> Unit,
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
                .verticalScroll(rememberScrollState()),
        ) {
            // Foto de portada (280dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
            ) {
                RfPhoto(photoKey = RfPhotoKey.BODA, contentDescription = "Bodas Rosa", cornerRadius = 0.dp, modifier = Modifier.fillMaxSize())
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .offset(x = 20.dp, y = 54.dp)
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("s21") },
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 20.dp)
                }
            }

            Column(modifier = Modifier.padding(20.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFFEEF6D6))
                        .padding(horizontal = 9.dp, vertical = 3.dp),
                ) {
                    BasicText("Ahorra 15%", style = typo.sectionCap.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4E6A10)))
                }
                Spacer(Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    BasicText("Bodas Rosa", style = typo.heroTitle.copy(fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)))
                    Row(verticalAlignment = Alignment.Bottom) {
                        BasicText("desde ", style = typo.secondary.copy(fontSize = 14.sp, color = Color(0xFF5F6377)))
                        BasicText("RD$ 850", style = typo.heroTitle.copy(fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFC2338F)))
                    }
                }
                Spacer(Modifier.height(8.dp))
                BasicText(
                    text = "Paquete romántico completo para bodas. Incluye arco de globos, centros de mesa florales, sillas Tiffany y mesas redondas, con 15% de descuento.",
                    style = typo.body.copy(fontSize = 15.sp, color = Color(0xFF5F6377), lineHeight = 22.sp),
                )

                Spacer(Modifier.height(20.dp))

                BasicText("Incluye", style = typo.heroTitle.copy(fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)))
                Spacer(Modifier.height(10.dp))

                PackageItemRow("Arco de globos rosa", "RD$ 95 / día", "1", RfPhotoKey.ARCO_ROSA, Color(0xFFFDE6F2))
                Spacer(Modifier.height(8.dp))
                PackageItemRow("Centro de mesa floral", "RD$ 35 / día", "10", RfPhotoKey.CENTRO_FLORAL, Color(0xFFFFF2C9))
                Spacer(Modifier.height(8.dp))
                PackageItemRow("Silla Tiffany Cristal", "RD$ 4 / día", "80", RfPhotoKey.SILLA_TIFFANY, Color(0xFFEFE8FD))
                Spacer(Modifier.height(8.dp))
                PackageItemRow("Mesa redonda", "RD$ 25 / día", "8", RfPhotoKey.MESA_REDONDA, Color(0xFFEEF6D6))

                Spacer(Modifier.height(90.dp))
            }
        }

        // Botón inferior
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color.White)
                .border(1.dp, Color(0xFFF0EBF4))
                .padding(horizontal = 20.dp, vertical = 18.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
                    .clickable { onSelectScreenCode("s37") },
            ) {
                BasicText("Agregar paquete · RD$ 850", style = typo.body.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White))
            }
        }
    }
}

@Composable
private fun PackageItemRow(
    title: String,
    price: String,
    qty: String,
    photoKey: RfPhotoKey,
    bg: Color,
) {
    val typo = LocalRfTypography.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFF2EDF5), RoundedCornerShape(18.dp))
            .padding(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFFEA4EA9), Color(0xFF865DEC)))),
            ) {
                RfIcon(kind = RfIconKind.CHECK, tint = Color.White, size = 16.dp)
            }

            Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(bg)) {
                RfPhoto(photoKey = photoKey, contentDescription = title, cornerRadius = 12.dp, modifier = Modifier.fillMaxSize())
            }

            Column(modifier = Modifier.weight(1f)) {
                BasicText(title, style = typo.body.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)))
                BasicText(price, style = typo.secondary.copy(fontSize = 13.sp, color = Color(0xFF5F6377)))
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0xFFF2F2F8))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                BasicText(qty, style = typo.body.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)))
            }
        }
    }
}

// ==============================================================================
// s23 · TIENDA (GLOBOS)
// ==============================================================================

@Composable
internal fun RenderShopScreenS23(
    onSelectScreenCode: (String) -> Unit,
) {
    val typo = LocalRfTypography.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 54.dp, start = 20.dp, end = 20.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column {
                BasicText("Tienda", style = typo.secondary.copy(fontSize = 13.sp, color = Color(0xFF5F6377)))
                BasicText("Globos", style = typo.heroTitle.copy(fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(44.dp).clip(CircleShape).background(Color.White).border(1.dp, Color(0xFFECECF5), CircleShape),
                ) {
                    RfIcon(kind = RfIconKind.SEARCH, tint = Color(0xFF151729), size = 20.dp)
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(44.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF6664E9))))
                        .padding(horizontal = 12.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        RfIcon(kind = RfIconKind.ORDERS, tint = Color.White, size = 18.dp)
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(24.dp).clip(CircleShape).background(Color(0xFFFFD45C))) {
                            BasicText("3", style = typo.sectionCap.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E1A47)))
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // Banner hero de globos
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFEFE8FD)),
        ) {
            RfPhoto(photoKey = RfPhotoKey.ARCO_ROSA, contentDescription = "Arco Globos", cornerRadius = 24.dp, modifier = Modifier.fillMaxSize())
        }

        Spacer(Modifier.height(14.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                PersonalProductCard("Arco de globos", "Rosa y nude", "95", RfPhotoKey.ARCO_ROSA, Color(0xFFFDE6F2), onOpenDetail = { onSelectScreenCode("s19") }, modifier = Modifier.weight(1f))
                PersonalProductCard("Bouquet temático", "Metalizado helio", "45", RfPhotoKey.ARCO_ROSA, Color(0xFFEEF6D6), onOpenDetail = { onSelectScreenCode("s19") }, modifier = Modifier.weight(1f))
            }
        }
    }
}

// ==============================================================================
// s24 · FAVORITOS (CLIENTE LOGUEADO)
// ==============================================================================

@Composable
internal fun RenderClientFavoritesScreenS24(
    onSelectScreenCode: (String) -> Unit,
) {
    val typo = LocalRfTypography.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 54.dp, start = 20.dp, end = 20.dp),
    ) {
        BasicText("Favoritos", style = typo.heroTitle.copy(fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)))
        Spacer(Modifier.height(4.dp))
        BasicText("2 artículos guardados", style = typo.secondary.copy(fontSize = 15.sp, color = Color(0xFF5F6377)))

        Spacer(Modifier.height(16.dp))

        // Grid 2 columnas de favoritos
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            PersonalProductCard("Silla Tiffany Cristal", "Acrílico transparente", "4", RfPhotoKey.SILLA_TIFFANY, Color(0xFFDDF4F3), onOpenDetail = { onSelectScreenCode("s19") }, modifier = Modifier.weight(1f))
            PersonalProductCard("Mesa redonda", "Para 8 a 10 invitados", "25", RfPhotoKey.MESA_REDONDA, Color(0xFFFFF2C9), onOpenDetail = { onSelectScreenCode("s19") }, modifier = Modifier.weight(1f))
        }

        Spacer(Modifier.height(20.dp))

        // Tarjeta de asistencia de Rosa IA
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFFEFE8FD))
                .padding(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    RosaAvatar(size = RosaAvatarSize.MEDIUM)
                    Column(modifier = Modifier.weight(1f)) {
                        BasicText("¿Los agrego a tu boda?", style = typo.body.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)))
                        BasicText("Los dos artículos quedarían en “Boda Real Rosa & Dorado”.", style = typo.secondary.copy(fontSize = 13.sp, color = Color(0xFF5F6377)))
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF865DEC))))
                            .clickable { onSelectScreenCode("s37") },
                    ) {
                        BasicText("Agregar ambos", style = typo.body.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White))
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .height(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White)
                            .clickable { onSelectScreenCode("s37") }
                            .padding(horizontal = 16.dp),
                    ) {
                        BasicText("Elegir evento", style = typo.body.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)))
                    }
                }
            }
        }
    }
}
