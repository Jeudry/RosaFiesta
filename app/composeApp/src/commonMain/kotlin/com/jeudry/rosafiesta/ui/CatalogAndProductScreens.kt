package com.jeudry.rosafiesta.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.jeudry.rosafiesta.core.engine.RosaFiestaEngine
import com.jeudry.rosafiesta.designsystem.LocalRfPalette
import com.jeudry.rosafiesta.designsystem.LocalRfTypography
import com.jeudry.rosafiesta.designsystem.RfDimens
import com.jeudry.rosafiesta.designsystem.RfGradientPriceText
import com.jeudry.rosafiesta.designsystem.RfIcon
import com.jeudry.rosafiesta.designsystem.RfIconKind
import com.jeudry.rosafiesta.designsystem.RfPhoto
import com.jeudry.rosafiesta.designsystem.RfPhotoKey
import com.jeudry.rosafiesta.designsystem.RfThemeState
import com.jeudry.rosafiesta.designsystem.RosaAvatar
import com.jeudry.rosafiesta.designsystem.RosaAvatarSize
import com.jeudry.rosafiesta.designsystem.rfCardShadow
import com.jeudry.rosafiesta.designsystem.rfHeroBannerShadow
import com.jeudry.rosafiesta.designsystem.rfIconButtonShadow
import com.jeudry.rosafiesta.designsystem.rfInputShadow
import com.jeudry.rosafiesta.designsystem.rfPrimaryButtonShadow
import com.jeudry.rosafiesta.domain.BalloonProduct
import com.jeudry.rosafiesta.domain.CanonicalSampleData
import com.jeudry.rosafiesta.domain.CatalogArticle
import com.jeudry.rosafiesta.domain.EventPackage
import com.jeudry.rosafiesta.domain.validation.InputValidator
import kotlinx.coroutines.delay

private fun resolveCatalogCategoryIcon18(categoryName: String): RfIconKind {
    val clean = categoryName.trim().lowercase()
    return when {
        clean.startsWith("todas") -> RfIconKind.GRID
        clean.contains("mobiliario") -> RfIconKind.CHAIR
        clean.contains("decora") -> RfIconKind.SPARKLE
        clean.contains("floral") -> RfIconKind.HEART
        clean.contains("arco") -> RfIconKind.PARTY
        clean.contains("globo") -> RfIconKind.PARTY
        clean.contains("mantel") -> RfIconKind.BOX
        clean.contains("dulce") -> RfIconKind.CAKE
        clean.contains("ilumin") -> RfIconKind.LIGHTBULB
        clean.contains("letrero") -> RfIconKind.NOTE
        else -> RfIconKind.CATALOG
    }
}

@Composable
private fun CatalogCategoryChip18(
    label: String,
    cleanCategory: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val typo = LocalRfTypography.current
    val contentTint = if (isSelected) Color.White else RfThemeState.currentPalette.muted
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
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
            .border(1.dp, if (isSelected) Color.Transparent else RfThemeState.currentPalette.line, RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp),
    ) {
        RfIcon(
            kind = resolveCatalogCategoryIcon18(cleanCategory),
            tint = contentTint,
            size = 15.dp,
        )
        BasicText(
            text = label,
            maxLines = 1,
            style = typo.body.copy(
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = contentTint,
            ),
        )
    }
}

private fun isUnfilteredCatalogCategory18(category: String): Boolean {
    val clean = category.trim()
    val isEmptyCategory = clean.isEmpty()
    val isTodasPrefix = clean.startsWith("Todas", ignoreCase = true)
    val isTodosPrefix = clean.startsWith("Todos", ignoreCase = true)
    return listOf(isEmptyCategory, isTodasPrefix, isTodosPrefix).any { matched -> matched }
}

@Composable
internal fun RenderPersonalCatalogS18(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit,
    includeHeader: Boolean = true,
) {
    val typo = LocalRfTypography.current
    val articlesState = viewModel?.articles?.collectAsState()?.value ?: CanonicalSampleData.articles
    val draftQuantitiesState = viewModel?.draftQuantities?.collectAsState()?.value ?: emptyMap()
    val totalDraftCount = remember(draftQuantitiesState, viewModel) {
        viewModel?.totalDraftCartUnits() ?: draftQuantitiesState.values.sum()
    }
    val vmCategory = viewModel?.selectedCategory?.collectAsState()?.value ?: "Todas"
    var selectedCategory by remember(vmCategory) {
        val normalizedInitial = if (isUnfilteredCatalogCategory18(vmCategory)) "Todas" else vmCategory
        mutableStateOf(normalizedInitial)
    }
    val vmQuery = viewModel?.searchQuery?.collectAsState()?.value ?: ""
    var searchQuery by remember(vmQuery) { mutableStateOf(vmQuery.take(InputValidator.MAX_SEARCH_QUERY_LENGTH)) }
    var hasInteractedSearch by remember { mutableStateOf(false) }
    val searchFieldError = rememberDebouncedValidationError(
        value = searchQuery,
        hasInteracted = hasInteractedSearch,
    ) { query ->
        InputValidator.validateSearchQuery(query)
    }
    val observedMaxPrice = viewModel?.maxPriceFilterDop?.collectAsState()?.value ?: Int.MAX_VALUE
    val observedOnlyAvailable = viewModel?.onlyAvailableFilter?.collectAsState()?.value ?: false
    val observedSortOrder = viewModel?.searchSortOrder?.collectAsState()?.value ?: "Más populares"
    val favoritesSet = viewModel?.favorites?.collectAsState()?.value ?: emptySet()
    val isInlineVoiceListening = viewModel?.isVoiceListening?.collectAsState()?.value ?: false
    val voiceOrigin = viewModel?.voiceInputOrigin?.collectAsState()?.value ?: VoiceInputOrigin.NONE
    val isSearchMicActive = if (isInlineVoiceListening) voiceOrigin == VoiceInputOrigin.SEARCH else false
    val isCategoryFiltered = !isUnfilteredCatalogCategory18(selectedCategory)
    val isPriceFiltered = observedMaxPrice < 5000
    val activeFilterCount = listOf(isCategoryFiltered, isPriceFiltered, observedOnlyAvailable).count { active -> active }

    val visibleArticlesCount = remember(articlesState) {
        articlesState.count { article -> article.visibleInCatalog }
    }

    val filteredArticles: List<CatalogArticle> = remember(
        articlesState,
        selectedCategory,
        searchQuery,
        observedMaxPrice,
        observedOnlyAvailable,
        observedSortOrder,
        viewModel,
    ) {
        if (viewModel != null) {
            viewModel.filteredCatalogArticles(
                categoryOverride = selectedCategory,
                queryOverride = searchQuery,
            )
        } else {
            articlesState.filter { article ->
                val isVisible = article.visibleInCatalog
                val isAllSelected = isUnfilteredCatalogCategory18(selectedCategory)
                val matchesCategoryName = article.category.equals(selectedCategory, ignoreCase = true)
                val matchesCategory = listOf(isAllSelected, matchesCategoryName).any { matched -> matched }
                val queryBlank = searchQuery.isBlank()
                val matchesName = article.name.contains(searchQuery, ignoreCase = true)
                val matchesDescription = article.description.contains(searchQuery, ignoreCase = true)
                val matchesSearch = listOf(queryBlank, matchesName, matchesDescription).any { matched -> matched }
                listOf(isVisible, matchesCategory, matchesSearch).all { matched -> matched }
            }
        }
    }

    val catalogTopPadding = if (includeHeader) 16.dp else 6.dp
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = catalogTopPadding, start = 20.dp, end = 20.dp),
    ) {
        if (includeHeader) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                BasicText(
                    text = "Catálogo",
                    style = typo.heroTitle.copy(fontSize = 26.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink),
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(44.dp)
                            .rfIconButtonShadow(CircleShape, isAdmin = false)
                            .clip(CircleShape)
                            .background(RfThemeState.currentPalette.surface)
                            .border(1.dp, RfThemeState.currentPalette.line, CircleShape)
                            .clickable { onSelectScreenCode("s20") },
                    ) {
                        RfIcon(kind = RfIconKind.GRID, tint = RfThemeState.currentPalette.ink, size = 20.dp)
                    }

                    RfDraftCartPillButton(
                        count = totalDraftCount,
                        onClick = { onSelectScreenCode("s25") },
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            val searchBorderColor = if (searchFieldError != null) Color(0xFFE53935) else RfThemeState.currentPalette.line
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .rfInputShadow(RoundedCornerShape(16.dp), isAdmin = false)
                    .clip(RoundedCornerShape(16.dp))
                    .background(RfThemeState.currentPalette.surface)
                    .border(1.dp, searchBorderColor, RoundedCornerShape(16.dp))
                    .padding(start = 16.dp, end = 6.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        RfIcon(kind = RfIconKind.SEARCH, tint = RfThemeState.currentPalette.muted, size = 20.dp)
                        Box(modifier = Modifier.weight(1f)) {
                            if (searchQuery.isEmpty()) {
                                BasicText("Buscar sillas, mesas, flores…", style = typo.body.copy(fontSize = 15.sp, color = RfThemeState.currentPalette.muted))
                            }
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { updatedQuery ->
                                    val cappedQuery = updatedQuery.take(InputValidator.MAX_SEARCH_QUERY_LENGTH)
                                    searchQuery = cappedQuery
                                    hasInteractedSearch = true
                                    viewModel?.setSearchQuery(cappedQuery)
                                },
                                textStyle = typo.body.copy(fontSize = 15.sp, color = RfThemeState.currentPalette.ink),
                                cursorBrush = SolidColor(RfThemeState.currentPalette.primaryAccent),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                            )
                        }
                    }
                    if (searchQuery.isNotEmpty()) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF5EAF9))
                                .clickable {
                                    searchQuery = ""
                                    hasInteractedSearch = false
                                    viewModel?.setSearchQuery("")
                                },
                        ) {
                            RfIcon(kind = RfIconKind.CLOSE, tint = Color(0xFFB54BD6), size = 14.dp)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clickable { onSelectScreenCode("s81") },
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF5EAF9)),
                        ) {
                            RfIcon(kind = RfIconKind.FILTER, tint = Color(0xFFB54BD6), size = 18.dp)
                        }
                        if (activeFilterCount > 0) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 3.dp, y = (-3).dp)
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFD45C)),
                            ) {
                                BasicText(
                                    text = activeFilterCount.toString(),
                                    style = typo.sectionCap.copy(fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2E1A47)),
                                )
                            }
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
            RfValidationErrorText(
                errorMessage = searchFieldError,
                modifier = Modifier.padding(top = 4.dp),
            )

            InlineVoiceSearchBanner(
                viewModel = viewModel,
                onApplyVoiceQuery = { spokenQuery ->
                    val cappedSpoken = spokenQuery.take(InputValidator.MAX_SEARCH_QUERY_LENGTH)
                    searchQuery = cappedSpoken
                    hasInteractedSearch = true
                    viewModel?.setSearchQuery(cappedSpoken)
                },
                modifier = Modifier.padding(top = 10.dp),
            )

            Spacer(Modifier.height(12.dp))
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            val scrollableCategoriesScrollState = rememberScrollState()
            LaunchedEffect(Unit) {
                for (cycle in 0 until 240) {
                    delay(2800L)
                    val maxScroll = scrollableCategoriesScrollState.maxValue
                    val hasScrollRange = maxScroll > 0
                    val notBeingDragged = !scrollableCategoriesScrollState.isScrollInProgress
                    val canAdvanceCarousel = if (hasScrollRange) notBeingDragged else false
                    if (canAdvanceCarousel) {
                        val stepPx = (maxScroll / 4).coerceAtLeast(150)
                        val atEndOfStrip = scrollableCategoriesScrollState.value + 36 >= maxScroll
                        val targetScroll = if (atEndOfStrip) {
                            0
                        } else {
                            (scrollableCategoriesScrollState.value + stepPx).coerceAtMost(maxScroll)
                        }
                        scrollableCategoriesScrollState.animateScrollTo(
                            value = targetScroll,
                            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
                        )
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                val vmCategories = viewModel?.catalogCategories?.collectAsState()?.value
                val baseCategoryChips = if (vmCategories != null) {
                    vmCategories.map { category -> category.name.trim() }
                        .filter { name -> name.isNotEmpty() }
                        .distinctBy { name -> name.lowercase() }
                } else {
                    listOf(
                        "Mobiliario",
                        "Decoración",
                        "Centros florales",
                        "Arcos y globos",
                        "Mantelería",
                        "Estaciones dulces",
                        "Iluminación",
                        "Letreros",
                    )
                }
                val isAllCategoryActive = isUnfilteredCatalogCategory18(selectedCategory)
                val isKnownChip = baseCategoryChips.any { chipName -> chipName.equals(selectedCategory, ignoreCase = true) }
                val shouldInjectSelectedChip = if (isAllCategoryActive) false else !isKnownChip
                val scrollingCategories = if (shouldInjectSelectedChip) {
                    listOf(selectedCategory) + baseCategoryChips
                } else {
                    baseCategoryChips
                }

                CatalogCategoryChip18(
                    label = "Todas · $visibleArticlesCount",
                    cleanCategory = "Todas",
                    isSelected = isAllCategoryActive,
                    onClick = {
                        viewModel?.soundPlayer?.playTap()
                        selectedCategory = "Todas"
                        viewModel?.setSelectedCategory("Todas")
                    },
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(scrollableCategoriesScrollState),
                ) {
                    scrollingCategories.forEach { categoryLabel ->
                        val isSelected = selectedCategory.equals(categoryLabel, ignoreCase = true)
                        CatalogCategoryChip18(
                            label = categoryLabel,
                            cleanCategory = categoryLabel,
                            isSelected = isSelected,
                            onClick = {
                                viewModel?.soundPlayer?.playTap()
                                selectedCategory = categoryLabel
                                viewModel?.setSelectedCategory(categoryLabel)
                            },
                        )
                    }
                }
            }

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
            if (filteredArticles.isEmpty()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = false)
                        .clip(RoundedCornerShape(20.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp))
                        .padding(horizontal = 20.dp, vertical = 28.dp),
                ) {
                    RfIcon(kind = RfIconKind.SEARCH, tint = RfThemeState.currentPalette.muted, size = 28.dp)
                    BasicText(
                        text = "No hay artículos que coincidan con tus filtros",
                        style = typo.heroTitle.copy(
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = RfThemeState.currentPalette.ink,
                            textAlign = TextAlign.Center,
                        ),
                    )
                    BasicText(
                        text = "Restablece la categoría, el precio o la búsqueda para explorar todo el catálogo.",
                        style = typo.secondary.copy(
                            fontSize = 13.5.sp,
                            color = RfThemeState.currentPalette.muted,
                            textAlign = TextAlign.Center,
                        ),
                    )
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .height(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Brush.horizontalGradient(listOf(Color(0xFFE04FAF), Color(0xFF865DEC))))
                            .clickable {
                                selectedCategory = "Todas"
                                searchQuery = ""
                                viewModel?.resetCatalogFilters()
                                viewModel?.setSearchQuery("")
                            }
                            .padding(horizontal = 18.dp),
                    ) {
                        BasicText(
                            text = "Restablecer filtros",
                            style = typo.body.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White),
                        )
                    }
                }
            } else {
                filteredArticles.chunked(2).forEach { rowPair ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        val first = rowPair[0]
                        val firstPhoto = first.photo.toPhotoKey()
                        PersonalProductCard(
                            title = first.name,
                            desc = first.description,
                            price = first.pricePerDayDop.toString(),
                            photoKey = firstPhoto,
                            photoUrl = first.photoUrl,
                            viewModel = viewModel,
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
                            val secondPhoto = second.photo.toPhotoKey()
                            PersonalProductCard(
                                title = second.name,
                                desc = second.description,
                                price = second.pricePerDayDop.toString(),
                                photoKey = secondPhoto,
                                photoUrl = second.photoUrl,
                                viewModel = viewModel,
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
            }
            Spacer(Modifier.height(RfDimens.BottomBarContentClearance))
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
    photoUrl: String = "",
    viewModel: RosaFiestaViewModel? = null,
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
                    .height(100.dp)
                    .background(bg),
            ) {
                RfUploadedOrCatalogPhoto(
                    photoUrl = photoUrl,
                    fallbackPhotoKey = photoKey,
                    contentDescription = title,
                    viewModel = viewModel,
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
                    Row {
                        if (useGradientPrice) {
                            RfGradientPriceText(
                                text = price,
                                style = TextStyle(fontFamily = typo.displayFamily, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold),
                                modifier = Modifier.alignByBaseline(),
                            )
                            Spacer(Modifier.width(3.dp))
                            BasicText(
                                text = "RD$/día",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB54BD6)),
                                modifier = Modifier.alignByBaseline(),
                            )
                        } else {
                            BasicText(
                                "RD$ $price",
                                style = typo.heroTitle.copy(
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = RfThemeState.currentPalette.ink,
                                ),
                                modifier = Modifier.alignByBaseline(),
                            )
                            BasicText(
                                " /día",
                                style = typo.secondary.copy(fontSize = 12.sp, color = RfThemeState.currentPalette.muted),
                                modifier = Modifier.alignByBaseline(),
                            )
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
    val currentArticle = articlesState.firstOrNull { article -> article.id == selectedArticleId } ?: articlesState.firstOrNull()
    if (currentArticle == null) {
        RenderPersonalEmptyCatalogDetail(
            headerTitle = "Detalle de artículo",
            emptyTitle = "Producto no disponible",
            emptyMessage = "El catálogo aún no tiene artículos publicados.",
            backTargetCode = "BACK:s18",
            onSelectScreenCode = onSelectScreenCode,
        )
        return
    }
    val draftQuantities = viewModel?.draftQuantities?.collectAsState()?.value ?: emptyMap()
    val isOutOfStock = currentArticle.availableOct23 <= 0
    val maxAvailableUnits = currentArticle.availableOct23.coerceAtLeast(1)
    val initialDraftCount = draftQuantities[currentArticle.id] ?: 1
    val initialCount = initialDraftCount.coerceIn(1, maxAvailableUnits)

    val persistedVariants = viewModel?.selectedArticleVariantColors?.collectAsState()?.value ?: emptyMap()
    val variantOptions = remember(currentArticle.id, currentArticle.variants) {
        currentArticle.variants.filter { variant -> variant.isNotBlank() }
    }
    val persistedVariantForArticle = persistedVariants[currentArticle.id]
    var selectedColor by remember(currentArticle.id, persistedVariantForArticle, variantOptions) {
        mutableStateOf(persistedVariantForArticle ?: variantOptions.firstOrNull().orEmpty())
    }
    var count by remember(currentArticle.id, maxAvailableUnits) { mutableStateOf(initialCount) }
    var selectedTab by remember { mutableStateOf("Descripción") }

    val companyReviews = viewModel?.companyReviews?.collectAsState()?.value.orEmpty()
    val hasRealReviews = companyReviews.isNotEmpty()
    val averageRatingLabel = if (hasRealReviews) {
        viewModel?.companyAverageRatingFormatted()
    } else {
        null
    }
    val totalReviewCount = companyReviews.size

    val primaryPhoto = currentArticle.photo.toPhotoKey()
    val galleryMedia = remember(currentArticle.id, currentArticle.photoUrl, primaryPhoto, articlesState, currentArticle.category) {
        val cleanPrimaryUrl = currentArticle.photoUrl.trim()
        if (cleanPrimaryUrl.isNotEmpty()) {
            listOf(cleanPrimaryUrl to primaryPhoto)
        } else {
            val relatedPhotos = articlesState
                .filter { candidate -> candidate.category.equals(currentArticle.category, ignoreCase = true) }
                .map { candidate -> candidate.photo.toPhotoKey() }
            val fallbackPhotos = articlesState.map { candidate -> candidate.photo.toPhotoKey() }
            (listOf(primaryPhoto) + relatedPhotos + fallbackPhotos)
                .distinct()
                .take(3)
                .map { key -> "" to key }
        }
    }
    var selectedPhotoIndex by remember(currentArticle.id) { mutableStateOf(0) }
    var isPhotoExpanded by remember(currentArticle.id) { mutableStateOf(false) }
    var showFullScreenPhoto by remember(currentArticle.id) { mutableStateOf(false) }
    val activeGalleryMedia = galleryMedia.getOrElse(selectedPhotoIndex) { galleryMedia.first() }
    val sheetScrollState = rememberScrollState()
    val parallaxOffsetDp = (-sheetScrollState.value / 6).dp

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(RfThemeState.currentPalette.bg),
    ) {
        val targetHeroSpacerHeight = if (isPhotoExpanded) {
            (maxHeight - 152.dp).coerceAtLeast(340.dp)
        } else {
            236.dp
        }
        val animatedHeroSpacerHeight by animateDpAsState(
            targetValue = targetHeroSpacerHeight,
            animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
            label = "s19-hero-expand",
        )
        val animatedHeroPhotoHeight = animatedHeroSpacerHeight + 60.dp

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(animatedHeroPhotoHeight)
                .background(if (isPhotoExpanded) Color(0xFF141522) else Color.Transparent)
                .offset(y = if (isPhotoExpanded) 0.dp else parallaxOffsetDp),
        ) {
            RfUploadedOrCatalogPhoto(
                photoUrl = activeGalleryMedia.first,
                fallbackPhotoKey = activeGalleryMedia.second,
                contentDescription = currentArticle.name,
                viewModel = viewModel,
                cornerRadius = 0.dp,
                contentScale = if (isPhotoExpanded) ContentScale.Fit else ContentScale.Crop,
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
                    .height(animatedHeroSpacerHeight)
                    .clickable { isPhotoExpanded = !isPhotoExpanded },
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 20.dp, bottom = 14.dp),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0x881A1A2E))
                            .clickable { showFullScreenPhoto = true },
                    ) {
                        Canvas(modifier = Modifier.size(16.dp)) {
                            val strokeWidth = 2.dp.toPx()
                            val cornerLen = size.width * 0.34f
                            val w = size.width
                            val h = size.height
                            drawLine(Color.White, Offset(0f, cornerLen), Offset(0f, 0f), strokeWidth, StrokeCap.Round)
                            drawLine(Color.White, Offset(0f, 0f), Offset(cornerLen, 0f), strokeWidth, StrokeCap.Round)
                            drawLine(Color.White, Offset(w - cornerLen, 0f), Offset(w, 0f), strokeWidth, StrokeCap.Round)
                            drawLine(Color.White, Offset(w, 0f), Offset(w, cornerLen), strokeWidth, StrokeCap.Round)
                            drawLine(Color.White, Offset(0f, h - cornerLen), Offset(0f, h), strokeWidth, StrokeCap.Round)
                            drawLine(Color.White, Offset(0f, h), Offset(cornerLen, h), strokeWidth, StrokeCap.Round)
                            drawLine(Color.White, Offset(w - cornerLen, h), Offset(w, h), strokeWidth, StrokeCap.Round)
                            drawLine(Color.White, Offset(w, h), Offset(w, h - cornerLen), strokeWidth, StrokeCap.Round)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color(0x881A1A2E))
                            .clickable { isPhotoExpanded = !isPhotoExpanded }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    ) {
                        BasicText(
                            text = if (isPhotoExpanded) "Ver detalles" else "Ver foto",
                            style = typo.caption.copy(fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White),
                        )
                    }
                }
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
                    galleryMedia.forEachIndexed { photoIndex, mediaEntry ->
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
                            RfUploadedOrCatalogPhoto(
                                photoUrl = mediaEntry.first,
                                fallbackPhotoKey = mediaEntry.second,
                                contentDescription = "Vista ${photoIndex + 1}",
                                viewModel = viewModel,
                                cornerRadius = 12.dp,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }
                    if (galleryMedia.size > 1) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(RfThemeState.currentPalette.surface)
                                .clickable { selectedPhotoIndex = (selectedPhotoIndex + 1) % galleryMedia.size },
                        ) {
                            BasicText("+${galleryMedia.size}", style = typo.body.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.muted))
                        }
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
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isPhotoExpanded = !isPhotoExpanded }
                        .pointerInput(Unit) {
                            detectVerticalDragGestures { _, dragAmount ->
                                if (dragAmount > 12f) {
                                    isPhotoExpanded = true
                                } else if (dragAmount < -12f) {
                                    isPhotoExpanded = false
                                }
                            }
                        }
                        .padding(vertical = 4.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .width(42.dp)
                            .height(5.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color(0xFFE3DCE8)),
                    )
                }

                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        BasicText(
                            currentArticle.name,
                            style = typo.heroTitle.copy(fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = RfThemeState.currentPalette.ink),
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
                                    .clickable { onSelectScreenCode("s17") }
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (averageRatingLabel != null) {
                                        BasicText("★ $averageRatingLabel", style = typo.body.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7A5700)))
                                        BasicText("$totalReviewCount reseñas", style = typo.secondary.copy(fontSize = 13.sp, color = Color(0xFF7A5700)))
                                    } else {
                                        BasicText("Sin reseñas", style = typo.secondary.copy(fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7A5700)))
                                    }
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(Color(0xFFEEF6D6))
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                            ) {
                                BasicText(
                                    "${currentArticle.availableOct23} disp.",
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

                if (variantOptions.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row {
                            BasicText("Variante: ", style = typo.body.copy(fontSize = 14.sp, color = RfThemeState.currentPalette.muted))
                            BasicText(selectedColor, style = typo.body.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink))
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                        ) {
                            variantOptions.forEachIndexed { variantIndex, variantLabel ->
                                val isSel = variantLabel == selectedColor
                                val accentColor = when (variantIndex % 4) {
                                    0 -> Color(0xFF7C5CF0)
                                    1 -> Color(0xFFEA4EA9)
                                    2 -> Color(0xFFA9CC3A)
                                    else -> Color(0xFFF0B92E)
                                }
                                val gradDiag = Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9)))
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .height(38.dp)
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(
                                            if (isSel) {
                                                gradDiag
                                            } else {
                                                SolidColor(RfThemeState.currentPalette.chipBg)
                                            },
                                        )
                                        .border(
                                            width = if (isSel) 0.dp else 1.5.dp,
                                            color = if (isSel) Color.Transparent else accentColor.copy(alpha = 0.65f),
                                            shape = RoundedCornerShape(999.dp),
                                        )
                                        .clickable {
                                            selectedColor = variantLabel
                                            viewModel?.setArticleColorVariant(currentArticle.id, variantLabel)
                                        }
                                        .padding(horizontal = 14.dp),
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        if (isSel) {
                                            RfIcon(kind = RfIconKind.CHECK, tint = Color.White, size = 14.dp)
                                        } else {
                                            Box(Modifier.size(8.dp).clip(CircleShape).background(accentColor))
                                        }
                                        BasicText(
                                            text = variantLabel,
                                            style = typo.body.copy(
                                                fontSize = 13.5.sp,
                                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSel) Color.White else RfThemeState.currentPalette.ink,
                                            ),
                                        )
                                    }
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
                                        color = if (isSel) Color.White else RfThemeState.currentPalette.muted,
                                    ),
                                )
                            }
                        }
                    }
                }

                when (selectedTab) {
                    "Detalles" -> {
                        val detailRows = buildList {
                            add("Código de catálogo" to currentArticle.code)
                            add("Categoría" to currentArticle.category)
                            add("Disponibilidad" to "${currentArticle.availableOct23} / ${currentArticle.totalStock}")
                            if (currentArticle.material.isNotBlank()) {
                                add("Material y acabado" to currentArticle.material)
                            }
                            if (currentArticle.dimensions.isNotBlank()) {
                                add("Dimensiones" to currentArticle.dimensions)
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            detailRows.forEach { (specLabel, specValue) ->
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(RfThemeState.currentPalette.chipBg)
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                ) {
                                    BasicText(specLabel, style = typo.body.copy(fontSize = 13.5.sp, color = RfThemeState.currentPalette.muted), modifier = Modifier.weight(0.45f))
                                    BasicText(
                                        specValue,
                                        style = typo.body.copy(fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink, textAlign = TextAlign.End),
                                        modifier = Modifier.weight(0.55f),
                                    )
                                }
                            }
                        }
                    }

                    "Reseñas" -> {
                        val displayReviews: List<Triple<String, String, String>> = companyReviews.take(4).map { reviewModel ->
                            val starsLabel = "★".repeat(reviewModel.rating.coerceIn(1, 5))
                            Triple(reviewModel.author, "$starsLabel · ${formatReadableSpanishDateText(reviewModel.dateLabel)}", reviewModel.comment)
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            if (displayReviews.isEmpty()) {
                                BasicText(
                                    text = "Aún no hay reseñas publicadas.",
                                    style = typo.body.copy(fontSize = 13.5.sp, color = RfThemeState.currentPalette.muted),
                                )
                            } else {
                                displayReviews.forEach { (authorName, ratingMeta, reviewComment) ->
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(RfThemeState.currentPalette.chipBg)
                                            .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(14.dp))
                                            .padding(14.dp),
                                    ) {
                                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                            BasicText(authorName, style = typo.body.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink))
                                            BasicText(ratingMeta, style = typo.caption.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7A5700)))
                                        }
                                        BasicText(reviewComment, style = typo.body.copy(fontSize = 13.5.sp, color = RfThemeState.currentPalette.muted, lineHeight = 19.sp))
                                    }
                                }
                            }
                            val canWriteReview = viewModel?.hasCompletedOrUsedRental() ?: false
                            val hasReviews = totalReviewCount > 0
                            val showReviewAction = hasReviews || canWriteReview
                            if (showReviewAction) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFFDE6F2))
                                        .clickable { onSelectScreenCode("s17") },
                                ) {
                                    BasicText(
                                        text = if (hasReviews) "Ver todas las reseñas ($totalReviewCount) →" else "Escribir reseña →",
                                        style = typo.body.copy(fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC2338F)),
                                    )
                                }
                            } else {
                                BasicText(
                                    text = "Solo puedes escribir una reseña después de alquilar o usar este producto.",
                                    style = typo.caption.copy(fontSize = 12.sp, color = RfThemeState.currentPalette.muted),
                                )
                            }
                        }
                    }

                    else -> {
                        BasicText(
                            text = currentArticle.description,
                            style = typo.body.copy(fontSize = 15.sp, color = RfThemeState.currentPalette.muted, lineHeight = 22.sp),
                        )
                        val summaryLines = buildList {
                            add("• Código: ${currentArticle.code}")
                            if (selectedColor.isNotBlank()) {
                                add("• Variante seleccionada: $selectedColor")
                            }
                            if (currentArticle.material.isNotBlank()) {
                                add("• Material: ${currentArticle.material}")
                            }
                            if (currentArticle.dimensions.isNotBlank()) {
                                add("• Dimensiones: ${currentArticle.dimensions}")
                            }
                        }.joinToString("\n")
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(RfThemeState.currentPalette.chipBg)
                                .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(16.dp))
                                .padding(14.dp),
                        ) {
                            BasicText(
                                "Especificaciones destacadas",
                                style = typo.body.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink),
                            )
                            BasicText(
                                summaryLines,
                                style = typo.body.copy(fontSize = 13.5.sp, color = RfThemeState.currentPalette.muted, lineHeight = 20.sp),
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
                    .size(RfDimens.IconButtonSize)
                    .rfIconButtonShadow(CircleShape, isAdmin = false)
                    .clip(CircleShape)
                    .background(RfThemeState.currentPalette.surface)
                    .clickable { onSelectScreenCode("BACK:s18") },
            ) {
                RfIcon(kind = RfIconKind.BACK, tint = RfThemeState.currentPalette.ink, size = 20.dp)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(RfDimens.IconButtonSize)
                        .rfIconButtonShadow(CircleShape, isAdmin = false)
                        .clip(CircleShape)
                        .background(RfThemeState.currentPalette.surface)
                        .clickable {
                            viewModel?.sharePlainText(
                                title = currentArticle.name,
                                text = "${currentArticle.name} (${currentArticle.code}) · RD$ ${currentArticle.pricePerDayDop}/día — ${currentArticle.description}",
                            )
                        },
                ) {
                    RfIcon(kind = RfIconKind.SHARE, tint = RfThemeState.currentPalette.ink, size = 20.dp)
                }
                val favoritesSet = viewModel?.favorites?.collectAsState()?.value
                val isArticleFavorite = favoritesSet?.contains(currentArticle.id) ?: false
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(RfDimens.IconButtonSize)
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
                        tint = if (isArticleFavorite) Color(0xFFEA4EA9) else RfThemeState.currentPalette.ink,
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
                                .clickable { count = (count - 1).coerceIn(1, maxAvailableUnits) },
                        ) {
                            BasicText("-", style = typo.heroTitle.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF865DEC)))
                        }
                        BasicText(
                            text = count.toString(),
                            style = typo.heroTitle.copy(fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = RfThemeState.currentPalette.ink),
                            modifier = Modifier.padding(horizontal = 8.dp),
                        )
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(44.dp)
                                .clickable { count = (count + 1).coerceIn(1, maxAvailableUnits) },
                        ) {
                            BasicText("+", style = typo.heroTitle.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF865DEC)))
                        }
                    }
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = false)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isOutOfStock) {
                                SolidColor(RfThemeState.currentPalette.muted)
                            } else {
                                Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9)))
                            },
                        )
                        .clickable(enabled = !isOutOfStock) {
                            viewModel?.setArticleColorVariant(currentArticle.id, selectedColor)
                            viewModel?.setDraftQuantity(currentArticle.id, count)
                            viewModel?.soundPlayer?.playCartAdd()
                            viewModel?.showSavedItemPreview(article = currentArticle, quantity = count)
                            onSelectScreenCode("s18")
                        },
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        RfIcon(kind = RfIconKind.SPARKLE, tint = Color.White, size = 20.dp)
                        BasicText(
                            text = if (isOutOfStock) "Agotado temporalmente" else "Agregar · RD$ ${count * currentArticle.pricePerDayDop}",
                            style = typo.body.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White),
                        )
                    }
                }
            }
        }

        if (showFullScreenPhoto) {
            Box(modifier = Modifier.fillMaxSize().zIndex(5f)) {
                RfFullScreenPhotoViewer(
                    mediaItems = galleryMedia,
                    currentIndex = selectedPhotoIndex,
                    title = currentArticle.name,
                    viewModel = viewModel,
                    onSelectIndex = { newIndex -> selectedPhotoIndex = newIndex },
                    onDismiss = { showFullScreenPhoto = false },
                )
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
    includeHeader: Boolean = true,
) {
    val typo = LocalRfTypography.current
    val articlesState = viewModel?.articles?.collectAsState()?.value ?: CanonicalSampleData.articles
    val vmCategories = viewModel?.catalogCategories?.collectAsState()?.value
    val balloonProductsState = viewModel?.balloonProducts?.collectAsState()?.value ?: CanonicalSampleData.balloonProducts

    fun countForCategory(categoryName: String): Int {
        val visibleArticles = articlesState.filter { article -> article.visibleInCatalog }
        val lowerCat = categoryName.lowercase()
        return visibleArticles.count { article ->
            val articleCategory = article.category.lowercase()
            val articleName = article.name.lowercase()
            when {
                lowerCat.contains("floral") -> articleCategory.contains("floral")

                lowerCat.contains("globo") -> articleCategory.contains("globo")

                lowerCat.contains("dulce") -> articleCategory.contains("dulce")

                lowerCat.contains("estaci") -> articleCategory.contains("dulce")

                lowerCat.contains("decoraci") -> listOf(
                    articleCategory.contains("floral"),
                    articleCategory.contains("globo"),
                    articleCategory.contains("decor"),
                ).any { matched -> matched }

                lowerCat.contains("mantel") -> listOf(
                    articleCategory.contains("mantel"),
                    articleName.contains("mantel"),
                ).any { matched -> matched }

                lowerCat.contains("iluminaci") -> listOf(
                    articleCategory.contains("ilumin"),
                    articleName.contains("luce"),
                ).any { matched -> matched }

                lowerCat.contains("letrero") -> listOf(
                    articleCategory.contains("letrero"),
                    articleName.contains("letrero"),
                ).any { matched -> matched }

                else -> articleCategory.equals(lowerCat, ignoreCase = true)
            }
        }
    }

    val categoriesTopPadding = if (includeHeader) 16.dp else 6.dp
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (includeHeader) RfThemeState.currentPalette.bg else Color.Transparent),
    ) {
        if (includeHeader) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(Brush.radialGradient(colors = listOf(RfThemeState.currentPalette.radialPrimary, RfThemeState.currentPalette.radialPrimary.copy(alpha = 0f)), center = Offset(0f, 0f), radius = size.width * 0.9f))
                drawRect(Brush.radialGradient(colors = listOf(RfThemeState.currentPalette.radialSecondary, RfThemeState.currentPalette.radialSecondary.copy(alpha = 0f)), center = Offset(size.width, 0f), radius = size.width * 0.8f))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = categoriesTopPadding, start = 20.dp, end = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            if (includeHeader) {
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
            }

            if (vmCategories != null) {
                if (vmCategories.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = false)
                            .clip(RoundedCornerShape(20.dp))
                            .background(RfThemeState.currentPalette.surface)
                            .padding(20.dp),
                    ) {
                        BasicText(
                            text = "El negocio aún no ha publicado categorías en el catálogo.",
                            style = typo.secondary.copy(fontSize = 14.sp, color = RfThemeState.currentPalette.muted),
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        vmCategories.chunked(2).forEach { rowItems ->
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                rowItems.forEachIndexed { colIndex, category ->
                                    val liveCount = countForCategory(category.name).coerceAtLeast(category.itemCount)
                                    val cardBg = if (colIndex == 0) Color(0xFFFFF2C9) else Color(0xFFFDE6F2)
                                    CategoryGridCard(
                                        title = category.name,
                                        subtitle = "$liveCount artículos",
                                        bg = cardBg,
                                        illustration = resolvePersonalCategoryIllustration(category.illoKey, category.name),
                                        customIconUrl = category.illoKey,
                                        viewModel = viewModel,
                                        modifier = Modifier.weight(1f),
                                    ) {
                                        viewModel.setSelectedCategory(category.name)
                                        viewModel.soundPlayer.playTap()
                                        onSelectScreenCode("s18")
                                    }
                                }
                                if (rowItems.size < 2) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        CategoryGridCard(
                            title = "Mobiliario",
                            subtitle = "${countForCategory("Mobiliario")} artículos",
                            bg = Color(0xFFFFF2C9),
                            illustration = CategoryIllustration.MOBILIARIO,
                            modifier = Modifier.weight(1f),
                        ) {
                            viewModel?.setSelectedCategory("Mobiliario")
                            viewModel?.soundPlayer?.playTap()
                            onSelectScreenCode("s18")
                        }
                        CategoryGridCard(
                            title = "Centros florales",
                            subtitle = "${countForCategory("Centros florales")} artículos",
                            bg = Color(0xFFFDE6F2),
                            illustration = CategoryIllustration.FLORAL,
                            modifier = Modifier.weight(1f),
                        ) {
                            viewModel?.setSelectedCategory("Centros florales")
                            viewModel?.soundPlayer?.playTap()
                            onSelectScreenCode("s18")
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        CategoryGridCard(
                            title = "Globos de fiesta",
                            subtitle = "${balloonProductsState.size} productos",
                            bg = Color(0xFFFFF2C9),
                            illustration = CategoryIllustration.GLOBOS,
                            modifier = Modifier.weight(1f),
                        ) {
                            viewModel?.soundPlayer?.playTap()
                            onSelectScreenCode("s23")
                        }
                        CategoryGridCard(
                            title = "Decoración",
                            subtitle = "${countForCategory("Decoración")} artículos",
                            bg = Color(0xFFFDE6F2),
                            illustration = CategoryIllustration.DECORACION,
                            modifier = Modifier.weight(1f),
                        ) {
                            viewModel?.setSelectedCategory("Decoración")
                            viewModel?.soundPlayer?.playTap()
                            onSelectScreenCode("s18")
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        CategoryGridCard(
                            title = "Estaciones dulces",
                            subtitle = "${countForCategory("Estaciones dulces")} artículos",
                            bg = Color(0xFFEEF6D6),
                            illustration = CategoryIllustration.MESA_DULCE,
                            modifier = Modifier.weight(1f),
                        ) {
                            viewModel?.setSelectedCategory("Estaciones dulces")
                            viewModel?.soundPlayer?.playTap()
                            onSelectScreenCode("s18")
                        }
                        CategoryGridCard(
                            title = "Mantelería",
                            subtitle = "${countForCategory("Mantelería")} artículos",
                            bg = Color(0xFFEFE8FD),
                            illustration = CategoryIllustration.MANTELERIA,
                            modifier = Modifier.weight(1f),
                        ) {
                            viewModel?.setSelectedCategory("Mantelería")
                            viewModel?.soundPlayer?.playTap()
                            onSelectScreenCode("s18")
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        CategoryGridCard(
                            title = "Iluminación",
                            subtitle = "${countForCategory("Iluminación")} artículos",
                            bg = Color(0xFFDDF4F3),
                            illustration = CategoryIllustration.ILUMINACION,
                            modifier = Modifier.weight(1f),
                        ) {
                            viewModel?.setSelectedCategory("Iluminación")
                            viewModel?.soundPlayer?.playTap()
                            onSelectScreenCode("s18")
                        }
                        CategoryGridCard(
                            title = "Letreros",
                            subtitle = "${countForCategory("Letreros")} artículos",
                            bg = Color(0xFF1E1A2E),
                            illustration = CategoryIllustration.LETREROS,
                            modifier = Modifier.weight(1f),
                            titleColor = Color(0xFFFFFFFF),
                        ) {
                            viewModel?.setSelectedCategory("Letreros")
                            viewModel?.soundPlayer?.playTap()
                            onSelectScreenCode("s18")
                        }
                    }
                }
            }

            Spacer(Modifier.height(RfDimens.BottomBarContentClearance))
        }
    }
}

private fun resolvePersonalCategoryIllustration(illoKey: String, name: String): CategoryIllustration {
    val combined = "${illoKey.lowercase()} ${name.lowercase()}"
    return when {
        combined.contains("flor") -> CategoryIllustration.FLORAL
        combined.contains("glob") || combined.contains("arco") -> CategoryIllustration.GLOBOS
        combined.contains("dulce") -> CategoryIllustration.MESA_DULCE
        combined.contains("mantel") -> CategoryIllustration.MANTELERIA
        combined.contains("ilumin") || combined.contains("luz") -> CategoryIllustration.ILUMINACION
        combined.contains("letrer") -> CategoryIllustration.LETREROS
        combined.contains("decor") -> CategoryIllustration.DECORACION
        else -> CategoryIllustration.MOBILIARIO
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
    subtitle: String = "",
    bg: Color,
    illustration: CategoryIllustration,
    customIconUrl: String = "",
    viewModel: RosaFiestaViewModel? = null,
    modifier: Modifier = Modifier,
    titleColor: Color = Color(0xFF1A1A2E),
    onClick: () -> Unit,
) {
    val typo = LocalRfTypography.current
    Box(
        modifier = modifier
            .height(110.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(start = 14.dp, bottom = 14.dp),
    ) {
        if (isCustomMediaIconKey(customIconUrl)) {
            RfCategoryCustomOrFallbackIcon(
                iconUrlOrKey = customIconUrl,
                contentDescription = title,
                viewModel = viewModel,
                size = 76.dp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 12.dp, end = 12.dp),
            )
        } else {
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
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.align(Alignment.BottomStart),
        ) {
            BasicText(
                text = title,
                style = TextStyle(
                    fontFamily = typo.displayFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    letterSpacing = (-0.34).sp,
                    color = titleColor,
                ),
            )
            if (subtitle.isNotBlank()) {
                BasicText(
                    text = subtitle,
                    style = typo.secondary.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = titleColor.copy(alpha = 0.72f),
                    ),
                )
            }
        }
    }
}

// ==============================================================================
// s21 · PAQUETES
// ==============================================================================

private fun matchesPackageFilter21(
    packageItem: EventPackage,
    selectedFilter: String,
    rawQuery: String,
): Boolean {
    val cleanQuery = rawQuery.trim().lowercase()
    val matchesQuery = if (cleanQuery.isEmpty()) {
        true
    } else {
        val inName = packageItem.name.lowercase().contains(cleanQuery)
        val inSubtitle = packageItem.subtitle.lowercase().contains(cleanQuery)
        listOf(inName, inSubtitle).any { matched -> matched }
    }
    val lowerName = packageItem.name.lowercase()
    val matchesFilter = when (selectedFilter) {
        "Bodas" -> lowerName.contains("boda")
        "Infantil" -> listOf(lowerName.contains("infantil"), lowerName.contains("cumple")).any { matched -> matched }
        "Quinceañeras" -> lowerName.contains("quince")
        else -> true
    }
    return if (matchesQuery) matchesFilter else false
}

@Composable
internal fun RenderPackagesScreenS21(
    onSelectScreenCode: (String) -> Unit,
    viewModel: RosaFiestaViewModel? = null,
    includeHeader: Boolean = true,
) {
    val typo = LocalRfTypography.current
    val palette = RfThemeState.currentPalette
    val allPackages = viewModel?.packages?.collectAsState()?.value ?: CanonicalSampleData.packages
    val draftQuantitiesState = viewModel?.draftQuantities?.collectAsState()?.value ?: emptyMap()
    val totalDraftCount = remember(draftQuantitiesState, viewModel) {
        viewModel?.totalDraftCartUnits() ?: draftQuantitiesState.values.sum()
    }
    var selectedOccasionFilter by remember { mutableStateOf("Todos") }
    var packageQuery by remember { mutableStateOf("") }
    var hasInteractedPackageSearch by remember { mutableStateOf(false) }
    val packageSearchError = rememberDebouncedValidationError(
        value = packageQuery,
        hasInteracted = hasInteractedPackageSearch,
    ) { query ->
        InputValidator.validateSearchQuery(query)
    }
    val visiblePackages = remember(allPackages, selectedOccasionFilter, packageQuery) {
        allPackages.filter { packageItem ->
            val isVisible = packageItem.visibleInCatalog
            val matchesFilter = matchesPackageFilter21(packageItem, selectedOccasionFilter, packageQuery)
            if (isVisible) matchesFilter else false
        }
    }

    val packagesTopPadding = if (includeHeader) 16.dp else 6.dp
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (includeHeader) palette.bg else Color.Transparent),
    ) {
        if (includeHeader) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    Brush.radialGradient(
                        colors = listOf(palette.radialPrimary, palette.radialPrimary.copy(alpha = 0f)),
                        center = Offset(0f, 0f),
                        radius = size.width * 0.9f,
                    ),
                )
                drawRect(
                    Brush.radialGradient(
                        colors = listOf(palette.radialSecondary, palette.radialSecondary.copy(alpha = 0f)),
                        center = Offset(size.width, 0f),
                        radius = size.width * 0.8f,
                    ),
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = packagesTopPadding, start = 20.dp, end = 20.dp, bottom = 116.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (includeHeader) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(44.dp)
                            .rfIconButtonShadow(CircleShape, isAdmin = false)
                            .clip(CircleShape)
                            .background(palette.surface)
                            .border(1.dp, palette.line, CircleShape)
                            .clickable { onSelectScreenCode("BACK:s14") },
                    ) {
                        RfIcon(kind = RfIconKind.BACK, tint = palette.ink, size = 22.dp)
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        BasicText(
                            "Paquetes",
                            style = typo.heroTitle.copy(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = palette.ink,
                            ),
                        )
                        BasicText(
                            "Colecciones temáticas, listas para tu evento",
                            style = typo.secondary.copy(
                                fontSize = 13.5.sp,
                                color = palette.muted,
                            ),
                        )
                    }

                    RfDraftCartPillButton(
                        count = totalDraftCount,
                        onClick = { onSelectScreenCode("s25") },
                    )
                }
            }

            val packageBorderColor = if (packageSearchError != null) Color(0xFFE53935) else palette.line
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .rfInputShadow(RoundedCornerShape(16.dp), isAdmin = false)
                        .clip(RoundedCornerShape(16.dp))
                        .background(palette.surface)
                        .border(1.dp, packageBorderColor, RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp),
                ) {
                    RfIcon(kind = RfIconKind.SEARCH, tint = palette.muted, size = 18.dp)
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        if (packageQuery.isEmpty()) {
                            BasicText(
                                text = "Buscar paquete por ocasión o estilo...",
                                style = typo.body.copy(fontSize = 14.sp, color = palette.muted),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        BasicTextField(
                            value = packageQuery,
                            onValueChange = { updatedText ->
                                packageQuery = updatedText.take(InputValidator.MAX_SEARCH_QUERY_LENGTH)
                                hasInteractedPackageSearch = true
                            },
                            singleLine = true,
                            textStyle = typo.body.copy(fontSize = 14.5.sp, color = palette.ink),
                            cursorBrush = SolidColor(Color(0xFFC2338F)),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    if (packageQuery.isNotEmpty()) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(palette.chipBg)
                                .clickable {
                                    packageQuery = ""
                                    hasInteractedPackageSearch = false
                                },
                        ) {
                            RfIcon(kind = RfIconKind.CLOSE, tint = palette.muted, size = 13.dp)
                        }
                    }
                }
                RfValidationErrorText(errorMessage = packageSearchError)
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
            ) {
                listOf("Todos", "Bodas", "Infantil", "Quinceañeras").forEach { occasionLabel ->
                    val isSelected = selectedOccasionFilter == occasionLabel
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .height(36.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(
                                if (isSelected) {
                                    Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9)))
                                } else {
                                    SolidColor(palette.surface)
                                },
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color.Transparent else palette.line,
                                shape = RoundedCornerShape(999.dp),
                            )
                            .clickable { selectedOccasionFilter = occasionLabel }
                            .padding(horizontal = 14.dp),
                    ) {
                        BasicText(
                            text = occasionLabel,
                            style = typo.body.copy(
                                fontSize = 13.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else palette.muted,
                            ),
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                visiblePackages.forEachIndexed { packageIndex, packageItem ->
                    val photoBg = when (packageIndex % 4) {
                        0 -> Color(0xFFFDE6F2)
                        1 -> Color(0xFFEFE8FD)
                        2 -> Color(0xFFEEF6D6)
                        else -> Color(0xFFFFF2C9)
                    }
                    val savingsBadge = if (packageItem.savingsDop > 0) {
                        "Ahorra ${RosaFiestaEngine.formatDop(packageItem.savingsDop)}"
                    } else {
                        "${packageItem.suggestedGuests} invitados"
                    }
                    PackageSummaryCard(
                        title = packageItem.name,
                        desc = packageItem.subtitle,
                        price = RosaFiestaEngine.formatDop(packageItem.priceDop),
                        discountBadge = savingsBadge,
                        guestsBadge = "${packageItem.suggestedGuests} inv.",
                        photoKey = packageItem.photo.toPhotoKey(),
                        photoBg = photoBg,
                    ) {
                        viewModel?.selectPackage(packageItem.id)
                        onSelectScreenCode("s22")
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(RoundedCornerShape(22.dp), isAdmin = false)
                        .clip(RoundedCornerShape(22.dp))
                        .background(palette.chipBg)
                        .border(1.dp, palette.line, RoundedCornerShape(22.dp))
                        .padding(16.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        RosaAvatar(size = RosaAvatarSize.MEDIUM)
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            BasicText(
                                text = "¿Buscas un paquete a tu medida?",
                                style = typo.body.copy(
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.ink,
                                ),
                            )
                            BasicText(
                                text = "Rosa IA diseña tu colección según tus invitados y presupuesto.",
                                style = typo.secondary.copy(
                                    fontSize = 12.5.sp,
                                    lineHeight = 17.sp,
                                    color = palette.muted,
                                ),
                            )
                        }
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .height(38.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF865DEC))))
                                .clickable { onSelectScreenCode("s26") }
                                .padding(horizontal = 14.dp),
                        ) {
                            BasicText(
                                text = "Rosa IA →",
                                style = typo.body.copy(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                ),
                            )
                        }
                    }
                }

                Spacer(Modifier.height(84.dp))
            }
        }
    }
}

@Composable
private fun PackageSummaryCard(
    title: String,
    desc: String,
    price: String,
    discountBadge: String,
    guestsBadge: String = "",
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
            .padding(12.dp),
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

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color(0xFFEEF6D6))
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    ) {
                        BasicText(discountBadge, style = typo.sectionCap.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4E6A10)))
                    }
                    if (guestsBadge.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(RfThemeState.currentPalette.chipBg)
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                        ) {
                            BasicText(guestsBadge, style = typo.sectionCap.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = RfThemeState.currentPalette.muted))
                        }
                    }
                }
                BasicText(title, style = typo.heroTitle.copy(fontSize = 17.5.sp, fontWeight = FontWeight.ExtraBold, color = RfThemeState.currentPalette.ink))
                BasicText(desc, maxLines = 2, style = typo.secondary.copy(fontSize = 13.sp, lineHeight = 18.2.sp, color = RfThemeState.currentPalette.muted))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                ) {
                    Row {
                        BasicText(
                            "desde ",
                            style = typo.secondary.copy(fontSize = 12.5.sp, color = RfThemeState.currentPalette.muted),
                            modifier = Modifier.alignByBaseline(),
                        )
                        BasicText(
                            price,
                            style = typo.heroTitle.copy(fontSize = 16.5.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFC2338F)),
                            modifier = Modifier.alignByBaseline(),
                        )
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .height(28.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color(0xFFFDE6F2))
                            .padding(horizontal = 10.dp),
                    ) {
                        BasicText(
                            text = "Ver →",
                            style = typo.sectionCap.copy(fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC2338F)),
                        )
                    }
                }
            }
        }
    }
}

private data class PackageLineItemConfig(
    val rowKey: String,
    val articleId: String?,
    val title: String,
    val unitPriceDop: Int,
    val defaultQuantity: Int,
    val photoKey: RfPhotoKey,
    val bg: Color,
)

private fun resolvePackageIncludedItems(
    packageItem: EventPackage,
    articles: List<CatalogArticle>,
): List<PackageLineItemConfig> = packageItem.includedItems.mapIndexed { index, rawDescription ->
    val trimmedLine = rawDescription.trim()
    val parsedQty = trimmedLine.takeWhile { character -> character.isDigit() }.toIntOrNull() ?: 1
    val cleanLabel = trimmedLine.dropWhile { character ->
        listOf(character.isDigit(), character.isWhitespace()).any { matched -> matched }
    }.ifBlank { trimmedLine }
    val lowerLine = trimmedLine.lowercase()
    val matchedArticle = articles.firstOrNull { candidate ->
        when {
            lowerLine.contains("silla") -> candidate.id == "art-silla-tiffany"
            lowerLine.contains("redonda") -> candidate.id == "art-mesa-redonda"
            lowerLine.contains("rectangular") -> candidate.id == "art-mesa-plegable"
            lowerLine.contains("plegable") -> candidate.id == "art-mesa-plegable"
            lowerLine.contains("centro") -> candidate.id == "art-centro-floral"
            lowerLine.contains("floral") -> candidate.id == "art-centro-floral"
            lowerLine.contains("arco") -> candidate.id == "art-arco-rosa"
            lowerLine.contains("globo") -> candidate.id == "art-arco-rosa"
            lowerLine.contains("dulce") -> candidate.id == "art-mesa-helados"
            lowerLine.contains("helado") -> candidate.id == "art-mesa-helados"
            lowerLine.contains("postre") -> candidate.id == "art-mesa-helados"
            else -> false
        }
    }
    val bgTint = when (index % 4) {
        0 -> Color(0xFFFDE6F2)
        1 -> Color(0xFFFFF2C9)
        2 -> Color(0xFFEFE8FD)
        else -> Color(0xFFEEF6D6)
    }
    val resolvedTitle = matchedArticle?.name ?: cleanLabel
    val resolvedPrice = matchedArticle?.pricePerDayDop ?: (packageItem.priceDop / packageItem.includedItems.size.coerceAtLeast(1)).coerceAtLeast(10)
    val resolvedPhoto = matchedArticle?.photo?.toPhotoKey() ?: packageItem.photo.toPhotoKey()
    PackageLineItemConfig(
        rowKey = matchedArticle?.id ?: "pkg-line-$index",
        articleId = matchedArticle?.id,
        title = resolvedTitle,
        unitPriceDop = resolvedPrice,
        defaultQuantity = parsedQty,
        photoKey = resolvedPhoto,
        bg = bgTint,
    )
}

// ==============================================================================
// s22 · DETALLE DE PAQUETE
// ==============================================================================

@Composable
internal fun RenderPackageDetailScreenS22(
    onSelectScreenCode: (String) -> Unit,
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val palette = RfThemeState.currentPalette
    val selectedPkgId = viewModel?.selectedPackageId?.collectAsState()?.value ?: "pkg-boda-real"
    val allPackages = viewModel?.packages?.collectAsState()?.value ?: CanonicalSampleData.packages
    val articlesState = viewModel?.articles?.collectAsState()?.value ?: CanonicalSampleData.articles
    val selectedPackage = allPackages.firstOrNull { packageItem -> packageItem.id == selectedPkgId } ?: allPackages.firstOrNull()
    if (selectedPackage == null) {
        RenderPersonalEmptyCatalogDetail(
            headerTitle = "Detalle de paquete",
            emptyTitle = "Paquete no disponible",
            emptyMessage = "Aún no hay paquetes armados en el catálogo.",
            backTargetCode = "BACK:s21",
            onSelectScreenCode = onSelectScreenCode,
        )
        return
    }

    val resolvedItems = remember(selectedPackage, articlesState) {
        resolvePackageIncludedItems(selectedPackage, articlesState)
    }
    var itemQuantities by remember(selectedPackage.id, resolvedItems) {
        mutableStateOf(resolvedItems.associate { lineItem -> lineItem.rowKey to lineItem.defaultQuantity })
    }
    var checkedItems by remember(selectedPackage.id, resolvedItems) {
        mutableStateOf(resolvedItems.map { lineItem -> lineItem.rowKey }.toSet())
    }
    var hasCustomizedRows by remember(selectedPackage.id) { mutableStateOf(false) }

    val totalSelectedItemsCount = resolvedItems.sumOf { lineItem ->
        val isChecked = checkedItems.contains(lineItem.rowKey)
        val qty = itemQuantities[lineItem.rowKey] ?: lineItem.defaultQuantity
        if (isChecked) qty else 0
    }
    val rawSelectedSubtotalDop = resolvedItems.sumOf { lineItem ->
        val isChecked = checkedItems.contains(lineItem.rowKey)
        val qty = itemQuantities[lineItem.rowKey] ?: lineItem.defaultQuantity
        if (isChecked) qty * lineItem.unitPriceDop else 0
    }
    val discountedTotalDop = if (!hasCustomizedRows) {
        selectedPackage.priceDop
    } else {
        (rawSelectedSubtotalDop - selectedPackage.savingsDop).coerceAtLeast(0)
    }
    val subtotalBeforeSavingsDop = if (!hasCustomizedRows) {
        selectedPackage.priceDop + selectedPackage.savingsDop
    } else {
        rawSelectedSubtotalDop
    }
    val savingsLabel = if (selectedPackage.savingsDop > 0) {
        "Ahorra RD$ ${selectedPackage.savingsDop}"
    } else {
        "${selectedPackage.suggestedGuests} invitados"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.bg),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(224.dp)
                    .background(palette.chipBg),
            ) {
                RfPhoto(
                    photoKey = selectedPackage.photo.toPhotoKey(),
                    contentDescription = selectedPackage.name,
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
                        .background(palette.surface)
                        .clickable { onSelectScreenCode("BACK:s21") },
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = palette.ink, size = 20.dp)
                }
            }

            Column(modifier = Modifier.padding(20.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFFEEF6D6))
                        .padding(horizontal = 9.dp, vertical = 3.dp),
                ) {
                    BasicText(savingsLabel, style = typo.sectionCap.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4E6A10)))
                }
                Spacer(Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    BasicText(
                        selectedPackage.name,
                        style = typo.heroTitle.copy(fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = palette.ink),
                        modifier = Modifier.weight(1f),
                    )
                    Row {
                        BasicText(
                            "desde ",
                            style = typo.secondary.copy(fontSize = 14.sp, color = palette.muted),
                            modifier = Modifier.alignByBaseline(),
                        )
                        BasicText(
                            "RD$ $discountedTotalDop",
                            style = typo.heroTitle.copy(fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFC2338F)),
                            modifier = Modifier.alignByBaseline(),
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                BasicText(
                    text = "${selectedPackage.subtitle} · Sugerido para ${selectedPackage.suggestedGuests} invitados.",
                    style = typo.body.copy(fontSize = 15.sp, color = palette.muted, lineHeight = 22.sp),
                )

                Spacer(Modifier.height(20.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    BasicText("Incluye", style = typo.heroTitle.copy(fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, color = palette.ink))
                    BasicText("Desmarca lo que no quieras", style = typo.secondary.copy(fontSize = 13.sp, color = palette.muted))
                }
                Spacer(Modifier.height(10.dp))

                resolvedItems.forEachIndexed { itemIndex, lineItem ->
                    val currentQty = itemQuantities[lineItem.rowKey] ?: lineItem.defaultQuantity
                    val isChecked = checkedItems.contains(lineItem.rowKey)
                    PackageItemRow(
                        title = lineItem.title,
                        price = "RD$ ${lineItem.unitPriceDop} / día",
                        qty = currentQty.toString(),
                        photoKey = lineItem.photoKey,
                        bg = lineItem.bg,
                        isChecked = isChecked,
                        onToggleCheck = {
                            hasCustomizedRows = true
                            checkedItems = if (isChecked) {
                                checkedItems - lineItem.rowKey
                            } else {
                                checkedItems + lineItem.rowKey
                            }
                        },
                        onDecrease = {
                            hasCustomizedRows = true
                            val nextQty = (currentQty - 1).coerceAtLeast(1)
                            itemQuantities = itemQuantities + (lineItem.rowKey to nextQty)
                        },
                        onIncrease = {
                            hasCustomizedRows = true
                            val nextQty = currentQty + 1
                            itemQuantities = itemQuantities + (lineItem.rowKey to nextQty)
                            if (!isChecked) {
                                checkedItems = checkedItems + lineItem.rowKey
                            }
                        },
                    )
                    if (itemIndex < resolvedItems.lastIndex) {
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
                .background(palette.surface)
                .border(1.dp, palette.line),
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
                            style = typo.secondary.copy(fontSize = 12.sp, color = palette.muted),
                        )
                        BasicText(
                            text = "RD$ $subtotalBeforeSavingsDop",
                            style = typo.secondary.copy(
                                fontSize = 12.sp,
                                color = palette.muted,
                                textDecoration = TextDecoration.LineThrough,
                            ),
                        )
                    }
                    BasicText(
                        text = "RD$ $discountedTotalDop",
                        style = TextStyle(
                            fontFamily = typo.displayFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            color = palette.ink,
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
                            viewModel?.applyPackageToDraftCart(selectedPackage)
                            if (hasCustomizedRows) {
                                resolvedItems.forEach { lineItem ->
                                    val targetArticleId = lineItem.articleId
                                    if (targetArticleId != null) {
                                        val isRowChecked = checkedItems.contains(lineItem.rowKey)
                                        val desiredQty = if (isRowChecked) {
                                            itemQuantities[lineItem.rowKey] ?: lineItem.defaultQuantity
                                        } else {
                                            0
                                        }
                                        viewModel?.setDraftQuantity(targetArticleId, desiredQty)
                                    }
                                }
                            }
                            viewModel?.soundPlayer?.playCartAdd()
                            onSelectScreenCode("s25")
                        },
                ) {
                    BasicText("Personalizar este paquete", style = typo.body.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White))
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
    val palette = RfThemeState.currentPalette
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .rfCardShadow(RoundedCornerShape(18.dp), isAdmin = false)
            .clip(RoundedCornerShape(18.dp))
            .background(palette.surface)
            .border(1.dp, palette.line, RoundedCornerShape(18.dp))
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
                            SolidColor(palette.chipBg)
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
                BasicText(title, maxLines = 1, overflow = TextOverflow.Ellipsis, style = typo.body.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = palette.ink))
                BasicText(price, style = typo.secondary.copy(fontSize = 13.sp, color = palette.muted))
            }

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, palette.line, RoundedCornerShape(12.dp))
                    .height(36.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp, 36.dp)
                        .clickable(onClick = onDecrease),
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.MINUS, tint = palette.ink, size = 16.dp)
                }
                BasicText(qty, style = typo.body.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = palette.ink))
                Box(
                    modifier = Modifier
                        .size(32.dp, 36.dp)
                        .clickable(onClick = onIncrease),
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.PLUS, tint = palette.ink, size = 16.dp)
                }
            }
        }
    }
}

// ==============================================================================
// s23 · TIENDA (GLOBOS)
// ==============================================================================

private data class BalloonShopItemSpec(
    val id: String,
    val title: String,
    val description: String,
    val code: String,
    val price: String,
    val bg: Color,
    val category: String,
    val hasOfferBadge: Boolean = false,
    val illustration: @Composable () -> Unit,
)

private fun classifyBalloonShopCategory(balloon: BalloonProduct): String {
    val searchableText = "${balloon.id} ${balloon.name} ${balloon.material}".lowercase()
    val isMetallic = listOf("metal", "numero", "número", "foil").any { keyword ->
        searchableText.contains(keyword)
    }
    val isBouquet = listOf("bouquet", "helio", "ramo").any { keyword ->
        searchableText.contains(keyword)
    }
    return when {
        isMetallic -> "Metalizados"
        isBouquet -> "Bouquets"
        else -> "Látex"
    }
}

private fun matchesBalloonSearchQuery(
    item: BalloonShopItemSpec,
    rawQuery: String,
): Boolean {
    val normalizedQuery = rawQuery.trim().lowercase()
    if (normalizedQuery.isEmpty()) {
        return true
    }
    val searchableFields = listOf(item.title, item.description, item.code, item.category)
    return searchableFields.any { fieldValue ->
        fieldValue.lowercase().contains(normalizedQuery)
    }
}

@Composable
private fun resolveBalloonShopIllustration(
    balloonId: String,
    fallbackIndex: Int,
): @Composable () -> Unit = when {
    balloonId.contains("numero") -> {
        { BalloonStarIllustration(modifier = Modifier.size(108.dp)) }
    }

    balloonId.contains("bouquet") -> {
        { BalloonTricolorIllustration(modifier = Modifier.size(108.dp)) }
    }

    balloonId.contains("guirnalda") -> {
        { BalloonArchShopIllustration(modifier = Modifier.size(108.dp)) }
    }

    balloonId.contains("burbuja") -> {
        { BalloonPastelSetIllustration(modifier = Modifier.size(108.dp)) }
    }

    else -> when (fallbackIndex % 4) {
        0 -> {
            { BalloonStarIllustration(modifier = Modifier.size(108.dp)) }
        }

        1 -> {
            { BalloonPastelSetIllustration(modifier = Modifier.size(108.dp)) }
        }

        2 -> {
            { BalloonTricolorIllustration(modifier = Modifier.size(108.dp)) }
        }

        else -> {
            { BalloonArchShopIllustration(modifier = Modifier.size(108.dp)) }
        }
    }
}

@Composable
internal fun RenderShopScreenS23(
    onSelectScreenCode: (String) -> Unit,
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val palette = RfThemeState.currentPalette
    val gradDiag = Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9)))
    var selectedFilter by remember { mutableStateOf("Todos") }
    var showSearchInput by remember { mutableStateOf(false) }
    var balloonSearchQuery by remember { mutableStateOf("") }
    var hasInteractedBalloonSearch by remember { mutableStateOf(false) }
    val balloonSearchError = rememberDebouncedValidationError(
        value = balloonSearchQuery,
        hasInteracted = hasInteractedBalloonSearch,
    ) { query ->
        InputValidator.validateSearchQuery(query)
    }

    val balloonProducts = viewModel?.balloonProducts?.collectAsState()?.value ?: CanonicalSampleData.balloonProducts
    val favoritesSet = viewModel?.favorites?.collectAsState()?.value ?: emptySet()
    val draftQuantities = viewModel?.draftQuantities?.collectAsState()?.value ?: emptyMap()
    val cartCount = remember(draftQuantities, viewModel) {
        viewModel?.totalDraftCartUnits() ?: draftQuantities.values.filter { quantity -> quantity > 0 }.sum()
    }

    val cardBackgrounds = remember {
        listOf(
            Color(0xFFFFF2C9),
            Color(0xFFFDE6F2),
            Color(0xFFDDF4F3),
            Color(0xFFEEF6D6),
        )
    }

    val allBalloonItems = balloonProducts.mapIndexed { balloonIndex, balloon ->
        val cardBg = cardBackgrounds[balloonIndex % cardBackgrounds.size]
        val cardIllustration = resolveBalloonShopIllustration(
            balloonId = balloon.id,
            fallbackIndex = balloonIndex,
        )
        BalloonShopItemSpec(
            id = balloon.id,
            title = balloon.name,
            description = balloon.description,
            code = balloon.code,
            price = "RD$ ${balloon.unitPriceDop}",
            bg = cardBg,
            category = classifyBalloonShopCategory(balloon),
            hasOfferBadge = balloonIndex == 1,
            illustration = cardIllustration,
        )
    }

    val filteredItems = remember(allBalloonItems, selectedFilter, balloonSearchQuery) {
        val categoryMatched = if (selectedFilter == "Todos") {
            allBalloonItems
        } else {
            allBalloonItems.filter { item -> item.category == selectedFilter }
        }
        categoryMatched.filter { item ->
            matchesBalloonSearchQuery(item = item, rawQuery = balloonSearchQuery)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.bg),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                Brush.radialGradient(
                    colors = listOf(palette.radialPrimary, palette.radialPrimary.copy(alpha = 0f)),
                    center = Offset(0f, 0f),
                    radius = size.width * 0.9f,
                ),
            )
            drawRect(
                Brush.radialGradient(
                    colors = listOf(palette.radialSecondary, palette.radialSecondary.copy(alpha = 0f)),
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
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = false)
                        .clip(CircleShape)
                        .background(palette.surface)
                        .border(1.dp, palette.line, CircleShape)
                        .clickable { onSelectScreenCode("BACK:s14") },
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = palette.ink, size = 22.dp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    BasicText("Tienda", style = typo.secondary.copy(fontSize = 13.sp, color = palette.muted))
                    BasicText(
                        "Globos",
                        style = TextStyle(
                            fontFamily = typo.displayFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 26.sp,
                            letterSpacing = (-0.52).sp,
                            color = palette.ink,
                        ),
                    )
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(8.dp, CircleShape, spotColor = Color(0x33B450B4))
                        .clip(CircleShape)
                        .background(if (showSearchInput) palette.chipBg else palette.surface)
                        .clickable {
                            val nextSearchVisible = !showSearchInput
                            showSearchInput = nextSearchVisible
                            if (!nextSearchVisible) {
                                balloonSearchQuery = ""
                            }
                        },
                ) {
                    RfIcon(kind = RfIconKind.SEARCH, tint = palette.ink, size = 20.dp)
                }
                RfDraftCartPillButton(
                    count = cartCount,
                    onClick = { onSelectScreenCode("s25") },
                )
            }

            AnimatedVisibility(visible = showSearchInput) {
                val balloonBorderColor = if (balloonSearchError != null) Color(0xFFE53935) else palette.line
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .rfInputShadow(RoundedCornerShape(14.dp), isAdmin = false)
                            .clip(RoundedCornerShape(14.dp))
                            .background(palette.surface)
                            .border(1.dp, balloonBorderColor, RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp),
                    ) {
                        RfIcon(kind = RfIconKind.SEARCH, tint = palette.muted, size = 18.dp)
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                            if (balloonSearchQuery.isEmpty()) {
                                BasicText(
                                    text = "Buscar globos por nombre o código...",
                                    style = typo.body.copy(fontSize = 14.sp, color = palette.muted),
                                )
                            }
                            BasicTextField(
                                value = balloonSearchQuery,
                                onValueChange = { updatedQuery ->
                                    balloonSearchQuery = updatedQuery.take(InputValidator.MAX_SEARCH_QUERY_LENGTH)
                                    hasInteractedBalloonSearch = true
                                },
                                singleLine = true,
                                textStyle = typo.body.copy(fontSize = 14.sp, color = palette.ink),
                                cursorBrush = SolidColor(palette.ink),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        if (balloonSearchQuery.isNotEmpty()) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(palette.chipBg)
                                    .clickable {
                                        balloonSearchQuery = ""
                                        hasInteractedBalloonSearch = false
                                    },
                            ) {
                                RfIcon(kind = RfIconKind.CLOSE, tint = palette.muted, size = 13.dp)
                            }
                        }
                    }
                    RfValidationErrorText(errorMessage = balloonSearchError)
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
                            .background(if (isSelected) gradDiag else SolidColor(palette.surface))
                            .border(1.dp, if (isSelected) Color.Transparent else palette.line, RoundedCornerShape(999.dp))
                            .clickable { selectedFilter = label }
                            .padding(horizontal = 15.dp),
                    ) {
                        BasicText(
                            text = label,
                            style = typo.body.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) Color.White else palette.muted,
                            ),
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(128.dp)
                    .rfHeroBannerShadow(RoundedCornerShape(24.dp), isAdmin = false)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFEFE8FD)),
            ) {
                Box(
                    modifier = Modifier
                        .size(132.dp)
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
                                color = Color(0xFF1E1632),
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
                        color = palette.ink,
                    ),
                )
                BasicText(
                    text = "Ver todo",
                    style = typo.body.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC2338F)),
                    modifier = Modifier.clickable {
                        selectedFilter = "Todos"
                        balloonSearchQuery = ""
                    },
                )
            }

            if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
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
                        BasicText(
                            text = "No encontramos globos con ese filtro",
                            style = typo.body.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = palette.ink),
                        )
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .height(40.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(gradDiag)
                                .clickable {
                                    selectedFilter = "Todos"
                                    balloonSearchQuery = ""
                                }
                                .padding(horizontal = 18.dp),
                        ) {
                            BasicText(
                                text = "Ver todos los globos",
                                style = typo.body.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White),
                            )
                        }
                    }
                }
            }

            filteredItems.chunked(2).forEach { rowPair ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                ) {
                    val first = rowPair[0]
                    val isFirstFavorite = favoritesSet.contains(first.id)
                    TiendaBalloonProductCard(
                        title = first.title,
                        price = first.price,
                        bg = first.bg,
                        isFavorite = isFirstFavorite,
                        hasOfferBadge = first.hasOfferBadge,
                        illustration = first.illustration,
                        onToggleFavorite = { viewModel?.toggleFavorite(first.id) },
                        onAddClick = { viewModel?.adjustDraftQuantity(first.id, 1) },
                        modifier = Modifier.weight(1f),
                    )
                    if (rowPair.size > 1) {
                        val second = rowPair[1]
                        val isSecondFavorite = favoritesSet.contains(second.id)
                        TiendaBalloonProductCard(
                            title = second.title,
                            price = second.price,
                            bg = second.bg,
                            isFavorite = isSecondFavorite,
                            hasOfferBadge = second.hasOfferBadge,
                            illustration = second.illustration,
                            onToggleFavorite = { viewModel?.toggleFavorite(second.id) },
                            onAddClick = { viewModel?.adjustDraftQuantity(second.id, 1) },
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            Spacer(Modifier.height(RfDimens.BottomBarContentClearance))
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
    onToggleFavorite: () -> Unit = {},
    onAddClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val typo = LocalRfTypography.current
    val palette = RfThemeState.currentPalette
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
                    .height(100.dp)
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
                        .clickable {
                            favoriteState = !favoriteState
                            onToggleFavorite()
                        },
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
                Column(modifier = Modifier.weight(1f).padding(end = 6.dp)) {
                    BasicText(
                        text = title,
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.ink,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    BasicText(
                        text = price,
                        style = TextStyle(
                            fontFamily = typo.displayFamily,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = palette.ink,
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

private data class FavoriteDisplayCardSpec(
    val id: String,
    val title: String,
    val description: String,
    val priceLabel: String,
    val photoKey: RfPhotoKey,
    val bg: Color,
    val isBalloon: Boolean,
)

@Composable
internal fun RenderClientFavoritesScreenS24(
    onSelectScreenCode: (String) -> Unit,
    viewModel: RosaFiestaViewModel? = null,
    includeHeader: Boolean = true,
) {
    val typo = LocalRfTypography.current
    val palette = RfThemeState.currentPalette
    val allArticles = viewModel?.articles?.collectAsState()?.value ?: CanonicalSampleData.articles
    val allBalloons = viewModel?.balloonProducts?.collectAsState()?.value ?: CanonicalSampleData.balloonProducts
    val favoriteIds = viewModel?.favorites?.collectAsState()?.value ?: setOf("art-silla-tiffany", "art-mesa-redonda")
    val observedEvents = viewModel?.events?.collectAsState()?.value
    val observedSelectedEventId = viewModel?.selectedEventId?.collectAsState()?.value

    val favoriteDisplayItems = remember(allArticles, allBalloons, favoriteIds, viewModel) {
        val pastelBackgrounds = listOf(
            Color(0xFFDDF4F3),
            Color(0xFFFFF2C9),
            Color(0xFFFDE6F2),
            Color(0xFFEEF6D6),
        )
        val matchedArticles = allArticles
            .filter { article -> favoriteIds.contains(article.id) }
            .mapIndexed { articleIndex, article ->
                FavoriteDisplayCardSpec(
                    id = article.id,
                    title = article.name,
                    description = article.description,
                    priceLabel = article.pricePerDayDop.toString(),
                    photoKey = article.photo.toPhotoKey(),
                    bg = pastelBackgrounds[articleIndex % pastelBackgrounds.size],
                    isBalloon = false,
                )
            }
        val matchedBalloons = allBalloons
            .filter { balloon -> favoriteIds.contains(balloon.id) }
            .mapIndexed { balloonIndex, balloon ->
                val bgOffset = (matchedArticles.size + balloonIndex) % pastelBackgrounds.size
                FavoriteDisplayCardSpec(
                    id = balloon.id,
                    title = balloon.name,
                    description = balloon.description,
                    priceLabel = balloon.unitPriceDop.toString(),
                    photoKey = balloon.photo.toPhotoKey(),
                    bg = pastelBackgrounds[bgOffset],
                    isBalloon = true,
                )
            }
        val combinedFavorites = matchedArticles + matchedBalloons
        val shouldUseFallback = viewModel == null
        if (combinedFavorites.isNotEmpty()) {
            combinedFavorites
        } else if (shouldUseFallback) {
            allArticles.take(2).mapIndexed { fallbackIndex, article ->
                FavoriteDisplayCardSpec(
                    id = article.id,
                    title = article.name,
                    description = article.description,
                    priceLabel = article.pricePerDayDop.toString(),
                    photoKey = article.photo.toPhotoKey(),
                    bg = pastelBackgrounds[fallbackIndex % pastelBackgrounds.size],
                    isBalloon = false,
                )
            }
        } else {
            emptyList()
        }
    }

    val totalFavoritesCount = favoriteDisplayItems.size
    val activeClientEvent = remember(observedEvents, observedSelectedEventId, viewModel) {
        viewModel?.activeClientEventOrNull()
    }
    val activeEventTitle = activeClientEvent?.title ?: "tu evento"
    val rosaSuggestionDetail = if (activeClientEvent != null) {
        "Los artículos seleccionados quedarían en “${activeClientEvent.title}”."
    } else {
        "Agrega tus favoritos al borrador o elige un evento activo."
    }
    val addAllButtonLabel = if (totalFavoritesCount == 2) "Agregar ambos" else "Agregar todos"

    val favoritesTopPadding = if (includeHeader) 16.dp else 6.dp
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = favoritesTopPadding, start = 20.dp, end = 20.dp, bottom = 116.dp),
    ) {
        if (includeHeader) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = false)
                        .clip(CircleShape)
                        .background(palette.surface)
                        .border(1.dp, palette.line, CircleShape)
                        .clickable { onSelectScreenCode("BACK:s14") },
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = palette.ink, size = 22.dp)
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    BasicText("Favoritos", style = typo.heroTitle.copy(fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = palette.ink))
                    BasicText("$totalFavoritesCount artículos guardados", style = typo.secondary.copy(fontSize = 15.sp, color = palette.muted))
                }
            }

            Spacer(Modifier.height(18.dp))
        }

        if (favoriteDisplayItems.isEmpty()) {
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

        favoriteDisplayItems.chunked(2).forEachIndexed { rowIdx, pair ->
            if (rowIdx > 0) {
                Spacer(Modifier.height(12.dp))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                val first = pair[0]
                val isFirstFav = favoriteIds.contains(first.id)
                androidx.compose.runtime.key(first.id) {
                    PersonalProductCard(
                        title = first.title,
                        desc = first.description,
                        price = first.priceLabel,
                        photoKey = first.photoKey,
                        bg = first.bg,
                        initialFavorite = isFirstFav,
                        onOpenDetail = {
                            if (first.isBalloon) {
                                onSelectScreenCode("s23")
                            } else {
                                viewModel?.selectArticle(first.id)
                                onSelectScreenCode("s19")
                            }
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
                    val isSecondFav = favoriteIds.contains(second.id)
                    androidx.compose.runtime.key(second.id) {
                        PersonalProductCard(
                            title = second.title,
                            desc = second.description,
                            price = second.priceLabel,
                            photoKey = second.photoKey,
                            bg = second.bg,
                            initialFavorite = isSecondFav,
                            onOpenDetail = {
                                if (second.isBalloon) {
                                    onSelectScreenCode("s23")
                                } else {
                                    viewModel?.selectArticle(second.id)
                                    onSelectScreenCode("s19")
                                }
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

        if (totalFavoritesCount > 0) {
            Spacer(Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(22.dp), isAdmin = false)
                    .clip(RoundedCornerShape(22.dp))
                    .background(palette.chipBg)
                    .border(1.dp, palette.line, RoundedCornerShape(22.dp))
                    .padding(16.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        RosaAvatar(size = RosaAvatarSize.MEDIUM)
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            BasicText(
                                text = "¿Los agrego a $activeEventTitle?",
                                style = typo.body.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = palette.ink),
                            )
                            BasicText(
                                text = rosaSuggestionDetail,
                                style = typo.secondary.copy(fontSize = 14.sp, lineHeight = 20.sp, color = palette.muted),
                            )
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
                                    favoriteDisplayItems.forEach { favItem ->
                                        viewModel?.adjustDraftQuantity(favItem.id, 1)
                                    }
                                    viewModel?.soundPlayer?.playCartAdd()
                                    onSelectScreenCode("s25")
                                },
                        ) {
                            BasicText(addAllButtonLabel, style = typo.body.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White))
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .height(46.dp)
                                .rfCardShadow(RoundedCornerShape(14.dp), isAdmin = false)
                                .clip(RoundedCornerShape(14.dp))
                                .background(palette.surface)
                                .clickable { onSelectScreenCode("s37") }
                                .padding(horizontal = 16.dp),
                        ) {
                            BasicText("Elegir evento", style = typo.body.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = palette.ink))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RenderPersonalEmptyCatalogDetail(
    headerTitle: String,
    emptyTitle: String,
    emptyMessage: String,
    backTargetCode: String,
    onSelectScreenCode: (String) -> Unit,
) {
    val typo = LocalRfTypography.current
    val palette = RfThemeState.currentPalette

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.bg)
            .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .rfIconButtonShadow(CircleShape, isAdmin = false)
                    .clip(CircleShape)
                    .background(palette.surface)
                    .border(1.dp, palette.line, CircleShape)
                    .clickable { onSelectScreenCode(backTargetCode) },
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.BACK, tint = palette.ink, size = 20.dp)
            }
            BasicText(
                text = headerTitle,
                style = typo.displayFamily.let { family ->
                    TextStyle(fontFamily = family, fontSize = 19.sp, fontWeight = FontWeight.Bold, color = palette.ink)
                },
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(palette.surface)
                    .border(1.dp, palette.line, RoundedCornerShape(22.dp))
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                RfIcon(kind = RfIconKind.CATALOG, tint = Color(0xFFC2338F), size = 30.dp)
                BasicText(
                    text = emptyTitle,
                    style = typo.displayFamily.let { family ->
                        TextStyle(fontFamily = family, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = palette.ink, textAlign = TextAlign.Center)
                    },
                )
                BasicText(
                    text = emptyMessage,
                    style = typo.body.copy(fontSize = 13.5.sp, lineHeight = 19.sp, color = palette.muted, textAlign = TextAlign.Center),
                )
            }
        }
    }
}
