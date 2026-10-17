// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.core.service

import com.tercad.zwyka.core.model.DayState
import com.tercad.zwyka.core.repository.DayStateRepository

class DayStateService(
    private val dayStates: DayStateRepository,
) {
    /** Day states within [from]..[to] (ISO dates, inclusive). */
    suspend fun getRange(from: String, to: String): List<DayState> = dayStates.getRange(from, to)

    suspend fun setMark(state: DayState) = dayStates.upsert(state)
}
