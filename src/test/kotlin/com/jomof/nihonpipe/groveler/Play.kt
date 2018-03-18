package com.jomof.nihonpipe.groveler

import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import com.jomof.nihonpipe.play.Player
import org.junit.Test

class Play {

    @Test
    fun addNewSentencesIfNecessaryIsIdempotent() {
        val player = Player()
        player.addNewSentencesIfNecessary()
        Truth.assertThat(player.sentencesStudying).hasSize(10)
        player.addNewSentencesIfNecessary()
        Truth.assertThat(player.sentencesStudying).hasSize(10)
    }

    @Test
    fun initialScoreCoordinates() {
        val player = Player()
        val coordinates = player.sentenceScoreCoordinates(50195)
    }

    @Test
    fun scoreSaves() {
        val player = Player()
        player.sentencesStudying += 1160
        player.scoreCorrect(1160)
        val coordinates = player.sentenceScoreCoordinates(1160)
        assertThat(coordinates).hasSize(4)
        coordinates.forEach {
            val score = player.getScore(it)
            assertThat(score.attempts()).isEqualTo(1)
            assertThat(score.correct).isEqualTo(1)
        }
    }

    @Test
    fun iteratePlayer() {
        val player = Player()
        fun scoreAllCorrect() {
            for (sentence in player.sentencesStudying) {
                player.scoreCorrect(sentence)
            }
        }
        player.addNewSentencesIfNecessary()
        scoreAllCorrect()
        Truth.assertThat(player.sentencesStudying).hasSize(10)
        player.addNewSentencesIfNecessary()
        Truth.assertThat(player.sentencesStudying).hasSize(10)
        scoreAllCorrect()
        player.addNewSentencesIfNecessary()
        Truth.assertThat(player.sentencesStudying).hasSize(10)
        scoreAllCorrect()
        player.addNewSentencesIfNecessary()
        Truth.assertThat(player.sentencesStudying).hasSize(11)
        scoreAllCorrect()
        player.addNewSentencesIfNecessary()
        Truth.assertThat(player.sentencesStudying).hasSize(12)
        scoreAllCorrect()
        player.addNewSentencesIfNecessary()
        Truth.assertThat(player.sentencesStudying).hasSize(18)
        scoreAllCorrect()
        player.addNewSentencesIfNecessary()
        Truth.assertThat(player.sentencesStudying).hasSize(23)
        scoreAllCorrect()
        player.addNewSentencesIfNecessary()
        Truth.assertThat(player.sentencesStudying).hasSize(29)
        scoreAllCorrect()
        player.addNewSentencesIfNecessary()
        Truth.assertThat(player.sentencesStudying).hasSize(37)
        scoreAllCorrect()
    }
}