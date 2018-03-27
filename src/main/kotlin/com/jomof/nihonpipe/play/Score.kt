package com.jomof.nihonpipe.play

import kotlin.math.max
import kotlin.math.min

data class Score(
        private var correct: Int = 0,
        private var incorrect: Int = 0,
        private var level: Int = 0,
        private var lastCorrect: Long = 0L,
        private var lastAttempt: Long = 0L) {

    fun addFrom(score: Score) {
        correct += score.correct
        incorrect += score.incorrect
        level += level
        lastCorrect = max(lastCorrect, score.lastCorrect)
        lastAttempt = max(lastAttempt, score.lastAttempt)
    }

    fun recordCorrect(time: Long) {
        ++correct
        ++level
        lastCorrect = time
        lastAttempt = time
    }

    fun recordIncorrect(time: Long) {
        ++incorrect
        level = max(0, level - 1)
        lastAttempt = time
    }

    fun level(): Int {
        return level + 1
    }

    fun mezzo(): MezzoScore {
        val ordinal = min((level() - 1) / levelsPerMezzo, MezzoScore.BURNED.ordinal)
        return MezzoScore.values()[ordinal]
    }

    companion object {
        const val levelsPerMezzo = 5
    }
}
