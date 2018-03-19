package com.jomof.nihonpipe.play

import com.jomof.algorithm.getsert
import com.jomof.intset.IntSet
import com.jomof.intset.intSetOf
import com.jomof.intset.intersect
import com.jomof.intset.union
import com.jomof.nihonpipe.datafiles.KuromojiIpadicCache
import com.jomof.nihonpipe.datafiles.TranslatedSentences
import com.jomof.nihonpipe.groveler.schema.particleSkeletonForm
import kotlin.math.min

data class Player(
        val sentencesStudying: IntSet = intSetOf(),
        private val ladders: MutableMap<LadderKind, Scores> = mutableMapOf()) {

    fun allSentenceScoreCoordinates(): Set<ScoreCoordinate> {
        val result = mutableSetOf<ScoreCoordinate>()
        for (ladderKind in LadderKind.values()) {
            val scores = ladders.getsert(ladderKind) { Scores() }
            val level = scores.currentLevel
            val levelProvider = ladderKind.levelProvider
            for ((key, _) in levelProvider.getKeySentences(level)) {
                result += ScoreCoordinate(ladderKind, level, key)
            }
        }
        return result
    }

    fun sentenceScoreCoordinates(sentence: Int): Set<ScoreCoordinate> {
        val (japanese, _) = TranslatedSentences.sentences.sentences[sentence]!!
        val tokenization = KuromojiIpadicCache.tokenize(japanese)
        val result = mutableSetOf<ScoreCoordinate>()
        for (ladderKind in LadderKind.values()) {
            val scores = ladders.getsert(ladderKind) { Scores() }
            val levelProvider = ladderKind.levelProvider
            val levelKeysAndSentences = levelProvider.getKeySentences(scores.currentLevel)
            val keysOfSentence = levelProvider.keysOf(tokenization)
            for ((key, sentences) in levelKeysAndSentences) {
                if (keysOfSentence.contains(key) && sentences.contains(sentence)) {
                    result += ScoreCoordinate(ladderKind, scores.currentLevel, key)
                    assert(scores.currentLevel == 0)
                }
            }
        }
        return result
    }

    fun getScore(coordinate: ScoreCoordinate): Score {
        val ladder = ladders.getsert(coordinate.ladderKind) { Scores() }
        return ladder[coordinate.level, coordinate.key]
    }

    private fun newSentenceValue(sentence: Int): Int {
        if (sentencesStudying.contains(sentence)) {
            return 0
        }
        val apprenticeLevelsBefore = apprenticeLevelCoordinates(sentencesStudying)
        val apprenticeLevelsAfter = apprenticeLevelCoordinates(
                sentencesStudying union intSetOf(sentence))
        val apprenticeLevelsAdded = apprenticeLevelsAfter - apprenticeLevelsBefore
        if (apprenticeLevelsAdded == 0) {
            return 0
        }
        val coordinates = sentenceScoreCoordinates(sentence)
        var value = apprenticeLevelsAdded
        for (coordinate in coordinates) {
            val score = getScore(coordinate)
            value += coordinate.ladderKind.valueOfNewSentence
            if (score.attempts() == 0) {
                // Larger value if this key isn't covered yet
                value += 2
            }
        }

        return value
    }

    fun scoreCorrect(sentence: Int) {
        val coordinates = sentenceScoreCoordinates(sentence)
        val seen = mutableSetOf<ScoreCoordinate>()
        for (coordinate in coordinates) {
            if (!seen.contains(coordinate)) {
                seen += coordinate
                val score = getScore(coordinate)
                score.correct++
            }
        }
    }

    fun addNewSentencesIfNecessary() {
        var searchRadius = 0
        while (true) {
            val currentApprenticeLevelCoordinates =
                    apprenticeLevelCoordinates(sentencesStudying)
            val apprenticeCoordinatesNeeded = 10 - currentApprenticeLevelCoordinates
            if (apprenticeCoordinatesNeeded <= 0) {
                return
            }
            val result = listOf(LadderKind.values())
                    .map { ladderKinds ->
                        var candidates: IntSet? = null
                        for (ladderKind in ladderKinds) {
                            val levelProvider = ladderKind.levelProvider
                            val scores = ladders.getsert(ladderKind) { Scores() }
                            val currentLevel = scores.currentLevel
                            var combined: IntSet = levelProvider.getSentences(
                                    min(currentLevel + searchRadius, levelProvider.size - 1))
                            candidates = if (candidates == null) {
                                combined
                            } else {
                                candidates intersect combined
                            }
                        }
                        val result = (candidates ?: intSetOf())

                        Pair(ladderKinds, result)
                    }
                    .sortedBy { it.second.size }
                    .map { it.second }
                    .flatten()
                    .filter { !sentencesStudying.contains(it) }
                    .distinctBy { it }
                    .map { Pair(newSentenceValue(it), it) }
                    .sortedByDescending { it.first }
                    .take(1)
                    .filter { it.first > 0 }
                    .onEach { (value, sentence) ->
                        val tokenize = KuromojiIpadicCache.tokenize
                        val (japanese, english) = TranslatedSentences.sentences.sentences[sentence]!!
                        val tokenization = tokenize(japanese)
                        val skeleton = tokenization.particleSkeletonForm()
                        println("$sentence $value skeleton=$skeleton japanese=$japanese english=$english")
                    }
                    .singleOrNull()
            if (result == null || result.first == 0) {
                searchRadius++
            } else {
                sentencesStudying += result.second
            }
        }
    }

    private fun apprenticeLevelCoordinates(
            sentences: IntSet): Int {
        val coordinates = mutableSetOf<ScoreCoordinate>()
        for (sentence in sentences) {
            for (coordinate in sentenceScoreCoordinates(sentence)) {
                coordinates.add(coordinate)
            }
        }
        return coordinates
                .filter {
                    it.toMezzoLevel(getScore(it).value()) == MezzoScore.APPRENTICE
                }
                .count()
    }
}