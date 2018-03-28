package com.jomof.nihonpipe.play

import kotlin.math.max
import kotlin.math.min

data class Score(
        private var correct: Int = 0,
        private var incorrect: Int = 0,
        private var level: Int = 0,
        private var lastCorrect: Long = 0L,
        private var lastAttempt: Long = 0L,
        private var unlocked: Long = 0L,
        private var recentWinStreak: Long = 0L,
        private var recentLoseStreak: Long = 0L,
        private var longestWinStreak: Long = 0L,
        private var longestLoseStreak: Long = 0L) {

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
        ++recentWinStreak
        longestWinStreak = max(recentWinStreak, longestWinStreak)
        recentLoseStreak = 0
        lastCorrect = time
        lastAttempt = time
    }

    fun recordIncorrect(time: Long) {
        ++incorrect
        level = max(0, level - 1)
        ++recentLoseStreak
        longestLoseStreak = max(recentLoseStreak, longestLoseStreak)
        recentWinStreak = 0
        lastAttempt = time
    }

    fun level() = level + 1
    fun recentWinStreak() = recentWinStreak
    fun recentLoseStreak() = recentLoseStreak
    fun longestWinStreak() = longestWinStreak
    fun longestLoseStreak() = longestLoseStreak

    fun mezzo(): MezzoScore {
        val ordinal = min((level() - 1) / levelsPerMezzo, MezzoScore.BURNED.ordinal)
        return MezzoScore.values()[ordinal]
    }

    companion object {
        const val levelsPerMezzo = 5
    }
}
