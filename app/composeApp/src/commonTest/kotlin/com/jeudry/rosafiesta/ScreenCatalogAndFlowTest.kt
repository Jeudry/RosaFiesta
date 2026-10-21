package com.jeudry.rosafiesta

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.jeudry.rosafiesta.core.engine.AppView
import com.jeudry.rosafiesta.designsystem.AdminTab
import com.jeudry.rosafiesta.designsystem.PersonalTab
import com.jeudry.rosafiesta.ui.Navigator
import com.jeudry.rosafiesta.ui.ScreenCatalog
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
        assertEquals(PersonalTab.FAVORITOS, ScreenCatalog.activePersonalTab("s24"))

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
            RosaFiestaApp(initialAppView = AppView.PERSONAL, initialScreenCode = "s14")
        }

        onNodeWithTag("screen-s14").assertExists()

        // Cambiar a App Administrativa (Inicio A01)
        onNodeWithTag("switch-admin").performClick()
        onNodeWithTag("screen-A01").assertExists()

        // Abrir el panel de desarrollo con las 133 pantallas
        onNodeWithTag("open-dev-panel").performClick()
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
        assertEquals(4, viewModel.payments.value.size)

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
}
