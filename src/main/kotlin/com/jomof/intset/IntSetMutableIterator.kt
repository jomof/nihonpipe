package com.jomof.intset

class IntSetMutableIterator(intSet: IntSet) : MutableIterator<Int> {
    var offset = 0
    var page = 0
    var elements = 0L
    var pages = intSet.pages().iterator()
    var lastHasNext: Boolean? = null

    override fun hasNext(): Boolean {
        if (lastHasNext != null) {
            return lastHasNext!!
        }
        lastHasNext = when (elements) {
            0L -> {
                if (pages.hasNext()) {
                    val (page, elements) = pages.next()
                    this.page = page
                    this.elements = elements
                    this.offset = 0
                    hasNext()
                } else {
                    false
                }
            }
            else -> {
                val top = -1L xor (1L shl 63)
                while ((elements and 1L) != 1L) {
                    ++offset
                    elements = elements shr 1
                    elements = elements and top
                    assert(offset < 100L)
                }
                ++offset
                elements = elements shr 1
                true
            }
        }
        return lastHasNext!!
    }

    override fun next(): Int {
        return when (lastHasNext) {
            null -> {
                lastHasNext = hasNext()
                if (!lastHasNext!!) {
                    throw RuntimeException()
                }
                return next()
            }
            false -> throw RuntimeException()
            true -> {
                lastHasNext = null
                page * 64 + (offset - 1)
            }
        }
    }

    override fun remove() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
