// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.importing

import com.tercad.zwyka.core.importing.ImportedHabit
import java.io.File
import kotlin.test.assertNotNull

/** Exports of other apps in `samples/` (made with the formats of the apps, with made-up data). */
internal fun sample(name: String): ByteArray = File("test@jvm/com/tercad/zwyka/importing/samples/$name").readBytes()

internal fun List<ImportedHabit>.named(title: String): ImportedHabit =
    assertNotNull(firstOrNull { it.draft.title == title }, "No habit $title in ${map { it.draft.title }}")
