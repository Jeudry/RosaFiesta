package com.jeudry.rosafiesta.designsystem

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

public actual fun decodeEncodedImageBitmapOrNull(bytes: ByteArray): ImageBitmap? {
    if (bytes.isEmpty()) return null
    if (looksLikeSvgBytes(bytes)) {
        return decodeSvgToImageBitmapOrNull(bytes)
    }
    return runCatching {
        val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return@runCatching null
        decoded.asImageBitmap()
    }.getOrNull()
}
