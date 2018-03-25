package com.jomof.nihonpipe.play

import com.jomof.algorithm.getsert
import com.jomof.algorithm.takeMaxBy
import com.jomof.algorithm.takeMinBy
import com.jomof.intset.IntSet
import com.jomof.intset.forEachElement
import com.jomof.intset.intSetOf
import com.jomof.intset.minus
import com.jomof.nihonpipe.datafiles.TranslatedSentences
import kotlin.coroutines.experimental.buildSequence

data class Player(
        private val sentencesStudying: MutableMap<String, Score>) {
    private val keyScores = mutableMapOf<ScoreCoordinate, Score>()
    private val studying = intSetOf()
    val coordinates = intSetOf()

    init {
        reconstructScores()
    }

    fun addSentence(sentence: String) {
        assert(!sentencesStudying.contains(sentence))
        sentencesStudying[sentence] = Score()
        reconstructScores()
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

    fun <T> Iterable<T>.elseUse(alternate: () -> Iterable<T>): Sequence<T> {
        var saw = false
        return buildSequence {
            forEach {
                saw = true
                yield(it)
            }
            if (!saw) {
                yieldAll(alternate())
            }
        }
    }

    fun <T> Sequence<T>.elseUse(alternate: () -> Iterable<T>): Sequence<T> {
        var saw = false
        return buildSequence {
            forEach {
                saw = true
                yield(it)
            }
            if (!saw) {
                yieldAll(alternate())
            }
        }
    }

    fun addOneSentence(): SentenceRank {
        val sentences = TranslatedSentences().sentences
        val keySentence =
                when (studying.size % 5) {
                    0 ->
                        (0 until sentences.size)
                    else ->
                        sentencesOfLowestLevels(
                                LadderKind.WANIKANI_VOCAB_LADDER,
                                LadderKind.JLPT_VOCAB_LADDER).flatten()

                }


        // We want to find sentences that will increase the total number of
        // scores by the smallest positive value. This represents the least
        // cognitive burden to the learner.
        var leastBurdenSeen = Int.MAX_VALUE
        val (marginalBurden, sentenceIndex, scoreCoordinates) = keySentence
                .filter { !studying.contains(it) }
                .mapNotNull { sentence ->
                    val cognitiveBurden = burdenOf(sentence)
                    if (cognitiveBurden > leastBurdenSeen) {
                        null
                    } else {
                        val coordinateIndex = ScoreCoordinateIndex()
                        val allReasons = coordinateIndex.sentences[sentence]
                        val marginalReasons = allReasons minus coordinates
                        if (!marginalReasons.isEmpty()) {
                            if (cognitiveBurden != 0 && cognitiveBurden < leastBurdenSeen) {
                                leastBurdenSeen = cognitiveBurden
                            }
                            if (cognitiveBurden == leastBurdenSeen) {
                                Triple(cognitiveBurden, sentence, marginalReasons)
                            } else {
                                null
                            }
                        } else {
                            null
                        }
                    }
                }
                .filter { it.first > 0 }
                .asIterable()
                .takeMinBy { it.first } // Take the least burden
                .takeMinBy { it.third.size } // Take by fewest coordinates
                .takeMinBy { sentences[it.second]!!.japanese.length } // Take by length
                .takeMaxBy { it.second } // Take the last sentence
                .single()
        val translatedSentence = sentences[sentenceIndex]!!
        addSentence(translatedSentence.japanese)
        return SentenceRank(
                rank = sentencesStudying.size,
                sentenceIndex = sentenceIndex,
                marginalScoreCoordinates = scoreCoordinates,
                marginalBurden = marginalBurden)
    }


    private fun sentencesOfLowestLevels(vararg ladders: LadderKind): List<IntSet> {
        val incompleteLevels = incompleteLadderLevelKeys()

        val ladderToLevel = incompleteLevels.keys
                .filter { (ladder, _) -> ladders.contains(ladder) }
                .toMap()
        val lowestLevel = ladderToLevel.values.min()!!
        return incompleteLevels
                .entries
                .filter { (ladderLevel, _) ->
                    val (ladder, level) = ladderLevel
                    level == lowestLevel && ladders.contains(ladder)
                }
                .sortedBy { (ladderLevel, _) -> ladderLevel.first }
                .map { (_, list) ->
                    list }
                .flatten()
                .map { list -> list.second}
    }

    /**
     * Reconstruct scores from recorded sentence scores.
     */
    private fun reconstructScores() {
        keyScores.clear()
        studying.clear()
        coordinates.clear()

        // Reconstruct keyScores from sentence scores
        val sentenceScoreMap = mutableMapOf<Int, Score>()
        val sentences = TranslatedSentences()
        for ((japanese, sentenceScore) in sentencesStudying) {
            val index = sentences.sentenceToIndex(japanese)!!
            studying += index
            sentenceScoreMap[index] = sentenceScore
        }
        val coordinateIndex = ScoreCoordinateIndex()
        studying.forEachElement { sentence ->
            val sentenceScore = sentenceScoreMap[sentence]!!
            val scoreCoordinates = coordinateIndex.sentences[sentence]
            coordinates += scoreCoordinates
            scoreCoordinates.forEachElement { scoreCoordinateIndex ->
                val scoreCoordinate = coordinateIndex.coordinates[scoreCoordinateIndex]
                val keyScore = keyScores.getsert(scoreCoordinate) { Score() }
                keyScore.addFrom(sentenceScore)
            }
        }
    }

    companion object {
        private val cognitiveBurden = mutableMapOf<Int, Int>()
        fun burdenOf(sentence: Int): Int {
            return cognitiveBurden.getsert(sentence) {
                val counts = mutableMapOf<String, Int>()
                val costs = mutableMapOf<String, Int>()
                val coordinateIndex = ScoreCoordinateIndex()
                val allReasons = coordinateIndex.sentences[sentence]
                allReasons.forEachElement { reason ->
                    val (ladder, level, key) = coordinateIndex.coordinates[reason]
                    val count = counts.getsert(key) { 0 }
                    val cost = costs.getsert(key) { 0 }
                    counts[key] = count + 1
                    costs[key] = cost + (level + 1) * (level + 1) * ladder.levelsPerMezzo
                }
                counts.keys.sumBy { key ->
                    costs[key]!! / counts[key]!!
                }
            }
        }
    }
}