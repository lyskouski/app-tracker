// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.core.importing

/*
 * JSON (RFC 8259) as plain values: Map<String, Any?>, List<Any?>, String, Double, Boolean or null.
 */

class JsonException(position: Int, reason: String) : IllegalArgumentException("JSON at $position: $reason")

fun parseJson(text: String): Any? = JsonReader(text).run {
    val value = readValue()
    skipSpaces()
    if (index != text.length) fail("unexpected text")
    value
}

private class JsonReader(private val text: String) {
    var index = 0

    fun fail(reason: String): Nothing = throw JsonException(index, reason)

    fun skipSpaces() {
        while (index < text.length && text[index].isWhitespace()) index++
    }

    fun readValue(): Any? {
        skipSpaces()
        if (index >= text.length) fail("missing value")
        return when (text[index]) {
            '{' -> readObject()
            '[' -> readArray()
            '"' -> readString()
            't' -> readWord("true", true)
            'f' -> readWord("false", false)
            'n' -> readWord("null", null)
            else -> readNumber()
        }
    }

    private fun readWord(word: String, value: Any?): Any? {
        if (!text.startsWith(word, index)) fail("unexpected character")
        index += word.length
        return value
    }

    private fun readNumber(): Double {
        val start = index
        while (index < text.length && text[index] in "+-0123456789.eE") index++
        return text.substring(start, index).toDoubleOrNull() ?: fail("invalid number")
    }

    private fun readObject(): Map<String, Any?> {
        val result = LinkedHashMap<String, Any?>()
        index++
        skipSpaces()
        if (text.getOrNull(index) == '}') return result.also { index++ }
        while (true) {
            skipSpaces()
            if (text.getOrNull(index) != '"') fail("expected a key")
            val key = readString()
            skipSpaces()
            if (text.getOrNull(index++) != ':') fail("expected ':'")
            result[key] = readValue()
            skipSpaces()
            when (text.getOrNull(index++)) {
                ',' -> continue
                '}' -> return result
                else -> fail("expected ',' or '}'")
            }
        }
    }

    private fun readArray(): List<Any?> {
        val result = mutableListOf<Any?>()
        index++
        skipSpaces()
        if (text.getOrNull(index) == ']') return result.also { index++ }
        while (true) {
            result += readValue()
            skipSpaces()
            when (text.getOrNull(index++)) {
                ',' -> continue
                ']' -> return result
                else -> fail("expected ',' or ']'")
            }
        }
    }

    private fun readString(): String {
        val result = StringBuilder()
        index++
        while (true) {
            if (index >= text.length) fail("unterminated string")
            when (val char = text[index++]) {
                '"' -> return result.toString()
                '\\' -> {
                    if (index >= text.length) fail("unterminated string")
                    when (val escaped = text[index++]) {
                        'b' -> result.append('\b')
                        'f' -> result.append('\u000C')
                        'n' -> result.append('\n')
                        'r' -> result.append('\r')
                        't' -> result.append('\t')
                        'u' -> {
                            val code = text.substring(index, minOf(index + 4, text.length)).toIntOrNull(16)
                                ?: fail("invalid escape")
                            result.append(code.toChar())
                            index += 4
                        }
                        else -> result.append(escaped)
                    }
                }
                else -> result.append(char)
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
internal fun Any?.jsonObject(): Map<String, Any?>? = this as? Map<String, Any?>

internal fun Any?.jsonList(): List<Any?> = this as? List<Any?> ?: emptyList()

internal fun Map<String, Any?>.text(key: String): String? = this[key] as? String

internal fun Map<String, Any?>.number(key: String): Double? = this[key] as? Double

internal fun Map<String, Any?>.flag(key: String): Boolean = this[key] as? Boolean ?: false
