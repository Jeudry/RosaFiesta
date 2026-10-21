package com.jeudry.rosafiesta.dev

import com.jeudry.rosafiesta.testing.ScreenshotAssertions
import kotlin.test.Test

/**
 * Pruebas de regresión visual de la galería de componentes (`DesignGallery`)
 * y de los tableros canónicos de la App Personal y la App Administrativa (incluyendo `s15` oscuro y `A39` web 1440×900).
 */
class GalleryScreenshotTest {
    @Test
    fun `la galeria de componentes del sistema de diseno coincide con la captura aprobada`() {
        ScreenshotAssertions.assertMatchesApproved("design-gallery") {
            DesignGallery()
        }
    }

    @Test
    fun `los tableros canonicos de App Personal y App Administrativa coinciden con las capturas aprobadas`() {
        GoldenScreenshotBoards.forEach { board ->
            ScreenshotAssertions.assertBoard(board)
        }
    }
}
