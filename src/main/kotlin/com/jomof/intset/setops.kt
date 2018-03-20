package com.jomof.intset

infix fun IntSet.intersect(other: IntSet): IntSet {
    if (size == 0) {
        return this.copy()
    }
    if (other.size == 0) {
        return other.copy()
    }
    val result = intSetOf()
    (this.pages() coiterate other.pages())
            .forEach { (page, left, right) ->
                result.setPage(page, left and right)
            }
    return result
}

infix fun IntSet.union(other: IntSet): IntSet {
    if (size == 0) {
        return other.copy()
    }
    if (other.size == 0) {
        return this.copy()
    }
    val result = intSetOf()
    (this.pages() coiterate other.pages())
            .forEach { (page, left, right) ->
                result.setPage(page, left or right)
            }
    return result
}

infix fun IntSet.minus(other: IntSet): IntSet {
    if (size == 0) {
        return this.copy()
    }
    if (other.size == 0) {
        return this.copy()
    }
    val result = intSetOf()
    (this.pages() coiterate other.pages())
            .forEach { (page, left, right) ->
                val negated = -1L xor right
                val combined = left and negated
                result.setPage(page, combined)
            }
    return result
}

fun IntSet.forEachElement(action: (Int) -> Unit) {
    return top.forEach(action)
}
