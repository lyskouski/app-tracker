// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.defines

import androidx.compose.runtime.Composable

// Supported by mobile browsers only (Vibration API); two pulses: vibrate, pause, vibrate.
private fun vibrateNavigator(): Unit = js("{ if (navigator.vibrate) navigator.vibrate([400, 200, 400]); }")

@Composable
actual fun rememberVibration(): () -> Unit = { vibrateNavigator() }
