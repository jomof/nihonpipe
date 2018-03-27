package com.jomof.nihonpipe.play

import com.google.common.truth.Truth
import org.junit.Test

class ScoreTest {
    @Test
    fun testDefaultIsOne() {
        val score = Score()
        Truth.assertThat(score.level()).isEqualTo(1)
    }

    @Test
    fun testBurnedAt20() {
        val score = Score(correct = 19)
        Truth.assertThat(score.level()).isEqualTo(20)
        Truth.assertThat(score.mezzo()).isEqualTo(MezzoScore.ENLIGHTENED)
    }

    @Test
    fun testBurnedAt21() {
        val score = Score(correct = 20)
        Truth.assertThat(score.level()).isEqualTo(21)
        Truth.assertThat(score.mezzo()).isEqualTo(MezzoScore.BURNED)
    }

    @Test
    fun testBurnedAt100() {
        val score = Score(correct = 99)
        Truth.assertThat(score.level()).isEqualTo(100)
        Truth.assertThat(score.mezzo()).isEqualTo(MezzoScore.BURNED)
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
        Truth.assertThat(score.level()).isEqualTo(1)
        Truth.assertThat(score.mezzo()).isEqualTo(MezzoScore.APPRENTICE)
        score.recordCorrect(0)
        score.recordCorrect(0)
        score.recordCorrect(0)
        score.recordCorrect(0)
        score.recordCorrect(0)
        Truth.assertThat(score.level()).isEqualTo(6)
        Truth.assertThat(score.mezzo()).isEqualTo(MezzoScore.GURU)
    }
}