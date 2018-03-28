package com.jomof.nihonpipe.play

import com.jomof.algorithm.getsert
import com.jomof.intset.IntSet
import com.jomof.intset.forEachElement
import com.jomof.intset.intSetOf
import com.jomof.nihonpipe.datafiles.TranslatedSentences

class PlayerCache {
    private val keyScores = mutableMapOf<ScoreCoordinate, Score>()
    val sentencesStudying = intSetOf()
    val sentencesNotStudying = allSentences.copy()
    val keyScoresCovered = intSetOf()
            .withRangeLimit(ScoreCoordinateIndex.rangeOfCoordinates)

    /**
     * Add one sentence to the current user state.
     */
    fun addSentence(sentence: String, score: Score) {
        val sentenceIndex = TranslatedSentences().sentenceToIndex(sentence)
        sentencesStudying += sentenceIndex
        sentencesNotStudying -= sentenceIndex
        val coordinateIndex = ScoreCoordinateIndex()
        val scoreCoordinates = coordinateIndex.getCoordinatesFromSentence(sentenceIndex)
        keyScoresCovered += scoreCoordinates
        scoreCoordinates.forEachElement { scoreCoordinateIndex ->
            val scoreCoordinate = coordinateIndex.getCoordinateFromCoordinateIndex(scoreCoordinateIndex)
            val keyScore = keyScores.getsert(scoreCoordinate) { Score() }
            keyScore.addFrom(score)
        }
    }

    /**
     * Return true if the ScoreCoordinate is covered
     */
    fun containsScoreCoordinate(coordinateIndex: Int): Boolean {
        return keyScoresCovered.contains(coordinateIndex)
    }

    /**
     * Get the current level for each ladder along with the keys that are still
     * missing for that ladder level.
     */
    fun incompleteLadderLevelKeys(): Map<Pair<LadderKind, Int>, List<Pair<String, IntSet>>> {
        val missing = mutableMapOf<
                Pair<LadderKind, Int>,
                List<Pair<String, IntSet>>>()
        LadderKind.values().forEach { ladderKind ->
            for (level in 0 until ladderKind.levelProvider.size) {
                val incompletes = mutableListOf<Pair<String, IntSet>>()
                ladderKind.forEachKeySentence(level) { (key, sentences) ->
                    val coordinate = ScoreCoordinate(ladderKind, level, key)
                    if (!keyScores.contains(coordinate)) {
                        incompletes += Pair(key, sentences)
                    }
                }
                if (incompletes.size > 0) {
                    missing[Pair(ladderKind, level)] = incompletes
                    break
                }
            }
        }
        return missing
    }

    companion object {
        val allSentences = intSetOf(0 until TranslatedSentences().sentences.size)
    }
}