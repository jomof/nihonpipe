package com.jomof.nihonpipe.play

import com.jomof.algorithm.getsert
import com.jomof.algorithm.takeMinBy
import com.jomof.intset.IntSet
import com.jomof.intset.forEachElement
import com.jomof.intset.intSetOf
import com.jomof.intset.minus
import com.jomof.nihonpipe.datafiles.TranslatedSentences

data class Player(
        private val seedSentences: List<String> = listOf(),
        private val sentencesStudying: MutableMap<String, Score>) {
    private val keyScores = mutableMapOf<ScoreCoordinate, Score>()
    private val studying = intSetOf()
    val coordinates = intSetOf()
    private val sentencesNotStudying = allSentences.copy()

    init {
        reconstructScores()
        seedSentences
                .groupBy { it }
                .filter { it.value.size > 1 }
                .onEach { throw RuntimeException(it.key) }
    }

    fun addSentence(sentence: String, score: Score = Score()) {
        sentencesStudying[sentence] = score
        val sentenceIndex = TranslatedSentences().sentenceToIndex(sentence)
        studying += sentenceIndex
        sentencesNotStudying -= sentenceIndex
        val coordinateIndex = ScoreCoordinateIndex()
        val scoreCoordinates = coordinateIndex.getCoordinatesFromSentence(sentenceIndex)
        coordinates += scoreCoordinates
        scoreCoordinates.forEachElement { scoreCoordinateIndex ->
            val scoreCoordinate = coordinateIndex.getCoordinateFromCoordinateIndex(scoreCoordinateIndex)
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

    private fun getNextSentence(keySentences: IntSet): Int? {
        var leastBurdenSeen = Int.MAX_VALUE
        val result = mutableListOf<Int>()
        keySentences.forEachElement { sentence ->
            val sentenceCoordinates = ScoreCoordinateIndex()
                    .getCoordinatesFromSentence(sentence)
            var total = 0
            var contained = 0
            var notContained = 0
            sentenceCoordinates.forEach { sentence ->
                ++total
                if (coordinates.contains(sentence)) {
                    ++contained
                } else {
                    ++notContained
                }
            }

            if (total == contained || notContained == 0) {
                null
            } else {
                val burden = burdenOf(sentence)
                if (burden < leastBurdenSeen) {
                    leastBurdenSeen = burden
                    result.clear()
                }
                result += sentence
            }
        }
        val hopefullySingle = result
                .takeMinBy { ScoreCoordinateIndex().getCoordinatesFromSentence(it).size }
                .takeMinBy { TranslatedSentences().sentences[it]!!.japanese.length }
                .toList()
        assert(hopefullySingle.size <= 1)
        return hopefullySingle.singleOrNull()
    }

    fun addOneSentence(): SentenceRank {
        val seeding = seedSentences.size > studying.size
        val scoreCoordinateIndex = ScoreCoordinateIndex()
        val sentences = TranslatedSentences().sentences
        if (!seeding) {
            val transitions = LeastBurdenSentenceTransitions()
            val adjacents = intSetOf()
            adjacents += studying.map { sentence ->
                transitions.getNextSentences(sentence).first
            }.flatten().filter { !studying.contains(it) }
            if (adjacents.size > 20) { // If too few then weird sentences
                val nextByAdjacent = getNextSentence(adjacents)
                if (nextByAdjacent != null) {
                    val (from, deltaCoordinates) = studying.filter { sentence ->
                        transitions.getNextSentences(sentence)
                                .first
                                .contains(nextByAdjacent)
                    }.mapNotNull { from ->
                        val fromCoordinates = scoreCoordinateIndex.getCoordinatesFromSentence(from)
                        val toCoordinates = scoreCoordinateIndex.getCoordinatesFromSentence(nextByAdjacent)
                        val delta = (toCoordinates minus fromCoordinates) minus coordinates
                        if (delta.isEmpty()) {
                            null
                        } else {
                            Pair(from, delta)
                        }
                    }.sortedBy { it.second.size }.first()
                    val marginalBurden = deltaCoordinates.map { it ->
                        val coordinate = scoreCoordinateIndex.getCoordinateFromCoordinateIndex(it)
                        val level = coordinate.level + 1
                        level * level
                    }.sum()
                    addSentence(sentences[nextByAdjacent]!!.japanese)
                    return SentenceRank(
                            rank = studying.size,
                            sourceSentence = from,
                            marginalScoreCoordinates = deltaCoordinates,
                            sentenceIndex = nextByAdjacent,
                            marginalBurden = marginalBurden
                    )
                }
            }
        }
        val sentencesToConsider = if (seeding) {
            val result = intSetOf()
            result += seedSentences
                    .map { TranslatedSentences().sentenceToIndex(it) }
                    .filter { !studying.contains(it) }
            result
        } else {
            sentencesNotStudying
        }

        var to = getNextSentence(sentencesToConsider) ?: getNextSentence(sentencesToConsider)!!

        val toCoordinates = scoreCoordinateIndex.getCoordinatesFromSentence(to)
        val marginalCoordinates = toCoordinates minus coordinates
        val burden = marginalCoordinates.map { it ->
            val coordinate = scoreCoordinateIndex.getCoordinateFromCoordinateIndex(it)
            val level = coordinate.level + 1
            level * level
        }.sum()
        addSentence(sentences[to]!!.japanese)
        return SentenceRank(
                rank = studying.size,
                sourceSentence = null,
                marginalScoreCoordinates = marginalCoordinates,
                sentenceIndex = to,
                marginalBurden = burden
        )
    }

    private fun apprenticeLevelSentences(): List<String> {
        return sentencesStudying.entries
                .filter { it ->
                    it.value.mezzo() == MezzoScore.APPRENTICE
                }
                .map { it.key }
    }

    fun requestNextStudyAction(currentTime: Long): StudyAction? {
        // Add sentences if needed to reach maintenance level.
        while (apprenticeLevelSentences().size
                < maintenanceApprenticeLevelSentences) {
            addOneSentence()
        }
        return null
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
        const val maintenanceApprenticeLevelSentences = 10
        val allSentences = intSetOf(0 until TranslatedSentences().sentences.size)
        private val cognitiveBurdenMap = mutableMapOf<Int, Int>()
        fun calculateBurden(sentence: Int): Int {
            val counts = mutableMapOf<String, Int>()
            val costs = mutableMapOf<String, Int>()
            val coordinateIndex = ScoreCoordinateIndex()
            val allReasons = coordinateIndex.getCoordinatesFromSentence(sentence)
            val reasonList = mutableListOf<ScoreCoordinate>()
            allReasons.forEachElement { reason ->
                reasonList += coordinateIndex
                        .getCoordinateFromCoordinateIndex(reason)
            }
            val grouped = reasonList.groupBy { it.ladderKind }
            assert(grouped.size == LadderKind.values().size) {
                "Each ladder should be represented"
            }
            reasonList.forEach { (ladder, level, key) ->
                val count = counts.getsert(key) { 0 }
                val cost = costs.getsert(key) { 0 }
                counts[key] = count + 1
                costs[key] = cost + (level + 1) * (level + 1) * ladder.levelsPerMezzo
            }
            return counts.keys.sumBy { key ->
                costs[key]!! / counts[key]!!
            }
        }

        fun burdenOf(sentence: Int) = cognitiveBurdenMap.getsert(sentence) {
            calculateBurden(sentence)
        }
    }
}