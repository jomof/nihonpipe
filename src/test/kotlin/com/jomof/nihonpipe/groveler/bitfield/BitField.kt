package com.jomof.nihonpipe.groveler.bitfield

import java.io.Serializable

private const val debug = false

data class BitField(var init: String, var size: Int)
    : Iterable<Segment>, Serializable {
    init {
        if (debug) {
            val calculate = calculateBitfieldSize(init)
            assert(size == calculate, { "$size != $calculate" })
        }
    }

    override fun iterator(): Iterator<Segment> = getBitFieldIterator(init)
}

private fun getBitFieldIterator(init: String): Iterator<Segment> {
    var i = 0
    var start = 0
    return object : Iterator<Segment> {
        override fun hasNext() =
                i != init.length

        override fun next(): Segment {
            var num = 0
            while (i != init.length) {
                val c = init[i]
                ++i
                num = when (c) {
                    '-', '+' -> {
                        val result =
                                Segment(start until start + num, c == '+')
                        start += num
                        return result
                    }
                    else -> (num * 36) + toNum(c)
                }
            }
            throw RuntimeException()
        }
    }
}
fun createBitField(): BitField {
    return BitField("", 0)
}

private fun toBaseString(num: Int): String {
    return java.lang.Integer.toString(num, 36)
}

private fun toNum(c: Char): Int {
    return when (c) {
        '0' -> 0
        '1' -> 1
        '2' -> 2
        '3' -> 3
        '4' -> 4
        '5' -> 5
        '6' -> 6
        '7' -> 7
        '8' -> 8
        '9' -> 9
        'a' -> 10
        'b' -> 11
        'c' -> 12
        'd' -> 13
        'e' -> 14
        'f' -> 15
        'g' -> 16
        'h' -> 17
        'i' -> 18
        'j' -> 19
        'k' -> 20
        'l' -> 21
        'm' -> 22
        'n' -> 23
        'o' -> 24
        'p' -> 25
        'q' -> 26
        'r' -> 27
        's' -> 28
        't' -> 29
        'u' -> 30
        'v' -> 31
        'w' -> 32
        'x' -> 33
        'y' -> 34
        'z' -> 35
        else -> throw RuntimeException(c.toString())
    }
}

fun BitField.forEachSpan(action: (span: IntRange, set: Boolean) -> Unit) {
    for ((span, set) in this) {
        action(span, set)
    }
}

fun calculateBitfieldSize(init: String): Int {
    var size = 0
    for ((range) in getBitFieldIterator(init)) {
        size = range.endInclusive + 1
    }
    return size
}

fun BitField.spans(): Int {
    var result = 0
    for (c in init) {
        when (c) {
            '+', '-' -> ++result
        }
    }
    return result
}

private fun encode(size: Int, value: Boolean): String {
    val c = if (value) {
        '+'
    } else {
        '-'
    }
    return "${toBaseString(size)}$c"
}

private fun BitField.encode(segments: List<Segment>) {
    var last: Boolean? = null
    val sb = StringBuilder()
    var sum = 0
    var total = 0
    for ((range, set) in segments) {
        if (range.isEmpty()) {
            continue
        }
        var size = range.endInclusive - range.first + 1
        if (last == null || last == set) {
            last = set
            sum += size
            continue
        }
        sb.append(encode(sum, last))
        total += sum
        last = set
        sum = size
    }
    if (sum != 0 && last != null && last) {
        total += sum
        sb.append(encode(sum, last))
    }

    this.init = sb.toString()
    this.size = total
}

private fun insert(
        index: Int,
        offset: Int,
        value: Boolean, list: MutableList<Segment>) {
    val (range, set) = list[index]
    assert(range.contains(offset)) { "$offset not withing range $range" }

    if (offset != range.start) {
        list[index] = Segment(range.start until offset, set)
        list.add(index + 1, Segment(offset..offset, value))
        list.add(index + 2, Segment(offset + 1 until range.last, set))
    } else {
        list[index] = Segment(range.start until offset, value)
        list.add(index + 1, Segment(offset until range.last, set))
    }
}

operator fun BitField.set(key: Int, value: Boolean) {
    val segments = toMutableList()
    var last = 0
    for (index in 0 until segments.size) {
        val (range) = segments[index]
        if (key in range) {
            insert(index, key, value, segments)
            encode(segments)
            return
        }
        last = range.last + 1
    }
    if (!value) {
        return
    }
    segments.add(Segment(last until key, false))
    segments.add(Segment(key..key, true))
    encode(segments)
    return
}

operator fun BitField.get(key: Int): Boolean {
    for ((range, set) in this) {
        if (key in range) {
            return set
        }
    }
    return false
}
