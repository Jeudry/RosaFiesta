package com.jeudry.rosafiesta.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
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
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.concurrent.Volatile

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
    public val createdAt: String = "",
    public val avatarKey: String? = null,
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
    public val user: UserDto,
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
    public val clientEmail: String? = null,
    public val clientId: String? = null,
    public val eventId: String? = null,
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
) {
    @Volatile
    private var explicitAccessToken: String? = null

    /**
     * Updates or clears the active bearer token used for authenticated endpoints.
     */
    public fun setAccessToken(token: String?) {
        explicitAccessToken = token?.trim()?.ifEmpty { null }
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
        httpClient.get("$baseUrl/healthz").body()
    }

    public suspend fun fetchReady(): HealthOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/readyz").body()
    }

    private suspend fun HttpResponse.decodeLoginOrThrow(): LoginOutputDto {
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

    public suspend fun login(input: LoginInputDto): LoginOutputDto = withContext(ioDispatcher) {
        val response = httpClient.post("$baseUrl/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(input)
        }
        response.decodeLoginOrThrow()
    }

    public suspend fun register(input: RegisterInputDto): LoginOutputDto = withContext(ioDispatcher) {
        val response = httpClient.post("$baseUrl/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(input)
        }
        response.decodeLoginOrThrow()
    }

    private companion object {
        val errorEnvelopeJson = Json { ignoreUnknownKeys = true }
    }

    public suspend fun logout(): LogoutOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/auth/logout") {
            applyBearerAuth()
        }.body()
    }

    public suspend fun requestOtp(input: OtpRequestInputDto): OtpRequestResultDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/auth/otp/request") {
            contentType(ContentType.Application.Json)
            setBody(input)
        }.body()
    }

    public suspend fun requestOtp(email: String): OtpRequestResultDto = requestOtp(OtpRequestInputDto(email = email.trim()))

    public suspend fun verifyOtp(input: OtpVerifyInputDto): OtpVerifyResultDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/auth/otp/verify") {
            contentType(ContentType.Application.Json)
            setBody(input)
        }.body()
    }

    public suspend fun verifyOtp(email: String, code: String): OtpVerifyResultDto = verifyOtp(OtpVerifyInputDto(email = email.trim(), code = code.trim()))

    public suspend fun resetPassword(input: ResetPasswordInputDto): LoginOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/auth/reset-password") {
            contentType(ContentType.Application.Json)
            setBody(input)
        }.body()
    }

    public suspend fun loginWithOAuth(input: OAuthLoginInputDto): LoginOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/auth/oauth") {
            contentType(ContentType.Application.Json)
            setBody(input)
        }.body()
    }

    public suspend fun loginWithOAuth(provider: String, idToken: String): LoginOutputDto = loginWithOAuth(OAuthLoginInputDto(provider = provider.trim(), idToken = idToken.trim()))

    public suspend fun fetchCurrentUser(bearerToken: String = ""): SwitchViewOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/auth/me") {
            applyBearerAuth(bearerToken)
        }.body()
    }

    public suspend fun updateMyProfile(input: UpdateProfileInputDto): SwitchViewOutputDto = withContext(ioDispatcher) {
        httpClient.patch("$baseUrl/v1/auth/me") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.body()
    }

    public suspend fun changePassword(input: ChangePasswordInputDto): PasswordChangedOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/auth/change-password") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.body()
    }

    public suspend fun listRegisteredUsers(): RegisteredUsersOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/auth/users") {
            applyBearerAuth()
        }.body()
    }

    public suspend fun switchView(bearerToken: String, targetView: String): SwitchViewOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/auth/switch-view") {
            applyBearerAuth(bearerToken)
            contentType(ContentType.Application.Json)
            setBody(SwitchViewInputDto(view = targetView))
        }.body()
    }

    public suspend fun switchView(targetView: String): SwitchViewOutputDto = switchView(bearerToken = "", targetView = targetView)

    public suspend fun deleteAccount(bearerToken: String = ""): DeleteAccountOutputDto = withContext(ioDispatcher) {
        httpClient.delete("$baseUrl/v1/account") {
            applyBearerAuth(bearerToken)
        }.body()
    }

    public suspend fun listClients(): ClientsOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/admin/clients") {
            applyBearerAuth()
        }.body()
    }

    public suspend fun updateClient(clientId: String, input: UpdateClientInputDto): ClientOutputDto = withContext(ioDispatcher) {
        httpClient.patch("$baseUrl/v1/admin/clients/${clientId.trim()}") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.body()
    }

    public suspend fun listStaff(): StaffOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/admin/staff") {
            applyBearerAuth()
        }.body()
    }

    public suspend fun inviteStaff(input: InviteStaffInputDto): StaffMemberOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/admin/staff") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.body()
    }

    public suspend fun updateStaffMember(staffId: String, input: UpdateStaffInputDto): StaffMemberOutputDto = withContext(ioDispatcher) {
        httpClient.patch("$baseUrl/v1/admin/staff/${staffId.trim()}") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.body()
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
        }.body()
    }

    public suspend fun listBalloonProducts(): ArticlesOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/catalog/balloons").body()
    }

    public suspend fun createCatalogArticle(input: CreateArticleInputDto): ArticleOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/catalog/articles") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.body()
    }

    public suspend fun updateCatalogArticle(articleId: String, input: UpdateArticleInputDto): ArticleOutputDto = withContext(ioDispatcher) {
        httpClient.patch("$baseUrl/v1/catalog/articles/${articleId.trim()}") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.body()
    }

    public suspend fun getCatalogAvailability(articleId: String, month: String? = null): AvailabilityOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/catalog/availability") {
            parameter("articleId", articleId.trim())
            if (!month.isNullOrBlank()) {
                parameter("month", month.trim())
            }
        }.body()
    }

    public suspend fun listCategories(): CategoriesOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/catalog/categories").body()
    }

    public suspend fun listPackages(): PackagesOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/catalog/packages") {
            applyBearerAuth()
        }.body()
    }

    public suspend fun createCatalogPackage(input: SavePackageInputDto): PackageOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/catalog/packages") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.body()
    }

    public suspend fun updateCatalogPackage(packageId: String, input: UpdatePackageInputDto): PackageOutputDto = withContext(ioDispatcher) {
        httpClient.put("$baseUrl/v1/catalog/packages/${packageId.trim()}") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.body()
    }

    public suspend fun listEvents(status: String? = null): EventsListOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/events") {
            applyBearerAuth()
            if (!status.isNullOrBlank()) {
                parameter("status", status.trim())
            }
        }.body()
    }

    public suspend fun createRemoteEvent(input: CreateEventInputDto): EventDetailOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/events") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.body()
    }

    public suspend fun listEventTemplates(): TemplatesOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/events/templates").body()
    }

    public suspend fun getEventById(id: String): EventDetailOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/events/${id.trim()}") {
            applyBearerAuth()
        }.body()
    }

    public suspend fun updateRemoteEvent(eventId: String, input: UpdateEventInputDto): EventDetailOutputDto = withContext(ioDispatcher) {
        httpClient.patch("$baseUrl/v1/events/${eventId.trim()}") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.body()
    }

    public suspend fun cancelEvent(id: String): CancelEventOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/events/${id.trim()}/cancel") {
            applyBearerAuth()
        }.body()
    }

    public suspend fun requestDateChange(id: String, input: DateChangeInputDto): DateChangeOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/events/${id.trim()}/date-change") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.body()
    }

    public suspend fun duplicateRemoteEvent(eventId: String): EventDetailOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/events/${eventId.trim()}/duplicate") {
            applyBearerAuth()
        }.body()
    }

    public suspend fun listEventGuests(eventId: String): GuestsListOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/events/${eventId.trim()}/guests") {
            applyBearerAuth()
        }.body()
    }

    public suspend fun addEventGuest(eventId: String, input: AddGuestInputDto): GuestOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/events/${eventId.trim()}/guests") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.body()
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
        }.body()
    }

    public suspend fun listEventSchedule(eventId: String): ScheduleListOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/events/${eventId.trim()}/schedule") {
            applyBearerAuth()
        }.body()
    }

    public suspend fun addEventScheduleBlock(
        eventId: String,
        input: AddScheduleBlockInputDto,
    ): ScheduleBlockOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/events/${eventId.trim()}/schedule") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.body()
    }

    public suspend fun deleteEventScheduleBlock(
        eventId: String,
        blockId: String,
    ): DeleteScheduleBlockOutputDto = withContext(ioDispatcher) {
        httpClient.delete("$baseUrl/v1/events/${eventId.trim()}/schedule/${blockId.trim()}") {
            applyBearerAuth()
        }.body()
    }

    public suspend fun listChatThreads(): ChatThreadsOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/chats") {
            applyBearerAuth()
        }.body()
    }

    public suspend fun listThreadMessages(threadId: String): ChatMessagesOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/chats/${threadId.trim()}/messages") {
            applyBearerAuth()
        }.body()
    }

    public suspend fun sendThreadMessage(
        threadId: String,
        input: PostChatMessageInputDto,
    ): ChatMessageOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/chats/${threadId.trim()}/messages") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.body()
    }

    public suspend fun getEventLogistics(eventId: String): EventLogisticsOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/operations/events/${eventId.trim()}") {
            applyBearerAuth()
        }.body()
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
        }.body()
    }

    public suspend fun reportDamage(
        eventId: String,
        input: ReportDamageInputDto,
    ): ReportDamageOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/operations/events/${eventId.trim()}/damages") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.body()
    }

    public suspend fun listDamages(eventId: String? = null): DamagesListOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/operations/damages") {
            applyBearerAuth()
            if (!eventId.isNullOrBlank()) {
                parameter("eventId", eventId.trim())
            }
        }.body()
    }

    public suspend fun listOfflineQueue(): OfflineQueueOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/operations/offline-queue") {
            applyBearerAuth()
        }.body()
    }

    public suspend fun syncOfflineQueue(): OfflineQueueOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/operations/offline-queue/sync") {
            applyBearerAuth()
        }.body()
    }

    public suspend fun listTodayRoutes(): RouteStopsOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/operations/routes/today") {
            applyBearerAuth()
        }.body()
    }

    public suspend fun listPayments(eventId: String? = null): PaymentsListOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/payments") {
            applyBearerAuth()
            if (!eventId.isNullOrBlank()) {
                parameter("eventId", eventId.trim())
            }
        }.body()
    }

    public suspend fun registerPayment(input: RegisterPaymentInputDto): PaymentOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/payments") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.body()
    }

    public suspend fun getFinanceSummary(): FinanceSummaryOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/payments/summary") {
            applyBearerAuth()
        }.body()
    }

    public suspend fun listRefunds(): RefundsListOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/refunds") {
            applyBearerAuth()
        }.body()
    }

    public suspend fun approveRefund(refundId: String): RefundOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/refunds/${refundId.trim()}/approve") {
            applyBearerAuth()
        }.body()
    }

    public suspend fun listQuotes(status: String? = null): QuotesListOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/quotes") {
            applyBearerAuth()
            if (!status.isNullOrBlank()) {
                parameter("status", status.trim())
            }
        }.body()
    }

    public suspend fun createQuote(input: CreateQuoteInputDto): QuoteOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/quotes") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.body()
    }

    public suspend fun getQuoteById(id: String): QuoteOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/quotes/${id.trim()}") {
            applyBearerAuth()
        }.body()
    }

    public suspend fun declineQuote(id: String, input: DeclineQuoteInputDto): QuoteOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/quotes/${id.trim()}/decline") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.body()
    }

    public suspend fun duplicateQuote(quoteId: String): QuoteOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/quotes/${quoteId.trim()}/duplicate") {
            applyBearerAuth()
        }.body()
    }

    public suspend fun signContract(id: String, input: SignContractInputDto): QuoteOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/quotes/${id.trim()}/sign") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.body()
    }

    public suspend fun generateRosaProposal(input: RosaProposalInputDto): RosaProposalOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/quotes/rosa-proposal") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.body()
    }

    public suspend fun sendRosaChat(input: RosaChatInputDto): RosaChatResponseDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/rosa/chat") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.body()
    }

    public suspend fun transcribeRosaAudio(input: RosaTranscribeInputDto): TranscriptionResultDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/rosa/transcribe") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.body()
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
        }.body()
    }

    public suspend fun createReview(input: CreateReviewInputDto): ReviewOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/reviews") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.body()
    }

    public suspend fun replyToReview(reviewId: String, input: ReplyReviewInputDto): ReviewOutputDto = withContext(ioDispatcher) {
        httpClient.patch("$baseUrl/v1/reviews/${reviewId.trim()}/reply") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.body()
    }

    public suspend fun replyToReview(reviewId: String, adminReply: String): ReviewOutputDto = replyToReview(reviewId = reviewId, input = ReplyReviewInputDto(adminReply = adminReply))

    public suspend fun listSuppliers(category: String? = null): SuppliersListOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/operations/suppliers") {
            applyBearerAuth()
            if (!category.isNullOrBlank()) {
                parameter("category", category.trim())
            }
        }.body()
    }

    public suspend fun createSupplier(input: UpsertSupplierInputDto): SupplierOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/operations/suppliers") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.body()
    }

    public suspend fun listLeads(stage: String? = null): LeadsListOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/operations/leads") {
            applyBearerAuth()
            if (!stage.isNullOrBlank()) {
                parameter("stage", stage.trim())
            }
        }.body()
    }

    public suspend fun createLead(input: CreateLeadInputDto): LeadOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/operations/leads") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.body()
    }

    public suspend fun updateLead(leadId: String, input: UpdateLeadInputDto): LeadOutputDto = withContext(ioDispatcher) {
        httpClient.patch("$baseUrl/v1/operations/leads/${leadId.trim()}") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.body()
    }

    public suspend fun getPublicSettings(): PublicSettingsOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/operations/settings/public").body()
    }

    public suspend fun getAdminSettings(): SettingsOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/operations/settings") {
            applyBearerAuth()
        }.body()
    }

    public suspend fun updateAdminSettings(input: UpdateSettingsInputDto): SettingsOutputDto = withContext(ioDispatcher) {
        httpClient.put("$baseUrl/v1/operations/settings") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.body()
    }

    public suspend fun listNotifications(): NotificationsOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/notifications") {
            applyBearerAuth()
        }.body()
    }

    public suspend fun markNotificationRead(id: String): NotificationOutputDto = withContext(ioDispatcher) {
        httpClient.patch("$baseUrl/v1/notifications/${id.trim()}/read") {
            applyBearerAuth()
        }.body()
    }

    public suspend fun listInsurancePolicies(): InsuranceOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/operations/insurance") {
            applyBearerAuth()
        }.body()
    }

    public suspend fun listAuditLogs(): AuditLogsOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/operations/audit-logs") {
            applyBearerAuth()
        }.body()
    }

    public suspend fun listBotFaqs(): BotFaqsOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/operations/bot/faqs") {
            applyBearerAuth()
        }.body()
    }

    public suspend fun createBotFaq(input: UpsertBotFaqInputDto): BotFaqOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/operations/bot/faqs") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.body()
    }

    public suspend fun listBotKeywords(): BotKeywordsOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/operations/bot/keywords") {
            applyBearerAuth()
        }.body()
    }

    public suspend fun createBotKeyword(input: UpsertBotKeywordInputDto): BotKeywordOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/operations/bot/keywords") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.body()
    }

    public suspend fun listBotThreads(): BotThreadsOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/operations/bot/threads") {
            applyBearerAuth()
        }.body()
    }

    public suspend fun updateBotThread(id: String, input: UpdateBotThreadInputDto): BotThreadOutputDto = withContext(ioDispatcher) {
        httpClient.patch("$baseUrl/v1/operations/bot/threads/${id.trim()}") {
            applyBearerAuth()
            contentType(ContentType.Application.Json)
            setBody(input)
        }.body()
    }

    public suspend fun exportReport(
        type: String = "ingresos",
        period: String = "2026-10",
    ): ReportExportOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/reports/export") {
            applyBearerAuth()
            parameter("type", type.trim())
            parameter("period", period.trim())
        }.body()
    }
}
