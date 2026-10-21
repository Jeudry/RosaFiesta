package com.jeudry.rosafiesta.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

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
    public val fullName: String,
    public val email: String,
    public val phone: String,
    public val role: String,
    public val permissions: List<String>? = null,
    public val activeView: String,
    public val createdAt: String,
)

@Serializable
public data class LoginInputDto(
    public val email: String,
    public val password: String,
)

@Serializable
public data class LoginOutputDto(
    public val accessToken: String,
    public val user: UserDto,
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
)

@Serializable
public data class RosaProposalOutputDto(
    public val proposal: RosaMoodboardProposalDto,
)

/**
 * Cliente HTTP Ktor tipado hacia el servidor Go de RosaFiesta (`api/openapi.json`).
 * Todas las llamadas de red se ejecutan fuera del hilo principal mediante [ioDispatcher].
 */
public class RosaFiestaApiClient(
    public val baseUrl: String = "http://localhost:8788",
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val httpClient: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    },
) {
    public suspend fun fetchHealthRaw(): String = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/healthz").bodyAsText()
    }

    public suspend fun fetchHealth(): HealthOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/healthz").body()
    }

    public suspend fun login(input: LoginInputDto): LoginOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(input)
        }.body()
    }

    public suspend fun switchView(bearerToken: String, targetView: String): SwitchViewOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/auth/switch-view") {
            header(HttpHeaders.Authorization, "Bearer $bearerToken")
            contentType(ContentType.Application.Json)
            setBody(SwitchViewInputDto(view = targetView))
        }.body()
    }

    public suspend fun deleteAccount(bearerToken: String): DeleteAccountOutputDto = withContext(ioDispatcher) {
        httpClient.delete("$baseUrl/v1/account") {
            header(HttpHeaders.Authorization, "Bearer $bearerToken")
        }.body()
    }

    public suspend fun listClients(): ClientsOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/admin/clients").body()
    }

    public suspend fun listStaff(): StaffOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/admin/staff").body()
    }

    public suspend fun listArticles(query: String? = null): ArticlesOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/catalog/articles") {
            if (!query.isNullOrBlank()) {
                parameter("q", query.trim())
            }
        }.body()
    }

    public suspend fun listCategories(): CategoriesOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/catalog/categories").body()
    }

    public suspend fun listPackages(): PackagesOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/catalog/packages").body()
    }

    public suspend fun listEvents(status: String? = null): EventsListOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/events") {
            if (!status.isNullOrBlank()) {
                parameter("status", status.trim())
            }
        }.body()
    }

    public suspend fun listEventTemplates(): TemplatesOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/events/templates").body()
    }

    public suspend fun getEventById(id: String): EventDetailOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/events/${id.trim()}").body()
    }

    public suspend fun cancelEvent(id: String): CancelEventOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/events/${id.trim()}/cancel").body()
    }

    public suspend fun requestDateChange(id: String, input: DateChangeInputDto): DateChangeOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/events/${id.trim()}/date-change") {
            contentType(ContentType.Application.Json)
            setBody(input)
        }.body()
    }

    public suspend fun getEventLogistics(eventId: String): EventLogisticsOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/operations/events/${eventId.trim()}").body()
    }

    public suspend fun toggleChecklistItem(
        eventId: String,
        itemId: String,
        input: ToggleChecklistInputDto,
    ): EventLogisticsOutputDto = withContext(ioDispatcher) {
        httpClient.patch("$baseUrl/v1/operations/events/${eventId.trim()}/checklist/${itemId.trim()}") {
            contentType(ContentType.Application.Json)
            setBody(input)
        }.body()
    }

    public suspend fun reportDamage(
        eventId: String,
        input: ReportDamageInputDto,
    ): ReportDamageOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/operations/events/${eventId.trim()}/damages") {
            contentType(ContentType.Application.Json)
            setBody(input)
        }.body()
    }

    public suspend fun listOfflineQueue(): OfflineQueueOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/operations/offline-queue").body()
    }

    public suspend fun syncOfflineQueue(): OfflineQueueOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/operations/offline-queue/sync").body()
    }

    public suspend fun listTodayRoutes(): RouteStopsOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/operations/routes/today").body()
    }

    public suspend fun listPayments(eventId: String? = null): PaymentsListOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/payments") {
            if (!eventId.isNullOrBlank()) {
                parameter("eventId", eventId.trim())
            }
        }.body()
    }

    public suspend fun registerPayment(input: RegisterPaymentInputDto): PaymentOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/payments") {
            contentType(ContentType.Application.Json)
            setBody(input)
        }.body()
    }

    public suspend fun getFinanceSummary(): FinanceSummaryOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/payments/summary").body()
    }

    public suspend fun listQuotes(status: String? = null): QuotesListOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/quotes") {
            if (!status.isNullOrBlank()) {
                parameter("status", status.trim())
            }
        }.body()
    }

    public suspend fun createQuote(input: CreateQuoteInputDto): QuoteOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/quotes") {
            contentType(ContentType.Application.Json)
            setBody(input)
        }.body()
    }

    public suspend fun getQuoteById(id: String): QuoteOutputDto = withContext(ioDispatcher) {
        httpClient.get("$baseUrl/v1/quotes/${id.trim()}").body()
    }

    public suspend fun declineQuote(id: String, input: DeclineQuoteInputDto): QuoteOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/quotes/${id.trim()}/decline") {
            contentType(ContentType.Application.Json)
            setBody(input)
        }.body()
    }

    public suspend fun signContract(id: String, input: SignContractInputDto): QuoteOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/quotes/${id.trim()}/sign") {
            contentType(ContentType.Application.Json)
            setBody(input)
        }.body()
    }

    public suspend fun generateRosaProposal(input: RosaProposalInputDto): RosaProposalOutputDto = withContext(ioDispatcher) {
        httpClient.post("$baseUrl/v1/quotes/rosa-proposal") {
            contentType(ContentType.Application.Json)
            setBody(input)
        }.body()
    }
}
