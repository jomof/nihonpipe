package com.jomof.nihonpipe.groveler

import java.io.Serializable

class BitField(val init: String) : Iterable<Pair<Int, Boolean>>, Serializable {
    private class BitFieldIterator(val init: String) : Iterator<Pair<Int, Boolean>> {
        var i = 0
        override fun next(): Pair<Int, Boolean> {
            var num = 0
            while (i != init.length) {
                val c = init[i]
                ++i
                num = when (c) {
                    '-', '+' -> {
                        return Pair(num, c == '+')
                    }
                    else -> (num * 36) + toNum(c)
                }
            }
            throw RuntimeException()
        }

        override fun hasNext(): Boolean {
            return i != init.length
        }

    }

    override fun iterator(): Iterator<Pair<Int, Boolean>> {
        return BitFieldIterator(init)
    }

    override fun toString(): String {
        return init
    }
}

fun createBitField(): BitField {
    return BitField("")
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

fun BitField.forEachSpan(action: (size: Int, set: Boolean) -> Unit) {
    for ((size, set) in this) {
        action(size, set)
    }
}

fun BitField.mapEachSpan(action: (size: Int, set: Boolean) -> BitField): BitField {
    val sb = StringBuilder()
    forEachSpan { size, set ->
        sb.append(action(size, set).init)
    }
    return BitField(sb.toString())
}

fun BitField.size(): Int {
    var result = 0
    forEachSpan { size, _ ->
        result += size
    }
    return result
}

fun BitField.spans(): Int {
    var result = 0
    forEachSpan { size, _ ->
        result++
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

private fun encode(segments: List<Pair<Int, Boolean>>): BitField {
    var last: Boolean? = null
    val sb = StringBuilder()
    var sum = 0
    for ((size, set) in segments) {
        if (size == 0) {
            continue
        }
        if (last == null || last == set) {
            last = set
            sum += size
            continue
        }
        sb.append(encode(sum, last))
        sum = size
        last = set
    }
    if (sum != 0 && last != null && last) {
        sb.append(encode(sum, last))
    }
    return BitField(sb.toString())
}

private fun insert(i: Int, offset: Int, value: Boolean, list: MutableList<Pair<Int, Boolean>>) {
    val (sizei, seti) = list[i]
    var n = i
    if (offset > 0) {
        list[n] = Pair(offset, seti)
        list.add(i + 1, Pair(1, value))
        list.add(i + 2, Pair(sizei - offset - 1, seti))
    } else {
        list[i] = Pair(1, value)
        list.add(i + 1, Pair(sizei - 1, seti))
    }
}

fun BitField?.set(key: Int, value: Boolean): BitField {
    val bitfield = this ?: createBitField()
    val size = bitfield.size()
    if (key >= size) {
        return BitField("${bitfield.init}${encode(key - size + 1, false)}")
                .set(key, value)
    }

    val segments = bitfield.toMutableList()
    var start = 0
    for (i in 0 until segments.size) {
        val (size, _) = segments[i]
        val offset = key - start
        if (offset in 0 until size) {
            insert(i, offset, value, segments)
            return encode(segments)
        }
        start += size
    }
    throw RuntimeException()
}

operator fun BitField.get(key: Int): Boolean {
    var start = 0
    for ((size, set) in this) {
        if (key in start until start + size) {
            return set
        }
        start += size
    }
    return false
}