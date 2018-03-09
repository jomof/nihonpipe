package com.jomof.nihonpipe.groveler.schema

import com.jomof.nihonpipe.groveler.bitfield.BitField
import com.jomof.nihonpipe.groveler.bitfield.bitFieldOf

fun <T> Sequence<T>.takeOnly(): T {
    val result = toList()
    require(result.size == 1) {
        var combined = this.joinToString()
        "expected exactly one element but: $combined"
    }
    return result[0]
}

internal fun <T> Sequence<Row<T>>.indexInto(
        map: MutableMap<String, BitField>,
        action: (T) -> String): Sequence<Row<T>> {
    return onEach { (row, value) ->
        val key = action(value)
        val bitField = map[key] ?: bitFieldOf()
        bitField[row] = true
        map[key] = bitField
    }
}

internal fun <T> Sequence<Row<T>>.indexEachInto(
        map: MutableMap<String, BitField>,
        action: (T) -> Iterable<String>): Sequence<Row<T>> {
    return onEach { (row, value) ->
        val keys = action(value)
        keys.forEach { key ->
            val bitField = map[key] ?: bitFieldOf()
            bitField[row] = true
            map[key] = bitField
        }
    }
}
