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
        reconstructScores()
    }

    private fun existingScoreMap() =
            keyScores
                    .entries
                    .map { (coordinate, score) ->
                        Pair(coordinate, score)
                    }
                    .toMap()

    fun incompleteLadderLevelKeys(): Map<Pair<LadderKind, Int>, List<String>> {
        val existingScores = existingScoreMap()
        val missing = mutableMapOf<Pair<LadderKind, Int>, List<String>>()
        LadderKind.values().forEach { ladderKind ->
            for (level in 0 until ladderKind.levelProvider.size) {
                val incompletes = mutableListOf<String>()
                for ((key, sentence) in ladderKind.levelProvider.getKeySentences(level)) {
                    val coordinate = ScoreCoordinate(ladderKind, level, key)
                    if (!existingScores.containsKey(coordinate)) {
                        incompletes += key
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

    fun reportMezzoLevels() {
        keyScores
                .entries
                .map { (coordinate, score) ->
                    Pair(coordinate, coordinate.toMezzoLevel(score.value()))
                }
                .groupBy { (coordinate, mezzo) ->
                    Pair(coordinate.ladderKind, mezzo)
                }
                .toList()
                .sortedBy { it.first.toString() }
                .map { (key, group) ->
                    println("$key - ${group.size}")
                }

    }

    /**
     * Reconstruct scores from recorded sentence scores.
     */
    private fun reconstructScores() {
        keyScores.clear()
        studying.clear()

        // Reconstruct keyScores from sentence scores
        val sentenceScoreMap = mutableMapOf<Int, Score>()
        val sentences = TranslatedSentences()
        for ((japanese, sentenceScore) in sentencesStudying) {
            val index = sentences.sentenceToIndex(japanese)!!
            studying += index
            sentenceScoreMap[index] = sentenceScore
        }

        LadderKind.forEachLadderLevel { ladderKind, level ->
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