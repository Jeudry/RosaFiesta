package com.jeudry.rosafiesta

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import com.jeudry.rosafiesta.domain.platform.AppLifecycleBridge

/**
 * Vector window icon [Painter] for RosaFiesta Desktop JVM applications.
 */
internal class RosaFiestaDesktopIconPainter(
    private val isAdmin: Boolean,
) : Painter() {
    override val intrinsicSize: Size = Size(128f, 128f)

    override fun DrawScope.onDraw() {
        val startColor = if (isAdmin) Color(0xFF312E81) else Color(0xFFD6336C)
        val endColor = if (isAdmin) Color(0xFF0D9488) else Color(0xFF7C3AED)
        drawRoundRect(
            brush = Brush.linearGradient(listOf(startColor, endColor)),
            cornerRadius = CornerRadius(size.width * 0.24f, size.height * 0.24f),
        )
        val inset = size.width * 0.24f
        drawRoundRect(
            color = Color(0xFFFFF5F7),
            topLeft = Offset(inset, inset),
            size = Size(size.width - inset * 2f, size.height - inset * 2f),
            cornerRadius = CornerRadius(size.width * 0.12f, size.height * 0.12f),
        )
        val innerInset = size.width * 0.36f
        drawRoundRect(
            color = startColor,
            topLeft = Offset(innerInset, innerInset),
            size = Size(size.width - innerInset * 2f, size.height - innerInset * 2f),
            cornerRadius = CornerRadius(size.width * 0.06f, size.height * 0.06f),
        )
    }
}

@Composable
internal fun ApplicationScope.RosaFiestaDesktopWindow(
    title: String,
    isAdmin: Boolean,
    state: WindowState,
    content: @Composable () -> Unit,
) {
    val iconPainter = remember(isAdmin) { RosaFiestaDesktopIconPainter(isAdmin = isAdmin) }
    LaunchedEffect(state.isMinimized) {
        if (state.isMinimized) {
            AppLifecycleBridge.dispatchAppBackgrounded()
        }
    }
    Window(
        onCloseRequest = {
            AppLifecycleBridge.dispatchAppBackgrounded()
            exitApplication()
        },
        title = title,
        icon = iconPainter,
        state = state,
    ) {
        content()
    }
}
