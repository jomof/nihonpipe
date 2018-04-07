package com.jomof.nihonpipe.play

import com.jomof.algorithm.*
import com.jomof.intset.IntSet
import com.jomof.intset.forEachElement
import com.jomof.intset.intSetOf
import com.jomof.intset.minus
import com.jomof.nihonpipe.datafiles.*
import com.jomof.nihonpipe.play.io.*

data class Player(
        private val seedSentences: List<String> = listOf(),
        private val sentenceScores: MutableMap<String, Score>,
        private val achievementsUnlocked: MutableSet<String> = mutableSetOf()) {
    private val keyScores = mutableMapOf<LadderCoordinate, Score>()
    private val sentencesStudying = intSetOf()
    private val sentencesNotStudying = allSentences.copy()
    val disambiguationStats = StatisticsAccumulator()
    val postAchievementStats = StatisticsAccumulator()
    val ladderKeysCovered = intSetOf()
    val achievementsCovered = intSetOf()

    init {
        reconstructScores()
        assert(seedSentences == seedSentences.distinct())
    }

    /**
     * Add one sentence to the current user state.
     */
    fun addSentence(sentence: String, score: Score = Score()) {
        sentenceScores[sentence] = score
        val sentenceIndex = japaneseToSentenceIndex(sentence)
        sentencesStudying += sentenceIndex
        sentencesNotStudying -= sentenceIndex
        val scoreCoordinates = ladderCoordinateIndexesOfSentence(sentenceIndex)
        ladderKeysCovered += scoreCoordinates
        scoreCoordinates.forEachElement { scoreCoordinateIndex ->
            val scoreCoordinate = ladderCoordinateOfLadderCoordinateIndex(scoreCoordinateIndex)
            val keyScore = keyScores.getsert(scoreCoordinate) { Score() }
            keyScore.addFrom(score)
        }

        val achievementIndexes = achievementIndexesOfSentence(sentenceIndex)
        if (!achievementIndexes.isEmpty()) {
            achievementsCovered += achievementIndexes
        }
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
                    val coordinate = LadderCoordinate(ladderKind, level, key)
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

    private fun getNextSentence(keySentences: IntSet): Int? {
        var leastBurdenSeen = Int.MAX_VALUE
        val result = mutableListOf<Int>()
        keySentences.forEachElement { sentence ->
            val sentenceCoordinates = ladderCoordinateIndexesOfSentence(sentence)
            var total = 0
            var contained = 0
            var notContained = 0
            sentenceCoordinates.forEach { sentenceIndex ->
                ++total
                if (ladderKeysCovered.contains(sentenceIndex)) {
                    ++contained
                } else {
                    ++notContained
                }
            }

            if (total != contained && notContained != 0) {
                val burden = absoluteBurdenOfSentence(sentence)
                if (burden < leastBurdenSeen) {
                    leastBurdenSeen = burden
                    result.clear()
                }
                result += sentence
            }
        }

        /*
        f(sentence-index) : returns number of uncovered achievements that would be covered.
           n = achievements covered by sentence index : Map<Int, IntSet>
           m = achievements already covered by this player : IntSet
           o = n - m
         */
        fun coversAnAchievement(sentence: Int): Int {
            val n = achievementIndexesOfSentence(sentence)
            val found = n.doWhile { achievementsCovered.contains(it) }
            return if (!found) {
                1
            } else {
                0
            }
        }
        val hopefullySingle = result
                .gatherListStatistics(disambiguationStats)
                .takeMaxBy { coversAnAchievement(it) }
                .gatherListStatistics(postAchievementStats)
                .takeMinBy { ladderCoordinateIndexesOfSentence(it).size }
                .takeMinBy { sentenceIndexToTranslatedSentence(it).japanese.length }
                .toList()
        assert(hopefullySingle.size <= 1)
        return hopefullySingle.singleOrNull()
    }

    fun addOneSentence(): SentenceRank {
        val seeding = seedSentences.size > sentencesStudying.size
        if (!seeding) {
            val transitions = LeastBurdenSentenceTransitions()
            val adjacents = intSetOf()
            adjacents += sentencesStudying.map { sentence ->
                transitions.getNextSentences(sentence).first
            }.flatten().filter { !sentencesStudying.contains(it) }
            if (adjacents.size > 20) { // If too few then weird sentences
                val nextByAdjacent = getNextSentence(adjacents)
                if (nextByAdjacent != null) {
                    val (from, deltaCoordinates) = sentencesStudying.filter { sentence ->
                        transitions.getNextSentences(sentence)
                                .first
                                .contains(nextByAdjacent)
                    }.mapNotNull { from ->
                        val fromCoordinates = ladderCoordinateIndexesOfSentence(from)
                        val toCoordinates = ladderCoordinateIndexesOfSentence(nextByAdjacent)
                        val delta = (toCoordinates minus fromCoordinates) minus ladderKeysCovered
                        if (delta.isEmpty()) {
                            null
                        } else {
                            Pair(from, delta)
                        }
                    }.sortedBy { it.second.size }.first()
                    val marginalBurden = deltaCoordinates.map { it ->
                        val coordinate = ladderCoordinateOfLadderCoordinateIndex(it)
                        val level = coordinate.level + 1
                        level * level
                    }.sum()
                    addSentence(
                            sentenceIndexToTranslatedSentence(nextByAdjacent)
                                    .japanese)
                    return SentenceRank(
                            rank = sentencesStudying.size,
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
                    .map { japaneseToSentenceIndex(it) }
                    .filter { !sentencesStudying.contains(it) }
            result
        } else {
            sentencesNotStudying
        }

        val toSentence = getNextSentence(sentencesToConsider)!!
        val toCoordinates = ladderCoordinateIndexesOfSentence(toSentence)
        val marginalCoordinates = toCoordinates minus ladderKeysCovered
        val burden = marginalCoordinates.map { it ->
            val coordinate = ladderCoordinateOfLadderCoordinateIndex(it)
            val level = coordinate.level + 1
            level * level
        }.sum()
        addSentence(sentenceIndexToTranslatedSentence(toSentence).japanese)
        return SentenceRank(
                rank = sentencesStudying.size,
                sourceSentence = null,
                marginalScoreCoordinates = marginalCoordinates,
                sentenceIndex = toSentence,
                marginalBurden = burden
        )
    }

    private fun apprenticeLevelSentences(): List<String> {
        return sentenceScores.entries
                .filter { it ->
                    it.value.mezzo() == MezzoScore.APPRENTICE
                }
                .map { it.key }
    }

    fun requestStudyAction(currentTime: Long): StudyActionResponse {
        // Add sentences if needed to reach maintenance level.
        while (apprenticeLevelSentences().size
                < maintenanceApprenticeLevelSentences) {
            addOneSentence()
        }
        // Get the first sentence to study
        val possibleSentence = sentenceScores
                .entries
                .filter { (_, score) ->
                    var nextReview = score.timeOfNextReview()
                    nextReview <= currentTime
                }
                .sortedBy { (_, score) ->
                    score.timeOfLastAttempt()
                }
                .firstOrNull()

        return when (possibleSentence) {
            null -> StudyActionResponse(StudyActionType.NOTHING)
            else -> {
                val (japanese, score) = possibleSentence
                val sentence = japaneseToSentenceIndex(japanese)
                val translated = sentenceIndexToTranslatedSentence(sentence)
                StudyActionResponse(
                        type = StudyActionType.SENTENCE_TEST,
                        english = translated.english,
                        sentence = sentence,
                        hints = StudyActionHints(
                                skeleton = skeletonHint(japanese, score)
                        ),
                        debug = StudyActionDebug(
                                reading = readingOfJapaneseSentence(japanese),
                                pronunciation = pronunciationOfJapaneseSentence(japanese)
                        )
                )
            }
        }
    }

    fun respondSentenceTest(
            sentence: Int,
            answer: String,
            currentTime: Long): RespondSentenceTestResponse {
        val translated = sentenceIndexToTranslatedSentence(sentence)
        val japanese = translated.japanese
        val reading = readingOfJapaneseSentence(japanese)
        val pronunciation = pronunciationOfJapaneseSentence(japanese)
        val score = sentenceScores[japanese]!!
        val priorMezzo = score.mezzo()
        val priorMasterLevelAchievementElements = achievementsCoveredAtMasterLevel()
        val priorMasterLevelLadderKeyElements = ladderKeysCoveredAtMasterLevel()
        val wasCorrect =
                if (answer == reading || answer == pronunciation) {
                    score.recordCorrect(currentTime)
                    true
                } else {
                    score.recordIncorrect(currentTime)
                    false
                }

        val achievements = currentAchievements()
        val marginalAchievements = mutableSetOf<String>()
        marginalAchievements += achievementsUnlocked
        marginalAchievements -= achievements
        achievementsUnlocked += achievements

        var mezzoPromotion = if (score.mezzo() > priorMezzo) {
            score.mezzo()
        } else {
            null
        }

        val masterLevelAchievementElements = achievementsCoveredAtMasterLevel()
        val marginalAchievementElementIndexes =
                masterLevelAchievementElements minus priorMasterLevelAchievementElements
        val marginalAchievementElements = marginalAchievementElementIndexes
                .map { index ->
                    AchievementElement(
                            vocab = achievementIndexToVocab(index),
                            flavors = achievementIndexToFlavor(index))
                }

        val masterLevelLadderKeyElements = ladderKeysCoveredAtMasterLevel()
        val marginalLadderKeyIndexes =
                masterLevelLadderKeyElements minus priorMasterLevelLadderKeyElements
        val marginalLadderKeyElements = marginalLadderKeyIndexes
                .map { index ->
                    ladderCoordinateOfLadderCoordinateIndex(index)
                }
        return RespondSentenceTestResponse(
                wasCorrect = wasCorrect,
                japanese = normalizationOfJapaneseSentence(japanese),
                pronunciation = pronunciation,
                reading = reading,
                mezzoPromotion = mezzoPromotion,
                achievementsUnlocked = marginalAchievements,
                achievementElementsUnlocked = marginalAchievementElements,
                ladderKeyElementsUnlocked = marginalLadderKeyElements)
    }

    fun requestUserStatistics(): UserStatisticsResponse {
        val sentenceScoreMap = sentenceScores
                .values
                .groupBy { it.mezzo() }
                .map { (mezzo, value) -> Pair(mezzo, value.count()) }
                .toMap()

        return UserStatisticsResponse(
                apprenticeSentences = sentenceScoreMap[MezzoScore.APPRENTICE] ?: 0,
                guruSentences = sentenceScoreMap[MezzoScore.GURU] ?: 0,
                masterSentences = sentenceScoreMap[MezzoScore.MASTER] ?: 0,
                enlightenedSentences = sentenceScoreMap[MezzoScore.ENLIGHTENED] ?: 0,
                burnedSentences = sentenceScoreMap[MezzoScore.BURNED] ?: 0
        )
    }

    private fun achievementsCoveredAtMasterLevel(): IntSet {
        val achievementsCoveredAtMasterLevel = intSetOf()
        sentenceScores.forEach { (sentence, score) ->
            if (score.mezzo() >= MezzoScore.MASTER) {
                val sentenceIndex = japaneseToSentenceIndex(sentence)
                val achievements = achievementIndexesOfSentence(sentenceIndex)
                achievementsCoveredAtMasterLevel += achievements
            }
        }
        return achievementsCoveredAtMasterLevel
    }

    private fun ladderKeysCoveredAtMasterLevel(): IntSet {
        val ladderKeysCoveredAtMasterLevel = intSetOf()
        sentenceScores.forEach { (sentence, score) ->
            if (score.mezzo() >= MezzoScore.MASTER) {
                val sentenceIndex = japaneseToSentenceIndex(sentence)
                val achievements = ladderCoordinateIndexesOfSentence(sentenceIndex)
                ladderKeysCoveredAtMasterLevel += achievements
            }
        }
        return ladderKeysCoveredAtMasterLevel
    }

    private fun currentAchievements(): List<String> {
        val achievementsCoveredAtMasterLevel = intSetOf()
        sentenceScores.forEach { (sentence, score) ->
            if (score.mezzo() >= MezzoScore.MASTER) {
                val sentenceIndex = japaneseToSentenceIndex(sentence)
                val achievements = achievementIndexesOfSentence(sentenceIndex)
                achievementsCoveredAtMasterLevel += achievements
            }
        }

        val list = mutableListOf<String>()
        for (achievement in achievementNames()) {
            val achievementIndexes = achievementIndexes(achievement)
            val remainingAchievements = achievementIndexes minus achievementsCoveredAtMasterLevel
            if (remainingAchievements.isEmpty()) {
                list += achievement
            }
        }
        return list
    }

    /**
     * Reconstruct scores from recorded sentence scores.
     */
    private fun reconstructScores() {
        sentenceScores.forEach { (sentence, score) ->
            addSentence(sentence, score)
        }
    }

    companion object {
        const val maintenanceApprenticeLevelSentences = 30
        val allSentences = intSetOf(sentenceIndexRange())
    }
}