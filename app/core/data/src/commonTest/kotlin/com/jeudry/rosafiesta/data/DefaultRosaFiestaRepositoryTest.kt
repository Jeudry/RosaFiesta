package com.jeudry.rosafiesta.data

import com.jeudry.rosafiesta.domain.CanonicalSampleData
import com.jeudry.rosafiesta.domain.EventLineItem
import com.jeudry.rosafiesta.domain.PhotoAsset
import com.jeudry.rosafiesta.domain.security.JwtAuthManager
import com.jeudry.rosafiesta.domain.security.RegisteredUserAccount
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
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DefaultRosaFiestaRepositoryTest {
    @Test
    fun `alterna pasos del checklist de montaje y sincroniza la cola offline`() {
        val repo = DefaultRosaFiestaRepository()
        val before = repo.checklist.value.first { it.id == "chk-5" }
        assertEquals(false, before.completed)

        repo.toggleChecklistStep("chk-5")
        val after = repo.checklist.value.first { it.id == "chk-5" }
        assertTrue(after.completed)

        repo.syncOfflineQueue()
        assertTrue(repo.offlineQueue.value.all { it.synced })
    }

    @Test
    fun `ajusta el stock de un articulo sin permitir cantidades negativas`() {
        val repo = DefaultRosaFiestaRepository()
        repo.adjustArticleStock("art-arco-rosa", 2)
        val updated = repo.articles.value.first { it.id == "art-arco-rosa" }
        assertEquals(12, updated.totalStock)
        assertEquals(7, updated.availableOct23)
    }

    @Test
    fun `gestiona autenticacion roles favoritos eventos pagos y chat en memoria`() {
        val repo = DefaultRosaFiestaRepository()
        assertEquals(false, repo.login("invalido", "123"))
        assertTrue(repo.login("yuli@rosafiesta.do", "rosa2026"))
        assertEquals("staff_yuli", repo.activeRole.value)

        repo.switchRole("admin")
        assertEquals("admin", repo.activeRole.value)

        repo.toggleFavorite("art-mesa-helados")
        assertTrue("art-mesa-helados" in repo.favorites.value)

        repo.toggleStaffPaymentAccess("Yuli")
        assertTrue(repo.staffMembers.value.first { it.name == "Yuli" }.canViewPayments)

        repo.createArticle("RF-088", "Silla Imperial", "Mobiliario", 120, 40)
        assertTrue(repo.articles.value.any { it.code == "RF-088" })

        repo.createEvent("Boda Jardín", "Elena Gómez", "12 dic 2026", "Casa de Campo", 120)
        assertTrue(repo.events.value.any { it.title == "Boda Jardín" })

        val change = repo.changeEventDate("evt-boda-real", "28 oct 2026")
        assertTrue(change.allowed)

        val payment = repo.registerPayment("evt-boda-real", 3500, "BHD León", "BHD-9988")
        assertEquals(3500, payment?.amountDop)

        val refund = repo.cancelEvent("evt-quince-valentina")
        assertEquals(90, refund.refundPercent)

        repo.sendChatMessage("Camila", "Hola equipo")
        assertTrue(repo.chatMessages.value.last().contains("Hola equipo"))
    }

    @Test
    fun `registra nuevos usuarios reales emite sesion local segura y permite login por usuario o correo`() {
        val repo = DefaultRosaFiestaRepository()
        val regOutcome = repo.registerUser(
            firstName = "Valeria",
            lastName = "Montes",
            username = "valeria_m",
            email = "valeria@rosafiesta.do",
            birthDate = "14/05/1998",
            phone = "809-555-0199",
            password = "Valeria2026!",
        )
        assertTrue(regOutcome.success)
        val issuedToken = regOutcome.accessToken.orEmpty()
        assertTrue(issuedToken.startsWith("rf-local."))
        assertEquals("valeria_m", repo.currentAuthenticatedUser.value?.username)

        val duplicateUser = repo.registerUser(
            firstName = "Otra",
            lastName = "Valeria",
            username = "valeria_m",
            email = "otra@rosafiesta.do",
            birthDate = "14/05/1998",
            phone = "809-555-0100",
            password = "Valeria2026!",
        )
        assertEquals(false, duplicateUser.success)
        assertEquals("AUTH_USERNAME_TAKEN", duplicateUser.errorCode)

        val loginByUsername = repo.authenticateUser("valeria_m", "Valeria2026!")
        assertTrue(loginByUsername.success)

        val resetOutcome = repo.resetUserPassword("valeria@rosafiesta.do", "NuevaClave2026!")
        assertTrue(resetOutcome.success)
        assertTrue(repo.authenticateUser("valeria@rosafiesta.do", "NuevaClave2026!").success)
    }

    @Test
    fun `prevents privilege escalation when registering email starting with maria`() {
        val repo = DefaultRosaFiestaRepository()
        val hackerRegistration = repo.registerUser(
            firstName = "Maria",
            lastName = "Hacker",
            username = "maria_hacker",
            email = "maria_hacker@gmail.com",
            birthDate = "10/10/1995",
            phone = "809-555-0144",
            password = "ClaveSegura2026!",
        )
        assertTrue(hackerRegistration.success)
        assertEquals("client", hackerRegistration.account?.role)
        assertEquals("client", repo.activeRole.value)
    }

    @Test
    fun `manages quotes refunds reviews threads guests leads suppliers and reports`() = runTest {
        val repo = DefaultRosaFiestaRepository()

        val createdQuote = repo.createQuote(
            eventTitle = "Cena Dorada",
            clientName = "Elena Gómez",
            clientEmail = "elena@gmail.com",
            venue = "Hotel Embajador",
            dateLabel = "18 dic 2026",
            guests = 80,
            lines = listOf(
                EventLineItem(
                    articleCode = "RF-001",
                    name = "Silla Tiffany Dorada",
                    quantity = 80,
                    unitPriceDop = 4,
                    photo = PhotoAsset.SILLA_TIFFANY,
                ),
            ),
            deliveryFeeDop = 500,
            discountDop = 20,
        )
        assertEquals(800, createdQuote.totalDop)

        val approvedQuote = repo.approveQuote(createdQuote.id, "Elena Gómez", "<svg/>")
        assertEquals("APPROVED", approvedQuote?.status)

        val duplicatedQuote = repo.duplicateQuote(createdQuote.id)
        assertNotNull(duplicatedQuote)
        val declinedQuote = repo.declineQuote(duplicatedQuote.id, "Cambio de presupuesto")
        assertEquals("DECLINED", declinedQuote?.status)

        val gabrielaRefund = repo.refundRequests.value.first { refund ->
            refund.clientName == "Gabriela Núñez"
        }
        val approvedRefund = repo.approveRefund(gabrielaRefund.id)
        assertEquals("APPROVED", approvedRefund?.status)

        val createdReview = repo.createReview(
            eventId = "evt-boda-real",
            authorName = "Invitada VIP",
            rating = 5,
            comment = "Montaje impecable",
        )
        val repliedReview = repo.replyToReview(createdReview.id, "Gracias por confiar en RosaFiesta")
        assertEquals("Gracias por confiar en RosaFiesta", repliedReview?.adminReply)

        val camilaThread = repo.chatThreads.value.first { thread -> thread.contactName.contains("Camila") }
        val sentMsg = repo.sendThreadMessage(
            threadId = camilaThread.id,
            senderName = "María",
            body = "Enviamos plano actualizado",
            attachmentName = "plano.pdf",
        )
        assertEquals("plano.pdf", sentMsg.attachmentName)

        val guest = repo.addEventGuest("evt-boda-real", "Lucía Herrera", "Confirmado", "Mesa 4", "Sin gluten")
        val updatedGuest = repo.updateEventGuest(
            eventId = "evt-boda-real",
            guestId = guest.id,
            rsvpStatus = "Asistirá",
            tableLabel = "Mesa 1",
            notes = "VIP",
        )
        assertEquals("Mesa 1", updatedGuest?.tableLabel)

        val block = repo.addEventScheduleBlock("evt-boda-real", "16:00 - 17:00", "Prueba de luces", "Jorge")
        assertTrue(repo.deleteEventScheduleBlock("evt-boda-real", block.id))

        val lead = repo.createLead(
            clientName = "Paola Díaz",
            eventType = "Boda",
            dateLabel = "14 feb 2027",
            guests = 150,
            estimatedDop = 9500,
            channel = "Instagram",
            phone = "809-555-0177",
        )
        val updatedLead = repo.updateLead(
            leadId = lead.id,
            stage = "Cotizado",
            followUpNote = "Propuesta enviada por correo",
        )
        assertEquals("Cotizado", updatedLead?.stage)

        val supplier = repo.createSupplier(
            name = "Flores del Valle",
            category = "Floristería",
            contactName = "Rosa Mejía",
            phone = "+18095550188",
            email = "ventas@floresvalle.do",
        )
        assertEquals("Flores del Valle", supplier.name)

        val report = repo.exportReport("ingresos", "2026-10")
        assertTrue(report.csvContent.isNotEmpty())
    }

    @Test
    fun `synchronizes in memory repository with backend api client for web and wasm`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        var capturedAuthHeader: String? = null
        val mockEngine = MockEngine { request ->
            val currentAuth = request.headers[HttpHeaders.Authorization]
            if (currentAuth != null) {
                capturedAuthHeader = currentAuth
            }
            val payload = when (request.url.encodedPath) {
                "/v1/auth/login" -> """
                    {
                      "accessToken": "jwt-wasm-session-99",
                      "user": {
                        "id": "usr-camila",
                        "username": "camila",
                        "fullName": "Camila Rodríguez",
                        "email": "camila@gmail.com",
                        "role": "client"
                      }
                    }
                """.trimIndent()

                "/v1/catalog/balloons" -> """
                    {
                      "articles": [
                        {
                          "id": "balloon-bouquet-helio",
                          "name": "Bouquet helio sincronizado",
                          "categoryId": "Globos",
                          "photoKey": "arco_globos",
                          "dailyPriceDop": 28,
                          "totalStock": 40,
                          "availableStock": 35,
                          "lowStockThreshold": 5,
                          "description": "Set de globos con helio",
                          "isDirectSale": true,
                          "visible": true
                        }
                      ]
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
        val repo = DefaultRosaFiestaRepository(
            apiClient = apiClient,
            ioDispatcher = testDispatcher,
        )

        val loginOutcome = repo.authenticateUserOnline("camila@gmail.com", "ClaveCamila2026!")
        assertTrue(loginOutcome.success)
        assertEquals("jwt-wasm-session-99", apiClient.currentAccessToken())

        repo.syncFromBackendAsync()
        assertEquals("Bearer jwt-wasm-session-99", capturedAuthHeader)
        assertEquals("Bouquet helio sincronizado", repo.balloonProducts.value.first().name)
    }

    @Test
    fun `online registration awaits server and rejects conflict or unauthorized without fake fallback`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        var registerAttempt = 0
        val mockEngine = MockEngine { request ->
            when (request.url.encodedPath) {
                "/v1/auth/register" -> {
                    val isFirstConflictAttempt = registerAttempt == 0
                    registerAttempt += 1
                    if (isFirstConflictAttempt) {
                        respond(
                            content = """{"code":"AUTH_EMAIL_TAKEN","message":"Este correo ya está registrado"}""",
                            status = HttpStatusCode.Conflict,
                            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                        )
                    } else {
                        respond(
                            content = """
                                {
                                  "accessToken": "jwt-nuevo-registro-2026",
                                  "user": {
                                    "id": "usr-nuevo",
                                    "username": "jeudry_nuevo",
                                    "fullName": "Jeudry Peña",
                                    "email": "nuevo@gmail.com",
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
        val repo = DefaultRosaFiestaRepository(
            apiClient = apiClient,
            ioDispatcher = testDispatcher,
        )

        val conflictOutcome = repo.registerUserOnline(
            firstName = "Jeudry",
            lastName = "Peña",
            username = "jeudry",
            email = "repetido@gmail.com",
            birthDate = "21/08/1998",
            phone = "8497505944",
            password = "ClaveSegura2026!",
        )
        assertFalse(conflictOutcome.success)
        assertEquals("AUTH_EMAIL_TAKEN", conflictOutcome.errorCode)

        val createdOutcome = repo.registerUserOnline(
            firstName = "Jeudry",
            lastName = "Peña",
            username = "jeudry_nuevo",
            email = "nuevo@gmail.com",
            birthDate = "21/08/1998",
            phone = "8497505944",
            password = "ClaveSegura2026!",
        )
        assertTrue(createdOutcome.success)
        assertEquals("jwt-nuevo-registro-2026", createdOutcome.accessToken)
        assertEquals("jwt-nuevo-registro-2026", apiClient.currentAccessToken())

        val invalidLogin = repo.authenticateUserOnline("nuevo@gmail.com", "ClaveErronea123!")
        assertFalse(invalidLogin.success)
        assertEquals("AUTH_INVALID_CREDENTIALS", invalidLogin.errorCode)
    }

    @Test
    fun `renueva access token automaticamente ante 401 y limpia sesion si el refresh es rechazado`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        var refreshStage = 0
        var expiredCallbackCalled = false
        var refreshedPair: Pair<String, String>? = null

        val mockEngine = MockEngine { request ->
            val path = request.url.encodedPath
            when (path) {
                "/v1/auth/me" -> {
                    val authHeader = request.headers[HttpHeaders.Authorization].orEmpty()
                    if (authHeader == "Bearer rotated-access-jwt") {
                        respond(
                            content = """{"user":{"id":"usr-1","username":"maria","fullName":"María Demo","email":"maria@rosafiesta.do","role":"admin","activeView":"admin"}}""",
                            status = HttpStatusCode.OK,
                            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                        )
                    } else {
                        respond(
                            content = """{"code":"AUTH_UNAUTHORIZED","message":"Token expirado"}""",
                            status = HttpStatusCode.Unauthorized,
                            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                        )
                    }
                }

                "/v1/auth/refresh" -> {
                    refreshStage += 1
                    if (refreshStage == 1) {
                        respond(
                            content = """{"accessToken":"rotated-access-jwt","refreshToken":"rotated-refresh-2","expiresIn":900,"user":{"id":"usr-1","username":"maria","fullName":"María Demo","email":"maria@rosafiesta.do","role":"admin","activeView":"admin"}}""",
                            status = HttpStatusCode.OK,
                            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                        )
                    } else {
                        respond(
                            content = """{"code":"AUTH_REFRESH_REUSE","message":"Sesión revocada por reutilización"}""",
                            status = HttpStatusCode.Unauthorized,
                            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                        )
                    }
                }

                else -> respond(
                    content = """{"googleEnabled":true,"appleEnabled":false,"webPushVapidPublicKey":"BTestVapidKey123"}""",
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
        apiClient.setSessionLifecycleCallbacks(
            onTokensRefreshed = { access, refresh ->
                refreshedPair = access to refresh
            },
            onSessionExpired = {
                expiredCallbackCalled = true
            },
        )
        apiClient.setAccessToken("expired-access-jwt")
        apiClient.setRefreshToken("initial-refresh-1")

        val profileOutput = apiClient.fetchCurrentUser()
        assertEquals("maria@rosafiesta.do", profileOutput.user.email)
        assertEquals("rotated-access-jwt" to "rotated-refresh-2", refreshedPair)
        assertEquals("rotated-refresh-2", apiClient.currentRefreshToken())

        val oauthConfig = RosaFiestaBackendMappers.toOAuthProviderConfig(apiClient.getOAuthConfig())
        assertEquals("BTestVapidKey123", oauthConfig.webPushVapidPublicKey)

        apiClient.setAccessToken("stale-revoked-jwt")
        val failedRefresh = runCatching { apiClient.fetchCurrentUser() }
        assertTrue(failedRefresh.isFailure)
        assertTrue(expiredCallbackCalled)
        assertEquals(null, apiClient.currentAccessToken())
        assertEquals(null, apiClient.currentRefreshToken())
    }

    @Test
    fun `short circuits backend sync on network failure and handles checklist settings reviews and setup photos`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val offlinePaths = mutableListOf<String>()
        val offlineEngine = MockEngine { request ->
            offlinePaths.add(request.url.encodedPath)
            throw IllegalStateException("Network unreachable")
        }
        val offlineHttp = HttpClient(offlineEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        val offlineApi = RosaFiestaApiClient(
            baseUrl = "http://localhost:8788",
            ioDispatcher = testDispatcher,
            httpClient = offlineHttp,
        )
        val offlineRepo = DefaultRosaFiestaRepository(
            apiClient = offlineApi,
            ioDispatcher = testDispatcher,
            seedDemoData = false,
            snapshotCache = null,
        )
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
            for (attempt in 0 until 50) {
                testScheduler.advanceUntilIdle()
                if (offlinePaths.isNotEmpty()) break
                kotlinx.coroutines.delay(10)
            }
        }
        testScheduler.advanceUntilIdle()
        offlinePaths.clear()
        val syncOk = offlineRepo.syncFromBackendAsync()
        assertFalse(syncOk)
        assertEquals(listOf("/v1/catalog/categories"), offlinePaths)

        var lastReviewReplyBody = ""
        var lastSettingsBody = ""
        var lastEventPatchBody = ""
        val onlineEngine = MockEngine { request ->
            val path = request.url.encodedPath
            when {
                path == "/v1/auth/login" -> {
                    respond(
                        content = """{"accessToken":"server.jwt.token","user":{"id":"usr-admin-test","username":"admin_test","fullName":"Admin Test","email":"admin@rosafiesta.do","role":"admin"}}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )
                }

                path.contains("/checklist/chk-1") -> {
                    respond(
                        content = """{"code":"AUTH_FORBIDDEN","message":"Sin permiso para checklist"}""",
                        status = HttpStatusCode.Forbidden,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )
                }

                path.startsWith("/v1/reviews/") -> {
                    lastReviewReplyBody = request.body.toString()
                    val targetRevId = CanonicalSampleData.reviews.first().id
                    respond(
                        content = """{"review":{"id":"$targetRevId","eventId":"evt-1","clientName":"Laura","rating":5,"comment":"Excelente","verified":false,"adminReply":"Gracias por comentar"}}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )
                }

                path == "/v1/operations/settings" -> {
                    lastSettingsBody = request.body.toString()
                    respond(
                        content = """{"settings":{"id":"default","companyName":"RosaFiesta","rnc":"131","address":"Av 27","phone":"8095550101","whatsapp":"8095550101","email":"info@rf.do","businessHours":"9-6","instagram":"","facebook":"","depositPercent":35,"itbisPercent":0,"whatsappAutoAlertsEnabled":true},"deliveryZones":[]}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )
                }

                path.startsWith("/v1/events/") -> {
                    lastEventPatchBody = request.body.toString()
                    val firstEvtId = CanonicalSampleData.events.first().id
                    respond(
                        content = """{"event":{"id":"$firstEvtId","code":"RF-2026-1","clientName":"Camila","title":"Boda Real","eventType":"Boda","dateLabel":"23 Oct 2026","daysUntilEvent":5,"venue":"Salón Piantini","zone":"Santo Domingo DN","guests":40,"articleCount":1,"totalRD":10000,"paidDepositRD":5000,"balanceDueDate":"2026-11-01","status":"CONFIRMED","dateChangesUsed":0,"assignedLead":"Ana","sketchJson":"","setupPhotoUrls":["/v1/media/setup-1"]},"balanceRD":5000}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )
                }

                else -> {
                    respond(
                        content = """{}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )
                }
            }
        }
        val onlineHttp = HttpClient(onlineEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        val onlineApi = RosaFiestaApiClient(
            baseUrl = "http://localhost:8788",
            ioDispatcher = testDispatcher,
            httpClient = onlineHttp,
        )
        val onlineRepo = DefaultRosaFiestaRepository(
            apiClient = onlineApi,
            ioDispatcher = testDispatcher,
            snapshotCache = null,
        )
        val authOutcome = onlineRepo.authenticateUserOnline("admin@rosafiesta.do", "Password2026")
        assertTrue(authOutcome.success)
        testScheduler.advanceUntilIdle()

        val initialStep = onlineRepo.checklist.value.first { step -> step.id == "chk-1" }
        var checklistError = ""
        onlineRepo.toggleChecklistStep("chk-1") { msg -> checklistError = msg }
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
            for (attempt in 0 until 50) {
                testScheduler.advanceUntilIdle()
                if (checklistError.isNotBlank()) break
                kotlinx.coroutines.delay(10)
            }
        }
        testScheduler.advanceUntilIdle()
        val revertedStep = onlineRepo.checklist.value.first { step -> step.id == "chk-1" }
        assertEquals(initialStep.completed, revertedStep.completed)
        assertTrue(checklistError.isNotBlank())

        val targetReviewId = onlineRepo.reviews.value.first().id
        val replied = onlineRepo.replyToReview(targetReviewId, "Gracias por comentar", verified = false)
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
            for (attempt in 0 until 50) {
                testScheduler.advanceUntilIdle()
                if (lastReviewReplyBody.isNotEmpty()) break
                kotlinx.coroutines.delay(10)
            }
        }
        testScheduler.advanceUntilIdle()
        assertEquals(false, replied?.verified)
        assertTrue(lastReviewReplyBody.isNotEmpty())

        val zeroItbisSettings = onlineRepo.businessSettings.value.copy(
            itbisPercent = 0,
            whatsappAutoAlertsEnabled = true,
            zones = emptyList(),
        )
        val savedSettingsResult = onlineRepo.saveBusinessSettingsOnline(zeroItbisSettings)
        assertTrue(savedSettingsResult.isSuccess)
        val savedSettings = savedSettingsResult.getOrThrow()
        assertEquals(0, savedSettings.itbisPercent)
        assertTrue(savedSettings.whatsappAutoAlertsEnabled)
        assertTrue(savedSettings.zones.isEmpty())
        assertTrue(lastSettingsBody.isNotEmpty())

        val targetEventId = onlineRepo.events.value.first().id
        onlineRepo.updateEventDetails(eventId = targetEventId, sketchJson = "{\"tables\":4}")
        testScheduler.advanceUntilIdle()
        val setupResult = onlineRepo.updateEventSetupPhotos(targetEventId, listOf("/v1/media/setup-1"))
        assertTrue(setupResult.isSuccess, "setup failed: ${setupResult.exceptionOrNull()}")
        val updatedEvent = setupResult.getOrThrow()
        assertEquals(listOf("/v1/media/setup-1"), updatedEvent.setupPhotoUrls)
        assertEquals("{\"tables\":4}", updatedEvent.sketchJson)
        assertTrue(lastEventPatchBody.isNotEmpty())
    }

    @Test
    fun `public settings mapper preserves 0 percent ITBIS and realtime mapper parses category`() {
        val publicDto = PublicBusinessSettingsDto(
            companyName = "Rosa Fiesta",
            rnc = "131",
            address = "Av 27",
            phone = "8095550101",
            whatsapp = "8095550101",
            email = "info@rosafiesta.do",
            businessHours = "9-6",
            instagram = "@rf",
            facebook = "rf",
            depositPercent = 30L,
            itbisPercent = 0L,
            deliveryZones = emptyList(),
        )
        val mappedSettings = RosaFiestaBackendMappers.fromPublicSettings(publicDto, CanonicalSampleData.businessSettings)
        assertEquals(0, mappedSettings.itbisPercent)

        val eventDto = RealtimeEventDto(
            seq = 42L,
            id = "rt-42",
            type = "notification.created",
            entityId = "ntf-42",
            audience = "client",
            summary = "Recordatorio pendiente",
            payload = buildJsonObject { put("category", "recordatorios") },
        )
        val streamEvent = eventDto.toDomain()
        assertEquals("recordatorios", streamEvent.category)
    }
}
