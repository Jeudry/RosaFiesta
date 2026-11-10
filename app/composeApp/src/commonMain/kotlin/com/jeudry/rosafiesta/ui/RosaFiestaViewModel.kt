package com.jeudry.rosafiesta.ui

import androidx.lifecycle.ViewModel
import com.jeudry.rosafiesta.core.engine.CancellationRefund
import com.jeudry.rosafiesta.core.engine.DateChangeResult
import com.jeudry.rosafiesta.core.engine.RosaFiestaEngine
import com.jeudry.rosafiesta.domain.BusinessMetrics
import com.jeudry.rosafiesta.domain.CatalogArticle
import com.jeudry.rosafiesta.domain.ChecklistStep
import com.jeudry.rosafiesta.domain.ClientEvent
import com.jeudry.rosafiesta.domain.OfflineQueueItem
import com.jeudry.rosafiesta.domain.PaymentRecord
import com.jeudry.rosafiesta.domain.RosaFiestaRepository
import com.jeudry.rosafiesta.domain.StaffMember
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
 * ViewModel multiplataforma compartido para `PersonalApp` y `AdminApp`.
 * Mantiene el estado reactivo de formularios, filtros, Rosa IA, sonidos CC0, almacenamiento seguro y operaciones de negocio.
 */
public class RosaFiestaViewModel(
    private val repository: RosaFiestaRepository,
    public val soundPlayer: SoundPlayer = NoOpSoundPlayer(),
    private val secureStorage: SecureStorage = InMemorySecureStorage(),
    public val voiceInputMonitor: VoiceInputMonitor = NoOpVoiceInputMonitor(),
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
    private val securityScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // Búsqueda, filtros y selección activa
    private val _searchQuery = MutableStateFlow("")
    public val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Todos")
    public val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _maxPriceFilterDop = MutableStateFlow(200)
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

    // Cantidades en borrador de cotización
    private val _draftQuantities = MutableStateFlow(
        mapOf(
            "art-silla-tiffany" to 120,
            "art-mesa-redonda" to 15,
            "art-centro-floral" to 8,
            "art-arco-rosa" to 1,
            "art-mesa-helados" to 1,
        ),
    )
    public val draftQuantities: StateFlow<Map<String, Int>> = _draftQuantities.asStateFlow()

    // Parámetros interactivos de Rosa IA
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

    private val _voiceInputOrigin = MutableStateFlow(VoiceInputOrigin.NONE)
    public val voiceInputOrigin: StateFlow<VoiceInputOrigin> = _voiceInputOrigin.asStateFlow()

    public val isVoiceListening: StateFlow<Boolean> = voiceInputMonitor.isListening
    public val isVoiceTranscribing: StateFlow<Boolean> = voiceInputMonitor.isTranscribing
    public val voiceAmplitude: StateFlow<Float> = voiceInputMonitor.amplitudeLevel
    public val hasHardwareMicrophone: StateFlow<Boolean> = voiceInputMonitor.hasHardwareMicrophone

    private val _voiceTranscript = MutableStateFlow("")
    public val voiceTranscript: StateFlow<String> = _voiceTranscript.asStateFlow()

    // Formularios interactivos (Auth, Eventos, Pagos, Catálogo, Perfil, Soporte, Config)
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

    private val _eventTypeInput = MutableStateFlow("Boda Real")
    public val eventTypeInput: StateFlow<String> = _eventTypeInput.asStateFlow()

    private val _eventDateInput = MutableStateFlow("Vie 23 oct")
    public val eventDateInput: StateFlow<String> = _eventDateInput.asStateFlow()

    private val _eventVenueInput = MutableStateFlow("Jardín Botánico")
    public val eventVenueInput: StateFlow<String> = _eventVenueInput.asStateFlow()

    private val _eventGuestsInput = MutableStateFlow("120")
    public val eventGuestsInput: StateFlow<String> = _eventGuestsInput.asStateFlow()

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

    private val _isDarkTheme = MutableStateFlow(false)
    public val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _preferredLanguage = MutableStateFlow("Español")
    public val preferredLanguage: StateFlow<String> = _preferredLanguage.asStateFlow()

    private val _remindersEnabled = MutableStateFlow(true)
    public val remindersEnabled: StateFlow<Boolean> = _remindersEnabled.asStateFlow()

    private val _savedPaymentMethod = MutableStateFlow("Visa •••• 4821 · Transferencia")
    public val savedPaymentMethod: StateFlow<String> = _savedPaymentMethod.asStateFlow()

    private val _sketchTableCount = MutableStateFlow(6)
    public val sketchTableCount: StateFlow<Int> = _sketchTableCount.asStateFlow()

    private val _activeOtpCode = MutableStateFlow<String?>(null)
    public val activeOtpCode: StateFlow<String?> = _activeOtpCode.asStateFlow()

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
                dateLabel = "Hoy",
                rating = 5,
                comment = "El montaje de nuestra boda superó todas las expectativas. Las sillas Tiffany y el arco floral quedaron de revista.",
                isVerified = true,
            ),
            CompanyReviewUiModel(
                id = "rev-2",
                author = "Laura M.",
                initial = "L",
                dateLabel = "Hoy",
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
                timeLabel = "10:14 a. m.",
                isFromClient = false,
            ),
            DirectChatMessageUiModel(
                id = "msg-2",
                senderName = "Cliente",
                text = "¡Hola equipo Rosa Fiesta! ¿Podemos incluir 20 sillas Chiavari doradas adicionales para el área del brindis?",
                timeLabel = "10:16 a. m.",
                isFromClient = true,
            ),
            DirectChatMessageUiModel(
                id = "msg-3",
                senderName = "María · Rosa Fiesta",
                text = "¡Claro que sí! Tenemos disponibilidad inmediata de sillas Tiffany y Chiavari doradas (RD$ 4/día). Dime qué más deseas ajustar y te actualizo el expediente al instante.",
                timeLabel = "10:17 a. m.",
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

    private val _deliveryBaseInput = MutableStateFlow("450")
    public val deliveryBaseInput: StateFlow<String> = _deliveryBaseInput.asStateFlow()

    private val _cancellationDaysInput = MutableStateFlow("7")
    public val cancellationDaysInput: StateFlow<String> = _cancellationDaysInput.asStateFlow()

    // Switches interactivos de preferencias y notificaciones
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

    // Estado de validación, feedback y protección contra doble envío
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
        securityScope.launch {
            val savedToken = secureStorage.get(SecureStorage.KEY_ACCESS_TOKEN)
            if (!savedToken.isNullOrBlank()) {
                val restoredAccount = repository.restoreSessionFromJwt(savedToken)
                if (restoredAccount != null) {
                    applyProfileFieldsFromAccount(restoredAccount)
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

    private fun serializeSuppliersForStorage(suppliers: List<SupplierUiModel>): String = suppliers.joinToString("||") { item ->
        "${item.id}::${item.name.replace("::", " ")}::${item.category.replace("::", " ")}::${item.phone.replace("::", " ")}::${item.ratingLabel}::${item.statusLabel}"
    }

    private fun parseReviewsFromStorage(serialized: String): List<CompanyReviewUiModel> = serialized.split("||").mapNotNull { entry ->
        val parts = entry.split("::")
        if (parts.size >= 6) {
            CompanyReviewUiModel(
                id = parts[0],
                author = parts[1],
                initial = parts[2],
                dateLabel = parts[3],
                rating = parts[4].toIntOrNull() ?: 5,
                comment = parts[5],
                isVerified = true,
            )
        } else {
            null
        }
    }

    private fun serializeReviewsForStorage(reviews: List<CompanyReviewUiModel>): String = reviews.joinToString("||") { reviewItem ->
        "${reviewItem.id}::${reviewItem.author}::${reviewItem.initial}::${reviewItem.dateLabel}::${reviewItem.rating}::${reviewItem.comment.replace("::", " ").replace("||", " ")}"
    }

    private fun parseChatFromStorage(serialized: String): List<DirectChatMessageUiModel> = serialized.split("||").mapNotNull { entry ->
        val parts = entry.split("::")
        if (parts.size >= 5) {
            DirectChatMessageUiModel(
                id = parts[0],
                senderName = parts[1],
                text = parts[2],
                timeLabel = parts[3],
                isFromClient = parts[4] == "true",
            )
        } else {
            null
        }
    }

    private fun serializeChatForStorage(messages: List<DirectChatMessageUiModel>): String = messages.joinToString("||") { msgItem ->
        "${msgItem.id}::${msgItem.senderName}::${msgItem.text.replace("::", " ").replace("||", " ")}::${msgItem.timeLabel}::${msgItem.isFromClient}"
    }

    private fun applyProfileFieldsFromAccount(account: RegisteredUserAccount) {
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

    public fun clearFormError() {
        _formError.value = null
    }

    public fun clearFormFeedback() {
        _formFeedback.value = null
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

    public fun adjustDraftQuantity(articleId: String, delta: Int) {
        soundPlayer.playCartAdd()
        _draftQuantities.update { current ->
            val next = ((current[articleId] ?: 1) + delta).coerceAtLeast(0)
            current + (articleId to next)
        }
    }

    public fun setDraftQuantity(articleId: String, quantity: Int) {
        _draftQuantities.update { current ->
            current + (articleId to quantity.coerceAtLeast(0))
        }
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
        val proposal = RosaFiestaEngine.buildRosaProposal(_rosaEventType.value, _rosaGuestsCount.value)
        _rosaProposalMessage.value =
            "Propuesta Rosa IA para ${_rosaGuestsCount.value} invitados (${_rosaPalette.value}): ${RosaFiestaEngine.formatDop(proposal.totalDop)}"
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
        } else if (_voiceTranscript.value.isBlank()) {
            _voiceTranscript.value = "Una boda en un jardín para unas 120 personas en blanco y dorado"
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

    public fun stopVoiceInput() {
        soundPlayer.playTap()
        voiceInputMonitor.stopListening()
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

    public fun setEventDateInput(value: String) {
        _eventDateInput.value = value
    }

    public fun setEventVenueInput(value: String) {
        _eventVenueInput.value = value
    }

    public fun setEventGuestsInput(value: String) {
        _eventGuestsInput.value = value
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
            .map { token -> token.replaceFirstChar { char -> char.uppercaseChar() } }
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
        _pushNotificationsEnabled.update { !it }
    }

    public fun toggleWhatsappAlerts() {
        _whatsappAlertsEnabled.update { !it }
    }

    public fun toggleEmailReceipts() {
        _emailReceiptsEnabled.update { !it }
    }

    public fun toggleBiometricLock() {
        _biometricLockEnabled.update { !it }
    }

    public fun toggleAutoSyncWifiOnly() {
        soundPlayer.playTap()
        _autoSyncWifiOnly.update { !it }
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
                phone = _registerPhone.value,
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
        _loginPassword.value = ""
        _formError.value = null
        _formFeedback.value = "Sesión cerrada correctamente."
        repository.clearSession()
        securityScope.launch {
            secureStorage.remove(SecureStorage.KEY_ACCESS_TOKEN)
            secureStorage.remove(SecureStorage.KEY_USER_ROLE)
        }
    }

    public fun submitPasswordReset(
        identifier: String,
        newPassword: String,
        confirmPassword: String,
    ): Boolean {
        if (_isSubmitting.value) return false
        _isSubmitting.value = true
        try {
            val cleanPass = newPassword.trim()
            val cleanConfirm = confirmPassword.trim()
            val passwordError = InputValidator.validatePassword(cleanPass)
            if (passwordError != null) {
                soundPlayer.playAlertError()
                _formError.value = passwordError
                return false
            }
            if (cleanPass != cleanConfirm) {
                soundPlayer.playAlertError()
                _formError.value = SensitiveDataRedactor.translateBusinessError("AUTH_PASSWORD_MISMATCH", null)
                return false
            }
            val outcome = repository.resetUserPassword(identifier, cleanPass)
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
            _formFeedback.value = "Contraseña actualizada correctamente."
            return true
        } finally {
            _isSubmitting.value = false
        }
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

    public fun submitRegister(): Boolean {
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
            password = _loginPassword.value.ifBlank { "rosa2026" },
            birthDate = _profileBirthDate.value,
            acceptedTerms = true,
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
            if (title.isEmpty() || client.isEmpty() || date.isEmpty() || venue.isEmpty() || guests <= 0) {
                _formError.value = "Completa título, cliente, fecha, lugar y un número válido de invitados."
                return false
            }
            repository.createEvent(title, client, date, venue, guests)
            repository.events.value.firstOrNull()?.id?.let { createdId ->
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
            val title = _eventTypeInput.value.trim()
            val date = _eventDateInput.value.trim()
            val venue = _eventVenueInput.value.trim()
            val guests = _eventGuestsInput.value.trim().toIntOrNull() ?: 0
            val isMissingFields = title.isEmpty() || date.isEmpty() || venue.isEmpty() || guests <= 0
            if (isMissingFields) {
                _formError.value = "Completa el tipo de evento, fecha, lugar e invitados (> 0)."
                return false
            }
            val resolvedClientName = _profileName.value.trim().ifEmpty { "Cliente" }
            repository.createEvent(title, resolvedClientName, date, venue, guests)
            repository.events.value.firstOrNull()?.id?.let { createdId ->
                _selectedEventId.value = createdId
            }
            _formError.value = null
            _formFeedback.value = "Evento '$title' configurado."
            return true
        } finally {
            _isSubmitting.value = false
        }
    }

    public fun submitNewArticle(): Boolean {
        if (_isSubmitting.value) return false
        _isSubmitting.value = true
        try {
            val code = _newArticleCode.value.trim()
            val name = _newArticleName.value.trim()
            val cat = _newArticleCategory.value.trim().ifEmpty { "Mobiliario" }
            val price = _newArticlePrice.value.trim().toIntOrNull() ?: 0
            val stock = _newArticleStock.value.trim().toIntOrNull() ?: -1
            if (code.isEmpty() || name.isEmpty() || price <= 0 || stock < 0) {
                _formError.value = "Ingresa código, nombre, precio diario (> 0) y stock válido."
                return false
            }
            repository.createArticle(code, name, cat, price, stock)
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
            val amount = _paymentAmountInput.value.trim().filter { it.isDigit() }.toIntOrNull() ?: 0
            val method = _paymentMethodInput.value.trim()
            val ref = _paymentRefInput.value.trim()
            if (amount <= 0 || method.isEmpty()) {
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

    public fun submitCancelEvent(eventId: String = _selectedEventId.value): CancellationRefund {
        soundPlayer.playAlertError()
        val refund = repository.cancelEvent(eventId)
        _formFeedback.value = "Evento cancelado · Reembolso ${refund.refundPercent}% (${RosaFiestaEngine.formatDop(refund.refundDop)})"
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
        if (subj.isEmpty() || msg.isEmpty()) {
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
        if (!draftAvatarKey.isNullOrBlank()) {
            securityScope.launch {
                secureStorage.put(SecureStorage.KEY_AVATAR_KEY, draftAvatarKey)
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
        securityScope.launch {
            if (avatarKey.isNullOrBlank()) {
                secureStorage.remove(SecureStorage.KEY_AVATAR_KEY)
            } else {
                secureStorage.put(SecureStorage.KEY_AVATAR_KEY, avatarKey)
            }
        }
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
        soundPlayer.playTap()
        val nextValue = !_isDarkTheme.value
        _isDarkTheme.value = nextValue
        com.jeudry.rosafiesta.designsystem.RfThemeState.isPersonalDark = nextValue
        securityScope.launch {
            secureStorage.put(SecureStorage.KEY_DARK_THEME, nextValue.toString())
        }
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
        val cleanMethod = methodLabel.trim().ifBlank { "Visa •••• 4821 · Transferencia" }
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

    public fun clientEventsForCurrentUser(): List<ClientEvent> {
        val allEvents = events.value
        val activeName = _profileName.value.trim()
        val firstToken = activeName.substringBefore(" ").trim()
        val isGenericGuest = activeName.isBlank() || activeName.equals("Invitado Rosa Fiesta", ignoreCase = true) || activeName.equals("Cliente", ignoreCase = true)
        if (isGenericGuest) {
            return allEvents
        }
        val matchingEvents = allEvents.filter { eventItem ->
            val matchesFull = eventItem.clientName.equals(activeName, ignoreCase = true)
            if (matchesFull) {
                true
            } else {
                val hasValidToken = firstToken.length >= 3
                if (hasValidToken) eventItem.clientName.contains(firstToken, ignoreCase = true) else false
            }
        }
        return if (matchingEvents.isNotEmpty()) matchingEvents else allEvents
    }

    public fun activeClientEvent(): ClientEvent {
        val userEvents = clientEventsForCurrentUser()
        val currentSelectedId = _selectedEventId.value
        return userEvents.firstOrNull { eventItem -> eventItem.id == currentSelectedId }
            ?: events.value.firstOrNull { eventItem -> eventItem.id == currentSelectedId }
            ?: userEvents.firstOrNull()
            ?: events.value.first()
    }

    public fun clientPaymentsForCurrentUser(): List<PaymentRecord> {
        val userEventsIds = clientEventsForCurrentUser().map { eventItem -> eventItem.id }.toSet()
        val allPayments = payments.value
        val matchingPayments = allPayments.filter { paymentItem -> paymentItem.eventId in userEventsIds }
        return if (matchingPayments.isNotEmpty()) matchingPayments else allPayments
    }

    public fun addEventTimelineItem() {
        soundPlayer.playTap()
        val nextNum = _eventTimelineItems.value.size + 1
        val activeEvt = activeClientEvent()
        val newItem = EventTimelineItemUiModel(
            id = "tl-$nextNum",
            startTime = "22:00",
            endTime = "23:30",
            title = "Desmontaje y retorno a bodega #$nextNum",
            subtitle = "Conteo de ${activeEvt.articleCount} piezas (${activeEvt.title})",
            dotColorHex = 0xFF8DB52C,
            ringColorHex = 0xFFEEF6D6,
        )
        _eventTimelineItems.update { currentList -> currentList + newItem }
    }

    public fun toggleEventTask(taskId: String) {
        soundPlayer.playChecklistTick()
        _eventTasksList.update { currentList ->
            currentList.map { taskItem ->
                if (taskItem.id == taskId) taskItem.copy(completed = !taskItem.completed) else taskItem
            }
        }
    }

    public fun addEventTask(customTitle: String = "") {
        soundPlayer.playTap()
        val nextNum = _eventTasksList.value.size + 1
        val activeEvt = activeClientEvent()
        val resolvedTitle = customTitle.trim().ifBlank { "Coordinación #$nextNum · ${activeEvt.title}" }
        val newTask = EventTaskUiModel(
            id = "tsk-$nextNum",
            title = resolvedTitle,
            badgeText = "Pendiente",
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
                    val nextStatus = if (guestItem.status == "Confirmado") "Pendiente" else "Confirmado"
                    guestItem.copy(status = nextStatus)
                } else {
                    guestItem
                }
            }
        }
    }

    public fun addEventGuest(guestName: String = "") {
        soundPlayer.playTap()
        val nextNum = _eventGuestsList.value.size + 1
        val resolvedName = guestName.trim().ifBlank { "Invitado VIP #$nextNum" }
        val newGuest = EventGuestUiModel(
            id = "gst-$nextNum",
            name = resolvedName,
            details = "Confirmado · Mesa ${nextNum + 2} · 2 personas",
            status = "Confirmado",
        )
        _eventGuestsList.update { currentList -> currentList + newGuest }
    }

    public fun authenticateSocialAccount(provider: String, typedIdentifier: String = ""): Boolean {
        soundPlayer.playPaymentSuccess()
        val cleanTyped = typedIdentifier.trim()
        val isTypedValid = InputValidator.validateLoginIdentifier(cleanTyped) == null
        val resolvedEmail = if (isTypedValid) {
            if (cleanTyped.contains("@")) cleanTyped.lowercase() else "${cleanTyped.lowercase()}@rosafiesta.do"
        } else {
            "camila@rosafiesta.do"
        }
        val authOutcome = repository.authenticateUser(resolvedEmail, "RosaFiesta2026!")
        val account = authOutcome.account
        if (account != null) {
            applyAuthenticatedAccount(account, authOutcome.accessToken)
        } else {
            applyUserIdentityFromLogin(resolvedEmail)
        }
        _formError.value = null
        _formFeedback.value = "Sesión iniciada con $provider como ${_profileName.value}"
        return true
    }

    public fun requestPasswordResetOtp(emailInput: String): String {
        val cleanEmail = emailInput.trim().lowercase()
        val codeDigits = ((cleanEmail.hashCode().let { hash -> if (hash < 0) -hash else hash } % 900000) + 100000).toString()
        _activeOtpCode.value = codeDigits
        _loginEmail.value = cleanEmail
        applyUserIdentityFromLogin(cleanEmail)
        _formError.value = null
        _formFeedback.value = "Código de seguridad enviado a $cleanEmail: $codeDigits"
        return codeDigits
    }

    public fun exportReceiptDocument(event: ClientEvent, payment: PaymentRecord? = null): String {
        soundPlayer.playPaymentSuccess()
        val receiptNo = payment?.receiptNumber ?: "RF-2026-0412"
        val paidAmount = payment?.amountDop ?: event.paidDepositDop
        val summaryText = "Recibo $receiptNo · ${event.title} (${event.dateLabel}) · Pagado: RD$ $paidAmount de RD$ ${event.totalDop}"
        _formFeedback.value = "Comprobante $receiptNo exportado correctamente."
        return summaryText
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
        val isTooShort = trimmedComment.length < 6
        if (isTooShort) {
            soundPlayer.playAlertError()
            _formError.value = "Escribe al menos 6 caracteres para publicar tu reseña."
            return false
        }
        val cleanRating = rating.coerceIn(1, 5)
        val authorName = _profileName.value.trim().ifBlank { "Cliente Rosa Fiesta" }
        val initialChar = authorName.firstOrNull()?.uppercaseChar()?.toString() ?: "C"
        val newReview = CompanyReviewUiModel(
            id = "rev-${_companyReviews.value.size + 1}",
            author = authorName,
            initial = initialChar,
            dateLabel = "Ahora",
            rating = cleanRating,
            comment = trimmedComment,
            isVerified = true,
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

    public fun sendDirectChatMessage(rawText: String) {
        val trimmedText = rawText.trim()
        if (trimmedText.isEmpty()) return
        soundPlayer.playRosaSend()
        val clientName = _profileName.value.substringBefore(" ").ifBlank { "Cliente" }
        val clientMessage = DirectChatMessageUiModel(
            id = "msg-${_directChatMessages.value.size + 1}",
            senderName = clientName,
            text = SensitiveDataRedactor.redactText(trimmedText),
            timeLabel = "Ahora",
            isFromClient = true,
        )
        val mariaReplyText = buildMariaContextualReply(trimmedText, clientName)
        val mariaReply = DirectChatMessageUiModel(
            id = "msg-${_directChatMessages.value.size + 2}",
            senderName = "María · Rosa Fiesta",
            text = mariaReplyText,
            timeLabel = "Ahora",
            isFromClient = false,
        )
        _directChatMessages.update { existingMessages ->
            existingMessages + clientMessage + mariaReply
        }
        val updatedChat = _directChatMessages.value
        securityScope.launch {
            secureStorage.put(SecureStorage.KEY_DIRECT_CHAT, serializeChatForStorage(updatedChat))
        }
        repository.sendChatMessage(clientName, SensitiveDataRedactor.redactText(trimmedText))
    }

    public fun sendAdminDirectChatReply(rawReplyText: String) {
        val trimmedReply = rawReplyText.trim()
        if (trimmedReply.isEmpty()) return
        soundPlayer.playRosaSend()
        val adminReply = DirectChatMessageUiModel(
            id = "msg-${_directChatMessages.value.size + 1}",
            senderName = "María · Rosa Fiesta",
            text = SensitiveDataRedactor.redactText(trimmedReply),
            timeLabel = "Ahora",
            isFromClient = false,
        )
        _directChatMessages.update { existingMessages ->
            existingMessages + adminReply
        }
        val updatedChat = _directChatMessages.value
        securityScope.launch {
            secureStorage.put(SecureStorage.KEY_DIRECT_CHAT, serializeChatForStorage(updatedChat))
        }
        repository.sendChatMessage("María · Rosa Fiesta", SensitiveDataRedactor.redactText(trimmedReply))
    }

    private fun buildMariaContextualReply(userMessage: String, clientName: String): String {
        val normalized = userMessage.lowercase()
        val activeEvent = events.value.firstOrNull { eventItem -> eventItem.id == _selectedEventId.value }
            ?: events.value.firstOrNull()
        val eventName = activeEvent?.title ?: "tu evento"
        val eventDate = activeEvent?.dateLabel ?: "Vie 23 oct"

        val mentionsChair = listOf("silla", "tiffany", "chiavari", "asiento").any { keyword -> normalized.contains(keyword) }
        if (mentionsChair) {
            val chairStock = articles.value.firstOrNull { item -> item.id == "art-silla-tiffany" }?.totalStock ?: 200
            return "¡Perfecto, $clientName! Contamos con $chairStock Sillas Tiffany doradas libres para $eventDate a RD$ 4/día. Ya dejé la nota lista en el expediente de «$eventName»."
        }

        val mentionsTable = listOf("mesa", "mantel", "redonda", "postre", "dulce").any { keyword -> normalized.contains(keyword) }
        if (mentionsTable) {
            return "¡Claro que sí, $clientName! Tenemos mesas redondas para 10 personas (RD$ 18/día) y estación de postres completa (RD$ 150/día) con mantelería incluida para «$eventName»."
        }

        val mentionsArchOrFlowers = listOf("arco", "flor", "centro", "globo", "decoraci").any { keyword -> normalized.contains(keyword) }
        if (mentionsArchOrFlowers) {
            return "¡Nos encanta esa idea, $clientName! Yuli (nuestra líder de decoración) puede personalizar el arco orgánico de 3 m (RD$ 95/día) y los centros florales (RD$ 45/día) en tu paleta favorita 🌸"
        }

        val mentionsPriceOrQuote = listOf("precio", "cuanto", "cuánto", "costo", "cotiza", "presupuesto", "descuento", "pago", "anticipo").any { keyword -> normalized.contains(keyword) }
        if (mentionsPriceOrQuote) {
            return "Para «$eventName» reservas tu fecha con solo el 30 % de anticipo por transferencia Banreservas o tarjeta, y el saldo 7 días antes del montaje ($eventDate). ¿Quieres que te genere el PDF actualizado?"
        }

        val mentionsDateOrPlace = listOf("fecha", "día", "dia", "hora", "lugar", "jardín", "jardin", "salón", "salon", "visita", "direcci", "ubicaci").any { keyword -> normalized.contains(keyword) }
        if (mentionsDateOrPlace) {
            return "Tenemos disponibilidad confirmada para $eventDate y nuestro equipo de logística (Pedro) coordina la entrega y desmontaje puntual en el salón. También puedes visitarnos en Piantini de lunes a sábado."
        }

        return "¡Recibido, $clientName! Ya registré tu comentario («${userMessage.take(60)}») en la orden de «$eventName» para el $eventDate. Pedro y Yuli ya están al tanto ✨"
    }

    public fun addSupplier(
        name: String,
        category: String,
        phone: String,
        statusLabel: String = "Confirmado",
    ): Boolean {
        val cleanName = name.trim()
        val cleanCat = category.trim().ifBlank { "Servicios" }
        val cleanPhone = phone.trim().ifBlank { "809-555-0100" }
        if (cleanName.length < 2) {
            soundPlayer.playAlertError()
            _formError.value = "Ingresa el nombre del proveedor."
            return false
        }
        val nextIdx = _suppliersList.value.size + 1
        val created = SupplierUiModel(
            id = "sup-$nextIdx",
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
        soundPlayer.playAlertError()
        repository.deleteCurrentAccount()
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

    public fun saveBusinessConfig(): Boolean {
        val dep = _depositPercentInput.value.trim().toIntOrNull() ?: 0
        val deliv = _deliveryBaseInput.value.trim().toIntOrNull() ?: -1
        if (dep !in 10..90 || deliv < 0) {
            _formError.value = "Anticipo debe estar entre 10% y 90% y tarifa de envío >= 0."
            return false
        }
        _formError.value = null
        _formFeedback.value = "Reglas guardadas: anticipo $dep% · envío base RD$ $deliv."
        return true
    }

    public fun getAdminOnboardingSummary(): AdminOnboardingSummary {
        val currentEvents = events.value
        val leadEvent = currentEvents.firstOrNull { eventItem -> eventItem.id == "evt-boda-real" }
            ?: currentEvents.firstOrNull()
        val currentArticles = articles.value
        val primaryChair = currentArticles.firstOrNull { articleItem -> articleItem.id == "art-silla-tiffany" }
            ?: currentArticles.firstOrNull()
        val currentMetrics = metrics.value
        val calculatedIncome = if (currentMetrics.octoberRevenueDop > 0) {
            currentMetrics.octoberRevenueDop
        } else {
            12450
        }
        val calculatedDepositPending = if (currentMetrics.accountsReceivableDop > 0) {
            currentMetrics.accountsReceivableDop
        } else {
            4500
        }

        return AdminOnboardingSummary(
            activeEventsCount = currentEvents.size.coerceAtLeast(3),
            leadEventTitle = leadEvent?.title ?: "Boda Real Rosa & Dorado",
            leadEventDate = leadEvent?.dateLabel ?: "Vie 23 oct",
            leadEventGuests = leadEvent?.guests ?: 120,
            leadEventVenue = leadEvent?.venue ?: "Jardín Botánico",
            leadEventItemsCount = leadEvent?.items?.sumOf { item -> item.quantity } ?: 145,
            primaryArticleTitle = primaryChair?.name ?: "Sillas Tiffany doradas",
            primaryArticleAvailableStock = primaryChair?.totalStock ?: 200,
            monthlyIncomeDop = calculatedIncome,
            depositPendingDop = calculatedDepositPending,
            totalArticlesInCatalog = currentArticles.size.coerceAtLeast(12),
        )
    }
}
