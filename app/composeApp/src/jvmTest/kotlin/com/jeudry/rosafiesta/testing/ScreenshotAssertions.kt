package com.jeudry.rosafiesta.testing

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.v2.runSkikoComposeUiTest
import androidx.compose.ui.unit.Density
import com.jeudry.rosafiesta.dev.Board
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.FilterMipmap
import org.jetbrains.skia.FilterMode
import org.jetbrains.skia.Image
import org.jetbrains.skia.MipmapMode
import org.jetbrains.skia.Surface
import java.io.File
import org.jetbrains.skia.Rect as SkiaRect

/**
 * Verificación visual determinista de RosaFiesta contra capturas aprobadas en `screenshots/approved/<codigo>.png`.
 * Soporta tanto el marco móvil oficial (390 × 820 dp) como la vista web de escritorio `A39` (1440 × 900 dp).
 */
internal object ScreenshotAssertions {
    private const val MOBILE_WIDTH = 390
    private const val MOBILE_HEIGHT = 820
    private const val DESKTOP_WIDTH = 1440
    private const val DESKTOP_HEIGHT = 900

    private const val SCALE = 2f
    private const val CHANNEL_TOLERANCE = 8
    private const val MAX_DIFFERENT_FRACTION = 0.002

    private val approvedDir = File("screenshots/approved")
    private val actualDir = File("build/screenshots/actual")
    private val diffDir = File("build/screenshots/diff")

    fun assertBoard(board: Board) {
        val w = if (board.isDesktop) DESKTOP_WIDTH else MOBILE_WIDTH
        val h = if (board.isDesktop) DESKTOP_HEIGHT else MOBILE_HEIGHT
        assertMatchesApproved(
            name = board.code,
            width = w,
            height = h,
            content = board.content,
        )
    }

    @OptIn(ExperimentalTestApi::class)
    fun assertMatchesApproved(
        name: String,
        width: Int = MOBILE_WIDTH,
        height: Int = MOBILE_HEIGHT,
        content: @Composable () -> Unit,
    ) {
        lateinit var actual: ImageBitmap
        runSkikoComposeUiTest(
            size = Size(width * SCALE, height * SCALE),
            density = Density(SCALE),
        ) {
            setContent(content)
            waitForIdle()
            actual = downscale(onRoot().captureToImage(), width, height)
        }
        assertMatches(name, actual)
    }

    private fun downscale(image: ImageBitmap, targetW: Int, targetH: Int): ImageBitmap {
        val source = Image.makeFromBitmap(image.asSkiaBitmap())
        val surface = Surface.makeRasterN32Premul(targetW, targetH)
        surface.canvas.drawImageRect(
            source,
            SkiaRect.makeWH(source.width.toFloat(), source.height.toFloat()),
            SkiaRect.makeWH(targetW.toFloat(), targetH.toFloat()),
            FilterMipmap(FilterMode.LINEAR, MipmapMode.LINEAR),
            null,
            true,
        )
        return surface.makeImageSnapshot().toComposeImageBitmap()
    }

    private fun assertMatches(name: String, actual: ImageBitmap) {
        val actualPng = actual.toPng()
        write(File(actualDir, "$name.png"), actualPng)

        val approvedFile = File(approvedDir, "$name.png")
        if (System.getenv("UPDATE_SCREENSHOTS") == "1") {
            write(approvedFile, actualPng)
            return
        }
        check(approvedFile.exists()) {
            "No approved screenshot for '$name'. Approve with `make app-screenshots-approve`."
        }

        val approved = Image.makeFromEncoded(approvedFile.readBytes()).toComposeImageBitmap()
        val fraction = differentFraction(approved, actual, File(diffDir, "$name.png"))
        check(fraction <= MAX_DIFFERENT_FRACTION) {
            "Screenshot '$name' differs in ${"%.2f".format(fraction * 100)}% of pixels. See build/screenshots/diff/$name.png"
        }
    }

    private fun differentFraction(approved: ImageBitmap, actual: ImageBitmap, diffFile: File): Double {
        check(approved.width == actual.width && approved.height == actual.height) {
            "Screenshot size changed: approved ${approved.width}x${approved.height}, actual ${actual.width}x${actual.height}"
        }
        val expectedPixels = approved.toPixelMap()
        val actualPixels = actual.toPixelMap()
        val diff = org.jetbrains.skia.Bitmap().apply { allocN32Pixels(actual.width, actual.height) }

        var different = 0
        for (y in 0 until actual.height) {
            for (x in 0 until actual.width) {
                val a = expectedPixels[x, y]
                val b = actualPixels[x, y]
                val isDifferent =
                    channelsDiffer(a.red, b.red) || channelsDiffer(a.green, b.green) || channelsDiffer(a.blue, b.blue)
                if (isDifferent) different++
                diff.erase(if (isDifferent) RED else faded(b), org.jetbrains.skia.IRect.makeXYWH(x, y, 1, 1))
            }
        }
        if (different > 0) write(diffFile, Image.makeFromBitmap(diff).encodeToData(EncodedImageFormat.PNG)!!.bytes)
        return different.toDouble() / (actual.width * actual.height)
    }

    private fun channelsDiffer(a: Float, b: Float): Boolean = kotlin.math.abs(a - b) * 255f > CHANNEL_TOLERANCE

    private const val RED: Int = 0xFFFF0000.toInt()

    private fun faded(color: androidx.compose.ui.graphics.Color): Int {
        val r = (color.red * 255 * 0.35f + 255 * 0.65f).toInt()
        val g = (color.green * 255 * 0.35f + 255 * 0.65f).toInt()
        val b = (color.blue * 255 * 0.35f + 255 * 0.65f).toInt()
        return (0xFF shl 24) or (r shl 16) or (g shl 8) or b
    }

    private fun ImageBitmap.toPng(): ByteArray = Image.makeFromBitmap(asSkiaBitmap()).encodeToData(EncodedImageFormat.PNG)!!.bytes

    private fun write(file: File, bytes: ByteArray) {
        file.parentFile.mkdirs()
        file.writeBytes(bytes)
    }
}
