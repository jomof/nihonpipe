package com.jomof.nihonpipe.groveler.schema

fun <T> Sequence<T>.takeOnly(): T {
    val result = toList()
    require(result.size == 1) {
        var combined = this.joinToString()
        "expected exactly one element but: $combined"
    }
    return result[0]
}