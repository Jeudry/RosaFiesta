package com.jeudry.rosafiesta.designsystem

import androidx.compose.ui.graphics.ImageBitmap

/** Decodes encoded image bytes into an [ImageBitmap], or returns `null` when empty or unreadable. */
public expect fun decodeEncodedImageBitmapOrNull(bytes: ByteArray): ImageBitmap?
