package com.jeudry.rosafiesta.ui

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeudry.rosafiesta.designsystem.LocalRfTypography
import com.jeudry.rosafiesta.designsystem.RfColors
import com.jeudry.rosafiesta.designsystem.RfDimens
import com.jeudry.rosafiesta.designsystem.RfIcon
import com.jeudry.rosafiesta.designsystem.RfIconKind
import com.jeudry.rosafiesta.designsystem.RfLogo
import com.jeudry.rosafiesta.designsystem.RfLogoSize
import com.jeudry.rosafiesta.designsystem.rfCardShadow

private enum class AdminMoreCategory(val label: String, val sectionTitle: String) {
    ALL(label = "Todo", sectionTitle = ""),
    COMMERCIAL(label = "Comercial", sectionTitle = "Comercial y clientes"),
    CATALOG(label = "Catálogo", sectionTitle = "Catálogo y portada"),
    FINANCE(label = "Finanzas", sectionTitle = "Finanzas y analítica"),
    OPERATIONS(label = "Operación", sectionTitle = "Operación y sistema"),
}

private data class AdminMoreBadge(
    val text: String,
    val backgroundColor: Color,
    val textColor: Color,
)

private data class AdminMoreShortcut(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: AdminMoreCategory,
    val icon: RfIconKind,
    val iconBg: Color,
    val iconTint: Color,
    val targetScreenCode: String,
    val keywords: String,
    val badge: AdminMoreBadge? = null,
)

private data class AdminMoreLiveMetrics(
    val unreadChats: Int,
    val openQuotes: Int,
    val unverifiedPayments: Int,
    val pendingRefunds: Int,
    val activeLeads: Int,
    val packagesCount: Int,
    val categoriesCount: Int,
    val activeTrendsCount: Int,
)

private val AdminMoreIndigoGradient = Brush.horizontalGradient(
    listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0)),
)

private fun resolveAdminRoleLabel(rawRole: String): String {
    val normalized = rawRole.trim().lowercase()
    return when {
        normalized.contains("decor") -> "Decoración"
        normalized.contains("logist") -> "Logística"
        normalized.contains("ware") -> "Almacén"
        normalized.startsWith("staff") -> "Equipo"
        else -> "Dueña"
    }
}

private fun isOpenQuoteStatus(status: String): Boolean {
    val normalized = status.trim().lowercase()
    return when (normalized) {
        "pending", "sent", "draft", "enviada", "pendiente", "borrador" -> true
        else -> false
    }
}

private fun countBadge(count: Int, suffix: String = ""): AdminMoreBadge? {
    if (count <= 0) {
        return null
    }
    val label = if (suffix.isEmpty()) "$count" else "$count $suffix"
    return AdminMoreBadge(
        text = label,
        backgroundColor = Color(0xFFECEBFF),
        textColor = Color(0xFF4B47D6),
    )
}

private fun alertBadge(count: Int, singularSuffix: String, pluralSuffix: String): AdminMoreBadge? {
    if (count <= 0) {
        return null
    }
    val word = if (count == 1) singularSuffix else pluralSuffix
    return AdminMoreBadge(
        text = "$count $word",
        backgroundColor = Color(0xFFFFF2C9),
        textColor = Color(0xFF7A5700),
    )
}

private fun buildAdminMoreShortcuts(metrics: AdminMoreLiveMetrics): List<AdminMoreShortcut> = listOf(
    AdminMoreShortcut(
        id = "chats",
        title = "Chats",
        subtitle = "Mensajes directos con clientes",
        category = AdminMoreCategory.COMMERCIAL,
        icon = RfIconKind.CHAT,
        iconBg = Color(0xFFDDF4F3),
        iconTint = Color(0xFF1F8A84),
        targetScreenCode = "A14",
        keywords = "mensajes inbox clientes conversar",
        badge = if (metrics.unreadChats > 0) {
            val word = if (metrics.unreadChats == 1) "nuevo" else "nuevos"
            AdminMoreBadge("${metrics.unreadChats} $word", Color(0xFFDDF4F3), Color(0xFF1F8A84))
        } else {
            null
        },
    ),
    AdminMoreShortcut(
        id = "quotes",
        title = "Cotizaciones",
        subtitle = "Propuestas y seguimiento de cierre",
        category = AdminMoreCategory.COMMERCIAL,
        icon = RfIconKind.ORDERS,
        iconBg = Color(0xFFFFF2C9),
        iconTint = Color(0xFF7A5700),
        targetScreenCode = "A22",
        keywords = "presupuestos propuestas firma ventas",
        badge = alertBadge(metrics.openQuotes, "abierta", "abiertas"),
    ),
    AdminMoreShortcut(
        id = "clients",
        title = "Clientes",
        subtitle = "Directorio e historial de eventos",
        category = AdminMoreCategory.COMMERCIAL,
        icon = RfIconKind.USER,
        iconBg = RfColors.VioletaTint,
        iconTint = RfColors.VioletaTintInk,
        targetScreenCode = "A26",
        keywords = "contactos personas historial",
    ),
    AdminMoreShortcut(
        id = "leads",
        title = "Leads",
        subtitle = "Solicitudes y prospectos entrantes",
        category = AdminMoreCategory.COMMERCIAL,
        icon = RfIconKind.LOCATION,
        iconBg = RfColors.VioletaTint,
        iconTint = RfColors.VioletaTintInk,
        targetScreenCode = "s64",
        keywords = "oportunidades prospectos crm",
        badge = countBadge(metrics.activeLeads),
    ),
    AdminMoreShortcut(
        id = "reviews",
        title = "Reseñas",
        subtitle = "Calificaciones y respuestas públicas",
        category = AdminMoreCategory.COMMERCIAL,
        icon = RfIconKind.STAR,
        iconBg = Color(0xFFFFF2C9),
        iconTint = Color(0xFF7A5700),
        targetScreenCode = "s71",
        keywords = "estrellas opiniones feedback",
    ),
    AdminMoreShortcut(
        id = "whatsapp_bot",
        title = "Bot WhatsApp",
        subtitle = "Respuestas rápidas y automatización",
        category = AdminMoreCategory.COMMERCIAL,
        icon = RfIconKind.WHATSAPP,
        iconBg = Color(0xFFEEF6D6),
        iconTint = Color(0xFF4E6A10),
        targetScreenCode = "s72",
        keywords = "mensajeria automatico faq",
    ),
    AdminMoreShortcut(
        id = "packages",
        title = "Paquetes",
        subtitle = "Combos y colecciones armadas",
        category = AdminMoreCategory.CATALOG,
        icon = RfIconKind.GIFT,
        iconBg = Color(0xFFECEBFF),
        iconTint = Color(0xFF4B47D6),
        targetScreenCode = "A24",
        keywords = "combos colecciones ofertas",
        badge = countBadge(metrics.packagesCount),
    ),
    AdminMoreShortcut(
        id = "categories",
        title = "Categorías",
        subtitle = "Iconos SVG y organización visual",
        category = AdminMoreCategory.CATALOG,
        icon = RfIconKind.CATALOG,
        iconBg = Color(0xFFE8EEFF),
        iconTint = Color(0xFF4B47D6),
        targetScreenCode = "A40",
        keywords = "svg iconos rubros inventario",
        badge = countBadge(metrics.categoriesCount),
    ),
    AdminMoreShortcut(
        id = "trends",
        title = "Tendencias y portada",
        subtitle = "Temporada destacada e inspiraciones",
        category = AdminMoreCategory.CATALOG,
        icon = RfIconKind.TREND_UP,
        iconBg = Color(0xFFFCE8F6),
        iconTint = Color(0xFFC2338F),
        targetScreenCode = "A41",
        keywords = "banner temporada portada inicio",
        badge = countBadge(metrics.activeTrendsCount),
    ),
    AdminMoreShortcut(
        id = "payments",
        title = "Pagos",
        subtitle = "Transferencias, abonos y recibos",
        category = AdminMoreCategory.FINANCE,
        icon = RfIconKind.WALLET,
        iconBg = Color(0xFFEEF6D6),
        iconTint = Color(0xFF4E6A10),
        targetScreenCode = "A20",
        keywords = "cobros comprobantes dinero ncf",
        badge = alertBadge(metrics.unverifiedPayments, "por validar", "por validar"),
    ),
    AdminMoreShortcut(
        id = "dashboard",
        title = "Panel",
        subtitle = "Indicadores clave del negocio",
        category = AdminMoreCategory.FINANCE,
        icon = RfIconKind.TREND_UP,
        iconBg = Color(0xFFECEBFF),
        iconTint = Color(0xFF4B47D6),
        targetScreenCode = "s58",
        keywords = "kpi metricas resumen",
    ),
    AdminMoreShortcut(
        id = "reports",
        title = "Reportes",
        subtitle = "Ingresos, exportación e impuestos",
        category = AdminMoreCategory.FINANCE,
        icon = RfIconKind.TREND_UP,
        iconBg = Color(0xFFDDF4F3),
        iconTint = Color(0xFF1F8A84),
        targetScreenCode = "s67",
        keywords = "csv pdf 607 dgii contabilidad",
    ),
    AdminMoreShortcut(
        id = "cancellations",
        title = "Cancelaciones",
        subtitle = "Reembolsos y políticas aplicadas",
        category = AdminMoreCategory.FINANCE,
        icon = RfIconKind.WARNING,
        iconBg = Color(0xFFFCE3E8),
        iconTint = Color(0xFFB42345),
        targetScreenCode = "A32",
        keywords = "devoluciones reembolsos politicas",
        badge = if (metrics.pendingRefunds > 0) {
            AdminMoreBadge("${metrics.pendingRefunds} pend.", Color(0xFFFCE3E8), Color(0xFFB42345))
        } else {
            null
        },
    ),
    AdminMoreShortcut(
        id = "calendar",
        title = "Calendario",
        subtitle = "Agenda mensual y disponibilidad",
        category = AdminMoreCategory.OPERATIONS,
        icon = RfIconKind.CALENDAR,
        iconBg = Color(0xFFDDF4F3),
        iconTint = Color(0xFF1F8A84),
        targetScreenCode = "s63",
        keywords = "fechas agenda reservas",
    ),
    AdminMoreShortcut(
        id = "suppliers",
        title = "Proveedores",
        subtitle = "Aliados externos y suministros",
        category = AdminMoreCategory.OPERATIONS,
        icon = RfIconKind.TRUCK,
        iconBg = Color(0xFFEEF6D6),
        iconTint = Color(0xFF4E6A10),
        targetScreenCode = "s55",
        keywords = "logistica transporte suplidores",
    ),
    AdminMoreShortcut(
        id = "team",
        title = "Equipo",
        subtitle = "Personal, roles y permisos",
        category = AdminMoreCategory.OPERATIONS,
        icon = RfIconKind.USER,
        iconBg = Color(0xFFECEBFF),
        iconTint = Color(0xFF4B47D6),
        targetScreenCode = "A28",
        keywords = "staff colaboradores acceso",
    ),
    AdminMoreShortcut(
        id = "web_desktop",
        title = "Versión web",
        subtitle = "Centro administrativo desktop (1440px)",
        category = AdminMoreCategory.OPERATIONS,
        icon = RfIconKind.GLOBE,
        iconBg = Color(0xFFF2F2F8),
        iconTint = Color(0xFF5F6377),
        targetScreenCode = "WEB_MODAL",
        keywords = "escritorio navegador laptop",
    ),
)

private fun shortcutMatchesFilter(
    item: AdminMoreShortcut,
    selectedCategory: AdminMoreCategory,
    normalizedQuery: String,
): Boolean {
    val isAllSelected = selectedCategory == AdminMoreCategory.ALL
    val matchesCategory = if (isAllSelected) true else item.category == selectedCategory
    if (!matchesCategory) {
        return false
    }
    if (normalizedQuery.isEmpty()) {
        return true
    }
    val inTitle = item.title.contains(normalizedQuery, ignoreCase = true)
    val inSubtitle = item.subtitle.contains(normalizedQuery, ignoreCase = true)
    val inKeywords = item.keywords.contains(normalizedQuery, ignoreCase = true)
    val inCategory = item.category.sectionTitle.contains(normalizedQuery, ignoreCase = true)
    val textMatches = if (inTitle) true else inSubtitle
    val extraMatches = if (inKeywords) true else inCategory
    return if (textMatches) true else extraMatches
}

@Composable
internal fun RenderAdminMoreA12(
    viewModel: RosaFiestaViewModel? = null,
    onSelectScreenCode: (String) -> Unit = {},
    onSwitchAppMode: () -> Unit = {},
) {
    val typo = LocalRfTypography.current
    val rawProfileName = viewModel?.profileName?.collectAsState()?.value ?: "María"
    val rawActiveRole = viewModel?.activeRole?.collectAsState()?.value ?: "admin"
    val rawUserEmail = viewModel?.currentUserEmail?.collectAsState()?.value.orEmpty()
    val chatThreads = viewModel?.chatThreads?.collectAsState()?.value.orEmpty()
    val quotes = viewModel?.quotes?.collectAsState()?.value.orEmpty()
    val payments = viewModel?.payments?.collectAsState()?.value.orEmpty()
    val refunds = viewModel?.refundRequests?.collectAsState()?.value.orEmpty()
    val leads = viewModel?.leads?.collectAsState()?.value.orEmpty()
    val packages = viewModel?.packages?.collectAsState()?.value.orEmpty()
    val categories = viewModel?.catalogCategories?.collectAsState()?.value.orEmpty()
    val trends = viewModel?.catalogTrends?.collectAsState()?.value.orEmpty()

    val liveMetrics = remember(chatThreads, quotes, payments, refunds, leads, packages, categories, trends) {
        AdminMoreLiveMetrics(
            unreadChats = chatThreads.sumOf { thread -> thread.unreadCount },
            openQuotes = quotes.count { quote -> isOpenQuoteStatus(quote.status) },
            unverifiedPayments = payments.count { payment -> !payment.verified },
            pendingRefunds = refunds.count { refund -> refund.status.trim().lowercase() == "pending" },
            activeLeads = leads.size,
            packagesCount = packages.size,
            categoriesCount = categories.size,
            activeTrendsCount = trends.count { trend -> trend.active },
        )
    }

    val adminDisplayName = resolveAdminFirstName(rawProfileName)
    val adminRoleLabel = resolveAdminRoleLabel(rawActiveRole)
    val adminDisplayEmail = rawUserEmail.trim().ifBlank { "maria@rosafiesta.do" }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(AdminMoreCategory.ALL) }
    var showWebDesktopModal by remember { mutableStateOf(false) }

    val allShortcuts = remember(liveMetrics) { buildAdminMoreShortcuts(liveMetrics) }
    val normalizedQuery = searchQuery.trim()
    val groupedSections = remember(allShortcuts, selectedCategory, normalizedQuery) {
        AdminMoreCategory.entries
            .filter { category -> category != AdminMoreCategory.ALL }
            .mapNotNull { category ->
                val items = allShortcuts.filter { item ->
                    val sameCategory = item.category == category
                    if (sameCategory) {
                        shortcutMatchesFilter(item, selectedCategory, normalizedQuery)
                    } else {
                        false
                    }
                }
                if (items.isEmpty()) null else category to items
            }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    top = 16.dp,
                    start = 20.dp,
                    end = 20.dp,
                    bottom = RfDimens.BottomBarContentClearance + 24.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                BasicText(
                    text = "Más",
                    style = TextStyle(fontFamily = typo.displayFamily, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E324A)),
                )
                BasicText(
                    text = "Módulos, operación y ajustes del negocio",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)),
                )
            }

            AdminMoreProfileCard(
                adminDisplayName = adminDisplayName,
                adminRoleLabel = adminRoleLabel,
                adminDisplayEmail = adminDisplayEmail,
                onClick = { onSelectScreenCode("A31") },
            )

            AdminMoreFilterControls(
                searchQuery = searchQuery,
                selectedCategory = selectedCategory,
                onSearchQueryChange = { updated -> searchQuery = updated },
                onSelectCategory = { nextCategory -> selectedCategory = nextCategory },
            )

            if (groupedSections.isEmpty()) {
                AdminMoreEmptySearchCard(
                    query = normalizedQuery,
                    onReset = {
                        searchQuery = ""
                        selectedCategory = AdminMoreCategory.ALL
                    },
                )
            } else {
                groupedSections.forEach { (category, items) ->
                    AdminMoreSectionCard(
                        sectionTitle = category.sectionTitle,
                        items = items,
                        onItemClick = { item ->
                            if (item.targetScreenCode == "WEB_MODAL") {
                                showWebDesktopModal = true
                            } else {
                                onSelectScreenCode(item.targetScreenCode)
                            }
                        },
                    )
                }
            }

            AdminMoreAccountCard(
                onOpenSettings = { onSelectScreenCode("A31") },
                onSwitchToClient = {
                    viewModel?.switchRole("client")
                    onSwitchAppMode()
                },
                onLogout = {
                    viewModel?.logoutSession()
                    onSelectScreenCode("A30")
                },
            )
        }

        AdminMoreWebDesktopSheet(
            visible = showWebDesktopModal,
            onDismiss = { showWebDesktopModal = false },
            onOpenBrowser = {
                showWebDesktopModal = false
                viewModel?.openExternalUrl("https://admin.rosafiesta.do")
            },
            onOpenDesktopPreview = {
                showWebDesktopModal = false
                onSelectScreenCode("A39")
            },
        )
    }
}

@Composable
private fun AdminMoreProfileCard(
    adminDisplayName: String,
    adminRoleLabel: String,
    adminDisplayEmail: String,
    onClick: () -> Unit,
) {
    val typo = LocalRfTypography.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
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
                    .border(1.dp, RfColors.IndigoTint, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                RfLogo(size = RfLogoSize.MEDIUM)
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    BasicText(
                        text = adminDisplayName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E324A)),
                    )
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFFECEBFF))
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    ) {
                        BasicText(
                            text = adminRoleLabel,
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF4B47D6)),
                        )
                    }
                }
                BasicText(
                    text = adminDisplayEmail,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)),
                )
            }

            RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = Color(0xFF5F6377), size = 20.dp)
        }
    }
}

@Composable
private fun AdminMoreFilterControls(
    searchQuery: String,
    selectedCategory: AdminMoreCategory,
    onSearchQueryChange: (String) -> Unit,
    onSelectCategory: (AdminMoreCategory) -> Unit,
) {
    val typo = LocalRfTypography.current
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                RfIcon(kind = RfIconKind.SEARCH, tint = Color(0xFF5F6377), size = 18.dp)
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (searchQuery.isEmpty()) {
                        BasicText(
                            text = "Buscar módulo o herramienta...",
                            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF7B7F94)),
                        )
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        singleLine = true,
                        cursorBrush = SolidColor(Color(0xFF4B47D6)),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        textStyle = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, color = Color(0xFF2E324A)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin-more-search-input"),
                    )
                }
                if (searchQuery.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF2F2F8))
                            .clickable { onSearchQueryChange("") },
                        contentAlignment = Alignment.Center,
                    ) {
                        RfIcon(kind = RfIconKind.CLOSE, tint = Color(0xFF5F6377), size = 14.dp)
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
            AdminMoreCategory.entries.forEach { category ->
                val isSelected = selectedCategory == category
                Box(
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("admin-more-filter-${category.name.lowercase()}")
                        .clip(CircleShape)
                        .then(
                            if (isSelected) {
                                Modifier.background(AdminMoreIndigoGradient)
                            } else {
                                Modifier
                                    .background(Color.White)
                                    .border(1.dp, Color(0xFFE3E4EF), CircleShape)
                            },
                        )
                        .clickable { onSelectCategory(category) }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = category.label,
                        style = TextStyle(
                            fontFamily = typo.bodyFamily,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (isSelected) Color.White else Color(0xFF4B4F66),
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminMoreSectionCard(
    sectionTitle: String,
    items: List<AdminMoreShortcut>,
    onItemClick: (AdminMoreShortcut) -> Unit,
) {
    val typo = LocalRfTypography.current
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        BasicText(
            text = sectionTitle.uppercase(),
            style = TextStyle(
                fontFamily = typo.bodyFamily,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.7.sp,
                color = Color(0xFF6E7288),
            ),
            modifier = Modifier.padding(horizontal = 4.dp),
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
                items.forEachIndexed { index, item ->
                    if (index > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 64.dp)
                                .height(1.dp)
                                .background(Color(0xFFF0F0F6)),
                        )
                    }
                    AdminMoreMenuRow(
                        item = item,
                        onClick = { onItemClick(item) },
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminMoreMenuRow(
    item: AdminMoreShortcut,
    onClick: () -> Unit,
) {
    val typo = LocalRfTypography.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin-more-item-${item.id}")
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(item.iconBg),
            contentAlignment = Alignment.Center,
        ) {
            RfIcon(kind = item.icon, tint = item.iconTint, size = 19.dp)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            BasicText(
                text = item.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E324A)),
            )
            BasicText(
                text = item.subtitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 12.sp, color = Color(0xFF6E7288)),
            )
        }
        val activeBadge = item.badge
        if (activeBadge != null) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(activeBadge.backgroundColor)
                    .padding(horizontal = 9.dp, vertical = 3.dp),
            ) {
                BasicText(
                    text = activeBadge.text,
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = activeBadge.textColor),
                )
            }
        }
        RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = Color(0xFF8C90A6), size = 16.dp)
    }
}

@Composable
private fun AdminMoreEmptySearchCard(
    query: String,
    onReset: () -> Unit,
) {
    val typo = LocalRfTypography.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
            .padding(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BasicText(
                text = if (query.isEmpty()) "Sin módulos en esta categoría" else "No encontramos \"$query\"",
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E324A)),
            )
            BasicText(
                text = "Prueba otro término o vuelve a mostrar todos los módulos.",
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, color = Color(0xFF5F6377)),
            )
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFFECEBFF))
                    .clickable(onClick = onReset)
                    .padding(horizontal = 14.dp, vertical = 7.dp),
            ) {
                BasicText(
                    text = "Mostrar todo",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF4B47D6)),
                )
            }
        }
    }
}

@Composable
private fun AdminMoreAccountCard(
    onOpenSettings: () -> Unit,
    onSwitchToClient: () -> Unit,
    onLogout: () -> Unit,
) {
    val typo = LocalRfTypography.current
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        BasicText(
            text = "CUENTA Y SESIÓN",
            style = TextStyle(
                fontFamily = typo.bodyFamily,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.7.sp,
                color = Color(0xFF6E7288),
            ),
            modifier = Modifier.padding(horizontal = 4.dp),
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
                        .clickable(onClick = onOpenSettings)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(modifier = Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFFF2F2F8)), contentAlignment = Alignment.Center) {
                        RfIcon(kind = RfIconKind.SETTINGS, tint = Color(0xFF5F6377), size = 18.dp)
                    }
                    BasicText("Ajustes del negocio", modifier = Modifier.weight(1f), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E324A)))
                    RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = Color(0xFF8C90A6), size = 16.dp)
                }

                Box(modifier = Modifier.fillMaxWidth().padding(start = 60.dp).height(1.dp).background(Color(0xFFF0F0F6)))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onSwitchToClient)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(modifier = Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(RfColors.VioletaTint), contentAlignment = Alignment.Center) {
                        RfIcon(kind = RfIconKind.USER, tint = RfColors.VioletaTintInk, size = 18.dp)
                    }
                    BasicText("Entrar como cliente", modifier = Modifier.weight(1f), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E324A)))
                    RfIcon(kind = RfIconKind.CHEVRON_RIGHT, tint = Color(0xFF8C90A6), size = 16.dp)
                }

                Box(modifier = Modifier.fillMaxWidth().padding(start = 60.dp).height(1.dp).background(Color(0xFFF0F0F6)))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onLogout)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(modifier = Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFFFCE3E8)), contentAlignment = Alignment.Center) {
                        RfIcon(kind = RfIconKind.LOGOUT, tint = Color(0xFFDC2B48), size = 18.dp)
                    }
                    BasicText("Cerrar sesión", modifier = Modifier.weight(1f), style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFDC2B48)))
                }
            }
        }
    }
}

@Composable
private fun AdminMoreWebDesktopSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    onOpenBrowser: () -> Unit,
    onOpenDesktopPreview: () -> Unit,
) {
    val typo = LocalRfTypography.current
    AdminBottomSheetOverlay(
        visible = visible,
        onDismiss = onDismiss,
        bottomClearance = RfDimens.BottomBarContentClearance,
        verticalSpacing = 14.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            BasicText(
                text = "Panel Web Desktop (1440px)",
                style = TextStyle(fontFamily = typo.displayFamily, fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Color(0xFF151729)),
            )
            RfIcon(
                kind = RfIconKind.CLOSE,
                tint = Color(0xFF5F6377),
                size = 20.dp,
                modifier = Modifier.clickable(onClick = onDismiss),
            )
        }
        BasicText(
            text = "El centro administrativo de escritorio está optimizado para monitores amplios de 1440px. Puedes abrirlo directamente en tu navegador o explorar la vista previa adaptada.",
            style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 13.5.sp, color = Color(0xFF5F6377), lineHeight = 19.sp),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(AdminMoreIndigoGradient)
                .clickable(onClick = onOpenBrowser),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                RfIcon(kind = RfIconKind.GLOBE, tint = Color.White, size = 18.dp)
                BasicText(
                    text = "Abrir en navegador",
                    style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White),
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFECEBFF))
                .clickable(onClick = onOpenDesktopPreview),
            contentAlignment = Alignment.Center,
        ) {
            BasicText(
                text = "Vista previa adaptada",
                style = TextStyle(fontFamily = typo.bodyFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B47D6)),
            )
        }
    }
}
