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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextOverflow
import com.jeudry.rosafiesta.domain.CanonicalSampleData
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeudry.rosafiesta.designsystem.LocalRfTypography
import com.jeudry.rosafiesta.designsystem.RfGradientPriceText
import com.jeudry.rosafiesta.designsystem.RfIcon
import com.jeudry.rosafiesta.designsystem.RfIconKind
import com.jeudry.rosafiesta.designsystem.RfPhoto
import com.jeudry.rosafiesta.designsystem.RfPhotoKey
import com.jeudry.rosafiesta.designsystem.RosaAvatar
import com.jeudry.rosafiesta.designsystem.RosaAvatarSize
import com.jeudry.rosafiesta.designsystem.rfCardShadow
import com.jeudry.rosafiesta.designsystem.rfHeroBannerShadow
import com.jeudry.rosafiesta.designsystem.rfIconButtonShadow
import com.jeudry.rosafiesta.designsystem.rfInputShadow
import com.jeudry.rosafiesta.designsystem.rfPrimaryButtonShadow

// ==============================================================================
// s18 · CATÁLOGO (APP PERSONAL)
// ==============================================================================

@Composable
internal fun RenderPersonalCatalogS18(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit,
) {
    val typo = LocalRfTypography.current
    val articlesState = viewModel?.articles?.collectAsState()?.value ?: CanonicalSampleData.articles
    val draftQuantitiesState = viewModel?.draftQuantities?.collectAsState()?.value ?: emptyMap()
    val totalDraftCount = draftQuantitiesState.values.sum()
    val vmCategory = viewModel?.selectedCategory?.collectAsState()?.value ?: "Todas"

    var selectedCategory by remember(vmCategory) { mutableStateOf(if (vmCategory in listOf("Todas", "Iluminación", "Floral", "Globos", "Mobiliario", "Mesa dulce")) vmCategory else "Todas") }
    var searchQuery by remember { mutableStateOf("") }

    val filteredArticles = remember(articlesState, selectedCategory, searchQuery) {
        articlesState.filter { art ->
            val matchesCategory = selectedCategory.startsWith("Todas") ||
                art.category.equals(selectedCategory, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() ||
                art.name.contains(searchQuery, ignoreCase = true) ||
                art.description.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 54.dp, start = 20.dp, end = 20.dp),
    ) {
        // Cabecera: Título + Botón Cuadrícula + Botón Mi Evento con badge dinámico
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            BasicText(
                text = "Catálogo",
                style = typo.heroTitle.copy(fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A)),
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = false)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFECECF5), CircleShape)
                        .clickable { onSelectScreenCode("s20") },
                ) {
                    RfIcon(kind = RfIconKind.GRID, tint = Color(0xFF151729), size = 20.dp)
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clickable { onSelectScreenCode("s25") },
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .rfIconButtonShadow(CircleShape, isAdmin = false)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.dp, Color(0xFFECECF5), CircleShape),
                    ) {
                        RfIcon(kind = RfIconKind.PARTY, tint = Color(0xFF151729), size = 20.dp)
                    }
                    // Badge dinámico según artículos en borrador
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 4.dp, y = (-4).dp)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFD45C)),
                    ) {
                        BasicText(
                            text = if (totalDraftCount > 0) totalDraftCount.toString() else "16",
                            style = typo.sectionCap.copy(fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2E1A47)),
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // Campo de búsqueda interactivo con voz
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .rfInputShadow(RoundedCornerShape(16.dp), isAdmin = false)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFF0EBF4), RoundedCornerShape(16.dp))
                .padding(start = 16.dp, end = 6.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    RfIcon(kind = RfIconKind.SEARCH, tint = Color(0xFF5F6377), size = 20.dp)
                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isEmpty()) {
                            BasicText("Buscar sillas, mesas, flores…", style = typo.body.copy(fontSize = 15.sp, color = Color(0xFF5F6377)))
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                                viewModel?.setSearchQuery(it)
                            },
                            textStyle = typo.body.copy(fontSize = 15.sp, color = Color(0xFF151729)),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )
                    }
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

        // Chips de categoría interactivos
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
        ) {
            val categories = listOf("Todas · ${articlesState.size}", "Iluminación", "Floral", "Globos", "Mobiliario", "Mesa dulce")
            categories.forEach { cat ->
                val catClean = if (cat.startsWith("Todas")) "Todas" else cat
                val isSelected = selectedCategory == catClean || (selectedCategory.startsWith("Todas") && catClean == "Todas")
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(38.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(
                            if (isSelected) {
                                Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9)))
                            } else {
                                SolidColor(Color(0xFFF1EFF5))
                            },
                        )
                        .border(1.dp, if (isSelected) Color.Transparent else Color(0xFFE9E9F2), RoundedCornerShape(999.dp))
                        .clickable {
                            viewModel?.soundPlayer?.playTap()
                            selectedCategory = catClean
                            viewModel?.setSelectedCategory(catClean)
                        }
                        .padding(horizontal = 14.dp),
                ) {
                    BasicText(
                        text = cat,
                        style = typo.body.copy(
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) Color.White else Color(0xFF5F6377),
                        ),
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // Banner asistente Rosa IA
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .rfCardShadow(RoundedCornerShape(18.dp), isAdmin = false)
                .clip(RoundedCornerShape(18.dp))
                .background(Brush.horizontalGradient(listOf(Color(0xFFFCE6F3), Color(0xFFECE4FD))))
                .clickable { onSelectScreenCode("s26") }
                .padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                RosaAvatar(size = RosaAvatarSize.MEDIUM)
                Column(modifier = Modifier.weight(1f)) {
                    BasicText("¿No sabes por dónde empezar?", style = typo.body.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A)))
                    BasicText("Rosa arma tu lista en 2 minutos.", style = typo.secondary.copy(fontSize = 13.sp, color = Color(0xFF5F6377)))
                }
                RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF865DEC), size = 18.dp, modifier = Modifier.rotate(180f))
            }
        }

        Spacer(Modifier.height(16.dp))

        // Cuadrícula dinámica 2 columnas de productos
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.verticalScroll(rememberScrollState()),
        ) {
            filteredArticles.chunked(2).forEach { rowPair ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    val first = rowPair[0]
                    val firstPhoto = RfPhotoKey.entries.firstOrNull { it.assetKey == first.photo.key || it.name.equals(first.photo.key, ignoreCase = true) } ?: RfPhotoKey.SILLA_TIFFANY
                    PersonalProductCard(
                        title = first.name,
                        desc = first.description,
                        price = first.pricePerDayDop.toString(),
                        photoKey = firstPhoto,
                        bg = Color(0xFFEFE8FD),
                        useGradientPrice = true,
                        onOpenDetail = {
                            viewModel?.soundPlayer?.playTap()
                            viewModel?.selectArticle(first.id)
                            onSelectScreenCode("s19")
                        },
                        modifier = Modifier.weight(1f),
                    )
                    if (rowPair.size > 1) {
                        val second = rowPair[1]
                        val secondPhoto = RfPhotoKey.entries.firstOrNull { it.assetKey == second.photo.key || it.name.equals(second.photo.key, ignoreCase = true) } ?: RfPhotoKey.SILLA_TIFFANY
                        PersonalProductCard(
                            title = second.name,
                            desc = second.description,
                            price = second.pricePerDayDop.toString(),
                            photoKey = secondPhoto,
                            bg = Color(0xFFEEF6D6),
                            useGradientPrice = true,
                            onOpenDetail = {
                                viewModel?.soundPlayer?.playTap()
                                viewModel?.selectArticle(second.id)
                                onSelectScreenCode("s19")
                            },
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                }
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
    initialFavorite: Boolean = false,
    useGradientPrice: Boolean = false,
    onOpenDetail: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val typo = LocalRfTypography.current
    var isFavorite by remember { mutableStateOf(initialFavorite) }

    Box(
        modifier = modifier
            .rfCardShadow(RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFF2EDF5), RoundedCornerShape(20.dp))
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
                        .background(Color.White)
                        .clickable { isFavorite = !isFavorite },
                ) {
                    RfIcon(
                        kind = RfIconKind.HEART,
                        tint = Color(0xFFD63F98),
                        filled = isFavorite,
                        size = 18.dp,
                    )
                }
            }

            Column(modifier = Modifier.padding(start = 14.dp, end = 12.dp, top = 12.dp, bottom = 12.dp)) {
                BasicText(title, maxLines = 1, style = typo.body.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E)))
                Spacer(Modifier.height(3.dp))
                BasicText(desc, maxLines = 1, style = typo.secondary.copy(fontSize = 13.sp, color = Color(0xFF62627A)))
                Spacer(Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        if (useGradientPrice) {
                            RfGradientPriceText(price, style = TextStyle(fontFamily = typo.displayFamily, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold))
                            Spacer(Modifier.width(3.dp))
                            BasicText("RD$/día", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB54BD6)))
                        } else {
                            BasicText(
                                "RD$ $price",
                                style = typo.heroTitle.copy(
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1A1A2E),
                                ),
                            )
                            BasicText(" /día", style = typo.secondary.copy(fontSize = 12.sp, color = Color(0xFF62627A)))
                        }
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .shadow(elevation = 8.dp, shape = CircleShape, spotColor = Color(0x99865DEC), ambientColor = Color.Transparent)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9))),
                            ),
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
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit,
) {
    val typo = LocalRfTypography.current
    val selectedArticleId = viewModel?.selectedArticleId?.collectAsState()?.value ?: "art-silla-tiffany"
    val articlesState = viewModel?.articles?.collectAsState()?.value ?: CanonicalSampleData.articles
    val currentArticle = articlesState.firstOrNull { it.id == selectedArticleId } ?: articlesState.first()
    val draftQuantities = viewModel?.draftQuantities?.collectAsState()?.value ?: emptyMap()
    val initialCount = (draftQuantities[currentArticle.id] ?: 80).coerceAtLeast(1)

    var count by remember(currentArticle.id) { mutableStateOf(initialCount) }
    var selectedColor by remember { mutableStateOf("Transparente") }
    var selectedTab by remember { mutableStateOf("Descripción") }

    val articlePhoto = RfPhotoKey.entries.firstOrNull { it.assetKey == currentArticle.photo.key || it.name.equals(currentArticle.photo.key, ignoreCase = true) } ?: RfPhotoKey.SILLA_TIFFANY

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFC)),
    ) {
        // Foto de portada (alto 430dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(430.dp),
        ) {
            RfPhoto(
                photoKey = articlePhoto,
                contentDescription = currentArticle.name,
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
                        .rfIconButtonShadow(CircleShape, isAdmin = false)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("BACK:s18") },
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF151729), size = 20.dp)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(44.dp)
                            .rfIconButtonShadow(CircleShape, isAdmin = false)
                            .clip(CircleShape)
                            .background(Color.White),
                    ) {
                        RfIcon(kind = RfIconKind.SHARE, tint = Color(0xFF151729), size = 20.dp)
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(44.dp)
                            .rfIconButtonShadow(CircleShape, isAdmin = false)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable {
                                viewModel?.soundPlayer?.playTap()
                                viewModel?.toggleFavorite(currentArticle.id)
                            },
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
                    RfPhoto(photoKey = articlePhoto, contentDescription = "View 1", cornerRadius = 12.dp, modifier = Modifier.fillMaxSize())
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

        // Hoja blanca con detalles (top = 400dp)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 400.dp)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Color.White),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 110.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Título y Precio
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        BasicText(currentArticle.name, style = typo.heroTitle.copy(fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)))
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
                        RfGradientPriceText(
                            text = "RD$ ${currentArticle.pricePerDayDop}",
                            style = typo.heroTitle.copy(fontSize = 28.sp, fontWeight = FontWeight.ExtraBold),
                        )
                        BasicText("por unidad / día", style = typo.secondary.copy(fontSize = 12.sp, color = Color(0xFF62627A)))
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
                            val gradDiag = Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9)))
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (isSel && name == "Transparente") gradDiag else if (isSel) SolidColor(col) else SolidColor(Color.White))
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
                                            Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9)))
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
                                        fontWeight = if (isSel) FontWeight.Medium else FontWeight.Normal,
                                        color = if (isSel) Color.White else Color(0xFF5F6377),
                                    ),
                                )
                            }
                        }
                    }
                }

                BasicText(
                    text = currentArticle.description,
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
                                .clickable { if (count > 1) count = (count - 10).coerceAtLeast(1) },
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

                // Botón primario dinámico
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = false)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9))))
                        .clickable {
                            val delta = count - (draftQuantities[currentArticle.id] ?: 0)
                            viewModel?.adjustDraftQuantity(currentArticle.id, delta)
                            viewModel?.soundPlayer?.playCartAdd()
                            onSelectScreenCode("s25")
                        },
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        RfIcon(kind = RfIconKind.SPARKLE, tint = Color.White, size = 20.dp)
                        BasicText(
                            text = "Agregar · RD$ ${count * currentArticle.pricePerDayDop}",
                            style = typo.body.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White),
                        )
                    }
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
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit,
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
                .verticalScroll(rememberScrollState())
                .padding(top = 56.dp, start = 20.dp, end = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            // Header: back + title + subtitle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = false)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("BACK:s18") },
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1A1A2E), size = 22.dp)
                }
                Column {
                    BasicText(
                        "Categorías",
                        style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, letterSpacing = (-0.48).sp, color = Color(0xFF1A1A2E)),
                    )
                    BasicText(
                        "Explora el catálogo por tipo de artículo",
                        style = typo.secondary.copy(fontSize = 14.sp, color = Color(0xFF62627A)),
                    )
                }
            }

            // Grid 2x4 — conectado reactivamente al ViewModel
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    CategoryGridCard("Iluminación", Color(0xFFDDF4F3), CategoryIllustration.ILUMINACION, Modifier.weight(1f)) {
                        viewModel?.setSelectedCategory("Iluminación")
                        viewModel?.soundPlayer?.playTap()
                        onSelectScreenCode("s18")
                    }
                    CategoryGridCard("Floral", Color(0xFFFDE6F2), CategoryIllustration.FLORAL, Modifier.weight(1f)) {
                        viewModel?.setSelectedCategory("Floral")
                        viewModel?.soundPlayer?.playTap()
                        onSelectScreenCode("s18")
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    CategoryGridCard("Globos", Color(0xFFFFF2C9), CategoryIllustration.GLOBOS, Modifier.weight(1f)) {
                        viewModel?.setSelectedCategory("Globos")
                        viewModel?.soundPlayer?.playTap()
                        onSelectScreenCode("s18")
                    }
                    CategoryGridCard("Mantelería", Color(0xFFEFE8FD), CategoryIllustration.MANTELERIA, Modifier.weight(1f)) {
                        viewModel?.setSelectedCategory("Mantelería")
                        viewModel?.soundPlayer?.playTap()
                        onSelectScreenCode("s18")
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    CategoryGridCard("Mesa dulce", Color(0xFFEEF6D6), CategoryIllustration.MESA_DULCE, Modifier.weight(1f)) {
                        viewModel?.setSelectedCategory("Mesa dulce")
                        viewModel?.soundPlayer?.playTap()
                        onSelectScreenCode("s18")
                    }
                    CategoryGridCard("Letreros", Color(0xFF1E1A2E), CategoryIllustration.LETREROS, Modifier.weight(1f), titleColor = Color(0xFFFFFFFF)) {
                        viewModel?.setSelectedCategory("Letreros")
                        viewModel?.soundPlayer?.playTap()
                        onSelectScreenCode("s18")
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    CategoryGridCard("Mobiliario", Color(0xFFFFF2C9), CategoryIllustration.MOBILIARIO, Modifier.weight(1f)) {
                        viewModel?.setSelectedCategory("Mobiliario")
                        viewModel?.soundPlayer?.playTap()
                        onSelectScreenCode("s18")
                    }
                    CategoryGridCard("Decoración", Color(0xFFFDE6F2), CategoryIllustration.DECORACION, Modifier.weight(1f)) {
                        viewModel?.setSelectedCategory("Decoración")
                        viewModel?.soundPlayer?.playTap()
                        onSelectScreenCode("s18")
                    }
                }
            }

            Spacer(Modifier.height(96.dp))
        }
    }
}

private enum class CategoryIllustration {
    ILUMINACION, FLORAL, GLOBOS, MANTELERIA, MESA_DULCE, LETREROS, MOBILIARIO, DECORACION
}

@Composable
private fun CategoryGridCard(
    title: String,
    bg: Color,
    illustration: CategoryIllustration,
    modifier: Modifier = Modifier,
    titleColor: Color = Color(0xFF1A1A2E),
    onClick: () -> Unit,
) {
    val typo = LocalRfTypography.current
    Box(
        modifier = modifier
            .height(142.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(start = 14.dp, bottom = 14.dp),
    ) {
        // SVG-style illustration via Canvas, positioned top-end 104dp
        Canvas(
            modifier = Modifier
                .size(104.dp)
                .align(Alignment.TopEnd)
                .offset(x = 4.dp, y = (-2).dp),
        ) {
            val w = size.width
            val h = size.height
            when (illustration) {
                CategoryIllustration.ILUMINACION -> {
                    // String of colorful lights
                    val wireColor = androidx.compose.ui.graphics.Color(0xFF2B1730).copy(alpha = 0.45f)
                    drawPath(
                        path = androidx.compose.ui.graphics.Path().apply {
                            moveTo(w * 0.02f, h * 0.30f)
                            cubicTo(w * 0.20f, h * 0.52f, w * 0.40f, h * 0.52f, w * 0.60f, h * 0.36f)
                            cubicTo(w * 0.78f, h * 0.20f, w * 0.98f, h * 0.24f, w * 1.0f, h * 0.40f)
                        },
                        color = wireColor, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f * density)
                    )
                    drawPath(
                        path = androidx.compose.ui.graphics.Path().apply {
                            moveTo(w * 0.02f, h * 0.72f)
                            cubicTo(w * 0.22f, h * 0.94f, w * 0.44f, h * 0.94f, w * 0.64f, h * 0.78f)
                            cubicTo(w * 0.80f, h * 0.66f, w * 1.0f, h * 0.66f, w * 1.0f, h * 0.82f)
                        },
                        color = wireColor, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f * density)
                    )
                    // Bulbs
                    val bulbColors = listOf(
                        androidx.compose.ui.graphics.Color(0xFFF5B53B),
                        androidx.compose.ui.graphics.Color(0xFFEC4F97),
                        androidx.compose.ui.graphics.Color(0xFF2FB58F),
                        androidx.compose.ui.graphics.Color(0xFF7650E8),
                        androidx.compose.ui.graphics.Color(0xFF4AA8E0),
                        androidx.compose.ui.graphics.Color(0xFFF5B53B),
                    )
                    val bulbXs = listOf(0.14f, 0.32f, 0.50f, 0.70f, 0.90f, 1.08f)
                    val bulbYs = listOf(0.53f, 0.58f, 0.52f, 0.43f, 0.42f, 0.49f)
                    bulbXs.forEachIndexed { i, bx ->
                        val by = bulbYs[i]
                        val c = bulbColors[i % bulbColors.size]
                        drawOval(color = c.copy(alpha = 0.18f), topLeft = androidx.compose.ui.geometry.Offset((bx - 0.11f) * w, (by - 0.11f) * h), size = androidx.compose.ui.geometry.Size(w * 0.22f, h * 0.22f))
                        drawOval(color = c, topLeft = androidx.compose.ui.geometry.Offset((bx - 0.05f) * w, (by - 0.07f) * h), size = androidx.compose.ui.geometry.Size(w * 0.10f, h * 0.14f))
                    }
                }
                CategoryIllustration.FLORAL -> {
                    // Simple flower vase
                    // Vase
                    drawRoundRect(color = androidx.compose.ui.graphics.Color(0xFFB9A6F7), topLeft = androidx.compose.ui.geometry.Offset(w * 0.35f, h * 0.58f), size = androidx.compose.ui.geometry.Size(w * 0.30f, h * 0.32f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f * density))
                    // Stem
                    drawLine(color = androidx.compose.ui.graphics.Color(0xFF4AA8E0).copy(alpha = 0.6f), start = androidx.compose.ui.geometry.Offset(w * 0.50f, h * 0.60f), end = androidx.compose.ui.geometry.Offset(w * 0.50f, h * 0.28f), strokeWidth = 3f * density)
                    // Pink flower
                    for (angle in listOf(0.0, 60.0, 120.0, 180.0, 240.0, 300.0)) {
                        val rad = angle * kotlin.math.PI / 180.0
                        val px = (w * 0.50f + w * 0.12f * kotlin.math.cos(rad)).toFloat()
                        val py = (h * 0.22f + h * 0.12f * kotlin.math.sin(rad)).toFloat()
                        drawCircle(color = androidx.compose.ui.graphics.Color(0xFFF9B6D3), radius = w * 0.10f, center = androidx.compose.ui.geometry.Offset(px, py))
                    }
                    drawCircle(color = androidx.compose.ui.graphics.Color(0xFFEC4F97), radius = w * 0.09f, center = androidx.compose.ui.geometry.Offset(w * 0.50f, h * 0.22f))
                    // Yellow flower
                    for (angle in listOf(0.0, 72.0, 144.0, 216.0, 288.0)) {
                        val rad = angle * kotlin.math.PI / 180.0
                        val px = (w * 0.72f + w * 0.09f * kotlin.math.cos(rad)).toFloat()
                        val py = (h * 0.32f + h * 0.09f * kotlin.math.sin(rad)).toFloat()
                        drawCircle(color = androidx.compose.ui.graphics.Color(0xFFF5B53B), radius = w * 0.08f, center = androidx.compose.ui.geometry.Offset(px, py))
                    }
                    drawCircle(color = androidx.compose.ui.graphics.Color(0xFFD9A441), radius = w * 0.06f, center = androidx.compose.ui.geometry.Offset(w * 0.72f, h * 0.32f))
                }
                CategoryIllustration.GLOBOS -> {
                    // 3 balloons
                    val balloonData = listOf(
                        Triple(w * 0.30f, h * 0.35f, androidx.compose.ui.graphics.Color(0xFFEC4F97)),
                        Triple(w * 0.60f, h * 0.28f, androidx.compose.ui.graphics.Color(0xFF7650E8)),
                        Triple(w * 0.50f, h * 0.52f, androidx.compose.ui.graphics.Color(0xFFF5B53B)),
                    )
                    balloonData.forEach { (bx, by, bc) ->
                        drawCircle(color = bc.copy(alpha = 0.18f), radius = w * 0.19f, center = androidx.compose.ui.geometry.Offset(bx, by))
                        drawCircle(color = bc, radius = w * 0.15f, center = androidx.compose.ui.geometry.Offset(bx, by))
                        drawLine(color = androidx.compose.ui.graphics.Color(0xFF2B1730).copy(alpha = 0.4f), start = androidx.compose.ui.geometry.Offset(bx, by + w * 0.15f), end = androidx.compose.ui.geometry.Offset(w * 0.50f, h * 0.95f), strokeWidth = 1.4f * density)
                    }
                }
                CategoryIllustration.MANTELERIA -> {
                    // Stacked tablecloth layers
                    drawRoundRect(color = androidx.compose.ui.graphics.Color(0xFFB9A6F7), topLeft = androidx.compose.ui.geometry.Offset(w * 0.22f, h * 0.68f), size = androidx.compose.ui.geometry.Size(w * 0.56f, h * 0.16f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f * density))
                    drawRoundRect(color = androidx.compose.ui.graphics.Color(0xFFF9B6D3), topLeft = androidx.compose.ui.geometry.Offset(w * 0.25f, h * 0.52f), size = androidx.compose.ui.geometry.Size(w * 0.50f, h * 0.16f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f * density))
                    drawRoundRect(color = androidx.compose.ui.graphics.Color(0xFF9BE3C9), topLeft = androidx.compose.ui.geometry.Offset(w * 0.20f, h * 0.37f), size = androidx.compose.ui.geometry.Size(w * 0.54f, h * 0.16f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f * density))
                    // Dashes on top layer
                    val dashColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.6f)
                    drawLine(dashColor, start = androidx.compose.ui.geometry.Offset(w * 0.28f, h * 0.45f), end = androidx.compose.ui.geometry.Offset(w * 0.72f, h * 0.45f), strokeWidth = 1.6f * density)
                    drawLine(dashColor, start = androidx.compose.ui.geometry.Offset(w * 0.28f, h * 0.60f), end = androidx.compose.ui.geometry.Offset(w * 0.72f, h * 0.60f), strokeWidth = 1.6f * density)
                    drawLine(dashColor, start = androidx.compose.ui.geometry.Offset(w * 0.30f, h * 0.76f), end = androidx.compose.ui.geometry.Offset(w * 0.70f, h * 0.76f), strokeWidth = 1.6f * density)
                    // Bow
                    drawRoundRect(color = androidx.compose.ui.graphics.Color(0xFFD9A441), topLeft = androidx.compose.ui.geometry.Offset(w * 0.44f, h * 0.30f), size = androidx.compose.ui.geometry.Size(w * 0.12f, h * 0.04f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f * density))
                }
                CategoryIllustration.MESA_DULCE -> {
                    // Sweet table with cake
                    // Table legs
                    drawLine(color = androidx.compose.ui.graphics.Color(0xFFEC4F97).copy(alpha = 0.3f), start = androidx.compose.ui.geometry.Offset(w * 0.30f, h * 0.72f), end = androidx.compose.ui.geometry.Offset(w * 0.30f, h * 0.90f), strokeWidth = 2f * density)
                    drawLine(color = androidx.compose.ui.graphics.Color(0xFFEC4F97).copy(alpha = 0.3f), start = androidx.compose.ui.geometry.Offset(w * 0.44f, h * 0.72f), end = androidx.compose.ui.geometry.Offset(w * 0.44f, h * 0.90f), strokeWidth = 2f * density)
                    drawLine(color = androidx.compose.ui.graphics.Color(0xFFEC4F97).copy(alpha = 0.3f), start = androidx.compose.ui.geometry.Offset(w * 0.58f, h * 0.72f), end = androidx.compose.ui.geometry.Offset(w * 0.58f, h * 0.90f), strokeWidth = 2f * density)
                    drawLine(color = androidx.compose.ui.graphics.Color(0xFFEC4F97).copy(alpha = 0.3f), start = androidx.compose.ui.geometry.Offset(w * 0.72f, h * 0.72f), end = androidx.compose.ui.geometry.Offset(w * 0.72f, h * 0.90f), strokeWidth = 2f * density)
                    // Table top
                    drawRoundRect(color = androidx.compose.ui.graphics.Color(0xFFF9B6D3), topLeft = androidx.compose.ui.geometry.Offset(w * 0.22f, h * 0.65f), size = androidx.compose.ui.geometry.Size(w * 0.56f, h * 0.08f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f * density))
                    // Cake layers
                    drawRoundRect(color = androidx.compose.ui.graphics.Color(0xFFF9B6D3), topLeft = androidx.compose.ui.geometry.Offset(w * 0.28f, h * 0.53f), size = androidx.compose.ui.geometry.Size(w * 0.44f, h * 0.13f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f * density))
                    drawRoundRect(color = androidx.compose.ui.graphics.Color(0xFFB9A6F7), topLeft = androidx.compose.ui.geometry.Offset(w * 0.33f, h * 0.40f), size = androidx.compose.ui.geometry.Size(w * 0.34f, h * 0.13f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f * density))
                    drawRoundRect(color = androidx.compose.ui.graphics.Color(0xFF9BE3C9), topLeft = androidx.compose.ui.geometry.Offset(w * 0.38f, h * 0.27f), size = androidx.compose.ui.geometry.Size(w * 0.24f, h * 0.13f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f * density))
                    // Candle
                    drawLine(color = androidx.compose.ui.graphics.Color(0xFF4AA8E0), start = androidx.compose.ui.geometry.Offset(w * 0.50f, h * 0.18f), end = androidx.compose.ui.geometry.Offset(w * 0.50f, h * 0.27f), strokeWidth = 3f * density)
                    drawCircle(color = androidx.compose.ui.graphics.Color(0xFFF5B53B), radius = 5f * density, center = androidx.compose.ui.geometry.Offset(w * 0.50f, h * 0.16f))
                }
                CategoryIllustration.LETREROS -> {
                    // Neon heart sign (pink on dark bg)
                    val neonColor = androidx.compose.ui.graphics.Color(0xFFFF6FB0)
                    val neonGlow = androidx.compose.ui.graphics.Color(0xFFEC4F97).copy(alpha = 0.22f)
                    // Heart path: scale to 104x104
                    fun heartPath(scale: Float): androidx.compose.ui.graphics.Path {
                        val p = androidx.compose.ui.graphics.Path()
                        val cx = w * 0.50f; val cy = h * 0.42f
                        p.moveTo(cx, cy + scale * 0.46f)
                        p.cubicTo(cx - scale * 0.60f, cy + scale * 0.26f, cx - scale * 0.80f, cy + scale * 0.05f, cx - scale * 0.50f, cy - scale * 0.18f)
                        p.cubicTo(cx - scale * 0.20f, cy - scale * 0.40f, cx, cy - scale * 0.25f, cx, cy - scale * 0.10f)
                        p.cubicTo(cx, cy - scale * 0.25f, cx + scale * 0.20f, cy - scale * 0.40f, cx + scale * 0.50f, cy - scale * 0.18f)
                        p.cubicTo(cx + scale * 0.80f, cy + scale * 0.05f, cx + scale * 0.60f, cy + scale * 0.26f, cx, cy + scale * 0.46f)
                        p.close()
                        return p
                    }
                    drawPath(heartPath(w * 0.45f), color = neonGlow, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 14f * density))
                    drawPath(heartPath(w * 0.45f), color = neonColor, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.5f * density))
                    // Wavy line at bottom (yellow neon)
                    drawPath(
                        path = androidx.compose.ui.graphics.Path().apply {
                            moveTo(w * 0.28f, h * 0.82f)
                            cubicTo(w * 0.33f, h * 0.76f, w * 0.38f, h * 0.76f, w * 0.44f, h * 0.82f)
                            cubicTo(w * 0.50f, h * 0.88f, w * 0.55f, h * 0.88f, w * 0.60f, h * 0.82f)
                            cubicTo(w * 0.66f, h * 0.76f, w * 0.71f, h * 0.76f, w * 0.74f, h * 0.82f)
                        },
                        color = androidx.compose.ui.graphics.Color(0xFFF5B53B).copy(alpha = 0.2f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 10f * density)
                    )
                    drawPath(
                        path = androidx.compose.ui.graphics.Path().apply {
                            moveTo(w * 0.28f, h * 0.82f)
                            cubicTo(w * 0.33f, h * 0.76f, w * 0.38f, h * 0.76f, w * 0.44f, h * 0.82f)
                            cubicTo(w * 0.50f, h * 0.88f, w * 0.55f, h * 0.88f, w * 0.60f, h * 0.82f)
                            cubicTo(w * 0.66f, h * 0.76f, w * 0.71f, h * 0.76f, w * 0.74f, h * 0.82f)
                        },
                        color = androidx.compose.ui.graphics.Color(0xFFFFC75A),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.2f * density)
                    )
                    // String
                    drawLine(color = androidx.compose.ui.graphics.Color(0xFF9B8AA0), start = androidx.compose.ui.geometry.Offset(w * 0.50f, h * 0.05f), end = androidx.compose.ui.geometry.Offset(w * 0.50f, h * 0.17f), strokeWidth = 1.4f * density)
                }
                CategoryIllustration.MOBILIARIO -> {
                    // Chair — Tiffany style
                    val chairColor = androidx.compose.ui.graphics.Color(0xFFD9A441)
                    // Legs
                    drawLine(chairColor, start = androidx.compose.ui.geometry.Offset(w * 0.32f, h * 0.72f), end = androidx.compose.ui.geometry.Offset(w * 0.30f, h * 0.92f), strokeWidth = 3f * density)
                    drawLine(chairColor, start = androidx.compose.ui.geometry.Offset(w * 0.68f, h * 0.72f), end = androidx.compose.ui.geometry.Offset(w * 0.70f, h * 0.92f), strokeWidth = 3f * density)
                    drawLine(chairColor, start = androidx.compose.ui.geometry.Offset(w * 0.32f, h * 0.92f), end = androidx.compose.ui.geometry.Offset(w * 0.68f, h * 0.92f), strokeWidth = 2.5f * density)
                    // Seat
                    drawRoundRect(color = chairColor, topLeft = androidx.compose.ui.geometry.Offset(w * 0.25f, h * 0.65f), size = androidx.compose.ui.geometry.Size(w * 0.50f, h * 0.08f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f * density))
                    // Cushion
                    drawRoundRect(color = androidx.compose.ui.graphics.Color(0xFFF9B6D3), topLeft = androidx.compose.ui.geometry.Offset(w * 0.27f, h * 0.62f), size = androidx.compose.ui.geometry.Size(w * 0.46f, h * 0.06f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f * density))
                    // Back slats
                    drawLine(chairColor, start = androidx.compose.ui.geometry.Offset(w * 0.40f, h * 0.62f), end = androidx.compose.ui.geometry.Offset(w * 0.40f, h * 0.20f), strokeWidth = 3f * density)
                    drawLine(chairColor, start = androidx.compose.ui.geometry.Offset(w * 0.60f, h * 0.62f), end = androidx.compose.ui.geometry.Offset(w * 0.60f, h * 0.20f), strokeWidth = 3f * density)
                    // Top rail
                    drawRoundRect(color = chairColor, topLeft = androidx.compose.ui.geometry.Offset(w * 0.30f, h * 0.17f), size = androidx.compose.ui.geometry.Size(w * 0.40f, h * 0.06f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f * density))
                    // Middle rail
                    drawRoundRect(color = chairColor, topLeft = androidx.compose.ui.geometry.Offset(w * 0.33f, h * 0.38f), size = androidx.compose.ui.geometry.Size(w * 0.34f, h * 0.04f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f * density))
                }
                CategoryIllustration.DECORACION -> {
                    // Vase with plants and candle
                    // Vase
                    drawRoundRect(color = androidx.compose.ui.graphics.Color(0xFFF5F0EA), topLeft = androidx.compose.ui.geometry.Offset(w * 0.35f, h * 0.55f), size = androidx.compose.ui.geometry.Size(w * 0.30f, h * 0.38f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f * density))
                    // Stems
                    drawLine(color = androidx.compose.ui.graphics.Color(0xFFD9A441), start = androidx.compose.ui.geometry.Offset(w * 0.45f, h * 0.55f), end = androidx.compose.ui.geometry.Offset(w * 0.38f, h * 0.15f), strokeWidth = 2.5f * density)
                    drawLine(color = androidx.compose.ui.graphics.Color(0xFFD9A441), start = androidx.compose.ui.geometry.Offset(w * 0.50f, h * 0.55f), end = androidx.compose.ui.geometry.Offset(w * 0.52f, h * 0.08f), strokeWidth = 2.5f * density)
                    drawLine(color = androidx.compose.ui.graphics.Color(0xFFD9A441), start = androidx.compose.ui.geometry.Offset(w * 0.55f, h * 0.55f), end = androidx.compose.ui.geometry.Offset(w * 0.64f, h * 0.20f), strokeWidth = 2.5f * density)
                    // Leaves
                    drawOval(color = androidx.compose.ui.graphics.Color(0xFFD9A441), topLeft = androidx.compose.ui.geometry.Offset(w * 0.28f, h * 0.14f), size = androidx.compose.ui.geometry.Size(w * 0.22f, h * 0.14f))
                    drawOval(color = androidx.compose.ui.graphics.Color(0xFFD9A441), topLeft = androidx.compose.ui.geometry.Offset(w * 0.44f, h * 0.05f), size = androidx.compose.ui.geometry.Size(w * 0.16f, h * 0.10f))
                    drawOval(color = androidx.compose.ui.graphics.Color(0xFFD9A441), topLeft = androidx.compose.ui.geometry.Offset(w * 0.56f, h * 0.15f), size = androidx.compose.ui.geometry.Size(w * 0.20f, h * 0.12f))
                    // Candle
                    drawRoundRect(color = androidx.compose.ui.graphics.Color(0xFFEC4F97), topLeft = androidx.compose.ui.geometry.Offset(w * 0.68f, h * 0.55f), size = androidx.compose.ui.geometry.Size(w * 0.18f, h * 0.35f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f * density))
                    drawCircle(color = androidx.compose.ui.graphics.Color(0xFFF5B53B), radius = 5f * density, center = androidx.compose.ui.geometry.Offset(w * 0.77f, h * 0.52f))
                }
            }
        }

        BasicText(
            text = title,
            style = TextStyle(
                fontFamily = typo.displayFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 17.sp,
                letterSpacing = (-0.34).sp,
                color = titleColor,
            ),
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
            .verticalScroll(rememberScrollState())
            .padding(top = 58.dp, start = 20.dp, end = 20.dp, bottom = 116.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            BasicText("Paquetes", style = typo.heroTitle.copy(fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1A2E)))
            BasicText("Colecciones temáticas, listas para tu evento", style = typo.secondary.copy(fontSize = 14.sp, color = Color(0xFF62627A)))
        }

        Spacer(Modifier.height(18.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
            PackageSummaryCard("Bodas Rosa", "Paquete romántico completo para bodas, con arco floral y centros de mesa.", "RD$ 850", "Ahorra 15%", RfPhotoKey.BODA, Color(0xFFFDE6F2)) { onSelectScreenCode("s22") }
            PackageSummaryCard("Quinceañera Premium", "Paquete de lujo para quinceañeras con arco floral.", "RD$ 1,200", "Ahorra 12%", RfPhotoKey.QUINCEANERA, Color(0xFFEFE8FD)) { onSelectScreenCode("s22") }
            PackageSummaryCard("Cumpleaños Infantil", "Kit completo para fiestas infantiles: globos, banner y más.", "RD$ 450", "Ahorra 10%", RfPhotoKey.CUMPLE_INFANTIL, Color(0xFFEEF6D6)) { onSelectScreenCode("s22") }
            PackageSummaryCard("Evento Corporativo", "Solución profesional para eventos empresariales.", "RD$ 600", "Ahorra 8%", RfPhotoKey.CORPORATIVO, Color(0xFFFFF2C9)) { onSelectScreenCode("s22") }
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
            .rfCardShadow(RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFF2EDF5), RoundedCornerShape(20.dp))
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

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFFEEF6D6))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                ) {
                    BasicText(discountBadge, style = typo.sectionCap.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4E6A10)))
                }
                BasicText(title, style = typo.heroTitle.copy(fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1A2E)))
                BasicText(desc, maxLines = 2, style = typo.secondary.copy(fontSize = 13.sp, lineHeight = 18.2.sp, color = Color(0xFF62627A)))
                Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(top = 2.dp)) {
                    BasicText("desde ", style = typo.secondary.copy(fontSize = 13.sp, color = Color(0xFF62627A)))
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
            // Foto de portada (300dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(Color(0xFFFDE6F2)),
            ) {
                RfPhoto(
                    photoKey = RfPhotoKey.BODA,
                    contentDescription = "Bodas Rosa",
                    cornerRadius = 0.dp,
                    alignment = androidx.compose.ui.BiasAlignment(0f, -0.4f),
                    modifier = Modifier.fillMaxSize(),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0x4D1A1A2E), Color.Transparent),
                            ),
                        ),
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .offset(x = 20.dp, y = 54.dp)
                        .size(44.dp)
                        .rfIconButtonShadow(RoundedCornerShape(14.dp), isAdmin = false)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .clickable { onSelectScreenCode("BACK:s21") },
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

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    BasicText("Incluye", style = typo.heroTitle.copy(fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)))
                    BasicText("Desmarca lo que no quieras", style = typo.secondary.copy(fontSize = 13.sp, color = Color(0xFF62627A)))
                }
                Spacer(Modifier.height(10.dp))

                PackageItemRow("Arco de globos rosa", "RD$ 95 / día", "1", RfPhotoKey.ARCO_ROSA, Color(0xFFFDE6F2))
                Spacer(Modifier.height(8.dp))
                PackageItemRow("Centro de mesa floral", "RD$ 35 / día", "10", RfPhotoKey.CENTRO_FLORAL, Color(0xFFFFF2C9))
                Spacer(Modifier.height(8.dp))
                PackageItemRow("Silla Tiffany Cristal", "RD$ 4 / día", "120", RfPhotoKey.SILLA_TIFFANY, Color(0xFFEFE8FD))
                Spacer(Modifier.height(8.dp))
                PackageItemRow("Mesa redonda", "RD$ 25 / día", "3", RfPhotoKey.MESA_REDONDA, Color(0xFFEEF6D6))

                Spacer(Modifier.height(90.dp))
            }
        }

        // Botón inferior con precio + CTA
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color.White)
                .border(1.dp, Color(0xFFF2EDF5)),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 34.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.wrapContentWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BasicText(
                            text = "134 artículos · ",
                            style = typo.secondary.copy(fontSize = 12.sp, color = Color(0xFF62627A)),
                        )
                        BasicText(
                            text = "RD$ 1,000",
                            style = typo.secondary.copy(
                                fontSize = 12.sp,
                                color = Color(0xFF62627A),
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough,
                            ),
                        )
                    }
                    BasicText(
                        text = "RD$ 850",
                        style = TextStyle(
                            fontFamily = typo.displayFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            color = Color(0xFF1A1A2E),
                        ),
                    )
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = false)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.horizontalGradient(listOf(Color(0xFFEA4EA9), Color(0xFFB754CB), Color(0xFF865DEC))))
                        .clickable { onSelectScreenCode("s37") },
                ) {
                    BasicText("Agregar al evento", style = typo.body.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White))
                }
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
            .rfCardShadow(RoundedCornerShape(18.dp), isAdmin = false)
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

            // Stepper
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFE8E1EE), RoundedCornerShape(12.dp))
                    .height(36.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.size(32.dp, 36.dp), contentAlignment = Alignment.Center) {
                    RfIcon(kind = RfIconKind.MINUS, tint = Color(0xFF1A1A2E), size = 16.dp)
                }
                BasicText(qty, style = typo.body.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E)))
                Box(modifier = Modifier.size(32.dp, 36.dp), contentAlignment = Alignment.Center) {
                    RfIcon(kind = RfIconKind.PLUS, tint = Color(0xFF1A1A2E), size = 16.dp)
                }
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
    val gradDiag = Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9)))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFC)),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                Brush.radialGradient(
                    colors = listOf(Color(0xFFF9E3F1), Color(0x00F9E3F1)),
                    center = Offset(0f, 0f),
                    radius = size.width * 0.9f,
                ),
            )
            drawRect(
                Brush.radialGradient(
                    colors = listOf(Color(0xFFEEE7FB), Color(0x00EEE7FB)),
                    center = Offset(size.width, 0f),
                    radius = size.width * 0.8f,
                ),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 54.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    BasicText("Tienda", style = typo.secondary.copy(fontSize = 13.sp, color = Color(0xFF62627A)))
                    BasicText(
                        "Globos",
                        style = TextStyle(
                            fontFamily = typo.displayFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 26.sp,
                            letterSpacing = (-0.52).sp,
                            color = Color(0xFF1A1A2E),
                        ),
                    )
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(8.dp, CircleShape, spotColor = Color(0x33B450B4))
                        .clip(CircleShape)
                        .background(Color.White),
                ) {
                    RfIcon(kind = RfIconKind.SEARCH, tint = Color(0xFF1A1A2E), size = 20.dp)
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(44.dp)
                        .shadow(8.dp, RoundedCornerShape(999.dp), spotColor = Color(0x66A050D2))
                        .clip(RoundedCornerShape(999.dp))
                        .background(gradDiag)
                        .padding(horizontal = 10.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        RfIcon(kind = RfIconKind.ORDERS, tint = Color.White, size = 20.dp)
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(24.dp).clip(CircleShape).background(Color(0xFFFFD45C)),
                        ) {
                            BasicText("3", style = typo.sectionCap.copy(fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2E1A47)))
                        }
                    }
                }
            }

            // Chips filtro
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(38.dp)
                        .shadow(8.dp, RoundedCornerShape(999.dp), spotColor = Color(0x66A050D2))
                        .clip(RoundedCornerShape(999.dp))
                        .background(gradDiag)
                        .padding(horizontal = 15.dp),
                ) {
                    BasicText("Todos", style = typo.body.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White))
                }
                listOf("Metalizados", "Látex", "Bouquets").forEach { label ->
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .height(38.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFEEE9F2), RoundedCornerShape(999.dp))
                            .padding(horizontal = 15.dp),
                    ) {
                        BasicText(label, style = typo.body.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF62627A)))
                    }
                }
            }

            // Banner hero "Colección Pastel Dream"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(164.dp)
                    .rfHeroBannerShadow(RoundedCornerShape(24.dp), isAdmin = false)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFEFE8FD)),
            ) {
                // Globos SVG exactos
                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 6.dp, y = 4.dp),
                ) {
                    PastelDreamBalloonsIllustration(modifier = Modifier.fillMaxSize())
                }

                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.widthIn(max = 190.dp),
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.height(24.dp).clip(RoundedCornerShape(999.dp)).background(Color.White).padding(horizontal = 9.dp),
                        ) {
                            BasicText("Nuevo", style = typo.sectionCap.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7A4FD8)))
                        }
                        BasicText(
                            "Colección Pastel Dream",
                            style = TextStyle(
                                fontFamily = typo.displayFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 21.sp,
                                letterSpacing = (-0.42).sp,
                                lineHeight = 22.sp,
                                color = Color(0xFF1A1A2E),
                            ),
                        )
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .height(42.dp)
                            .shadow(8.dp, RoundedCornerShape(999.dp), spotColor = Color(0x66A050D2), ambientColor = Color.Transparent)
                            .clip(RoundedCornerShape(999.dp))
                            .background(gradDiag)
                            .padding(horizontal = 16.dp),
                    ) {
                        BasicText("Ver colección", style = typo.body.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White))
                    }
                }
            }

            // "Favoritos de la fiesta"
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicText(
                    "Favoritos de la fiesta",
                    style = TextStyle(
                        fontFamily = typo.displayFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        letterSpacing = (-0.40).sp,
                        color = Color(0xFF1A1A2E),
                    ),
                )
                BasicText("Ver todo", style = typo.body.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC2338F)))
            }

            // Grid de 4 artículos (2x2)
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            ) {
                TiendaBalloonProductCard(
                    title = "Estrella dorada",
                    price = "RD$ 5.99",
                    bg = Color(0xFFFFF2C9),
                    isFavorite = true,
                    illustration = { BalloonStarIllustration(modifier = Modifier.size(108.dp)) },
                    modifier = Modifier.weight(1f),
                )
                TiendaBalloonProductCard(
                    title = "Set rosa pastel",
                    price = "RD$ 3.50",
                    bg = Color(0xFFFDE6F2),
                    hasOfferBadge = true,
                    illustration = { BalloonPastelSetIllustration(modifier = Modifier.size(108.dp)) },
                    modifier = Modifier.weight(1f),
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            ) {
                TiendaBalloonProductCard(
                    title = "Bouquet tricolor",
                    price = "RD$ [precio]",
                    bg = Color(0xFFDDF4F3),
                    illustration = { BalloonTricolorIllustration(modifier = Modifier.size(108.dp)) },
                    modifier = Modifier.weight(1f),
                )
                TiendaBalloonProductCard(
                    title = "Arco orgánico",
                    price = "RD$ [precio]",
                    bg = Color(0xFFEEF6D6),
                    illustration = { BalloonArchShopIllustration(modifier = Modifier.size(108.dp)) },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(96.dp))
        }
    }
}

@Composable
private fun PastelDreamBalloonsIllustration(modifier: Modifier = Modifier) {
    val knotPath = remember {
        PathParser().parsePathString("M53 104c-3-4-9-3-9 1s9 3 16-1c7 4 16 5 16 1s-6-5-9-1").toPath()
    }
    Canvas(modifier = modifier) {
        val s = size.width / 120f
        val strokePaint = Color(0x736E5A72)
        val strokeW = 1.2f * s

        // Strings
        drawLine(strokePaint, Offset(40f * s, 54f * s), Offset(60f * s, 104f * s), strokeWidth = strokeW)
        drawLine(strokePaint, Offset(70f * s, 48f * s), Offset(60f * s, 104f * s), strokeWidth = strokeW)
        drawLine(strokePaint, Offset(58f * s, 72f * s), Offset(60f * s, 104f * s), strokeWidth = strokeW)
        drawLine(strokePaint, Offset(84f * s, 70f * s), Offset(60f * s, 104f * s), strokeWidth = strokeW)
        drawLine(strokePaint, Offset(34f * s, 78f * s), Offset(60f * s, 104f * s), strokeWidth = strokeW)

        // Ellipses
        drawOval(Color(0xFFF9B6D3), topLeft = Offset((40f - 15f) * s, (38f - 17f) * s), size = androidx.compose.ui.geometry.Size(30f * s, 34f * s))
        drawOval(Color(0xFFA8D8F5), topLeft = Offset((70f - 15f) * s, (32f - 17f) * s), size = androidx.compose.ui.geometry.Size(30f * s, 34f * s))
        drawOval(Color(0xFFB9A6F7), topLeft = Offset((86f - 13f) * s, (56f - 15f) * s), size = androidx.compose.ui.geometry.Size(26f * s, 30f * s))
        drawOval(Color(0xFFFFD98A), topLeft = Offset((34f - 13f) * s, (64f - 15f) * s), size = androidx.compose.ui.geometry.Size(26f * s, 30f * s))
        drawOval(Color(0xFF9BE3C9), topLeft = Offset((59f - 15f) * s, (58f - 17f) * s), size = androidx.compose.ui.geometry.Size(30f * s, 34f * s))

        // Ribbon knot
        scale(s, s, Offset.Zero) {
            drawPath(knotPath, Color(0xFFEC4F97))
        }
    }
}

@Composable
private fun BalloonStarIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val s = size.width / 120f
        val stringPath = Path().apply {
            moveTo(60f * s, 66f * s)
            cubicTo(60f * s, 80f * s, 54f * s, 92f * s, 58f * s, 112f * s)
        }
        drawPath(stringPath, color = Color(0x806E5A72), style = Stroke(width = 1.4f * s))
        val starPath = Path().apply {
            val pts = listOf(
                Offset(60f, 16f), Offset(68.8f, 37.9f), Offset(92.3f, 39.5f), Offset(74.3f, 54.6f),
                Offset(80f, 77.5f), Offset(60f, 65f), Offset(40f, 77.5f), Offset(45.7f, 54.6f),
                Offset(27.7f, 39.5f), Offset(51.2f, 37.9f),
            )
            pts.forEachIndexed { i, p ->
                if (i == 0) moveTo(p.x * s, p.y * s) else lineTo(p.x * s, p.y * s)
            }
            close()
        }
        drawPath(starPath, color = Color(0xFFF0CC7A))
        drawPath(starPath, color = Color(0xFFD9A441), style = Stroke(width = 2f * s))
        val glintPath = Path().apply {
            moveTo(55f * s, 32f * s)
            lineTo(50f * s, 40f * s)
            lineTo(40f * s, 41f * s)
        }
        drawPath(glintPath, color = Color.White.copy(alpha = 0.7f), style = Stroke(width = 2.4f * s, cap = StrokeCap.Round))
    }
}

@Composable
private fun BalloonPastelSetIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val s = size.width / 120f
        val strings = listOf(
            Offset(40f, 54f) to Offset(60f, 104f),
            Offset(70f, 48f) to Offset(60f, 104f),
            Offset(58f, 72f) to Offset(60f, 104f),
            Offset(84f, 70f) to Offset(60f, 104f),
            Offset(34f, 78f) to Offset(60f, 104f),
        )
        strings.forEach { (from, to) ->
            drawLine(Color(0x736E5A72), Offset(from.x * s, from.y * s), Offset(to.x * s, to.y * s), strokeWidth = 1.2f * s)
        }
        drawOval(Color(0xFFF9B6D3), topLeft = Offset(25f * s, 21f * s), size = androidx.compose.ui.geometry.Size(30f * s, 34f * s))
        drawOval(Color(0xFFA8D8F5), topLeft = Offset(55f * s, 15f * s), size = androidx.compose.ui.geometry.Size(30f * s, 34f * s))
        drawOval(Color(0xFFB9A6F7), topLeft = Offset(73f * s, 41f * s), size = androidx.compose.ui.geometry.Size(26f * s, 30f * s))
        drawOval(Color(0xFFFFD98A), topLeft = Offset(21f * s, 49f * s), size = androidx.compose.ui.geometry.Size(26f * s, 30f * s))
        drawOval(Color(0xFF9BE3C9), topLeft = Offset(44f * s, 41f * s), size = androidx.compose.ui.geometry.Size(30f * s, 34f * s))
        val knot = Path().apply {
            moveTo(53f * s, 104f * s)
            cubicTo(50f * s, 100f * s, 44f * s, 101f * s, 44f * s, 105f * s)
            cubicTo(44f * s, 109f * s, 53f * s, 107f * s, 60f * s, 103f * s)
            cubicTo(67f * s, 107f * s, 76f * s, 109f * s, 76f * s, 105f * s)
            cubicTo(76f * s, 101f * s, 70f * s, 100f * s, 67f * s, 104f * s)
        }
        drawPath(knot, color = Color(0xFFEC4F97))
    }
}

@Composable
private fun BalloonTricolorIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val s = size.width / 120f
        val strings = listOf(
            Path().apply { moveTo(46f * s, 62f * s); cubicTo(48f * s, 80f * s, 58f * s, 90f * s, 60f * s, 110f * s) },
            Path().apply { moveTo(76f * s, 58f * s); cubicTo(74f * s, 78f * s, 62f * s, 90f * s, 60f * s, 110f * s) },
            Path().apply { moveTo(60f * s, 78f * s); cubicTo(60f * s, 92f * s, 60f * s, 100f * s, 60f * s, 110f * s) },
        )
        strings.forEach { drawPath(it, Color(0x806E5A72), style = Stroke(width = 1.4f * s)) }
        drawOval(Color(0xFFEC4F97), topLeft = Offset(29f * s, 22f * s), size = androidx.compose.ui.geometry.Size(34f * s, 40f * s))
        drawOval(Color(0xFF7650E8), topLeft = Offset(59f * s, 18f * s), size = androidx.compose.ui.geometry.Size(34f * s, 40f * s))
        drawOval(Color(0xFFF5B53B), topLeft = Offset(44f * s, 41f * s), size = androidx.compose.ui.geometry.Size(32f * s, 38f * s))
        drawOval(Color.White.copy(alpha = 0.55f), topLeft = Offset(36.5f * s, 28f * s), size = androidx.compose.ui.geometry.Size(7f * s, 12f * s))
        drawOval(Color.White.copy(alpha = 0.55f), topLeft = Offset(66.5f * s, 24f * s), size = androidx.compose.ui.geometry.Size(7f * s, 12f * s))
        drawOval(Color.White.copy(alpha = 0.55f), topLeft = Offset(50.5f * s, 46f * s), size = androidx.compose.ui.geometry.Size(7f * s, 12f * s))
    }
}

@Composable
private fun BalloonArchShopIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val s = size.width / 120f
        drawOval(Color(0x142B1730), topLeft = Offset(10f * s, 106.5f * s), size = androidx.compose.ui.geometry.Size(100f * s, 7f * s))
        val small = listOf(
            Offset(9f, 91f) to Color(0xFFF9B6D3),
            Offset(26.6f, 60f) to Color(0xFFFFD98A),
            Offset(60f, 47f) to Color(0xFFB9A6F7),
            Offset(93.4f, 60f) to Color(0xFFA8D8F5),
            Offset(111f, 91f) to Color(0xFF9BE3C9),
        )
        small.forEach { (pt, col) ->
            drawCircle(col, radius = 6.5f * s, center = Offset(pt.x * s, pt.y * s))
        }
        val large = listOf(
            Offset(18f, 104f) to Color(0xFFEC4F97),
            Offset(20.5f, 86f) to Color(0xFFF5B53B),
            Offset(28f, 72f) to Color(0xFF7650E8),
            Offset(39f, 62.5f) to Color(0xFF4AA8E0),
            Offset(52.7f, 58f) to Color(0xFF2FB58F),
            Offset(67.3f, 58f) to Color(0xFFEC4F97),
            Offset(81f, 62.5f) to Color(0xFFF5B53B),
            Offset(92f, 72f) to Color(0xFF7650E8),
            Offset(99.5f, 86f) to Color(0xFF4AA8E0),
            Offset(102f, 104f) to Color(0xFF2FB58F),
        )
        large.forEach { (pt, col) ->
            drawCircle(col, radius = 10.5f * s, center = Offset(pt.x * s, pt.y * s))
        }
    }
}

@Composable
private fun TiendaBalloonProductCard(
    title: String,
    price: String,
    bg: Color,
    isFavorite: Boolean = false,
    hasOfferBadge: Boolean = false,
    illustration: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val typo = LocalRfTypography.current
    val gradDiag = Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9)))

    Box(
        modifier = modifier
            .rfCardShadow(RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFF2EDF5), RoundedCornerShape(20.dp)),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(128.dp)
                    .background(bg),
                contentAlignment = Alignment.Center,
            ) {
                illustration()
                if (hasOfferBadge) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(start = 10.dp, top = 10.dp)
                            .height(24.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Color(0xFFEA4EA9), Color(0xFF865DEC))))
                            .padding(horizontal = 9.dp),
                    ) {
                        BasicText("Oferta", style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White))
                    }
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 8.dp, end = 8.dp)
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.95f)),
                ) {
                    RfIcon(kind = RfIconKind.HEART, tint = if (isFavorite) Color(0xFFEA4EA9) else Color(0xFF1A1A2E), size = 18.dp)
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = title,
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A2E),
                        ),
                        maxLines = 1,
                    )
                    BasicText(
                        text = price,
                        style = TextStyle(
                            fontFamily = typo.displayFamily,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1A1A2E),
                        ),
                    )
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(gradDiag),
                ) {
                    RfIcon(kind = RfIconKind.PLUS, tint = Color.White, size = 18.dp)
                }
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
            .verticalScroll(rememberScrollState())
            .padding(top = 58.dp, start = 20.dp, end = 20.dp, bottom = 116.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            BasicText("Favoritos", style = typo.heroTitle.copy(fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1A2E)))
            BasicText("2 artículos guardados", style = typo.secondary.copy(fontSize = 15.sp, color = Color(0xFF62627A)))
        }

        Spacer(Modifier.height(18.dp))

        // Grid 2 columnas de favoritos
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            PersonalProductCard(
                title = "Silla Tiffany Cristal",
                desc = "Acrílico transparente, elegante",
                price = "4",
                photoKey = RfPhotoKey.SILLA_TIFFANY,
                bg = Color(0xFFDDF4F3),
                initialFavorite = true,
                onOpenDetail = { onSelectScreenCode("s19") },
                modifier = Modifier.weight(1f),
            )
            PersonalProductCard(
                title = "Mesa redonda",
                desc = "Para 8 a 10 invitados",
                price = "25",
                photoKey = RfPhotoKey.MESA_REDONDA,
                bg = Color(0xFFFFF2C9),
                initialFavorite = true,
                onOpenDetail = { onSelectScreenCode("s19") },
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(20.dp))

        // Tarjeta de asistencia de Rosa IA
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .rfCardShadow(RoundedCornerShape(22.dp), isAdmin = false)
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFFEFE8FD))
                .padding(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    RosaAvatar(size = RosaAvatarSize.MEDIUM)
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        BasicText("¿Los agrego a tu boda?", style = typo.body.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E)))
                        BasicText("Los dos artículos quedarían en “Boda Real Rosa & Dorado”.", style = typo.secondary.copy(fontSize = 14.sp, lineHeight = 20.sp, color = Color(0xFF62627A)))
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .rfPrimaryButtonShadow(RoundedCornerShape(999.dp), isAdmin = false)
                            .clip(RoundedCornerShape(999.dp))
                            .background(Brush.horizontalGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9))))
                            .clickable { onSelectScreenCode("s37") },
                    ) {
                        BasicText("Agregar ambos", style = typo.body.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White))
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .height(46.dp)
                            .rfCardShadow(RoundedCornerShape(14.dp), isAdmin = false)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White)
                            .clickable { onSelectScreenCode("s37") }
                            .padding(horizontal = 16.dp),
                    ) {
                        BasicText("Elegir evento", style = typo.body.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E)))
                    }
                }
            }
        }
    }
}
