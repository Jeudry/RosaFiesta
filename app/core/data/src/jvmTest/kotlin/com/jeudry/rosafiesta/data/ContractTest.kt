package com.jeudry.rosafiesta.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.elementNames
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Verifica que los DTOs de Kotlin (`core:data`) coincidan con los esquemas generados por el servidor Go en `api/openapi.json`.
 */
@OptIn(ExperimentalSerializationApi::class)
class ContractTest {
    private val spec: JsonObject by lazy {
        val candidates = listOf(
            File("../../../api/openapi.json"),
            File("../../api/openapi.json"),
            File("../api/openapi.json"),
            File("api/openapi.json"),
        )
        val file = candidates.firstOrNull { it.exists() }
            ?: error("No se encontro api/openapi.json desde ${File(".").absolutePath}")
        Json.parseToJsonElement(file.readText()).jsonObject
    }

    private fun schemaProperties(schemaName: String): Set<String> {
        val schemas = spec["components"]!!.jsonObject["schemas"]!!.jsonObject
        val schema = schemas[schemaName]?.jsonObject
            ?: error("Esquema '$schemaName' no existe en api/openapi.json")
        val props = schema["properties"]!!.jsonObject.keys
        return props - "\$schema"
    }

    private fun assertMatchesSchema(schemaName: String, descriptor: SerialDescriptor) {
        val expected = schemaProperties(schemaName)
        val actual = descriptor.elementNames.toSet()
        assertEquals(
            expected,
            actual,
            "DTO ${descriptor.serialName} no coincide con el esquema OpenAPI '$schemaName'",
        )
    }

    @Test
    fun `todos los DTOs de core data coinciden exactamente con api openapi json`() {
        val mappings = listOf(
            "OutputBody" to HealthOutputDto.serializer().descriptor,
            "DeleteAccountOutputBody" to DeleteAccountOutputDto.serializer().descriptor,
            "User" to UserDto.serializer().descriptor,
            "LoginInputBody" to LoginInputDto.serializer().descriptor,
            "LoginOutputBody" to LoginOutputDto.serializer().descriptor,
            "SwitchViewInputBody" to SwitchViewInputDto.serializer().descriptor,
            "SwitchViewOutputBody" to SwitchViewOutputDto.serializer().descriptor,
            "ClientSummary" to ClientSummaryDto.serializer().descriptor,
            "ClientsOutputBody" to ClientsOutputDto.serializer().descriptor,
            "StaffMember" to StaffMemberDto.serializer().descriptor,
            "StaffOutputBody" to StaffOutputDto.serializer().descriptor,
            "Article" to ArticleDto.serializer().descriptor,
            "ArticlesOutputBody" to ArticlesOutputDto.serializer().descriptor,
            "Category" to CategoryDto.serializer().descriptor,
            "CategoriesOutputBody" to CategoriesOutputDto.serializer().descriptor,
            "PackageBundle" to PackageBundleDto.serializer().descriptor,
            "PackagesOutputBody" to PackagesOutputDto.serializer().descriptor,
            "EventItem" to EventItemDto.serializer().descriptor,
            "Event" to EventDto.serializer().descriptor,
            "EventsListOutputBody" to EventsListOutputDto.serializer().descriptor,
            "EventDetailOutputBody" to EventDetailOutputDto.serializer().descriptor,
            "EventTemplate" to EventTemplateDto.serializer().descriptor,
            "TemplatesOutputBody" to TemplatesOutputDto.serializer().descriptor,
            "RefundPolicyResult" to RefundPolicyResultDto.serializer().descriptor,
            "CancelEventOutputBody" to CancelEventOutputDto.serializer().descriptor,
            "DateChangeInputBody" to DateChangeInputDto.serializer().descriptor,
            "DateChangeEvaluation" to DateChangeEvaluationDto.serializer().descriptor,
            "DateChangeOutputBody" to DateChangeOutputDto.serializer().descriptor,
            "ChecklistItem" to ChecklistItemDto.serializer().descriptor,
            "DamageReport" to DamageReportDto.serializer().descriptor,
            "EventLogistics" to EventLogisticsDto.serializer().descriptor,
            "EventLogisticsOutputBody" to EventLogisticsOutputDto.serializer().descriptor,
            "ToggleChecklistInputBody" to ToggleChecklistInputDto.serializer().descriptor,
            "ReportDamageInputBody" to ReportDamageInputDto.serializer().descriptor,
            "ReportDamageOutputBody" to ReportDamageOutputDto.serializer().descriptor,
            "OfflineMutation" to OfflineMutationDto.serializer().descriptor,
            "OfflineQueueOutputBody" to OfflineQueueOutputDto.serializer().descriptor,
            "RouteStop" to RouteStopDto.serializer().descriptor,
            "RouteStopsOutputBody" to RouteStopsOutputDto.serializer().descriptor,
            "Payment" to PaymentDto.serializer().descriptor,
            "PaymentOutputBody" to PaymentOutputDto.serializer().descriptor,
            "PaymentsListOutputBody" to PaymentsListOutputDto.serializer().descriptor,
            "RegisterPaymentInputBody" to RegisterPaymentInputDto.serializer().descriptor,
            "FinanceSummary" to FinanceSummaryDto.serializer().descriptor,
            "FinanceSummaryOutputBody" to FinanceSummaryOutputDto.serializer().descriptor,
            "QuoteLine" to QuoteLineDto.serializer().descriptor,
            "Quote" to QuoteDto.serializer().descriptor,
            "QuoteOutputBody" to QuoteOutputDto.serializer().descriptor,
            "QuotesListOutputBody" to QuotesListOutputDto.serializer().descriptor,
            "CreateQuoteInputBody" to CreateQuoteInputDto.serializer().descriptor,
            "DeclineQuoteInputBody" to DeclineQuoteInputDto.serializer().descriptor,
            "SignContractInputBody" to SignContractInputDto.serializer().descriptor,
            "RosaProposalInputBody" to RosaProposalInputDto.serializer().descriptor,
            "RosaMoodboardProposal" to RosaMoodboardProposalDto.serializer().descriptor,
            "RosaProposalOutputBody" to RosaProposalOutputDto.serializer().descriptor,
        )

        mappings.forEach { (schemaName, descriptor) ->
            assertMatchesSchema(schemaName, descriptor)
        }
    }

    @Test
    fun `el cliente HTTP serializa y deserializa correctamente las rutas del contrato OpenAPI`() = runTest {
        val mockEngine = MockEngine { request ->
            val jsonHeaders = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            when (request.url.encodedPath) {
                "/healthz" -> respond(
                    content = """{"status":"ok","product":"RosaFiesta"}""",
                    status = HttpStatusCode.OK,
                    headers = jsonHeaders,
                )

                "/v1/payments/summary" -> respond(
                    content = """{"summary":{"monthLabel":"Octubre 2026","octRevenueRD":12450,"accountsReceivableRD":7724,"activeEventsCount":4,"newQuoteRequests":2,"pendingVerificationCount":1}}""",
                    status = HttpStatusCode.OK,
                    headers = jsonHeaders,
                )

                "/v1/quotes/rosa-proposal" -> respond(
                    content = """{"proposal":{"step":7,"eventType":"Boda","styleTitle":"Rosa & Dorado","colorPalette":["#EA4EA9","#FFD45C"],"guests":120,"budgetRD":4500,"suggestedLines":[{"articleCode":"RF-001","name":"Silla Tiffany dorada","quantity":120,"unitPriceRD":4}],"estimatedTotalRD":4500,"depositRequiredRD":1350,"assistantReply":"Propuesta lista"}}""",
                    status = HttpStatusCode.OK,
                    headers = jsonHeaders,
                )

                else -> respond(
                    content = """{"articles":[{"id":"silla-tiffany","name":"Silla Tiffany dorada","categoryId":"mobiliario","photoKey":"silla-tiffany","dailyPriceDop":4,"totalStock":240,"availableStock":120,"lowStockThreshold":20,"description":"Silla dorada"}]}""",
                    status = HttpStatusCode.OK,
                    headers = jsonHeaders,
                )
            }
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        val api = RosaFiestaApiClient(baseUrl = "http://localhost:8788", httpClient = httpClient)

        val health = api.fetchHealth()
        assertEquals("ok", health.status)
        assertEquals("RosaFiesta", health.product)

        val summary = api.getFinanceSummary()
        assertEquals(12450L, summary.summary.octRevenueRD)
        assertEquals(7724L, summary.summary.accountsReceivableRD)

        val proposal = api.generateRosaProposal(RosaProposalInputDto(eventType = "Boda", guests = 120, budgetRD = 4500))
        assertEquals(4500L, proposal.proposal.estimatedTotalRD)
        assertEquals(1350L, proposal.proposal.depositRequiredRD)

        val articles = api.listArticles("silla")
        assertTrue(articles.articles!!.isNotEmpty())
        assertEquals(4L, articles.articles.first().dailyPriceDop)
    }
}
