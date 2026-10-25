// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.infrastructures

/*
 * Backup format: one `INSERT IGNORE INTO Table (columns) VALUES (values);` statement per line.
 * Values are NULL, integers, real numbers or quoted strings; strings escape `\`, `'` and line breaks with `\`.
 */

/** One parsed statement: column name to value (null, [Long], [Double] or [String]). */
data class SqlInsert(val table: String, val values: Map<String, Any?>)

class SqlDumpException(lineNumber: Int, reason: String) : Exception("Line $lineNumber: $reason")

fun sqlInsert(table: String, values: List<Pair<String, Any?>>): String =
    "INSERT IGNORE INTO $table (${values.joinToString(", ") { it.first }}) " +
        "VALUES (${values.joinToString(", ") { sqlLiteral(it.second) }});"

private fun sqlLiteral(value: Any?): String = when (value) {
    null -> "NULL"
    is Long, is Int -> value.toString()
    is Double -> value.toString()
    is String -> buildString {
        append('\'')
        value.forEach { char ->
            when (char) {
                '\\' -> append("\\\\")
                '\'' -> append("\\'")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> append(char)
            }
        }
        append('\'')
    }
    else -> error("Unsupported SQL value: $value")
}

private val insertPattern = Regex(
    """^INSERT\s+(?:OR\s+)?IGNORE\s+INTO\s+(\w+)\s*\(([^)]*)\)\s*VALUES\s*\((.*)\)\s*;?$""",
    RegexOption.IGNORE_CASE,
)

/** Statements of [sql]; empty lines and `--` comments are skipped. */
fun parseSqlDump(sql: String): List<SqlInsert> =
    sql.lineSequence().mapIndexedNotNull { index, rawLine ->
        val line = rawLine.trim()
        if (line.isEmpty() || line.startsWith("--")) return@mapIndexedNotNull null
        val lineNumber = index + 1
        val match = insertPattern.matchEntire(line)
            ?: throw SqlDumpException(lineNumber, "expected INSERT IGNORE statement")
        val (table, columnList, valueList) = match.destructured
        val columns = columnList.split(',').map { it.trim() }
        val values = try {
            parseValues(valueList)
        } catch (error: IllegalArgumentException) {
            throw SqlDumpException(lineNumber, error.message ?: "invalid values")
        }
        if (columns.size != values.size) {
            throw SqlDumpException(lineNumber, "${columns.size} columns, but ${values.size} values")
        }
        SqlInsert(table, columns.zip(values).toMap())
    }.toList()

private fun parseValues(text: String): List<Any?> {
    val values = mutableListOf<Any?>()
    var index = 0
    fun skipSpaces() {
        while (index < text.length && text[index].isWhitespace()) index++
    }
    while (true) {
        skipSpaces()
        require(index < text.length) { "missing value" }
        if (text[index] == '\'') {
            val value = StringBuilder()
            index++
            while (true) {
                require(index < text.length) { "unterminated string" }
                val char = text[index++]
                when {
                    char == '\\' && index < text.length -> value.append(
                        when (val escaped = text[index++]) {
                            'n' -> '\n'
                            'r' -> '\r'
                            't' -> '\t'
                            '0' -> '\u0000'
                            else -> escaped
                        },
                    )
                    // SQL standard escape of a quote: ''
                    char == '\'' && index < text.length && text[index] == '\'' -> {
                        value.append('\'')
                        index++
                    }
                    char == '\'' -> break
                    else -> value.append(char)
                }
            }
            values += value.toString()
        } else {
            val start = index
            while (index < text.length && text[index] != ',' && !text[index].isWhitespace()) index++
            val token = text.substring(start, index)
            values += when {
                token.equals("NULL", ignoreCase = true) -> null
                else -> token.toLongOrNull()
                    ?: token.toDoubleOrNull()?.takeIf { it.isFinite() }
                    ?: throw IllegalArgumentException("invalid value: $token")
            }
        }
        skipSpaces()
        if (index == text.length) return values
        require(text[index] == ',') { "expected ',' at position ${index + 1}" }
        index++
    }
}
