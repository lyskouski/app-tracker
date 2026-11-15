// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.test.withKeyDown
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class TextInputTest {
    @Test
    fun tabMovesFocusInsteadOfTyping() = runComposeUiTest {
        setContent {
            var title by remember { mutableStateOf("") }
            var note by remember { mutableStateOf("") }
            Column {
                TextInput(
                    value = title,
                    onValueChange = { title = it },
                    singleLine = true,
                    modifier = Modifier.testTag("title"),
                )
                TextInput(
                    value = note,
                    onValueChange = { note = it },
                    minLines = 2,
                    modifier = Modifier.testTag("note"),
                )
                TextButton(onClick = {}) { Text("Save") }
            }
        }

        onNodeWithTag("title").performClick().performTextInput("Run")
        onNodeWithTag("title").performKeyInput { pressKey(Key.Tab) }
        onNodeWithTag("note").assertIsFocused()

        // Even a multi-line field passes the focus on.
        onNodeWithTag("note").performTextInput("a")
        onNodeWithTag("note").performKeyInput { pressKey(Key.Tab) }
        onNodeWithText("Save").assertIsFocused()

        onNodeWithTag("note").performClick()
        onNodeWithTag("note").performKeyInput { withKeyDown(Key.ShiftLeft) { pressKey(Key.Tab) } }
        onNodeWithTag("title").assertIsFocused()

        onNodeWithTag("title").assertTextEquals("Run")
        onNodeWithTag("note").assertTextEquals("a")
    }
}
