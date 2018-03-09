package com.jomof.nihonpipe.groveler.bitfield

fun getBitFieldSegmentsIterable(init: String): Iterable<Segment> {
    return object : Iterable<Segment> {
        override fun iterator(): Iterator<Segment> {
            return object : Iterator<Segment> {
                var i = 0
                var start = 0
                override fun hasNext() = i != init.length

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
                            else -> baseInsertDigit(num, c)
                        }
                    }
                    throw RuntimeException()
                }
            }
        }
    }
}

private fun encode(size: Int, value: Boolean): String {
    val c = if (value) {
        '+'
    } else {
        '-'
    }
    return "${baseEncode(size)}$c"
}

fun Iterable<Segment>.toBitField(): BitField {
    var last: Boolean? = null
    val sb = StringBuilder()
    var sum = 0
    var total = 0
    for ((range, set) in this) {
        if (range.isEmpty()) {
            continue
        }
        val size = range.endInclusive - range.first + 1
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

    return BitField(sb.toString(), total)
}

fun MutableList<Segment>.insert(
        index: Int,
        offset: Int,
        value: Boolean) {
    val (range, set) = this[index]
    if (set == value) {
        return
    }
    assert(range.contains(offset)) { "$offset not withing range $range" }

    if (offset != range.start) {
        this[index] = Segment(range.start until offset, set)
        add(index + 1, Segment(offset..offset, value))
        add(index + 2, Segment(offset + 1..range.last, set))
    } else {
        this[index] = Segment(range.start..offset, value)
        add(index + 1, Segment(offset until range.last, set))
    }
}

operator fun MutableList<Segment>.set(key: Int, value: Boolean) {
    var last = 0
    for (index in 0 until size) {
        val (range) = this[index]
        if (key in range) {
            insert(index, key, value)
            return
        }
        last = range.last + 1
    }
    if (!value) {
        return
    }
    add(Segment(last until key, false))
    add(Segment(key..key, true))
    return
}

operator fun Iterable<Segment>.get(key: Int): Boolean {
    for ((range, set) in this) {
        if (key in range) {
            return set
        }
    }
    return false
}

fun bitFieldOf(vararg elements: Pair<IntRange, Boolean>): BitField {
    val bf = BitField("", 0)
    elements.forEach { (range, set) ->
        for (i in range) {
            bf[i] = set
        }
    }
    return bf
}


