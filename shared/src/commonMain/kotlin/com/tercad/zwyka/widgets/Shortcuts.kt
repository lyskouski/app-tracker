// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.widgets

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalWindowInfo

/*
 * Keyboard shortcuts (WCAG 2.1.1).
 *
 * Keys reach the focused element first and then its parents, so a focused
 * element keeps its own keys: Enter clicks a button, a multi-line text field
 * types a new line, an editable text field deletes a character by Delete or Backspace.
 */

/*
 * Shortcuts of the shown screens; the latest registered (the top screen) goes first.
 */
class Shortcuts {
    private val handlers = mutableListOf<(KeyEvent) -> Boolean>()

    fun dispatch(event: KeyEvent): Boolean = handlers.asReversed().any { it(event) }

    internal fun add(handler: (KeyEvent) -> Boolean) {
        handlers += handler
    }

    internal fun remove(handler: (KeyEvent) -> Boolean) {
        handlers -= handler
    }
}

val LocalShortcuts = staticCompositionLocalOf { Shortcuts() }

/*
 * Root of the application: passes the keys no element has handled to the shortcuts of the screen.
 * It holds the focus whenever nothing else does (on start, or after the focused element has gone),
 * so the shortcuts work before anything is focused; the focus is not taken from other windows.
 */
@Composable
fun ShortcutsHost(content: @Composable () -> Unit) {
    val shortcuts = remember { Shortcuts() }
    val focusRequester = remember { FocusRequester() }
    val windowInfo = LocalWindowInfo.current
    var hasFocus by remember { mutableStateOf(false) }
    LaunchedEffect(focusRequester) {
        snapshotFlow { !hasFocus && windowInfo.isWindowFocused }
            .collect { isUnfocused -> if (isUnfocused) focusRequester.requestFocus() }
    }
    CompositionLocalProvider(LocalShortcuts provides shortcuts) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onFocusChanged { hasFocus = it.hasFocus }
                .onKeyEvent(shortcuts::dispatch)
                .focusRequester(focusRequester)
                .focusTarget(),
        ) {
            content()
        }
    }
}

/*
 * Shortcuts of a screen, active while it is shown; [onKeyEvent] returns true for a handled key.
 */
@Composable
fun ScreenShortcuts(onKeyEvent: (KeyEvent) -> Boolean) {
    val shortcuts = LocalShortcuts.current
    val latest by rememberUpdatedState(onKeyEvent)
    DisposableEffect(shortcuts) {
        val handler: (KeyEvent) -> Boolean = { latest(it) }
        shortcuts.add(handler)
        onDispose { shortcuts.remove(handler) }
    }
}

/** Ctrl + N (Cmd + N on macOS): a new item of the screen, as the `(+)` button. */
val KeyEvent.isNewItem: Boolean
    get() = type == KeyEventType.KeyDown && key == Key.N &&
        (isCtrlPressed || isMetaPressed) && !isAltPressed && !isShiftPressed

/** Backspace: the "Back" button of the screen. */
val KeyEvent.isBack: Boolean
    get() = type == KeyEventType.KeyDown && key == Key.Backspace &&
        !isCtrlPressed && !isMetaPressed && !isAltPressed && !isShiftPressed

/** Alt + 1 ... Alt + 9, Alt + 0: index 0 ... 9, null for other keys. */
val KeyEvent.altDigitIndex: Int?
    get() {
        if (type != KeyEventType.KeyDown || !isAltPressed || isCtrlPressed || isMetaPressed) return null
        return altDigits.indexOf(key).takeIf { it >= 0 }
    }

private val altDigits = listOf(
    Key.One, Key.Two, Key.Three, Key.Four, Key.Five,
    Key.Six, Key.Seven, Key.Eight, Key.Nine, Key.Zero,
)

/*
 * Shortcuts of a popup: Enter applies it ([onEnter]), Delete deletes its item ([onDelete]);
 * Esc closes any popup on its own. Applied to the popup itself, e.g. `AlertDialog(modifier = ...)`.
 *
 * The popup takes the focus when opened, unless one of its elements has taken it,
 * so the keys work before anything in it is focused.
 */
fun Modifier.popupShortcuts(
    onEnter: (() -> Unit)?,
    onDelete: (() -> Unit)? = null,
): Modifier = composed {
    val focusRequester = remember { FocusRequester() }
    var hasFocus by remember { mutableStateOf(false) }
    LaunchedEffect(focusRequester) {
        if (!hasFocus) focusRequester.requestFocus()
    }
    val latestEnter by rememberUpdatedState(onEnter)
    val latestDelete by rememberUpdatedState(onDelete)
    Modifier
        .onFocusChanged { hasFocus = it.hasFocus }
        .onKeyEvent { event ->
            val isPlain = event.type == KeyEventType.KeyDown && !event.isAltPressed &&
                !event.isCtrlPressed && !event.isMetaPressed && !event.isShiftPressed
            val action = when {
                !isPlain -> null
                event.key == Key.Enter || event.key == Key.NumPadEnter -> latestEnter
                event.key == Key.Delete -> latestDelete
                else -> null
            }
            action?.invoke()
            action != null
        }
        .focusRequester(focusRequester)
        .focusTarget()
}
