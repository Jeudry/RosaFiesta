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
        assertEquals(17450, repo.metrics.value.octoberRevenueDop)

        val reloadedRepo = SqlRosaFiestaRepository(
            driver = driver,
            ioDispatcher = testDispatcher,
            apiClient = apiClient,
        )
        assertTrue(reloadedRepo.articles.value.any { it.code == "RF-099" })
        assertTrue(reloadedRepo.events.value.any { it.title == "Gala Aniversario" })
        assertTrue(reloadedRepo.payments.value.any { it.referenceCode == "POP-778899" })
        assertTrue(reloadedRepo.staffMembers.value.first { it.name == "Yuli" }.canViewPayments)
        assertTrue(reloadedRepo.chatMessages.value.last().contains("Confirmado el montaje a las 9 AM"))
        assertEquals(17450, reloadedRepo.metrics.value.octoberRevenueDop)
    }

    @Test
    fun `prevents privilege escalation on maria emails and rejects fake password on remote accounts`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val driver = DriverFactory().createDriver()
        val mockEngine = MockEngine { request ->
            val payload = when (request.url.encodedPath) {
                "/v1/auth/users" -> """
                    {
                      "users": [
                        {
                          "id": "usr-remoto-ana",
                          "username": "ana_remota",
                          "fullName": "Ana Remota",
                          "email": "ana_remota@rosafiesta.do",
                          "role": "client"
                        }
                      ]
                    }
                """.trimIndent()

                "/v1/auth/login" -> """
                    {
                      "accessToken": "jwt-ana-real-2026",
                      "user": {
                        "id": "usr-remoto-ana",
                        "username": "ana_remota",
                        "fullName": "Ana Remota",
                        "email": "ana_remota@rosafiesta.do",
                        "role": "client"
                      }
                    }
                """.trimIndent()

                else -> """{"queue":[]}"""
            }
            respond(
                content = payload,
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

        val mariaHackerOutcome = repo.registerUser(
            firstName = "Maria",
            lastName = "Externa",
            username = "maria_hacker",
            email = "maria_hacker@gmail.com",
            birthDate = "12/08/1996",
            phone = "809-555-0166",
            password = "MiClavePropia2026!",
        )
        assertTrue(mariaHackerOutcome.success)
        assertEquals("client", mariaHackerOutcome.account?.role)
        assertEquals("client", repo.activeRole.value)

        repo.syncFromBackendAsync()
        val fakePasswordAttempt = repo.authenticateUser("ana_remota@rosafiesta.do", "rosa2026")
        assertFalse(fakePasswordAttempt.success)

        val realOnlineLogin = repo.authenticateUserOnline("ana_remota@rosafiesta.do", "ClaveServidorReal99!")
        assertTrue(realOnlineLogin.success)
        assertEquals("jwt-ana-real-2026", apiClient.currentAccessToken())
    }

    @Test
    fun `computes real days until event dynamic staff normalized payments and clock checklist time`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val driver = DriverFactory().createDriver()
        val capturedPaths = mutableListOf<String>()
        var capturedPaymentBody = ""
        val mockEngine = MockEngine { request ->
            capturedPaths += request.url.encodedPath
            if (request.url.encodedPath == "/v1/payments") {
                capturedPaymentBody = request.body.toString()
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

        repo.createEventAsync(
            title = "Recepción Coral",
            clientName = "Nadia Pérez",
            dateLabel = "29 dic 2026",
            venue = "Zona Colonial",
            guests = 90,
        )
        repo.createEventAsync(
            title = "Coctel Esmeralda",
            clientName = "Roberto Díaz",
            dateLabel = "25 ene 2027",
            venue = "Santo Domingo Este",
            guests = 70,
        )

        val coralEvent = repo.events.value.first { event -> event.title == "Recepción Coral" }
        val esmeraldaEvent = repo.events.value.first { event -> event.title == "Coctel Esmeralda" }
        assertTrue(esmeraldaEvent.daysUntilEvent > coralEvent.daysUntilEvent)
        val assignedLeads = setOf(coralEvent.leadStaffName, esmeraldaEvent.leadStaffName)
        assertTrue(assignedLeads.all { lead -> lead.isNotBlank() })

        repo.toggleChecklistStepAsync("chk-5")
        val toggledStep = repo.checklist.value.first { step -> step.id == "chk-5" }
        assertTrue(toggledStep.completed)
        assertFalse(toggledStep.timeLabel.contains("AM"))
        assertTrue(capturedPaths.contains("/v1/operations/events/${toggledStep.eventId}/checklist/chk-5"))

        val registeredPayment = repo.registerPaymentAsync(
            eventId = coralEvent.id,
            amountDop = 2500,
            methodLabel = "Transferencia Popular",
            referenceCode = "POP-445566",
        )
        assertEquals(2500, registeredPayment?.amountDop)
        assertFalse(registeredPayment?.dateLabel == "20 oct 2026")
        assertTrue(capturedPaymentBody.isNotEmpty())
    }
}
