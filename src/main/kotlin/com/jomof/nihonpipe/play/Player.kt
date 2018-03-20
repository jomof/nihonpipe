package com.jomof.nihonpipe.play

import com.jomof.algorithm.getsert
import com.jomof.intset.forEachElement
import com.jomof.intset.intSetOf
import com.jomof.nihonpipe.datafiles.TranslatedSentences

data class Player(
        private val sentencesStudying: MutableMap<String, Score>) {
    private val studying = intSetOf()
    private val keyScores = mutableMapOf<ScoreCoordinate, Score>()

    init {
        // Reconstruct keyScores from sentence scores
        val sentenceScoreMap = mutableMapOf<Int, Score>()
        val sentences = TranslatedSentences()
        for ((japanese, sentenceScore) in sentencesStudying) {
            val index = sentences.sentenceToIndex(japanese)!!
            studying += index
            sentenceScoreMap[index] = sentenceScore
        }

        for (ladderKind in LadderKind.values()) {
            for (level in 0 until ladderKind.levelProvider.size) {
                val levelSentences = ladderKind.levelProvider.getLevelSentences(level)
                studying.forEachElement { index ->
                    if (levelSentences.contains(index)) {
                        for ((key, sentences) in ladderKind.levelProvider.getKeySentences(level)) {
                            if (sentences.contains(index)) {
                                val sentenceScore = sentenceScoreMap[index]!!
                                val scoreCoordinate = ScoreCoordinate(ladderKind, level, key)
                                val keyScore = keyScores.getsert(scoreCoordinate) { Score() }
                                keyScore.addFrom(sentenceScore)
                            }
                        }
                    }
                }
            }
        }
    }
}