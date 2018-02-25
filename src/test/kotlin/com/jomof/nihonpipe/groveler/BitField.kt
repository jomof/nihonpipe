package com.jomof.nihonpipe.groveler

class BitField(val init: String) {
    val size = computeSize()
}

fun createBitField(size: Int): BitField {
    return createBitField(size to false)
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
    var i = 0
    var num = 0
    while (i != init.length) {
        val c = init[i]
        num = when (c) {
            '-', '+' -> {
                action(num, c == '+')
                0
            }
            else -> (num * 36) + toNum(c)
        }
        ++i
    }
}

fun BitField.mapEachSpan(action: (size: Int, set: Boolean) -> BitField): BitField {
    val sb = StringBuilder()
    forEachSpan { size, set ->
        sb.append(action(size, set).init)
    }
    return BitField(sb.toString())
}

fun BitField.computeSize(): Int {
    var result = 0
    forEachSpan { size, set ->
        result += size
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

fun createBitField(vararg vals: Pair<Int, Boolean>): BitField {
    val sb = StringBuilder()
    vals.forEach { (size, set) ->
        if (size != 0) {
            sb.append(encode(size, set))
        }
    }
    return BitField(sb.toString())
}

fun BitField.set(key: Int, value: Boolean): BitField {
    var key = key
    return mapEachSpan { size, set ->
        var current = key
        key -= size
        if (current in 0 until size && value != set) {
            createBitField(
                    current to set,
                    1 to !set,
                    size - current - 1 to set)
        } else {
            createBitField(size to set)
        }
    }
}

operator fun BitField.get(key: Int): Boolean {
    var key = key
    var result = false
    forEachSpan { size, set ->
        var current = key
        key -= size
        if (current in 0 until size) {
            result = set
        }
    }
    return result
}