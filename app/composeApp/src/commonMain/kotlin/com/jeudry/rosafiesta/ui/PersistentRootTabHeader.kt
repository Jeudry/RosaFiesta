package com.jeudry.rosafiesta.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeudry.rosafiesta.designsystem.LocalRfTypography
import com.jeudry.rosafiesta.designsystem.RfBitmapPhoto
import com.jeudry.rosafiesta.designsystem.RfColors
import com.jeudry.rosafiesta.designsystem.RfIcon
import com.jeudry.rosafiesta.designsystem.RfIconKind
import com.jeudry.rosafiesta.designsystem.RfLogo
import com.jeudry.rosafiesta.designsystem.RfLogoSize
import com.jeudry.rosafiesta.designsystem.RfPhoto
import com.jeudry.rosafiesta.designsystem.RfPhotoKey
import com.jeudry.rosafiesta.designsystem.RfThemeState
import com.jeudry.rosafiesta.designsystem.rememberDecodedImageBitmap
import com.jeudry.rosafiesta.designsystem.rfIconButtonShadow
import com.jeudry.rosafiesta.designsystem.rfInputShadow
import com.jeudry.rosafiesta.designsystem.rfPrimaryButtonShadow
import com.jeudry.rosafiesta.domain.EventStatus
import com.jeudry.rosafiesta.domain.validation.InputValidator

private val HEADER_ACTION_SIZE = 44.dp

private val PERSONAL_PERSISTENT_HEADER_CODES: Set<String> = setOf(
    "s14",
    "s15",
    "s18",
    "s37",
    "s20",
    "s21",
    "s24",
    "s39",
    "s53",
    "s79",
    "s82",
    "s16",
)

private val ADMIN_PERSISTENT_HEADER_CODES: Set<String> = setOf(
    "A01",
    "A09",
    "A11",
    "A12",
    "A02",
    "A14",
    "A22",
    "A24",
    "A26",
    "A32",
)

public fun isPersonalPersistentHeaderCode(code: String): Boolean = code in PERSONAL_PERSISTENT_HEADER_CODES

public fun isAdminPersistentHeaderCode(code: String): Boolean = code in ADMIN_PERSISTENT_HEADER_CODES

public fun hasPersistentRootHeader(code: String): Boolean {
    val isPersonal = isPersonalPersistentHeaderCode(code)
    val isAdmin = isAdminPersistentHeaderCode(code)
    return listOf(isPersonal, isAdmin).any { matched -> matched }
}

private fun persistentHeaderSlotTransition() = (
    fadeIn(tween(durationMillis = 160, easing = FastOutSlowInEasing)) +
        slideInVertically(
            animationSpec = tween(durationMillis = 170, easing = FastOutSlowInEasing),
            initialOffsetY = { fullHeight -> fullHeight / 6 },
        )
    ) togetherWith (
    fadeOut(tween(durationMillis = 110, easing = FastOutSlowInEasing)) +
        slideOutVertically(
            animationSpec = tween(durationMillis = 120, easing = FastOutSlowInEasing),
            targetOffsetY = { fullHeight -> -fullHeight / 8 },
        )
    )

@Composable
internal fun PersistentRootTabHeader(
    screenCode: String,
    viewModel: RosaFiestaViewModel,
    onSelectScreenCode: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (isAdminPersistentHeaderCode(screenCode)) {
        AdminPersistentRootHeader(
            screenCode = screenCode,
            viewModel = viewModel,
            onSelectScreenCode = onSelectScreenCode,
            modifier = modifier.testTag("persistent-root-header"),
        )
    } else if (isPersonalPersistentHeaderCode(screenCode)) {
        PersonalPersistentRootHeader(
            screenCode = screenCode,
            viewModel = viewModel,
            onSelectScreenCode = onSelectScreenCode,
            modifier = modifier.testTag("persistent-root-header"),
        )
    }
}

@Composable
private fun PersonalPersistentRootHeader(
    screenCode: String,
    viewModel: RosaFiestaViewModel,
    onSelectScreenCode: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val draftQuantitiesState = viewModel.draftQuantities.collectAsState().value
    val totalDraftCartCount = remember(draftQuantitiesState, viewModel) {
        viewModel.totalDraftCartUnits()
    }
    val normalizedTab = normalizePersonalHeaderTab(screenCode)
    val showSearchSection = normalizedTab in setOf("s14", "s18")
    val showNewEventAction = normalizedTab in setOf("s37", "s39")

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 6.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
        ) {
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier.weight(1f),
            ) {
                AnimatedContent(
                    targetState = normalizedTab,
                    transitionSpec = { persistentHeaderSlotTransition() },
                    label = "rf-personal-header-left",
                ) { targetTab ->
                    PersonalHeaderLeftSlot(
                        tabCode = targetTab,
                        viewModel = viewModel,
                        onSelectScreenCode = onSelectScreenCode,
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AnimatedContent(
                    targetState = normalizedTab,
                    transitionSpec = {
                        fadeIn(tween(150, easing = FastOutSlowInEasing)) togetherWith
                            fadeOut(tween(110, easing = FastOutSlowInEasing))
                    },
                    label = "rf-personal-header-leading-action",
                ) { targetTab ->
                    PersonalHeaderLeadingAction(
                        tabCode = targetTab,
                        viewModel = viewModel,
                        onSelectScreenCode = onSelectScreenCode,
                    )
                }

                RfDraftCartPillButton(
                    count = totalDraftCartCount,
                    buttonHeight = HEADER_ACTION_SIZE,
                    onClick = { onSelectScreenCode(viewModel.resolveMyEventEntryTarget()) },
                )

                AnimatedVisibility(
                    visible = showNewEventAction,
                    enter = fadeIn(tween(150)),
                    exit = fadeOut(tween(110)),
                ) {
                    HeaderGradientActionCircle(
                        icon = RfIconKind.PLUS,
                        accessibilityLabel = "Nuevo evento",
                        isAdmin = false,
                        onClick = {
                            viewModel.startStandaloneEventCreation()
                            onSelectScreenCode("s38")
                        },
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = showSearchSection,
            enter = fadeIn(tween(160, easing = FastOutSlowInEasing)) +
                expandVertically(tween(180, easing = FastOutSlowInEasing)),
            exit = fadeOut(tween(120, easing = FastOutSlowInEasing)) +
                shrinkVertically(tween(160, easing = FastOutSlowInEasing)),
        ) {
            PersonalPersistentSearchSection(
                screenCode = normalizedTab,
                viewModel = viewModel,
                onSelectScreenCode = onSelectScreenCode,
            )
        }
    }
}

private fun normalizePersonalHeaderTab(screenCode: String): String = when (screenCode) {
    "s15" -> "s14"
    else -> screenCode
}

@Composable
private fun PersonalHeaderLeftSlot(
    tabCode: String,
    viewModel: RosaFiestaViewModel,
    onSelectScreenCode: (String) -> Unit,
) {
    when (tabCode) {
        "s14" -> {
            PersonalHomeGreetingHeaderLeft(
                viewModel = viewModel,
                onSelectScreenCode = onSelectScreenCode,
            )
        }

        "s18" -> {
            val articles = viewModel.articles.collectAsState().value
            val visibleCount = remember(articles) {
                articles.count { article -> article.visibleInCatalog }
            }
            val articleWord = if (visibleCount == 1) "artículo disponible" else "artículos disponibles"
            HeaderTitleBlock(
                title = "Catálogo",
                subtitle = "$visibleCount $articleWord",
                isAdmin = false,
            )
        }

        "s37" -> {
            val observedEvents = viewModel.events.collectAsState().value
            val observedUser = viewModel.currentAuthenticatedUser.collectAsState().value
            val observedGuest = viewModel.isExplicitGuestSession.collectAsState().value
            val observedName = viewModel.profileName.collectAsState().value
            val upcomingCount = remember(observedEvents, observedUser, observedGuest, observedName, viewModel) {
                viewModel.clientEventsForCurrentUser().count { eventItem ->
                    val isClosed = listOf(
                        eventItem.status == EventStatus.COMPLETED,
                        eventItem.status == EventStatus.CANCELLED,
                    ).any { closed -> closed }
                    !isClosed
                }
            }
            val upcomingLabel = if (upcomingCount == 1) "1 evento activo" else "$upcomingCount eventos activos"
            HeaderTitleBlock(
                title = "Mis eventos",
                subtitle = upcomingLabel,
                isAdmin = false,
            )
        }

        "s20" -> {
            HeaderBackAndTitleSlot(
                title = "Categorías",
                subtitle = "Explora el catálogo por tipo",
                isAdmin = false,
                onBack = { onSelectScreenCode("BACK:s18") },
            )
        }

        "s21" -> {
            HeaderBackAndTitleSlot(
                title = "Paquetes",
                subtitle = "Colecciones listas para tu evento",
                isAdmin = false,
                onBack = { onSelectScreenCode("BACK:s14") },
            )
        }

        "s24" -> {
            val favoritesCount = viewModel.favorites.collectAsState().value.size
            val itemWord = if (favoritesCount == 1) "artículo guardado" else "artículos guardados"
            HeaderBackAndTitleSlot(
                title = "Favoritos",
                subtitle = "$favoritesCount $itemWord",
                isAdmin = false,
                onBack = { onSelectScreenCode("BACK:s14") },
            )
        }

        "s39" -> {
            HeaderBackAndTitleSlot(
                title = "Calendario",
                subtitle = "Agenda de tus eventos",
                isAdmin = false,
                onBack = { onSelectScreenCode("BACK:s37") },
            )
        }

        "s53" -> {
            HeaderBackAndTitleSlot(
                title = "Mis reservas",
                subtitle = "Seguimiento de tus fechas",
                isAdmin = false,
                onBack = { onSelectScreenCode("BACK:s14") },
            )
        }

        "s79" -> {
            val rawNotifications = viewModel.notifications.collectAsState().value
            val clientNotifications = viewModel.filterNotificationsForPersonalClient(rawNotifications)
            val unreadCount = clientNotifications.count { notification -> !notification.isRead }
            val subtitleText = if (unreadCount == 0) "Al día · 0 sin leer" else "$unreadCount sin leer"
            HeaderBackAndTitleSlot(
                title = "Notificaciones",
                subtitle = subtitleText,
                isAdmin = false,
                onBack = { onSelectScreenCode("BACK:s14") },
            )
        }

        "s82" -> {
            viewModel.events.collectAsState().value
            viewModel.currentAuthenticatedUser.collectAsState().value
            viewModel.profileName.collectAsState().value
            val clientEventsCount = viewModel.clientEventsForCurrentUser().size
            HeaderBackAndTitleSlot(
                title = "Mis pagos",
                subtitle = "Recibos y balances ($clientEventsCount)",
                isAdmin = false,
                onBack = { onSelectScreenCode("BACK:s14") },
            )
        }

        else -> {
            HeaderBackAndTitleSlot(
                title = "Acerca de",
                subtitle = "Rosa Fiesta · Eventos",
                isAdmin = false,
                onBack = { onSelectScreenCode("BACK:s14") },
            )
        }
    }
}

@Composable
private fun PersonalHomeGreetingHeaderLeft(
    viewModel: RosaFiestaViewModel,
    onSelectScreenCode: (String) -> Unit,
) {
    val typo = LocalRfTypography.current
    val palette = RfThemeState.currentPalette
    val fullName = viewModel.profileName.collectAsState().value
    val avatarKey = viewModel.profileAvatarKey.collectAsState().value
    val avatarBytes = viewModel.profileAvatarBytes.collectAsState().value
    val firstName = fullName.trim().substringBefore(" ").ifBlank { "Camila" }
    val avatarInitial = firstName.take(1).uppercase()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectScreenCode("s54") },
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Color(0xFFEA4EA9), Color(0xFF865DEC))))
                .padding(2.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(palette.surface),
            ) {
                val matchedPhoto = remember(avatarKey) {
                    if (avatarKey.isNullOrBlank()) {
                        null
                    } else {
                        RfPhotoKey.entries.firstOrNull { entry -> entry.name == avatarKey }
                    }
                }
                val isCustomAvatar = avatarKey?.startsWith("CUSTOM:") == true
                val customAvatarBitmap = rememberDecodedImageBitmap(
                    bytes = if (isCustomAvatar) avatarBytes else null,
                )
                if (matchedPhoto != null) {
                    RfPhoto(
                        photoKey = matchedPhoto,
                        contentDescription = fullName,
                        cornerRadius = 999.dp,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else if (customAvatarBitmap != null) {
                    RfBitmapPhoto(
                        bitmap = customAvatarBitmap,
                        contentDescription = fullName,
                        cornerRadius = 999.dp,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else if (isCustomAvatar) {
                    RfIcon(kind = RfIconKind.CAMERA, tint = Color(0xFFC84FB8), size = 20.dp)
                } else {
                    BasicText(
                        text = avatarInitial,
                        style = typo.rootTabTitle.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFC84FB8),
                        ),
                    )
                }
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            BasicText(
                text = "Hola de nuevo",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = typo.caption.copy(fontSize = 13.sp, color = palette.muted),
            )
            BasicText(
                text = firstName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = typo.rootTabTitle.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.ink,
                ),
            )
        }
    }
}

@Composable
private fun PersonalHeaderLeadingAction(
    tabCode: String,
    viewModel: RosaFiestaViewModel,
    onSelectScreenCode: (String) -> Unit,
) {
    val palette = RfThemeState.currentPalette
    when (tabCode) {
        "s18" -> {
            HeaderSurfaceActionCircle(
                icon = RfIconKind.GRID,
                tint = palette.ink,
                accessibilityLabel = "Categorías",
                isAdmin = false,
                onClick = { onSelectScreenCode("s20") },
            )
        }

        "s37" -> {
            HeaderSurfaceActionCircle(
                icon = RfIconKind.CALENDAR,
                tint = palette.ink,
                accessibilityLabel = "Calendario",
                isAdmin = false,
                onClick = { onSelectScreenCode("s39") },
            )
        }

        "s14" -> {
            val notifications = viewModel.notifications.collectAsState().value
            val clientNotifications = viewModel.filterNotificationsForPersonalClient(notifications)
            val hasUnread = clientNotifications.any { item -> !item.isRead }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(HEADER_ACTION_SIZE)
                    .rfIconButtonShadow(CircleShape, isAdmin = false)
                    .clip(CircleShape)
                    .background(palette.surface)
                    .border(1.dp, palette.line, CircleShape)
                    .clickable { onSelectScreenCode("s79") },
            ) {
                RfIcon(kind = RfIconKind.BELL, tint = palette.ink, size = 21.dp)
                if (hasUnread) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 9.dp, end = 10.dp)
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE53958))
                            .border(2.dp, palette.surface, CircleShape),
                    )
                }
            }
        }

        else -> {
            Box(modifier = Modifier.size(0.dp))
        }
    }
}

@Composable
private fun PersonalPersistentSearchSection(
    screenCode: String,
    viewModel: RosaFiestaViewModel,
    onSelectScreenCode: (String) -> Unit,
) {
    val typo = LocalRfTypography.current
    val palette = RfThemeState.currentPalette
    val vmQuery = viewModel.searchQuery.collectAsState().value
    var hasInteractedSearch by remember { mutableStateOf(false) }
    val searchFieldError = rememberDebouncedValidationError(
        value = vmQuery,
        hasInteracted = hasInteractedSearch,
    ) { query ->
        InputValidator.validateSearchQuery(query)
    }
    val selectedCategory = viewModel.selectedCategory.collectAsState().value
    val observedMaxPrice = viewModel.maxPriceFilterDop.collectAsState().value
    val observedOnlyAvailable = viewModel.onlyAvailableFilter.collectAsState().value
    val isInlineVoiceListening = viewModel.isVoiceListening.collectAsState().value
    val voiceOrigin = viewModel.voiceInputOrigin.collectAsState().value
    val isSearchMicActive = if (isInlineVoiceListening) voiceOrigin == VoiceInputOrigin.SEARCH else false

    val isCategoryFiltered = !isUnfilteredPersonalCategory(selectedCategory)
    val isPriceFiltered = observedMaxPrice < 5000
    val activeFilterCount = listOf(isCategoryFiltered, isPriceFiltered, observedOnlyAvailable).count { active -> active }
    val placeholderText = if (screenCode == "s18") {
        "Buscar sillas, mesas, flores…"
    } else {
        "¿Qué celebramos hoy? (sillas, arcos, mesas...)"
    }
    val searchBorderColor = if (searchFieldError != null) Color(0xFFE53935) else palette.line

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .rfInputShadow(RoundedCornerShape(16.dp), isAdmin = false)
                .clip(RoundedCornerShape(16.dp))
                .background(palette.surface)
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
                    RfIcon(
                        kind = RfIconKind.SEARCH,
                        tint = palette.muted,
                        size = 20.dp,
                        modifier = Modifier.clickable { onSelectScreenCode("s80") },
                    )
                    Box(modifier = Modifier.weight(1f)) {
                        if (vmQuery.isEmpty()) {
                            BasicText(
                                text = placeholderText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = typo.body.copy(fontSize = 14.5.sp, color = palette.muted),
                            )
                        }
                        BasicTextField(
                            value = vmQuery,
                            onValueChange = { updatedQuery ->
                                val cappedQuery = updatedQuery.take(InputValidator.MAX_SEARCH_QUERY_LENGTH)
                                hasInteractedSearch = true
                                viewModel.setSearchQuery(cappedQuery)
                            },
                            textStyle = typo.body.copy(fontSize = 15.sp, color = palette.ink),
                            cursorBrush = SolidColor(Color(0xFFC2338F)),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )
                    }
                }

                if (vmQuery.isNotEmpty()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(palette.chipBg)
                            .clickable { viewModel.setSearchQuery("") },
                    ) {
                        RfIcon(kind = RfIconKind.CLOSE, tint = palette.muted, size = 14.dp)
                    }
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (activeFilterCount > 0) Color(0xFFFDE6F2) else palette.chipBg)
                        .clickable { onSelectScreenCode("s81") },
                ) {
                    RfIcon(
                        kind = RfIconKind.FILTER,
                        tint = if (activeFilterCount > 0) Color(0xFFC2338F) else Color(0xFFB54BD6),
                        size = 18.dp,
                    )
                    if (activeFilterCount > 0) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 3.dp, end = 3.dp)
                                .size(15.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFC2338F)),
                        ) {
                            BasicText(
                                text = "$activeFilterCount",
                                style = typo.caption.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White),
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
                                SolidColor(palette.chipBg)
                            },
                        )
                        .clickable { viewModel.toggleInlineVoiceSearch() },
                ) {
                    val micTint = if (isSearchMicActive) Color.White else Color(0xFFB54BD6)
                    RfIcon(kind = RfIconKind.MIC, tint = micTint, size = 20.dp)
                }
            }
        }

        RfValidationErrorText(errorMessage = searchFieldError)

        InlineVoiceSearchBanner(
            viewModel = viewModel,
            onApplyVoiceQuery = { spokenQuery ->
                val cappedSpoken = spokenQuery.take(InputValidator.MAX_SEARCH_QUERY_LENGTH)
                hasInteractedSearch = true
                viewModel.setSearchQuery(cappedSpoken)
            },
        )
    }
}

private fun isUnfilteredPersonalCategory(category: String): Boolean {
    val clean = category.trim()
    val isEmpty = clean.isEmpty()
    val isTodas = clean.equals("Todas", ignoreCase = true)
    val isTodos = clean.equals("Todos", ignoreCase = true)
    return listOf(isEmpty, isTodas, isTodos).any { matched -> matched }
}

@Composable
private fun AdminPersistentRootHeader(
    screenCode: String,
    viewModel: RosaFiestaViewModel,
    onSelectScreenCode: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier.weight(1f),
            ) {
                AnimatedContent(
                    targetState = screenCode,
                    transitionSpec = { persistentHeaderSlotTransition() },
                    label = "rf-admin-header-left",
                ) { targetCode ->
                    AdminHeaderLeftSlot(
                        screenCode = targetCode,
                        viewModel = viewModel,
                        onSelectScreenCode = onSelectScreenCode,
                    )
                }
            }

            AnimatedContent(
                targetState = screenCode,
                transitionSpec = {
                    fadeIn(tween(150, easing = FastOutSlowInEasing)) togetherWith
                        fadeOut(tween(110, easing = FastOutSlowInEasing))
                },
                label = "rf-admin-header-right",
            ) { targetCode ->
                AdminHeaderRightActions(
                    screenCode = targetCode,
                    viewModel = viewModel,
                    onSelectScreenCode = onSelectScreenCode,
                )
            }
        }
    }
}

@Composable
private fun AdminHeaderLeftSlot(
    screenCode: String,
    viewModel: RosaFiestaViewModel,
    onSelectScreenCode: (String) -> Unit,
) {
    val typo = LocalRfTypography.current
    when (screenCode) {
        "A01" -> {
            val rawProfileName = viewModel.profileName.collectAsState().value
            val adminFirstName = rawProfileName.trim().substringBefore(" ").ifBlank { "María" }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectScreenCode("s54") },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(HEADER_ACTION_SIZE)
                        .rfIconButtonShadow(CircleShape, isAdmin = true)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, RfColors.IndigoTint, CircleShape),
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
                                style = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4B47D6),
                                ),
                            )
                        }
                    }
                    BasicText(
                        text = "Hola, $adminFirstName",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(
                            fontFamily = typo.displayFamily,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF151729),
                            lineHeight = 24.sp,
                        ),
                    )
                }
            }
        }

        "A09" -> {
            val articles = viewModel.articles.collectAsState().value
            val vmCategories = viewModel.catalogCategories.collectAsState().value
            val distinctCategoriesCount = remember(vmCategories, articles) {
                val fromCatalog = vmCategories.map { category -> category.name.trim() }.filter { name -> name.isNotEmpty() }
                val fromArticles = articles.map { article -> article.category.trim() }.filter { name -> name.isNotEmpty() }
                (fromCatalog + fromArticles).distinctBy { categoryName -> categoryName.lowercase() }.size
            }
            val articleWord = if (articles.size == 1) "artículo" else "artículos"
            val categoryWord = if (distinctCategoriesCount == 1) "categoría" else "categorías"
            HeaderTitleBlock(
                title = "Inventario",
                subtitle = "${articles.size} $articleWord · $distinctCategoriesCount $categoryWord",
                isAdmin = true,
            )
        }

        "A11" -> {
            val allEvents = viewModel.events.collectAsState().value
            val metrics = viewModel.metrics.collectAsState().value
            val upcomingCount = remember(allEvents) {
                allEvents.count { eventItem ->
                    val isClosed = listOf(
                        eventItem.status == EventStatus.COMPLETED,
                        eventItem.status == EventStatus.CANCELLED,
                    ).any { closed -> closed }
                    !isClosed
                }
            }
            HeaderTitleBlock(
                title = "Eventos",
                subtitle = "${metrics.monthLabel} · $upcomingCount próximos",
                isAdmin = true,
            )
        }

        "A12" -> {
            HeaderTitleBlock(
                title = "Más",
                subtitle = "Módulos, operación y ajustes del negocio",
                isAdmin = true,
            )
        }

        "A02" -> {
            HeaderBackAndTitleSlot(
                title = "Solicitudes",
                subtitle = "Cotizaciones y prospectos entrantes",
                isAdmin = true,
                onBack = { onSelectScreenCode("BACK:A01") },
            )
        }

        "A14" -> {
            val unreadTotal = viewModel.chatThreads.collectAsState().value.sumOf { thread -> thread.unreadCount }
            HeaderBackAndTitleSlot(
                title = "Chats",
                subtitle = "$unreadTotal sin leer",
                isAdmin = true,
                onBack = { onSelectScreenCode("BACK:A01") },
            )
        }

        "A22" -> {
            val quotesCount = viewModel.quotes.collectAsState().value.size
            HeaderBackAndTitleSlot(
                title = "Cotizaciones",
                subtitle = "$quotesCount registradas",
                isAdmin = true,
                onBack = { onSelectScreenCode("BACK:A12") },
            )
        }

        "A24" -> {
            val activePackagesCount = viewModel.packages.collectAsState().value.count { pkg -> pkg.visibleInCatalog }
            HeaderBackAndTitleSlot(
                title = "Paquetes",
                subtitle = "$activePackagesCount activos en el catálogo",
                isAdmin = true,
                onBack = { onSelectScreenCode("BACK:A12") },
            )
        }

        "A26" -> {
            val clientsCount = viewModel.clients.collectAsState().value.size
            HeaderBackAndTitleSlot(
                title = "Clientes",
                subtitle = "$clientsCount clientes",
                isAdmin = true,
                onBack = { onSelectScreenCode("BACK:A12") },
            )
        }

        else -> {
            val pendingRefundsCount = viewModel.refundRequests.collectAsState().value.count { refund ->
                refund.status.trim().lowercase() == "pending"
            }
            HeaderBackAndTitleSlot(
                title = "Cancelaciones",
                subtitle = "$pendingRefundsCount solicitudes por aprobar",
                isAdmin = true,
                onBack = { onSelectScreenCode("BACK:A12") },
            )
        }
    }
}

@Composable
private fun AdminHeaderRightActions(
    screenCode: String,
    viewModel: RosaFiestaViewModel,
    onSelectScreenCode: (String) -> Unit,
) {
    val notifications = viewModel.notifications.collectAsState().value
    val unreadNotificationsCount = notifications.count { item -> !item.isRead }

    when (screenCode) {
        "A09" -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                HeaderSurfaceActionCircle(
                    icon = RfIconKind.CATALOG,
                    tint = Color(0xFF4B47D6),
                    accessibilityLabel = "Categorías",
                    isAdmin = true,
                    onClick = { onSelectScreenCode("A40") },
                )
                HeaderSurfaceActionCircle(
                    icon = RfIconKind.TREND_UP,
                    tint = Color(0xFFC2338F),
                    accessibilityLabel = "Tendencias",
                    isAdmin = true,
                    onClick = { onSelectScreenCode("A41") },
                )
                HeaderSurfaceActionCircle(
                    icon = RfIconKind.CALENDAR,
                    tint = Color(0xFF151729),
                    accessibilityLabel = "Calendario",
                    isAdmin = true,
                    onClick = { onSelectScreenCode("s63") },
                )
                HeaderGradientActionCircle(
                    icon = RfIconKind.PLUS,
                    accessibilityLabel = "Nuevo artículo",
                    isAdmin = true,
                    onClick = {
                        viewModel.startEditingArticle(null)
                        onSelectScreenCode("A10")
                    },
                )
            }
        }

        "A11" -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                HeaderSurfaceActionCircle(
                    icon = RfIconKind.CALENDAR,
                    tint = Color(0xFF151729),
                    accessibilityLabel = "Calendario",
                    isAdmin = true,
                    onClick = { onSelectScreenCode("s63") },
                )
                HeaderGradientActionCircle(
                    icon = RfIconKind.PLUS,
                    accessibilityLabel = "Crear evento",
                    isAdmin = true,
                    onClick = { onSelectScreenCode("A04") },
                )
            }
        }

        "A24" -> {
            HeaderGradientActionCircle(
                icon = RfIconKind.PLUS,
                accessibilityLabel = "Nuevo paquete",
                isAdmin = true,
                onClick = {
                    viewModel.startEditingPackage("NEW")
                    onSelectScreenCode("A25")
                },
            )
        }

        else -> {
            AdminNotificationBellButton(
                unreadCount = unreadNotificationsCount,
                onClick = { onSelectScreenCode("A29") },
            )
        }
    }
}

@Composable
private fun HeaderTitleBlock(
    title: String,
    subtitle: String,
    isAdmin: Boolean,
) {
    val typo = LocalRfTypography.current
    val palette = RfThemeState.currentPalette
    val titleColor = if (isAdmin) Color(0xFF2E324A) else palette.ink
    val subtitleColor = if (isAdmin) Color(0xFF5F6377) else palette.muted
    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
        BasicText(
            text = title,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = TextStyle(
                fontFamily = typo.displayFamily,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = titleColor,
            ),
        )
        BasicText(
            text = subtitle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = TextStyle(
                fontFamily = typo.bodyFamily,
                fontSize = 12.5.sp,
                color = subtitleColor,
            ),
        )
    }
}

@Composable
private fun HeaderBackAndTitleSlot(
    title: String,
    subtitle: String,
    isAdmin: Boolean,
    onBack: () -> Unit,
) {
    val palette = RfThemeState.currentPalette
    val iconTint = if (isAdmin) Color(0xFF151729) else palette.ink
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        HeaderSurfaceActionCircle(
            icon = RfIconKind.BACK,
            tint = iconTint,
            accessibilityLabel = "Volver",
            isAdmin = isAdmin,
            onClick = onBack,
        )
        Box(modifier = Modifier.weight(1f)) {
            HeaderTitleBlock(
                title = title,
                subtitle = subtitle,
                isAdmin = isAdmin,
            )
        }
    }
}

@Composable
private fun HeaderSurfaceActionCircle(
    icon: RfIconKind,
    tint: Color,
    accessibilityLabel: String,
    isAdmin: Boolean,
    onClick: () -> Unit,
) {
    val palette = RfThemeState.currentPalette
    val surfaceColor = if (isAdmin) Color.White else palette.surface
    val borderColor = if (isAdmin) Color(0xFFE3E4F2) else palette.line
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(HEADER_ACTION_SIZE)
            .rfIconButtonShadow(CircleShape, isAdmin = isAdmin)
            .clip(CircleShape)
            .background(surfaceColor)
            .border(1.dp, borderColor, CircleShape)
            .semantics { contentDescription = accessibilityLabel }
            .clickable(onClick = onClick),
    ) {
        RfIcon(kind = icon, tint = tint, size = 20.dp)
    }
}

@Composable
private fun HeaderGradientActionCircle(
    icon: RfIconKind,
    accessibilityLabel: String,
    isAdmin: Boolean,
    onClick: () -> Unit,
) {
    val gradient = if (isAdmin) {
        Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0)))
    } else {
        Brush.horizontalGradient(listOf(Color(0xFFEA4EA9), Color(0xFF865DEC)))
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(HEADER_ACTION_SIZE)
            .rfPrimaryButtonShadow(CircleShape, isAdmin = isAdmin)
            .clip(CircleShape)
            .background(gradient)
            .semantics { contentDescription = accessibilityLabel }
            .clickable(onClick = onClick),
    ) {
        RfIcon(kind = icon, tint = Color.White, size = 21.dp)
    }
}

@Composable
private fun AdminNotificationBellButton(
    unreadCount: Int,
    onClick: () -> Unit,
) {
    val typo = LocalRfTypography.current
    Box(
        modifier = Modifier
            .size(HEADER_ACTION_SIZE)
            .clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .rfIconButtonShadow(CircleShape, isAdmin = true)
                .clip(CircleShape)
                .background(Color.White)
                .border(1.dp, Color(0xFFE3E4F2), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            RfIcon(kind = RfIconKind.BELL, tint = Color(0xFF151729), size = 21.dp)
        }
        if (unreadCount > 0) {
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
                    text = "$unreadCount",
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF2E1A47),
                    ),
                )
            }
        }
    }
}
