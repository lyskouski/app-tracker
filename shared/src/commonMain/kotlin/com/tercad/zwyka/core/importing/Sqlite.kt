// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.core.importing

/*
 * Read-only access to the tables of an SQLite 3 database file (https://www.sqlite.org/fileformat.html),
 * as the other apps export it: no database driver opens a file of bytes on every platform.
 */

internal fun isSqlite(bytes: ByteArray): Boolean =
    bytes.size >= 100 && bytes.decodeToString(0, 15) == "SQLite format 3"

class SqliteFile(private val bytes: ByteArray) {
    init {
        require(isSqlite(bytes)) { "Not an SQLite database" }
    }

    private val pageSize: Int = bytes.uint(16, 2).toInt().let { if (it == 1) 65536 else it }
    private val usableSize: Int = pageSize - (bytes[20].toInt() and 0xFF)
    private val encoding: Int = bytes.uint(56, 4).toInt()

    private val schema: List<Map<String, Any?>> by lazy {
        rows(1).map { (_, values) ->
            mapOf("type" to values[0], "name" to values[1], "rootpage" to values[3], "sql" to values[4])
        }
    }

    fun hasTable(name: String): Boolean = tableEntry(name) != null

    /** Rows of the table [name]: lowercase column name to value (null, Long, Double, String or ByteArray). */
    fun table(name: String): List<Map<String, Any?>> {
        val entry = tableEntry(name) ?: throw IllegalArgumentException("SQLite: no table $name")
        val columns = columnsOf(entry["sql"] as String)
        val rootPage = (entry["rootpage"] as Long).toInt()
        return rows(rootPage).map { (rowId, values) ->
            columns.mapIndexed { index, column ->
                // An INTEGER PRIMARY KEY is the row id, stored as NULL in the record.
                val value = values.getOrNull(index)
                column.name.lowercase() to if (value == null && column.isRowId) rowId else value
            }.toMap()
        }
    }

    private fun tableEntry(name: String): Map<String, Any?>? =
        schema.firstOrNull { it["type"] == "table" && (it["name"] as? String).equals(name, ignoreCase = true) }

    private class Column(val name: String, val isRowId: Boolean)

    /** Columns of a `CREATE TABLE` statement (it keeps the columns added by `ALTER TABLE`). */
    private fun columnsOf(sql: String): List<Column> {
        val body = sql.substring(sql.indexOf('(') + 1, sql.lastIndexOf(')'))
        val definitions = mutableListOf<String>()
        var depth = 0
        var start = 0
        body.forEachIndexed { index, char ->
            when (char) {
                '(' -> depth++
                ')' -> depth--
                ',' -> if (depth == 0) {
                    definitions += body.substring(start, index)
                    start = index + 1
                }
            }
        }
        definitions += body.substring(start)
        return definitions.map { it.trim() }
            .filter { definition ->
                val word = definition.substringBefore(' ').uppercase()
                word !in setOf("PRIMARY", "UNIQUE", "CHECK", "FOREIGN", "CONSTRAINT")
            }
            .map { definition ->
                val name = definition.substringBefore(' ').trim('"', '`', '[', ']')
                val type = definition.substringAfter(' ', "").uppercase()
                Column(name, type.startsWith("INTEGER") && "PRIMARY KEY" in type)
            }
    }

    /** Rows of the table b-tree at [page]: row id with the record values. */
    private fun rows(page: Int): List<Pair<Long, List<Any?>>> {
        val result = mutableListOf<Pair<Long, List<Any?>>>()
        val pages = ArrayDeque(listOf(page))
        val visited = mutableSetOf<Int>()
        while (pages.isNotEmpty()) {
            val number = pages.removeFirst()
            require(visited.add(number)) { "SQLite: page loop" }
            val start = (number - 1) * pageSize
            val header = if (number == 1) start + 100 else start
            require(header < bytes.size) { "SQLite: missing page $number" }
            val cells = bytes.uint(header + 3, 2).toInt()
            when (bytes[header].toInt()) {
                INTERIOR_TABLE -> {
                    val children = (0 until cells).map { cell ->
                        bytes.uint(start + bytes.uint(header + 12 + cell * 2, 2).toInt(), 4).toInt()
                    }
                    pages.addAll(0, children + bytes.uint(header + 8, 4).toInt())
                }
                LEAF_TABLE -> for (cell in 0 until cells) {
                    var offset = start + bytes.uint(header + 8 + cell * 2, 2).toInt()
                    val (payloadSize, sizeLength) = bytes.varint(offset)
                    offset += sizeLength
                    val (rowId, idLength) = bytes.varint(offset)
                    offset += idLength
                    result += rowId to record(payload(offset, payloadSize.toInt()))
                }
                else -> throw IllegalArgumentException("SQLite: unexpected page type of $number")
            }
        }
        return result
    }

    /** Payload of a cell, with its overflow pages. */
    private fun payload(offset: Int, size: Int): ByteArray {
        val maxLocal = usableSize - 35
        if (size <= maxLocal) return bytes.copyOfRange(offset, offset + size)
        val minLocal = (usableSize - 12) * 32 / 255 - 23
        val local = (minLocal + (size - minLocal) % (usableSize - 4)).let { if (it <= maxLocal) it else minLocal }
        val result = ByteArray(size)
        bytes.copyInto(result, 0, offset, offset + local)
        var filled = local
        var overflow = bytes.uint(offset + local, 4).toInt()
        while (filled < size) {
            require(overflow > 0) { "SQLite: missing overflow page" }
            val start = (overflow - 1) * pageSize
            val length = minOf(usableSize - 4, size - filled)
            bytes.copyInto(result, filled, start + 4, start + 4 + length)
            filled += length
            overflow = bytes.uint(start, 4).toInt()
        }
        return result
    }

    private fun record(payload: ByteArray): List<Any?> {
        val (headerSize, headerLength) = payload.varint(0)
        val types = mutableListOf<Long>()
        var offset = headerLength
        while (offset < headerSize) {
            val (type, length) = payload.varint(offset)
            types += type
            offset += length
        }
        return types.map { type ->
            val size = when {
                type in 1L..4L -> type.toInt()
                type == 5L -> 6
                type == 6L || type == 7L -> 8
                type >= 12 -> ((type - 12) / 2).toInt()
                else -> 0
            }
            val value: Any? = when {
                type == 0L -> null
                type in 1L..6L -> payload.int(offset, size)
                type == 7L -> Double.fromBits(payload.int(offset, 8))
                type == 8L -> 0L
                type == 9L -> 1L
                type >= 12 && type % 2 == 0L -> payload.copyOfRange(offset, offset + size)
                type >= 13 -> text(payload, offset, size)
                else -> throw IllegalArgumentException("SQLite: invalid serial type $type")
            }
            offset += size
            value
        }
    }

    private fun text(payload: ByteArray, offset: Int, size: Int): String = when (encoding) {
        2, 3 -> buildString {
            for (i in offset until offset + size - 1 step 2) {
                val low = payload[i + if (encoding == 2) 0 else 1].toInt() and 0xFF
                val high = payload[i + if (encoding == 2) 1 else 0].toInt() and 0xFF
                append(((high shl 8) or low).toChar())
            }
        }
        else -> payload.decodeToString(offset, offset + size)
    }

    private companion object {
        const val INTERIOR_TABLE = 0x05
        const val LEAF_TABLE = 0x0D
    }
}

/** Big-endian unsigned integer of [size] bytes. */
private fun ByteArray.uint(at: Int, size: Int): Long {
    var value = 0L
    for (i in at until at + size) value = (value shl 8) or (this[i].toLong() and 0xFF)
    return value
}

/** Big-endian two's complement integer of [size] bytes. */
private fun ByteArray.int(at: Int, size: Int): Long {
    val value = uint(at, size)
    val shift = 64 - size * 8
    return (value shl shift) shr shift
}

/** SQLite variable-length integer at [at]: value with its length in bytes. */
private fun ByteArray.varint(at: Int): Pair<Long, Int> {
    var value = 0L
    for (i in 0 until 8) {
        val byte = this[at + i].toInt() and 0xFF
        value = (value shl 7) or (byte and 0x7F).toLong()
        if (byte < 0x80) return value to i + 1
    }
    return ((value shl 8) or (this[at + 8].toLong() and 0xFF)) to 9
}
