package com.jomof.nihonpipe.play

import com.jomof.algorithm.getsert
import com.jomof.algorithm.takeMaxBy
import com.jomof.algorithm.takeMinBy
import com.jomof.intset.IntSet
import com.jomof.intset.forEachElement
import com.jomof.intset.intSetOf
import com.jomof.intset.minus
import com.jomof.nihonpipe.datafiles.TranslatedSentences

data class Player(
        private val sentencesStudying: MutableMap<String, Score>) {
    private val keyScores = mutableMapOf<ScoreCoordinate, Score>()
    private val studying = intSetOf()
    val coordinates = intSetOf()

    init {
        reconstructScores()
    }

    fun addSentence(sentence: String, score: Score = Score()) {
        assert(!sentencesStudying.contains(sentence))
        sentencesStudying[sentence] = score
        val sentence = TranslatedSentences().sentenceToIndex(sentence)!!
        studying += sentence
        val coordinateIndex = ScoreCoordinateIndex()
        val scoreCoordinates = coordinateIndex.sentences[sentence]
        coordinates += scoreCoordinates
        scoreCoordinates.forEachElement { scoreCoordinateIndex ->
            val scoreCoordinate = coordinateIndex.coordinates[scoreCoordinateIndex]
            val keyScore = keyScores.getsert(scoreCoordinate) { Score() }
            keyScore.addFrom(score)
        }
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

    fun addOneSentence(): SentenceRank {
        val sentences = TranslatedSentences().sentences
        val keySentence = (0 until sentences.size)
//                when (studying.size % 20) {
//                    1 -> sentencesOfLowestLevels(
//                            LadderKind.WANIKANI_VOCAB_LADDER,
//                            LadderKind.JLPT_VOCAB_LADDER).flatten()
//                    10 -> sentencesOfLowestLevels(
//                            LadderKind.TOKEN_FREQUENCY_LADDER).first()
//                    else -> (0 until sentences.size)
//                }

        // We want to find sentences that will increase the total number of
        // scores by the smallest positive value. This represents the least
        // cognitive burden to the learner.
        val coordinateIndex = ScoreCoordinateIndex()
        var leastBurdenSeen = Int.MAX_VALUE
        val (marginalBurden, sentenceIndex) = keySentence
                .mapNotNull { sentence ->
                    val cognitiveBurden = burdenOf(sentence)
                    if (cognitiveBurden == 0 || cognitiveBurden > leastBurdenSeen) {
                        null
                    } else if (studying.contains(sentence)) {
                        null
                    } else {
                        val allReasons = coordinateIndex.sentences[sentence]
                        if (cognitiveBurden < leastBurdenSeen) {
                            var noDifference = allReasons.doWhile { reason ->
                                coordinates.contains(reason)
                            }
                            if (noDifference) {
                                null
                            } else {
                                leastBurdenSeen = cognitiveBurden
                                Pair(cognitiveBurden, sentence)
                            }
                        } else {
                            Pair(cognitiveBurden, sentence)
                        }
                    }
                }
                .filter { it.first > 0 }
                .asIterable()
                .takeMinBy { it.first } // Take the least burden
                .takeMinBy { sentences[it.second]!!.japanese.length } // Take by length
                .takeMaxBy { it.second } // Take the last sentence
                .single()
        val translatedSentence = sentences[sentenceIndex]!!
        val marginalCoordinates = coordinateIndex.sentences[sentenceIndex] minus coordinates
        addSentence(translatedSentence.japanese)
        return SentenceRank(
                rank = sentencesStudying.size,
                sentenceIndex = sentenceIndex,
                marginalScoreCoordinates = marginalCoordinates,
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
                .map { (ladderLevel, list) ->
                    val (ladder, level) = ladderLevel
                    list.filter { (key, sentences) ->
                        val scoreCoordinate = ScoreCoordinate(ladder, level, key)
                        !keyScores.containsKey(scoreCoordinate)

                    }
                }
                .flatten()
                .map { list -> list.second }
    }

    /**
     * Reconstruct scores from recorded sentence scores.
     */
    private fun reconstructScores() {
        sentencesStudying.forEach { (sentence, score) ->
            addSentence(sentence, score)
        }
    }

    companion object {
        private val cognitiveBurden = Array(TranslatedSentences().sentences.size) { sentence ->
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

        fun burdenOf(sentence: Int) = cognitiveBurden[sentence]
    }
}