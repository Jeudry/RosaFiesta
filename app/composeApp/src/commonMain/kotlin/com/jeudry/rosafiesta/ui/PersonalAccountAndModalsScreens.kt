package com.jeudry.rosafiesta.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeudry.rosafiesta.designsystem.LocalRfTypography
import com.jeudry.rosafiesta.designsystem.RfButton
import com.jeudry.rosafiesta.designsystem.RfButtonStyle
import com.jeudry.rosafiesta.designsystem.RfColors
import com.jeudry.rosafiesta.designsystem.RfGradientPriceText
import com.jeudry.rosafiesta.designsystem.RfIcon
import com.jeudry.rosafiesta.designsystem.RfIconKind
import com.jeudry.rosafiesta.designsystem.RfPhoto
import com.jeudry.rosafiesta.designsystem.RfPhotoKey
import com.jeudry.rosafiesta.designsystem.RfRadialBackgroundBox
import com.jeudry.rosafiesta.designsystem.RfThemeVariant
import com.jeudry.rosafiesta.designsystem.RosaAvatar
import com.jeudry.rosafiesta.designsystem.RosaAvatarSize
import com.jeudry.rosafiesta.designsystem.rememberRfSkeletonBrush
import com.jeudry.rosafiesta.designsystem.rfCardShadow
import com.jeudry.rosafiesta.designsystem.rfHeroBannerShadow
import com.jeudry.rosafiesta.designsystem.rfIconButtonShadow
import com.jeudry.rosafiesta.designsystem.rfInputShadow
import com.jeudry.rosafiesta.designsystem.rfPrimaryButtonShadow

private val PersonalButtonGradient = Brush.horizontalGradient(
    listOf(Color(0xFFFF3366), Color(0xFFE6007A)),
)

private val RosaButtonGradient = Brush.horizontalGradient(
    listOf(Color(0xFFEA4EA9), Color(0xFFB754CB), Color(0xFF865DEC)),
)

// ==============================================================================
// s79 · NOTIFICACIONES PERSONAL
// ==============================================================================
@Composable
internal fun RenderPersonalNotificationsS79(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    var allMarkedRead by remember { mutableStateOf(false) }
    var selectedFilterIndex by remember { mutableStateOf(0) }

    RfRadialBackgroundBox(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Header
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
                        .background(Color.White)
                        .clickable { onSelectScreenCode("BACK:s14") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1A1A2E), size = 22.dp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "Notificaciones",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF1A1A2E)),
                    )
                    BasicText(
                        text = if (allMarkedRead) "Al día · 0 sin leer" else "3 sin leer",
                        maxLines = 1,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF62627A)),
                    )
                }

                BasicText(
                    text = if (allMarkedRead) "Leídas" else "Marcar leídas",
                    modifier = Modifier.clickable { allMarkedRead = !allMarkedRead },
                    style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFC2338F)),
                )
            }

            // Filter chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Todas", "Pagos", "Mis eventos", "Mensajes").forEachIndexed { index, label ->
                    val selected = index == selectedFilterIndex
                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (selected) {
                                    Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9)))
                                } else {
                                    SolidColor(Color(0xFFF1EFF5))
                                },
                            )
                            .clickable { selectedFilterIndex = index }
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
            }

            NotificationSectionLabel79("Hoy")
            NotificationCard79(
                items = listOf(
                    NotificationItem79(
                        title = "Tu cotización está lista",
                        body = "Boda Real Rosa & Dorado · Total RD$ 4,500. Revísala y aparta tu fecha.",
                        time = "9:40 a. m.",
                        icon = RfIconKind.NOTE,
                        iconBg = Color(0xFFFDE6F2),
                        iconTint = Color(0xFFC2338F),
                        unread = true,
                        onClick = { onSelectScreenCode("s48") },
                    ),
                    NotificationItem79(
                        title = "Nuevo mensaje de Rosa Fiesta",
                        body = "María: “¡Confirmado el arco de globos para el viernes!”",
                        time = "8:15 a. m.",
                        icon = RfIconKind.CHAT,
                        iconBg = Color(0xFFEFE8FD),
                        iconTint = Color(0xFF7A4FD8),
                        unread = true,
                        onClick = { onSelectScreenCode("s42") },
                    ),
                    NotificationItem79(
                        title = "Tu boda es en 3 días",
                        body = "Revisa tu checklist y confirma la hora de montaje.",
                        time = "7:00 a. m.",
                        icon = RfIconKind.CALENDAR,
                        iconBg = Color(0xFFFFF2C9),
                        iconTint = Color(0xFF7A5700),
                        unread = true,
                        onClick = { onSelectScreenCode("s46") },
                    ),
                ),
            )

            NotificationSectionLabel79("Ayer")
            NotificationCard79(
                items = listOf(
                    NotificationItem79(
                        title = "Recordatorio de pago",
                        body = "El saldo de RD$ 3,150 de tu boda vence el jue 22 oct.",
                        time = "6:30 p. m.",
                        icon = RfIconKind.WALLET,
                        iconBg = Color(0xFFEEF6D6),
                        iconTint = Color(0xFF4E6A10),
                        unread = false,
                        onClick = { onSelectScreenCode("s82") },
                    ),
                    NotificationItem79(
                        title = "Rosa tiene ideas para ti",
                        body = "3 combinaciones para la mesa de dulces de tu quinceañera.",
                        time = "11:02 a. m.",
                        icon = RfIconKind.SPARKLE,
                        iconBg = Color(0xFFDDF4F3),
                        iconTint = Color(0xFF1F8A84),
                        unread = false,
                        onClick = { onSelectScreenCode("s29") },
                    ),
                ),
            )

            NotificationSectionLabel79("Esta semana")
            NotificationCard79(
                items = listOf(
                    NotificationItem79(
                        title = "Recibimos tu anticipo",
                        body = "RD$ 1,350 · Boda Real Rosa & Dorado. Ver recibo.",
                        time = "18 oct",
                        icon = RfIconKind.CHECK,
                        iconBg = Color(0xFFEEF6D6),
                        iconTint = Color(0xFF4E6A10),
                        unread = false,
                        onClick = { onSelectScreenCode("s83") },
                    ),
                ),
            )

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
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
            color = Color(0xFF62627A),
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
            .background(Color.White)
            .border(1.dp, Color(0xFFF2EDF5), RoundedCornerShape(20.dp)),
    ) {
        items.forEachIndexed { index, item ->
            if (index > 0) {
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF2F0F5)))
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
                            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.5.sp, color = Color(0xFF1A1A2E)),
                        )
                        BasicText(
                            text = item.time,
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF62627A)),
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    BasicText(
                        text = item.body,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.5.sp, lineHeight = 18.9.sp, color = Color(0xFF62627A)),
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

// ==============================================================================
// s80 · BÚSQUEDA
// ==============================================================================
@Composable
internal fun RenderPersonalSearchS80(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val cardShape = RoundedCornerShape(20.dp)
    val vmQuery = viewModel?.searchQuery?.collectAsState()?.value ?: ""
    var localQuery by remember(vmQuery) { mutableStateOf(vmQuery) }
    val selectedCategory = viewModel?.selectedCategory?.collectAsState()?.value ?: "Todas"
    val maxPriceFilter = viewModel?.maxPriceFilterDop?.collectAsState()?.value ?: 5000
    val onlyAvailable = viewModel?.onlyAvailableFilter?.collectAsState()?.value ?: true
    val sortOrder = viewModel?.searchSortOrder?.collectAsState()?.value ?: "Populares"
    val allArticles = viewModel?.articles?.collectAsState()?.value ?: com.jeudry.rosafiesta.domain.CanonicalSampleData.articles
    val isInlineVoiceListening = viewModel?.isVoiceListening?.collectAsState()?.value ?: false
    val voiceOrigin = viewModel?.voiceInputOrigin?.collectAsState()?.value ?: VoiceInputOrigin.NONE
    val isSearchMicActive = if (isInlineVoiceListening) voiceOrigin == VoiceInputOrigin.SEARCH else false

    val filteredArticles = remember(localQuery, selectedCategory, maxPriceFilter, onlyAvailable, sortOrder, allArticles) {
        val trimmedQuery = localQuery.trim()
        val matchingList = allArticles.filter { article ->
            val matchesQuery = if (trimmedQuery.isEmpty()) {
                true
            } else {
                val byName = article.name.contains(trimmedQuery, ignoreCase = true)
                val byCategory = article.category.contains(trimmedQuery, ignoreCase = true)
                val byCode = article.code.contains(trimmedQuery, ignoreCase = true)
                val byDesc = article.description.contains(trimmedQuery, ignoreCase = true)
                listOf(byName, byCategory, byCode, byDesc).any { matched -> matched }
            }
            val matchesCategory = if (selectedCategory == "Todas") {
                true
            } else {
                article.category.equals(selectedCategory, ignoreCase = true)
            }
            val matchesPrice = article.pricePerDayDop <= maxPriceFilter
            val matchesStock = if (onlyAvailable) {
                article.totalStock > 0
            } else {
                true
            }
            listOf(matchesQuery, matchesCategory, matchesPrice, matchesStock).all { matched -> matched }
        }
        when (sortOrder) {
            "Precio ↑" -> matchingList.sortedBy { article -> article.pricePerDayDop }
            "Precio ↓" -> matchingList.sortedByDescending { article -> article.pricePerDayDop }
            "Nuevos" -> matchingList.sortedByDescending { article -> article.code }
            else -> matchingList
        }
    }

    val activeFilterCount = listOf(
        selectedCategory != "Todas",
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
                        .background(Color.White)
                        .clickable { onSelectScreenCode("BACK:s14") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1A1A2E), size = 22.dp)
                }

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .rfInputShadow(RoundedCornerShape(16.dp), isAdmin = false)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(start = 16.dp, end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    RfIcon(kind = RfIconKind.SEARCH, tint = Color(0xFF62627A), size = 18.dp)
                    BasicTextField(
                        value = localQuery,
                        onValueChange = { updatedText ->
                            localQuery = updatedText
                            viewModel?.setSearchQuery(updatedText)
                        },
                        singleLine = true,
                        textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF1A1A2E)),
                        cursorBrush = SolidColor(Color(0xFFC2338F)),
                        modifier = Modifier.weight(1f),
                        decorationBox = { innerField ->
                            Box(contentAlignment = Alignment.CenterStart) {
                                if (localQuery.isEmpty()) {
                                    BasicText(
                                        text = "Buscar sillas, mesas, arcos...",
                                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF62627A)),
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
                                .background(Color(0xFFF1EFF5))
                                .clickable {
                                    localQuery = ""
                                    viewModel?.setSearchQuery("")
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            RfIcon(kind = RfIconKind.CLOSE, tint = Color(0xFF62627A), size = 14.dp)
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
                            .background(Color.White)
                            .clickable { onSelectScreenCode("s81") },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.FILTER, tint = Color(0xFF1A1A2E), size = 20.dp)
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
                onOpenFullScreenVoice = {
                    onSelectScreenCode("s35")
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
                                .background(Color(0xFFF1EFF5))
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
                if (selectedCategory != "Todas") {
                    SearchFilterChip80(
                        label = selectedCategory,
                        isActive = true,
                        onClick = { viewModel?.setSelectedCategory("Todas") },
                    )
                }
                SearchFilterChip80(
                    label = if (onlyAvailable) "Disponible vie 23 oct" else "Incluir agotados",
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
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1A1A2E)),
                    )
                    if (localQuery.isNotBlank()) {
                        BasicText(
                            text = " para “${localQuery.trim()}”",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF62627A)),
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            val nextOrder = when (sortOrder) {
                                "Populares" -> "Precio ↑"
                                "Precio ↑" -> "Precio ↓"
                                "Precio ↓" -> "Nuevos"
                                else -> "Populares"
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
                        .background(Color.White)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        BasicText(
                            text = "Sin resultados para tu búsqueda",
                            style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1A1A2E)),
                        )
                        BasicText(
                            text = "Intenta con otro término o limpia los filtros activos.",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF62627A)),
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
                                    viewModel?.setSelectedCategory("Todas")
                                    viewModel?.setMaxPriceFilterDop(5000)
                                    viewModel?.setOnlyAvailableFilter(false)
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
                val freeCount = article.totalStock.coerceAtLeast(0)
                val lowStock = freeCount <= 3
                val photoKey = RfPhotoKey.entries.firstOrNull { entry ->
                    val matchesAsset = entry.assetKey == article.photo.key
                    val matchesName = entry.name.equals(article.photo.key, ignoreCase = true)
                    matchesAsset || matchesName
                } ?: RfPhotoKey.MESA_REDONDA
                SearchResultCard80(
                    photoKey = photoKey,
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
                        onSelectScreenCode("s25")
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
    val chipBg = if (isActive) Color(0xFFFDE6F2) else Color(0xFFF1EFF5)
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
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RfPhoto(
            photoKey = photoKey,
            contentDescription = title,
            modifier = Modifier.size(88.dp),
            cornerRadius = 14.dp,
        )
        Column(modifier = Modifier.weight(1f)) {
            BasicText(
                text = title,
                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1A1A2E)),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            BasicText(
                text = subtitle,
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF62627A)),
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
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    RfGradientPriceText(
                        text = price,
                        style = TextStyle(
                            fontFamily = typo.displayFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 19.sp,
                            lineHeight = 21.sp,
                        ),
                    )
                    BasicText(
                        text = "RD$/día",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFFB54BD6)),
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

// ==============================================================================
// s82 · MIS PAGOS
// ==============================================================================
@Composable
internal fun RenderPersonalPaymentsS82(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val cardShape = RoundedCornerShape(20.dp)

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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .rfIconButtonShadow(CircleShape, isAdmin = false)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSelectScreenCode("BACK:s54") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1A1A2E), size = 22.dp)
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
                            color = Color(0xFF1A1A2E),
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    BasicText(
                        text = "Pagos y recibos de tus eventos",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 13.sp,
                            color = Color(0xFF62627A),
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
                        text = "RD$ 3,210",
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
                        text = "RD$ 7,490",
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

            BasicText(
                text = "PRÓXIMOS PAGOS",
                style = TextStyle(
                    fontFamily = typo.bodyFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    letterSpacing = 0.96.sp,
                    color = Color(0xFF62627A),
                ),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(cardShape, isAdmin = false)
                    .clip(cardShape)
                    .background(Color.White),
            ) {
                PendingPaymentRow82(
                    title = "Boda Real · saldo",
                    due = "Vence el jue 22 oct",
                    dueColor = Color(0xFFB42345),
                    amount = "RD$ 3,150",
                    highlighted = true,
                    onClick = { onSelectScreenCode("s50") },
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFFF2F0F5)),
                )
                PendingPaymentRow82(
                    title = "Quinceañera · saldo",
                    due = "Vence el vie 30 oct",
                    dueColor = Color(0xFF7A5700),
                    amount = "RD$ 4,340",
                    highlighted = false,
                    onClick = { onSelectScreenCode("s50") },
                )
            }

            BasicText(
                text = "HISTORIAL",
                style = TextStyle(
                    fontFamily = typo.bodyFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    letterSpacing = 0.96.sp,
                    color = Color(0xFF62627A),
                ),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(cardShape, isAdmin = false)
                    .clip(cardShape)
                    .background(Color.White),
            ) {
                ReceiptRow82(
                    title = "Anticipo · Boda Real",
                    subtitle = "18 oct · Transferencia Banco Popular",
                    amount = "RD$ 1,350",
                    onClick = { onSelectScreenCode("s83") },
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFFF2F0F5)),
                )
                ReceiptRow82(
                    title = "Anticipo · Quinceañera",
                    subtitle = "15 oct · Efectivo",
                    amount = "RD$ 1,860",
                    onClick = { onSelectScreenCode("s83") },
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(cardShape, isAdmin = false)
                    .clip(cardShape)
                    .background(Color.White)
                    .clickable { onSelectScreenCode("s50") }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RfIcon(kind = RfIconKind.WALLET, tint = Color(0xFF3B3550), size = 18.dp)
                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = "Métodos de pago",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = Color(0xFF1A1A2E)),
                    )
                    BasicText(
                        text = "Visa •••• 4821 · Transferencia",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF62627A)),
                    )
                }
                RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = Color(0xFF62627A), size = 16.dp)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PendingPaymentRow82(
    title: String,
    due: String,
    dueColor: Color,
    amount: String,
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
                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Medium, fontSize = 14.5.sp, color = Color(0xFF1A1A2E)),
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
                color = Color(0xFF1A1A2E),
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
                        Modifier.background(Color(0xFFF1EFF5))
                    },
                )
                .padding(horizontal = 12.dp),
        ) {
            BasicText(
                text = "Pagar",
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
                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Medium, fontSize = 14.5.sp, color = Color(0xFF1A1A2E)),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            BasicText(
                text = subtitle,
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF62627A)),
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
                    color = Color(0xFF1A1A2E),
                ),
            )
            BasicText(
                text = "Recibo",
                style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFFC2338F)),
            )
        }
    }
}

// ==============================================================================

// s86 · AJUSTES DE CUENTA
// ==============================================================================
@Composable
internal fun RenderAccountSettingsS86(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

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
                        .background(Color.White)
                        .clickable { onSelectScreenCode("BACK:s54") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1A1A2E), size = 22.dp)
                }
                BasicText(
                    "Ajustes",
                    modifier = Modifier.weight(1f),
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, lineHeight = 22.sp, color = Color(0xFF1A1A2E)),
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            SettingsCaption86("CUENTA")
            Spacer(modifier = Modifier.height(8.dp))
            SettingsGroup86 {
                SettingsRow86("Datos personales", RfIconKind.USER, onClick = { onSelectScreenCode("s54") })
                SettingsDivider86()
                SettingsRow86("Cambiar contraseña", RfIconKind.LOCK, onClick = { onSelectScreenCode("s54") })
                SettingsDivider86()
                SettingsRow86("Métodos de pago", RfIconKind.RECEIPT, onClick = { onSelectScreenCode("s82") }) {
                    BasicText("Visa •••• 4821", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF62627A)))
                }
                SettingsDivider86()
                SettingsRow86("Direcciones de mis eventos", RfIconKind.LOCATION) {
                    BasicText("2", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF62627A)))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            SettingsCaption86("AVISOS")
            Spacer(modifier = Modifier.height(8.dp))
            SettingsGroup86 {
                SettingsRow86("Notificaciones push", RfIconKind.BELL) { SettingsSwitch86(checked = true) }
                SettingsDivider86()
                SettingsRow86("Mensajes por WhatsApp", RfIconKind.WHATSAPP) { SettingsSwitch86(checked = true) }
                SettingsDivider86()
                SettingsRow86("Correos de ofertas", RfIconKind.MAIL) { SettingsSwitch86(checked = false) }
            }

            Spacer(modifier = Modifier.height(14.dp))
            SettingsCaption86("PRIVACIDAD Y LEGAL")
            Spacer(modifier = Modifier.height(8.dp))
            SettingsGroup86 {
                SettingsRow86("Términos y condiciones", RfIconKind.NOTE, onClick = { onSelectScreenCode("s87") })
                SettingsDivider86()
                SettingsRow86("Política de privacidad", RfIconKind.SHIELD, onClick = { onSelectScreenCode("s87") })
                SettingsDivider86()
                var permissionsGranted by remember { mutableStateOf(true) }
                Row(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp).clickable { permissionsGranted = !permissionsGranted }.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    RfIcon(kind = RfIconKind.CAMERA, tint = Color(0xFF3B3550), size = 20.dp)
                    Column(modifier = Modifier.weight(1f)) {
                        BasicText("Permisos de la app", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF1A1A2E)))
                        BasicText(if (permissionsGranted) "Cámara y micrófono" else "Solo micrófono", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF62627A)))
                    }
                    RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = Color(0xFF62627A), size = 16.dp)
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
                        "Eliminar cuenta",
                        modifier = Modifier.weight(1f),
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color(0xFFB42345)),
                    )
                    RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = Color(0xFFB42345), size = 16.dp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            BasicText(
                "Rosa Fiesta · versión 2.4.0",
                modifier = Modifier.fillMaxWidth(),
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, textAlign = TextAlign.Center, color = Color(0xFF62627A)),
            )
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun SettingsCaption86(label: String) {
    val typo = LocalRfTypography.current

    BasicText(
        label,
        modifier = Modifier.padding(horizontal = 20.dp),
        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp, color = Color(0xFF62627A)),
    )
}

@Composable
private fun SettingsGroup86(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFF2EDF5), RoundedCornerShape(20.dp)),
        content = content,
    )
}

@Composable
private fun SettingsDivider86() {
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF2F0F5)))
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
            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF1A1A2E)),
        )
        trailing()
        RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = Color(0xFF62627A), size = 16.dp)
    }
}

@Composable
private fun SettingsSwitch86(checked: Boolean) {
    var isSwitchChecked by remember { mutableStateOf(checked) }
    Box(
        modifier = Modifier
            .width(46.dp)
            .height(28.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(
                if (isSwitchChecked) {
                    Brush.linearGradient(listOf(Color(0xFFEA4EA9), Color(0xFF865DEC)))
                } else {
                    Brush.linearGradient(listOf(Color(0xFFE3DCE8), Color(0xFFE3DCE8)))
                },
            )
            .clickable { isSwitchChecked = !isSwitchChecked },
        contentAlignment = if (isSwitchChecked) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Box(modifier = Modifier.padding(horizontal = 3.dp).size(22.dp).clip(CircleShape).background(Color.White).shadow(1.dp, CircleShape))
    }
}

// ==============================================================================
// s87 · TÉRMINOS Y PRIVACIDAD
// ==============================================================================
@Composable
internal fun RenderTermsS87(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val cardShape = RoundedCornerShape(20.dp)
    var selectedSegmentIndex by remember { mutableStateOf(0) }

    RfRadialBackgroundBox(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 108.dp),
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
                            .rfIconButtonShadow(CircleShape, isAdmin = false)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable { onSelectScreenCode("BACK:s86") },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1A1A2E), size = 22.dp)
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
                            color = Color(0xFF1A1A2E),
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFF1EFF5))
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
                    text = "Última actualización: 1 de octubre de 2026",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF62627A)),
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(cardShape, isAdmin = false)
                        .clip(cardShape)
                        .background(Color.White)
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    TermsSection87(
                        title = "1. Reservas y cotizaciones",
                        body = "Tu fecha queda apartada cuando Rosa Fiesta confirma la cotización y recibe el anticipo. Las cotizaciones tienen validez de 7 días.",
                    )
                    TermsSection87(
                        title = "2. Pagos y anticipos",
                        body = "El anticipo es del 30% del total. El saldo se paga a más tardar un día antes del evento.",
                    )
                    TermsSection87(
                        title = "3. Cambios y cancelaciones",
                        body = "El primer cambio de fecha es gratis, sujeto a disponibilidad. Las cancelaciones se reembolsan según los días que falten para el evento.",
                    )
                    TermsSection87(
                        title = "4. Cuidado de los artículos",
                        body = "Los artículos dañados o perdidos se cobran según la lista de precios vigente, previa revisión con fotos.",
                    )
                }

                BasicText(
                    text = "Texto de ejemplo para el diseño. El texto legal final lo redacta el negocio.",
                    modifier = Modifier.fillMaxWidth(),
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = Color(0xFF62627A),
                        textAlign = TextAlign.Center,
                    ),
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 36.dp),
            ) {
                RfButton(
                    label = "Entendido",
                    onClick = { onSelectScreenCode("s86") },
                    modifier = Modifier.fillMaxWidth(),
                    style = RfButtonStyle.PRIMARY,
                )
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
                        .rfCardShadow(RoundedCornerShape(12.dp), isAdmin = false)
                        .background(Color.White)
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
                color = Color(0xFF1A1A2E),
            ),
        )
        BasicText(
            text = body,
            style = TextStyle(
                fontFamily = typo.bodyFamily,
                fontSize = 14.sp,
                lineHeight = 22.sp,
                color = Color(0xFF62627A),
            ),
        )
    }
}

// ==============================================================================
// s88 · ELIMINAR CUENTA
// ==============================================================================
@Composable
internal fun RenderDeleteAccountS88(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    RfRadialBackgroundBox(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 140.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Header
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
                        .background(Color.White)
                        .clickable { onSelectScreenCode("BACK:s54") },
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1A1A2E), size = 22.dp)
                }

                BasicText(
                    text = "Eliminar cuenta",
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF1A1A2E)),
                    modifier = Modifier.weight(1f),
                )
            }

            // Warning Icon + Title section
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
                    style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Color(0xFF1A1A2E)),
                )

                BasicText(
                    text = "Esta acción es permanente. Si eliminas tu cuenta:",
                    modifier = Modifier.widthIn(max = 310.dp),
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontSize = 14.5.sp,
                        lineHeight = 21.75.sp,
                        color = Color(0xFF62627A),
                        textAlign = TextAlign.Center,
                    ),
                )
            }

            // Consequences Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = false)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFF2EDF5), RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
                        RfIcon(kind = RfIconKind.CLOSE, tint = Color(0xFFB42345), size = 16.dp)
                        BasicText("Perderás tus eventos, cotizaciones y bocetos.", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.5.sp, lineHeight = 20.sp, color = Color(0xFF1A1A2E)))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
                        RfIcon(kind = RfIconKind.CLOSE, tint = Color(0xFFB42345), size = 16.dp)
                        BasicText("Se borrarán tus favoritos y conversaciones con Rosa.", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.5.sp, lineHeight = 20.sp, color = Color(0xFF1A1A2E)))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
                        RfIcon(kind = RfIconKind.CLOSE, tint = Color(0xFFB42345), size = 16.dp)
                        BasicText("Tus recibos se conservan 5 años por ley y te los enviamos por correo.", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.5.sp, lineHeight = 20.sp, color = Color(0xFF1A1A2E)))
                    }
                }
            }

            // Active events warning note
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
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("2 eventos activos") }
                            append(". Cancélalos o espera a que terminen para poder eliminar tu cuenta.")
                        },
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.5.sp, lineHeight = 18.9.sp, color = Color(0xFF5E4300)),
                    )
                }
            }

            // Confirmation field
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BasicText("Escribe ELIMINAR para confirmar", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF3B3550)))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFF0EBF4), RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    BasicText("ELIMINAR", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF62627A)))
                }
            }
        }

        // Bottom action buttons
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 20.dp, end = 20.dp, bottom = 30.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = false)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF1D5DB)),
                contentAlignment = Alignment.Center,
            ) {
                BasicText("Eliminar cuenta", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clickable { onSelectScreenCode("s54") },
                contentAlignment = Alignment.Center,
            ) {
                BasicText("Mantener mi cuenta", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF62627A)))
            }
        }
    }
}

// ==============================================================================
// s89 · ESTADO VACÍO PERSONAL
// ==============================================================================
@Composable
internal fun RenderPersonalEmptyStateS89(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

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
                    color = Color(0xFF1A1A2E),
                ),
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EmptyStateChip89(label = "Próximos", selected = true, onClick = {})
                EmptyStateChip89(label = "Pasados", selected = false, onClick = {})
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                BalloonsIllustration89(modifier = Modifier.size(190.dp))

                BasicText(
                    text = "Aún no tienes eventos",
                    style = TextStyle(
                        fontFamily = typo.displayFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        color = Color(0xFF1A1A2E),
                    ),
                )
                BasicText(
                    text = "Cuéntale a Rosa qué celebras y te armamos la lista en 2 minutos.",
                    modifier = Modifier.widthIn(max = 290.dp),
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontSize = 15.sp,
                        lineHeight = 22.5.sp,
                        color = Color(0xFF62627A),
                        textAlign = TextAlign.Center,
                    ),
                )

                Spacer(modifier = Modifier.height(12.dp))

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
                            .background(Color.White)
                            .border(1.dp, Color(0xFFEEE9F2), RoundedCornerShape(16.dp))
                            .clickable { onSelectScreenCode("s18") },
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = "Explorar el catálogo",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E), fontSize = 16.sp),
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
                    SolidColor(Color(0xFFF1EFF5))
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
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val skGradient = rememberRfSkeletonBrush(isAdmin = false, animate = true)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFC)),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(Color(0xFFFAFAFC))
            drawRect(
                Brush.radialGradient(
                    listOf(Color(0xFFF9E3F1), Color(0x00F9E3F1)),
                    center = Offset(0f, 0f),
                    radius = size.width * 1.1f,
                ),
            )
            drawRect(
                Brush.radialGradient(
                    listOf(Color(0xFFEEE7FB), Color(0x00EEE7FB)),
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
            // Header: "Catálogo" (26px, 800) + 2 circular buttons (44x44)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                BasicText(
                    text = "Catálogo",
                    style = TextStyle(
                        fontFamily = typo.displayFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 26.sp,
                        lineHeight = 28.sp,
                        color = Color(0xFF1A1A2E),
                    ),
                    modifier = Modifier.weight(1f),
                )
                Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(skGradient))
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
            .background(Color.White)
            .border(1.dp, Color(0xFFF2EDF5), RoundedCornerShape(20.dp)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(128.dp)
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
// s75 · MODAL INICIA SESIÓN (MODAL PARA INVITADOS)
// ==============================================================================
@Composable
internal fun RenderGuestAuthModalS75(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFC)),
    ) {
        // Background guest catalog (s01)
        RenderGuestCatalogS01(onSelectScreenCode = onSelectScreenCode)

        // Scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x731E1432))
                .clickable { onSelectScreenCode("s01") },
        )

        // Bottom sheet dialog
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Color.White)
                .padding(top = 12.dp, start = 24.dp, end = 24.dp, bottom = 34.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Grab handle
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFFE3DCE8)),
                )
                Spacer(Modifier.height(2.dp))

                // Candado illustration 110x110
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
                        color = Color(0xFF1A1A2E),
                        textAlign = TextAlign.Center,
                    ),
                )

                // Subtitle
                BasicText(
                    text = "Entra o crea tu cuenta para guardar favoritos y armar tu evento.",
                    style = TextStyle(
                        fontFamily = typo.bodyFamily,
                        fontSize = 15.sp,
                        color = Color(0xFF62627A),
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
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE8E1EE), RoundedCornerShape(16.dp))
                        .clickable { onSelectScreenCode("s08") },
                ) {
                    BasicText(
                        "Crear cuenta",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF1A1A2E),
                        ),
                    )
                }

                // Ahora no
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clickable { onSelectScreenCode("s01") },
                ) {
                    BasicText(
                        "Ahora no",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = Color(0xFF62627A),
                        ),
                    )
                }
            }
        }
    }
}

// ==============================================================================
// s76 · MODAL CÓMO PLANIFICAR
// ==============================================================================
@Composable
internal fun RenderPlanningModalS76(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFC)),
    ) {
        RenderPersonalHomeScreen(
            spec = ScreenCatalog.findByCode("s14"),
            viewModel = viewModel,
            onSelectScreenCode = onSelectScreenCode,
        )

        // Scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x731E1432))
                .clickable { onSelectScreenCode("s14") },
        )

        // Bottom sheet dialog
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Color.White)
                .padding(top = 12.dp, start = 20.dp, end = 20.dp, bottom = 34.dp),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Grab handle
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(40.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFFE3DCE8)),
                )

                // Title header
                Column(modifier = Modifier.padding(horizontal = 4.dp)) {
                    BasicText(
                        "¿Cómo quieres planificar?",
                        style = TextStyle(
                            fontFamily = typo.displayFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            color = Color(0xFF1A1A2E),
                        ),
                    )
                    Spacer(Modifier.height(4.dp))
                    BasicText(
                        "Elige cómo prefieres hablar con nosotros.",
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 15.sp,
                            color = Color(0xFF62627A),
                        ),
                    )
                }

                // Opción 1: WhatsApp con María
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 84.dp)
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = false)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFEEE9F2), RoundedCornerShape(20.dp))
                        .clickable { onSelectScreenCode("s31") }
                        .padding(14.dp),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFEEF6D6)),
                    ) {
                        RfIcon(kind = RfIconKind.WHATSAPP, tint = Color(0xFF4E6A10), size = 26.dp)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        BasicText(
                            "WhatsApp con María",
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF1A1A2E),
                            ),
                        )
                        Spacer(Modifier.height(2.dp))
                        BasicText(
                            "Habla directo con la dueña",
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = 14.sp,
                                color = Color(0xFF62627A),
                            ),
                        )
                    }
                    RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = Color(0xFF62627A), size = 20.dp)
                }

                // Opción 2: Chat con Rosa (Seleccionado)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 84.dp)
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
                            .size(52.dp)
                            .clip(CircleShape),
                    ) {
                        RosaAvatar(size = RosaAvatarSize.MEDIUM)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            BasicText(
                                "Chat con Rosa",
                                style = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF1A1A2E),
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
                                fontSize = 14.sp,
                                color = Color(0xFF62627A),
                            ),
                        )
                    }
                    RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = Color(0xFF7A4FD8), size = 20.dp)
                }

                // Opción 3: Voz con Rosa
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 84.dp)
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = false)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFEEE9F2), RoundedCornerShape(20.dp))
                        .clickable {
                            viewModel?.startVoiceInput(VoiceInputOrigin.ROSA_WIZARD)
                            onSelectScreenCode("s35")
                        }
                        .padding(14.dp),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFFFF2C9)),
                    ) {
                        Canvas(modifier = Modifier.size(26.dp)) {
                            val scale = size.width / 24f
                            val strokeW = 1.8f * scale
                            val waveColor = Color(0xFF7A5700)
                            fun vLine(x: Float, y1: Float, y2: Float) {
                                drawLine(
                                    color = waveColor,
                                    start = Offset(x * scale, y1 * scale),
                                    end = Offset(x * scale, y2 * scale),
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
                                    color = Color(0xFF1A1A2E),
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
                                fontSize = 14.sp,
                                color = Color(0xFF62627A),
                            ),
                        )
                    }
                    RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = Color(0xFF62627A), size = 20.dp)
                }
            }
        }
    }
}

// ==============================================================================
// s77 · MODAL RESEÑAS DEL EVENTO
// ==============================================================================
@Composable
internal fun RenderEventReviewsModalS77(
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFC)),
    ) {
        // Background my events (s37)
        RenderMyEventsScreenS37(onSelectScreenCode = onSelectScreenCode)

        // Scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x731E1432))
                .clickable { onSelectScreenCode("s37") },
        )

        // Bottom sheet dialog (height 540dp, bg #FAFAFC)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(540.dp)
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Color(0xFFFAFAFC))
                .padding(top = 12.dp, start = 20.dp, end = 20.dp, bottom = 34.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Grab handle
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(40.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFFE3DCE8)),
                )

                // Header row
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
                                color = Color(0xFF1A1A2E),
                            ),
                        )
                        Spacer(Modifier.height(2.dp))
                        BasicText(
                            "Gala Corporativa Grupo Caribe",
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = 14.sp,
                                color = Color(0xFF62627A),
                            ),
                        )
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFEEE9F2), RoundedCornerShape(14.dp))
                            .clickable { onSelectScreenCode("s37") },
                    ) {
                        RfIcon(kind = RfIconKind.CLOSE, tint = Color(0xFF1A1A2E), size = 20.dp)
                    }
                }

                // Review card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = false)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFF2EDF5), RoundedCornerShape(20.dp))
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
                                    "A",
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
                                    "Anónimo",
                                    style = TextStyle(
                                        fontFamily = typo.bodyFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF1A1A2E),
                                    ),
                                )
                                Spacer(Modifier.height(2.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    repeat(5) {
                                        RfIcon(kind = RfIconKind.STAR, tint = Color(0xFFF7B500), size = 14.dp)
                                    }
                                }
                            }
                            BasicText(
                                "19 oct",
                                style = TextStyle(
                                    fontFamily = typo.bodyFamily,
                                    fontSize = 13.sp,
                                    color = Color(0xFF62627A),
                                ),
                            )
                        }
                        BasicText(
                            "Excelente coordinación en nuestra Gala Corporativa. Todo el mobiliario llegó impecable.",
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontSize = 15.sp,
                                color = Color(0xFF1A1A2E),
                                lineHeight = 22.sp,
                            ),
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                // CTA button: Escribir reseña
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = false)
                        .clip(RoundedCornerShape(16.dp))
                        .background(RosaButtonGradient)
                        .clickable { onSelectScreenCode("s37") },
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        RfIcon(kind = RfIconKind.EDIT, tint = Color.White, size = 20.dp)
                        BasicText(
                            "Escribir reseña",
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White,
                            ),
                        )
                    }
                }
            }
        }
    }
}

// ==============================================================================
@Composable
internal fun RenderCatalogFiltersModalS81(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val vmCategory = viewModel?.selectedCategory?.collectAsState()?.value ?: "Todas"
    val vmAvailableOnly = viewModel?.onlyAvailableFilter?.collectAsState()?.value ?: true
    val vmSortLabel = viewModel?.searchSortOrder?.collectAsState()?.value ?: "Populares"
    val vmMaxPrice = viewModel?.maxPriceFilterDop?.collectAsState()?.value ?: 5000
    var selectedCategory by remember(vmCategory) { mutableStateOf(vmCategory) }
    var availableOnly by remember(vmAvailableOnly) { mutableStateOf(vmAvailableOnly) }
    var selectedSortOption by remember(vmSortLabel) { mutableStateOf(vmSortLabel) }
    var selectedMaxPrice by remember(vmMaxPrice) { mutableStateOf(vmMaxPrice) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFC)),
    ) {
        RenderPersonalSearchS80(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x731E1432))
                .clickable { onSelectScreenCode("s80") },
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Color.White)
                .padding(top = 12.dp, start = 20.dp, end = 20.dp, bottom = 34.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
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
                        style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF1A1A2E)),
                    )
                    BasicText(
                        "Limpiar",
                        modifier = Modifier.clickable {
                            selectedCategory = "Todas"
                            availableOnly = true
                            selectedSortOption = "Populares"
                            selectedMaxPrice = 5000
                            viewModel?.setSelectedCategory("Todas")
                            viewModel?.setOnlyAvailableFilter(true)
                            viewModel?.setSearchSortOrder("Populares")
                            viewModel?.setMaxPriceFilterDop(5000)
                        },
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFC2338F)),
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BasicText("Categoría", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF3B3550)))
                    val rows = listOf(
                        listOf("Todas", "Mobiliario", "Floral"),
                        listOf("Iluminación", "Globos", "Mantelería"),
                    )
                    rows.forEach { rowItems ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowItems.forEach { categoryLabel ->
                                val isSelected = categoryLabel == selectedCategory
                                Box(
                                    modifier = Modifier
                                        .height(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) {
                                                Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9)))
                                            } else {
                                                SolidColor(Color(0xFFF1EFF5))
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
                                            fontSize = 14.sp,
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
                    ) {
                        BasicText("Precio por día", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF3B3550)))
                        val priceRangeLabel = if (selectedMaxPrice < 5000) "RD$ 0 – RD$ $selectedMaxPrice" else "Sin límite"
                        BasicText(
                            priceRangeLabel,
                            style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1A1A2E)),
                            modifier = Modifier.clickable {
                                val nextCap = when (selectedMaxPrice) {
                                    5000 -> 1500
                                    1500 -> 500
                                    else -> 5000
                                }
                                selectedMaxPrice = nextCap
                                viewModel?.setMaxPriceFilterDop(nextCap)
                            },
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .clickable {
                                val nextCap = when (selectedMaxPrice) {
                                    5000 -> 1500
                                    1500 -> 500
                                    else -> 5000
                                }
                                selectedMaxPrice = nextCap
                                viewModel?.setMaxPriceFilterDop(nextCap)
                            },
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFFF1EFF5)),
                        )
                        val trackFraction = when (selectedMaxPrice) {
                            500 -> 0.35f
                            1500 -> 0.65f
                            else -> 1.0f
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(trackFraction)
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(RosaButtonGradient),
                        )
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .shadow(4.dp, CircleShape, spotColor = Color(0x80C846B4))
                                .border(2.dp, Color(0xFFEA4EA9), CircleShape)
                                .clip(CircleShape)
                                .background(Color.White),
                        )
                        val thumbOffsetDp = when (selectedMaxPrice) {
                            500 -> 110.dp
                            1500 -> 191.dp
                            else -> 280.dp
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .offset(x = thumbOffsetDp)
                                .size(24.dp)
                                .shadow(4.dp, CircleShape, spotColor = Color(0x80865DEC))
                                .border(2.dp, Color(0xFF865DEC), CircleShape)
                                .clip(CircleShape)
                                .background(Color.White),
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BasicText("Disponible para", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF3B3550)))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .shadow(2.dp, RoundedCornerShape(16.dp), spotColor = Color(0x20B450B4))
                                .border(1.dp, Color(0xFFF0EBF4), RoundedCornerShape(16.dp))
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White)
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                RfIcon(kind = RfIconKind.CALENDAR, tint = Color(0xFF1A1A2E), size = 18.dp)
                                BasicText("Vie 23 oct", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF1A1A2E)))
                            }
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .shadow(2.dp, RoundedCornerShape(16.dp), spotColor = Color(0x20B450B4))
                                .border(1.dp, Color(0xFFF0EBF4), RoundedCornerShape(16.dp))
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White)
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                RfIcon(kind = RfIconKind.PARTY, tint = Color(0xFF1A1A2E), size = 18.dp)
                                BasicText("Mi boda", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF1A1A2E)))
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        BasicText("Solo artículos disponibles", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF1A1A2E)))
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
                                    .background(Color.White),
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BasicText("Ordenar por", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF3B3550)))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFF1EFF5))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        listOf("Populares", "Precio ↑", "Precio ↓", "Nuevos").forEach { sortOption ->
                            val isSel = sortOption == selectedSortOption
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .then(if (isSel) Modifier.shadow(4.dp, RoundedCornerShape(12.dp), spotColor = Color(0x33B450B4), ambientColor = Color.Transparent) else Modifier)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSel) Color.White else Color.Transparent)
                                    .clickable {
                                        selectedSortOption = sortOption
                                        viewModel?.setSearchSortOrder(sortOption)
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                BasicText(
                                    sortOption,
                                    style = TextStyle(
                                        fontFamily = typo.bodyFamily,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 13.sp,
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
                        .clickable { onSelectScreenCode("s80") },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        "Aplicar filtros",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White),
                    )
                }
            }
        }
    }
}

// ==============================================================================
// s84 · CAMBIAR FECHA DEL EVENTO
// ==============================================================================
@Composable
internal fun RenderChangeDateModalS84(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    var selectedDay by remember { mutableStateOf("30") }
    val eventsState = viewModel?.events?.collectAsState()?.value ?: com.jeudry.rosafiesta.domain.CanonicalSampleData.events
    val selectedEventId = viewModel?.selectedEventId?.collectAsState()?.value
    val activeEvent = eventsState.firstOrNull { event -> event.id == selectedEventId } ?: eventsState.firstOrNull()
    val eventTitle = activeEvent?.title ?: "Boda Real Rosa & Dorado"

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
                .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 130.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Header
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
                        .background(Color.White)
                        .clickable { onSelectScreenCode("BACK:s40") },
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1A1A2E), size = 22.dp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    BasicText("Cambiar o cancelar", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF1A1A2E)))
                    BasicText(eventTitle, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF62627A)))
                }
            }

            // Segmented tabs: Cambiar fecha / Cancelar evento
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF1EFF5))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .shadow(4.dp, RoundedCornerShape(12.dp), spotColor = Color(0x33B450B4))
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText("Cambiar fecha", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1A1A2E)))
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelectScreenCode("s85") },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText("Cancelar evento", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color(0xFF62627A)))
                }
            }

            // Calendar Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = false)
                    .border(1.dp, Color(0xFFF2EDF5), RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .padding(14.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Month header
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
                                .background(Color(0xFFF1EFF5)),
                        ) {
                            RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1A1A2E), size = 16.dp)
                        }
                        BasicText("Octubre 2026", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF1A1A2E)))
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF1EFF5)),
                        ) {
                            RfIcon(kind = RfIconKind.ARROW_RIGHT, tint = Color(0xFF1A1A2E), size = 16.dp)
                        }
                    }

                    // Weekday headers
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        listOf("L", "M", "X", "J", "V", "S", "D").forEach { weekdayLabel ->
                            BasicText(weekdayLabel, style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF62627A)))
                        }
                    }

                    // Days grid
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        val weeks = listOf(
                            listOf("", "", "", "1", "2", "3", "4"),
                            listOf("5", "6", "7", "8", "9", "10", "11"),
                            listOf("12", "13", "14", "15", "16", "17", "18"),
                            listOf("19", "20", "21", "22", "23", "24", "25"),
                            listOf("26", "27", "28", "29", "30", "31", ""),
                        )
                        weeks.forEach { week ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                                week.forEach { day ->
                                    val parsedDay = day.toIntOrNull() ?: 99
                                    val isPast = parsedDay < 20
                                    val isCurrent = day == "23"
                                    val isSelected = day == selectedDay
                                    val hasDot = setOf("24", "31").contains(day)
                                    val isSelectable = day.isNotEmpty()

                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .then(
                                                if (isSelected) {
                                                    Modifier
                                                        .shadow(8.dp, RoundedCornerShape(12.dp), spotColor = Color(0xB3865DEC), ambientColor = Color.Transparent)
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF945AE1), Color(0xFF6664E9))))
                                                } else if (isCurrent) {
                                                    Modifier.clip(RoundedCornerShape(12.dp)).border(1.5.dp, Color(0xFFE3DCE8), RoundedCornerShape(12.dp))
                                                } else {
                                                    Modifier.clip(RoundedCornerShape(12.dp))
                                                },
                                            )
                                            .then(
                                                if (isSelectable) Modifier.clickable { selectedDay = day } else Modifier,
                                            ),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        if (isSelectable) {
                                            val isBoldDay = setOf(selectedDay, "20").contains(day)
                                            val isMutedDay = if (isPast) true else isCurrent
                                            BasicText(
                                                day,
                                                style = TextStyle(
                                                    fontFamily = typo.bodyFamily,
                                                    fontWeight = if (isBoldDay) FontWeight.ExtraBold else FontWeight.SemiBold,
                                                    fontSize = 14.sp,
                                                    color = if (isSelected) {
                                                        Color.White
                                                    } else if (day == "20") {
                                                        Color(0xFFC2338F)
                                                    } else if (isMutedDay) {
                                                        Color(0xFFC9C3D1)
                                                    } else {
                                                        Color(0xFF1A1A2E)
                                                    },
                                                ),
                                            )
                                            if (hasDot) {
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

                    // Legend
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.padding(top = 6.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).border(1.5.dp, Color(0xFFE3DCE8), RoundedCornerShape(3.dp)))
                            BasicText("Fecha actual", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF62627A)))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFFF7B500)))
                            BasicText("Poca disponibilidad", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF62627A)))
                        }
                    }
                }
            }

            // Input fields row
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .shadow(2.dp, RoundedCornerShape(16.dp), spotColor = Color(0x20B450B4))
                        .border(1.dp, Color(0xFFF0EBF4), RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        RfIcon(kind = RfIconKind.CALENDAR, tint = Color(0xFF1A1A2E), size = 18.dp)
                        BasicText("Vie $selectedDay oct", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1A1A2E)))
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .shadow(2.dp, RoundedCornerShape(16.dp), spotColor = Color(0x20B450B4))
                        .border(1.dp, Color(0xFFF0EBF4), RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        RfIcon(kind = RfIconKind.ORDERS, tint = Color(0xFF1A1A2E), size = 18.dp)
                        BasicText("2:00 p. m.", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1A1A2E)))
                    }
                }
            }

            // Green banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFEEF6D6))
                    .padding(12.dp, 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top,
            ) {
                RfIcon(kind = RfIconKind.CHECK, tint = Color(0xFF3F560C), size = 18.dp)
                BasicText(
                    "Tus 145 artículos están disponibles el vie 30 oct. Tu primer cambio de fecha es gratis.",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.5.sp, lineHeight = 19.sp, color = Color(0xFF3F560C)),
                )
            }
        }

        // Bottom action button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(start = 20.dp, end = 20.dp, bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BasicText(
                "Rosa Fiesta confirmará el cambio en menos de 24 h.",
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.5.sp, color = Color(0xFF62627A)),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = false)
                    .clip(RoundedCornerShape(16.dp))
                    .background(RosaButtonGradient)
                    .clickable {
                        viewModel?.setDateChangeInput("Vie $selectedDay oct")
                        viewModel?.submitDateChange()
                        onSelectScreenCode("s40")
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

// ==============================================================================
// s85 · CANCELAR EVENTO
// ==============================================================================
@Composable
internal fun RenderCancelEventModalS85(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current
    val eventsState = viewModel?.events?.collectAsState()?.value ?: com.jeudry.rosafiesta.domain.CanonicalSampleData.events
    val selectedEventId = viewModel?.selectedEventId?.collectAsState()?.value
    val activeEvent = eventsState.firstOrNull { event -> event.id == selectedEventId } ?: eventsState.firstOrNull()
    val eventHeaderSubtitle = if (activeEvent != null) {
        "${activeEvent.title} · ${activeEvent.dateLabel}"
    } else {
        "Quinceañera Valentina Crystal · Lun 2 nov"
    }
    val paidDepositDop = activeEvent?.paidDepositDop ?: 1860
    val feeDop = (paidDepositDop * 10) / 100
    val refundDop = (paidDepositDop - feeDop).coerceAtLeast(0)
    var selectedReason by remember { mutableStateOf("Cambio de planes") }

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
                .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 140.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Header
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
                        .background(Color.White)
                        .clickable { onSelectScreenCode("BACK:s40") },
                ) {
                    RfIcon(kind = RfIconKind.BACK, tint = Color(0xFF1A1A2E), size = 22.dp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    BasicText("Cambiar o cancelar", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF1A1A2E)))
                    BasicText(eventHeaderSubtitle, style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF62627A)))
                }
            }

            // Segmented tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF1EFF5))
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
                    BasicText("Cambiar fecha", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color(0xFF62627A)))
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .shadow(4.dp, RoundedCornerShape(12.dp), spotColor = Color(0x33B450B4))
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText("Cancelar evento", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1A1A2E)))
                }
            }

            // Card 1: Política de cancelación
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = false)
                    .border(1.dp, Color(0xFFF2EDF5), RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .padding(14.dp, 16.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row {
                        BasicText("Política de cancelación ", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1A1A2E)))
                        BasicText("· faltan 13 días", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF62627A)))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .shadow(4.dp, CircleShape, spotColor = Color(0x80FDE6F2))
                                .border(4.dp, Color(0xFFFDE6F2), CircleShape)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(Color(0xFFE04FAF), Color(0xFF6664E9)))),
                        )
                        BasicText("Más de 7 días antes", modifier = Modifier.weight(1f), style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1A1A2E)))
                        BasicText("90%", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color(0xFF1A1A2E)))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.size(18.dp).border(2.dp, Color(0xFFE3DCE8), CircleShape))
                        BasicText("Entre 3 y 7 días", modifier = Modifier.weight(1f), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF9A95A6)))
                        BasicText("50%", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color(0xFF9A95A6)))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.size(18.dp).border(2.dp, Color(0xFFE3DCE8), CircleShape))
                        BasicText("Menos de 3 días", modifier = Modifier.weight(1f), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF9A95A6)))
                        BasicText("Sin reembolso", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color(0xFF9A95A6)))
                    }
                }
            }

            // Card 2: Desglose financiero
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = false)
                    .border(1.dp, Color(0xFFF2EDF5), RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 6.dp),
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        BasicText("Anticipo pagado", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF62627A)))
                        BasicText("RD$ $paidDepositDop", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF1A1A2E)))
                    }
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF2F0F5)))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        BasicText("Cargo de gestión (10%)", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF62627A)))
                        BasicText("− RD$ $feeDop", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF1A1A2E)))
                    }
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF2F0F5)))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        BasicText("Te devolvemos", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1A1A2E)))
                        BasicText("RD$ $refundDop", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Color(0xFF4E6A10)))
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                BasicText("¿Por qué cancelas?", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF3B3550)))
                val cancelReasons = listOf("Cambio de planes", "El presupuesto no me alcanza", "Encontré otra opción")
                cancelReasons.forEach { reason ->
                    val isReasonSelected = selectedReason == reason
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                            .clickable { selectedReason = reason },
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
                            reason,
                            style = TextStyle(
                                fontFamily = typo.bodyFamily,
                                fontWeight = if (isReasonSelected) FontWeight.SemiBold else FontWeight.Normal,
                                fontSize = 14.5.sp,
                                color = Color(0xFF1A1A2E),
                            ),
                        )
                    }
                }
            }

            // Alternative banner
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
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.5.sp, color = Color(0xFF1A1A2E)),
                )
                RfIcon(kind = RfIconKind.ARROW_RIGHT, tint = Color(0xFF865DEC), size = 16.dp)
            }
        }

        // Bottom actions
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(start = 20.dp, end = 20.dp, bottom = 30.dp),
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
                        viewModel?.submitCancelEvent()
                        onSelectScreenCode("s37")
                    },
                contentAlignment = Alignment.Center,
            ) {
                BasicText("Cancelar evento", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White))
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clickable { onSelectScreenCode("s40") },
                contentAlignment = Alignment.Center,
            ) {
                BasicText("Mantener mi evento", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, color = Color(0xFF62627A)))
            }
        }
    }
}

// ==============================================================================
// s91 · ESTADO SIN CONEXIÓN PERSONAL
// ==============================================================================
@Composable
internal fun RenderPersonalOfflineStateS91(
    onSelectScreenCode: (String) -> Unit = {},
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

        // Top dark banner
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
            // Icon
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .shadow(12.dp, CircleShape, spotColor = Color(0x80FDE6F2))
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFFFDE6F2), Color(0xFFEFE8FD)))),
                contentAlignment = Alignment.Center,
            ) {
                RfIcon(kind = RfIconKind.WIFI_OFF, tint = Color(0xFFB754CB), size = 52.dp)
            }

            Spacer(Modifier.height(4.dp))

            BasicText("No hay conexión", style = TextStyle(fontFamily = typo.displayFamily, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Color(0xFF1A1A2E)))
            BasicText(
                "Revisa tu internet. Tu lista de Mi evento está guardada y la enviaremos cuando vuelvas a conectarte.",
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, lineHeight = 22.sp, color = Color(0xFF62627A), textAlign = androidx.compose.ui.text.style.TextAlign.Center),
            )

            Spacer(Modifier.height(6.dp))

            // Reintentar button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .rfPrimaryButtonShadow(RoundedCornerShape(16.dp), isAdmin = false)
                    .clip(RoundedCornerShape(16.dp))
                    .background(RosaButtonGradient)
                    .clickable { onSelectScreenCode("s91") },
                contentAlignment = Alignment.Center,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    RfIcon(kind = RfIconKind.REFRESH, tint = Color.White, size = 18.dp)
                    BasicText("Reintentar", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White))
                }
            }

            // Ver mis favoritos guardados button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = Color(0x33B450B4))
                    .border(1.dp, Color(0xFFEEE9F2), RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .clickable { onSelectScreenCode("s24") },
                contentAlignment = Alignment.Center,
            ) {
                BasicText("Ver mis favoritos guardados", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1A1A2E)))
            }

            // Variante: error del servidor
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .shadow(8.dp, RoundedCornerShape(20.dp), spotColor = Color(0x20AA46A0))
                    .border(1.dp, Color(0xFFF2EDF5), RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .padding(12.dp, 14.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFCE3E8)),
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.WARNING, tint = Color(0xFFB42345), size = 20.dp)
                    }
                    Column {
                        BasicText("Variante: error del servidor", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1A1A2E)))
                        BasicText("“Algo salió mal de nuestro lado. Intenta de nuevo en un momento.”", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF62627A)))
                    }
                }
            }
        }
    }
}

// ==============================================================================
// s92 · ESTADO ÉXITO PERSONAL (CON TOAST)
// ==============================================================================
@Composable
internal fun RenderPersonalSuccessStateS92(
    onSelectScreenCode: (String) -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
) {
    val typo = LocalRfTypography.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFC)),
    ) {
        RenderPersonalCatalogS18(viewModel = viewModel, onSelectScreenCode = onSelectScreenCode)

        // Floating Toast overlay above bottom bar
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
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Green checkmark icon
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFB9D84A)),
                    contentAlignment = Alignment.Center,
                ) {
                    RfIcon(kind = RfIconKind.CHECK, tint = Color(0xFF2E3A08), size = 18.dp)
                }

                // Text
                Column(modifier = Modifier.weight(1f)) {
                    BasicText("Agregado a Mi evento", style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White))
                    BasicText("Arco de globos · 17 en total", style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color.White.copy(alpha = 0.75f)))
                }

                // Deshacer button
                Box(
                    modifier = Modifier
                        .clickable { onSelectScreenCode("s18") }
                        .padding(horizontal = 6.dp),
                ) {
                    BasicText(
                        "Deshacer",
                        style = TextStyle(fontFamily = typo.bodyFamily, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color(0xFFFFD45C)),
                    )
                }
            }
        }
    }
}
