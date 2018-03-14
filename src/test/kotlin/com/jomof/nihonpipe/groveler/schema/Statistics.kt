package com.jomof.nihonpipe.groveler.schema

import java.io.Serializable
import kotlin.math.max
import kotlin.math.min

data class Statistics(
        val min: Int = Int.MAX_VALUE,
        val max: Int = Int.MIN_VALUE,
        val count: Int = 0,
        val sum: Int = 0) : Serializable

operator fun Statistics.plus(n: Int): Statistics {
    return Statistics(min(n, min), max(n, max), count + 1, sum + n)
}