package com.jeudry.rosafiesta

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.jeudry.rosafiesta.core.engine.AppView
import com.jeudry.rosafiesta.ui.ScreenCatalog
import kotlin.test.Test
import kotlin.test.assertEquals
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

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `navega entre App Personal y App Administrativa y ejecuta el cambio de vista A30`() = runComposeUiTest {
        setContent {
            RosaFiestaApp(initialAppView = AppView.PERSONAL, initialScreenCode = "s14")
        }

        onNodeWithTag("screen-s14").assertExists()

        // Cambiar a App Administrativa
        onNodeWithTag("switch-admin").performClick()
        onNodeWithTag("screen-s55").assertExists()

        // Abrir el panel de desarrollo con las 133 pantallas
        onNodeWithTag("open-dev-panel").performClick()
    }
}
