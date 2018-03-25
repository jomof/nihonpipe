package com.jomof.algorithm

fun <T, V : Comparable<V>> Iterable<T>.takeMinBy(value: (T) -> V): Iterable<T> {
    var v: V? = null
    return sortedBy(value)
            .takeWhile { it ->
                val current = value(it)
                if (v == null) {
                    v = current
                    true
                } else {
                    v!! == value
                }
            }
}

fun <T, V : Comparable<V>> Iterable<T>.takeMaxBy(value: (T) -> V): Iterable<T> {
    var v: V? = null
    return sortedByDescending(value)
            .takeWhile { it ->
                val current = value(it)
                if (v == null) {
                    v = current
                    true
                } else {
                    v!! == value
                }
            }
}
