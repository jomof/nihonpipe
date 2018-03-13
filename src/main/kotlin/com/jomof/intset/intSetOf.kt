package com.jomof.intset

fun intSetOf() = IntSet()

fun intSetOf(vararg values: Int): IntSet {
    val result = IntSet()
    values.forEach { result.add(it) }
    return result
}

fun intSetOf(values: Iterable<Int>): IntSet {
    val result = IntSet()
    values.forEach { result.add(it) }
    return result
}