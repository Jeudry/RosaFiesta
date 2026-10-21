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
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SqlRosaFiestaRepositoryTest {
    @Test
    fun `persiste articulos checklist y cola offline en SQLite fuera del hilo principal`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val driver = DriverFactory().createDriver()
        var syncEndpointCalled = false

        val mockEngine = MockEngine { request ->
            if (request.url.encodedPath == "/v1/operations/offline-queue/sync") {
                syncEndpointCalled = true
            }
            respond(
                content = """{"queue":[]}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        val apiClient = RosaFiestaApiClient(
            baseUrl = "http://localhost:8788",
            ioDispatcher = testDispatcher,
            httpClient = httpClient,
        )

        val repo = SqlRosaFiestaRepository(
            driver = driver,
            ioDispatcher = testDispatcher,
            apiClient = apiClient,
        )

        assertEquals(6, repo.articles.value.size)
        assertEquals(6, repo.checklist.value.size)
        assertEquals(3, repo.offlineQueue.value.size)

        val initialStep = repo.checklist.value.first { it.id == "chk-5" }
        assertFalse(initialStep.completed)

        repo.toggleChecklistStepAsync("chk-5")
        val updatedStep = repo.checklist.value.first { it.id == "chk-5" }
        assertTrue(updatedStep.completed)

        repo.adjustArticleStockAsync("art-arco-rosa", 3)
        val updatedArco = repo.articles.value.first { it.id == "art-arco-rosa" }
        assertEquals(13, updatedArco.totalStock)
        assertEquals(8, updatedArco.availableOct23)

        repo.syncOfflineQueueAsync()
        assertTrue(syncEndpointCalled)
        assertTrue(repo.offlineQueue.value.all { it.synced })

        repo.createArticleAsync(
            code = "RF-099",
            name = "Arco Orgánico Dorado",
            category = "Arcos",
            pricePerDayDop = 3200,
            totalStock = 8,
        )
        assertTrue(repo.articles.value.any { it.code == "RF-099" && it.pricePerDayDop == 3200 })

        repo.createEventAsync(
            title = "Gala Aniversario",
            clientName = "Valeria Santos",
            dateLabel = "15 nov 2026",
            venue = "Hotel Embajador",
            guests = 100,
        )
        assertTrue(repo.events.value.any { it.title == "Gala Aniversario" && it.clientName == "Valeria Santos" })

        val dateChangeFirst = repo.changeEventDateAsync("evt-quince-valentina", "20 nov 2026")
        assertTrue(dateChangeFirst.allowed)
        assertEquals(0, dateChangeFirst.feeDop)

        val dateChangeSecond = repo.changeEventDateAsync("evt-quince-valentina", "25 nov 2026")
        assertTrue(dateChangeSecond.allowed)
        assertEquals(750, dateChangeSecond.feeDop)

        val payment = repo.registerPaymentAsync(
            eventId = "evt-boda-real",
            amountDop = 5000,
            methodLabel = "Transferencia Popular",
            referenceCode = "POP-778899",
        )
        assertEquals(5000, payment?.amountDop)
        assertTrue(repo.payments.value.any { it.referenceCode == "POP-778899" })

        val refund = repo.cancelEventAsync("evt-quince-valentina")
        assertEquals(90, refund.refundPercent)

        repo.toggleFavoriteAsync("art-mesa-helados")
        assertTrue("art-mesa-helados" in repo.favorites.value)
        repo.toggleFavoriteAsync("art-mesa-helados")
        assertFalse("art-mesa-helados" in repo.favorites.value)

        val yuliBefore = repo.staffMembers.value.first { it.name == "Yuli" }
        assertFalse(yuliBefore.canViewPayments)
        repo.toggleStaffPaymentAccessAsync("Yuli")
        val yuliAfter = repo.staffMembers.value.first { it.name == "Yuli" }
        assertTrue(yuliAfter.canViewPayments)

        val initialChatCount = repo.chatMessages.value.size
        repo.sendChatMessageAsync("Camila", "Confirmado el montaje a las 9 AM")
        assertEquals(initialChatCount + 1, repo.chatMessages.value.size)
        assertTrue(repo.chatMessages.value.last().contains("Confirmado el montaje a las 9 AM"))
    }
}
