// Copyright 2026 The terCAD team. All rights reserved.
// Use of this source code is governed by a CC BY-NC-ND 4.0 license that can be found in the LICENSE file.

package com.tercad.zwyka.core.importing

/*
 * ZIP archive reader: stored and deflated (RFC 1951) entries, without encryption and ZIP64.
 */

internal fun isZip(bytes: ByteArray): Boolean =
    bytes.size >= 4 && bytes.int32le(0) == LOCAL_HEADER

/** Files of the archive [bytes]: path to content; directories are left out. */
fun readZip(bytes: ByteArray): Map<String, ByteArray> {
    val end = (bytes.size - 22 downTo maxOf(0, bytes.size - 22 - 0xFFFF))
        .firstOrNull { bytes.int32le(it) == END_OF_DIRECTORY }
        ?: throw IllegalArgumentException("ZIP: no central directory")
    val count = bytes.int16le(end + 10)
    var offset = bytes.int32le(end + 16)
    val files = LinkedHashMap<String, ByteArray>()
    repeat(count) {
        require(bytes.int32le(offset) == DIRECTORY_HEADER) { "ZIP: broken central directory" }
        val method = bytes.int16le(offset + 10)
        val compressedSize = bytes.int32le(offset + 20)
        val size = bytes.int32le(offset + 24)
        val nameLength = bytes.int16le(offset + 28)
        val extraLength = bytes.int16le(offset + 30)
        val commentLength = bytes.int16le(offset + 32)
        val localOffset = bytes.int32le(offset + 42)
        val name = bytes.decodeToString(offset + 46, offset + 46 + nameLength)
        offset += 46 + nameLength + extraLength + commentLength

        if (name.endsWith('/')) return@repeat
        require(bytes.int32le(localOffset) == LOCAL_HEADER) { "ZIP: broken entry $name" }
        val start = localOffset + 30 + bytes.int16le(localOffset + 26) + bytes.int16le(localOffset + 28)
        val data = bytes.copyOfRange(start, start + compressedSize)
        files[name] = when (method) {
            0 -> data
            8 -> inflate(data, size)
            else -> throw IllegalArgumentException("ZIP: unsupported compression $method of $name")
        }
    }
    return files
}

private const val LOCAL_HEADER = 0x04034b50
private const val DIRECTORY_HEADER = 0x02014b50
private const val END_OF_DIRECTORY = 0x06054b50

private fun ByteArray.int16le(at: Int): Int = (this[at].toInt() and 0xFF) or ((this[at + 1].toInt() and 0xFF) shl 8)

private fun ByteArray.int32le(at: Int): Int = int16le(at) or (int16le(at + 2) shl 16)

/** Raw DEFLATE data (RFC 1951) of [data]; [sizeHint] is the expected size. */
fun inflate(data: ByteArray, sizeHint: Int = data.size * 4): ByteArray = Inflater(data, sizeHint).run()

private class Inflater(private val input: ByteArray, sizeHint: Int) {
    private var output = ByteArray(maxOf(sizeHint, 64))
    private var outputSize = 0
    private var position = 0
    private var bitBuffer = 0
    private var bitCount = 0

    fun run(): ByteArray {
        do {
            val last = bits(1) == 1
            when (bits(2)) {
                0 -> stored()
                1 -> codes(FIXED_LENGTHS, FIXED_DISTANCES)
                2 -> dynamic()
                else -> throw IllegalArgumentException("DEFLATE: invalid block type")
            }
        } while (!last)
        return output.copyOf(outputSize)
    }

    private fun bits(count: Int): Int {
        while (bitCount < count) {
            require(position < input.size) { "DEFLATE: unexpected end of data" }
            bitBuffer = bitBuffer or ((input[position++].toInt() and 0xFF) shl bitCount)
            bitCount += 8
        }
        val value = bitBuffer and ((1 shl count) - 1)
        bitBuffer = bitBuffer ushr count
        bitCount -= count
        return value
    }

    private fun put(byte: Byte) {
        if (outputSize == output.size) output = output.copyOf(output.size * 2)
        output[outputSize++] = byte
    }

    private fun stored() {
        bitBuffer = 0
        bitCount = 0
        require(position + 4 <= input.size) { "DEFLATE: unexpected end of data" }
        val length = (input[position].toInt() and 0xFF) or ((input[position + 1].toInt() and 0xFF) shl 8)
        position += 4
        require(position + length <= input.size) { "DEFLATE: unexpected end of data" }
        repeat(length) { put(input[position++]) }
    }

    private fun decode(huffman: Huffman): Int {
        var code = 0
        var first = 0
        var index = 0
        for (length in 1..MAX_BITS) {
            code = code or bits(1)
            val count = huffman.counts[length]
            if (code - count < first) return huffman.symbols[index + (code - first)]
            index += count
            first = (first + count) shl 1
            code = code shl 1
        }
        throw IllegalArgumentException("DEFLATE: invalid code")
    }

    private fun codes(lengths: Huffman, distances: Huffman) {
        while (true) {
            val symbol = decode(lengths)
            when {
                symbol < 256 -> put(symbol.toByte())
                symbol == 256 -> return
                else -> {
                    val lengthIndex = symbol - 257
                    require(lengthIndex < LENGTH_BASE.size) { "DEFLATE: invalid length" }
                    val length = LENGTH_BASE[lengthIndex] + bits(LENGTH_EXTRA[lengthIndex])
                    val distanceIndex = decode(distances)
                    require(distanceIndex < DISTANCE_BASE.size) { "DEFLATE: invalid distance" }
                    val distance = DISTANCE_BASE[distanceIndex] + bits(DISTANCE_EXTRA[distanceIndex])
                    require(distance <= outputSize) { "DEFLATE: distance too far" }
                    repeat(length) { put(output[outputSize - distance]) }
                }
            }
        }
    }

    private fun dynamic() {
        val lengthCount = bits(5) + 257
        val distanceCount = bits(5) + 1
        val codeCount = bits(4) + 4
        val codeLengths = IntArray(19)
        for (i in 0 until codeCount) codeLengths[CODE_LENGTH_ORDER[i]] = bits(3)
        val codeHuffman = Huffman(codeLengths)
        val lengths = IntArray(lengthCount + distanceCount)
        var index = 0
        while (index < lengths.size) {
            val symbol = decode(codeHuffman)
            if (symbol < 16) {
                lengths[index++] = symbol
                continue
            }
            val (value, repeat) = when (symbol) {
                16 -> {
                    require(index > 0) { "DEFLATE: repeat without a length" }
                    lengths[index - 1] to 3 + bits(2)
                }
                17 -> 0 to 3 + bits(3)
                else -> 0 to 11 + bits(7)
            }
            require(index + repeat <= lengths.size) { "DEFLATE: too many lengths" }
            repeat(repeat) { lengths[index++] = value }
        }
        codes(Huffman(lengths.copyOfRange(0, lengthCount)), Huffman(lengths.copyOfRange(lengthCount, lengths.size)))
    }

    /** Canonical Huffman code: number of codes per bit length, and the symbols ordered by code. */
    class Huffman(lengths: IntArray) {
        val counts = IntArray(MAX_BITS + 1)
        val symbols = IntArray(lengths.size)

        init {
            lengths.forEach { counts[it]++ }
            counts[0] = 0
            val offsets = IntArray(MAX_BITS + 2)
            for (length in 1..MAX_BITS) offsets[length + 1] = offsets[length] + counts[length]
            lengths.forEachIndexed { symbol, length ->
                if (length != 0) symbols[offsets[length]++] = symbol
            }
        }
    }

    companion object {
        const val MAX_BITS = 15
        val LENGTH_BASE = intArrayOf(
            3, 4, 5, 6, 7, 8, 9, 10, 11, 13, 15, 17, 19, 23, 27, 31,
            35, 43, 51, 59, 67, 83, 99, 115, 131, 163, 195, 227, 258,
        )
        val LENGTH_EXTRA = intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 0)
        val DISTANCE_BASE = intArrayOf(
            1, 2, 3, 4, 5, 7, 9, 13, 17, 25, 33, 49, 65, 97, 129, 193,
            257, 385, 513, 769, 1025, 1537, 2049, 3073, 4097, 6145, 8193, 12289, 16385, 24577,
        )
        val DISTANCE_EXTRA = intArrayOf(
            0, 0, 0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7, 8, 8, 9, 9, 10, 10, 11, 11, 12, 12, 13, 13,
        )
        val CODE_LENGTH_ORDER = intArrayOf(16, 17, 18, 0, 8, 7, 9, 6, 10, 5, 11, 4, 12, 3, 13, 2, 14, 1, 15)
        val FIXED_LENGTHS = Huffman(IntArray(288) { symbol ->
            when {
                symbol < 144 -> 8
                symbol < 256 -> 9
                symbol < 280 -> 7
                else -> 8
            }
        })
        val FIXED_DISTANCES = Huffman(IntArray(30) { 5 })
    }
}
