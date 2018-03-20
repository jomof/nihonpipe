package com.jomof.nihonpipe.play

import kotlin.math.max

data class Score(
        private var correct: Int = 0,
        private var incorrect: Int = 0) {
    fun value() = correct - max(incorrect, 0)
    fun attempts() = correct + incorrect
    fun addFrom(score: Score) {
        correct += score.correct
        incorrect += score.incorrect
    }
}
