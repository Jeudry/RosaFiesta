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
import kotlin.test.assertNull
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

    @Test
    fun `sql online registration propagates backend 409 conflict and persists 200 session`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val driver = DriverFactory().createDriver()
        var registerAttempt = 0
        val mockEngine = MockEngine { request ->
            when (request.url.encodedPath) {
                "/v1/auth/register" -> {
                    val isFirstConflictAttempt = registerAttempt == 0
                    registerAttempt += 1
                    if (isFirstConflictAttempt) {
                        respond(
                            content = """{"code":"AUTH_EMAIL_TAKEN","message":"Correo ya en uso"}""",
                            status = HttpStatusCode.Conflict,
                            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                        )
                    } else {
                        respond(
                            content = """
                                {
                                  "accessToken": "jwt-sql-registro-2026",
                                  "user": {
                                    "id": "usr-sql-nuevo",
                                    "username": "laura_sql",
                                    "fullName": "Laura Gómez",
                                    "email": "laura_sql@gmail.com",
                                    "role": "client"
                                  }
                                }
                            """.trimIndent(),
                            status = HttpStatusCode.OK,
                            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                        )
                    }
                }

                "/v1/auth/login" -> respond(
                    content = """{"code":"AUTH_INVALID_CREDENTIALS","message":"Credenciales inválidas"}""",
                    status = HttpStatusCode.Unauthorized,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                )

                else -> respond(
                    content = """{"queue":[]}""",
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                )
            }
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

        val duplicateOutcome = repo.registerUserOnline(
            firstName = "Laura",
            lastName = "Gómez",
            username = "laura_dup",
            email = "duplicado@gmail.com",
            birthDate = "21/08/1998",
            phone = "809-555-0199",
            password = "ClaveSql2026!",
        )
        assertFalse(duplicateOutcome.success)
        assertEquals("AUTH_EMAIL_TAKEN", duplicateOutcome.errorCode)

        val okOutcome = repo.registerUserOnline(
            firstName = "Laura",
            lastName = "Gómez",
            username = "laura_sql",
            email = "laura_sql@gmail.com",
            birthDate = "21/08/1998",
            phone = "809-555-0199",
            password = "ClaveSql2026!",
        )
        assertTrue(okOutcome.success)
        assertEquals("jwt-sql-registro-2026", okOutcome.accessToken)
        assertEquals("laura_sql@gmail.com", repo.currentUserEmail.value)

        val badLogin = repo.authenticateUserOnline("laura_sql@gmail.com", "ClaveMala!")
        assertFalse(badLogin.success)
        assertEquals("AUTH_INVALID_CREDENTIALS", badLogin.errorCode)
    }

    @Test
    fun adminOnlySeedStartsEmptyExceptAdminAndStaffAndCanCreateFirstArticle() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val driver = DriverFactory().createDriver()
        val repo = SqlRosaFiestaRepository(
            driver = driver,
            ioDispatcher = testDispatcher,
            seedDemoData = false,
        )

        assertTrue(repo.articles.value.isEmpty())
        assertTrue(repo.events.value.isEmpty())
        assertTrue(repo.payments.value.isEmpty())
        assertTrue(repo.quotes.value.isEmpty())
        assertTrue(repo.clients.value.isEmpty())
        assertTrue(repo.notifications.value.isEmpty())
        assertTrue(repo.checklist.value.isEmpty())
        assertEquals(0, repo.metrics.value.octoberRevenueDop)
        assertEquals(0, repo.metrics.value.activeEventsCount)
        assertEquals(2, repo.registeredAccounts.value.size)
        assertEquals(2, repo.staffMembers.value.size)
        assertEquals("guest", repo.activeRole.value)

        val camilaLogin = repo.authenticateUserOnline("camila@rosafiesta.do", "rosa2026")
        assertFalse(camilaLogin.success)

        val mariaLogin = repo.authenticateUserOnline("maria@rosafiesta.do", "rosa2026")
        assertTrue(mariaLogin.success)
        assertEquals("admin", repo.activeRole.value)

        repo.createArticle(
            code = "RF-001",
            name = "Mesa imperial roble",
            category = "Mobiliario",
            pricePerDayDop = 45,
            totalStock = 8,
        )
        assertEquals(1, repo.articles.value.size)
        assertEquals("Mesa imperial roble", repo.articles.value.first().name)
    }

    @Test
    fun `replays structured outbox mutations and preserves detailed payment and availability checks`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val driver = DriverFactory().createDriver()
        val capturedSyncBodies = mutableListOf<String>()

        val mockEngine = MockEngine { request ->
            val jsonHeaders = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            when (request.url.encodedPath) {
                "/v1/payments" -> respond(
                    content = """{"code":"TEMPORARY_OFFLINE","message":"Backend temporalmente fuera de línea"}""",
                    status = HttpStatusCode.ServiceUnavailable,
                    headers = jsonHeaders,
                )

                "/v1/catalog/availability-check" -> respond(
                    content = """{"eventDateIso":"2026-12-20","pickupDateIso":"2026-12-19","returnDateIso":"2026-12-21","available":true,"conflicts":[]}""",
                    status = HttpStatusCode.OK,
                    headers = jsonHeaders,
                )

                "/v1/operations/offline-queue/sync" -> {
                    capturedSyncBodies.add(request.body.toString())
                    respond(
                        content = """{"queue":[]}""",
                        status = HttpStatusCode.OK,
                        headers = jsonHeaders,
                    )
                }

                else -> respond(
                    content = """{"queue":[]}""",
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

        val detailedPayment = repo.registerDetailedPayment(
            eventId = "evt-boda-real",
            amountDop = 2950,
            methodLabel = "Transferencia BHD",
            referenceCode = "BHD-998811",
            voucherUrl = "/v1/media/med-voucher-01",
            ncfType = "B01",
            clientRnc = "131849202",
            bankName = "Banco BHD",
            kindLabel = "Anticipo",
        )
        testScheduler.advanceUntilIdle()
        assertEquals("B01", detailedPayment?.ncfType)
        assertEquals("131849202", detailedPayment?.clientRnc)
        assertEquals("/v1/media/med-voucher-01", detailedPayment?.voucherUrl)
        assertFalse(detailedPayment?.verified ?: true)

        val pendingMutations = repo.offlineQueue.value.filter { mutation -> !mutation.synced }
        assertTrue(pendingMutations.any { mutation -> mutation.operationType == "register_payment" })

        repo.syncOfflineQueueAsync()
        testScheduler.advanceUntilIdle()
        assertTrue(repo.offlineQueue.value.all { mutation -> mutation.synced })

        val availability = repo.checkRangeAvailability(
            date = "2026-12-20",
            articleId = "art-silla-tiffany",
            quantity = 1,
        )
        assertTrue(availability.isSuccess)
    }

    @Test
    fun `preserves server JWT on profile edit rejects expired JWT wipes SQL on logout and validates payments`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val driver = DriverFactory().createDriver()
        val mockEngine = MockEngine { request ->
            val jsonHeaders = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            when (request.url.encodedPath) {
                "/v1/auth/me" -> respond(
                    content = """{"user":{"id":"usr-real","username":"ana_real","email":"ana.real@correo.do","fullName":"Ana Real","phone":"8095550177","birthDate":"1995-03-14","role":"client"}}""",
                    status = HttpStatusCode.OK,
                    headers = jsonHeaders,
                )

                else -> respond(
                    content = """{"ok":true}""",
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
        val apiClient = RosaFiestaApiClient(
            baseUrl = "http://localhost:8788",
            ioDispatcher = testDispatcher,
            httpClient = httpClient,
        )
        val repo = SqlRosaFiestaRepository(
            driver = driver,
            ioDispatcher = testDispatcher,
            apiClient = apiClient,
            seedDemoData = false,
        )
        testScheduler.advanceUntilIdle()

        val unauthenticatedEdit = repo.updateUserProfile(
            fullName = "Ana Real",
            email = "ana.real@correo.do",
            phone = "8095550177",
            birthDate = "14/03/1995",
            username = "ana_real",
        )
        assertFalse(unauthenticatedEdit.success)
        assertEquals("AUTH_UNAUTHORIZED", unauthenticatedEdit.errorCode)

        val account = com.jeudry.rosafiesta.domain.security.RegisteredUserAccount(
            id = "usr-real",
            username = "ana_real",
            email = "ana.real@correo.do",
            fullName = "Ana Real",
            passwordHash = "hash",
            salt = "salt",
            birthDate = "14/03/1995",
            phone = "8095550177",
            role = "client",
        )
        val initialToken = com.jeudry.rosafiesta.domain.security.JwtAuthManager.issueAccessToken(
            account = account,
            ttlSeconds = 7200L,
        )
        apiClient.setAccessToken(initialToken)
        apiClient.setRefreshToken("refresh-real-123")
        val restored = repo.restoreSessionFromJwt(initialToken)
        assertEquals("ana.real@correo.do", restored?.email)

        val profileOutcome = repo.updateUserProfile(
            fullName = "Ana Real Actualizada",
            email = "ana.real@correo.do",
            phone = "8095550188",
            birthDate = "14/03/1995",
            username = "ana_real",
        )
        assertTrue(profileOutcome.success)
        assertEquals(initialToken, profileOutcome.accessToken)
        assertEquals("refresh-real-123", profileOutcome.refreshToken)

        val missingEventPayment = repo.registerPaymentAsync(
            eventId = "evt-does-not-exist",
            amountDop = 2000,
            methodLabel = "Efectivo",
            referenceCode = "EF-01",
        )
        assertNull(missingEventPayment)

        val expiredToken = com.jeudry.rosafiesta.domain.security.JwtAuthManager.issueAccessToken(
            account = account,
            issuedAtEpochSeconds = 100L,
            ttlSeconds = 10L,
        )
        assertNull(repo.restoreSessionFromJwt(expiredToken))

        repo.logoutSession()
        testScheduler.advanceUntilIdle()
        assertTrue(repo.events.value.isEmpty())
        assertTrue(repo.payments.value.isEmpty())
        assertTrue(repo.quotes.value.isEmpty())
        assertNull(repo.currentSessionJwt.value)
    }

    @Test
    fun `persists extended article event checklist fields in SQLite and keeps outbox unsynced offline`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val driver = DriverFactory().createDriver()

        val offlineRepo = SqlRosaFiestaRepository(
            driver = driver,
            ioDispatcher = testDispatcher,
            apiClient = null,
            seedDemoData = true,
        )
        testScheduler.advanceUntilIdle()

        offlineRepo.createArticleAsync(
            code = "RF-META-1",
            name = "Mesa Imperial Cristal",
            category = "Mesas",
            pricePerDayDop = 4500,
            totalStock = 4,
            photoUrl = "",
            description = "Mesa con tope biselado",
            dimensions = "240x110cm",
            material = "Acero y vidrio",
            visibleInCatalog = false,
        )
        offlineRepo.createEventAsync(
            title = "Boda Jardin",
            clientName = "Clara Diaz",
            dateLabel = "25 Nov 2026",
            venue = "Salon Norte",
            guests = 40,
            preassignedEventId = "evt-meta-1",
        )
        offlineRepo.cancelEventAsync("evt-meta-1", reason = "Cambio de ciudad")
        offlineRepo.syncOfflineQueueAsync()

        val pendingOffline = offlineRepo.offlineQueue.value.filter { item -> !item.synced }
        assertTrue(pendingOffline.any { item -> item.operationType == "create_article" })
        assertTrue(pendingOffline.any { item -> item.operationType == "create_event" })
        assertTrue(pendingOffline.any { item -> item.operationType == "cancel_event" })

        val reloadedRepo = SqlRosaFiestaRepository(
            driver = driver,
            ioDispatcher = testDispatcher,
            apiClient = null,
            seedDemoData = true,
        )
        testScheduler.advanceUntilIdle()

        val reloadedArticle = reloadedRepo.articles.value.first { art -> art.code == "RF-META-1" }
        assertEquals("240x110cm", reloadedArticle.dimensions)
        assertEquals("Acero y vidrio", reloadedArticle.material)
        assertFalse(reloadedArticle.visibleInCatalog)

        val reloadedEvent = reloadedRepo.events.value.first { evt -> evt.id == "evt-meta-1" }
        assertEquals("Cambio de ciudad", reloadedEvent.cancellationReason)
        assertTrue(reloadedEvent.items.isNotEmpty())
        assertTrue(reloadedRepo.offlineQueue.value.any { item -> !item.synced })
    }
}
