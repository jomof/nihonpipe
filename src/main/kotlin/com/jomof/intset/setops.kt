package com.jomof.intset

infix fun IntSet.intersect(other: IntSet): IntSet {
    val result = intSetOf()
    (this.pages() coiterate other.pages())
            .forEach { (page, left, right) ->
                result.setPage(page, left and right)
            }
    return result
}

infix fun IntSet.minus(other: IntSet): IntSet {
    val result = intSetOf()
    (this.pages() coiterate other.pages())
            .forEach { (page, left, right) ->
                val negated = -1L xor right
                val combined = left and negated
                result.setPage(page, combined)
            }
    return result
}
