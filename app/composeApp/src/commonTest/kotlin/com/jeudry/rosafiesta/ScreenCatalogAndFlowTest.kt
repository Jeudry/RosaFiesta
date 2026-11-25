package com.jeudry.rosafiesta

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import com.jeudry.rosafiesta.core.engine.AppView
import com.jeudry.rosafiesta.designsystem.AdminTab
import com.jeudry.rosafiesta.designsystem.PersonalTab
import com.jeudry.rosafiesta.designsystem.RfThemeState
import com.jeudry.rosafiesta.designsystem.RfThemeVariant
import com.jeudry.rosafiesta.domain.RosaFiestaRepository
import com.jeudry.rosafiesta.domain.platform.NoOpPlatformActions
import com.jeudry.rosafiesta.domain.platform.OAuthBrowserOutcome
import com.jeudry.rosafiesta.domain.platform.PickedMediaFile
import com.jeudry.rosafiesta.domain.security.AuthOutcome
import com.jeudry.rosafiesta.domain.security.InMemorySecureStorage
import com.jeudry.rosafiesta.domain.security.RegisteredUserAccount
import com.jeudry.rosafiesta.domain.security.SecureStorage
import com.jeudry.rosafiesta.domain.sound.AppAudioConfig
import com.jeudry.rosafiesta.domain.sound.NoOpSoundPlayer
import com.jeudry.rosafiesta.domain.sound.SoundEffect
import com.jeudry.rosafiesta.domain.sound.SoundEffectCategory
import com.jeudry.rosafiesta.ui.Navigator
import com.jeudry.rosafiesta.ui.RenderAdminClientProfileA27
import com.jeudry.rosafiesta.ui.RenderAdminRecordPaymentA21
import com.jeudry.rosafiesta.ui.RenderAdminReplyReviewA34
import com.jeudry.rosafiesta.ui.RosaFiestaViewModel
import com.jeudry.rosafiesta.ui.ScreenCatalog
import com.jeudry.rosafiesta.ui.SketchCanvasElement
import com.jeudry.rosafiesta.ui.SketchElementKind
import com.jeudry.rosafiesta.ui.buildSmartChairElementForTap
import com.jeudry.rosafiesta.ui.formatReadableSpanishDateText
import com.jeudry.rosafiesta.ui.formatSpanishDateWithWords
import com.jeudry.rosafiesta.ui.removeSketchElementById
import com.jeudry.rosafiesta.ui.translateSketchElementWithAttachedChairs
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlin.io.encoding.Base64
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ScreenCatalogAndFlowTest {
    @BeforeTest
    fun disableLiveApiInAutomatedTests() {
        com.jeudry.rosafiesta.di.AppModuleRuntime.connectLiveApiByDefault = false
    }

    @AfterTest
    fun restoreLiveApiDefault() {
        com.jeudry.rosafiesta.di.AppModuleRuntime.connectLiveApiByDefault = true
    }

    @Test
    fun `verifica que el catalogo completo contenga las 135 pantallas sin duplicados`() {
        assertEquals(73, ScreenCatalog.personalScreens.size)
        assertEquals(62, ScreenCatalog.adminScreens.size)
        assertEquals(135, ScreenCatalog.allScreens.size)

        val uniqueCodes = ScreenCatalog.allScreens.map { it.code }.toSet()
        assertEquals(135, uniqueCodes.size)

        assertTrue("p00" in uniqueCodes)
        assertTrue("s01" in uniqueCodes)
        assertTrue("s15" in uniqueCodes)
        assertTrue("s88" in uniqueCodes)
        assertTrue("A00" in uniqueCodes)
        assertTrue("A30" in uniqueCodes)
        assertTrue("A39" in uniqueCodes)
        assertTrue("A40" in uniqueCodes)
        assertTrue("A41" in uniqueCodes)
        assertTrue(ScreenCatalog.findByCode("A39").isDesktopWeb)
    }

    @Test
    fun `verifica hojas inferiores modo invitado y pestanas de las secciones 4 y 5`() {
        assertTrue(ScreenCatalog.isBottomSheet("s75"))
        assertTrue(ScreenCatalog.isBottomSheet("s76"))
        assertTrue(ScreenCatalog.isBottomSheet("s77"))
        assertTrue(ScreenCatalog.isBottomSheet("s81"))
        assertTrue(ScreenCatalog.isBottomSheet("s84"))
        assertTrue(ScreenCatalog.isBottomSheet("s85"))
        assertFalse(ScreenCatalog.isBottomSheet("s14"))

        assertTrue(ScreenCatalog.isGuestScreen("s01"))
        assertTrue(ScreenCatalog.isGuestScreen("s02"))
        assertTrue(ScreenCatalog.isGuestScreen("s75"))
        assertFalse(ScreenCatalog.isGuestScreen("s14"))

        assertEquals(PersonalTab.INICIO, ScreenCatalog.activePersonalTab("s14"))
        assertEquals(PersonalTab.CATALOGO, ScreenCatalog.activePersonalTab("s18"))
        assertEquals(PersonalTab.ROSA, ScreenCatalog.activePersonalTab("s26"))
        assertEquals(PersonalTab.EVENTOS, ScreenCatalog.activePersonalTab("s37"))
        assertEquals(PersonalTab.MAS, ScreenCatalog.activePersonalTab("s24"))
        assertEquals(PersonalTab.FAVORITOS, ScreenCatalog.activePersonalTab("s02"))

        assertEquals(AdminTab.INICIO, ScreenCatalog.activeAdminTab("A01"))
        assertEquals(AdminTab.EVENTOS, ScreenCatalog.activeAdminTab("A11"))
        assertEquals(AdminTab.ROSA, ScreenCatalog.activeAdminTab("A08"))
        assertEquals(AdminTab.INVENTARIO, ScreenCatalog.activeAdminTab("A09"))
        assertEquals(AdminTab.MAS, ScreenCatalog.activeAdminTab("A12"))

        assertFalse(com.jeudry.rosafiesta.ui.shouldShowBottomBar("s26"))
        assertFalse(com.jeudry.rosafiesta.ui.shouldShowBottomBar("s27"))
        assertFalse(com.jeudry.rosafiesta.ui.shouldShowBottomBar("s34"))
        assertFalse(com.jeudry.rosafiesta.ui.shouldShowBottomBar("s17"))
        assertFalse(com.jeudry.rosafiesta.ui.shouldShowBottomBar("s87"))
        assertFalse(com.jeudry.rosafiesta.ui.shouldShowBottomBar("s88"))
        assertFalse(com.jeudry.rosafiesta.ui.shouldShowBottomBar("A08"))
        assertTrue(com.jeudry.rosafiesta.ui.shouldShowBottomBar("s14"))
        assertTrue(com.jeudry.rosafiesta.ui.shouldShowBottomBar("s37"))
        assertTrue(com.jeudry.rosafiesta.ui.shouldShowBottomBar("s54"))
    }

    @Test
    fun `navegador mantiene pila de regreso y alterna rol A30 entre Admin y Personal`() {
        val nav = Navigator(initialAppView = AppView.PERSONAL, initialScreenCode = "s14")
        assertFalse(nav.canGoBack)

        nav.navigateTo("s18")
        assertEquals("s18", nav.currentCode)
        assertTrue(nav.canGoBack)

        assertTrue(nav.popBack())
        assertEquals("s14", nav.currentCode)
        assertFalse(nav.canGoBack)

        nav.switchToAdmin("A30")
        assertEquals(AppView.ADMIN, nav.activeView)
        assertEquals("A30", nav.currentCode)

        nav.switchToPersonal("s14", fromAdminPreview = true)
        assertEquals(AppView.PERSONAL, nav.activeView)
        assertTrue(nav.adminPreviewingPersonal)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `navega entre App Personal y App Administrativa y ejecuta el cambio de vista A30`() = runComposeUiTest {
        setContent {
            RosaFiestaApp(
                initialAppView = AppView.PERSONAL,
                initialScreenCode = "s14",
                showDevHeader = true,
            )
        }

        onNodeWithTag("screen-s14").assertExists()

        // Cambiar a App Administrativa (Inicio A01)
        onNodeWithTag("switch-admin").performClick()
        onNodeWithTag("screen-A01").assertExists()

        // Abrir el panel de desarrollo con las 133 pantallas
        onNodeWithTag("open-dev-panel").performClick()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `PersonalApp y AdminApp se ejecutan como dos aplicaciones independientes sin cabecera unificada`() = runComposeUiTest {
        setContent {
            PersonalApp(initialScreenCode = "s14")
        }
        onNodeWithTag("screen-s14").assertExists()
        onNodeWithTag("switch-admin").assertDoesNotExist()
        onNodeWithTag("open-dev-panel").assertDoesNotExist()
    }

    @Test
    fun `modulo Koin por defecto inicia vacio con solo cuentas admin`() {
        val koin = org.koin.dsl.koinApplication {
            modules(com.jeudry.rosafiesta.di.appModule)
        }.koin
        val viewModel = koin.get<com.jeudry.rosafiesta.ui.RosaFiestaViewModel>()

        assertTrue(viewModel.articles.value.isEmpty())
        assertTrue(viewModel.events.value.isEmpty())
        assertTrue(viewModel.payments.value.isEmpty())
        assertTrue(viewModel.checklist.value.isEmpty())
        assertEquals(0, viewModel.metrics.value.octoberRevenueDop)
    }

    @Test
    fun `modulo Koin resuelve RosaFiestaViewModel y muta estado reactivo`() {
        val koin = org.koin.dsl.koinApplication {
            modules(com.jeudry.rosafiesta.di.buildAppModule(seedDemoData = true))
        }.koin
        val viewModel = koin.get<com.jeudry.rosafiesta.ui.RosaFiestaViewModel>()
        val apiClient = koin.get<com.jeudry.rosafiesta.data.RosaFiestaApiClient>()

        assertEquals("http://localhost:8788", apiClient.baseUrl)
        assertEquals(6, viewModel.articles.value.size)
        assertEquals(5, viewModel.events.value.size)
        assertEquals(5, viewModel.payments.value.size)

        viewModel.toggleChecklistStep("chk-5")
        assertTrue(viewModel.checklist.value.first { it.id == "chk-5" }.completed)

        viewModel.adjustArticleStock("art-arco-rosa", 2)
        assertEquals(12, viewModel.articles.value.first { it.id == "art-arco-rosa" }.totalStock)

        viewModel.syncOfflineQueue()
        val offlineOnlyRepo = com.jeudry.rosafiesta.data.DefaultRosaFiestaRepository(seedDemoData = true)
        offlineOnlyRepo.syncOfflineQueue()
        assertTrue(offlineOnlyRepo.offlineQueue.value.all { it.synced })
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `pantalla s46 conectada a RosaFiestaViewModel alterna paso del checklist al hacer clic`() = runComposeUiTest {
        val koin = org.koin.dsl.koinApplication {
            modules(com.jeudry.rosafiesta.di.buildAppModule(seedDemoData = true))
        }.koin
        val viewModel = koin.get<com.jeudry.rosafiesta.ui.RosaFiestaViewModel>()
        assertFalse(viewModel.checklist.value.first { it.id == "chk-5" }.completed)

        setContent {
            RosaFiestaApp(
                initialAppView = AppView.PERSONAL,
                initialScreenCode = "s46",
                viewModel = viewModel,
            )
        }

        onNodeWithTag("screen-s46").assertExists()
        onNodeWithTag("checklist-step-chk-5").performClick()
        assertTrue(viewModel.checklist.value.first { it.id == "chk-5" }.completed)
    }

    @Test
    fun `RosaFiestaViewModel valida formularios alterna roles permisos y ejecuta acciones de negocio`() {
        val koin = org.koin.dsl.koinApplication {
            modules(com.jeudry.rosafiesta.di.appModule)
        }.koin
        val viewModel = koin.get<com.jeudry.rosafiesta.ui.RosaFiestaViewModel>()

        viewModel.setLoginEmail("correo-invalido")
        assertFalse(viewModel.submitLogin())
        assertTrue(viewModel.formError.value != null)

        viewModel.setLoginEmail("yuli@rosafiesta.do")
        viewModel.setLoginPassword("rosa2026")
        assertTrue(viewModel.submitLogin())
        assertEquals("staff_yuli", viewModel.activeRole.value)

        assertFalse(viewModel.staffMembers.value.first { it.name == "Yuli" }.canViewPayments)
        viewModel.toggleStaffPaymentAccess("Yuli")
        assertTrue(viewModel.staffMembers.value.first { it.name == "Yuli" }.canViewPayments)

        viewModel.setNewEventTitle("Aniversario Coral")
        viewModel.setNewEventClient("Patricia Lora")
        viewModel.setNewEventDate("Vie 20 nov")
        viewModel.setNewEventVenue("Hotel Jaragua")
        viewModel.setNewEventGuests("90")
        assertTrue(viewModel.submitNewEvent())
        assertTrue(viewModel.events.value.any { it.title == "Aniversario Coral" })

        viewModel.setNewArticleCode("RF-110")
        viewModel.setNewArticleName("Lámpara Colgante")
        viewModel.setNewArticleCategory("Iluminación")
        viewModel.setNewArticlePrice("250")
        viewModel.setNewArticleStock("15")
        assertTrue(viewModel.submitNewArticle())
        assertTrue(viewModel.articles.value.any { it.code == "RF-110" })

        viewModel.setPaymentAmountInput("1500")
        viewModel.setPaymentMethodInput("Transferencia Popular")
        viewModel.setPaymentRefInput("POP-2026-11")
        val createdEventId = viewModel.events.value.first { it.title == "Aniversario Coral" }.id
        val payment = viewModel.submitPayment(createdEventId)
        assertEquals(1500, payment?.amountDop)

        viewModel.setChatInput("Todo listo para el montaje en Jardín Botánico")
        viewModel.submitChatMessage("Camila")
        assertTrue(viewModel.chatMessages.value.last().contains("Todo listo para el montaje"))
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `pantalla A08 Rosa Asistente Admin ejecuta chips rapidos y actualiza el borrador de propuesta`() = runComposeUiTest {
        val koin = org.koin.dsl.koinApplication {
            modules(com.jeudry.rosafiesta.di.appModule)
        }.koin
        val viewModel = koin.get<com.jeudry.rosafiesta.ui.RosaFiestaViewModel>()

        setContent {
            RosaFiestaApp(
                initialAppView = AppView.ADMIN,
                initialScreenCode = "A08",
                viewModel = viewModel,
            )
        }

        onNodeWithTag("screen-A08").assertExists()
        onNodeWithText("RD$ 1,295").assertExists()

        onNodeWithText("Agregar iluminación").performClick()
        onNodeWithText("RD$ 1,475").assertExists()
        assertEquals("Agregar iluminación", viewModel.rosaProposalMessage.value)

        onNodeWithText("Asignar a un cliente").performClick()
        onNodeWithText("No hay clientes registrados aún para asignar", substring = true).assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `pantalla A08 procesa prompts compuestos con numero personalizado de invitados y sincroniza borrador`() = runComposeUiTest {
        val koin = org.koin.dsl.koinApplication {
            modules(com.jeudry.rosafiesta.di.appModule)
        }.koin
        val viewModel = koin.get<com.jeudry.rosafiesta.ui.RosaFiestaViewModel>()

        setContent {
            RosaFiestaApp(
                initialAppView = AppView.ADMIN,
                initialScreenCode = "A08",
                viewModel = viewModel,
            )
        }

        onNodeWithText("120 invitados").performClick()
        onNodeWithText("150 invitados").assertExists()
        onNodeWithText("Mesa redonda × 15").assertExists()

        onNodeWithText("Usar borrador").performClick()
        assertEquals("150", viewModel.newEventGuests.value)
        assertEquals(15, viewModel.draftQuantities.value["art-mesa-redonda"])
        assertEquals(150, viewModel.draftQuantities.value["art-silla-tiffany"])
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `pantalla A09 filtra articulos por categoria y A25 recalcula descuento del paquete`() = runComposeUiTest {
        val viewModel = RosaFiestaViewModel(
            repository = com.jeudry.rosafiesta.data.DefaultRosaFiestaRepository(),
            secureStorage = InMemorySecureStorage(),
            platformActions = NoOpPlatformActions(),
        )

        setContent {
            RosaFiestaApp(
                initialAppView = AppView.ADMIN,
                initialScreenCode = "A09",
                viewModel = viewModel,
            )
        }

        onNodeWithTag("screen-A09").assertExists()
        onNodeWithText("Centro de mesa floral").assertExists()
        onNodeWithText("Mesa redonda para 8").assertExists()

        onNodeWithText("Diseño floral").performClick()
        onNodeWithText("Centro de mesa floral").assertExists()
        onNodeWithText("Mesa redonda para 8").assertDoesNotExist()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `pantalla A25 recalcula precio normal y con descuento al alternar porcentaje`() = runComposeUiTest {
        setContent {
            RosaFiestaApp(
                initialAppView = AppView.ADMIN,
                initialScreenCode = "A25",
            )
        }

        onNodeWithTag("screen-A25").assertExists()
        onNodeWithText("RD$ 1,000").assertExists()
        onNodeWithText("RD$ 850").assertExists()

        onNodeWithText("15%").performClick()
        onNodeWithText("20%").assertExists()
        onNodeWithText("RD$ 800").assertExists()
    }

    @Test
    fun `Navigator rompe ciclos en pila de regreso y reemplaza pestanas hermanas sin duplicar backStack`() {
        val nav = Navigator(initialAppView = AppView.PERSONAL, initialScreenCode = "s14")
        nav.navigateTo("s37")
        nav.navigateTo("s40")
        nav.navigateTo("s43")

        nav.navigateTo("s40")
        assertEquals("s40", nav.currentCode)
        assertTrue(nav.popBack())
        assertEquals("s37", nav.currentCode)
        assertTrue(nav.popBack())
        assertEquals("s14", nav.currentCode)
        assertFalse(nav.canGoBack)

        val adminNav = Navigator(initialAppView = AppView.ADMIN, initialScreenCode = "A12")
        adminNav.navigateTo("s58")
        adminNav.navigateTo("s59")
        adminNav.navigateTo("s62")
        assertEquals("s62", adminNav.currentCode)

        assertTrue(adminNav.popBack())
        assertEquals("A12", adminNav.currentCode)
        assertFalse(adminNav.canGoBack)
    }

    @Test
    fun `RosaFiestaViewModel alterna busqueda por voz en linea y distingue entre origen de buscador y Rosa IA`() {
        val koin = org.koin.dsl.koinApplication {
            modules(com.jeudry.rosafiesta.di.appModule)
        }.koin
        val viewModel = koin.get<com.jeudry.rosafiesta.ui.RosaFiestaViewModel>()

        assertEquals(com.jeudry.rosafiesta.ui.VoiceInputOrigin.NONE, viewModel.voiceInputOrigin.value)
        assertFalse(viewModel.isVoiceListening.value)

        viewModel.toggleInlineVoiceSearch()
        assertEquals(com.jeudry.rosafiesta.ui.VoiceInputOrigin.SEARCH, viewModel.voiceInputOrigin.value)
        assertTrue(viewModel.isVoiceListening.value)

        viewModel.setVoiceTranscript("Silla Tiffany dorada")
        assertEquals("Silla Tiffany dorada", viewModel.voiceTranscript.value)

        viewModel.toggleInlineVoiceSearch()
        assertFalse(viewModel.isVoiceListening.value)

        viewModel.startVoiceInput(com.jeudry.rosafiesta.ui.VoiceInputOrigin.ROSA_WIZARD)
        assertEquals(com.jeudry.rosafiesta.ui.VoiceInputOrigin.ROSA_WIZARD, viewModel.voiceInputOrigin.value)
        assertTrue(viewModel.isVoiceListening.value)

        viewModel.stopVoiceInput()
        assertFalse(viewModel.isVoiceListening.value)
    }

    @Test
    fun `RosaFiestaViewModel rechaza datos invalidos al editar perfil sin corromper el estado activo`() {
        val koin = org.koin.dsl.koinApplication {
            modules(com.jeudry.rosafiesta.di.appModule)
        }.koin
        val viewModel = koin.get<com.jeudry.rosafiesta.ui.RosaFiestaViewModel>()

        val rejectedUnauthenticated = viewModel.saveProfile(
            draftName = "Jeudry Rosario",
            draftEmail = "jeudry@rosafiesta.do",
            draftPhone = "809-555-0199",
            draftBirthDate = "22/09/1996",
            draftUsername = "jeudry_rf",
        )
        assertFalse(rejectedUnauthenticated)

        val registeredInitialClient = viewModel.submitRegistration(
            firstName = "Ana",
            lastName = "Perez",
            username = "ana_perez",
            email = "ana.perez@correo.do",
            password = "Password2026",
            birthDate = "22/09/1996",
            acceptedTerms = true,
            phone = "809-555-0140",
        )
        assertTrue(registeredInitialClient)

        val originalEmail = viewModel.profileEmail.value
        val rejectedInvalidEmail = viewModel.saveProfile(
            draftName = "Jeudry",
            draftEmail = "jeudry",
            draftPhone = "809-555-0102",
            draftBirthDate = "22/09/1996",
            draftUsername = "yuli",
        )
        assertFalse(rejectedInvalidEmail)
        assertEquals(originalEmail, viewModel.profileEmail.value)
        assertTrue(viewModel.formError.value != null)

        val rejectedShortPhone = viewModel.saveProfile(
            draftName = "Jeudry",
            draftEmail = "jeudry@rosafiesta.do",
            draftPhone = "809",
            draftBirthDate = "22/09/1996",
            draftUsername = "jeudry",
        )
        assertFalse(rejectedShortPhone)

        val savedValidProfile = viewModel.saveProfile(
            draftName = "Jeudry Rosario",
            draftEmail = "jeudry@rosafiesta.do",
            draftPhone = "809-555-0199",
            draftBirthDate = "22/09/1996",
            draftUsername = "jeudry_rf",
            draftAvatarKey = "BODA",
        )
        assertTrue(savedValidProfile)
        assertEquals("Jeudry Rosario", viewModel.profileName.value)
        assertEquals("jeudry@rosafiesta.do", viewModel.profileEmail.value)
        assertEquals("809-555-0199", viewModel.profilePhone.value)
        assertEquals("22/09/1996", viewModel.profileBirthDate.value)
        assertEquals("jeudry_rf", viewModel.profileUsername.value)
        assertEquals("BODA", viewModel.profileAvatarKey.value)

        assertEquals("22/", com.jeudry.rosafiesta.ui.formatDateDigitsWithSlashes("2", "22"))
        assertEquals("22/09/", com.jeudry.rosafiesta.ui.formatDateDigitsWithSlashes("22/0", "22/09"))
        assertEquals("22/09/1996", com.jeudry.rosafiesta.ui.formatDateDigitsWithSlashes("", "22091996"))

        assertEquals("5.0", viewModel.companyAverageRatingFormatted())
        assertFalse(viewModel.submitCompanyReview("Excelente montaje y atención", 5))
        assertEquals(0, viewModel.companyReviews.value.size)

        val initialChatCount = viewModel.directChatMessages.value.size
        viewModel.sendDirectChatMessage("Quiero cotizar sillas Tiffany")
        assertEquals(initialChatCount + 1, viewModel.directChatMessages.value.size)

        val registeredNewClient = viewModel.submitRegistration(
            firstName = "Carlos",
            lastName = "Santana",
            username = "carlos_santana",
            email = "carlos.santana@correo.do",
            password = "Password2026",
            birthDate = "10/05/1994",
            acceptedTerms = true,
            phone = "809-555-0991",
        )
        assertTrue(registeredNewClient)
        assertTrue(viewModel.clientEventsForCurrentUser().isEmpty())
        assertTrue(viewModel.clientPaymentsForCurrentUser().isEmpty())

        viewModel.setDarkTheme(true)
        assertTrue(viewModel.isDarkTheme.value)
        viewModel.setDarkTheme(false)
        assertFalse(viewModel.isDarkTheme.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `Yuli sin permiso de pagos ve pantalla restringida en A20 hasta habilitar acceso en ViewModel`() = runComposeUiTest {
        val viewModel = RosaFiestaViewModel(
            repository = com.jeudry.rosafiesta.data.DefaultRosaFiestaRepository(),
            secureStorage = InMemorySecureStorage(),
            platformActions = NoOpPlatformActions(),
        )
        viewModel.switchRole("staff_yuli")

        val restrictedCodes = listOf("A20", "A21", "A32", "A33", "s57", "s58", "s59", "s67")
        restrictedCodes.forEach { restrictedCode ->
            assertTrue(viewModel.isScreenRestrictedForActiveStaff(restrictedCode))
        }
        assertFalse(viewModel.isScreenRestrictedForActiveStaff("A01"))

        setContent {
            RosaFiestaApp(
                initialAppView = AppView.ADMIN,
                initialScreenCode = "A20",
                viewModel = viewModel,
            )
        }

        onNodeWithTag("screen-A20").assertExists()
        onNodeWithTag("staff-payment-restricted-A20").assertExists()

        viewModel.toggleStaffPaymentAccess("Yuli")
        waitForIdle()

        assertFalse(viewModel.isScreenRestrictedForActiveStaff("A20"))
        onNodeWithTag("staff-payment-restricted-A20").assertDoesNotExist()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `PersonalApp abre panel Mas consume toques internos sin cerrar y navega a Mis reservas s53`() = runComposeUiTest {
        setContent {
            PersonalApp(initialScreenCode = "s14")
        }

        onNodeWithTag("screen-s14").assertExists()
        onNodeWithText("Más").performClick()

        onNodeWithTag("personal-more-panel").assertExists()
        onNodeWithTag("more-option-s24").assertExists()
        onNodeWithTag("more-option-s82").assertExists()
        onNodeWithTag("more-option-s53").assertExists()
        onNodeWithTag("more-option-s86").assertExists()
        onNodeWithTag("more-option-s16").assertExists()
        onNodeWithTag("more-option-s54").assertDoesNotExist()

        onNodeWithTag("personal-more-panel").performTouchInput {
            val paddingPoint = Offset(centerX, top + 4f)
            click(paddingPoint)
        }
        onNodeWithTag("personal-more-panel").assertExists()
        onNodeWithTag("screen-s14").assertExists()

        onNodeWithTag("personal-more-scrim").performTouchInput {
            val outsideScrimPoint = Offset(centerX, top + 10f)
            click(outsideScrimPoint)
        }
        onNodeWithTag("personal-more-panel").assertDoesNotExist()

        onNodeWithText("Más").performClick()
        onNodeWithTag("more-option-s53").performClick()
        onNodeWithTag("personal-more-panel").assertDoesNotExist()
        onNodeWithTag("screen-s53").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `modo invitado en s01 redirige pestana Paquetes al modal de autenticacion s75`() = runComposeUiTest {
        setContent {
            PersonalApp(initialScreenCode = "s01")
        }

        onNodeWithTag("screen-s01").assertExists()
        onNodeWithText("Paquetes").performClick()
        onNodeWithTag("screen-s75").assertExists()
    }

    @Test
    fun `Navigator resuelve BACK con destino explicito en pila o vacio y catalogo sincroniza titulos y defaultNextCode`() {
        val stackedNav = Navigator(initialAppView = AppView.PERSONAL, initialScreenCode = "s14")
        stackedNav.navigateTo("s37")
        stackedNav.navigateTo("s40")
        stackedNav.navigateTo("s46")

        stackedNav.navigateTo("BACK:s37")
        assertEquals("s37", stackedNav.currentCode)
        assertTrue(stackedNav.popBack())
        assertEquals("s14", stackedNav.currentCode)

        val emptyStackNav = Navigator(initialAppView = AppView.PERSONAL, initialScreenCode = "s87")
        emptyStackNav.navigateTo("BACK:s86")
        assertEquals("s86", emptyStackNav.currentCode)

        assertEquals("s14", ScreenCatalog.defaultNextCode("s07"))
        assertEquals("s14", ScreenCatalog.defaultNextCode("s11"))
        assertEquals("s10", ScreenCatalog.defaultNextCode("s12"))
        assertEquals("s18", ScreenCatalog.defaultNextCode("s81"))
        assertEquals("s83", ScreenCatalog.defaultNextCode("s51"))
        assertEquals("s54", ScreenCatalog.defaultNextCode("s86"))
        assertEquals("s86", ScreenCatalog.defaultNextCode("s87"))

        assertEquals("Cuenta activada", ScreenCatalog.findByCode("s11").title)
        assertEquals("Correo verificado", ScreenCatalog.findByCode("s12").title)
        assertEquals("Nuevo proveedor", ScreenCatalog.findByCode("s56").title)
        assertEquals("Análisis", ScreenCatalog.findByCode("s57").title)
        assertEquals("Panel · Resumen", ScreenCatalog.findByCode("s58").title)
        assertEquals("Panel · Finanzas", ScreenCatalog.findByCode("s59").title)
        assertEquals("Panel · Seguros", ScreenCatalog.findByCode("s60").title)
        assertEquals("Panel · Auditoría", ScreenCatalog.findByCode("s61").title)
        assertEquals("Panel · Clientes", ScreenCatalog.findByCode("s62").title)
        assertEquals("Leads · Seguimientos", ScreenCatalog.findByCode("s65").title)
        assertEquals("Leads · Estadísticas", ScreenCatalog.findByCode("s66").title)
        assertEquals("Reportes · Ingresos", ScreenCatalog.findByCode("s67").title)
        assertEquals("Reportes · Eventos", ScreenCatalog.findByCode("s68").title)
        assertEquals("Reportes · Clientes", ScreenCatalog.findByCode("s69").title)
        assertEquals("Reportes · Inventario", ScreenCatalog.findByCode("s70").title)
        assertEquals("Bot de WhatsApp · Preguntas", ScreenCatalog.findByCode("s72").title)
        assertEquals("Bot de WhatsApp · Palabras clave", ScreenCatalog.findByCode("s73").title)

        val s87Spec = ScreenCatalog.findByCode("s87")
        assertFalse(s87Spec.title.contains("ejemplo", ignoreCase = true))
        assertFalse(s87Spec.subtitle.contains("ejemplo", ignoreCase = true))
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `pantalla s54 omite Ajustes y Ayuda y previsualiza foto personalizada decodificada`() = runComposeUiTest {
        val samplePngBytes = Base64.decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+ip1sAAAAASUVORK5CYII=",
        )
        val platformActions = NoOpPlatformActions().apply {
            queuedPickedFile = PickedMediaFile(
                fileName = "camila-avatar.png",
                mimeType = "image/png",
                sizeBytes = samplePngBytes.size.toLong(),
                bytes = samplePngBytes,
            )
        }
        val koin = org.koin.dsl.koinApplication {
            modules(com.jeudry.rosafiesta.di.appModule)
        }.koin
        val secureStorage = InMemorySecureStorage()
        val viewModel = RosaFiestaViewModel(
            repository = koin.get<RosaFiestaRepository>(),
            platformActions = platformActions,
            secureStorage = secureStorage,
        )

        setContent {
            RosaFiestaApp(
                initialAppView = AppView.PERSONAL,
                initialScreenCode = "s54",
                viewModel = viewModel,
            )
        }

        onNodeWithTag("screen-s54").assertExists()
        assertEquals("15 de agosto de 1998", formatSpanishDateWithWords("15/08/1998"))
        assertEquals("15 de agosto de 1998", formatSpanishDateWithWords("1998-08-15"))
        onNodeWithText("Pagos").assertExists()
        onNodeWithText("Mis reservas").assertExists()
        onNodeWithText("Eliminar cuenta").assertExists()
        onNodeWithText("Ajustes").assertDoesNotExist()
        onNodeWithText("Ayuda / Planificación").assertDoesNotExist()

        mainClock.autoAdvance = false
        viewModel.pickProfileAvatarFromDevice()
        waitUntil(timeoutMillis = 3_000L) {
            viewModel.profileAvatarBytes.value != null
        }
        mainClock.advanceTimeBy(100L)

        assertEquals("CUSTOM:camila-avatar.png", viewModel.profileAvatarKey.value)
        onNodeWithText("Foto 'camila-avatar.png' seleccionada.").assertExists()
        onNodeWithContentDescription("Foto de perfil").assertExists()
        onNodeWithText("camila-avatar.png").assertDoesNotExist()

        mainClock.advanceTimeBy(3_100L)
        mainClock.autoAdvance = true
        waitForIdle()
        onNodeWithText("Foto 'camila-avatar.png' seleccionada.").assertDoesNotExist()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `modal de filtros s81 preserva pantalla de origen y transicion modal al abrir y cerrar`() = runComposeUiTest {
        val navigator = com.jeudry.rosafiesta.ui.Navigator(AppView.PERSONAL, "s14")
        navigator.navigateTo("s81")
        assertEquals("s81", navigator.currentCode)
        assertEquals("s14", navigator.previousCode)
        assertEquals(com.jeudry.rosafiesta.ui.NavigationTransitionKind.MODAL_SHEET, navigator.lastTransition)

        navigator.navigateTo("BACK:s14")
        assertEquals("s14", navigator.currentCode)
        assertEquals(com.jeudry.rosafiesta.ui.NavigationTransitionKind.MODAL_SHEET, navigator.lastTransition)

        val koin = org.koin.dsl.koinApplication {
            modules(com.jeudry.rosafiesta.di.appModule)
        }.koin
        val viewModel = RosaFiestaViewModel(
            repository = koin.get<RosaFiestaRepository>(),
            platformActions = NoOpPlatformActions(),
            secureStorage = InMemorySecureStorage(),
        )

        setContent {
            com.jeudry.rosafiesta.ui.RenderScreenSpec(
                spec = ScreenCatalog.findByCode("s81"),
                viewModel = viewModel,
                underlyingScreenCode = "s14",
                isFrameHarness = false,
                includeBottomBar = false,
            )
        }

        onNodeWithTag("screen-s81").assertExists()
        onNodeWithText("Filtros").assertExists()
        onNodeWithText("Hola de nuevo").assertExists()
        onNodeWithText("¿Qué celebramos hoy? (sillas, arcos, mesas...)").assertExists()
        onNodeWithText("Buscar sillas, mesas, arcos...").assertDoesNotExist()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `pantalla s82 separa proximos pagos e historial en pestanas y Navigator mantiene transiciones simetricas`() = runComposeUiTest {
        val navigator = Navigator(AppView.PERSONAL, "s14")
        navigator.navigateTo("s21")
        assertEquals(com.jeudry.rosafiesta.ui.NavigationTransitionKind.FORWARD, navigator.lastTransition)

        navigator.navigateTo("BACK:s14")
        assertEquals(com.jeudry.rosafiesta.ui.NavigationTransitionKind.BACKWARD, navigator.lastTransition)

        navigator.navigateTo("s18")
        assertEquals(com.jeudry.rosafiesta.ui.NavigationTransitionKind.TAB_NEXT, navigator.lastTransition)
        navigator.navigateTo("s37")
        assertEquals(com.jeudry.rosafiesta.ui.NavigationTransitionKind.TAB_NEXT, navigator.lastTransition)
        navigator.navigateTo("s18")
        assertEquals(com.jeudry.rosafiesta.ui.NavigationTransitionKind.TAB_PREV, navigator.lastTransition)
        navigator.navigateTo("s19")
        assertEquals(com.jeudry.rosafiesta.ui.NavigationTransitionKind.FORWARD, navigator.lastTransition)
        navigator.navigateTo("s14")
        assertEquals(com.jeudry.rosafiesta.ui.NavigationTransitionKind.TAB_PREV, navigator.lastTransition)

        setContent {
            com.jeudry.rosafiesta.ui.RenderScreenSpec(
                spec = ScreenCatalog.findByCode("s82"),
                isFrameHarness = false,
                includeBottomBar = false,
            )
        }

        onNodeWithTag("payments-tab-0").assertExists()
        onNodeWithTag("payments-tab-1").assertExists()
        onNodeWithText("PRÓXIMOS PAGOS").assertExists()
        onNodeWithText("Método de pago preferido").assertExists()

        onNodeWithTag("payments-tab-1").performClick()
        onNodeWithText("COMPROBANTES EMITIDOS").assertExists()
    }

    @Test
    fun `flujo Rosa IA conserva titulo horarios presupuesto y selecciona el evento creado`() {
        assertEquals("s31", ScreenCatalog.defaultNextCode("s30"))
        assertEquals("s32", ScreenCatalog.defaultNextCode("s31"))

        val koin = org.koin.dsl.koinApplication {
            modules(com.jeudry.rosafiesta.di.appModule)
        }.koin
        val viewModel = koin.get<RosaFiestaViewModel>()

        viewModel.setEventTypeInput("Boda")
        viewModel.setClientEventTitleInput("Boda Jardín Encantado")
        viewModel.setRosaBudgetDop(75000)
        viewModel.setEventDateInput("25/12/2026")
        viewModel.setEventGuestsInput("120")
        viewModel.setEventVenueInput("Jardín Botánico Nacional")
        viewModel.addWizardScheduleItem("18:00", "19:00", "Ceremonia al aire libre")

        assertTrue(viewModel.submitQuickClientEvent())
        val createdEvent = viewModel.activeClientEvent()
        assertEquals("Boda Jardín Encantado", createdEvent.title)
        assertTrue(viewModel.eventTimelineItems.value.any { timelineBlock -> timelineBlock.title == "Ceremonia al aire libre" })

        val eventId = createdEvent.id
        viewModel.markCalendarPromptDismissed(eventId)
        assertTrue(eventId in viewModel.calendarSyncPromptDismissedIds.value)
    }

    @Test
    fun `formatea fechas legibles en espanol completo desde formatos ISO barra y abreviados`() {
        assertEquals("Vie, 23 de octubre 2026", formatReadableSpanishDateText("2026-10-23"))
        assertEquals("Vie, 21 de agosto 1998", formatReadableSpanishDateText("21/08/1998"))
        assertEquals("Vie 23 de octubre", formatReadableSpanishDateText("Vie 23 oct"))
        assertEquals("21 de agosto de 1998", formatSpanishDateWithWords("21/08/1998"))
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `presupuesto vacio horarios en siguiente fase invitados y notificaciones sin datos falsos`() = runComposeUiTest {
        val koin = org.koin.dsl.koinApplication {
            modules(com.jeudry.rosafiesta.di.appModule)
        }.koin
        val viewModel = koin.get<RosaFiestaViewModel>()

        assertEquals(0, viewModel.rosaBudgetDop.value)
        viewModel.updateRosaParameters(guests = 90)
        assertEquals(0, viewModel.rosaBudgetDop.value)

        assertTrue(viewModel.guestsForActiveEvent().isEmpty())
        viewModel.setGuestsWizardMode(true)
        assertTrue(viewModel.guestsForActiveEvent().isEmpty())
        viewModel.setGuestsWizardMode(false)

        assertTrue(viewModel.filterNotificationsForPersonalClient().isEmpty())

        setContent {
            com.jeudry.rosafiesta.ui.RenderScreenSpec(
                spec = ScreenCatalog.findByCode("s31"),
                isFrameHarness = false,
                includeBottomBar = false,
                viewModel = viewModel,
            )
        }
        onNodeWithText("Presupuesto RD$").assertExists()
        onNodeWithText("Invitados (opc.)").assertExists()
        onNodeWithText("HORARIOS DEL EVENTO").assertDoesNotExist()
    }

    @Test
    fun `boceto coloca sillas automaticas alrededor de mesas y selector mueve o borra elemento`() {
        val roundTable = SketchCanvasElement(
            id = "tbl-1",
            kind = SketchElementKind.ROUND_TABLE,
            normX = 0.50f,
            normY = 0.50f,
            colorHex = 0xFFEA4EA9L,
            label = "Mesa 1",
        )
        var elements = listOf(roundTable)

        val firstChair = requireNotNull(
            buildSmartChairElementForTap(
                elements = elements,
                tapNormX = 0.51f,
                tapNormY = 0.49f,
                colorHex = 0xFFEA4EA9L,
                chairId = "ch-1",
            ),
        )
        elements = elements + firstChair
        val secondChair = requireNotNull(
            buildSmartChairElementForTap(
                elements = elements,
                tapNormX = 0.50f,
                tapNormY = 0.52f,
                colorHex = 0xFFEA4EA9L,
                chairId = "ch-2",
            ),
        )
        elements = elements + secondChair

        for (extraSeat in 3..10) {
            val seatedChair = requireNotNull(
                buildSmartChairElementForTap(
                    elements = elements,
                    tapNormX = 0.50f,
                    tapNormY = 0.50f,
                    colorHex = 0xFFEA4EA9L,
                    chairId = "ch-$extraSeat",
                ),
            )
            elements = elements + seatedChair
        }
        val overflowChair = buildSmartChairElementForTap(
            elements = elements,
            tapNormX = 0.50f,
            tapNormY = 0.50f,
            colorHex = 0xFFEA4EA9L,
            chairId = "ch-11",
        )
        assertNull(overflowChair)

        elements = listOf(roundTable, firstChair, secondChair)

        assertEquals(SketchElementKind.SINGLE_CHAIR, firstChair.kind)
        assertEquals("seat:tbl-1:0", firstChair.label)
        assertEquals("seat:tbl-1:1", secondChair.label)
        val shiftedHorizontally = firstChair.normX != secondChair.normX
        val shiftedVertically = firstChair.normY != secondChair.normY
        val placedInDistinctSlots = if (shiftedHorizontally) true else shiftedVertically
        assertTrue(placedInDistinctSlots)

        val moved = translateSketchElementWithAttachedChairs(
            elements = elements,
            movedIndex = 0,
            deltaX = 0.10f,
            deltaY = -0.10f,
        )
        assertEquals(0.60f, moved[0].normX, 0.005f)
        assertEquals(firstChair.normX + 0.10f, moved[1].normX, 0.005f)
        assertEquals(secondChair.normY - 0.10f, moved[2].normY, 0.005f)

        val afterDelete = removeSketchElementById(moved, "ch-1")
        assertEquals(2, afterDelete.size)
        assertFalse(afterDelete.any { item -> item.id == "ch-1" })
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `resumen rosa s32 incluye horarios del evento fuera de s31`() = runComposeUiTest {
        setContent {
            com.jeudry.rosafiesta.ui.RenderScreenSpec(
                spec = ScreenCatalog.findByCode("s32"),
                isFrameHarness = false,
                includeBottomBar = false,
            )
        }
        onNodeWithText("HORARIOS DEL EVENTO").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `pantalla invitados s47 inicia limpia sin invitados hardcodeados`() = runComposeUiTest {
        setContent {
            com.jeudry.rosafiesta.ui.RenderScreenSpec(
                spec = ScreenCatalog.findByCode("s47"),
                isFrameHarness = false,
                includeBottomBar = false,
            )
        }
        onNodeWithText("Familia Pérez Martínez").assertDoesNotExist()
        onNodeWithText("Aún no hay invitados agregados. Usa «+ Agregar invitado» o continúa.").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `notificaciones cliente s79 inicia limpia sin avisos administrativos hardcodeados`() = runComposeUiTest {
        setContent {
            com.jeudry.rosafiesta.ui.RenderScreenSpec(
                spec = ScreenCatalog.findByCode("s79"),
                isFrameHarness = false,
                includeBottomBar = false,
            )
        }
        onNodeWithText("Anticipo pendiente de verificación").assertDoesNotExist()
        onNodeWithText("Sin notificaciones por ahora").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `catalogo s18 mantiene Todas fijado e incluye categorias con iconos`() = runComposeUiTest {
        mainClock.autoAdvance = false
        setContent {
            com.jeudry.rosafiesta.ui.RenderScreenSpec(
                spec = ScreenCatalog.findByCode("s18"),
                isFrameHarness = false,
                includeBottomBar = false,
            )
        }
        mainClock.advanceTimeByFrame()
        onNodeWithText("Todas ·", substring = true).assertExists()
        onNodeWithText("Mobiliario").assertExists()
    }

    @Test
    fun `viewmodel sincroniza y persiste configuracion de audio volumen y categorias`() = kotlinx.coroutines.test.runTest {
        val storage = InMemorySecureStorage()
        val firstPlayer = NoOpSoundPlayer()
        val firstViewModel = RosaFiestaViewModel(
            repository = com.jeudry.rosafiesta.data.DefaultRosaFiestaRepository(),
            soundPlayer = firstPlayer,
            secureStorage = storage,
        )

        firstViewModel.stepSoundVolume(15)
        assertEquals(90, firstViewModel.audioConfig.value.volumePercent())
        assertEquals(0.90f, firstPlayer.soundVolume)

        firstViewModel.setAudioCategoryEnabled(SoundEffectCategory.ROSA_ASSISTANT, false)
        assertFalse(SoundEffectCategory.ROSA_ASSISTANT in firstPlayer.enabledCategories)
        firstViewModel.previewAudioEffect(SoundEffect.ROSA_REPLY)
        assertEquals(SoundEffect.TAP_SOFT, firstPlayer.lastPlayedEffect)

        firstViewModel.setSoundEffectsEnabled(false)
        assertFalse(firstPlayer.isEnabled)
        firstViewModel.previewAudioEffect(SoundEffect.PAYMENT_SUCCESS)
        assertEquals(
            "Activa los efectos de sonido para escuchar la prueba.",
            firstViewModel.formFeedback.value,
        )

        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
            for (attempt in 0 until 50) {
                val persistedEnabled = storage.get(com.jeudry.rosafiesta.domain.security.SecureStorage.KEY_AUDIO_ENABLED)
                if (persistedEnabled == "false") break
                kotlinx.coroutines.delay(20)
            }
        }

        val restoredPlayer = NoOpSoundPlayer()
        val secondViewModel = RosaFiestaViewModel(
            repository = com.jeudry.rosafiesta.data.DefaultRosaFiestaRepository(),
            soundPlayer = restoredPlayer,
            secureStorage = storage,
        )
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
            for (attempt in 0 until 50) {
                if (!secondViewModel.audioConfig.value.soundEffectsEnabled) break
                kotlinx.coroutines.delay(20)
            }
        }
        assertFalse(secondViewModel.audioConfig.value.soundEffectsEnabled)
        assertEquals(90, secondViewModel.audioConfig.value.volumePercent())
        assertFalse(secondViewModel.audioConfig.value.rosaAssistantSoundsEnabled)
        assertFalse(restoredPlayer.isEnabled)

        secondViewModel.resetAudioDefaults()
        assertEquals(AppAudioConfig.DEFAULT, secondViewModel.audioConfig.value)
        assertTrue(restoredPlayer.isEnabled)
        assertEquals(SoundEffect.ROSA_REPLY, restoredPlayer.lastPlayedEffect)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `ajustes s86 expone configuracion completa de audio volumen y categorias`() = runComposeUiTest {
        setContent {
            com.jeudry.rosafiesta.ui.RenderScreenSpec(
                spec = ScreenCatalog.findByCode("s86"),
                isFrameHarness = false,
                includeBottomBar = false,
            )
        }
        onNodeWithTag("personal-settings-menu-preferences").assertExists()
        onNodeWithTag("personal-settings-menu-audio").assertExists()
        onNodeWithTag("personal-settings-menu-account").assertExists()
        onNodeWithTag("personal-settings-menu-notifications").assertExists()
        onNodeWithTag("personal-settings-menu-security_sync").assertExists()
        onNodeWithTag("personal-settings-menu-legal").assertExists()

        onNodeWithTag("personal-settings-menu-audio").performClick()
        onNodeWithText("Audio y efectos de sonido").assertExists()
        onNodeWithText("Efectos de sonido").assertExists()
        onNodeWithText("Volumen de efectos").assertExists()
        onNodeWithText("75 %").assertExists()
        onNodeWithText("Botones, catálogo y boceto").assertExists()
        onNodeWithText("Asistente Rosa IA").assertExists()
        onNodeWithText("Pagos y alertas").assertExists()

        onNodeWithTag("audio-volume-plus").performClick()
        onNodeWithText("85 %").assertExists()

        onNodeWithTag("settings-back-button").performClick()
        onNodeWithTag("personal-settings-menu-preferences").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `ajustes admin A31 incluye tarjeta de audio y efectos de sonido`() = runComposeUiTest {
        setContent {
            com.jeudry.rosafiesta.ui.RenderScreenSpec(
                spec = ScreenCatalog.findByCode("A31"),
                isFrameHarness = false,
                includeBottomBar = false,
            )
        }
        onNodeWithTag("admin-settings-menu-business_data").assertExists()
        onNodeWithTag("admin-settings-menu-delivery_zones").assertExists()
        onNodeWithTag("admin-settings-menu-reservations_deposit").assertExists()
        onNodeWithTag("admin-settings-menu-audio").assertExists()
        onNodeWithTag("admin-settings-menu-security_prefs").assertExists()

        onNodeWithTag("admin-settings-menu-audio").performClick()
        onNodeWithText("Audio y efectos de sonido").assertExists()
        onNodeWithText("Probar").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `onboarding admin A30 muestra etapas visuales ilustradas y llega a entrada`() = runComposeUiTest {
        setContent {
            com.jeudry.rosafiesta.ui.RenderScreenSpec(
                spec = ScreenCatalog.findByCode("A30"),
                isFrameHarness = false,
                includeBottomBar = false,
            )
        }
        onNodeWithText("01 · OPERACIONES EN VIVO").assertExists()
        onNodeWithText("Tu semana de eventos, de un vistazo").assertExists()
        onNodeWithText("Omitir").assertExists()

        onNodeWithTag("admin-onboarding-next").performClick()
        waitForIdle()
        onNodeWithText("02 · COPILOTO ROSA IA").assertExists()
        onNodeWithText("Rosa IA cuida tu stock por fecha").assertExists()

        onNodeWithTag("admin-onboarding-skip").performClick()
        waitForIdle()
        onNodeWithText("Elige cómo deseas entrar hoy").assertExists()
        onNodeWithText("Entrar al panel Admin").assertExists()
        onNodeWithText("Entrar como cliente").assertExists()
    }

    private fun createEmptySeedTestViewModel(): RosaFiestaViewModel = RosaFiestaViewModel(
        repository = com.jeudry.rosafiesta.data.DefaultRosaFiestaRepository(seedDemoData = false),
        platformActions = NoOpPlatformActions(),
        secureStorage = InMemorySecureStorage(),
    )

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `pantallas admin y personal vacias muestran estado vacio sin explotar ni mostrar botones falsos`() = runComposeUiTest {
        val emptyViewModel = createEmptySeedTestViewModel()

        setContent {
            androidx.compose.foundation.layout.Column {
                com.jeudry.rosafiesta.ui.RenderScreenSpec(
                    spec = ScreenCatalog.findByCode("A01"),
                    viewModel = emptyViewModel,
                    isFrameHarness = false,
                    includeBottomBar = false,
                )
            }
        }
        onNodeWithText("Sin eventos programados").assertExists()
        onNodeWithText("Crear\ncategoría").assertExists()
        onNodeWithText("Boda Real Rosa & Dorado").assertDoesNotExist()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `detalle A13 chat A15 reembolso A33 pago A21 producto s19 y calendario s63 manejan listas vacias`() = runComposeUiTest {
        val emptyViewModel = createEmptySeedTestViewModel()
        val targetCodeState = androidx.compose.runtime.mutableStateOf("A13")

        setContent {
            com.jeudry.rosafiesta.ui.RenderScreenSpec(
                spec = ScreenCatalog.findByCode(targetCodeState.value),
                viewModel = emptyViewModel,
                isFrameHarness = false,
                includeBottomBar = false,
            )
        }

        onNodeWithText("Aún no tienes eventos").assertExists()
        onNodeWithText("Modo ejecución").assertDoesNotExist()

        targetCodeState.value = "A15"
        waitForIdle()
        onNodeWithText("Sin conversaciones activas").assertExists()

        targetCodeState.value = "A33"
        waitForIdle()
        onNodeWithText("Sin solicitudes de reembolso").assertExists()

        targetCodeState.value = "A21"
        waitForIdle()
        onNodeWithText("Sin eventos para cobrar").assertExists()

        targetCodeState.value = "s19"
        waitForIdle()
        onNodeWithText("Producto no disponible").assertExists()

        targetCodeState.value = "s63"
        waitForIdle()
        onNodeWithText("Sin artículos en inventario").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `entrar al panel admin desde A30 navega a vista de login s07 con recuperar usuario`() = runComposeUiTest {
        val emptyViewModel = createEmptySeedTestViewModel()

        setContent {
            RosaFiestaApp(
                initialAppView = AppView.ADMIN,
                initialScreenCode = "A30",
                viewModel = emptyViewModel,
            )
        }

        onNodeWithTag("admin-onboarding-skip").performClick()
        waitForIdle()
        onNodeWithText("Entrar al panel Admin").performClick()
        waitForIdle()

        onNodeWithTag("screen-s07").assertExists()
        onNodeWithText("Acceso Admin").assertExists()
        onNodeWithText("Recuperar usuario").assertExists()
        onNodeWithText("¿La olvidaste?").assertExists()
        assertTrue(RfThemeState.currentPalette.isAdmin)

        onNodeWithText("¿La olvidaste?").performClick()
        waitForIdle()

        onNodeWithTag("screen-s09").assertExists()
        assertTrue(RfThemeState.currentPalette.isAdmin)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `pantallas compartidas s54 y s87 conservan paleta admin al recibir override`() = runComposeUiTest {
        val emptyViewModel = createEmptySeedTestViewModel()
        val targetCodeState = androidx.compose.runtime.mutableStateOf("s54")

        setContent {
            com.jeudry.rosafiesta.ui.RenderScreenSpec(
                spec = ScreenCatalog.findByCode(targetCodeState.value),
                viewModel = emptyViewModel,
                isFrameHarness = false,
                includeBottomBar = false,
                themeVariantOverride = RfThemeVariant.ADMIN,
            )
        }

        onNodeWithTag("screen-s54").assertExists()
        assertTrue(RfThemeState.currentPalette.isAdmin)

        targetCodeState.value = "s87"
        waitForIdle()
        onNodeWithTag("screen-s87").assertExists()
        assertTrue(RfThemeState.currentPalette.isAdmin)
    }

    @Test
    fun `hojas inferiores modales registradas preservan origen y transicion MODAL_SHEET`() {
        val registeredModalCodes = listOf("A04", "A21", "s34", "s75", "s76", "s77", "s81", "s84", "s85", "s92")
        registeredModalCodes.forEach { modalCode ->
            assertTrue(ScreenCatalog.isBottomSheet(modalCode), "Expected $modalCode in ScreenCatalog.isBottomSheet")
        }
        listOf("s14", "A01", "A11", "A13").forEach { regularCode ->
            assertFalse(ScreenCatalog.isBottomSheet(regularCode))
        }

        val adminHomeNav = Navigator(initialAppView = AppView.ADMIN, initialScreenCode = "A01")
        adminHomeNav.navigateTo("A04")
        assertEquals("A04", adminHomeNav.currentCode)
        assertEquals("A01", adminHomeNav.lastModalOriginCode)
        assertEquals(com.jeudry.rosafiesta.ui.NavigationTransitionKind.MODAL_SHEET, adminHomeNav.lastTransition)
        adminHomeNav.navigateTo("BACK:A01")
        assertEquals("A01", adminHomeNav.currentCode)
        assertEquals(com.jeudry.rosafiesta.ui.NavigationTransitionKind.MODAL_SHEET, adminHomeNav.lastTransition)

        val adminEventsNav = Navigator(initialAppView = AppView.ADMIN, initialScreenCode = "A11")
        adminEventsNav.navigateTo("A04")
        assertEquals("A04", adminEventsNav.currentCode)
        assertEquals("A11", adminEventsNav.lastModalOriginCode)
        assertEquals(com.jeudry.rosafiesta.ui.NavigationTransitionKind.MODAL_SHEET, adminEventsNav.lastTransition)
        adminEventsNav.navigateTo("BACK:A11")
        assertEquals("A11", adminEventsNav.currentCode)
        assertEquals(com.jeudry.rosafiesta.ui.NavigationTransitionKind.MODAL_SHEET, adminEventsNav.lastTransition)

        val adminDetailNav = Navigator(initialAppView = AppView.ADMIN, initialScreenCode = "A13")
        adminDetailNav.navigateTo("A21")
        assertEquals("A21", adminDetailNav.currentCode)
        assertEquals("A13", adminDetailNav.lastModalOriginCode)
        assertEquals(com.jeudry.rosafiesta.ui.NavigationTransitionKind.MODAL_SHEET, adminDetailNav.lastTransition)
        adminDetailNav.navigateTo("BACK:A13")
        assertEquals("A13", adminDetailNav.currentCode)
        assertEquals(com.jeudry.rosafiesta.ui.NavigationTransitionKind.MODAL_SHEET, adminDetailNav.lastTransition)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `abrir A04 desde hero vacio de A01 muestra modal sobre A01 y regresa limpiamente`() = runComposeUiTest {
        val emptyViewModel = createEmptySeedTestViewModel()

        setContent {
            RosaFiestaApp(
                initialAppView = AppView.ADMIN,
                initialScreenCode = "A01",
                viewModel = emptyViewModel,
            )
        }

        onNodeWithTag("screen-A01").assertExists()
        onNodeWithText("Sin eventos programados").assertExists()
        onNodeWithText("Toca para crear tu primer evento o cotización").performClick()
        waitForIdle()

        onNodeWithTag("screen-A04").assertExists()
        onNodeWithText("Nuevo evento").assertExists()
        onNodeWithText("¿Cómo quieres crearlo?").assertExists()
        onNodeWithText("Sin eventos programados").assertExists()

        onNodeWithText("Cancelar").performClick()
        waitForIdle()

        onNodeWithTag("screen-A01").assertExists()
        onNodeWithText("Nuevo evento").assertDoesNotExist()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `ajustes A31 retrasa errores 2000ms tras escribir telefono y correo invalidos y limpia al corregir`() = runComposeUiTest {
        val viewModel = RosaFiestaViewModel(
            repository = com.jeudry.rosafiesta.data.DefaultRosaFiestaRepository(seedDemoData = true),
            platformActions = NoOpPlatformActions(),
            secureStorage = InMemorySecureStorage(),
        )
        mainClock.autoAdvance = false

        setContent {
            com.jeudry.rosafiesta.ui.RenderScreenSpec(
                spec = ScreenCatalog.findByCode("A31"),
                viewModel = viewModel,
                isFrameHarness = false,
                includeBottomBar = false,
            )
        }

        mainClock.advanceTimeByFrame()
        onNodeWithTag("admin-settings-menu-business_data").performClick()
        mainClock.advanceTimeByFrame()

        val phoneErrorSubstring = "El teléfono debe contener al menos 10 dígitos"
        val emailErrorSubstring = "Ingresa un correo electrónico válido"

        onNodeWithText("+1 (809) 555-0100").performTextReplacement("809")
        mainClock.advanceTimeBy(500L)
        onNodeWithText(phoneErrorSubstring, substring = true).assertDoesNotExist()

        onNodeWithText("hola@rosafiesta.do").performTextReplacement("correo-invalido")
        mainClock.advanceTimeBy(500L)
        onNodeWithText(emailErrorSubstring, substring = true).assertDoesNotExist()

        mainClock.advanceTimeBy(2_100L)
        mainClock.advanceTimeByFrame()
        onNodeWithText(phoneErrorSubstring, substring = true).assertExists()
        onNodeWithText(emailErrorSubstring, substring = true).assertExists()

        onNodeWithText("809").performTextReplacement("8095550100")
        mainClock.advanceTimeByFrame()
        onNodeWithText(phoneErrorSubstring, substring = true).assertDoesNotExist()

        onNodeWithText("correo-invalido").performTextReplacement("hola@rosafiesta.do")
        mainClock.advanceTimeByFrame()
        onNodeWithText(emailErrorSubstring, substring = true).assertDoesNotExist()
    }

    @Test
    fun `viewmodel valida NCF B01 RNC pago detallado boceto y contrasena de equipo`() = kotlinx.coroutines.test.runTest {
        val viewModel = RosaFiestaViewModel(
            repository = com.jeudry.rosafiesta.data.DefaultRosaFiestaRepository(seedDemoData = true),
            platformActions = NoOpPlatformActions(),
            secureStorage = InMemorySecureStorage(),
        )

        assertTrue(viewModel.isValidDominicanTaxId("131-84920-2"))
        assertTrue(viewModel.isValidDominicanTaxId("00112345678"))
        assertFalse(viewModel.isValidDominicanTaxId("123"))

        val targetEventId = viewModel.events.value.first().id
        val rejectedPayment = viewModel.submitDetailedPaymentFlow(
            eventId = targetEventId,
            amountDop = 2000,
            methodLabel = "Transferencia",
            bankName = "Banco Popular",
            referenceCode = "REF-BAD-RNC",
            voucherUrl = "",
            ncfType = "B01",
            clientRnc = "123",
            kindLabel = "Abono",
        )
        assertNull(rejectedPayment)
        assertFalse(viewModel.formError.value.isNullOrBlank())

        val acceptedPayment = viewModel.submitDetailedPaymentFlow(
            eventId = targetEventId,
            amountDop = 2500,
            methodLabel = "Transferencia",
            bankName = "Banco Popular",
            referenceCode = "REF-OK-01",
            voucherUrl = "/v1/media/voucher-1",
            ncfType = "B01",
            clientRnc = "131849202",
            kindLabel = "Abono",
        )
        assertNotNull(acceptedPayment)
        assertEquals("B01", acceptedPayment.ncfType)
        assertEquals("131849202", acceptedPayment.clientRnc)
        assertFalse(acceptedPayment.verified)
        assertTrue(acceptedPayment.rejectionReason.isBlank())
        assertTrue(acceptedPayment.itbisDop > 0)

        val tableArticleId = viewModel.articles.value.firstOrNull { item -> item.code == "RF-002" }?.id
            ?: viewModel.articles.value.first().id
        viewModel.setDraftQuantity(tableArticleId, 2)
        viewModel.syncDraftQuantitiesToSketch(targetEventId)
        assertTrue(viewModel.sketchLayoutJson.value.isNotBlank())

        val invalidInvite = viewModel.inviteNewStaffMember(
            fullName = "Ana Finanzas",
            email = "ana@rosafiesta.do",
            roleLabel = "Administración",
            phone = "8095550199",
            permissions = listOf("PAYMENTS"),
            initialPassword = "123",
        )
        assertNull(invalidInvite)
        assertFalse(viewModel.formError.value.isNullOrBlank())

        val validInvite = viewModel.inviteNewStaffMember(
            fullName = "Ana Finanzas",
            email = "ana@rosafiesta.do",
            roleLabel = "Administración",
            phone = "8095550199",
            permissions = listOf("PAYMENTS"),
            initialPassword = "TempUser2026!",
        )
        assertNotNull(validInvite)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `checkout s50 muestra selector NCF ITBIS y carga de comprobante`() = runComposeUiTest {
        val viewModel = RosaFiestaViewModel(
            repository = com.jeudry.rosafiesta.data.DefaultRosaFiestaRepository(seedDemoData = true),
            platformActions = NoOpPlatformActions(),
            secureStorage = InMemorySecureStorage(),
        )
        setContent {
            com.jeudry.rosafiesta.ui.RenderScreenSpec(
                spec = ScreenCatalog.findByCode("s50"),
                viewModel = viewModel,
                isFrameHarness = false,
                includeBottomBar = false,
            )
        }
        onNodeWithText("Consumidor Final (B02)").assertExists()
        onNodeWithText("Crédito Fiscal (B01)").assertExists()
        onNodeWithText("ITBIS (18%)").assertExists()
        onNodeWithText("Adjuntar comprobante de transferencia (JPG/PNG/PDF)").assertExists()

        onNodeWithText("Crédito Fiscal (B01)").performScrollTo().performClick()
        waitForIdle()
        onNodeWithText("RNC (9 dígitos) o Cédula (11 dígitos)").performScrollTo().assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `pagos admin A20 muestra exportar DGII 607 y pestana de verificacion`() = runComposeUiTest {
        val viewModel = RosaFiestaViewModel(
            repository = com.jeudry.rosafiesta.data.DefaultRosaFiestaRepository(seedDemoData = true),
            platformActions = NoOpPlatformActions(),
            secureStorage = InMemorySecureStorage(),
        )
        setContent {
            com.jeudry.rosafiesta.ui.RenderScreenSpec(
                spec = ScreenCatalog.findByCode("A20"),
                viewModel = viewModel,
                isFrameHarness = false,
                includeBottomBar = false,
            )
        }
        onNodeWithText("Exportar 607 DGII").assertExists()
        onNodeWithText("Verificar (", substring = true).assertExists()
    }

    @Test
    fun `preferencias wifi bloquean sincronizacion cuando no hay wifi y permiten al conectarse`() {
        val platformActions = NoOpPlatformActions().apply {
            simulatedWifiConnected = false
        }
        val repo = com.jeudry.rosafiesta.data.DefaultRosaFiestaRepository(seedDemoData = true)
        val viewModel = RosaFiestaViewModel(
            repository = repo,
            platformActions = platformActions,
            secureStorage = InMemorySecureStorage(),
        )
        assertTrue(viewModel.isWifiSyncPermitted())

        viewModel.toggleAutoSyncWifiOnly()
        assertTrue(viewModel.autoSyncWifiOnly.value)
        assertFalse(viewModel.isWifiSyncPermitted())

        var syncResult = true
        viewModel.syncWithServer { ok -> syncResult = ok }
        assertFalse(syncResult)
        assertFalse(viewModel.wifiSyncBlockedNotice.value.isNullOrBlank())

        platformActions.simulatedWifiConnected = true
        assertTrue(viewModel.isWifiSyncPermitted())
    }

    @Test
    fun `bloqueo biometrico protege la sesion al activar y permite desbloquear con biometria o clave`() {
        val platformActions = NoOpPlatformActions().apply {
            simulatedDeviceAuthOutcome = false to "Biometría denegada"
        }
        val repo = com.jeudry.rosafiesta.data.DefaultRosaFiestaRepository(seedDemoData = true)
        assertTrue(repo.login("yuli@rosafiesta.do", "rosa2026"))
        val viewModel = RosaFiestaViewModel(
            repository = repo,
            platformActions = platformActions,
            secureStorage = InMemorySecureStorage(),
        )

        viewModel.toggleBiometricLock()
        assertTrue(viewModel.biometricLockEnabled.value)
        viewModel.lockAppIfProtected()
        assertTrue(viewModel.isAppLocked.value)

        viewModel.unlockAppWithBiometrics()
        assertTrue(viewModel.isAppLocked.value)
        assertFalse(viewModel.appLockError.value.isNullOrBlank())

        platformActions.simulatedDeviceAuthOutcome = true to null
        viewModel.unlockAppWithBiometrics()
        assertFalse(viewModel.isAppLocked.value)
        assertNull(viewModel.appLockError.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `app muestra pantalla de bloqueo cuando biometric lock esta activo`() = runComposeUiTest {
        val platformActions = NoOpPlatformActions().apply {
            simulatedDeviceAuthOutcome = false to "Biometría requerida"
        }
        val repo = com.jeudry.rosafiesta.data.DefaultRosaFiestaRepository(seedDemoData = true)
        assertTrue(repo.login("yuli@rosafiesta.do", "rosa2026"))
        val viewModel = RosaFiestaViewModel(
            repository = repo,
            platformActions = platformActions,
            secureStorage = InMemorySecureStorage(),
        )
        viewModel.toggleBiometricLock()
        viewModel.lockAppIfProtected()
        setContent {
            RosaFiestaApp(viewModel = viewModel)
        }
        waitForIdle()
        onNodeWithTag("app-lock-overlay").assertExists()
        onNodeWithText("Desbloquear con biometría").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `a34 muestra estado vacio honesto cuando el viewmodel no tiene resenas`() = runComposeUiTest {
        val emptyRepo = com.jeudry.rosafiesta.data.DefaultRosaFiestaRepository(seedDemoData = false)
        val viewModel = RosaFiestaViewModel(
            repository = emptyRepo,
            platformActions = NoOpPlatformActions(),
            secureStorage = InMemorySecureStorage(),
        )
        setContent {
            RenderAdminReplyReviewA34(
                viewModel = viewModel,
                onSelectScreenCode = {},
            )
        }
        waitForIdle()
        onNodeWithText("Sin reseñas por responder").assertExists()
        onNodeWithText("Publicar respuesta").assertDoesNotExist()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `a21 valida rnc b01 tras debounce de 2000ms y limpia al completar 9 digitos`() = runComposeUiTest {
        val repo = com.jeudry.rosafiesta.data.DefaultRosaFiestaRepository(seedDemoData = true)
        val viewModel = RosaFiestaViewModel(
            repository = repo,
            platformActions = NoOpPlatformActions(),
            secureStorage = InMemorySecureStorage(),
        )
        setContent {
            RenderAdminRecordPaymentA21(
                onSelectScreenCode = {},
                viewModel = viewModel,
            )
        }
        waitForIdle()
        onNodeWithText("Crédito Fiscal (B01)").performClick()
        waitForIdle()

        val rncField = onNodeWithTag("a21-rnc-input")
        rncField.assertExists()
        rncField.performTextReplacement("123")
        mainClock.advanceTimeBy(1500)
        waitForIdle()
        onNodeWithText("Ingresa un RNC (9 dígitos) o Cédula (11 dígitos) válido para NCF B01.").assertDoesNotExist()

        mainClock.advanceTimeBy(700)
        waitForIdle()
        onNodeWithText("Ingresa un RNC (9 dígitos) o Cédula (11 dígitos) válido para NCF B01.").assertExists()

        rncField.performTextReplacement("131849202")
        waitForIdle()
        onNodeWithText("Ingresa un RNC (9 dígitos) o Cédula (11 dígitos) válido para NCF B01.").assertDoesNotExist()
    }

    @Test
    fun `oauth pkce por navegador inicia sesion y sincroniza token push del dispositivo`() = runTest {
        val delegateRepo = com.jeudry.rosafiesta.data.DefaultRosaFiestaRepository(seedDemoData = true)
        val fakeOAuthAccount = RegisteredUserAccount(
            id = "usr-oauth-1",
            username = "yuli_oauth",
            email = "yuli@rosafiesta.do",
            fullName = "Yulissa Marte",
            passwordHash = "",
            salt = "",
            birthDate = "1996-05-12",
            phone = "+18095550142",
            role = "CLIENT",
        )
        val pushSyncDone = CompletableDeferred<Unit>()
        var capturedProvider = ""
        var capturedCodeVerifier = ""
        var capturedPushPlatform = ""
        var capturedPushToken = ""
        val oauthRepo = object : RosaFiestaRepository by delegateRepo {
            override suspend fun startOAuthAuthorization(
                provider: String,
                redirectUri: String,
                codeChallenge: String,
                state: String,
            ): Result<String> {
                capturedProvider = provider
                return Result.success("https://accounts.google.com/o/oauth2/v2/auth?code_challenge=$codeChallenge&state=$state")
            }

            override suspend fun exchangeOAuthCode(
                provider: String,
                code: String,
                codeVerifier: String,
                redirectUri: String,
            ): AuthOutcome {
                capturedCodeVerifier = codeVerifier
                return AuthOutcome.authenticated(
                    accessToken = "oauth-jwt-session-token",
                    account = fakeOAuthAccount,
                )
            }

            override suspend fun registerDevicePushToken(
                platform: String,
                token: String,
                appVariant: String,
            ): Result<Boolean> {
                capturedPushPlatform = platform
                capturedPushToken = token
                pushSyncDone.complete(Unit)
                return Result.success(true)
            }
        }
        val platformActions = NoOpPlatformActions().apply {
            queuedOAuthOutcome = OAuthBrowserOutcome(
                code = "google-auth-code-99",
                state = null,
                redirectUri = "http://127.0.0.1:43123/callback",
            )
            simulatedPushRegistration = "android" to "fcm-real-token-99"
        }
        val testSecureStorage = InMemorySecureStorage()
        val viewModel = RosaFiestaViewModel(
            repository = oauthRepo,
            platformActions = platformActions,
            secureStorage = testSecureStorage,
        )

        val oauthCompleted = CompletableDeferred<Boolean>()
        viewModel.signInWithBrowserOAuth("Google") { ok ->
            oauthCompleted.complete(ok)
        }

        val loginSucceeded = withContext(Dispatchers.Default) {
            withTimeout(5_000) {
                val finished = oauthCompleted.await()
                pushSyncDone.await()
                finished
            }
        }
        assertTrue(loginSucceeded)
        assertEquals("google", capturedProvider)
        assertTrue(capturedCodeVerifier.isNotBlank())
        assertTrue(platformActions.lastOAuthAuthorizeUrlRequested?.startsWith("https://accounts.google.com") == true)
        assertEquals("android", capturedPushPlatform)
        assertEquals("fcm-real-token-99", capturedPushToken)
        assertEquals("oauth-jwt-session-token", testSecureStorage.get(SecureStorage.KEY_ACCESS_TOKEN))
        assertEquals("Sesión iniciada con Google.", viewModel.formFeedback.value)
    }

    @Test
    fun `el deep link de retorno de Stripe confirma o cancela el checkout pendiente`() = runTest {
        val confirmationDone = CompletableDeferred<Unit>()
        var confirmedSessionId = ""
        val delegateRepo = com.jeudry.rosafiesta.data.DefaultRosaFiestaRepository(seedDemoData = false)
        val paymentRepo = object : RosaFiestaRepository by delegateRepo {
            override suspend fun confirmCardCheckoutSession(sessionId: String): Result<com.jeudry.rosafiesta.domain.PaymentRecord> {
                confirmedSessionId = sessionId
                confirmationDone.complete(Unit)
                return Result.success(
                    com.jeudry.rosafiesta.domain.PaymentRecord(
                        id = "pay-stripe-1",
                        receiptNumber = "REC-2026-9901",
                        eventId = "evt-1",
                        eventTitle = "Boda",
                        clientName = "Camila",
                        amountDop = 12000,
                        methodLabel = "Tarjeta Stripe",
                        kindLabel = "Abono",
                        dateLabel = "Hoy",
                        referenceCode = "cs_test_777",
                        verified = true,
                        ncfType = "B02",
                    ),
                )
            }
        }
        val viewModel = RosaFiestaViewModel(
            repository = paymentRepo,
            platformActions = NoOpPlatformActions(),
            secureStorage = InMemorySecureStorage(),
        )

        val handledSuccess = viewModel.handleExternalPaymentDeepLink("rosafiesta://payment/success?session_id=cs_test_777&event_id=evt-1")
        assertTrue(handledSuccess)
        withContext(Dispatchers.Default) {
            withTimeout(5_000) {
                confirmationDone.await()
            }
        }
        assertEquals("cs_test_777", confirmedSessionId)
        assertTrue(viewModel.formFeedback.value?.contains("REC-2026-9901") == true)

        val handledCancel = viewModel.handleExternalPaymentDeepLink("rosafiesta://payment/cancel?cancelled=1")
        assertTrue(handledCancel)
        assertEquals("Pago con tarjeta cancelado. No se realizó ningún cargo.", viewModel.formFeedback.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `la pantalla A27 muestra estado vacio honesto cuando no existen clientes registrados`() = runComposeUiTest {
        val emptyRepo = com.jeudry.rosafiesta.data.DefaultRosaFiestaRepository(seedDemoData = false)
        val viewModel = RosaFiestaViewModel(
            repository = emptyRepo,
            platformActions = NoOpPlatformActions(),
            secureStorage = InMemorySecureStorage(),
        )
        setContent {
            com.jeudry.rosafiesta.designsystem.RfTheme(variant = RfThemeVariant.ADMIN) {
                RenderAdminClientProfileA27(
                    viewModel = viewModel,
                    onSelectScreenCode = {},
                )
            }
        }
        onNodeWithText("Aún no tienes clientes").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `calendario s63 con viewModel real no inventa reservas simuladas`() = runComposeUiTest {
        val repo = com.jeudry.rosafiesta.data.DefaultRosaFiestaRepository(seedDemoData = false)
        repo.createArticle(
            code = "RF-900",
            name = "Mesa Imperial Real",
            category = "Mobiliario",
            pricePerDayDop = 450,
            totalStock = 50,
        )
        val viewModel = RosaFiestaViewModel(
            repository = repo,
            platformActions = NoOpPlatformActions(),
            secureStorage = InMemorySecureStorage(),
        )
        setContent {
            com.jeudry.rosafiesta.ui.RenderScreenSpec(
                spec = ScreenCatalog.findByCode("s63"),
                viewModel = viewModel,
                isFrameHarness = false,
                includeBottomBar = false,
            )
        }
        onNodeWithText("9").performClick()
        waitForIdle()
        onNodeWithText("Disponibles: 50 de 50 uds · Reservadas: 0 uds").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `asistente Rosa s26 con viewModel real no inyecta extras de CanonicalSampleData`() = runComposeUiTest {
        val repo = com.jeudry.rosafiesta.data.DefaultRosaFiestaRepository(seedDemoData = false)
        repo.createArticle(
            code = "RF-901",
            name = "Arco Orgánico Real",
            category = "Mobiliario",
            pricePerDayDop = 1200,
            totalStock = 8,
        )
        val viewModel = RosaFiestaViewModel(
            repository = repo,
            platformActions = NoOpPlatformActions(),
            secureStorage = InMemorySecureStorage(),
        )
        setContent {
            com.jeudry.rosafiesta.ui.RenderScreenSpec(
                spec = ScreenCatalog.findByCode("s26"),
                viewModel = viewModel,
                isFrameHarness = false,
                includeBottomBar = false,
            )
        }
        onNodeWithText("Sillón lounge capitoné").assertDoesNotExist()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `pantalla A40 gestiona crud completo de categorias sin seed por defecto`() = runComposeUiTest {
        val repo = com.jeudry.rosafiesta.data.DefaultRosaFiestaRepository(seedDemoData = false)
        assertEquals(0, repo.catalogCategories.value.size)

        val viewModel = RosaFiestaViewModel(
            repository = repo,
            platformActions = NoOpPlatformActions(),
            secureStorage = InMemorySecureStorage(),
        )
        setContent {
            com.jeudry.rosafiesta.ui.RenderScreenSpec(
                spec = ScreenCatalog.findByCode("A40"),
                viewModel = viewModel,
                isFrameHarness = false,
                includeBottomBar = false,
            )
        }
        onNodeWithText("No hay categorías creadas").assertExists()

        kotlinx.coroutines.runBlocking {
            val firstCreated = repo.createCategory(name = "Tarimas y pistas", illoKey = "iluminacion")
            assertTrue(firstCreated.isSuccess)
            val createdId = firstCreated.getOrNull()?.id.orEmpty()
            val secondCreated = repo.createCategory(name = "Sonido pro", illoKey = "general")
            assertTrue(secondCreated.isSuccess)
            val updated = repo.updateCategory(categoryId = createdId, name = "Tarimas LED", illoKey = "iluminacion")
            assertTrue(updated.isSuccess)
            val deleted = repo.deleteCategory(secondCreated.getOrNull()?.id.orEmpty())
            assertTrue(deleted.isSuccess)
        }
        waitForIdle()
        onNodeWithText("No hay categorías creadas").assertDoesNotExist()
        onNodeWithText("Tarimas LED").assertExists()
        onNodeWithText("Sonido pro").assertDoesNotExist()

        onNodeWithTag("a40-new-category-btn").performClick()
        waitForIdle()
        onNodeWithText("Subir ícono SVG").assertExists()
        onNodeWithText("Elegir .svg").assertExists()

        val customSvgPayload = """
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">
                <path d="M4 12h16" stroke="#4B47D6" stroke-width="2" fill="none" />
                <circle cx="12" cy="12" r="4" fill="#C2338F" />
            </svg>
        """.trimIndent().encodeToByteArray()
        val decodedPure = com.jeudry.rosafiesta.designsystem.decodeSvgToImageBitmapOrNull(customSvgPayload, targetSizePx = 64)
        assertTrue(decodedPure != null && decodedPure.width == 64 && decodedPure.height == 64)
        val decodedSkia = com.jeudry.rosafiesta.designsystem.decodeEncodedImageBitmapOrNull(customSvgPayload)
        assertTrue(decodedSkia != null && decodedSkia.width > 0 && decodedSkia.height > 0)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `pantalla A09 muestra boton eliminar articulo y elimina item con confirmacion`() = runComposeUiTest {
        val repo = com.jeudry.rosafiesta.data.DefaultRosaFiestaRepository(seedDemoData = false)
        kotlinx.coroutines.runBlocking {
            repo.createCategory(name = "Mobiliario", illoKey = "mobiliario")
        }
        repo.createArticle(
            code = "RF-001",
            name = "Silla plastica",
            category = "Mobiliario",
            pricePerDayDop = 15,
            totalStock = 137,
        )
        repo.createArticle(
            code = "RF-002",
            name = "Mesa plegable",
            category = "Mobiliario",
            pricePerDayDop = 20,
            totalStock = 10,
        )
        val viewModel = RosaFiestaViewModel(
            repository = repo,
            platformActions = NoOpPlatformActions(),
            secureStorage = InMemorySecureStorage(),
        )
        setContent {
            com.jeudry.rosafiesta.ui.RenderScreenSpec(
                spec = ScreenCatalog.findByCode("A09"),
                viewModel = viewModel,
                isFrameHarness = false,
                includeBottomBar = false,
            )
        }

        onNodeWithText("Silla plastica").assertExists()
        onNodeWithTag("delete-article-art-rf-001").performClick()
        waitForIdle()
        onNodeWithText("Eliminar artículo").assertExists()

        onNodeWithTag("confirm-delete-article").performClick()
        waitForIdle()

        onNodeWithText("Silla plastica").assertDoesNotExist()
        onNodeWithText("Mesa plegable").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `error de validacion en s07 aparece inmediatamente al hacer click fuera sin esperar 2 segundos`() = runComposeUiTest {
        mainClock.autoAdvance = false
        val repo = com.jeudry.rosafiesta.data.DefaultRosaFiestaRepository(seedDemoData = false)
        val viewModel = RosaFiestaViewModel(
            repository = repo,
            platformActions = NoOpPlatformActions(),
            secureStorage = InMemorySecureStorage(),
        )
        setContent {
            com.jeudry.rosafiesta.ui.RenderScreenSpec(
                spec = ScreenCatalog.findByCode("s07"),
                viewModel = viewModel,
                isFrameHarness = true,
                includeBottomBar = false,
            )
        }

        mainClock.advanceTimeBy(100)
        val expectedError = checkNotNull(
            com.jeudry.rosafiesta.domain.validation.InputValidator.validateLoginIdentifier("a"),
        )
        onAllNodes(hasSetTextAction())[0].performClick()
        mainClock.advanceTimeBy(50)
        onAllNodes(hasSetTextAction())[0].performTextReplacement("a")
        mainClock.advanceTimeBy(200)

        onNodeWithText(expectedError).assertDoesNotExist()

        onNodeWithText("Correo o usuario").performClick()
        mainClock.advanceTimeBy(100)

        onNodeWithText(expectedError).assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `pantalla s14 oculta tendencias y portada hardcodeadas cuando backend inicia vacio y renderiza datos creados por Admin`() = runComposeUiTest {
        val repo = com.jeudry.rosafiesta.data.DefaultRosaFiestaRepository(seedDemoData = false)
        val viewModel = RosaFiestaViewModel(
            repository = repo,
            platformActions = NoOpPlatformActions(),
            secureStorage = InMemorySecureStorage(),
        )
        setContent {
            com.jeudry.rosafiesta.ui.RenderScreenSpec(
                spec = ScreenCatalog.findByCode("s14"),
                viewModel = viewModel,
                isFrameHarness = false,
                includeBottomBar = false,
            )
        }

        waitForIdle()
        onNodeWithText("Graduaciones").assertDoesNotExist()
        onNodeWithText("NUEVA TEMPORADA 2026").assertDoesNotExist()

        kotlinx.coroutines.runBlocking {
            repo.saveSeasonBanner(
                com.jeudry.rosafiesta.domain.SeasonBanner(
                    badgeLabel = "TEMPORADA NAVIDAD",
                    title = "Arma tu evento de fin de año",
                    ctaLabel = "Ver paquetes",
                    targetScreenCode = "s21",
                    active = true,
                ),
            )
            repo.createTrend(
                com.jeudry.rosafiesta.domain.CatalogTrend(
                    id = "trend-navidad",
                    title = "Cenas Corporativas",
                    eventsLabel = "18 reservas",
                    active = true,
                ),
            )
        }
        waitForIdle()

        onNodeWithText("TEMPORADA NAVIDAD").assertExists()
        onNodeWithText("Arma tu evento de fin de año").assertExists()
        onNodeWithText("Tendencias").assertExists()
        onNodeWithText("Cenas Corporativas").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `pantalla A41 crea y elimina tendencias en flujo completo de admin`() = runComposeUiTest {
        val repo = com.jeudry.rosafiesta.data.DefaultRosaFiestaRepository(seedDemoData = false)
        val viewModel = RosaFiestaViewModel(
            repository = repo,
            platformActions = NoOpPlatformActions(),
            secureStorage = InMemorySecureStorage(),
        )
        setContent {
            com.jeudry.rosafiesta.ui.RenderScreenSpec(
                spec = ScreenCatalog.findByCode("A41"),
                viewModel = viewModel,
                isFrameHarness = false,
                includeBottomBar = false,
            )
        }

        onNodeWithText("Aún no hay tendencias creadas").assertExists()
        onNodeWithTag("add-trend-button").performClick()
        waitForIdle()

        onNodeWithText("Nueva tendencia").assertExists()
        val sheetInputs = onAllNodes(hasSetTextAction())
        sheetInputs[sheetInputs.fetchSemanticsNodes().size - 2].performTextReplacement("Noche de Gala")
        waitForIdle()

        onNodeWithText("Crear tendencia").performClick()
        waitForIdle()

        onNodeWithText("Noche de Gala").assertExists()
        onNodeWithTag("delete-trend-trend-noche-de-gala").performClick()
        waitForIdle()
        onNodeWithText("Eliminar tendencia").assertExists()
        onNodeWithTag("confirm-delete-trend").performClick()
        waitForIdle()

        onNodeWithText("Noche de Gala").assertDoesNotExist()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `modal eliminar articulo en A09 queda sobre barra inferior y muestra categorias reales`() = runComposeUiTest {
        val repo = com.jeudry.rosafiesta.data.DefaultRosaFiestaRepository(seedDemoData = false)
        repo.createArticleWithMedia(
            code = "RF-001",
            name = "Silla plastica",
            category = "Mobiliario",
            pricePerDayDop = 15,
            totalStock = 137,
            photoUrl = "/v1/media/med-1",
        )
        val created = repo.articles.value.first()
        val viewModel = RosaFiestaViewModel(
            repository = repo,
            platformActions = NoOpPlatformActions(),
            secureStorage = InMemorySecureStorage(),
        )
        setContent {
            Box(modifier = Modifier.size(width = 420.dp, height = 870.dp)) {
                RosaFiestaApp(
                    initialAppView = AppView.ADMIN,
                    initialScreenCode = "A09",
                    showDevHeader = false,
                    viewModel = viewModel,
                )
            }
        }

        onNodeWithText("1 artículo · 1 categoría").assertExists()
        onNodeWithTag("delete-article-${created.id}").performClick()
        waitForIdle()

        val confirmNode = onNodeWithTag("confirm-delete-article").fetchSemanticsNode()
        val navEventsNode = onAllNodesWithText("Eventos")
            .fetchSemanticsNodes()
            .maxBy { node -> node.boundsInRoot.top }
        assertTrue(
            confirmNode.boundsInRoot.bottom <= navEventsNode.boundsInRoot.top,
            "Delete button bottom (${confirmNode.boundsInRoot.bottom}) must sit above bottom bar top (${navEventsNode.boundsInRoot.top})",
        )

        onNodeWithTag("confirm-delete-article").performClick()
        waitForIdle()
        onNodeWithText("Silla plastica").assertDoesNotExist()
    }

    @Test
    fun `ensureMediaLoaded conserva todas las imagenes concurrentes sin sobrescribir cache`() = runTest {
        val baseRepo = com.jeudry.rosafiesta.data.DefaultRosaFiestaRepository(seedDemoData = false)
        val firstGate = CompletableDeferred<Unit>()
        val secondGate = CompletableDeferred<Unit>()
        val delegatingRepo = object : RosaFiestaRepository by baseRepo {
            override suspend fun fetchMediaBytes(urlOrId: String): Result<ByteArray> = when (urlOrId) {
                "/v1/media/chair-a" -> {
                    firstGate.await()
                    Result.success(byteArrayOf(1, 2, 3))
                }

                "/v1/media/chair-b" -> {
                    secondGate.await()
                    Result.success(byteArrayOf(4, 5, 6))
                }

                else -> Result.failure(IllegalArgumentException("Unknown media $urlOrId"))
            }
        }
        val viewModel = RosaFiestaViewModel(
            repository = delegatingRepo,
            platformActions = NoOpPlatformActions(),
            secureStorage = InMemorySecureStorage(),
        )

        viewModel.ensureMediaLoaded("/v1/media/chair-a")
        viewModel.ensureMediaLoaded("/v1/media/chair-b")
        firstGate.complete(Unit)
        secondGate.complete(Unit)

        withContext(Dispatchers.Default) {
            withTimeout(2_000) {
                for (attempt in 0 until 40) {
                    if (viewModel.mediaBytesCache.value.size >= 2) {
                        break
                    }
                    kotlinx.coroutines.delay(15)
                }
            }
        }

        val cached = viewModel.mediaBytesCache.value
        assertEquals(2, cached.size)
        assertTrue(cached["/v1/media/chair-a"]?.contentEquals(byteArrayOf(1, 2, 3)) == true)
        assertTrue(cached["/v1/media/chair-b"]?.contentEquals(byteArrayOf(4, 5, 6)) == true)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `pantalla A12 agrupa modulos por categoria y permite filtrar por chip y busqueda`() = runComposeUiTest {
        val repo = com.jeudry.rosafiesta.data.DefaultRosaFiestaRepository(seedDemoData = true)
        val viewModel = RosaFiestaViewModel(
            repository = repo,
            platformActions = NoOpPlatformActions(),
            secureStorage = InMemorySecureStorage(),
        )
        var navigatedTarget = ""
        setContent {
            com.jeudry.rosafiesta.ui.RenderAdminMoreA12(
                viewModel = viewModel,
                onSelectScreenCode = { target -> navigatedTarget = target },
            )
        }

        onNodeWithText("COMERCIAL Y CLIENTES").assertExists()
        onNodeWithText("CATÁLOGO Y PORTADA").assertExists()
        onNodeWithText("FINANZAS Y ANALÍTICA").assertExists()
        onNodeWithText("OPERACIÓN Y SISTEMA").assertExists()

        onNodeWithTag("admin-more-filter-catalog").performClick()
        waitForIdle()
        onNodeWithText("CATÁLOGO Y PORTADA").assertExists()
        onNodeWithText("COMERCIAL Y CLIENTES").assertDoesNotExist()
        onNodeWithText("Tendencias y portada").assertExists()

        onNodeWithTag("admin-more-filter-all").performClick()
        waitForIdle()
        onNodeWithTag("admin-more-search-input").performTextReplacement("whatsapp")
        waitForIdle()
        onNodeWithTag("admin-more-item-whatsapp_bot").assertExists()
        onNodeWithTag("admin-more-item-payments").assertDoesNotExist()

        onNodeWithTag("admin-more-item-whatsapp_bot").performClick()
        assertEquals("s72", navigatedTarget)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `detalle s19 y A09 cargan foto real subida en galeria e inventario`() = runComposeUiTest {
        val baseRepo = com.jeudry.rosafiesta.data.DefaultRosaFiestaRepository(seedDemoData = false)
        baseRepo.createArticleWithMedia(
            code = "RF-002",
            name = "Silla Plastica",
            category = "Mobiliario",
            pricePerDayDop = 15,
            totalStock = 138,
            photoUrl = "/v1/media/med-silla-blanca",
        )
        val onePixelPng = Base64.decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+X2Z0AAAAASUVORK5CYII=",
        )
        val requestedUrls = mutableListOf<String>()
        val delegatingRepo = object : RosaFiestaRepository by baseRepo {
            override suspend fun fetchMediaBytes(urlOrId: String): Result<ByteArray> {
                requestedUrls.add(urlOrId)
                return Result.success(onePixelPng)
            }
        }
        val viewModel = RosaFiestaViewModel(
            repository = delegatingRepo,
            platformActions = NoOpPlatformActions(),
            secureStorage = InMemorySecureStorage(),
        )
        val created = baseRepo.articles.value.first()
        viewModel.selectArticle(created.id)

        setContent {
            com.jeudry.rosafiesta.ui.RenderScreenSpec(
                spec = ScreenCatalog.findByCode("s19"),
                viewModel = viewModel,
                isFrameHarness = false,
                includeBottomBar = false,
            )
        }

        waitForIdle()
        onNodeWithContentDescription("Vista 1").assertExists()
        assertTrue(requestedUrls.contains("/v1/media/med-silla-blanca"))
        assertTrue(viewModel.mediaBytesCache.value.containsKey("/v1/media/med-silla-blanca"))
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `flujo cotizacion pide detalles en s25 muestra en revision en s48 y habilita firma tras envio admin`() = runComposeUiTest {
        assertEquals("s31", ScreenCatalog.defaultNextCode("s25"))

        val baseRepo = com.jeudry.rosafiesta.data.DefaultRosaFiestaRepository(seedDemoData = false)
        val viewModel = RosaFiestaViewModel(
            repository = baseRepo,
            platformActions = NoOpPlatformActions(),
            secureStorage = InMemorySecureStorage(),
        )

        baseRepo.createArticleWithMedia(
            code = "RF-002",
            name = "Silla Plastica",
            category = "Mobiliario",
            pricePerDayDop = 15,
            totalStock = 138,
        )
        val article = baseRepo.articles.value.first()
        viewModel.setDraftQuantity(article.id, 5)

        val pendingQuote = baseRepo.createQuote(
            eventTitle = "Boda Ana & Carlos",
            clientName = "Ana Pérez",
            clientEmail = "ana@rosafiesta.do",
            venue = "Salón Mirador",
            dateLabel = "25/12/2026",
            guests = 80,
            lines = listOf(
                com.jeudry.rosafiesta.domain.EventLineItem(
                    articleCode = article.code,
                    name = article.name,
                    quantity = 5,
                    unitPriceDop = article.pricePerDayDop,
                    photo = article.photo,
                ),
            ),
            deliveryFeeDop = 350,
            discountDop = 0,
            eventId = "",
        )
        viewModel.selectQuote(pendingQuote.id)

        val currentScreen = androidx.compose.runtime.mutableStateOf("s25")
        setContent {
            com.jeudry.rosafiesta.ui.RenderScreenSpec(
                spec = ScreenCatalog.findByCode(currentScreen.value),
                viewModel = viewModel,
                isFrameHarness = false,
                includeBottomBar = false,
                onSelectScreenCode = { nextCode -> currentScreen.value = nextCode },
            )
        }

        onNodeWithText("Configurar datos del evento").assertExists()
        onNodeWithText("Ver cotización oficial").assertDoesNotExist()

        currentScreen.value = "s48"
        waitForIdle()
        onNodeWithText("En revisión").assertExists()
        onNodeWithText("En revisión por Rosa Fiesta").assertExists()
        onNodeWithText("Firma digital del contrato").assertDoesNotExist()
        onNodeWithText("Aprobar cotización").assertDoesNotExist()
        onNodeWithText("Nota de María · Rosa Fiesta").assertDoesNotExist()

        viewModel.createFormalQuoteFromAdmin(
            eventTitle = "Boda Ana & Carlos",
            clientName = "Ana Pérez",
            clientEmail = "ana@rosafiesta.do",
            venue = "Jardín Los Rosales",
            dateLabel = "25/12/2026",
            guests = 80,
            lines = pendingQuote.lines,
            deliveryFeeDop = 500,
            discountDop = 50,
            notes = "Incluye montaje 2 horas antes y funda blanca",
            existingQuoteId = pendingQuote.id,
        )
        waitForIdle()

        onNodeWithText("Lista para aceptar").assertExists()
        onNodeWithText("Incluye montaje 2 horas antes y funda blanca").assertExists()
        onNodeWithText("Firma digital del contrato").assertExists()
        onNodeWithText("Aceptar y firmar cotización").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `s31 prioriza tipo de evento autocompleta titulo omite mas ideas y dicta notas en linea`() = runComposeUiTest {
        val koin = org.koin.dsl.koinApplication {
            modules(com.jeudry.rosafiesta.di.appModule)
        }.koin
        val viewModel = koin.get<RosaFiestaViewModel>()
        val navigatedScreens = mutableListOf<String>()

        setContent {
            com.jeudry.rosafiesta.ui.RenderScreenSpec(
                spec = ScreenCatalog.findByCode("s31"),
                isFrameHarness = false,
                includeBottomBar = false,
                viewModel = viewModel,
                onSelectScreenCode = { nextCode -> navigatedScreens.add(nextCode) },
            )
        }

        onNodeWithText("Tipo de evento").assertExists()
        onNodeWithText("Boda").assertExists()
        onNodeWithText("Cumpleaños").assertExists()
        onNodeWithText("Más ideas").assertDoesNotExist()
        onNodeWithText("Siguiente").assertExists()

        onNodeWithText("Cumpleaños").performClick()
        waitForIdle()
        assertEquals("Cumpleaños", viewModel.eventTypeInput.value)
        assertEquals("Cumpleaños de ", viewModel.clientEventTitleInput.value)

        onNodeWithText("Elegir").performClick()
        waitForIdle()
        onNodeWithText("Confirmar", substring = true).performClick()
        waitForIdle()

        assertTrue(viewModel.eventDateInput.value.contains("/"))
        onNodeWithText("La fecha del evento debe usar el formato DD/MM/AAAA.").assertDoesNotExist()

        viewModel.toggleInlineVoiceNotes("Mesa dulce")
        waitForIdle()
        assertEquals(com.jeudry.rosafiesta.ui.VoiceInputOrigin.NOTES_INLINE, viewModel.voiceInputOrigin.value)
        assertTrue(navigatedScreens.none { screenCode -> screenCode == "s35" })
        viewModel.stopVoiceInput()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `resumen s32 muestra apartados opcionales claros s40 evita tabs duplicados y s45 inicia limpio`() = runComposeUiTest {
        val sharedStorage = com.jeudry.rosafiesta.domain.security.InMemorySecureStorage()
        val firstRepo = com.jeudry.rosafiesta.data.DefaultRosaFiestaRepository()
        val firstVm = RosaFiestaViewModel(
            repository = firstRepo,
            secureStorage = sharedStorage,
            soundPlayer = com.jeudry.rosafiesta.domain.sound.NoOpSoundPlayer(),
        )
        firstVm.setEventTypeInput("Boda")
        firstVm.setClientEventTitleInput("Boda Persistida")
        firstVm.setEventDateInput("28/12/2026")
        firstVm.setEventVenueInput("Villa Colonial")
        firstVm.setDraftQuantity("art-silla-tiffany", 25)
        kotlinx.coroutines.runBlocking {
            kotlinx.coroutines.delay(50)
        }

        val secondRepo = com.jeudry.rosafiesta.data.DefaultRosaFiestaRepository()
        val restoredVm = RosaFiestaViewModel(
            repository = secondRepo,
            secureStorage = sharedStorage,
            soundPlayer = com.jeudry.rosafiesta.domain.sound.NoOpSoundPlayer(),
        )
        kotlinx.coroutines.runBlocking {
            kotlinx.coroutines.delay(100)
        }
        assertEquals(25, restoredVm.draftQuantities.value["art-silla-tiffany"])
        assertEquals("Boda Persistida", restoredVm.clientEventTitleInput.value)

        val currentScreen = androidx.compose.runtime.mutableStateOf("s32")
        setContent {
            com.jeudry.rosafiesta.ui.RenderScreenSpec(
                spec = ScreenCatalog.findByCode(currentScreen.value),
                isFrameHarness = false,
                includeBottomBar = false,
                viewModel = restoredVm,
                onSelectScreenCode = { nextCode -> currentScreen.value = nextCode },
            )
        }

        onNodeWithText("Lista de invitados").assertExists()
        onNodeWithText("Boceto del espacio").assertExists()
        onNodeWithText("Opcional · Diseñar").assertExists()
        assertEquals(2, onAllNodesWithText("Opcional · Agregar").fetchSemanticsNodes().size)

        assertTrue(restoredVm.submitClientQuoteRequest())
        assertTrue(restoredVm.draftQuantities.value.isEmpty())
        assertEquals("", restoredVm.clientEventTitleInput.value)
        assertEquals("", restoredVm.eventTypeInput.value)
        assertEquals("", restoredVm.eventDateInput.value)
        assertEquals("", restoredVm.eventVenueInput.value)
        assertEquals(0, restoredVm.rosaBudgetDop.value)
        assertTrue(restoredVm.wizardScheduleItems.value.isEmpty())
        assertTrue(restoredVm.eventGuestsList.value.isEmpty())
        assertEquals("Boda Persistida", restoredVm.activeClientEvent().title)
        assertTrue(restoredVm.quotes.value.any { quoteItem -> quoteItem.eventTitle == "Boda Persistida" })

        kotlinx.coroutines.runBlocking {
            kotlinx.coroutines.delay(50)
        }
        val postSubmitVm = RosaFiestaViewModel(
            repository = com.jeudry.rosafiesta.data.DefaultRosaFiestaRepository(),
            secureStorage = sharedStorage,
            soundPlayer = com.jeudry.rosafiesta.domain.sound.NoOpSoundPlayer(),
        )
        kotlinx.coroutines.runBlocking {
            kotlinx.coroutines.delay(100)
        }
        assertTrue(postSubmitVm.draftQuantities.value.isEmpty())
        assertEquals("", postSubmitVm.clientEventTitleInput.value)

        currentScreen.value = "s40"
        waitForIdle()
        onNodeWithText("Modo ejecución").assertDoesNotExist()
        onNodeWithText("Chat en la app").assertExists()

        currentScreen.value = "s45"
        waitForIdle()
        onNodeWithText("Confirmar paleta floral blanco y rosa pastel").assertDoesNotExist()
        onNodeWithText("Aún no hay tareas para este evento. Usa «+ Agregar tarea» para crear tu lista.").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `modal de filtros s81 se superpone sin desmontar catalogo s18 y cierra sin parpadeo`() = runComposeUiTest {
        val repo = com.jeudry.rosafiesta.data.DefaultRosaFiestaRepository()
        val vm = RosaFiestaViewModel(
            repository = repo,
            secureStorage = com.jeudry.rosafiesta.domain.security.InMemorySecureStorage(),
            soundPlayer = com.jeudry.rosafiesta.domain.sound.NoOpSoundPlayer(),
        )
        setContent {
            RosaFiestaApp(
                initialScreenCode = "s18",
                viewModel = vm,
            )
        }

        onNodeWithTag("screen-s18").assertExists()
        onNodeWithTag("screen-s81").assertDoesNotExist()

        onNodeWithText("Aplicar", substring = true).assertDoesNotExist()
        onNodeWithText("Filtros").assertDoesNotExist()

        val filterNav = com.jeudry.rosafiesta.ui.Navigator(initialScreenCode = "s18")
        filterNav.navigateTo("s81")
        assertEquals("s81", filterNav.currentCode)
        assertEquals("s18", filterNav.lastModalOriginCode)
        filterNav.navigateTo("BACK:s18")
        assertEquals("s18", filterNav.currentCode)
        assertTrue(com.jeudry.rosafiesta.ui.isFloatingOverlayModalCode("s81"))
        assertFalse(com.jeudry.rosafiesta.ui.isFloatingOverlayModalCode("s18"))
    }

    @Test
    fun `microfono transcribe WAV asincrono tras stop y en auto-stop y muestra error si STT no configurado`() = runTest {
        val mutableListening = kotlinx.coroutines.flow.MutableStateFlow(false)
        val mutableTranscribing = kotlinx.coroutines.flow.MutableStateFlow(false)
        val mutableAmplitude = kotlinx.coroutines.flow.MutableStateFlow(0f)
        val mutableHasMic = kotlinx.coroutines.flow.MutableStateFlow(true)
        val mutableTranscript = kotlinx.coroutines.flow.MutableStateFlow("")
        val mutableCapturedWav = kotlinx.coroutines.flow.MutableStateFlow<ByteArray?>(null)
        val mutableError = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)

        val asyncVoiceMonitor = object : com.jeudry.rosafiesta.domain.sound.VoiceInputMonitor {
            override val isListening = mutableListening
            override val isTranscribing = mutableTranscribing
            override val amplitudeLevel = mutableAmplitude
            override val hasHardwareMicrophone = mutableHasMic
            override val recognizedTranscript = mutableTranscript
            override val capturedWavBytes = mutableCapturedWav
            override val errorMessage = mutableError

            override fun startListening(onTranscriptResult: (String) -> Unit) {
                mutableError.value = null
                mutableCapturedWav.value = null
                mutableListening.value = true
            }

            override fun stopListening() {
                mutableListening.value = false
            }
        }

        val responses = ArrayDeque(
            listOf(
                com.jeudry.rosafiesta.domain.VoiceTranscriptionOutcome(
                    transcript = "silla tiffany dorada",
                    locale = "es-DO",
                    voiceDetected = true,
                    durationMs = 1200,
                    provider = "gemini",
                ),
                com.jeudry.rosafiesta.domain.VoiceTranscriptionOutcome(
                    transcript = "flores blancas y velas",
                    locale = "es-DO",
                    voiceDetected = true,
                    durationMs = 1400,
                    provider = "gemini",
                ),
                com.jeudry.rosafiesta.domain.VoiceTranscriptionOutcome(
                    transcript = "",
                    locale = "es-DO",
                    voiceDetected = false,
                    durationMs = 900,
                    provider = "STT_PROVIDER_NOT_CONFIGURED:Proveedor de transcripción por voz no configurado.",
                ),
            ),
        )
        var transcriptionCalls = 0
        val delegateRepo = com.jeudry.rosafiesta.data.DefaultRosaFiestaRepository(seedDemoData = false)
        val voiceRepo = object : RosaFiestaRepository by delegateRepo {
            override suspend fun transcribeVoicePrompt(
                wavBase64: String,
                locale: String,
            ): com.jeudry.rosafiesta.domain.VoiceTranscriptionOutcome {
                transcriptionCalls += 1
                return responses.removeFirst()
            }
        }

        val viewModel = RosaFiestaViewModel(
            repository = voiceRepo,
            voiceInputMonitor = asyncVoiceMonitor,
            secureStorage = InMemorySecureStorage(),
        )

        viewModel.setSearchQuery("mesa anterior")
        viewModel.startVoiceInput(com.jeudry.rosafiesta.ui.VoiceInputOrigin.SEARCH, "mesa anterior")
        viewModel.stopVoiceInput()
        assertEquals(0, transcriptionCalls)

        mutableCapturedWav.value = ByteArray(128) { byteIndex -> byteIndex.toByte() }
        withContext(Dispatchers.Default) {
            withTimeout(5_000) {
                for (attempt in 0 until 100) {
                    if (viewModel.searchQuery.value == "silla tiffany dorada") break
                    kotlinx.coroutines.delay(25)
                }
            }
        }
        assertEquals(1, transcriptionCalls)
        assertEquals("silla tiffany dorada", viewModel.searchQuery.value)
        assertEquals(null, viewModel.voiceErrorMessage.value)

        viewModel.startVoiceInput(com.jeudry.rosafiesta.ui.VoiceInputOrigin.NOTES_INLINE, "Nota base")
        mutableListening.value = false
        mutableCapturedWav.value = ByteArray(140) { byteIndex -> (byteIndex + 1).toByte() }
        withContext(Dispatchers.Default) {
            withTimeout(5_000) {
                for (attempt in 0 until 100) {
                    if (viewModel.eventNotesInput.value.contains("flores blancas y velas")) break
                    kotlinx.coroutines.delay(25)
                }
            }
        }
        assertEquals(2, transcriptionCalls)
        assertEquals("Nota base\nflores blancas y velas", viewModel.eventNotesInput.value)

        viewModel.startVoiceInput(com.jeudry.rosafiesta.ui.VoiceInputOrigin.SEARCH, "")
        viewModel.stopVoiceInput()
        mutableCapturedWav.value = ByteArray(160) { byteIndex -> (byteIndex + 2).toByte() }
        withContext(Dispatchers.Default) {
            withTimeout(5_000) {
                for (attempt in 0 until 100) {
                    if (viewModel.voiceErrorMessage.value != null) break
                    kotlinx.coroutines.delay(25)
                }
            }
        }
        assertEquals(3, transcriptionCalls)
        assertEquals("Proveedor de transcripción por voz no configurado.", viewModel.voiceErrorMessage.value)
        viewModel.dismissVoiceError()
        withContext(Dispatchers.Default) {
            withTimeout(5_000) {
                for (attempt in 0 until 100) {
                    if (viewModel.voiceErrorMessage.value == null) break
                    kotlinx.coroutines.delay(25)
                }
            }
        }
        assertEquals(null, viewModel.voiceErrorMessage.value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `header raiz persistente se mantiene fijo en tabs de Personal y Admin cambiando solo su contenido`() = runComposeUiTest {
        val repo = com.jeudry.rosafiesta.data.DefaultRosaFiestaRepository()
        val vm = RosaFiestaViewModel(
            repository = repo,
            secureStorage = com.jeudry.rosafiesta.domain.security.InMemorySecureStorage(),
            soundPlayer = com.jeudry.rosafiesta.domain.sound.NoOpSoundPlayer(),
        )
        assertTrue(com.jeudry.rosafiesta.ui.hasPersistentRootHeader("s14"))
        assertTrue(com.jeudry.rosafiesta.ui.hasPersistentRootHeader("s18"))
        assertTrue(com.jeudry.rosafiesta.ui.hasPersistentRootHeader("s37"))
        assertTrue(com.jeudry.rosafiesta.ui.hasPersistentRootHeader("s20"))
        assertTrue(com.jeudry.rosafiesta.ui.hasPersistentRootHeader("s21"))
        assertTrue(com.jeudry.rosafiesta.ui.hasPersistentRootHeader("s24"))
        assertTrue(com.jeudry.rosafiesta.ui.hasPersistentRootHeader("s39"))
        assertTrue(com.jeudry.rosafiesta.ui.hasPersistentRootHeader("s53"))
        assertTrue(com.jeudry.rosafiesta.ui.hasPersistentRootHeader("s79"))
        assertTrue(com.jeudry.rosafiesta.ui.hasPersistentRootHeader("s82"))
        assertTrue(com.jeudry.rosafiesta.ui.hasPersistentRootHeader("s16"))
        assertFalse(com.jeudry.rosafiesta.ui.hasPersistentRootHeader("s19"))
        assertFalse(com.jeudry.rosafiesta.ui.hasPersistentRootHeader("s31"))
        assertTrue(com.jeudry.rosafiesta.ui.hasPersistentRootHeader("A01"))
        assertTrue(com.jeudry.rosafiesta.ui.hasPersistentRootHeader("A09"))
        assertTrue(com.jeudry.rosafiesta.ui.hasPersistentRootHeader("A11"))
        assertTrue(com.jeudry.rosafiesta.ui.hasPersistentRootHeader("A12"))
        assertTrue(com.jeudry.rosafiesta.ui.hasPersistentRootHeader("A02"))
        assertTrue(com.jeudry.rosafiesta.ui.hasPersistentRootHeader("A14"))
        assertTrue(com.jeudry.rosafiesta.ui.hasPersistentRootHeader("A22"))
        assertTrue(com.jeudry.rosafiesta.ui.hasPersistentRootHeader("A24"))
        assertTrue(com.jeudry.rosafiesta.ui.hasPersistentRootHeader("A26"))
        assertTrue(com.jeudry.rosafiesta.ui.hasPersistentRootHeader("A32"))
        assertFalse(com.jeudry.rosafiesta.ui.hasPersistentRootHeader("A03"))

        setContent {
            RosaFiestaApp(
                initialScreenCode = "s14",
                viewModel = vm,
            )
        }
        onNodeWithTag("persistent-root-header").assertExists()
        onNodeWithText("Hola de nuevo").assertExists()
    }

    @Test
    fun `boton Mi evento abre flujo de 3 pasos s38 primero y luego s25 directo a resumen s32 sin formulario gigante`() {
        val repo = com.jeudry.rosafiesta.data.DefaultRosaFiestaRepository()
        val vm = RosaFiestaViewModel(
            repository = repo,
            secureStorage = com.jeudry.rosafiesta.domain.security.InMemorySecureStorage(),
            soundPlayer = com.jeudry.rosafiesta.domain.sound.NoOpSoundPlayer(),
        )

        assertFalse(vm.hasConfiguredEventDraft())
        assertEquals("s38", vm.resolveMyEventEntryTarget())
        assertTrue(vm.isEventDraftSetupMode.value)
        assertEquals("s25", vm.eventDraftSetupReturnScreen.value)

        vm.saveEventDraftDetails(
            eventType = "Boda",
            title = "Boda Ana y Luis",
            date = "28/11/2026 - 29/11/2026",
            venue = "Jardín Botánico",
            guestsCount = 120,
            notes = "Montaje floral blanco a las 4:00 PM",
        )
        assertTrue(vm.hasConfiguredEventDraft())
        assertFalse(vm.isEventDraftSetupMode.value)
        assertEquals("Montaje floral blanco a las 4:00 PM", vm.eventNotesInput.value)
        assertEquals("s25", vm.resolveMyEventEntryTarget())

        assertEquals(
            "Boda de ",
            com.jeudry.rosafiesta.ui.resolveAutoFilledTitleForEventTypeName("", "Boda"),
        )
        assertEquals(
            "Cumpleaños de ",
            com.jeudry.rosafiesta.ui.resolveAutoFilledTitleForEventTypeName("Boda de ", "Cumpleaños"),
        )
        assertEquals(
            "Boda de Ana y Luis",
            com.jeudry.rosafiesta.ui.resolveAutoFilledTitleForEventTypeName("Boda de Ana y Luis", "Graduación"),
        )
        assertEquals(
            "",
            com.jeudry.rosafiesta.ui.resolveAutoFilledTitleForEventTypeName("Cumpleaños de ", "Otro"),
        )

        vm.startStandaloneEventCreation()
        assertFalse(vm.isEventDraftSetupMode.value)
        assertEquals("s40", vm.eventDraftSetupReturnScreen.value)

        assertFalse(vm.openMapsSearch(""))
        assertEquals(null, vm.formFeedback.value)
        assertTrue(vm.formError.value?.contains("dirección", ignoreCase = true) == true)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `s38 inicia sin tipo preseleccionado muestra Detalles con Maps notas y sin badges de ejemplo`() = runComposeUiTest {
        val repo = com.jeudry.rosafiesta.data.DefaultRosaFiestaRepository()
        val vm = RosaFiestaViewModel(
            repository = repo,
            secureStorage = com.jeudry.rosafiesta.domain.security.InMemorySecureStorage(),
            soundPlayer = com.jeudry.rosafiesta.domain.sound.NoOpSoundPlayer(),
        )
        vm.resolveMyEventEntryTarget()

        setContent {
            com.jeudry.rosafiesta.ui.RenderScreenSpec(
                spec = ScreenCatalog.findByCode("s38"),
                isFrameHarness = false,
                includeBottomBar = false,
                viewModel = vm,
                onSelectScreenCode = {},
            )
        }

        onNodeWithText("Siguiente: detalles").performClick()
        waitForIdle()
        onNodeWithText("Elige el tipo de evento para continuar.").assertExists()

        onNodeWithText("Cumpleaños").performClick()
        waitForIdle()
        onNodeWithText("Siguiente: detalles").performClick()
        waitForIdle()

        onNodeWithText("Fecha o rango de fechas").assertExists()
        onNodeWithText("Maps").assertExists()
        onNodeWithText("Detalles extras o notas (opcional)").assertExists()
        onNodeWithText("Jardín Botánico").assertDoesNotExist()
        onNodeWithText("Salón Piantini").assertDoesNotExist()
        onNodeWithText("Terraza Colonial").assertDoesNotExist()
    }

    @Test
    fun `aisla RBAC A28 A31 eventos activos por cliente hilos de chat y cierre de sesion completo`() = runTest {
        val repo = com.jeudry.rosafiesta.data.DefaultRosaFiestaRepository(seedDemoData = true)
        val storage = InMemorySecureStorage()
        storage.put(SecureStorage.KEY_DIRECT_CHAT, "stale-chat")
        storage.put(SecureStorage.KEY_EVENT_GUESTS, "stale-guests")
        storage.put("rf_pref_zone", "Piantini")

        val vm = RosaFiestaViewModel(
            repository = repo,
            secureStorage = storage,
            soundPlayer = NoOpSoundPlayer(),
            platformActions = NoOpPlatformActions(),
        )

        vm.switchRole("staff_yuli")
        assertTrue(vm.isScreenRestrictedForActiveStaff("A28"))
        assertTrue(vm.isScreenRestrictedForActiveStaff("A31"))

        val registered = vm.submitRegistration(
            firstName = "Lucia",
            lastName = "Mendez",
            username = "lucia_mendez",
            email = "lucia.mendez@correo.do",
            password = "Password2026",
            birthDate = "14/03/1995",
            acceptedTerms = true,
            phone = "809-555-0191",
        )
        assertTrue(registered)
        assertNull(vm.activeClientEventOrNull())
        assertEquals("", vm.activeClientEvent().id)

        repo.sendThreadMessage(
            threadId = "thr-other",
            senderName = "Otro Cliente",
            body = "Mensaje confidencial de otro cliente",
        )
        vm.selectChatThread("thr-lucia")
        withContext(Dispatchers.Default) {
            for (attempt in 0 until 40) {
                if (storage.get(SecureStorage.KEY_ACCESS_TOKEN) != null) break
                kotlinx.coroutines.delay(15)
            }
        }
        assertFalse(vm.directChatMessages.value.any { msg -> msg.text.contains("confidencial") })

        vm.logoutSession()
        withContext(Dispatchers.Default) {
            for (attempt in 0 until 60) {
                val tokenCleared = storage.get(SecureStorage.KEY_ACCESS_TOKEN) == null
                val chatCleared = storage.get(SecureStorage.KEY_DIRECT_CHAT) == null
                val zoneCleared = storage.get("rf_pref_zone") == null
                val isWiped = if (tokenCleared) {
                    if (chatCleared) zoneCleared else false
                } else {
                    false
                }
                if (isWiped) break
                kotlinx.coroutines.delay(15)
            }
        }
        assertNull(storage.get(SecureStorage.KEY_ACCESS_TOKEN))
        assertNull(storage.get(SecureStorage.KEY_DIRECT_CHAT))
        assertNull(storage.get(SecureStorage.KEY_EVENT_GUESTS))
        assertNull(storage.get("rf_pref_zone"))
        assertEquals("", vm.profilePhone.value)
        assertEquals("", vm.profileZone.value)
    }

    @Test
    fun `replays cold start deep link bridges once and clears replay cache`() = runTest {
        val vm = RosaFiestaViewModel(
            repository = com.jeudry.rosafiesta.data.DefaultRosaFiestaRepository(),
            secureStorage = InMemorySecureStorage(),
            soundPlayer = NoOpSoundPlayer(),
            platformActions = NoOpPlatformActions(),
        )
        withContext(Dispatchers.Default) {
            kotlinx.coroutines.delay(30)
        }
        assertTrue(
            com.jeudry.rosafiesta.domain.platform.ExternalPaymentCallbackBridge.dispatchPaymentDeepLink(
                "rosafiesta://payment/cancel?session_id=cs_cold_start",
            ),
        )
        withContext(Dispatchers.Default) {
            for (attempt in 0 until 60) {
                val hasFeedback = vm.formFeedback.value?.contains("cancelado", ignoreCase = true) == true
                if (hasFeedback) break
                kotlinx.coroutines.delay(15)
            }
        }
        assertTrue(vm.formFeedback.value?.contains("cancelado", ignoreCase = true) == true)
        assertEquals(0, com.jeudry.rosafiesta.domain.platform.ExternalPaymentCallbackBridge.paymentReturns.replayCache.size)
    }

    @Test
    fun `notifications filter preserves ntf ids unlimited price filter works entry choice persists and LRU bounds media cache`() = runTest {
        val repo = com.jeudry.rosafiesta.data.DefaultRosaFiestaRepository(seedDemoData = true)
        val storage = InMemorySecureStorage()
        val vm = RosaFiestaViewModel(
            repository = repo,
            secureStorage = storage,
            soundPlayer = NoOpSoundPlayer(),
            platformActions = NoOpPlatformActions(),
        )
        vm.submitRegistration(
            firstName = "Carmen",
            lastName = "Diaz",
            username = "carmen_d",
            email = "carmen@correo.do",
            password = "Password2026",
            birthDate = "10/04/1994",
            acceptedTerms = true,
            phone = "8095550122",
        )
        val notificationsSample = listOf(
            com.jeudry.rosafiesta.domain.NotificationRecord(
                id = "ntf-live-1",
                title = "Recordatorio vivo",
                body = "Evento proximo",
                category = "EVENT",
                createdAtLabel = "Ahora",
                isRead = false,
                recipientRole = "client",
            ),
            com.jeudry.rosafiesta.domain.NotificationRecord(
                id = "ntf-admin-only",
                title = "Alerta interna",
                body = "Solo admin",
                category = "SYSTEM",
                createdAtLabel = "Ahora",
                isRead = false,
                recipientRole = "admin",
            ),
        )
        val filtered = vm.filterNotificationsForPersonalClient(notificationsSample)
        assertEquals(listOf("ntf-live-1"), filtered.map { item -> item.id })

        repo.createArticle(
            code = "RF-LUX-99",
            name = "Pergola Cristal",
            category = "Estructuras",
            pricePerDayDop = 12500,
            totalStock = 2,
        )
        vm.setMaxPriceFilterDop(5000)
        assertTrue(vm.filteredCatalogArticles().any { art -> art.code == "RF-LUX-99" })
        vm.setMaxPriceFilterDop(4500)
        assertFalse(vm.filteredCatalogArticles().any { art -> art.code == "RF-LUX-99" })

        vm.saveRememberedEntryChoice(true)
        withContext(Dispatchers.Default) {
            for (attempt in 0 until 40) {
                if (storage.get(SecureStorage.KEY_REMEMBER_ENTRY_CHOICE) == "true") break
                kotlinx.coroutines.delay(15)
            }
        }
        assertEquals("true", storage.get(SecureStorage.KEY_REMEMBER_ENTRY_CHOICE))
        val restoredVm = RosaFiestaViewModel(
            repository = repo,
            secureStorage = storage,
            soundPlayer = NoOpSoundPlayer(),
            platformActions = NoOpPlatformActions(),
        )
        withContext(Dispatchers.Default) {
            for (attempt in 0 until 40) {
                if (restoredVm.rememberEntryChoice.value) break
                kotlinx.coroutines.delay(15)
            }
        }
        assertTrue(restoredVm.rememberEntryChoice.value)

        val lruRepo = object : RosaFiestaRepository by repo {
            override suspend fun uploadMedia(
                filename: String,
                mimeType: String,
                purpose: String,
                bytes: ByteArray,
            ): Result<com.jeudry.rosafiesta.domain.MediaUploadAsset> = Result.success(
                com.jeudry.rosafiesta.domain.MediaUploadAsset(
                    id = filename,
                    url = "/v1/media/$filename",
                    mimeType = mimeType,
                    byteSize = bytes.size.toLong(),
                    filename = filename,
                ),
            )
        }
        val lruVm = RosaFiestaViewModel(
            repository = lruRepo,
            secureStorage = InMemorySecureStorage(),
            soundPlayer = NoOpSoundPlayer(),
            platformActions = NoOpPlatformActions(),
        )
        for (idx in 1..44) {
            val done = CompletableDeferred<Unit>()
            lruVm.uploadAndAttachMedia(
                purpose = "setup",
                file = PickedMediaFile(
                    fileName = "photo-$idx.png",
                    mimeType = "image/png",
                    sizeBytes = 1024L,
                    bytes = ByteArray(1024),
                ),
                onUploaded = { done.complete(Unit) },
            )
            withContext(Dispatchers.Default) {
                withTimeout(2_000L) { done.await() }
            }
        }
        assertEquals(40, lruVm.mediaBytesCache.value.size)
        assertFalse(lruVm.mediaBytesCache.value.containsKey("/v1/media/photo-1.png"))
        assertTrue(lruVm.mediaBytesCache.value.containsKey("/v1/media/photo-44.png"))
    }

    @Test
    fun `password reset change password account deletion callbacks reminders revert whatsapp alerts and analytics work`() = runTest {
        val baseRepo = com.jeudry.rosafiesta.data.DefaultRosaFiestaRepository(seedDemoData = true)
        val failingPrefsRepo = object : RosaFiestaRepository by baseRepo {
            var resetOtpCalled = false

            override suspend fun resetPasswordWithOtp(
                email: String,
                newPassword: String,
                resetToken: String,
                otpCode: String,
            ): AuthOutcome {
                resetOtpCalled = true
                return baseRepo.resetPasswordWithOtp(email, newPassword, resetToken, otpCode)
            }

            override suspend fun updateUserPreferences(
                pushNotificationsEnabled: Boolean?,
                whatsappAlertsEnabled: Boolean?,
                emailReceiptsEnabled: Boolean?,
                biometricLockEnabled: Boolean?,
                autoSyncWifiOnly: Boolean?,
                eventRemindersEnabled: Boolean?,
                preferredLanguage: String?,
                darkThemeEnabled: Boolean?,
                savedPaymentMethod: String?,
                profileZone: String?,
            ): Result<RegisteredUserAccount> = Result.failure(IllegalStateException("Error persistiendo recordatorios"))
        }
        val storage = InMemorySecureStorage()
        val vm = RosaFiestaViewModel(
            repository = failingPrefsRepo,
            secureStorage = storage,
            soundPlayer = NoOpSoundPlayer(),
            platformActions = NoOpPlatformActions(),
        )

        val regOk = vm.submitRegistration(
            firstName = "Pedro",
            lastName = "Nunez",
            username = "pedro_n",
            email = "pedro.nunez@correo.do",
            password = "Password2026",
            birthDate = "11/02/1992",
            acceptedTerms = true,
            phone = "8095550177",
        )
        assertTrue(regOk)

        val initialReminders = vm.remindersEnabled.value
        vm.setRemindersEnabled(!initialReminders)
        withContext(Dispatchers.Default) {
            for (attempt in 0 until 40) {
                if (vm.formError.value != null) break
                kotlinx.coroutines.delay(15)
            }
        }
        assertEquals(initialReminders, vm.remindersEnabled.value)
        assertTrue(vm.formError.value?.contains("Error persistiendo") == true)

        val resetDeferred = CompletableDeferred<Boolean>()
        vm.submitPasswordReset(
            identifier = "pedro.nunez@correo.do",
            newPassword = "NewPassword2026",
            confirmPassword = "NewPassword2026",
            otpCode = "123456",
            onComplete = { ok -> resetDeferred.complete(ok) },
        )
        val resetResult = withContext(Dispatchers.Default) {
            withTimeout(2_000L) { resetDeferred.await() }
        }
        assertTrue(resetResult)
        assertTrue(failingPrefsRepo.resetOtpCalled)

        val changeFailDeferred = CompletableDeferred<Boolean>()
        vm.changeCurrentPassword(
            currentPassword = "WrongPassword1",
            newPassword = "AnotherPassword2",
            confirmPassword = "AnotherPassword2",
            onComplete = { ok -> changeFailDeferred.complete(ok) },
        )
        val changeFailResult = withContext(Dispatchers.Default) {
            withTimeout(2_000L) { changeFailDeferred.await() }
        }
        assertFalse(changeFailResult)

        val beforeWhatsapp = vm.businessSettings.value.whatsappAutoAlertsEnabled
        vm.toggleBusinessWhatsappAutoAlerts()
        assertEquals(!beforeWhatsapp, vm.businessSettings.value.whatsappAutoAlertsEnabled)

        val analytics3m = vm.computePeriodSpendingAnalytics("3m")
        val analytics12m = vm.computePeriodSpendingAnalytics("12m")
        assertEquals("90d", analytics3m.rangeKey)
        assertEquals("365d", analytics12m.rangeKey)
        assertEquals(3, analytics3m.trendMonths.size)
        assertEquals(6, analytics12m.trendMonths.size)

        val deleteDeferred = CompletableDeferred<Boolean>()
        vm.deleteCurrentUserAccount { ok -> deleteDeferred.complete(ok) }
        val deleteResult = withContext(Dispatchers.Default) {
            withTimeout(2_000L) { deleteDeferred.await() }
        }
        assertTrue(deleteResult)
    }
}
