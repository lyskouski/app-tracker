// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.core.importing

/*
 * CSV (RFC 4180): fields in double quotes may hold the [delimiter], line breaks and doubled quotes.
 */

/** Rows of [text]; empty lines are skipped. */
fun parseCsv(text: String, delimiter: Char = ','): List<List<String>> {
    val rows = mutableListOf<List<String>>()
    var row = mutableListOf<String>()
    val field = StringBuilder()
    var quoted = false
    var index = 0
    fun endField() {
        row += field.toString()
        field.clear()
    }
    fun endRow() {
        endField()
        if (row.size > 1 || row[0].isNotEmpty()) rows += row
        row = mutableListOf()
    }
    while (index < text.length) {
        val char = text[index++]
        when {
            quoted && char == '"' && index < text.length && text[index] == '"' -> {
                field.append('"')
                index++
            }
            char == '"' && (quoted || field.isEmpty()) -> quoted = !quoted
            quoted -> field.append(char)
            char == delimiter -> endField()
            char == '\r' -> Unit
            char == '\n' -> endRow()
            else -> field.append(char)
        }
    }
    if (field.isNotEmpty() || row.isNotEmpty()) endRow()
    return rows
}

/** [text] without the byte order mark that spreadsheet tools put first. */
internal fun String.withoutBom(): String = removePrefix("﻿")

/** Rows of a CSV with a header: column name to value; missing cells are empty. */
internal fun List<List<String>>.withHeader(): List<Map<String, String>> {
    if (isEmpty()) return emptyList()
    val header = first().map { it.trim() }
    return drop(1).map { cells -> header.mapIndexed { i, name -> name to (cells.getOrNull(i) ?: "") }.toMap() }
}
