package com.jomof.algorithm


/**
 * Get a value from the map. If not present then insert the result
 * of calling new()
 */
fun <K, V> MutableMap<K, V>.getsert(key: K, new: () -> V): V {
    val value = get(key)
    if (value == null) {
        set(key, new())
        return getsert(key, new)
    }
    return value
}
