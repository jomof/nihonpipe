package com.jomof.nihonpipe.play

const val square = 12.598263
const val linear = -59.161105
const val constant = 69.429158

/**
 * Calculate the number of hours to go f
 */
fun hoursUntilNextSrsLevel(level: Int): Double {
    assert(level >= 1) { "should be one-relative" }
    return hoursUntilSrsLevel(level + 1) - hoursUntilSrsLevel(level)
}

/**
 * Total number of hours to reach the given level.
 */
fun hoursUntilSrsLevel(level: Int): Double {
    assert(level >= 1) { "should be one-relative" }
    if (level == 1) {
        return 0.0
    }
    val p2 = level * level * square
    val p1 = level * linear
    val p0 = constant
    return p2 + p1 + p0
}