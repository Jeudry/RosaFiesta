package com.jeudry.rosafiesta.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
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
import com.jeudry.rosafiesta.designsystem.rfClearFocusOnOutsidePress
import com.jeudry.rosafiesta.designsystem.rfIconButtonShadow
import com.jeudry.rosafiesta.designsystem.rfTrackValidationFocus
import com.jeudry.rosafiesta.domain.BusinessSettings
import com.jeudry.rosafiesta.domain.CanonicalSampleData
import com.jeudry.rosafiesta.domain.CatalogArticle
import com.jeudry.rosafiesta.domain.ChatThread
import com.jeudry.rosafiesta.domain.ClientEvent
import com.jeudry.rosafiesta.domain.ClientProfile
import com.jeudry.rosafiesta.domain.DomainClockFormatter
import com.jeudry.rosafiesta.domain.EventStatus
import com.jeudry.rosafiesta.domain.LeadRecord
import com.jeudry.rosafiesta.domain.NotificationRecord
import com.jeudry.rosafiesta.domain.PaymentRecord
import com.jeudry.rosafiesta.domain.PaymentsAnalyticsSummary
import com.jeudry.rosafiesta.domain.PhotoAsset
import com.jeudry.rosafiesta.domain.QuoteRecord
import com.jeudry.rosafiesta.domain.StaffMember
import com.jeudry.rosafiesta.domain.validation.InputValidator
import com.jeudry.rosafiesta.domain.validation.MaskedTextCursorResult

private fun firstDesktopValidationError(vararg errors: String?): String? = errors.firstOrNull { message -> !message.isNullOrBlank() }

private fun validateDesktopEventDateText(rawDate: String): String? {
    val trimmed = rawDate.trim()
    if (trimmed.isEmpty()) {
        return "La fecha del evento es obligatoria."
    }
    val hasSlashDate = trimmed.contains("/")
    if (hasSlashDate) {
        return InputValidator.validateEventDate(trimmed, isRequired = true)
    }
    val lower = trimmed.lowercase()
    val spanishMonths = listOf(
        "ene", "feb", "mar", "abr", "may", "jun",
        "jul", "ago", "sep", "oct", "nov", "dic",
    )
    val hasMonth = spanishMonths.any { monthToken -> lower.contains(monthToken) }
    val hasDayDigit = trimmed.any { character -> character.isDigit() }
    val hasSpanishLabel = if (hasMonth) hasDayDigit else false
    if (hasSpanishLabel) {
        return null
    }
    return InputValidator.validateEventDate(trimmed, isRequired = true)
}

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
            Box(modifier = Modifier.fillMaxSize().rfClearFocusOnOutsidePress()) {
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
                        onOpenScreenCode = onSelectScreenCode,
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
                                    viewModel.exportReportCsv(
                                        reportType = "income",
                                        period = DomainClockFormatter.currentYearMonthIso(),
                                    )
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
                                        onSelectScreenCode("A03")
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
                                        onSelectScreenCode("A03")
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
                                    isSubmitting = viewModel?.isSubmitting?.collectAsState()?.value ?: false,
                                    onAdjustStock = { articleId, delta ->
                                        viewModel?.adjustArticleStock(articleId = articleId, delta = delta)
                                    },
                                    onDeleteArticle = { articleId, onDone ->
                                        if (viewModel != null) {
                                            viewModel.deleteArticle(articleId, onDone)
                                        } else {
                                            onDone(Result.success(Unit))
                                        }
                                    },
                                    onOpenMobileInventory = { onSelectScreenCode("A09") },
                                    onOpenArticleEditor = { articleId ->
                                        viewModel?.startEditingArticle(articleId)
                                        onSelectScreenCode("A10")
                                    },
                                    onOpenCategories = { onSelectScreenCode("A40") },
                                    onOpenTrends = { onSelectScreenCode("A41") },
                                    viewModel = viewModel,
                                    modifier = Modifier.weight(1f),
                                )
                            }

                            A39DesktopSection.COTIZACIONES -> {
                                AdminDesktopQuotesSection(
                                    quotesList = quotesList,
                                    searchQuery = desktopSearchQuery,
                                    onEnrichQuote = { quoteItem ->
                                        viewModel?.selectQuote(quoteItem.id)
                                        onSelectScreenCode("A03")
                                    },
                                    onApproveAndSendQuote = { quoteItem ->
                                        viewModel?.approveAndSendQuoteFromAdmin(quoteId = quoteItem.id)
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
                                    onVerifyPayment = { paymentId ->
                                        viewModel?.verifyPendingPayment(paymentId)
                                    },
                                    onRejectPayment = { paymentId ->
                                        viewModel?.rejectPendingPayment(
                                            paymentId = paymentId,
                                            reason = "Rechazado desde panel administrativo",
                                        )
                                    },
                                    onOpenTeamManagement = { onSelectScreenCode("A28") },
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
                                            viewModel.exportReportCsv(
                                                reportType = "income",
                                                period = DomainClockFormatter.currentYearMonthIso(),
                                            )
                                        } else {
                                            localStatusMessage = "CSV de ingresos generado."
                                        }
                                    },
                                    onExportDgii607 = {
                                        if (viewModel != null) {
                                            viewModel.exportReportCsv(
                                                reportType = "dgii607",
                                                period = DomainClockFormatter.currentYearMonthIso(),
                                            )
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
    onOpenScreenCode: (String) -> Unit,
    onSwitchToMobile: () -> Unit,
) {
    val typo = LocalRfTypography.current
    val palette = LocalRfPalette.current
    val pillIndigoBg = if (palette.isDark) Color(0xFF2B2852) else Color(0xFFECEBFF)
    val pillIndigoInk = if (palette.isDark) Color(0xFFA7A3FF) else Color(0xFF4B47D6)
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
    val moduleShortcuts = listOf(
        Triple("Paquetes", RfIconKind.GIFT, "A24"),
        Triple("Equipo", RfIconKind.USER, "A28"),
        Triple("Reembolsos", RfIconKind.WARNING, "A32"),
        Triple("Reseñas", RfIconKind.STAR, "s71"),
        Triple("Proveedores", RfIconKind.TRUCK, "s55"),
        Triple("Bot", RfIconKind.WHATSAPP, "s72"),
    )

    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .width(256.dp)
            .fillMaxHeight()
            .background(palette.surface)
            .border(width = 1.dp, color = palette.line)
            .padding(horizontal = 16.dp, vertical = 24.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
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
                            color = palette.ink,
                        ),
                    )
                    Spacer(Modifier.height(2.dp))
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .height(20.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(pillIndigoBg)
                            .padding(horizontal = 7.dp),
                    ) {
                        BasicText(
                            text = "Admin",
                            style = typo.tagLabel.copy(
                                color = pillIndigoInk,
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

            Spacer(Modifier.height(8.dp))
            BasicText(
                text = "MÓDULOS",
                style = typo.sectionCap.copy(fontSize = 11.sp, color = palette.muted),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            )
            moduleShortcuts.forEach { shortcut ->
                AdminDesktopSidebarNavRow(
                    label = shortcut.first,
                    icon = shortcut.second,
                    badge = null,
                    isSelected = false,
                    onClick = { onOpenScreenCode(shortcut.third) },
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
                    .background(palette.line),
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
                    val avatarInitial = InputValidator.extractInitials(profileName, fallback = "M", maxInitials = 1)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(pillIndigoBg),
                    ) {
                        BasicText(
                            text = avatarInitial,
                            style = typo.bodyStrong.copy(
                                color = pillIndigoInk,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                            ),
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        BasicText(
                            text = profileName.substringBefore(' ').ifBlank { "María" },
                            style = typo.bodyStrong.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = palette.ink),
                        )
                        BasicText(
                            text = profileEmail,
                            style = typo.secondary.copy(fontSize = 12.5.sp, color = palette.muted),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(palette.chipBg)
                        .clickable(onClick = onSwitchToMobile)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    BasicText(
                        text = "Móvil",
                        style = typo.caption.copy(
                            color = pillIndigoInk,
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
    val palette = LocalRfPalette.current
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
                tint = if (isSelected) Color.White else palette.ink,
                size = 19.dp,
            )
            BasicText(
                text = label,
                style = typo.body.copy(
                    fontSize = 14.5.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (isSelected) Color.White else palette.ink,
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
    val palette = LocalRfPalette.current
    val headingText = if (activeSection == A39DesktopSection.INICIO) {
        "Hola, $profileFirstName"
    } else {
        activeSection.title
    }
    val subtitleText = if (activeSection == A39DesktopSection.INICIO) {
        formatReadableSpanishDateText(DomainClockFormatter.currentFullDateLabel())
    } else {
        "Panel administrativo web · RosaFiesta"
    }

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val isCompactTopBar = maxWidth < 760.dp
        if (isCompactTopBar) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        BasicText(
                            text = subtitleText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = typo.secondary.copy(fontSize = 14.sp, color = palette.muted),
                        )
                        Spacer(Modifier.height(2.dp))
                        BasicText(
                            text = headingText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = typo.rootTabTitle.copy(fontSize = 24.sp, fontWeight = FontWeight.ExtraBold),
                        )
                    }
                    AdminA39NotificationButton(
                        bellBadgeCount = bellBadgeCount,
                        onOpenNotifications = onOpenNotifications,
                    )
                }
                AdminA39SearchBox(
                    searchQuery = searchQuery,
                    searchFocusRequester = searchFocusRequester,
                    onSearchChange = onSearchChange,
                    onFocusSearch = onFocusSearch,
                    modifier = Modifier.fillMaxWidth(),
                )
                AdminA39ActionsRow(
                    activeSection = activeSection,
                    fillAvailable = fillAvailable,
                    onOpenRosaAi = onOpenRosaAi,
                    onOpenNewEvent = onOpenNewEvent,
                    onExportActiveReport = onExportActiveReport,
                    onSwitchAppMode = onSwitchAppMode,
                )
            }
        } else {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                    BasicText(
                        text = subtitleText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = typo.secondary.copy(fontSize = 14.sp, color = palette.muted),
                    )
                    Spacer(Modifier.height(2.dp))
                    BasicText(
                        text = headingText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = typo.rootTabTitle.copy(fontSize = 28.sp, fontWeight = FontWeight.ExtraBold),
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    AdminA39SearchBox(
                        searchQuery = searchQuery,
                        searchFocusRequester = searchFocusRequester,
                        onSearchChange = onSearchChange,
                        onFocusSearch = onFocusSearch,
                        modifier = Modifier.width(340.dp),
                    )
                    AdminA39NotificationButton(
                        bellBadgeCount = bellBadgeCount,
                        onOpenNotifications = onOpenNotifications,
                    )
                    AdminA39ActionsRow(
                        activeSection = activeSection,
                        fillAvailable = fillAvailable,
                        onOpenRosaAi = onOpenRosaAi,
                        onOpenNewEvent = onOpenNewEvent,
                        onExportActiveReport = onExportActiveReport,
                        onSwitchAppMode = onSwitchAppMode,
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminA39SearchBox(
    searchQuery: String,
    searchFocusRequester: FocusRequester,
    onSearchChange: (String) -> Unit,
    onFocusSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalRfPalette.current
    val typo = LocalRfTypography.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .height(46.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(palette.surface)
            .border(1.dp, palette.fieldBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f),
        ) {
            RfIcon(
                kind = RfIconKind.SEARCH,
                tint = palette.muted,
                size = 18.dp,
            )
            BasicTextField(
                value = searchQuery,
                onValueChange = { updatedQuery ->
                    onSearchChange(updatedQuery.take(InputValidator.MAX_SEARCH_QUERY_LENGTH))
                },
                singleLine = true,
                textStyle = typo.body.copy(fontSize = 14.sp, color = palette.ink),
                cursorBrush = SolidColor(palette.primaryAccent),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(searchFocusRequester),
                decorationBox = { innerTextField ->
                    if (searchQuery.isEmpty()) {
                        BasicText(
                            text = "Buscar eventos, clientes, artículos",
                            style = typo.body.copy(fontSize = 14.sp, color = palette.muted),
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
                        .background(palette.chipBg)
                        .clickable { onSearchChange("") }
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                ) {
                    BasicText(
                        text = "✕",
                        style = typo.caption.copy(
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.muted,
                        ),
                    )
                }
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(palette.chipBg)
                    .clickable(onClick = onFocusSearch)
                    .padding(horizontal = 7.dp, vertical = 3.dp),
            ) {
                BasicText(
                    text = "⌘K",
                    style = typo.caption.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.muted,
                    ),
                )
            }
        }
    }
}

@Composable
private fun AdminA39NotificationButton(
    bellBadgeCount: Int,
    onOpenNotifications: () -> Unit,
) {
    val palette = LocalRfPalette.current
    val typo = LocalRfTypography.current
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(44.dp)
            .rfIconButtonShadow(CircleShape, isAdmin = true)
            .clip(CircleShape)
            .background(palette.surface)
            .border(1.dp, palette.line, CircleShape)
            .clickable(onClick = onOpenNotifications),
    ) {
        RfIcon(kind = RfIconKind.BELL, tint = palette.ink, size = 20.dp)
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
}

@Composable
private fun AdminA39ActionsRow(
    activeSection: A39DesktopSection,
    fillAvailable: Boolean,
    onOpenRosaAi: () -> Unit,
    onOpenNewEvent: () -> Unit,
    onExportActiveReport: () -> Unit,
    onSwitchAppMode: () -> Unit,
) {
    val palette = LocalRfPalette.current
    val typo = LocalRfTypography.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.horizontalScroll(rememberScrollState()),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .height(46.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(palette.surface)
                .border(1.dp, palette.fieldBorder, RoundedCornerShape(16.dp))
                .clickable(onClick = onOpenRosaAi)
                .padding(horizontal = 16.dp),
        ) {
            RosaAvatar(size = RosaAvatarSize.MINI, forceAdminPalette = true)
            BasicText(
                text = "Crear con Rosa",
                style = typo.buttonLabel.copy(
                    color = palette.ink,
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

@Composable
private fun AdminDesktopFeedbackBanner(
    message: String,
    isError: Boolean,
    onDismiss: () -> Unit,
) {
    val typo = LocalRfTypography.current
    val palette = LocalRfPalette.current
    val bgColor = if (isError) {
        if (palette.isDark) Color(0xFF3D1924) else Color(0xFFFDE8EE)
    } else {
        if (palette.isDark) Color(0xFF253516) else Color(0xFFEEF6D6)
    }
    val borderColor = if (isError) {
        if (palette.isDark) Color(0xFF703144) else Color(0xFFF2B6C5)
    } else {
        if (palette.isDark) Color(0xFF405C24) else Color(0xFFCDE29B)
    }
    val textColor = if (isError) {
        if (palette.isDark) Color(0xFFFF7A96) else Color(0xFFB42345)
    } else {
        if (palette.isDark) Color(0xFFAEDB5C) else Color(0xFF3E560B)
    }

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
    val pendingReceivableLabel = when {
        pendingPaymentEventsCount > 0 -> "$pendingPaymentEventsCount por verificar · 1 vence el jueves"
        viewModel != null -> "Sin cobros pendientes"
        else -> "3 pagos · 1 vence el jueves"
    }
    val openLeads = leadsList.filter { lead ->
        lead.stage.equals("Nueva", ignoreCase = true) || lead.stage.equals("Contactada", ignoreCase = true)
    }
    val requestsCount = if (viewModel != null) openLeads.size else metrics.newRequestsCount
    val estimatedLeadSum = openLeads.sumOf { lead -> lead.estimatedDop }
    val requestsSubtext = when {
        estimatedLeadSum > 0 -> "Est. ${formatDesktopDop(estimatedLeadSum)} en total"
        viewModel != null -> "Sin solicitudes pendientes"
        else -> "Est. RD$ 2,545 en total"
    }
    val rawNextDate = eventsList.firstOrNull { eventItem -> eventItem.status != EventStatus.CANCELLED }?.dateLabel
    val nextEventSubtext = when {
        rawNextDate != null -> "Próximo: ${formatReadableSpanishDateText(rawNextDate).lowercase()}"
        viewModel != null -> "Sin eventos programados"
        else -> "Próximo: vie, 23 de octubre"
    }

    val analyticsSummary = viewModel?.analyticsSummary?.collectAsState()?.value
    val revenueMarginPct = analyticsSummary?.netMarginPercent
    val revenueSubtext = when {
        revenueMarginPct != null -> {
            val trendArrow = if (revenueMarginPct >= 0) "▲" else "▼"
            "$trendArrow $revenueMarginPct% margen neto"
        }

        viewModel != null -> "Resumen del periodo actual"

        else -> "▲ 22% vs. septiembre"
    }

    val palette = LocalRfPalette.current
    val statCardColors = if (palette.isDark) {
        listOf(
            Color(0xFFAEDB5C), Color(0xFF253516), Color(0xFFAEDB5C),
            palette.muted, Color(0xFF3A2F16), Color(0xFFF5C954),
            palette.muted, Color(0xFF2B2852), Color(0xFFA7A3FF),
            palette.muted, Color(0xFF2B2852), Color(0xFFC5B8FF),
        )
    } else {
        listOf(
            Color(0xFF4E6A10), Color(0xFFEEF6D6), Color(0xFF4E6A10),
            Color(0xFF5F6377), Color(0xFFFFF2C9), Color(0xFF7A5700),
            Color(0xFF5F6377), Color(0xFFECEBFF), Color(0xFF4B47D6),
            Color(0xFF5F6377), RfColors.VioletaTint, RfColors.VioletaTintInk,
        )
    }

    val kpiCards = listOf(
        AdminA39KpiCardSpec(
            label = "Ingresos de ${metrics.monthLabel.lowercase()}",
            value = formatDesktopDop(metrics.octoberRevenueDop),
            subtext = revenueSubtext,
            subtextColor = statCardColors[0],
            iconKind = RfIconKind.ORDERS,
            tileBg = statCardColors[1],
            tileTint = statCardColors[2],
            onClick = { onNavigateSection(A39DesktopSection.REPORTES) },
        ),
        AdminA39KpiCardSpec(
            label = "Por cobrar",
            value = formatDesktopDop(metrics.accountsReceivableDop),
            subtext = pendingReceivableLabel,
            subtextColor = statCardColors[3],
            iconKind = RfIconKind.WALLET,
            tileBg = statCardColors[4],
            tileTint = statCardColors[5],
            onClick = { onNavigateSection(A39DesktopSection.PAGOS) },
        ),
        AdminA39KpiCardSpec(
            label = "Eventos activos",
            value = activeEventsCount.toString(),
            subtext = nextEventSubtext,
            subtextColor = statCardColors[6],
            iconKind = RfIconKind.CALENDAR,
            tileBg = statCardColors[7],
            tileTint = statCardColors[8],
            onClick = { onNavigateSection(A39DesktopSection.EVENTOS) },
        ),
        AdminA39KpiCardSpec(
            label = "Solicitudes nuevas",
            value = requestsCount.toString(),
            subtext = requestsSubtext,
            subtextColor = statCardColors[9],
            iconKind = RfIconKind.ORDERS,
            tileBg = statCardColors[10],
            tileTint = statCardColors[11],
            onClick = { onNavigateSection(A39DesktopSection.SOLICITUDES) },
        ),
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isCompactKpiGrid = maxWidth < 820.dp
            if (isCompactKpiGrid) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    kpiCards.chunked(2).forEach { pair ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            pair.forEach { card ->
                                AdminStatCard(
                                    spec = card,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                }
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    kpiCards.forEach { card ->
                        AdminStatCard(
                            spec = card,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
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

                val analyticsSummaryRef = analyticsSummary
                AdminMonthlyRevenueChart(
                    currentMonthRevenueDop = metrics.octoberRevenueDop,
                    analyticsSummary = analyticsSummaryRef,
                    paymentsList = viewModel?.payments?.collectAsState()?.value ?: CanonicalSampleData.payments,
                    onRequestMonthsRange = { monthsCount ->
                        val rangeKey = if (monthsCount == 12) "365d" else "180d"
                        viewModel?.loadAnalytics(rangeKey)
                    },
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
                    hasLiveViewModel = viewModel != null,
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

private data class AdminA39KpiCardSpec(
    val label: String,
    val value: String,
    val subtext: String,
    val subtextColor: Color,
    val iconKind: RfIconKind,
    val tileBg: Color,
    val tileTint: Color,
    val onClick: () -> Unit,
)

@Composable
private fun AdminStatCard(
    spec: AdminA39KpiCardSpec,
    modifier: Modifier = Modifier,
) {
    val typo = LocalRfTypography.current
    val palette = LocalRfPalette.current
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
            .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
            .clip(RoundedCornerShape(20.dp))
            .background(palette.surface)
            .border(1.dp, palette.line, RoundedCornerShape(20.dp))
            .clickable(onClick = spec.onClick)
            .padding(horizontal = 20.dp, vertical = 18.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            BasicText(
                text = spec.label,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = typo.body.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = palette.muted),
                modifier = Modifier.weight(1f),
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(spec.tileBg),
            ) {
                RfIcon(kind = spec.iconKind, tint = spec.tileTint, size = 18.dp)
            }
        }
        BasicText(
            text = spec.value,
            style = typo.webKpi.copy(fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = palette.ink),
        )
        BasicText(
            text = spec.subtext,
            style = typo.caption.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = spec.subtextColor),
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
    val palette = LocalRfPalette.current
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
            .background(palette.surface)
            .border(1.dp, palette.line, RoundedCornerShape(20.dp)),
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
                    color = palette.primaryAccent,
                ),
                modifier = Modifier.clickable(onClick = onViewAll),
            )
        }

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isCompactTable = maxWidth < 580.dp
            if (isCompactTable) {
                Box(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                    AdminUpcomingEventsTableBody(
                        filteredRows = filteredRows,
                        searchQuery = searchQuery,
                        onEventClick = onEventClick,
                        modifier = Modifier.width(620.dp),
                    )
                }
            } else {
                AdminUpcomingEventsTableBody(
                    filteredRows = filteredRows,
                    searchQuery = searchQuery,
                    onEventClick = onEventClick,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun AdminUpcomingEventsTableBody(
    filteredRows: List<AdminDesktopEventRowSpec>,
    searchQuery: String,
    onEventClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val typo = LocalRfTypography.current
    val palette = LocalRfPalette.current
    val headerStyle = typo.sectionCap.copy(fontSize = 11.sp)
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(palette.chipBg)
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            BasicText("EVENTO", maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(3.0f), style = headerStyle)
            BasicText("FECHA", maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(0.85f), style = headerStyle)
            BasicText("CLIENTE", maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(0.95f), style = headerStyle)
            BasicText("TOTAL", maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(0.85f), style = headerStyle)
            BasicText("PAGADO", maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1.1f), style = headerStyle)
            BasicText("ESTADO", maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1.25f), style = headerStyle)
        }

        if (filteredRows.isEmpty()) {
            val emptyTableText = if (searchQuery.isBlank()) {
                "Aún no tienes eventos próximos."
            } else {
                "Sin eventos coincidentes con \"${searchQuery.trim()}\"."
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth().padding(24.dp),
            ) {
                BasicText(
                    text = emptyTableText,
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
    val palette = LocalRfPalette.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 0.5.dp, color = palette.line)
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
                color = if (total.startsWith("RD$")) palette.ink else palette.muted,
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
                    .background(palette.chipBg),
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
                style = typo.secondary.copy(fontSize = 12.5.sp, color = palette.muted),
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
    analyticsSummary: PaymentsAnalyticsSummary? = null,
    paymentsList: List<PaymentRecord> = emptyList(),
    onRequestMonthsRange: (Int) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val typo = LocalRfTypography.current
    var selectedMonths by remember { mutableStateOf(6) }
    LaunchedEffect(selectedMonths) {
        onRequestMonthsRange(selectedMonths)
    }

    val palette = LocalRfPalette.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
            .clip(RoundedCornerShape(20.dp))
            .background(palette.surface)
            .border(1.dp, palette.line, RoundedCornerShape(20.dp))
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
                    .background(palette.chipBg)
                    .padding(3.dp),
            ) {
                val isSixMonths = selectedMonths == 6
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSixMonths) palette.surface else Color.Transparent)
                        .clickable { selectedMonths = 6 }
                        .padding(horizontal = 12.dp),
                ) {
                    BasicText(
                        text = "6 meses",
                        style = typo.caption.copy(
                            fontSize = 13.sp,
                            fontWeight = if (isSixMonths) FontWeight.Bold else FontWeight.SemiBold,
                            color = if (isSixMonths) palette.ink else palette.muted,
                        ),
                    )
                }
                val isTwelveMonths = selectedMonths == 12
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isTwelveMonths) palette.surface else Color.Transparent)
                        .clickable { selectedMonths = 12 }
                        .padding(horizontal = 12.dp),
                ) {
                    BasicText(
                        text = "12 meses",
                        style = typo.caption.copy(
                            fontSize = 13.sp,
                            fontWeight = if (isTwelveMonths) FontWeight.Bold else FontWeight.SemiBold,
                            color = if (isTwelveMonths) palette.ink else palette.muted,
                        ),
                    )
                }
            }
        }

        Spacer(Modifier.weight(1f))

        val bars = remember(analyticsSummary, paymentsList, selectedMonths, currentMonthRevenueDop) {
            buildMonthlyRevenueBars(
                analyticsSummary = analyticsSummary,
                paymentsList = paymentsList,
                selectedMonths = selectedMonths,
                currentMonthRevenueDop = currentMonthRevenueDop,
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
                            color = if (isLatestMonth) palette.ink else palette.muted,
                        ),
                    )
                    val mutedBarColor = if (palette.isDark) Color(0xFF2D314B) else Color(0xFFE3E4F5)
                    val barBrush = if (isLatestMonth) {
                        Brush.linearGradient(
                            listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0), Color(0xFF2FB8C8)),
                        )
                    } else {
                        Brush.linearGradient(listOf(mutedBarColor, mutedBarColor))
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
                        style = typo.secondary.copy(fontSize = labelSize, color = palette.muted),
                    )
                }
            }
        }
    }
}

private fun buildMonthlyRevenueBars(
    analyticsSummary: PaymentsAnalyticsSummary?,
    paymentsList: List<PaymentRecord>,
    selectedMonths: Int,
    currentMonthRevenueDop: Int,
): List<A39ChartBarSpec> {
    val serverSeries = analyticsSummary?.monthlySeries.orEmpty()
    if (serverSeries.isNotEmpty()) {
        val visibleSeries = serverSeries.takeLast(selectedMonths.coerceAtLeast(1))
        val maxRevenue = visibleSeries.maxOfOrNull { item -> item.revenueDop }?.coerceAtLeast(1) ?: 1
        return visibleSeries.map { barItem ->
            val ratio = (barItem.revenueDop.toFloat() / maxRevenue.toFloat()).coerceIn(0f, 1f)
            val barHeight = (22f + (ratio * 111f)).dp
            A39ChartBarSpec(
                month = barItem.monthLabel.ifBlank { barItem.monthKey.takeLast(2) }.lowercase(),
                amount = formatShortK(barItem.revenueDop),
                height = barHeight,
            )
        }
    }
    val allSpanishMonths = listOf("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic")
    val currentMonthIdx = (DomainClockFormatter.currentCivilDateTriple().second - 1).coerceIn(0, 11)
    val monthCount = if (selectedMonths == 12) 12 else 6
    val monthPairs = ((monthCount - 1) downTo 0).map { offset ->
        val wrappedIdx = (currentMonthIdx - offset + 12) % 12
        allSpanishMonths[wrappedIdx]
    }
    val totals = monthPairs.mapIndexed { index, monthToken ->
        val summed = paymentsList
            .filter { payment -> payment.dateLabel.lowercase().contains(monthToken) }
            .sumOf { payment -> payment.amountDop }
        val isFinalMonth = index == monthPairs.lastIndex
        val shouldFallbackToCurrentMonth = summed == 0 && isFinalMonth
        val effective = if (shouldFallbackToCurrentMonth) currentMonthRevenueDop else summed
        monthToken to effective
    }
    val maxTotal = totals.maxOfOrNull { pair -> pair.second }?.coerceAtLeast(1) ?: 1
    return totals.map { pair ->
        val ratio = (pair.second.toFloat() / maxTotal.toFloat()).coerceIn(0f, 1f)
        val heightDp = if (pair.second <= 0) 18.dp else (24f + (ratio * 109f)).dp
        A39ChartBarSpec(
            month = pair.first,
            amount = formatShortK(pair.second),
            height = heightDp,
        )
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
    val titleText = heroEvent?.title ?: "Sin eventos próximos"
    val subtitleText = if (heroEvent != null) {
        "Montaje 2:00 p. m. · ${heroEvent.venue} · ${heroEvent.guests} invitados"
    } else {
        "Crea un evento o cotización para programar montaje y logística"
    }
    val timingText = if (heroEvent != null) {
        "${formatReadableSpanishDateText(heroEvent.dateLabel)} · Próximo montaje"
    } else {
        "Agenda disponible"
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
        if (heroEvent != null) {
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
}

@Composable
private fun AdminNewRequestsCard(
    leadsList: List<LeadRecord>,
    hasLiveViewModel: Boolean = false,
    onQuoteLead: (String) -> Unit,
    onOpenFallbackBuilder: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val typo = LocalRfTypography.current
    val displayLeads = remember(leadsList) {
        leadsList.filter { lead -> !lead.stage.equals("Cerrada", ignoreCase = true) }.take(2)
    }
    val badgeCountText = if (hasLiveViewModel) {
        displayLeads.size.toString()
    } else if (leadsList.isNotEmpty()) {
        displayLeads.size.toString()
    } else {
        "2"
    }

    val palette = LocalRfPalette.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
            .clip(RoundedCornerShape(20.dp))
            .background(palette.surface)
            .border(1.dp, palette.line, RoundedCornerShape(20.dp)),
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
        } else if (hasLiveViewModel) {
            AdminNewRequestRow(
                photoKey = RfPhotoKey.BODA,
                title = "Sin solicitudes nuevas",
                subtitle = "Usa el constructor para cotizar directamente",
                onQuoteClick = onOpenFallbackBuilder,
            )
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
    val palette = LocalRfPalette.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 0.5.dp, color = palette.line)
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
                style = typo.secondary.copy(fontSize = 12.5.sp, color = palette.muted),
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
            title = balanceDueEvent?.let { ev -> "Saldo ${ev.title.substringBefore(' ').ifBlank { ev.title }}" } ?: "Sin saldos pendientes",
            subtitle = unverifiedPayment?.let { payment -> "Pago pendiente: ${payment.methodLabel}" } ?: "Pagos y anticipos al día",
            trailingText = balanceDueEvent?.let { ev -> formatDesktopDop(ev.balanceDop) } ?: "RD$ 0",
            badgeText = null,
            icon = RfIconKind.WALLET,
            tileBg = Color(0xFFFCE3E8),
            tileTint = Color(0xFFB42345),
            onClick = onOpenPayments,
        ),
        A39AttentionRowData(
            title = lowStockArticle?.name ?: "Stock al día",
            subtitle = lowStockArticle?.let { article -> "Quedan ${article.availableOct23} · stock bajo" } ?: "Sin alertas críticas de inventario",
            trailingText = null,
            badgeText = if (lowStockArticle != null) "Stock bajo" else "OK",
            icon = RfIconKind.CATALOG,
            tileBg = Color(0xFFFFF2C9),
            tileTint = Color(0xFF7A5700),
            onClick = onOpenInventory,
        ),
        A39AttentionRowData(
            title = if (cancelledCount > 0) "$cancelledCount cancelaciones por revisar" else "Sin cancelaciones pendientes",
            subtitle = if (cancelledCount > 0) "Revisa solicitudes y reembolsos" else "No hay reembolsos por procesar",
            trailingText = null,
            badgeText = null,
            icon = RfIconKind.CLOCK,
            tileBg = Color(0xFFECEBFF),
            tileTint = Color(0xFF4B47D6),
            onClick = onOpenCancellations,
        ),
    )

    val palette = LocalRfPalette.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
            .clip(RoundedCornerShape(20.dp))
            .background(palette.surface)
            .border(1.dp, palette.line, RoundedCornerShape(20.dp)),
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
                        .background(LocalRfPalette.current.chipBg)
                        .border(1.dp, LocalRfPalette.current.line, RoundedCornerShape(14.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        BasicText(
                            text = "${lead.eventType} · ${lead.clientName}",
                            style = typo.bodyStrong.copy(fontSize = 15.sp),
                        )
                        BasicText(
                            text = "${formatReadableSpanishDateText(lead.dateLabel)} · ${lead.guests} invitados · ${formatDesktopDop(lead.estimatedDop)} · Canal ${lead.channel}",
                            style = typo.secondary.copy(fontSize = 12.5.sp, color = LocalRfPalette.current.muted),
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
                        .background(LocalRfPalette.current.chipBg)
                        .border(1.dp, LocalRfPalette.current.line, RoundedCornerShape(14.dp))
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
                            style = typo.secondary.copy(fontSize = 12.5.sp, color = LocalRfPalette.current.muted),
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
    isSubmitting: Boolean = false,
    onAdjustStock: (String, Int) -> Unit,
    onDeleteArticle: (String, (Result<Unit>) -> Unit) -> Unit = { _, onDone -> onDone(Result.success(Unit)) },
    onOpenMobileInventory: () -> Unit,
    onOpenArticleEditor: (String?) -> Unit = {},
    onOpenCategories: () -> Unit,
    onOpenTrends: () -> Unit = {},
    viewModel: RosaFiestaViewModel? = null,
    modifier: Modifier = Modifier,
) {
    val typo = LocalRfTypography.current
    val trimmed = searchQuery.trim()
    var pendingDeleteArticle by remember { mutableStateOf<CatalogArticle?>(null) }
    var deleteLocalError by remember { mutableStateOf<String?>(null) }
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

    Box(modifier = modifier) {
        DesktopSectionCardContainer(
            title = "Inventario y disponibilidad (${filtered.size})",
            actionLabel = "Catálogo admin",
            onAction = onOpenMobileInventory,
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
                            .clickable { onOpenArticleEditor(null) }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        BasicText(
                            text = "+ Nuevo artículo",
                            style = typo.bodyStrong.copy(fontSize = 12.5.sp, color = Color.White),
                        )
                    }
                    val invPalette = LocalRfPalette.current
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (invPalette.isDark) Color(0xFF3B1A33) else Color(0xFFFDE6F2))
                            .border(1.dp, if (invPalette.isDark) Color(0xFF6B2D5C) else Color(0xFFF2C2DF), RoundedCornerShape(10.dp))
                            .clickable { onOpenTrends() }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        BasicText(
                            text = "Tendencias y portada",
                            style = typo.bodyStrong.copy(fontSize = 12.5.sp, color = if (invPalette.isDark) Color(0xFFF48AD0) else Color(0xFFC2338F)),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (invPalette.isDark) Color(0xFF2B2852) else Color(0xFFECEBFF))
                            .border(1.dp, if (invPalette.isDark) Color(0xFF474285) else Color(0xFFD5D4F7), RoundedCornerShape(10.dp))
                            .clickable { onOpenCategories() }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        BasicText(
                            text = "Gestionar categorías",
                            style = typo.bodyStrong.copy(fontSize = 12.5.sp, color = if (invPalette.isDark) Color(0xFFA7A3FF) else Color(0xFF4B47D6)),
                        )
                    }
                }
                filtered.forEach { article ->
                    val rowPalette = LocalRfPalette.current
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(rowPalette.chipBg)
                            .border(1.dp, rowPalette.line, RoundedCornerShape(14.dp))
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                    ) {
                        RfUploadedOrCatalogPhoto(
                            photoUrl = article.photoUrl,
                            fallbackPhotoKey = photoKeyForDesktopEvent(article.photo),
                            contentDescription = article.name,
                            viewModel = viewModel,
                            cornerRadius = 10.dp,
                            modifier = Modifier.size(44.dp),
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            BasicText(text = article.name, style = typo.bodyStrong.copy(fontSize = 14.5.sp))
                            BasicText(
                                text = "${article.category} · ${formatDesktopDop(article.pricePerDayDop)}/día · Disp.: ${article.availableOct23}/${article.totalStock}",
                                style = typo.secondary.copy(fontSize = 12.5.sp, color = rowPalette.muted),
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            DesktopSmallPillButton(label = "Editar", onClick = { onOpenArticleEditor(article.id) })
                            DesktopSmallPillButton(label = "-1", onClick = { onAdjustStock(article.id, -1) })
                            BasicText(
                                text = article.totalStock.toString(),
                                style = typo.bodyStrong.copy(fontSize = 14.sp),
                                modifier = Modifier.padding(horizontal = 6.dp),
                            )
                            DesktopSmallPillButton(label = "+1", onClick = { onAdjustStock(article.id, 1) })
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (rowPalette.isDark) Color(0xFF3D1924) else Color(0xFFFCE3E8))
                                    .clickable {
                                        deleteLocalError = null
                                        pendingDeleteArticle = article
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                RfIcon(
                                    kind = RfIconKind.TRASH,
                                    tint = if (rowPalette.isDark) Color(0xFFFF7A96) else Color(0xFFB42345),
                                    size = 16.dp,
                                )
                            }
                        }
                    }
                }
            }
        }

        AdminDeleteArticleConfirmSheet(
            targetArticle = pendingDeleteArticle,
            errorMessage = deleteLocalError,
            isSubmitting = isSubmitting,
            onDismiss = {
                pendingDeleteArticle = null
                deleteLocalError = null
            },
            onConfirmDelete = { target ->
                onDeleteArticle(target.id) { outcome ->
                    if (outcome.isSuccess) {
                        pendingDeleteArticle = null
                        deleteLocalError = null
                    } else {
                        deleteLocalError = outcome.exceptionOrNull()?.message ?: "No se pudo eliminar el artículo."
                    }
                }
            },
        )
    }
}

@Composable
private fun AdminDesktopQuotesSection(
    quotesList: List<QuoteRecord>,
    searchQuery: String,
    onEnrichQuote: (QuoteRecord) -> Unit,
    onApproveAndSendQuote: (QuoteRecord) -> Unit,
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
                        .background(LocalRfPalette.current.chipBg)
                        .border(1.dp, LocalRfPalette.current.line, RoundedCornerShape(14.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        BasicText(text = "${quote.code} · ${quote.eventTitle}", style = typo.bodyStrong.copy(fontSize = 14.5.sp))
                        BasicText(
                            text = "${quote.clientName} · ${formatReadableSpanishDateText(quote.dateLabel)} · Total ${formatDesktopDop(quote.totalDop)}",
                            style = typo.secondary.copy(fontSize = 12.5.sp, color = LocalRfPalette.current.muted),
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        val normalizedStatus = quote.status.trim().uppercase()
                        val isPendingStatus = normalizedStatus in setOf("PENDING", "PENDIENTE", "DECLINED", "REJECTED")
                        val statusTagLabel = when (normalizedStatus) {
                            "SENT", "ENVIADA", "APPROVED", "APROBADA" -> "Enviada"
                            "SIGNED", "FIRMADA", "CONFIRMADA" -> "Firmada"
                            "DECLINED", "REJECTED", "RECHAZADA" -> "Ajuste"
                            else -> "En revisión"
                        }
                        RfTag(text = statusTagLabel, tone = RfTagTone.SOL)
                        DesktopSmallPillButton(label = "Enriquecer", onClick = { onEnrichQuote(quote) })
                        if (isPendingStatus) {
                            DesktopGradientPillButton(label = "Aprobar y enviar", onClick = { onApproveAndSendQuote(quote) })
                        }
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
    onVerifyPayment: (String) -> Unit = {},
    onRejectPayment: (String) -> Unit = {},
    onOpenTeamManagement: () -> Unit = {},
    onOpenMobilePayments: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val typo = LocalRfTypography.current
    if (!canViewPayments) {
        DesktopSectionCardContainer(
            title = "Acceso restringido",
            actionLabel = "Gestionar equipo",
            onAction = onOpenTeamManagement,
            modifier = modifier,
        ) {
            BasicText(
                text = "Tu rol actual no tiene permiso para visualizar cobros ni registrar pagos manuales.",
                style = typo.body.copy(fontSize = 14.5.sp, color = Color(0xFFB42345)),
            )
        }
        return
    }

    val defaultEventId = remember(eventsList) {
        val candidate = eventsList.firstOrNull { ev -> ev.balanceDop > 0 }
        if (candidate != null) {
            candidate.id
        } else {
            eventsList.firstOrNull()?.id.orEmpty()
        }
    }
    var selectedQuickEventId by remember(defaultEventId) { mutableStateOf(defaultEventId) }
    var amountInput by remember { mutableStateOf("") }
    var referenceInput by remember { mutableStateOf("") }
    var amountTouched by remember { mutableStateOf(false) }
    var referenceTouched by remember { mutableStateOf(false) }
    var paymentSubmitAttempted by remember { mutableStateOf(false) }
    val amountError = rememberDebouncedValidationError(
        value = amountInput,
        hasInteracted = amountTouched,
        forceShowError = paymentSubmitAttempted,
    ) { candidate ->
        InputValidator.validateMoneyAmount(
            rawValue = candidate,
            minAmount = 1.0,
            maxAmount = InputValidator.MAX_MONEY_AMOUNT,
            fieldLabel = "El monto",
        )
    }
    val referenceError = rememberDebouncedValidationError(
        value = referenceInput,
        hasInteracted = referenceTouched,
        forceShowError = paymentSubmitAttempted,
    ) { candidate ->
        InputValidator.validateBankReference(candidate, isRequired = true)
    }
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
            val payPalette = LocalRfPalette.current
            val payCardBg = if (payPalette.isDark) Color(0xFF2B2852) else Color(0xFFF3F2FE)
            val payAccentInk = if (payPalette.isDark) Color(0xFFA7A3FF) else Color(0xFF4B47D6)
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(payCardBg)
                    .padding(12.dp),
            ) {
                if (eventsList.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    ) {
                        BasicText("Evento:", style = typo.caption.copy(fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = payAccentInk))
                        eventsList.take(8).forEach { ev ->
                            val isSelectedEvent = ev.id == selectedQuickEventId
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (isSelectedEvent) Color(0xFF4B47D6) else payPalette.surface)
                                    .border(1.dp, if (isSelectedEvent) Color(0xFF4B47D6) else payPalette.fieldBorder, CircleShape)
                                    .clickable { selectedQuickEventId = ev.id }
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                            ) {
                                BasicText(
                                    text = ev.title.take(24),
                                    style = typo.caption.copy(
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelectedEvent) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelectedEvent) Color.White else payPalette.ink,
                                    ),
                                )
                            }
                        }
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    BasicText("Registro rápido:", style = typo.bodyStrong.copy(fontSize = 13.5.sp))
                    DesktopInlineInput(
                        value = amountInput,
                        placeholder = "Monto",
                        isError = amountError != null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        onValueChange = { next ->
                            amountTouched = true
                            paymentSubmitAttempted = false
                            amountInput = InputValidator.filterDigitsOnly(next, 8)
                        },
                        modifier = Modifier.width(120.dp),
                    )
                    DesktopInlineInput(
                        value = referenceInput,
                        placeholder = "Referencia",
                        isError = referenceError != null,
                        onValueChange = { next ->
                            referenceTouched = true
                            paymentSubmitAttempted = false
                            referenceInput = next.take(InputValidator.MAX_BANK_REFERENCE_LENGTH)
                        },
                        modifier = Modifier.width(180.dp),
                    )
                    val targetEvent = eventsList.firstOrNull { ev -> ev.id == selectedQuickEventId }
                        ?: eventsList.firstOrNull { ev -> ev.balanceDop > 0 }
                        ?: eventsList.firstOrNull()
                    DesktopGradientPillButton(
                        label = "Registrar pago",
                        onClick = {
                            paymentSubmitAttempted = true
                            val paymentValidation = firstDesktopValidationError(
                                InputValidator.validateMoneyAmount(
                                    rawValue = amountInput,
                                    minAmount = 1.0,
                                    maxAmount = InputValidator.MAX_MONEY_AMOUNT,
                                    fieldLabel = "El monto",
                                ),
                                InputValidator.validateBankReference(referenceInput, isRequired = true),
                            )
                            if (paymentValidation == null) {
                                if (targetEvent != null) {
                                    onRegisterQuickPayment(targetEvent.id, amountInput, "Transferencia bancaria", referenceInput)
                                    paymentSubmitAttempted = false
                                }
                            }
                        },
                    )
                }
                RfValidationErrorText(errorMessage = firstDesktopValidationError(amountError, referenceError))
            }

            filtered.forEach { payment ->
                val matchingEvent = eventsList.firstOrNull { ev -> ev.id == payment.eventId } ?: eventsList.firstOrNull()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(payPalette.chipBg)
                        .border(1.dp, payPalette.line, RoundedCornerShape(14.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        BasicText(text = "${payment.receiptNumber} · ${payment.eventTitle}", style = typo.bodyStrong.copy(fontSize = 14.5.sp))
                        BasicText(
                            text = "${payment.clientName} · ${payment.methodLabel} · ${formatReadableSpanishDateText(payment.dateLabel)} · Ref ${payment.referenceCode}",
                            style = typo.secondary.copy(fontSize = 12.5.sp, color = payPalette.muted),
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        BasicText(text = formatDesktopDop(payment.amountDop), style = typo.cardTitle.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold))
                        RfTag(text = if (payment.verified) "Verificado" else "En revisión", tone = if (payment.verified) RfTagTone.LIMA else RfTagTone.SOL)
                        if (!payment.verified) {
                            DesktopGradientPillButton(
                                label = "Verificar",
                                onClick = { onVerifyPayment(payment.id) },
                            )
                            DesktopSmallPillButton(
                                label = "Rechazar",
                                onClick = { onRejectPayment(payment.id) },
                            )
                        }
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
                        .background(LocalRfPalette.current.chipBg)
                        .border(1.dp, LocalRfPalette.current.line, RoundedCornerShape(14.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        BasicText(text = client.fullName, style = typo.bodyStrong.copy(fontSize = 14.5.sp))
                        BasicText(
                            text = "${client.email} · ${InputValidator.formatPhoneDisplay(client.phone)} · ${client.zone} · ${client.eventsCount} eventos",
                            style = typo.secondary.copy(fontSize = 12.5.sp, color = LocalRfPalette.current.muted),
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
    var messageTouched by remember { mutableStateOf(false) }
    var sendAttempted by remember { mutableStateOf(false) }
    val messageError = rememberDebouncedValidationError(
        value = draftMessage,
        hasInteracted = messageTouched,
        forceShowError = sendAttempted,
    ) { candidate ->
        InputValidator.validateChatMessage(candidate, isRequired = true)
    }

    DesktopSectionCardContainer(
        title = "Mensajería centralizada (${threads.size} hilos)",
        actionLabel = "Abrir chat completo",
        onAction = onOpenMobileChats,
        modifier = modifier,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxSize()) {
            val chatsPalette = LocalRfPalette.current
            val selectedThreadBg = if (chatsPalette.isDark) Color(0xFF2B2852) else Color(0xFFECEBFF)
            val threadTagInk = if (chatsPalette.isDark) Color(0xFFA7A3FF) else Color(0xFF4B47D6)
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
                            .background(if (isSelected) selectedThreadBg else chatsPalette.chipBg)
                            .border(1.dp, if (isSelected) Color(0xFF7C5CF0) else chatsPalette.line, RoundedCornerShape(12.dp))
                            .clickable { onSelectThread(thread.id) }
                            .padding(12.dp),
                    ) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            BasicText(text = thread.contactName, style = typo.bodyStrong.copy(fontSize = 14.sp))
                            BasicText(text = thread.timestampLabel, style = typo.caption.copy(fontSize = 11.5.sp, color = chatsPalette.muted))
                        }
                        BasicText(text = thread.eventTitle, style = typo.caption.copy(fontSize = 12.sp, color = threadTagInk))
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
                    .background(chatsPalette.chipBg)
                    .border(1.dp, chatsPalette.line, RoundedCornerShape(14.dp))
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
                                .background(if (isStaffBubble) selectedThreadBg else chatsPalette.surface)
                                .padding(10.dp),
                        ) {
                            Column {
                                BasicText(text = "${msg.senderName} · ${msg.timestampLabel}", style = typo.caption.copy(fontSize = 11.5.sp, fontWeight = FontWeight.Bold))
                                BasicText(text = msg.body, style = typo.body.copy(fontSize = 13.5.sp))
                            }
                        }
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                    val submitChatReply = {
                        sendAttempted = true
                        val validation = InputValidator.validateChatMessage(draftMessage, isRequired = true)
                        if (validation == null) {
                            onSendReply(draftMessage.trim())
                            draftMessage = ""
                            messageTouched = false
                            sendAttempted = false
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        DesktopInlineInput(
                            value = draftMessage,
                            placeholder = "Responder al cliente...",
                            isError = messageError != null,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            onSubmit = submitChatReply,
                            onValueChange = { updated ->
                                messageTouched = true
                                sendAttempted = false
                                draftMessage = updated.take(InputValidator.MAX_CHAT_MESSAGE_LENGTH)
                            },
                            modifier = Modifier.weight(1f),
                        )
                        DesktopGradientPillButton(
                            label = "Enviar",
                            onClick = submitChatReply,
                        )
                    }
                    RfValidationErrorText(errorMessage = messageError)
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
                        .background(LocalRfPalette.current.chipBg)
                        .border(1.dp, LocalRfPalette.current.line, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                ) {
                    Column {
                        BasicText(text = "${member.name} · ${member.roleLabel}", style = typo.bodyStrong.copy(fontSize = 14.sp))
                        BasicText(
                            text = "Acceso financiero: ${if (memberHasPayments) "Habilitado" else "Restringido"} · ${member.activeEventsCount} eventos",
                            style = typo.secondary.copy(fontSize = 12.5.sp, color = LocalRfPalette.current.muted),
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
    var rnc by remember(initialSettings) { mutableStateOf(InputValidator.formatRncDisplay(initialSettings.rnc)) }
    var address by remember(initialSettings) { mutableStateOf(initialSettings.address) }
    var email by remember(initialSettings) { mutableStateOf(initialSettings.email) }
    var phone by remember(initialSettings) { mutableStateOf(InputValidator.formatPhoneDisplay(initialSettings.phone)) }

    var nameTouched by remember { mutableStateOf(false) }
    var rncTouched by remember { mutableStateOf(false) }
    var phoneTouched by remember { mutableStateOf(false) }
    var emailTouched by remember { mutableStateOf(false) }
    var addressTouched by remember { mutableStateOf(false) }
    var saveAttempted by remember { mutableStateOf(false) }
    var localSaveError by remember { mutableStateOf<String?>(null) }

    val nameError = rememberDebouncedValidationError(
        value = businessName,
        hasInteracted = nameTouched,
        forceShowError = saveAttempted,
    ) { candidate ->
        InputValidator.validateBusinessName(candidate, isRequired = true)
    }
    val rncError = rememberDebouncedValidationError(
        value = rnc,
        hasInteracted = rncTouched,
        forceShowError = saveAttempted,
    ) { candidate ->
        InputValidator.validateRnc(candidate, isRequired = true)
    }
    val phoneError = rememberDebouncedValidationError(
        value = phone,
        hasInteracted = phoneTouched,
        forceShowError = saveAttempted,
    ) { candidate ->
        InputValidator.validateDominicanPhone(
            rawValue = candidate,
            isRequired = true,
            allowInternational = true,
            fieldLabel = "El teléfono",
        )
    }
    val emailError = rememberDebouncedValidationError(
        value = email,
        hasInteracted = emailTouched,
        forceShowError = saveAttempted,
    ) { candidate ->
        InputValidator.validateEmail(candidate)
    }
    val addressError = rememberDebouncedValidationError(
        value = address,
        hasInteracted = addressTouched,
        forceShowError = saveAttempted,
    ) { candidate ->
        InputValidator.validateAddress(candidate, isRequired = true)
    }

    DesktopSectionCardContainer(
        title = "Configuración comercial y fiscal",
        actionLabel = "Ajustes avanzados",
        onAction = onOpenMobileSettings,
        modifier = modifier,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
            if (!localSaveError.isNullOrBlank()) {
                BasicText(
                    text = localSaveError.orEmpty(),
                    style = typo.bodyStrong.copy(fontSize = 13.sp, color = Color(0xFFD63031)),
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                BasicText(text = "Razón social", style = typo.bodyStrong.copy(fontSize = 13.5.sp))
                DesktopInlineInput(
                    value = businessName,
                    placeholder = "Nombre del negocio",
                    isError = nameError != null,
                    onValueChange = { next ->
                        nameTouched = true
                        localSaveError = null
                        businessName = next.take(InputValidator.MAX_BUSINESS_NAME_LENGTH)
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                RfValidationErrorText(errorMessage = nameError)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    BasicText(text = "RNC", style = typo.bodyStrong.copy(fontSize = 13.5.sp))
                    DesktopInlineInput(
                        value = rnc,
                        placeholder = "1-31-84920-2",
                        isError = rncError != null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        cursorMask = InputValidator::filterRncInputWithCursor,
                        onValueChange = { next ->
                            rncTouched = true
                            localSaveError = null
                            rnc = next
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    RfValidationErrorText(errorMessage = rncError)
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    BasicText(text = "Teléfono", style = typo.bodyStrong.copy(fontSize = 13.5.sp))
                    DesktopInlineInput(
                        value = phone,
                        placeholder = "+1809...",
                        isError = phoneError != null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        cursorMask = InputValidator::filterPhoneInputWithCursor,
                        onValueChange = { next ->
                            phoneTouched = true
                            localSaveError = null
                            phone = next
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    RfValidationErrorText(errorMessage = phoneError)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                BasicText(text = "Correo administrativo", style = typo.bodyStrong.copy(fontSize = 13.5.sp))
                DesktopInlineInput(
                    value = email,
                    placeholder = "hola@rosafiesta.do",
                    isError = emailError != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    onValueChange = { next ->
                        emailTouched = true
                        localSaveError = null
                        email = next.take(InputValidator.MAX_EMAIL_LENGTH)
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                RfValidationErrorText(errorMessage = emailError)
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                BasicText(text = "Dirección física", style = typo.bodyStrong.copy(fontSize = 13.5.sp))
                DesktopInlineInput(
                    value = address,
                    placeholder = "Dirección comercial",
                    isError = addressError != null,
                    onValueChange = { next ->
                        addressTouched = true
                        localSaveError = null
                        address = next.take(InputValidator.MAX_ADDRESS_LENGTH)
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                RfValidationErrorText(errorMessage = addressError)
            }

            DesktopGradientPillButton(
                label = "Guardar configuración",
                onClick = {
                    saveAttempted = true
                    val validationError = firstDesktopValidationError(
                        InputValidator.validateBusinessName(businessName, isRequired = true),
                        InputValidator.validateRnc(rnc, isRequired = true),
                        InputValidator.validateDominicanPhone(
                            rawValue = phone,
                            isRequired = true,
                            allowInternational = true,
                            fieldLabel = "El teléfono",
                        ),
                        InputValidator.validateEmail(email),
                        InputValidator.validateAddress(address, isRequired = true),
                    )
                    if (validationError != null) {
                        localSaveError = validationError
                    } else {
                        localSaveError = null
                        onSaveSettings(
                            initialSettings.copy(
                                businessName = businessName.trim(),
                                rnc = InputValidator.formatRncDisplay(rnc.trim()),
                                phone = InputValidator.formatPhoneDisplay(phone.trim()),
                                email = email.trim(),
                                address = address.trim(),
                            ),
                        )
                    }
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
    var renderedModal by remember { mutableStateOf(activeModal) }
    var showNewEventCalendar by remember { mutableStateOf(false) }
    if (activeModal != A39DesktopModal.NONE) {
        renderedModal = activeModal
    }
    LaunchedEffect(activeModal) {
        if (activeModal != A39DesktopModal.NEW_EVENT) {
            showNewEventCalendar = false
        }
    }

    AnimatedVisibility(
        visible = activeModal != A39DesktopModal.NONE,
        enter = fadeIn(animationSpec = tween(180)),
        exit = fadeOut(animationSpec = tween(150)),
    ) {
        val scrimInteraction = remember { MutableInteractionSource() }
        val cardInteraction = remember { MutableInteractionSource() }
        val currentEventDate = viewModel?.newEventDate?.collectAsState()?.value.orEmpty()
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x66151729))
                .onPreviewKeyEvent { keyEvent ->
                    val isKeyDown = keyEvent.type == KeyEventType.KeyDown
                    val isEscape = keyEvent.key == Key.Escape
                    val shouldDismiss = if (isKeyDown) isEscape else false
                    if (shouldDismiss) {
                        showNewEventCalendar = false
                        onDismiss()
                        true
                    } else {
                        false
                    }
                }
                .clickable(
                    interactionSource = scrimInteraction,
                    indication = null,
                ) {
                    showNewEventCalendar = false
                    onDismiss()
                }
                .padding(24.dp),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .animateEnterExit(
                        enter = scaleIn(initialScale = 0.94f, animationSpec = tween(200)),
                        exit = scaleOut(targetScale = 0.94f, animationSpec = tween(150)),
                    )
                    .width(560.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(LocalRfPalette.current.surface)
                    .border(1.dp, LocalRfPalette.current.fieldBorder, RoundedCornerShape(22.dp))
                    .clickable(
                        interactionSource = cardInteraction,
                        indication = null,
                    ) {}
                    .padding(24.dp),
            ) {
                when (renderedModal) {
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
                            onOpenCalendar = { showNewEventCalendar = true },
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

            if (showNewEventCalendar) {
                RfInteractiveCalendarModal(
                    title = "Fecha del evento",
                    currentDateText = currentEventDate,
                    isAdmin = true,
                    dateMode = RfCalendarDateMode.EVENT_UPCOMING,
                    onSelectDate = { formattedDate ->
                        viewModel?.setNewEventDate(formattedDate)
                        showNewEventCalendar = false
                    },
                    onDismiss = { showNewEventCalendar = false },
                )
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
    val palette = LocalRfPalette.current
    val unreadNotifBg = if (palette.isDark) Color(0xFF2B2852) else Color(0xFFECEBFF)
    Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        BasicText(text = "Notificaciones recientes", style = typo.cardTitle.copy(fontSize = 18.sp, fontWeight = FontWeight.ExtraBold))
        DesktopSmallPillButton(label = "Cerrar", onClick = onDismiss)
    }
    if (notifications.isEmpty()) {
        BasicText(text = "No tienes alertas pendientes en este momento.", style = typo.body.copy(fontSize = 14.sp, color = palette.muted))
    } else {
        notifications.take(5).forEach { notif ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (notif.isRead) palette.chipBg else unreadNotifBg)
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
    DesktopGradientPillButton(label = "Centro de notificaciones", onClick = onOpenFullCenter)
}

@Composable
private fun AdminDesktopRosaAiModal(
    viewModel: RosaFiestaViewModel?,
    onOpenFullScreen: () -> Unit,
    onDismiss: () -> Unit,
) {
    val typo = LocalRfTypography.current
    val palette = LocalRfPalette.current
    val isSubmitting = viewModel?.isSubmitting?.collectAsState()?.value ?: false
    val rosaReply = viewModel?.rosaProposalMessage?.collectAsState()?.value
    var promptText by remember { mutableStateOf("") }
    var promptTouched by remember { mutableStateOf(false) }
    var generateAttempted by remember { mutableStateOf(false) }
    val promptError = rememberDebouncedValidationError(
        value = promptText,
        hasInteracted = promptTouched,
        forceShowError = generateAttempted,
    ) { candidate ->
        InputValidator.validateEntityName(
            rawValue = candidate,
            fieldLabel = "La descripción del evento",
            minLength = 5,
            maxLength = InputValidator.MAX_DESCRIPTION_LENGTH,
            isRequired = true,
        )
    }

    Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        BasicText(text = "Crear propuesta con Rosa IA", style = typo.cardTitle.copy(fontSize = 18.sp, fontWeight = FontWeight.ExtraBold))
        DesktopSmallPillButton(label = "Cerrar", onClick = onDismiss)
    }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        DesktopInlineInput(
            value = promptText,
            placeholder = "Describe el evento, fecha e invitados...",
            isError = promptError != null,
            onValueChange = { updated ->
                promptTouched = true
                generateAttempted = false
                promptText = updated.take(InputValidator.MAX_DESCRIPTION_LENGTH)
            },
            modifier = Modifier.fillMaxWidth(),
        )
        RfValidationErrorText(errorMessage = promptError)
    }
    if (!rosaReply.isNullOrBlank()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (palette.isDark) Color(0xFF2B2852) else Color(0xFFF3F2FE))
                .padding(12.dp),
        ) {
            BasicText(text = rosaReply, style = typo.body.copy(fontSize = 13.5.sp))
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        DesktopGradientPillButton(
            label = if (isSubmitting) "Generando..." else "Generar con Rosa",
            enabled = !isSubmitting,
            onClick = {
                generateAttempted = true
                val validation = InputValidator.validateEntityName(
                    rawValue = promptText,
                    fieldLabel = "La descripción del evento",
                    minLength = 5,
                    maxLength = InputValidator.MAX_DESCRIPTION_LENGTH,
                    isRequired = true,
                )
                if (validation == null) {
                    viewModel?.sendConversationalRosaPrompt(promptText.trim())
                }
            },
        )
        DesktopSmallPillButton(label = "Asistente completo", onClick = onOpenFullScreen)
    }
}

@Composable
private fun AdminDesktopNewEventModal(
    viewModel: RosaFiestaViewModel?,
    onOpenCalendar: () -> Unit,
    onOpenMobileWizard: () -> Unit,
    onDismiss: () -> Unit,
) {
    val typo = LocalRfTypography.current
    val title = viewModel?.newEventTitle?.collectAsState()?.value ?: ""
    val client = viewModel?.newEventClient?.collectAsState()?.value ?: ""
    val date = viewModel?.newEventDate?.collectAsState()?.value ?: ""
    val venue = viewModel?.newEventVenue?.collectAsState()?.value ?: ""
    val guests = viewModel?.newEventGuests?.collectAsState()?.value ?: ""

    var titleTouched by remember { mutableStateOf(false) }
    var clientTouched by remember { mutableStateOf(false) }
    var dateTouched by remember { mutableStateOf(false) }
    var guestsTouched by remember { mutableStateOf(false) }
    var venueTouched by remember { mutableStateOf(false) }
    var submitAttempted by remember { mutableStateOf(false) }
    var localSubmitError by remember { mutableStateOf<String?>(null) }

    val titleError = rememberDebouncedValidationError(
        value = title,
        hasInteracted = titleTouched,
        forceShowError = submitAttempted,
    ) { candidate ->
        InputValidator.validateEventName(candidate, isRequired = true)
    }
    val clientError = rememberDebouncedValidationError(
        value = client,
        hasInteracted = clientTouched,
        forceShowError = submitAttempted,
    ) { candidate ->
        InputValidator.validateEntityName(
            rawValue = candidate,
            fieldLabel = "El nombre del cliente",
            minLength = InputValidator.MIN_NAME_LENGTH,
            maxLength = InputValidator.MAX_FULL_NAME_LENGTH,
        )
    }
    val dateError = rememberDebouncedValidationError(
        value = date,
        hasInteracted = dateTouched,
        forceShowError = submitAttempted,
    ) { candidate ->
        validateDesktopEventDateText(candidate)
    }
    val guestsError = rememberDebouncedValidationError(
        value = guests,
        hasInteracted = guestsTouched,
        forceShowError = submitAttempted,
    ) { candidate ->
        InputValidator.validateGuestCount(candidate)
    }
    val venueError = rememberDebouncedValidationError(
        value = venue,
        hasInteracted = venueTouched,
        forceShowError = submitAttempted,
    ) { candidate ->
        InputValidator.validateAddress(candidate, "El lugar del evento")
    }

    Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        BasicText(text = "Crear nuevo evento", style = typo.cardTitle.copy(fontSize = 18.sp, fontWeight = FontWeight.ExtraBold))
        DesktopSmallPillButton(label = "Cerrar", onClick = onDismiss)
    }
    if (!localSubmitError.isNullOrBlank()) {
        BasicText(
            text = localSubmitError.orEmpty(),
            style = typo.bodyStrong.copy(fontSize = 12.5.sp, color = Color(0xFFD63031)),
        )
    }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        DesktopInlineInput(
            value = title,
            placeholder = "Título del evento",
            isError = titleError != null,
            onValueChange = { updated ->
                titleTouched = true
                localSubmitError = null
                viewModel?.setNewEventTitle(updated.take(InputValidator.MAX_EVENT_NAME_LENGTH))
            },
            modifier = Modifier.fillMaxWidth(),
        )
        RfValidationErrorText(errorMessage = titleError)
    }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        DesktopInlineInput(
            value = client,
            placeholder = "Nombre del cliente",
            isError = clientError != null,
            onValueChange = { updated ->
                clientTouched = true
                localSubmitError = null
                viewModel?.setNewEventClient(updated.take(InputValidator.MAX_FULL_NAME_LENGTH))
            },
            modifier = Modifier.fillMaxWidth(),
        )
        RfValidationErrorText(errorMessage = clientError)
    }
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            RfMaskedDateField(
                value = date,
                onValueChange = { updated ->
                    dateTouched = true
                    localSubmitError = null
                    viewModel?.setNewEventDate(updated)
                },
                onOpenCalendar = onOpenCalendar,
                placeholder = "DD/MM/AAAA",
                isAdmin = true,
                compactInline = false,
                hasError = dateError != null,
                modifier = Modifier.fillMaxWidth(),
            )
            RfValidationErrorText(errorMessage = dateError)
        }
        Column(modifier = Modifier.width(140.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            DesktopInlineInput(
                value = guests,
                placeholder = "Invitados",
                isError = guestsError != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                onValueChange = { updated ->
                    guestsTouched = true
                    localSubmitError = null
                    viewModel?.setNewEventGuests(InputValidator.filterDigitsOnly(updated, 5))
                },
                modifier = Modifier.fillMaxWidth(),
            )
            RfValidationErrorText(errorMessage = guestsError)
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        DesktopInlineInput(
            value = venue,
            placeholder = "Lugar o salón",
            isError = venueError != null,
            onValueChange = { updated ->
                venueTouched = true
                localSubmitError = null
                viewModel?.setNewEventVenue(updated.take(InputValidator.MAX_ADDRESS_LENGTH))
            },
            modifier = Modifier.fillMaxWidth(),
        )
        RfValidationErrorText(errorMessage = venueError)
    }
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        DesktopGradientPillButton(
            label = "Guardar evento",
            onClick = {
                submitAttempted = true
                val firstError = firstDesktopValidationError(
                    InputValidator.validateEventName(title, isRequired = true),
                    InputValidator.validateEntityName(
                        rawValue = client,
                        fieldLabel = "El nombre del cliente",
                        minLength = InputValidator.MIN_NAME_LENGTH,
                        maxLength = InputValidator.MAX_FULL_NAME_LENGTH,
                    ),
                    validateDesktopEventDateText(date),
                    InputValidator.validateGuestCount(guests),
                    InputValidator.validateAddress(venue, "El lugar del evento"),
                )
                if (firstError != null) {
                    localSubmitError = firstError
                } else {
                    val createdOk = viewModel?.submitNewEvent() ?: true
                    if (createdOk) {
                        localSubmitError = null
                        onDismiss()
                    } else {
                        localSubmitError = viewModel.formError.value ?: "No se pudo guardar el evento."
                    }
                }
            },
        )
        DesktopSmallPillButton(label = "Asistente completo", onClick = onOpenMobileWizard)
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
    val palette = LocalRfPalette.current
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
            style = typo.secondary.copy(fontSize = 13.5.sp, color = palette.muted),
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
            DesktopSmallPillButton(label = "Abrir ficha completa", onClick = onOpenFullDetail)
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
    val palette = LocalRfPalette.current
    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier
            .fillMaxWidth()
            .rfCardShadow(RoundedCornerShape(20.dp), isAdmin = true)
            .clip(RoundedCornerShape(20.dp))
            .background(palette.surface)
            .border(1.dp, palette.line, RoundedCornerShape(20.dp))
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
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    cursorMask: ((previousFormatted: String, newRawText: String, newSelectionEnd: Int) -> MaskedTextCursorResult)? = null,
    onFocusChanged: ((Boolean) -> Unit)? = null,
    onSubmit: (() -> Unit)? = null,
) {
    val typo = LocalRfTypography.current
    val palette = LocalRfPalette.current
    val submitActions = if (onSubmit != null) {
        KeyboardActions(
            onDone = { onSubmit() },
            onSend = { onSubmit() },
        )
    } else {
        KeyboardActions.Default
    }
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(palette.surface)
            .border(1.dp, if (isError) Color(0xFFD63031) else palette.fieldBorder, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp),
    ) {
        if (cursorMask != null) {
            val maskKey = remember { Any() }
            RfMaskedDigitBasicTextField(
                value = value,
                onValueChange = onValueChange,
                maskTransform = cursorMask,
                singleLine = true,
                keyboardOptions = keyboardOptions,
                textStyle = typo.body.copy(fontSize = 13.5.sp, color = palette.ink),
                cursorBrush = SolidColor(palette.primaryAccent),
                modifier = Modifier
                    .fillMaxWidth()
                    .onPreviewKeyEvent { keyEvent ->
                        val isKeyUp = keyEvent.type == KeyEventType.KeyUp
                        val isEnter = if (keyEvent.key == Key.Enter) true else keyEvent.key == Key.NumPadEnter
                        val canSubmit = if (isKeyUp) isEnter else false
                        val shouldRunSubmit = if (canSubmit) onSubmit != null else false
                        if (shouldRunSubmit) {
                            onSubmit?.invoke()
                            true
                        } else {
                            false
                        }
                    }
                    .rfTrackValidationFocus(focusKey = maskKey) { isFocused ->
                        onFocusChanged?.invoke(isFocused)
                    },
                decorationBox = { inner ->
                    if (value.isEmpty()) {
                        BasicText(text = placeholder, style = typo.secondary.copy(fontSize = 13.5.sp, color = palette.muted))
                    }
                    inner()
                },
            )
        } else {
            val plainKey = remember { Any() }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                keyboardOptions = keyboardOptions,
                keyboardActions = submitActions,
                textStyle = typo.body.copy(fontSize = 13.5.sp, color = palette.ink),
                cursorBrush = SolidColor(palette.primaryAccent),
                modifier = Modifier
                    .fillMaxWidth()
                    .onPreviewKeyEvent { keyEvent ->
                        val isKeyUp = keyEvent.type == KeyEventType.KeyUp
                        val isEnter = if (keyEvent.key == Key.Enter) true else keyEvent.key == Key.NumPadEnter
                        val canSubmit = if (isKeyUp) isEnter else false
                        val shouldRunSubmit = if (canSubmit) onSubmit != null else false
                        if (shouldRunSubmit) {
                            onSubmit?.invoke()
                            true
                        } else {
                            false
                        }
                    }
                    .rfTrackValidationFocus(focusKey = plainKey) { isFocused ->
                        onFocusChanged?.invoke(isFocused)
                    },
                decorationBox = { inner ->
                    if (value.isEmpty()) {
                        BasicText(text = placeholder, style = typo.secondary.copy(fontSize = 13.5.sp, color = palette.muted))
                    }
                    inner()
                },
            )
        }
    }
}

@Composable
private fun DesktopGradientPillButton(
    label: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val typo = LocalRfTypography.current
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .height(34.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B6CF0))))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp),
    ) {
        BasicText(
            text = label,
            style = typo.buttonLabel.copy(
                color = if (enabled) Color.White else Color.White.copy(alpha = 0.65f),
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
    }
}

@Composable
private fun DesktopSmallPillButton(
    label: String,
    onClick: () -> Unit,
) {
    val typo = LocalRfTypography.current
    val palette = LocalRfPalette.current
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .height(34.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(palette.chipBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
    ) {
        BasicText(text = label, style = typo.buttonLabel.copy(color = palette.ink, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold))
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
    return "${parts.first()} ${parts.last()}"
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
