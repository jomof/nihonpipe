package com.jomof.nihonpipe.groveler.schema

import java.io.Serializable
import kotlin.math.max
import kotlin.math.min

data class Statistics(
        val min: Int = 0,
        val max: Int = 0,
        val count: Int = 0,
        val sum: Int = 0) : Serializable

operator fun Statistics.plus(n: Int): Statistics {
    return Statistics(min(n, min), max(n, max), count + 1, sum + n)
}

fun jlptToInt(jlpt: String) =
        when (jlpt) {
            "JLPT1", "jlpt1" -> 6
            "JLPT2", "jlpt2" -> 5
            "JLPT3", "jlpt3" -> 4
            "JLPT4", "jlpt4" -> 3
            "JLPT5", "jlpt5" -> 2
            "JLPT6", "jlpt6" -> 1
            "JLPT0" -> 0
            else -> throw RuntimeException(jlpt)
        }