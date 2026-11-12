package com.jeudry.rosafiesta

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.click
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.v2.runComposeUiTest
import com.jeudry.rosafiesta.core.engine.AppView
import com.jeudry.rosafiesta.designsystem.AdminTab
import com.jeudry.rosafiesta.designsystem.PersonalTab
import com.jeudry.rosafiesta.domain.RosaFiestaRepository
import com.jeudry.rosafiesta.domain.platform.NoOpPlatformActions
import com.jeudry.rosafiesta.domain.platform.PickedMediaFile
import com.jeudry.rosafiesta.domain.security.InMemorySecureStorage
import com.jeudry.rosafiesta.ui.Navigator
import com.jeudry.rosafiesta.ui.RosaFiestaViewModel
import com.jeudry.rosafiesta.ui.ScreenCatalog
import com.jeudry.rosafiesta.ui.formatSpanishDateWithWords
import kotlin.io.encoding.Base64
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ScreenCatalogAndFlowTest {
    @Test
    fun `verifica que el catalogo completo contenga las 133 pantallas sin duplicados`() {
        assertEquals(73, ScreenCatalog.personalScreens.size)
        assertEquals(60, ScreenCatalog.adminScreens.size)
        assertEquals(133, ScreenCatalog.allScreens.size)

        val uniqueCodes = ScreenCatalog.allScreens.map { it.code }.toSet()
        assertEquals(133, uniqueCodes.size)

        assertTrue("p00" in uniqueCodes)
        assertTrue("s01" in uniqueCodes)
        assertTrue("s15" in uniqueCodes)
        assertTrue("s88" in uniqueCodes)
        assertTrue("A00" in uniqueCodes)
        assertTrue("A30" in uniqueCodes)
        assertTrue("A39" in uniqueCodes)
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
    fun `modulo Koin resuelve RosaFiestaViewModel y muta estado reactivo`() {
        val koin = org.koin.dsl.koinApplication {
            modules(com.jeudry.rosafiesta.di.appModule)
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
        assertTrue(viewModel.offlineQueue.value.all { it.synced })
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `pantalla s46 conectada a RosaFiestaViewModel alterna paso del checklist al hacer clic`() = runComposeUiTest {
        val koin = org.koin.dsl.koinApplication {
            modules(com.jeudry.rosafiesta.di.appModule)
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
        viewModel.setNewArticlePrice("250")
        viewModel.setNewArticleStock("15")
        assertTrue(viewModel.submitNewArticle())
        assertTrue(viewModel.articles.value.any { it.code == "RF-110" })

        viewModel.setPaymentAmountInput("1500")
        viewModel.setPaymentMethodInput("Transferencia Popular")
        viewModel.setPaymentRefInput("POP-2026-11")
        val payment = viewModel.submitPayment("evt-boda-real")
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
        onNodeWithText("Cliente: Camila Rodríguez").assertExists()
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
        val koin = org.koin.dsl.koinApplication {
            modules(com.jeudry.rosafiesta.di.appModule)
        }.koin
        val viewModel = koin.get<com.jeudry.rosafiesta.ui.RosaFiestaViewModel>()

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

        onNodeWithText("Floral").performClick()
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
        viewModel.submitCompanyReview("Excelente montaje y atención", 5)
        assertEquals(3, viewModel.companyReviews.value.size)
        assertEquals("5.0", viewModel.companyAverageRatingFormatted())

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
        val koin = org.koin.dsl.koinApplication {
            modules(com.jeudry.rosafiesta.di.appModule)
        }.koin
        val viewModel = koin.get<com.jeudry.rosafiesta.ui.RosaFiestaViewModel>()
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
        onNodeWithTag("more-option-s54").assertExists()

        onNodeWithTag("personal-more-panel").performTouchInput {
            val headerPoint = Offset(centerX, top + 10f)
            click(headerPoint)
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

        viewModel.pickProfileAvatarFromDevice()
        waitUntil(timeoutMillis = 3_000L) {
            viewModel.profileAvatarBytes.value != null
        }

        assertEquals("CUSTOM:camila-avatar.png", viewModel.profileAvatarKey.value)
        onNodeWithText("Foto 'camila-avatar.png' seleccionada.").assertExists()
        onNodeWithContentDescription("Foto de perfil").assertExists()
        onNodeWithText("camila-avatar.png").assertDoesNotExist()

        mainClock.advanceTimeBy(3_100L)
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
}
