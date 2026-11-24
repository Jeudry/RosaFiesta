package com.jeudry.rosafiesta.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.pluginOrNull
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.readRawBytes
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.concurrent.Volatile
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Serializable
public data class HealthOutputDto(
    public val status: String,
    public val product: String,
)

@Serializable
public data class DeleteAccountOutputDto(
    public val deleted: Boolean,
)

@Serializable
public data class UserDto(
    public val id: String,
    public val username: String = "",
    public val fullName: String,
    public val email: String,
    public val phone: String = "",
    public val birthDate: String = "",
    public val role: String,
    public val permissions: List<String>? = null,
    public val activeView: String = "personal",
    public val active: Boolean = true,
    public val createdAt: String = "",
    public val avatarKey: String? = null,
    public val preferences: UserPreferencesDto? = null,
)

@Serializable
public data class LoginInputDto(
    public val email: String,
    public val password: String,
)

@Serializable
public data class RegisterInputDto(
    public val username: String,
    public val fullName: String,
    public val email: String,
    public val password: String,
    public val birthDate: String = "",
    public val phone: String = "",
)

@Serializable
public data class ResetPasswordInputDto(
    public val identifier: String? = null,
    public val newPassword: String,
    public val email: String? = null,
    public val otpCode: String? = null,
    public val resetToken: String? = null,
)

@Serializable
public data class LoginOutputDto(
    public val accessToken: String,
    public val refreshToken: String = "",
    public val expiresIn: Long = 0L,
    public val user: UserDto,
)

@Serializable
public data class RefreshTokenInputDto(
    public val refreshToken: String,
)

@Serializable
public data class RegisteredUsersOutputDto(
    public val users: List<UserDto>? = null,
)

@Serializable
public data class SwitchViewInputDto(
    public val view: String,
)

@Serializable
public data class SwitchViewOutputDto(
    public val user: UserDto,
)

@Serializable
public data class ClientSummaryDto(
    public val id: String,
    public val fullName: String,
    public val email: String,
    public val phone: String,
    public val eventsCount: Long,
    public val totalSpentDop: Long,
    public val balanceDop: Long,
    public val notes: String? = null,
)

@Serializable
public data class ClientsOutputDto(
    public val clients: List<ClientSummaryDto>? = null,
)

@Serializable
public data class StaffMemberDto(
    public val id: String,
    public val fullName: String,
    public val roleLabel: String,
    public val role: String,
    public val permissions: List<String>? = null,
    public val active: Boolean = true,
    public val email: String? = null,
    public val phone: String? = null,
)

@Serializable
public data class StaffOutputDto(
    public val staff: List<StaffMemberDto>? = null,
)

@Serializable
public data class ArticleDto(
    public val id: String,
    public val name: String,
    public val categoryId: String,
    public val photoKey: String,
    public val dailyPriceDop: Long,
    public val totalStock: Long,
    public val availableStock: Long,
    public val lowStockThreshold: Long,
    public val description: String,
    public val colorVariants: List<String>? = null,
    public val dimensions: String? = null,
    public val isDirectSale: Boolean? = null,
    public val material: String? = null,
    public val visible: Boolean = true,
    public val photoUrl: String? = null,
    public val damagedStock: Long = 0,
)

@Serializable
public data class ArticlesOutputDto(
    public val articles: List<ArticleDto>? = null,
)

@Serializable
public data class CategoryDto(
    public val id: String,
    public val name: String,
    public val illoKey: String,
    public val itemCount: Long,
)

@Serializable
public data class CategoriesOutputDto(
    public val categories: List<CategoryDto>? = null,
)

@Serializable
public data class CategoryOutputDto(
    public val category: CategoryDto,
)

@Serializable
public data class SaveCategoryInputDto(
    public val id: String? = null,
    public val name: String,
    public val illoKey: String? = null,
)

@Serializable
public data class UpdateCategoryInputDto(
    public val name: String,
    public val illoKey: String? = null,
)

@Serializable
public data class DeleteCategoryOutputDto(
    public val deletedId: String,
)

@Serializable
public data class DeleteArticleOutputDto(
    public val deletedId: String,
)

@Serializable
public data class CatalogTrendDto(
    public val id: String,
    public val title: String,
    public val eventsLabel: String = "",
    public val photoKey: String = "boda",
    public val photoUrl: String? = null,
    public val photoBgHex: String = "#F3EEF6",
    public val linkedCategory: String? = null,
    public val linkedPackageId: String? = null,
    public val sortOrder: Int = 0,
    public val active: Boolean = true,
)

@Serializable
public data class TrendsOutputDto(
    public val trends: List<CatalogTrendDto>? = null,
)

@Serializable
public data class TrendOutputDto(
    public val trend: CatalogTrendDto,
)

@Serializable
public data class SaveTrendInputDto(
    public val id: String? = null,
    public val title: String,
    public val eventsLabel: String = "",
    public val photoKey: String = "boda",
    public val photoUrl: String? = null,
    public val photoBgHex: String = "#F3EEF6",
    public val linkedCategory: String? = null,
    public val linkedPackageId: String? = null,
    public val sortOrder: Int = 0,
    public val active: Boolean = true,
)

@Serializable
public data class DeleteTrendOutputDto(
    public val deletedId: String,
)

@Serializable
public data class SeasonBannerDto(
    public val id: String = "default",
    public val badgeLabel: String = "",
    public val title: String = "",
    public val ctaLabel: String = "",
    public val targetScreenCode: String = "s21",
    public val active: Boolean = false,
)

@Serializable
public data class SeasonBannerOutputDto(
    public val banner: SeasonBannerDto,
)

@Serializable
public data class SaveSeasonBannerInputDto(
    public val badgeLabel: String = "",
    public val title: String,
    public val ctaLabel: String = "",
    public val targetScreenCode: String = "s21",
    public val active: Boolean = false,
)

@Serializable
public data class PackageBundleDto(
    public val id: String,
    public val name: String,
    public val eventType: String,
    public val photoKey: String,
    public val totalPriceDop: Long,
    public val guestCapacity: Long,
    public val itemsSummary: String,
    public val items: List<PackageItemDto>? = null,
    public val visible: Boolean = true,
    public val photoUrl: String? = null,
)

@Serializable
public data class PackagesOutputDto(
    public val packages: List<PackageBundleDto>? = null,
)

@Serializable
public data class EventItemDto(
    public val articleCode: String,
    public val name: String,
    public val quantity: Long,
    public val unitPriceRD: Long,
)

@Serializable
public data class EventDto(
    public val id: String,
    public val code: String,
    public val clientName: String,
    public val title: String,
    public val eventType: String,
    public val dateLabel: String,
    public val daysUntilEvent: Long,
    public val venue: String,
    public val zone: String,
    public val guests: Long,
    public val articleCount: Long,
    public val totalRD: Long,
    public val paidDepositRD: Long,
    public val balanceDueDate: String,
    public val status: String,
    public val dateChangesUsed: Long,
    public val assignedLead: String,
    public val items: List<EventItemDto>? = null,
    public val cancellationReason: String? = null,
    public val clientEmail: String? = null,
    public val clientId: String? = null,
    public val sketchJson: String? = null,
    public val eventDateIso: String? = null,
    public val pickupDateIso: String? = null,
    public val returnDateIso: String? = null,
    public val quoteId: String? = null,
    public val closeoutCompleted: Boolean = false,
    public val closeoutNotes: String? = null,
    public val lastPaymentReminderAt: String? = null,
    public val lastPaymentReminderChannel: String? = null,
)

@Serializable
public data class EventsListOutputDto(
    public val events: List<EventDto>? = null,
)

@Serializable
public data class EventDetailOutputDto(
    public val event: EventDto,
    public val balanceRD: Long,
)

@Serializable
public data class EventTemplateDto(
    public val id: String,
    public val slug: String,
    public val name: String,
    public val subtitle: String,
    public val defaultGuests: Long,
    public val startingPriceRD: Long,
    public val photoKey: String,
    public val paletteColors: List<String>? = null,
)

@Serializable
public data class TemplatesOutputDto(
    public val templates: List<EventTemplateDto>? = null,
)

@Serializable
public data class RefundPolicyResultDto(
    public val refundPercent: Long,
    public val refundRD: Long,
    public val retainedRD: Long,
    public val policyTier: String,
)

@Serializable
public data class CancelEventOutputDto(
    public val event: EventDto,
    public val refund: RefundPolicyResultDto,
)

@Serializable
public data class DateChangeInputDto(
    public val newDateLabel: String,
    public val newDaysUntilEvent: Long,
)

@Serializable
public data class DateChangeEvaluationDto(
    public val allowed: Boolean,
    public val isFree: Boolean,
    public val feeRD: Long,
    public val reason: String,
    public val nextCount: Long,
)

@Serializable
public data class DateChangeOutputDto(
    public val event: EventDto,
    public val evaluation: DateChangeEvaluationDto,
)

@Serializable
public data class ChecklistItemDto(
    public val id: String,
    public val stage: String,
    public val title: String,
    public val completed: Boolean,
    public val completedBy: String? = null,
    public val completedAt: String? = null,
)

@Serializable
public data class DamageReportDto(
    public val id: String,
    public val eventId: String,
    public val articleCode: String,
    public val articleName: String,
    public val damagedUnits: Long,
    public val missingUnits: Long,
    public val replacementRD: Long,
    public val notes: String,
    public val status: String = "open",
    public val photoUrl: String? = null,
    public val resolutionNotes: String? = null,
    public val chargePaymentId: String? = null,
)

@Serializable
public data class EventLogisticsDto(
    public val eventId: String,
    public val eventTitle: String,
    public val dateLabel: String,
    public val setupWindow: String,
    public val teardownWindow: String,
    public val venue: String,
    public val leadStaffName: String,
    public val assignedCrew: List<String>? = null,
    public val vehicleName: String,
    public val checklist: List<ChecklistItemDto>? = null,
    public val evidencePhotos: Long,
    public val damageReports: List<DamageReportDto>? = null,
)

@Serializable
public data class EventLogisticsOutputDto(
    public val logistics: EventLogisticsDto,
    public val completionPercent: Long,
)

@Serializable
public data class ToggleChecklistInputDto(
    public val completed: Boolean,
    public val staffName: String,
)

@Serializable
public data class ReportDamageInputDto(
    public val articleCode: String,
    public val articleName: String,
    public val damagedUnits: Long,
    public val missingUnits: Long,
    public val replacementRD: Long,
    public val notes: String,
    public val photoUrl: String? = null,
)

@Serializable
public data class ReportDamageOutputDto(
    public val report: DamageReportDto,
)

@Serializable
public data class OfflineMutationDto(
    public val id: String,
    public val actionLabel: String,
    public val entityCode: String,
    public val createdAt: String,
    public val synced: Boolean,
    public val operationType: String = "",
    public val payloadJson: String = "",
    public val idempotencyKey: String = "",
)

@Serializable
public data class OfflineQueueOutputDto(
    public val queue: List<OfflineMutationDto>? = null,
)

@Serializable
public data class RouteStopDto(
    public val order: Long,
    public val eventId: String,
    public val eventTitle: String,
    public val venue: String,
    public val windowLabel: String,
    public val assignedVehicle: String,
    public val status: String,
)

@Serializable
public data class RouteStopsOutputDto(
    public val stops: List<RouteStopDto>? = null,
)

@Serializable
public data class PaymentDto(
    public val id: String,
    public val receiptNumber: String,
    public val eventId: String,
    public val eventTitle: String,
    public val clientName: String,
    public val amountRD: Long,
    public val method: String,
    public val kind: String,
    public val paidDateLabel: String,
    public val referenceCode: String,
    public val status: String,
    public val bankName: String? = null,
    public val clientEmail: String? = null,
    public val clientId: String? = null,
    public val voucherUrl: String? = null,
    public val ncfType: String? = null,
    public val ncfNumber: String? = null,
    public val clientRnc: String? = null,
    public val subtotalDop: Long = 0,
    public val itbisDop: Long = 0,
    public val securityDepositDop: Long = 0,
    public val referenceNumber: String? = null,
    public val verifiedBy: String? = null,
    public val verifiedAtIso: String? = null,
    public val rejectionReason: String? = null,
    public val gatewaySessionId: String? = null,
    public val gatewayPaymentIntentId: String? = null,
)

@Serializable
public data class PaymentOutputDto(
    public val payment: PaymentDto,
)

@Serializable
public data class PaymentsListOutputDto(
    public val payments: List<PaymentDto>? = null,
)

@Serializable
public data class RegisterPaymentInputDto(
    public val eventId: String,
    public val eventTitle: String,
    public val clientName: String,
    public val amountRD: Long,
    public val method: String,
    public val kind: String,
    public val referenceCode: String,
    public val bankName: String? = null,
    public val clientEmail: String? = null,
    public val voucherUrl: String? = null,
    public val ncfType: String? = null,
    public val clientRnc: String? = null,
    public val securityDepositDop: Long? = null,
    public val referenceNumber: String? = null,
)

@Serializable
public data class FinanceSummaryDto(
    public val monthLabel: String,
    public val octRevenueRD: Long,
    public val accountsReceivableRD: Long,
    public val activeEventsCount: Long,
    public val newQuoteRequests: Long,
    public val pendingVerificationCount: Long,
)

@Serializable
public data class FinanceSummaryOutputDto(
    public val summary: FinanceSummaryDto,
)

@Serializable
public data class QuoteLineDto(
    public val articleCode: String,
    public val name: String,
    public val quantity: Long,
    public val unitPriceRD: Long,
)

@Serializable
public data class QuoteDto(
    public val id: String,
    public val code: String,
    public val eventTitle: String,
    public val clientName: String,
    public val eventDateLabel: String,
    public val venue: String,
    public val guests: Long,
    public val lines: List<QuoteLineDto>? = null,
    public val deliveryFeeRD: Long,
    public val discountRD: Long,
    public val totalRD: Long,
    public val depositPercent: Long,
    public val depositRD: Long,
    public val balanceRD: Long,
    public val status: String,
    public val signedByName: String? = null,
    public val signatureSvg: String? = null,
    public val rejectionReason: String? = null,
    public val notes: String? = null,
    public val clientEmail: String? = null,
    public val clientId: String? = null,
    public val eventId: String? = null,
    public val eventDateIso: String? = null,
    public val packageId: String? = null,
    public val contractPdfUrl: String? = null,
    public val signedAtIso: String? = null,
)

@Serializable
public data class QuoteOutputDto(
    public val quote: QuoteDto,
)

@Serializable
public data class QuotesListOutputDto(
    public val quotes: List<QuoteDto>? = null,
)

@Serializable
public data class CreateQuoteInputDto(
    public val eventTitle: String,
    public val clientName: String,
    public val eventDateLabel: String,
    public val venue: String,
    public val guests: Long,
    public val lines: List<QuoteLineDto>? = null,
    public val deliveryFeeRD: Long,
    public val discountRD: Long,
    public val clientEmail: String? = null,
    public val eventId: String? = null,
    public val eventDateIso: String? = null,
    public val packageId: String? = null,
    public val notes: String? = null,
    public val sendImmediately: Boolean? = null,
)

@Serializable
public data class SendQuoteInputDto(
    public val eventTitle: String? = null,
    public val venue: String? = null,
    public val eventDateLabel: String? = null,
    public val eventDateIso: String? = null,
    public val guests: Long? = null,
    public val lines: List<QuoteLineDto>? = null,
    public val deliveryFeeRD: Long? = null,
    public val discountRD: Long? = null,
    public val notes: String? = null,
)

@Serializable
public data class DeclineQuoteInputDto(
    public val reason: String,
)

@Serializable
public data class SignContractInputDto(
    public val signedByName: String,
    public val signatureSvg: String,
)

@Serializable
public data class RosaProposalInputDto(
    public val eventType: String,
    public val guests: Long,
    public val budgetRD: Long,
    public val excludedCategories: List<String>? = null,
    public val prompt: String? = null,
)

@Serializable
public data class RosaMoodboardProposalDto(
    public val step: Long,
    public val eventType: String,
    public val styleTitle: String,
    public val colorPalette: List<String>? = null,
    public val guests: Long,
    public val budgetRD: Long,
    public val suggestedLines: List<QuoteLineDto>? = null,
    public val estimatedTotalRD: Long,
    public val depositRequiredRD: Long,
    public val assistantReply: String,
    public val excludedCategories: List<String>? = null,
    public val layout: RosaLayoutPlanDto = RosaLayoutPlanDto(),
    public val withinBudget: Boolean = true,
)

@Serializable
public data class RosaProposalOutputDto(
    public val proposal: RosaMoodboardProposalDto,
)

/**
 * HTTP API failure carrying structured backend status and business error code.
 */
public class RosaFiestaApiException(
    public val statusCode: Int,
    public val errorCode: String,
    override val message: String,
) : RuntimeException(message)

@Serializable
private data class ApiErrorEnvelopeDto(
    val code: String = "",
    val message: String = "",
    val detail: String = "",
    val title: String = "",
)

/**
 * Typed Ktor HTTP client for the RosaFiesta Go API (`api/openapi.json`).
 * Automatically attaches `Authorization: Bearer <token>` on every authenticated endpoint when available.
 */
public class RosaFiestaApiClient(
    public val baseUrl: String = "http://localhost:8788",
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val httpClient: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    },
    private val accessTokenProvider: () -> String? = { null },
    private val refreshTokenProvider: () -> String? = { null },
) {
    private val refreshMutex: Mutex = Mutex()

    @Volatile
    private var explicitAccessToken: String? = null

    @Volatile
    private var explicitRefreshToken: String? = null

    @Volatile
    private var onTokensRefreshedCallback: (suspend (accessToken: String, refreshToken: String) -> Unit)? = null

    @Volatile
    private var onSessionExpiredCallback: (suspend () -> Unit)? = null

    @Volatile
    private var sessionExpiredNotifiedForToken: String? = null

    init {
        installAutoRefreshInterceptor()
    }

    /**
     * Registers callbacks triggered when a 401 auto-refresh rotates session tokens or expires permanently.
     */
    public fun setSessionLifecycleCallbacks(
        onTokensRefreshed: suspend (accessToken: String, refreshToken: String) -> Unit,
        onSessionExpired: suspend () -> Unit,
    ) {
        onTokensRefreshedCallback = onTokensRefreshed
        onSessionExpiredCallback = onSessionExpired
    }

    /**
     * Updates or clears the active bearer token used for authenticated endpoints.
     */
    public fun setAccessToken(token: String?) {
        val clean = token?.trim()?.ifEmpty { null }
        explicitAccessToken = clean
        if (clean != null) {
            sessionExpiredNotifiedForToken = null
        }
    }

    /**
     * Updates or clears the active refresh token used for automatic silent renewal on HTTP 401.
     */
    public fun setRefreshToken(token: String?) {
        explicitRefreshToken = token?.trim()?.ifEmpty { null }
    }

    /**
     * Resolves the current bearer token from [setAccessToken] or [accessTokenProvider].
     */
    public fun currentAccessToken(): String? {
        val explicit = explicitAccessToken?.trim().orEmpty()
        if (explicit.isNotEmpty()) {
            return explicit
        }
        val provided = accessTokenProvider()?.trim().orEmpty()
        if (provided.isNotEmpty()) {
            return provided
        }
        return null
    }

    /**
     * Resolves the current refresh token from [setRefreshToken] or [refreshTokenProvider].
     */
    public fun currentRefreshToken(): String? {
        val explicit = explicitRefreshToken?.trim().orEmpty()
        if (explicit.isNotEmpty()) {
            return explicit
        }
        val provided = refreshTokenProvider()?.trim().orEmpty()
        if (provided.isNotEmpty()) {
            return provided
        }
        return null
    }

    private fun installAutoRefreshInterceptor() {
        val sendPlugin = httpClient.pluginOrNull(HttpSend) ?: return
        sendPlugin.intercept { request ->
            val initialCall = execute(request)
            if (initialCall.response.status != HttpStatusCode.Unauthorized) {
                return@intercept initialCall
            }
            val canRetry = shouldRetryUnauthorizedRequest(
                requestUrl = request.url.buildString(),
                hasAuthorizationHeader = request.headers.contains(HttpHeaders.Authorization),
            )
            if (!canRetry) {
                return@intercept initialCall
            }
            val failedToken = request.headers[HttpHeaders.Authorization]
                ?.removePrefix("Bearer ")
                ?.trim()
                .orEmpty()
            val refreshedToken = refreshTokenAfterUnauthorized(failedToken)
                ?: return@intercept initialCall
            request.headers.remove(HttpHeaders.Authorization)
            request.headers.append(HttpHeaders.Authorization, "Bearer $refreshedToken")
            execute(request)
        }
    }

    private fun shouldRetryUnauthorizedRequest(
        requestUrl: String,
        hasAuthorizationHeader: Boolean,
    ): Boolean {
        if (!hasAuthorizationHeader) {
            return false
        }
        val cleanPath = requestUrl.substringBefore("?").trimEnd('/')
        val isAuthLifecycleRoute = listOf(
            "/v1/auth/refresh",
            "/v1/auth/login",
            "/v1/auth/register",
            "/v1/auth/logout",
        ).any { suffix -> cleanPath.endsWith(suffix) }
        return !isAuthLifecycleRoute
    }

    private suspend fun refreshTokenAfterUnauthorized(failedAccessToken: String): String? = refreshMutex.withLock {
        val currentToken = currentAccessToken().orEmpty()
        val alreadyRotatedInFlight = if (currentToken.isNotEmpty()) currentToken != failedAccessToken else false
        if (alreadyRotatedInFlight) {
            return@withLock currentToken
        }
        val activeRefresh = currentRefreshToken()
        if (activeRefresh == null) {
            val hasFailedToken = failedAccessToken.isNotEmpty()
            val notAlreadyNotified = sessionExpiredNotifiedForToken != failedAccessToken
            val shouldNotifyExpired = if (hasFailedToken) notAlreadyNotified else false
            if (shouldNotifyExpired) {
                sessionExpiredNotifiedForToken = failedAccessToken
                setAccessToken(null)
                onSessionExpiredCallback?.invoke()
            }
            return@withLock null
        }
        runCatching {
            performRefreshAccessToken(activeRefresh)
        }.getOrNull()?.accessToken
    }

    private suspend fun performRefreshAccessToken(refreshToken: String): LoginOutputDto {
        val response = httpClient.post("$baseUrl/v1/auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody(RefreshTokenInputDto(refreshToken = refreshToken.trim()))
        }
        return try {
            val output: LoginOutputDto = response.decodeBodyOrThrow()
            setAccessToken(output.accessToken)
            if (output.refreshToken.isNotBlank()) {
                setRefreshToken(output.refreshToken)
            }
            onTokensRefreshedCallback?.invoke(output.accessToken, output.refreshToken)
            output
        } catch (apiError: RosaFiestaApiException) {
            val isUnauthorizedRefresh = response.status == HttpStatusCode.Unauthorized
            val isForbiddenRefresh = response.status == HttpStatusCode.Forbidden
            val isRejectedRefresh = if (isUnauthorizedRefresh) true else isForbiddenRefresh
            if (isRejectedRefresh) {
                val expiredToken = currentAccessToken().orEmpty()
                setAccessToken(null)
                setRefreshToken(null)
                if (sessionExpiredNotifiedForToken != expiredToken) {
                    sessionExpiredNotifiedForToken = expiredToken
                    onSessionExpiredCallback?.invoke()
                }
            }
            throw apiError
        }
    }

    private fun HttpRequestBuilder.applyBearerAuth(overrideToken: String? = null) {
        val explicitOverride = overrideToken?.trim().orEmpty()
        val resolvedToken = if (explicitOverride.isNotEmpty()) {
            explicitOverride
        } else {
            currentAccessToken().orEmpty()
        }
        if (resolvedToken.isNotEmpty()) {
            header(HttpHeaders.Authorization, "Bearer $resolvedToken")
        }
    }

    public suspend fun fetchHealthRaw(): String = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/healthz").bodyAsText()
    }

    public suspend fun fetchHealth(): HealthOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/healthz").decodeBodyOrThrow()
    }

    public suspend fun fetchReady(): HealthOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/readyz").decodeBodyOrThrow()
    }

    internal fun internalHttpClient(): HttpClient = httpClient

    internal fun internalBaseUrl(): String = baseUrl

    private suspend inline fun <reified T> HttpResponse.decodeBodyOrThrow(): T {
        if (status.isSuccess()) {
            return body()
        }
        val rawBody = runCatching { bodyAsText() }.getOrDefault("")
        val parsed = runCatching {
            errorEnvelopeJson.decodeFromString<ApiErrorEnvelopeDto>(rawBody)
        }.getOrNull()
        val resolvedCode = parsed?.code?.trim().orEmpty().ifEmpty { "HTTP_${status.value}" }
        val resolvedMessage = parsed?.message?.trim().orEmpty()
            .ifEmpty { parsed?.detail?.trim().orEmpty() }
            .ifEmpty { parsed?.title?.trim().orEmpty() }
            .ifEmpty { status.description }
        throw RosaFiestaApiException(
            statusCode = status.value,
            errorCode = resolvedCode,
            message = resolvedMessage,
        )
    }

    private fun applyIssuedSessionTokens(output: LoginOutputDto): LoginOutputDto {
        setAccessToken(output.accessToken)
        if (output.refreshToken.isNotBlank()) {
            setRefreshToken(output.refreshToken)
        }
        return output
    }

    /**
     * Verifies account credentials against `/v1/auth/login` without mutating active session tokens.
     */
    public suspend fun verifyCredentials(input: LoginInputDto): Boolean = withContext(ioDispatcher) {
        val response = httpClient.post("$baseUrl/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(input)
        }
        response.status.isSuccess()
    }

    public suspend fun login(input: LoginInputDto): LoginOutputDto = withContext(ioDispatcher) {
        val response = httpClient.post("$baseUrl/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(input)
        }
        applyIssuedSessionTokens(response.decodeBodyOrThrow())
    }

    public suspend fun register(input: RegisterInputDto): LoginOutputDto = withContext(ioDispatcher) {
        val response = httpClient.post("$baseUrl/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(input)
        }
        applyIssuedSessionTokens(response.decodeBodyOrThrow())
    }

    /**
     * Exchanges a single-use refresh token for a rotated access + refresh session pair.
     */
    public suspend fun refreshAccessToken(
        refreshToken: String = currentRefreshToken().orEmpty(),
    ): LoginOutputDto = withContext(ioDispatcher) {
        val cleanRefresh = refreshToken.trim()
        require(cleanRefresh.isNotEmpty()) { "Refresh token is required." }
        refreshMutex.withLock {
            performRefreshAccessToken(cleanRefresh)
        }
    }

    private companion object {
        val errorEnvelopeJson = Json { ignoreUnknownKeys = true }
    }

    public suspend fun logout(
        refreshToken: String = currentRefreshToken().orEmpty(),
        bearerToken: String = "",
    ): LogoutOutputDto = withContext(ioDispatcher) {
        val cleanRefresh = refreshToken.trim()
        val cleanBearer = bearerToken.trim()
        try {
            httpClient.post("$baseUrl/v1/auth/logout") {
                applyBearerAuth(cleanBearer)
                if (cleanRefresh.isNotEmpty()) {
                    header("X-Refresh-Token", cleanRefresh)
                }
            }.decodeBodyOrThrow()
        } finally {
            setAccessToken(null)
            setRefreshToken(null)
        }
    }

    public suspend fun requestOtp(input: OtpRequestInputDto): OtpRequestResultDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/auth/otp/request") {
            contentType(ContentType.Application.Json)
            setBody(input)
        }.decodeBodyOrThrow()
    }

    public suspend fun requestOtp(email: String): OtpRequestResultDto = requestOtp(OtpRequestInputDto(email = email.trim()))

    public suspend fun verifyOtp(input: OtpVerifyInputDto): OtpVerifyResultDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/auth/otp/verify") {
            contentType(ContentType.Application.Json)
            setBody(input)
        }.decodeBodyOrThrow()
    }

    public suspend fun verifyOtp(email: String, code: String): OtpVerifyResultDto = verifyOtp(OtpVerifyInputDto(email = email.trim(), code = code.trim()))

    public suspend fun resetPassword(input: ResetPasswordInputDto): LoginOutputDto = withContext(ioDispatcher) {
        val output: LoginOutputDto = httpClient.post("$baseUrl/v1/auth/reset-password") {
            contentType(ContentType.Application.Json)
            setBody(input)
        }.decodeBodyOrThrow()
        applyIssuedSessionTokens(output)
    }

    public suspend fun loginWithOAuth(input: OAuthLoginInputDto): LoginOutputDto = withContext(ioDispatcher) {
        val output: LoginOutputDto = httpClient.post("$baseUrl/v1/auth/oauth") {
            contentType(ContentType.Application.Json)
            setBody(input)
        }.decodeBodyOrThrow()
        applyIssuedSessionTokens(output)
    }

    public suspend fun loginWithOAuth(provider: String, idToken: String): LoginOutputDto = loginWithOAuth(OAuthLoginInputDto(provider = provider.trim(), idToken = idToken.trim()))

    public suspend fun startOAuthAuthorization(
        provider: String,
        redirectUri: String,
        codeChallenge: String,
        state: String,
    ): OAuthStartResultDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/auth/oauth/${provider.trim().lowercase()}/start") {
            parameter("redirectUri", redirectUri.trim())
            parameter("codeChallenge", codeChallenge.trim())
            parameter("state", state.trim())
        }.decodeBodyOrThrow()
    }

    public suspend fun exchangeOAuthCode(input: OAuthExchangeInputDto): LoginOutputDto = withContext(ioDispatcher) {
        val output: LoginOutputDto = httpClient.post("$baseUrl/v1/auth/oauth/exchange") {
            contentType(ContentType.Application.Json)
            setBody(input)
        }.decodeBodyOrThrow()
        applyIssuedSessionTokens(output)
    }

    public suspend fun registerDevicePushToken(input: PushTokenInputDto): PushTokenOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/devices/push-token") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.decodeBodyOrThrow()
    }

    public suspend fun unregisterDevicePushToken(token: String = ""): DeletePushTokenOutputDto = withContext(ioDispatcher) {
        httpClient.delete("$baseUrl/v1/devices/push-token") {
            applyBearerAuth()
            if (token.isNotBlank()) {
                parameter("token", token.trim())
            }
        }.decodeBodyOrThrow()
    }

    public suspend fun fetchCurrentUser(bearerToken: String = ""): SwitchViewOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/auth/me") {
            applyBearerAuth(bearerToken)
        }.decodeBodyOrThrow()
    }

    public suspend fun updateMyProfile(input: UpdateProfileInputDto): SwitchViewOutputDto = withContext(ioDispatcher) {
        httpClient.patch("$baseUrl/v1/auth/me") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.decodeBodyOrThrow()
    }

    public suspend fun changePassword(input: ChangePasswordInputDto): PasswordChangedOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/auth/change-password") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.decodeBodyOrThrow()
    }

    public suspend fun listRegisteredUsers(): RegisteredUsersOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/auth/users") {
            applyBearerAuth()
        }.decodeBodyOrThrow()
    }

    public suspend fun switchView(bearerToken: String, targetView: String): SwitchViewOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/auth/switch-view") {
            applyBearerAuth(bearerToken)
            contentType(ContentType.Application.Json)
            setBody(SwitchViewInputDto(view = targetView))
        }.decodeBodyOrThrow()
    }

    public suspend fun switchView(targetView: String): SwitchViewOutputDto = switchView(bearerToken = "", targetView = targetView)

    public suspend fun deleteAccount(bearerToken: String = ""): DeleteAccountOutputDto = withContext(ioDispatcher) {
        httpClient.delete("$baseUrl/v1/account") {
            applyBearerAuth(bearerToken)
        }.decodeBodyOrThrow()
    }

    public suspend fun listClients(): ClientsOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/admin/clients") {
            applyBearerAuth()
        }.decodeBodyOrThrow()
    }

    public suspend fun updateClient(clientId: String, input: UpdateClientInputDto): ClientOutputDto = withContext(ioDispatcher) {
        httpClient.patch("$baseUrl/v1/admin/clients/${clientId.trim()}") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.decodeBodyOrThrow()
    }

    public suspend fun listStaff(): StaffOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/admin/staff") {
            applyBearerAuth()
        }.decodeBodyOrThrow()
    }

    public suspend fun inviteStaff(input: InviteStaffInputDto): StaffMemberOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/admin/staff") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.decodeBodyOrThrow()
    }

    public suspend fun updateStaffMember(staffId: String, input: UpdateStaffInputDto): StaffMemberOutputDto = withContext(ioDispatcher) {
        httpClient.patch("$baseUrl/v1/admin/staff/${staffId.trim()}") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.decodeBodyOrThrow()
    }

    public suspend fun listArticles(
        query: String? = null,
        kind: String? = null,
        includeHidden: Boolean? = null,
    ): ArticlesOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/catalog/articles") {
            applyBearerAuth()
            if (!query.isNullOrBlank()) {
                parameter("q", query.trim())
            }
            if (!kind.isNullOrBlank()) {
                parameter("kind", kind.trim())
            }
            if (includeHidden != null) {
                parameter("includeHidden", includeHidden)
            }
        }.decodeBodyOrThrow()
    }

    public suspend fun listBalloonProducts(): ArticlesOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/catalog/balloons").decodeBodyOrThrow()
    }

    public suspend fun createCatalogArticle(input: CreateArticleInputDto): ArticleOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/catalog/articles") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.decodeBodyOrThrow()
    }

    public suspend fun updateCatalogArticle(articleId: String, input: UpdateArticleInputDto): ArticleOutputDto = withContext(ioDispatcher) {
        httpClient.patch("$baseUrl/v1/catalog/articles/${articleId.trim()}") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.decodeBodyOrThrow()
    }

    public suspend fun deleteCatalogArticle(articleId: String): DeleteArticleOutputDto = withContext(ioDispatcher) {
        httpClient.delete("$baseUrl/v1/catalog/articles/${articleId.trim()}") {
            applyBearerAuth()
        }.decodeBodyOrThrow()
    }

    public suspend fun getCatalogAvailability(articleId: String, month: String? = null): AvailabilityOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/catalog/availability") {
            parameter("articleId", articleId.trim())
            if (!month.isNullOrBlank()) {
                parameter("month", month.trim())
            }
        }.decodeBodyOrThrow()
    }

    public suspend fun checkAvailabilityRange(
        dateIso: String,
        articleId: String,
        quantity: Int,
        excludeEventId: String = "",
    ): AvailabilityCheckOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/catalog/availability-check") {
            applyBearerAuth()
            parameter("date", dateIso.trim())
            parameter("articleId", articleId.trim())
            parameter("quantity", quantity)
            if (excludeEventId.isNotBlank()) {
                parameter("excludeEventId", excludeEventId.trim())
            }
        }.decodeBodyOrThrow()
    }

    @OptIn(ExperimentalEncodingApi::class)
    public suspend fun uploadMedia(
        filename: String,
        mimeType: String,
        purpose: String,
        bytes: ByteArray,
    ): MediaAssetDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/media") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(
                MediaUploadInputDto(
                    filename = filename.trim(),
                    mimeType = mimeType.trim(),
                    purpose = purpose.trim(),
                    dataBase64 = Base64.encode(bytes),
                ),
            )
        }.decodeBodyOrThrow()
    }

    public suspend fun fetchMediaBytes(mediaPathOrUrl: String): ByteArray = withContext(ioDispatcher) {
        val target = mediaPathOrUrl.trim()
        val isAbsoluteHttp = target.startsWith("http://")
        val isAbsoluteHttps = target.startsWith("https://")
        val resolvedUrl = if (isAbsoluteHttp) {
            target
        } else if (isAbsoluteHttps) {
            target
        } else {
            val normalizedPath = if (target.startsWith("/")) target else "/$target"
            "$baseUrl$normalizedPath"
        }
        val response = httpClient.get(resolvedUrl) {
            applyBearerAuth()
        }
        if (response.status.isSuccess()) {
            response.readRawBytes()
        } else {
            throw RosaFiestaApiException(
                statusCode = response.status.value,
                errorCode = "MEDIA_FETCH_FAILED",
                message = "No se pudo cargar la imagen ($resolvedUrl).",
            )
        }
    }

    public suspend fun listCategories(): CategoriesOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/catalog/categories").decodeBodyOrThrow()
    }

    public suspend fun createCatalogCategory(input: SaveCategoryInputDto): CategoryOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/catalog/categories") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.decodeBodyOrThrow()
    }

    public suspend fun updateCatalogCategory(categoryId: String, input: UpdateCategoryInputDto): CategoryOutputDto = withContext(ioDispatcher) {
        httpClient.patch("$baseUrl/v1/catalog/categories/${categoryId.trim()}") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.decodeBodyOrThrow()
    }

    public suspend fun deleteCatalogCategory(categoryId: String): DeleteCategoryOutputDto = withContext(ioDispatcher) {
        httpClient.delete("$baseUrl/v1/catalog/categories/${categoryId.trim()}") {
            applyBearerAuth()
        }.decodeBodyOrThrow()
    }

    public suspend fun listCatalogTrends(includeInactive: Boolean = true): TrendsOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/catalog/trends") {
            parameter("includeInactive", includeInactive)
        }.decodeBodyOrThrow()
    }

    public suspend fun createCatalogTrend(input: SaveTrendInputDto): TrendOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/catalog/trends") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.decodeBodyOrThrow()
    }

    public suspend fun updateCatalogTrend(trendId: String, input: SaveTrendInputDto): TrendOutputDto = withContext(ioDispatcher) {
        httpClient.put("$baseUrl/v1/catalog/trends/${trendId.trim()}") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.decodeBodyOrThrow()
    }

    public suspend fun deleteCatalogTrend(trendId: String): DeleteTrendOutputDto = withContext(ioDispatcher) {
        httpClient.delete("$baseUrl/v1/catalog/trends/${trendId.trim()}") {
            applyBearerAuth()
        }.decodeBodyOrThrow()
    }

    public suspend fun getSeasonBanner(): SeasonBannerOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/catalog/season-banner").decodeBodyOrThrow()
    }

    public suspend fun saveSeasonBanner(input: SaveSeasonBannerInputDto): SeasonBannerOutputDto = withContext(ioDispatcher) {
        httpClient.put("$baseUrl/v1/catalog/season-banner") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.decodeBodyOrThrow()
    }

    public suspend fun listPackages(includeHidden: Boolean = false): PackagesOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/catalog/packages") {
            applyBearerAuth()
            if (includeHidden) {
                parameter("includeHidden", true)
            }
        }.decodeBodyOrThrow()
    }

    public suspend fun deleteCatalogPackage(packageId: String): DeletePackageOutputDto = withContext(ioDispatcher) {
        httpClient.delete("$baseUrl/v1/catalog/packages/${packageId.trim()}") {
            applyBearerAuth()
        }.decodeBodyOrThrow()
    }

    public suspend fun createCatalogPackage(input: SavePackageInputDto): PackageOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/catalog/packages") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.decodeBodyOrThrow()
    }

    public suspend fun updateCatalogPackage(packageId: String, input: UpdatePackageInputDto): PackageOutputDto = withContext(ioDispatcher) {
        httpClient.put("$baseUrl/v1/catalog/packages/${packageId.trim()}") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.decodeBodyOrThrow()
    }

    public suspend fun listEvents(status: String? = null): EventsListOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/events") {
            applyBearerAuth()
            if (!status.isNullOrBlank()) {
                parameter("status", status.trim())
            }
        }.decodeBodyOrThrow()
    }

    public suspend fun createRemoteEvent(input: CreateEventInputDto): EventDetailOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/events") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.decodeBodyOrThrow()
    }

    public suspend fun listEventTemplates(): TemplatesOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/events/templates").decodeBodyOrThrow()
    }

    public suspend fun getEventById(id: String): EventDetailOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/events/${id.trim()}") {
            applyBearerAuth()
        }.decodeBodyOrThrow()
    }

    public suspend fun updateRemoteEvent(eventId: String, input: UpdateEventInputDto): EventDetailOutputDto = withContext(ioDispatcher) {
        httpClient.patch("$baseUrl/v1/events/${eventId.trim()}") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.decodeBodyOrThrow()
    }

    public suspend fun cancelEvent(id: String, reason: String = ""): CancelEventOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/events/${id.trim()}/cancel") {
            applyBearerAuth()
            if (reason.isNotBlank()) {
                contentType(ContentType.Application.Json)
                setBody(CancelEventInputDto(reason = reason.trim()))
            }
        }.decodeBodyOrThrow()
    }

    public suspend fun closeoutEvent(id: String, notes: String): EventDetailOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/events/${id.trim()}/closeout") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(CloseoutEventInputDto(notes = notes.trim()))
        }.decodeBodyOrThrow()
    }

    public suspend fun requestDateChange(id: String, input: DateChangeInputDto): DateChangeOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/events/${id.trim()}/date-change") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.decodeBodyOrThrow()
    }

    public suspend fun duplicateRemoteEvent(eventId: String): EventDetailOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/events/${eventId.trim()}/duplicate") {
            applyBearerAuth()
        }.decodeBodyOrThrow()
    }

    public suspend fun listEventGuests(eventId: String): GuestsListOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/events/${eventId.trim()}/guests") {
            applyBearerAuth()
        }.decodeBodyOrThrow()
    }

    public suspend fun addEventGuest(eventId: String, input: AddGuestInputDto): GuestOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/events/${eventId.trim()}/guests") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.decodeBodyOrThrow()
    }

    public suspend fun updateEventGuest(
        eventId: String,
        guestId: String,
        input: UpdateGuestInputDto,
    ): GuestOutputDto = withContext(ioDispatcher) {
        httpClient.patch("$baseUrl/v1/events/${eventId.trim()}/guests/${guestId.trim()}") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.decodeBodyOrThrow()
    }

    public suspend fun listEventSchedule(eventId: String): ScheduleListOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/events/${eventId.trim()}/schedule") {
            applyBearerAuth()
        }.decodeBodyOrThrow()
    }

    public suspend fun addEventScheduleBlock(
        eventId: String,
        input: AddScheduleBlockInputDto,
    ): ScheduleBlockOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/events/${eventId.trim()}/schedule") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.decodeBodyOrThrow()
    }

    public suspend fun deleteEventScheduleBlock(
        eventId: String,
        blockId: String,
    ): DeleteScheduleBlockOutputDto = withContext(ioDispatcher) {
        httpClient.delete("$baseUrl/v1/events/${eventId.trim()}/schedule/${blockId.trim()}") {
            applyBearerAuth()
        }.decodeBodyOrThrow()
    }

    public suspend fun listChatThreads(): ChatThreadsOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/chats") {
            applyBearerAuth()
        }.decodeBodyOrThrow()
    }

    public suspend fun listThreadMessages(threadId: String): ChatMessagesOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/chats/${threadId.trim()}/messages") {
            applyBearerAuth()
        }.decodeBodyOrThrow()
    }

    public suspend fun sendThreadMessage(
        threadId: String,
        input: PostChatMessageInputDto,
    ): ChatMessageOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/chats/${threadId.trim()}/messages") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.decodeBodyOrThrow()
    }

    public suspend fun getEventLogistics(eventId: String): EventLogisticsOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/operations/events/${eventId.trim()}") {
            applyBearerAuth()
        }.decodeBodyOrThrow()
    }

    public suspend fun toggleChecklistItem(
        eventId: String,
        itemId: String,
        input: ToggleChecklistInputDto,
    ): EventLogisticsOutputDto = withContext(ioDispatcher) {
        httpClient.patch("$baseUrl/v1/operations/events/${eventId.trim()}/checklist/${itemId.trim()}") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.decodeBodyOrThrow()
    }

    public suspend fun reportDamage(
        eventId: String,
        input: ReportDamageInputDto,
    ): ReportDamageOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/operations/events/${eventId.trim()}/damages") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.decodeBodyOrThrow()
    }

    public suspend fun resolveDamage(
        damageId: String,
        notes: String = "",
    ): ReportDamageOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/operations/damage/${damageId.trim()}/resolve") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(ResolveDamageInputDto(notes = notes.trim(), resolutionNotes = notes.trim()))
        }.decodeBodyOrThrow()
    }

    public suspend fun listDamages(eventId: String? = null): DamagesListOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/operations/damages") {
            applyBearerAuth()
            if (!eventId.isNullOrBlank()) {
                parameter("eventId", eventId.trim())
            }
        }.decodeBodyOrThrow()
    }

    public suspend fun listOfflineQueue(): OfflineQueueOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/operations/offline-queue") {
            applyBearerAuth()
        }.decodeBodyOrThrow()
    }

    public suspend fun syncOfflineQueue(): OfflineQueueOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/operations/offline-queue/sync") {
            applyBearerAuth()
        }.decodeBodyOrThrow()
    }

    public suspend fun syncOfflineQueue(
        mutations: List<OfflineMutationSyncItemDto>,
    ): OfflineQueueOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/operations/offline-queue/sync") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(OfflineSyncInputDto(mutations = mutations))
        }.decodeBodyOrThrow()
    }

    public suspend fun listTodayRoutes(): RouteStopsOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/operations/routes/today") {
            applyBearerAuth()
        }.decodeBodyOrThrow()
    }

    public suspend fun listPayments(eventId: String? = null): PaymentsListOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/payments") {
            applyBearerAuth()
            if (!eventId.isNullOrBlank()) {
                parameter("eventId", eventId.trim())
            }
        }.decodeBodyOrThrow()
    }

    public suspend fun registerPayment(input: RegisterPaymentInputDto): PaymentOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/payments") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.decodeBodyOrThrow()
    }

    public suspend fun verifyPayment(
        paymentId: String,
        notes: String = "",
    ): PaymentOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/payments/${paymentId.trim()}/verify") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(VerifyPaymentInputDto(notes = notes.trim(), reason = notes.trim()))
        }.decodeBodyOrThrow()
    }

    public suspend fun rejectPayment(
        paymentId: String,
        reason: String,
    ): PaymentOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/payments/${paymentId.trim()}/reject") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(RejectPaymentInputDto(reason = reason.trim()))
        }.decodeBodyOrThrow()
    }

    public suspend fun createCardSession(input: CreateCardSessionInputDto): CardSessionOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/payments/card-session") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.decodeBodyOrThrow()
    }

    public suspend fun exportDgii607(period: String = "2026-10"): DGII607OutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/payments/dgii-607") {
            applyBearerAuth()
            if (period.isNotBlank()) {
                parameter("period", period.trim())
            }
        }.decodeBodyOrThrow()
    }

    public suspend fun getFinanceSummary(): FinanceSummaryOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/payments/summary") {
            applyBearerAuth()
        }.decodeBodyOrThrow()
    }

    public suspend fun listRefunds(): RefundsListOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/refunds") {
            applyBearerAuth()
        }.decodeBodyOrThrow()
    }

    public suspend fun approveRefund(
        refundId: String,
        input: ApproveRefundInputDto? = null,
    ): RefundOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/refunds/${refundId.trim()}/approve") {
            applyBearerAuth()
            if (input != null) {
                contentType(ContentType.Application.Json)
                setBody(input)
            }
        }.decodeBodyOrThrow()
    }

    public suspend fun listQuotes(status: String? = null): QuotesListOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/quotes") {
            applyBearerAuth()
            if (!status.isNullOrBlank()) {
                parameter("status", status.trim())
            }
        }.decodeBodyOrThrow()
    }

    public suspend fun createQuote(input: CreateQuoteInputDto): QuoteOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/quotes") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.decodeBodyOrThrow()
    }

    public suspend fun sendQuote(id: String, input: SendQuoteInputDto): QuoteOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/quotes/${id.trim()}/send") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.decodeBodyOrThrow()
    }

    public suspend fun getQuoteById(id: String): QuoteOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/quotes/${id.trim()}") {
            applyBearerAuth()
        }.decodeBodyOrThrow()
    }

    public suspend fun declineQuote(id: String, input: DeclineQuoteInputDto): QuoteOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/quotes/${id.trim()}/decline") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.decodeBodyOrThrow()
    }

    public suspend fun duplicateQuote(quoteId: String): QuoteOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/quotes/${quoteId.trim()}/duplicate") {
            applyBearerAuth()
        }.decodeBodyOrThrow()
    }

    public suspend fun signContract(id: String, input: SignContractInputDto): QuoteOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/quotes/${id.trim()}/sign") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.decodeBodyOrThrow()
    }

    public suspend fun generateRosaProposal(input: RosaProposalInputDto): RosaProposalOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/quotes/rosa-proposal") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.decodeBodyOrThrow()
    }

    public suspend fun sendRosaChat(input: RosaChatInputDto): RosaChatResponseDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/rosa/chat") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.decodeBodyOrThrow()
    }

    public suspend fun transcribeRosaAudio(input: RosaTranscribeInputDto): TranscriptionResultDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/rosa/transcribe") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.decodeBodyOrThrow()
    }

    public suspend fun transcribeRosaAudio(
        wavBase64: String,
        locale: String = "es-DO",
    ): TranscriptionResultDto = transcribeRosaAudio(
        RosaTranscribeInputDto(
            audioBase64 = wavBase64,
            locale = locale,
            mimeType = "audio/wav",
        ),
    )

    public suspend fun listReviews(eventId: String? = null): ReviewsListOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/reviews") {
            applyBearerAuth()
            if (!eventId.isNullOrBlank()) {
                parameter("eventId", eventId.trim())
            }
        }.decodeBodyOrThrow()
    }

    public suspend fun createReview(input: CreateReviewInputDto): ReviewOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/reviews") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.decodeBodyOrThrow()
    }

    public suspend fun replyToReview(reviewId: String, input: ReplyReviewInputDto): ReviewOutputDto = withContext(ioDispatcher) {
        httpClient.patch("$baseUrl/v1/reviews/${reviewId.trim()}/reply") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.decodeBodyOrThrow()
    }

    public suspend fun replyToReview(reviewId: String, adminReply: String): ReviewOutputDto = replyToReview(reviewId = reviewId, input = ReplyReviewInputDto(adminReply = adminReply))

    public suspend fun listSuppliers(category: String? = null): SuppliersListOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/operations/suppliers") {
            applyBearerAuth()
            if (!category.isNullOrBlank()) {
                parameter("category", category.trim())
            }
        }.decodeBodyOrThrow()
    }

    public suspend fun createSupplier(input: UpsertSupplierInputDto): SupplierOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/operations/suppliers") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.decodeBodyOrThrow()
    }

    public suspend fun listLeads(stage: String? = null): LeadsListOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/operations/leads") {
            applyBearerAuth()
            if (!stage.isNullOrBlank()) {
                parameter("stage", stage.trim())
            }
        }.decodeBodyOrThrow()
    }

    public suspend fun createLead(input: CreateLeadInputDto): LeadOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/operations/leads") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.decodeBodyOrThrow()
    }

    public suspend fun updateLead(leadId: String, input: UpdateLeadInputDto): LeadOutputDto = withContext(ioDispatcher) {
        httpClient.patch("$baseUrl/v1/operations/leads/${leadId.trim()}") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.decodeBodyOrThrow()
    }

    public suspend fun getPublicSettings(): PublicSettingsOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/operations/settings/public").decodeBodyOrThrow()
    }

    public suspend fun getAdminSettings(): SettingsOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/operations/settings") {
            applyBearerAuth()
        }.decodeBodyOrThrow()
    }

    public suspend fun updateAdminSettings(input: UpdateSettingsInputDto): SettingsOutputDto = withContext(ioDispatcher) {
        httpClient.put("$baseUrl/v1/operations/settings") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.decodeBodyOrThrow()
    }

    public suspend fun listNotifications(): NotificationsOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/notifications") {
            applyBearerAuth()
        }.decodeBodyOrThrow()
    }

    public suspend fun markNotificationRead(id: String): NotificationOutputDto = withContext(ioDispatcher) {
        httpClient.patch("$baseUrl/v1/notifications/${id.trim()}/read") {
            applyBearerAuth()
        }.decodeBodyOrThrow()
    }

    public suspend fun listInsurancePolicies(): InsuranceOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/operations/insurance") {
            applyBearerAuth()
        }.decodeBodyOrThrow()
    }

    public suspend fun listAuditLogs(): AuditLogsOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/operations/audit-logs") {
            applyBearerAuth()
        }.decodeBodyOrThrow()
    }

    public suspend fun listBotFaqs(): BotFaqsOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/operations/bot/faqs") {
            applyBearerAuth()
        }.decodeBodyOrThrow()
    }

    public suspend fun createBotFaq(input: UpsertBotFaqInputDto): BotFaqOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/operations/bot/faqs") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.decodeBodyOrThrow()
    }

    public suspend fun listBotKeywords(): BotKeywordsOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/operations/bot/keywords") {
            applyBearerAuth()
        }.decodeBodyOrThrow()
    }

    public suspend fun createBotKeyword(input: UpsertBotKeywordInputDto): BotKeywordOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/operations/bot/keywords") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.decodeBodyOrThrow()
    }

    public suspend fun listBotThreads(): BotThreadsOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/operations/bot/threads") {
            applyBearerAuth()
        }.decodeBodyOrThrow()
    }

    public suspend fun updateBotThread(id: String, input: UpdateBotThreadInputDto): BotThreadOutputDto = withContext(ioDispatcher) {
        httpClient.patch("$baseUrl/v1/operations/bot/threads/${id.trim()}") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.decodeBodyOrThrow()
    }

    public suspend fun sendWhatsAppReply(input: SendWhatsAppReplyInputDto): BotThreadOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/bot/whatsapp/send") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.decodeBodyOrThrow()
    }

    public suspend fun exportReport(
        type: String = "ingresos",
        period: String = "2026-10",
    ): ReportExportOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/reports/export") {
            applyBearerAuth()
            parameter("type", type.trim())
            parameter("period", period.trim())
        }.decodeBodyOrThrow()
    }

    public suspend fun getOAuthConfig(): OAuthConfigOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/auth/oauth/config").decodeBodyOrThrow()
    }

    public suspend fun recoverUsername(identifier: String): RecoverUsernameOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/auth/recover-username") {
            contentType(ContentType.Application.Json)
            setBody(RecoverUsernameInputDto(identifier = identifier.trim()))
        }.decodeBodyOrThrow()
    }

    public suspend fun updateMyPreferences(input: UpdatePreferencesInputDto): SwitchViewOutputDto = withContext(ioDispatcher) {
        httpClient.patch("$baseUrl/v1/auth/me/preferences") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.decodeBodyOrThrow()
    }

    public suspend fun sendPaymentReminder(
        eventId: String,
        input: PaymentReminderInputDto,
    ): PaymentReminderOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/events/${eventId.trim()}/payment-reminder") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.decodeBodyOrThrow()
    }

    public suspend fun confirmCardSession(sessionId: String): PaymentOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/payments/card-session/${sessionId.trim()}/confirm") {
            applyBearerAuth()
        }.decodeBodyOrThrow()
    }

    public suspend fun getPaymentsAnalytics(range: String = "30d"): PaymentsAnalyticsOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/payments/analytics") {
            applyBearerAuth()
            parameter("range", range.trim().ifEmpty { "30d" })
        }.decodeBodyOrThrow()
    }

    public suspend fun generateRosaSketch(input: RosaSketchInputDto): RosaSketchResponseDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/quotes/rosa/sketch") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.decodeBodyOrThrow()
    }

    public suspend fun listFavorites(): FavoritesOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/auth/me/favorites") {
            applyBearerAuth()
        }.decodeBodyOrThrow()
    }

    public suspend fun addFavorite(articleId: String): FavoritesOutputDto = withContext(ioDispatcher) {
        httpClient.put("$baseUrl/v1/auth/me/favorites/${articleId.trim()}") {
            applyBearerAuth()
        }.decodeBodyOrThrow()
    }

    public suspend fun removeFavorite(articleId: String): FavoritesOutputDto = withContext(ioDispatcher) {
        httpClient.delete("$baseUrl/v1/auth/me/favorites/${articleId.trim()}") {
            applyBearerAuth()
        }.decodeBodyOrThrow()
    }

    public suspend fun deleteStaffMember(staffId: String): DeleteEntityOutputDto = withContext(ioDispatcher) {
        httpClient.delete("$baseUrl/v1/admin/staff/${staffId.trim()}") {
            applyBearerAuth()
        }.decodeBodyOrThrow()
    }

    public suspend fun deleteEventGuest(eventId: String, guestId: String): DeleteEntityOutputDto = withContext(ioDispatcher) {
        httpClient.delete("$baseUrl/v1/events/${eventId.trim()}/guests/${guestId.trim()}") {
            applyBearerAuth()
        }.decodeBodyOrThrow()
    }

    public suspend fun listEventTasks(eventId: String): EventTasksListOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/events/${eventId.trim()}/tasks") {
            applyBearerAuth()
        }.decodeBodyOrThrow()
    }

    public suspend fun createEventTask(eventId: String, input: CreateEventTaskInputDto): EventTaskOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/events/${eventId.trim()}/tasks") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.decodeBodyOrThrow()
    }

    public suspend fun updateEventTask(
        eventId: String,
        taskId: String,
        input: UpdateEventTaskInputDto,
    ): EventTaskOutputDto = withContext(ioDispatcher) {
        httpClient.patch("$baseUrl/v1/events/${eventId.trim()}/tasks/${taskId.trim()}") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.decodeBodyOrThrow()
    }

    public suspend fun deleteEventTask(eventId: String, taskId: String): DeleteEntityOutputDto = withContext(ioDispatcher) {
        httpClient.delete("$baseUrl/v1/events/${eventId.trim()}/tasks/${taskId.trim()}") {
            applyBearerAuth()
        }.decodeBodyOrThrow()
    }

    public suspend fun patchSupplier(id: String, input: PatchSupplierInputDto): SupplierOutputDto = withContext(ioDispatcher) {
        httpClient.patch("$baseUrl/v1/operations/suppliers/${id.trim()}") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.decodeBodyOrThrow()
    }

    public suspend fun deleteSupplier(id: String): DeleteEntityOutputDto = withContext(ioDispatcher) {
        httpClient.delete("$baseUrl/v1/operations/suppliers/${id.trim()}") {
            applyBearerAuth()
        }.decodeBodyOrThrow()
    }

    public suspend fun deleteBotFaq(id: String): DeleteEntityOutputDto = withContext(ioDispatcher) {
        httpClient.delete("$baseUrl/v1/operations/bot/faqs/${id.trim()}") {
            applyBearerAuth()
        }.decodeBodyOrThrow()
    }

    public suspend fun deleteBotKeyword(id: String): DeleteEntityOutputDto = withContext(ioDispatcher) {
        httpClient.delete("$baseUrl/v1/operations/bot/keywords/${id.trim()}") {
            applyBearerAuth()
        }.decodeBodyOrThrow()
    }

    public suspend fun downloadQuoteContractPdf(quoteId: String): ByteArray = withContext(ioDispatcher) {
        val response = httpClient.get("$baseUrl/v1/quotes/${quoteId.trim()}/contract") {
            applyBearerAuth()
        }
        if (response.status.isSuccess()) {
            response.readRawBytes()
        } else {
            throw RosaFiestaApiException(
                statusCode = response.status.value,
                errorCode = "CONTRACT_PDF_FAILED",
                message = "No se pudo descargar el contrato PDF.",
            )
        }
    }
}
