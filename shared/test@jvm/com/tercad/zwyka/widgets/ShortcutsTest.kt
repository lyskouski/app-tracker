// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.test.withKeyDown
import androidx.compose.ui.platform.testTag
import com.tercad.zwyka.defines.ColorSchemeLight
import com.tercad.zwyka.defines.LocalColorScheme
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class ShortcutsTest {
    @Test
    fun screenShortcutsWorkWithoutFocusedElement() = runComposeUiTest {
        var added = 0
        var opened: Int? = null
        setContent {
            ShortcutsHost {
                ScreenShortcuts { event ->
                    if (event.isNewItem) added++
                    event.altDigitIndex?.let { opened = it }
                    event.isNewItem || event.altDigitIndex != null
                }
                TextButton(onClick = {}) { Text("Action") }
            }
        }

        onRoot().performKeyInput { withKeyDown(Key.CtrlLeft) { pressKey(Key.N) } }
        onRoot().performKeyInput { pressKey(Key.N) }
        assertEquals(1, added)

        onRoot().performKeyInput { withKeyDown(Key.AltLeft) { pressKey(Key.One) } }
        assertEquals(0, opened)
        onRoot().performKeyInput { withKeyDown(Key.AltLeft) { pressKey(Key.Zero) } }
        assertEquals(9, opened)

        // The focus held by the root still moves to the elements of the screen.
        onRoot().performKeyInput { pressKey(Key.Tab) }
        onNodeWithText("Action").assertIsFocused()
        onRoot().performKeyInput { withKeyDown(Key.CtrlLeft) { pressKey(Key.N) } }
        assertEquals(2, added)
    }

    @Test
    fun popupAppliesByEnterAndDeletesByDelete() = runComposeUiTest {
        var log by mutableStateOf(listOf<String>())
        setContent {
            var isOpen by remember { mutableStateOf(true) }
            var title by remember { mutableStateOf("") }
            var note by remember { mutableStateOf("") }
            if (isOpen) {
                AlertDialog(
                    onDismissRequest = { isOpen = false },
                    modifier = Modifier.popupShortcuts(
                        onEnter = { log = log + "enter:$title" },
                        onDelete = { log = log + "delete" },
                    ),
                    text = {
                        Column {
                            OutlinedTextField(
                                value = title,
                                onValueChange = { title = it },
                                singleLine = true,
                                modifier = Modifier.testTag("title"),
                            )
                            OutlinedTextField(
                                value = note,
                                onValueChange = { note = it },
                                minLines = 2,
                                modifier = Modifier.testTag("note"),
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { log = log + "button" }) { Text("Save") }
                    },
                )
            }
        }
        waitForIdle()

        // Nothing focused yet: the popup itself has the focus.
        onNodeWithText("Save").performKeyInput { pressKey(Key.Delete) }
        onNodeWithText("Save").performKeyInput { pressKey(Key.Enter) }
        assertEquals(listOf("delete", "enter:"), log)

        // A single-line text field applies the popup by Enter, but keeps Delete.
        onNodeWithTag("title").performClick().performTextInput("Run")
        onNodeWithTag("title").performKeyInput {
            pressKey(Key.DirectionLeft)
            pressKey(Key.Delete)
            pressKey(Key.Enter)
        }
        assertEquals(listOf("delete", "enter:", "enter:Ru"), log)

        // A multi-line text field types a new line.
        onNodeWithTag("note").performClick().performTextInput("a")
        onNodeWithTag("note").performKeyInput { pressKey(Key.Enter) }
        onNodeWithTag("note").assertTextEquals("a\n")
        assertEquals(3, log.size)

        // A focused button is clicked by Enter.
        onNodeWithText("Save").requestFocus().performKeyInput { pressKey(Key.Enter) }
        assertEquals("button", log.last())
    }

    @Test
    fun ctrlEnterLongClicksFocusedElementOnly() = runComposeUiTest {
        var log by mutableStateOf(listOf<String>())
        setContent {
            ShortcutsHost {
                Column(modifier = Modifier.actionable(onClick = null, onLongClick = { log = log + "card" })) {
                    Text(
                        text = "Item",
                        modifier = Modifier.actionable(
                            onClick = { log = log + "click" },
                            onLongClick = { log = log + "long" },
                        ),
                    )
                    TextButton(onClick = { log = log + "button" }) { Text("Plain") }
                }
            }
        }

        onNodeWithText("Item").requestFocus().performKeyInput { withKeyDown(Key.CtrlLeft) { pressKey(Key.Enter) } }
        assertEquals(listOf("long"), log)
        onNodeWithText("Item").performKeyInput { pressKey(Key.Enter) }
        assertEquals(listOf("long", "click"), log)

        // An element without a long click keeps Ctrl + Enter: the card is not long clicked.
        onNodeWithText("Plain").requestFocus().performKeyInput { withKeyDown(Key.CtrlLeft) { pressKey(Key.Enter) } }
        assertEquals(false, "card" in log)
    }

    @Test
    fun backspacePressesBackButton() = runComposeUiTest {
        var back = 0
        setContent {
            ShortcutsHost {
                Column {
                    var text by remember { mutableStateOf("") }
                    OutlinedTextField(value = text, onValueChange = { text = it }, modifier = Modifier.testTag("text"))
                    CompositionLocalProvider(LocalColorScheme provides ColorSchemeLight) {
                        BottomBar { BottomBarBackButton(onClick = { back++ }) }
                    }
                }
            }
        }

        onRoot().performKeyInput { pressKey(Key.Backspace) }
        assertEquals(1, back)
        onRoot().performKeyInput { withKeyDown(Key.CtrlLeft) { pressKey(Key.Backspace) } }
        assertEquals(1, back)

        // A text field keeps Backspace, even when empty.
        onNodeWithTag("text").performClick().performTextInput("a")
        onNodeWithTag("text").performKeyInput {
            pressKey(Key.Backspace)
            pressKey(Key.Backspace)
        }
        onNodeWithTag("text").assertTextEquals("")
        assertEquals(1, back)
    }
}
