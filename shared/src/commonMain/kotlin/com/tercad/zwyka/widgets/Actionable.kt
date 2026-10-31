// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.widgets

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
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
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics

/*
 * Click and long click reachable without a pointer (WCAG 2.1.1, 4.1.2):
 *
 * - screen readers announce both actions by their labels;
 * - the keyboard clicks with Enter / Space and long clicks with
 *   the context menu key or Shift + F10, as desktop applications do,
 *   or with Ctrl + Enter (Cmd + Enter on macOS).
 *
 * Without [onClick] only the long click is handled, e.g. for a card
 * whose children have their own clicks.
 */
@OptIn(ExperimentalFoundationApi::class)
fun Modifier.actionable(
    onClick: (() -> Unit)?,
    onClickLabel: String? = null,
    onLongClick: (() -> Unit)? = null,
    onLongClickLabel: String? = null,
    role: Role? = null,
    enabled: Boolean = true,
): Modifier {
    val longClickKeys = if (onLongClick != null && enabled) {
        Modifier.composed {
            var isFocused by remember { mutableStateOf(false) }
            Modifier
                .onFocusChanged { isFocused = it.isFocused }
                .onKeyEvent { event ->
                    val isContextMenu = event.key == Key.Menu ||
                        (event.key == Key.F10 && event.isShiftPressed)
                    if (isContextMenu && event.type == KeyEventType.KeyUp) onLongClick()
                    isContextMenu
                }
                // Previewed: the click takes Enter whatever modifier keys are pressed.
                // Only for the element itself, not for its focused children.
                .onPreviewKeyEvent { event ->
                    val isCtrlEnter = isFocused && event.isCtrlEnter
                    if (isCtrlEnter && event.type == KeyEventType.KeyUp) onLongClick()
                    isCtrlEnter
                }
        }
    } else {
        Modifier
    }
    if (onClick != null) {
        return this
            .then(longClickKeys)
            .combinedClickable(
                enabled = enabled,
                onClickLabel = onClickLabel,
                role = role,
                onLongClickLabel = onLongClickLabel,
                onLongClick = onLongClick,
                onClick = onClick,
            )
    }
    if (onLongClick == null || !enabled) return this
    return this.composed {
        // The focus is highlighted as on a clickable element (WCAG 2.4.7).
        val interactionSource = remember { MutableInteractionSource() }
        Modifier
            .pointerInput(onLongClick) { detectTapGestures(onLongPress = { onLongClick() }) }
            .semantics { onLongClick(label = onLongClickLabel) { onLongClick(); true } }
            .then(longClickKeys)
            .focusable(interactionSource = interactionSource)
            .indication(interactionSource, LocalIndication.current)
    }
}

/** Ctrl + Enter (Cmd + Enter on macOS), pressed or released. */
private val KeyEvent.isCtrlEnter: Boolean
    get() = (key == Key.Enter || key == Key.NumPadEnter) &&
        (isCtrlPressed || isMetaPressed) && !isAltPressed && !isShiftPressed
