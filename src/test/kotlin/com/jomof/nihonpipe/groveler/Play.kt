package com.jomof.nihonpipe.groveler

import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import com.jomof.nihonpipe.datafiles.KuromojiIpadicCache
import com.jomof.nihonpipe.datafiles.SentenceSkeletonFilter
import com.jomof.nihonpipe.datafiles.TranslatedSentences
import com.jomof.nihonpipe.datafiles.VocabToSentenceFilter
import com.jomof.nihonpipe.groveler.schema.particleSkeletonForm
import com.jomof.nihonpipe.play.LadderKind
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
        assertThat(coordinates).hasSize(5)
        coordinates.forEach {
            val score = player.getScore(it)
            assertThat(score.attempts()).isEqualTo(1)
            assertThat(score.correct).isEqualTo(1)
        }
    }

    @Test
    fun analyzeSentence() {
        val target = "入口はどこですか。"
        val found =
                TranslatedSentences
                        .sentences
                        .sentences
                        .filter { (key, sentence) ->
                            sentence.japanese == target
                        }
                        .entries
                        .toList()
                        .single()
        println("$found")
        val tokenization = KuromojiIpadicCache
                .tokenize(target)
        println("tokens = ${tokenization.tokens}")
        println("skeleton = ${tokenization.particleSkeletonForm()}")
        val skeletonSentences = SentenceSkeletonFilter
                .filterOf
                .skeletons[tokenization.particleSkeletonForm()]!!
        println("There are ${skeletonSentences.size} sentences with this skeleton")
        val index = found.key
        fun locateInLevel(ladderKind: LadderKind) {
            val provider = ladderKind.levelProvider
            var found = false
            for (level in 0 until provider.size) {
                val keySentences = provider.getKeySentences(level)
                for (keySentence in keySentences) {
                    if (keySentence.sentences.contains(index)) {
                        println("$ladderKind level=$level of ${provider.size} key=${keySentence.key}")
                        found = true
                    }
                }
            }
            if (!found) {
                println("NOT FOUND in $ladderKind")
            }
        }
        for (ladderKind in LadderKind.values()) {
            locateInLevel(ladderKind)
        }

        var player = Player()
        var matchingCoordinates = player.sentenceScoreCoordinates(index)
        println("matching coordinates $matchingCoordinates")

        val vocabToSentenceFilter = VocabToSentenceFilter()

    }

    @Test
    fun iteratePlayer() {
        val player = Player()
        fun totalNoCovered() =
                player.allSentenceScoreCoordinates()
                        .map {
                            player.getScore(it)
                        }.map {
                            if (it.value() > 0) 0 else 1
                        }.sum()

        fun itemsNotCovered() =
                player.allSentenceScoreCoordinates()
                        .filter {
                            player.getScore(it).value() == 0
                        }.joinToString {
                            it.key
                        }

        fun scoreAllCorrect() {
            for (sentence in player.sentencesStudying) {
                player.scoreCorrect(sentence)
            }

            println("total not covered: ${totalNoCovered()}: ${itemsNotCovered()}")
        }

        for (i in 0..2) {
            println("total not covered: ${totalNoCovered()}")
            player.addNewSentencesIfNecessary()
            scoreAllCorrect()
            player.addNewSentencesIfNecessary()
            scoreAllCorrect()
            player.addNewSentencesIfNecessary()
            scoreAllCorrect()
            player.addNewSentencesIfNecessary()
            scoreAllCorrect()
            player.addNewSentencesIfNecessary()
            scoreAllCorrect()
            player.addNewSentencesIfNecessary()
            scoreAllCorrect()
            player.addNewSentencesIfNecessary()
            scoreAllCorrect()
            player.addNewSentencesIfNecessary()
            scoreAllCorrect()
            player.addNewSentencesIfNecessary()
            scoreAllCorrect()
        }
    }
}