package com.jomof.intset

data class PageRange(val first: Int, val last: Int) {
    init {
        assert(first <= last)
    }

    operator fun contains(value: Int): Boolean {
        return value in first..last
    }

    val count = last - first + 1
}

