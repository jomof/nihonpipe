package com.jomof.intset

data class PageRange(val first: Int, val last: Int) {
    init {
        assert(first <= last)
    }

    operator fun contains(value: Int): Boolean {
        return value in first..last
    }

    val count = last - first + 1

    override fun toString() = "$first..$last"
}

infix fun PageRange.adjacent(other : PageRange) =
        this.last == other.first - 1

infix fun PageRange.union(other : PageRange) : PageRange {
    assert(this adjacent other)
    return PageRange(first, other.last)
}

infix fun PageRange.extend(add : Int) : PageRange {
    return copy(last = last + add)
}
