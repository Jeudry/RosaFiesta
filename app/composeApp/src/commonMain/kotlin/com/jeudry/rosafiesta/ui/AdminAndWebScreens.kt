package com.jeudry.rosafiesta.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeudry.rosafiesta.core.engine.StaffPermission
import com.jeudry.rosafiesta.designsystem.LocalRfPalette
import com.jeudry.rosafiesta.designsystem.LocalRfTypography
import com.jeudry.rosafiesta.designsystem.RfColors
import com.jeudry.rosafiesta.designsystem.RfDimens
import com.jeudry.rosafiesta.designsystem.RfIcon
import com.jeudry.rosafiesta.designsystem.RfIconKind
import com.jeudry.rosafiesta.designsystem.RfLogo
import com.jeudry.rosafiesta.designsystem.RfLogoSize
import com.jeudry.rosafiesta.designsystem.RfPhoto
import com.jeudry.rosafiesta.designsystem.RfPhotoKey
import com.jeudry.rosafiesta.designsystem.RfRadialBackgroundBox
import com.jeudry.rosafiesta.designsystem.RfSwitch
import com.jeudry.rosafiesta.designsystem.RfTag
import com.jeudry.rosafiesta.designsystem.RfTagTone
import com.jeudry.rosafiesta.designsystem.RfTheme
import com.jeudry.rosafiesta.designsystem.RfThemeVariant
import com.jeudry.rosafiesta.designsystem.RosaAvatar
import com.jeudry.rosafiesta.designsystem.RosaAvatarSize
import com.jeudry.rosafiesta.designsystem.rfCardShadow
import com.jeudry.rosafiesta.designsystem.rfIconButtonShadow
import com.jeudry.rosafiesta.domain.BusinessSettings
import com.jeudry.rosafiesta.domain.CanonicalSampleData
import com.jeudry.rosafiesta.domain.CatalogArticle
import com.jeudry.rosafiesta.domain.ChatThread
import com.jeudry.rosafiesta.domain.ClientEvent
import com.jeudry.rosafiesta.domain.ClientProfile
import com.jeudry.rosafiesta.domain.EventStatus
import com.jeudry.rosafiesta.domain.LeadRecord
import com.jeudry.rosafiesta.domain.NotificationRecord
import com.jeudry.rosafiesta.domain.PaymentRecord
import com.jeudry.rosafiesta.domain.PhotoAsset
import com.jeudry.rosafiesta.domain.QuoteRecord
import com.jeudry.rosafiesta.domain.StaffMember

private enum class A39DesktopSection(val title: String) {
    INICIO("Inicio"),
    SOLICITUDES("Solicitudes"),
    EVENTOS("Eventos"),
    INVENTARIO("Inventario"),
    COTIZACIONES("Cotizaciones"),
    PAGOS("Pagos"),
    CLIENTES("Clientes"),
    CHATS("Chats"),
    REPORTES("Reportes"),
    AJUSTES("Ajustes"),
}

private enum class A39DesktopModal {
    NONE,
    NOTIFICATIONS,
    ROSA_AI,
    NEW_EVENT,
    EVENT_DETAIL,
}

private data class A39NavItem(
    val section: A39DesktopSection,
    val label: String,
    val icon: RfIconKind,
    val badge: String? = null,
)

private data class AdminDesktopEventRowSpec(
    val eventId: String,
    val photoKey: RfPhotoKey,
    val title: String,
    val date: String,
    val client: String,
    val total: String,
    val percent: Int,
    val statusLabel: String,
    val statusTone: RfTagTone,
)

private data class A39AttentionRowData(
    val title: String,
    val subtitle: String,
    val trailingText: String?,
    val badgeText: String?,
    val icon: RfIconKind,
    val tileBg: Color,
    val tileTint: Color,
    val onClick: () -> Unit,
)

/**
 * Pantalla `A39` — Versión web de escritorio de la App Administrativa (1440×900 dp con barra lateral).
 * Permite operar secciones dentro del lienzo desktop, filtrar con `⌘K`, abrir cajones contextuales y exportar CSV/DGII/PDF.
 */
@Composable
public fun RenderAdminWebDesktopA39(
    spec: ScreenSpec,
    viewModel: RosaFiestaViewModel? = null,
    onPrimaryAction: () -> Unit = {},
    onSelectScreenCode: (String) -> Unit = {},
    onSwitchAppMode: () -> Unit = {},
    fillAvailable: Boolean = false,
    modifier: Modifier = Modifier,
) {
    RfTheme(variant = RfThemeVariant.ADMIN) {
        val palette = LocalRfPalette.current
        val typo = LocalRfTypography.current

        val eventsList = viewModel?.events?.collectAsState()?.value ?: CanonicalSampleData.events
        val metrics = viewModel?.metrics?.collectAsState()?.value ?: CanonicalSampleData.businessMetrics
        val leadsList = viewModel?.leads?.collectAsState()?.value ?: emptyList()
        val articlesList = viewModel?.articles?.collectAsState()?.value ?: CanonicalSampleData.articles
        val quotesList = viewModel?.quotes?.collectAsState()?.value ?: CanonicalSampleData.quotes
        val paymentsList = viewModel?.payments?.collectAsState()?.value ?: CanonicalSampleData.payments
        val clientsList = viewModel?.clients?.collectAsState()?.value ?: CanonicalSampleData.clients
        val chatThreadsList = viewModel?.chatThreads?.collectAsState()?.value ?: emptyList()
        val selectedChatThreadId = viewModel?.selectedChatThreadId?.collectAsState()?.value ?: ""
        val staffList = viewModel?.staffMembers?.collectAsState()?.value ?: CanonicalSampleData.staffMembers
        val settings = viewModel?.businessSettings?.collectAsState()?.value ?: BusinessSettings()
        val notificationsList = viewModel?.notifications?.collectAsState()?.value ?: emptyList()
        val profileName = viewModel?.profileName?.collectAsState()?.value ?: "María"
        val profileEmail = viewModel?.profileEmail?.collectAsState()?.value ?: "admin@rosafiesta.com"
        val selectedEventId = viewModel?.selectedEventId?.collectAsState()?.value ?: ""
        val formFeedback = viewModel?.formFeedback?.collectAsState()?.value
        val formError = viewModel?.formError?.collectAsState()?.value

        var activeSection by remember { mutableStateOf(A39DesktopSection.INICIO) }
        var activeModal by remember { mutableStateOf(A39DesktopModal.NONE) }
        var desktopSearchQuery by remember { mutableStateOf("") }
        var localStatusMessage by remember { mutableStateOf<String?>(null) }
        val searchFocusRequester = remember { FocusRequester() }

        val isPaymentsRestricted = viewModel?.isScreenRestrictedForActiveStaff("A20") ?: false
        val canViewPayments = !isPaymentsRestricted
        val pendingLeadsCount = leadsList.count { lead -> lead.stage.equals("Nueva", ignoreCase = true) }
        val leadsBadgeCount = if (viewModel != null) pendingLeadsCount else 2
        val totalUnreadChats = chatThreadsList.sumOf { thread -> thread.unreadCount }
        val chatBadgeCount = if (viewModel != null) totalUnreadChats else 2
        val unreadNotifications = notificationsList.count { notification -> !notification.isRead }
        val bellBadgeCount = if (viewModel != null) unreadNotifications else 3

        val frameModifier = buildDesktopFrameModifier(
            fillAvailable = fillAvailable,
            screenCode = spec.code,
            lineColor = palette.line,
            baseModifier = modifier,
        ).onPreviewKeyEvent { keyEvent ->
            val isKeyDown = keyEvent.type == KeyEventType.KeyDown
            val isCommandMod = if (keyEvent.isCtrlPressed) true else keyEvent.isMetaPressed
            val isKeyK = keyEvent.key == Key.K
            val shouldFocusSearch = listOf(isKeyDown, isCommandMod, isKeyK).all { condition -> condition }
            if (shouldFocusSearch) {
                searchFocusRequester.requestFocus()
                true
            } else {
                false
            }
        }

        RfRadialBackgroundBox(modifier = frameModifier) {
            Box(modifier = Modifier.fillMaxSize()) {
                Row(modifier = Modifier.fillMaxSize()) {
                    AdminDesktopSidebar(
                        activeSection = activeSection,
                        leadsBadgeCount = leadsBadgeCount,
                        chatsBadgeCount = chatBadgeCount,
                        profileName = profileName,
                        profileEmail = profileEmail,
                        onSelectSection = { targetSection ->
                            activeSection = targetSection
                            activeModal = A39DesktopModal.NONE
                        },
                        onSwitchToMobile = { onSelectScreenCode("A01") },
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(horizontal = 32.dp, vertical = 24.dp),
                    ) {
                        AdminDesktopTopBar(
                            activeSection = activeSection,
                            profileFirstName = profileName.substringBefore(' ').ifBlank { "María" },
                            searchQuery = desktopSearchQuery,
                            searchFocusRequester = searchFocusRequester,
                            bellBadgeCount = bellBadgeCount,
                            fillAvailable = fillAvailable,
                            onSearchChange = { updatedQuery -> desktopSearchQuery = updatedQuery },
                            onFocusSearch = { searchFocusRequester.requestFocus() },
                            onOpenNotifications = { activeModal = A39DesktopModal.NOTIFICATIONS },
                            onOpenRosaAi = { activeModal = A39DesktopModal.ROSA_AI },
                            onOpenNewEvent = { activeModal = A39DesktopModal.NEW_EVENT },
                            onExportActiveReport = {
                                if (viewModel != null) {
                                    viewModel.exportReportCsv(reportType = "income", period = "2026-10")
                                } else {
                                    localStatusMessage = "Reporte mensual generado."
                                    onPrimaryAction()
                                }
                            },
                            onSwitchAppMode = onSwitchAppMode,
                        )

                        val activeToast = localStatusMessage ?: formFeedback ?: formError
                        if (!activeToast.isNullOrBlank()) {
                            AdminDesktopFeedbackBanner(
                                message = activeToast,
                                isError = formError != null,
                                onDismiss = {
                                    localStatusMessage = null
                                    viewModel?.clearFormFeedback()
                                    viewModel?.clearFormError()
                                },
                            )
                        }

                        when (activeSection) {
                            A39DesktopSection.INICIO -> {
                                AdminDesktopInicioContent(
                                    viewModel = viewModel,
                                    eventsList = eventsList,
                                    metrics = metrics,
                                    leadsList = leadsList,
                                    articlesList = articlesList,
                                    paymentsList = paymentsList,
                                    searchQuery = desktopSearchQuery,
                                    onNavigateSection = { nextSection -> activeSection = nextSection },
                                    onOpenEventDetail = { eventId ->
                                        viewModel?.selectEvent(eventId)
                                        activeModal = A39DesktopModal.EVENT_DETAIL
                                    },
                                    onOpenLeadInQuotes = { leadId ->
                                        viewModel?.selectLead(leadId)
                                        activeSection = A39DesktopSection.COTIZACIONES
                                    },
                                    onSelectScreenCode = onSelectScreenCode,
                                    modifier = Modifier.weight(1f),
                                )
                            }

                            A39DesktopSection.SOLICITUDES -> {
                                AdminDesktopLeadsSection(
                                    leadsList = leadsList,
                                    searchQuery = desktopSearchQuery,
                                    onAdvanceStage = { leadItem, nextStage ->
                                        viewModel?.updateCommercialLead(
                                            leadId = leadItem.id,
                                            stage = nextStage,
                                            followUpNote = "Actualizado desde web",
                                        )
                                        localStatusMessage = "Solicitud de ${leadItem.clientName} movida a $nextStage."
                                    },
                                    onOpenQuoteBuilder = { leadId ->
                                        viewModel?.selectLead(leadId)
                                        activeSection = A39DesktopSection.COTIZACIONES
                                    },
                                    onOpenMobileRequests = { onSelectScreenCode("A02") },
                                    modifier = Modifier.weight(1f),
                                )
                            }

                            A39DesktopSection.EVENTOS -> {
                                AdminDesktopEventsSection(
                                    eventsList = eventsList,
                                    searchQuery = desktopSearchQuery,
                                    onOpenEventDetail = { eventId ->
                                        viewModel?.selectEvent(eventId)
                                        activeModal = A39DesktopModal.EVENT_DETAIL
                                    },
                                    onDuplicateEvent = { eventId ->
                                        viewModel?.duplicateExistingEvent(eventId)
                                    },
                                    onOpenMobileEvents = { onSelectScreenCode("A11") },
                                    modifier = Modifier.weight(1f),
                                )
                            }

                            A39DesktopSection.INVENTARIO -> {
                                AdminDesktopInventorySection(
                                    articlesList = articlesList,
                                    searchQuery = desktopSearchQuery,
                                    onAdjustStock = { articleId, delta ->
                                        viewModel?.adjustArticleStock(articleId = articleId, delta = delta)
                                    },
                                    onOpenMobileInventory = { onSelectScreenCode("A09") },
                                    modifier = Modifier.weight(1f),
                                )
                            }

                            A39DesktopSection.COTIZACIONES -> {
                                AdminDesktopQuotesSection(
                                    quotesList = quotesList,
                                    searchQuery = desktopSearchQuery,
                                    onApproveQuote = { quoteItem ->
                                        viewModel?.approveQuoteById(
                                            quoteId = quoteItem.id,
                                            signedByName = profileName,
                                            signatureSvg = "M12,32 L78,18",
                                        )
                                    },
                                    onDeclineQuote = { quoteItem ->
                                        viewModel?.declineQuoteById(quoteId = quoteItem.id)
                                    },
                                    onDuplicateQuote = { quoteItem ->
                                        viewModel?.duplicateQuoteById(quoteItem.id)
                                    },
                                    onOpenBuilder = { onSelectScreenCode("A03") },
                                    modifier = Modifier.weight(1f),
                                )
                            }

                            A39DesktopSection.PAGOS -> {
                                AdminDesktopPaymentsSection(
                                    canViewPayments = canViewPayments,
                                    paymentsList = paymentsList,
                                    eventsList = eventsList,
                                    searchQuery = desktopSearchQuery,
                                    onExportReceipt = { matchingEvent, paymentRecord ->
                                        viewModel?.exportReceiptDocument(
                                            event = matchingEvent,
                                            payment = paymentRecord,
                                            shareInsteadOfSave = false,
                                        )
                                    },
                                    onRegisterQuickPayment = { targetEventId, amountText, methodLabel, referenceCode ->
                                        if (viewModel != null) {
                                            viewModel.setPaymentAmountInput(amountText)
                                            viewModel.setPaymentMethodInput(methodLabel)
                                            viewModel.setPaymentRefInput(referenceCode)
                                            viewModel.submitPayment(targetEventId)
                                        }
                                    },
                                    onOpenMobilePayments = { onSelectScreenCode("A20") },
                                    modifier = Modifier.weight(1f),
                                )
                            }

                            A39DesktopSection.CLIENTES -> {
                                AdminDesktopClientsSection(
                                    clientsList = clientsList,
                                    searchQuery = desktopSearchQuery,
                                    onSelectClient = { clientId ->
                                        viewModel?.selectClient(clientId)
                                        onSelectScreenCode("A27")
                                    },
                                    onOpenChats = { activeSection = A39DesktopSection.CHATS },
                                    modifier = Modifier.weight(1f),
                                )
                            }

                            A39DesktopSection.CHATS -> {
                                val threadMessages = viewModel?.messagesForSelectedThread() ?: emptyList()
                                AdminDesktopChatsSection(
                                    threads = chatThreadsList,
                                    selectedThreadId = selectedChatThreadId,
                                    messages = threadMessages,
                                    onSelectThread = { threadId -> viewModel?.selectChatThread(threadId) },
                                    onSendReply = { messageText ->
                                        viewModel?.sendAdminDirectChatReply(rawReplyText = messageText)
                                    },
                                    onOpenMobileChats = { onSelectScreenCode("A14") },
                                    modifier = Modifier.weight(1f),
                                )
                            }

                            A39DesktopSection.REPORTES -> {
                                AdminDesktopReportsSection(
                                    metrics = metrics,
                                    paymentsCount = paymentsList.size,
                                    staffMembers = staffList,
                                    canViewPayments = canViewPayments,
                                    onExportIncomeCsv = {
                                        if (viewModel != null) {
                                            viewModel.exportReportCsv(reportType = "income", period = "2026-10")
                                        } else {
                                            localStatusMessage = "CSV de ingresos generado."
                                        }
                                    },
                                    onExportDgii607 = {
                                        if (viewModel != null) {
                                            viewModel.exportReportCsv(reportType = "dgii607", period = "2026-10")
                                        } else {
                                            localStatusMessage = "Archivo DGII 607 generado."
                                        }
                                    },
                                    onToggleStaffPaymentAccess = { staffName ->
                                        viewModel?.toggleStaffPaymentAccess(staffName)
                                    },
                                    onOpenDetailedAnalytics = { onSelectScreenCode("s68") },
                                    modifier = Modifier.weight(1f),
                                )
                            }

                            A39DesktopSection.AJUSTES -> {
                                AdminDesktopSettingsSection(
                                    viewModel = viewModel,
                                    initialSettings = settings,
                                    onSaveSettings = { updatedSettings ->
                                        val savedOk = viewModel?.saveBusinessConfig(updatedSettings) ?: true
                                        if (savedOk) {
                                            localStatusMessage = "Configuración guardada para ${updatedSettings.businessName}."
                                        }
                                    },
                                    onOpenMobileSettings = { onSelectScreenCode("A31") },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                }

                if (activeModal != A39DesktopModal.NONE) {
                    val selectedEvent = eventsList.firstOrNull { candidate -> candidate.id == selectedEventId }
                        ?: eventsList.firstOrNull()
                    AdminDesktopModalHost(
                        activeModal = activeModal,
                        viewModel = viewModel,
                        notifications = notificationsList,
                        selectedEvent = selectedEvent,
                        onDismiss = { activeModal = A39DesktopModal.NONE },
                        onMarkNotificationRead = { notificationId ->
                            viewModel?.markNotificationAsRead(notificationId)
                        },
                        onSelectScreenCode = { targetCode ->
                            activeModal = A39DesktopModal.NONE
                            onSelectScreenCode(targetCode)
                        },
                    )
                }
            }
        }
    }
}

private fun buildDesktopFrameModifier(
    fillAvailable: Boolean,
    screenCode: String,
    lineColor: Color,
    baseModifier: Modifier,
): Modifier {
    if (fillAvailable) {
        return baseModifier.fillMaxSize().testTag("screen-$screenCode")
    }
    return baseModifier
        .width(RfDimens.WebFrameWidth)
        .height(RfDimens.WebFrameHeight)
        .clip(RoundedCornerShape(24.dp))
        .border(1.5.dp, lineColor, RoundedCornerShape(24.dp))
        .testTag("screen-$screenCode")
}

@Composable
private fun AdminDesktopSidebar(
    activeSection: A39DesktopSection,
    leadsBadgeCount: Int,
    chatsBadgeCount: Int,
    profileName: String,
    profileEmail: String,
    onSelectSection: (A39DesktopSection) -> Unit,
    onSwitchToMobile: () -> Unit,
) {
    val typo = LocalRfTypography.current
    val navItems = listOf(
        A39NavItem(A39DesktopSection.INICIO, "Inicio", RfIconKind.HOME),
        A39NavItem(
            A39DesktopSection.SOLICITUDES,
            "Solicitudes",
            RfIconKind.ORDERS,
            badge = if (leadsBadgeCount > 0) leadsBadgeCount.toString() else null,
        ),
        A39NavItem(A39DesktopSection.EVENTOS, "Eventos", RfIconKind.CALENDAR),
        A39NavItem(A39DesktopSection.INVENTARIO, "Inventario", RfIconKind.CATALOG),
        A39NavItem(A39DesktopSection.COTIZACIONES, "Cotizaciones", RfIconKind.RECEIPT),
        A39NavItem(A39DesktopSection.PAGOS, "Pagos", RfIconKind.WALLET),
        A39NavItem(A39DesktopSection.CLIENTES, "Clientes", RfIconKind.USER),
        A39NavItem(
            A39DesktopSection.CHATS,
            "Chats",
            RfIconKind.CHAT,
            badge = if (chatsBadgeCount > 0) chatsBadgeCount.toString() else null,
        ),
        A39NavItem(A39DesktopSection.REPORTES, "Reportes", RfIconKind.ORDERS),
    )

    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .width(256.dp)
            .fillMaxHeight()
            .background(Color.White)
            .border(width = 1.dp, color = Color(0xFFECECF5))
            .padding(horizontal = 16.dp, vertical = 24.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 6.dp, end = 6.dp, bottom = 16.dp),
            ) {
                RfLogo(size = RfLogoSize.MEDIUM)
                Column {
                    BasicText(
                        text = "Rosa Fiesta",
                        style = typo.backScreenTitle.copy(
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF151729),
                        ),
                    )
                    Spacer(Modifier.height(2.dp))
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .height(20.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color(0xFFECEBFF))
                            .padding(horizontal = 7.dp),
                    ) {
                        BasicText(
                            text = "Admin",
                            style = typo.tagLabel.copy(
                                color = Color(0xFF4B47D6),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                        )
                    }
                }
            }

            navItems.forEach { item ->
                val isSelected = item.section == activeSection
                AdminDesktopSidebarNavRow(
                    label = item.label,
                    icon = item.icon,
                    badge = item.badge,
                    isSelected = isSelected,
                    onClick = { onSelectSection(item.section) },
                )
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            AdminDesktopSidebarNavRow(
                label = "Ajustes",
                icon = RfIconKind.SETTINGS,
                badge = null,
                isSelected = activeSection == A39DesktopSection.AJUSTES,
                onClick = { onSelectSection(A39DesktopSection.AJUSTES) },
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFFECECF5)),
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    val avatarInitial = profileName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "M"
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFECEBFF)),
                    ) {
                        BasicText(
                            text = avatarInitial,
                            style = typo.bodyStrong.copy(
                                color = Color(0xFF4B47D6),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                            ),
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        BasicText(
                            text = profileName.substringBefore(' ').ifBlank { "María" },
                            style = typo.bodyStrong.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                        )
                        BasicText(
                            text = profileEmail,
                            style = typo.secondary.copy(fontSize = 12.5.sp, color = Color(0xFF5F6377)),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF2F2F8))
                        .clickable(onClick = onSwitchToMobile)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    BasicText(
                        text = "Móvil",
                        style = typo.caption.copy(
                            color = Color(0xFF4B47D6),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminDesktopSidebarNavRow(
    label: String,
    icon: RfIconKind,
    badge: String?,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val typo = LocalRfTypography.current
    val bgModifier = if (isSelected) {
        Modifier.background(
            brush = Brush.linearGradient(
                listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0)),
            ),
            shape = RoundedCornerShape(12.dp),
        )
    } else {
        Modifier
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .then(bgModifier)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            RfIcon(
                kind = icon,
                tint = if (isSelected) Color.White else Color(0xFF3A3D52),
                size = 19.dp,
            )
            BasicText(
                text = label,
                style = typo.body.copy(
                    fontSize = 14.5.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (isSelected) Color.White else Color(0xFF3A3D52),
                ),
            )
        }
        if (badge != null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .height(22.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0xFFFFD45C))
                    .padding(horizontal = 7.dp),
            ) {
                BasicText(
                    text = badge,
                    style = typo.tagLabel.copy(
                        color = Color(0xFF2E1A47),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                    ),
                )
            }
        }
    }
}

@Composable
private fun AdminDesktopTopBar(
    activeSection: A39DesktopSection,
    profileFirstName: String,
    searchQuery: String,
    searchFocusRequester: FocusRequester,
    bellBadgeCount: Int,
    fillAvailable: Boolean,
    onSearchChange: (String) -> Unit,
    onFocusSearch: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenRosaAi: () -> Unit,
    onOpenNewEvent: () -> Unit,
    onExportActiveReport: () -> Unit,
    onSwitchAppMode: () -> Unit,
) {
    val typo = LocalRfTypography.current
    val headingText = if (activeSection == A39DesktopSection.INICIO) {
        "Hola, $profileFirstName"
    } else {
        activeSection.title
    }
    val subtitleText = if (activeSection == A39DesktopSection.INICIO) {
        "Martes 20 de octubre"
    } else {
        "Panel administrativo web · RosaFiesta"
    }

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            BasicText(
                text = subtitleText,
                style = typo.secondary.copy(fontSize = 14.sp, color = Color(0xFF5F6377)),
            )
            Spacer(Modifier.height(2.dp))
            BasicText(
                text = headingText,
                style = typo.rootTabTitle.copy(fontSize = 28.sp, fontWeight = FontWeight.ExtraBold),
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .width(340.dp)
                    .height(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    RfIcon(
                        kind = RfIconKind.SEARCH,
                        tint = Color(0xFF5F6377),
                        size = 18.dp,
                    )
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        singleLine = true,
                        textStyle = typo.body.copy(fontSize = 14.sp, color = Color(0xFF151729)),
                        cursorBrush = SolidColor(Color(0xFF5B6CF0)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(searchFocusRequester),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                BasicText(
                                    text = "Buscar eventos, clientes, artículos",
                                    style = typo.body.copy(fontSize = 14.sp, color = Color(0xFF5F6377)),
                                    maxLines = 1,
                                )
                            }
                            innerTextField()
                        },
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    if (searchQuery.isNotEmpty()) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFF2F2F8))
                                .clickable { onSearchChange("") }
                                .padding(horizontal = 6.dp, vertical = 3.dp),
                        ) {
                            BasicText(
                                text = "✕",
                                style = typo.caption.copy(
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF5F6377),
                                ),
                            )
                        }
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFF2F2F8))
                            .clickable(onClick = onFocusSearch)
                            .padding(horizontal = 7.dp, vertical = 3.dp),
                    ) {
                        BasicText(
                            text = "⌘K",
                            style = typo.caption.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF5F6377),
                            ),
                        )
                    }
                }
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .rfIconButtonShadow(CircleShape, isAdmin = true)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Color(0xFFECECF5), CircleShape)
                    .clickable(onClick = onOpenNotifications),
            ) {
                RfIcon(kind = RfIconKind.BELL, tint = Color(0xFF151729), size = 20.dp)
                if (bellBadgeCount > 0) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFD45C)),
                    ) {
                        BasicText(
                            text = bellBadgeCount.toString(),
                            style = typo.tagLabel.copy(
                                color = Color(0xFF2E1A47),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                            ),
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .height(46.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(16.dp))
                    .clickable(onClick = onOpenRosaAi)
                    .padding(horizontal = 16.dp),
            ) {
                RosaAvatar(size = RosaAvatarSize.MINI, forceAdminPalette = true)
                BasicText(
                    text = "Crear con Rosa",
                    style = typo.buttonLabel.copy(
                        color = Color(0xFF151729),
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
            }

            if (activeSection == A39DesktopSection.REPORTES) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .height(46.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            brush = Brush.linearGradient(
                                listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0)),
                            ),
                        )
                        .clickable(onClick = onExportActiveReport)
                        .padding(horizontal = 18.dp),
                ) {
                    RfIcon(kind = RfIconKind.RECEIPT, tint = Color.White, size = 18.dp)
                    BasicText(
                        text = "Exportar",
                        style = typo.buttonLabel.copy(
                            color = Color.White,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .height(46.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            brush = Brush.linearGradient(
                                listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0)),
                            ),
                        )
                        .clickable(onClick = onOpenNewEvent)
                        .padding(horizontal = 18.dp),
                ) {
                    RfIcon(kind = RfIconKind.PLUS, tint = Color.White, size = 18.dp)
                    BasicText(
                        text = "Nuevo evento",
                        style = typo.buttonLabel.copy(
                            color = Color.White,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                }
            }

            if (fillAvailable) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .height(46.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(RfColors.RosaBrand)
                        .clickable(onClick = onSwitchAppMode)
                        .padding(horizontal = 14.dp),
                ) {
                    BasicText(
                        text = "🌸 App Personal",
                        style = typo.buttonLabel.copy(color = Color.White, fontSize = 13.5.sp),
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminDesktopFeedbackBanner(
    message: String,
    isError: Boolean,
    onDismiss: () -> Unit,
) {
    val typo = LocalRfTypography.current
    val bgColor = if (isError) Color(0xFFFDE8EE) else Color(0xFFEEF6D6)
    val borderColor = if (isError) Color(0xFFF2B6C5) else Color(0xFFCDE29B)
    val textColor = if (isError) Color(0xFFB42345) else Color(0xFF3E560B)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        BasicText(
            text = message,
            style = typo.bodyStrong.copy(fontSize = 13.5.sp, color = textColor),
            modifier = Modifier.weight(1f),
        )
        BasicText(
            text = "Cerrar",
            style = typo.caption.copy(fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = textColor),
            modifier = Modifier.clickable(onClick = onDismiss).padding(start = 12.dp),
        )
    }
}

@Composable
private fun AdminDesktopInicioContent(
    viewModel: RosaFiestaViewModel?,
    eventsList: List<ClientEvent>,
    metrics: com.jeudry.rosafiesta.domain.BusinessMetrics,
    leadsList: List<LeadRecord>,
    articlesList: List<CatalogArticle>,
    paymentsList: List<PaymentRecord>,
    searchQuery: String,
    onNavigateSection: (A39DesktopSection) -> Unit,
    onOpenEventDetail: (String) -> Unit,
    onOpenLeadInQuotes: (String) -> Unit,
    onSelectScreenCode: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val activeEventsCount = if (viewModel != null) {
        eventsList.count { eventItem -> eventItem.status != EventStatus.CANCELLED && eventItem.status != EventStatus.COMPLETED }
    } else {
        metrics.activeEventsCount
    }
    val pendingPaymentEventsCount = paymentsList.count { payment -> !payment.verified }
    val pendingReceivableLabel = if (pendingPaymentEventsCount > 0) {
        "$pendingPaymentEventsCount por verificar · 1 vence el jueves"
    } else {
        "3 pagos · 1 vence el jueves"
    }
    val openLeads = leadsList.filter { lead ->
        lead.stage.equals("Nueva", ignoreCase = true) || lead.stage.equals("Contactada", ignoreCase = true)
    }
    val requestsCount = if (viewModel != null) openLeads.size else metrics.newRequestsCount
    val estimatedLeadSum = openLeads.sumOf { lead -> lead.estimatedDop }
    val hasLiveLeadEstimate = listOf(viewModel != null, estimatedLeadSum > 0).all { flag -> flag }
    val requestsSubtext = if (hasLiveLeadEstimate) {
        "Est. ${formatDesktopDop(estimatedLeadSum)} en total"
    } else {
        "Est. RD$ 2,545 en total"
    }
    val rawNextDate = eventsList.firstOrNull { eventItem -> eventItem.status != EventStatus.CANCELLED }?.dateLabel
    val nextEventDate = if (rawNextDate != null) {
        formatReadableSpanishDateText(rawNextDate).lowercase()
    } else {
        "vie, 23 de octubre"
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            AdminStatCard(
                label = "Ingresos de ${metrics.monthLabel.lowercase()}",
                value = formatDesktopDop(metrics.octoberRevenueDop),
                subtext = "▲ 22% vs. septiembre",
                subtextColor = Color(0xFF4E6A10),
                iconKind = RfIconKind.ORDERS,
                tileBg = Color(0xFFEEF6D6),
                tileTint = Color(0xFF4E6A10),
                onClick = { onNavigateSection(A39DesktopSection.REPORTES) },
                modifier = Modifier.weight(1f),
            )
            AdminStatCard(
                label = "Por cobrar",
                value = formatDesktopDop(metrics.accountsReceivableDop),
                subtext = pendingReceivableLabel,
                subtextColor = Color(0xFF5F6377),
                iconKind = RfIconKind.WALLET,
                tileBg = Color(0xFFFFF2C9),
                tileTint = Color(0xFF7A5700),
                onClick = { onNavigateSection(A39DesktopSection.PAGOS) },
                modifier = Modifier.weight(1f),
            )
            AdminStatCard(
                label = "Eventos activos",
                value = activeEventsCount.toString(),
                subtext = "Próximo: $nextEventDate",
                subtextColor = Color(0xFF5F6377),
                iconKind = RfIconKind.CALENDAR,
                tileBg = Color(0xFFECEBFF),
                tileTint = Color(0xFF4B47D6),
                onClick = { onNavigateSection(A39DesktopSection.EVENTOS) },
                modifier = Modifier.weight(1f),
            )
            AdminStatCard(
                label = "Solicitudes nuevas",
                value = requestsCount.toString(),
                subtext = requestsSubtext,
                subtextColor = Color(0xFF5F6377),
                iconKind = RfIconKind.ORDERS,
                tileBg = Color(0xFFFDE6F2),
                tileTint = Color(0xFFC2338F),
                onClick = { onNavigateSection(A39DesktopSection.SOLICITUDES) },
                modifier = Modifier.weight(1f),
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            ) {
                AdminUpcomingEventsTable(
                    eventsList = eventsList,
                    searchQuery = searchQuery,
                    onViewAll = { onNavigateSection(A39DesktopSection.EVENTOS) },
                    onEventClick = onOpenEventDetail,
                    modifier = Modifier.weight(1.3f),
                )

                AdminMonthlyRevenueChart(
                    currentMonthRevenueDop = metrics.octoberRevenueDop,
                    modifier = Modifier.weight(1.1f),
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .width(360.dp)
                    .fillMaxHeight(),
            ) {
                val heroEvent = eventsList.firstOrNull { eventItem -> eventItem.status == EventStatus.IN_SETUP }
                    ?: eventsList.firstOrNull { eventItem -> eventItem.status == EventStatus.CONFIRMED }
                    ?: eventsList.firstOrNull()

                AdminHeroEventBanner(
                    heroEvent = heroEvent,
                    onViewEvent = {
                        if (heroEvent != null) {
                            onOpenEventDetail(heroEvent.id)
                        } else {
                            onSelectScreenCode("A13")
                        }
                    },
                    onOpenExecutionMode = {
                        if (heroEvent != null) {
                            viewModel?.selectEvent(heroEvent.id)
                        }
                        onSelectScreenCode("A16")
                    },
                )

                AdminNewRequestsCard(
                    leadsList = leadsList,
                    onQuoteLead = { leadId ->
                        onOpenLeadInQuotes(leadId)
                    },
                    onOpenFallbackBuilder = { onSelectScreenCode("A03") },
                )

                AdminAttentionTodayCard(
                    eventsList = eventsList,
                    articlesList = articlesList,
                    paymentsList = paymentsList,
                    onOpenPayments = { onNavigateSection(A39DesktopSection.PAGOS) },
                    onOpenInventory = { onNavigateSection(A39DesktopSection.INVENTARIO) },
                    onOpenCancellations = { onSelectScreenCode("A32") },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun AdminStatCard(
    label: String,
    value: String,
    subtext: String,
    subtextColor: Color,
    iconKind: RfIconKind,
    tileBg: Color,
    tileTint: Color,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val typo = LocalRfTypography.current
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
            .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 18.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            BasicText(
                text = label,
                style = typo.body.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF5F6377)),
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(tileBg),
            ) {
                RfIcon(kind = iconKind, tint = tileTint, size = 18.dp)
            }
        }
        BasicText(
            text = value,
            style = typo.webKpi.copy(fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF151729)),
        )
        BasicText(
            text = subtext,
            style = typo.caption.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = subtextColor),
        )
    }
}

@Composable
private fun AdminUpcomingEventsTable(
    eventsList: List<ClientEvent>,
    searchQuery: String = "",
    onViewAll: () -> Unit,
    onEventClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val typo = LocalRfTypography.current
    val mappedRows = remember(eventsList) {
        eventsList.take(4).map { eventItem ->
            mapEventToDesktopRowSpec(eventItem)
        }
    }
    val filteredRows = remember(mappedRows, searchQuery) {
        val trimmed = searchQuery.trim()
        if (trimmed.isEmpty()) {
            mappedRows
        } else {
            mappedRows.filter { row ->
                val matchesTitle = row.title.contains(trimmed, ignoreCase = true)
                val matchesClient = row.client.contains(trimmed, ignoreCase = true)
                val matchesStatus = row.statusLabel.contains(trimmed, ignoreCase = true)
                listOf(matchesTitle, matchesClient, matchesStatus).any { matched -> matched }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp)),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            BasicText(
                text = "Próximos eventos",
                style = typo.cardTitle.copy(fontSize = 16.sp, fontWeight = FontWeight.ExtraBold),
            )
            BasicText(
                text = "Ver todos",
                style = typo.buttonLabel.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4B47D6),
                ),
                modifier = Modifier.clickable(onClick = onViewAll),
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF8F8FC))
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            BasicText("EVENTO", modifier = Modifier.weight(3.0f), style = typo.sectionCap.copy(fontSize = 11.sp))
            BasicText("FECHA", modifier = Modifier.weight(0.85f), style = typo.sectionCap.copy(fontSize = 11.sp))
            BasicText("CLIENTE", modifier = Modifier.weight(0.95f), style = typo.sectionCap.copy(fontSize = 11.sp))
            BasicText("TOTAL", modifier = Modifier.weight(0.85f), style = typo.sectionCap.copy(fontSize = 11.sp))
            BasicText("PAGADO", modifier = Modifier.weight(1.1f), style = typo.sectionCap.copy(fontSize = 11.sp))
            BasicText("ESTADO", modifier = Modifier.weight(1.25f), style = typo.sectionCap.copy(fontSize = 11.sp))
        }

        if (filteredRows.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth().padding(24.dp),
            ) {
                BasicText(
                    text = "Sin eventos coincidentes con \"$searchQuery\".",
                    style = typo.secondary.copy(fontSize = 13.5.sp, color = Color(0xFF5F6377)),
                )
            }
        } else {
            filteredRows.forEach { rowSpec ->
                AdminEventRow(
                    photoKey = rowSpec.photoKey,
                    title = rowSpec.title,
                    date = rowSpec.date,
                    client = rowSpec.client,
                    total = rowSpec.total,
                    percent = rowSpec.percent,
                    statusLabel = rowSpec.statusLabel,
                    statusTone = rowSpec.statusTone,
                    onClick = { onEventClick(rowSpec.eventId) },
                )
            }
        }
    }
}

private fun mapEventToDesktopRowSpec(eventItem: ClientEvent): AdminDesktopEventRowSpec {
    val paidPercent = if (eventItem.totalDop > 0) {
        ((eventItem.paidDepositDop * 100) / eventItem.totalDop).coerceIn(0, 100)
    } else {
        0
    }
    val totalText = if (eventItem.totalDop > 0) formatDesktopDop(eventItem.totalDop) else "Por cotizar"
    val statusTone = when (eventItem.status) {
        EventStatus.IN_SETUP -> RfTagTone.TEAL
        EventStatus.CONFIRMED -> RfTagTone.LIMA
        EventStatus.PENDING_QUOTE -> RfTagTone.SOL
        EventStatus.COMPLETED -> RfTagTone.INDIGO
        EventStatus.CANCELLED -> RfTagTone.INDIGO
    }
    val statusLabel = when {
        eventItem.id == "ev-boda-real" && eventItem.status == EventStatus.IN_SETUP -> "Ajustado"
        else -> eventItem.status.label
    }
    val shortClientName = formatCompactClientName(eventItem.clientName)
    return AdminDesktopEventRowSpec(
        eventId = eventItem.id,
        photoKey = photoKeyForDesktopEvent(eventItem.photo),
        title = eventItem.title,
        date = formatReadableSpanishDateText(eventItem.dateLabel),
        client = shortClientName,
        total = totalText,
        percent = paidPercent,
        statusLabel = statusLabel,
        statusTone = statusTone,
    )
}

@Composable
private fun AdminEventRow(
    photoKey: RfPhotoKey,
    title: String,
    date: String,
    client: String,
    total: String,
    percent: Int,
    statusLabel: String,
    statusTone: RfTagTone,
    onClick: () -> Unit,
) {
    val typo = LocalRfTypography.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 0.5.dp, color = Color(0xFFF2F0F5))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(3.0f),
        ) {
            RfPhoto(
                photoKey = photoKey,
                contentDescription = title,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp)),
            )
            BasicText(
                text = title,
                style = typo.bodyStrong.copy(fontSize = 14.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        BasicText(date, modifier = Modifier.weight(0.85f), style = typo.body.copy(fontSize = 14.sp))
        BasicText(client, modifier = Modifier.weight(0.95f), style = typo.body.copy(fontSize = 14.sp), maxLines = 1, overflow = TextOverflow.Ellipsis)
        BasicText(
            text = total,
            modifier = Modifier.weight(0.85f),
            style = typo.cardTitle.copy(
                fontSize = 15.sp,
                fontWeight = if (total.startsWith("RD$")) FontWeight.Bold else FontWeight.Normal,
                color = if (total.startsWith("RD$")) Color(0xFF151729) else Color(0xFF5F6377),
            ),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1.1f),
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFFEDEDF5)),
            ) {
                if (percent > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(percent / 100f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0)),
                                ),
                            ),
                    )
                }
            }
            BasicText(
                text = "$percent%",
                style = typo.secondary.copy(fontSize = 12.5.sp, color = Color(0xFF5F6377)),
                modifier = Modifier.width(30.dp),
            )
        }
        Box(modifier = Modifier.weight(1.25f)) {
            RfTag(text = statusLabel, tone = statusTone)
        }
    }
}

private data class A39ChartBarSpec(
    val month: String,
    val amount: String,
    val height: Dp,
)

@Composable
private fun AdminMonthlyRevenueChart(
    currentMonthRevenueDop: Int,
    modifier: Modifier = Modifier,
) {
    val typo = LocalRfTypography.current
    var selectedMonths by remember { mutableStateOf(6) }
    val currentMonthBarLabel = formatShortK(currentMonthRevenueDop)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            BasicText(
                text = "Ingresos por mes",
                style = typo.cardTitle.copy(fontSize = 16.sp, fontWeight = FontWeight.ExtraBold),
            )
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF2F2F8))
                    .padding(3.dp),
            ) {
                val isSixMonths = selectedMonths == 6
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSixMonths) Color.White else Color.Transparent)
                        .clickable { selectedMonths = 6 }
                        .padding(horizontal = 12.dp),
                ) {
                    BasicText(
                        text = "6 meses",
                        style = typo.caption.copy(
                            fontSize = 13.sp,
                            fontWeight = if (isSixMonths) FontWeight.Bold else FontWeight.SemiBold,
                            color = if (isSixMonths) Color(0xFF151729) else Color(0xFF5F6377),
                        ),
                    )
                }
                val isTwelveMonths = selectedMonths == 12
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isTwelveMonths) Color.White else Color.Transparent)
                        .clickable { selectedMonths = 12 }
                        .padding(horizontal = 12.dp),
                ) {
                    BasicText(
                        text = "12 meses",
                        style = typo.caption.copy(
                            fontSize = 13.sp,
                            fontWeight = if (isTwelveMonths) FontWeight.Bold else FontWeight.SemiBold,
                            color = if (isTwelveMonths) Color(0xFF151729) else Color(0xFF5F6377),
                        ),
                    )
                }
            }
        }

        Spacer(Modifier.weight(1f))

        val bars = if (selectedMonths == 6) {
            listOf(
                A39ChartBarSpec("may", "6.2k", 66.dp),
                A39ChartBarSpec("jun", "9.8k", 105.dp),
                A39ChartBarSpec("jul", "8.1k", 87.dp),
                A39ChartBarSpec("ago", "11.3k", 121.dp),
                A39ChartBarSpec("sep", "10.2k", 109.dp),
                A39ChartBarSpec("oct", currentMonthBarLabel, 133.dp),
            )
        } else {
            listOf(
                A39ChartBarSpec("nov", "7.4k", 78.dp),
                A39ChartBarSpec("dic", "11.8k", 124.dp),
                A39ChartBarSpec("ene", "8.5k", 90.dp),
                A39ChartBarSpec("feb", "7.9k", 84.dp),
                A39ChartBarSpec("mar", "9.1k", 96.dp),
                A39ChartBarSpec("abr", "8.7k", 92.dp),
                A39ChartBarSpec("may", "6.2k", 66.dp),
                A39ChartBarSpec("jun", "9.8k", 105.dp),
                A39ChartBarSpec("jul", "8.1k", 87.dp),
                A39ChartBarSpec("ago", "11.3k", 121.dp),
                A39ChartBarSpec("sep", "10.2k", 109.dp),
                A39ChartBarSpec("oct", currentMonthBarLabel, 133.dp),
            )
        }

        val barSpacing = if (selectedMonths == 12) 8.dp else 18.dp
        val labelSize = if (selectedMonths == 12) 10.5.sp else 12.sp

        Row(
            horizontalArrangement = Arrangement.spacedBy(barSpacing),
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
        ) {
            bars.forEachIndexed { idx, bar ->
                val isLatestMonth = idx == bars.lastIndex
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    BasicText(
                        text = bar.amount,
                        maxLines = 1,
                        style = typo.caption.copy(
                            fontSize = labelSize,
                            fontWeight = FontWeight.Bold,
                            color = if (isLatestMonth) Color(0xFF151729) else Color(0xFF5F6377),
                        ),
                    )
                    val barBrush = if (isLatestMonth) {
                        Brush.linearGradient(
                            listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0), Color(0xFF2FB8C8)),
                        )
                    } else {
                        Brush.linearGradient(listOf(Color(0xFFE3E4F5), Color(0xFFE3E4F5)))
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(bar.height)
                            .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                            .background(barBrush),
                    )
                    BasicText(
                        text = bar.month,
                        maxLines = 1,
                        style = typo.secondary.copy(fontSize = labelSize, color = Color(0xFF5F6377)),
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminHeroEventBanner(
    heroEvent: ClientEvent?,
    onViewEvent: () -> Unit,
    onOpenExecutionMode: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val typo = LocalRfTypography.current
    val titleText = heroEvent?.title ?: "Boda Real Rosa & Dorado"
    val subtitleText = if (heroEvent != null) {
        "Montaje 2:00 p. m. · ${heroEvent.venue} · ${heroEvent.guests} invitados"
    } else {
        "Montaje 2:00 p. m. · Jardín Botánico · 120 invitados"
    }
    val timingText = if (heroEvent != null) {
        "${formatReadableSpanishDateText(heroEvent.dateLabel)} · Próximo montaje"
    } else {
        "Este viernes · en 3 días"
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0), Color(0xFF2FB8C8)),
                ),
            )
            .padding(horizontal = 20.dp, vertical = 18.dp),
    ) {
        BasicText(
            text = timingText,
            style = typo.caption.copy(color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp),
        )
        BasicText(
            text = titleText,
            style = typo.cardTitle.copy(color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold),
        )
        BasicText(
            text = subtitleText,
            style = typo.body.copy(color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp),
        )
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .height(36.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color.White)
                    .clickable(onClick = onViewEvent)
                    .padding(horizontal = 14.dp),
            ) {
                BasicText(
                    text = "Ver evento",
                    style = typo.buttonLabel.copy(color = Color(0xFF4B47D6), fontSize = 13.5.sp, fontWeight = FontWeight.Bold),
                )
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .height(36.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color.White.copy(alpha = 0.2f))
                    .clickable(onClick = onOpenExecutionMode)
                    .padding(horizontal = 14.dp),
            ) {
                BasicText(
                    text = "Modo ejecución",
                    style = typo.buttonLabel.copy(color = Color.White, fontSize = 13.5.sp, fontWeight = FontWeight.Bold),
                )
            }
        }
    }
}

@Composable
private fun AdminNewRequestsCard(
    leadsList: List<LeadRecord>,
    onQuoteLead: (String) -> Unit,
    onOpenFallbackBuilder: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val typo = LocalRfTypography.current
    val displayLeads = remember(leadsList) {
        leadsList.filter { lead -> !lead.stage.equals("Cerrada", ignoreCase = true) }.take(2)
    }
    val badgeCountText = if (leadsList.isNotEmpty()) displayLeads.size.toString() else "2"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp)),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
        ) {
            BasicText(
                text = "Solicitudes nuevas",
                style = typo.cardTitle.copy(fontSize = 16.sp, fontWeight = FontWeight.ExtraBold),
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .height(24.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0xFFFFD45C))
                    .padding(horizontal = 9.dp),
            ) {
                BasicText(
                    text = badgeCountText,
                    style = typo.tagLabel.copy(color = Color(0xFF2E1A47), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold),
                )
            }
        }

        if (displayLeads.isNotEmpty()) {
            displayLeads.forEach { leadItem ->
                AdminNewRequestRow(
                    photoKey = photoKeyForDesktopCategory(leadItem.eventType),
                    title = leadItem.eventType,
                    subtitle = "${leadItem.clientName} · ${formatReadableSpanishDateText(leadItem.dateLabel)}",
                    onQuoteClick = { onQuoteLead(leadItem.id) },
                )
            }
        } else {
            AdminNewRequestRow(
                photoKey = RfPhotoKey.CUMPLE_INFANTIL,
                title = "Cumpleaños de Mateo",
                subtitle = "Laura Peña · Sáb, 7 de noviembre",
                onQuoteClick = onOpenFallbackBuilder,
            )
            AdminNewRequestRow(
                photoKey = RfPhotoKey.GRADUACION,
                title = "Graduación Promo 2026",
                subtitle = "Ana Gómez · Vie 13 nov",
                onQuoteClick = onOpenFallbackBuilder,
            )
        }
    }
}

@Composable
private fun AdminNewRequestRow(
    photoKey: RfPhotoKey,
    title: String,
    subtitle: String,
    onQuoteClick: () -> Unit,
) {
    val typo = LocalRfTypography.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 0.5.dp, color = Color(0xFFF2F0F5))
            .padding(horizontal = 18.dp, vertical = 12.dp),
    ) {
        RfPhoto(
            photoKey = photoKey,
            contentDescription = title,
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(10.dp)),
        )
        Column(modifier = Modifier.weight(1f)) {
            BasicText(
                text = title,
                style = typo.bodyStrong.copy(fontSize = 14.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            BasicText(
                text = subtitle,
                style = typo.secondary.copy(fontSize = 12.5.sp, color = Color(0xFF5F6377)),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .height(32.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0)),
                    ),
                )
                .clickable(onClick = onQuoteClick)
                .padding(horizontal = 12.dp),
        ) {
            BasicText(
                text = "Cotizar",
                style = typo.buttonLabel.copy(color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold),
            )
        }
    }
}

@Composable
private fun AdminAttentionTodayCard(
    eventsList: List<ClientEvent>,
    articlesList: List<CatalogArticle>,
    paymentsList: List<PaymentRecord>,
    onOpenPayments: () -> Unit,
    onOpenInventory: () -> Unit,
    onOpenCancellations: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val typo = LocalRfTypography.current
    val balanceDueEvent = eventsList.firstOrNull { eventItem -> eventItem.balanceDop > 0 }
    val unverifiedPayment = paymentsList.firstOrNull { payment -> !payment.verified }
    val lowStockArticle = articlesList.firstOrNull { article -> article.availableOct23 <= 2 }
    val cancelledCount = eventsList.count { eventItem -> eventItem.status == EventStatus.CANCELLED }

    val rows = listOf(
        A39AttentionRowData(
            title = balanceDueEvent?.let { ev -> "Saldo ${ev.title.substringBefore(' ').ifBlank { ev.title }}" } ?: "Saldo Boda Real",
            subtitle = unverifiedPayment?.let { payment -> "Pago pendiente: ${payment.methodLabel}" } ?: "Vence el jue 22 oct",
            trailingText = balanceDueEvent?.let { ev -> formatDesktopDop(ev.balanceDop) } ?: "RD$ 3,150",
            badgeText = null,
            icon = RfIconKind.WALLET,
            tileBg = Color(0xFFFCE3E8),
            tileTint = Color(0xFFB42345),
            onClick = onOpenPayments,
        ),
        A39AttentionRowData(
            title = lowStockArticle?.name ?: "Mesa dulce temática",
            subtitle = lowStockArticle?.let { article -> "Quedan ${article.availableOct23} · stock bajo" } ?: "Quedan 2 · stock bajo",
            trailingText = null,
            badgeText = "Stock bajo",
            icon = RfIconKind.CATALOG,
            tileBg = Color(0xFFFFF2C9),
            tileTint = Color(0xFF7A5700),
            onClick = onOpenInventory,
        ),
        A39AttentionRowData(
            title = if (cancelledCount > 0) "$cancelledCount cancelaciones por revisar" else "2 cancelaciones por revisar",
            subtitle = "Bautizo de Lucas · Cumpleaños...",
            trailingText = null,
            badgeText = null,
            icon = RfIconKind.CLOCK,
            tileBg = Color(0xFFECEBFF),
            tileTint = Color(0xFF4B47D6),
            onClick = onOpenCancellations,
        ),
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp)),
    ) {
        BasicText(
            text = "Atención hoy",
            style = typo.cardTitle.copy(fontSize = 16.sp, fontWeight = FontWeight.ExtraBold),
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
        )

        rows.forEach { rowData ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 0.5.dp, color = Color(0xFFF2F0F5))
                    .clickable(onClick = rowData.onClick)
                    .padding(horizontal = 18.dp, vertical = 11.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(rowData.tileBg),
                ) {
                    RfIcon(kind = rowData.icon, tint = rowData.tileTint, size = 17.dp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = rowData.title,
                        style = typo.bodyStrong.copy(fontSize = 14.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    BasicText(
                        text = rowData.subtitle,
                        style = typo.secondary.copy(fontSize = 12.5.sp, color = Color(0xFF5F6377)),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (rowData.trailingText != null) {
                    BasicText(
                        text = rowData.trailingText,
                        style = typo.cardTitle.copy(fontSize = 15.sp, fontWeight = FontWeight.ExtraBold),
                    )
                } else if (rowData.badgeText != null) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .height(24.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color(0xFFFFF2C9))
                            .padding(horizontal = 9.dp),
                    ) {
                        BasicText(
                            text = rowData.badgeText,
                            style = typo.tagLabel.copy(color = Color(0xFF7A5700), fontSize = 12.sp, fontWeight = FontWeight.Bold),
                        )
                    }
                } else {
                    BasicText(
                        text = "›",
                        style = typo.cardTitle.copy(color = Color(0xFF5F6377), fontSize = 20.sp, fontWeight = FontWeight.Bold),
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminDesktopLeadsSection(
    leadsList: List<LeadRecord>,
    searchQuery: String,
    onAdvanceStage: (LeadRecord, String) -> Unit,
    onOpenQuoteBuilder: (String) -> Unit,
    onOpenMobileRequests: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val typo = LocalRfTypography.current
    val trimmed = searchQuery.trim()
    val filtered = remember(leadsList, trimmed) {
        if (trimmed.isEmpty()) {
            leadsList
        } else {
            leadsList.filter { lead ->
                val matchClient = lead.clientName.contains(trimmed, ignoreCase = true)
                val matchType = lead.eventType.contains(trimmed, ignoreCase = true)
                val matchStage = lead.stage.contains(trimmed, ignoreCase = true)
                listOf(matchClient, matchType, matchStage).any { matched -> matched }
            }
        }
    }

    DesktopSectionCardContainer(
        title = "Bandeja de solicitudes (${filtered.size})",
        actionLabel = "Abrir vista móvil",
        onAction = onOpenMobileRequests,
        modifier = modifier,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
            filtered.forEach { lead ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFF9F9FD))
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(14.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        BasicText(
                            text = "${lead.eventType} · ${lead.clientName}",
                            style = typo.bodyStrong.copy(fontSize = 15.sp),
                        )
                        BasicText(
                            text = "${formatReadableSpanishDateText(lead.dateLabel)} · ${lead.guests} invitados · ${formatDesktopDop(lead.estimatedDop)} · Canal ${lead.channel}",
                            style = typo.secondary.copy(fontSize = 12.5.sp, color = Color(0xFF5F6377)),
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        RfTag(text = lead.stage, tone = RfTagTone.INDIGO)
                        val nextStage = when {
                            lead.stage.equals("Nueva", ignoreCase = true) -> "Contactada"
                            lead.stage.equals("Contactada", ignoreCase = true) -> "Cotizada"
                            else -> "Cerrada"
                        }
                        DesktopSmallPillButton(
                            label = "Mover a $nextStage",
                            onClick = { onAdvanceStage(lead, nextStage) },
                        )
                        DesktopGradientPillButton(
                            label = "Cotizar",
                            onClick = { onOpenQuoteBuilder(lead.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminDesktopEventsSection(
    eventsList: List<ClientEvent>,
    searchQuery: String,
    onOpenEventDetail: (String) -> Unit,
    onDuplicateEvent: (String) -> Unit,
    onOpenMobileEvents: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val typo = LocalRfTypography.current
    val trimmed = searchQuery.trim()
    val filtered = remember(eventsList, trimmed) {
        if (trimmed.isEmpty()) {
            eventsList
        } else {
            eventsList.filter { eventItem ->
                val matchTitle = eventItem.title.contains(trimmed, ignoreCase = true)
                val matchClient = eventItem.clientName.contains(trimmed, ignoreCase = true)
                val matchVenue = eventItem.venue.contains(trimmed, ignoreCase = true)
                listOf(matchTitle, matchClient, matchVenue).any { matched -> matched }
            }
        }
    }

    DesktopSectionCardContainer(
        title = "Eventos programados (${filtered.size})",
        actionLabel = "Ver agenda completa",
        onAction = onOpenMobileEvents,
        modifier = modifier,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
            filtered.forEach { eventItem ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFF9F9FD))
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(14.dp))
                        .clickable { onOpenEventDetail(eventItem.id) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    RfPhoto(
                        photoKey = photoKeyForDesktopEvent(eventItem.photo),
                        contentDescription = eventItem.title,
                        modifier = Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        BasicText(text = eventItem.title, style = typo.bodyStrong.copy(fontSize = 14.5.sp))
                        BasicText(
                            text = "${formatReadableSpanishDateText(eventItem.dateLabel)} · ${eventItem.clientName} · ${eventItem.venue} · ${eventItem.guests} inv.",
                            style = typo.secondary.copy(fontSize = 12.5.sp, color = Color(0xFF5F6377)),
                        )
                    }
                    BasicText(
                        text = formatDesktopDop(eventItem.totalDop),
                        style = typo.cardTitle.copy(fontSize = 14.5.sp, fontWeight = FontWeight.Bold),
                    )
                    RfTag(text = eventItem.status.label, tone = RfTagTone.TEAL)
                    DesktopSmallPillButton(
                        label = "Duplicar",
                        onClick = { onDuplicateEvent(eventItem.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminDesktopInventorySection(
    articlesList: List<CatalogArticle>,
    searchQuery: String,
    onAdjustStock: (String, Int) -> Unit,
    onOpenMobileInventory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val typo = LocalRfTypography.current
    val trimmed = searchQuery.trim()
    val filtered = remember(articlesList, trimmed) {
        if (trimmed.isEmpty()) {
            articlesList
        } else {
            articlesList.filter { article ->
                val matchName = article.name.contains(trimmed, ignoreCase = true)
                val matchCategory = article.category.contains(trimmed, ignoreCase = true)
                listOf(matchName, matchCategory).any { matched -> matched }
            }
        }
    }

    DesktopSectionCardContainer(
        title = "Inventario y disponibilidad (${filtered.size})",
        actionLabel = "Catálogo admin",
        onAction = onOpenMobileInventory,
        modifier = modifier,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
            filtered.forEach { article ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFF9F9FD))
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(14.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                ) {
                    RfPhoto(
                        photoKey = photoKeyForDesktopEvent(article.photo),
                        contentDescription = article.name,
                        modifier = Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        BasicText(text = article.name, style = typo.bodyStrong.copy(fontSize = 14.5.sp))
                        BasicText(
                            text = "${article.category} · ${formatDesktopDop(article.pricePerDayDop)}/día · Disp. Oct 23: ${article.availableOct23}/${article.totalStock}",
                            style = typo.secondary.copy(fontSize = 12.5.sp, color = Color(0xFF5F6377)),
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        DesktopSmallPillButton(label = "-1", onClick = { onAdjustStock(article.id, -1) })
                        BasicText(
                            text = article.totalStock.toString(),
                            style = typo.bodyStrong.copy(fontSize = 14.sp),
                            modifier = Modifier.padding(horizontal = 6.dp),
                        )
                        DesktopSmallPillButton(label = "+1", onClick = { onAdjustStock(article.id, 1) })
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminDesktopQuotesSection(
    quotesList: List<QuoteRecord>,
    searchQuery: String,
    onApproveQuote: (QuoteRecord) -> Unit,
    onDeclineQuote: (QuoteRecord) -> Unit,
    onDuplicateQuote: (QuoteRecord) -> Unit,
    onOpenBuilder: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val typo = LocalRfTypography.current
    val trimmed = searchQuery.trim()
    val filtered = remember(quotesList, trimmed) {
        if (trimmed.isEmpty()) {
            quotesList
        } else {
            quotesList.filter { quote ->
                val matchTitle = quote.eventTitle.contains(trimmed, ignoreCase = true)
                val matchClient = quote.clientName.contains(trimmed, ignoreCase = true)
                val matchCode = quote.code.contains(trimmed, ignoreCase = true)
                listOf(matchTitle, matchClient, matchCode).any { matched -> matched }
            }
        }
    }

    DesktopSectionCardContainer(
        title = "Cotizaciones activas (${filtered.size})",
        actionLabel = "Constructor cotizaciones",
        onAction = onOpenBuilder,
        modifier = modifier,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
            filtered.forEach { quote ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFF9F9FD))
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(14.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        BasicText(text = "${quote.code} · ${quote.eventTitle}", style = typo.bodyStrong.copy(fontSize = 14.5.sp))
                        BasicText(
                            text = "${quote.clientName} · ${formatReadableSpanishDateText(quote.dateLabel)} · Total ${formatDesktopDop(quote.totalDop)}",
                            style = typo.secondary.copy(fontSize = 12.5.sp, color = Color(0xFF5F6377)),
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        RfTag(text = quote.status, tone = RfTagTone.SOL)
                        DesktopGradientPillButton(label = "Aprobar", onClick = { onApproveQuote(quote) })
                        DesktopSmallPillButton(label = "Duplicar", onClick = { onDuplicateQuote(quote) })
                        DesktopSmallPillButton(label = "Rechazar", onClick = { onDeclineQuote(quote) })
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminDesktopPaymentsSection(
    canViewPayments: Boolean,
    paymentsList: List<PaymentRecord>,
    eventsList: List<ClientEvent>,
    searchQuery: String,
    onExportReceipt: (ClientEvent, PaymentRecord) -> Unit,
    onRegisterQuickPayment: (String, String, String, String) -> Unit,
    onOpenMobilePayments: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val typo = LocalRfTypography.current
    if (!canViewPayments) {
        DesktopSectionCardContainer(
            title = "Acceso restringido",
            actionLabel = "Gestionar equipo",
            onAction = onOpenMobilePayments,
            modifier = modifier,
        ) {
            BasicText(
                text = "Tu rol actual no tiene permiso para visualizar cobros ni registrar pagos manuales.",
                style = typo.body.copy(fontSize = 14.5.sp, color = Color(0xFFB42345)),
            )
        }
        return
    }

    var amountInput by remember { mutableStateOf("1500") }
    var referenceInput by remember { mutableStateOf("TRF-8841") }
    val trimmed = searchQuery.trim()
    val filtered = remember(paymentsList, trimmed) {
        if (trimmed.isEmpty()) {
            paymentsList
        } else {
            paymentsList.filter { payment ->
                val matchEvent = payment.eventTitle.contains(trimmed, ignoreCase = true)
                val matchClient = payment.clientName.contains(trimmed, ignoreCase = true)
                val matchReceipt = payment.receiptNumber.contains(trimmed, ignoreCase = true)
                listOf(matchEvent, matchClient, matchReceipt).any { matched -> matched }
            }
        }
    }

    DesktopSectionCardContainer(
        title = "Cobros y transferencias (${filtered.size})",
        actionLabel = "Registro completo",
        onAction = onOpenMobilePayments,
        modifier = modifier,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF3F2FE))
                    .padding(12.dp),
            ) {
                BasicText("Registro rápido:", style = typo.bodyStrong.copy(fontSize = 13.5.sp))
                DesktopInlineInput(value = amountInput, placeholder = "Monto", onValueChange = { next -> amountInput = next }, modifier = Modifier.width(120.dp))
                DesktopInlineInput(value = referenceInput, placeholder = "Referencia", onValueChange = { next -> referenceInput = next }, modifier = Modifier.width(180.dp))
                val targetEvent = eventsList.firstOrNull { ev -> ev.balanceDop > 0 } ?: eventsList.firstOrNull()
                DesktopGradientPillButton(
                    label = "Registrar pago",
                    onClick = {
                        if (targetEvent != null) {
                            onRegisterQuickPayment(targetEvent.id, amountInput, "Transferencia bancaria", referenceInput)
                        }
                    },
                )
            }

            filtered.forEach { payment ->
                val matchingEvent = eventsList.firstOrNull { ev -> ev.id == payment.eventId } ?: eventsList.firstOrNull()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFF9F9FD))
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(14.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        BasicText(text = "${payment.receiptNumber} · ${payment.eventTitle}", style = typo.bodyStrong.copy(fontSize = 14.5.sp))
                        BasicText(
                            text = "${payment.clientName} · ${payment.methodLabel} · ${formatReadableSpanishDateText(payment.dateLabel)} · Ref ${payment.referenceCode}",
                            style = typo.secondary.copy(fontSize = 12.5.sp, color = Color(0xFF5F6377)),
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        BasicText(text = formatDesktopDop(payment.amountDop), style = typo.cardTitle.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold))
                        RfTag(text = if (payment.verified) "Verificado" else "En revisión", tone = if (payment.verified) RfTagTone.LIMA else RfTagTone.SOL)
                        if (matchingEvent != null) {
                            DesktopSmallPillButton(
                                label = "PDF",
                                onClick = { onExportReceipt(matchingEvent, payment) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminDesktopClientsSection(
    clientsList: List<ClientProfile>,
    searchQuery: String,
    onSelectClient: (String) -> Unit,
    onOpenChats: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val typo = LocalRfTypography.current
    val trimmed = searchQuery.trim()
    val filtered = remember(clientsList, trimmed) {
        if (trimmed.isEmpty()) {
            clientsList
        } else {
            clientsList.filter { client ->
                val matchName = client.fullName.contains(trimmed, ignoreCase = true)
                val matchZone = client.zone.contains(trimmed, ignoreCase = true)
                val matchEmail = client.email.contains(trimmed, ignoreCase = true)
                listOf(matchName, matchZone, matchEmail).any { matched -> matched }
            }
        }
    }

    DesktopSectionCardContainer(
        title = "Directorio de clientes (${filtered.size})",
        actionLabel = "Ir a chats",
        onAction = onOpenChats,
        modifier = modifier,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
            filtered.forEach { client ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFF9F9FD))
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(14.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        BasicText(text = client.fullName, style = typo.bodyStrong.copy(fontSize = 14.5.sp))
                        BasicText(
                            text = "${client.email} · ${client.phone} · ${client.zone} · ${client.eventsCount} eventos",
                            style = typo.secondary.copy(fontSize = 12.5.sp, color = Color(0xFF5F6377)),
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        BasicText(text = formatDesktopDop(client.totalContractedDop), style = typo.bodyStrong.copy(fontSize = 14.sp))
                        RfTag(text = client.vipTag, tone = RfTagTone.INDIGO)
                        DesktopSmallPillButton(label = "Ficha CRM", onClick = { onSelectClient(client.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminDesktopChatsSection(
    threads: List<ChatThread>,
    selectedThreadId: String,
    messages: List<com.jeudry.rosafiesta.domain.ChatMessageRecord>,
    onSelectThread: (String) -> Unit,
    onSendReply: (String) -> Unit,
    onOpenMobileChats: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val typo = LocalRfTypography.current
    var draftMessage by remember { mutableStateOf("") }

    DesktopSectionCardContainer(
        title = "Mensajería centralizada (${threads.size} hilos)",
        actionLabel = "Abrir chat completo",
        onAction = onOpenMobileChats,
        modifier = modifier,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxSize()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.width(300.dp).fillMaxHeight().verticalScroll(rememberScrollState()),
            ) {
                threads.forEach { thread ->
                    val isSelected = thread.id == selectedThreadId
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0xFFECEBFF) else Color(0xFFF9F9FD))
                            .border(1.dp, if (isSelected) Color(0xFF7C5CF0) else Color(0xFFECECF5), RoundedCornerShape(12.dp))
                            .clickable { onSelectThread(thread.id) }
                            .padding(12.dp),
                    ) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            BasicText(text = thread.contactName, style = typo.bodyStrong.copy(fontSize = 14.sp))
                            BasicText(text = thread.timestampLabel, style = typo.caption.copy(fontSize = 11.5.sp, color = Color(0xFF5F6377)))
                        }
                        BasicText(text = thread.eventTitle, style = typo.caption.copy(fontSize = 12.sp, color = Color(0xFF4B47D6)))
                        BasicText(text = thread.lastMessage, maxLines = 1, overflow = TextOverflow.Ellipsis, style = typo.secondary.copy(fontSize = 12.5.sp))
                    }
                }
            }

            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF9F9FD))
                    .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(14.dp))
                    .padding(16.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    messages.forEach { msg ->
                        val isStaffBubble = msg.senderRole.equals("Admin", ignoreCase = true) ||
                            msg.senderRole.equals("Staff", ignoreCase = true) ||
                            msg.senderRole.equals("Equipo", ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isStaffBubble) Color(0xFFECEBFF) else Color.White)
                                .padding(10.dp),
                        ) {
                            Column {
                                BasicText(text = "${msg.senderName} · ${msg.timestampLabel}", style = typo.caption.copy(fontSize = 11.5.sp, fontWeight = FontWeight.Bold))
                                BasicText(text = msg.body, style = typo.body.copy(fontSize = 13.5.sp))
                            }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    DesktopInlineInput(
                        value = draftMessage,
                        placeholder = "Responder al cliente...",
                        onValueChange = { updated -> draftMessage = updated },
                        modifier = Modifier.weight(1f),
                    )
                    DesktopGradientPillButton(
                        label = "Enviar",
                        onClick = {
                            if (draftMessage.isNotBlank()) {
                                onSendReply(draftMessage)
                                draftMessage = ""
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminDesktopReportsSection(
    metrics: com.jeudry.rosafiesta.domain.BusinessMetrics,
    paymentsCount: Int,
    staffMembers: List<StaffMember>,
    canViewPayments: Boolean,
    onExportIncomeCsv: () -> Unit,
    onExportDgii607: () -> Unit,
    onToggleStaffPaymentAccess: (String) -> Unit,
    onOpenDetailedAnalytics: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val typo = LocalRfTypography.current
    DesktopSectionCardContainer(
        title = "Reportes financieros y fiscales",
        actionLabel = "Ver analítica detallada",
        onAction = onOpenDetailedAnalytics,
        modifier = modifier,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                DesktopGradientPillButton(label = "Exportar CSV de ingresos", onClick = onExportIncomeCsv)
                DesktopGradientPillButton(label = "Exportar DGII 607 (.csv)", onClick = onExportDgii607)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                BasicText(
                    text = "Ingresos ${metrics.monthLabel}: ${formatDesktopDop(metrics.octoberRevenueDop)} · Por cobrar: ${formatDesktopDop(metrics.accountsReceivableDop)} · Ocupación: ${metrics.occupancyRatePercent}% · Transacciones: $paymentsCount",
                    style = typo.bodyStrong.copy(fontSize = 14.5.sp),
                )
            }

            BasicText(text = "Control de permisos del equipo", style = typo.cardTitle.copy(fontSize = 15.5.sp, fontWeight = FontWeight.Bold))
            staffMembers.forEach { member ->
                val memberHasPayments = member.permissions.contains(StaffPermission.PAYMENTS)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF9F9FD))
                        .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                ) {
                    Column {
                        BasicText(text = "${member.name} · ${member.roleLabel}", style = typo.bodyStrong.copy(fontSize = 14.sp))
                        BasicText(
                            text = "Acceso financiero: ${if (memberHasPayments) "Habilitado" else "Restringido"} · ${member.activeEventsCount} eventos",
                            style = typo.secondary.copy(fontSize = 12.5.sp, color = Color(0xFF5F6377)),
                        )
                    }
                    if (canViewPayments) {
                        RfSwitch(
                            checked = memberHasPayments,
                            onCheckedChange = { onToggleStaffPaymentAccess(member.name) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminDesktopSettingsSection(
    viewModel: RosaFiestaViewModel?,
    initialSettings: BusinessSettings,
    onSaveSettings: (BusinessSettings) -> Unit,
    onOpenMobileSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val typo = LocalRfTypography.current
    var businessName by remember(initialSettings) { mutableStateOf(initialSettings.businessName) }
    var rnc by remember(initialSettings) { mutableStateOf(initialSettings.rnc) }
    var address by remember(initialSettings) { mutableStateOf(initialSettings.address) }
    var email by remember(initialSettings) { mutableStateOf(initialSettings.email) }
    var phone by remember(initialSettings) { mutableStateOf(initialSettings.phone) }

    DesktopSectionCardContainer(
        title = "Configuración comercial y fiscal",
        actionLabel = "Ajustes avanzados",
        onAction = onOpenMobileSettings,
        modifier = modifier,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
            BasicText(text = "Razón social", style = typo.bodyStrong.copy(fontSize = 13.5.sp))
            DesktopInlineInput(value = businessName, placeholder = "Nombre del negocio", onValueChange = { next -> businessName = next }, modifier = Modifier.fillMaxWidth())

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    BasicText(text = "RNC", style = typo.bodyStrong.copy(fontSize = 13.5.sp))
                    DesktopInlineInput(value = rnc, placeholder = "1-31-84920-2", onValueChange = { next -> rnc = next }, modifier = Modifier.fillMaxWidth())
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    BasicText(text = "Teléfono", style = typo.bodyStrong.copy(fontSize = 13.5.sp))
                    DesktopInlineInput(value = phone, placeholder = "+1809...", onValueChange = { next -> phone = next }, modifier = Modifier.fillMaxWidth())
                }
            }

            BasicText(text = "Correo administrativo", style = typo.bodyStrong.copy(fontSize = 13.5.sp))
            DesktopInlineInput(value = email, placeholder = "hola@rosafiesta.do", onValueChange = { next -> email = next }, modifier = Modifier.fillMaxWidth())

            BasicText(text = "Dirección física", style = typo.bodyStrong.copy(fontSize = 13.5.sp))
            DesktopInlineInput(value = address, placeholder = "Dirección comercial", onValueChange = { next -> address = next }, modifier = Modifier.fillMaxWidth())

            DesktopGradientPillButton(
                label = "Guardar configuración",
                onClick = {
                    onSaveSettings(
                        initialSettings.copy(
                            businessName = businessName.trim(),
                            rnc = rnc.trim(),
                            phone = phone.trim(),
                            email = email.trim(),
                            address = address.trim(),
                        ),
                    )
                },
            )

            Spacer(Modifier.height(6.dp))
            BasicText(text = "Audio y efectos de sonido", style = typo.bodyStrong.copy(fontSize = 14.sp))
            AppAudioSettingsCard(
                viewModel = viewModel,
                isEnglish = false,
                isAdminSurface = true,
            )
        }
    }
}

@Composable
private fun AdminDesktopModalHost(
    activeModal: A39DesktopModal,
    viewModel: RosaFiestaViewModel?,
    notifications: List<NotificationRecord>,
    selectedEvent: ClientEvent?,
    onDismiss: () -> Unit,
    onMarkNotificationRead: (String) -> Unit,
    onSelectScreenCode: (String) -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x66151729))
            .padding(24.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .width(560.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(22.dp))
                .padding(24.dp),
        ) {
            when (activeModal) {
                A39DesktopModal.NOTIFICATIONS -> {
                    AdminDesktopNotificationsModal(
                        notifications = notifications,
                        onMarkNotificationRead = onMarkNotificationRead,
                        onOpenFullCenter = { onSelectScreenCode("A29") },
                        onDismiss = onDismiss,
                    )
                }

                A39DesktopModal.ROSA_AI -> {
                    AdminDesktopRosaAiModal(
                        viewModel = viewModel,
                        onOpenFullScreen = { onSelectScreenCode("A08") },
                        onDismiss = onDismiss,
                    )
                }

                A39DesktopModal.NEW_EVENT -> {
                    AdminDesktopNewEventModal(
                        viewModel = viewModel,
                        onOpenMobileWizard = { onSelectScreenCode("A04") },
                        onDismiss = onDismiss,
                    )
                }

                A39DesktopModal.EVENT_DETAIL -> {
                    AdminDesktopEventDetailModal(
                        selectedEvent = selectedEvent,
                        viewModel = viewModel,
                        onOpenFullDetail = { onSelectScreenCode("A13") },
                        onDismiss = onDismiss,
                    )
                }

                A39DesktopModal.NONE -> Unit
            }
        }
    }
}

@Composable
private fun AdminDesktopNotificationsModal(
    notifications: List<NotificationRecord>,
    onMarkNotificationRead: (String) -> Unit,
    onOpenFullCenter: () -> Unit,
    onDismiss: () -> Unit,
) {
    val typo = LocalRfTypography.current
    Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        BasicText(text = "Notificaciones recientes", style = typo.cardTitle.copy(fontSize = 18.sp, fontWeight = FontWeight.ExtraBold))
        DesktopSmallPillButton(label = "Cerrar", onClick = onDismiss)
    }
    if (notifications.isEmpty()) {
        BasicText(text = "No tienes alertas pendientes en este momento.", style = typo.body.copy(fontSize = 14.sp, color = Color(0xFF5F6377)))
    } else {
        notifications.take(5).forEach { notif ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (notif.isRead) Color(0xFFF9F9FD) else Color(0xFFECEBFF))
                    .padding(12.dp),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    BasicText(text = notif.title, style = typo.bodyStrong.copy(fontSize = 14.sp))
                    BasicText(text = "${notif.body} · ${notif.createdAtLabel}", style = typo.secondary.copy(fontSize = 12.5.sp))
                }
                if (!notif.isRead) {
                    DesktopSmallPillButton(label = "Leída", onClick = { onMarkNotificationRead(notif.id) })
                }
            }
        }
    }
    DesktopGradientPillButton(label = "Abrir centro A29", onClick = onOpenFullCenter)
}

@Composable
private fun AdminDesktopRosaAiModal(
    viewModel: RosaFiestaViewModel?,
    onOpenFullScreen: () -> Unit,
    onDismiss: () -> Unit,
) {
    val typo = LocalRfTypography.current
    val rosaReply = viewModel?.rosaProposalMessage?.collectAsState()?.value
    var promptText by remember { mutableStateOf("Boda romántica para 120 personas en Jardín Botánico") }

    Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        BasicText(text = "Crear propuesta con Rosa IA", style = typo.cardTitle.copy(fontSize = 18.sp, fontWeight = FontWeight.ExtraBold))
        DesktopSmallPillButton(label = "Cerrar", onClick = onDismiss)
    }
    DesktopInlineInput(
        value = promptText,
        placeholder = "Describe el evento, fecha e invitados...",
        onValueChange = { updated -> promptText = updated },
        modifier = Modifier.fillMaxWidth(),
    )
    if (!rosaReply.isNullOrBlank()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF3F2FE))
                .padding(12.dp),
        ) {
            BasicText(text = rosaReply, style = typo.body.copy(fontSize = 13.5.sp))
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        DesktopGradientPillButton(
            label = "Generar con Rosa",
            onClick = { viewModel?.sendConversationalRosaPrompt(promptText) },
        )
        DesktopSmallPillButton(label = "Abrir pantalla A08", onClick = onOpenFullScreen)
    }
}

@Composable
private fun AdminDesktopNewEventModal(
    viewModel: RosaFiestaViewModel?,
    onOpenMobileWizard: () -> Unit,
    onDismiss: () -> Unit,
) {
    val typo = LocalRfTypography.current
    val title = viewModel?.newEventTitle?.collectAsState()?.value ?: ""
    val client = viewModel?.newEventClient?.collectAsState()?.value ?: ""
    val date = viewModel?.newEventDate?.collectAsState()?.value ?: ""
    val venue = viewModel?.newEventVenue?.collectAsState()?.value ?: ""
    val guests = viewModel?.newEventGuests?.collectAsState()?.value ?: ""

    Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        BasicText(text = "Crear nuevo evento", style = typo.cardTitle.copy(fontSize = 18.sp, fontWeight = FontWeight.ExtraBold))
        DesktopSmallPillButton(label = "Cerrar", onClick = onDismiss)
    }
    DesktopInlineInput(value = title, placeholder = "Título del evento", onValueChange = { updated -> viewModel?.setNewEventTitle(updated) }, modifier = Modifier.fillMaxWidth())
    DesktopInlineInput(value = client, placeholder = "Nombre del cliente", onValueChange = { updated -> viewModel?.setNewEventClient(updated) }, modifier = Modifier.fillMaxWidth())
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        DesktopInlineInput(value = date, placeholder = "Fecha (ej. Sáb, 14 de noviembre 2026)", onValueChange = { updated -> viewModel?.setNewEventDate(updated) }, modifier = Modifier.weight(1f))
        DesktopInlineInput(value = guests, placeholder = "Invitados", onValueChange = { updated -> viewModel?.setNewEventGuests(updated) }, modifier = Modifier.width(120.dp))
    }
    DesktopInlineInput(value = venue, placeholder = "Lugar o salón", onValueChange = { updated -> viewModel?.setNewEventVenue(updated) }, modifier = Modifier.fillMaxWidth())
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        DesktopGradientPillButton(
            label = "Guardar evento",
            onClick = {
                val createdOk = viewModel?.submitNewEvent() ?: true
                if (createdOk) {
                    onDismiss()
                }
            },
        )
        DesktopSmallPillButton(label = "Asistente completo A04", onClick = onOpenMobileWizard)
    }
}

@Composable
private fun AdminDesktopEventDetailModal(
    selectedEvent: ClientEvent?,
    viewModel: RosaFiestaViewModel?,
    onOpenFullDetail: () -> Unit,
    onDismiss: () -> Unit,
) {
    val typo = LocalRfTypography.current
    Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        BasicText(text = selectedEvent?.title ?: "Detalle de evento", style = typo.cardTitle.copy(fontSize = 18.sp, fontWeight = FontWeight.ExtraBold))
        DesktopSmallPillButton(label = "Cerrar", onClick = onDismiss)
    }
    if (selectedEvent != null) {
        BasicText(
            text = "Código: ${selectedEvent.code} · Cliente: ${selectedEvent.clientName} · Fecha: ${formatReadableSpanishDateText(selectedEvent.dateLabel)}",
            style = typo.bodyStrong.copy(fontSize = 14.sp),
        )
        BasicText(
            text = "Lugar: ${selectedEvent.venue} · ${selectedEvent.guests} invitados · Total: ${formatDesktopDop(selectedEvent.totalDop)} · Pagado: ${formatDesktopDop(selectedEvent.paidDepositDop)}",
            style = typo.secondary.copy(fontSize = 13.5.sp, color = Color(0xFF5F6377)),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DesktopGradientPillButton(
                label = "Descargar recibo PDF",
                onClick = {
                    viewModel?.exportReceiptDocument(event = selectedEvent, payment = null, shareInsteadOfSave = false)
                },
            )
            DesktopSmallPillButton(
                label = "Duplicar",
                onClick = {
                    viewModel?.duplicateExistingEvent(selectedEvent.id)
                    onDismiss()
                },
            )
            DesktopSmallPillButton(label = "Abrir ficha A13", onClick = onOpenFullDetail)
        }
    }
}

@Composable
private fun DesktopSectionCardContainer(
    title: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val typo = LocalRfTypography.current
    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier
            .fillMaxWidth()
            .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFECECF5), RoundedCornerShape(20.dp))
            .padding(20.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            BasicText(text = title, style = typo.cardTitle.copy(fontSize = 17.sp, fontWeight = FontWeight.ExtraBold))
            DesktopSmallPillButton(label = actionLabel, onClick = onAction)
        }
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            content()
        }
    }
}

@Composable
private fun DesktopInlineInput(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val typo = LocalRfTypography.current
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE3E4EF), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp),
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = typo.body.copy(fontSize = 13.5.sp, color = Color(0xFF151729)),
            cursorBrush = SolidColor(Color(0xFF5B6CF0)),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    BasicText(text = placeholder, style = typo.secondary.copy(fontSize = 13.5.sp, color = Color(0xFF8C90A6)))
                }
                inner()
            },
        )
    }
}

@Composable
private fun DesktopGradientPillButton(
    label: String,
    onClick: () -> Unit,
) {
    val typo = LocalRfTypography.current
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .height(34.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
    ) {
        BasicText(text = label, style = typo.buttonLabel.copy(color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Bold))
    }
}

@Composable
private fun DesktopSmallPillButton(
    label: String,
    onClick: () -> Unit,
) {
    val typo = LocalRfTypography.current
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .height(34.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(Color(0xFFF2F2F8))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
    ) {
        BasicText(text = label, style = typo.buttonLabel.copy(color = Color(0xFF3A3D52), fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold))
    }
}

private fun formatDesktopDop(amount: Int): String {
    if (amount < 1000) {
        return "RD$ $amount"
    }
    val thousands = amount / 1000
    val remainder = (amount % 1000).toString().padStart(3, '0')
    return "RD$ $thousands,$remainder"
}

private fun formatShortK(amountDop: Int): String {
    if (amountDop <= 0) {
        return "0k"
    }
    val wholeK = amountDop / 1000
    val remHundreds = (amountDop % 1000) / 10
    val suffix = remHundreds.toString().padStart(2, '0').trimEnd('0')
    return if (suffix.isEmpty()) "${wholeK}k" else "$wholeK.${suffix}k"
}

private fun formatCompactClientName(fullName: String): String {
    val parts = fullName.trim().split(' ').filter { part -> part.isNotBlank() }
    if (parts.size < 2) {
        return fullName.ifBlank { "Cliente" }
    }
    return if (parts.first() == "Camila") {
        "Camila ${parts.last().first()}."
    } else {
        "${parts.first()} ${parts.last()}"
    }
}

private fun photoKeyForDesktopEvent(photo: PhotoAsset): RfPhotoKey = when (photo) {
    PhotoAsset.SILLA_TIFFANY -> RfPhotoKey.SILLA_TIFFANY
    PhotoAsset.MESA_PLEGABLE -> RfPhotoKey.MESA_PLEGABLE
    PhotoAsset.MESA_REDONDA -> RfPhotoKey.MESA_REDONDA
    PhotoAsset.CENTRO_FLORAL -> RfPhotoKey.CENTRO_FLORAL
    PhotoAsset.ARCO_ROSA -> RfPhotoKey.ARCO_ROSA
    PhotoAsset.MESA_HELADOS -> RfPhotoKey.MESA_HELADOS
    PhotoAsset.BODA -> RfPhotoKey.BODA
    PhotoAsset.QUINCEANERA -> RfPhotoKey.QUINCEANERA
    PhotoAsset.CUMPLE_INFANTIL -> RfPhotoKey.CUMPLE_INFANTIL
    PhotoAsset.BABY_SHOWER -> RfPhotoKey.BABY_SHOWER
    PhotoAsset.GRADUACION -> RfPhotoKey.GRADUACION
    PhotoAsset.CORPORATIVO -> RfPhotoKey.CORPORATIVO
}

private fun photoKeyForDesktopCategory(categoryOrTitle: String): RfPhotoKey {
    val lower = categoryOrTitle.lowercase()
    return when {
        lower.contains("boda") -> RfPhotoKey.BODA
        lower.contains("quince") -> RfPhotoKey.QUINCEANERA
        lower.contains("baby") -> RfPhotoKey.BABY_SHOWER
        lower.contains("corp") -> RfPhotoKey.CORPORATIVO
        lower.contains("grad") -> RfPhotoKey.GRADUACION
        else -> RfPhotoKey.CUMPLE_INFANTIL
    }
}
