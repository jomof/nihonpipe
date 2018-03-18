package com.jomof.nihonpipe.play

import kotlin.math.max

data class Score(
        var correct: Int = 0,
        var incorrect: Int = 0) {
    fun value() = correct - max(incorrect, 0)
    fun attempts() = correct + incorrect
}
