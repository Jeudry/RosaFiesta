package com.jeudry.rosafiesta.ui

import androidx.lifecycle.ViewModel
import com.jeudry.rosafiesta.core.engine.CancellationRefund
import com.jeudry.rosafiesta.core.engine.DateChangeResult
import com.jeudry.rosafiesta.core.engine.RosaFiestaEngine
import com.jeudry.rosafiesta.core.engine.StaffPermission
import com.jeudry.rosafiesta.domain.ArticleAvailabilityCalendar
import com.jeudry.rosafiesta.domain.AuditLogRecord
import com.jeudry.rosafiesta.domain.AvailabilityRangeCheck
import com.jeudry.rosafiesta.domain.BalloonProduct
import com.jeudry.rosafiesta.domain.BotFaqRecord
import com.jeudry.rosafiesta.domain.BotKeywordRecord
import com.jeudry.rosafiesta.domain.BotThreadRecord
import com.jeudry.rosafiesta.domain.BusinessMetrics
import com.jeudry.rosafiesta.domain.BusinessSettings
import com.jeudry.rosafiesta.domain.CardSessionResult
import com.jeudry.rosafiesta.domain.CatalogArticle
import com.jeudry.rosafiesta.domain.CatalogCategory
import com.jeudry.rosafiesta.domain.CatalogTrend
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
import com.jeudry.rosafiesta.domain.EventTaskRecord
import com.jeudry.rosafiesta.domain.EventTypeTemplate
import com.jeudry.rosafiesta.domain.InsurancePolicyRecord
import com.jeudry.rosafiesta.domain.LeadRecord
import com.jeudry.rosafiesta.domain.MediaUploadAsset
import com.jeudry.rosafiesta.domain.NotificationRecord
import com.jeudry.rosafiesta.domain.OAuthProviderConfig
import com.jeudry.rosafiesta.domain.OfflineQueueItem
import com.jeudry.rosafiesta.domain.PaymentRecord
import com.jeudry.rosafiesta.domain.PaymentReminderOutcome
import com.jeudry.rosafiesta.domain.PaymentsAnalyticsSummary
import com.jeudry.rosafiesta.domain.PeriodSpendingAnalytics
import com.jeudry.rosafiesta.domain.PhotoAsset
import com.jeudry.rosafiesta.domain.QuoteRecord
import com.jeudry.rosafiesta.domain.RealtimeStreamEvent
import com.jeudry.rosafiesta.domain.RefundApprovalOptions
import com.jeudry.rosafiesta.domain.RefundRequest
import com.jeudry.rosafiesta.domain.ReviewRecord
import com.jeudry.rosafiesta.domain.RosaChatReply
import com.jeudry.rosafiesta.domain.RosaFiestaRepository
import com.jeudry.rosafiesta.domain.RosaSketchProposal
import com.jeudry.rosafiesta.domain.SeasonBanner
import com.jeudry.rosafiesta.domain.SpendingAnalyticsCalculator
import com.jeudry.rosafiesta.domain.StaffMember
import com.jeudry.rosafiesta.domain.SupplierRecord
import com.jeudry.rosafiesta.domain.platform.AppLifecycleBridge
import com.jeudry.rosafiesta.domain.platform.Dgii607SaleRecord
import com.jeudry.rosafiesta.domain.platform.DgiiIdentificationType
import com.jeudry.rosafiesta.domain.platform.ExternalOAuthCallbackBridge
import com.jeudry.rosafiesta.domain.platform.ExternalPaymentCallbackBridge
import com.jeudry.rosafiesta.domain.platform.ExternalPaymentReturn
import com.jeudry.rosafiesta.domain.platform.FinancialReportCsvRow
import com.jeudry.rosafiesta.domain.platform.NoOpPlatformActions
import com.jeudry.rosafiesta.domain.platform.OAuthBrowserOutcome
import com.jeudry.rosafiesta.domain.platform.PickedMediaFile
import com.jeudry.rosafiesta.domain.platform.PlatformActions
import com.jeudry.rosafiesta.domain.platform.ReceiptPdfBuilder
import com.jeudry.rosafiesta.domain.platform.ReceiptPdfDocument
import com.jeudry.rosafiesta.domain.platform.ReceiptPdfLineItem
import com.jeudry.rosafiesta.domain.platform.ReportCsvBuilder
import com.jeudry.rosafiesta.domain.security.AuthOutcome
import com.jeudry.rosafiesta.domain.security.InMemorySecureStorage
import com.jeudry.rosafiesta.domain.security.JwtAuthManager
import com.jeudry.rosafiesta.domain.security.RegisteredUserAccount
import com.jeudry.rosafiesta.domain.security.SecureStorage
import com.jeudry.rosafiesta.domain.security.SensitiveDataRedactor
import com.jeudry.rosafiesta.domain.sound.AppAudioConfig
import com.jeudry.rosafiesta.domain.sound.NoOpSoundPlayer
import com.jeudry.rosafiesta.domain.sound.NoOpVoiceInputMonitor
import com.jeudry.rosafiesta.domain.sound.SoundEffect
import com.jeudry.rosafiesta.domain.sound.SoundEffectCategory
import com.jeudry.rosafiesta.domain.sound.SoundPlayer
import com.jeudry.rosafiesta.domain.sound.VoiceInputMonitor
import com.jeudry.rosafiesta.domain.validation.InputValidator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.random.Random

/**
 * Origin context from which voice input capture is initiated.
 */
public enum class VoiceInputOrigin {
    NONE,
    SEARCH,
    ROSA_WIZARD,
    NOTES_INLINE,
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

/**
 * Compact item preview shown briefly when an article is saved to the event draft.
 */
public data class SavedCartItemPreview(
    public val articleId: String,
    public val title: String,
    public val quantity: Int,
    public val unitPriceDop: Int,
    public val photoUrl: String = "",
    public val photo: PhotoAsset = PhotoAsset.SILLA_TIFFANY,
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

    public val catalogCategories: StateFlow<List<CatalogCategory>> = repository.catalogCategories
    public val catalogTrends: StateFlow<List<CatalogTrend>> = repository.catalogTrends
    public val seasonBanner: StateFlow<SeasonBanner> = repository.seasonBanner
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

    private val _rosaEventType = MutableStateFlow("")
    public val rosaEventType: StateFlow<String> = _rosaEventType.asStateFlow()

    private val _rosaGuestsCount = MutableStateFlow(120)
    public val rosaGuestsCount: StateFlow<Int> = _rosaGuestsCount.asStateFlow()
    public val rosaGuests: StateFlow<Int> = _rosaGuestsCount.asStateFlow()

    private val _rosaBudgetDop = MutableStateFlow(0)
    public val rosaBudgetDop: StateFlow<Int> = _rosaBudgetDop.asStateFlow()

    private val _rosaPalette = MutableStateFlow("Rosa & Dorado")
    public val rosaPalette: StateFlow<String> = _rosaPalette.asStateFlow()

    private val _rosaProposalMessage = MutableStateFlow("")
    public val rosaProposalMessage: StateFlow<String> = _rosaProposalMessage.asStateFlow()

    private val _clientEventTitleInput = MutableStateFlow("")
    public val clientEventTitleInput: StateFlow<String> = _clientEventTitleInput.asStateFlow()

    private val _guestsWizardMode = MutableStateFlow(false)
    public val guestsWizardMode: StateFlow<Boolean> = _guestsWizardMode.asStateFlow()

    private val _isEventDraftSetupMode = MutableStateFlow(false)
    public val isEventDraftSetupMode: StateFlow<Boolean> = _isEventDraftSetupMode.asStateFlow()

    private val _eventDraftSetupReturnScreen = MutableStateFlow("s25")
    public val eventDraftSetupReturnScreen: StateFlow<String> = _eventDraftSetupReturnScreen.asStateFlow()

    private val _voiceAppendToIdea = MutableStateFlow(false)
    public val voiceAppendToIdea: StateFlow<Boolean> = _voiceAppendToIdea.asStateFlow()

    private val _wizardScheduleItems = MutableStateFlow<List<EventTimelineItemUiModel>>(emptyList())
    public val wizardScheduleItems: StateFlow<List<EventTimelineItemUiModel>> = _wizardScheduleItems.asStateFlow()

    private val _calendarSyncPromptDismissedIds = MutableStateFlow<Set<String>>(emptySet())
    public val calendarSyncPromptDismissedIds: StateFlow<Set<String>> = _calendarSyncPromptDismissedIds.asStateFlow()

    private val _voiceInputOrigin = MutableStateFlow(VoiceInputOrigin.NONE)
    public val voiceInputOrigin: StateFlow<VoiceInputOrigin> = _voiceInputOrigin.asStateFlow()

    private val _voiceTranscript = MutableStateFlow("")
    public val voiceTranscript: StateFlow<String> = _voiceTranscript.asStateFlow()

    private val _isRemoteVoiceTranscribing = MutableStateFlow(false)
    public val isRemoteVoiceTranscribing: StateFlow<Boolean> = _isRemoteVoiceTranscribing.asStateFlow()

    private val remoteVoiceErrorState = MutableStateFlow<String?>(null)

    public val isVoiceListening: StateFlow<Boolean> = voiceInputMonitor.isListening
    public val isVoiceTranscribing: StateFlow<Boolean> = combine(
        voiceInputMonitor.isTranscribing,
        _isRemoteVoiceTranscribing,
    ) { localTranscribing, remoteTranscribing ->
        listOf(localTranscribing, remoteTranscribing).any { active -> active }
    }.stateIn(securityScope, SharingStarted.Eagerly, false)
    public val voiceAmplitude: StateFlow<Float> = voiceInputMonitor.amplitudeLevel
    public val hasHardwareMicrophone: StateFlow<Boolean> = voiceInputMonitor.hasHardwareMicrophone
    public val voiceErrorMessage: StateFlow<String?> = combine(
        voiceInputMonitor.errorMessage,
        remoteVoiceErrorState,
    ) { hardwareError, remoteError ->
        remoteError ?: hardwareError
    }.stateIn(securityScope, SharingStarted.Eagerly, null)

    private var inlineNotesBaseText: String = ""
    private var hasNativeTranscriptForSession: Boolean = false
    private var voiceCaptureSessionId: Long = 0L
    private var transcribedSessionId: Long = -1L
    private var activeTranscriptionJob: Job? = null

    private val _loginEmail = MutableStateFlow("")
    public val loginEmail: StateFlow<String> = _loginEmail.asStateFlow()

    private val _loginPassword = MutableStateFlow("")
    public val loginPassword: StateFlow<String> = _loginPassword.asStateFlow()

    private val _recoveryInitialMode = MutableStateFlow("password")
    public val recoveryInitialMode: StateFlow<String> = _recoveryInitialMode.asStateFlow()

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

    private val _newEventTitle = MutableStateFlow("")
    public val newEventTitle: StateFlow<String> = _newEventTitle.asStateFlow()

    private val _newEventClient = MutableStateFlow("")
    public val newEventClient: StateFlow<String> = _newEventClient.asStateFlow()

    private val _newEventDate = MutableStateFlow("")
    public val newEventDate: StateFlow<String> = _newEventDate.asStateFlow()

    private val _newEventVenue = MutableStateFlow("")
    public val newEventVenue: StateFlow<String> = _newEventVenue.asStateFlow()

    private val _newEventGuests = MutableStateFlow("")
    public val newEventGuests: StateFlow<String> = _newEventGuests.asStateFlow()

    private val _paymentAmountInput = MutableStateFlow("")
    public val paymentAmountInput: StateFlow<String> = _paymentAmountInput.asStateFlow()

    private val _paymentMethodInput = MutableStateFlow("")
    public val paymentMethodInput: StateFlow<String> = _paymentMethodInput.asStateFlow()

    private val _paymentRefInput = MutableStateFlow("")
    public val paymentRefInput: StateFlow<String> = _paymentRefInput.asStateFlow()

    private val _dateChangeInput = MutableStateFlow("")
    public val dateChangeInput: StateFlow<String> = _dateChangeInput.asStateFlow()

    private val _dateChangeBanner = MutableStateFlow("Primer cambio gratis con al menos 48h de antelación.")
    public val dateChangeBanner: StateFlow<String> = _dateChangeBanner.asStateFlow()

    private val _profileName = MutableStateFlow("Invitado")
    public val profileName: StateFlow<String> = _profileName.asStateFlow()

    private val _profileEmail = MutableStateFlow("")
    public val profileEmail: StateFlow<String> = _profileEmail.asStateFlow()

    private val _profileUsername = MutableStateFlow("invitado")
    public val profileUsername: StateFlow<String> = _profileUsername.asStateFlow()

    private val _profileBirthDate = MutableStateFlow("")
    public val profileBirthDate: StateFlow<String> = _profileBirthDate.asStateFlow()

    private val _profilePhone = MutableStateFlow("")
    public val profilePhone: StateFlow<String> = _profilePhone.asStateFlow()

    private val _profileZone = MutableStateFlow("")
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

    private val _eventTimelineItems = MutableStateFlow<List<EventTimelineItemUiModel>>(emptyList())
    public val eventTimelineItems: StateFlow<List<EventTimelineItemUiModel>> = _eventTimelineItems.asStateFlow()

    private val tasksByEventId = mutableMapOf<String, List<EventTaskUiModel>>()
    private val sessionCreatedEventIds = mutableSetOf<String>()

    private val _eventTasksList = MutableStateFlow<List<EventTaskUiModel>>(emptyList())
    public val eventTasksList: StateFlow<List<EventTaskUiModel>> = _eventTasksList.asStateFlow()

    private val _eventGuestsList = MutableStateFlow<List<EventGuestUiModel>>(emptyList())
    public val eventGuestsList: StateFlow<List<EventGuestUiModel>> = _eventGuestsList.asStateFlow()

    private val _editingSupplierId = MutableStateFlow<String?>(null)
    public val editingSupplierId: StateFlow<String?> = _editingSupplierId.asStateFlow()

    private val _companyReviews = MutableStateFlow<List<CompanyReviewUiModel>>(emptyList())
    public val companyReviews: StateFlow<List<CompanyReviewUiModel>> = _companyReviews.asStateFlow()

    private val _suppliersList = MutableStateFlow<List<SupplierUiModel>>(emptyList())
    public val suppliersList: StateFlow<List<SupplierUiModel>> = _suppliersList.asStateFlow()

    private val _directChatMessages = MutableStateFlow<List<DirectChatMessageUiModel>>(emptyList())
    public val directChatMessages: StateFlow<List<DirectChatMessageUiModel>> = _directChatMessages.asStateFlow()

    private val _supportSubject = MutableStateFlow("")
    public val supportSubject: StateFlow<String> = _supportSubject.asStateFlow()

    private val _supportMessage = MutableStateFlow("")
    public val supportMessage: StateFlow<String> = _supportMessage.asStateFlow()

    private val _chatInput = MutableStateFlow("")
    public val chatInput: StateFlow<String> = _chatInput.asStateFlow()

    private val _newArticleCode = MutableStateFlow("")
    public val newArticleCode: StateFlow<String> = _newArticleCode.asStateFlow()

    private val _newArticleName = MutableStateFlow("")
    public val newArticleName: StateFlow<String> = _newArticleName.asStateFlow()

    private val _newArticleCategory = MutableStateFlow(repository.catalogCategories.value.firstOrNull()?.name.orEmpty())
    public val newArticleCategory: StateFlow<String> = _newArticleCategory.asStateFlow()

    private val _newArticlePrice = MutableStateFlow("")
    public val newArticlePrice: StateFlow<String> = _newArticlePrice.asStateFlow()

    private val _newArticleStock = MutableStateFlow("")
    public val newArticleStock: StateFlow<String> = _newArticleStock.asStateFlow()

    private val _promoCodeInput = MutableStateFlow("")
    public val promoCodeInput: StateFlow<String> = _promoCodeInput.asStateFlow()

    private val _promoDiscountInput = MutableStateFlow("")
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

    private val _rememberEntryChoice = MutableStateFlow(false)
    public val rememberEntryChoice: StateFlow<Boolean> = _rememberEntryChoice.asStateFlow()

    private val _audioConfig = MutableStateFlow(AppAudioConfig.DEFAULT)
    public val audioConfig: StateFlow<AppAudioConfig> = _audioConfig.asStateFlow()

    private val _soundEffectsEnabled = MutableStateFlow(true)
    public val soundEffectsEnabled: StateFlow<Boolean> = _soundEffectsEnabled.asStateFlow()

    private val _formError = MutableStateFlow<String?>(null)
    public val formError: StateFlow<String?> = _formError.asStateFlow()

    private val _formFeedback = MutableStateFlow<String?>(null)
    public val formFeedback: StateFlow<String?> = _formFeedback.asStateFlow()

    private val _actionToast = MutableStateFlow<String?>(null)
    public val actionToast: StateFlow<String?> = _actionToast.asStateFlow()

    public fun clearActionToast() {
        _actionToast.value = null
    }

    public fun showActionToast(message: String) {
        val cleanMessage = message.trim()
        if (cleanMessage.isNotEmpty()) {
            _savedItemPreview.value = null
            _actionToast.value = cleanMessage
        }
    }

    private val _savedItemPreview = MutableStateFlow<SavedCartItemPreview?>(null)
    public val savedItemPreview: StateFlow<SavedCartItemPreview?> = _savedItemPreview.asStateFlow()

    public fun showSavedItemPreview(article: CatalogArticle, quantity: Int) {
        _actionToast.value = null
        _savedItemPreview.value = SavedCartItemPreview(
            articleId = article.id,
            title = article.name,
            quantity = quantity.coerceAtLeast(1),
            unitPriceDop = article.pricePerDayDop,
            photoUrl = article.photoUrl,
            photo = article.photo,
        )
    }

    public fun showSavedPackagePreview(packageItem: EventPackage) {
        _actionToast.value = null
        _savedItemPreview.value = SavedCartItemPreview(
            articleId = packageItem.id,
            title = packageItem.name,
            quantity = 1,
            unitPriceDop = packageItem.priceDop,
            photoUrl = packageItem.photoUrl,
            photo = packageItem.photo,
        )
    }

    private fun showSavedPreviewForItemId(itemId: String, quantity: Int) {
        val matchedArticle = articles.value.firstOrNull { candidate -> candidate.id == itemId }
        if (matchedArticle != null) {
            showSavedItemPreview(matchedArticle, quantity)
            return
        }
        val matchedBalloon = balloonProducts.value.firstOrNull { candidate -> candidate.id == itemId }
        if (matchedBalloon != null) {
            _actionToast.value = null
            _savedItemPreview.value = SavedCartItemPreview(
                articleId = matchedBalloon.id,
                title = matchedBalloon.name,
                quantity = quantity.coerceAtLeast(1),
                unitPriceDop = matchedBalloon.priceDop,
                photo = matchedBalloon.photo,
            )
        }
    }

    public fun clearSavedItemPreview() {
        _savedItemPreview.value = null
    }

    private val _isSubmitting = MutableStateFlow(false)
    public val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _mediaBytesCache = MutableStateFlow<Map<String, ByteArray>>(emptyMap())
    public val mediaBytesCache: StateFlow<Map<String, ByteArray>> = _mediaBytesCache.asStateFlow()
    private val mediaFetchMutex = Mutex()
    private val draftStorageMutex = Mutex()
    private val activeMediaFetchUrls = mutableSetOf<String>()

    private val _newArticlePhotoUrl = MutableStateFlow("")
    public val newArticlePhotoUrl: StateFlow<String> = _newArticlePhotoUrl.asStateFlow()

    private val _paymentNcfType = MutableStateFlow("B02")
    public val paymentNcfType: StateFlow<String> = _paymentNcfType.asStateFlow()

    private val _paymentClientRnc = MutableStateFlow("")
    public val paymentClientRnc: StateFlow<String> = _paymentClientRnc.asStateFlow()

    private val _paymentVoucherUrl = MutableStateFlow("")
    public val paymentVoucherUrl: StateFlow<String> = _paymentVoucherUrl.asStateFlow()

    private val _dateAvailabilityWarning = MutableStateFlow<String?>(null)
    public val dateAvailabilityWarning: StateFlow<String?> = _dateAvailabilityWarning.asStateFlow()

    private val _isRealtimeConnected = MutableStateFlow(false)
    public val isRealtimeConnected: StateFlow<Boolean> = _isRealtimeConnected.asStateFlow()

    private val _oauthConfig = MutableStateFlow(OAuthProviderConfig())
    public val oauthConfig: StateFlow<OAuthProviderConfig> = _oauthConfig.asStateFlow()

    private val _isAppLocked = MutableStateFlow(false)
    public val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    private val _appLockError = MutableStateFlow<String?>(null)
    public val appLockError: StateFlow<String?> = _appLockError.asStateFlow()

    private val _pendingCardCheckoutSession = MutableStateFlow<CardSessionResult?>(null)
    public val pendingCardCheckoutSession: StateFlow<CardSessionResult?> = _pendingCardCheckoutSession.asStateFlow()

    private val _analyticsSummary = MutableStateFlow<PaymentsAnalyticsSummary?>(null)
    public val analyticsSummary: StateFlow<PaymentsAnalyticsSummary?> = _analyticsSummary.asStateFlow()

    private val _wifiSyncBlockedNotice = MutableStateFlow<String?>(null)
    public val wifiSyncBlockedNotice: StateFlow<String?> = _wifiSyncBlockedNotice.asStateFlow()

    private val _requiresReauthentication = MutableStateFlow(false)
    public val requiresReauthentication: StateFlow<Boolean> = _requiresReauthentication.asStateFlow()

    @kotlin.concurrent.Volatile
    private var mutatedPreferenceKeys: Set<String> = emptySet()

    init {
        registerRepositorySessionCallbacks()
        observeExternalPaymentReturns()
        observeLifecycleBackgroundEvents()
        observeExternalOAuthReturns()
        observeCapturedVoiceAudio()
        restorePersistedAccountOnStartup()
        observeRepositoryBridges()
        observeRealtimeUpdates()
        loadOAuthConfig()
    }

    private fun observeRepositoryBridges() {
        securityScope.launch {
            repository.isRealtimeConnected.collect { connected ->
                _isRealtimeConnected.value = connected
            }
        }
        securityScope.launch {
            repository.suppliers.collect { domainSuppliers ->
                if (domainSuppliers.isNotEmpty()) {
                    _suppliersList.value = domainSuppliers.map { sup ->
                        SupplierUiModel(
                            id = sup.id,
                            name = sup.name,
                            category = sup.category,
                            phone = sup.phone,
                            ratingLabel = sup.rating.ifBlank { "5.0" },
                            statusLabel = if (sup.notes.isNotBlank()) sup.notes else "Confirmado",
                        )
                    }
                }
            }
        }
        securityScope.launch {
            repository.reviews.collect { domainReviews ->
                if (domainReviews.isNotEmpty()) {
                    _companyReviews.value = domainReviews.map { rev ->
                        val initialChar = rev.authorName.trim().firstOrNull()?.uppercase() ?: "R"
                        CompanyReviewUiModel(
                            id = rev.id,
                            author = rev.authorName,
                            initial = initialChar,
                            rating = rev.rating,
                            comment = rev.comment,
                            dateLabel = rev.dateLabel,
                        )
                    }
                }
            }
        }
        securityScope.launch {
            combine(repository.threadMessages, _selectedChatThreadId) { domainMessages, activeThreadId ->
                domainMessages to activeThreadId
            }.collect { pair ->
                val domainMessages = pair.first
                val activeThreadId = pair.second
                if (activeThreadId.isNotBlank()) {
                    val relevant = domainMessages.filter { msg -> msg.threadId == activeThreadId }
                    _directChatMessages.value = relevant.map { msg ->
                        val isClient = msg.senderRole.equals("client", ignoreCase = true)
                        val isCliente = msg.senderRole.equals("cliente", ignoreCase = true)
                        val isClientSender = if (isClient) true else isCliente
                        DirectChatMessageUiModel(
                            id = msg.id,
                            senderName = msg.senderName,
                            text = msg.body,
                            timeLabel = msg.timestampLabel,
                            isFromClient = isClientSender,
                            attachmentName = msg.attachmentName,
                        )
                    }
                } else if (domainMessages.isEmpty()) {
                    _directChatMessages.value = emptyList()
                }
            }
        }
        securityScope.launch {
            repository.eventTasks.collect { backendTasks ->
                val activeId = activeClientEventOrNull()?.id.orEmpty().ifBlank { _selectedEventId.value }
                if (activeId.isNotBlank()) {
                    val eventSpecific = backendTasks.filter { task -> task.eventId == activeId }
                    if (eventSpecific.isNotEmpty()) {
                        _eventTasksList.value = eventSpecific.map { task ->
                            EventTaskUiModel(
                                id = task.id,
                                title = task.title,
                                badgeText = if (task.done) "Completado" else task.assigneeName.ifBlank { "Pendiente" },
                                isUrgent = false,
                                completed = task.done,
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Clears the pending reauthentication prompt flag after navigating back to login.
     */
    public fun consumeSessionExpiredPrompt() {
        _requiresReauthentication.value = false
    }

    private fun registerRepositorySessionCallbacks() {
        repository.setSessionLifecycleCallbacks(
            onTokensRefreshed = { newAccessToken, newRefreshToken ->
                secureStorage.put(SecureStorage.KEY_ACCESS_TOKEN, newAccessToken)
                if (newRefreshToken.isNotBlank()) {
                    secureStorage.put(SecureStorage.KEY_REFRESH_TOKEN, newRefreshToken)
                }
            },
            onSessionExpired = {
                handleExpiredSessionCleanup()
            },
        )
    }

    private val userScopedStorageKeys: List<String> = listOf(
        SecureStorage.KEY_ACCESS_TOKEN,
        SecureStorage.KEY_REFRESH_TOKEN,
        SecureStorage.KEY_SESSION_METADATA,
        SecureStorage.KEY_PENDING_OAUTH_PKCE,
        SecureStorage.KEY_ACTIVE_VIEW,
        SecureStorage.KEY_USER_ROLE,
        SecureStorage.KEY_DARK_THEME,
        SecureStorage.KEY_AVATAR_KEY,
        SecureStorage.KEY_AVATAR_IMAGE_DATA,
        SecureStorage.KEY_LANGUAGE,
        SecureStorage.KEY_REMINDERS,
        SecureStorage.KEY_PAYMENT_METHOD,
        SecureStorage.KEY_COMPANY_REVIEWS,
        SecureStorage.KEY_DIRECT_CHAT,
        SecureStorage.KEY_EVENT_TASKS,
        SecureStorage.KEY_EVENT_TIMELINE,
        SecureStorage.KEY_EVENT_GUESTS,
        SecureStorage.KEY_SKETCH_TABLES,
        SecureStorage.KEY_SUPPLIERS,
        SecureStorage.KEY_SELECTED_EVENT_ID,
        SecureStorage.KEY_DRAFT_QUANTITIES,
        SecureStorage.KEY_DRAFT_DISCOUNT,
        SecureStorage.KEY_WIZARD_EVENT_DRAFT,
        SecureStorage.KEY_WIZARD_SCHEDULE,
        "rf_sketch_layout_json",
        "rf_pref_push",
        "rf_pref_whatsapp",
        "rf_pref_email_receipts",
        "rf_pref_biometric_lock",
        "rf_pref_auto_sync_wifi",
        "rf_pref_zone",
    )

    private suspend fun wipeUserScopedSecureStorage() {
        userScopedStorageKeys.forEach { key ->
            secureStorage.remove(key)
        }
    }

    private fun resetUserScopedViewModelState() {
        _isExplicitGuestSession.value = true
        _isAppLocked.value = false
        _appLockError.value = null
        _pendingCardCheckoutSession.value = null
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
        _profilePhone.value = ""
        _profileBirthDate.value = ""
        _profileZone.value = ""
        _profileAvatarKey.value = null
        _profileAvatarBytes.value = null
        _mediaBytesCache.value = emptyMap()
        _analyticsSummary.value = null
        _selectedChatThreadId.value = ""
        _selectedEventId.value = ""
        _selectedQuoteId.value = ""
        _selectedClientId.value = ""
        _selectedRefundId.value = ""
        _directChatMessages.value = emptyList()
        _eventTasksList.value = emptyList()
        tasksByEventId.clear()
        sessionCreatedEventIds.clear()
        _eventGuestsList.value = emptyList()
        _eventTimelineItems.value = emptyList()
        _companyReviews.value = emptyList()
        _suppliersList.value = emptyList()
        _wizardScheduleItems.value = emptyList()
        _draftQuantities.value = emptyMap()
        _draftDiscountDop.value = 0
        _sketchTableCount.value = 6
        _sketchLayoutJson.value = ""
        _clientEventTitleInput.value = ""
        _rosaBudgetDop.value = 0
        mutatedPreferenceKeys = emptySet()
    }

    private suspend fun handleExpiredSessionCleanup() {
        wipeUserScopedSecureStorage()
        repository.setRefreshToken(null)
        repository.clearSession()
        resetUserScopedViewModelState()
        _formError.value = "Tu sesión expiró. Inicia sesión nuevamente."
        _requiresReauthentication.value = true
    }

    private fun observeExternalPaymentReturns() {
        securityScope.launch {
            ExternalPaymentCallbackBridge.paymentReturns.collect { paymentReturn ->
                ExternalPaymentCallbackBridge.clearReplayedReturn()
                handleExternalPaymentReturn(paymentReturn)
            }
        }
    }

    private fun observeLifecycleBackgroundEvents() {
        securityScope.launch {
            AppLifecycleBridge.backgroundEvents.collect {
                onAppBackgrounded()
            }
        }
    }

    private fun observeExternalOAuthReturns() {
        securityScope.launch {
            ExternalOAuthCallbackBridge.oauthReturns.collect { outcome ->
                ExternalOAuthCallbackBridge.clearReplayedReturn()
                handleRecoveredOAuthCallback(outcome)
            }
        }
    }

    private suspend fun handleRecoveredOAuthCallback(outcome: OAuthBrowserOutcome) {
        val rawPending = secureStorage.get(SecureStorage.KEY_PENDING_OAUTH_PKCE)?.trim().orEmpty()
        if (rawPending.isEmpty()) return
        secureStorage.remove(SecureStorage.KEY_PENDING_OAUTH_PKCE)
        val parts = rawPending.split("|")
        if (parts.size < 4) return
        val provider = parts[0]
        val codeVerifier = parts[1]
        val expectedState = parts[2]
        val redirectUri = parts[3]
        val code = outcome.code?.trim().orEmpty()
        if (code.isEmpty()) {
            val errorText = translateBrowserOAuthError(provider.replaceFirstChar { ch -> ch.uppercase() }, outcome.errorCode)
            _formError.value = errorText
            return
        }
        val returnedState = outcome.state?.trim().orEmpty()
        if (returnedState.isNotEmpty()) {
            if (returnedState != expectedState) {
                _formError.value = "Validación de estado OAuth inválida."
                return
            }
        }
        val exchangeOutcome = repository.exchangeOAuthCode(
            provider = provider,
            code = code,
            codeVerifier = codeVerifier,
            redirectUri = outcome.redirectUri.ifBlank { redirectUri },
        )
        val account = exchangeOutcome.account
        val isVerified = if (exchangeOutcome.success) account != null else false
        if (isVerified) {
            val authenticatedAccount = account ?: return
            applyAuthenticatedAccount(authenticatedAccount, exchangeOutcome.accessToken, exchangeOutcome.refreshToken)
            soundPlayer.playPaymentSuccess()
            _formError.value = null
            _formFeedback.value = "Sesión iniciada con ${provider.replaceFirstChar { ch -> ch.uppercase() }}."
        } else {
            soundPlayer.playAlertError()
            _formError.value = exchangeOutcome.errorMessage ?: "No se pudo verificar tu cuenta OAuth."
        }
    }

    override fun onCleared() {
        securityScope.cancel()
        super.onCleared()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeRealtimeUpdates() {
        securityScope.launch {
            combine(currentSessionJwt, _autoSyncWifiOnly) { token, wifiOnly ->
                token to wifiOnly
            }
                .flatMapLatest { pair ->
                    val token = pair.first?.trim().orEmpty()
                    val isLocalToken = token.startsWith("rf-local.")
                    val isUsableServerToken = if (token.isNotEmpty()) !isLocalToken else false
                    if (!isUsableServerToken) {
                        _isRealtimeConnected.value = false
                        emptyFlow()
                    } else if (!isWifiSyncPermitted()) {
                        _wifiSyncBlockedNotice.value = "Sincronización en vivo pausada: solo por Wi‑Fi activado."
                        _isRealtimeConnected.value = false
                        emptyFlow()
                    } else {
                        _wifiSyncBlockedNotice.value = null
                        repository.observeRealtimeEvents()
                    }
                }
                .conflate()
                .collect { streamEvent ->
                    handleRealtimeStreamEvent(streamEvent)
                }
        }
    }

    internal suspend fun handleRealtimeStreamEvent(streamEvent: RealtimeStreamEvent) {
        if (!isWifiSyncPermitted()) {
            _wifiSyncBlockedNotice.value = "Sincronización en segundo plano pausada: solo por Wi‑Fi activado."
            return
        }
        _wifiSyncBlockedNotice.value = null
        repository.syncFromBackendAsync()
        val isReminderType = streamEvent.type.contains("reminder", ignoreCase = true)
        val isReminderNotification = isReminderNotificationCategory(streamEvent)
        val isReminderEvent = if (isReminderType) true else isReminderNotification
        val readableNotice = resolveReadableRealtimeNotice(streamEvent)
        val canNotifyReminder = if (isReminderEvent) _remindersEnabled.value else true
        val hasReadableNotice = if (readableNotice.isNotEmpty()) canNotifyReminder else false
        if (hasReadableNotice) {
            _formFeedback.value = readableNotice
            if (_pushNotificationsEnabled.value) {
                platformActions.showLocalNotification("RosaFiesta", readableNotice)
            }
        }
    }

    private fun isReminderNotificationCategory(streamEvent: RealtimeStreamEvent): Boolean {
        if (streamEvent.type != "notification.created") return false
        val normalized = streamEvent.category.trim().lowercase()
        return when (normalized) {
            "reminder", "reminders", "recordatorio", "recordatorios" -> true
            else -> false
        }
    }

    private fun resolveReadableRealtimeNotice(streamEvent: RealtimeStreamEvent): String {
        val role = activeRole.value.trim().lowercase()
        val isAdminView = when (role) {
            "admin", "staff", "manager", "supervisor" -> true
            else -> false
        }
        return when (streamEvent.type) {
            "payment.verified" -> "Pago verificado en tiempo real."

            "payment.rejected" -> "Transferencia marcada como rechazada."

            "payment.pending_verification" -> {
                if (isAdminView) streamEvent.summary.ifBlank { "Nuevo comprobante de pago pendiente de revisión." } else ""
            }

            "quote.created" -> {
                if (isAdminView) streamEvent.summary.ifBlank { "Nueva cotización solicitada por un cliente." } else ""
            }

            "quote.sent" -> {
                if (isAdminView) "" else streamEvent.summary.ifBlank { "Tienes una nueva cotización lista para revisar." }
            }

            "quote.declined" -> {
                if (isAdminView) streamEvent.summary.ifBlank { "El cliente solicitó cambios en la cotización." } else ""
            }

            "quote.signed" -> "Cotización firmada y reservación actualizada."

            "chat.message", "whatsapp.message" -> streamEvent.summary.ifBlank { "Nuevo mensaje sincronizado en tiempo real." }

            "logistics.updated" -> streamEvent.summary.ifBlank { "Checklist o logística del evento actualizada." }

            "damage.reported" -> {
                if (isAdminView) streamEvent.summary.ifBlank { "Nueva incidencia de daño reportada." } else ""
            }

            "notification.created" -> streamEvent.summary.ifBlank { "Tienes una nueva notificación." }

            "event.reminder", "payment.reminder" -> {
                if (_remindersEnabled.value) "Recordatorio de evento o pago pendiente." else ""
            }

            else -> ""
        }
    }

    private suspend fun restoreBooleanPrefIfUntouched(
        storageKey: String,
        applyValue: (Boolean) -> Unit,
    ) {
        if (storageKey in mutatedPreferenceKeys) return
        val savedValue = secureStorage.get(storageKey) ?: return
        if (storageKey !in mutatedPreferenceKeys) {
            applyValue(savedValue == "true")
        }
    }

    private fun resolveUntouchedBool(
        storageKey: String,
        savedValue: String?,
        fallback: Boolean,
    ): Boolean {
        if (storageKey in mutatedPreferenceKeys) return fallback
        return if (savedValue != null) savedValue == "true" else fallback
    }

    private fun resolveUntouchedVolume(
        savedVolume: Float?,
        fallback: Float,
    ): Float {
        if (SecureStorage.KEY_AUDIO_VOLUME in mutatedPreferenceKeys) return fallback
        return savedVolume?.coerceIn(0f, 1f) ?: fallback
    }

    private suspend fun restoreAudioConfigIfUntouched() {
        val savedEnabled = secureStorage.get(SecureStorage.KEY_AUDIO_ENABLED)
        val savedVolume = secureStorage.get(SecureStorage.KEY_AUDIO_VOLUME)?.toFloatOrNull()
        val savedInteraction = secureStorage.get(SecureStorage.KEY_AUDIO_INTERACTION)
        val savedRosa = secureStorage.get(SecureStorage.KEY_AUDIO_ROSA)
        val savedAlerts = secureStorage.get(SecureStorage.KEY_AUDIO_ALERTS)
        val current = _audioConfig.value
        val nextEnabled = resolveUntouchedBool(SecureStorage.KEY_AUDIO_ENABLED, savedEnabled, current.soundEffectsEnabled)
        val nextVolume = resolveUntouchedVolume(savedVolume, current.masterVolume)
        val nextInteraction = resolveUntouchedBool(SecureStorage.KEY_AUDIO_INTERACTION, savedInteraction, current.interactionSoundsEnabled)
        val nextRosa = resolveUntouchedBool(SecureStorage.KEY_AUDIO_ROSA, savedRosa, current.rosaAssistantSoundsEnabled)
        val nextAlerts = resolveUntouchedBool(SecureStorage.KEY_AUDIO_ALERTS, savedAlerts, current.paymentAlertSoundsEnabled)
        val restored = current.copy(
            soundEffectsEnabled = nextEnabled,
            masterVolume = nextVolume,
            interactionSoundsEnabled = nextInteraction,
            rosaAssistantSoundsEnabled = nextRosa,
            paymentAlertSoundsEnabled = nextAlerts,
        )
        if (restored != current) {
            applyAudioConfig(restored, persist = false)
        }
    }

    private fun restorePersistedAccountOnStartup() {
        val currentRepoUser = repository.currentAuthenticatedUser.value
        if (currentRepoUser != null) {
            applyProfileFieldsFromAccount(
                account = currentRepoUser,
                persistPreferencesToStorage = false,
            )
        }
        val settings = repository.businessSettings.value
        _depositPercentInput.value = settings.depositPercent.toString()
        val firstZoneFee = settings.zones.firstOrNull()?.feeDop ?: 350
        _deliveryBaseInput.value = firstZoneFee.toString()

        securityScope.launch {
            restorePersistedDraftAndEventState()
        }

        securityScope.launch {
            val savedRefresh = secureStorage.get(SecureStorage.KEY_REFRESH_TOKEN)?.trim().orEmpty()
            if (savedRefresh.isNotEmpty()) {
                repository.setRefreshToken(savedRefresh)
            }
            val savedToken = secureStorage.get(SecureStorage.KEY_ACCESS_TOKEN)
            var sessionRestored = false
            if (!savedToken.isNullOrBlank()) {
                val restoredAccount = repository.restoreSessionFromJwt(savedToken)
                if (restoredAccount != null) {
                    applyProfileFieldsFromAccount(
                        account = restoredAccount,
                        persistPreferencesToStorage = false,
                    )
                    sessionRestored = true
                } else {
                    secureStorage.remove(SecureStorage.KEY_ACCESS_TOKEN)
                }
            }
            val canAttemptRefresh = if (!sessionRestored) savedRefresh.isNotEmpty() else false
            if (canAttemptRefresh) {
                val refreshOutcome = repository.refreshSession(savedRefresh)
                val refreshedAccount = refreshOutcome.account
                val refreshSucceeded = if (refreshOutcome.success) refreshedAccount != null else false
                if (refreshSucceeded) {
                    val account = refreshedAccount ?: return@launch
                    applyAuthenticatedAccount(account, refreshOutcome.accessToken, refreshOutcome.refreshToken)
                } else {
                    val isRejectedCode = refreshOutcome.errorCode in setOf(
                        "AUTH_SESSION_EXPIRED",
                        "AUTH_UNAUTHORIZED",
                        "HTTP_401",
                        "HTTP_403",
                    )
                    if (isRejectedCode) {
                        handleExpiredSessionCleanup()
                    }
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
            val savedRememberChoice = secureStorage.get(SecureStorage.KEY_REMEMBER_ENTRY_CHOICE)
            if (savedRememberChoice != null) {
                _rememberEntryChoice.value = savedRememberChoice == "true"
            }
            val savedMethod = secureStorage.get(SecureStorage.KEY_PAYMENT_METHOD)
            if (!savedMethod.isNullOrBlank()) {
                _savedPaymentMethod.value = savedMethod
            }
            val savedTables = secureStorage.get(SecureStorage.KEY_SKETCH_TABLES)?.toIntOrNull()
            if (savedTables != null) {
                _sketchTableCount.value = savedTables.coerceIn(2, 12)
            }
            restoreBooleanPrefIfUntouched("rf_pref_push") { enabled -> _pushNotificationsEnabled.value = enabled }
            restoreBooleanPrefIfUntouched("rf_pref_whatsapp") { enabled -> _whatsappAlertsEnabled.value = enabled }
            restoreBooleanPrefIfUntouched("rf_pref_email_receipts") { enabled -> _emailReceiptsEnabled.value = enabled }
            restoreBooleanPrefIfUntouched("rf_pref_biometric_lock") { enabled -> _biometricLockEnabled.value = enabled }
            restoreBooleanPrefIfUntouched("rf_pref_auto_sync_wifi") { enabled -> _autoSyncWifiOnly.value = enabled }
            val hasSession = hasValidAuthenticatedSession()
            val shouldLock = if (_biometricLockEnabled.value) hasSession else false
            if (shouldLock) {
                _isAppLocked.value = true
            }
            restoreAudioConfigIfUntouched()
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
        if (isWifiSyncPermitted()) {
            _wifiSyncBlockedNotice.value = null
            repository.refreshSharedState()
        } else {
            _wifiSyncBlockedNotice.value = "Sincronización pausada: solo por Wi‑Fi activado."
        }
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

    private suspend fun restorePersistedDraftAndEventState() {
        val isCartMutated = SecureStorage.KEY_DRAFT_QUANTITIES in mutatedPreferenceKeys
        if (!isCartMutated) {
            val savedDraftRaw = secureStorage.get(SecureStorage.KEY_DRAFT_QUANTITIES).orEmpty()
            val parsedCart = parseDraftQuantitiesFromStorage(savedDraftRaw)
            if (parsedCart.isNotEmpty()) {
                _draftQuantities.value = parsedCart
            }
            val savedDiscount = secureStorage.get(SecureStorage.KEY_DRAFT_DISCOUNT)?.toIntOrNull()
            if (savedDiscount != null) {
                _draftDiscountDop.value = savedDiscount.coerceAtLeast(0)
            }
        }
        val isWizardMutated = SecureStorage.KEY_WIZARD_EVENT_DRAFT in mutatedPreferenceKeys
        if (!isWizardMutated) {
            val savedWizardRaw = secureStorage.get(SecureStorage.KEY_WIZARD_EVENT_DRAFT).orEmpty()
            applyWizardDraftFromStorage(savedWizardRaw)
        }
        val isScheduleMutated = SecureStorage.KEY_WIZARD_SCHEDULE in mutatedPreferenceKeys
        if (!isScheduleMutated) {
            val savedScheduleRaw = secureStorage.get(SecureStorage.KEY_WIZARD_SCHEDULE).orEmpty()
            val parsedSchedule = parseWizardScheduleFromStorage(savedScheduleRaw)
            if (parsedSchedule.isNotEmpty()) {
                _wizardScheduleItems.value = parsedSchedule
            }
        }
        val isGuestsMutated = SecureStorage.KEY_EVENT_GUESTS in mutatedPreferenceKeys
        if (!isGuestsMutated) {
            val savedGuestsRaw = secureStorage.get(SecureStorage.KEY_EVENT_GUESTS).orEmpty()
            val parsedGuests = parseWizardGuestsFromStorage(savedGuestsRaw)
            if (parsedGuests.isNotEmpty()) {
                _eventGuestsList.value = parsedGuests
            }
        }
        val isTasksMutated = SecureStorage.KEY_EVENT_TASKS in mutatedPreferenceKeys
        if (!isTasksMutated) {
            val savedTasksRaw = secureStorage.get(SecureStorage.KEY_EVENT_TASKS).orEmpty()
            val parsedTasksMap = parseTasksByEventFromStorage(savedTasksRaw)
            tasksByEventId.clear()
            tasksByEventId.putAll(parsedTasksMap)
        }
        val isSelectionMutated = SecureStorage.KEY_SELECTED_EVENT_ID in mutatedPreferenceKeys
        if (!isSelectionMutated) {
            val savedEventId = secureStorage.get(SecureStorage.KEY_SELECTED_EVENT_ID).orEmpty().trim()
            if (savedEventId.isNotEmpty()) {
                val existsInEvents = events.value.any { eventItem -> eventItem.id == savedEventId }
                if (existsInEvents) {
                    _selectedEventId.value = savedEventId
                }
            }
        }
        syncTasksForSelectedEvent()
    }

    private fun persistDraftCartToStorage() {
        mutatedPreferenceKeys = mutatedPreferenceKeys + SecureStorage.KEY_DRAFT_QUANTITIES
        securityScope.launch {
            draftStorageMutex.withLock {
                val snapshot = _draftQuantities.value
                val discountSnapshot = _draftDiscountDop.value
                val serializedCart = snapshot.entries
                    .filter { entry -> entry.value > 0 }
                    .joinToString("||") { entry -> "${entry.key.replace("::", "")}::${entry.value}" }
                if (serializedCart.isEmpty()) {
                    secureStorage.remove(SecureStorage.KEY_DRAFT_QUANTITIES)
                } else {
                    secureStorage.put(SecureStorage.KEY_DRAFT_QUANTITIES, serializedCart)
                }
                secureStorage.put(SecureStorage.KEY_DRAFT_DISCOUNT, discountSnapshot.toString())
            }
        }
    }

    private fun parseDraftQuantitiesFromStorage(serialized: String): Map<String, Int> {
        if (serialized.isBlank()) return emptyMap()
        val restored = mutableMapOf<String, Int>()
        serialized.split("||").forEach { entry ->
            val parts = entry.split("::")
            if (parts.size == 2) {
                val itemId = parts[0].trim()
                val quantity = parts[1].toIntOrNull() ?: 0
                val isValidEntry = if (itemId.isNotEmpty()) quantity > 0 else false
                if (isValidEntry) {
                    restored[itemId] = quantity
                }
            }
        }
        return restored
    }

    private fun persistWizardDraftToStorage() {
        mutatedPreferenceKeys = mutatedPreferenceKeys + SecureStorage.KEY_WIZARD_EVENT_DRAFT
        securityScope.launch {
            draftStorageMutex.withLock {
                val encodedFields = listOf(
                    _eventTypeInput.value,
                    _rosaEventType.value,
                    _clientEventTitleInput.value,
                    _eventDateInput.value,
                    _eventVenueInput.value,
                    _eventGuestsInput.value,
                    _rosaBudgetDop.value.toString(),
                    _eventNotesInput.value,
                ).joinToString("||") { rawField ->
                    rawField.replace("||", " ").replace("::", " ")
                }
                secureStorage.put(SecureStorage.KEY_WIZARD_EVENT_DRAFT, encodedFields)
            }
        }
    }

    private fun applyWizardDraftFromStorage(serialized: String) {
        if (serialized.isBlank()) return
        val parts = serialized.split("||")
        if (parts.size < 8) return
        if (parts[0].isNotBlank()) _eventTypeInput.value = parts[0]
        if (parts[1].isNotBlank()) _rosaEventType.value = parts[1]
        if (parts[2].isNotBlank()) _clientEventTitleInput.value = parts[2]
        if (parts[3].isNotBlank()) _eventDateInput.value = parts[3]
        if (parts[4].isNotBlank()) _eventVenueInput.value = parts[4]
        if (parts[5].isNotBlank()) _eventGuestsInput.value = parts[5]
        val restoredBudget = parts[6].toIntOrNull()
        if (restoredBudget != null) {
            if (restoredBudget > 0) _rosaBudgetDop.value = restoredBudget
        }
        if (parts[7].isNotBlank()) _eventNotesInput.value = parts[7]
    }

    private fun persistWizardScheduleToStorage() {
        mutatedPreferenceKeys = mutatedPreferenceKeys + SecureStorage.KEY_WIZARD_SCHEDULE
        securityScope.launch {
            draftStorageMutex.withLock {
                val serialized = _wizardScheduleItems.value.joinToString("||") { block ->
                    val safeTitle = block.title.replace("::", " ").replace("||", " ")
                    "${block.id}::${block.startTime}::${block.endTime}::$safeTitle"
                }
                if (serialized.isEmpty()) {
                    secureStorage.remove(SecureStorage.KEY_WIZARD_SCHEDULE)
                } else {
                    secureStorage.put(SecureStorage.KEY_WIZARD_SCHEDULE, serialized)
                }
            }
        }
    }

    private fun parseWizardScheduleFromStorage(serialized: String): List<EventTimelineItemUiModel> {
        if (serialized.isBlank()) return emptyList()
        return serialized.split("||").mapNotNull { entry ->
            val parts = entry.split("::")
            if (parts.size >= 4) {
                EventTimelineItemUiModel(
                    id = parts[0],
                    startTime = parts[1],
                    endTime = parts[2],
                    title = parts[3],
                    subtitle = "Definido por ti en la solicitud",
                    dotColorHex = 0xFF865DEC,
                    ringColorHex = 0xFFEFE8FD,
                )
            } else {
                null
            }
        }
    }

    private fun persistWizardGuestsToStorage() {
        mutatedPreferenceKeys = mutatedPreferenceKeys + SecureStorage.KEY_EVENT_GUESTS
        val serialized = _eventGuestsList.value.joinToString("||") { guest ->
            val safeName = guest.name.replace("::", " ").replace("||", " ")
            val safeDetails = guest.details.replace("::", " ").replace("||", " ")
            "${guest.id}::$safeName::$safeDetails::${guest.status}"
        }
        securityScope.launch {
            secureStorage.put(SecureStorage.KEY_EVENT_GUESTS, serialized)
        }
    }

    private fun parseWizardGuestsFromStorage(serialized: String): List<EventGuestUiModel> {
        if (serialized.isBlank()) return emptyList()
        return serialized.split("||").mapNotNull { entry ->
            val parts = entry.split("::")
            if (parts.size >= 4) {
                EventGuestUiModel(
                    id = parts[0],
                    name = parts[1],
                    details = parts[2],
                    status = parts[3],
                )
            } else {
                null
            }
        }
    }

    private fun persistEventTasksToStorage() {
        mutatedPreferenceKeys = mutatedPreferenceKeys + SecureStorage.KEY_EVENT_TASKS
        val serialized = tasksByEventId.entries.joinToString("@@") { eventEntry ->
            val encodedTasks = eventEntry.value.joinToString("||") { taskItem ->
                val safeTitle = taskItem.title.replace("::", " ").replace("||", " ").replace("@@", " ")
                val safeBadge = taskItem.badgeText.replace("::", " ").replace("||", " ").replace("@@", " ")
                "${taskItem.id}::$safeTitle::$safeBadge::${taskItem.isUrgent}::${taskItem.completed}"
            }
            "${eventEntry.key}==$encodedTasks"
        }
        securityScope.launch {
            secureStorage.put(SecureStorage.KEY_EVENT_TASKS, serialized)
        }
    }

    private fun parseTasksByEventFromStorage(serialized: String): Map<String, List<EventTaskUiModel>> {
        if (serialized.isBlank()) return emptyMap()
        val parsed = mutableMapOf<String, List<EventTaskUiModel>>()
        serialized.split("@@").forEach { eventBlock ->
            val splitEvent = eventBlock.split("==", limit = 2)
            if (splitEvent.size == 2) {
                val eventId = splitEvent[0].trim()
                val rawTasks = splitEvent[1]
                if (eventId.isNotEmpty()) {
                    val items = if (rawTasks.isBlank()) {
                        emptyList()
                    } else {
                        rawTasks.split("||").mapNotNull { taskEntry ->
                            val fields = taskEntry.split("::")
                            if (fields.size >= 5) {
                                EventTaskUiModel(
                                    id = fields[0],
                                    title = fields[1],
                                    badgeText = fields[2],
                                    isUrgent = fields[3] == "true",
                                    completed = fields[4] == "true",
                                )
                            } else {
                                null
                            }
                        }
                    }
                    parsed[eventId] = items
                }
            }
        }
        return parsed
    }

    private fun syncTasksForSelectedEvent() {
        val activeId = activeClientEventOrNull()?.id.orEmpty().ifBlank { _selectedEventId.value }
        _eventTasksList.value = tasksByEventId[activeId].orEmpty()
    }

    public fun isWifiSyncPermitted(): Boolean {
        val wifiOnly = _autoSyncWifiOnly.value
        if (!wifiOnly) return true
        return platformActions.isOnWifiNetwork()
    }

    public fun isOnWifiConnection(): Boolean = platformActions.isOnWifiNetwork()

    public fun syncWithServer(onComplete: (Boolean) -> Unit = {}) {
        if (!isWifiSyncPermitted()) {
            val notice = "Sincronización pausada: conéctate a una red Wi‑Fi o desactiva 'Sincronizar solo con Wi‑Fi'."
            _wifiSyncBlockedNotice.value = notice
            _formFeedback.value = notice
            onComplete(false)
            return
        }
        _wifiSyncBlockedNotice.value = null
        securityScope.launch {
            val synced = repository.syncFromBackendAsync()
            if (synced) {
                repository.currentAuthenticatedUser.value?.let { syncedAccount ->
                    applyPreferencesFromAccount(syncedAccount)
                }
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

    private fun applyPreferencesFromAccount(
        account: RegisteredUserAccount,
        persistToStorage: Boolean = true,
    ) {
        val prefs = account.preferences
        if ("rf_pref_push" !in mutatedPreferenceKeys) {
            _pushNotificationsEnabled.value = prefs.pushNotificationsEnabled
        }
        if ("rf_pref_whatsapp" !in mutatedPreferenceKeys) {
            _whatsappAlertsEnabled.value = prefs.whatsappAlertsEnabled
        }
        if ("rf_pref_email_receipts" !in mutatedPreferenceKeys) {
            _emailReceiptsEnabled.value = prefs.emailReceiptsEnabled
        }
        if ("rf_pref_biometric_lock" !in mutatedPreferenceKeys) {
            _biometricLockEnabled.value = prefs.biometricLockEnabled
        }
        if ("rf_pref_auto_sync_wifi" !in mutatedPreferenceKeys) {
            _autoSyncWifiOnly.value = prefs.autoSyncWifiOnly
        }
        if (SecureStorage.KEY_REMINDERS !in mutatedPreferenceKeys) {
            _remindersEnabled.value = prefs.eventRemindersEnabled
        }
        if (SecureStorage.KEY_LANGUAGE !in mutatedPreferenceKeys) {
            val displayLang = if (prefs.preferredLanguage.equals("en", ignoreCase = true)) "English" else "Español"
            _preferredLanguage.value = displayLang
        }
        if (SecureStorage.KEY_DARK_THEME !in mutatedPreferenceKeys) {
            _isDarkTheme.value = prefs.darkThemeEnabled
            com.jeudry.rosafiesta.designsystem.RfThemeState.isPersonalDark = prefs.darkThemeEnabled
        }
        if (SecureStorage.KEY_PAYMENT_METHOD !in mutatedPreferenceKeys) {
            if (prefs.savedPaymentMethod.isNotBlank()) {
                _savedPaymentMethod.value = prefs.savedPaymentMethod
            }
        }
        if ("rf_pref_zone" !in mutatedPreferenceKeys) {
            if (prefs.profileZone.isNotBlank()) {
                _profileZone.value = prefs.profileZone
            }
        }
        if (persistToStorage) {
            securityScope.launch {
                val displayLang = if (prefs.preferredLanguage.equals("en", ignoreCase = true)) "English" else "Español"
                secureStorage.put("rf_pref_push", prefs.pushNotificationsEnabled.toString())
                secureStorage.put("rf_pref_whatsapp", prefs.whatsappAlertsEnabled.toString())
                secureStorage.put("rf_pref_email_receipts", prefs.emailReceiptsEnabled.toString())
                secureStorage.put("rf_pref_biometric_lock", prefs.biometricLockEnabled.toString())
                secureStorage.put("rf_pref_auto_sync_wifi", prefs.autoSyncWifiOnly.toString())
                secureStorage.put(SecureStorage.KEY_REMINDERS, prefs.eventRemindersEnabled.toString())
                secureStorage.put(SecureStorage.KEY_LANGUAGE, displayLang)
                secureStorage.put(SecureStorage.KEY_DARK_THEME, prefs.darkThemeEnabled.toString())
                secureStorage.put(SecureStorage.KEY_PAYMENT_METHOD, prefs.savedPaymentMethod)
                secureStorage.put("rf_pref_zone", prefs.profileZone)
            }
        }
    }

    private fun applyProfileFieldsFromAccount(
        account: RegisteredUserAccount,
        persistPreferencesToStorage: Boolean = true,
    ) {
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
        if (account.avatarKey.isNotBlank()) {
            _profileAvatarKey.value = account.avatarKey
            val isRelativeMedia = account.avatarKey.startsWith("/")
            val isHttpMedia = account.avatarKey.startsWith("http", ignoreCase = true)
            val isRemoteMedia = if (isRelativeMedia) true else isHttpMedia
            if (isRemoteMedia) {
                ensureMediaLoaded(account.avatarKey)
            }
        }
        applyPreferencesFromAccount(
            account = account,
            persistToStorage = persistPreferencesToStorage,
        )
    }

    public fun enterGuestMode() {
        soundPlayer.playTap()
        _isExplicitGuestSession.value = true
        _isAppLocked.value = false
        _appLockError.value = null
        _pendingCardCheckoutSession.value = null
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
            val isUnlimitedBudget = maxPrice >= 5000
            val priceOk = if (isUnlimitedBudget) true else article.pricePerDayDop <= maxPrice
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
        if (articleCat.contains(normalizedSelected)) return true

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
        mutatedPreferenceKeys = mutatedPreferenceKeys + SecureStorage.KEY_SELECTED_EVENT_ID
        _selectedEventId.value = eventId
        _eventTasksList.value = tasksByEventId[eventId].orEmpty()
        securityScope.launch {
            secureStorage.put(SecureStorage.KEY_SELECTED_EVENT_ID, eventId)
        }
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
                _newArticlePhotoUrl.value = found.photoUrl
                if (found.photoUrl.isNotBlank()) {
                    ensureMediaLoaded(found.photoUrl)
                }
            }
        } else {
            val nextNumber = articles.value.size + 1
            val paddedCode = nextNumber.toString().padStart(3, '0')
            _newArticleCode.value = "RF-$paddedCode"
            _newArticleName.value = ""
            _newArticleCategory.value = catalogCategories.value.firstOrNull()?.name.orEmpty()
            _newArticlePrice.value = ""
            _newArticleStock.value = ""
            _newArticlePhotoUrl.value = ""
        }
    }

    public fun startEditingPackage(packageId: String?) {
        _editingPackageId.value = packageId
    }

    /**
     * Clears manual event creation draft fields when starting a brand-new event from scratch.
     */
    public fun startNewManualEvent() {
        _newEventTitle.value = ""
        _newEventClient.value = ""
        _newEventDate.value = ""
        _newEventVenue.value = ""
        _newEventGuests.value = ""
        _draftQuantities.value = emptyMap()
        _draftDiscountDop.value = 0
        _formError.value = null
        persistDraftCartToStorage()
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
        persistDraftCartToStorage()
    }

    public fun adjustDraftQuantity(
        articleId: String,
        delta: Int,
        showPreview: Boolean = true,
    ) {
        soundPlayer.playCartAdd()
        val previous = _draftQuantities.value[articleId] ?: 0
        val next = (previous + delta).coerceAtLeast(0)
        if (delta > 0) {
            _lastAddedDraftItem.value = articleId to delta
        }
        _draftQuantities.update { current ->
            current + (articleId to next)
        }
        persistDraftCartToStorage()
        val shouldShowPreview = if (delta > 0) showPreview else false
        if (shouldShowPreview) {
            showSavedPreviewForItemId(articleId, next)
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
        persistDraftCartToStorage()
    }

    public fun clearDraftCart() {
        _draftQuantities.value = emptyMap()
        _draftDiscountDop.value = 0
        persistDraftCartToStorage()
    }

    public fun applyPackageToDraftCart(
        packageItem: EventPackage,
        showPreview: Boolean = true,
    ) {
        soundPlayer.playCartAdd()
        val resolvedMap = mutableMapOf<String, Int>()
        for (lineDescription in packageItem.includedItems) {
            val quantityPrefix = lineDescription.trim().takeWhile { character -> character.isDigit() }.toIntOrNull() ?: 1
            val matchedArticle = findBestArticleMatch(articles.value, lineDescription)
            if (matchedArticle != null) {
                resolvedMap[matchedArticle.id] = quantityPrefix
            }
        }
        if (resolvedMap.isNotEmpty()) {
            _draftQuantities.value = resolvedMap
        }
        _draftDiscountDop.value = packageItem.savingsDop.coerceAtLeast(0)
        _selectedPackageId.value = packageItem.id
        persistDraftCartToStorage()
        if (showPreview) {
            showSavedPackagePreview(packageItem)
        }
    }

    public fun undoLastDraftAddition(): Boolean {
        val lastAdded = _lastAddedDraftItem.value ?: return false
        val currentQty = _draftQuantities.value[lastAdded.first] ?: 0
        val revertedQty = (currentQty - lastAdded.second).coerceAtLeast(0)
        _draftQuantities.update { current ->
            current + (lastAdded.first to revertedQty)
        }
        persistDraftCartToStorage()
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
                val chosenColor = _selectedArticleVariantColors.value[itemId]?.trim().orEmpty()
                val formattedName = if (chosenColor.isNotEmpty()) "${catalogItem.name} ($chosenColor)" else catalogItem.name
                result += EventLineItem(
                    articleCode = catalogItem.code,
                    name = formattedName,
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
            }
        }
        return result
    }

    public fun syncDraftToOfficialQuote(contactPhone: String = ""): QuoteRecord? {
        val lines = draftLineItems()
        if (lines.isEmpty()) {
            postError("Agrega al menos un artículo al carrito para generar la cotización oficial.")
            return null
        }
        val deliveryFee = _deliveryBaseInput.value.toIntOrNull() ?: 350
        val discount = _draftDiscountDop.value
        val clientLabel = _profileName.value.trim().ifBlank { "Cliente Rosa Fiesta" }
        val resolvedPhone = contactPhone.trim().ifBlank { _profilePhone.value.trim() }
        val resolvedTitle = _clientEventTitleInput.value.trim()
            .ifBlank { _eventTypeInput.value.trim() }
            .ifBlank { _rosaEventType.value.trim() }
            .ifBlank { "Evento Rosa Fiesta" }
        val resolvedGuests = (_eventGuestsInput.value.trim().toIntOrNull() ?: 0).coerceAtLeast(0)
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
            notes = _eventNotesInput.value.trim(),
            contactPhone = resolvedPhone,
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
        _rosaEventType.value = eventType.trim().ifEmpty { "Evento" }
        _rosaGuestsCount.value = guests.coerceIn(10, 500)
        _rosaBudgetDop.value = budgetDop.coerceAtLeast(0)
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
            if (reply.intent == "ERROR") {
                _formError.value = reply.reply
                soundPlayer.playAlertError()
            } else {
                _formError.value = null
            }
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

    private fun observeCapturedVoiceAudio() {
        securityScope.launch {
            voiceInputMonitor.capturedWavBytes.collect { capturedBytes ->
                triggerRemoteTranscriptionIfNeeded(capturedBytes)
            }
        }
    }

    public fun dismissVoiceError() {
        remoteVoiceErrorState.value = null
    }

    public fun startVoiceInput(origin: VoiceInputOrigin, initialText: String = "") {
        soundPlayer.playTap()
        activeTranscriptionJob?.cancel()
        activeTranscriptionJob = null
        _isRemoteVoiceTranscribing.value = false
        remoteVoiceErrorState.value = null
        voiceCaptureSessionId += 1L
        hasNativeTranscriptForSession = false
        _voiceInputOrigin.value = origin
        val cleanInitial = initialText.trim()
        when (origin) {
            VoiceInputOrigin.NOTES_INLINE -> {
                inlineNotesBaseText = cleanInitial
                _voiceTranscript.value = ""
            }

            VoiceInputOrigin.SEARCH -> {
                _voiceTranscript.value = cleanInitial.ifEmpty { _searchQuery.value.trim() }
            }

            VoiceInputOrigin.ROSA_WIZARD, VoiceInputOrigin.NONE -> {
                _voiceTranscript.value = cleanInitial
            }
        }
        voiceInputMonitor.startListening { recognizedText ->
            val cleanRecognized = recognizedText.trim()
            if (cleanRecognized.isNotEmpty()) {
                hasNativeTranscriptForSession = true
                remoteVoiceErrorState.value = null
                applyRecognizedVoiceTranscript(cleanRecognized)
            }
        }
    }

    private fun applyRecognizedVoiceTranscript(cleanTranscript: String) {
        _voiceTranscript.value = cleanTranscript
        when (_voiceInputOrigin.value) {
            VoiceInputOrigin.SEARCH -> {
                _searchQuery.value = cleanTranscript
            }

            VoiceInputOrigin.NOTES_INLINE -> {
                val combinedNotes = if (inlineNotesBaseText.isBlank()) {
                    cleanTranscript
                } else {
                    "$inlineNotesBaseText\n$cleanTranscript"
                }
                _eventNotesInput.value = combinedNotes.take(InputValidator.MAX_NOTES_LENGTH)
            }

            VoiceInputOrigin.ROSA_WIZARD, VoiceInputOrigin.NONE -> Unit
        }
    }

    public fun stopVoiceInput() {
        soundPlayer.playTap()
        voiceInputMonitor.stopListening()
        triggerRemoteTranscriptionIfNeeded(voiceInputMonitor.capturedWavBytes.value)
    }

    private fun shouldTranscribeCapturedAudio(wavBytes: ByteArray?): Boolean {
        val wavSize = wavBytes?.size ?: 0
        val hasAudioPayload = wavSize > WAV_HEADER_MIN_BYTES
        val hasActiveOrigin = _voiceInputOrigin.value != VoiceInputOrigin.NONE
        val awaitingRemoteStt = !hasNativeTranscriptForSession
        val sessionNotTranscribedYet = transcribedSessionId != voiceCaptureSessionId
        return listOf(hasAudioPayload, hasActiveOrigin, awaitingRemoteStt, sessionNotTranscribedYet).all { ready -> ready }
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun triggerRemoteTranscriptionIfNeeded(wavBytes: ByteArray?) {
        if (!shouldTranscribeCapturedAudio(wavBytes)) return
        val nonNullBytes = wavBytes ?: return
        val sessionId = voiceCaptureSessionId
        transcribedSessionId = sessionId
        _isRemoteVoiceTranscribing.value = true
        remoteVoiceErrorState.value = null
        activeTranscriptionJob?.cancel()
        activeTranscriptionJob = securityScope.launch {
            transcribeCapturedVoiceBytes(nonNullBytes, sessionId)
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    private suspend fun transcribeCapturedVoiceBytes(
        wavBytes: ByteArray,
        sessionId: Long,
    ) {
        try {
            val wavBase64 = Base64.encode(wavBytes)
            val outcome = repository.transcribeVoicePrompt(wavBase64, locale = "es-DO")
            if (sessionId != voiceCaptureSessionId) return
            val cleanTranscript = outcome.transcript.trim()
            if (cleanTranscript.isNotEmpty()) {
                remoteVoiceErrorState.value = null
                _formError.value = null
                applyRecognizedVoiceTranscript(cleanTranscript)
                return
            }
            val errorDetail = extractVoiceTranscriptionError(outcome.provider)
            remoteVoiceErrorState.value = errorDetail
            _formError.value = errorDetail
            soundPlayer.playAlertError()
        } finally {
            if (sessionId == voiceCaptureSessionId) {
                _isRemoteVoiceTranscribing.value = false
            }
        }
    }

    private fun extractVoiceTranscriptionError(providerPayload: String): String {
        if (providerPayload.contains(":")) {
            val detail = providerPayload.substringAfter(":").trim()
            if (detail.isNotEmpty()) return detail
        }
        return "No se reconoció voz clara en la grabación. Intenta hablar más cerca del micrófono."
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

    public fun toggleInlineVoiceNotes(currentNotes: String) {
        val currentlyListening = isVoiceListening.value
        val isNotesOrigin = _voiceInputOrigin.value == VoiceInputOrigin.NOTES_INLINE
        val shouldStop = if (currentlyListening) isNotesOrigin else false
        if (shouldStop) {
            stopVoiceInput()
        } else {
            startVoiceInput(VoiceInputOrigin.NOTES_INLINE, currentNotes)
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

    public fun setRecoveryInitialMode(mode: String) {
        _recoveryInitialMode.value = if (mode == "user") "user" else "password"
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
        persistWizardDraftToStorage()
    }

    public fun setClientEventTitleInput(value: String) {
        _clientEventTitleInput.value = value
        persistWizardDraftToStorage()
    }

    public fun setRosaBudgetDop(value: Int) {
        _rosaBudgetDop.value = value.coerceAtLeast(0)
        persistWizardDraftToStorage()
    }

    public fun setGuestsWizardMode(enabled: Boolean) {
        _guestsWizardMode.value = enabled
    }

    public fun hasConfiguredEventDraft(): Boolean {
        val hasTitle = _clientEventTitleInput.value.trim().isNotEmpty()
        val hasType = _eventTypeInput.value.trim().isNotEmpty()
        val hasIdentity = listOf(hasTitle, hasType).any { present -> present }
        val hasDate = _eventDateInput.value.trim().isNotEmpty()
        val hasVenue = _eventVenueInput.value.trim().isNotEmpty()
        return listOf(hasIdentity, hasDate, hasVenue).all { ready -> ready }
    }

    public fun startEventDraftSetup(returnScreenCode: String = "s25") {
        _isEventDraftSetupMode.value = true
        _eventDraftSetupReturnScreen.value = returnScreenCode
    }

    public fun startStandaloneEventCreation() {
        _isEventDraftSetupMode.value = false
        _eventDraftSetupReturnScreen.value = "s40"
    }

    public fun resolveMyEventEntryTarget(): String {
        if (hasConfiguredEventDraft()) {
            return "s25"
        }
        startEventDraftSetup(returnScreenCode = "s25")
        return "s38"
    }

    public fun saveEventDraftDetails(
        eventType: String,
        title: String,
        date: String,
        venue: String,
        guestsCount: Int,
        notes: String = "",
    ) {
        val cleanType = eventType.trim().ifEmpty { "Evento" }
        val cleanTitle = title.trim().ifEmpty { cleanType }
        val cleanDate = date.trim()
        val cleanVenue = venue.trim()
        val cleanNotes = notes.trim().take(InputValidator.MAX_NOTES_LENGTH)
        val safeGuests = guestsCount.coerceAtLeast(0)
        _rosaEventType.value = cleanType
        _eventTypeInput.value = cleanType
        _clientEventTitleInput.value = cleanTitle
        _eventDateInput.value = cleanDate
        _eventVenueInput.value = cleanVenue
        _eventNotesInput.value = cleanNotes
        _eventGuestsInput.value = if (safeGuests > 0) safeGuests.toString() else ""
        _rosaGuestsCount.value = safeGuests
        _isEventDraftSetupMode.value = false
        _formError.value = null
        persistWizardDraftToStorage()
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
        persistWizardScheduleToStorage()
    }

    public fun removeWizardScheduleItem(itemId: String) {
        soundPlayer.playTap()
        _wizardScheduleItems.update { currentList ->
            currentList.filterNot { item -> item.id == itemId }
        }
        persistWizardScheduleToStorage()
    }

    public fun markCalendarPromptDismissed(eventId: String) {
        val cleanId = eventId.trim()
        if (cleanId.isEmpty()) return
        _calendarSyncPromptDismissedIds.update { existing -> existing + cleanId }
    }

    public fun setEventDateInput(value: String) {
        _eventDateInput.value = value
        persistWizardDraftToStorage()
    }

    public fun setEventVenueInput(value: String) {
        _eventVenueInput.value = value
        persistWizardDraftToStorage()
    }

    public fun setEventGuestsInput(value: String) {
        _eventGuestsInput.value = value
        persistWizardDraftToStorage()
    }

    public fun setEventNotesInput(value: String) {
        _eventNotesInput.value = value
        persistWizardDraftToStorage()
    }

    public fun setNewEventTitle(value: String) {
        _newEventTitle.value = value.take(InputValidator.MAX_EVENT_NAME_LENGTH)
        _formError.value = null
    }

    public fun setNewEventClient(value: String) {
        _newEventClient.value = value.take(InputValidator.MAX_FULL_NAME_LENGTH)
        _formError.value = null
    }

    public fun setNewEventDate(value: String) {
        _newEventDate.value = value.take(40)
        _formError.value = null
    }

    public fun setNewEventVenue(value: String) {
        _newEventVenue.value = value.take(InputValidator.MAX_ADDRESS_LENGTH)
        _formError.value = null
    }

    public fun setNewEventGuests(value: String) {
        _newEventGuests.value = InputValidator.filterDigitsOnly(value, 5)
        _formError.value = null
    }

    public fun setPaymentAmountInput(value: String) {
        _paymentAmountInput.value = InputValidator.filterDigitsOnly(value, 7)
        _formError.value = null
    }

    public fun setPaymentMethodInput(value: String) {
        _paymentMethodInput.value = value.take(60)
        _formError.value = null
    }

    public fun setPaymentRefInput(value: String) {
        _paymentRefInput.value = value.take(InputValidator.MAX_BANK_REFERENCE_LENGTH)
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
        _profilePhone.value = InputValidator.filterPhoneInput(value)
        _formError.value = null
    }

    public fun setProfileZone(value: String) {
        val cleanZone = value.trim()
        _profileZone.value = value
        mutatedPreferenceKeys = mutatedPreferenceKeys + "rf_pref_zone"
        securityScope.launch {
            secureStorage.put("rf_pref_zone", cleanZone)
            if (hasValidAuthenticatedSession()) {
                repository.updateUserPreferences(profileZone = cleanZone)
            }
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
        _newArticleCode.value = value.take(24)
        _formError.value = null
    }

    public fun setNewArticleName(value: String) {
        _newArticleName.value = value.take(InputValidator.MAX_ARTICLE_NAME_LENGTH)
        _formError.value = null
    }

    public fun setNewArticleCategory(value: String) {
        _newArticleCategory.value = value.take(40)
    }

    public fun setNewArticlePrice(value: String) {
        _newArticlePrice.value = InputValidator.filterDigitsOnly(value, 7)
        _formError.value = null
    }

    public fun setNewArticleStock(value: String) {
        _newArticleStock.value = InputValidator.filterDigitsOnly(value, 6)
        _formError.value = null
    }

    public fun setPromoCodeInput(value: String) {
        _promoCodeInput.value = value.take(24)
    }

    public fun setPromoDiscountInput(value: String) {
        _promoDiscountInput.value = InputValidator.filterDigitsOnly(value, 6)
    }

    public fun setDepositPercentInput(value: String) {
        _depositPercentInput.value = InputValidator.filterDigitsOnly(value, 3)
    }

    public fun setDeliveryBaseInput(value: String) {
        _deliveryBaseInput.value = InputValidator.filterDigitsOnly(value, 6)
    }

    public fun setCancellationDaysInput(value: String) {
        _cancellationDaysInput.value = InputValidator.filterDigitsOnly(value, 3)
    }

    private fun persistAndSyncPreference(
        storageKey: String,
        value: Boolean,
        pushNotificationsEnabled: Boolean? = null,
        whatsappAlertsEnabled: Boolean? = null,
        emailReceiptsEnabled: Boolean? = null,
        biometricLockEnabled: Boolean? = null,
        autoSyncWifiOnly: Boolean? = null,
    ) {
        mutatedPreferenceKeys = mutatedPreferenceKeys + storageKey
        securityScope.launch {
            secureStorage.put(storageKey, value.toString())
            if (hasValidAuthenticatedSession()) {
                val syncedResult = repository.updateUserPreferences(
                    pushNotificationsEnabled = pushNotificationsEnabled,
                    whatsappAlertsEnabled = whatsappAlertsEnabled,
                    emailReceiptsEnabled = emailReceiptsEnabled,
                    biometricLockEnabled = biometricLockEnabled,
                    autoSyncWifiOnly = autoSyncWifiOnly,
                )
                syncedResult.onSuccess { updatedAccount ->
                    secureStorage.put("rf_pref_push", updatedAccount.preferences.pushNotificationsEnabled.toString())
                    secureStorage.put("rf_pref_whatsapp", updatedAccount.preferences.whatsappAlertsEnabled.toString())
                    secureStorage.put("rf_pref_email_receipts", updatedAccount.preferences.emailReceiptsEnabled.toString())
                    secureStorage.put("rf_pref_biometric_lock", updatedAccount.preferences.biometricLockEnabled.toString())
                    secureStorage.put("rf_pref_auto_sync_wifi", updatedAccount.preferences.autoSyncWifiOnly.toString())
                }
            }
        }
    }

    public fun togglePushNotifications() {
        val next = !_pushNotificationsEnabled.value
        _pushNotificationsEnabled.value = next
        persistAndSyncPreference(
            storageKey = "rf_pref_push",
            value = next,
            pushNotificationsEnabled = next,
        )
        syncPushTokenRegistration(next)
    }

    public fun toggleWhatsappAlerts() {
        val next = !_whatsappAlertsEnabled.value
        _whatsappAlertsEnabled.value = next
        persistAndSyncPreference(
            storageKey = "rf_pref_whatsapp",
            value = next,
            whatsappAlertsEnabled = next,
        )
    }

    public fun toggleEmailReceipts() {
        val next = !_emailReceiptsEnabled.value
        _emailReceiptsEnabled.value = next
        persistAndSyncPreference(
            storageKey = "rf_pref_email_receipts",
            value = next,
            emailReceiptsEnabled = next,
        )
    }

    public fun toggleBiometricLock() {
        val next = !_biometricLockEnabled.value
        _biometricLockEnabled.value = next
        if (!next) {
            _isAppLocked.value = false
            _appLockError.value = null
        }
        persistAndSyncPreference(
            storageKey = "rf_pref_biometric_lock",
            value = next,
            biometricLockEnabled = next,
        )
    }

    public fun toggleAutoSyncWifiOnly() {
        soundPlayer.playTap()
        val next = !_autoSyncWifiOnly.value
        _autoSyncWifiOnly.value = next
        if (!next) {
            _wifiSyncBlockedNotice.value = null
        }
        persistAndSyncPreference(
            storageKey = "rf_pref_auto_sync_wifi",
            value = next,
            autoSyncWifiOnly = next,
        )
    }

    /**
     * Activa el bloqueo biométrico al pasar la aplicación a segundo plano.
     */
    public fun onAppBackgrounded() {
        lockAppIfProtected()
    }

    /**
     * Engages the app lock screen when biometric protection is enabled for a signed-in account.
     */
    public fun lockAppIfProtected() {
        val isProtected = _biometricLockEnabled.value
        val isSignedIn = hasValidAuthenticatedSession()
        val shouldLock = if (isProtected) isSignedIn else false
        if (shouldLock) {
            _appLockError.value = null
            _isAppLocked.value = true
        }
    }

    /**
     * Requests native OS biometric / device credential verification to unlock the app session.
     */
    public fun unlockAppWithBiometrics(onUnlocked: () -> Unit = {}) {
        _appLockError.value = null
        platformActions.authenticateDeviceOwner(
            promptTitle = "Desbloquear RosaFiesta",
            promptSubtitle = "Confirma tu identidad para continuar",
        ) { authenticated, errorMessage ->
            if (authenticated) {
                _isAppLocked.value = false
                _appLockError.value = null
                soundPlayer.playPaymentSuccess()
                onUnlocked()
            } else {
                soundPlayer.playAlertError()
                val readableError = when (errorMessage?.trim()) {
                    "DEVICE_AUTH_UNAVAILABLE", "BIOMETRIC_UNAVAILABLE", "BIOMETRIC_NOT_ENROLLED" ->
                        "La autenticación biométrica no está disponible en este dispositivo. Ingresa tu contraseña para desbloquear."

                    "BIOMETRIC_CANCELLED" ->
                        "Autenticación biométrica cancelada. Inténtalo de nuevo o usa tu contraseña."

                    "BIOMETRIC_FAILED" ->
                        "No coincidió la verificación biométrica. Intenta nuevamente."

                    else ->
                        errorMessage ?: "No se pudo verificar tu identidad. Ingresa tu contraseña para desbloquear."
                }
                _appLockError.value = readableError
            }
        }
    }

    /**
     * Unlocks the protected app session after verifying the account password against local/backend credentials.
     */
    public fun unlockAppWithPassword(
        password: String,
        onUnlocked: () -> Unit = {},
    ) {
        val cleanPassword = password.trim()
        if (cleanPassword.isEmpty()) {
            _appLockError.value = "Ingresa tu contraseña para desbloquear."
            soundPlayer.playAlertError()
            return
        }
        _appLockError.value = null
        securityScope.launch {
            val verifiedResult = repository.verifyAccountPassword(password)
            val isVerified = verifiedResult.getOrDefault(false)
            if (isVerified) {
                _isAppLocked.value = false
                _appLockError.value = null
                soundPlayer.playPaymentSuccess()
                onUnlocked()
            } else {
                soundPlayer.playAlertError()
                _appLockError.value = verifiedResult.exceptionOrNull()?.message
                    ?: "Contraseña incorrecta. Intenta nuevamente."
            }
        }
    }

    public fun toggleSoundEffects() {
        val nextEnabled = !_audioConfig.value.soundEffectsEnabled
        setSoundEffectsEnabled(nextEnabled)
    }

    /**
     * Enables or mutes all application UI sound effects and persists the choice.
     */
    public fun setSoundEffectsEnabled(enabled: Boolean) {
        mutatedPreferenceKeys = mutatedPreferenceKeys + SecureStorage.KEY_AUDIO_ENABLED
        val updated = _audioConfig.value.copy(soundEffectsEnabled = enabled)
        applyAudioConfig(updated, persist = true)
        if (enabled) {
            soundPlayer.playTap()
        }
    }

    /**
     * Updates the master sound effects volume (`0.0f..1.0f`) and persists it.
     */
    public fun setSoundVolume(volume: Float) {
        mutatedPreferenceKeys = mutatedPreferenceKeys + SecureStorage.KEY_AUDIO_VOLUME
        val clampedVolume = volume.coerceIn(0f, 1f)
        val updated = _audioConfig.value.copy(masterVolume = clampedVolume)
        applyAudioConfig(updated, persist = true)
    }

    /**
     * Adjusts the master sound effects volume by [deltaPercent] steps and plays a subtle preview.
     */
    public fun stepSoundVolume(deltaPercent: Int) {
        mutatedPreferenceKeys = mutatedPreferenceKeys + SecureStorage.KEY_AUDIO_VOLUME
        val currentPercent = _audioConfig.value.volumePercent()
        val updated = _audioConfig.value.withVolumePercent(currentPercent + deltaPercent)
        applyAudioConfig(updated, persist = true)
        if (updated.soundEffectsEnabled) {
            soundPlayer.playTap()
        }
    }

    /**
     * Enables or disables a specific [SoundEffectCategory] and persists audio preferences.
     */
    public fun setAudioCategoryEnabled(category: SoundEffectCategory, enabled: Boolean) {
        val storageKey = when (category) {
            SoundEffectCategory.INTERACTION -> SecureStorage.KEY_AUDIO_INTERACTION
            SoundEffectCategory.ROSA_ASSISTANT -> SecureStorage.KEY_AUDIO_ROSA
            SoundEffectCategory.PAYMENTS_ALERTS -> SecureStorage.KEY_AUDIO_ALERTS
            SoundEffectCategory.NONE -> null
        }
        if (storageKey != null) {
            mutatedPreferenceKeys = mutatedPreferenceKeys + storageKey
        }
        val current = _audioConfig.value
        val updated = when (category) {
            SoundEffectCategory.INTERACTION -> current.copy(interactionSoundsEnabled = enabled)
            SoundEffectCategory.ROSA_ASSISTANT -> current.copy(rosaAssistantSoundsEnabled = enabled)
            SoundEffectCategory.PAYMENTS_ALERTS -> current.copy(paymentAlertSoundsEnabled = enabled)
            SoundEffectCategory.NONE -> current
        }
        applyAudioConfig(updated, persist = true)
        if (enabled) {
            playSampleForCategory(category)
        }
    }

    /**
     * Plays a live preview sound or informs the user if audio is currently muted/at zero volume.
     */
    public fun previewAudioEffect(effect: SoundEffect = SoundEffect.ROSA_REPLY) {
        val config = _audioConfig.value
        if (!config.soundEffectsEnabled) {
            _formFeedback.value = "Activa los efectos de sonido para escuchar la prueba."
            return
        }
        if (config.normalizedVolume <= 0.01f) {
            _formFeedback.value = "Sube el volumen sobre 0 % para escuchar la prueba."
            return
        }
        if (config.activeCategories().isEmpty()) {
            _formFeedback.value = "Activa al menos una categoría de sonido para probar."
            return
        }
        val targetEffect = resolvePreviewEffect(effect, config)
        soundPlayer.play(targetEffect)
    }

    /**
     * Restores audio settings to their default enabled state and volume (`75 %`).
     */
    public fun resetAudioDefaults() {
        mutatedPreferenceKeys = mutatedPreferenceKeys + setOf(
            SecureStorage.KEY_AUDIO_ENABLED,
            SecureStorage.KEY_AUDIO_VOLUME,
            SecureStorage.KEY_AUDIO_INTERACTION,
            SecureStorage.KEY_AUDIO_ROSA,
            SecureStorage.KEY_AUDIO_ALERTS,
        )
        applyAudioConfig(AppAudioConfig.DEFAULT, persist = true)
        soundPlayer.playRosaReply()
    }

    private fun resolvePreviewEffect(requested: SoundEffect, config: AppAudioConfig): SoundEffect {
        if (requested.category in config.activeCategories()) {
            return requested
        }
        val firstCategory = config.activeCategories().firstOrNull() ?: return requested
        return when (firstCategory) {
            SoundEffectCategory.INTERACTION -> SoundEffect.TAP_SOFT
            SoundEffectCategory.ROSA_ASSISTANT -> SoundEffect.ROSA_REPLY
            SoundEffectCategory.PAYMENTS_ALERTS -> SoundEffect.PAYMENT_SUCCESS
            SoundEffectCategory.NONE -> requested
        }
    }

    private fun playSampleForCategory(category: SoundEffectCategory) {
        when (category) {
            SoundEffectCategory.INTERACTION -> soundPlayer.playTap()
            SoundEffectCategory.ROSA_ASSISTANT -> soundPlayer.playRosaReply()
            SoundEffectCategory.PAYMENTS_ALERTS -> soundPlayer.playPaymentSuccess()
            SoundEffectCategory.NONE -> Unit
        }
    }

    private fun applyAudioConfig(next: AppAudioConfig, persist: Boolean) {
        _audioConfig.value = next
        _soundEffectsEnabled.value = next.soundEffectsEnabled
        soundPlayer.isEnabled = next.soundEffectsEnabled
        soundPlayer.soundVolume = next.normalizedVolume
        soundPlayer.enabledCategories = next.activeCategories()
        if (!persist) return
        securityScope.launch {
            secureStorage.put(SecureStorage.KEY_AUDIO_ENABLED, next.soundEffectsEnabled.toString())
            secureStorage.put(SecureStorage.KEY_AUDIO_VOLUME, next.normalizedVolume.toString())
            secureStorage.put(SecureStorage.KEY_AUDIO_INTERACTION, next.interactionSoundsEnabled.toString())
            secureStorage.put(SecureStorage.KEY_AUDIO_ROSA, next.rosaAssistantSoundsEnabled.toString())
            secureStorage.put(SecureStorage.KEY_AUDIO_ALERTS, next.paymentAlertSoundsEnabled.toString())
        }
    }

    public fun toggleChecklistStep(stepId: String) {
        soundPlayer.playChecklistTick()
        repository.toggleChecklistStep(stepId) { errorMessage ->
            postError(errorMessage)
        }
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
        "s59",
    )

    private val eventsRestrictedScreenCodes: Set<String> = setOf(
        "A04",
        "A05",
        "A06",
        "A07",
        "A11",
        "A13",
        "A19",
    )

    private val inventoryRestrictedScreenCodes: Set<String> = setOf(
        "A09",
        "A10",
        "A23",
        "A24",
        "A25",
        "s63",
    )

    private val decorationRestrictedScreenCodes: Set<String> = setOf(
        "A16",
        "A17",
        "A18",
    )

    private val reportsRestrictedScreenCodes: Set<String> = setOf(
        "s58",
        "s67",
        "s68",
        "s69",
        "s70",
    )

    private val ownerOnlyScreenCodes: Set<String> = setOf(
        "A28",
        "A31",
    )

    public fun switchRole(role: String) {
        soundPlayer.playTap()
        repository.switchRole(role)
        _formFeedback.value = "Vista cambiada al rol: $role"
    }

    public fun toggleFavorite(articleId: String) {
        soundPlayer.playTap()
        repository.toggleFavorite(articleId)
        val isAdded = favorites.value.contains(articleId)
        val articleName = articles.value.firstOrNull { item -> item.id == articleId }?.name
        _actionToast.value = if (isAdded) {
            if (!articleName.isNullOrBlank()) "$articleName agregado a favoritos." else "Agregado a favoritos."
        } else {
            if (!articleName.isNullOrBlank()) "$articleName quitado de favoritos." else "Quitado de favoritos."
        }
    }

    public fun toggleStaffPaymentAccess(staffName: String) {
        soundPlayer.playTap()
        repository.toggleStaffPaymentAccess(staffName)
    }

    public fun isScreenRestrictedForActiveStaff(screenCode: String): Boolean {
        val normalizedRole = activeRole.value.trim().lowercase()
        val isStaffRole = normalizedRole.startsWith("staff_")
        if (!isStaffRole) {
            return false
        }
        if (screenCode in ownerOnlyScreenCodes) {
            return true
        }
        val staffNameKey = normalizedRole.removePrefix("staff_")
        val matchedStaffMember = staffMembers.value.firstOrNull { member ->
            member.name.equals(staffNameKey, ignoreCase = true)
        } ?: return false
        val perms = matchedStaffMember.permissions
        return when (screenCode) {
            in financeRestrictedScreenCodes -> {
                val hasPerms = StaffPermission.PAYMENTS in perms
                if (matchedStaffMember.canViewPayments) false else !hasPerms
            }

            in eventsRestrictedScreenCodes -> StaffPermission.EVENTS !in perms

            in inventoryRestrictedScreenCodes -> StaffPermission.INVENTORY !in perms

            in decorationRestrictedScreenCodes -> StaffPermission.DECORATION !in perms

            in reportsRestrictedScreenCodes -> StaffPermission.REPORTS !in perms

            else -> false
        }
    }

    private fun validateLoginInputs(): Boolean {
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
        return true
    }

    private fun applyLoginOutcome(outcome: AuthOutcome): Boolean {
        if (!outcome.success) {
            soundPlayer.playAlertError()
            _formError.value = outcome.errorMessage
                ?: SensitiveDataRedactor.translateBusinessError(outcome.errorCode, null)
            return false
        }
        val account = outcome.account
        if (account != null) {
            applyAuthenticatedAccount(account, outcome.accessToken, outcome.refreshToken)
        } else {
            applyUserIdentityFromLogin(_loginEmail.value)
        }
        soundPlayer.playPaymentSuccess()
        _formError.value = null
        val maskedEmail = SensitiveDataRedactor.maskEmail(_profileEmail.value)
        _formFeedback.value = "Sesión iniciada como $maskedEmail"
        return true
    }

    public fun submitLogin(): Boolean {
        if (_isSubmitting.value) return false
        _isSubmitting.value = true
        try {
            if (!validateLoginInputs()) return false
            val outcome = repository.authenticateUser(_loginEmail.value, _loginPassword.value)
            return applyLoginOutcome(outcome)
        } finally {
            _isSubmitting.value = false
        }
    }

    public suspend fun submitLoginOnline(): Boolean {
        if (_isSubmitting.value) return false
        _isSubmitting.value = true
        try {
            if (!validateLoginInputs()) return false
            val outcome = repository.authenticateUserOnline(_loginEmail.value, _loginPassword.value)
            return applyLoginOutcome(outcome)
        } finally {
            _isSubmitting.value = false
        }
    }

    private fun validateRegistrationInputs(
        firstName: String,
        lastName: String,
        username: String,
        email: String,
        password: String,
        birthDate: String,
        acceptedTerms: Boolean,
    ): Boolean {
        val validationError = listOfNotNull(
            InputValidator.validatePersonNamePart(firstName, "El nombre"),
            InputValidator.validatePersonNamePart(lastName, "El apellido"),
            InputValidator.validateUsername(username),
            InputValidator.validateEmail(email),
            InputValidator.validateBirthDate(birthDate, isRequired = true),
            InputValidator.validatePassword(password),
            if (!acceptedTerms) SensitiveDataRedactor.translateBusinessError("AUTH_TERMS_REQUIRED", null) else null,
        ).firstOrNull()
        if (validationError != null) {
            soundPlayer.playAlertError()
            _formError.value = validationError
            return false
        }
        return true
    }

    private fun applyRegistrationOutcome(outcome: AuthOutcome): Boolean {
        if (!outcome.success) {
            soundPlayer.playAlertError()
            _formError.value = outcome.errorMessage
                ?: SensitiveDataRedactor.translateBusinessError(outcome.errorCode, null)
            return false
        }
        val account = outcome.account
        if (account != null) {
            applyAuthenticatedAccount(account, outcome.accessToken, outcome.refreshToken)
        }
        soundPlayer.playPaymentSuccess()
        _formError.value = null
        _formFeedback.value = "Cuenta creada correctamente."
        return true
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
            val isValid = validateRegistrationInputs(
                firstName = firstName,
                lastName = lastName,
                username = username,
                email = email,
                password = password,
                birthDate = birthDate,
                acceptedTerms = acceptedTerms,
            )
            if (!isValid) return false
            val outcome = repository.registerUser(
                firstName = firstName,
                lastName = lastName,
                username = username,
                email = email,
                password = password,
                birthDate = birthDate,
                phone = phone.trim(),
            )
            return applyRegistrationOutcome(outcome)
        } finally {
            _isSubmitting.value = false
        }
    }

    public suspend fun submitRegistrationOnline(
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
            val isValid = validateRegistrationInputs(
                firstName = firstName,
                lastName = lastName,
                username = username,
                email = email,
                password = password,
                birthDate = birthDate,
                acceptedTerms = acceptedTerms,
            )
            if (!isValid) return false
            val outcome = repository.registerUserOnline(
                firstName = firstName,
                lastName = lastName,
                username = username,
                email = email,
                password = password,
                birthDate = birthDate,
                phone = phone.trim(),
            )
            return applyRegistrationOutcome(outcome)
        } finally {
            _isSubmitting.value = false
        }
    }

    public fun logoutSession() {
        soundPlayer.playTap()
        repository.setRefreshToken(null)
        repository.logoutSession()
        resetUserScopedViewModelState()
        _formError.value = null
        _formFeedback.value = "Sesión cerrada correctamente."
        securityScope.launch {
            wipeUserScopedSecureStorage()
        }
    }

    public fun submitPasswordReset(
        identifier: String,
        newPassword: String,
        confirmPassword: String,
        otpCode: String = "",
        onComplete: (Boolean) -> Unit = {},
    ): Boolean {
        if (_isSubmitting.value) {
            onComplete(false)
            return false
        }
        val cleanPass = newPassword.trim()
        val cleanConfirm = confirmPassword.trim()
        val hasMinimumLength = cleanPass.length >= 8
        val hasDigit = cleanPass.any { character -> character.isDigit() }
        val isStrongPassword = if (hasMinimumLength) hasDigit else false
        if (!isStrongPassword) {
            soundPlayer.playAlertError()
            _formError.value = "La nueva contraseña debe tener mínimo 8 caracteres e incluir al menos un número."
            onComplete(false)
            return false
        }
        if (cleanPass != cleanConfirm) {
            soundPlayer.playAlertError()
            _formError.value = SensitiveDataRedactor.translateBusinessError("AUTH_PASSWORD_MISMATCH", null)
            onComplete(false)
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
                onComplete(false)
                return false
            }
        }
        val targetIdentifier = identifier.trim().ifBlank { _pendingOtpEmail.value }
        _isSubmitting.value = true
        securityScope.launch {
            try {
                val outcome = repository.resetPasswordWithOtp(
                    email = targetIdentifier,
                    newPassword = cleanPass,
                    resetToken = _verifiedOtpResetToken.value,
                    otpCode = cleanOtp,
                )
                if (!outcome.success) {
                    soundPlayer.playAlertError()
                    _formError.value = outcome.errorMessage
                        ?: SensitiveDataRedactor.translateBusinessError(outcome.errorCode, null)
                    onComplete(false)
                } else {
                    _activeOtpCode.value = null
                    _verifiedOtpResetToken.value = ""
                    soundPlayer.playPaymentSuccess()
                    _formError.value = null
                    _formFeedback.value = "Contraseña actualizada. Inicia sesión con tu nueva clave."
                    onComplete(true)
                }
            } finally {
                _isSubmitting.value = false
            }
        }
        return true
    }

    public fun changeCurrentPassword(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String,
        onComplete: (Boolean) -> Unit = {},
    ): Boolean {
        if (_isSubmitting.value) {
            onComplete(false)
            return false
        }
        val cleanCurrent = currentPassword.trim()
        val cleanNew = newPassword.trim()
        val cleanConfirm = confirmPassword.trim()
        if (cleanCurrent.isEmpty()) {
            postError("Ingresa tu contraseña actual.")
            onComplete(false)
            return false
        }
        val hasMinLength = cleanNew.length >= 8
        val hasDigit = cleanNew.any { character -> character.isDigit() }
        val isStrong = if (hasMinLength) hasDigit else false
        if (!isStrong) {
            postError("La nueva contraseña requiere mínimo 8 caracteres y al menos un número.")
            onComplete(false)
            return false
        }
        if (cleanNew != cleanConfirm) {
            postError("La confirmación de contraseña no coincide.")
            onComplete(false)
            return false
        }
        val activeToken = repository.currentSessionJwt.value?.trim().orEmpty()
        val hasRealServerToken = if (activeToken.isNotEmpty()) !activeToken.startsWith("rf-local.") else false
        if (!hasRealServerToken) {
            _isSubmitting.value = true
            try {
                val outcome = repository.changeUserPassword(cleanCurrent, cleanNew)
                if (!outcome.success) {
                    postError(outcome.errorMessage ?: "La contraseña actual no es correcta.")
                    onComplete(false)
                    return false
                }
                soundPlayer.playPaymentSuccess()
                _formError.value = null
                _formFeedback.value = "Contraseña actualizada correctamente."
                onComplete(true)
                return true
            } finally {
                _isSubmitting.value = false
            }
        }
        _isSubmitting.value = true
        securityScope.launch {
            try {
                val remoteOutcome = repository.changeUserPasswordOnline(cleanCurrent, cleanNew)
                if (!remoteOutcome.success) {
                    postError(remoteOutcome.errorMessage ?: "No se pudo actualizar la contraseña en el servidor.")
                    onComplete(false)
                } else {
                    soundPlayer.playPaymentSuccess()
                    _formError.value = null
                    _formFeedback.value = "Contraseña actualizada correctamente."
                    onComplete(true)
                }
            } finally {
                _isSubmitting.value = false
            }
        }
        return true
    }

    private fun applyAuthenticatedAccount(
        account: RegisteredUserAccount,
        accessToken: String?,
        refreshToken: String? = null,
    ) {
        _requiresReauthentication.value = false
        applyProfileFieldsFromAccount(account)
        val cleanRefresh = refreshToken?.trim().orEmpty()
        if (cleanRefresh.isNotEmpty()) {
            repository.setRefreshToken(cleanRefresh)
        }
        if (!accessToken.isNullOrBlank()) {
            securityScope.launch {
                secureStorage.put(SecureStorage.KEY_ACCESS_TOKEN, accessToken)
                if (cleanRefresh.isNotEmpty()) {
                    secureStorage.put(SecureStorage.KEY_REFRESH_TOKEN, cleanRefresh)
                }
                secureStorage.put(SecureStorage.KEY_USER_ROLE, account.role)
                syncPushTokenRegistrationForRole(account.preferences.pushNotificationsEnabled, account.role)
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
            val guestsRaw = _newEventGuests.value.trim()
            val validationError = firstNonBlankError(
                InputValidator.validateEventName(title, isRequired = true),
                InputValidator.validateEntityName(
                    rawValue = client,
                    fieldLabel = "El nombre del cliente",
                    minLength = InputValidator.MIN_NAME_LENGTH,
                    maxLength = InputValidator.MAX_FULL_NAME_LENGTH,
                    isRequired = true,
                ),
                validateEventDateOrSpanishLabel(date, allowPlaceholder = false),
                InputValidator.validateAddress(venue, fieldLabel = "El lugar", isRequired = true),
                InputValidator.validateGuestCount(guestsRaw, isRequired = true),
            )
            if (validationError != null) {
                _formError.value = validationError
                return false
            }
            val guests = guestsRaw.toIntOrNull() ?: 0
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

    /**
     * Submits the active client event and requests an official quote for any selected cart items,
     * then clears the in-progress draft without deleting the persisted event or quote records.
     */
    public fun submitClientQuoteRequest(contactPhone: String = ""): Boolean {
        val eventCreated = submitQuickClientEvent(clearDraftAfter = false, contactPhone = contactPhone)
        if (!eventCreated) return false
        val hasDraftItems = draftLineItems().isNotEmpty()
        if (hasDraftItems) {
            syncDraftToOfficialQuote(contactPhone = contactPhone)
        }
        clearEventDraftAfterSubmission()
        return true
    }

    /**
     * Clears only the in-progress event wizard and cart draft after a successful submission,
     * keeping all persisted events, quotes, guests, schedules, and sketches intact in storage.
     */
    public fun clearEventDraftAfterSubmission() {
        mutatedPreferenceKeys = mutatedPreferenceKeys + setOf(
            SecureStorage.KEY_DRAFT_QUANTITIES,
            SecureStorage.KEY_DRAFT_DISCOUNT,
            SecureStorage.KEY_WIZARD_EVENT_DRAFT,
            SecureStorage.KEY_WIZARD_SCHEDULE,
            SecureStorage.KEY_EVENT_GUESTS,
            "rf_sketch_layout_json",
        )
        _draftQuantities.value = emptyMap()
        _draftDiscountDop.value = 0
        _lastAddedDraftItem.value = null
        _eventTypeInput.value = ""
        _rosaEventType.value = ""
        _clientEventTitleInput.value = ""
        _eventDateInput.value = ""
        _eventVenueInput.value = ""
        _eventGuestsInput.value = ""
        _rosaBudgetDop.value = 0
        _eventNotesInput.value = ""
        _wizardScheduleItems.value = emptyList()
        _eventGuestsList.value = emptyList()
        _guestsWizardMode.value = false
        _rosaOmittedCategories.value = emptySet()
        _sketchLayoutJson.value = ""
        securityScope.launch {
            draftStorageMutex.withLock {
                secureStorage.remove(SecureStorage.KEY_DRAFT_QUANTITIES)
                secureStorage.remove(SecureStorage.KEY_DRAFT_DISCOUNT)
                secureStorage.remove(SecureStorage.KEY_WIZARD_EVENT_DRAFT)
                secureStorage.remove(SecureStorage.KEY_WIZARD_SCHEDULE)
                secureStorage.remove(SecureStorage.KEY_EVENT_GUESTS)
                secureStorage.remove("rf_sketch_layout_json")
            }
        }
    }

    public fun submitQuickClientEvent(
        clearDraftAfter: Boolean = true,
        contactPhone: String = "",
    ): Boolean {
        if (_isSubmitting.value) return false
        _isSubmitting.value = true
        try {
            val categoryLabel = _eventTypeInput.value.trim().ifBlank { _rosaEventType.value.trim() }
            val title = _clientEventTitleInput.value.trim().ifBlank { categoryLabel }
            val date = _eventDateInput.value.trim()
            val venue = _eventVenueInput.value.trim()
            val guests = _eventGuestsInput.value.trim().toIntOrNull()?.coerceAtLeast(0) ?: 0
            val resolvedContactPhone = contactPhone.trim().ifBlank { _profilePhone.value.trim() }
            val hasValidInputs = allConditionsTrue(
                first = title.isNotEmpty(),
                second = date.isNotEmpty(),
                third = venue.isNotEmpty(),
                fourth = guests >= 0,
            )
            if (!hasValidInputs) {
                _formError.value = "Completa el título o tipo de evento, fecha y lugar."
                return false
            }
            val accountName = currentAuthenticatedUser.value?.fullName.orEmpty().trim()
            val resolvedClientName = accountName.ifEmpty { _profileName.value.trim().ifEmpty { "Cliente" } }
            repository.createEvent(
                title = title,
                clientName = resolvedClientName,
                dateLabel = date,
                venue = venue,
                guests = guests,
                contactPhone = resolvedContactPhone,
            )
            repository.events.value.lastOrNull()?.id?.let { createdId ->
                sessionCreatedEventIds.add(createdId)
                selectEvent(createdId)
                val draftSketch = _sketchLayoutJson.value.trim()
                if (draftSketch.isNotEmpty()) {
                    repository.updateEventDetails(eventId = createdId, sketchJson = draftSketch)
                }
                tasksByEventId[createdId] = emptyList()
                _eventTasksList.value = emptyList()
                persistEventTasksToStorage()
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
                val draftGuests = _eventGuestsList.value
                draftGuests.forEach { draftGuest ->
                    val rsvpCode = when (draftGuest.status) {
                        "Confirmado" -> "CONFIRMED"
                        "Rechazado" -> "DECLINED"
                        else -> "PENDING"
                    }
                    val tableSegment = draftGuest.details
                        .split("·")
                        .getOrNull(1)
                        ?.trim()
                        .orEmpty()
                        .ifEmpty { "Por asignar" }
                    val notesSegment = draftGuest.details
                        .split("·")
                        .getOrNull(2)
                        ?.trim()
                        .orEmpty()
                    repository.addEventGuest(
                        eventId = createdId,
                        fullName = draftGuest.name,
                        rsvpStatus = rsvpCode,
                        tableLabel = tableSegment,
                        notes = notesSegment,
                    )
                }
            }
            if (clearDraftAfter) {
                clearEventDraftAfterSubmission()
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
            val cat = _newArticleCategory.value.trim().ifEmpty {
                catalogCategories.value.firstOrNull()?.name.orEmpty()
            }
            val categoryError = if (cat.isEmpty()) {
                "Crea y selecciona una categoría antes de guardar el artículo."
            } else {
                null
            }
            val priceRaw = _newArticlePrice.value.trim()
            val stockRaw = _newArticleStock.value.trim()
            val cleanDescription = description.trim()
            val cleanDimensions = dimensions.trim()
            val cleanMaterial = material.trim()
            val validationError = firstNonBlankError(
                categoryError,
                validateArticleCode(code),
                InputValidator.validateArticleName(name, isRequired = true),
                InputValidator.validateMoneyAmount(
                    rawValue = priceRaw,
                    fieldLabel = "El precio diario",
                    isRequired = true,
                    minAmount = 1.0,
                    maxAmount = InputValidator.MAX_MONEY_AMOUNT,
                ),
                InputValidator.validateStockCount(stockRaw, isRequired = true),
                InputValidator.validateNotes(
                    rawValue = cleanDescription,
                    fieldLabel = "La descripción",
                    isRequired = false,
                    maxLength = InputValidator.MAX_DESCRIPTION_LENGTH,
                ),
                InputValidator.validateNotes(
                    rawValue = cleanDimensions,
                    fieldLabel = "Las dimensiones",
                    isRequired = false,
                    maxLength = 80,
                ),
                InputValidator.validateNotes(
                    rawValue = cleanMaterial,
                    fieldLabel = "El material",
                    isRequired = false,
                    maxLength = 80,
                ),
            )
            if (validationError != null) {
                _formError.value = validationError
                return false
            }
            val price = priceRaw.toIntOrNull() ?: 0
            val stock = stockRaw.toIntOrNull() ?: 0
            val editingId = _editingArticleId.value
            val uploadedPhotoUrl = _newArticlePhotoUrl.value.trim()
            if (editingId != null) {
                repository.updateArticleWithMedia(
                    articleId = editingId,
                    name = name,
                    category = cat,
                    description = cleanDescription.ifBlank { null },
                    pricePerDayDop = price,
                    totalStock = stock,
                    dimensions = cleanDimensions.ifBlank { null },
                    material = cleanMaterial.ifBlank { null },
                    visibleInCatalog = visibleInCatalog,
                    photoUrl = uploadedPhotoUrl,
                )
            } else {
                repository.createArticleWithMedia(
                    code = code,
                    name = name,
                    category = cat,
                    pricePerDayDop = price,
                    totalStock = stock,
                    photoUrl = uploadedPhotoUrl,
                    description = cleanDescription,
                    dimensions = cleanDimensions,
                    material = cleanMaterial,
                    visibleInCatalog = visibleInCatalog,
                )
            }
            _editingArticleId.value = null
            _newArticlePhotoUrl.value = ""
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
            val rawAmountDigits = InputValidator.filterDigitsOnly(_paymentAmountInput.value.trim(), 8)
            val method = _paymentMethodInput.value.trim()
            val ref = _paymentRefInput.value.trim()
            val isCashMethod = method.startsWith("Efectivo", ignoreCase = true)
            val methodError = if (method.isEmpty()) "Selecciona un método de pago." else null
            val refError = InputValidator.validateBankReference(ref, isRequired = !isCashMethod)
            val validationError = firstNonBlankError(
                InputValidator.validateMoneyAmount(
                    rawValue = rawAmountDigits,
                    fieldLabel = "El monto del pago",
                    isRequired = true,
                    minAmount = 1.0,
                    maxAmount = InputValidator.MAX_MONEY_AMOUNT,
                ),
                methodError,
                refError,
            )
            if (validationError != null) {
                soundPlayer.playAlertError()
                _formError.value = validationError
                return null
            }
            val amount = rawAmountDigits.toIntOrNull() ?: 0
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
        if (_isSubmitting.value) {
            return DateChangeResult(allowed = false, isFree = false, feeDop = 0, message = "Operación en curso.")
        }
        val nextDate = _dateChangeInput.value.trim()
        if (nextDate.isEmpty()) {
            soundPlayer.playAlertError()
            _formError.value = "Indica la nueva fecha solicitada."
            return DateChangeResult(allowed = false, isFree = false, feeDop = 0, message = "Fecha vacía.")
        }
        _isSubmitting.value = true
        try {
            val res = repository.changeEventDate(eventId, nextDate)
            if (res.allowed) soundPlayer.playPaymentSuccess() else soundPlayer.playAlertError()
            _dateChangeBanner.value = res.message
            _formError.value = if (res.allowed) null else res.message
            return res
        } finally {
            _isSubmitting.value = false
        }
    }

    public fun submitCancelEvent(
        eventId: String = _selectedEventId.value,
        reason: String = "",
    ): CancellationRefund {
        if (_isSubmitting.value) {
            return RosaFiestaEngine.calculateCancellationRefund(0, 0)
        }
        _isSubmitting.value = true
        try {
            soundPlayer.playAlertError()
            val cleanReason = reason.trim()
            val refund = repository.cancelEvent(eventId, cleanReason)
            val reasonSuffix = if (cleanReason.isNotEmpty()) " ($cleanReason)" else ""
            _formFeedback.value =
                "Evento cancelado$reasonSuffix · Reembolso ${refund.refundPercent}% (${RosaFiestaEngine.formatDop(refund.refundDop)})"
            return refund
        } finally {
            _isSubmitting.value = false
        }
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
        if (_isSubmitting.value) return false
        _isSubmitting.value = true
        try {
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

            val isCustomDraft = draftAvatarKey?.startsWith("CUSTOM:") == true
            val persistedAvatarArg = if (isCustomDraft) null else draftAvatarKey
            val outcome = repository.updateUserProfile(
                fullName = cleanName,
                email = cleanEmail,
                phone = cleanPhone,
                birthDate = cleanBirthDate,
                username = cleanUsername,
                avatarKey = persistedAvatarArg,
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
                applyAuthenticatedAccount(
                    account = updatedAccount,
                    accessToken = outcome.accessToken,
                    refreshToken = outcome.refreshToken,
                )
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
        } finally {
            _isSubmitting.value = false
        }
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
                if (hasValidAuthenticatedSession()) {
                    val syncOutcome = repository.updateUserAvatarOnline(avatarKey)
                    if (!syncOutcome.success) {
                        val message = syncOutcome.errorMessage ?: "No se pudo sincronizar el avatar en el servidor."
                        _formError.value = message
                    }
                }
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
                _pickedAttachmentFile.value = picked
                val activeToken = repository.currentSessionJwt.value?.trim().orEmpty()
                val hasServerToken = if (activeToken.isNotEmpty()) !activeToken.startsWith("rf-local.") else false
                val canSyncRemote = if (hasValidAuthenticatedSession()) hasServerToken else false
                if (!canSyncRemote) {
                    setProfileAvatarKey(customKey)
                    _formError.value = null
                    _formFeedback.value = "Foto '${picked.fileName}' seleccionada."
                    onPicked(picked)
                    return@launch
                }
                val uploadResult = repository.uploadMedia(
                    filename = picked.fileName,
                    mimeType = picked.mimeType,
                    purpose = "avatar",
                    bytes = picked.bytes,
                )
                val uploadedAsset = uploadResult.getOrNull()
                if (uploadedAsset == null) {
                    setProfileAvatarKey(customKey)
                    val uploadErr = uploadResult.exceptionOrNull()?.message?.trim().orEmpty().ifBlank {
                        "No se pudo subir '${picked.fileName}' al servidor."
                    }
                    _formError.value = uploadErr
                    soundPlayer.playAlertError()
                    onPicked(picked)
                    return@launch
                }
                putBoundedMediaCacheEntry(uploadedAsset.url, picked.bytes)
                val avatarOutcome = repository.updateUserAvatarOnline(uploadedAsset.url)
                if (!avatarOutcome.success) {
                    val syncErr = avatarOutcome.errorMessage ?: "No se pudo guardar la foto de perfil."
                    _formError.value = syncErr
                    soundPlayer.playAlertError()
                    onPicked(picked)
                    return@launch
                }
                _profileAvatarKey.value = uploadedAsset.url
                secureStorage.put(SecureStorage.KEY_AVATAR_KEY, uploadedAsset.url)
                secureStorage.remove(SecureStorage.KEY_AVATAR_IMAGE_DATA)
                _formError.value = null
                _formFeedback.value = "Foto '${picked.fileName}' actualizada."
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
        mutatedPreferenceKeys = mutatedPreferenceKeys + SecureStorage.KEY_DARK_THEME
        securityScope.launch {
            secureStorage.put(SecureStorage.KEY_DARK_THEME, enabled.toString())
            if (hasValidAuthenticatedSession()) {
                repository.updateUserPreferences(darkThemeEnabled = enabled)
            }
        }
    }

    public fun toggleDarkTheme() {
        setDarkTheme(!_isDarkTheme.value)
    }

    public fun setPreferredLanguage(language: String) {
        soundPlayer.playTap()
        val cleanLang = language.trim().ifBlank { "Español" }
        _preferredLanguage.value = cleanLang
        mutatedPreferenceKeys = mutatedPreferenceKeys + SecureStorage.KEY_LANGUAGE
        val canonicalLangCode = if (cleanLang.startsWith("En", ignoreCase = true)) "en" else "es"
        securityScope.launch {
            secureStorage.put(SecureStorage.KEY_LANGUAGE, cleanLang)
            if (hasValidAuthenticatedSession()) {
                repository.updateUserPreferences(preferredLanguage = canonicalLangCode)
            }
        }
    }

    public fun togglePreferredLanguage() {
        val nextLanguage = if (_preferredLanguage.value == "Español") "English" else "Español"
        setPreferredLanguage(nextLanguage)
    }

    public fun setRemindersEnabled(enabled: Boolean) {
        soundPlayer.playTap()
        val previousEnabled = _remindersEnabled.value
        _remindersEnabled.value = enabled
        mutatedPreferenceKeys = mutatedPreferenceKeys + SecureStorage.KEY_REMINDERS
        securityScope.launch {
            secureStorage.put(SecureStorage.KEY_REMINDERS, enabled.toString())
            if (hasValidAuthenticatedSession()) {
                val result = repository.updateUserPreferences(eventRemindersEnabled = enabled)
                if (result.isFailure) {
                    _remindersEnabled.value = previousEnabled
                    secureStorage.put(SecureStorage.KEY_REMINDERS, previousEnabled.toString())
                    val err = result.exceptionOrNull()
                    postError(
                        err?.message?.trim().orEmpty().ifBlank {
                            "No se pudo guardar la preferencia de recordatorios."
                        },
                    )
                }
            }
        }
    }

    public fun toggleRemindersEnabled() {
        setRemindersEnabled(!_remindersEnabled.value)
    }

    public fun setSavedPaymentMethod(methodLabel: String) {
        soundPlayer.playTap()
        val cleanMethod = methodLabel.trim().ifBlank { "Transferencia Banreservas" }
        _savedPaymentMethod.value = cleanMethod
        mutatedPreferenceKeys = mutatedPreferenceKeys + SecureStorage.KEY_PAYMENT_METHOD
        securityScope.launch {
            secureStorage.put(SecureStorage.KEY_PAYMENT_METHOD, cleanMethod)
            if (hasValidAuthenticatedSession()) {
                repository.updateUserPreferences(savedPaymentMethod = cleanMethod)
            }
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
        val isAnonymousProfile = if (isEmptyName) {
            true
        } else if (isGuest) {
            true
        } else {
            isGuestRosa
        }
        if (isAnonymousProfile) {
            return allEvents.filter { eventItem -> eventItem.id in sessionCreatedEventIds }
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

    public fun activeClientEvent(): ClientEvent = activeClientEventOrNull() ?: emptyNeutralClientEvent()

    private fun emptyNeutralClientEvent(): ClientEvent = ClientEvent(
        id = "",
        code = "RF-000",
        title = "Sin eventos",
        clientName = "",
        eventType = "",
        dateLabel = "",
        daysUntilEvent = 0,
        venue = "",
        zone = "",
        guests = 0,
        articleCount = 0,
        totalDop = 0,
        paidDepositDop = 0,
        depositMethodLabel = "",
        depositDateLabel = "",
        balanceDueDateLabel = "",
        status = EventStatus.PENDING_QUOTE,
        leadStaffName = "",
        photo = PhotoAsset.BODA,
        items = emptyList(),
    )

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

    public fun loadTasksForActiveEvent() {
        val activeId = activeClientEventOrNull()?.id.orEmpty().ifBlank { _selectedEventId.value }
        if (activeId.isBlank()) return
        securityScope.launch {
            repository.loadEventTasks(activeId)
        }
    }

    public fun toggleEventTask(taskId: String) {
        soundPlayer.playChecklistTick()
        val target = _eventTasksList.value.firstOrNull { item -> item.id == taskId }
        val nextDone = !(target?.completed ?: false)
        val updated = _eventTasksList.value.map { taskItem ->
            if (taskItem.id == taskId) taskItem.copy(completed = nextDone) else taskItem
        }
        _eventTasksList.value = updated
        val activeId = activeClientEventOrNull()?.id.orEmpty().ifBlank { _selectedEventId.value }
        tasksByEventId[activeId] = updated
        persistEventTasksToStorage()
        if (activeId.isNotBlank()) {
            repository.toggleEventTask(activeId, taskId, nextDone)
        }
    }

    public fun addEventTask(customTitle: String = "", badgeLabel: String = "Pendiente") {
        soundPlayer.playTap()
        val nextNum = _eventTasksList.value.size + 1
        val activeEvt = activeClientEvent()
        val resolvedTitle = customTitle.trim().ifBlank { "Coordinación #$nextNum · ${activeEvt.title}" }
        val activeId = activeEvt.id.ifBlank { _selectedEventId.value }
        val backendTask = if (activeId.isNotBlank()) {
            repository.createEventTask(activeId, resolvedTitle, badgeLabel.trim())
        } else {
            null
        }
        val newTask = EventTaskUiModel(
            id = backendTask?.id ?: "tsk-$nextNum",
            title = resolvedTitle,
            badgeText = badgeLabel.trim().ifBlank { "Pendiente" },
            isUrgent = false,
            completed = false,
        )
        val updated = listOf(newTask) + _eventTasksList.value
        _eventTasksList.value = updated
        tasksByEventId[activeId] = updated
        persistEventTasksToStorage()
    }

    public fun deleteEventTask(taskId: String) {
        soundPlayer.playTap()
        val activeId = activeClientEventOrNull()?.id.orEmpty().ifBlank { _selectedEventId.value }
        val updated = _eventTasksList.value.filterNot { item -> item.id == taskId }
        _eventTasksList.value = updated
        tasksByEventId[activeId] = updated
        persistEventTasksToStorage()
        if (activeId.isNotBlank()) {
            repository.deleteEventTask(activeId, taskId)
        }
    }

    public fun deleteEventGuest(guestId: String) {
        val activeId = activeClientEventOrNull()?.id.orEmpty().ifBlank { _selectedEventId.value }
        if (activeId.isBlank()) return
        soundPlayer.playTap()
        repository.deleteEventGuest(activeId, guestId)
    }

    public fun guestsForActiveEvent(): List<EventGuestUiModel> {
        if (_guestsWizardMode.value) {
            return _eventGuestsList.value
        }
        val activeEvent = activeClientEventOrNull() ?: return emptyList()
        val repoGuests = repository.eventGuests.value
            .filter { guest -> guest.eventId == activeEvent.id }
            .map { guest ->
                val statusLabel = when (guest.rsvpStatus.trim().uppercase()) {
                    "CONFIRMED" -> "Confirmado"
                    "DECLINED" -> "Rechazado"
                    else -> "Pendiente"
                }
                val detailSegments = listOf(
                    statusLabel,
                    guest.tableLabel.trim(),
                    guest.notes.trim(),
                ).filter { segment -> segment.isNotEmpty() }
                EventGuestUiModel(
                    id = guest.id,
                    name = guest.fullName,
                    details = detailSegments.joinToString(" · "),
                    status = statusLabel,
                )
            }
        return repoGuests
    }

    public fun filterNotificationsForPersonalClient(
        source: List<NotificationRecord> = notifications.value,
    ): List<NotificationRecord> {
        if (_isExplicitGuestSession.value) {
            return emptyList()
        }
        return source.filter { item ->
            val roleLower = item.recipientRole.trim().lowercase()
            when (roleLower) {
                "client", "user", "all", "" -> true
                else -> false
            }
        }
    }

    public fun toggleEventGuestStatus(guestId: String) {
        soundPlayer.playTap()
        if (_guestsWizardMode.value) {
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
            persistWizardGuestsToStorage()
            return
        }
        val activeEventId = activeClientEventOrNull()?.id ?: _selectedEventId.value
        val existingRepoGuest = repository.eventGuests.value.firstOrNull { guest -> guest.id == guestId }
        if (existingRepoGuest != null) {
            val nextRsvpCode = when (existingRepoGuest.rsvpStatus.trim().uppercase()) {
                "CONFIRMED" -> "PENDING"
                "PENDING" -> "DECLINED"
                else -> "CONFIRMED"
            }
            repository.updateEventGuest(
                eventId = activeEventId,
                guestId = guestId,
                rsvpStatus = nextRsvpCode,
            )
        }
    }

    public fun addEventGuest(
        guestName: String = "",
        tableNumberLabel: String = "",
        partySize: Int = 2,
        status: String = "Confirmado",
    ) {
        soundPlayer.playTap()
        val existingCount = if (_guestsWizardMode.value) {
            _eventGuestsList.value.size
        } else {
            guestsForActiveEvent().size
        }
        val nextNum = existingCount + 1
        val resolvedName = guestName.trim().ifBlank { "Invitado #$nextNum" }
        val resolvedTable = tableNumberLabel.trim().ifBlank { "Mesa $nextNum" }
        val peopleLabel = if (partySize == 1) "1 persona" else "${partySize.coerceAtLeast(1)} personas"
        val cleanStatus = status.trim().ifBlank { "Confirmado" }
        val rsvpCode = when (cleanStatus) {
            "Confirmado" -> "CONFIRMED"
            "Rechazado" -> "DECLINED"
            else -> "PENDING"
        }
        if (_guestsWizardMode.value) {
            val newGuest = EventGuestUiModel(
                id = "draft-gst-$nextNum",
                name = resolvedName,
                details = "$cleanStatus · $resolvedTable · $peopleLabel",
                status = cleanStatus,
            )
            _eventGuestsList.update { currentList -> currentList + newGuest }
            persistWizardGuestsToStorage()
        } else {
            val targetEventId = activeClientEventOrNull()?.id ?: _selectedEventId.value
            if (targetEventId.isNotBlank()) {
                repository.addEventGuest(
                    eventId = targetEventId,
                    fullName = resolvedName,
                    rsvpStatus = rsvpCode,
                    tableLabel = resolvedTable,
                    notes = peopleLabel,
                )
            }
        }
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
                    applyAuthenticatedAccount(nonNullAccount, outcome.accessToken, outcome.refreshToken)
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

    /**
     * Initiates an external browser OAuth PKCE authorization flow for [provider] (`google` or `apple`).
     */
    public fun signInWithBrowserOAuth(
        provider: String,
        onComplete: (Boolean) -> Unit = {},
    ) {
        if (_isSubmitting.value) return
        val normalizedProvider = provider.trim().lowercase()
        val displayProvider = normalizedProvider.replaceFirstChar { ch -> ch.uppercase() }
        val codeVerifier = JwtAuthManager.base64UrlEncode(Random.nextBytes(48))
        val codeChallenge = JwtAuthManager.base64UrlEncode(JwtAuthManager.sha256(codeVerifier.encodeToByteArray()))
        val state = JwtAuthManager.base64UrlEncode(Random.nextBytes(24))
        _isSubmitting.value = true
        _formError.value = null
        _formFeedback.value = null
        securityScope.launch {
            try {
                val browserOutcome = platformActions.launchOAuthBrowserFlow { redirectUri ->
                    val resolvedUrl = repository.startOAuthAuthorization(
                        provider = normalizedProvider,
                        redirectUri = redirectUri,
                        codeChallenge = codeChallenge,
                        state = state,
                    ).getOrNull()
                    if (resolvedUrl != null) {
                        val packedPkce = "$normalizedProvider|$codeVerifier|$state|$redirectUri"
                        secureStorage.put(SecureStorage.KEY_PENDING_OAUTH_PKCE, packedPkce)
                    }
                    resolvedUrl
                }
                secureStorage.remove(SecureStorage.KEY_PENDING_OAUTH_PKCE)
                val code = browserOutcome.code?.trim().orEmpty()
                if (code.isEmpty()) {
                    soundPlayer.playAlertError()
                    _formError.value = translateBrowserOAuthError(displayProvider, browserOutcome.errorCode)
                    onComplete(false)
                    return@launch
                }
                val returnedState = browserOutcome.state?.trim().orEmpty()
                val hasStateMismatch = if (returnedState.isNotEmpty()) returnedState != state else false
                if (hasStateMismatch) {
                    soundPlayer.playAlertError()
                    _formError.value = "Validación de estado OAuth inválida para $displayProvider."
                    onComplete(false)
                    return@launch
                }
                val exchangeOutcome = repository.exchangeOAuthCode(
                    provider = normalizedProvider,
                    code = code,
                    codeVerifier = codeVerifier,
                    redirectUri = browserOutcome.redirectUri,
                )
                val account = exchangeOutcome.account
                val isVerified = if (exchangeOutcome.success) account != null else false
                if (isVerified) {
                    val authenticatedAccount = account ?: return@launch
                    applyAuthenticatedAccount(authenticatedAccount, exchangeOutcome.accessToken, exchangeOutcome.refreshToken)
                    soundPlayer.playPaymentSuccess()
                    _formError.value = null
                    _formFeedback.value = "Sesión iniciada con $displayProvider."
                    onComplete(true)
                } else {
                    soundPlayer.playAlertError()
                    _formError.value = exchangeOutcome.errorMessage
                        ?: "No se pudo verificar tu cuenta de $displayProvider."
                    onComplete(false)
                }
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    private fun translateBrowserOAuthError(displayProvider: String, errorCode: String?): String = when (errorCode) {
        "OAUTH_START_FAILED" -> "$displayProvider OAuth no está habilitado en el servidor."
        "OAUTH_CANCELLED" -> "Inicio de sesión con $displayProvider cancelado."
        "OAUTH_TIMEOUT" -> "Tiempo de espera agotado al autenticar con $displayProvider."
        else -> "No se pudo abrir el navegador para $displayProvider. Ingresa con tu correo y contraseña."
    }

    private fun syncPushTokenRegistration(enabled: Boolean) {
        if (!hasValidAuthenticatedSession()) return
        val role = currentAuthenticatedUser.value?.role ?: activeRole.value
        syncPushTokenRegistrationForRole(enabled, role)
    }

    private fun syncPushTokenRegistrationForRole(enabled: Boolean, role: String) {
        val isAdminRole = role == "admin"
        val isStaffRole = role == "staff"
        val isPrivilegedRole = if (isAdminRole) true else isStaffRole
        val appVariant = if (isPrivilegedRole) "admin" else "personal"
        if (!enabled) {
            securityScope.launch {
                repository.unregisterDevicePushToken()
            }
            return
        }
        platformActions.registerPushNotificationsWithConfig(
            appVariant = appVariant,
            vapidPublicKey = _oauthConfig.value.webPushVapidPublicKey,
        ) { platform, token ->
            val cleanToken = token?.trim().orEmpty()
            if (cleanToken.isNotEmpty()) {
                securityScope.launch {
                    repository.registerDevicePushToken(
                        platform = platform,
                        token = cleanToken,
                        appVariant = appVariant,
                    )
                }
            }
        }
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
        val cleanPhone = rawPhone.trim()
        if (cleanPhone.isEmpty()) {
            _formFeedback.value = null
            _formError.value = "El teléfono de contacto aún no está configurado."
            soundPlayer.playAlertError()
            return false
        }
        val launched = platformActions.dialPhoneNumber(cleanPhone)
        if (!launched) {
            _formError.value = null
            _formFeedback.value = "Teléfono de contacto: $cleanPhone"
        } else {
            _formError.value = null
        }
        return launched
    }

    public fun openWhatsAppChat(rawPhone: String = businessSettings.value.whatsapp, message: String = ""): Boolean {
        val cleanPhone = rawPhone.trim()
        if (cleanPhone.isEmpty()) {
            _formFeedback.value = null
            _formError.value = "El número de WhatsApp aún no está configurado."
            soundPlayer.playAlertError()
            return false
        }
        val launched = platformActions.openWhatsAppChat(cleanPhone, message)
        if (!launched) {
            _formError.value = null
            _formFeedback.value = "WhatsApp RosaFiesta: $cleanPhone"
        } else {
            _formError.value = null
        }
        return launched
    }

    public fun openMapsSearch(query: String): Boolean {
        val cleanQuery = query.trim()
        if (cleanQuery.isEmpty()) {
            _formFeedback.value = null
            _formError.value = "La dirección aún no está configurada en los ajustes del negocio."
            soundPlayer.playAlertError()
            return false
        }
        val launched = platformActions.openMapsSearch(cleanQuery)
        if (!launched) {
            _formFeedback.value = null
            _formError.value = "No se pudo abrir Mapas para: $cleanQuery"
            soundPlayer.playAlertError()
        } else {
            _formError.value = null
            _formFeedback.value = null
        }
        return launched
    }

    public fun sharePlainText(title: String, text: String): Boolean {
        val shared = platformActions.shareContent(title = title, text = text)
        if (shared) {
            _actionToast.value = "Copiado al portapapeles para compartir."
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
        val itbisRate = settings.itbisPercent.coerceAtLeast(0).toLong()
        val itbisDivisor = 100L + itbisRate
        val netSubtotalDop = if (payment != null) {
            if (payment.subtotalDop > 0) payment.subtotalDop.toLong() else ((paidAmount * 100L) / itbisDivisor)
        } else {
            ((event.totalDop * 100L) / itbisDivisor)
        }
        val computedItbisDop = if (payment != null) {
            if (payment.itbisDop > 0) payment.itbisDop.toLong() else (paidAmount.toLong() - netSubtotalDop).coerceAtLeast(0L)
        } else {
            (event.totalDop.toLong() - netSubtotalDop).coerceAtLeast(0L)
        }
        val resolvedStatusLabel = when {
            payment == null -> "VERIFICADO"
            payment.rejectionReason.isNotBlank() -> "RECHAZADO"
            !payment.verified -> "PENDIENTE DE VERIFICACIÓN"
            else -> "VERIFICADO"
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
            verificationStatus = resolvedStatusLabel,
            companyName = settings.businessName,
            companyRnc = settings.rnc,
            ncfNumber = payment?.ncfNumber.orEmpty(),
            ncfType = payment?.ncfType ?: "B02",
            clientRnc = payment?.clientRnc.orEmpty(),
            subtotalDop = netSubtotalDop,
            itbisDop = computedItbisDop,
            paidAmountDop = paidAmount.toLong(),
            remainingBalanceDop = remaining.toLong(),
            securityDepositDop = (payment?.securityDepositDop ?: 0).toLong(),
            lineItems = lineItems,
        )
        val pdfBytes = ReceiptPdfBuilder.buildPdfBytes(pdfDoc)
        val fileName = "$receiptNo.pdf"
        if (shareInsteadOfSave) {
            val sharedOk = platformActions.shareContent(
                title = "Comprobante $receiptNo · RosaFiesta",
                text = "Recibo $receiptNo · ${event.title} · RD$ $paidAmount",
                fileName = fileName,
                fileBytes = pdfBytes,
                mimeType = "application/pdf",
            )
            if (!sharedOk) {
                _formError.value = "No se pudo abrir la hoja de compartir en este dispositivo."
                return ""
            }
            _formFeedback.value = "Comprobante $receiptNo compartido en PDF."
        } else {
            val savedPath = platformActions.saveDocument(fileName, "application/pdf", pdfBytes)
            if (savedPath == null) {
                _formError.value = "No se pudo guardar el recibo PDF en el dispositivo."
                return ""
            }
            _formFeedback.value = "PDF descargado: $fileName"
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
                val remoteCsvResult = repository.exportDgii607Csv(period)
                val remoteCsv = remoteCsvResult.getOrNull().orEmpty().trim()
                if (remoteCsv.isNotEmpty()) {
                    val utf8Remote = remoteCsv.encodeToByteArray()
                    byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()) + utf8Remote
                } else {
                    val (curYear, curMonth, curDay) = DomainClockFormatter.currentCivilDateTriple()
                    val todayYyyymmdd = buildString {
                        append(curYear.toString())
                        append(curMonth.toString().padStart(2, '0'))
                        append(curDay.toString().padStart(2, '0'))
                    }
                    val activeItbisDivisor = 100 + businessSettings.value.itbisPercent.coerceAtLeast(0)
                    val records = payments.value.mapIndexed { index, payment ->
                        val netAmount = ((payment.amountDop * 100) / activeItbisDivisor).toLong()
                        val taxAmount = payment.amountDop.toLong() - netAmount
                        val paddedSeq = (index + 1).toString().padStart(8, '0')
                        val resolvedNcf = payment.ncfNumber.ifBlank { "${payment.ncfType.ifBlank { "B02" }}$paddedSeq" }
                        val resolvedTaxId = payment.clientRnc.ifBlank { "000000000" }
                        Dgii607SaleRecord(
                            customerTaxId = resolvedTaxId,
                            identificationType = DgiiIdentificationType.RNC,
                            ncfNumber = resolvedNcf,
                            invoiceDateYyyymmdd = todayYyyymmdd,
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
                }
            } else if (exported.csvContent.isNotBlank()) {
                val utf8Body = exported.csvContent.encodeToByteArray()
                byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()) + utf8Body
            } else {
                val activeItbisDivisor = 100 + businessSettings.value.itbisPercent.coerceAtLeast(0)
                val rows = payments.value.map { payment ->
                    val subtotal = ((payment.amountDop * 100) / activeItbisDivisor).toLong()
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

    public fun hasCompletedOrUsedRental(): Boolean = clientEventsForCurrentUser().any { eventItem ->
        val isCompleted = eventItem.status == EventStatus.COMPLETED
        val isInSetup = eventItem.status == EventStatus.IN_SETUP
        isCompleted || isInSetup
    }

    public fun submitCompanyReview(comment: String, rating: Int = 5): Boolean {
        if (!hasCompletedOrUsedRental()) {
            soundPlayer.playAlertError()
            _formError.value = "Solo puedes escribir una reseña después de alquilar o usar un producto."
            return false
        }
        val trimmedComment = comment.trim()
        if (trimmedComment.length < 6) {
            soundPlayer.playAlertError()
            _formError.value = "Escribe al menos 6 caracteres para publicar tu reseña."
            return false
        }
        val cleanRating = rating.coerceIn(1, 5)
        val authorName = _profileName.value.trim().ifBlank { "Cliente Rosa Fiesta" }
        val initialChar = InputValidator.extractInitials(authorName, fallback = "C", maxInitials = 1)
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

    public fun replyToSelectedReview(
        replyText: String,
        verified: Boolean = true,
    ): Boolean {
        val cleanReply = replyText.trim()
        if (cleanReply.length < 4) {
            postError("Escribe una respuesta de al menos 4 caracteres.")
            return false
        }
        val targetReview = selectedReview() ?: return false
        repository.replyToReview(targetReview.id, cleanReply, verified)
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
        _pickedAttachmentFile.value = null
    }

    public fun sendAdminDirectChatReply(rawReplyText: String, attachmentName: String = "") {
        val trimmedReply = rawReplyText.trim()
        val cleanAttachment = attachmentName.trim()
        val isBothEmpty = if (trimmedReply.isEmpty()) cleanAttachment.isEmpty() else false
        if (isBothEmpty) {
            postError("Escribe un mensaje o adjunta un archivo antes de enviar.")
            return
        }
        val replyValidationError = InputValidator.validateChatMessage(
            rawValue = trimmedReply,
            isRequired = cleanAttachment.isEmpty(),
        )
        if (replyValidationError != null) {
            postError(replyValidationError)
            return
        }
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

    /**
     * Creates a new catalog category in the backend and updates local article category choices.
     */
    public fun createCategory(
        name: String,
        illoKey: String = "mobiliario",
        onComplete: (Result<CatalogCategory>) -> Unit = {},
    ) {
        val cleanName = name.trim()
        val validationError = InputValidator.validateCategoryName(cleanName, isRequired = true)
        if (validationError != null) {
            soundPlayer.playAlertError()
            _formError.value = validationError
            onComplete(Result.failure(IllegalArgumentException(validationError)))
            return
        }
        if (_isSubmitting.value) return
        _isSubmitting.value = true
        securityScope.launch {
            try {
                val result = repository.createCategory(name = cleanName, illoKey = illoKey)
                if (result.isSuccess) {
                    val created = result.getOrNull()
                    if (created != null) {
                        if (_newArticleCategory.value.isBlank()) {
                            _newArticleCategory.value = created.name
                        }
                        _formFeedback.value = "Categoría \"${created.name}\" creada."
                    }
                    _formError.value = null
                    soundPlayer.playPaymentSuccess()
                } else {
                    val message = result.exceptionOrNull()?.message ?: "No se pudo crear la categoría."
                    _formError.value = message
                    soundPlayer.playAlertError()
                }
                onComplete(result)
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    /**
     * Updates an existing catalog category's display name and icon key.
     */
    public fun updateCategory(
        categoryId: String,
        name: String,
        illoKey: String = "mobiliario",
        onComplete: (Result<CatalogCategory>) -> Unit = {},
    ) {
        val cleanName = name.trim()
        val validationError = InputValidator.validateCategoryName(cleanName, isRequired = true)
        if (validationError != null) {
            soundPlayer.playAlertError()
            _formError.value = validationError
            onComplete(Result.failure(IllegalArgumentException(validationError)))
            return
        }
        if (_isSubmitting.value) return
        _isSubmitting.value = true
        val previousName = catalogCategories.value.firstOrNull { cat ->
            cat.id.equals(categoryId.trim(), ignoreCase = true)
        }?.name.orEmpty()
        securityScope.launch {
            try {
                val result = repository.updateCategory(
                    categoryId = categoryId,
                    name = cleanName,
                    illoKey = illoKey,
                )
                if (result.isSuccess) {
                    val updated = result.getOrNull()
                    if (updated != null) {
                        if (_newArticleCategory.value.equals(previousName, ignoreCase = true)) {
                            _newArticleCategory.value = updated.name
                        }
                        if (_selectedCategory.value.equals(previousName, ignoreCase = true)) {
                            _selectedCategory.value = updated.name
                        }
                        _formFeedback.value = "Categoría actualizada a \"${updated.name}\"."
                    }
                    _formError.value = null
                    soundPlayer.playPaymentSuccess()
                } else {
                    val message = result.exceptionOrNull()?.message ?: "No se pudo actualizar la categoría."
                    _formError.value = message
                    soundPlayer.playAlertError()
                }
                onComplete(result)
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    /**
     * Deletes an unused catalog category and refreshes catalog filters.
     */
    public fun deleteCategory(
        categoryId: String,
        onComplete: (Result<Unit>) -> Unit = {},
    ) {
        if (_isSubmitting.value) return
        _isSubmitting.value = true
        val targetName = catalogCategories.value.firstOrNull { cat ->
            cat.id.equals(categoryId.trim(), ignoreCase = true)
        }?.name.orEmpty()
        securityScope.launch {
            try {
                val result = repository.deleteCategory(categoryId)
                if (result.isSuccess) {
                    if (_newArticleCategory.value.equals(targetName, ignoreCase = true)) {
                        _newArticleCategory.value = catalogCategories.value.firstOrNull()?.name.orEmpty()
                    }
                    if (_selectedCategory.value.equals(targetName, ignoreCase = true)) {
                        _selectedCategory.value = "Todos"
                    }
                    _formError.value = null
                    _formFeedback.value = "Categoría \"$targetName\" eliminada."
                    soundPlayer.playTap()
                } else {
                    val message = result.exceptionOrNull()?.message ?: "No se pudo eliminar la categoría."
                    _formError.value = message
                    soundPlayer.playAlertError()
                }
                onComplete(result)
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    /**
     * Creates a seasonal trend card and reloads catalog feedback cleanly.
     */
    public fun createTrend(
        trend: CatalogTrend,
        onComplete: (Result<CatalogTrend>) -> Unit = {},
    ) {
        if (_isSubmitting.value) return
        _isSubmitting.value = true
        securityScope.launch {
            try {
                val result = repository.createTrend(trend)
                handleTrendMutationOutcome(
                    result = result,
                    successMessage = "Tendencia \"${result.getOrNull()?.title.orEmpty()}\" creada.",
                    fallbackError = "No se pudo crear la tendencia.",
                    onComplete = onComplete,
                )
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    /**
     * Updates an existing seasonal trend card.
     */
    public fun updateTrend(
        trend: CatalogTrend,
        onComplete: (Result<CatalogTrend>) -> Unit = {},
    ) {
        if (_isSubmitting.value) return
        _isSubmitting.value = true
        securityScope.launch {
            try {
                val result = repository.updateTrend(trend)
                handleTrendMutationOutcome(
                    result = result,
                    successMessage = "Tendencia \"${result.getOrNull()?.title.orEmpty()}\" actualizada.",
                    fallbackError = "No se pudo actualizar la tendencia.",
                    onComplete = onComplete,
                )
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    /**
     * Deletes a seasonal trend card by [trendId].
     */
    public fun deleteTrend(
        trendId: String,
        onComplete: (Result<Unit>) -> Unit = {},
    ) {
        if (_isSubmitting.value) return
        _isSubmitting.value = true
        val targetTitle = catalogTrends.value.firstOrNull { item ->
            item.id.equals(trendId.trim(), ignoreCase = true)
        }?.title.orEmpty()
        securityScope.launch {
            try {
                val result = repository.deleteTrend(trendId)
                if (result.isSuccess) {
                    _formError.value = null
                    _formFeedback.value = "Tendencia \"$targetTitle\" eliminada."
                    soundPlayer.playTap()
                } else {
                    _formError.value = result.exceptionOrNull()?.message ?: "No se pudo eliminar la tendencia."
                    soundPlayer.playAlertError()
                }
                onComplete(result)
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    /**
     * Saves the Personal Home seasonal hero banner configuration from Admin.
     */
    public fun saveSeasonBanner(
        banner: SeasonBanner,
        onComplete: (Result<SeasonBanner>) -> Unit = {},
    ) {
        if (_isSubmitting.value) return
        _isSubmitting.value = true
        securityScope.launch {
            try {
                val result = repository.saveSeasonBanner(banner)
                if (result.isSuccess) {
                    _formError.value = null
                    _formFeedback.value = "Banner de temporada actualizado."
                    soundPlayer.playPaymentSuccess()
                } else {
                    _formError.value = result.exceptionOrNull()?.message ?: "No se pudo guardar el banner."
                    soundPlayer.playAlertError()
                }
                onComplete(result)
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    private fun handleTrendMutationOutcome(
        result: Result<CatalogTrend>,
        successMessage: String,
        fallbackError: String,
        onComplete: (Result<CatalogTrend>) -> Unit,
    ) {
        if (result.isSuccess) {
            _formError.value = null
            _formFeedback.value = successMessage
            soundPlayer.playPaymentSuccess()
        } else {
            _formError.value = result.exceptionOrNull()?.message ?: fallbackError
            soundPlayer.playAlertError()
        }
        onComplete(result)
    }

    /**
     * Deletes an unused catalog article and clears any selection referencing it.
     */
    public fun deleteArticle(
        articleId: String,
        onComplete: (Result<Unit>) -> Unit = {},
    ) {
        if (_isSubmitting.value) return
        _isSubmitting.value = true
        val trimmedId = articleId.trim()
        val targetName = articles.value.firstOrNull { item ->
            item.id.equals(trimmedId, ignoreCase = true)
        }?.name.orEmpty()
        securityScope.launch {
            try {
                val result = repository.deleteArticle(trimmedId)
                if (result.isSuccess) {
                    if (_selectedArticleId.value.equals(trimmedId, ignoreCase = true)) {
                        _selectedArticleId.value = articles.value.firstOrNull()?.id.orEmpty()
                    }
                    if (_editingArticleId.value?.equals(trimmedId, ignoreCase = true) == true) {
                        _editingArticleId.value = null
                    }
                    _formError.value = null
                    _formFeedback.value = if (targetName.isNotEmpty()) {
                        "Artículo \"$targetName\" eliminado."
                    } else {
                        "Artículo eliminado."
                    }
                    soundPlayer.playTap()
                } else {
                    val message = result.exceptionOrNull()?.message ?: "No se pudo eliminar el artículo."
                    _formError.value = message
                    soundPlayer.playAlertError()
                }
                onComplete(result)
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    public fun deleteCurrentUserAccount(
        onComplete: (Boolean) -> Unit = {},
    ): Boolean {
        if (_isSubmitting.value) {
            onComplete(false)
            return false
        }
        if (activeClientEventCount() > 0) {
            postError("No puedes eliminar tu cuenta mientras tengas eventos activos pendientes.")
            onComplete(false)
            return false
        }
        _isSubmitting.value = true
        soundPlayer.playAlertError()
        securityScope.launch {
            try {
                val result = repository.deleteCurrentAccountOnline()
                if (result.isFailure) {
                    postError(result.exceptionOrNull()?.message ?: "No se pudo eliminar la cuenta en este momento.")
                    onComplete(false)
                    return@launch
                }
                repository.setRefreshToken(null)
                wipeUserScopedSecureStorage()
                resetUserScopedViewModelState()
                _formError.value = null
                _formFeedback.value = "Tu cuenta y sesión activa han sido eliminadas."
                onComplete(true)
            } finally {
                _isSubmitting.value = false
            }
        }
        return true
    }

    /**
     * Toggles the business-wide automatic WhatsApp event/payment alerts flag and persists it.
     */
    public fun toggleBusinessWhatsappAutoAlerts() {
        val current = businessSettings.value
        val next = current.copy(whatsappAutoAlertsEnabled = !current.whatsappAutoAlertsEnabled)
        saveBusinessConfig(next)
    }

    /**
     * Persists the user's choice to remember workspace entry selection (`Personal` vs `Admin`).
     */
    public fun saveRememberedEntryChoice(remember: Boolean) {
        _rememberEntryChoice.value = remember
        securityScope.launch {
            secureStorage.put(SecureStorage.KEY_REMEMBER_ENTRY_CHOICE, remember.toString())
        }
    }

    public fun saveBusinessConfig(updatedSettings: BusinessSettings? = null): Boolean {
        val dep = updatedSettings?.depositPercent ?: (_depositPercentInput.value.trim().toIntOrNull() ?: 0)
        val deliv = _deliveryBaseInput.value.trim().toIntOrNull() ?: 350
        val baseSettings = updatedSettings ?: businessSettings.value.copy(depositPercent = dep)
        val validationError = validateBusinessSettingsConfig(baseSettings, deliv)
        if (validationError != null) {
            soundPlayer.playAlertError()
            _formError.value = validationError
            return false
        }
        val normalizedSettings = normalizeBusinessSettingsConfig(baseSettings)
        repository.saveBusinessSettings(normalizedSettings)
        _depositPercentInput.value = normalizedSettings.depositPercent.toString()
        soundPlayer.playPaymentSuccess()
        _formError.value = null
        _formFeedback.value = "Ajustes del negocio guardados correctamente."
        if (hasValidAuthenticatedSession()) {
            securityScope.launch {
                val remoteResult = repository.saveBusinessSettingsOnline(normalizedSettings)
                if (remoteResult.isFailure) {
                    val err = remoteResult.exceptionOrNull()
                    postError(err?.message?.trim().orEmpty().ifBlank { "No se pudieron guardar los ajustes en el servidor." })
                }
            }
        }
        return true
    }

    public fun approveQuoteById(
        quoteId: String = _selectedQuoteId.value,
        signedByName: String,
        signatureSvg: String,
    ): QuoteRecord? {
        if (_isSubmitting.value) return null
        val cleanSigner = signedByName.trim()
        if (cleanSigner.length < 3) {
            postError("Escribe tu nombre completo para firmar el contrato.")
            return null
        }
        _isSubmitting.value = true
        try {
            val approved = repository.approveQuote(quoteId, cleanSigner, signatureSvg)
            if (approved != null) {
                soundPlayer.playPaymentSuccess()
                _formError.value = null
                _formFeedback.value = "Contrato firmado por $cleanSigner."
            }
            return approved
        } finally {
            _isSubmitting.value = false
        }
    }

    public fun declineQuoteById(
        quoteId: String = _selectedQuoteId.value,
        reason: String = "Solicita ajuste",
    ): QuoteRecord? {
        if (_isSubmitting.value) return null
        _isSubmitting.value = true
        try {
            val declined = repository.declineQuote(quoteId, reason)
            if (declined != null) {
                _formFeedback.value = "Cotización marcada como rechazada."
            }
            return declined
        } finally {
            _isSubmitting.value = false
        }
    }

    public fun duplicateQuoteById(quoteId: String): QuoteRecord? {
        val duplicated = repository.duplicateQuote(quoteId)
        if (duplicated != null) {
            _selectedQuoteId.value = duplicated.id
            _formFeedback.value = "Cotización duplicada (${duplicated.code})."
        }
        return duplicated
    }

    public fun approveAndSendQuoteFromAdmin(quoteId: String): QuoteRecord? {
        val sentQuote = repository.sendQuote(quoteId = quoteId)
        if (sentQuote != null) {
            _selectedQuoteId.value = sentQuote.id
            soundPlayer.playPaymentSuccess()
            _formError.value = null
            _formFeedback.value = "Cotización ${sentQuote.code} aprobada y enviada al cliente."
        }
        return sentQuote
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
        notes: String = "",
        existingQuoteId: String? = null,
    ): QuoteRecord? {
        if (lines.isEmpty()) {
            postError("Agrega al menos una línea a la cotización antes de enviarla.")
            return null
        }
        val cleanTitle = eventTitle.trim()
        val cleanClient = clientName.trim()
        val cleanEmail = clientEmail.trim()
        val cleanVenue = venue.trim()
        val cleanDate = dateLabel.trim()
        val deliveryError = if (deliveryFeeDop in 0..MAX_DELIVERY_FEE_DOP) {
            null
        } else {
            "La tarifa de envío debe estar entre RD$ 0 y RD$ $MAX_DELIVERY_FEE_DOP."
        }
        val discountError = if (discountDop >= 0) null else "El descuento no puede ser negativo."
        val validationError = firstNonBlankError(
            InputValidator.validateEventName(cleanTitle, isRequired = true),
            InputValidator.validateEntityName(
                rawValue = cleanClient,
                fieldLabel = "El nombre del cliente",
                minLength = InputValidator.MIN_NAME_LENGTH,
                maxLength = InputValidator.MAX_FULL_NAME_LENGTH,
                isRequired = true,
            ),
            InputValidator.validateOptionalEmail(cleanEmail),
            InputValidator.validateAddress(cleanVenue, fieldLabel = "El lugar", isRequired = true),
            validateEventDateOrSpanishLabel(cleanDate, allowPlaceholder = true),
            InputValidator.validateGuestCount(guests.toString(), isRequired = true),
            deliveryError,
            discountError,
        )
        if (validationError != null) {
            postError(validationError)
            return null
        }
        val cleanNotes = notes.trim()
        val cleanExistingId = existingQuoteId.orEmpty().trim()
        val persistedQuote = if (cleanExistingId.isNotEmpty()) {
            repository.sendQuote(
                quoteId = cleanExistingId,
                eventTitle = cleanTitle,
                venue = cleanVenue,
                dateLabel = cleanDate,
                guests = guests,
                lines = lines,
                deliveryFeeDop = deliveryFeeDop,
                discountDop = discountDop,
                notes = cleanNotes,
            )
        } else {
            null
        } ?: repository.createQuote(
            eventTitle = cleanTitle,
            clientName = cleanClient,
            clientEmail = cleanEmail.lowercase(),
            venue = cleanVenue,
            dateLabel = cleanDate,
            guests = guests,
            lines = lines,
            deliveryFeeDop = deliveryFeeDop,
            discountDop = discountDop,
            notes = cleanNotes,
            sendImmediately = true,
        )
        _selectedQuoteId.value = persistedQuote.id
        soundPlayer.playPaymentSuccess()
        _formError.value = null
        _formFeedback.value = "Cotización ${persistedQuote.code} aprobada y enviada a ${persistedQuote.clientName}."
        return persistedQuote
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
        val cleanSubtitle = subtitle.trim()
        val validationError = firstNonBlankError(
            InputValidator.validateEntityName(
                rawValue = cleanName,
                fieldLabel = "El nombre del paquete",
                minLength = 3,
                maxLength = InputValidator.MAX_PACKAGE_NAME_LENGTH,
                isRequired = true,
            ),
            InputValidator.validateNotes(
                rawValue = cleanSubtitle,
                fieldLabel = "El subtítulo",
                isRequired = false,
                maxLength = 120,
            ),
            InputValidator.validateGuestCount(suggestedGuests.toString(), isRequired = true),
            InputValidator.validateMoneyAmount(
                rawValue = priceDop.toString(),
                fieldLabel = "El precio del paquete",
                isRequired = true,
                minAmount = 1.0,
                maxAmount = InputValidator.MAX_MONEY_AMOUNT,
            ),
        )
        if (validationError != null) {
            postError(validationError)
            return null
        }
        val editingId = _editingPackageId.value
        val saved = if (editingId != null) {
            repository.updatePackage(
                packageId = editingId,
                name = cleanName,
                subtitle = cleanSubtitle,
                eventType = eventType.trim().ifBlank { "Evento" },
                suggestedGuests = suggestedGuests,
                priceDop = priceDop,
                includedItems = includedItems,
                visibleInCatalog = visibleInCatalog,
            )
        } else {
            repository.createPackage(
                name = cleanName,
                subtitle = cleanSubtitle,
                eventType = eventType.trim().ifBlank { "Evento" },
                suggestedGuests = suggestedGuests,
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
        val cleanName = fullName.trim()
        val cleanEmail = email.trim().lowercase()
        val cleanPhone = phone.trim()
        val cleanNotes = notes.trim()
        val validationError = firstNonBlankError(
            InputValidator.validateEntityName(
                rawValue = cleanName,
                fieldLabel = "El nombre del cliente",
                minLength = InputValidator.MIN_NAME_LENGTH,
                maxLength = InputValidator.MAX_FULL_NAME_LENGTH,
                isRequired = true,
            ),
            InputValidator.validateEmail(cleanEmail),
            InputValidator.validateOptionalPhone(cleanPhone),
            InputValidator.validateNotes(
                rawValue = cleanNotes,
                fieldLabel = "Las notas internas",
                isRequired = false,
                maxLength = InputValidator.MAX_NOTES_LENGTH,
            ),
        )
        if (validationError != null) {
            postError(validationError)
            return null
        }
        val formattedPhone = if (cleanPhone.isEmpty()) "" else InputValidator.formatPhoneDisplay(cleanPhone)
        val updated = repository.updateClientProfile(
            clientId = target.id,
            fullName = cleanName,
            email = cleanEmail,
            phone = formattedPhone,
            notes = cleanNotes,
        )
        if (updated != null) {
            soundPlayer.playPaymentSuccess()
            _formError.value = null
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
        initialPassword: String = "",
    ): StaffMember? {
        val cleanName = fullName.trim()
        val cleanEmail = email.trim().lowercase()
        val cleanPhone = phone.trim()
        val cleanPassword = initialPassword.trim()
        val passwordError = if (cleanPassword.isEmpty()) null else InputValidator.validatePassword(cleanPassword)
        val validationError = firstNonBlankError(
            InputValidator.validateFullName(cleanName),
            InputValidator.validateEmail(cleanEmail),
            InputValidator.validateOptionalPhone(cleanPhone),
            passwordError,
        )
        if (validationError != null) {
            postError(validationError)
            return null
        }
        val formattedPhone = if (cleanPhone.isEmpty()) "" else InputValidator.formatPhoneDisplay(cleanPhone)
        val invited = if (cleanPassword.isNotEmpty()) {
            repository.inviteStaffMemberWithPassword(
                fullName = cleanName,
                email = cleanEmail,
                roleLabel = roleLabel.trim().ifBlank { "Operaciones" },
                phone = formattedPhone,
                permissions = permissions,
                initialPassword = cleanPassword,
            )
        } else {
            repository.inviteStaffMember(
                fullName = cleanName,
                email = cleanEmail,
                roleLabel = roleLabel.trim().ifBlank { "Operaciones" },
                phone = formattedPhone,
                permissions = permissions,
            )
        }
        soundPlayer.playPaymentSuccess()
        _formError.value = null
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
        val cleanDate = dateLabel.trim().ifBlank { "Por confirmar" }
        val cleanNote = followUpNote.trim()
        val cleanPhone = phone.trim()
        val validationError = firstNonBlankError(
            InputValidator.validateEntityName(
                rawValue = cleanClient,
                fieldLabel = "El nombre del prospecto",
                minLength = InputValidator.MIN_NAME_LENGTH,
                maxLength = InputValidator.MAX_FULL_NAME_LENGTH,
                isRequired = true,
            ),
            validateEventDateOrSpanishLabel(cleanDate, allowPlaceholder = true),
            InputValidator.validateGuestCount(guests.toString(), isRequired = true),
            InputValidator.validateMoneyAmount(
                rawValue = estimatedDop.toString(),
                fieldLabel = "El presupuesto estimado",
                isRequired = true,
                minAmount = 1.0,
                maxAmount = InputValidator.MAX_MONEY_AMOUNT,
            ),
            InputValidator.validateNotes(
                rawValue = cleanNote,
                fieldLabel = "La nota de seguimiento",
                isRequired = false,
                maxLength = InputValidator.MAX_NOTES_LENGTH,
            ),
            InputValidator.validateOptionalPhone(cleanPhone),
        )
        if (validationError != null) {
            postError(validationError)
            return null
        }
        val formattedPhone = if (cleanPhone.isEmpty()) "" else InputValidator.formatPhoneDisplay(cleanPhone)
        val created = repository.createLead(
            clientName = cleanClient,
            eventType = eventType.trim().ifBlank { "Evento social" },
            dateLabel = cleanDate,
            guests = guests,
            estimatedDop = estimatedDop,
            channel = channel,
            followUpNote = cleanNote,
            phone = formattedPhone,
        )
        soundPlayer.playPaymentSuccess()
        _formError.value = null
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

    private fun firstNonBlankError(vararg errors: String?): String? = errors.firstOrNull { error -> error != null }

    private fun validateArticleCode(rawCode: String): String? {
        val trimmed = rawCode.trim()
        if (trimmed.isEmpty()) return "El código del artículo es obligatorio."
        if (trimmed.length < 2) return "El código del artículo debe tener al menos 2 caracteres."
        if (trimmed.length > 24) return "El código del artículo no puede exceder 24 caracteres."
        val hasInvalidCharacter = trimmed.any { character ->
            val isAllowedDash = character == '-' || character == '_'
            val isAllowed = character.isLetterOrDigit() || isAllowedDash
            !isAllowed
        }
        if (hasInvalidCharacter) {
            return "El código solo permite letras, números y guiones."
        }
        return null
    }

    private fun validateEventDateOrSpanishLabel(
        rawDate: String,
        allowPlaceholder: Boolean = false,
    ): String? {
        val trimmed = rawDate.trim()
        if (trimmed.isEmpty()) return "La fecha del evento es obligatoria."
        if (allowPlaceholder) {
            val lower = trimmed.lowercase()
            val isPlaceholder = lower == "por confirmar" || lower == "por definir"
            if (isPlaceholder) return null
        }
        val hasSlash = trimmed.contains('/')
        val allDigits = trimmed.all { character -> character.isDigit() }
        if (hasSlash) return InputValidator.validateEventDate(trimmed, isRequired = true)
        if (allDigits) return InputValidator.validateEventDate(trimmed, isRequired = true)
        val hasDigit = trimmed.any { character -> character.isDigit() }
        val hasLetter = trimmed.any { character -> character.isLetter() }
        val lowerText = trimmed.lowercase()
        val spanishMonths = listOf("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic")
        val hasSpanishMonth = spanishMonths.any { token -> lowerText.contains(token) }
        val isReadableLabel = allConditionsTrue(
            first = trimmed.length in 4..40,
            second = hasDigit,
            third = hasLetter,
            fourth = hasSpanishMonth,
        )
        if (isReadableLabel) return null
        return InputValidator.validateEventDate(trimmed, isRequired = true)
    }

    private fun validateBusinessSettingsConfig(
        settings: BusinessSettings,
        deliveryBaseDop: Int,
    ): String? {
        val depositError = if (settings.depositPercent in 10..90) {
            null
        } else {
            "El anticipo para reservar debe estar entre 10% y 90%."
        }
        val deliveryError = if (deliveryBaseDop in 0..MAX_DELIVERY_FEE_DOP) {
            null
        } else {
            "La tarifa base de envío debe estar entre RD$ 0 y RD$ $MAX_DELIVERY_FEE_DOP."
        }
        val baseError = firstNonBlankError(
            InputValidator.validateBusinessName(settings.businessName, isRequired = true),
            InputValidator.validateRnc(settings.rnc, isRequired = true),
            InputValidator.validateDominicanPhone(
                rawValue = settings.phone,
                isRequired = true,
                allowInternational = true,
                fieldLabel = "El teléfono",
            ),
            InputValidator.validateWhatsApp(
                rawValue = settings.whatsapp,
                isRequired = true,
                allowInternational = true,
            ),
            InputValidator.validateEmail(settings.email),
            InputValidator.validateAddress(settings.address, fieldLabel = "La dirección", isRequired = true),
            InputValidator.validateBusinessHours(settings.businessHours, isRequired = true),
            depositError,
            deliveryError,
        )
        if (baseError != null) return baseError
        for (zone in settings.zones) {
            val zoneNameError = InputValidator.validateEntityName(
                rawValue = zone.name,
                fieldLabel = "El nombre de la zona",
                minLength = 2,
                maxLength = 50,
                isRequired = true,
            )
            if (zoneNameError != null) return zoneNameError
            val isFeeInvalid = zone.feeDop !in 0..MAX_DELIVERY_FEE_DOP
            if (isFeeInvalid) {
                return "La tarifa de la zona ${zone.name} debe estar entre RD$ 0 y RD$ $MAX_DELIVERY_FEE_DOP."
            }
            val isEtaInvalid = zone.etaMinutes !in 5..240
            if (isEtaInvalid) {
                return "El tiempo estimado de ${zone.name} debe estar entre 5 y 240 minutos."
            }
        }
        return null
    }

    private fun normalizeBusinessSettingsConfig(settings: BusinessSettings): BusinessSettings {
        val formattedPhone = InputValidator.formatPhoneDisplay(settings.phone.trim())
        val formattedWhatsapp = InputValidator.formatPhoneDisplay(settings.whatsapp.trim())
        val formattedRnc = InputValidator.formatRncDisplay(settings.rnc.trim())
        val trimmedZones = settings.zones.map { zone ->
            zone.copy(
                name = zone.name.trim(),
                feeDop = zone.feeDop.coerceIn(0, MAX_DELIVERY_FEE_DOP),
                etaMinutes = zone.etaMinutes.coerceIn(5, 240),
            )
        }
        return settings.copy(
            businessName = settings.businessName.trim(),
            rnc = formattedRnc,
            address = settings.address.trim(),
            phone = formattedPhone,
            whatsapp = formattedWhatsapp,
            email = settings.email.trim().lowercase(),
            businessHours = settings.businessHours.trim(),
            zones = trimmedZones,
        )
    }

    public fun setNewArticlePhotoUrl(photoUrl: String) {
        _newArticlePhotoUrl.value = photoUrl.trim()
    }

    public fun setPaymentNcfType(ncfType: String) {
        val normalized = ncfType.trim().uppercase()
        _paymentNcfType.value = if (normalized == "B01") "B01" else "B02"
    }

    public fun setPaymentClientRnc(rnc: String) {
        _paymentClientRnc.value = InputValidator.filterDigitsOnly(rnc, 11)
    }

    public fun setPaymentVoucherUrl(url: String) {
        _paymentVoucherUrl.value = url.trim()
    }

    public fun canCurrentUserManageFinances(): Boolean {
        val roleKey = activeRole.value.trim().lowercase()
        val isFinanceRole = when (roleKey) {
            "admin", "finanzas" -> true
            else -> false
        }
        if (isFinanceRole) {
            return true
        }
        val currentUser = currentAuthenticatedUser.value
        val userRole = currentUser?.role?.trim()?.lowercase().orEmpty()
        val isAuthenticatedFinance = when (userRole) {
            "admin", "finanzas" -> true
            else -> false
        }
        if (isAuthenticatedFinance) {
            return true
        }
        val isStaff = roleKey.startsWith("staff_")
        if (isStaff) {
            val staffKey = roleKey.removePrefix("staff_")
            val matched = staffMembers.value.firstOrNull { member -> member.name.equals(staffKey, ignoreCase = true) }
            return matched?.canViewPayments ?: false
        }
        return false
    }

    private fun exceedsMediaCacheLimits(entryCount: Int, totalBytes: Long): Boolean {
        if (entryCount > MAX_MEDIA_CACHE_ENTRIES) {
            return true
        }
        return totalBytes > MAX_MEDIA_CACHE_BYTES
    }

    private fun putBoundedMediaCacheEntry(url: String, bytes: ByteArray) {
        val cleanUrl = url.trim()
        if (cleanUrl.isBlank()) {
            return
        }
        _mediaBytesCache.update { currentCache ->
            val ordered = linkedMapOf<String, ByteArray>()
            for ((existingKey, existingBytes) in currentCache) {
                if (existingKey != cleanUrl) {
                    ordered[existingKey] = existingBytes
                }
            }
            ordered[cleanUrl] = bytes
            var totalBytes = 0L
            for (entryBytes in ordered.values) {
                totalBytes += entryBytes.size.toLong()
            }
            val maxIterations = ordered.size + 1
            for (step in 0 until maxIterations) {
                if (!exceedsMediaCacheLimits(ordered.size, totalBytes)) {
                    break
                }
                val oldestKey = ordered.keys.firstOrNull() ?: break
                val removed = ordered.remove(oldestKey)
                if (removed != null) {
                    totalBytes -= removed.size.toLong()
                }
            }
            ordered.toMap()
        }
    }

    private fun touchBoundedMediaCacheEntry(url: String) {
        val currentCache = _mediaBytesCache.value
        val existingBytes = currentCache[url] ?: return
        val mostRecentKey = currentCache.keys.lastOrNull()
        if (mostRecentKey == url) {
            return
        }
        putBoundedMediaCacheEntry(url, existingBytes)
    }

    public fun ensureMediaLoaded(urlOrId: String) {
        val cleanUrl = urlOrId.trim()
        if (cleanUrl.isBlank()) {
            return
        }
        if (_mediaBytesCache.value.containsKey(cleanUrl)) {
            touchBoundedMediaCacheEntry(cleanUrl)
            return
        }
        securityScope.launch {
            var shouldFetch = false
            mediaFetchMutex.withLock {
                val cachedNow = _mediaBytesCache.value.containsKey(cleanUrl)
                val alreadyFetching = activeMediaFetchUrls.contains(cleanUrl)
                val canStartFetch = if (cachedNow) false else !alreadyFetching
                if (canStartFetch) {
                    activeMediaFetchUrls.add(cleanUrl)
                    shouldFetch = true
                }
            }
            if (!shouldFetch) {
                return@launch
            }
            try {
                repository.fetchMediaBytes(cleanUrl).onSuccess { decodedBytes ->
                    if (decodedBytes.isNotEmpty()) {
                        putBoundedMediaCacheEntry(cleanUrl, decodedBytes)
                    }
                }
            } finally {
                mediaFetchMutex.withLock {
                    activeMediaFetchUrls.remove(cleanUrl)
                }
            }
        }
    }

    public fun uploadAndAttachMedia(
        purpose: String,
        file: PickedMediaFile,
        onError: (String) -> Unit = {},
        onUploaded: (String) -> Unit = {},
    ) {
        if (_isSubmitting.value) return
        _isSubmitting.value = true
        securityScope.launch {
            try {
                val result = repository.uploadMedia(
                    filename = file.fileName,
                    mimeType = file.mimeType,
                    purpose = purpose,
                    bytes = file.bytes,
                )
                result.onSuccess { uploadedAsset ->
                    putBoundedMediaCacheEntry(uploadedAsset.url, file.bytes)
                    _formError.value = null
                    _formFeedback.value = "Archivo '${file.fileName}' subido correctamente."
                    onUploaded(uploadedAsset.url)
                }.onFailure { error ->
                    val errorMessage = error.message?.trim().orEmpty().ifBlank { "No se pudo subir '${file.fileName}'." }
                    _formError.value = errorMessage
                    soundPlayer.playAlertError()
                    onError(errorMessage)
                }
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    /**
     * Uploads montage setup photos in [files] for [eventId] and persists all resulting URLs onto the event.
     */
    public fun attachSetupPhotosToEvent(
        eventId: String = _selectedEventId.value,
        files: List<PickedMediaFile>,
        onComplete: (Boolean, List<String>) -> Unit = { _, _ -> },
    ) {
        val cleanEventId = eventId.trim()
        val hasMissingEvent = cleanEventId.isEmpty()
        val hasNoFiles = files.isEmpty()
        val isInvalidRequest = if (hasMissingEvent) true else hasNoFiles
        if (isInvalidRequest) {
            onComplete(false, emptyList())
            return
        }
        if (_isSubmitting.value) {
            onComplete(false, emptyList())
            return
        }
        val existingEvent = events.value.firstOrNull { event -> event.id == cleanEventId }
        val existingUrls = existingEvent?.setupPhotoUrls.orEmpty()
        _isSubmitting.value = true
        securityScope.launch {
            try {
                val uploadedUrls = mutableListOf<String>()
                var failedMessage: String? = null
                for (file in files) {
                    val uploadResult = repository.uploadMedia(
                        filename = file.fileName,
                        mimeType = file.mimeType,
                        purpose = "setup",
                        bytes = file.bytes,
                    )
                    val asset = uploadResult.getOrNull()
                    if (asset == null) {
                        failedMessage = uploadResult.exceptionOrNull()?.message?.trim().orEmpty()
                            .ifBlank { "No se pudo subir '${file.fileName}'." }
                        break
                    }
                    putBoundedMediaCacheEntry(asset.url, file.bytes)
                    uploadedUrls.add(asset.url)
                }
                if (failedMessage != null) {
                    _formError.value = failedMessage
                    soundPlayer.playAlertError()
                    onComplete(false, uploadedUrls)
                    return@launch
                }
                val mergedUrls = (existingUrls + uploadedUrls).distinct()
                val persistResult = repository.updateEventSetupPhotos(
                    eventId = cleanEventId,
                    setupPhotoUrls = mergedUrls,
                )
                persistResult.onSuccess { updatedEvent ->
                    _formError.value = null
                    _formFeedback.value = "Fotos de montaje guardadas en ${updatedEvent.code}."
                    soundPlayer.playPaymentSuccess()
                    onComplete(true, updatedEvent.setupPhotoUrls)
                }.onFailure { err ->
                    val errText = err.message?.trim().orEmpty()
                        .ifBlank { "No se pudieron asociar las fotos al evento." }
                    _formError.value = errText
                    soundPlayer.playAlertError()
                    onComplete(false, uploadedUrls)
                }
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    /**
     * Persists a pre-resolved list of montage [photoUrls] onto [eventId].
     */
    public fun saveEventSetupPhotoUrls(
        eventId: String = _selectedEventId.value,
        photoUrls: List<String>,
        onComplete: (Boolean) -> Unit = {},
    ) {
        val cleanEventId = eventId.trim()
        if (cleanEventId.isEmpty()) {
            onComplete(false)
            return
        }
        val normalizedUrls = photoUrls.map { url -> url.trim() }.filter { url -> url.isNotBlank() }.distinct()
        _isSubmitting.value = true
        securityScope.launch {
            try {
                val persistResult = repository.updateEventSetupPhotos(
                    eventId = cleanEventId,
                    setupPhotoUrls = normalizedUrls,
                )
                persistResult.onSuccess { updatedEvent ->
                    _formError.value = null
                    _formFeedback.value = "Fotos de montaje actualizadas en ${updatedEvent.code}."
                    soundPlayer.playPaymentSuccess()
                    onComplete(true)
                }.onFailure { err ->
                    _formError.value = err.message?.trim().orEmpty()
                        .ifBlank { "No se pudieron guardar las fotos de montaje." }
                    soundPlayer.playAlertError()
                    onComplete(false)
                }
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    /**
     * Computes real period-scoped income, expenses, monthly series, and event volume for `s57`.
     */
    public fun computePeriodSpendingAnalytics(
        periodLabel: String,
        nowEpochSeconds: Long = JwtAuthManager.currentEpochSeconds(),
    ): PeriodSpendingAnalytics = SpendingAnalyticsCalculator.compute(
        periodLabel = periodLabel,
        analyticsSummary = _analyticsSummary.value,
        events = events.value,
        payments = payments.value,
        refunds = refundRequests.value,
        damageIncidents = damageIncidents.value,
        nowEpochSeconds = nowEpochSeconds,
    )

    public fun submitDetailedPaymentFlow(
        eventId: String = _selectedEventId.value,
        amountDop: Int,
        methodLabel: String,
        bankName: String = "",
        referenceCode: String = "",
        voucherUrl: String = _paymentVoucherUrl.value,
        ncfType: String = _paymentNcfType.value,
        clientRnc: String = _paymentClientRnc.value,
        kindLabel: String = "Abono",
        contactPhone: String = "",
    ): PaymentRecord? {
        if (_isSubmitting.value) return null
        val normalizedNcf = if (ncfType.trim().uppercase() == "B01") "B01" else "B02"
        val cleanRnc = InputValidator.filterDigitsOnly(clientRnc, 11)
        val resolvedPhone = contactPhone.trim().ifBlank { _profilePhone.value.trim() }
        if (normalizedNcf == "B01") {
            val isValidRnc = isValidDominicanTaxId(cleanRnc)
            if (!isValidRnc) {
                soundPlayer.playAlertError()
                _formError.value = "Para comprobante B01 debes ingresar un RNC (9 dígitos) o Cédula (11 dígitos) válido."
                return null
            }
        }
        _isSubmitting.value = true
        try {
            val recorded = repository.registerDetailedPayment(
                eventId = eventId,
                amountDop = amountDop,
                methodLabel = methodLabel,
                referenceCode = referenceCode.trim(),
                voucherUrl = voucherUrl.trim(),
                ncfType = normalizedNcf,
                clientRnc = cleanRnc,
                bankName = bankName.trim(),
                kindLabel = kindLabel,
                contactPhone = resolvedPhone,
            )
            if (recorded != null) {
                soundPlayer.playPaymentSuccess()
                _paymentVoucherUrl.value = ""
                _formError.value = null
                val hasRejectionReason = recorded.rejectionReason.isNotBlank()
                val isPendingVerification = if (hasRejectionReason) false else !recorded.verified
                val pendingSuffix = if (isPendingVerification) {
                    " · En revisión de finanzas"
                } else {
                    ""
                }
                _formFeedback.value = "Pago registrado (${recorded.receiptNumber})$pendingSuffix."
            }
            return recorded
        } finally {
            _isSubmitting.value = false
        }
    }

    public fun startCardCheckoutSession(
        eventId: String = _selectedEventId.value,
        amountDop: Int,
        ncfType: String = _paymentNcfType.value,
        clientRnc: String = _paymentClientRnc.value,
        contactPhone: String = "",
        onSessionReady: (CardSessionResult) -> Unit = {},
        onUnavailableOrError: (String) -> Unit = {},
    ) {
        if (_isSubmitting.value) return
        val normalizedNcf = if (ncfType.trim().uppercase() == "B01") "B01" else "B02"
        val cleanRnc = InputValidator.filterDigitsOnly(clientRnc, 11)
        val resolvedPhone = contactPhone.trim().ifBlank { _profilePhone.value.trim() }
        if (normalizedNcf == "B01") {
            val isValidRnc = isValidDominicanTaxId(cleanRnc)
            if (!isValidRnc) {
                val errorText = "Para comprobante B01 debes ingresar un RNC (9 dígitos) o Cédula (11 dígitos) válido."
                _formError.value = errorText
                soundPlayer.playAlertError()
                onUnavailableOrError(errorText)
                return
            }
        }
        _isSubmitting.value = true
        securityScope.launch {
            try {
                val result = repository.createCardPaymentSession(
                    eventId = eventId,
                    amountDop = amountDop,
                    ncfType = normalizedNcf,
                    clientRnc = cleanRnc,
                    contactPhone = resolvedPhone,
                )
                result.onSuccess { session ->
                    _pendingCardCheckoutSession.value = session
                    _formError.value = null
                    _formFeedback.value = "Abriendo pasarela segura de tarjeta..."
                    platformActions.openExternalUrl(session.checkoutUrl)
                    onSessionReady(session)
                }.onFailure { error ->
                    val message = error.message?.trim().orEmpty().ifBlank {
                        "La pasarela de pago con tarjeta no está configurada. Usa transferencia bancaria con comprobante."
                    }
                    _formError.value = message
                    soundPlayer.playAlertError()
                    onUnavailableOrError(message)
                }
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    /**
     * Handles a deep link (`rosafiesta://payment/success?session_id=...` or `/cancel`) returned from Stripe Checkout.
     */
    public fun handleExternalPaymentDeepLink(rawUrl: String): Boolean {
        val parsed = ExternalPaymentCallbackBridge.parsePaymentReturnUrl(rawUrl) ?: return false
        handleExternalPaymentReturn(parsed)
        return true
    }

    private fun handleExternalPaymentReturn(paymentReturn: ExternalPaymentReturn) {
        if (paymentReturn.cancelled) {
            _pendingCardCheckoutSession.value = null
            _formError.value = null
            _formFeedback.value = "Pago con tarjeta cancelado. No se realizó ningún cargo."
            return
        }
        val sessionId = paymentReturn.sessionId.ifBlank { _pendingCardCheckoutSession.value?.sessionId.orEmpty() }
        if (sessionId.isNotBlank()) {
            confirmPendingCardCheckout(sessionId = sessionId)
        }
    }

    /**
     * Clears any pending Stripe card checkout session indicator.
     */
    public fun clearPendingCardCheckoutSession() {
        _pendingCardCheckoutSession.value = null
    }

    /**
     * Verifies and records a completed card checkout session against the backend payment confirmation endpoint.
     */
    public fun confirmPendingCardCheckout(
        sessionId: String = _pendingCardCheckoutSession.value?.sessionId.orEmpty(),
        onConfirmed: (PaymentRecord) -> Unit = {},
        onError: (String) -> Unit = {},
    ) {
        val cleanSessionId = sessionId.trim()
        if (cleanSessionId.isEmpty()) {
            val errorText = "No hay ninguna sesión de pago con tarjeta pendiente por verificar."
            postError(errorText)
            onError(errorText)
            return
        }
        if (_isSubmitting.value) return
        _isSubmitting.value = true
        _formError.value = null
        securityScope.launch {
            try {
                val outcome = repository.confirmCardCheckoutSession(cleanSessionId)
                outcome.onSuccess { recorded ->
                    _pendingCardCheckoutSession.value = null
                    soundPlayer.playPaymentSuccess()
                    _formError.value = null
                    _formFeedback.value = "Pago con tarjeta verificado (${recorded.receiptNumber})."
                    onConfirmed(recorded)
                }.onFailure { error ->
                    val message = error.message?.trim().orEmpty().ifBlank {
                        "El pago con tarjeta aún no figura confirmado. Si ya pagaste en Stripe, intenta verificar de nuevo en unos segundos."
                    }
                    postError(message)
                    onError(message)
                }
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    public fun verifyPendingPayment(paymentId: String, notes: String = "") {
        if (_isSubmitting.value) return
        if (!canCurrentUserManageFinances()) {
            postError("Tu rol no tiene permisos de finanzas para verificar transferencias.")
            return
        }
        _isSubmitting.value = true
        securityScope.launch {
            try {
                val result = repository.verifyPayment(paymentId = paymentId, notes = notes.trim())
                result.onSuccess { verified ->
                    soundPlayer.playPaymentSuccess()
                    _formError.value = null
                    _formFeedback.value = "Transferencia ${verified.receiptNumber} verificada (NCF ${verified.ncfNumber.ifBlank { verified.ncfType }})."
                }.onFailure { error ->
                    postError(error.message ?: "No se pudo verificar el pago.")
                }
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    public fun rejectPendingPayment(paymentId: String, reason: String) {
        if (_isSubmitting.value) return
        if (!canCurrentUserManageFinances()) {
            postError("Tu rol no tiene permisos de finanzas para rechazar transferencias.")
            return
        }
        val cleanReason = reason.trim()
        if (cleanReason.length < 4) {
            postError("Indica el motivo del rechazo (mínimo 4 caracteres).")
            return
        }
        _isSubmitting.value = true
        securityScope.launch {
            try {
                val result = repository.rejectPayment(paymentId = paymentId, reason = cleanReason)
                result.onSuccess { rejected ->
                    soundPlayer.playAlertError()
                    _formError.value = null
                    _formFeedback.value = "Pago ${rejected.receiptNumber} marcado como rechazado."
                }.onFailure { error ->
                    postError(error.message ?: "No se pudo rechazar el pago.")
                }
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    public fun checkDraftAvailabilityForDate(
        dateText: String,
        excludeEventId: String = "",
    ) {
        val cleanDate = dateText.trim()
        if (cleanDate.isBlank()) {
            _dateAvailabilityWarning.value = null
            return
        }
        val activeLines = draftLineItems()
        val checkTargets: List<Pair<String, Int>> = if (activeLines.isNotEmpty()) {
            activeLines.mapNotNull { line ->
                val resolvedId = articles.value.firstOrNull { item -> item.code == line.articleCode }?.id
                    ?: return@mapNotNull null
                resolvedId to line.quantity.coerceAtLeast(1)
            }
        } else {
            val fallbackId = articles.value.firstOrNull()?.id ?: return
            listOf(fallbackId to 1)
        }
        if (checkTargets.isEmpty()) {
            _dateAvailabilityWarning.value = null
            return
        }
        securityScope.launch {
            val conflictMessages = mutableListOf<String>()
            for ((targetArticleId, requestedQty) in checkTargets) {
                val result = repository.checkRangeAvailability(
                    date = cleanDate,
                    articleId = targetArticleId,
                    quantity = requestedQty,
                    excludeEventId = excludeEventId,
                )
                result.onSuccess { check ->
                    val hasConflict = if (!check.available) check.conflicts.isNotEmpty() else false
                    if (hasConflict) {
                        conflictMessages.add(check.conflicts.joinToString("; "))
                    }
                }
            }
            if (conflictMessages.isNotEmpty()) {
                _dateAvailabilityWarning.value = "Conflicto de agenda (+/- 1 día montaje/retorno): ${conflictMessages.joinToString("; ")}"
            } else {
                _dateAvailabilityWarning.value = null
            }
        }
    }

    public fun reportEventDamageWithPhoto(
        eventId: String,
        articleCode: String,
        articleName: String,
        damagedUnits: Int,
        missingUnits: Int,
        replacementDop: Int,
        notes: String,
        photoUrl: String,
    ): DamageIncident {
        val created = repository.reportDamageWithMedia(
            eventId = eventId,
            articleCode = articleCode,
            articleName = articleName,
            damagedUnits = damagedUnits.coerceAtLeast(0),
            missingUnits = missingUnits.coerceAtLeast(0),
            replacementDop = replacementDop.coerceAtLeast(0),
            notes = notes.trim(),
            photoUrl = photoUrl.trim(),
        )
        soundPlayer.playChecklistTick()
        _formFeedback.value = "Incidencia registrada con evidencia para $articleName."
        return created
    }

    public fun resolveDamageIncidentById(incidentId: String, notes: String = "Reparado y liberado a almacén") {
        securityScope.launch {
            val result = repository.resolveDamageIncident(incidentId = incidentId, notes = notes.trim())
            result.onSuccess { resolved ->
                soundPlayer.playPaymentSuccess()
                _formError.value = null
                _formFeedback.value = "Cuarentena liberada para ${resolved.articleName}."
            }.onFailure { error ->
                postError(error.message ?: "No se pudo cerrar la incidencia.")
            }
        }
    }

    public fun completeCloseoutWithServer(eventId: String, notes: String = "") {
        securityScope.launch {
            val result = repository.completeEventCloseout(eventId = eventId, notes = notes.trim())
            result.onSuccess { closedEvent ->
                soundPlayer.playPaymentSuccess()
                _formError.value = null
                _formFeedback.value = "Cierre completado para ${closedEvent.title} · Stock liberado."
            }.onFailure { error ->
                postError(error.message ?: "No se pudo completar el cierre del evento en el servidor.")
            }
        }
    }

    public fun sendBotThreadReply(
        threadId: String,
        phone: String,
        message: String,
        onSent: () -> Unit = {},
    ) {
        val cleanMsg = message.trim()
        if (cleanMsg.isEmpty()) {
            postError("Escribe el mensaje antes de enviar por WhatsApp.")
            return
        }
        securityScope.launch {
            val result = repository.sendWhatsAppBotReply(
                threadId = threadId,
                phone = phone.trim(),
                message = cleanMsg,
            )
            result.onSuccess {
                soundPlayer.playRosaSend()
                _formError.value = null
                _formFeedback.value = "Mensaje enviado por WhatsApp."
                onSent()
            }.onFailure { error ->
                postError(error.message ?: "WhatsApp Cloud API no está configurado en el servidor.")
            }
        }
    }

    private fun findArticleIdByCodeOrId(code: String, fallbackId: String): String? {
        val matched = articles.value.firstOrNull { item ->
            val matchesCode = item.code == code
            val matchesId = item.id == fallbackId
            if (matchesCode) true else matchesId
        }
        return matched?.id
    }

    /**
     * Loads OAuth provider readiness configuration from the backend.
     */
    public fun loadOAuthConfig() {
        securityScope.launch {
            repository.fetchOAuthConfig().onSuccess { config ->
                _oauthConfig.value = config
                val hasPushKey = config.webPushVapidPublicKey.isNotBlank()
                val shouldSyncWebPush = if (hasPushKey) hasValidAuthenticatedSession() else false
                if (shouldSyncWebPush) {
                    val user = currentAuthenticatedUser.value
                    val pushEnabled = user?.preferences?.pushNotificationsEnabled ?: false
                    if (pushEnabled) {
                        syncPushTokenRegistrationForRole(true, user.role)
                    }
                }
            }
        }
    }

    /**
     * Triggers privacy-safe username recovery for the supplied email or phone identifier.
     */
    public fun recoverUsername(
        identifier: String,
        onComplete: (Boolean, String) -> Unit = { _, _ -> },
    ) {
        val cleanIdentifier = identifier.trim()
        if (cleanIdentifier.length < 3) {
            val errorMsg = "Ingresa un correo, teléfono o referencia válida (mínimo 3 caracteres)."
            postError(errorMsg)
            onComplete(false, errorMsg)
            return
        }
        securityScope.launch {
            val result = repository.recoverUsernameByIdentifier(cleanIdentifier)
            result.onSuccess { serverMessage ->
                soundPlayer.playTap()
                _formError.value = null
                val finalMessage = serverMessage.ifBlank {
                    "Si existe una cuenta asociada, te enviamos un correo con el usuario registrado."
                }
                _formFeedback.value = finalMessage
                onComplete(true, finalMessage)
            }.onFailure { error ->
                val msg = error.message?.trim().orEmpty().ifBlank {
                    "No se pudo procesar la recuperación de usuario en este momento."
                }
                postError(msg)
                onComplete(false, msg)
            }
        }
    }

    /**
     * Sends a live balance payment reminder to the event client via the configured channel.
     */
    public fun sendEventPaymentReminder(
        eventId: String,
        channel: String = "whatsapp",
        message: String = "",
        onComplete: (Boolean, PaymentReminderOutcome?) -> Unit = { _, _ -> },
    ) {
        if (eventId.isBlank()) {
            postError("Selecciona un evento válido para enviar el recordatorio.")
            onComplete(false, null)
            return
        }
        securityScope.launch {
            val result = repository.sendEventPaymentReminder(
                eventId = eventId,
                channel = channel,
                message = message,
            )
            result.onSuccess { outcome ->
                soundPlayer.playRosaSend()
                _formError.value = null
                val recipientLabel = outcome.event.clientName.ifBlank { "cliente" }
                _formFeedback.value = if (outcome.sent) {
                    "Recordatorio (${outcome.channel}) enviado a $recipientLabel."
                } else {
                    "Recordatorio (${outcome.channel}) registrado para $recipientLabel."
                }
                onComplete(true, outcome)
            }.onFailure { error ->
                val msg = error.message?.trim().orEmpty().ifBlank {
                    "No se pudo enviar el recordatorio de pago."
                }
                postError(msg)
                onComplete(false, null)
            }
        }
    }

    /**
     * Loads real aggregated analytics for reports and charts across the requested range (`30d`, `90d`, `180d`, `365d`, `all`).
     */
    public fun loadAnalytics(range: String = "30d") {
        securityScope.launch {
            repository.fetchPaymentsAnalytics(range).onSuccess { summary ->
                _analyticsSummary.value = summary
            }
        }
    }

    /**
     * Requests a Rosa IA floorplan sketch arrangement from the backend quote engine.
     */
    public fun requestRosaSketch(
        prompt: String,
        eventType: String = _rosaEventType.value,
        guests: Int = _rosaGuestsCount.value,
        onResult: (RosaSketchProposal?) -> Unit,
    ) {
        securityScope.launch {
            val result = repository.requestRosaSketch(
                prompt = prompt,
                eventType = eventType,
                guests = guests.coerceAtLeast(16),
            )
            result.onSuccess { proposal ->
                val tablesCount = proposal.elements.count { item ->
                    val normalizedKind = item.kind.lowercase()
                    normalizedKind.contains("table")
                }
                if (tablesCount > 0) {
                    _sketchTableCount.value = tablesCount.coerceIn(2, 12)
                }
                soundPlayer.playRosaReply()
                _formError.value = null
                onResult(proposal)
            }.onFailure { error ->
                val msg = error.message?.trim().orEmpty().ifBlank {
                    "No se pudo conectar con Rosa IA para generar el boceto."
                }
                postError(msg)
                onResult(null)
            }
        }
    }

    /**
     * Merges suggested catalog item quantities into an event (or draft cart when no persisted event matches) and syncs with the backend.
     */
    public fun applySuggestedItemsToEvent(
        eventId: String,
        additionalQuantities: Map<String, Int>,
        feedbackMessage: String,
        onComplete: (Boolean) -> Unit = {},
    ) {
        if (additionalQuantities.isEmpty()) {
            onComplete(false)
            return
        }
        val targetEvent = events.value.firstOrNull { candidate -> candidate.id == eventId }
        if (targetEvent == null) {
            additionalQuantities.forEach { (articleId, qtyDelta) ->
                adjustDraftQuantity(articleId, qtyDelta)
            }
            _formFeedback.value = feedbackMessage
            onComplete(true)
            return
        }
        val mergedItems = mergeAdditionalEventItems(
            existingItems = targetEvent.items,
            additionalQuantities = additionalQuantities,
        )
        securityScope.launch {
            val updatedResult = repository.updateEventItems(eventId = targetEvent.id, items = mergedItems)
            updatedResult.onSuccess {
                soundPlayer.playCartAdd()
                _formError.value = null
                _formFeedback.value = feedbackMessage
                onComplete(true)
            }.onFailure { error ->
                postError(error.message ?: "No se pudieron agregar los artículos sugeridos al evento.")
                onComplete(false)
            }
        }
    }

    private fun mergeAdditionalEventItems(
        existingItems: List<EventLineItem>,
        additionalQuantities: Map<String, Int>,
    ): List<EventLineItem> {
        val mergedByCode = linkedMapOf<String, EventLineItem>()
        for (existing in existingItems) {
            mergedByCode[existing.articleCode] = existing
        }
        val catalogById = articles.value.associateBy { article -> article.id }
        val catalogByCode = articles.value.associateBy { article -> article.code }
        for ((articleKey, deltaQty) in additionalQuantities) {
            if (deltaQty <= 0) continue
            val catalogArticle = catalogById[articleKey] ?: catalogByCode[articleKey] ?: continue
            val previous = mergedByCode[catalogArticle.code]
            if (previous != null) {
                val nextQty = (previous.quantity + deltaQty).coerceAtMost(catalogArticle.totalStock)
                mergedByCode[catalogArticle.code] = previous.copy(quantity = nextQty)
            } else {
                val cappedQty = deltaQty.coerceAtMost(catalogArticle.totalStock.coerceAtLeast(1))
                mergedByCode[catalogArticle.code] = EventLineItem(
                    articleCode = catalogArticle.code,
                    name = catalogArticle.name,
                    quantity = cappedQty,
                    unitPriceDop = catalogArticle.pricePerDayDop,
                    photo = catalogArticle.photo,
                )
            }
        }
        return mergedByCode.values.toList()
    }

    public fun syncDraftQuantitiesToSketch(eventId: String = _selectedEventId.value) {
        val currentMap = _draftQuantities.value
        val roundTables = sumDraftQtyMatching(currentMap, articles.value, "redonda", "RF-002", "art-mesa-redonda")
        val rectTables = sumDraftQtyMatching(currentMap, articles.value, "rectangular", "RF-003", "art-mesa-plegable")
        val chairs = sumDraftQtyMatching(currentMap, articles.value, "silla", "RF-001", "art-silla-tiffany")
        val arch = sumDraftQtyMatching(currentMap, articles.value, "arco", "RF-005", "art-arco-rosa")
        val elements = mutableListOf<String>()
        var serial = 1
        repeat(roundTables.coerceAtMost(12)) { index ->
            val col = (index % 3) * 0.25f + 0.25f
            val row = (index / 3) * 0.20f + 0.30f
            elements.add("ROUND_TABLE|el-sync-$serial|${col.coerceIn(0.12f, 0.88f)}|${row.coerceIn(0.18f, 0.85f)}|0xFF865DEC|Mesa ${index + 1}")
            serial += 1
        }
        repeat(rectTables.coerceAtMost(8)) { index ->
            val col = (index % 2) * 0.35f + 0.32f
            val row = 0.72f
            elements.add("RECT_TABLE|el-sync-$serial|$col|$row|0xFF1FBDC0|Mesa Rect ${index + 1}")
            serial += 1
        }
        if (arch > 0) {
            elements.add("ARCH_STAGE|el-sync-$serial|0.50|0.12|0xFFEA4EA9|Tarima")
            serial += 1
        }
        if (chairs > 0) {
            val chairGroups = ((chairs + 5) / 6).coerceAtMost(16)
            repeat(chairGroups) { index ->
                val col = (index % 4) * 0.20f + 0.20f
                val row = 0.55f + (index / 4) * 0.12f
                elements.add("CHAIR_GROUP|el-sync-$serial|${col.coerceIn(0.12f, 0.88f)}|${row.coerceIn(0.15f, 0.88f)}|0xFF8DB52C|Grupo Sillas")
                serial += 1
            }
        }
        if (elements.isNotEmpty()) {
            val payload = elements.joinToString("\n")
            saveSketchLayoutJson(payload, eventId)
        }
    }

    public fun isValidDominicanTaxId(cleanDigits: String): Boolean = when (cleanDigits.length) {
        9, 11 -> true
        else -> false
    }

    public fun approveSelectedRefundWithOptions(options: RefundApprovalOptions): RefundRequest? {
        if (_isSubmitting.value) return null
        val target = selectedRefund() ?: return null
        _isSubmitting.value = true
        try {
            val approved = repository.approveRefundWithOptions(target.id, options)
            if (approved != null) {
                soundPlayer.playPaymentSuccess()
                _formFeedback.value = "Reembolso de ${RosaFiestaEngine.formatDop(approved.refundAmountDop)} aprobado para ${approved.clientName}."
            }
            return approved
        } finally {
            _isSubmitting.value = false
        }
    }

    public fun selectSupplierForEdit(supplierId: String?) {
        _editingSupplierId.value = supplierId?.trim()?.takeIf { id -> id.isNotEmpty() }
    }

    public fun editSupplier(
        supplierId: String,
        name: String,
        category: String,
        phone: String,
        contactName: String = "",
        email: String = "",
        website: String = "",
        notes: String = "",
    ): Boolean {
        val updated = repository.updateSupplier(
            supplierId = supplierId,
            name = name,
            category = category,
            contactName = contactName,
            phone = phone,
            email = email,
            website = website,
            notes = notes,
        ) ?: return false
        _suppliersList.update { list ->
            list.map { sup ->
                if (sup.id == supplierId) {
                    sup.copy(
                        name = updated.name,
                        category = updated.category,
                        phone = updated.phone,
                        statusLabel = notes.ifBlank { sup.statusLabel },
                    )
                } else {
                    sup
                }
            }
        }
        val snapshot = _suppliersList.value
        securityScope.launch {
            secureStorage.put(SecureStorage.KEY_SUPPLIERS, serializeSuppliersForStorage(snapshot))
        }
        soundPlayer.playPaymentSuccess()
        _formFeedback.value = "Proveedor '${updated.name}' actualizado."
        return true
    }

    public fun removeSupplier(supplierId: String): Boolean {
        val deleted = repository.deleteSupplier(supplierId)
        if (!deleted) return false
        _suppliersList.update { list -> list.filterNot { item -> item.id == supplierId } }
        val snapshot = _suppliersList.value
        securityScope.launch {
            secureStorage.put(SecureStorage.KEY_SUPPLIERS, serializeSuppliersForStorage(snapshot))
        }
        soundPlayer.playTap()
        _formFeedback.value = "Proveedor eliminado del directorio."
        return true
    }

    public fun deleteBotFaq(faqId: String): Boolean {
        val removed = repository.deleteBotFaq(faqId)
        if (removed) {
            soundPlayer.playTap()
            _formFeedback.value = "Pregunta frecuente eliminada."
        }
        return removed
    }

    public fun deleteBotKeyword(keywordId: String): Boolean {
        val removed = repository.deleteBotKeyword(keywordId)
        if (removed) {
            soundPlayer.playTap()
            _formFeedback.value = "Palabra clave eliminada."
        }
        return removed
    }

    public fun deleteStaffMemberById(staffId: String): Boolean {
        val removed = repository.deleteStaffMember(staffId)
        if (removed) {
            soundPlayer.playTap()
            _formFeedback.value = "Integrante eliminado del equipo."
        }
        return removed
    }

    public fun deletePackageById(packageId: String): Boolean {
        val removed = repository.deletePackage(packageId)
        if (removed) {
            soundPlayer.playTap()
            _formFeedback.value = "Paquete eliminado del catálogo."
        }
        return removed
    }

    public fun downloadContractPdf(quoteId: String) {
        securityScope.launch {
            val result = repository.downloadQuoteContractPdf(quoteId)
            result.onSuccess { pdfBytes ->
                val filename = "contrato-${quoteId.trim()}.pdf"
                val savedPath = platformActions.saveDocument(filename, "application/pdf", pdfBytes)
                val shared = platformActions.shareContent(
                    title = "Contrato RosaFiesta",
                    text = "Contrato de cotización $quoteId",
                    fileName = filename,
                    fileBytes = pdfBytes,
                    mimeType = "application/pdf",
                )
                if (savedPath != null) {
                    _formFeedback.value = "Contrato PDF descargado: $filename"
                } else if (shared) {
                    _formFeedback.value = "Contrato PDF compartido: $filename"
                } else {
                    _formFeedback.value = "Contrato PDF listo ($filename)."
                }
                soundPlayer.playPaymentSuccess()
            }.onFailure { error ->
                postError(error.message ?: "No se pudo descargar el contrato PDF.")
            }
        }
    }

    private fun findBestArticleMatch(
        catalog: List<CatalogArticle>,
        description: String,
    ): CatalogArticle? {
        val cleanDesc = description.trim().lowercase()
        val tokens = cleanDesc.split(" ", "-", "/").filter { tok -> tok.length >= 4 && !tok.all { ch -> ch.isDigit() } }
        val byExactNameOrToken = catalog.firstOrNull { candidate ->
            val nameLower = candidate.name.lowercase()
            val codeLower = candidate.code.lowercase()
            val matchesCode = codeLower.length >= 3 && cleanDesc.contains(codeLower)
            if (matchesCode) {
                true
            } else {
                tokens.any { token -> nameLower.contains(token) }
            }
        }
        if (byExactNameOrToken != null) return byExactNameOrToken
        return catalog.firstOrNull { candidate ->
            when {
                cleanDesc.contains("silla") -> candidate.name.contains("silla", ignoreCase = true) || candidate.id == "art-silla-tiffany"
                cleanDesc.contains("redonda") -> candidate.name.contains("redonda", ignoreCase = true) || candidate.id == "art-mesa-redonda"
                cleanDesc.contains("rectangular") -> candidate.name.contains("rectangular", ignoreCase = true) || candidate.id == "art-mesa-plegable"
                cleanDesc.contains("centro") -> candidate.name.contains("centro", ignoreCase = true) || candidate.id == "art-centro-floral"
                cleanDesc.contains("arco") -> candidate.name.contains("arco", ignoreCase = true) || candidate.id == "art-arco-rosa"
                else -> false
            }
        }
    }

    private fun sumDraftQtyMatching(
        quantities: Map<String, Int>,
        catalog: List<CatalogArticle>,
        nameKeyword: String,
        fallbackCode: String,
        fallbackId: String,
    ): Int {
        var sum = 0
        for (article in catalog) {
            val qty = quantities[article.id] ?: 0
            if (qty <= 0) continue
            val isMatch = article.name.contains(nameKeyword, ignoreCase = true) ||
                article.code.equals(fallbackCode, ignoreCase = true) ||
                article.id == fallbackId
            if (isMatch) {
                sum += qty
            }
        }
        return sum
    }

    private companion object {
        private const val MAX_DELIVERY_FEE_DOP: Int = 50_000
        private const val WAV_HEADER_MIN_BYTES: Int = 44
        private const val MAX_MEDIA_CACHE_ENTRIES: Int = 40
        private const val MAX_MEDIA_CACHE_BYTES: Int = 25 * 1024 * 1024
    }
}
