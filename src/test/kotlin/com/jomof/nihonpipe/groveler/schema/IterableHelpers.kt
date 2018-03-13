package com.jomof.nihonpipe.groveler.schema

import com.jomof.intset.IntSet
import com.jomof.intset.intSetOf

fun <T> Sequence<T>.takeOnly(): T {
    val result = toList()
    require(result.size == 1) {
        val combined = this.joinToString()
        "expected exactly one element but: $combined"
    }
    return result[0]
}

internal fun <K, T> Sequence<Row<T>>.indexInto(
        map: MutableMap<K, IntSet>,
        action: (T) -> K): Sequence<Row<T>> {
    return onEach { (row, value) ->
        val key = action(value)
        val bitField = map[key] ?: intSetOf()
        bitField += row
        map[key] = bitField
    }
}

internal fun <T> Sequence<Row<T>>.indexEachInto(
        map: MutableMap<String, IntSet>,
        action: (T) -> Iterable<String>): Sequence<Row<T>> {
    return onEach { (row, value) ->
        val keys = action(value)
        keys.forEach { key ->
            val bitField = map[key] ?: intSetOf()
            bitField += row
            map[key] = bitField
        }
    }
}
