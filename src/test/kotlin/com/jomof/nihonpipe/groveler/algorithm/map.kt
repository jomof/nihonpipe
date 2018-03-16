package com.jomof.nihonpipe.groveler.algorithm


fun <K, V> MutableMap<K, V>.getsert(key: K, new: () -> V): V {
    val value = get(key)
    if (value == null) {
        set(key, new())
        return getsert(key, new)
    }
    return value
}