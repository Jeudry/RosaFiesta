package com.jeudry.rosafiesta.designsystem

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalFocusManager

/**
 * Tracks the currently focused text input bounds in root coordinates so pointer taps
 * anywhere outside the active field can clear focus immediately.
 */
@Stable
public class RfFocusedTextInputTracker {
    private var activeOwnerKey: Any? = null
    private var activeBoundsInRoot: Rect? = null

    /** Updates tracked bounds when [ownerKey] owns focus. */
    public fun registerFocusedBounds(
        ownerKey: Any,
        boundsInRoot: Rect,
    ) {
        activeOwnerKey = ownerKey
        activeBoundsInRoot = boundsInRoot
    }

    /** Clears tracked bounds when [ownerKey] loses focus or leaves composition. */
    public fun clearOwner(ownerKey: Any) {
        if (activeOwnerKey == ownerKey) {
            activeOwnerKey = null
            activeBoundsInRoot = null
        }
    }

    /** Returns true when an active field is focused and [pressRootOffset] falls outside its bounds. */
    public fun isPressOutsideActiveField(pressRootOffset: Offset): Boolean {
        val currentBounds = activeBoundsInRoot ?: return false
        return !currentBounds.contains(pressRootOffset)
    }
}

public val LocalRfFocusedTextInputTracker: ProvidableCompositionLocal<RfFocusedTextInputTracker> =
    staticCompositionLocalOf { RfFocusedTextInputTracker() }

/**
 * Intercepts root pointer presses outside the currently focused input field and clears focus.
 */
public fun Modifier.rfClearFocusOnOutsidePress(): Modifier = composed {
    val focusManager = LocalFocusManager.current
    val tracker = LocalRfFocusedTextInputTracker.current
    var containerCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }

    onGloballyPositioned { coords ->
        containerCoords = coords
    }.pointerInput(focusManager, tracker) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
            val rootOffset = containerCoords?.localToRoot(down.position) ?: down.position
            if (tracker.isPressOutsideActiveField(rootOffset)) {
                focusManager.clearFocus()
            }
        }
    }
}

/**
 * Registers this input field's bounds while focused and notifies [onFocusChanged] on gain/loss.
 */
public fun Modifier.rfTrackValidationFocus(
    focusKey: Any,
    onFocusChanged: (Boolean) -> Unit,
): Modifier = composed {
    val tracker = LocalRfFocusedTextInputTracker.current
    val latestCallback by rememberUpdatedState(onFocusChanged)
    var isCurrentlyFocused by remember(focusKey) { mutableStateOf(false) }
    var lastCoordinates by remember(focusKey) { mutableStateOf<LayoutCoordinates?>(null) }

    DisposableEffect(tracker, focusKey) {
        onDispose {
            tracker.clearOwner(focusKey)
        }
    }

    onGloballyPositioned { coords ->
        lastCoordinates = coords
        if (isCurrentlyFocused) {
            if (coords.isAttached) {
                tracker.registerFocusedBounds(focusKey, coords.boundsInRoot())
            }
        }
    }.onFocusChanged { focusState ->
        val nextFocused = focusState.isFocused
        isCurrentlyFocused = nextFocused
        if (nextFocused) {
            val coords = lastCoordinates
            if (coords != null) {
                if (coords.isAttached) {
                    tracker.registerFocusedBounds(focusKey, coords.boundsInRoot())
                }
            }
        } else {
            tracker.clearOwner(focusKey)
        }
        latestCallback(nextFocused)
    }
}
