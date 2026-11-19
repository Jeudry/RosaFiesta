package com.jeudry.rosafiesta.designsystem

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Data
import org.jetbrains.skia.Image
import org.jetbrains.skia.Surface
import org.jetbrains.skia.svg.SVGDOM

public actual fun decodeEncodedImageBitmapOrNull(bytes: ByteArray): ImageBitmap? {
    if (bytes.isEmpty()) return null
    if (looksLikeSvgBytes(bytes)) {
        val skiaSvgBitmap = runCatching {
            val svgDom = SVGDOM(Data.makeFromBytes(bytes))
            svgDom.setContainerSize(96f, 96f)
            val surface = Surface.makeRasterN32Premul(96, 96)
            svgDom.render(surface.canvas)
            surface.makeImageSnapshot().toComposeImageBitmap()
        }.getOrNull()
        return skiaSvgBitmap ?: decodeSvgToImageBitmapOrNull(bytes)
    }
    return runCatching {
        Image.makeFromEncoded(bytes).toComposeImageBitmap()
    }.getOrNull()
}
