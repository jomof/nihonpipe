package com.jomof.algorithm

fun <T> List<T>.permutations(): List<List<T>> {
    if (size == 1) return listOf(this)
    val perms = mutableListOf<List<T>>()
    val toInsert = get(0)
    for (perm in drop(1).permutations()) {
        for (i in 0..perm.size) {
            val newPerm = perm.toMutableList()
            newPerm.add(i, toInsert)
            perms.add(newPerm)
        }
    }
    return perms
}