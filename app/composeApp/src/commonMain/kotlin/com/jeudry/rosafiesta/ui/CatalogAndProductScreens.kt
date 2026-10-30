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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.jeudry.rosafiesta.designsystem.LocalRfPalette
import com.jeudry.rosafiesta.designsystem.LocalRfTypography
import com.jeudry.rosafiesta.designsystem.RfThemeState
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
import com.jeudry.rosafiesta.domain.CanonicalSampleData

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

    val validCategories = remember { listOf("Todas", "Iluminación", "Floral", "Globos", "Mobiliario", "Mesa dulce") }
    var selectedCategory by remember(vmCategory) {
        mutableStateOf(if (vmCategory in validCategories) vmCategory else "Todas")
    }
    val vmQuery = viewModel?.searchQuery?.collectAsState()?.value ?: ""
    var searchQuery by remember(vmQuery) { mutableStateOf(vmQuery) }
    val favoritesSet = viewModel?.favorites?.collectAsState()?.value ?: setOf("art-silla-tiffany", "art-mesa-redonda")
    val isInlineVoiceListening = viewModel?.isVoiceListening?.collectAsState()?.value ?: false
    val voiceOrigin = viewModel?.voiceInputOrigin?.collectAsState()?.value ?: VoiceInputOrigin.NONE
    val isSearchMicActive = if (isInlineVoiceListening) voiceOrigin == VoiceInputOrigin.SEARCH else false

    val filteredArticles = remember(articlesState, selectedCategory, searchQuery) {
        articlesState.filter { article ->
            val isAllSelected = selectedCategory.startsWith("Todas")
            val matchesCategoryName = article.category.equals(selectedCategory, ignoreCase = true)
            val matchesCategory = listOf(isAllSelected, matchesCategoryName).any { matched -> matched }
            val queryBlank = searchQuery.isBlank()
            val matchesName = article.name.contains(searchQuery, ignoreCase = true)
            val matchesDescription = article.description.contains(searchQuery, ignoreCase = true)
            val matchesSearch = listOf(queryBlank, matchesName, matchesDescription).any { matched -> matched }
            listOf(matchesCategory, matchesSearch).all { matched -> matched }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp, start = 20.dp, end = 20.dp),
    ) {
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
                        .background(RfThemeState.currentPalette.surface)
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
                            .background(RfThemeState.currentPalette.surface)
                            .border(1.dp, Color(0xFFECECF5), CircleShape),
                    ) {
                        RfIcon(kind = RfIconKind.PARTY, tint = Color(0xFF151729), size = 20.dp)
                    }
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

        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .rfInputShadow(RoundedCornerShape(16.dp), isAdmin = false)
                .clip(RoundedCornerShape(16.dp))
                .background(RfThemeState.currentPalette.surface)
                .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(16.dp))
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
                            onValueChange = { updatedQuery ->
                                searchQuery = updatedQuery
                                viewModel?.setSearchQuery(updatedQuery)
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
                        .background(
                            if (isSearchMicActive) {
                                Brush.linearGradient(listOf(Color(0xFFEA4EA9), Color(0xFF865DEC)))
                            } else {
                                SolidColor(Color(0xFFF5EAF9))
                            },
                        )
                        .clickable {
                            viewModel?.toggleInlineVoiceSearch()
                        },
                ) {
                    val micTint = if (isSearchMicActive) Color.White else Color(0xFFB54BD6)
                    RfIcon(kind = RfIconKind.MIC, tint = micTint, size = 20.dp)
                }
            }
        }

        InlineVoiceSearchBanner(
            viewModel = viewModel,
            onApplyVoiceQuery = { spokenQuery ->
                searchQuery = spokenQuery
                viewModel?.setSearchQuery(spokenQuery)
            },
            modifier = Modifier.padding(top = 10.dp),
        )

        Spacer(Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
        ) {
            val categories = listOf("Todas · ${articlesState.size}", "Iluminación", "Floral", "Globos", "Mobiliario", "Mesa dulce")
            categories.forEach { categoryLabel ->
                val cleanCategory = if (categoryLabel.startsWith("Todas")) "Todas" else categoryLabel
                val exactMatch = selectedCategory == cleanCategory
                val allPrefixMatch = if (selectedCategory.startsWith("Todas")) cleanCategory == "Todas" else false
                val isSelected = listOf(exactMatch, allPrefixMatch).any { matched -> matched }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(38.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(
                            if (isSelected) {
                                Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9)))
                            } else {
                                SolidColor(RfThemeState.currentPalette.chipBg)
                            },
                        )
                        .border(1.dp, if (isSelected) Color.Transparent else Color(0xFFE9E9F2), RoundedCornerShape(999.dp))
                        .clickable {
                            viewModel?.soundPlayer?.playTap()
                            selectedCategory = cleanCategory
                            viewModel?.setSelectedCategory(cleanCategory)
                        }
                        .padding(horizontal = 14.dp),
                ) {
                    BasicText(
                        text = categoryLabel,
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

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.verticalScroll(rememberScrollState()),
        ) {
            filteredArticles.chunked(2).forEach { rowPair ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    val first = rowPair[0]
                    val firstPhoto = RfPhotoKey.entries.firstOrNull { photoEntry ->
                        val matchesAsset = photoEntry.assetKey == first.photo.key
                        val matchesName = photoEntry.name.equals(first.photo.key, ignoreCase = true)
                        listOf(matchesAsset, matchesName).any { matched -> matched }
                    } ?: RfPhotoKey.SILLA_TIFFANY
                    PersonalProductCard(
                        title = first.name,
                        desc = first.description,
                        price = first.pricePerDayDop.toString(),
                        photoKey = firstPhoto,
                        bg = Color(0xFFEFE8FD),
                        initialFavorite = favoritesSet.contains(first.id),
                        useGradientPrice = true,
                        onOpenDetail = {
                            viewModel?.soundPlayer?.playTap()
                            viewModel?.selectArticle(first.id)
                            onSelectScreenCode("s19")
                        },
                        onAddClick = {
                            viewModel?.soundPlayer?.playTap()
                            viewModel?.adjustDraftQuantity(first.id, 1)
                        },
                        onToggleFavorite = {
                            viewModel?.soundPlayer?.playTap()
                            viewModel?.toggleFavorite(first.id)
                        },
                        modifier = Modifier.weight(1f),
                    )
                    if (rowPair.size > 1) {
                        val second = rowPair[1]
                        val secondPhoto = RfPhotoKey.entries.firstOrNull { photoEntry ->
                            val matchesAsset = photoEntry.assetKey == second.photo.key
                            val matchesName = photoEntry.name.equals(second.photo.key, ignoreCase = true)
                            listOf(matchesAsset, matchesName).any { matched -> matched }
                        } ?: RfPhotoKey.SILLA_TIFFANY
                        PersonalProductCard(
                            title = second.name,
                            desc = second.description,
                            price = second.pricePerDayDop.toString(),
                            photoKey = secondPhoto,
                            bg = Color(0xFFEEF6D6),
                            initialFavorite = favoritesSet.contains(second.id),
                            useGradientPrice = true,
                            onOpenDetail = {
                                viewModel?.soundPlayer?.playTap()
                                viewModel?.selectArticle(second.id)
                                onSelectScreenCode("s19")
                            },
                            onAddClick = {
                                viewModel?.soundPlayer?.playTap()
                                viewModel?.adjustDraftQuantity(second.id, 1)
                            },
                            onToggleFavorite = {
                                viewModel?.soundPlayer?.playTap()
                                viewModel?.toggleFavorite(second.id)
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
    onAddClick: () -> Unit = onOpenDetail,
    onToggleFavorite: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val typo = LocalRfTypography.current
    val palette = LocalRfPalette.current
    var isFavorite by remember(title, initialFavorite) { mutableStateOf(initialFavorite) }

    Box(
        modifier = modifier
            .rfCardShadow(RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(palette.surface)
            .border(1.dp, palette.line, RoundedCornerShape(20.dp))
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
                        .background(RfThemeState.currentPalette.surface)
                        .clickable {
                            isFavorite = !isFavorite
                            onToggleFavorite?.invoke()
                        },
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
                BasicText(title, maxLines = 1, style = typo.body.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink))
                Spacer(Modifier.height(3.dp))
                BasicText(desc, maxLines = 1, style = typo.secondary.copy(fontSize = 13.sp, color = RfThemeState.currentPalette.muted))
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
                                    color = RfThemeState.currentPalette.ink,
                                ),
                            )
                            BasicText(" /día", style = typo.secondary.copy(fontSize = 12.sp, color = RfThemeState.currentPalette.muted))
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
                            )
                            .clickable(onClick = onAddClick),
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

    val primaryPhoto = RfPhotoKey.entries.firstOrNull { entry ->
        val matchesAsset = entry.assetKey == currentArticle.photo.key
        val matchesName = entry.name.equals(currentArticle.photo.key, ignoreCase = true)
        matchesAsset || matchesName
    } ?: RfPhotoKey.SILLA_TIFFANY
    val galleryPhotos = remember(primaryPhoto) {
        listOf(primaryPhoto, RfPhotoKey.MESA_REDONDA, RfPhotoKey.CENTRO_FLORAL)
    }
    var selectedPhotoIndex by remember(currentArticle.id) { mutableStateOf(0) }
    val activeHeroPhoto = galleryPhotos.getOrElse(selectedPhotoIndex) { primaryPhoto }
    val sheetScrollState = rememberScrollState()
    val parallaxOffsetDp = (-sheetScrollState.value / 6).dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RfThemeState.currentPalette.bg),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(410.dp)
                .offset(y = parallaxOffsetDp),
        ) {
            RfPhoto(
                photoKey = activeHeroPhoto,
                contentDescription = currentArticle.name,
                cornerRadius = 0.dp,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(Brush.verticalGradient(listOf(Color(0x501A1A2E), Color.Transparent))),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(sheetScrollState),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(316.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 20.dp, bottom = 14.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0x6628283C))
                        .padding(6.dp),
                ) {
                    galleryPhotos.forEachIndexed { photoIndex, galleryKey ->
                        val isSelectedPhoto = photoIndex == selectedPhotoIndex
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .then(
                                    if (isSelectedPhoto) {
                                        Modifier.border(2.5.dp, Color(0xFFEA4EA9), RoundedCornerShape(12.dp))
                                    } else {
                                        Modifier
                                    },
                                )
                                .clickable { selectedPhotoIndex = photoIndex },
                        ) {
                            RfPhoto(
                                photoKey = galleryKey,
                                contentDescription = "Vista ${photoIndex + 1}",
                                cornerRadius = 12.dp,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(RfThemeState.currentPalette.surface)
                            .clickable { selectedPhotoIndex = (selectedPhotoIndex + 1) % galleryPhotos.size },
                    ) {
                        BasicText("+3", style = typo.body.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.muted))
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(18.dp, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp), spotColor = Color(0x331A1A2E))
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(RfThemeState.currentPalette.surface)
                    .padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 116.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(42.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFFE3DCE8)),
                )

                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        BasicText(
                            currentArticle.name,
                            style = typo.heroTitle.copy(fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)),
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(Color(0xFFFFF2C9))
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    BasicText("★ 5.0", style = typo.body.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7A5700)))
                                    BasicText("12 reseñas", style = typo.secondary.copy(fontSize = 13.sp, color = Color(0xFF7A5700)))
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(Color(0xFFEEF6D6))
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                            ) {
                                BasicText(
                                    "${currentArticle.availableOct23} disp. 23 oct",
                                    style = typo.secondary.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4E6A10)),
                                )
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        RfGradientPriceText(
                            text = "RD$ ${currentArticle.pricePerDayDop}",
                            style = typo.heroTitle.copy(fontSize = 28.sp, fontWeight = FontWeight.ExtraBold),
                        )
                        BasicText("por unidad / día", style = typo.secondary.copy(fontSize = 12.sp, color = RfThemeState.currentPalette.muted))
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row {
                        BasicText("Color: ", style = typo.body.copy(fontSize = 14.sp, color = Color(0xFF5F6377)))
                        BasicText(selectedColor, style = typo.body.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        val colors = listOf("Transparente" to Color(0xFF7C5CF0), "Rosa" to Color(0xFFEA4EA9), "Verde" to Color(0xFFA9CC3A), "Dorado" to Color(0xFFF0B92E))
                        colors.forEach { (colorName, colorValue) ->
                            val isSel = colorName == selectedColor
                            val isSelectedTransparent = if (isSel) colorName == "Transparente" else false
                            val gradDiag = Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9)))
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelectedTransparent) {
                                            gradDiag
                                        } else if (isSel) {
                                            SolidColor(colorValue)
                                        } else {
                                            SolidColor(Color.White)
                                        },
                                    )
                                    .border(2.5.dp, colorValue, CircleShape)
                                    .clickable { selectedColor = colorName },
                            ) {
                                if (isSel) {
                                    RfIcon(kind = RfIconKind.CHECK, tint = Color.White, size = 18.dp)
                                }
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(RfThemeState.currentPalette.chipBg)
                        .padding(4.dp),
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        val tabs = listOf("Descripción", "Detalles", "Reseñas")
                        tabs.forEach { tabName ->
                            val isSel = tabName == selectedTab
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
                                    .clickable { selectedTab = tabName },
                            ) {
                                BasicText(
                                    text = tabName,
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

                when (selectedTab) {
                    "Detalles" -> {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            listOf(
                                "Código de catálogo" to currentArticle.code,
                                "Categoría" to currentArticle.category,
                                "Stock total en almacén" to "${currentArticle.totalStock} unidades",
                                "Disponibles para el 23 oct" to "${currentArticle.availableOct23} unidades",
                                "Material y acabado" to "Resina reforzada UV · Cojín impermeable incluido",
                                "Dimensiones" to "92 cm alto × 40 cm ancho × 42 cm fondo",
                                "Incluye montaje" to "Sí, transporte e instalación por el equipo Rosa Fiesta",
                            ).forEach { (specLabel, specValue) ->
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFF9F7FC))
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                ) {
                                    BasicText(specLabel, style = typo.body.copy(fontSize = 13.5.sp, color = RfThemeState.currentPalette.muted))
                                    BasicText(specValue, style = typo.body.copy(fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)))
                                }
                            }
                        }
                    }

                    "Reseñas" -> {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            listOf(
                                Triple("Camila Rodríguez", "★★★★★ · Boda Real", "Llegaron impecables y con los cojines blancos nuevos. El montaje estuvo listo 2 horas antes."),
                                Triple("Valentina Mejía", "★★★★★ · Quinceañera", "El acabado $selectedColor se vio espectacular con la iluminación cálida del salón."),
                                Triple("Laura Peña", "★★★★★ · Evento Corporativo", "Muy cómodas y resistentes, pedimos 120 unidades y todas estaban en perfecto estado."),
                            ).forEach { (authorName, ratingMeta, reviewComment) ->
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color(0xFFF9F7FC))
                                        .border(1.dp, Color(0xFFEEE9F5), RoundedCornerShape(14.dp))
                                        .padding(14.dp),
                                ) {
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        BasicText(authorName, style = typo.body.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)))
                                        BasicText(ratingMeta, style = typo.caption.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7A5700)))
                                    }
                                    BasicText(reviewComment, style = typo.body.copy(fontSize = 13.5.sp, color = Color(0xFF5F6377), lineHeight = 19.sp))
                                }
                            }
                        }
                    }

                    else -> {
                        BasicText(
                            text = currentArticle.description,
                            style = typo.body.copy(fontSize = 15.sp, color = Color(0xFF5F6377), lineHeight = 22.sp),
                        )
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFF9F7FC))
                                .border(1.dp, Color(0xFFEEE9F5), RoundedCornerShape(16.dp))
                                .padding(14.dp),
                        ) {
                            BasicText(
                                "Especificaciones destacadas",
                                style = typo.body.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)),
                            )
                            BasicText(
                                "• Acabado seleccionado: $selectedColor (${currentArticle.code})\n• Cojín acolchado blanco marfil desmontable e impermeable\n• Limpieza y desinfección certificada antes de cada entrega\n• Garantía de reposición inmediata incluida en tu reserva",
                                style = typo.body.copy(fontSize = 13.5.sp, color = Color(0xFF5F6377), lineHeight = 20.sp),
                            )
                        }
                    }
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .zIndex(2f)
                .padding(top = 16.dp, start = 20.dp, end = 20.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .rfIconButtonShadow(CircleShape, isAdmin = false)
                    .clip(CircleShape)
                    .background(RfThemeState.currentPalette.surface)
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
                        .background(RfThemeState.currentPalette.surface),
                ) {
                    RfIcon(kind = RfIconKind.SHARE, tint = Color(0xFF151729), size = 20.dp)
                }
                val favoritesSet = viewModel?.favorites?.collectAsState()?.value
                val isArticleFavorite = favoritesSet?.contains(currentArticle.id) ?: true
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = false)
                        .clip(CircleShape)
                        .background(RfThemeState.currentPalette.surface)
                        .clickable {
                            viewModel?.soundPlayer?.playTap()
                            viewModel?.toggleFavorite(currentArticle.id)
                        },
                ) {
                    RfIcon(
                        kind = RfIconKind.HEART,
                        tint = if (isArticleFavorite) Color(0xFFEA4EA9) else Color(0xFF151729),
                        filled = isArticleFavorite,
                        size = 20.dp,
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .zIndex(2f)
                .background(RfThemeState.currentPalette.surface)
                .border(1.dp, RfThemeState.currentPalette.line)
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
                        .background(RfThemeState.currentPalette.chipBg)
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
            .background(RfThemeState.currentPalette.bg),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(Brush.radialGradient(colors = listOf(RfThemeState.currentPalette.radialPrimary, RfThemeState.currentPalette.radialPrimary.copy(alpha = 0f)), center = Offset(0f, 0f), radius = size.width * 0.9f))
            drawRect(Brush.radialGradient(colors = listOf(RfThemeState.currentPalette.radialSecondary, RfThemeState.currentPalette.radialSecondary.copy(alpha = 0f)), center = Offset(size.width, 0f), radius = size.width * 0.8f))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp, start = 20.dp, end = 20.dp),
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
                        .background(RfThemeState.currentPalette.surface)
                        .clickable { onSelectScreenCode("BACK:s18") },
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = RfThemeState.currentPalette.ink, size = 22.dp)
                }
                Column {
                    BasicText(
                        "Categorías",
                        style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, letterSpacing = (-0.48).sp, color = RfThemeState.currentPalette.ink),
                    )
                    BasicText(
                        "Explora el catálogo por tipo de artículo",
                        style = typo.secondary.copy(fontSize = 14.sp, color = RfThemeState.currentPalette.muted),
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
    ILUMINACION,
    FLORAL,
    GLOBOS,
    MANTELERIA,
    MESA_DULCE,
    LETREROS,
    MOBILIARIO,
    DECORACION,
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
                        color = wireColor,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f * density),
                    )
                    drawPath(
                        path = androidx.compose.ui.graphics.Path().apply {
                            moveTo(w * 0.02f, h * 0.72f)
                            cubicTo(w * 0.22f, h * 0.94f, w * 0.44f, h * 0.94f, w * 0.64f, h * 0.78f)
                            cubicTo(w * 0.80f, h * 0.66f, w * 1.0f, h * 0.66f, w * 1.0f, h * 0.82f)
                        },
                        color = wireColor,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f * density),
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
                        val cx = w * 0.50f
                        val cy = h * 0.42f
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
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 10f * density),
                    )
                    drawPath(
                        path = androidx.compose.ui.graphics.Path().apply {
                            moveTo(w * 0.28f, h * 0.82f)
                            cubicTo(w * 0.33f, h * 0.76f, w * 0.38f, h * 0.76f, w * 0.44f, h * 0.82f)
                            cubicTo(w * 0.50f, h * 0.88f, w * 0.55f, h * 0.88f, w * 0.60f, h * 0.82f)
                            cubicTo(w * 0.66f, h * 0.76f, w * 0.71f, h * 0.76f, w * 0.74f, h * 0.82f)
                        },
                        color = androidx.compose.ui.graphics.Color(0xFFFFC75A),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.2f * density),
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
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 116.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            BasicText("Paquetes", style = typo.heroTitle.copy(fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = RfThemeState.currentPalette.ink))
            BasicText("Colecciones temáticas, listas para tu evento", style = typo.secondary.copy(fontSize = 14.sp, color = RfThemeState.currentPalette.muted))
        }

        Spacer(Modifier.height(18.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
            PackageSummaryCard("Bodas Rosa", "Paquete romántico completo para bodas, con arco floral y centros de mesa.", "RD$ 850", "Ahorra 15%", RfPhotoKey.BODA, Color(0xFFFDE6F2)) {
                viewModel?.selectPackage("pkg-boda-real")
                onSelectScreenCode("s22")
            }
            PackageSummaryCard("Quinceañera Premium", "Paquete de lujo para quinceañeras con arco floral.", "RD$ 1,200", "Ahorra 12%", RfPhotoKey.QUINCEANERA, Color(0xFFEFE8FD)) {
                viewModel?.selectPackage("pkg-quince")
                onSelectScreenCode("s22")
            }
            PackageSummaryCard("Cumpleaños Infantil", "Kit completo para fiestas infantiles: globos, banner y más.", "RD$ 450", "Ahorra 10%", RfPhotoKey.CUMPLE_INFANTIL, Color(0xFFEEF6D6)) {
                viewModel?.selectPackage("pkg-infantil")
                onSelectScreenCode("s22")
            }
            PackageSummaryCard("Evento Corporativo", "Solución profesional para eventos empresariales.", "RD$ 600", "Ahorra 8%", RfPhotoKey.CORPORATIVO, Color(0xFFFFF2C9)) {
                viewModel?.selectPackage("pkg-corporativo")
                onSelectScreenCode("s22")
            }
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
            .background(RfThemeState.currentPalette.surface)
            .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp))
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
                BasicText(title, style = typo.heroTitle.copy(fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = RfThemeState.currentPalette.ink))
                BasicText(desc, maxLines = 2, style = typo.secondary.copy(fontSize = 13.sp, lineHeight = 18.2.sp, color = RfThemeState.currentPalette.muted))
                Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(top = 2.dp)) {
                    BasicText("desde ", style = typo.secondary.copy(fontSize = 13.sp, color = RfThemeState.currentPalette.muted))
                    BasicText(price, style = typo.heroTitle.copy(fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFC2338F)))
                }
            }
        }
    }
}

private data class PackageLineItemConfig(
    val articleId: String,
    val title: String,
    val unitPriceDop: Int,
    val defaultQuantity: Int,
    val photoKey: RfPhotoKey,
    val bg: Color,
)

private data class PackageCatalogDetailSpec(
    val id: String,
    val title: String,
    val description: String,
    val discountPercent: Int,
    val coverPhoto: RfPhotoKey,
    val coverBg: Color,
    val items: List<PackageLineItemConfig>,
)

// ==============================================================================
// s22 · DETALLE DE PAQUETE
// ==============================================================================

@Composable
internal fun RenderPackageDetailScreenS22(
    onSelectScreenCode: (String) -> Unit,
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val selectedPkgId = viewModel?.selectedPackageId?.collectAsState()?.value ?: "pkg-boda-real"
    val packageSpecs = remember {
        listOf(
            PackageCatalogDetailSpec(
                id = "pkg-boda-real",
                title = "Bodas Rosa",
                description = "Paquete romántico completo para bodas. Incluye arco de globos, centros de mesa florales, sillas Tiffany y mesas redondas, con 15% de descuento.",
                discountPercent = 15,
                coverPhoto = RfPhotoKey.BODA,
                coverBg = Color(0xFFFDE6F2),
                items = listOf(
                    PackageLineItemConfig("art-arco-rosa", "Arco de globos rosa", 95, 1, RfPhotoKey.ARCO_ROSA, Color(0xFFFDE6F2)),
                    PackageLineItemConfig("art-centro-floral", "Centro de mesa floral", 35, 10, RfPhotoKey.CENTRO_FLORAL, Color(0xFFFFF2C9)),
                    PackageLineItemConfig("art-silla-tiffany", "Silla Tiffany Cristal", 4, 120, RfPhotoKey.SILLA_TIFFANY, Color(0xFFEFE8FD)),
                    PackageLineItemConfig("art-mesa-redonda", "Mesa redonda", 25, 3, RfPhotoKey.MESA_REDONDA, Color(0xFFEEF6D6)),
                ),
            ),
            PackageCatalogDetailSpec(
                id = "pkg-quince",
                title = "Quinceañera Premium",
                description = "Paquete de lujo para quinceañeras con arco floral, estación dulce, sillas Tiffany y mesas redondas con 12% de descuento.",
                discountPercent = 12,
                coverPhoto = RfPhotoKey.QUINCEANERA,
                coverBg = Color(0xFFEFE8FD),
                items = listOf(
                    PackageLineItemConfig("art-arco-rosa", "Arco de globos rosa", 95, 2, RfPhotoKey.ARCO_ROSA, Color(0xFFFDE6F2)),
                    PackageLineItemConfig("art-mesa-helados", "Estación mesa dulce", 150, 1, RfPhotoKey.MESA_HELADOS, Color(0xFFEEF6D6)),
                    PackageLineItemConfig("art-centro-floral", "Centro de mesa floral", 35, 12, RfPhotoKey.CENTRO_FLORAL, Color(0xFFFFF2C9)),
                    PackageLineItemConfig("art-silla-tiffany", "Silla Tiffany Cristal", 4, 150, RfPhotoKey.SILLA_TIFFANY, Color(0xFFEFE8FD)),
                ),
            ),
            PackageCatalogDetailSpec(
                id = "pkg-infantil",
                title = "Cumpleaños Infantil",
                description = "Kit completo para fiestas infantiles: arco orgánico de globos, carrito de helados, sillas y mesas plegables con 10% de descuento.",
                discountPercent = 10,
                coverPhoto = RfPhotoKey.CUMPLE_INFANTIL,
                coverBg = Color(0xFFEEF6D6),
                items = listOf(
                    PackageLineItemConfig("art-arco-rosa", "Arco de globos rosa", 95, 1, RfPhotoKey.ARCO_ROSA, Color(0xFFFDE6F2)),
                    PackageLineItemConfig("art-mesa-helados", "Mesa de helados", 150, 1, RfPhotoKey.MESA_HELADOS, Color(0xFFEEF6D6)),
                    PackageLineItemConfig("art-mesa-plegable", "Mesa plegable", 18, 6, RfPhotoKey.MESA_PLEGABLE, Color(0xFFFFF2C9)),
                    PackageLineItemConfig("art-silla-tiffany", "Silla Tiffany Cristal", 4, 40, RfPhotoKey.SILLA_TIFFANY, Color(0xFFEFE8FD)),
                ),
            ),
            PackageCatalogDetailSpec(
                id = "pkg-corporativo",
                title = "Evento Corporativo",
                description = "Solución profesional para eventos empresariales con mobiliario ejecutivo, centros florales sobrios y 8% de descuento.",
                discountPercent = 8,
                coverPhoto = RfPhotoKey.CORPORATIVO,
                coverBg = Color(0xFFFFF2C9),
                items = listOf(
                    PackageLineItemConfig("art-mesa-redonda", "Mesa redonda", 25, 10, RfPhotoKey.MESA_REDONDA, Color(0xFFEEF6D6)),
                    PackageLineItemConfig("art-silla-tiffany", "Silla Tiffany Cristal", 4, 80, RfPhotoKey.SILLA_TIFFANY, Color(0xFFEFE8FD)),
                    PackageLineItemConfig("art-centro-floral", "Centro de mesa floral", 35, 4, RfPhotoKey.CENTRO_FLORAL, Color(0xFFFFF2C9)),
                ),
            ),
        )
    }
    val currentPkg = packageSpecs.firstOrNull { spec -> spec.id == selectedPkgId } ?: packageSpecs.first()
    var itemQuantities by remember(currentPkg.id) {
        mutableStateOf(currentPkg.items.associate { item -> item.articleId to item.defaultQuantity })
    }
    var checkedItems by remember(currentPkg.id) {
        mutableStateOf(currentPkg.items.map { item -> item.articleId }.toSet())
    }

    val totalSelectedItemsCount = currentPkg.items.sumOf { item ->
        val isChecked = checkedItems.contains(item.articleId)
        val qty = itemQuantities[item.articleId] ?: item.defaultQuantity
        if (isChecked) qty else 0
    }
    val subtotalDop = currentPkg.items.sumOf { item ->
        val isChecked = checkedItems.contains(item.articleId)
        val qty = itemQuantities[item.articleId] ?: item.defaultQuantity
        if (isChecked) qty * item.unitPriceDop else 0
    }
    val discountedTotalDop = ((subtotalDop * (100 - currentPkg.discountPercent)) / 100.0).toInt()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RfThemeState.currentPalette.bg),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(currentPkg.coverBg),
            ) {
                RfPhoto(
                    photoKey = currentPkg.coverPhoto,
                    contentDescription = currentPkg.title,
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
                        .background(RfThemeState.currentPalette.surface)
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
                    BasicText("Ahorra ${currentPkg.discountPercent}%", style = typo.sectionCap.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4E6A10)))
                }
                Spacer(Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    BasicText(currentPkg.title, style = typo.heroTitle.copy(fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)))
                    Row(verticalAlignment = Alignment.Bottom) {
                        BasicText("desde ", style = typo.secondary.copy(fontSize = 14.sp, color = Color(0xFF5F6377)))
                        BasicText("RD$ $discountedTotalDop", style = typo.heroTitle.copy(fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFC2338F)))
                    }
                }
                Spacer(Modifier.height(8.dp))
                BasicText(
                    text = currentPkg.description,
                    style = typo.body.copy(fontSize = 15.sp, color = Color(0xFF5F6377), lineHeight = 22.sp),
                )

                Spacer(Modifier.height(20.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    BasicText("Incluye", style = typo.heroTitle.copy(fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)))
                    BasicText("Desmarca lo que no quieras", style = typo.secondary.copy(fontSize = 13.sp, color = RfThemeState.currentPalette.muted))
                }
                Spacer(Modifier.height(10.dp))

                currentPkg.items.forEachIndexed { itemIndex, lineItem ->
                    val currentQty = itemQuantities[lineItem.articleId] ?: lineItem.defaultQuantity
                    val isChecked = checkedItems.contains(lineItem.articleId)
                    PackageItemRow(
                        title = lineItem.title,
                        price = "RD$ ${lineItem.unitPriceDop} / día",
                        qty = currentQty.toString(),
                        photoKey = lineItem.photoKey,
                        bg = lineItem.bg,
                        isChecked = isChecked,
                        onToggleCheck = {
                            checkedItems = if (isChecked) {
                                checkedItems - lineItem.articleId
                            } else {
                                checkedItems + lineItem.articleId
                            }
                        },
                        onDecrease = {
                            val nextQty = (currentQty - 1).coerceAtLeast(1)
                            itemQuantities = itemQuantities + (lineItem.articleId to nextQty)
                        },
                        onIncrease = {
                            val nextQty = currentQty + 1
                            itemQuantities = itemQuantities + (lineItem.articleId to nextQty)
                            if (!isChecked) {
                                checkedItems = checkedItems + lineItem.articleId
                            }
                        },
                    )
                    if (itemIndex < currentPkg.items.lastIndex) {
                        Spacer(Modifier.height(8.dp))
                    }
                }

                Spacer(Modifier.height(90.dp))
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(RfThemeState.currentPalette.surface)
                .border(1.dp, RfThemeState.currentPalette.line),
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
                            text = "$totalSelectedItemsCount artículos · ",
                            style = typo.secondary.copy(fontSize = 12.sp, color = RfThemeState.currentPalette.muted),
                        )
                        BasicText(
                            text = "RD$ $subtotalDop",
                            style = typo.secondary.copy(
                                fontSize = 12.sp,
                                color = RfThemeState.currentPalette.muted,
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough,
                            ),
                        )
                    }
                    BasicText(
                        text = "RD$ $discountedTotalDop",
                        style = TextStyle(
                            fontFamily = typo.displayFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            color = RfThemeState.currentPalette.ink,
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
                        .clickable {
                            currentPkg.items.forEach { item ->
                                val isChecked = checkedItems.contains(item.articleId)
                                val qty = itemQuantities[item.articleId] ?: item.defaultQuantity
                                if (isChecked) {
                                    viewModel?.adjustDraftQuantity(item.articleId, qty)
                                }
                            }
                            onSelectScreenCode("s25")
                        },
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
    isChecked: Boolean = true,
    onToggleCheck: () -> Unit = {},
    onDecrease: () -> Unit = {},
    onIncrease: () -> Unit = {},
) {
    val typo = LocalRfTypography.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .rfCardShadow(RoundedCornerShape(18.dp), isAdmin = false)
            .clip(RoundedCornerShape(18.dp))
            .background(RfThemeState.currentPalette.surface)
            .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(18.dp))
            .padding(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(
                        if (isChecked) {
                            Brush.linearGradient(listOf(Color(0xFFEA4EA9), Color(0xFF865DEC)))
                        } else {
                            SolidColor(RfThemeState.currentPalette.chipBg)
                        },
                    )
                    .clickable(onClick = onToggleCheck),
            ) {
                if (isChecked) {
                    RfIcon(kind = RfIconKind.CHECK, tint = Color.White, size = 16.dp)
                }
            }

            Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(bg)) {
                RfPhoto(photoKey = photoKey, contentDescription = title, cornerRadius = 12.dp, modifier = Modifier.fillMaxSize())
            }

            Column(modifier = Modifier.weight(1f)) {
                BasicText(title, style = typo.body.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)))
                BasicText(price, style = typo.secondary.copy(fontSize = 13.sp, color = Color(0xFF5F6377)))
            }

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(12.dp))
                    .height(36.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp, 36.dp)
                        .clickable(onClick = onDecrease),
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.MINUS, tint = RfThemeState.currentPalette.ink, size = 16.dp)
                }
                BasicText(qty, style = typo.body.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink))
                Box(
                    modifier = Modifier
                        .size(32.dp, 36.dp)
                        .clickable(onClick = onIncrease),
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.PLUS, tint = RfThemeState.currentPalette.ink, size = 16.dp)
                }
            }
        }
    }
}

// ==============================================================================
// s23 · TIENDA (GLOBOS)
// ==============================================================================

private data class BalloonShopItemSpec(
    val title: String,
    val price: String,
    val bg: Color,
    val category: String,
    val initialFavorite: Boolean = false,
    val hasOfferBadge: Boolean = false,
    val illustration: @Composable () -> Unit,
)

@Composable
internal fun RenderShopScreenS23(
    onSelectScreenCode: (String) -> Unit,
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val gradDiag = Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9)))
    var selectedFilter by remember { mutableStateOf("Todos") }
    val draftQuantities = viewModel?.draftQuantities?.collectAsState()?.value ?: emptyMap()
    val vmCartTotal = draftQuantities.values.sum()
    var localAddedCount by remember { mutableStateOf(0) }
    val cartCount = if (vmCartTotal > 0) vmCartTotal + localAddedCount else 3 + localAddedCount

    val allBalloonItems = remember {
        listOf(
            BalloonShopItemSpec(
                title = "Estrella dorada",
                price = "RD$ 5.99",
                bg = Color(0xFFFFF2C9),
                category = "Metalizados",
                initialFavorite = true,
                illustration = { BalloonStarIllustration(modifier = Modifier.size(108.dp)) },
            ),
            BalloonShopItemSpec(
                title = "Set rosa pastel",
                price = "RD$ 3.50",
                bg = Color(0xFFFDE6F2),
                category = "Látex",
                hasOfferBadge = true,
                illustration = { BalloonPastelSetIllustration(modifier = Modifier.size(108.dp)) },
            ),
            BalloonShopItemSpec(
                title = "Bouquet tricolor",
                price = "RD$ 12.50",
                bg = Color(0xFFDDF4F3),
                category = "Bouquets",
                illustration = { BalloonTricolorIllustration(modifier = Modifier.size(108.dp)) },
            ),
            BalloonShopItemSpec(
                title = "Arco orgánico",
                price = "RD$ 45.00",
                bg = Color(0xFFEEF6D6),
                category = "Látex",
                illustration = { BalloonArchShopIllustration(modifier = Modifier.size(108.dp)) },
            ),
        )
    }

    val filteredItems = remember(allBalloonItems, selectedFilter) {
        if (selectedFilter == "Todos") {
            allBalloonItems
        } else {
            allBalloonItems.filter { item -> item.category == selectedFilter }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RfThemeState.currentPalette.bg),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                Brush.radialGradient(
                    colors = listOf(RfThemeState.currentPalette.radialPrimary, RfThemeState.currentPalette.radialPrimary.copy(alpha = 0f)),
                    center = Offset(0f, 0f),
                    radius = size.width * 0.9f,
                ),
            )
            drawRect(
                Brush.radialGradient(
                    colors = listOf(RfThemeState.currentPalette.radialSecondary, RfThemeState.currentPalette.radialSecondary.copy(alpha = 0f)),
                    center = Offset(size.width, 0f),
                    radius = size.width * 0.8f,
                ),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    BasicText("Tienda", style = typo.secondary.copy(fontSize = 13.sp, color = RfThemeState.currentPalette.muted))
                    BasicText(
                        "Globos",
                        style = TextStyle(
                            fontFamily = typo.displayFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 26.sp,
                            letterSpacing = (-0.52).sp,
                            color = RfThemeState.currentPalette.ink,
                        ),
                    )
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(8.dp, CircleShape, spotColor = Color(0x33B450B4))
                        .clip(CircleShape)
                        .background(RfThemeState.currentPalette.surface)
                        .clickable { onSelectScreenCode("s80") },
                ) {
                    RfIcon(kind = RfIconKind.SEARCH, tint = RfThemeState.currentPalette.ink, size = 20.dp)
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(44.dp)
                        .shadow(8.dp, RoundedCornerShape(999.dp), spotColor = Color(0x66A050D2))
                        .clip(RoundedCornerShape(999.dp))
                        .background(gradDiag)
                        .clickable { onSelectScreenCode("s25") }
                        .padding(horizontal = 10.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        RfIcon(kind = RfIconKind.ORDERS, tint = Color.White, size = 20.dp)
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(24.dp).clip(CircleShape).background(Color(0xFFFFD45C)),
                        ) {
                            BasicText("$cartCount", style = typo.sectionCap.copy(fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2E1A47)))
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf("Todos", "Metalizados", "Látex", "Bouquets").forEach { label ->
                    val isSelected = selectedFilter == label
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .height(38.dp)
                            .then(
                                if (isSelected) {
                                    Modifier.shadow(8.dp, RoundedCornerShape(999.dp), spotColor = Color(0x66A050D2))
                                } else {
                                    Modifier
                                },
                            )
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (isSelected) gradDiag else SolidColor(Color.White))
                            .border(1.dp, if (isSelected) Color.Transparent else RfThemeState.currentPalette.line, RoundedCornerShape(999.dp))
                            .clickable { selectedFilter = label }
                            .padding(horizontal = 15.dp),
                    ) {
                        BasicText(
                            text = label,
                            style = typo.body.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) Color.White else Color(0xFF62627A),
                            ),
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(164.dp)
                    .rfHeroBannerShadow(RoundedCornerShape(24.dp), isAdmin = false)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFEFE8FD)),
            ) {
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
                            modifier = Modifier.height(24.dp).clip(RoundedCornerShape(999.dp)).background(RfThemeState.currentPalette.surface).padding(horizontal = 9.dp),
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
                                color = RfThemeState.currentPalette.ink,
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
                            .clickable { selectedFilter = "Látex" }
                            .padding(horizontal = 16.dp),
                    ) {
                        BasicText("Ver colección", style = typo.body.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White))
                    }
                }
            }

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
                        color = RfThemeState.currentPalette.ink,
                    ),
                )
                BasicText(
                    text = "Ver todo",
                    style = typo.body.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC2338F)),
                    modifier = Modifier.clickable { selectedFilter = "Todos" },
                )
            }

            filteredItems.chunked(2).forEach { rowPair ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                ) {
                    val first = rowPair[0]
                    TiendaBalloonProductCard(
                        title = first.title,
                        price = first.price,
                        bg = first.bg,
                        isFavorite = first.initialFavorite,
                        hasOfferBadge = first.hasOfferBadge,
                        illustration = first.illustration,
                        onAddClick = {
                            localAddedCount += 1
                            viewModel?.adjustDraftQuantity("art-arco-rosa", 1)
                        },
                        modifier = Modifier.weight(1f),
                    )
                    if (rowPair.size > 1) {
                        val second = rowPair[1]
                        TiendaBalloonProductCard(
                            title = second.title,
                            price = second.price,
                            bg = second.bg,
                            isFavorite = second.initialFavorite,
                            hasOfferBadge = second.hasOfferBadge,
                            illustration = second.illustration,
                            onAddClick = {
                                localAddedCount += 1
                                viewModel?.adjustDraftQuantity("art-arco-rosa", 1)
                            },
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
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

        drawLine(strokePaint, Offset(40f * s, 54f * s), Offset(60f * s, 104f * s), strokeWidth = strokeW)
        drawLine(strokePaint, Offset(70f * s, 48f * s), Offset(60f * s, 104f * s), strokeWidth = strokeW)
        drawLine(strokePaint, Offset(58f * s, 72f * s), Offset(60f * s, 104f * s), strokeWidth = strokeW)
        drawLine(strokePaint, Offset(84f * s, 70f * s), Offset(60f * s, 104f * s), strokeWidth = strokeW)
        drawLine(strokePaint, Offset(34f * s, 78f * s), Offset(60f * s, 104f * s), strokeWidth = strokeW)

        drawOval(Color(0xFFF9B6D3), topLeft = Offset((40f - 15f) * s, (38f - 17f) * s), size = androidx.compose.ui.geometry.Size(30f * s, 34f * s))
        drawOval(Color(0xFFA8D8F5), topLeft = Offset((70f - 15f) * s, (32f - 17f) * s), size = androidx.compose.ui.geometry.Size(30f * s, 34f * s))
        drawOval(Color(0xFFB9A6F7), topLeft = Offset((86f - 13f) * s, (56f - 15f) * s), size = androidx.compose.ui.geometry.Size(26f * s, 30f * s))
        drawOval(Color(0xFFFFD98A), topLeft = Offset((34f - 13f) * s, (64f - 15f) * s), size = androidx.compose.ui.geometry.Size(26f * s, 30f * s))
        drawOval(Color(0xFF9BE3C9), topLeft = Offset((59f - 15f) * s, (58f - 17f) * s), size = androidx.compose.ui.geometry.Size(30f * s, 34f * s))

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
            pts.forEachIndexed { pointIndex, point ->
                if (pointIndex == 0) moveTo(point.x * s, point.y * s) else lineTo(point.x * s, point.y * s)
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
            Path().apply {
                moveTo(46f * s, 62f * s)
                cubicTo(48f * s, 80f * s, 58f * s, 90f * s, 60f * s, 110f * s)
            },
            Path().apply {
                moveTo(76f * s, 58f * s)
                cubicTo(74f * s, 78f * s, 62f * s, 90f * s, 60f * s, 110f * s)
            },
            Path().apply {
                moveTo(60f * s, 78f * s)
                cubicTo(60f * s, 92f * s, 60f * s, 100f * s, 60f * s, 110f * s)
            },
        )
        strings.forEach { stringPath -> drawPath(stringPath, Color(0x806E5A72), style = Stroke(width = 1.4f * s)) }
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
        small.forEach { (point, color) ->
            drawCircle(color, radius = 6.5f * s, center = Offset(point.x * s, point.y * s))
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
        large.forEach { (point, color) ->
            drawCircle(color, radius = 10.5f * s, center = Offset(point.x * s, point.y * s))
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
    onAddClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val typo = LocalRfTypography.current
    val palette = LocalRfPalette.current
    val gradDiag = Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9)))
    var favoriteState by remember(title, isFavorite) { mutableStateOf(isFavorite) }

    Box(
        modifier = modifier
            .rfCardShadow(RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(palette.surface)
            .border(1.dp, palette.line, RoundedCornerShape(20.dp)),
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
                        .background(Color.White.copy(alpha = 0.95f))
                        .clickable { favoriteState = !favoriteState },
                ) {
                    RfIcon(
                        kind = RfIconKind.HEART,
                        tint = if (favoriteState) Color(0xFFEA4EA9) else Color(0xFF1A1A2E),
                        filled = favoriteState,
                        size = 18.dp,
                    )
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
                            color = RfThemeState.currentPalette.ink,
                        ),
                        maxLines = 1,
                    )
                    BasicText(
                        text = price,
                        style = TextStyle(
                            fontFamily = typo.displayFamily,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = RfThemeState.currentPalette.ink,
                        ),
                    )
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(gradDiag)
                        .clickable(onClick = onAddClick),
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
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val palette = LocalRfPalette.current
    val allArticles = viewModel?.articles?.collectAsState()?.value ?: CanonicalSampleData.articles
    val favoriteIds = viewModel?.favorites?.collectAsState()?.value ?: setOf("art-silla-tiffany", "art-mesa-redonda")
    val favoriteArticles = remember(allArticles, favoriteIds, viewModel) {
        val matched = allArticles.filter { article -> favoriteIds.contains(article.id) }
        val shouldUseFallback = viewModel == null
        if (matched.isNotEmpty()) {
            matched
        } else if (shouldUseFallback) {
            allArticles.take(2)
        } else {
            emptyList()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 116.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            BasicText("Favoritos", style = typo.heroTitle.copy(fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = palette.ink))
            BasicText("${favoriteArticles.size} artículos guardados", style = typo.secondary.copy(fontSize = 15.sp, color = palette.muted))
        }

        Spacer(Modifier.height(18.dp))

        if (favoriteArticles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(palette.surface)
                    .border(1.dp, palette.line, RoundedCornerShape(20.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    RfIcon(kind = RfIconKind.HEART, tint = Color(0xFFEA4EA9), filled = false, size = 32.dp)
                    BasicText(
                        "Aún no tienes favoritos guardados",
                        style = typo.body.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = palette.ink),
                    )
                    BasicText(
                        "Explora el catálogo y toca el corazón en los artículos que te gusten.",
                        style = typo.secondary.copy(fontSize = 14.sp, color = palette.muted),
                    )
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .height(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF865DEC))))
                            .clickable { onSelectScreenCode("s18") }
                            .padding(horizontal = 20.dp),
                    ) {
                        BasicText("Explorar catálogo", style = typo.body.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White))
                    }
                }
            }
        }

        favoriteArticles.chunked(2).forEachIndexed { rowIdx, pair ->
            if (rowIdx > 0) {
                Spacer(Modifier.height(12.dp))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                val first = pair[0]
                val firstPhoto = RfPhotoKey.entries.firstOrNull { entry ->
                    val matchesAsset = entry.assetKey == first.photo.key
                    val matchesName = entry.name.equals(first.photo.key, ignoreCase = true)
                    listOf(matchesAsset, matchesName).any { matched -> matched }
                } ?: RfPhotoKey.SILLA_TIFFANY
                val isFirstFav = favoriteIds.contains(first.id)
                androidx.compose.runtime.key(first.id) {
                    PersonalProductCard(
                        title = first.name,
                        desc = first.description,
                        price = first.pricePerDayDop.toString(),
                        photoKey = firstPhoto,
                        bg = Color(0xFFDDF4F3),
                        initialFavorite = isFirstFav,
                        onOpenDetail = {
                            viewModel?.selectArticle(first.id)
                            onSelectScreenCode("s19")
                        },
                        onAddClick = {
                            viewModel?.adjustDraftQuantity(first.id, 1)
                        },
                        onToggleFavorite = {
                            viewModel?.toggleFavorite(first.id)
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (pair.size > 1) {
                    val second = pair[1]
                    val secondPhoto = RfPhotoKey.entries.firstOrNull { entry ->
                        val matchesAsset = entry.assetKey == second.photo.key
                        val matchesName = entry.name.equals(second.photo.key, ignoreCase = true)
                        listOf(matchesAsset, matchesName).any { matched -> matched }
                    } ?: RfPhotoKey.MESA_REDONDA
                    val isSecondFav = favoriteIds.contains(second.id)
                    androidx.compose.runtime.key(second.id) {
                        PersonalProductCard(
                            title = second.name,
                            desc = second.description,
                            price = second.pricePerDayDop.toString(),
                            photoKey = secondPhoto,
                            bg = Color(0xFFFFF2C9),
                            initialFavorite = isSecondFav,
                            onOpenDetail = {
                                viewModel?.selectArticle(second.id)
                                onSelectScreenCode("s19")
                            },
                            onAddClick = {
                                viewModel?.adjustDraftQuantity(second.id, 1)
                            },
                            onToggleFavorite = {
                                viewModel?.toggleFavorite(second.id)
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                } else {
                    Spacer(Modifier.weight(1f))
                }
            }
        }

        Spacer(Modifier.height(20.dp))

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
                        BasicText("¿Los agrego a tu boda?", style = typo.body.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink))
                        BasicText("Los artículos seleccionados quedarían en “Boda Real Rosa & Dorado”.", style = typo.secondary.copy(fontSize = 14.sp, lineHeight = 20.sp, color = RfThemeState.currentPalette.muted))
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
                            .clickable {
                                favoriteArticles.forEach { favArticle ->
                                    viewModel?.adjustDraftQuantity(favArticle.id, 1)
                                }
                                onSelectScreenCode("s25")
                            },
                    ) {
                        BasicText("Agregar ambos", style = typo.body.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White))
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .height(46.dp)
                            .rfCardShadow(RoundedCornerShape(14.dp), isAdmin = false)
                            .clip(RoundedCornerShape(14.dp))
                            .background(RfThemeState.currentPalette.surface)
                            .clickable { onSelectScreenCode("s37") }
                            .padding(horizontal = 16.dp),
                    ) {
                        BasicText("Elegir evento", style = typo.body.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink))
                    }
                }
            }
        }
    }
}
