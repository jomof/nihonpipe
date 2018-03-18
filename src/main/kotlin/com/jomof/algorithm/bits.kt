package com.jomof.algorithm

fun Long.toBitList() : List<Int> {
    val result = mutableListOf<Int>()
    for (i in 0 until 64) {
        val mask = 1L shl i
        if ((this and mask) == mask) {
            result += i
        }
    }
    return result
}