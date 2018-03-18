package com.jomof.nihonpipe.play

import com.jomof.algorithm.combinations
import com.jomof.algorithm.getsert
import com.jomof.intset.IntSet
import com.jomof.intset.intSetOf
import com.jomof.intset.intersect
import com.jomof.nihonpipe.datafiles.KuromojiIpadicCache
import com.jomof.nihonpipe.datafiles.TranslatedSentences
import com.jomof.nihonpipe.groveler.schema.particleSkeletonForm

data class Player(
        val sentencesStudying: IntSet = intSetOf(),
        private val ladders: MutableMap<LadderKind, Scores> = mutableMapOf()) {

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

    fun newSentenceValue(sentence: Int): Int {
        val coordinates = sentenceScoreCoordinates(sentence)
        var value = 0
        for (coordinate in coordinates) {
            val score = getScore(coordinate)
            if (score.attempts() > 0) {
                // small value if this score is already covered
                value++
            } else {
                value += 3
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
        val coordinates = mutableSetOf<ScoreCoordinate>()
        for (sentence in sentencesStudying) {
            for (coordinate in sentenceScoreCoordinates(sentence)) {
                coordinates.add(coordinate)
            }
        }
        val currentApprenticeLevelCoordinates = coordinates
                .filter {
                    it.toMezzoLevel(getScore(it).value()) == MezzoScore.APPRENTICE
                }
                .count()
        val apprenticeCoordinatesNeeded = 10 - currentApprenticeLevelCoordinates
        if (apprenticeCoordinatesNeeded <= 0) {
            return
        }
        val sentences = intSetOf()
        println("-----")
        sentences += LadderKind.values()
                .combinations()
                .toList()
                .sortedByDescending { ladderKinds -> ladderKinds.size }
                .map { ladderKinds ->
                    var candidates: IntSet? = null
                    for (ladderKind in ladderKinds) {
                        val scores = ladders.getsert(ladderKind) { Scores() }
                        val levelProvider = ladderKind.levelProvider
                        val combined =
                                levelProvider.getSentences(scores.currentLevel)
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
                .onEach { (ladderKinds, result) ->
                    println("$ladderKinds provides ${result.size} sentences")
                }
                .map { it.second }
                .flatten()
                .filter { !sentencesStudying.contains(it) }
                .take(apprenticeCoordinatesNeeded * 20)
                .distinctBy { it }
                .map { Pair(newSentenceValue(it), it) }
                .sortedByDescending { it.first }
                .take(apprenticeCoordinatesNeeded)
                .onEach { (value, sentence) ->
                    val tokenize = KuromojiIpadicCache.tokenize
                    val (japanese, english) = TranslatedSentences.sentences.sentences[sentence]!!
                    val tokenization = tokenize(japanese)
                    val skeleton = tokenization.particleSkeletonForm()
                    println("$sentence $value skeleton=$skeleton japanese=$japanese english=$english")
                }
                .map { it.second }

        sentencesStudying += sentences
    }
}