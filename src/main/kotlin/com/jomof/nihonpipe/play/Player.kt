package com.jomof.nihonpipe.play

import com.jomof.algorithm.getsert
import com.jomof.intset.IntSet
import com.jomof.intset.forEachElement
import com.jomof.intset.intSetOf
import com.jomof.nihonpipe.datafiles.TranslatedSentences

data class Player(
        private val sentencesStudying: MutableMap<String, Score>) {
    private var keyScores: Map<ScoreCoordinate, Score>

    init {
        keyScores = reconstructScores(sentencesStudying)
    }

    fun addSentence(sentence: String) {
        assert(!sentencesStudying.contains(sentence))
        sentencesStudying[sentence] = Score()
        keyScores = reconstructScores(sentencesStudying)
    }

    private fun existingScoreSet() =
            keyScores.entries.map { it.key }.toSet()

    /**
     * Get the current level for each ladder along with the keys that are still
     * missing for that ladder level.
     */
    fun incompleteLadderLevelKeys(): Map<Pair<LadderKind, Int>, List<Pair<String, IntSet>>> {
        val existingScores = existingScoreSet()
        val missing = mutableMapOf<
                Pair<LadderKind, Int>,
                List<Pair<String, IntSet>>>()
        LadderKind.values().forEach { ladderKind ->
            for (level in 0 until ladderKind.levelProvider.size) {
                val incompletes = mutableListOf<Pair<String, IntSet>>()
                ladderKind.forEachKeySentence(level) { (key, sentences) ->
                    val coordinate = ScoreCoordinate(ladderKind, level, key)
                    if (!existingScores.contains(coordinate)) {
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

    fun findNextSentence(): Pair<Int, List<ScoreCoordinate>> {
        val incompleteLevels = incompleteLadderLevelKeys()
        val ladderToLevel = incompleteLevels.keys.toMap()
        val lowestLevel = ladderToLevel.values.min()!!
        val (ladderLevel, keySentences) = incompleteLevels
                .entries
                .first { (ladderLevel, _) ->
                    val (_, level) = ladderLevel
                    level == lowestLevel
                }
        val keySentence = keySentences.first()

        // We want to find sentences that will increase the total number of
        // scores by the smallest positive value. This represents the least
        // cognitive burden to the learner.
        val leastCognitiveBurdenList = keySentence
                .second
                .map { sentence ->
                    var cognitiveBurden = 0
                    val reasons = mutableListOf<ScoreCoordinate>()
                    LadderKind.forEachSentenceCoordinate(sentence) { scoreCoordinate ->
                        val currentUserLevel = ladderToLevel[scoreCoordinate.ladderKind]!!
                        if (scoreCoordinate.level >= currentUserLevel) {
                            val levelCost = scoreCoordinate.level + 1
                            cognitiveBurden += levelCost * levelCost
                            if (scoreCoordinate.level == currentUserLevel) {
                                reasons += scoreCoordinate
                            }
                        }
                    }
                    Triple(cognitiveBurden, sentence, reasons)
                }
                .filter { it.first > 0 }
                .sortedBy { it.first }
                .groupBy { it.first }
                .toList()
        val leastCognitiveBurden = leastCognitiveBurdenList.first()

        // To break ties, take the shortest
        val shortestList = leastCognitiveBurden
                .second
                .map {
                    Triple(TranslatedSentences().sentences[it.second]!!.japanese.length,
                            it.second, it.third)
                }
                .sortedBy { it.first }
                .groupBy { it.first }
                .toList()
        val shortest = shortestList
                .first()
                .second
                .maxBy { it.second }!!


        return Pair(shortest.second, shortest.third)
    }

    /**
     * Reconstruct scores from recorded sentence scores.
     */
    private fun reconstructScores(
            sentencesStudying: Map<String, Score>): Map<ScoreCoordinate, Score> {
        val keyScores = mutableMapOf<ScoreCoordinate, Score>()
        val studying = intSetOf()

        // Reconstruct keyScores from sentence scores
        val sentenceScoreMap = mutableMapOf<Int, Score>()
        val sentences = TranslatedSentences()
        for ((japanese, sentenceScore) in sentencesStudying) {
            val index = sentences.sentenceToIndex(japanese)!!
            studying += index
            sentenceScoreMap[index] = sentenceScore
        }

        studying.forEachElement { index ->
            LadderKind.forEachSentenceCoordinate(index) { scoreCoordinate ->
                val sentenceScore = sentenceScoreMap[index]!!
                val keyScore = keyScores.getsert(scoreCoordinate) { Score() }
                keyScore.addFrom(sentenceScore)
            }
        }
        return keyScores
    }
}