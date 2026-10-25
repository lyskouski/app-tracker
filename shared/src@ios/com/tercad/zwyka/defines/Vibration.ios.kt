// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.defines

import androidx.compose.runtime.Composable
import platform.AudioToolbox.AudioServicesPlaySystemSound
import platform.AudioToolbox.kSystemSoundID_Vibrate

@Composable
actual fun rememberVibration(): () -> Unit = {
    AudioServicesPlaySystemSound(kSystemSoundID_Vibrate)
}
