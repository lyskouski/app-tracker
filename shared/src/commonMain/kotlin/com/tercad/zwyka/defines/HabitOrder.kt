// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.defines

import org.jetbrains.compose.resources.StringResource

import com.tercad.zwyka.core.model.HabitOrderBy
import com.tercad.zwyka.core.model.SortDirection
import com.tercad.zwyka.resources.Res
import com.tercad.zwyka.resources.order_created_at
import com.tercad.zwyka.resources.order_title
import com.tercad.zwyka.resources.order_updated_at
import com.tercad.zwyka.resources.sort_ascending
import com.tercad.zwyka.resources.sort_descending

val HabitOrderBy.label: StringResource
    get() = when (this) {
        HabitOrderBy.CREATED_AT -> Res.string.order_created_at
        HabitOrderBy.UPDATED_AT -> Res.string.order_updated_at
        HabitOrderBy.TITLE -> Res.string.order_title
    }

val SortDirection.label: StringResource
    get() = when (this) {
        SortDirection.ASC -> Res.string.sort_ascending
        SortDirection.DESC -> Res.string.sort_descending
    }

val habitOrderByOptions = HabitOrderBy.entries
val sortDirectionOptions = SortDirection.entries
