package com.jeudry.rosafiesta.designsystem

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

private const val MAX_DECODED_IMAGE_DIMENSION_PX: Int = 1280
private const val MAX_SAMPLE_POWER_STEPS: Int = 16

private fun computePowerOfTwoSampleSize(
    rawWidth: Int,
    rawHeight: Int,
    maxDimensionPx: Int,
): Int {
    val isWidthWithinBounds = rawWidth <= maxDimensionPx
    val isHeightWithinBounds = rawHeight <= maxDimensionPx
    if (isWidthWithinBounds) {
        if (isHeightWithinBounds) {
            return 1
        }
    }
    var sampleSize = 1
    val halfWidth = rawWidth / 2
    val halfHeight = rawHeight / 2
    for (step in 0 until MAX_SAMPLE_POWER_STEPS) {
        val widthExceedsLimit = halfWidth / sampleSize >= maxDimensionPx
        val heightExceedsLimit = halfHeight / sampleSize >= maxDimensionPx
        val shouldDownsample = if (widthExceedsLimit) true else heightExceedsLimit
        if (!shouldDownsample) {
            break
        }
        sampleSize *= 2
    }
    return sampleSize
}

public actual fun decodeEncodedImageBitmapOrNull(bytes: ByteArray): ImageBitmap? {
    if (bytes.isEmpty()) return null
    if (looksLikeSvgBytes(bytes)) {
        return decodeSvgToImageBitmapOrNull(bytes)
    }
    return runCatching {
        val boundsOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, boundsOptions)
        val rawWidth = boundsOptions.outWidth
        val rawHeight = boundsOptions.outHeight
        val hasValidBounds = if (rawWidth > 0) rawHeight > 0 else false
        if (!hasValidBounds) {
            return@runCatching null
        }
        val sampleSize = computePowerOfTwoSampleSize(
            rawWidth = rawWidth,
            rawHeight = rawHeight,
            maxDimensionPx = MAX_DECODED_IMAGE_DIMENSION_PX,
        )
        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
        }
        val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOptions)
            ?: return@runCatching null
        decoded.asImageBitmap()
    }.getOrNull()
}
