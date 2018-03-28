package com.jomof.nihonpipe.play

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ScoreTest {
    @Test
    fun testDefaultIsOne() {
        val score = Score()
        assertThat(score.level()).isEqualTo(1)
    }

    @Test
    fun testBurnedAt20() {
        val score = Score(correct = 19)
        (0 until 18).onEach { score.recordCorrect(0) }
        assertThat(score.mezzo()).isEqualTo(MezzoScore.ENLIGHTENED)
    }

    @Test
    fun testBurnedAt21() {
        val score = Score()
        (0 until 20).onEach { score.recordCorrect(0) }
        assertThat(score.level()).isEqualTo(21)
        assertThat(score.mezzo()).isEqualTo(MezzoScore.BURNED)
    }

    @Test
    fun testBurnedAt100() {
        val score = Score()
        (0 until 99).onEach { score.recordCorrect(0) }
        assertThat(score.level()).isEqualTo(100)
        assertThat(score.mezzo()).isEqualTo(MezzoScore.BURNED)
    }

    @Test
    fun lossesForgiven() {
        val score = Score()
        score.recordIncorrect(0)
        score.recordIncorrect(0)
        score.recordIncorrect(0)
        score.recordIncorrect(0)
        score.recordIncorrect(0)
        score.recordIncorrect(0)
        score.recordIncorrect(0)
        score.recordIncorrect(0)
        score.recordIncorrect(0)
        score.recordIncorrect(0)
        assertThat(score.level()).isEqualTo(1)
        assertThat(score.mezzo()).isEqualTo(MezzoScore.APPRENTICE)
        score.recordCorrect(0)
        score.recordCorrect(0)
        score.recordCorrect(0)
        score.recordCorrect(0)
        score.recordCorrect(0)
        assertThat(score.level()).isEqualTo(6)
        assertThat(score.mezzo()).isEqualTo(MezzoScore.GURU)
    }

    @Test
    fun recentStreak() {
        val score = Score()
        score.recordCorrect(0)
        assertThat(score.recentWinStreak()).isEqualTo(1)
        assertThat(score.recentLoseStreak()).isEqualTo(0)
        score.recordCorrect(0)
        assertThat(score.recentWinStreak()).isEqualTo(2)
        assertThat(score.recentLoseStreak()).isEqualTo(0)
        score.recordIncorrect(0)
        assertThat(score.recentWinStreak()).isEqualTo(0)
        assertThat(score.longestWinStreak()).isEqualTo(2)
        assertThat(score.recentLoseStreak()).isEqualTo(1)
        score.recordIncorrect(0)
        score.recordCorrect(0)
        assertThat(score.longestLoseStreak()).isEqualTo(2)
        assertThat(score.longestWinStreak()).isEqualTo(2)
    }
}