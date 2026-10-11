// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.defines

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue

/** Overrides the language used by the application resources; null restores the system language. */
expect object LocalAppLocale {
    @Composable
    infix fun provides(tag: String?): ProvidedValue<*>
}
