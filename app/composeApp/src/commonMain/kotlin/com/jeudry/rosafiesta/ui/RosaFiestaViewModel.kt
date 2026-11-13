package com.jeudry.rosafiesta.ui

import androidx.lifecycle.ViewModel
import com.jeudry.rosafiesta.core.engine.CancellationRefund
import com.jeudry.rosafiesta.core.engine.DateChangeResult
import com.jeudry.rosafiesta.core.engine.RosaFiestaEngine
import com.jeudry.rosafiesta.domain.ArticleAvailabilityCalendar
import com.jeudry.rosafiesta.domain.AuditLogRecord
import com.jeudry.rosafiesta.domain.BalloonProduct
import com.jeudry.rosafiesta.domain.BotFaqRecord
import com.jeudry.rosafiesta.domain.BotKeywordRecord
import com.jeudry.rosafiesta.domain.BotThreadRecord
import com.jeudry.rosafiesta.domain.BusinessMetrics
import com.jeudry.rosafiesta.domain.BusinessSettings
import com.jeudry.rosafiesta.domain.CanonicalSampleData
import com.jeudry.rosafiesta.domain.CatalogArticle
import com.jeudry.rosafiesta.domain.ChatMessageRecord
import com.jeudry.rosafiesta.domain.ChatThread
import com.jeudry.rosafiesta.domain.ChecklistStep
import com.jeudry.rosafiesta.domain.ClientEvent
import com.jeudry.rosafiesta.domain.ClientProfile
import com.jeudry.rosafiesta.domain.DamageIncident
import com.jeudry.rosafiesta.domain.DeliveryStop
import com.jeudry.rosafiesta.domain.DomainClockFormatter
import com.jeudry.rosafiesta.domain.EventGuest
import com.jeudry.rosafiesta.domain.EventLineItem
import com.jeudry.rosafiesta.domain.EventPackage
import com.jeudry.rosafiesta.domain.EventScheduleBlock
import com.jeudry.rosafiesta.domain.EventStatus
import com.jeudry.rosafiesta.domain.EventTypeTemplate
import com.jeudry.rosafiesta.domain.InsurancePolicyRecord
import com.jeudry.rosafiesta.domain.LeadRecord
import com.jeudry.rosafiesta.domain.NotificationRecord
import com.jeudry.rosafiesta.domain.OfflineQueueItem
import com.jeudry.rosafiesta.domain.PaymentRecord
import com.jeudry.rosafiesta.domain.QuoteRecord
import com.jeudry.rosafiesta.domain.RefundRequest
import com.jeudry.rosafiesta.domain.ReviewRecord
import com.jeudry.rosafiesta.domain.RosaChatReply
import com.jeudry.rosafiesta.domain.RosaFiestaRepository
import com.jeudry.rosafiesta.domain.StaffMember
import com.jeudry.rosafiesta.domain.SupplierRecord
import com.jeudry.rosafiesta.domain.platform.Dgii607SaleRecord
import com.jeudry.rosafiesta.domain.platform.DgiiIdentificationType
import com.jeudry.rosafiesta.domain.platform.FinancialReportCsvRow
import com.jeudry.rosafiesta.domain.platform.NoOpPlatformActions
import com.jeudry.rosafiesta.domain.platform.PickedMediaFile
import com.jeudry.rosafiesta.domain.platform.PlatformActions
import com.jeudry.rosafiesta.domain.platform.ReceiptPdfBuilder
import com.jeudry.rosafiesta.domain.platform.ReceiptPdfDocument
import com.jeudry.rosafiesta.domain.platform.ReceiptPdfLineItem
import com.jeudry.rosafiesta.domain.platform.ReportCsvBuilder
import com.jeudry.rosafiesta.domain.security.InMemorySecureStorage
import com.jeudry.rosafiesta.domain.security.RegisteredUserAccount
import com.jeudry.rosafiesta.domain.security.SecureStorage
import com.jeudry.rosafiesta.domain.security.SensitiveDataRedactor
import com.jeudry.rosafiesta.domain.sound.NoOpSoundPlayer
import com.jeudry.rosafiesta.domain.sound.NoOpVoiceInputMonitor
import com.jeudry.rosafiesta.domain.sound.SoundPlayer
import com.jeudry.rosafiesta.domain.sound.VoiceInputMonitor
import com.jeudry.rosafiesta.domain.validation.InputValidator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Origin context from which voice input capture is initiated.
 */
public enum class VoiceInputOrigin {
    NONE,
    SEARCH,
    ROSA_WIZARD,
}

public data class CompanyReviewUiModel(
    public val id: String,
    public val author: String,
    public val initial: String,
    public val dateLabel: String,
    public val rating: Int,
    public val comment: String,
    public val isVerified: Boolean = true,
    public val avatarBgHex: Long = 0xFFFDE6F2,
    public val avatarInkHex: Long = 0xFFC2338F,
)

public data class DirectChatMessageUiModel(
    public val id: String,
    public val senderName: String,
    public val text: String,
    public val timeLabel: String,
    public val isFromClient: Boolean,
    public val attachmentName: String = "",
)

public data class EventTimelineItemUiModel(
    public val id: String,
    public val startTime: String,
    public val endTime: String,
    public val title: String,
    public val subtitle: String,
    public val dotColorHex: Long,
    public val ringColorHex: Long,
)

public data class EventTaskUiModel(
    public val id: String,
    public val title: String,
    public val badgeText: String,
    public val isUrgent: Boolean,
    public val completed: Boolean,
)

public data class EventGuestUiModel(
    public val id: String,
    public val name: String,
    public val details: String,
    public val status: String,
)

public data class SupplierUiModel(
    public val id: String,
    public val name: String,
    public val category: String,
    public val phone: String,
    public val ratingLabel: String,
    public val statusLabel: String = "Confirmado",
) {
    public val initial: String
        get() = name.trim().take(1).uppercase().ifBlank { "P" }
}

public data class AdminOnboardingSummary(
    public val activeEventsCount: Int,
    public val leadEventTitle: String,
    public val leadEventDate: String,
    public val leadEventGuests: Int,
    public val leadEventVenue: String,
    public val leadEventItemsCount: Int,
    public val primaryArticleTitle: String,
    public val primaryArticleAvailableStock: Int,
    public val monthlyIncomeDop: Int,
    public val depositPendingDop: Int,
    public val totalArticlesInCatalog: Int,
)

/**
 * Shared multiplatform ViewModel for `PersonalApp` and `AdminApp`.
 * Coordinates reactive UI state, validation, RBAC, Rosa IA, native platform actions, and repository synchronization.
 */
public class RosaFiestaViewModel(
    private val repository: RosaFiestaRepository,
    public val soundPlayer: SoundPlayer = NoOpSoundPlayer(),
    private val secureStorage: SecureStorage = InMemorySecureStorage(),
    public val voiceInputMonitor: VoiceInputMonitor = NoOpVoiceInputMonitor(),
    public val platformActions: PlatformActions = NoOpPlatformActions(),
) : ViewModel() {
    public val articles: StateFlow<List<CatalogArticle>> = repository.articles
    public val events: StateFlow<List<ClientEvent>> = repository.events
    public val payments: StateFlow<List<PaymentRecord>> = repository.payments
    public val checklist: StateFlow<List<ChecklistStep>> = repository.checklist
    public val offlineQueue: StateFlow<List<OfflineQueueItem>> = repository.offlineQueue
    public val metrics: StateFlow<BusinessMetrics> = repository.metrics
    public val activeRole: StateFlow<String> = repository.activeRole
    public val currentUserEmail: StateFlow<String> = repository.currentUserEmail
    public val currentSessionJwt: StateFlow<String?> = repository.currentSessionJwt
    public val currentAuthenticatedUser: StateFlow<RegisteredUserAccount?> = repository.currentAuthenticatedUser
    public val registeredAccounts: StateFlow<List<RegisteredUserAccount>> = repository.registeredAccounts
    public val favorites: StateFlow<Set<String>> = repository.favorites
    public val staffMembers: StateFlow<List<StaffMember>> = repository.staffMembers
    public val chatMessages: StateFlow<List<String>> = repository.chatMessages

    public val packages: StateFlow<List<EventPackage>> = repository.packages
    public val templates: StateFlow<List<EventTypeTemplate>> = repository.templates
    public val balloonProducts: StateFlow<List<BalloonProduct>> = repository.balloonProducts
    public val clients: StateFlow<List<ClientProfile>> = repository.clients
    public val deliveryStops: StateFlow<List<DeliveryStop>> = repository.deliveryStops
    public val damageIncidents: StateFlow<List<DamageIncident>> = repository.damageIncidents
    public val quotes: StateFlow<List<QuoteRecord>> = repository.quotes
    public val refundRequests: StateFlow<List<RefundRequest>> = repository.refundRequests
    public val reviews: StateFlow<List<ReviewRecord>> = repository.reviews
    public val businessSettings: StateFlow<BusinessSettings> = repository.businessSettings
    public val suppliers: StateFlow<List<SupplierRecord>> = repository.suppliers
    public val leads: StateFlow<List<LeadRecord>> = repository.leads
    public val chatThreads: StateFlow<List<ChatThread>> = repository.chatThreads
    public val threadMessages: StateFlow<List<ChatMessageRecord>> = repository.threadMessages
    public val eventGuests: StateFlow<List<EventGuest>> = repository.eventGuests
    public val eventSchedule: StateFlow<List<EventScheduleBlock>> = repository.eventSchedule
    public val notifications: StateFlow<List<NotificationRecord>> = repository.notifications
    public val insurancePolicies: StateFlow<List<InsurancePolicyRecord>> = repository.insurancePolicies
    public val auditLogs: StateFlow<List<AuditLogRecord>> = repository.auditLogs
    public val botFaqs: StateFlow<List<BotFaqRecord>> = repository.botFaqs
    public val botKeywords: StateFlow<List<BotKeywordRecord>> = repository.botKeywords
    public val botThreads: StateFlow<List<BotThreadRecord>> = repository.botThreads

    private val securityScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _isExplicitGuestSession = MutableStateFlow(false)
    public val isExplicitGuestSession: StateFlow<Boolean> = _isExplicitGuestSession.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    public val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Todos")
    public val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _maxPriceFilterDop = MutableStateFlow(5000)
    public val maxPriceFilterDop: StateFlow<Int> = _maxPriceFilterDop.asStateFlow()

    private val _onlyAvailableFilter = MutableStateFlow(false)
    public val onlyAvailableFilter: StateFlow<Boolean> = _onlyAvailableFilter.asStateFlow()

    private val _searchSortOrder = MutableStateFlow("Más populares")
    public val searchSortOrder: StateFlow<String> = _searchSortOrder.asStateFlow()

    private val _selectedEventId = MutableStateFlow("evt-boda-real")
    public val selectedEventId: StateFlow<String> = _selectedEventId.asStateFlow()

    private val _selectedArticleId = MutableStateFlow("art-silla-tiffany")
    public val selectedArticleId: StateFlow<String> = _selectedArticleId.asStateFlow()

    private val _selectedPackageId = MutableStateFlow("pkg-boda-real")
    public val selectedPackageId: StateFlow<String> = _selectedPackageId.asStateFlow()

    private val _selectedClientId = MutableStateFlow("cli-camila")
    public val selectedClientId: StateFlow<String> = _selectedClientId.asStateFlow()

    private val _selectedQuoteId = MutableStateFlow("quo-boda-real")
    public val selectedQuoteId: StateFlow<String> = _selectedQuoteId.asStateFlow()

    private val _selectedChatThreadId = MutableStateFlow("thr-camila")
    public val selectedChatThreadId: StateFlow<String> = _selectedChatThreadId.asStateFlow()

    private val _selectedRefundId = MutableStateFlow("ref-rafael")
    public val selectedRefundId: StateFlow<String> = _selectedRefundId.asStateFlow()

    private val _selectedReviewId = MutableStateFlow("rev-ricardo")
    public val selectedReviewId: StateFlow<String> = _selectedReviewId.asStateFlow()

    private val _selectedLeadId = MutableStateFlow("lead-laura")
    public val selectedLeadId: StateFlow<String> = _selectedLeadId.asStateFlow()

    private val _paymentCheckoutMode = MutableStateFlow("DEPOSIT")
    public val paymentCheckoutMode: StateFlow<String> = _paymentCheckoutMode.asStateFlow()

    private val _inventoryPickerForQuote = MutableStateFlow(false)
    public val inventoryPickerForQuote: StateFlow<Boolean> = _inventoryPickerForQuote.asStateFlow()

    private val _editingArticleId = MutableStateFlow<String?>(null)
    public val editingArticleId: StateFlow<String?> = _editingArticleId.asStateFlow()

    private val _editingPackageId = MutableStateFlow<String?>(null)
    public val editingPackageId: StateFlow<String?> = _editingPackageId.asStateFlow()

    private val _voiceReturnScreenCode = MutableStateFlow("s30")
    public val voiceReturnScreenCode: StateFlow<String> = _voiceReturnScreenCode.asStateFlow()

    private val _lastAddedDraftItem = MutableStateFlow<Pair<String, Int>?>(null)
    public val lastAddedDraftItem: StateFlow<Pair<String, Int>?> = _lastAddedDraftItem.asStateFlow()

    private val _rosaOmittedCategories = MutableStateFlow<Set<String>>(emptySet())
    public val rosaOmittedCategories: StateFlow<Set<String>> = _rosaOmittedCategories.asStateFlow()

    private val _selectedArticleVariantColors = MutableStateFlow<Map<String, String>>(emptyMap())
    public val selectedArticleVariantColors: StateFlow<Map<String, String>> = _selectedArticleVariantColors.asStateFlow()

    private val _pickedAttachmentFile = MutableStateFlow<PickedMediaFile?>(null)
    public val pickedAttachmentFile: StateFlow<PickedMediaFile?> = _pickedAttachmentFile.asStateFlow()

    private val _draftDiscountDop = MutableStateFlow(0)
    public val draftDiscountDop: StateFlow<Int> = _draftDiscountDop.asStateFlow()

    private val _draftQuantities = MutableStateFlow<Map<String, Int>>(emptyMap())
    public val draftQuantities: StateFlow<Map<String, Int>> = _draftQuantities.asStateFlow()

    private val _rosaEventType = MutableStateFlow("Boda Real Rosa & Dorado")
    public val rosaEventType: StateFlow<String> = _rosaEventType.asStateFlow()

    private val _rosaGuestsCount = MutableStateFlow(120)
    public val rosaGuestsCount: StateFlow<Int> = _rosaGuestsCount.asStateFlow()
    public val rosaGuests: StateFlow<Int> = _rosaGuestsCount.asStateFlow()

    private val _rosaBudgetDop = MutableStateFlow(4500)
    public val rosaBudgetDop: StateFlow<Int> = _rosaBudgetDop.asStateFlow()

    private val _rosaPalette = MutableStateFlow("Rosa & Dorado")
    public val rosaPalette: StateFlow<String> = _rosaPalette.asStateFlow()

    private val _rosaProposalMessage = MutableStateFlow("")
    public val rosaProposalMessage: StateFlow<String> = _rosaProposalMessage.asStateFlow()

    private val _clientEventTitleInput = MutableStateFlow("")
    public val clientEventTitleInput: StateFlow<String> = _clientEventTitleInput.asStateFlow()

    private val _guestsWizardMode = MutableStateFlow(false)
    public val guestsWizardMode: StateFlow<Boolean> = _guestsWizardMode.asStateFlow()

    private val _voiceAppendToIdea = MutableStateFlow(false)
    public val voiceAppendToIdea: StateFlow<Boolean> = _voiceAppendToIdea.asStateFlow()

    private val _wizardScheduleItems = MutableStateFlow<List<EventTimelineItemUiModel>>(emptyList())
    public val wizardScheduleItems: StateFlow<List<EventTimelineItemUiModel>> = _wizardScheduleItems.asStateFlow()

    private val _calendarSyncPromptDismissedIds = MutableStateFlow<Set<String>>(emptySet())
    public val calendarSyncPromptDismissedIds: StateFlow<Set<String>> = _calendarSyncPromptDismissedIds.asStateFlow()

    private val _voiceInputOrigin = MutableStateFlow(VoiceInputOrigin.NONE)
    public val voiceInputOrigin: StateFlow<VoiceInputOrigin> = _voiceInputOrigin.asStateFlow()

    public val isVoiceListening: StateFlow<Boolean> = voiceInputMonitor.isListening
    public val isVoiceTranscribing: StateFlow<Boolean> = voiceInputMonitor.isTranscribing
    public val voiceAmplitude: StateFlow<Float> = voiceInputMonitor.amplitudeLevel
    public val hasHardwareMicrophone: StateFlow<Boolean> = voiceInputMonitor.hasHardwareMicrophone
    public val voiceErrorMessage: StateFlow<String?> = voiceInputMonitor.errorMessage

    private val _voiceTranscript = MutableStateFlow("")
    public val voiceTranscript: StateFlow<String> = _voiceTranscript.asStateFlow()

    private val _loginEmail = MutableStateFlow("")
    public val loginEmail: StateFlow<String> = _loginEmail.asStateFlow()

    private val _loginPassword = MutableStateFlow("")
    public val loginPassword: StateFlow<String> = _loginPassword.asStateFlow()

    private val _registerName = MutableStateFlow("")
    public val registerName: StateFlow<String> = _registerName.asStateFlow()

    private val _registerEmail = MutableStateFlow("")
    public val registerEmail: StateFlow<String> = _registerEmail.asStateFlow()

    private val _registerPhone = MutableStateFlow("")
    public val registerPhone: StateFlow<String> = _registerPhone.asStateFlow()

    private val _eventTypeInput = MutableStateFlow("")
    public val eventTypeInput: StateFlow<String> = _eventTypeInput.asStateFlow()

    private val _eventDateInput = MutableStateFlow("")
    public val eventDateInput: StateFlow<String> = _eventDateInput.asStateFlow()

    private val _eventVenueInput = MutableStateFlow("")
    public val eventVenueInput: StateFlow<String> = _eventVenueInput.asStateFlow()

    private val _eventGuestsInput = MutableStateFlow("")
    public val eventGuestsInput: StateFlow<String> = _eventGuestsInput.asStateFlow()

    private val _eventNotesInput = MutableStateFlow("")
    public val eventNotesInput: StateFlow<String> = _eventNotesInput.asStateFlow()

    private val _newEventTitle = MutableStateFlow("Quinceañera Cristal")
    public val newEventTitle: StateFlow<String> = _newEventTitle.asStateFlow()

    private val _newEventClient = MutableStateFlow("Sofía Méndez")
    public val newEventClient: StateFlow<String> = _newEventClient.asStateFlow()

    private val _newEventDate = MutableStateFlow("Sáb 14 nov")
    public val newEventDate: StateFlow<String> = _newEventDate.asStateFlow()

    private val _newEventVenue = MutableStateFlow("Salón Piantini")
    public val newEventVenue: StateFlow<String> = _newEventVenue.asStateFlow()

    private val _newEventGuests = MutableStateFlow("100")
    public val newEventGuests: StateFlow<String> = _newEventGuests.asStateFlow()

    private val _paymentAmountInput = MutableStateFlow("3150")
    public val paymentAmountInput: StateFlow<String> = _paymentAmountInput.asStateFlow()

    private val _paymentMethodInput = MutableStateFlow("Transferencia Banreservas")
    public val paymentMethodInput: StateFlow<String> = _paymentMethodInput.asStateFlow()

    private val _paymentRefInput = MutableStateFlow("BR-2026-9941")
    public val paymentRefInput: StateFlow<String> = _paymentRefInput.asStateFlow()

    private val _dateChangeInput = MutableStateFlow("Vie 30 oct")
    public val dateChangeInput: StateFlow<String> = _dateChangeInput.asStateFlow()

    private val _dateChangeBanner = MutableStateFlow("Primer cambio gratis con al menos 48h de antelación.")
    public val dateChangeBanner: StateFlow<String> = _dateChangeBanner.asStateFlow()

    private val _profileName = MutableStateFlow("Invitado Rosa Fiesta")
    public val profileName: StateFlow<String> = _profileName.asStateFlow()

    private val _profileEmail = MutableStateFlow("cliente@rosafiesta.do")
    public val profileEmail: StateFlow<String> = _profileEmail.asStateFlow()

    private val _profileUsername = MutableStateFlow("cliente")
    public val profileUsername: StateFlow<String> = _profileUsername.asStateFlow()

    private val _profileBirthDate = MutableStateFlow("")
    public val profileBirthDate: StateFlow<String> = _profileBirthDate.asStateFlow()

    private val _profilePhone = MutableStateFlow("809-555-0142")
    public val profilePhone: StateFlow<String> = _profilePhone.asStateFlow()

    private val _profileZone = MutableStateFlow("Piantini, Santo Domingo DN")
    public val profileZone: StateFlow<String> = _profileZone.asStateFlow()

    private val _profileAvatarKey = MutableStateFlow<String?>(null)
    public val profileAvatarKey: StateFlow<String?> = _profileAvatarKey.asStateFlow()

    private val _profileAvatarBytes = MutableStateFlow<ByteArray?>(null)
    public val profileAvatarBytes: StateFlow<ByteArray?> = _profileAvatarBytes.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(false)
    public val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _preferredLanguage = MutableStateFlow("Español")
    public val preferredLanguage: StateFlow<String> = _preferredLanguage.asStateFlow()

    private val _remindersEnabled = MutableStateFlow(true)
    public val remindersEnabled: StateFlow<Boolean> = _remindersEnabled.asStateFlow()

    private val _savedPaymentMethod = MutableStateFlow("Transferencia Banreservas")
    public val savedPaymentMethod: StateFlow<String> = _savedPaymentMethod.asStateFlow()

    private val _sketchTableCount = MutableStateFlow(6)
    public val sketchTableCount: StateFlow<Int> = _sketchTableCount.asStateFlow()

    private val _sketchLayoutJson = MutableStateFlow("")
    public val sketchLayoutJson: StateFlow<String> = _sketchLayoutJson.asStateFlow()

    private val _activeOtpCode = MutableStateFlow<String?>(null)
    public val activeOtpCode: StateFlow<String?> = _activeOtpCode.asStateFlow()

    private val _verifiedOtpResetToken = MutableStateFlow("")
    public val verifiedOtpResetToken: StateFlow<String> = _verifiedOtpResetToken.asStateFlow()

    private val _pendingOtpEmail = MutableStateFlow("")
    public val pendingOtpEmail: StateFlow<String> = _pendingOtpEmail.asStateFlow()

    private val _eventTimelineItems = MutableStateFlow(
        listOf(
            EventTimelineItemUiModel(
                id = "tl-1",
                startTime = "06:38",
                endTime = "09:08",
                title = "Descarga y montaje de mobiliario",
                subtitle = "Llega el camión con sillas Tiffany y mesas",
                dotColorHex = 0xFFF7B500,
                ringColorHex = 0xFFFFF2C9,
            ),
            EventTimelineItemUiModel(
                id = "tl-2",
                startTime = "09:38",
                endTime = "12:38",
                title = "Arco orgánico y flores",
                subtitle = "Montaje del backdrop principal y centros de mesa",
                dotColorHex = 0xFFEA4EA9,
                ringColorHex = 0xFFFDE6F2,
            ),
            EventTimelineItemUiModel(
                id = "tl-3",
                startTime = "13:08",
                endTime = "14:38",
                title = "Pruebas de sonido e iluminación",
                subtitle = "Microfonía, luces ambientales y playlist lista",
                dotColorHex = 0xFF1FBDC0,
                ringColorHex = 0xFFDDF4F3,
            ),
            EventTimelineItemUiModel(
                id = "tl-4",
                startTime = "15:38",
                endTime = "16:38",
                title = "Recepción de invitados",
                subtitle = "Cóctel de bienvenida en el área de la terraza",
                dotColorHex = 0xFF865DEC,
                ringColorHex = 0xFFEFE8FD,
            ),
        ),
    )
    public val eventTimelineItems: StateFlow<List<EventTimelineItemUiModel>> = _eventTimelineItems.asStateFlow()

    private val _eventTasksList = MutableStateFlow(
        listOf(
            EventTaskUiModel("tsk-1", "Realizar pago inicial de reserva (30%)", "Vence hoy", isUrgent = true, completed = false),
            EventTaskUiModel("tsk-2", "Inspección técnica de iluminación exterior", "Mañana", isUrgent = false, completed = false),
            EventTaskUiModel("tsk-3", "Confirmar paleta floral blanco y rosa pastel", "18 oct", isUrgent = false, completed = true),
            EventTaskUiModel("tsk-4", "Aprobar plano de mesas imperial y sillas Tiffany", "15 oct", isUrgent = false, completed = true),
        ),
    )
    public val eventTasksList: StateFlow<List<EventTaskUiModel>> = _eventTasksList.asStateFlow()

    private val _eventGuestsList = MutableStateFlow(
        listOf(
            EventGuestUiModel("gst-1", "Familia Pérez Martínez", "Confirmados · Mesa 2 · 4 personas", "Confirmado"),
            EventGuestUiModel("gst-2", "Carlos & Andrea Sánchez", "Confirmados · Mesa 1 · 2 personas", "Confirmado"),
            EventGuestUiModel("gst-3", "Dra. Laura Gómez", "Pendiente respuesta · 1 persona", "Pendiente"),
            EventGuestUiModel("gst-4", "Lic. Roberto Fernández", "Confirmado · Mesa 4 · 2 personas", "Confirmado"),
        ),
    )
    public val eventGuestsList: StateFlow<List<EventGuestUiModel>> = _eventGuestsList.asStateFlow()

    private val _companyReviews = MutableStateFlow(
        listOf(
            CompanyReviewUiModel(
                id = "rev-1",
                author = "Camila R.",
                initial = "C",
                dateLabel = "18 oct 2026",
                rating = 5,
                comment = "El montaje de nuestra boda superó todas las expectativas. Las sillas Tiffany y el arco floral quedaron de revista.",
                isVerified = true,
            ),
            CompanyReviewUiModel(
                id = "rev-2",
                author = "Laura M.",
                initial = "L",
                dateLabel = "15 oct 2026",
                rating = 5,
                comment = "Puntualidad impecable y atención personalizada con Rosa IA. ¡Recomendados 100%!",
                isVerified = true,
            ),
        ),
    )
    public val companyReviews: StateFlow<List<CompanyReviewUiModel>> = _companyReviews.asStateFlow()

    private val _suppliersList = MutableStateFlow(
        listOf(
            SupplierUiModel("sup-1", "Flores del Caribe", "Floristería", "809-555-0191", "4.9", "Confirmado"),
            SupplierUiModel("sup-2", "Sonido & Luces Pro", "Audio e iluminación", "809-555-0244", "4.8", "Confirmado"),
            SupplierUiModel("sup-3", "Dulce Fantasía", "Repostería", "809-555-0318", "5.0", "Cotizando"),
            SupplierUiModel("sup-4", "Transporte Veloz", "Logística", "809-555-0412", "4.7", "Confirmado"),
        ),
    )
    public val suppliersList: StateFlow<List<SupplierUiModel>> = _suppliersList.asStateFlow()

    private val _directChatMessages = MutableStateFlow(
        listOf(
            DirectChatMessageUiModel(
                id = "msg-1",
                senderName = "María · Rosa Fiesta",
                text = "¡Hola! Soy María, directora de Rosa Fiesta 🌸 Estoy en línea para ayudarte con tu cotización, fechas o detalles de montaje.",
                timeLabel = "10:14",
                isFromClient = false,
            ),
            DirectChatMessageUiModel(
                id = "msg-2",
                senderName = "Cliente",
                text = "¡Hola equipo Rosa Fiesta! ¿Podemos incluir 20 sillas Tiffany doradas adicionales para el área del brindis?",
                timeLabel = "10:16",
                isFromClient = true,
            ),
            DirectChatMessageUiModel(
                id = "msg-3",
                senderName = "María · Rosa Fiesta",
                text = "¡Claro que sí! Tenemos disponibilidad inmediata de sillas Tiffany doradas (RD$ 4/día). Dime qué más deseas ajustar y te actualizo el expediente al instante.",
                timeLabel = "10:17",
                isFromClient = false,
            ),
        ),
    )
    public val directChatMessages: StateFlow<List<DirectChatMessageUiModel>> = _directChatMessages.asStateFlow()

    private val _supportSubject = MutableStateFlow("Ajuste de sillas doradas")
    public val supportSubject: StateFlow<String> = _supportSubject.asStateFlow()

    private val _supportMessage = MutableStateFlow("Necesitamos confirmar 10 sillas adicionales para la terraza.")
    public val supportMessage: StateFlow<String> = _supportMessage.asStateFlow()

    private val _chatInput = MutableStateFlow("")
    public val chatInput: StateFlow<String> = _chatInput.asStateFlow()

    private val _newArticleCode = MutableStateFlow("RF-007")
    public val newArticleCode: StateFlow<String> = _newArticleCode.asStateFlow()

    private val _newArticleName = MutableStateFlow("Silla Chiavari Cristal")
    public val newArticleName: StateFlow<String> = _newArticleName.asStateFlow()

    private val _newArticleCategory = MutableStateFlow("Mobiliario")
    public val newArticleCategory: StateFlow<String> = _newArticleCategory.asStateFlow()

    private val _newArticlePrice = MutableStateFlow("120")
    public val newArticlePrice: StateFlow<String> = _newArticlePrice.asStateFlow()

    private val _newArticleStock = MutableStateFlow("60")
    public val newArticleStock: StateFlow<String> = _newArticleStock.asStateFlow()

    private val _promoCodeInput = MutableStateFlow("ROSA10")
    public val promoCodeInput: StateFlow<String> = _promoCodeInput.asStateFlow()

    private val _promoDiscountInput = MutableStateFlow("10")
    public val promoDiscountInput: StateFlow<String> = _promoDiscountInput.asStateFlow()

    private val _depositPercentInput = MutableStateFlow("30")
    public val depositPercentInput: StateFlow<String> = _depositPercentInput.asStateFlow()

    private val _deliveryBaseInput = MutableStateFlow("350")
    public val deliveryBaseInput: StateFlow<String> = _deliveryBaseInput.asStateFlow()

    private val _cancellationDaysInput = MutableStateFlow("7")
    public val cancellationDaysInput: StateFlow<String> = _cancellationDaysInput.asStateFlow()

    private val _pushNotificationsEnabled = MutableStateFlow(true)
    public val pushNotificationsEnabled: StateFlow<Boolean> = _pushNotificationsEnabled.asStateFlow()
    public val notificationsEnabled: StateFlow<Boolean> = _pushNotificationsEnabled.asStateFlow()

    private val _whatsappAlertsEnabled = MutableStateFlow(true)
    public val whatsappAlertsEnabled: StateFlow<Boolean> = _whatsappAlertsEnabled.asStateFlow()
    public val whatsappBotEnabled: StateFlow<Boolean> = _whatsappAlertsEnabled.asStateFlow()

    private val _emailReceiptsEnabled = MutableStateFlow(true)
    public val emailReceiptsEnabled: StateFlow<Boolean> = _emailReceiptsEnabled.asStateFlow()

    private val _biometricLockEnabled = MutableStateFlow(false)
    public val biometricLockEnabled: StateFlow<Boolean> = _biometricLockEnabled.asStateFlow()

    private val _autoSyncWifiOnly = MutableStateFlow(false)
    public val autoSyncWifiOnly: StateFlow<Boolean> = _autoSyncWifiOnly.asStateFlow()

    private val _soundEffectsEnabled = MutableStateFlow(true)
    public val soundEffectsEnabled: StateFlow<Boolean> = _soundEffectsEnabled.asStateFlow()

    private val _formError = MutableStateFlow<String?>(null)
    public val formError: StateFlow<String?> = _formError.asStateFlow()

    private val _formFeedback = MutableStateFlow<String?>(null)
    public val formFeedback: StateFlow<String?> = _formFeedback.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    public val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    init {
        restorePersistedAccountOnStartup()
    }

    private fun restorePersistedAccountOnStartup() {
        val currentRepoUser = repository.currentAuthenticatedUser.value
        if (currentRepoUser != null) {
            applyProfileFieldsFromAccount(currentRepoUser)
        }
        val settings = repository.businessSettings.value
        _depositPercentInput.value = settings.depositPercent.toString()
        val firstZoneFee = settings.zones.firstOrNull()?.feeDop ?: 350
        _deliveryBaseInput.value = firstZoneFee.toString()

        securityScope.launch {
            val savedToken = secureStorage.get(SecureStorage.KEY_ACCESS_TOKEN)
            if (!savedToken.isNullOrBlank()) {
                val restoredAccount = repository.restoreSessionFromJwt(savedToken)
                if (restoredAccount != null) {
                    applyProfileFieldsFromAccount(restoredAccount)
                } else {
                    secureStorage.remove(SecureStorage.KEY_ACCESS_TOKEN)
                }
            }
            val savedDark = secureStorage.get(SecureStorage.KEY_DARK_THEME)
            if (savedDark != null) {
                val darkEnabled = savedDark == "true"
                _isDarkTheme.value = darkEnabled
                com.jeudry.rosafiesta.designsystem.RfThemeState.isPersonalDark = darkEnabled
            }
            val savedAvatar = secureStorage.get(SecureStorage.KEY_AVATAR_KEY)
            if (!savedAvatar.isNullOrBlank()) {
                _profileAvatarKey.value = savedAvatar
                if (savedAvatar.startsWith("CUSTOM:")) {
                    val savedImageData = secureStorage.get(SecureStorage.KEY_AVATAR_IMAGE_DATA)
                    if (!savedImageData.isNullOrBlank()) {
                        _profileAvatarBytes.value = runCatching { Base64.decode(savedImageData) }.getOrNull()
                    }
                }
            }
            val savedLang = secureStorage.get(SecureStorage.KEY_LANGUAGE)
            if (!savedLang.isNullOrBlank()) {
                _preferredLanguage.value = savedLang
            }
            val savedReminders = secureStorage.get(SecureStorage.KEY_REMINDERS)
            if (savedReminders != null) {
                _remindersEnabled.value = savedReminders == "true"
            }
            val savedMethod = secureStorage.get(SecureStorage.KEY_PAYMENT_METHOD)
            if (!savedMethod.isNullOrBlank()) {
                _savedPaymentMethod.value = savedMethod
            }
            val savedTables = secureStorage.get(SecureStorage.KEY_SKETCH_TABLES)?.toIntOrNull()
            if (savedTables != null) {
                _sketchTableCount.value = savedTables.coerceIn(2, 12)
            }
            val savedPush = secureStorage.get("rf_pref_push")
            if (savedPush != null) {
                _pushNotificationsEnabled.value = savedPush == "true"
            }
            val savedWhatsapp = secureStorage.get("rf_pref_whatsapp")
            if (savedWhatsapp != null) {
                _whatsappAlertsEnabled.value = savedWhatsapp == "true"
            }
            val savedEmailReceipts = secureStorage.get("rf_pref_email_receipts")
            if (savedEmailReceipts != null) {
                _emailReceiptsEnabled.value = savedEmailReceipts == "true"
            }
            val savedBiometricLock = secureStorage.get("rf_pref_biometric_lock")
            if (savedBiometricLock != null) {
                _biometricLockEnabled.value = savedBiometricLock == "true"
            }
            val savedAutoSyncWifi = secureStorage.get("rf_pref_auto_sync_wifi")
            if (savedAutoSyncWifi != null) {
                _autoSyncWifiOnly.value = savedAutoSyncWifi == "true"
            }
            val savedAddress = secureStorage.get("rf_pref_zone")
            if (!savedAddress.isNullOrBlank()) {
                _profileZone.value = savedAddress
            }
            val savedSketchJson = secureStorage.get("rf_sketch_layout_json")
            if (!savedSketchJson.isNullOrBlank()) {
                _sketchLayoutJson.value = savedSketchJson
            }
            val savedReviewsRaw = secureStorage.get(SecureStorage.KEY_COMPANY_REVIEWS)
            if (!savedReviewsRaw.isNullOrBlank()) {
                val parsedReviews = parseReviewsFromStorage(savedReviewsRaw)
                if (parsedReviews.isNotEmpty()) {
                    _companyReviews.value = parsedReviews
                }
            }
            val savedChatRaw = secureStorage.get(SecureStorage.KEY_DIRECT_CHAT)
            if (!savedChatRaw.isNullOrBlank()) {
                val parsedChat = parseChatFromStorage(savedChatRaw)
                if (parsedChat.isNotEmpty()) {
                    _directChatMessages.value = parsedChat
                }
            }
            val savedSuppliersRaw = secureStorage.get(SecureStorage.KEY_SUPPLIERS)
            if (!savedSuppliersRaw.isNullOrBlank()) {
                val parsedSuppliers = parseSuppliersFromStorage(savedSuppliersRaw)
                if (parsedSuppliers.isNotEmpty()) {
                    _suppliersList.value = parsedSuppliers
                }
            }
        }
    }

    public fun refreshSharedStateFromDisk() {
        repository.refreshSharedState()
        securityScope.launch {
            val savedReviewsRaw = secureStorage.get(SecureStorage.KEY_COMPANY_REVIEWS)
            if (!savedReviewsRaw.isNullOrBlank()) {
                val parsedReviews = parseReviewsFromStorage(savedReviewsRaw)
                if (parsedReviews.isNotEmpty()) {
                    _companyReviews.value = parsedReviews
                }
            }
            val savedChatRaw = secureStorage.get(SecureStorage.KEY_DIRECT_CHAT)
            if (!savedChatRaw.isNullOrBlank()) {
                val parsedChat = parseChatFromStorage(savedChatRaw)
                if (parsedChat.isNotEmpty()) {
                    _directChatMessages.value = parsedChat
                }
            }
            val savedSuppliersRaw = secureStorage.get(SecureStorage.KEY_SUPPLIERS)
            if (!savedSuppliersRaw.isNullOrBlank()) {
                val parsedSuppliers = parseSuppliersFromStorage(savedSuppliersRaw)
                if (parsedSuppliers.isNotEmpty()) {
                    _suppliersList.value = parsedSuppliers
                }
            }
            val savedTables = secureStorage.get(SecureStorage.KEY_SKETCH_TABLES)?.toIntOrNull()
            if (savedTables != null) {
                _sketchTableCount.value = savedTables.coerceIn(2, 12)
            }
        }
    }

    public fun syncWithServer(onComplete: (Boolean) -> Unit = {}) {
        securityScope.launch {
            val synced = repository.syncFromBackendAsync()
            if (synced) {
                _formFeedback.value = "Datos sincronizados con el servidor."
                _formError.value = null
            } else {
                _formError.value = "No se pudo conectar al servidor. Mostrando datos locales."
            }
            onComplete(synced)
        }
    }

    private fun parseSuppliersFromStorage(serialized: String): List<SupplierUiModel> = serialized.split("||").mapNotNull { entry ->
        val parts = entry.split("::")
        if (parts.size >= 6) {
            SupplierUiModel(
                id = parts[0],
                name = parts[1],
                category = parts[2],
                phone = parts[3],
                ratingLabel = parts[4],
                statusLabel = parts[5],
            )
        } else {
            null
        }
    }

    private fun serializeSuppliersForStorage(suppliers: List<SupplierUiModel>): String = suppliers.joinToString("||") { supplierItem ->
        "${supplierItem.id}::${supplierItem.name.replace("::", " ")}::${supplierItem.category.replace("::", " ")}::${supplierItem.phone.replace("::", " ")}::${supplierItem.ratingLabel}::${supplierItem.statusLabel}"
    }

    private fun parseReviewsFromStorage(serialized: String): List<CompanyReviewUiModel> = serialized.split("||").mapNotNull { entry ->
        val parts = entry.split("::")
        if (parts.size >= 6) {
            val verifiedFlag = if (parts.size >= 7) parts[6] == "true" else true
            CompanyReviewUiModel(
                id = parts[0],
                author = parts[1],
                initial = parts[2],
                dateLabel = parts[3],
                rating = parts[4].toIntOrNull() ?: 5,
                comment = parts[5],
                isVerified = verifiedFlag,
            )
        } else {
            null
        }
    }

    private fun serializeReviewsForStorage(reviews: List<CompanyReviewUiModel>): String = reviews.joinToString("||") { reviewItem ->
        val safeComment = reviewItem.comment.replace("::", " ").replace("||", " ")
        "${reviewItem.id}::${reviewItem.author}::${reviewItem.initial}::${reviewItem.dateLabel}::${reviewItem.rating}::$safeComment::${reviewItem.isVerified}"
    }

    private fun parseChatFromStorage(serialized: String): List<DirectChatMessageUiModel> = serialized.split("||").mapNotNull { entry ->
        val parts = entry.split("::")
        if (parts.size >= 5) {
            val attachment = if (parts.size >= 6) parts[5] else ""
            DirectChatMessageUiModel(
                id = parts[0],
                senderName = parts[1],
                text = parts[2],
                timeLabel = parts[3],
                isFromClient = parts[4] == "true",
                attachmentName = attachment,
            )
        } else {
            null
        }
    }

    private fun serializeChatForStorage(messages: List<DirectChatMessageUiModel>): String = messages.joinToString("||") { msgItem ->
        val safeBody = msgItem.text.replace("::", " ").replace("||", " ")
        val safeAttachment = msgItem.attachmentName.replace("::", " ").replace("||", " ")
        "${msgItem.id}::${msgItem.senderName}::$safeBody::${msgItem.timeLabel}::${msgItem.isFromClient}::$safeAttachment"
    }

    private fun applyProfileFieldsFromAccount(account: RegisteredUserAccount) {
        _isExplicitGuestSession.value = false
        _profileName.value = account.fullName
        _profileEmail.value = account.email
        _profileUsername.value = account.username
        if (account.birthDate.isNotEmpty()) {
            _profileBirthDate.value = account.birthDate
        }
        if (account.phone.isNotEmpty()) {
            _profilePhone.value = account.phone
        }
    }

    public fun enterGuestMode() {
        soundPlayer.playTap()
        _isExplicitGuestSession.value = true
        repository.clearSession()
        _profileName.value = "Invitado"
        _profileEmail.value = ""
        _profileUsername.value = "invitado"
        _formError.value = null
    }

    public fun hasValidAuthenticatedSession(): Boolean {
        val account = currentAuthenticatedUser.value ?: return false
        val token = currentSessionJwt.value
        return !token.isNullOrBlank() || account.email.isNotBlank()
    }

    public fun isCurrentSessionAdminOrStaff(): Boolean {
        val normalizedRole = activeRole.value.trim().lowercase()
        if (normalizedRole == "admin") return true
        return normalizedRole.startsWith("staff")
    }

    public fun setSearchQuery(value: String) {
        _searchQuery.value = value
    }

    public fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    public fun setMaxPriceFilterDop(priceDop: Int) {
        _maxPriceFilterDop.value = priceDop.coerceAtLeast(1)
    }

    public fun setOnlyAvailableFilter(enabled: Boolean) {
        _onlyAvailableFilter.value = enabled
    }

    public fun setSearchSortOrder(order: String) {
        _searchSortOrder.value = order
    }

    public fun resetCatalogFilters() {
        _selectedCategory.value = "Todos"
        _maxPriceFilterDop.value = 5000
        _onlyAvailableFilter.value = false
        _searchSortOrder.value = "Más populares"
    }

    public fun filteredCatalogArticles(
        categoryOverride: String? = null,
        queryOverride: String? = null,
        includeHidden: Boolean = false,
    ): List<CatalogArticle> {
        val activeCategory = (categoryOverride ?: _selectedCategory.value).trim()
        val activeQuery = (queryOverride ?: _searchQuery.value).trim().lowercase()
        val maxPrice = _maxPriceFilterDop.value
        val onlyAvailable = _onlyAvailableFilter.value
        val sortMode = _searchSortOrder.value.trim().lowercase()

        val filtered = articles.value.filter { article ->
            val visibleOk = if (includeHidden) true else article.visibleInCatalog
            val categoryOk = matchesCatalogCategory(article, activeCategory)
            val priceOk = article.pricePerDayDop <= maxPrice
            val stockOk = if (onlyAvailable) article.availableOct23 > 0 else true
            val queryOk = if (activeQuery.isEmpty()) {
                true
            } else {
                val matchesName = article.name.lowercase().contains(activeQuery)
                val matchesCategory = article.category.lowercase().contains(activeQuery)
                val matchesCode = article.code.lowercase().contains(activeQuery)
                val matchesDescription = article.description.lowercase().contains(activeQuery)
                matchesSingleOr(matchesName, matchesCategory, matchesCode, matchesDescription)
            }
            allConditionsTrue(visibleOk, categoryOk, priceOk, stockOk, queryOk)
        }

        return when {
            sortMode.contains("menor") -> filtered.sortedBy { item -> item.pricePerDayDop }
            sortMode.contains("mayor") -> filtered.sortedByDescending { item -> item.pricePerDayDop }
            sortMode.contains("alfab") -> filtered.sortedBy { item -> item.name }
            else -> filtered.sortedByDescending { item -> item.totalStock }
        }
    }

    private fun matchesCatalogCategory(article: CatalogArticle, selectedCat: String): Boolean {
        val normalizedSelected = selectedCat.trim().lowercase()
        if (normalizedSelected.isEmpty()) return true
        if (normalizedSelected == "todos") return true
        if (normalizedSelected == "todas") return true

        val articleCat = article.category.trim().lowercase()
        val articleName = article.name.trim().lowercase()
        if (articleCat.equals(normalizedSelected, ignoreCase = true)) return true

        return when {
            normalizedSelected.contains("silla") -> articleName.contains("silla")

            normalizedSelected.contains("mesa") -> articleName.contains("mesa")

            normalizedSelected.contains("floral") || normalizedSelected.contains("flor") -> articleCat.contains("floral")

            normalizedSelected.contains("globo") || normalizedSelected.contains("arco") -> articleCat.contains("globo")

            normalizedSelected.contains("estaci") || normalizedSelected.contains("dulce") -> articleCat.contains("estaci")

            normalizedSelected.contains("decoraci") -> {
                val isFloral = articleCat.contains("floral")
                val isGlobo = articleCat.contains("globo")
                val isDecor = articleCat.contains("decor")
                matchesSingleOr(isFloral, isGlobo, isDecor, false)
            }

            normalizedSelected.contains("mantel") -> {
                val isMantel = articleCat.contains("mantel")
                val isNameMantel = articleName.contains("mantel")
                matchesSingleOr(isMantel, isNameMantel, false, false)
            }

            normalizedSelected.contains("ilumin") -> {
                val isLightCat = articleCat.contains("ilumin")
                val isLightName = articleName.contains("luz") || articleName.contains("lámpara") || articleName.contains("lampara")
                matchesSingleOr(isLightCat, isLightName, false, false)
            }

            normalizedSelected.contains("letrer") -> {
                val isSignCat = articleCat.contains("letrer")
                val isSignName = articleName.contains("letrero") || articleName.contains("neón") || articleName.contains("neon")
                matchesSingleOr(isSignCat, isSignName, false, false)
            }

            else -> articleCat.contains(normalizedSelected)
        }
    }

    private fun matchesSingleOr(first: Boolean, second: Boolean, third: Boolean, fourth: Boolean): Boolean {
        if (first) return true
        if (second) return true
        if (third) return true
        return fourth
    }

    private fun allConditionsTrue(
        first: Boolean,
        second: Boolean,
        third: Boolean,
        fourth: Boolean,
        fifth: Boolean = true,
    ): Boolean {
        if (!first) return false
        if (!second) return false
        if (!third) return false
        if (!fourth) return false
        return fifth
    }

    public fun clearFormError() {
        _formError.value = null
    }

    public fun clearFormFeedback() {
        _formFeedback.value = null
    }

    public fun postFeedback(message: String) {
        _formFeedback.value = message.trim()
        _formError.value = null
    }

    public fun postError(message: String) {
        soundPlayer.playAlertError()
        _formError.value = message.trim()
    }

    public fun selectEvent(eventId: String) {
        _selectedEventId.value = eventId
    }

    public fun selectArticle(articleId: String) {
        _selectedArticleId.value = articleId
    }

    public fun selectPackage(packageId: String) {
        _selectedPackageId.value = packageId
    }

    public fun selectClient(clientId: String) {
        _selectedClientId.value = clientId
    }

    public fun selectedClient(): ClientProfile? = clients.value.firstOrNull { client -> client.id == _selectedClientId.value }
        ?: clients.value.firstOrNull()

    public fun selectQuote(quoteId: String) {
        _selectedQuoteId.value = quoteId
    }

    public fun selectedQuote(): QuoteRecord? = quotes.value.firstOrNull { quote -> quote.id == _selectedQuoteId.value }
        ?: quotes.value.firstOrNull()

    public fun selectChatThread(threadId: String) {
        _selectedChatThreadId.value = threadId
    }

    public fun selectChatThreadForContact(contactName: String, eventId: String = "") {
        val cleanContact = contactName.trim()
        val matched = chatThreads.value.firstOrNull { thread ->
            val nameMatches = thread.contactName.equals(cleanContact, ignoreCase = true)
            if (nameMatches) {
                true
            } else if (eventId.isNotBlank()) {
                thread.eventId == eventId
            } else {
                thread.contactName.contains(cleanContact, ignoreCase = true)
            }
        }
        if (matched != null) {
            _selectedChatThreadId.value = matched.id
        }
    }

    public fun selectedChatThread(): ChatThread? = chatThreads.value.firstOrNull { thread -> thread.id == _selectedChatThreadId.value }
        ?: chatThreads.value.firstOrNull()

    public fun messagesForSelectedThread(): List<ChatMessageRecord> {
        val thread = selectedChatThread() ?: return emptyList()
        return repository.messagesForThread(thread.id)
    }

    public fun selectRefund(refundId: String) {
        _selectedRefundId.value = refundId
    }

    public fun selectedRefund(): RefundRequest? = refundRequests.value.firstOrNull { refund -> refund.id == _selectedRefundId.value }
        ?: refundRequests.value.firstOrNull()

    public fun selectReview(reviewId: String) {
        _selectedReviewId.value = reviewId
    }

    public fun selectedReview(): ReviewRecord? = reviews.value.firstOrNull { review -> review.id == _selectedReviewId.value }
        ?: reviews.value.firstOrNull()

    public fun selectLead(leadId: String) {
        _selectedLeadId.value = leadId
    }

    public fun selectedLead(): LeadRecord? = leads.value.firstOrNull { lead -> lead.id == _selectedLeadId.value }
        ?: leads.value.firstOrNull()

    public fun setPaymentCheckoutMode(mode: String) {
        _paymentCheckoutMode.value = mode.trim().uppercase().ifEmpty { "DEPOSIT" }
    }

    public fun setInventoryPickerForQuote(enabled: Boolean) {
        _inventoryPickerForQuote.value = enabled
    }

    public fun startEditingArticle(articleId: String?) {
        _editingArticleId.value = articleId
        if (articleId != null) {
            val found = articles.value.firstOrNull { item -> item.id == articleId }
            if (found != null) {
                _newArticleCode.value = found.code
                _newArticleName.value = found.name
                _newArticleCategory.value = found.category
                _newArticlePrice.value = found.pricePerDayDop.toString()
                _newArticleStock.value = found.totalStock.toString()
            }
        } else {
            val nextNumber = articles.value.size + 1
            val paddedCode = nextNumber.toString().padStart(3, '0')
            _newArticleCode.value = "RF-$paddedCode"
            _newArticleName.value = ""
            _newArticleCategory.value = "Mobiliario"
            _newArticlePrice.value = ""
            _newArticleStock.value = ""
        }
    }

    public fun startEditingPackage(packageId: String?) {
        _editingPackageId.value = packageId
    }

    public fun setVoiceReturnScreenCode(screenCode: String) {
        val clean = screenCode.trim()
        if (clean.isNotEmpty()) {
            _voiceReturnScreenCode.value = clean
        }
    }

    public fun setArticleColorVariant(articleId: String, colorLabel: String) {
        _selectedArticleVariantColors.update { existing ->
            existing + (articleId to colorLabel)
        }
    }

    public fun setDraftDiscountDop(discountDop: Int) {
        _draftDiscountDop.value = discountDop.coerceAtLeast(0)
    }

    public fun adjustDraftQuantity(articleId: String, delta: Int) {
        soundPlayer.playCartAdd()
        val previous = _draftQuantities.value[articleId] ?: 0
        val next = (previous + delta).coerceAtLeast(0)
        if (delta > 0) {
            _lastAddedDraftItem.value = articleId to delta
        }
        _draftQuantities.update { current ->
            current + (articleId to next)
        }
    }

    public fun setDraftQuantity(articleId: String, quantity: Int) {
        val clamped = quantity.coerceAtLeast(0)
        val previous = _draftQuantities.value[articleId] ?: 0
        if (clamped > previous) {
            _lastAddedDraftItem.value = articleId to (clamped - previous)
        }
        _draftQuantities.update { current ->
            current + (articleId to clamped)
        }
    }

    public fun clearDraftCart() {
        _draftQuantities.value = emptyMap()
        _draftDiscountDop.value = 0
    }

    public fun applyPackageToDraftCart(packageItem: EventPackage) {
        soundPlayer.playCartAdd()
        val resolvedMap = mutableMapOf<String, Int>()
        for (lineDescription in packageItem.includedItems) {
            val quantityPrefix = lineDescription.trim().takeWhile { character -> character.isDigit() }.toIntOrNull() ?: 1
            val lowerLine = lineDescription.lowercase()
            val matchedArticle = articles.value.firstOrNull { candidate ->
                when {
                    lowerLine.contains("silla") -> candidate.id == "art-silla-tiffany"
                    lowerLine.contains("redonda") -> candidate.id == "art-mesa-redonda"
                    lowerLine.contains("rectangular") || lowerLine.contains("plegable") -> candidate.id == "art-mesa-plegable"
                    lowerLine.contains("centro") || lowerLine.contains("floral") -> candidate.id == "art-centro-floral"
                    lowerLine.contains("arco") || lowerLine.contains("globo") -> candidate.id == "art-arco-rosa"
                    lowerLine.contains("dulce") || lowerLine.contains("helado") || lowerLine.contains("postre") -> candidate.id == "art-mesa-helados"
                    else -> false
                }
            }
            if (matchedArticle != null) {
                resolvedMap[matchedArticle.id] = quantityPrefix
            }
        }
        if (resolvedMap.isNotEmpty()) {
            _draftQuantities.value = resolvedMap
        }
        _draftDiscountDop.value = packageItem.savingsDop.coerceAtLeast(0)
        _selectedPackageId.value = packageItem.id
    }

    public fun undoLastDraftAddition(): Boolean {
        val lastAdded = _lastAddedDraftItem.value ?: return false
        val currentQty = _draftQuantities.value[lastAdded.first] ?: 0
        val revertedQty = (currentQty - lastAdded.second).coerceAtLeast(0)
        _draftQuantities.update { current ->
            current + (lastAdded.first to revertedQty)
        }
        _lastAddedDraftItem.value = null
        _formFeedback.value = "Se deshizo el último artículo agregado al carrito."
        return true
    }

    public fun totalDraftCartUnits(): Int = _draftQuantities.value.values.filter { qty -> qty > 0 }.sum()

    public fun draftLineItems(): List<EventLineItem> {
        val result = mutableListOf<EventLineItem>()
        for ((itemId, qty) in _draftQuantities.value) {
            if (qty <= 0) continue
            val catalogItem = articles.value.firstOrNull { article -> article.id == itemId }
            if (catalogItem != null) {
                result += EventLineItem(
                    articleCode = catalogItem.code,
                    name = catalogItem.name,
                    quantity = qty,
                    unitPriceDop = catalogItem.pricePerDayDop,
                    photo = catalogItem.photo,
                )
                continue
            }
            val balloonItem = balloonProducts.value.firstOrNull { balloon -> balloon.id == itemId }
            if (balloonItem != null) {
                result += EventLineItem(
                    articleCode = balloonItem.code,
                    name = balloonItem.name,
                    quantity = qty,
                    unitPriceDop = balloonItem.priceDop,
                    photo = balloonItem.photo,
                )
                continue
            }
            val extraWizardItem = CanonicalSampleData.rosaWizardExtras.firstOrNull { extra -> extra.id == itemId }
            if (extraWizardItem != null) {
                result += EventLineItem(
                    articleCode = extraWizardItem.code,
                    name = extraWizardItem.name,
                    quantity = qty,
                    unitPriceDop = extraWizardItem.pricePerDayDop,
                    photo = extraWizardItem.photo,
                )
            }
        }
        return result
    }

    public fun syncDraftToOfficialQuote(): QuoteRecord? {
        val lines = draftLineItems()
        if (lines.isEmpty()) {
            postError("Agrega al menos un artículo al carrito para generar la cotización oficial.")
            return null
        }
        val deliveryFee = _deliveryBaseInput.value.toIntOrNull() ?: 350
        val discount = _draftDiscountDop.value
        val clientLabel = _profileName.value.trim().ifBlank { "Cliente Rosa Fiesta" }
        val resolvedTitle = _clientEventTitleInput.value.trim()
            .ifBlank { _eventTypeInput.value.trim() }
            .ifBlank { _rosaEventType.value.trim() }
            .ifBlank { "Evento Rosa Fiesta" }
        val resolvedGuests = (_eventGuestsInput.value.trim().toIntOrNull() ?: _rosaGuestsCount.value).coerceAtLeast(1)
        val created = repository.createQuote(
            eventTitle = resolvedTitle,
            clientName = clientLabel,
            clientEmail = _profileEmail.value,
            venue = _eventVenueInput.value.ifBlank { "Por definir" },
            dateLabel = _eventDateInput.value.ifBlank { "Por definir" },
            guests = resolvedGuests,
            lines = lines,
            deliveryFeeDop = deliveryFee,
            discountDop = discount,
            eventId = _selectedEventId.value,
        )
        _selectedQuoteId.value = created.id
        return created
    }

    public fun toggleRosaOmittedCategory(category: String) {
        val clean = category.trim()
        if (clean.isEmpty()) return
        _rosaOmittedCategories.update { existing ->
            if (clean in existing) existing - clean else existing + clean
        }
    }

    public fun clearRosaOmittedCategories() {
        _rosaOmittedCategories.value = emptySet()
    }

    public fun updateRosaParameters(
        eventType: String = _rosaEventType.value,
        guests: Int = _rosaGuestsCount.value,
        budgetDop: Int = _rosaBudgetDop.value,
        palette: String = _rosaPalette.value,
    ) {
        soundPlayer.playRosaReply()
        _rosaEventType.value = eventType.trim().ifEmpty { "Boda Real Rosa & Dorado" }
        _rosaGuestsCount.value = guests.coerceIn(10, 500)
        _rosaBudgetDop.value = budgetDop.coerceAtLeast(500)
        _rosaPalette.value = palette.trim().ifEmpty { "Rosa & Dorado" }
    }

    public fun sendConversationalRosaPrompt(
        prompt: String,
        onReply: (RosaChatReply) -> Unit = {},
    ) {
        val cleanPrompt = prompt.trim()
        if (cleanPrompt.isEmpty()) return
        soundPlayer.playRosaSend()
        securityScope.launch {
            val reply = repository.sendRosaPrompt(
                message = cleanPrompt,
                eventType = _rosaEventType.value,
                guests = _rosaGuestsCount.value,
                budgetDop = _rosaBudgetDop.value,
                excludedCategories = _rosaOmittedCategories.value.toList(),
            )
            soundPlayer.playRosaReply()
            _rosaProposalMessage.value = reply.reply
            if (reply.suggestedLines.isNotEmpty()) {
                val updatedMap = mutableMapOf<String, Int>()
                for (suggestedLine in reply.suggestedLines) {
                    val matched = articles.value.firstOrNull { item -> item.code == suggestedLine.articleCode }
                    if (matched != null) {
                        updatedMap[matched.id] = suggestedLine.quantity
                    }
                }
                if (updatedMap.isNotEmpty()) {
                    _draftQuantities.value = updatedMap
                }
            }
            onReply(reply)
        }
    }

    public fun setRosaProposalMessage(message: String) {
        soundPlayer.playRosaReply()
        _rosaProposalMessage.value = message.trim()
    }

    public fun startVoiceInput(origin: VoiceInputOrigin, initialText: String = "") {
        soundPlayer.playTap()
        _voiceInputOrigin.value = origin
        val cleanInitial = initialText.trim()
        if (cleanInitial.isNotEmpty()) {
            _voiceTranscript.value = cleanInitial
        } else if (origin == VoiceInputOrigin.SEARCH) {
            _voiceTranscript.value = _searchQuery.value.trim()
        } else {
            _voiceTranscript.value = ""
        }
        voiceInputMonitor.startListening { recognizedText ->
            val cleanRecognized = recognizedText.trim()
            if (cleanRecognized.isNotEmpty()) {
                _voiceTranscript.value = cleanRecognized
                if (_voiceInputOrigin.value == VoiceInputOrigin.SEARCH) {
                    _searchQuery.value = cleanRecognized
                }
            }
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    public fun stopVoiceInput() {
        soundPlayer.playTap()
        voiceInputMonitor.stopListening()
        val wavBytes = voiceInputMonitor.capturedWavBytes.value
        val hasCapturedBytes = wavBytes != null && wavBytes.size > 44
        val needsRemoteTranscription = if (hasCapturedBytes) _voiceTranscript.value.isBlank() else false
        if (needsRemoteTranscription) {
            val nonNullBytes = wavBytes ?: return
            securityScope.launch {
                val wavBase64 = Base64.encode(nonNullBytes)
                val outcome = repository.transcribeVoicePrompt(wavBase64, locale = "es-DO")
                val cleanTranscript = outcome.transcript.trim()
                if (cleanTranscript.isNotEmpty()) {
                    _voiceTranscript.value = cleanTranscript
                    if (_voiceInputOrigin.value == VoiceInputOrigin.SEARCH) {
                        _searchQuery.value = cleanTranscript
                    }
                }
            }
        }
    }

    public fun setVoiceTranscript(transcript: String) {
        _voiceTranscript.value = transcript
        if (_voiceInputOrigin.value == VoiceInputOrigin.SEARCH) {
            _searchQuery.value = transcript
        }
    }

    public fun toggleInlineVoiceSearch() {
        val currentlyListening = isVoiceListening.value
        val isSearchOrigin = _voiceInputOrigin.value == VoiceInputOrigin.SEARCH
        val shouldStop = if (currentlyListening) isSearchOrigin else false
        if (shouldStop) {
            stopVoiceInput()
        } else {
            startVoiceInput(VoiceInputOrigin.SEARCH, _searchQuery.value)
        }
    }

    public fun setLoginEmail(value: String) {
        _loginEmail.value = value
        _formError.value = null
    }

    public fun setLoginPassword(value: String) {
        _loginPassword.value = value
        _formError.value = null
    }

    public fun setRegisterName(value: String) {
        _registerName.value = value
        _formError.value = null
    }

    public fun setRegisterEmail(value: String) {
        _registerEmail.value = value
        _formError.value = null
    }

    public fun setRegisterPhone(value: String) {
        _registerPhone.value = value
        _formError.value = null
    }

    public fun setEventTypeInput(value: String) {
        _eventTypeInput.value = value
    }

    public fun setClientEventTitleInput(value: String) {
        _clientEventTitleInput.value = value
    }

    public fun setRosaBudgetDop(value: Int) {
        _rosaBudgetDop.value = value.coerceAtLeast(0)
    }

    public fun setGuestsWizardMode(enabled: Boolean) {
        _guestsWizardMode.value = enabled
    }

    public fun setVoiceAppendToIdea(enabled: Boolean) {
        _voiceAppendToIdea.value = enabled
    }

    public fun addWizardScheduleItem(
        startTime: String,
        endTime: String,
        title: String,
    ) {
        val cleanTitle = title.trim()
        if (cleanTitle.isEmpty()) return
        soundPlayer.playTap()
        val nextIndex = _wizardScheduleItems.value.size + 1
        val item = EventTimelineItemUiModel(
            id = "wiz-tl-$nextIndex-${cleanTitle.hashCode()}",
            startTime = startTime.trim().ifBlank { "18:00" },
            endTime = endTime.trim().ifBlank { "19:30" },
            title = cleanTitle,
            subtitle = "Definido por ti en la solicitud",
            dotColorHex = 0xFF865DEC,
            ringColorHex = 0xFFEFE8FD,
        )
        _wizardScheduleItems.update { currentList -> currentList + item }
    }

    public fun removeWizardScheduleItem(itemId: String) {
        soundPlayer.playTap()
        _wizardScheduleItems.update { currentList ->
            currentList.filterNot { item -> item.id == itemId }
        }
    }

    public fun markCalendarPromptDismissed(eventId: String) {
        val cleanId = eventId.trim()
        if (cleanId.isEmpty()) return
        _calendarSyncPromptDismissedIds.update { existing -> existing + cleanId }
    }

    public fun setEventDateInput(value: String) {
        _eventDateInput.value = value
    }

    public fun setEventVenueInput(value: String) {
        _eventVenueInput.value = value
    }

    public fun setEventGuestsInput(value: String) {
        _eventGuestsInput.value = value
    }

    public fun setEventNotesInput(value: String) {
        _eventNotesInput.value = value
    }

    public fun setNewEventTitle(value: String) {
        _newEventTitle.value = value
        _formError.value = null
    }

    public fun setNewEventClient(value: String) {
        _newEventClient.value = value
        _formError.value = null
    }

    public fun setNewEventDate(value: String) {
        _newEventDate.value = value
        _formError.value = null
    }

    public fun setNewEventVenue(value: String) {
        _newEventVenue.value = value
        _formError.value = null
    }

    public fun setNewEventGuests(value: String) {
        _newEventGuests.value = value
        _formError.value = null
    }

    public fun setPaymentAmountInput(value: String) {
        _paymentAmountInput.value = value
        _formError.value = null
    }

    public fun setPaymentMethodInput(value: String) {
        _paymentMethodInput.value = value
        _formError.value = null
    }

    public fun setPaymentRefInput(value: String) {
        _paymentRefInput.value = value
        _formError.value = null
    }

    public fun setDateChangeInput(value: String) {
        _dateChangeInput.value = value
        _formError.value = null
    }

    public fun setProfileName(value: String) {
        _profileName.value = value
        _formError.value = null
    }

    public fun setProfileEmail(value: String) {
        _profileEmail.value = value
        _formError.value = null
    }

    public fun setProfileUsername(value: String) {
        _profileUsername.value = value
        _formError.value = null
    }

    public fun setProfileBirthDate(value: String) {
        _profileBirthDate.value = value
        _formError.value = null
    }

    public fun setProfilePhone(value: String) {
        _profilePhone.value = value
        _formError.value = null
    }

    public fun setProfileZone(value: String) {
        _profileZone.value = value
        securityScope.launch {
            secureStorage.put("rf_pref_zone", value.trim())
        }
    }

    public fun applyUserIdentityFromLogin(rawIdentifier: String) {
        val trimmed = rawIdentifier.trim()
        if (trimmed.isEmpty()) return
        val hasAtSymbol = trimmed.contains("@")
        val resolvedEmail = if (hasAtSymbol) trimmed.lowercase() else "${trimmed.lowercase()}@rosafiesta.do"
        val resolvedUsername = resolvedEmail.substringBefore("@")
        val derivedDisplayName = deriveDisplayNameFromUsername(resolvedUsername)
        _profileEmail.value = resolvedEmail
        _profileUsername.value = resolvedUsername
        _profileName.value = derivedDisplayName
    }

    private fun deriveDisplayNameFromUsername(username: String): String {
        val cleanUser = username.trim().lowercase()
        val knownDisplayName = when (cleanUser) {
            "maria" -> "María"
            "yuli" -> "Yuli"
            "pedro" -> "Pedro"
            "laura" -> "Laura Peña"
            "ana" -> "Ana Gómez"
            "camila", "camila.duarte", "camila.rodriguez" -> "Camila Rodríguez"
            else -> null
        }
        if (knownDisplayName != null) {
            return knownDisplayName
        }
        val words = cleanUser
            .split(".", "_", "-")
            .map { token -> token.trim() }
            .filter { token -> token.isNotEmpty() }
            .map { token -> token.replaceFirstChar { character -> character.uppercaseChar() } }
        return if (words.isEmpty()) "Cliente" else words.joinToString(" ")
    }

    public fun setSupportSubject(value: String) {
        _supportSubject.value = value
        _formError.value = null
    }

    public fun setSupportMessage(value: String) {
        _supportMessage.value = value
        _formError.value = null
    }

    public fun setChatInput(value: String) {
        _chatInput.value = value
    }

    public fun setNewArticleCode(value: String) {
        _newArticleCode.value = value
        _formError.value = null
    }

    public fun setNewArticleName(value: String) {
        _newArticleName.value = value
        _formError.value = null
    }

    public fun setNewArticleCategory(value: String) {
        _newArticleCategory.value = value
    }

    public fun setNewArticlePrice(value: String) {
        _newArticlePrice.value = value
        _formError.value = null
    }

    public fun setNewArticleStock(value: String) {
        _newArticleStock.value = value
        _formError.value = null
    }

    public fun setPromoCodeInput(value: String) {
        _promoCodeInput.value = value
    }

    public fun setPromoDiscountInput(value: String) {
        _promoDiscountInput.value = value
    }

    public fun setDepositPercentInput(value: String) {
        _depositPercentInput.value = value
    }

    public fun setDeliveryBaseInput(value: String) {
        _deliveryBaseInput.value = value
    }

    public fun setCancellationDaysInput(value: String) {
        _cancellationDaysInput.value = value
    }

    public fun togglePushNotifications() {
        val next = !_pushNotificationsEnabled.value
        _pushNotificationsEnabled.value = next
        securityScope.launch {
            secureStorage.put("rf_pref_push", next.toString())
        }
    }

    public fun toggleWhatsappAlerts() {
        val next = !_whatsappAlertsEnabled.value
        _whatsappAlertsEnabled.value = next
        securityScope.launch {
            secureStorage.put("rf_pref_whatsapp", next.toString())
        }
    }

    public fun toggleEmailReceipts() {
        val next = !_emailReceiptsEnabled.value
        _emailReceiptsEnabled.value = next
        securityScope.launch {
            secureStorage.put("rf_pref_email_receipts", next.toString())
        }
    }

    public fun toggleBiometricLock() {
        val next = !_biometricLockEnabled.value
        _biometricLockEnabled.value = next
        securityScope.launch {
            secureStorage.put("rf_pref_biometric_lock", next.toString())
        }
    }

    public fun toggleAutoSyncWifiOnly() {
        soundPlayer.playTap()
        val next = !_autoSyncWifiOnly.value
        _autoSyncWifiOnly.value = next
        securityScope.launch {
            secureStorage.put("rf_pref_auto_sync_wifi", next.toString())
        }
    }

    public fun toggleSoundEffects() {
        val next = !_soundEffectsEnabled.value
        _soundEffectsEnabled.value = next
        soundPlayer.isEnabled = next
        if (next) {
            soundPlayer.playTap()
        }
    }

    public fun toggleChecklistStep(stepId: String) {
        soundPlayer.playChecklistTick()
        repository.toggleChecklistStep(stepId)
    }

    public fun syncOfflineQueue() {
        soundPlayer.playPaymentSuccess()
        repository.syncOfflineQueue()
        _formFeedback.value = "Cola offline sincronizada correctamente."
    }

    public fun adjustArticleStock(articleId: String, delta: Int) {
        soundPlayer.playCartAdd()
        repository.adjustArticleStock(articleId, delta)
    }

    private val financeRestrictedScreenCodes: Set<String> = setOf(
        "A20",
        "A21",
        "A32",
        "A33",
        "s57",
        "s58",
        "s59",
        "s67",
    )

    public fun switchRole(role: String) {
        soundPlayer.playTap()
        repository.switchRole(role)
        _formFeedback.value = "Vista cambiada al rol: $role"
    }

    public fun toggleFavorite(articleId: String) {
        soundPlayer.playTap()
        repository.toggleFavorite(articleId)
    }

    public fun toggleStaffPaymentAccess(staffName: String) {
        soundPlayer.playTap()
        repository.toggleStaffPaymentAccess(staffName)
    }

    public fun isScreenRestrictedForActiveStaff(screenCode: String): Boolean {
        val isFinanceScreen = screenCode in financeRestrictedScreenCodes
        if (!isFinanceScreen) {
            return false
        }
        val normalizedRole = activeRole.value.trim().lowercase()
        val isStaffRole = normalizedRole.startsWith("staff_")
        if (!isStaffRole) {
            return false
        }
        val staffNameKey = normalizedRole.removePrefix("staff_")
        val matchedStaffMember = staffMembers.value.firstOrNull { member ->
            member.name.equals(staffNameKey, ignoreCase = true)
        }
        val hasPaymentPermission = matchedStaffMember?.canViewPayments ?: false
        return !hasPaymentPermission
    }

    public fun submitLogin(): Boolean {
        if (_isSubmitting.value) return false
        _isSubmitting.value = true
        try {
            val identifierError = InputValidator.validateLoginIdentifier(_loginEmail.value)
            if (identifierError != null) {
                soundPlayer.playAlertError()
                _formError.value = identifierError
                return false
            }
            val passwordError = InputValidator.validatePassword(_loginPassword.value)
            if (passwordError != null) {
                soundPlayer.playAlertError()
                _formError.value = passwordError
                return false
            }
            val outcome = repository.authenticateUser(_loginEmail.value, _loginPassword.value)
            if (!outcome.success) {
                soundPlayer.playAlertError()
                _formError.value = outcome.errorMessage
                    ?: SensitiveDataRedactor.translateBusinessError(outcome.errorCode, null)
                return false
            }
            val account = outcome.account
            if (account != null) {
                applyAuthenticatedAccount(account, outcome.accessToken)
            } else {
                applyUserIdentityFromLogin(_loginEmail.value)
            }
            soundPlayer.playPaymentSuccess()
            _formError.value = null
            val maskedEmail = SensitiveDataRedactor.maskEmail(_profileEmail.value)
            _formFeedback.value = "Sesión iniciada como $maskedEmail"
            return true
        } finally {
            _isSubmitting.value = false
        }
    }

    public fun submitRegistration(
        firstName: String,
        lastName: String,
        username: String,
        email: String,
        password: String,
        birthDate: String,
        acceptedTerms: Boolean,
        phone: String = _registerPhone.value,
    ): Boolean {
        if (_isSubmitting.value) return false
        _isSubmitting.value = true
        try {
            val firstNameError = InputValidator.validatePersonNamePart(firstName, "El nombre")
            if (firstNameError != null) {
                soundPlayer.playAlertError()
                _formError.value = firstNameError
                return false
            }
            val lastNameError = InputValidator.validatePersonNamePart(lastName, "El apellido")
            if (lastNameError != null) {
                soundPlayer.playAlertError()
                _formError.value = lastNameError
                return false
            }
            val usernameError = InputValidator.validateUsername(username)
            if (usernameError != null) {
                soundPlayer.playAlertError()
                _formError.value = usernameError
                return false
            }
            val emailError = InputValidator.validateEmail(email)
            if (emailError != null) {
                soundPlayer.playAlertError()
                _formError.value = emailError
                return false
            }
            val birthDateError = InputValidator.validateBirthDate(birthDate, isRequired = true)
            if (birthDateError != null) {
                soundPlayer.playAlertError()
                _formError.value = birthDateError
                return false
            }
            val passwordError = InputValidator.validatePassword(password)
            if (passwordError != null) {
                soundPlayer.playAlertError()
                _formError.value = passwordError
                return false
            }
            if (!acceptedTerms) {
                soundPlayer.playAlertError()
                _formError.value = SensitiveDataRedactor.translateBusinessError("AUTH_TERMS_REQUIRED", null)
                return false
            }
            val outcome = repository.registerUser(
                firstName = firstName,
                lastName = lastName,
                username = username,
                email = email,
                password = password,
                birthDate = birthDate,
                phone = phone.trim(),
            )
            if (!outcome.success) {
                soundPlayer.playAlertError()
                _formError.value = outcome.errorMessage
                    ?: SensitiveDataRedactor.translateBusinessError(outcome.errorCode, null)
                return false
            }
            val account = outcome.account
            if (account != null) {
                applyAuthenticatedAccount(account, outcome.accessToken)
            }
            soundPlayer.playPaymentSuccess()
            _formError.value = null
            _formFeedback.value = "Cuenta creada correctamente."
            return true
        } finally {
            _isSubmitting.value = false
        }
    }

    public fun logoutSession() {
        soundPlayer.playTap()
        _isExplicitGuestSession.value = true
        _loginEmail.value = ""
        _loginPassword.value = ""
        _registerName.value = ""
        _registerEmail.value = ""
        _registerPhone.value = ""
        _activeOtpCode.value = null
        _verifiedOtpResetToken.value = ""
        _pendingOtpEmail.value = ""
        _profileName.value = "Invitado"
        _profileEmail.value = ""
        _profileUsername.value = "invitado"
        _profileAvatarKey.value = null
        _profileAvatarBytes.value = null
        _formError.value = null
        _formFeedback.value = "Sesión cerrada correctamente."
        repository.logoutSession()
        securityScope.launch {
            secureStorage.remove(SecureStorage.KEY_ACCESS_TOKEN)
            secureStorage.remove(SecureStorage.KEY_USER_ROLE)
            secureStorage.remove(SecureStorage.KEY_AVATAR_KEY)
            secureStorage.remove(SecureStorage.KEY_AVATAR_IMAGE_DATA)
        }
    }

    public fun submitPasswordReset(
        identifier: String,
        newPassword: String,
        confirmPassword: String,
        otpCode: String = "",
    ): Boolean {
        if (_isSubmitting.value) return false
        _isSubmitting.value = true
        try {
            val cleanPass = newPassword.trim()
            val cleanConfirm = confirmPassword.trim()
            val hasMinimumLength = cleanPass.length >= 8
            val hasDigit = cleanPass.any { character -> character.isDigit() }
            val isStrongPassword = if (hasMinimumLength) hasDigit else false
            if (!isStrongPassword) {
                soundPlayer.playAlertError()
                _formError.value = "La nueva contraseña debe tener mínimo 8 caracteres e incluir al menos un número."
                return false
            }
            if (cleanPass != cleanConfirm) {
                soundPlayer.playAlertError()
                _formError.value = SensitiveDataRedactor.translateBusinessError("AUTH_PASSWORD_MISMATCH", null)
                return false
            }
            val cleanOtp = otpCode.trim()
            val expectedLocalOtp = _activeOtpCode.value
            val hasExpectedLocalOtp = !expectedLocalOtp.isNullOrBlank()
            if (hasExpectedLocalOtp) {
                val otpMatches = cleanOtp == expectedLocalOtp
                val hasVerifiedToken = _verifiedOtpResetToken.value.isNotBlank()
                val canProceedWithOtp = if (otpMatches) true else hasVerifiedToken
                if (!canProceedWithOtp) {
                    soundPlayer.playAlertError()
                    _formError.value = "Ingresa el código de 6 dígitos enviado a tu correo."
                    return false
                }
            }
            val targetIdentifier = identifier.trim().ifBlank { _pendingOtpEmail.value }
            val outcome = repository.resetUserPassword(targetIdentifier, cleanPass)
            if (!outcome.success) {
                soundPlayer.playAlertError()
                _formError.value = outcome.errorMessage
                    ?: SensitiveDataRedactor.translateBusinessError(outcome.errorCode, null)
                return false
            }
            _activeOtpCode.value = null
            _verifiedOtpResetToken.value = ""
            soundPlayer.playPaymentSuccess()
            _formError.value = null
            _formFeedback.value = "Contraseña actualizada. Inicia sesión con tu nueva clave."
            return true
        } finally {
            _isSubmitting.value = false
        }
    }

    public fun changeCurrentPassword(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String,
    ): Boolean {
        val cleanCurrent = currentPassword.trim()
        val cleanNew = newPassword.trim()
        val cleanConfirm = confirmPassword.trim()
        if (cleanCurrent.isEmpty()) {
            postError("Ingresa tu contraseña actual.")
            return false
        }
        val hasMinLength = cleanNew.length >= 8
        val hasDigit = cleanNew.any { character -> character.isDigit() }
        val isStrong = if (hasMinLength) hasDigit else false
        if (!isStrong) {
            postError("La nueva contraseña requiere mínimo 8 caracteres y al menos un número.")
            return false
        }
        if (cleanNew != cleanConfirm) {
            postError("La confirmación de contraseña no coincide.")
            return false
        }
        val outcome = repository.changeUserPassword(cleanCurrent, cleanNew)
        if (!outcome.success) {
            postError(outcome.errorMessage ?: "La contraseña actual no es correcta.")
            return false
        }
        soundPlayer.playPaymentSuccess()
        _formError.value = null
        _formFeedback.value = "Contraseña actualizada correctamente."
        return true
    }

    private fun applyAuthenticatedAccount(
        account: RegisteredUserAccount,
        accessToken: String?,
    ) {
        applyProfileFieldsFromAccount(account)
        if (!accessToken.isNullOrBlank()) {
            securityScope.launch {
                secureStorage.put(SecureStorage.KEY_ACCESS_TOKEN, accessToken)
                secureStorage.put(SecureStorage.KEY_USER_ROLE, account.role)
            }
        }
    }

    public fun submitRegister(acceptedTerms: Boolean = false): Boolean {
        val rawName = _registerName.value.trim()
        val firstPart = rawName.substringBefore(" ")
        val lastPart = rawName.substringAfter(" ", missingDelimiterValue = "")
        val derivedUser = _registerEmail.value.trim().substringBefore("@")
        val phoneVal = _registerPhone.value.trim()
        if (phoneVal.length < 7) {
            _formError.value = "Completa tu nombre, un correo válido y tu teléfono de contacto."
            return false
        }
        return submitRegistration(
            firstName = firstPart,
            lastName = lastPart,
            username = derivedUser,
            email = _registerEmail.value,
            password = _loginPassword.value,
            birthDate = _profileBirthDate.value,
            acceptedTerms = acceptedTerms,
            phone = phoneVal,
        )
    }

    public fun submitNewEvent(): Boolean {
        if (_isSubmitting.value) return false
        _isSubmitting.value = true
        try {
            val title = _newEventTitle.value.trim()
            val client = _newEventClient.value.trim()
            val date = _newEventDate.value.trim()
            val venue = _newEventVenue.value.trim()
            val guests = _newEventGuests.value.trim().toIntOrNull() ?: 0
            val hasValidInputs = allConditionsTrue(
                first = title.isNotEmpty(),
                second = client.isNotEmpty(),
                third = date.isNotEmpty(),
                fourth = venue.isNotEmpty(),
                fifth = guests > 0,
            )
            if (!hasValidInputs) {
                _formError.value = "Completa título, cliente, fecha, lugar y un número válido de invitados."
                return false
            }
            repository.createEvent(title, client, date, venue, guests)
            repository.events.value.lastOrNull()?.id?.let { createdId ->
                _selectedEventId.value = createdId
            }
            _formError.value = null
            _formFeedback.value = "Evento '$title' creado con propuesta automática."
            return true
        } finally {
            _isSubmitting.value = false
        }
    }

    public fun submitQuickClientEvent(): Boolean {
        if (_isSubmitting.value) return false
        _isSubmitting.value = true
        try {
            val categoryLabel = _eventTypeInput.value.trim().ifBlank { _rosaEventType.value.trim() }
            val title = _clientEventTitleInput.value.trim().ifBlank { categoryLabel }
            val date = _eventDateInput.value.trim()
            val venue = _eventVenueInput.value.trim()
            val guests = _eventGuestsInput.value.trim().toIntOrNull() ?: _rosaGuestsCount.value
            val hasValidInputs = allConditionsTrue(
                first = title.isNotEmpty(),
                second = date.isNotEmpty(),
                third = venue.isNotEmpty(),
                fourth = guests > 0,
            )
            if (!hasValidInputs) {
                _formError.value = "Completa el título o tipo de evento, fecha, lugar e invitados (> 0)."
                return false
            }
            val accountName = currentAuthenticatedUser.value?.fullName.orEmpty().trim()
            val resolvedClientName = accountName.ifEmpty { _profileName.value.trim().ifEmpty { "Cliente" } }
            repository.createEvent(title, resolvedClientName, date, venue, guests)
            repository.events.value.lastOrNull()?.id?.let { createdId ->
                _selectedEventId.value = createdId
                val wizardBlocks = _wizardScheduleItems.value
                if (wizardBlocks.isNotEmpty()) {
                    _eventTimelineItems.value = wizardBlocks
                    wizardBlocks.forEach { blockItem ->
                        repository.addEventScheduleBlock(
                            eventId = createdId,
                            timeRange = "${blockItem.startTime} - ${blockItem.endTime}",
                            title = blockItem.title,
                            ownerName = resolvedClientName,
                        )
                    }
                } else {
                    _eventTimelineItems.value = emptyList()
                }
            }
            _formError.value = null
            _formFeedback.value = "Evento '$title' configurado."
            return true
        } finally {
            _isSubmitting.value = false
        }
    }

    public fun submitNewArticle(
        description: String = "",
        dimensions: String = "",
        material: String = "",
        visibleInCatalog: Boolean = true,
    ): Boolean {
        if (_isSubmitting.value) return false
        _isSubmitting.value = true
        try {
            val code = _newArticleCode.value.trim()
            val name = _newArticleName.value.trim()
            val cat = _newArticleCategory.value.trim().ifEmpty { "Mobiliario" }
            val price = _newArticlePrice.value.trim().toIntOrNull() ?: 0
            val stock = _newArticleStock.value.trim().toIntOrNull() ?: -1
            val hasValidInputs = allConditionsTrue(
                first = code.isNotEmpty(),
                second = name.isNotEmpty(),
                third = price > 0,
                fourth = stock >= 0,
            )
            if (!hasValidInputs) {
                _formError.value = "Ingresa código, nombre, precio diario (> 0) y stock válido."
                return false
            }
            val editingId = _editingArticleId.value
            if (editingId != null) {
                repository.updateArticle(
                    articleId = editingId,
                    name = name,
                    category = cat,
                    description = description.ifBlank { null },
                    pricePerDayDop = price,
                    totalStock = stock,
                    dimensions = dimensions.ifBlank { null },
                    material = material.ifBlank { null },
                    visibleInCatalog = visibleInCatalog,
                )
            } else {
                repository.createArticle(code, name, cat, price, stock)
                val newlyCreated = repository.articles.value.firstOrNull { item -> item.code == code }
                if (newlyCreated != null) {
                    repository.updateArticle(
                        articleId = newlyCreated.id,
                        description = description.ifBlank { null },
                        dimensions = dimensions.ifBlank { null },
                        material = material.ifBlank { null },
                        visibleInCatalog = visibleInCatalog,
                    )
                }
            }
            _editingArticleId.value = null
            _formError.value = null
            _formFeedback.value = "Artículo $code ($name) guardado en catálogo."
            return true
        } finally {
            _isSubmitting.value = false
        }
    }

    public fun submitPayment(eventId: String = _selectedEventId.value): PaymentRecord? {
        if (_isSubmitting.value) return null
        _isSubmitting.value = true
        try {
            val amount = _paymentAmountInput.value.trim().filter { character -> character.isDigit() }.toIntOrNull() ?: 0
            val method = _paymentMethodInput.value.trim()
            val ref = _paymentRefInput.value.trim()
            val hasValidPayment = if (amount > 0) method.isNotEmpty() else false
            if (!hasValidPayment) {
                soundPlayer.playAlertError()
                _formError.value = SensitiveDataRedactor.translateBusinessError("PAYMENT_VALIDATION_FAILED", null)
                return null
            }
            val record = repository.registerPayment(eventId, amount, method, ref)
            if (record != null) {
                soundPlayer.playPaymentSuccess()
                _formError.value = null
                val maskedRef = if (ref.contains("-")) SensitiveDataRedactor.maskBankReference(ref) else record.receiptNumber
                _formFeedback.value = "Pago registrado ($maskedRef)."
            }
            return record
        } finally {
            _isSubmitting.value = false
        }
    }

    public fun submitDateChange(eventId: String = _selectedEventId.value): DateChangeResult {
        val nextDate = _dateChangeInput.value.trim()
        if (nextDate.isEmpty()) {
            soundPlayer.playAlertError()
            _formError.value = "Indica la nueva fecha solicitada."
            return DateChangeResult(allowed = false, isFree = false, feeDop = 0, message = "Fecha vacía.")
        }
        val res = repository.changeEventDate(eventId, nextDate)
        if (res.allowed) soundPlayer.playPaymentSuccess() else soundPlayer.playAlertError()
        _dateChangeBanner.value = res.message
        _formError.value = if (res.allowed) null else res.message
        return res
    }

    public fun submitCancelEvent(
        eventId: String = _selectedEventId.value,
        reason: String = "",
    ): CancellationRefund {
        soundPlayer.playAlertError()
        val refund = repository.cancelEvent(eventId)
        val cleanReason = reason.trim()
        val reasonSuffix = if (cleanReason.isNotEmpty()) " ($cleanReason)" else ""
        _formFeedback.value =
            "Evento cancelado$reasonSuffix · Reembolso ${refund.refundPercent}% (${RosaFiestaEngine.formatDop(refund.refundDop)})"
        return refund
    }

    public fun submitChatMessage(author: String = _profileName.value.substringBefore(" ").ifEmpty { "Cliente" }) {
        val text = _chatInput.value.trim()
        if (text.isEmpty()) return
        soundPlayer.playRosaSend()
        repository.sendChatMessage(author, SensitiveDataRedactor.redactText(text))
        _chatInput.value = ""
    }

    public fun submitSupportTicket(): Boolean {
        val subj = _supportSubject.value.trim()
        val msg = _supportMessage.value.trim()
        val hasSupportFields = if (subj.isNotEmpty()) msg.isNotEmpty() else false
        if (!hasSupportFields) {
            _formError.value = "Escribe el asunto y el detalle de tu consulta."
            return false
        }
        repository.sendChatMessage("Soporte ($subj)", msg)
        _formError.value = null
        _formFeedback.value = "Ticket enviado al equipo de RosaFiesta."
        return true
    }

    public fun saveProfile(
        draftName: String = _profileName.value,
        draftEmail: String = _profileEmail.value,
        draftPhone: String = _profilePhone.value,
        draftBirthDate: String = _profileBirthDate.value,
        draftUsername: String = _profileUsername.value,
        draftAvatarKey: String? = _profileAvatarKey.value,
    ): Boolean {
        val cleanName = draftName.trim()
        val cleanEmail = draftEmail.trim().lowercase()
        val cleanPhone = draftPhone.trim()
        val cleanBirthDate = draftBirthDate.trim()
        val cleanUsername = draftUsername.trim().lowercase()

        val nameError = InputValidator.validateFullName(cleanName)
        if (nameError != null) {
            soundPlayer.playAlertError()
            _formError.value = nameError
            return false
        }
        val emailError = InputValidator.validateEmail(cleanEmail)
        if (emailError != null) {
            soundPlayer.playAlertError()
            _formError.value = emailError
            return false
        }
        val phoneError = InputValidator.validatePhone(cleanPhone)
        if (phoneError != null) {
            soundPlayer.playAlertError()
            _formError.value = phoneError
            return false
        }
        val birthDateError = InputValidator.validateBirthDate(cleanBirthDate, isRequired = true)
        if (birthDateError != null) {
            soundPlayer.playAlertError()
            _formError.value = birthDateError
            return false
        }
        val usernameError = InputValidator.validateUsername(cleanUsername)
        if (usernameError != null) {
            soundPlayer.playAlertError()
            _formError.value = usernameError
            return false
        }

        val outcome = repository.updateUserProfile(
            fullName = cleanName,
            email = cleanEmail,
            phone = cleanPhone,
            birthDate = cleanBirthDate,
            username = cleanUsername,
        )
        val isUnsupportedFallback = outcome.errorCode == "PROFILE_UPDATE_UNSUPPORTED"
        val isRepositoryRejected = !outcome.success
        val shouldFailFromRepo = if (isRepositoryRejected) !isUnsupportedFallback else false
        if (shouldFailFromRepo) {
            soundPlayer.playAlertError()
            _formError.value = outcome.errorMessage ?: "No se pudo guardar el perfil."
            return false
        }

        val updatedAccount = outcome.account
        if (updatedAccount != null) {
            applyAuthenticatedAccount(updatedAccount, outcome.accessToken)
        } else {
            _profileName.value = cleanName
            _profileEmail.value = cleanEmail
            _profilePhone.value = cleanPhone
            _profileBirthDate.value = cleanBirthDate
            _profileUsername.value = cleanUsername
        }
        _profileAvatarKey.value = draftAvatarKey
        val isCustomAvatar = draftAvatarKey?.startsWith("CUSTOM:") == true
        if (!isCustomAvatar) {
            _profileAvatarBytes.value = null
        }
        securityScope.launch {
            if (draftAvatarKey.isNullOrBlank()) {
                secureStorage.remove(SecureStorage.KEY_AVATAR_KEY)
            } else {
                secureStorage.put(SecureStorage.KEY_AVATAR_KEY, draftAvatarKey)
            }
            if (!isCustomAvatar) {
                secureStorage.remove(SecureStorage.KEY_AVATAR_IMAGE_DATA)
            }
        }
        soundPlayer.playPaymentSuccess()
        _formError.value = null
        _formFeedback.value = "Perfil actualizado para $cleanName."
        return true
    }

    public fun setProfileAvatarKey(avatarKey: String?) {
        soundPlayer.playTap()
        _profileAvatarKey.value = avatarKey
        val isCustomAvatar = avatarKey?.startsWith("CUSTOM:") == true
        if (!isCustomAvatar) {
            _profileAvatarBytes.value = null
        }
        securityScope.launch {
            if (avatarKey.isNullOrBlank()) {
                secureStorage.remove(SecureStorage.KEY_AVATAR_KEY)
            } else {
                secureStorage.put(SecureStorage.KEY_AVATAR_KEY, avatarKey)
            }
            if (!isCustomAvatar) {
                secureStorage.remove(SecureStorage.KEY_AVATAR_IMAGE_DATA)
            }
        }
    }

    public fun pickProfileAvatarFromDevice(onPicked: (PickedMediaFile) -> Unit = {}) {
        securityScope.launch {
            val picked = platformActions.pickMediaFile(listOf("jpg", "jpeg", "png"))
            if (picked != null) {
                val customKey = "CUSTOM:${picked.fileName}"
                _profileAvatarBytes.value = picked.bytes
                if (picked.bytes.size <= 4 * 1024 * 1024) {
                    secureStorage.put(SecureStorage.KEY_AVATAR_IMAGE_DATA, Base64.encode(picked.bytes))
                }
                setProfileAvatarKey(customKey)
                _pickedAttachmentFile.value = picked
                _formError.value = null
                _formFeedback.value = "Foto '${picked.fileName}' seleccionada."
                onPicked(picked)
            }
        }
    }

    public fun pickMediaAttachment(
        allowedExtensions: List<String> = listOf("jpg", "jpeg", "png", "pdf"),
        onPicked: (PickedMediaFile) -> Unit = {},
    ) {
        securityScope.launch {
            val picked = platformActions.pickMediaFile(allowedExtensions)
            if (picked != null) {
                _pickedAttachmentFile.value = picked
                _formFeedback.value = "Archivo adjunto listo: ${picked.fileName}"
                onPicked(picked)
            }
        }
    }

    public fun clearPickedAttachment() {
        _pickedAttachmentFile.value = null
    }

    public fun setDarkTheme(enabled: Boolean) {
        soundPlayer.playTap()
        _isDarkTheme.value = enabled
        com.jeudry.rosafiesta.designsystem.RfThemeState.isPersonalDark = enabled
        securityScope.launch {
            secureStorage.put(SecureStorage.KEY_DARK_THEME, enabled.toString())
        }
    }

    public fun toggleDarkTheme() {
        setDarkTheme(!_isDarkTheme.value)
    }

    public fun setPreferredLanguage(language: String) {
        soundPlayer.playTap()
        val cleanLang = language.trim().ifBlank { "Español" }
        _preferredLanguage.value = cleanLang
        securityScope.launch {
            secureStorage.put(SecureStorage.KEY_LANGUAGE, cleanLang)
        }
    }

    public fun togglePreferredLanguage() {
        val nextLanguage = if (_preferredLanguage.value == "Español") "English" else "Español"
        setPreferredLanguage(nextLanguage)
    }

    public fun setRemindersEnabled(enabled: Boolean) {
        soundPlayer.playTap()
        _remindersEnabled.value = enabled
        securityScope.launch {
            secureStorage.put(SecureStorage.KEY_REMINDERS, enabled.toString())
        }
    }

    public fun toggleRemindersEnabled() {
        setRemindersEnabled(!_remindersEnabled.value)
    }

    public fun setSavedPaymentMethod(methodLabel: String) {
        soundPlayer.playTap()
        val cleanMethod = methodLabel.trim().ifBlank { "Transferencia Banreservas" }
        _savedPaymentMethod.value = cleanMethod
        securityScope.launch {
            secureStorage.put(SecureStorage.KEY_PAYMENT_METHOD, cleanMethod)
        }
    }

    public fun setSketchTableCount(count: Int) {
        val clamped = count.coerceIn(2, 12)
        _sketchTableCount.value = clamped
        securityScope.launch {
            secureStorage.put(SecureStorage.KEY_SKETCH_TABLES, clamped.toString())
        }
    }

    public fun adjustSketchTables(delta: Int) {
        setSketchTableCount(_sketchTableCount.value + delta)
    }

    public fun saveSketchLayoutJson(sketchJson: String, eventId: String = _selectedEventId.value) {
        _sketchLayoutJson.value = sketchJson
        repository.updateEventDetails(eventId = eventId, sketchJson = sketchJson)
        securityScope.launch {
            secureStorage.put("rf_sketch_layout_json", sketchJson)
        }
        _formFeedback.value = "Boceto guardado en el expediente del evento."
    }

    public fun clientEventsForCurrentUser(): List<ClientEvent> {
        if (_isExplicitGuestSession.value) {
            return emptyList()
        }
        val allEvents = events.value
        val authenticatedAccount = currentAuthenticatedUser.value
        if (authenticatedAccount != null) {
            val accountEmail = authenticatedAccount.email.trim().lowercase()
            val accountFullName = authenticatedAccount.fullName.trim()
            return allEvents.filter { eventItem ->
                val itemEmail = eventItem.clientEmail.trim().lowercase()
                val matchesEmail = if (itemEmail.isNotEmpty()) itemEmail == accountEmail else false
                if (matchesEmail) {
                    true
                } else {
                    eventItem.clientName.trim().equals(accountFullName, ignoreCase = true)
                }
            }
        }
        val activeName = _profileName.value.trim()
        val isEmptyName = activeName.isEmpty()
        val isGuest = activeName.equals("Invitado", ignoreCase = true)
        val isGuestRosa = activeName.equals("Invitado Rosa Fiesta", ignoreCase = true)
        if (isEmptyName) {
            return emptyList()
        }
        if (isGuest) {
            return emptyList()
        }
        if (isGuestRosa) {
            return emptyList()
        }
        return allEvents.filter { eventItem ->
            eventItem.clientName.trim().equals(activeName, ignoreCase = true)
        }
    }

    public fun activeClientEventCount(): Int = clientEventsForCurrentUser().count { eventItem ->
        val isCompleted = eventItem.status == EventStatus.COMPLETED
        val isCancelled = eventItem.status == EventStatus.CANCELLED
        val isClosed = if (isCompleted) true else isCancelled
        !isClosed
    }

    public fun activeClientEventOrNull(): ClientEvent? {
        val isAdminContext = isCurrentSessionAdminOrStaff()
        val sourceEvents = if (isAdminContext) events.value else clientEventsForCurrentUser()
        val currentSelectedId = _selectedEventId.value
        return sourceEvents.firstOrNull { eventItem -> eventItem.id == currentSelectedId }
            ?: sourceEvents.firstOrNull()
    }

    public fun activeClientEvent(): ClientEvent {
        val ownedOrAdminEvent = activeClientEventOrNull()
        if (ownedOrAdminEvent != null) {
            return ownedOrAdminEvent
        }
        val currentSelectedId = _selectedEventId.value
        return events.value.firstOrNull { eventItem -> eventItem.id == currentSelectedId }
            ?: events.value.first()
    }

    public fun clientPaymentsForCurrentUser(): List<PaymentRecord> {
        val userEventIds = clientEventsForCurrentUser().map { eventItem -> eventItem.id }.toSet()
        if (userEventIds.isEmpty()) {
            return emptyList()
        }
        return payments.value.filter { paymentItem -> paymentItem.eventId in userEventIds }
    }

    public fun addEventTimelineItem(
        startTime: String = "22:00",
        endTime: String = "23:30",
        customTitle: String = "",
        customSubtitle: String = "",
    ) {
        soundPlayer.playTap()
        val nextNum = _eventTimelineItems.value.size + 1
        val activeEvt = activeClientEvent()
        val resolvedTitle = customTitle.trim().ifBlank { "Desmontaje y retorno a bodega #$nextNum" }
        val resolvedSubtitle = customSubtitle.trim().ifBlank { "Conteo de ${activeEvt.articleCount} piezas (${activeEvt.title})" }
        val newItem = EventTimelineItemUiModel(
            id = "tl-$nextNum",
            startTime = startTime.trim().ifBlank { "22:00" },
            endTime = endTime.trim().ifBlank { "23:30" },
            title = resolvedTitle,
            subtitle = resolvedSubtitle,
            dotColorHex = 0xFF8DB52C,
            ringColorHex = 0xFFEEF6D6,
        )
        _eventTimelineItems.update { currentList -> currentList + newItem }
        repository.addEventScheduleBlock(
            eventId = activeEvt.id,
            timeRange = "${newItem.startTime} - ${newItem.endTime}",
            title = resolvedTitle,
            ownerName = activeEvt.leadStaffName,
        )
    }

    public fun removeEventTimelineItem(itemId: String) {
        soundPlayer.playTap()
        _eventTimelineItems.update { currentList ->
            currentList.filterNot { item -> item.id == itemId }
        }
        repository.deleteEventScheduleBlock(eventId = _selectedEventId.value, blockId = itemId)
    }

    public fun toggleEventTask(taskId: String) {
        soundPlayer.playChecklistTick()
        _eventTasksList.update { currentList ->
            currentList.map { taskItem ->
                if (taskItem.id == taskId) taskItem.copy(completed = !taskItem.completed) else taskItem
            }
        }
    }

    public fun addEventTask(customTitle: String = "", badgeLabel: String = "Pendiente") {
        soundPlayer.playTap()
        val nextNum = _eventTasksList.value.size + 1
        val activeEvt = activeClientEvent()
        val resolvedTitle = customTitle.trim().ifBlank { "Coordinación #$nextNum · ${activeEvt.title}" }
        val newTask = EventTaskUiModel(
            id = "tsk-$nextNum",
            title = resolvedTitle,
            badgeText = badgeLabel.trim().ifBlank { "Pendiente" },
            isUrgent = false,
            completed = false,
        )
        _eventTasksList.update { currentList -> listOf(newTask) + currentList }
    }

    public fun toggleEventGuestStatus(guestId: String) {
        soundPlayer.playTap()
        _eventGuestsList.update { currentList ->
            currentList.map { guestItem ->
                if (guestItem.id == guestId) {
                    val nextStatus = when (guestItem.status) {
                        "Confirmado" -> "Pendiente"
                        "Pendiente" -> "Rechazado"
                        else -> "Confirmado"
                    }
                    guestItem.copy(status = nextStatus)
                } else {
                    guestItem
                }
            }
        }
    }

    public fun addEventGuest(
        guestName: String = "",
        tableNumberLabel: String = "",
        partySize: Int = 2,
        status: String = "Confirmado",
    ) {
        soundPlayer.playTap()
        val nextNum = _eventGuestsList.value.size + 1
        val resolvedName = guestName.trim().ifBlank { "Invitado #$nextNum" }
        val resolvedTable = tableNumberLabel.trim().ifBlank { "Mesa ${nextNum + 2}" }
        val peopleLabel = if (partySize == 1) "1 persona" else "${partySize.coerceAtLeast(1)} personas"
        val cleanStatus = status.trim().ifBlank { "Confirmado" }
        val newGuest = EventGuestUiModel(
            id = "gst-$nextNum",
            name = resolvedName,
            details = "$cleanStatus · $resolvedTable · $peopleLabel",
            status = cleanStatus,
        )
        _eventGuestsList.update { currentList -> currentList + newGuest }
        repository.addEventGuest(
            eventId = _selectedEventId.value,
            fullName = resolvedName,
            rsvpStatus = when (cleanStatus) {
                "Confirmado" -> "CONFIRMED"
                "Rechazado" -> "DECLINED"
                else -> "PENDING"
            },
            tableLabel = resolvedTable,
            notes = peopleLabel,
        )
    }

    public fun authenticateSocialAccount(provider: String, idToken: String = ""): Boolean {
        if (_isSubmitting.value) return false
        val cleanProvider = provider.trim().ifBlank { "OAuth" }
        val cleanToken = idToken.trim()
        if (cleanToken.isEmpty()) {
            soundPlayer.playAlertError()
            _formFeedback.value = null
            _formError.value =
                "El inicio de sesión con $cleanProvider requiere un token de identidad válido. Ingresa con tu correo y contraseña."
            return false
        }
        val looksLikeJwt = cleanToken.count { character -> character == '.' } == 2
        if (!looksLikeJwt) {
            soundPlayer.playAlertError()
            _formFeedback.value = null
            _formError.value =
                "El token de $cleanProvider no tiene un formato válido. Ingresa con tu correo y contraseña."
            return false
        }
        _isSubmitting.value = true
        _formError.value = null
        securityScope.launch {
            try {
                val outcome = repository.loginWithOAuth(cleanProvider.lowercase(), cleanToken)
                val account = outcome.account
                val isVerifiedOAuth = if (outcome.success) account != null else false
                if (isVerifiedOAuth) {
                    val nonNullAccount = account ?: return@launch
                    applyAuthenticatedAccount(nonNullAccount, outcome.accessToken)
                    soundPlayer.playPaymentSuccess()
                    _formError.value = null
                    _formFeedback.value = "Sesión iniciada con $cleanProvider."
                } else {
                    soundPlayer.playAlertError()
                    _formError.value = outcome.errorMessage
                        ?: "No se pudo verificar la identidad con $cleanProvider."
                }
            } finally {
                _isSubmitting.value = false
            }
        }
        return true
    }

    public fun requestPasswordResetOtp(emailInput: String): Boolean {
        val cleanEmail = emailInput.trim().lowercase()
        val emailError = InputValidator.validateEmail(cleanEmail)
        if (emailError != null) {
            postError(emailError)
            return false
        }
        _pendingOtpEmail.value = cleanEmail
        _loginEmail.value = cleanEmail
        _verifiedOtpResetToken.value = ""
        _activeOtpCode.value = null
        _formError.value = null
        _formFeedback.value = "Si el correo existe, recibirás un código de verificación de 6 dígitos."
        securityScope.launch {
            val challenge = repository.requestPasswordOtp(cleanEmail)
            if (!challenge.success) {
                _formError.value = challenge.errorMessage
            } else if (challenge.devCode.isNotBlank()) {
                _activeOtpCode.value = challenge.devCode
            }
        }
        return true
    }

    public fun verifyPasswordResetOtpCode(codeInput: String, onVerified: (Boolean) -> Unit = {}) {
        val cleanCode = codeInput.trim().filter { character -> character.isDigit() }
        if (cleanCode.length != 6) {
            postError("Ingresa el código de 6 dígitos enviado a tu correo.")
            onVerified(false)
            return
        }
        val targetEmail = _pendingOtpEmail.value.ifBlank { _loginEmail.value.trim().lowercase() }
        securityScope.launch {
            val outcome = repository.verifyPasswordOtp(targetEmail, cleanCode)
            if (outcome.success) {
                _verifiedOtpResetToken.value = outcome.resetToken.orEmpty()
                _activeOtpCode.value = cleanCode
                _formError.value = null
                _formFeedback.value = "Código verificado correctamente."
                onVerified(true)
            } else {
                soundPlayer.playAlertError()
                _formError.value = outcome.errorMessage ?: "El código ingresado es inválido o expiró."
                onVerified(false)
            }
        }
    }

    public fun openExternalUrl(url: String): Boolean = platformActions.openExternalUrl(url)

    public fun dialPhoneNumber(rawPhone: String): Boolean {
        val launched = platformActions.dialPhoneNumber(rawPhone)
        if (!launched) {
            _formFeedback.value = "Teléfono de contacto: $rawPhone"
        }
        return launched
    }

    public fun openWhatsAppChat(rawPhone: String = businessSettings.value.whatsapp, message: String = ""): Boolean {
        val launched = platformActions.openWhatsAppChat(rawPhone, message)
        if (!launched) {
            _formFeedback.value = "WhatsApp RosaFiesta: $rawPhone"
        }
        return launched
    }

    public fun openMapsSearch(query: String): Boolean {
        val launched = platformActions.openMapsSearch(query)
        if (!launched) {
            _formFeedback.value = "Ubicación: $query"
        }
        return launched
    }

    public fun sharePlainText(title: String, text: String): Boolean {
        val shared = platformActions.shareContent(title = title, text = text)
        if (shared) {
            _formFeedback.value = "Contenido listo para compartir."
        }
        return shared
    }

    public fun exportReceiptDocument(
        event: ClientEvent,
        payment: PaymentRecord? = null,
        shareInsteadOfSave: Boolean = false,
    ): String {
        soundPlayer.playPaymentSuccess()
        val settings = businessSettings.value
        val receiptNo = payment?.receiptNumber ?: "REC-${event.code}"
        val paidAmount = payment?.amountDop ?: event.paidDepositDop
        val remaining = (event.totalDop - paidAmount).coerceAtLeast(0)
        val lineItems = event.items.map { item ->
            ReceiptPdfLineItem(
                description = item.name,
                quantity = item.quantity,
                unitPriceDop = item.unitPriceDop.toLong(),
            )
        }
        val pdfDoc = ReceiptPdfDocument(
            receiptNumber = receiptNo,
            issuedAtText = payment?.dateLabel ?: DomainClockFormatter.currentFullDateLabel(),
            clientName = payment?.clientName ?: event.clientName,
            clientDocumentOrEmail = event.clientEmail,
            eventTitle = event.title,
            eventDateText = event.dateLabel,
            paymentMethod = payment?.methodLabel ?: event.depositMethodLabel,
            paymentReference = payment?.referenceCode.orEmpty(),
            verificationStatus = if (payment?.verified == false) "EN REVISIÓN" else "VERIFICADO",
            companyName = settings.businessName,
            companyRnc = settings.rnc,
            subtotalDop = event.totalDop.toLong(),
            paidAmountDop = paidAmount.toLong(),
            remainingBalanceDop = remaining.toLong(),
            lineItems = lineItems,
        )
        val pdfBytes = ReceiptPdfBuilder.buildPdfBytes(pdfDoc)
        val fileName = "$receiptNo.pdf"
        if (shareInsteadOfSave) {
            platformActions.shareContent(
                title = "Comprobante $receiptNo · RosaFiesta",
                text = "Recibo $receiptNo · ${event.title} · RD$ $paidAmount",
                fileName = fileName,
                fileBytes = pdfBytes,
                mimeType = "application/pdf",
            )
            _formFeedback.value = "Comprobante $receiptNo compartido en PDF."
        } else {
            val savedPath = platformActions.saveDocument(fileName, "application/pdf", pdfBytes)
            _formFeedback.value = if (savedPath != null) {
                "PDF descargado: $fileName"
            } else {
                "Comprobante $receiptNo generado."
            }
        }
        return "Recibo $receiptNo · ${event.title} (${event.dateLabel}) · Pagado: RD$ $paidAmount de RD$ ${event.totalDop}"
    }

    public fun exportReportCsv(
        reportType: String,
        period: String = "2026-10",
    ) {
        soundPlayer.playPaymentSuccess()
        securityScope.launch {
            val exported = repository.exportReport(reportType = reportType, period = period)
            val csvBytes = if (reportType.equals("dgii607", ignoreCase = true)) {
                val records = payments.value.mapIndexed { index, payment ->
                    val netAmount = ((payment.amountDop * 100) / 118).toLong()
                    val taxAmount = payment.amountDop.toLong() - netAmount
                    val paddedSeq = (index + 1).toString().padStart(8, '0')
                    Dgii607SaleRecord(
                        customerTaxId = "101554219",
                        identificationType = DgiiIdentificationType.RNC,
                        ncfNumber = "B02$paddedSeq",
                        invoiceDateYyyymmdd = "20261020",
                        invoicedSubtotalDop = netAmount,
                        invoicedItbisDop = taxAmount,
                        transferOrCheckAmountDop = payment.amountDop.toLong(),
                    )
                }
                ReportCsvBuilder.buildDgii607CsvBytes(
                    companyRnc = businessSettings.value.rnc,
                    periodYyyymm = period,
                    records = records,
                )
            } else if (exported.csvContent.isNotBlank()) {
                val utf8Body = exported.csvContent.encodeToByteArray()
                byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()) + utf8Body
            } else {
                val rows = payments.value.map { payment ->
                    val subtotal = ((payment.amountDop * 100) / 118).toLong()
                    val itbis = payment.amountDop.toLong() - subtotal
                    FinancialReportCsvRow(
                        recordCode = payment.receiptNumber,
                        eventDate = payment.dateLabel,
                        clientName = payment.clientName,
                        clientTaxId = "",
                        eventTitle = payment.eventTitle,
                        paymentMethod = payment.methodLabel,
                        verificationStatus = if (payment.verified) "VERIFICADO" else "PENDIENTE",
                        subtotalDop = subtotal,
                        itbisDop = itbis,
                        totalDop = payment.amountDop.toLong(),
                        paidDop = payment.amountDop.toLong(),
                        pendingDop = 0L,
                    )
                }
                ReportCsvBuilder.buildFinancialReportCsvBytes(rows)
            }
            val safeFileName = exported.filename.ifBlank { "rosafiesta-$reportType-$period.csv" }
            val savedPath = platformActions.saveDocument(safeFileName, "text/csv", csvBytes)
            _formFeedback.value = if (savedPath != null) {
                "Reporte exportado: $safeFileName"
            } else {
                "Reporte $safeFileName preparado."
            }
        }
    }

    public fun companyAverageRatingFormatted(): String {
        val currentReviews = _companyReviews.value
        if (currentReviews.isEmpty()) return "5.0"
        val sumRatings = currentReviews.sumOf { reviewItem -> reviewItem.rating }
        val tenths = ((sumRatings.toDouble() / currentReviews.size.toDouble()) * 10.0).toInt()
        val wholePart = tenths / 10
        val decimalPart = tenths % 10
        return "$wholePart.$decimalPart"
    }

    public fun submitCompanyReview(comment: String, rating: Int = 5): Boolean {
        val trimmedComment = comment.trim()
        if (trimmedComment.length < 6) {
            soundPlayer.playAlertError()
            _formError.value = "Escribe al menos 6 caracteres para publicar tu reseña."
            return false
        }
        val cleanRating = rating.coerceIn(1, 5)
        val authorName = _profileName.value.trim().ifBlank { "Cliente Rosa Fiesta" }
        val initialChar = authorName.firstOrNull()?.uppercaseChar()?.toString() ?: "C"
        val createdDomainReview = repository.createReview(
            eventId = _selectedEventId.value,
            authorName = authorName,
            rating = cleanRating,
            comment = trimmedComment,
        )
        val newReview = CompanyReviewUiModel(
            id = createdDomainReview.id,
            author = authorName,
            initial = initialChar,
            dateLabel = createdDomainReview.dateLabel,
            rating = cleanRating,
            comment = trimmedComment,
            isVerified = createdDomainReview.verified,
        )
        _companyReviews.update { currentList -> listOf(newReview) + currentList }
        val updatedReviews = _companyReviews.value
        securityScope.launch {
            secureStorage.put(SecureStorage.KEY_COMPANY_REVIEWS, serializeReviewsForStorage(updatedReviews))
        }
        soundPlayer.playPaymentSuccess()
        _formError.value = null
        _formFeedback.value = "¡Gracias por tu reseña de $cleanRating estrellas!"
        return true
    }

    public fun replyToSelectedReview(replyText: String): Boolean {
        val cleanReply = replyText.trim()
        if (cleanReply.length < 4) {
            postError("Escribe una respuesta de al menos 4 caracteres.")
            return false
        }
        val targetReview = selectedReview() ?: return false
        repository.replyToReview(targetReview.id, cleanReply)
        soundPlayer.playPaymentSuccess()
        _formError.value = null
        _formFeedback.value = "Respuesta publicada para ${targetReview.authorName}."
        return true
    }

    public fun sendDirectChatMessage(rawText: String, attachmentName: String = "") {
        val trimmedText = rawText.trim()
        val cleanAttachment = attachmentName.trim()
        val isBothEmpty = if (trimmedText.isEmpty()) cleanAttachment.isEmpty() else false
        if (isBothEmpty) return
        soundPlayer.playRosaSend()
        val clientName = _profileName.value.substringBefore(" ").ifBlank { "Cliente" }
        val redactedBody = if (trimmedText.isNotEmpty()) {
            SensitiveDataRedactor.redactText(trimmedText)
        } else {
            "Archivo adjunto: $cleanAttachment"
        }
        val timestampLabel = DomainClockFormatter.currentTimeHmLabel()
        val clientMessage = DirectChatMessageUiModel(
            id = "msg-${_directChatMessages.value.size + 1}",
            senderName = clientName,
            text = redactedBody,
            timeLabel = timestampLabel,
            isFromClient = true,
            attachmentName = cleanAttachment,
        )
        _directChatMessages.update { existingMessages ->
            existingMessages + clientMessage
        }
        val updatedChat = _directChatMessages.value
        securityScope.launch {
            secureStorage.put(SecureStorage.KEY_DIRECT_CHAT, serializeChatForStorage(updatedChat))
        }
        val activeThreadId = _selectedChatThreadId.value
        repository.sendThreadMessage(
            threadId = activeThreadId,
            senderName = clientName,
            body = redactedBody,
            attachmentName = cleanAttachment,
        )
        repository.sendChatMessage(clientName, redactedBody)
        _pickedAttachmentFile.value = null
    }

    public fun sendAdminDirectChatReply(rawReplyText: String, attachmentName: String = "") {
        val trimmedReply = rawReplyText.trim()
        val cleanAttachment = attachmentName.trim()
        val isBothEmpty = if (trimmedReply.isEmpty()) cleanAttachment.isEmpty() else false
        if (isBothEmpty) return
        soundPlayer.playRosaSend()
        val senderLabel = _profileName.value.trim().ifBlank { "María · Rosa Fiesta" }
        val redactedReply = if (trimmedReply.isNotEmpty()) {
            SensitiveDataRedactor.redactText(trimmedReply)
        } else {
            "Archivo adjunto: $cleanAttachment"
        }
        val timestampLabel = DomainClockFormatter.currentTimeHmLabel()
        val adminReply = DirectChatMessageUiModel(
            id = "msg-${_directChatMessages.value.size + 1}",
            senderName = senderLabel,
            text = redactedReply,
            timeLabel = timestampLabel,
            isFromClient = false,
            attachmentName = cleanAttachment,
        )
        _directChatMessages.update { existingMessages ->
            existingMessages + adminReply
        }
        val updatedChat = _directChatMessages.value
        securityScope.launch {
            secureStorage.put(SecureStorage.KEY_DIRECT_CHAT, serializeChatForStorage(updatedChat))
        }
        repository.sendThreadMessage(
            threadId = _selectedChatThreadId.value,
            senderName = senderLabel,
            body = redactedReply,
            attachmentName = cleanAttachment,
        )
        repository.sendChatMessage(senderLabel, redactedReply)
    }

    public fun addSupplier(
        name: String,
        category: String,
        phone: String,
        statusLabel: String = "Confirmado",
        contactName: String = "",
        email: String = "",
        website: String = "",
        notes: String = "",
    ): Boolean {
        val cleanName = name.trim()
        val cleanCat = category.trim().ifBlank { "Servicios" }
        val cleanPhone = phone.trim()
        if (cleanName.length < 2) {
            soundPlayer.playAlertError()
            _formError.value = "Ingresa el nombre del proveedor."
            return false
        }
        if (cleanPhone.length < 7) {
            soundPlayer.playAlertError()
            _formError.value = "Ingresa un teléfono válido del proveedor."
            return false
        }
        val domainSupplier = repository.createSupplier(
            name = cleanName,
            category = cleanCat,
            contactName = contactName.trim().ifBlank { cleanName },
            phone = cleanPhone,
            email = email.trim(),
            website = website.trim(),
            notes = notes.trim(),
        )
        val created = SupplierUiModel(
            id = domainSupplier.id,
            name = cleanName,
            category = cleanCat,
            phone = cleanPhone,
            ratingLabel = "5.0",
            statusLabel = statusLabel,
        )
        _suppliersList.update { existing -> listOf(created) + existing }
        val snapshot = _suppliersList.value
        securityScope.launch {
            secureStorage.put(SecureStorage.KEY_SUPPLIERS, serializeSuppliersForStorage(snapshot))
        }
        soundPlayer.playPaymentSuccess()
        _formError.value = null
        _formFeedback.value = "Proveedor '$cleanName' guardado."
        return true
    }

    public fun deleteCurrentUserAccount(): Boolean {
        if (activeClientEventCount() > 0) {
            postError("No puedes eliminar tu cuenta mientras tengas eventos activos pendientes.")
            return false
        }
        soundPlayer.playAlertError()
        val deleted = repository.deleteCurrentAccount()
        if (!deleted) {
            postError("No se pudo eliminar la cuenta en este momento.")
            return false
        }
        _isExplicitGuestSession.value = true
        securityScope.launch {
            secureStorage.remove(SecureStorage.KEY_ACCESS_TOKEN)
            secureStorage.remove(SecureStorage.KEY_USER_ROLE)
        }
        _profileName.value = "Invitado"
        _profileEmail.value = ""
        _profileUsername.value = "invitado"
        _formFeedback.value = "Tu cuenta y sesión activa han sido eliminadas."
        return true
    }

    public fun saveBusinessConfig(updatedSettings: BusinessSettings? = null): Boolean {
        val dep = updatedSettings?.depositPercent ?: (_depositPercentInput.value.trim().toIntOrNull() ?: 0)
        val deliv = _deliveryBaseInput.value.trim().toIntOrNull() ?: 350
        val validDeposit = dep in 10..90
        val validDelivery = deliv >= 0
        val isConfigValid = if (validDeposit) validDelivery else false
        if (!isConfigValid) {
            _formError.value = "Anticipo debe estar entre 10% y 90% y tarifa de envío >= 0."
            return false
        }
        val baseSettings = updatedSettings ?: businessSettings.value.copy(depositPercent = dep)
        repository.saveBusinessSettings(baseSettings)
        _depositPercentInput.value = baseSettings.depositPercent.toString()
        soundPlayer.playPaymentSuccess()
        _formError.value = null
        _formFeedback.value = "Ajustes del negocio guardados correctamente."
        return true
    }

    public fun approveQuoteById(
        quoteId: String = _selectedQuoteId.value,
        signedByName: String,
        signatureSvg: String,
    ): QuoteRecord? {
        val cleanSigner = signedByName.trim()
        if (cleanSigner.length < 3) {
            postError("Escribe tu nombre completo para firmar el contrato.")
            return null
        }
        val approved = repository.approveQuote(quoteId, cleanSigner, signatureSvg)
        if (approved != null) {
            soundPlayer.playPaymentSuccess()
            _formError.value = null
            _formFeedback.value = "Contrato firmado por $cleanSigner."
        }
        return approved
    }

    public fun declineQuoteById(
        quoteId: String = _selectedQuoteId.value,
        reason: String = "Solicita ajuste",
    ): QuoteRecord? {
        val declined = repository.declineQuote(quoteId, reason)
        if (declined != null) {
            _formFeedback.value = "Cotización marcada como rechazada."
        }
        return declined
    }

    public fun duplicateQuoteById(quoteId: String): QuoteRecord? {
        val duplicated = repository.duplicateQuote(quoteId)
        if (duplicated != null) {
            _selectedQuoteId.value = duplicated.id
            _formFeedback.value = "Cotización duplicada (${duplicated.code})."
        }
        return duplicated
    }

    public fun createFormalQuoteFromAdmin(
        eventTitle: String,
        clientName: String,
        clientEmail: String = "",
        venue: String,
        dateLabel: String,
        guests: Int,
        lines: List<EventLineItem>,
        deliveryFeeDop: Int = 350,
        discountDop: Int = 0,
    ): QuoteRecord? {
        if (lines.isEmpty()) {
            postError("Agrega al menos una línea a la cotización antes de enviarla.")
            return null
        }
        val created = repository.createQuote(
            eventTitle = eventTitle.trim().ifBlank { "Evento RosaFiesta" },
            clientName = clientName.trim().ifBlank { "Cliente" },
            clientEmail = clientEmail.trim(),
            venue = venue.trim().ifBlank { "Por confirmar" },
            dateLabel = dateLabel.trim().ifBlank { "Por confirmar" },
            guests = guests.coerceAtLeast(1),
            lines = lines,
            deliveryFeeDop = deliveryFeeDop.coerceAtLeast(0),
            discountDop = discountDop.coerceAtLeast(0),
        )
        _selectedQuoteId.value = created.id
        soundPlayer.playPaymentSuccess()
        _formError.value = null
        _formFeedback.value = "Cotización ${created.code} enviada a ${created.clientName}."
        return created
    }

    public fun approveSelectedRefund(): RefundRequest? {
        val target = selectedRefund() ?: return null
        val approved = repository.approveRefund(target.id)
        if (approved != null) {
            soundPlayer.playPaymentSuccess()
            _formFeedback.value = "Reembolso de ${RosaFiestaEngine.formatDop(approved.refundAmountDop)} aprobado para ${approved.clientName}."
        }
        return approved
    }

    public fun duplicateExistingEvent(eventId: String): ClientEvent? {
        val cloned = repository.duplicateEvent(eventId)
        if (cloned != null) {
            _selectedEventId.value = cloned.id
            _newEventTitle.value = cloned.title
            _newEventClient.value = cloned.clientName
            _newEventDate.value = cloned.dateLabel
            _newEventVenue.value = cloned.venue
            _newEventGuests.value = cloned.guests.toString()
            _formFeedback.value = "Evento '${cloned.title}' duplicado en borrador."
        }
        return cloned
    }

    public fun saveAdminPackage(
        name: String,
        subtitle: String,
        eventType: String,
        suggestedGuests: Int,
        priceDop: Int,
        includedItems: List<String>,
        visibleInCatalog: Boolean = true,
    ): EventPackage? {
        val cleanName = name.trim()
        val hasValidPackageFields = if (cleanName.length >= 3) priceDop > 0 else false
        if (!hasValidPackageFields) {
            postError("Indica nombre válido y precio mayor que cero para el paquete.")
            return null
        }
        val editingId = _editingPackageId.value
        val saved = if (editingId != null) {
            repository.updatePackage(
                packageId = editingId,
                name = cleanName,
                subtitle = subtitle.trim(),
                eventType = eventType.trim().ifBlank { "Evento" },
                suggestedGuests = suggestedGuests.coerceAtLeast(10),
                priceDop = priceDop,
                includedItems = includedItems,
                visibleInCatalog = visibleInCatalog,
            )
        } else {
            repository.createPackage(
                name = cleanName,
                subtitle = subtitle.trim(),
                eventType = eventType.trim().ifBlank { "Evento" },
                suggestedGuests = suggestedGuests.coerceAtLeast(10),
                priceDop = priceDop,
                includedItems = includedItems,
                visibleInCatalog = visibleInCatalog,
            )
        }
        _editingPackageId.value = null
        soundPlayer.playPaymentSuccess()
        _formError.value = null
        _formFeedback.value = "Paquete '$cleanName' guardado."
        return saved
    }

    public fun updateSelectedClientProfile(
        fullName: String,
        email: String,
        phone: String,
        notes: String,
    ): ClientProfile? {
        val target = selectedClient() ?: return null
        val updated = repository.updateClientProfile(
            clientId = target.id,
            fullName = fullName.trim().ifBlank { target.fullName },
            email = email.trim().ifBlank { target.email },
            phone = phone.trim().ifBlank { target.phone },
            notes = notes.trim(),
        )
        if (updated != null) {
            soundPlayer.playPaymentSuccess()
            _formFeedback.value = "Ficha de ${updated.fullName} actualizada."
        }
        return updated
    }

    public fun inviteNewStaffMember(
        fullName: String,
        email: String,
        roleLabel: String,
        phone: String = "",
        permissions: List<String> = emptyList(),
    ): StaffMember? {
        val cleanName = fullName.trim()
        val cleanEmail = email.trim().lowercase()
        val emailErr = InputValidator.validateEmail(cleanEmail)
        val hasValidInviteFields = if (cleanName.length >= 2) emailErr == null else false
        if (!hasValidInviteFields) {
            postError(emailErr ?: "Ingresa el nombre completo del colaborador.")
            return null
        }
        val invited = repository.inviteStaffMember(
            fullName = cleanName,
            email = cleanEmail,
            roleLabel = roleLabel.trim().ifBlank { "Operaciones" },
            phone = phone.trim(),
            permissions = permissions,
        )
        soundPlayer.playPaymentSuccess()
        _formFeedback.value = "Invitación enviada a $cleanName ($cleanEmail)."
        return invited
    }

    public fun updateStaffPermissions(
        staffId: String,
        permissions: List<String>,
        active: Boolean? = null,
    ): StaffMember? {
        val updated = repository.updateStaffMember(
            staffId = staffId,
            active = active,
            permissions = permissions,
        )
        if (updated != null) {
            soundPlayer.playTap()
            _formFeedback.value = "Permisos actualizados para ${updated.name}."
        }
        return updated
    }

    public fun reportEventDamageIncident(
        eventId: String,
        articleCode: String,
        articleName: String,
        damagedUnits: Int,
        missingUnits: Int,
        replacementDop: Int,
        notes: String,
    ): DamageIncident {
        val created = repository.reportDamage(
            eventId = eventId,
            articleCode = articleCode,
            articleName = articleName,
            damagedUnits = damagedUnits.coerceAtLeast(0),
            missingUnits = missingUnits.coerceAtLeast(0),
            replacementDop = replacementDop.coerceAtLeast(0),
            notes = notes.trim(),
        )
        soundPlayer.playChecklistTick()
        _formFeedback.value = "Inspección registrada para $articleName."
        return created
    }

    public fun completeEventInspection(eventId: String) {
        repository.updateEventDetails(eventId = eventId, status = EventStatus.COMPLETED)
        soundPlayer.playPaymentSuccess()
        _formFeedback.value = "Inspección cerrada y evento marcado como completado."
    }

    public fun createCommercialLead(
        clientName: String,
        eventType: String,
        dateLabel: String,
        guests: Int,
        estimatedDop: Int,
        channel: String = "WhatsApp",
        followUpNote: String = "",
        phone: String = "",
    ): LeadRecord? {
        val cleanClient = clientName.trim()
        if (cleanClient.length < 2) {
            postError("Indica el nombre del prospecto.")
            return null
        }
        val created = repository.createLead(
            clientName = cleanClient,
            eventType = eventType.trim().ifBlank { "Evento social" },
            dateLabel = dateLabel.trim().ifBlank { "Por definir" },
            guests = guests.coerceAtLeast(10),
            estimatedDop = estimatedDop.coerceAtLeast(300),
            channel = channel,
            followUpNote = followUpNote.trim(),
            phone = phone.trim(),
        )
        soundPlayer.playPaymentSuccess()
        _formFeedback.value = "Oportunidad registrada para $cleanClient."
        return created
    }

    public fun updateCommercialLead(
        leadId: String,
        stage: String? = null,
        followUpNote: String? = null,
    ): LeadRecord? = repository.updateLead(leadId = leadId, stage = stage, followUpNote = followUpNote)

    public fun markNotificationAsRead(notificationId: String) {
        repository.markNotificationRead(notificationId)
    }

    public fun saveBotFaqItem(
        question: String,
        answer: String,
        category: String = "General",
        active: Boolean = true,
        id: String = "",
    ): Boolean {
        val cleanQuestion = question.trim()
        val cleanAnswer = answer.trim()
        val isValid = if (cleanQuestion.length >= 4) cleanAnswer.length >= 4 else false
        if (!isValid) {
            postError("Completa la pregunta y la respuesta automática.")
            return false
        }
        repository.saveBotFaq(
            question = cleanQuestion,
            answer = cleanAnswer,
            category = category.trim().ifBlank { "General" },
            active = active,
            id = id,
        )
        soundPlayer.playPaymentSuccess()
        _formFeedback.value = "Pregunta frecuente guardada en el Bot."
        return true
    }

    public fun saveBotKeywordRule(
        keyword: String,
        autoReply: String,
        handoffToHuman: Boolean = false,
        id: String = "",
    ): Boolean {
        val cleanKeyword = keyword.trim()
        val cleanReply = autoReply.trim()
        val isValid = if (cleanKeyword.length >= 2) cleanReply.length >= 4 else false
        if (!isValid) {
            postError("Ingresa la palabra clave y su respuesta automática.")
            return false
        }
        repository.saveBotKeyword(
            keyword = cleanKeyword,
            autoReply = cleanReply,
            handoffToHuman = handoffToHuman,
            id = id,
        )
        soundPlayer.playPaymentSuccess()
        _formFeedback.value = "Regla '$cleanKeyword' guardada en el Bot."
        return true
    }

    public fun toggleBotThreadPause(threadId: String, paused: Boolean, assignedAgent: String? = null) {
        repository.updateBotThread(threadId = threadId, botPaused = paused, assignedAgent = assignedAgent)
        _formFeedback.value = if (paused) "Bot pausado · Conversación derivada a agente humano." else "Bot reactivado en la conversación."
    }

    public suspend fun queryWarehouseAvailability(
        articleId: String,
        monthIso: String = DomainClockFormatter.currentYearMonthIso(),
    ): ArticleAvailabilityCalendar = repository.queryArticleAvailability(articleId = articleId, monthIso = monthIso)

    public fun getAdminOnboardingSummary(): AdminOnboardingSummary {
        val currentEvents = events.value
        val leadEvent = currentEvents.firstOrNull { eventItem -> eventItem.id == "evt-boda-real" }
            ?: currentEvents.firstOrNull()
        val currentArticles = articles.value
        val primaryChair = currentArticles.firstOrNull { articleItem -> articleItem.id == "art-silla-tiffany" }
            ?: currentArticles.firstOrNull()
        val currentMetrics = metrics.value

        return AdminOnboardingSummary(
            activeEventsCount = currentEvents.size,
            leadEventTitle = leadEvent?.title ?: "Sin eventos registrados",
            leadEventDate = leadEvent?.dateLabel ?: "—",
            leadEventGuests = leadEvent?.guests ?: 0,
            leadEventVenue = leadEvent?.venue ?: "—",
            leadEventItemsCount = leadEvent?.items?.sumOf { item -> item.quantity } ?: 0,
            primaryArticleTitle = primaryChair?.name ?: "Catálogo vacío",
            primaryArticleAvailableStock = primaryChair?.availableOct23 ?: 0,
            monthlyIncomeDop = currentMetrics.octoberRevenueDop,
            depositPendingDop = currentMetrics.accountsReceivableDop,
            totalArticlesInCatalog = currentArticles.size,
        )
    }
}
