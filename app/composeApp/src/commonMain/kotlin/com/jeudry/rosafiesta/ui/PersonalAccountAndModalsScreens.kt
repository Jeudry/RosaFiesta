package com.jeudry.rosafiesta.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeudry.rosafiesta.core.engine.RosaFiestaEngine
import com.jeudry.rosafiesta.designsystem.LocalRfTypography
import com.jeudry.rosafiesta.designsystem.RfButton
import com.jeudry.rosafiesta.designsystem.RfButtonStyle
import com.jeudry.rosafiesta.designsystem.RfColors
import com.jeudry.rosafiesta.designsystem.RfDimens
import com.jeudry.rosafiesta.designsystem.RfGradientPriceText
import com.jeudry.rosafiesta.designsystem.RfIcon
import com.jeudry.rosafiesta.designsystem.RfIconKind
import com.jeudry.rosafiesta.designsystem.RfPhoto
import com.jeudry.rosafiesta.designsystem.RfPhotoKey
import com.jeudry.rosafiesta.designsystem.RfRadialBackgroundBox
import com.jeudry.rosafiesta.designsystem.RfThemeState
import com.jeudry.rosafiesta.designsystem.RfThemeVariant
import com.jeudry.rosafiesta.designsystem.RosaAvatar
import com.jeudry.rosafiesta.designsystem.RosaAvatarSize
import com.jeudry.rosafiesta.designsystem.rememberRfSkeletonBrush
import com.jeudry.rosafiesta.designsystem.rfCardShadow
import com.jeudry.rosafiesta.designsystem.rfHeroBannerShadow
import com.jeudry.rosafiesta.designsystem.rfIconButtonShadow
import com.jeudry.rosafiesta.designsystem.rfInputShadow
import com.jeudry.rosafiesta.designsystem.rfPrimaryButtonShadow
import com.jeudry.rosafiesta.domain.CanonicalSampleData
import com.jeudry.rosafiesta.domain.CatalogArticle
import com.jeudry.rosafiesta.domain.DomainClockFormatter
import com.jeudry.rosafiesta.domain.NotificationRecord
import com.jeudry.rosafiesta.domain.sound.AppAudioConfig
import com.jeudry.rosafiesta.domain.sound.SoundEffect
import com.jeudry.rosafiesta.domain.sound.SoundEffectCategory
import com.jeudry.rosafiesta.domain.validation.InputValidator

private val RosaButtonGradient = Brush.horizontalGradient(
    listOf(Color(0xFFEA4EA9), Color(0xFFB754CB), Color(0xFF865DEC)),
)

@Composable
internal fun RenderPersonalNotificationsS79(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val rawNotificationsList = viewModel?.notifications?.collectAsState()?.value
    val notificationsList = if (viewModel != null) {
        viewModel.filterNotificationsForPersonalClient(rawNotificationsList.orEmpty())
    } else {
        emptyList()
    }
    var locallyReadIds by remember { mutableStateOf(emptySet<String>()) }
    var selectedFilterLabel by remember { mutableStateOf("Todas") }

    val unreadCount = notificationsList.count { notification ->
        val isMarkedLocally = locallyReadIds.contains(notification.id)
        val isRead = if (notification.isRead) true else isMarkedLocally
        !isRead
    }
    val allMarkedRead = unreadCount == 0

    val filteredNotifications = notificationsList.filter { notification ->
        val isMarkedLocally = locallyReadIds.contains(notification.id)
        val isRead = if (notification.isRead) true else isMarkedLocally
        matchesNotificationFilter79(notification, isRead, selectedFilterLabel)
    }

    RfRadialBackgroundBox(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = false)
                        .clip(CircleShape)
                        .background(RfThemeState.currentPalette.surface)
                        .clickable { onSelectScreenCode("BACK:s14") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = RfThemeState.currentPalette.ink, size = 22.dp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "Notificaciones",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = RfThemeState.currentPalette.ink),
                    )
                    BasicText(
                        text = if (allMarkedRead) "Al día · 0 sin leer" else "$unreadCount sin leer",
                        maxLines = 1,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = RfThemeState.currentPalette.muted),
                    )
                }

                BasicText(
                    text = if (allMarkedRead) "Leídas" else "Marcar leídas",
                    modifier = Modifier.clickable {
                        locallyReadIds = notificationsList.map { notification -> notification.id }.toSet()
                        notificationsList.forEach { notification ->
                            viewModel?.markNotificationAsRead(notification.id)
                        }
                    },
                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFC2338F)),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf("Todas", "No leídas", "Pagos", "Eventos").forEach { filterLabel ->
                    val selected = filterLabel == selectedFilterLabel
                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (selected) {
                                    Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9)))
                                } else {
                                    SolidColor(RfThemeState.currentPalette.chipBg)
                                },
                            )
                            .clickable { selectedFilterLabel = filterLabel }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = filterLabel,
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = if (selected) Color.White else Color(0xFF1A1A2E),
                            ),
                        )
                    }
                }
            }

            if (filteredNotifications.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = false)
                        .clip(RoundedCornerShape(20.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        val isTotallyEmpty = notificationsList.isEmpty()
                        BasicText(
                            text = if (isTotallyEmpty) "Sin notificaciones por ahora" else "Sin notificaciones en “$selectedFilterLabel”",
                            style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = RfThemeState.currentPalette.ink),
                        )
                        BasicText(
                            text = if (isTotallyEmpty) {
                                "Te avisaremos aquí cuando tu cotización o evento tenga novedades."
                            } else {
                                "Cambia el filtro para revisar el resto de tu actividad."
                            },
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = RfThemeState.currentPalette.muted),
                        )
                    }
                }
            } else {
                val mappedItems = filteredNotifications.map { notification ->
                    val isMarkedLocally = locallyReadIds.contains(notification.id)
                    val isRead = if (notification.isRead) true else isMarkedLocally
                    val visual = resolveNotificationVisual79(notification)
                    NotificationItem79(
                        title = notification.title,
                        body = notification.body,
                        time = notification.createdAtLabel,
                        icon = visual.icon,
                        iconBg = visual.iconBg,
                        iconTint = visual.iconTint,
                        unread = !isRead,
                        onClick = {
                            locallyReadIds = locallyReadIds + notification.id
                            viewModel?.markNotificationAsRead(notification.id)
                            onSelectScreenCode(resolveNotificationDestination79(notification))
                        },
                    )
                }
                NotificationSectionLabel79("Actividad reciente (${mappedItems.size})")
                NotificationCard79(items = mappedItems)
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

private data class NotificationVisual79(
    val icon: RfIconKind,
    val iconBg: Color,
    val iconTint: Color,
)

private fun isPaymentNotification79(notification: NotificationRecord): Boolean {
    val categoryUpper = notification.category.uppercase()
    if (categoryUpper == "PAYMENTS") return true
    if (categoryUpper == "REFUNDS") return true
    val combinedLower = "${notification.title} ${notification.body}".lowercase()
    if (combinedLower.contains("pago")) return true
    if (combinedLower.contains("anticipo")) return true
    if (combinedLower.contains("reembolso")) return true
    return combinedLower.contains("recibo")
}

private fun isQuoteNotification79(notification: NotificationRecord): Boolean {
    val categoryUpper = notification.category.uppercase()
    if (categoryUpper == "QUOTES") return true
    if (notification.actionPath.contains("quote", ignoreCase = true)) return true
    return notification.title.contains("cotización", ignoreCase = true)
}

private fun isEventNotification79(notification: NotificationRecord): Boolean {
    val categoryUpper = notification.category.uppercase()
    if (categoryUpper == "LOGISTICS") return true
    if (categoryUpper == "EVENTS") return true
    if (isQuoteNotification79(notification)) return true
    val combinedLower = "${notification.title} ${notification.body}".lowercase()
    if (combinedLower.contains("evento")) return true
    if (combinedLower.contains("montaje")) return true
    return combinedLower.contains("ruta")
}

private fun matchesNotificationFilter79(
    notification: NotificationRecord,
    isRead: Boolean,
    selectedFilterLabel: String,
): Boolean = when (selectedFilterLabel) {
    "No leídas" -> !isRead
    "Pagos" -> isPaymentNotification79(notification)
    "Eventos" -> isEventNotification79(notification)
    else -> true
}

private fun resolveNotificationDestination79(notification: NotificationRecord): String {
    if (isQuoteNotification79(notification)) return "s48"
    if (isPaymentNotification79(notification)) return "s82"
    return "s40"
}

private fun resolveNotificationVisual79(notification: NotificationRecord): NotificationVisual79 {
    if (isQuoteNotification79(notification)) {
        return NotificationVisual79(
            icon = RfIconKind.NOTE,
            iconBg = Color(0xFFFDE6F2),
            iconTint = Color(0xFFC2338F),
        )
    }
    if (isPaymentNotification79(notification)) {
        return NotificationVisual79(
            icon = RfIconKind.WALLET,
            iconBg = Color(0xFFEEF6D6),
            iconTint = Color(0xFF4E6A10),
        )
    }
    return NotificationVisual79(
        icon = RfIconKind.CALENDAR,
        iconBg = Color(0xFFFFF2C9),
        iconTint = Color(0xFF7A5700),
    )
}

private data class NotificationItem79(
    val title: String,
    val body: String,
    val time: String,
    val icon: RfIconKind,
    val iconBg: Color,
    val iconTint: Color,
    val unread: Boolean,
    val onClick: () -> Unit,
)

@Composable
private fun NotificationSectionLabel79(label: String) {
    val typo = LocalRfTypography.current
    BasicText(
        text = label.uppercase(),
        style = TextStyle(
            fontFamily = typo.bodyFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            letterSpacing = 0.96.sp,
            color = RfThemeState.currentPalette.muted,
        ),
    )
}

@Composable
private fun NotificationCard79(items: List<NotificationItem79>) {
    val typo = LocalRfTypography.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = false)
            .clip(RoundedCornerShape(20.dp))
            .background(RfThemeState.currentPalette.surface)
            .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp)),
    ) {
        items.forEachIndexed { index, item ->
            if (index > 0) {
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(RfThemeState.currentPalette.lineSoft))
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (item.unread) Color(0xFFFFF8FC) else Color.Transparent)
                    .clickable(onClick = item.onClick)
                    .padding(horizontal = 14.dp, vertical = 13.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(item.iconBg),
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = item.icon, tint = item.iconTint, size = 19.dp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        BasicText(
                            text = item.title,
                            modifier = Modifier.weight(1f),
                            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.5.sp, color = RfThemeState.currentPalette.ink),
                        )
                        BasicText(
                            text = item.time,
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = RfThemeState.currentPalette.muted),
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    BasicText(
                        text = item.body,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.5.sp, lineHeight = 18.9.sp, color = RfThemeState.currentPalette.muted),
                    )
                }

                if (item.unread) {
                    Box(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEA4EA9)),
                    )
                } else {
                    Spacer(modifier = Modifier.width(9.dp))
                }
            }
        }
    }
}

private fun isUnfilteredCategoryLabel80(categoryLabel: String): Boolean {
    val normalized = categoryLabel.trim().lowercase()
    if (normalized.isEmpty()) return true
    if (normalized == "todos") return true
    return normalized == "todas"
}

private fun filterFallbackArticles80(
    articles: List<CatalogArticle>,
    query: String,
    selectedCategory: String,
    maxPriceFilter: Int,
    onlyAvailable: Boolean,
    sortOrder: String,
): List<CatalogArticle> {
    val trimmedQuery = query.trim()
    val matchingList = articles.filter { article ->
        val matchesQuery = if (trimmedQuery.isEmpty()) {
            true
        } else {
            val byName = article.name.contains(trimmedQuery, ignoreCase = true)
            val byCategory = article.category.contains(trimmedQuery, ignoreCase = true)
            val byCode = article.code.contains(trimmedQuery, ignoreCase = true)
            val byDesc = article.description.contains(trimmedQuery, ignoreCase = true)
            listOf(byName, byCategory, byCode, byDesc).any { matched -> matched }
        }
        val matchesCategory = if (isUnfilteredCategoryLabel80(selectedCategory)) {
            true
        } else {
            article.category.contains(selectedCategory.trim(), ignoreCase = true)
        }
        val matchesPrice = article.pricePerDayDop <= maxPriceFilter
        val matchesStock = if (onlyAvailable) article.availableOct23 > 0 else true
        listOf(matchesQuery, matchesCategory, matchesPrice, matchesStock).all { matched -> matched }
    }
    val normalizedSort = sortOrder.trim().lowercase()
    return when {
        normalizedSort.contains("menor") -> matchingList.sortedBy { article -> article.pricePerDayDop }
        normalizedSort.contains("mayor") -> matchingList.sortedByDescending { article -> article.pricePerDayDop }
        normalizedSort.contains("alfab") -> matchingList.sortedBy { article -> article.name }
        else -> matchingList.sortedByDescending { article -> article.totalStock }
    }
}

private fun resolveArticlePhotoKey80(photoKeyText: String): RfPhotoKey = RfPhotoKey.entries.firstOrNull { entry ->
    if (entry.assetKey == photoKeyText) true else entry.name.equals(photoKeyText, ignoreCase = true)
} ?: RfPhotoKey.MESA_REDONDA

@Composable
internal fun RenderPersonalSearchS80(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val cardShape = RoundedCornerShape(20.dp)
    val vmQuery = viewModel?.searchQuery?.collectAsState()?.value ?: ""
    var localQuery by remember(vmQuery) { mutableStateOf(vmQuery) }
    val selectedCategory = viewModel?.selectedCategory?.collectAsState()?.value ?: "Todos"
    val maxPriceFilter = viewModel?.maxPriceFilterDop?.collectAsState()?.value ?: 5000
    val onlyAvailable = viewModel?.onlyAvailableFilter?.collectAsState()?.value ?: false
    val sortOrder = viewModel?.searchSortOrder?.collectAsState()?.value ?: "Más populares"
    val allArticles = viewModel?.articles?.collectAsState()?.value ?: CanonicalSampleData.articles
    val isInlineVoiceListening = viewModel?.isVoiceListening?.collectAsState()?.value ?: false
    val voiceOrigin = viewModel?.voiceInputOrigin?.collectAsState()?.value ?: VoiceInputOrigin.NONE
    val isSearchMicActive = if (isInlineVoiceListening) voiceOrigin == VoiceInputOrigin.SEARCH else false

    val filteredArticles = if (viewModel != null) {
        viewModel.filteredCatalogArticles(queryOverride = localQuery)
    } else {
        filterFallbackArticles80(
            articles = allArticles,
            query = localQuery,
            selectedCategory = selectedCategory,
            maxPriceFilter = maxPriceFilter,
            onlyAvailable = onlyAvailable,
            sortOrder = sortOrder,
        )
    }

    val isCategoryFiltered = !isUnfilteredCategoryLabel80(selectedCategory)
    val activeFilterCount = listOf(
        isCategoryFiltered,
        maxPriceFilter < 5000,
        onlyAvailable,
    ).count { isActive -> isActive }

    val sortLabel = if (sortOrder == "Populares") "Más populares" else sortOrder

    RfRadialBackgroundBox(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 112.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = false)
                        .clip(CircleShape)
                        .background(RfThemeState.currentPalette.surface)
                        .clickable { onSelectScreenCode("BACK:s14") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = RfThemeState.currentPalette.ink, size = 22.dp)
                }

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .rfInputShadow(RoundedCornerShape(16.dp), isAdmin = false)
                        .clip(RoundedCornerShape(16.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .padding(start = 16.dp, end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    RfIcon(kind = RfIconKind.SEARCH, tint = RfThemeState.currentPalette.muted, size = 18.dp)
                    BasicTextField(
                        value = localQuery,
                        onValueChange = { updatedText ->
                            val cappedQuery = updatedText.take(InputValidator.MAX_SEARCH_QUERY_LENGTH)
                            localQuery = cappedQuery
                            viewModel?.setSearchQuery(cappedQuery)
                        },
                        singleLine = true,
                        textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = RfThemeState.currentPalette.ink),
                        cursorBrush = SolidColor(Color(0xFFC2338F)),
                        modifier = Modifier.weight(1f),
                        decorationBox = { innerField ->
                            Box(contentAlignment = Alignment.CenterStart) {
                                if (localQuery.isEmpty()) {
                                    BasicText(
                                        text = "Buscar sillas, mesas, arcos...",
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = RfThemeState.currentPalette.muted),
                                        maxLines = 1,
                                    )
                                }
                                innerField()
                            }
                        },
                    )
                    if (localQuery.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(RfThemeState.currentPalette.chipBg)
                                .clickable {
                                    localQuery = ""
                                    viewModel?.setSearchQuery("")
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.CLOSE, tint = RfThemeState.currentPalette.muted, size = 14.dp)
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
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
                            contentAlignment = Alignment.Center,
                        ) {
                            val micTint = if (isSearchMicActive) Color.White else Color(0xFFB54BD6)
                            RfIcon(kind = RfIconKind.MIC, tint = micTint, size = 18.dp)
                        }
                    }
                }

                Box(modifier = Modifier.size(44.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .rfIconButtonShadow(CircleShape, isAdmin = false)
                            .clip(CircleShape)
                            .background(RfThemeState.currentPalette.surface)
                            .clickable { onSelectScreenCode("s81") },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.FILTER, tint = RfThemeState.currentPalette.ink, size = 20.dp)
                    }
                    if (activeFilterCount > 0) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 6.dp, y = (-4).dp)
                                .height(20.dp)
                                .widthIn(min = 20.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(Color(0xFFFFD45C))
                                .padding(horizontal = 5.dp),
                        ) {
                            BasicText(
                                text = activeFilterCount.toString(),
                                style = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp,
                                    color = Color(0xFF2E1A47),
                                ),
                            )
                        }
                    }
                }
            }

            InlineVoiceSearchBanner(
                viewModel = viewModel,
                onApplyVoiceQuery = { spokenQuery ->
                    localQuery = spokenQuery
                    viewModel?.setSearchQuery(spokenQuery)
                },
            )

            if (localQuery.isBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf("Silla", "Mesa", "Arco", "Floral", "Helados").forEach { suggestion ->
                        Box(
                            modifier = Modifier
                                .height(30.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(RfThemeState.currentPalette.chipBg)
                                .clickable {
                                    localQuery = suggestion
                                    viewModel?.setSearchQuery(suggestion)
                                }
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(
                                text = suggestion,
                                style = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = Color(0xFF3B3550),
                                ),
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (isCategoryFiltered) {
                    SearchFilterChip80(
                        label = selectedCategory,
                        isActive = true,
                        onClick = { viewModel?.setSelectedCategory("Todos") },
                    )
                }
                SearchFilterChip80(
                    label = if (onlyAvailable) "Solo disponibles" else "Incluir agotados",
                    isActive = onlyAvailable,
                    onClick = { viewModel?.setOnlyAvailableFilter(!onlyAvailable) },
                )
                SearchFilterChip80(
                    label = if (maxPriceFilter < 5000) "Hasta RD$ $maxPriceFilter" else "Cualquier precio",
                    isActive = maxPriceFilter < 5000,
                    onClick = {
                        val nextMaxPrice = when (maxPriceFilter) {
                            5000 -> 1500
                            1500 -> 500
                            else -> 5000
                        }
                        viewModel?.setMaxPriceFilterDop(nextMaxPrice)
                    },
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BasicText(
                        text = "${filteredArticles.size} resultados",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = RfThemeState.currentPalette.ink),
                    )
                    if (localQuery.isNotBlank()) {
                        BasicText(
                            text = " para “${localQuery.trim()}”",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = RfThemeState.currentPalette.muted),
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            val nextOrder = when (sortLabel) {
                                "Más populares" -> "Menor precio"
                                "Menor precio" -> "Mayor precio"
                                "Mayor precio" -> "Alfabético"
                                else -> "Más populares"
                            }
                            viewModel?.setSearchSortOrder(nextOrder)
                        }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                ) {
                    BasicText(
                        text = sortLabel,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF3B3550)),
                    )
                    RfIcon(kind = RfIconKind.CHEVRON_DOWN, tint = Color(0xFF3B3550), size = 16.dp)
                }
            }

            if (filteredArticles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(cardShape, isAdmin = false)
                        .clip(cardShape)
                        .background(RfThemeState.currentPalette.surface)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        BasicText(
                            text = "Sin resultados para tu búsqueda",
                            style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = RfThemeState.currentPalette.ink),
                        )
                        BasicText(
                            text = "Intenta con otro término o limpia los filtros activos.",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = RfThemeState.currentPalette.muted),
                        )
                        Box(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .height(36.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(Color(0xFFFDE6F2))
                                .clickable {
                                    localQuery = ""
                                    viewModel?.setSearchQuery("")
                                    viewModel?.resetCatalogFilters()
                                }
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(
                                text = "Limpiar búsqueda y filtros",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFC2338F)),
                            )
                        }
                    }
                }
            }

            filteredArticles.forEach { article ->
                val freeCount = article.availableOct23.coerceAtLeast(0)
                val lowStock = freeCount <= 3
                val photoKey = resolveArticlePhotoKey80(article.photo.key)
                SearchResultCard80(
                    photoKey = photoKey,
                    photoUrl = article.photoUrl,
                    viewModel = viewModel,
                    title = article.name,
                    subtitle = article.description,
                    availability = if (lowStock) "Quedan $freeCount" else "Disponible · $freeCount libres",
                    availabilityColor = if (lowStock) Color(0xFF7A5700) else Color(0xFF4E6A10),
                    availabilityBg = if (lowStock) Color(0xFFFFF2C9) else Color(0xFFEEF6D6),
                    price = article.pricePerDayDop.toString(),
                    cardShape = cardShape,
                    onClick = {
                        viewModel?.selectArticle(article.id)
                        onSelectScreenCode("s19")
                    },
                    onAddClick = {
                        viewModel?.adjustDraftQuantity(article.id, 1)
                    },
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SearchFilterChip80(
    label: String,
    isActive: Boolean = true,
    onClick: () -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val chipBg = if (isActive) Color(0xFFFDE6F2) else RfThemeState.currentPalette.chipBg
    val chipFg = if (isActive) Color(0xFFC2338F) else Color(0xFF62627A)

    Row(
        modifier = Modifier
            .height(32.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(chipBg)
            .clickable(onClick = onClick)
            .padding(start = 14.dp, end = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        BasicText(
            text = label,
            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = chipFg),
        )
        if (isActive) {
            RfIcon(kind = RfIconKind.CLOSE, tint = chipFg, size = 13.dp)
        }
    }
}

@Composable
private fun SearchResultCard80(
    photoKey: RfPhotoKey,
    photoUrl: String = "",
    viewModel: RosaFiestaViewModel? = null,
    title: String,
    subtitle: String,
    availability: String,
    availabilityColor: Color,
    availabilityBg: Color,
    price: String,
    cardShape: androidx.compose.ui.graphics.Shape,
    onClick: () -> Unit = {},
    onAddClick: () -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .rfCardShadow(cardShape, isAdmin = false)
            .clip(cardShape)
            .background(RfThemeState.currentPalette.surface)
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RfUploadedOrCatalogPhoto(
            photoUrl = photoUrl,
            fallbackPhotoKey = photoKey,
            contentDescription = title,
            viewModel = viewModel,
            modifier = Modifier.size(88.dp),
            cornerRadius = 14.dp,
        )
        Column(modifier = Modifier.weight(1f)) {
            BasicText(
                text = title,
                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = RfThemeState.currentPalette.ink),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            BasicText(
                text = subtitle,
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = RfThemeState.currentPalette.muted),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(3.dp))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .height(22.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(availabilityBg)
                    .padding(horizontal = 9.dp),
            ) {
                BasicText(
                    text = availability,
                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = availabilityColor),
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    RfGradientPriceText(
                        text = price,
                        style = TextStyle(
                            fontFamily = typo.displayFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 19.sp,
                            lineHeight = 21.sp,
                        ),
                        modifier = Modifier.alignByBaseline(),
                    )
                    BasicText(
                        text = "RD$/día",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFFB54BD6)),
                        modifier = Modifier.alignByBaseline(),
                    )
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(RfColors.PersonalHeroGradient)
                        .clickable(onClick = onAddClick),
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.PLUS, tint = Color.White, size = 16.dp)
                }
            }
        }
    }
}

@Composable
internal fun RenderPersonalPaymentsS82(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val palette = RfThemeState.currentPalette
    val cardShape = RoundedCornerShape(20.dp)
    val fallbackEvents = CanonicalSampleData.events
    val fallbackPayments = CanonicalSampleData.payments
    viewModel?.events?.collectAsState()?.value
    viewModel?.payments?.collectAsState()?.value
    viewModel?.currentAuthenticatedUser?.collectAsState()?.value
    viewModel?.profileName?.collectAsState()?.value
    viewModel?.profileEmail?.collectAsState()?.value
    viewModel?.isExplicitGuestSession?.collectAsState()?.value

    val userEvents = if (viewModel != null) {
        viewModel.clientEventsForCurrentUser()
    } else {
        fallbackEvents
    }
    val userPayments = if (viewModel != null) {
        viewModel.clientPaymentsForCurrentUser()
    } else {
        fallbackPayments
    }
    val savedPaymentMethod = viewModel?.savedPaymentMethod?.collectAsState()?.value ?: "Transferencia Banreservas"
    var localSavedMethod by remember(savedPaymentMethod) { mutableStateOf(savedPaymentMethod) }
    var showPaymentMethodSelector by remember { mutableStateOf(false) }
    var selectedPaymentsTab by remember { mutableStateOf(0) }

    val paidFromEvents = userEvents.sumOf { eventItem -> eventItem.paidDepositDop }
    val paidFromReceipts = userPayments.sumOf { paymentItem -> paymentItem.amountDop }
    val totalPaidDop = paidFromEvents.coerceAtLeast(paidFromReceipts)
    val totalPendingDop = userEvents.sumOf { eventItem ->
        (eventItem.totalDop - eventItem.paidDepositDop).coerceAtLeast(0)
    }
    val pendingEvents = userEvents.filter { eventItem -> eventItem.totalDop > eventItem.paidDepositDop }

    RfRadialBackgroundBox(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 20.dp, end = 20.dp, top = 16.dp),
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
                        .rfIconButtonShadow(CircleShape, isAdmin = false)
                        .clip(CircleShape)
                        .background(palette.surface)
                        .clickable { onSelectScreenCode("BACK:s14") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = palette.ink, size = 22.dp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "Mis pagos",
                        style = TextStyle(
                            fontFamily = typo.displayFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            lineHeight = 22.sp,
                            letterSpacing = (-0.4).sp,
                            color = palette.ink,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    BasicText(
                        text = "Pagos y recibos de tus eventos (${userEvents.size})",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 13.sp,
                            color = palette.muted,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(22.dp), isAdmin = false)
                    .clip(RoundedCornerShape(22.dp))
                    .background(RfColors.PersonalHeroGradient)
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "Pagado",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color.White.copy(alpha = 0.85f)),
                    )
                    BasicText(
                        text = RosaFiestaEngine.formatDop(totalPaidDop),
                        style = TextStyle(
                            fontFamily = typo.displayFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp,
                            lineHeight = 28.sp,
                            color = Color.White,
                        ),
                    )
                }
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(44.dp)
                        .background(Color.White.copy(alpha = 0.3f)),
                )
                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "Pendiente",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color.White.copy(alpha = 0.85f)),
                    )
                    BasicText(
                        text = RosaFiestaEngine.formatDop(totalPendingDop),
                        style = TextStyle(
                            fontFamily = typo.displayFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp,
                            lineHeight = 28.sp,
                            color = Color.White,
                        ),
                    )
                }
            }

            PaymentsSegmentedTabs82(
                selectedTab = selectedPaymentsTab,
                pendingCount = pendingEvents.size,
                historyCount = userPayments.size,
                onSelectTab = { nextTab -> selectedPaymentsTab = nextTab },
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 2.dp, bottom = RfDimens.BottomBarContentClearance),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (selectedPaymentsTab == 0) {
                    BasicText(
                        text = "PRÓXIMOS PAGOS",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            letterSpacing = 0.96.sp,
                            color = palette.muted,
                        ),
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .rfCardShadow(cardShape, isAdmin = false)
                            .clip(cardShape)
                            .background(palette.surface),
                    ) {
                        if (userEvents.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                BasicText(
                                    text = "Aún no tienes eventos registrados",
                                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = palette.ink),
                                )
                                BasicText(
                                    text = "Arma tu evento en el catálogo para gestionar tus anticipos y saldos aquí.",
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = palette.muted, textAlign = TextAlign.Center),
                                )
                                Box(
                                    modifier = Modifier
                                        .padding(top = 2.dp)
                                        .height(36.dp)
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(Color(0xFFFDE6F2))
                                        .clickable { onSelectScreenCode("s18") }
                                        .padding(horizontal = 16.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    BasicText(
                                        text = "Explorar catálogo",
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFC2338F)),
                                    )
                                }
                            }
                        } else if (pendingEvents.isEmpty()) {
                            PendingPaymentRow82(
                                title = "Todos tus eventos están al día",
                                due = "Sin saldos pendientes",
                                dueColor = Color(0xFF4E6A10),
                                amount = "RD$ 0",
                                actionLabel = "Ver eventos",
                                highlighted = false,
                                onClick = { onSelectScreenCode("s37") },
                            )
                        } else {
                            pendingEvents.forEachIndexed { index, eventItem ->
                                if (index > 0) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(palette.lineSoft),
                                    )
                                }
                                val remainingAmount = (eventItem.totalDop - eventItem.paidDepositDop).coerceAtLeast(0)
                                val actionLabel = if (eventItem.paidDepositDop > 0) "Pagar saldo" else "Abonar"
                                PendingPaymentRow82(
                                    title = "${eventItem.title} · saldo",
                                    due = "Vence ${formatReadableSpanishDateText(eventItem.balanceDueDateLabel.ifBlank { eventItem.dateLabel })}",
                                    dueColor = if (index == 0) Color(0xFFB42345) else Color(0xFF7A5700),
                                    amount = RosaFiestaEngine.formatDop(remainingAmount),
                                    actionLabel = actionLabel,
                                    highlighted = index == 0,
                                    onClick = {
                                        viewModel?.selectEvent(eventItem.id)
                                        viewModel?.setPaymentCheckoutMode("BALANCE")
                                        onSelectScreenCode("s50")
                                    },
                                )
                            }
                        }
                    }

                    BasicText(
                        text = "PREFERENCIAS",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            letterSpacing = 0.96.sp,
                            color = palette.muted,
                        ),
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .rfCardShadow(cardShape, isAdmin = false)
                            .clip(cardShape)
                            .background(palette.surface)
                            .clickable { showPaymentMethodSelector = !showPaymentMethodSelector }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RfIcon(kind = RfIconKind.WALLET, tint = palette.ink, size = 18.dp)
                        Column(modifier = Modifier.weight(1f)) {
                            BasicText(
                                text = "Método de pago preferido",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = palette.ink),
                            )
                            BasicText(
                                text = localSavedMethod,
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = palette.muted),
                            )
                        }
                        RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = palette.muted, size = 16.dp)
                    }

                    if (showPaymentMethodSelector) {
                        val methodOptions = listOf(
                            "Transferencia Banreservas",
                            "Transferencia Banco Popular",
                            "Transferencia BHD",
                            "Tarjeta de crédito / débito",
                            "Efectivo en tienda",
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(cardShape)
                                .background(palette.surface)
                                .border(1.dp, palette.line, cardShape)
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            methodOptions.forEach { optionLabel ->
                                val isOptionSelected = optionLabel == localSavedMethod
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isOptionSelected) palette.chipBg else Color.Transparent)
                                        .clickable {
                                            localSavedMethod = optionLabel
                                            viewModel?.setSavedPaymentMethod(optionLabel)
                                            showPaymentMethodSelector = false
                                        }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    BasicText(
                                        text = optionLabel,
                                        style = TextStyle(
                                            fontFamily = typo.bodyFamily,
                                            fontWeight = if (isOptionSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 14.sp,
                                            color = palette.ink,
                                        ),
                                    )
                                    if (isOptionSelected) {
                                        RfIcon(kind = RfIconKind.CHECK, tint = palette.primaryAccent, size = 16.dp)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    BasicText(
                        text = "COMPROBANTES EMITIDOS",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            letterSpacing = 0.96.sp,
                            color = palette.muted,
                        ),
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .rfCardShadow(cardShape, isAdmin = false)
                            .clip(cardShape)
                            .background(palette.surface),
                    ) {
                        if (userPayments.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 22.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                BasicText(
                                    text = "Sin comprobantes de pago registrados todavía.",
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.5.sp, color = palette.muted),
                                )
                            }
                        } else {
                            userPayments.forEachIndexed { index, paymentItem ->
                                if (index > 0) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(palette.lineSoft),
                                    )
                                }
                                ReceiptRow82(
                                    title = "${paymentItem.kindLabel} · ${paymentItem.eventTitle}",
                                    subtitle = "${formatReadableSpanishDateText(paymentItem.dateLabel)} · ${paymentItem.methodLabel} (${paymentItem.receiptNumber})",
                                    amount = RosaFiestaEngine.formatDop(paymentItem.amountDop),
                                    onClick = {
                                        viewModel?.selectEvent(paymentItem.eventId)
                                        onSelectScreenCode("s83")
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentsSegmentedTabs82(
    selectedTab: Int,
    pendingCount: Int,
    historyCount: Int,
    onSelectTab: (Int) -> Unit,
) {
    val typo = LocalRfTypography.current
    val palette = RfThemeState.currentPalette
    val tabs = listOf(
        "Por pagar ($pendingCount)",
        "Historial ($historyCount)",
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(palette.surface)
            .border(1.dp, palette.line, RoundedCornerShape(16.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        tabs.forEachIndexed { tabIndex, tabTitle ->
            val isSelected = selectedTab == tabIndex
            val tabBrush = if (isSelected) {
                Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9)))
            } else {
                SolidColor(Color.Transparent)
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(tabBrush)
                    .clickable { onSelectTab(tabIndex) }
                    .testTag("payments-tab-$tabIndex"),
            ) {
                BasicText(
                    text = tabTitle,
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontSize = 13.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else palette.muted,
                    ),
                )
            }
        }
    }
}

@Composable
private fun PendingPaymentRow82(
    title: String,
    due: String,
    dueColor: Color,
    amount: String,
    actionLabel: String,
    highlighted: Boolean,
    onClick: () -> Unit,
) {
    val typo = LocalRfTypography.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            BasicText(
                text = title,
                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Medium, fontSize = 14.5.sp, color = RfThemeState.currentPalette.ink),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            BasicText(
                text = due,
                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = dueColor),
            )
        }
        BasicText(
            text = amount,
            style = TextStyle(
                fontFamily = typo.displayFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = RfThemeState.currentPalette.ink,
            ),
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .height(34.dp)
                .clip(RoundedCornerShape(999.dp))
                .then(
                    if (highlighted) {
                        Modifier.background(RfColors.PersonalHeroGradient)
                    } else {
                        Modifier.background(RfThemeState.currentPalette.chipBg)
                    },
                )
                .padding(horizontal = 12.dp),
        ) {
            BasicText(
                text = actionLabel,
                style = TextStyle(
                    fontFamily = typo.bodyFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (highlighted) Color.White else Color(0xFF1A1A2E),
                ),
            )
        }
    }
}

@Composable
private fun ReceiptRow82(
    title: String,
    subtitle: String,
    amount: String,
    onClick: () -> Unit,
) {
    val typo = LocalRfTypography.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFEEF6D6)),
            contentAlignment = Alignment.Center,
        ) {
            RfIcon(kind = RfIconKind.CHECK, tint = Color(0xFF4E6A10), size = 18.dp)
        }
        Column(modifier = Modifier.weight(1f)) {
            BasicText(
                text = title,
                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Medium, fontSize = 14.5.sp, color = RfThemeState.currentPalette.ink),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            BasicText(
                text = subtitle,
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = RfThemeState.currentPalette.muted),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            BasicText(
                text = amount,
                style = TextStyle(
                    fontFamily = typo.displayFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = RfThemeState.currentPalette.ink,
                ),
            )
            BasicText(
                text = "Recibo",
                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFFC2338F)),
            )
        }
    }
}

private enum class PersonalSettingsSection(
    val icon: RfIconKind,
    val titleEs: String,
    val titleEn: String,
    val subtitleEs: String,
    val subtitleEn: String,
) {
    PREFERENCES(
        icon = RfIconKind.SPARKLE,
        titleEs = "Preferencias",
        titleEn = "Preferences",
        subtitleEs = "Tema, idioma y recordatorios",
        subtitleEn = "Theme, language and reminders",
    ),
    AUDIO(
        icon = RfIconKind.BELL,
        titleEs = "Audio y efectos de sonido",
        titleEn = "Audio & sound effects",
        subtitleEs = "Volumen, tono y categorías",
        subtitleEn = "Volume, tone and categories",
    ),
    ACCOUNT(
        icon = RfIconKind.USER,
        titleEs = "Cuenta y seguridad",
        titleEn = "Security & account",
        subtitleEs = "Datos personales, contraseña, pagos y dirección",
        subtitleEn = "Profile, password, payments and address",
    ),
    NOTIFICATIONS(
        icon = RfIconKind.MAIL,
        titleEs = "Avisos",
        titleEn = "Notifications",
        subtitleEs = "Notificaciones push, WhatsApp y recibos",
        subtitleEn = "Push notifications, WhatsApp and receipts",
    ),
    SECURITY_SYNC(
        icon = RfIconKind.SHIELD,
        titleEs = "Seguridad y sincronización",
        titleEn = "Security & sync",
        subtitleEs = "Bloqueo biométrico y sincronización Wi‑Fi",
        subtitleEn = "Biometric lock and Wi‑Fi sync",
    ),
    LEGAL(
        icon = RfIconKind.NOTE,
        titleEs = "Privacidad y legal",
        titleEn = "Privacy & legal",
        subtitleEs = "Términos, condiciones y privacidad",
        subtitleEn = "Terms, conditions and privacy",
    ),
}

@Composable
internal fun RenderAccountSettingsS86(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val palette = RfThemeState.currentPalette
    val vmDarkTheme = viewModel?.isDarkTheme?.collectAsState()?.value ?: palette.isDark
    val vmLanguage = viewModel?.preferredLanguage?.collectAsState()?.value ?: "Español"
    val vmReminders = viewModel?.remindersEnabled?.collectAsState()?.value ?: true
    val vmPushEnabled = viewModel?.pushNotificationsEnabled?.collectAsState()?.value ?: true
    val vmWhatsappEnabled = viewModel?.whatsappAlertsEnabled?.collectAsState()?.value ?: true
    val vmEmailReceipts = viewModel?.emailReceiptsEnabled?.collectAsState()?.value ?: true
    val vmBiometricLock = viewModel?.biometricLockEnabled?.collectAsState()?.value ?: false
    val vmAutoSyncWifiOnly = viewModel?.autoSyncWifiOnly?.collectAsState()?.value ?: false
    val wifiSyncBlockedNotice = viewModel?.wifiSyncBlockedNotice?.collectAsState()?.value
    val isWifiConnected = viewModel?.isOnWifiConnection() ?: true
    val vmProfileZone = viewModel?.profileZone?.collectAsState()?.value ?: "Piantini, Santo Domingo"
    val savedPaymentMethod = viewModel?.savedPaymentMethod?.collectAsState()?.value ?: "Transferencia Banreservas"
    val vmFormError = viewModel?.formError?.collectAsState()?.value

    var localDarkTheme by remember(vmDarkTheme) { mutableStateOf(vmDarkTheme) }
    var localLanguage by remember(vmLanguage) { mutableStateOf(vmLanguage) }
    var localReminders by remember(vmReminders) { mutableStateOf(vmReminders) }
    var localPush by remember(vmPushEnabled) { mutableStateOf(vmPushEnabled) }
    var localWhatsapp by remember(vmWhatsappEnabled) { mutableStateOf(vmWhatsappEnabled) }
    var localEmailReceipts by remember(vmEmailReceipts) { mutableStateOf(vmEmailReceipts) }
    var localBiometricLock by remember(vmBiometricLock) { mutableStateOf(vmBiometricLock) }
    var localAutoSyncWifi by remember(vmAutoSyncWifiOnly) { mutableStateOf(vmAutoSyncWifiOnly) }
    var draftEventAddress by remember(vmProfileZone) { mutableStateOf(vmProfileZone) }
    var hasInteractedAddress by remember { mutableStateOf(false) }
    var forceShowAddressError by remember { mutableStateOf(false) }
    var showAddressEditor by remember { mutableStateOf(false) }
    var addressSuccessBanner by remember { mutableStateOf<String?>(null) }

    var showPasswordEditor by remember { mutableStateOf(false) }
    var currentPasswordInput by remember { mutableStateOf("") }
    var newPasswordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var hasInteractedCurrentPassword by remember { mutableStateOf(false) }
    var hasInteractedNewPassword by remember { mutableStateOf(false) }
    var hasInteractedConfirmPassword by remember { mutableStateOf(false) }
    var forceShowPasswordErrors by remember { mutableStateOf(false) }
    var passwordErrorBanner by remember { mutableStateOf<String?>(null) }
    var passwordSuccessBanner by remember { mutableStateOf<String?>(null) }

    val isEnglish = localLanguage.startsWith("Eng", ignoreCase = true)
    val visibleCurrentPasswordError = rememberDebouncedValidationError(
        value = currentPasswordInput,
        hasInteracted = hasInteractedCurrentPassword,
        forceShowError = forceShowPasswordErrors,
    ) { value ->
        InputValidator.validatePassword(value)
    }
    val visibleNewPasswordError = rememberDebouncedValidationError(
        value = newPasswordInput,
        hasInteracted = hasInteractedNewPassword,
        forceShowError = forceShowPasswordErrors,
    ) { value ->
        InputValidator.validatePassword(value)
    }
    val visibleConfirmPasswordError = rememberDebouncedValidationError(
        value = confirmPasswordInput,
        hasInteracted = hasInteractedConfirmPassword,
        forceShowError = forceShowPasswordErrors,
    ) { value ->
        InputValidator.validatePasswordConfirmation(newPasswordInput, value)
    }
    val visibleAddressError = rememberDebouncedValidationError(
        value = draftEventAddress,
        hasInteracted = hasInteractedAddress,
        forceShowError = forceShowAddressError,
    ) { value ->
        val addressLabel = if (isEnglish) "The address" else "La dirección"
        InputValidator.validateAddress(value, fieldLabel = addressLabel, isRequired = true)
    }
    var selectedSection by remember { mutableStateOf<PersonalSettingsSection?>(null) }
    val screenTitle = if (isEnglish) "Settings" else "Ajustes"
    val activeSectionTitle = selectedSection?.let { section ->
        if (isEnglish) section.titleEn else section.titleEs
    } ?: screenTitle

    RfRadialBackgroundBox(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = false)
                        .clip(CircleShape)
                        .background(palette.surface)
                        .testTag("settings-back-button")
                        .clickable {
                            if (selectedSection != null) {
                                selectedSection = null
                            } else {
                                onSelectScreenCode("BACK:s14")
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = palette.ink, size = 22.dp)
                }
                BasicText(
                    activeSectionTitle,
                    modifier = Modifier.weight(1f),
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, lineHeight = 22.sp, color = palette.ink),
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            when (selectedSection) {
                null -> {
                    SettingsGroup86 {
                        PersonalSettingsSection.entries.forEachIndexed { index, section ->
                            if (index > 0) {
                                SettingsDivider86()
                            }
                            val sectionLabel = if (isEnglish) section.titleEn else section.titleEs
                            val sectionSubtitle = if (isEnglish) section.subtitleEn else section.subtitleEs
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 60.dp)
                                    .testTag("personal-settings-menu-${section.name.lowercase()}")
                                    .clickable { selectedSection = section }
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                RfIcon(kind = section.icon, tint = palette.ink, size = 20.dp)
                                Column(modifier = Modifier.weight(1f)) {
                                    BasicText(
                                        sectionLabel,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = palette.ink),
                                    )
                                    BasicText(
                                        sectionSubtitle,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.5.sp, color = palette.muted),
                                    )
                                }
                                RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = palette.muted, size = 16.dp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    SettingsGroup86 {
                        Row(
                            modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp).clickable { onSelectScreenCode("s88") }.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            RfIcon(kind = RfIconKind.TRASH, tint = Color(0xFFB42345), size = 20.dp)
                            BasicText(
                                if (isEnglish) "Delete account" else "Eliminar cuenta",
                                modifier = Modifier.weight(1f),
                                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color(0xFFB42345)),
                            )
                            RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = Color(0xFFB42345), size = 16.dp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    BasicText(
                        "RosaFiesta SRL · versión 2.4.0",
                        modifier = Modifier.fillMaxWidth(),
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, textAlign = TextAlign.Center, color = RfThemeState.currentPalette.muted),
                    )
                }

                PersonalSettingsSection.PREFERENCES -> {
                    SettingsGroup86 {
                        Row(
                            modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp).padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            RfIcon(kind = RfIconKind.MOON, tint = palette.ink, size = 20.dp)
                            BasicText(
                                if (isEnglish) "Theme" else "Tema",
                                modifier = Modifier.weight(1f),
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = palette.ink),
                            )
                            val lightSelected = !localDarkTheme
                            val darkSelected = localDarkTheme
                            Row(
                                modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(palette.chipBg).padding(3.dp),
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .height(30.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (lightSelected) {
                                                Brush.horizontalGradient(listOf(Color(0xFFEA4EA9), Color(0xFF865DEC)))
                                            } else {
                                                SolidColor(Color.Transparent)
                                            },
                                        )
                                        .clickable {
                                            localDarkTheme = false
                                            viewModel?.setDarkTheme(false)
                                        }
                                        .padding(horizontal = 10.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    BasicText(
                                        if (isEnglish) "Light" else "Claro",
                                        style = TextStyle(
                                            fontFamily = typo.bodyFamily,
                                            fontWeight = if (lightSelected) FontWeight.Bold else FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = if (lightSelected) Color.White else palette.muted,
                                        ),
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .height(30.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (darkSelected) {
                                                Brush.horizontalGradient(listOf(Color(0xFFEA4EA9), Color(0xFF865DEC)))
                                            } else {
                                                SolidColor(Color.Transparent)
                                            },
                                        )
                                        .clickable {
                                            localDarkTheme = true
                                            viewModel?.setDarkTheme(true)
                                        }
                                        .padding(horizontal = 10.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    BasicText(
                                        if (isEnglish) "Dark" else "Oscuro",
                                        style = TextStyle(
                                            fontFamily = typo.bodyFamily,
                                            fontWeight = if (darkSelected) FontWeight.Bold else FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = if (darkSelected) Color.White else palette.muted,
                                        ),
                                    )
                                }
                            }
                        }
                        SettingsDivider86()
                        SettingsRow86(
                            label = if (isEnglish) "Language" else "Idioma",
                            icon = RfIconKind.GLOBE,
                            onClick = {
                                localLanguage = if (isEnglish) "Español" else "English"
                                viewModel?.togglePreferredLanguage()
                            },
                        ) {
                            BasicText(localLanguage, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = palette.muted))
                        }
                        SettingsDivider86()
                        Row(
                            modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp).padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            RfIcon(kind = RfIconKind.BELL, tint = palette.ink, size = 20.dp)
                            Column(modifier = Modifier.weight(1f)) {
                                BasicText(if (isEnglish) "Event reminders" else "Recordatorios", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = palette.ink))
                                BasicText(if (isEnglish) "Before and after each event" else "Antes y después de cada evento", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = palette.muted))
                            }
                            SettingsSwitch86(
                                checked = localReminders,
                                onToggle = {
                                    localReminders = !localReminders
                                    viewModel?.toggleRemindersEnabled()
                                },
                            )
                        }
                    }
                }

                PersonalSettingsSection.AUDIO -> {
                    AppAudioSettingsCard(
                        viewModel = viewModel,
                        isEnglish = isEnglish,
                        isAdminSurface = false,
                        horizontalMargin = 20.dp,
                    )
                }

                PersonalSettingsSection.ACCOUNT -> {
                    SettingsGroup86 {
                        SettingsRow86(
                            label = if (isEnglish) "Personal profile" else "Datos personales",
                            icon = RfIconKind.USER,
                            onClick = { onSelectScreenCode("s54") },
                        )
                        SettingsDivider86()
                        SettingsRow86(
                            label = if (isEnglish) "Change password" else "Cambiar contraseña",
                            icon = RfIconKind.LOCK,
                            onClick = {
                                showPasswordEditor = !showPasswordEditor
                                passwordErrorBanner = null
                                passwordSuccessBanner = null
                                hasInteractedCurrentPassword = false
                                hasInteractedNewPassword = false
                                hasInteractedConfirmPassword = false
                                forceShowPasswordErrors = false
                            },
                        )
                        if (showPasswordEditor) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(palette.chipBg.copy(alpha = 0.45f))
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                SettingsInlineInputField86(
                                    placeholder = if (isEnglish) "Current password" else "Contraseña actual",
                                    value = currentPasswordInput,
                                    isPassword = true,
                                    maxLength = InputValidator.MAX_PASSWORD_LENGTH,
                                    isError = visibleCurrentPasswordError != null,
                                    errorMessage = visibleCurrentPasswordError,
                                    onValueChange = { updated ->
                                        currentPasswordInput = updated.take(InputValidator.MAX_PASSWORD_LENGTH)
                                        hasInteractedCurrentPassword = true
                                        passwordErrorBanner = null
                                        passwordSuccessBanner = null
                                    },
                                )
                                SettingsInlineInputField86(
                                    placeholder = if (isEnglish) "New password (6 to 64 chars)" else "Nueva contraseña (6 a 64 caracteres)",
                                    value = newPasswordInput,
                                    isPassword = true,
                                    maxLength = InputValidator.MAX_PASSWORD_LENGTH,
                                    isError = visibleNewPasswordError != null,
                                    errorMessage = visibleNewPasswordError,
                                    onValueChange = { updated ->
                                        newPasswordInput = updated.take(InputValidator.MAX_PASSWORD_LENGTH)
                                        hasInteractedNewPassword = true
                                        passwordErrorBanner = null
                                        passwordSuccessBanner = null
                                    },
                                )
                                SettingsInlineInputField86(
                                    placeholder = if (isEnglish) "Confirm new password" else "Confirmar nueva contraseña",
                                    value = confirmPasswordInput,
                                    isPassword = true,
                                    maxLength = InputValidator.MAX_PASSWORD_LENGTH,
                                    isError = visibleConfirmPasswordError != null,
                                    errorMessage = visibleConfirmPasswordError,
                                    onValueChange = { updated ->
                                        confirmPasswordInput = updated.take(InputValidator.MAX_PASSWORD_LENGTH)
                                        hasInteractedConfirmPassword = true
                                        passwordErrorBanner = null
                                        passwordSuccessBanner = null
                                    },
                                )
                                val activePasswordError = passwordErrorBanner ?: vmFormError
                                if (!activePasswordError.isNullOrBlank()) {
                                    BasicText(
                                        activePasswordError,
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp, color = RfColors.Error),
                                    )
                                }
                                if (!passwordSuccessBanner.isNullOrBlank()) {
                                    BasicText(
                                        passwordSuccessBanner ?: "",
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp, color = Color(0xFF3F560C)),
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(38.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(RosaButtonGradient)
                                            .clickable {
                                                forceShowPasswordErrors = true
                                                val firstError = InputValidator.validatePassword(currentPasswordInput)
                                                    ?: InputValidator.validatePassword(newPasswordInput)
                                                    ?: InputValidator.validatePasswordConfirmation(newPasswordInput, confirmPasswordInput)
                                                if (firstError != null) {
                                                    passwordErrorBanner = firstError
                                                    passwordSuccessBanner = null
                                                    viewModel?.soundPlayer?.playAlertError()
                                                    return@clickable
                                                }
                                                val changed = if (viewModel != null) {
                                                    viewModel.changeCurrentPassword(
                                                        currentPassword = currentPasswordInput,
                                                        newPassword = newPasswordInput,
                                                        confirmPassword = confirmPasswordInput,
                                                    )
                                                } else {
                                                    validateInlinePasswordChange86(currentPasswordInput, newPasswordInput, confirmPasswordInput)
                                                }
                                                if (changed) {
                                                    currentPasswordInput = ""
                                                    newPasswordInput = ""
                                                    confirmPasswordInput = ""
                                                    hasInteractedCurrentPassword = false
                                                    hasInteractedNewPassword = false
                                                    hasInteractedConfirmPassword = false
                                                    forceShowPasswordErrors = false
                                                    passwordErrorBanner = null
                                                    passwordSuccessBanner = if (isEnglish) "Password updated successfully." else "Contraseña actualizada correctamente."
                                                } else if (viewModel == null) {
                                                    passwordErrorBanner = if (isEnglish) {
                                                        "Verify your current password, 6+ characters, and matching confirmation."
                                                    } else {
                                                        "Verifica tu contraseña actual, mínimo 6 caracteres y confirmación igual."
                                                    }
                                                }
                                            },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        BasicText(
                                            if (isEnglish) "Save password" else "Actualizar contraseña",
                                            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White),
                                        )
                                    }
                                }
                            }
                        }
                        SettingsDivider86()
                        SettingsRow86(
                            label = if (isEnglish) "Payment methods" else "Métodos de pago",
                            icon = RfIconKind.RECEIPT,
                            onClick = { onSelectScreenCode("s82") },
                        ) {
                            BasicText(savedPaymentMethod.substringBefore(" ·"), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = palette.muted))
                        }
                        SettingsDivider86()
                        SettingsRow86(
                            label = if (isEnglish) "Default event address" else "Dirección habitual de eventos",
                            icon = RfIconKind.LOCATION,
                            onClick = {
                                showAddressEditor = !showAddressEditor
                                addressSuccessBanner = null
                                hasInteractedAddress = false
                                forceShowAddressError = false
                            },
                        ) {
                            BasicText(
                                draftEventAddress.ifBlank { "Santo Domingo" },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = 135.dp),
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = palette.muted),
                            )
                        }
                        if (showAddressEditor) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(palette.chipBg.copy(alpha = 0.45f))
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                SettingsInlineInputField86(
                                    placeholder = if (isEnglish) "Sector, street & venue reference" else "Sector, calle y referencia de entrega",
                                    value = draftEventAddress,
                                    maxLength = InputValidator.MAX_ADDRESS_LENGTH,
                                    isError = visibleAddressError != null,
                                    errorMessage = visibleAddressError,
                                    onValueChange = { updated ->
                                        draftEventAddress = updated.take(InputValidator.MAX_ADDRESS_LENGTH)
                                        hasInteractedAddress = true
                                        addressSuccessBanner = null
                                    },
                                )
                                if (!addressSuccessBanner.isNullOrBlank()) {
                                    BasicText(
                                        addressSuccessBanner ?: "",
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp, color = Color(0xFF3F560C)),
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(RosaButtonGradient)
                                        .clickable {
                                            forceShowAddressError = true
                                            val cleanZone = draftEventAddress.trim()
                                            val addressLabel = if (isEnglish) "The address" else "La dirección"
                                            val addressError = InputValidator.validateAddress(
                                                rawValue = cleanZone,
                                                fieldLabel = addressLabel,
                                                isRequired = true,
                                            )
                                            if (addressError == null) {
                                                viewModel?.setProfileZone(cleanZone)
                                                addressSuccessBanner = if (isEnglish) "Default event address saved." else "Dirección guardada correctamente."
                                                hasInteractedAddress = false
                                                forceShowAddressError = false
                                                showAddressEditor = false
                                            } else {
                                                addressSuccessBanner = null
                                                viewModel?.postFeedback(addressError)
                                            }
                                        },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    BasicText(
                                        if (isEnglish) "Save address" else "Guardar dirección",
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White),
                                    )
                                }
                            }
                        }
                    }
                }

                PersonalSettingsSection.NOTIFICATIONS -> {
                    SettingsGroup86 {
                        SettingsRow86(if (isEnglish) "Push notifications" else "Notificaciones push", RfIconKind.BELL) {
                            SettingsSwitch86(
                                checked = localPush,
                                onToggle = {
                                    localPush = !localPush
                                    viewModel?.togglePushNotifications()
                                },
                            )
                        }
                        SettingsDivider86()
                        SettingsRow86(if (isEnglish) "WhatsApp alerts" else "Mensajes por WhatsApp", RfIconKind.WHATSAPP) {
                            SettingsSwitch86(
                                checked = localWhatsapp,
                                onToggle = {
                                    localWhatsapp = !localWhatsapp
                                    viewModel?.toggleWhatsappAlerts()
                                },
                            )
                        }
                        SettingsDivider86()
                        SettingsRow86(if (isEnglish) "Email receipts" else "Recibos por correo", RfIconKind.MAIL) {
                            SettingsSwitch86(
                                checked = localEmailReceipts,
                                onToggle = {
                                    localEmailReceipts = !localEmailReceipts
                                    viewModel?.toggleEmailReceipts()
                                },
                            )
                        }
                    }
                }

                PersonalSettingsSection.SECURITY_SYNC -> {
                    SettingsGroup86 {
                        Row(
                            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            RfIcon(kind = RfIconKind.SHIELD, tint = palette.ink, size = 20.dp)
                            Column(modifier = Modifier.weight(1f)) {
                                BasicText(
                                    if (isEnglish) "Biometric app lock" else "Bloqueo biométrico",
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = palette.ink),
                                )
                                BasicText(
                                    if (isEnglish) "Require Face ID / Touch ID / password on open" else "Protege tus eventos y pagos al abrir la app",
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.5.sp, color = palette.muted),
                                )
                            }
                            SettingsSwitch86(
                                checked = localBiometricLock,
                                onToggle = {
                                    localBiometricLock = !localBiometricLock
                                    viewModel?.toggleBiometricLock()
                                },
                            )
                        }
                        SettingsDivider86()
                        Row(
                            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            RfIcon(kind = RfIconKind.REFRESH, tint = palette.ink, size = 20.dp)
                            Column(modifier = Modifier.weight(1f)) {
                                BasicText(
                                    if (isEnglish) "Sync on Wi‑Fi only" else "Sincronizar solo con Wi‑Fi",
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = palette.ink),
                                )
                                val syncStatusSubtext = when {
                                    !wifiSyncBlockedNotice.isNullOrBlank() -> wifiSyncBlockedNotice
                                    isWifiConnected -> if (isEnglish) "Wi‑Fi connected · auto sync active" else "Wi‑Fi conectado · sincronización activa"
                                    else -> if (isEnglish) "No Wi‑Fi · background sync paused" else "Sin Wi‑Fi · sincronización pausada"
                                }
                                BasicText(
                                    syncStatusSubtext,
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.5.sp, color = palette.muted),
                                )
                            }
                            SettingsSwitch86(
                                checked = localAutoSyncWifi,
                                onToggle = {
                                    localAutoSyncWifi = !localAutoSyncWifi
                                    viewModel?.toggleAutoSyncWifiOnly()
                                },
                            )
                        }
                        if (localBiometricLock) {
                            SettingsDivider86()
                            SettingsRow86(
                                label = if (isEnglish) "Lock session now" else "Bloquear sesión ahora",
                                icon = RfIconKind.LOCK,
                                onClick = { viewModel?.lockAppIfProtected() },
                            )
                        }
                    }
                }

                PersonalSettingsSection.LEGAL -> {
                    SettingsGroup86 {
                        SettingsRow86(
                            if (isEnglish) "Terms & conditions" else "Términos y condiciones",
                            RfIconKind.NOTE,
                            onClick = {
                                pendingTermsSegmentIndex87 = 0
                                onSelectScreenCode("s87")
                            },
                        )
                        SettingsDivider86()
                        SettingsRow86(
                            if (isEnglish) "Privacy policy" else "Política de privacidad",
                            RfIconKind.SHIELD,
                            onClick = {
                                pendingTermsSegmentIndex87 = 1
                                onSelectScreenCode("s87")
                            },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(RfDimens.BottomBarContentClearance))
        }
    }
}

private fun validateInlinePasswordChange86(
    currentPassword: String,
    newPassword: String,
    confirmPassword: String,
): Boolean {
    if (InputValidator.validatePassword(currentPassword) != null) return false
    if (InputValidator.validatePassword(newPassword) != null) return false
    return InputValidator.validatePasswordConfirmation(newPassword, confirmPassword) == null
}

@Composable
private fun SettingsInlineInputField86(
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    isPassword: Boolean = false,
    maxLength: Int = Int.MAX_VALUE,
    isError: Boolean = false,
    errorMessage: String? = null,
) {
    val typo = LocalRfTypography.current
    val palette = RfThemeState.currentPalette
    val borderColor = if (isError) RfColors.Error else palette.line
    val visualMask = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(palette.surface)
                .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            if (value.isEmpty()) {
                BasicText(placeholder, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.5.sp, color = palette.muted))
            }
            BasicTextField(
                value = value,
                onValueChange = { updatedText -> onValueChange(updatedText.take(maxLength)) },
                singleLine = true,
                visualTransformation = visualMask,
                textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.5.sp, color = palette.ink),
                cursorBrush = SolidColor(Color(0xFFC2338F)),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        RfValidationErrorText(errorMessage = errorMessage)
    }
}

@Composable
private fun SettingsCaption86(label: String) {
    val typo = LocalRfTypography.current

    BasicText(
        label,
        modifier = Modifier.padding(horizontal = 20.dp),
        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp, color = RfThemeState.currentPalette.muted),
    )
}

@Composable
private fun SettingsGroup86(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(RfThemeState.currentPalette.surface)
            .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp)),
        content = content,
    )
}

@Composable
private fun SettingsDivider86() {
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(RfThemeState.currentPalette.lineSoft))
}

@Composable
private fun SettingsRow86(
    label: String,
    icon: RfIconKind,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 54.dp)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RfIcon(kind = icon, tint = Color(0xFF3B3550), size = 20.dp)
        BasicText(
            label,
            modifier = Modifier.weight(1f),
            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = RfThemeState.currentPalette.ink),
        )
        trailing()
        if (onClick != null) {
            RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = RfThemeState.currentPalette.muted, size = 16.dp)
        }
    }
}

@Composable
private fun SettingsSwitch86(
    checked: Boolean,
    onToggle: () -> Unit,
) {
    Box(
        modifier = Modifier
            .width(46.dp)
            .height(28.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(
                if (checked) {
                    Brush.linearGradient(listOf(Color(0xFFEA4EA9), Color(0xFF865DEC)))
                } else {
                    Brush.linearGradient(listOf(Color(0xFFE3DCE8), Color(0xFFE3DCE8)))
                },
            )
            .clickable(onClick = onToggle),
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Box(modifier = Modifier.padding(horizontal = 3.dp).size(22.dp).clip(CircleShape).background(RfThemeState.currentPalette.surface).shadow(1.dp, CircleShape))
    }
}

@Composable
internal fun AppAudioSettingsCard(
    viewModel: RosaFiestaViewModel?,
    isEnglish: Boolean = false,
    isAdminSurface: Boolean = false,
    horizontalMargin: Dp = 0.dp,
) {
    val typo = LocalRfTypography.current
    val personalPalette = RfThemeState.currentPalette
    val vmAudioConfig = viewModel?.audioConfig?.collectAsState()?.value ?: AppAudioConfig.DEFAULT
    var localConfig by remember(vmAudioConfig) { mutableStateOf(vmAudioConfig) }

    val surfaceColor = if (isAdminSurface) Color.White else personalPalette.surface
    val borderColor = if (isAdminSurface) Color(0xFFECECF5) else personalPalette.line
    val dividerColor = if (isAdminSurface) Color(0xFFF0F0F6) else personalPalette.lineSoft
    val inkColor = if (isAdminSurface) Color(0xFF151729) else personalPalette.ink
    val mutedColor = if (isAdminSurface) Color(0xFF5F6377) else personalPalette.muted
    val chipBgColor = if (isAdminSurface) Color(0xFFF2F2F8) else personalPalette.chipBg
    val accentGradient = if (isAdminSurface) {
        Brush.horizontalGradient(listOf(Color(0xFF5B6CF0), Color(0xFF865DEC)))
    } else {
        Brush.horizontalGradient(listOf(Color(0xFFEA4EA9), Color(0xFF865DEC)))
    }
    val accentTint = if (isAdminSurface) Color(0xFF4B47D6) else Color(0xFFC2338F)
    val volumePercent = localConfig.volumePercent()
    val statusSummary = if (!localConfig.soundEffectsEnabled) {
        if (isEnglish) "Muted" else "Silenciados"
    } else if (isEnglish) {
        "Active · $volumePercent%"
    } else {
        "Activos · $volumePercent %"
    }
    val speakerIcon = if (localConfig.soundEffectsEnabled) RfIconKind.VOLUME_ON else RfIconKind.VOLUME_OFF

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalMargin)
            .clip(RoundedCornerShape(20.dp))
            .background(surfaceColor)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            RfIcon(kind = speakerIcon, tint = accentTint, size = 20.dp)
            Column(modifier = Modifier.weight(1f)) {
                BasicText(
                    text = if (isEnglish) "Sound effects" else "Efectos de sonido",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = inkColor),
                )
                BasicText(
                    text = statusSummary,
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.5.sp, color = mutedColor),
                )
            }
            SettingsSwitch86(
                checked = localConfig.soundEffectsEnabled,
                onToggle = {
                    val nextEnabled = !localConfig.soundEffectsEnabled
                    localConfig = localConfig.copy(soundEffectsEnabled = nextEnabled)
                    viewModel?.setSoundEffectsEnabled(nextEnabled)
                },
            )
        }

        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(dividerColor))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .graphicsLayer { alpha = if (localConfig.soundEffectsEnabled) 1f else 0.55f },
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                BasicText(
                    text = if (isEnglish) "Effects volume" else "Volumen de efectos",
                    modifier = Modifier.weight(1f),
                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = inkColor),
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(chipBgColor)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    BasicText(
                        text = "$volumePercent %",
                        modifier = Modifier.testTag("audio-volume-value"),
                        style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = inkColor),
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(accentGradient)
                        .clickable {
                            viewModel?.previewAudioEffect(SoundEffect.ROSA_REPLY)
                        }
                        .padding(horizontal = 11.dp, vertical = 5.dp)
                        .testTag("audio-preview-button"),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        RfIcon(kind = RfIconKind.PLAY, tint = Color.White, size = 12.dp, filled = true)
                        BasicText(
                            text = if (isEnglish) "Test" else "Probar",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White),
                        )
                    }
                }
            }

            AudioVolumeSliderRow(
                volumeFraction = localConfig.normalizedVolume,
                trackBackground = chipBgColor,
                fillBrush = accentGradient,
                thumbFill = surfaceColor,
                thumbBorder = accentTint,
                iconTint = accentTint,
                onVolumeFractionChange = { nextFraction ->
                    localConfig = localConfig.copy(masterVolume = nextFraction)
                    viewModel?.setSoundVolume(nextFraction)
                },
                onStepVolume = { deltaPercent ->
                    localConfig = localConfig.withVolumePercent(localConfig.volumePercent() + deltaPercent)
                    viewModel?.stepSoundVolume(deltaPercent)
                },
            )
        }

        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(dividerColor))

        AudioCategoryToggleRow(
            title = if (isEnglish) "Buttons, catalog & sketch" else "Botones, catálogo y boceto",
            subtitle = if (isEnglish) "Taps, items and checklist" else "Toques, artículos y checklist",
            checked = localConfig.interactionSoundsEnabled,
            inkColor = inkColor,
            mutedColor = mutedColor,
            testTagName = "audio-category-interaction",
            onToggle = {
                val nextChecked = !localConfig.interactionSoundsEnabled
                localConfig = localConfig.copy(interactionSoundsEnabled = nextChecked)
                viewModel?.setAudioCategoryEnabled(SoundEffectCategory.INTERACTION, nextChecked)
            },
        )
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(dividerColor))
        AudioCategoryToggleRow(
            title = if (isEnglish) "Rosa AI assistant" else "Asistente Rosa IA",
            subtitle = if (isEnglish) "Prompts and smart replies" else "Mensajes enviados y respuestas",
            checked = localConfig.rosaAssistantSoundsEnabled,
            inkColor = inkColor,
            mutedColor = mutedColor,
            testTagName = "audio-category-rosa",
            onToggle = {
                val nextChecked = !localConfig.rosaAssistantSoundsEnabled
                localConfig = localConfig.copy(rosaAssistantSoundsEnabled = nextChecked)
                viewModel?.setAudioCategoryEnabled(SoundEffectCategory.ROSA_ASSISTANT, nextChecked)
            },
        )
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(dividerColor))
        AudioCategoryToggleRow(
            title = if (isEnglish) "Payments & alerts" else "Pagos y alertas",
            subtitle = if (isEnglish) "Confirmations and error notices" else "Confirmaciones y avisos de error",
            checked = localConfig.paymentAlertSoundsEnabled,
            inkColor = inkColor,
            mutedColor = mutedColor,
            testTagName = "audio-category-alerts",
            onToggle = {
                val nextChecked = !localConfig.paymentAlertSoundsEnabled
                localConfig = localConfig.copy(paymentAlertSoundsEnabled = nextChecked)
                viewModel?.setAudioCategoryEnabled(SoundEffectCategory.PAYMENTS_ALERTS, nextChecked)
            },
        )

        if (localConfig.isModifiedFromDefault()) {
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(dividerColor))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        localConfig = AppAudioConfig.DEFAULT
                        viewModel?.resetAudioDefaults()
                    }
                    .padding(horizontal = 16.dp, vertical = 11.dp),
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    text = if (isEnglish) "Reset audio defaults (75%)" else "Restaurar audio predeterminado (75 %)",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = accentTint),
                )
            }
        }
    }
}

@Composable
private fun AudioVolumeSliderRow(
    volumeFraction: Float,
    trackBackground: Color,
    fillBrush: Brush,
    thumbFill: Color,
    thumbBorder: Color,
    iconTint: Color,
    onVolumeFractionChange: (Float) -> Unit,
    onStepVolume: (Int) -> Unit,
) {
    var trackWidthPx by remember { mutableStateOf(1f) }
    val clampedFraction = volumeFraction.coerceIn(0f, 1f)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(trackBackground)
                .clickable { onStepVolume(-10) }
                .testTag("audio-volume-minus"),
            contentAlignment = Alignment.Center,
        ) {
            RfIcon(kind = RfIconKind.MINUS, tint = iconTint, size = 14.dp)
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .height(26.dp)
                .onSizeChanged { measuredSize ->
                    val widthFloat = measuredSize.width.toFloat()
                    if (widthFloat > 0f) {
                        trackWidthPx = widthFloat
                    }
                }
                .pointerInput(trackWidthPx) {
                    detectTapGestures { tapOffset ->
                        val nextFraction = (tapOffset.x / trackWidthPx).coerceIn(0f, 1f)
                        onVolumeFractionChange(nextFraction)
                    }
                }
                .pointerInput(trackWidthPx) {
                    detectHorizontalDragGestures { change, _ ->
                        change.consume()
                        val nextFraction = (change.position.x / trackWidthPx).coerceIn(0f, 1f)
                        onVolumeFractionChange(nextFraction)
                    }
                },
            contentAlignment = Alignment.CenterStart,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(trackBackground),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = clampedFraction)
                        .height(8.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(fillBrush),
                )
            }
            val thumbCenterFraction = clampedFraction.coerceIn(0.03f, 0.97f)
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                if (thumbCenterFraction > 0.03f) {
                    Spacer(modifier = Modifier.weight(thumbCenterFraction))
                }
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .shadow(2.dp, CircleShape)
                        .clip(CircleShape)
                        .background(thumbFill)
                        .border(2.dp, thumbBorder, CircleShape),
                )
                val trailingWeight = (1f - thumbCenterFraction).coerceAtLeast(0.001f)
                Spacer(modifier = Modifier.weight(trailingWeight))
            }
        }
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(trackBackground)
                .clickable { onStepVolume(10) }
                .testTag("audio-volume-plus"),
            contentAlignment = Alignment.Center,
        ) {
            RfIcon(kind = RfIconKind.PLUS, tint = iconTint, size = 14.dp)
        }
    }
}

@Composable
private fun AudioCategoryToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    inkColor: Color,
    mutedColor: Color,
    testTagName: String,
    onToggle: () -> Unit,
) {
    val typo = LocalRfTypography.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag(testTagName),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            BasicText(
                text = title,
                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = inkColor),
            )
            BasicText(
                text = subtitle,
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = mutedColor),
            )
        }
        SettingsSwitch86(
            checked = checked,
            onToggle = onToggle,
        )
    }
}

private var pendingTermsSegmentIndex87: Int = 0

@Composable
internal fun RenderTermsS87(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val cardShape = RoundedCornerShape(20.dp)
    var selectedSegmentIndex by remember {
        val initialSegment = pendingTermsSegmentIndex87
        pendingTermsSegmentIndex87 = 0
        mutableStateOf(initialSegment)
    }
    val businessSettings = viewModel?.businessSettings?.collectAsState()?.value
    val commercialBrand = businessSettings?.businessName ?: "RosaFiesta Eventos & Alquileres"
    val legalEntityName = "RosaFiesta SRL ($commercialBrand)"
    val rncNumber = businessSettings?.rnc ?: "1-31-84920-2"
    val companyAddress = businessSettings?.address ?: "Av. Winston Churchill 1099, Piantini, Santo Domingo"

    RfRadialBackgroundBox(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .rfIconButtonShadow(CircleShape, isAdmin = RfThemeState.currentPalette.isAdmin)
                            .clip(CircleShape)
                            .background(RfThemeState.currentPalette.surface)
                            .clickable { onSelectScreenCode("BACK:s86") },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.BACK, tint = RfThemeState.currentPalette.ink, size = 22.dp)
                    }
                    BasicText(
                        text = "Términos y privacidad",
                        modifier = Modifier.weight(1f),
                        style = TextStyle(
                            fontFamily = typo.displayFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            lineHeight = 22.sp,
                            letterSpacing = (-0.4).sp,
                            color = RfThemeState.currentPalette.ink,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(RfThemeState.currentPalette.chipBg)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    TermsSegment87(
                        label = "Términos",
                        selected = selectedSegmentIndex == 0,
                        onClick = { selectedSegmentIndex = 0 },
                        modifier = Modifier.weight(1f),
                    )
                    TermsSegment87(
                        label = "Privacidad",
                        selected = selectedSegmentIndex == 1,
                        onClick = { selectedSegmentIndex = 1 },
                        modifier = Modifier.weight(1f),
                    )
                }

                BasicText(
                    text = "$legalEntityName · RNC $rncNumber · Vigente octubre 2026",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = RfThemeState.currentPalette.muted),
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(cardShape, isAdmin = RfThemeState.currentPalette.isAdmin)
                        .clip(cardShape)
                        .background(RfThemeState.currentPalette.surface)
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    if (selectedSegmentIndex == 0) {
                        TermsSection87(
                            title = "1. Contratación y bloqueo de inventario",
                            body = "El contrato de alquiler y montaje se celebra con $legalEntityName (RNC $rncNumber), con domicilio en $companyAddress. La disponibilidad de fecha e inventario queda bloqueada al confirmarse el anticipo y emitirse el comprobante oficial.",
                        )
                        TermsSection87(
                            title = "2. Anticipos, impuestos y liquidación de saldo",
                            body = "Para formalizar la reserva se requiere un anticipo del 30 % del monto cotizado (ITBIS del 18 % incluido según normativa de la DGII). El saldo restante debe liquidarse al menos 24 horas antes del inicio del montaje.",
                        )
                        TermsSection87(
                            title = "3. Reprogramación y reembolsos escalonados",
                            body = "El primer cambio de fecha solicitado con más de 48 horas de antelación no genera recargo, sujeto a disponibilidad. En caso de cancelación, el reembolso sobre el anticipo pagado aplica así: 90 % con más de 7 días de anticipación, 50 % entre 3 y 7 días, y sin reembolso con menos de 3 días.",
                        )
                        TermsSection87(
                            title = "4. Entrega, custodia y reposición de piezas",
                            body = "Durante el evento el cliente asume el resguardo de las piezas entregadas bajo acta de montaje. Cualquier rotura, extravío o deterioro irreparable se documenta fotográficamente al desmontar y se liquida conforme al valor unitario de reposición vigente en catálogo.",
                        )
                    } else {
                        TermsSection87(
                            title = "1. Responsable del tratamiento de datos",
                            body = "$legalEntityName (RNC $rncNumber), domiciliada en $companyAddress, trata tus datos personales en estricto cumplimiento de la Ley No. 172-13 sobre Protección de Datos de Carácter Personal de la República Dominicana.",
                        )
                        TermsSection87(
                            title = "2. Datos recopilados y finalidad operativa",
                            body = "Recopilamos nombre, correo electrónico, teléfono de contacto, dirección del evento e historial de cotizaciones exclusivamente para coordinar entregas, emitir comprobantes fiscales, enviar alertas logísticas y brindar asistencia personalizada.",
                        )
                        TermsSection87(
                            title = "3. Seguridad transaccional y retención fiscal",
                            body = "RosaFiesta no almacena códigos CVV ni numeración completa de tarjetas bancarias. Los comprobantes de pago y facturas se conservan durante 5 años conforme a las obligaciones tributarias ante la Dirección General de Impuestos Internos (DGII).",
                        )
                        TermsSection87(
                            title = "4. Ejercicio de derechos ARCO y supresión",
                            body = "Puedes acceder, rectificar o solicitar la eliminación definitiva de tu perfil desde Ajustes > Eliminar cuenta siempre que no mantengas eventos activos con inventario comprometido o saldos abiertos.",
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(RfThemeState.currentPalette.surface),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(RfThemeState.currentPalette.line),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                ) {
                    RfButton(
                        label = "Entendido",
                        onClick = { onSelectScreenCode("BACK:s86") },
                        modifier = Modifier.fillMaxWidth(),
                        style = RfButtonStyle.PRIMARY,
                    )
                }
            }
        }
    }
}

@Composable
private fun TermsSegment87(
    label: String,
    selected: Boolean,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val typo = LocalRfTypography.current

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(38.dp)
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (selected) {
                    Modifier
                        .rfCardShadow(RoundedCornerShape(12.dp), isAdmin = RfThemeState.currentPalette.isAdmin)
                        .background(RfThemeState.currentPalette.surface)
                } else {
                    Modifier.background(Color.Transparent)
                },
            )
            .clickable(onClick = onClick),
    ) {
        BasicText(
            text = label,
            style = TextStyle(
                fontFamily = typo.bodyFamily,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                fontSize = 14.sp,
                color = if (selected) Color(0xFF1A1A2E) else Color(0xFF62627A),
            ),
        )
    }
}

@Composable
private fun TermsSection87(
    title: String,
    body: String,
) {
    val typo = LocalRfTypography.current

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        BasicText(
            text = title,
            style = TextStyle(
                fontFamily = typo.bodyFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                color = RfThemeState.currentPalette.ink,
            ),
        )
        BasicText(
            text = body,
            style = TextStyle(
                fontFamily = typo.bodyFamily,
                fontSize = 14.sp,
                lineHeight = 22.sp,
                color = RfThemeState.currentPalette.muted,
            ),
        )
    }
}

private fun validateDeleteConfirmation88(rawValue: String): String? {
    val trimmed = rawValue.trim()
    if (trimmed.isEmpty()) return "Escribe ELIMINAR en mayúsculas para confirmar."
    if (trimmed != "ELIMINAR") return "Debes escribir exactamente ELIMINAR en mayúsculas."
    return null
}

@Composable
internal fun RenderDeleteAccountS88(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    var confirmationText by remember { mutableStateOf("") }
    var hasInteractedConfirmation by remember { mutableStateOf(false) }
    var forceShowConfirmationError by remember { mutableStateOf(false) }
    var deletionErrorText by remember { mutableStateOf<String?>(null) }

    val visibleConfirmationError = rememberDebouncedValidationError(
        value = confirmationText,
        hasInteracted = hasInteractedConfirmation,
        forceShowError = forceShowConfirmationError,
    ) { value ->
        validateDeleteConfirmation88(value)
    }

    viewModel?.events?.collectAsState()?.value
    viewModel?.currentAuthenticatedUser?.collectAsState()?.value
    viewModel?.profileName?.collectAsState()?.value
    viewModel?.profileEmail?.collectAsState()?.value
    viewModel?.isExplicitGuestSession?.collectAsState()?.value
    val vmFormError = viewModel?.formError?.collectAsState()?.value

    val activeEventsCount = viewModel?.activeClientEventCount() ?: 0
    val matchesDeleteKeyword = confirmationText.trim() == "ELIMINAR"
    val hasZeroActiveEvents = activeEventsCount == 0
    val canConfirmDeletion = if (matchesDeleteKeyword) hasZeroActiveEvents else false

    RfRadialBackgroundBox(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .rfIconButtonShadow(CircleShape, isAdmin = false)
                            .clip(CircleShape)
                            .background(RfThemeState.currentPalette.surface)
                            .clickable { onSelectScreenCode("BACK") },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.BACK, tint = RfThemeState.currentPalette.ink, size = 22.dp)
                    }

                    BasicText(
                        text = "Eliminar cuenta",
                        style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = RfThemeState.currentPalette.ink),
                        modifier = Modifier.weight(1f),
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(84.dp)
                            .border(10.dp, Color(0xFFFDF1F3), CircleShape)
                            .clip(CircleShape)
                            .background(Color(0xFFFCE3E8)),
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.TRASH, tint = Color(0xFFB42345), size = 36.dp)
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    BasicText(
                        text = "¿Eliminar tu cuenta?",
                        style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = RfThemeState.currentPalette.ink),
                    )

                    BasicText(
                        text = "Esta acción es permanente. Si eliminas tu cuenta:",
                        modifier = Modifier.widthIn(max = 310.dp),
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 14.5.sp,
                            lineHeight = 21.75.sp,
                            color = RfThemeState.currentPalette.muted,
                            textAlign = TextAlign.Center,
                        ),
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = false)
                        .clip(RoundedCornerShape(20.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
                            RfIcon(kind = RfIconKind.CLOSE, tint = Color(0xFFB42345), size = 16.dp)
                            BasicText("Perderás tus eventos, cotizaciones y bocetos.", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.5.sp, lineHeight = 20.sp, color = RfThemeState.currentPalette.ink))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
                            RfIcon(kind = RfIconKind.CLOSE, tint = Color(0xFFB42345), size = 16.dp)
                            BasicText("Se borrarán tus favoritos y conversaciones con Rosa.", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.5.sp, lineHeight = 20.sp, color = RfThemeState.currentPalette.ink))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
                            RfIcon(kind = RfIconKind.CLOSE, tint = Color(0xFFB42345), size = 16.dp)
                            BasicText("Tus recibos se conservan 5 años por ley y te los enviamos por correo.", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.5.sp, lineHeight = 20.sp, color = RfThemeState.currentPalette.ink))
                        }
                    }
                }

                if (activeEventsCount > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFFFF2C9))
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
                            RfIcon(kind = RfIconKind.WARNING, tint = Color(0xFF5E4300), size = 18.dp)
                            BasicText(
                                text = buildAnnotatedString {
                                    append("Tienes ")
                                    val eventLabel = if (activeEventsCount == 1) "1 evento activo" else "$activeEventsCount eventos activos"
                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(eventLabel) }
                                    append(". Cancélalos o espera a que terminen antes de poder eliminar tu cuenta.")
                                },
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.5.sp, lineHeight = 18.9.sp, color = Color(0xFF5E4300)),
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFEEF6D6))
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
                            RfIcon(kind = RfIconKind.CHECK, tint = Color(0xFF3F560C), size = 18.dp)
                            BasicText(
                                text = "No tienes eventos activos pendientes. Escribe ELIMINAR en mayúsculas para confirmar.",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.5.sp, lineHeight = 18.9.sp, color = Color(0xFF3F560C)),
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    BasicText("Escribe ELIMINAR para confirmar", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF3B3550)))
                    val confirmationBorderColor = if (visibleConfirmationError != null) RfColors.Error else RfThemeState.currentPalette.line
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(RfThemeState.currentPalette.surface)
                            .border(1.dp, confirmationBorderColor, RoundedCornerShape(16.dp))
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        if (confirmationText.isEmpty()) {
                            BasicText("ELIMINAR", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = RfThemeState.currentPalette.muted))
                        }
                        BasicTextField(
                            value = confirmationText,
                            onValueChange = { updatedText ->
                                confirmationText = updatedText.take(20)
                                hasInteractedConfirmation = true
                                deletionErrorText = null
                            },
                            singleLine = true,
                            textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = RfThemeState.currentPalette.ink),
                            cursorBrush = SolidColor(Color(0xFFB42345)),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    val displayedError = visibleConfirmationError ?: deletionErrorText ?: vmFormError
                    RfValidationErrorText(errorMessage = displayedError)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(RfThemeState.currentPalette.surface),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(RfThemeState.currentPalette.line),
                )
                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .then(
                                if (canConfirmDeletion) {
                                    Modifier.rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = false)
                                } else {
                                    Modifier
                                },
                            )
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (canConfirmDeletion) Color(0xFFB42345) else Color(0xFFF1D5DB))
                            .clickable {
                                forceShowConfirmationError = true
                                val confirmationError = validateDeleteConfirmation88(confirmationText)
                                if (confirmationError != null) {
                                    deletionErrorText = confirmationError
                                    viewModel?.soundPlayer?.playAlertError()
                                    return@clickable
                                }
                                if (!hasZeroActiveEvents) {
                                    deletionErrorText = "Cancela tus eventos activos antes de eliminar tu cuenta."
                                    viewModel?.soundPlayer?.playAlertError()
                                    return@clickable
                                }
                                val deleted = if (viewModel != null) {
                                    viewModel.deleteCurrentUserAccount()
                                } else {
                                    true
                                }
                                if (deleted) {
                                    onSelectScreenCode("s06")
                                } else {
                                    deletionErrorText = "No fue posible eliminar la cuenta activa."
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            "Eliminar cuenta",
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = if (canConfirmDeletion) Color.White else Color.White.copy(alpha = 0.78f),
                            ),
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clickable { onSelectScreenCode("BACK:s54") },
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText("Mantener mi cuenta", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = RfThemeState.currentPalette.muted))
                    }
                }
            }
        }
    }
}

@Composable
internal fun RenderPersonalEmptyStateS89(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    var selectedTabIndex by remember { mutableStateOf(0) }
    val vmCategories = viewModel?.catalogCategories?.collectAsState()?.value
    val suggestedCategories = if (vmCategories != null) {
        vmCategories.map { category -> category.name.trim() }.filter { name -> name.isNotEmpty() }
    } else {
        listOf("Mobiliario", "Arcos y globos", "Diseño floral", "Mesas de dulces")
    }

    RfRadialBackgroundBox(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 116.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            BasicText(
                text = "Mis eventos",
                style = TextStyle(
                    fontFamily = typo.displayFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 26.sp,
                    color = RfThemeState.currentPalette.ink,
                ),
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EmptyStateChip89(
                    label = "Próximos",
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                )
                EmptyStateChip89(
                    label = "Pasados",
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                BalloonsIllustration89(modifier = Modifier.size(138.dp))

                BasicText(
                    text = if (selectedTabIndex == 0) "Aún no tienes eventos próximos" else "Sin eventos pasados",
                    style = TextStyle(
                        fontFamily = typo.displayFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        color = RfThemeState.currentPalette.ink,
                        textAlign = TextAlign.Center,
                    ),
                )
                BasicText(
                    text = "Cuéntale a Rosa qué celebras o elige una categoría para armar tu lista en 2 minutos.",
                    modifier = Modifier.widthIn(max = 300.dp),
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontSize = 15.sp,
                        lineHeight = 22.5.sp,
                        color = RfThemeState.currentPalette.muted,
                        textAlign = TextAlign.Center,
                    ),
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    suggestedCategories.forEach { categoryLabel ->
                        Box(
                            modifier = Modifier
                                .height(34.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(Color(0xFFFDE6F2))
                                .clickable {
                                    viewModel?.resetCatalogFilters()
                                    viewModel?.setSelectedCategory(categoryLabel)
                                    onSelectScreenCode("s18")
                                }
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicText(
                                text = categoryLabel,
                                style = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFFC2338F),
                                ),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = false)
                            .clip(RoundedCornerShape(16.dp))
                            .background(RosaButtonGradient)
                            .clickable { onSelectScreenCode("s26") },
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            RfIcon(kind = RfIconKind.SPARKLE, tint = Color.White, size = 18.dp)
                            BasicText(
                                text = "Planear con Rosa",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp),
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .rfIconButtonShadow(RoundedCornerShape(16.dp), isAdmin = false)
                            .clip(RoundedCornerShape(16.dp))
                            .background(RfThemeState.currentPalette.surface)
                            .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(16.dp))
                            .clickable {
                                viewModel?.resetCatalogFilters()
                                onSelectScreenCode("s18")
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = "Explorar el catálogo",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = RfThemeState.currentPalette.ink, fontSize = 16.sp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyStateChip89(label: String, selected: Boolean, onClick: () -> Unit) {
    val typo = LocalRfTypography.current
    Box(
        modifier = Modifier
            .height(36.dp)
            .clip(CircleShape)
            .background(
                if (selected) {
                    Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9)))
                } else {
                    SolidColor(RfThemeState.currentPalette.chipBg)
                },
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = label,
            style = TextStyle(
                fontFamily = typo.bodyFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = if (selected) Color.White else Color(0xFF1A1A2E),
            ),
        )
    }
}

@Composable
private fun BalloonsIllustration89(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val s = size.width / 120f

        // Strings
        val stringColor = Color(0x806E5A72)
        drawPath(
            path = Path().apply {
                moveTo(46f * s, 62f * s)
                cubicTo(48f * s, 80f * s, 58f * s, 90f * s, 60f * s, 110f * s)
            },
            color = stringColor,
            style = Stroke(width = 1.4f * s),
        )
        drawPath(
            path = Path().apply {
                moveTo(76f * s, 58f * s)
                cubicTo(74f * s, 78f * s, 62f * s, 90f * s, 60f * s, 110f * s)
            },
            color = stringColor,
            style = Stroke(width = 1.4f * s),
        )
        drawPath(
            path = Path().apply {
                moveTo(60f * s, 78f * s)
                cubicTo(60f * s, 92f * s, 60f * s, 100f * s, 60f * s, 110f * s)
            },
            color = stringColor,
            style = Stroke(width = 1.4f * s),
        )

        // Balloons
        fun balloon(cx: Float, cy: Float, rx: Float, ry: Float, color: Color) {
            drawOval(
                color = color,
                topLeft = Offset((cx - rx) * s, (cy - ry) * s),
                size = Size(rx * 2f * s, ry * 2f * s),
            )
        }
        balloon(46f, 42f, 17f, 20f, Color(0xFFEC4F97))
        balloon(76f, 38f, 17f, 20f, Color(0xFF7650E8))
        balloon(60f, 60f, 16f, 19f, Color(0xFFF5B53B))

        // Highlights
        fun highlight(cx: Float, cy: Float) {
            drawOval(
                color = Color.White.copy(alpha = 0.55f),
                topLeft = Offset((cx - 3.5f) * s, (cy - 6f) * s),
                size = Size(7f * s, 12f * s),
            )
        }
        highlight(40f, 34f)
        highlight(70f, 30f)
        highlight(54f, 52f)

        // Knots
        fun knot(x1: Float, x2: Float, y1: Float, y2: Float, color: Color) {
            drawPath(
                path = Path().apply {
                    moveTo(x1 * s, y1 * s)
                    lineTo(x2 * s, y1 * s)
                    lineTo(((x1 + x2) / 2f) * s, y2 * s)
                    close()
                },
                color = color,
            )
        }
        knot(44f, 48f, 61.5f, 65f, Color(0xFFEC4F97))
        knot(74f, 78f, 57.5f, 61f, Color(0xFF7650E8))
        knot(58f, 62f, 78.5f, 82f, Color(0xFFF5B53B))
    }
}

// ==============================================================================
// s90 · ESTADO CARGANDO PERSONAL
// ==============================================================================
// s90 · ESTADO CARGANDO PERSONAL
// ==============================================================================
@Composable
internal fun RenderPersonalLoadingStateS90(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val skGradient = rememberRfSkeletonBrush(isAdmin = false, animate = true)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RfThemeState.currentPalette.bg),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(RfThemeState.currentPalette.bg)
            drawRect(
                Brush.radialGradient(
                    listOf(RfThemeState.currentPalette.radialPrimary, RfThemeState.currentPalette.radialPrimary.copy(alpha = 0f)),
                    center = Offset(0f, 0f),
                    radius = size.width * 1.1f,
                ),
            )
            drawRect(
                Brush.radialGradient(
                    listOf(RfThemeState.currentPalette.radialSecondary, RfThemeState.currentPalette.radialSecondary.copy(alpha = 0f)),
                    center = Offset(size.width, size.height * 0.15f),
                    radius = size.width * 0.8f,
                ),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 116.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = false)
                        .clip(CircleShape)
                        .background(RfThemeState.currentPalette.surface)
                        .clickable { onSelectScreenCode("BACK:s14") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = RfThemeState.currentPalette.ink, size = 22.dp)
                }
                BasicText(
                    text = "Catálogo",
                    style = TextStyle(
                        fontFamily = typo.displayFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 26.sp,
                        lineHeight = 28.sp,
                        color = RfThemeState.currentPalette.ink,
                    ),
                    modifier = Modifier.weight(1f),
                )
                Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(skGradient))
            }

            // Search bar: height 52dp, radius 16dp
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(skGradient),
            )

            // 4 chips: heights 36dp, widths 96dp, 110dp, 80dp, 90dp
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(modifier = Modifier.width(96.dp).height(36.dp).clip(RoundedCornerShape(999.dp)).background(skGradient))
                Box(modifier = Modifier.width(110.dp).height(36.dp).clip(RoundedCornerShape(999.dp)).background(skGradient))
                Box(modifier = Modifier.width(80.dp).height(36.dp).clip(RoundedCornerShape(999.dp)).background(skGradient))
                Box(modifier = Modifier.width(90.dp).height(36.dp).clip(RoundedCornerShape(999.dp)).background(skGradient))
            }

            // Hero banner: height 58dp, radius 18dp
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(skGradient),
            )

            // 2x2 grid of product cards
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                CatalogProductSkeletonCardS90(skGradient = skGradient, modifier = Modifier.weight(1f))
                CatalogProductSkeletonCardS90(skGradient = skGradient, modifier = Modifier.weight(1f))
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                CatalogProductSkeletonCardS90(skGradient = skGradient, modifier = Modifier.weight(1f))
                CatalogProductSkeletonCardS90(skGradient = skGradient, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun CatalogProductSkeletonCardS90(
    skGradient: Brush,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = false)
            .clip(RoundedCornerShape(20.dp))
            .background(RfThemeState.currentPalette.surface)
            .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(skGradient),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.80f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(skGradient),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.55f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(skGradient),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(18.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(skGradient),
                )
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(skGradient),
                )
            }
        }
    }
}

// ==============================================================================
private fun resolveModalOriginScreenCode(
    underlyingScreenCode: String?,
    defaultOrigin: String,
    excludedOrigins: Set<String> = emptySet(),
): String {
    val candidate = underlyingScreenCode?.trim().orEmpty()
    val hasCandidate = candidate.isNotEmpty()
    val isAllowed = candidate !in excludedOrigins
    val shouldUseCandidate = if (hasCandidate) isAllowed else false
    return if (shouldUseCandidate) candidate else defaultOrigin
}

private fun resolveModalDismissRoute(originScreenCode: String): String = "BACK:$originScreenCode"

@Composable
private fun RenderModalOriginBackdrop(
    originScreenCode: String,
    viewModel: RosaFiestaViewModel?,
    onOverlayInteraction: ((String) -> Unit)? = null,
) {
    val interactiveCallback = onOverlayInteraction ?: {}
    Box(modifier = Modifier.fillMaxSize()) {
        when (originScreenCode) {
            "s01" -> RenderGuestCatalogS01(viewModel = viewModel, onSelectScreenCode = interactiveCallback)

            "s02" -> RenderGuestFavoritesS02(viewModel = viewModel, onSelectScreenCode = interactiveCallback)

            "s14", "s15" -> RenderPersonalHomeScreen(
                spec = ScreenCatalog.findByCode(originScreenCode),
                viewModel = viewModel,
                onSelectScreenCode = interactiveCallback,
            )

            "s16" -> RenderAboutScreenS16(viewModel = viewModel, onSelectScreenCode = interactiveCallback)

            "s17" -> RenderCompanyReviewsS17(viewModel = viewModel, onSelectScreenCode = interactiveCallback)

            "s18" -> RenderPersonalCatalogS18(viewModel = viewModel, onSelectScreenCode = interactiveCallback)

            "s19" -> RenderProductDetailS19(viewModel = viewModel, onSelectScreenCode = interactiveCallback)

            "s20" -> RenderCategoriesScreenS20(viewModel = viewModel, onSelectScreenCode = interactiveCallback)

            "s21" -> RenderPackagesScreenS21(viewModel = viewModel, onSelectScreenCode = interactiveCallback)

            "s22" -> RenderPackageDetailScreenS22(viewModel = viewModel, onSelectScreenCode = interactiveCallback)

            "s23" -> RenderShopScreenS23(viewModel = viewModel, onSelectScreenCode = interactiveCallback)

            "s24" -> RenderClientFavoritesScreenS24(viewModel = viewModel, onSelectScreenCode = interactiveCallback)

            "s25" -> RenderMyEventDraftS25(viewModel = viewModel, onSelectScreenCode = interactiveCallback)

            "s37" -> RenderMyEventsScreenS37(viewModel = viewModel, onSelectScreenCode = interactiveCallback)

            "s39" -> RenderCalendarScreenS39(viewModel = viewModel, onSelectScreenCode = interactiveCallback)

            "s40" -> RenderEventDetailScreenS40(viewModel = viewModel, onSelectScreenCode = interactiveCallback)

            "s52" -> RenderPostEventReviewScreenS52(viewModel = viewModel, onSelectScreenCode = interactiveCallback)

            "s53" -> RenderMyReservationsScreenS53(viewModel = viewModel, onSelectScreenCode = interactiveCallback)

            "s80" -> RenderPersonalSearchS80(viewModel = viewModel, onSelectScreenCode = interactiveCallback)

            else -> RenderPersonalCatalogS18(viewModel = viewModel, onSelectScreenCode = interactiveCallback)
        }
        val originHasBottomBar = shouldShowBottomBar(originScreenCode)
        if (originHasBottomBar) {
            RenderAppBottomBar(
                spec = ScreenCatalog.findByCode(originScreenCode),
                onSelectScreenCode = interactiveCallback,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun RenderModalSheetScrim(
    onDismiss: () -> Unit,
) {
    val scrimInteractionSource = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x731E1432))
            .clickable(
                interactionSource = scrimInteractionSource,
                indication = null,
                onClick = onDismiss,
            ),
    )
}

@Composable
internal fun RenderGuestAuthModalS75(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
    underlyingScreenCode: String? = null,
    renderOriginBackdrop: Boolean = true,
) {
    val typo = LocalRfTypography.current
    val sheetSlideModifier = rememberModalSheetSlideModifier()
    val originScreenCode = remember {
        resolveModalOriginScreenCode(
            underlyingScreenCode = underlyingScreenCode,
            defaultOrigin = "s01",
            excludedOrigins = setOf("s75"),
        )
    }
    val dismissRoute = remember(originScreenCode) {
        resolveModalDismissRoute(originScreenCode)
    }
    val rootModifier = if (renderOriginBackdrop) {
        Modifier.fillMaxSize().background(RfThemeState.currentPalette.bg)
    } else {
        Modifier.fillMaxSize()
    }

    Box(modifier = rootModifier) {
        if (renderOriginBackdrop) {
            RenderModalOriginBackdrop(
                originScreenCode = originScreenCode,
                viewModel = viewModel,
            )
        }
        RenderModalSheetScrim(
            onDismiss = { onSelectScreenCode(dismissRoute) },
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .then(sheetSlideModifier)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(RfThemeState.currentPalette.surface)
                .padding(top = 12.dp, start = 24.dp, end = 24.dp, bottom = 34.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFFE3DCE8)),
                )
                Spacer(Modifier.height(2.dp))

                Canvas(modifier = Modifier.size(110.dp)) {
                    val scale = size.width / 120f
                    // Pink circle
                    drawCircle(
                        color = Color(0xFFFCE4EF),
                        radius = 42f * scale,
                        center = Offset(60f * scale, 62f * scale),
                    )
                    // Purple shackle
                    val shacklePath = androidx.compose.ui.graphics.Path().apply {
                        moveTo(44f * scale, 56f * scale)
                        lineTo(44f * scale, 42f * scale)
                        arcTo(
                            rect = Rect(44f * scale, 26f * scale, 76f * scale, 58f * scale),
                            startAngleDegrees = 180f,
                            sweepAngleDegrees = 180f,
                            forceMoveTo = false,
                        )
                        lineTo(76f * scale, 56f * scale)
                    }
                    drawPath(
                        path = shacklePath,
                        color = Color(0xFF7650E8),
                        style = Stroke(
                            width = 8f * scale,
                            cap = StrokeCap.Round,
                        ),
                    )
                    // Pink lock body
                    drawRoundRect(
                        color = Color(0xFFEC4F97),
                        topLeft = Offset(34f * scale, 52f * scale),
                        size = Size(52f * scale, 44f * scale),
                        cornerRadius = CornerRadius(12f * scale, 12f * scale),
                    )
                    // Keyhole circle
                    drawCircle(
                        color = Color.White,
                        radius = 6f * scale,
                        center = Offset(60f * scale, 70f * scale),
                    )
                    // Keyhole slot
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(57.5f * scale, 72f * scale),
                        size = Size(5f * scale, 12f * scale),
                        cornerRadius = CornerRadius(2.5f * scale, 2.5f * scale),
                    )
                    // Sparkle star at (96, 30)
                    val starPath = androidx.compose.ui.graphics.Path().apply {
                        moveTo(96f * scale, 30f * scale)
                        lineTo(98f * scale, 35f * scale)
                        lineTo(103f * scale, 37f * scale)
                        lineTo(98f * scale, 39f * scale)
                        lineTo(96f * scale, 44f * scale)
                        lineTo(94f * scale, 39f * scale)
                        lineTo(89f * scale, 37f * scale)
                        lineTo(94f * scale, 35f * scale)
                        close()
                    }
                    drawPath(path = starPath, color = Color(0xFFF5B53B))
                    // Dot at (22, 44)
                    drawCircle(
                        color = Color(0xFF2FB58F),
                        radius = 3f * scale,
                        center = Offset(22f * scale, 44f * scale),
                    )
                }

                // Title
                BasicText(
                    text = "Inicia sesión para continuar",
                    style = TextStyle(
                        fontFamily = typo.displayFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        color = RfThemeState.currentPalette.ink,
                        textAlign = TextAlign.Center,
                    ),
                )

                // Subtitle
                BasicText(
                    text = "Entra o crea tu cuenta para guardar favoritos y armar tu evento.",
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontSize = 15.sp,
                        color = RfThemeState.currentPalette.muted,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                    ),
                    modifier = Modifier.widthIn(max = 300.dp),
                )

                Spacer(Modifier.height(4.dp))

                // Iniciar sesión CTA
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = false)
                        .clip(RoundedCornerShape(16.dp))
                        .background(RosaButtonGradient)
                        .clickable { onSelectScreenCode("s07") },
                ) {
                    BasicText(
                        "Iniciar sesión",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White,
                        ),
                    )
                }

                // Crear cuenta CTA
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = false)
                        .clip(RoundedCornerShape(16.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(16.dp))
                        .clickable { onSelectScreenCode("s08") },
                ) {
                    BasicText(
                        "Crear cuenta",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = RfThemeState.currentPalette.ink,
                        ),
                    )
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clickable { onSelectScreenCode(dismissRoute) },
                ) {
                    BasicText(
                        "Seguir explorando",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = RfThemeState.currentPalette.muted,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
internal fun RenderPlanningModalS76(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
    underlyingScreenCode: String? = null,
    renderOriginBackdrop: Boolean = true,
) {
    val typo = LocalRfTypography.current
    val sheetSlideModifier = rememberModalSheetSlideModifier()
    val originScreenCode = remember {
        resolveModalOriginScreenCode(
            underlyingScreenCode = underlyingScreenCode,
            defaultOrigin = "s14",
            excludedOrigins = setOf("s76"),
        )
    }
    val dismissRoute = remember(originScreenCode) {
        resolveModalDismissRoute(originScreenCode)
    }
    val rootModifier = if (renderOriginBackdrop) {
        Modifier.fillMaxSize().background(RfThemeState.currentPalette.bg)
    } else {
        Modifier.fillMaxSize()
    }

    Box(modifier = rootModifier) {
        if (renderOriginBackdrop) {
            RenderModalOriginBackdrop(
                originScreenCode = originScreenCode,
                viewModel = viewModel,
            )
        }
        RenderModalSheetScrim(
            onDismiss = { onSelectScreenCode(dismissRoute) },
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .then(sheetSlideModifier)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(RfThemeState.currentPalette.surface)
                .padding(top = 12.dp, start = 20.dp, end = 20.dp, bottom = 34.dp),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(40.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFFE3DCE8)),
                )

                Column(modifier = Modifier.padding(horizontal = 4.dp)) {
                    BasicText(
                        "¿Cómo quieres planificar?",
                        style = TextStyle(
                            fontFamily = typo.displayFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            color = RfThemeState.currentPalette.ink,
                        ),
                    )
                    Spacer(Modifier.height(4.dp))
                    BasicText(
                        "Elige cómo prefieres hablar con nosotros.",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 15.sp,
                            color = RfThemeState.currentPalette.muted,
                        ),
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 76.dp)
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = false)
                        .clip(RoundedCornerShape(20.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp))
                        .clickable {
                            viewModel?.openWhatsAppChat(
                                message = "Hola María, necesito asesoría para mi evento",
                            )
                        }
                        .padding(14.dp),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFEEF6D6)),
                    ) {
                        RfIcon(kind = RfIconKind.WHATSAPP, tint = Color(0xFF4E6A10), size = 25.dp)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        BasicText(
                            "WhatsApp con María",
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = RfThemeState.currentPalette.ink,
                            ),
                        )
                        Spacer(Modifier.height(2.dp))
                        BasicText(
                            "Abre WhatsApp directo con asesoría",
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = 13.5.sp,
                                color = RfThemeState.currentPalette.muted,
                            ),
                        )
                    }
                    RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = RfThemeState.currentPalette.muted, size = 20.dp)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 76.dp)
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = false)
                        .clip(RoundedCornerShape(20.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp))
                        .clickable { onSelectScreenCode("s42") }
                        .padding(14.dp),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFFDE6F2)),
                    ) {
                        RfIcon(kind = RfIconKind.CHAT, tint = Color(0xFFC2338F), size = 24.dp)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        BasicText(
                            "Hablar en la app",
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = RfThemeState.currentPalette.ink,
                            ),
                        )
                        Spacer(Modifier.height(2.dp))
                        BasicText(
                            "Mensajería directa dentro de RosaFiesta",
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = 13.5.sp,
                                color = RfThemeState.currentPalette.muted,
                            ),
                        )
                    }
                    RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = RfThemeState.currentPalette.muted, size = 20.dp)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 76.dp)
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = false)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFFAF8FF))
                        .border(2.dp, Color(0xFF865DEC), RoundedCornerShape(20.dp))
                        .clickable { onSelectScreenCode("s26") }
                        .padding(14.dp),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape),
                    ) {
                        RosaAvatar(size = RosaAvatarSize.MEDIUM)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            BasicText(
                                "Usar Rosa IA",
                                style = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = RfThemeState.currentPalette.ink,
                                ),
                            )
                            Spacer(Modifier.width(6.dp))
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(Color(0xFFEFE8FD))
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                            ) {
                                BasicText(
                                    "Rápido",
                                    style = TextStyle(
                                        fontFamily = typo.bodyFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color(0xFF7A4FD8),
                                    ),
                                )
                            }
                        }
                        Spacer(Modifier.height(2.dp))
                        BasicText(
                            "Arma tu evento paso a paso",
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = 13.5.sp,
                                color = RfThemeState.currentPalette.muted,
                            ),
                        )
                    }
                    RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = Color(0xFF7A4FD8), size = 20.dp)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 76.dp)
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = false)
                        .clip(RoundedCornerShape(20.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp))
                        .clickable {
                            viewModel?.startVoiceInput(VoiceInputOrigin.ROSA_WIZARD)
                            onSelectScreenCode("s35")
                        }
                        .padding(14.dp),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFFFF2C9)),
                    ) {
                        Canvas(modifier = Modifier.size(26.dp)) {
                            val scale = size.width / 24f
                            val strokeW = 1.8f * scale
                            val waveColor = Color(0xFF7A5700)
                            fun vLine(xCoord: Float, yStart: Float, yEnd: Float) {
                                drawLine(
                                    color = waveColor,
                                    start = Offset(xCoord * scale, yStart * scale),
                                    end = Offset(xCoord * scale, yEnd * scale),
                                    strokeWidth = strokeW,
                                    cap = StrokeCap.Round,
                                )
                            }
                            vLine(3.5f, 11f, 13f)
                            vLine(7f, 8f, 16f)
                            vLine(11f, 5f, 19f)
                            vLine(15f, 9f, 15f)
                            vLine(19f, 7f, 17f)
                            vLine(21.75f, 11f, 13f)
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            BasicText(
                                "Voz con Rosa",
                                style = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = RfThemeState.currentPalette.ink,
                                ),
                            )
                            Spacer(Modifier.width(6.dp))
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(Color(0xFFFFF2C9))
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                            ) {
                                BasicText(
                                    "Manos libres",
                                    style = TextStyle(
                                        fontFamily = typo.bodyFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color(0xFF7A5700),
                                    ),
                                )
                            }
                        }
                        Spacer(Modifier.height(2.dp))
                        BasicText(
                            "Solo habla, nosotros te escuchamos",
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = 13.5.sp,
                                color = RfThemeState.currentPalette.muted,
                            ),
                        )
                    }
                    RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = RfThemeState.currentPalette.muted, size = 20.dp)
                }
            }
        }
    }
}

@Composable
internal fun RenderEventReviewsModalS77(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
    underlyingScreenCode: String? = null,
    renderOriginBackdrop: Boolean = true,
) {
    val typo = LocalRfTypography.current
    val sheetSlideModifier = rememberModalSheetSlideModifier()
    val originScreenCode = remember {
        resolveModalOriginScreenCode(
            underlyingScreenCode = underlyingScreenCode,
            defaultOrigin = "s37",
            excludedOrigins = setOf("s77"),
        )
    }
    val dismissRoute = remember(originScreenCode) {
        resolveModalDismissRoute(originScreenCode)
    }
    val activeEvent = viewModel?.activeClientEvent() ?: CanonicalSampleData.events.last()
    val reviewsState = viewModel?.companyReviews?.collectAsState()?.value ?: emptyList()
    var showComposer by remember { mutableStateOf(false) }
    var selectedStars by remember { mutableStateOf(5) }
    var draftComment by remember { mutableStateOf("") }
    var hasInteractedComment by remember { mutableStateOf(false) }
    var forceShowCommentError by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val debouncedCommentError = rememberDebouncedValidationError(
        value = draftComment,
        hasInteracted = hasInteractedComment,
        forceShowError = forceShowCommentError,
    ) { value ->
        InputValidator.validateNotes(
            rawValue = value,
            fieldLabel = "Tu reseña",
            isRequired = true,
            minLength = 6,
            maxLength = InputValidator.MAX_NOTES_LENGTH,
        )
    }
    val rootModifier = if (renderOriginBackdrop) {
        Modifier.fillMaxSize().background(RfThemeState.currentPalette.bg)
    } else {
        Modifier.fillMaxSize()
    }

    Box(modifier = rootModifier) {
        if (renderOriginBackdrop) {
            RenderModalOriginBackdrop(
                originScreenCode = originScreenCode,
                viewModel = viewModel,
            )
        }
        RenderModalSheetScrim(
            onDismiss = { onSelectScreenCode(dismissRoute) },
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 580.dp)
                .align(Alignment.BottomCenter)
                .then(sheetSlideModifier)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(RfThemeState.currentPalette.bg)
                .padding(top = 12.dp, start = 20.dp, end = 20.dp, bottom = 34.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(40.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFFE3DCE8)),
                )

                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        BasicText(
                            "Reseñas",
                            style = TextStyle(
                                fontFamily = typo.displayFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 21.sp,
                                color = RfThemeState.currentPalette.ink,
                            ),
                        )
                        Spacer(Modifier.height(2.dp))
                        BasicText(
                            activeEvent.title,
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = 14.sp,
                                color = RfThemeState.currentPalette.muted,
                            ),
                        )
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(RfThemeState.currentPalette.surface)
                            .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(14.dp))
                            .clickable { onSelectScreenCode(dismissRoute) },
                    ) {
                        RfIcon(kind = RfIconKind.CLOSE, tint = RfThemeState.currentPalette.ink, size = 20.dp)
                    }
                }

                if (reviewsState.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = false)
                            .clip(RoundedCornerShape(20.dp))
                            .background(RfThemeState.currentPalette.surface)
                            .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp))
                            .padding(20.dp),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            BasicText(
                                "Aún no hay reseñas publicadas",
                                style = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = RfThemeState.currentPalette.ink,
                                ),
                            )
                            BasicText(
                                "Sé el primero en compartir tu experiencia con Rosa Fiesta.",
                                style = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontSize = 13.5.sp,
                                    color = RfThemeState.currentPalette.muted,
                                    lineHeight = 19.sp,
                                ),
                            )
                        }
                    }
                } else {
                    reviewsState.forEach { reviewItem ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = false)
                                .clip(RoundedCornerShape(20.dp))
                                .background(RfThemeState.currentPalette.surface)
                                .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp))
                                .padding(16.dp),
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFFF2C9)),
                                    ) {
                                        BasicText(
                                            reviewItem.initial,
                                            style = TextStyle(
                                                fontFamily = typo.bodyFamily,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 16.sp,
                                                color = Color(0xFF7A5700),
                                            ),
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        BasicText(
                                            reviewItem.author,
                                            style = TextStyle(
                                                fontFamily = typo.bodyFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = RfThemeState.currentPalette.ink,
                                            ),
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                            repeat(reviewItem.rating.coerceIn(1, 5)) {
                                                RfIcon(kind = RfIconKind.STAR, tint = Color(0xFFF7B500), size = 14.dp)
                                            }
                                        }
                                    }
                                    BasicText(
                                        formatReadableSpanishDateText(reviewItem.dateLabel),
                                        style = TextStyle(
                                            fontFamily = typo.bodyFamily,
                                            fontSize = 13.sp,
                                            color = RfThemeState.currentPalette.muted,
                                        ),
                                    )
                                }
                                BasicText(
                                    reviewItem.comment,
                                    style = TextStyle(
                                        fontFamily = typo.bodyFamily,
                                        fontSize = 15.sp,
                                        color = RfThemeState.currentPalette.ink,
                                        lineHeight = 22.sp,
                                    ),
                                )
                            }
                        }
                    }
                }

                if (showComposer) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            BasicText(
                                "Califica tu experiencia",
                                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = RfThemeState.currentPalette.ink),
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                (1..5).forEach { starValue ->
                                    val isStarFilled = starValue <= selectedStars
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(if (isStarFilled) Color(0xFFFFF2C9) else RfThemeState.currentPalette.chipBg)
                                            .clickable { selectedStars = starValue },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        RfIcon(
                                            kind = RfIconKind.STAR,
                                            tint = if (isStarFilled) Color(0xFFF7B500) else RfThemeState.currentPalette.muted,
                                            size = 18.dp,
                                        )
                                    }
                                }
                            }
                        }

                        val activeCommentError = debouncedCommentError ?: validationError
                        val commentBorderColor = if (activeCommentError != null) RfColors.Error else RfThemeState.currentPalette.line
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(82.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(RfThemeState.currentPalette.surface)
                                .border(1.dp, commentBorderColor, RoundedCornerShape(16.dp))
                                .padding(12.dp),
                        ) {
                            if (draftComment.isEmpty()) {
                                BasicText(
                                    "Escribe tu comentario sobre el evento (mínimo 6 caracteres)...",
                                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = RfThemeState.currentPalette.muted),
                                )
                            }
                            BasicTextField(
                                value = draftComment,
                                onValueChange = { updatedText ->
                                    draftComment = updatedText.take(InputValidator.MAX_NOTES_LENGTH)
                                    hasInteractedComment = true
                                    validationError = null
                                },
                                textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = RfThemeState.currentPalette.ink),
                                cursorBrush = SolidColor(Color(0xFFC2338F)),
                                modifier = Modifier.fillMaxSize(),
                            )
                        }

                        RfValidationErrorText(errorMessage = activeCommentError)
                    }
                }

                val canWriteReview = viewModel?.hasCompletedOrUsedRental() ?: false
                if (!canWriteReview) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(RfThemeState.currentPalette.chipBg)
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                    ) {
                        BasicText(
                            text = "Solo puedes escribir una reseña después de alquilar o usar un producto.",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = RfThemeState.currentPalette.muted),
                        )
                    }
                } else {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = false)
                            .clip(RoundedCornerShape(16.dp))
                            .background(RosaButtonGradient)
                            .clickable {
                                if (!showComposer) {
                                    showComposer = true
                                    hasInteractedComment = false
                                    forceShowCommentError = false
                                    validationError = null
                                } else {
                                    forceShowCommentError = true
                                    val trimmedComment = draftComment.trim()
                                    val commentError = InputValidator.validateNotes(
                                        rawValue = trimmedComment,
                                        fieldLabel = "Tu reseña",
                                        isRequired = true,
                                        minLength = 6,
                                        maxLength = InputValidator.MAX_NOTES_LENGTH,
                                    )
                                    if (commentError != null) {
                                        validationError = commentError
                                        viewModel.soundPlayer.playAlertError()
                                    } else {
                                        val savedReview = viewModel.submitCompanyReview(
                                            comment = trimmedComment,
                                            rating = selectedStars,
                                        )
                                        if (savedReview) {
                                            showComposer = false
                                            draftComment = ""
                                            hasInteractedComment = false
                                            forceShowCommentError = false
                                            validationError = null
                                            onSelectScreenCode(dismissRoute)
                                        } else {
                                            validationError = "No fue posible publicar la reseña. Revisa tu comentario."
                                        }
                                    }
                                }
                            },
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            RfIcon(kind = RfIconKind.EDIT, tint = Color.White, size = 20.dp)
                            BasicText(
                                if (showComposer) "Publicar reseña" else "Escribir reseña",
                                style = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White,
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun rememberModalSheetSlideModifier(): Modifier {
    val slideProgress = remember { Animatable(0f) }
    val sheetInteractionSource = remember { MutableInteractionSource() }
    LaunchedEffect(Unit) {
        slideProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
        )
    }
    return Modifier
        .graphicsLayer {
            translationY = (1f - slideProgress.value) * 220f
        }
        .clickable(
            interactionSource = sheetInteractionSource,
            indication = null,
            onClick = {},
        )
}

private fun resolveFiltersDismissRoute81(originScreenCode: String): String = resolveModalDismissRoute(originScreenCode)

private fun resolveFiltersApplyRoute81(originScreenCode: String): String = resolveModalDismissRoute(originScreenCode)

@Composable
internal fun RenderCatalogFiltersModalS81(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
    underlyingScreenCode: String? = null,
    renderOriginBackdrop: Boolean = true,
) {
    val typo = LocalRfTypography.current
    val density = LocalDensity.current
    val originScreenCode = remember {
        resolveModalOriginScreenCode(
            underlyingScreenCode = underlyingScreenCode,
            defaultOrigin = "s18",
            excludedOrigins = setOf("s81"),
        )
    }
    val dismissRoute = remember(originScreenCode) {
        resolveFiltersDismissRoute81(originScreenCode)
    }
    val applyRoute = remember(originScreenCode) {
        resolveFiltersApplyRoute81(originScreenCode)
    }
    val sheetSlideModifier = rememberModalSheetSlideModifier()
    val vmCategory = viewModel?.selectedCategory?.collectAsState()?.value ?: "Todos"
    val vmAvailableOnly = viewModel?.onlyAvailableFilter?.collectAsState()?.value ?: false
    val vmSortLabel = viewModel?.searchSortOrder?.collectAsState()?.value ?: "Más populares"
    val vmMaxPrice = viewModel?.maxPriceFilterDop?.collectAsState()?.value ?: 5000
    val normalizedInitialSort = if (vmSortLabel == "Populares") "Más populares" else vmSortLabel
    var selectedCategory by remember(vmCategory) { mutableStateOf(vmCategory) }
    var availableOnly by remember(vmAvailableOnly) { mutableStateOf(vmAvailableOnly) }
    var selectedSortOption by remember(normalizedInitialSort) { mutableStateOf(normalizedInitialSort) }
    var selectedMaxPrice by remember(vmMaxPrice) { mutableStateOf(vmMaxPrice.coerceIn(0, 5000)) }
    var sliderWidthPx by remember { mutableStateOf(0f) }
    val rootModifier = if (renderOriginBackdrop) {
        Modifier.fillMaxSize().background(RfThemeState.currentPalette.bg)
    } else {
        Modifier.fillMaxSize()
    }

    Box(modifier = rootModifier) {
        if (renderOriginBackdrop) {
            RenderModalOriginBackdrop(
                originScreenCode = originScreenCode,
                viewModel = viewModel,
            )
        }
        RenderModalSheetScrim(
            onDismiss = { onSelectScreenCode(dismissRoute) },
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .then(sheetSlideModifier)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(RfThemeState.currentPalette.surface)
                .padding(top = 12.dp, start = 20.dp, end = 20.dp, bottom = 34.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(40.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFFE3DCE8)),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BasicText(
                        "Filtros",
                        style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = RfThemeState.currentPalette.ink),
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        BasicText(
                            "Limpiar",
                            modifier = Modifier.clickable {
                                selectedCategory = "Todos"
                                availableOnly = false
                                selectedSortOption = "Más populares"
                                selectedMaxPrice = 5000
                                viewModel?.resetCatalogFilters()
                            },
                            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFC2338F)),
                        )
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(RfThemeState.currentPalette.chipBg)
                                .clickable { onSelectScreenCode(dismissRoute) },
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.CLOSE, tint = RfThemeState.currentPalette.muted, size = 14.dp)
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BasicText("Categoría", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF3B3550)))
                    val vmCategories = viewModel?.catalogCategories?.collectAsState()?.value
                    val categoryLabels = if (vmCategories != null) {
                        listOf("Todos") + vmCategories.map { category -> category.name.trim() }
                            .filter { name -> name.isNotEmpty() }
                            .distinctBy { name -> name.lowercase() }
                    } else {
                        listOf("Todos", "Mobiliario", "Diseño floral", "Arcos y globos", "Mesas de dulces", "Mantelería")
                    }
                    val categoryRows = categoryLabels.chunked(3)
                    categoryRows.forEach { rowItems ->
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            rowItems.forEach { categoryLabel ->
                                val isUnfilteredChip = categoryLabel == "Todos"
                                val isSelected = if (isUnfilteredChip) {
                                    isUnfilteredCategoryLabel80(selectedCategory)
                                } else {
                                    selectedCategory.equals(categoryLabel, ignoreCase = true)
                                }
                                Box(
                                    modifier = Modifier
                                        .height(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) {
                                                Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9)))
                                            } else {
                                                SolidColor(RfThemeState.currentPalette.chipBg)
                                            },
                                        )
                                        .clickable {
                                            selectedCategory = categoryLabel
                                            viewModel?.setSelectedCategory(categoryLabel)
                                        }
                                        .padding(horizontal = 14.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    BasicText(
                                        categoryLabel,
                                        style = TextStyle(
                                            fontFamily = typo.bodyFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.5.sp,
                                            color = if (isSelected) Color.White else Color(0xFF1A1A2E),
                                        ),
                                    )
                                }
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        BasicText("Precio máximo por día", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF3B3550)))
                        val priceRangeLabel = if (selectedMaxPrice < 5000) "RD$ 0 – ${RosaFiestaEngine.formatDop(selectedMaxPrice)}" else "RD$ 0 – RD$ 5,000 (Sin límite)"
                        BasicText(
                            priceRangeLabel,
                            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = RfThemeState.currentPalette.ink),
                        )
                    }

                    val sliderFraction = (selectedMaxPrice.toFloat() / 5000f).coerceIn(0.02f, 1f)
                    val thumbDiameterDp = 24.dp
                    val thumbDiameterPx = with(density) { thumbDiameterDp.toPx() }
                    val maxThumbOffsetPx = (sliderWidthPx - thumbDiameterPx).coerceAtLeast(0f)
                    val thumbOffsetDp = with(density) { (maxThumbOffsetPx * sliderFraction).toDp() }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                            .onSizeChanged { measuredSize ->
                                sliderWidthPx = measuredSize.width.toFloat()
                            }
                            .pointerInput(sliderWidthPx) {
                                detectTapGestures { tapOffset ->
                                    val updatedPrice = computeSnappedSliderPrice81(tapOffset.x, sliderWidthPx)
                                    selectedMaxPrice = updatedPrice
                                    viewModel?.setMaxPriceFilterDop(updatedPrice)
                                }
                            }
                            .pointerInput(sliderWidthPx) {
                                detectHorizontalDragGestures { dragChange, _ ->
                                    dragChange.consume()
                                    val updatedPrice = computeSnappedSliderPrice81(dragChange.position.x, sliderWidthPx)
                                    selectedMaxPrice = updatedPrice
                                    viewModel?.setMaxPriceFilterDop(updatedPrice)
                                }
                            },
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(RfThemeState.currentPalette.chipBg),
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(sliderFraction)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(RosaButtonGradient),
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .offset(x = thumbOffsetDp)
                                .size(thumbDiameterDp)
                                .shadow(4.dp, CircleShape, spotColor = Color(0x80865DEC))
                                .border(2.5.dp, Color(0xFF865DEC), CircleShape)
                                .clip(CircleShape)
                                .background(RfThemeState.currentPalette.surface),
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        val pricePresets = listOf(500, 1500, 3000, 5000)
                        pricePresets.forEach { presetPrice ->
                            val isPresetActive = selectedMaxPrice == presetPrice
                            val presetText = if (presetPrice == 5000) "Sin límite" else "RD$ $presetPrice"
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(30.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(if (isPresetActive) Color(0xFFFDE6F2) else RfThemeState.currentPalette.chipBg)
                                    .clickable {
                                        selectedMaxPrice = presetPrice
                                        viewModel?.setMaxPriceFilterDop(presetPrice)
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                BasicText(
                                    presetText,
                                    style = TextStyle(
                                        fontFamily = typo.bodyFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp,
                                        color = if (isPresetActive) Color(0xFFC2338F) else RfThemeState.currentPalette.muted,
                                    ),
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BasicText("Solo artículos disponibles", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = RfThemeState.currentPalette.ink))
                    Box(
                        modifier = Modifier
                            .width(46.dp)
                            .height(28.dp)
                            .clip(CircleShape)
                            .background(
                                if (availableOnly) {
                                    Brush.linearGradient(listOf(Color(0xFFEA4EA9), Color(0xFF865DEC)))
                                } else {
                                    Brush.linearGradient(listOf(Color(0xFFE3DCE8), Color(0xFFE3DCE8)))
                                },
                            )
                            .clickable {
                                val updatedAvailable = !availableOnly
                                availableOnly = updatedAvailable
                                viewModel?.setOnlyAvailableFilter(updatedAvailable)
                            }
                            .padding(3.dp),
                        contentAlignment = if (availableOnly) Alignment.CenterEnd else Alignment.CenterStart,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .shadow(2.dp, CircleShape)
                                .clip(CircleShape)
                                .background(RfThemeState.currentPalette.surface),
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BasicText("Ordenar por", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF3B3550)))
                    val sortOptions = listOf("Más populares", "Menor precio", "Mayor precio", "Alfabético")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .clip(RoundedCornerShape(14.dp))
                            .background(RfThemeState.currentPalette.chipBg)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        sortOptions.forEach { sortOption ->
                            val isSel = sortOption.equals(selectedSortOption, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .height(36.dp)
                                    .then(if (isSel) Modifier.shadow(4.dp, RoundedCornerShape(12.dp), spotColor = Color(0x33B450B4), ambientColor = Color.Transparent) else Modifier)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSel) Color.White else Color.Transparent)
                                    .clickable {
                                        selectedSortOption = sortOption
                                        viewModel?.setSearchSortOrder(sortOption)
                                    }
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                BasicText(
                                    sortOption,
                                    style = TextStyle(
                                        fontFamily = typo.bodyFamily,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.5.sp,
                                        color = if (isSel) Color(0xFF1A1A2E) else Color(0xFF62627A),
                                    ),
                                )
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = false)
                        .clip(RoundedCornerShape(16.dp))
                        .background(RosaButtonGradient)
                        .clickable { onSelectScreenCode(applyRoute) },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        "Ver resultados",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White),
                    )
                }
            }
        }
    }
}

private fun computeSnappedSliderPrice81(offsetX: Float, trackWidthPx: Float): Int {
    if (trackWidthPx <= 1f) return 5000
    val rawFraction = (offsetX / trackWidthPx).coerceIn(0.02f, 1f)
    val rawPrice = (rawFraction * 5000f).toInt()
    val snappedPrice = ((rawPrice + 25) / 50) * 50
    return snappedPrice.coerceIn(100, 5000)
}

private fun spanishMonthFull84(month: Int): String = when (month) {
    1 -> "Enero"
    2 -> "Febrero"
    3 -> "Marzo"
    4 -> "Abril"
    5 -> "Mayo"
    6 -> "Junio"
    7 -> "Julio"
    8 -> "Agosto"
    9 -> "Septiembre"
    10 -> "Octubre"
    11 -> "Noviembre"
    else -> "Diciembre"
}

private fun spanishMonthShort84(month: Int): String = when (month) {
    1 -> "ene"
    2 -> "feb"
    3 -> "mar"
    4 -> "abr"
    5 -> "may"
    6 -> "jun"
    7 -> "jul"
    8 -> "ago"
    9 -> "sep"
    10 -> "oct"
    11 -> "nov"
    else -> "dic"
}

private fun spanishWeekdayShort84(year: Int, month: Int, day: Int): String {
    val firstDayOffset = computeGregorianMondayFirstOffset(year, month)
    val weekdayIndex = (firstDayOffset + (day - 1)).mod(7)
    return when (weekdayIndex) {
        0 -> "Lun"
        1 -> "Mar"
        2 -> "Mié"
        3 -> "Jue"
        4 -> "Vie"
        5 -> "Sáb"
        else -> "Dom"
    }
}

private fun computeInitialSelectableDay84(year: Int, month: Int): Int {
    val daysInMonth = computeDaysInGregorianMonth(year, month)
    val monthShort = spanishMonthShort84(month)
    for (candidateDay in 1..daysInMonth) {
        val daysUntil = DomainClockFormatter.computeDaysUntilEvent("$candidateDay $monthShort $year")
        if (daysUntil >= 3) {
            return candidateDay
        }
    }
    return daysInMonth
}

private fun buildCalendarWeeks84(year: Int, month: Int): List<List<Int?>> {
    val leadingBlanks = computeGregorianMondayFirstOffset(year, month)
    val totalDays = computeDaysInGregorianMonth(year, month)
    val cells = mutableListOf<Int?>()
    repeat(leadingBlanks) {
        cells.add(null)
    }
    for (dayNumber in 1..totalDays) {
        cells.add(dayNumber)
    }
    val remainder = cells.size.mod(7)
    if (remainder != 0) {
        repeat(7 - remainder) {
            cells.add(null)
        }
    }
    return cells.chunked(7)
}

@Composable
internal fun RenderChangeDateModalS84(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
    underlyingScreenCode: String? = null,
    renderOriginBackdrop: Boolean = true,
) {
    val typo = LocalRfTypography.current
    val sheetSlideModifier = rememberModalSheetSlideModifier()
    val originScreenCode = remember {
        resolveModalOriginScreenCode(
            underlyingScreenCode = underlyingScreenCode,
            defaultOrigin = "s40",
            excludedOrigins = setOf("s84", "s85"),
        )
    }
    val dismissRoute = remember(originScreenCode) {
        resolveModalDismissRoute(originScreenCode)
    }
    val eventsState = viewModel?.events?.collectAsState()?.value ?: CanonicalSampleData.events
    val selectedEventId = viewModel?.selectedEventId?.collectAsState()?.value
    val activeEvent = viewModel?.activeClientEvent()
        ?: eventsState.firstOrNull { eventItem -> eventItem.id == selectedEventId }
        ?: eventsState.firstOrNull()
    val eventTitle = activeEvent?.title ?: "Boda Real Rosa & Dorado"
    val itemCountTotal = activeEvent?.items?.sumOf { lineItem -> lineItem.quantity } ?: 145

    val currentYearMonth = remember { DomainClockFormatter.currentYearMonthIso() }
    val defaultYear = currentYearMonth.substringBefore("-").toIntOrNull() ?: 2026
    val defaultMonth = currentYearMonth.substringAfter("-").toIntOrNull()?.coerceIn(1, 12) ?: 10

    var displayedYear by remember { mutableStateOf(defaultYear) }
    var displayedMonth by remember { mutableStateOf(defaultMonth) }
    var selectedDayInt by remember { mutableStateOf(computeInitialSelectableDay84(defaultYear, defaultMonth)) }
    val timeSlotOptions = listOf("10:00 AM", "04:00 PM", "07:00 PM")
    var selectedTimeSlot by remember { mutableStateOf("04:00 PM") }
    var feedbackBannerMessage by remember { mutableStateOf<String?>(null) }
    var isFeedbackError by remember { mutableStateOf(false) }

    val weeksGrid = remember(displayedYear, displayedMonth) {
        buildCalendarWeeks84(displayedYear, displayedMonth)
    }
    val monthShort = spanishMonthShort84(displayedMonth)
    val weekdayShort = spanishWeekdayShort84(displayedYear, displayedMonth, selectedDayInt)
    val formattedSelection = "$weekdayShort $selectedDayInt $monthShort $displayedYear · $selectedTimeSlot"
    val selectedDaysUntil = DomainClockFormatter.computeDaysUntilEvent("$selectedDayInt $monthShort $displayedYear")
    val sheetInteractionSource = remember { MutableInteractionSource() }
    val rootModifier = if (renderOriginBackdrop) {
        Modifier.fillMaxSize().background(RfThemeState.currentPalette.bg)
    } else {
        Modifier.fillMaxSize()
    }

    Box(modifier = rootModifier) {
        if (renderOriginBackdrop) {
            RenderModalOriginBackdrop(
                originScreenCode = originScreenCode,
                viewModel = viewModel,
            )
        }
        RenderModalSheetScrim(
            onDismiss = { onSelectScreenCode(dismissRoute) },
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 700.dp)
                .align(Alignment.BottomCenter)
                .then(sheetSlideModifier)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(RfThemeState.currentPalette.bg)
                .clickable(
                    interactionSource = sheetInteractionSource,
                    indication = null,
                    onClick = {},
                )
                .padding(top = 12.dp, start = 20.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(40.dp)
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFFE3DCE8)),
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(8.dp, CircleShape, spotColor = Color(0x33B450B4))
                        .clip(CircleShape)
                        .background(RfThemeState.currentPalette.surface)
                        .clickable { onSelectScreenCode(dismissRoute) },
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = RfThemeState.currentPalette.ink, size = 22.dp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    BasicText("Cambiar o cancelar", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = RfThemeState.currentPalette.ink))
                    BasicText(eventTitle, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = RfThemeState.currentPalette.muted))
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(RfThemeState.currentPalette.chipBg)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .shadow(4.dp, RoundedCornerShape(12.dp), spotColor = Color(0x33B450B4))
                            .clip(RoundedCornerShape(12.dp))
                            .background(RfThemeState.currentPalette.surface),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText("Cambiar fecha", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = RfThemeState.currentPalette.ink))
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelectScreenCode("s85") },
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText("Cancelar evento", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = RfThemeState.currentPalette.muted))
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = false)
                        .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .padding(14.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(RfThemeState.currentPalette.chipBg)
                                    .clickable {
                                        if (displayedMonth == 1) {
                                            if (displayedYear > 2026) {
                                                displayedYear -= 1
                                                displayedMonth = 12
                                            }
                                        } else {
                                            displayedMonth -= 1
                                        }
                                        val maxDaysNext = computeDaysInGregorianMonth(displayedYear, displayedMonth)
                                        selectedDayInt = selectedDayInt.coerceIn(1, maxDaysNext)
                                        feedbackBannerMessage = null
                                    },
                            ) {
                                RfIcon(kind = RfIconKind.BACK, tint = RfThemeState.currentPalette.ink, size = 16.dp)
                            }
                            BasicText(
                                "${spanishMonthFull84(displayedMonth)} $displayedYear",
                                style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = RfThemeState.currentPalette.ink),
                            )
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(RfThemeState.currentPalette.chipBg)
                                    .clickable {
                                        if (displayedMonth == 12) {
                                            if (displayedYear < 2030) {
                                                displayedYear += 1
                                                displayedMonth = 1
                                            }
                                        } else {
                                            displayedMonth += 1
                                        }
                                        val maxDaysNext = computeDaysInGregorianMonth(displayedYear, displayedMonth)
                                        selectedDayInt = selectedDayInt.coerceIn(1, maxDaysNext)
                                        feedbackBannerMessage = null
                                    },
                            ) {
                                RfIcon(kind = RfIconKind.ARROW_RIGHT, tint = RfThemeState.currentPalette.ink, size = 16.dp)
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                            listOf("L", "M", "X", "J", "V", "S", "D").forEach { weekdayLabel ->
                                BasicText(weekdayLabel, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = RfThemeState.currentPalette.muted))
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            weeksGrid.forEach { weekCells ->
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                                    weekCells.forEach { dayNumber ->
                                        if (dayNumber == null) {
                                            Box(modifier = Modifier.size(38.dp))
                                        } else {
                                            val daysUntilDay = DomainClockFormatter.computeDaysUntilEvent("$dayNumber $monthShort $displayedYear")
                                            val isPastDate = daysUntilDay < 0
                                            val isTooSoon = daysUntilDay in 0..1
                                            val isBlockedDay = if (isPastDate) true else isTooSoon
                                            val isToday = daysUntilDay == 0
                                            val isSelected = dayNumber == selectedDayInt
                                            val hasLowStockDot = dayNumber.mod(7) == 0

                                            Box(
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .then(
                                                        if (isSelected) {
                                                            Modifier
                                                                .shadow(8.dp, RoundedCornerShape(12.dp), spotColor = Color(0xB3865DEC), ambientColor = Color.Transparent)
                                                                .clip(RoundedCornerShape(12.dp))
                                                                .background(Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9))))
                                                        } else if (isToday) {
                                                            Modifier.clip(RoundedCornerShape(12.dp)).border(1.5.dp, Color(0xFFC2338F), RoundedCornerShape(12.dp))
                                                        } else {
                                                            Modifier.clip(RoundedCornerShape(12.dp))
                                                        },
                                                    )
                                                    .clickable {
                                                        if (isPastDate) {
                                                            isFeedbackError = true
                                                            feedbackBannerMessage = "No puedes reprogramar hacia una fecha pasada."
                                                        } else if (isTooSoon) {
                                                            isFeedbackError = true
                                                            feedbackBannerMessage = "El cambio requiere mínimo 48 horas de anticipación."
                                                        } else {
                                                            selectedDayInt = dayNumber
                                                            isFeedbackError = false
                                                            feedbackBannerMessage = null
                                                        }
                                                    },
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                BasicText(
                                                    dayNumber.toString(),
                                                    style = TextStyle(
                                                        fontFamily = typo.bodyFamily,
                                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                                        fontSize = 14.sp,
                                                        color = when {
                                                            isSelected -> Color.White
                                                            isBlockedDay -> Color(0xFFC9C3D1)
                                                            else -> RfThemeState.currentPalette.ink
                                                        },
                                                    ),
                                                )
                                                val showWarningDot = if (hasLowStockDot) !isBlockedDay else false
                                                if (showWarningDot) {
                                                    Box(
                                                        modifier = Modifier
                                                            .align(Alignment.BottomCenter)
                                                            .offset(y = (-4).dp)
                                                            .size(5.dp)
                                                            .clip(CircleShape)
                                                            .background(Color(0xFFF7B500)),
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.padding(top = 6.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp).border(1.5.dp, Color(0xFFC2338F), RoundedCornerShape(3.dp)))
                                BasicText("Hoy", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = RfThemeState.currentPalette.muted))
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFFF7B500)))
                                BasicText("Alta demanda", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = RfThemeState.currentPalette.muted))
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BasicText(
                        "Horario de montaje · $formattedSelection",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = RfThemeState.currentPalette.ink),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        timeSlotOptions.forEach { slotOption ->
                            val isSlotSelected = slotOption == selectedTimeSlot
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (isSlotSelected) {
                                            Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF865DEC)))
                                        } else {
                                            SolidColor(RfThemeState.currentPalette.surface)
                                        },
                                    )
                                    .border(
                                        1.dp,
                                        if (isSlotSelected) Color.Transparent else RfThemeState.currentPalette.line,
                                        RoundedCornerShape(14.dp),
                                    )
                                    .clickable { selectedTimeSlot = slotOption },
                                contentAlignment = Alignment.Center,
                            ) {
                                BasicText(
                                    slotOption,
                                    style = TextStyle(
                                        fontFamily = typo.bodyFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (isSlotSelected) Color.White else RfThemeState.currentPalette.ink,
                                    ),
                                )
                            }
                        }
                    }
                }

                val bannerText = feedbackBannerMessage ?: if (selectedDaysUntil >= 2) {
                    "Tus $itemCountTotal artículos están disponibles para $formattedSelection. Tu primer cambio de fecha es gratis."
                } else {
                    "Selecciona una fecha con al menos 48 horas de anticipación para solicitar el cambio."
                }
                val showErrorStyle = if (isFeedbackError) true else selectedDaysUntil < 2
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (showErrorStyle) Color(0xFFFCE3E8) else Color(0xFFEEF6D6))
                        .padding(12.dp, 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    RfIcon(
                        kind = if (showErrorStyle) RfIconKind.WARNING else RfIconKind.CHECK,
                        tint = if (showErrorStyle) Color(0xFFB42345) else Color(0xFF3F560C),
                        size = 18.dp,
                    )
                    BasicText(
                        bannerText,
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 13.5.sp,
                            lineHeight = 19.sp,
                            color = if (showErrorStyle) Color(0xFFB42345) else Color(0xFF3F560C),
                        ),
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                BasicText(
                    "RosaFiesta confirmará el cambio en menos de 24 h.",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.5.sp, color = RfThemeState.currentPalette.muted),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = false)
                        .clip(RoundedCornerShape(16.dp))
                        .background(RosaButtonGradient)
                        .clickable {
                            if (selectedDaysUntil < 2) {
                                isFeedbackError = true
                                feedbackBannerMessage = "Elige una fecha futura con al menos 48 h de anticipación."
                            } else if (viewModel != null) {
                                viewModel.setDateChangeInput(formattedSelection)
                                val targetId = activeEvent?.id ?: ""
                                val changeResult = if (targetId.isNotBlank()) {
                                    viewModel.submitDateChange(targetId)
                                } else {
                                    viewModel.submitDateChange()
                                }
                                if (changeResult.allowed) {
                                    isFeedbackError = false
                                    feedbackBannerMessage = null
                                    onSelectScreenCode(dismissRoute)
                                } else {
                                    isFeedbackError = true
                                    feedbackBannerMessage = changeResult.message
                                }
                            } else {
                                onSelectScreenCode(dismissRoute)
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        "Solicitar cambio de fecha",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White),
                    )
                }
            }
        }
    }
}

@Composable
internal fun RenderCancelEventModalS85(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
    underlyingScreenCode: String? = null,
    renderOriginBackdrop: Boolean = true,
) {
    val typo = LocalRfTypography.current
    val sheetSlideModifier = rememberModalSheetSlideModifier()
    val originScreenCode = remember {
        resolveModalOriginScreenCode(
            underlyingScreenCode = underlyingScreenCode,
            defaultOrigin = "s40",
            excludedOrigins = setOf("s84", "s85"),
        )
    }
    val dismissRoute = remember(originScreenCode) {
        resolveModalDismissRoute(originScreenCode)
    }
    val eventsState = viewModel?.events?.collectAsState()?.value ?: CanonicalSampleData.events
    val selectedEventId = viewModel?.selectedEventId?.collectAsState()?.value
    val activeEvent = viewModel?.activeClientEvent()
        ?: eventsState.firstOrNull { eventItem -> eventItem.id == selectedEventId }
        ?: eventsState.firstOrNull()
    val eventHeaderSubtitle = if (activeEvent != null) {
        "${activeEvent.title} · ${formatReadableSpanishDateText(activeEvent.dateLabel)}"
    } else {
        "Evento activo"
    }
    val paidDepositDop = activeEvent?.paidDepositDop ?: 0
    val daysUntilEvent = activeEvent?.daysUntilEvent ?: 10
    val refundPreview = RosaFiestaEngine.calculateCancellationRefund(
        paidDepositDop = paidDepositDop,
        daysUntilEvent = daysUntilEvent,
    )
    val retentionPercent = (100 - refundPreview.refundPercent).coerceIn(0, 100)

    var selectedReason by remember { mutableStateOf("Cambio de planes") }
    var customReasonText by remember { mutableStateOf("") }
    var hasInteractedReason by remember { mutableStateOf(false) }
    var forceShowReasonError by remember { mutableStateOf(false) }
    var reasonErrorText by remember { mutableStateOf<String?>(null) }
    val debouncedReasonError = rememberDebouncedValidationError(
        value = customReasonText,
        hasInteracted = hasInteractedReason,
        forceShowError = forceShowReasonError,
    ) { candidate ->
        if (selectedReason != "Otro motivo") {
            null
        } else {
            InputValidator.validateNotes(
                rawValue = candidate,
                fieldLabel = "El motivo de cancelación",
                isRequired = true,
                minLength = 6,
                maxLength = InputValidator.MAX_NOTES_LENGTH,
            )
        }
    }
    val sheetInteractionSource = remember { MutableInteractionSource() }
    val rootModifier = if (renderOriginBackdrop) {
        Modifier.fillMaxSize().background(RfThemeState.currentPalette.bg)
    } else {
        Modifier.fillMaxSize()
    }

    Box(modifier = rootModifier) {
        if (renderOriginBackdrop) {
            RenderModalOriginBackdrop(
                originScreenCode = originScreenCode,
                viewModel = viewModel,
            )
        }
        RenderModalSheetScrim(
            onDismiss = { onSelectScreenCode(dismissRoute) },
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 700.dp)
                .align(Alignment.BottomCenter)
                .then(sheetSlideModifier)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(RfThemeState.currentPalette.bg)
                .clickable(
                    interactionSource = sheetInteractionSource,
                    indication = null,
                    onClick = {},
                )
                .padding(top = 12.dp, start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(40.dp)
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFFE3DCE8)),
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(8.dp, CircleShape, spotColor = Color(0x33B450B4))
                        .clip(CircleShape)
                        .background(RfThemeState.currentPalette.surface)
                        .clickable { onSelectScreenCode(dismissRoute) },
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = RfThemeState.currentPalette.ink, size = 22.dp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    BasicText("Cambiar o cancelar", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = RfThemeState.currentPalette.ink))
                    BasicText(eventHeaderSubtitle, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = RfThemeState.currentPalette.muted))
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(RfThemeState.currentPalette.chipBg)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelectScreenCode("s84") },
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText("Cambiar fecha", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = RfThemeState.currentPalette.muted))
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .shadow(4.dp, RoundedCornerShape(12.dp), spotColor = Color(0x33B450B4))
                            .clip(RoundedCornerShape(12.dp))
                            .background(RfThemeState.currentPalette.surface),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText("Cancelar evento", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = RfThemeState.currentPalette.ink))
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = false)
                        .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .padding(14.dp, 16.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        val daysStatusSuffix = when {
                            daysUntilEvent > 1 -> "· faltan $daysUntilEvent días"
                            daysUntilEvent == 1 -> "· falta 1 día"
                            daysUntilEvent == 0 -> "· es hoy"
                            else -> "· fecha vencida"
                        }
                        Row {
                            BasicText("Política de cancelación ", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = RfThemeState.currentPalette.ink))
                            BasicText(daysStatusSuffix, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = RfThemeState.currentPalette.muted))
                        }
                        CancelTierPolicyRow85(
                            label = "Más de 7 días antes",
                            percentText = "90%",
                            isActive = refundPreview.refundPercent == 90,
                        )
                        CancelTierPolicyRow85(
                            label = "Entre 3 y 7 días",
                            percentText = "50%",
                            isActive = refundPreview.refundPercent == 50,
                        )
                        CancelTierPolicyRow85(
                            label = "Menos de 3 días",
                            percentText = "Sin reembolso",
                            isActive = refundPreview.refundPercent == 0,
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = false)
                        .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(RfThemeState.currentPalette.surface)
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            BasicText("Anticipo pagado", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = RfThemeState.currentPalette.muted))
                            BasicText(RosaFiestaEngine.formatDop(paidDepositDop), style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = RfThemeState.currentPalette.ink))
                        }
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(RfThemeState.currentPalette.lineSoft))
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            BasicText("Cargo por ventana ($retentionPercent%)", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = RfThemeState.currentPalette.muted))
                            BasicText("− ${RosaFiestaEngine.formatDop(refundPreview.retainedDop)}", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = RfThemeState.currentPalette.ink))
                        }
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(RfThemeState.currentPalette.lineSoft))
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            BasicText("Te devolvemos (${refundPreview.refundPercent}%)", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = RfThemeState.currentPalette.ink))
                            BasicText(
                                RosaFiestaEngine.formatDop(refundPreview.refundDop),
                                style = TextStyle(
                                    fontFamily = typo.displayFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 22.sp,
                                    color = if (refundPreview.refundDop > 0) Color(0xFF4E6A10) else Color(0xFFB42345),
                                ),
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    BasicText("¿Por qué cancelas?", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF3B3550)))
                    val cancelReasons = listOf("Cambio de planes", "Ajuste de presupuesto", "Cambio de fecha sin definir", "Otro motivo")
                    cancelReasons.forEach { reasonOption ->
                        val isReasonSelected = selectedReason == reasonOption
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .clickable {
                                    selectedReason = reasonOption
                                    reasonErrorText = null
                                },
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .border(
                                        if (isReasonSelected) 6.dp else 2.dp,
                                        if (isReasonSelected) Color(0xFFC2338F) else Color(0xFFE3DCE8),
                                        CircleShape,
                                    ),
                            )
                            BasicText(
                                reasonOption,
                                style = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontWeight = if (isReasonSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    fontSize = 14.5.sp,
                                    color = RfThemeState.currentPalette.ink,
                                ),
                            )
                        }
                    }
                    val activeReasonError = if (selectedReason == "Otro motivo") {
                        reasonErrorText ?: debouncedReasonError
                    } else {
                        reasonErrorText
                    }
                    if (selectedReason == "Otro motivo") {
                        val reasonBorderColor = if (activeReasonError != null) RfColors.Error else RfThemeState.currentPalette.line
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(RfThemeState.currentPalette.surface)
                                .border(1.dp, reasonBorderColor, RoundedCornerShape(14.dp))
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            if (customReasonText.isEmpty()) {
                                BasicText("Cuéntanos brevemente el motivo...", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = RfThemeState.currentPalette.muted))
                            }
                            BasicTextField(
                                value = customReasonText,
                                onValueChange = { updated ->
                                    customReasonText = updated.take(InputValidator.MAX_NOTES_LENGTH)
                                    hasInteractedReason = true
                                    reasonErrorText = null
                                },
                                singleLine = true,
                                textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = RfThemeState.currentPalette.ink),
                                cursorBrush = SolidColor(Color(0xFFC2338F)),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                    RfValidationErrorText(errorMessage = activeReasonError)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.horizontalGradient(listOf(Color(0xFFFCE6F3), Color(0xFFECE4FD))))
                        .clickable { onSelectScreenCode("s84") }
                        .padding(10.dp, 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RfIcon(kind = RfIconKind.CALENDAR, tint = Color(0xFF865DEC), size = 18.dp)
                    BasicText(
                        "¿Mejor otra fecha? Cambiarla es gratis.",
                        modifier = Modifier.weight(1f),
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.5.sp, color = RfThemeState.currentPalette.ink),
                    )
                    RfIcon(kind = RfIconKind.ARROW_RIGHT, tint = Color(0xFF865DEC), size = 16.dp)
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = Color(0x99B42345))
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFB42345))
                        .clickable {
                            val isCustomReason = selectedReason == "Otro motivo"
                            val customValidationError = if (isCustomReason) {
                                InputValidator.validateNotes(
                                    rawValue = customReasonText,
                                    fieldLabel = "El motivo de cancelación",
                                    isRequired = true,
                                    minLength = 6,
                                    maxLength = InputValidator.MAX_NOTES_LENGTH,
                                )
                            } else {
                                null
                            }
                            if (customValidationError != null) {
                                forceShowReasonError = true
                                reasonErrorText = customValidationError
                            } else {
                                val resolvedReason = if (isCustomReason) {
                                    customReasonText.trim()
                                } else {
                                    selectedReason.trim()
                                }
                                if (resolvedReason.isEmpty()) {
                                    reasonErrorText = "Indica el motivo de cancelación antes de continuar."
                                } else {
                                    if (activeEvent != null) {
                                        viewModel?.submitCancelEvent(
                                            eventId = activeEvent.id,
                                            reason = resolvedReason,
                                        )
                                    } else {
                                        viewModel?.submitCancelEvent(reason = resolvedReason)
                                    }
                                    onSelectScreenCode(dismissRoute)
                                }
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText("Confirmar cancelación", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White))
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clickable { onSelectScreenCode(dismissRoute) },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText("Mantener mi evento", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = RfThemeState.currentPalette.muted))
                }
            }
        }
    }
}

@Composable
private fun CancelTierPolicyRow85(
    label: String,
    percentText: String,
    isActive: Boolean,
) {
    val typo = LocalRfTypography.current
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        if (isActive) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .shadow(4.dp, CircleShape, spotColor = Color(0x80FDE6F2))
                    .border(4.dp, Color(0xFFFDE6F2), CircleShape)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF6664E9)))),
            )
        } else {
            Box(modifier = Modifier.size(18.dp).border(2.dp, Color(0xFFE3DCE8), CircleShape))
        }
        BasicText(
            label,
            modifier = Modifier.weight(1f),
            style = TextStyle(
                fontFamily = typo.bodyFamily,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                fontSize = 14.sp,
                color = if (isActive) RfThemeState.currentPalette.ink else Color(0xFF9A95A6),
            ),
        )
        BasicText(
            percentText,
            style = TextStyle(
                fontFamily = typo.displayFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                color = if (isActive) RfThemeState.currentPalette.ink else Color(0xFF9A95A6),
            ),
        )
    }
}

@Composable
internal fun RenderPersonalOfflineStateS91(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    var isRetrying by remember { mutableStateOf(false) }
    var retryStatusMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RfThemeState.currentPalette.bg),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(Brush.radialGradient(colors = listOf(RfThemeState.currentPalette.radialPrimary, RfThemeState.currentPalette.radialPrimary.copy(alpha = 0f)), center = Offset(0f, 0f), radius = size.width * 0.9f))
            drawRect(Brush.radialGradient(colors = listOf(RfThemeState.currentPalette.radialSecondary, RfThemeState.currentPalette.radialSecondary.copy(alpha = 0f)), center = Offset(size.width, 0f), radius = size.width * 0.8f))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(92.dp)
                .background(Color(0xFF1A1A2E))
                .padding(horizontal = 20.dp, vertical = 12.dp),
            contentAlignment = Alignment.BottomStart,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                RfIcon(kind = RfIconKind.WIFI_OFF, tint = Color.White, size = 18.dp)
                BasicText(
                    "Sin conexión · tus cambios se guardan aquí",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp, color = Color.White),
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 92.dp)
                .verticalScroll(rememberScrollState())
                .padding(top = 38.dp, start = 24.dp, end = 24.dp, bottom = 116.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .shadow(12.dp, CircleShape, spotColor = Color(0x80FDE6F2))
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFFFDE6F2), Color(0xFFEFE8FD)))),
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.WIFI_OFF, tint = Color(0xFFB754CB), size = 52.dp)
            }

            Spacer(Modifier.height(4.dp))

            BasicText("No hay conexión", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = RfThemeState.currentPalette.ink))
            BasicText(
                "Revisa tu internet. Tu lista de Mi evento está guardada localmente y se sincronizará en cuanto recuperes señal.",
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, lineHeight = 22.sp, color = RfThemeState.currentPalette.muted, textAlign = TextAlign.Center),
            )

            if (!retryStatusMessage.isNullOrBlank()) {
                BasicText(
                    retryStatusMessage ?: "",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFFC2338F), textAlign = TextAlign.Center),
                )
            }

            Spacer(Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = false)
                    .clip(RoundedCornerShape(16.dp))
                    .background(RosaButtonGradient)
                    .clickable {
                        if (!isRetrying) {
                            isRetrying = true
                            retryStatusMessage = null
                            if (viewModel != null) {
                                viewModel.syncWithServer { synced ->
                                    isRetrying = false
                                    if (synced) {
                                        onSelectScreenCode("s14")
                                    } else {
                                        retryStatusMessage = "Seguimos sin conexión al servidor. Tus cambios permanecen resguardados."
                                    }
                                }
                            } else {
                                isRetrying = false
                                onSelectScreenCode("s14")
                            }
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    RfIcon(kind = RfIconKind.REFRESH, tint = Color.White, size = 18.dp)
                    BasicText(
                        if (isRetrying) "Verificando conexión..." else "Reintentar conexión",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White),
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = Color(0x33B450B4))
                    .border(1.dp, RfThemeState.currentPalette.line, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(RfThemeState.currentPalette.surface)
                    .clickable { onSelectScreenCode("s24") },
                contentAlignment = Alignment.Center,
            ) {
                BasicText("Ver mis favoritos guardados", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = RfThemeState.currentPalette.ink))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clickable { onSelectScreenCode("BACK:s14") },
                contentAlignment = Alignment.Center,
            ) {
                BasicText("Volver al inicio", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = RfThemeState.currentPalette.muted))
            }
        }
    }
}

@Composable
internal fun RenderPersonalSuccessStateS92(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
    underlyingScreenCode: String? = null,
    renderOriginBackdrop: Boolean = true,
) {
    val typo = LocalRfTypography.current
    val originScreenCode = remember {
        resolveModalOriginScreenCode(
            underlyingScreenCode = underlyingScreenCode,
            defaultOrigin = "s18",
            excludedOrigins = setOf("s92"),
        )
    }
    val dismissRoute = remember(originScreenCode) {
        resolveModalDismissRoute(originScreenCode)
    }
    val draftQuantities = viewModel?.draftQuantities?.collectAsState()?.value ?: emptyMap()
    val totalPieces = draftQuantities.values.sum()
    val summarySubtitle = if (totalPieces > 0) {
        "$totalPieces artículo(s) en tu lista actual"
    } else {
        "Artículo agregado a tu lista"
    }
    val rootModifier = if (renderOriginBackdrop) {
        Modifier.fillMaxSize().background(RfThemeState.currentPalette.bg)
    } else {
        Modifier.fillMaxSize()
    }

    Box(modifier = rootModifier) {
        if (renderOriginBackdrop) {
            RenderModalOriginBackdrop(
                originScreenCode = originScreenCode,
                viewModel = viewModel,
                onOverlayInteraction = onSelectScreenCode,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(start = 16.dp, end = 16.dp, bottom = 112.dp)
                .shadow(18.dp, RoundedCornerShape(18.dp), spotColor = Color(0x991A1A2E))
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF1A1A2E))
                .padding(12.dp, 12.dp, 12.dp, 14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
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

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectScreenCode("s25") },
                ) {
                    BasicText("Agregado a Mi evento", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White))
                    BasicText(summarySubtitle, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.5.sp, color = Color.White.copy(alpha = 0.78f)))
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color.White.copy(alpha = 0.14f))
                        .clickable { onSelectScreenCode("s25") }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                ) {
                    BasicText(
                        "Ver carrito",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = Color.White),
                    )
                }

                Box(
                    modifier = Modifier
                        .clickable {
                            viewModel?.undoLastDraftAddition()
                            onSelectScreenCode(dismissRoute)
                        }
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                ) {
                    BasicText(
                        "Deshacer",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.ExtraBold, fontSize = 13.5.sp, color = Color(0xFFFFD45C)),
                    )
                }
            }
        }
    }
}
