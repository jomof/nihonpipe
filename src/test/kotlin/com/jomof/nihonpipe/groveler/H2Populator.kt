package com.jomof.nihonpipe.groveler

import com.google.common.truth.Truth.assertThat
import com.jomof.intset.IntSet
import com.jomof.intset.intSetOf
import com.jomof.intset.minus
import com.jomof.intset.union
import com.jomof.intset.intersect
import com.jomof.algorithm.combinations
import com.jomof.nihonpipe.groveler.algorithm.getsert
import com.jomof.nihonpipe.groveler.datafiles.*
import com.jomof.nihonpipe.groveler.schema.particleSkeletonForm
import org.junit.Test
import kotlin.math.max

class H2Populator {

    @Test
    fun jishoJlpt() {
        JishoJlptVocabs.vocabOf
    }

    @Test
    fun optimizedKore() {
        OptimizedKoreVocabs.vocabOf
    }

    @Test
    fun wanikaniVocab() {
        WanikaniVocabs.vocabOf
    }

    @Test
    fun tanakaCorpus() {
        TranslatedSentences.sentences
    }

    @Test
    fun wanikaniVsJlpt() {
        WanikaniVsJlptVocabs.vocabOf
    }

    @Test
    fun statistics() {
        val summarize = SentenceStatisticsCache.summarize
        val stats = summarize("ええ誕生日に友達のクラークさんにもらいました")
        assertThat(stats.waniKaniLevel.min).isEqualTo(2)
        assertThat(stats.waniKaniLevel.max).isEqualTo(18)
        SentenceStatisticsCache.save()
    }

    @Test
    fun tokenization() {
        val tokenize = KuromojiIpadicCache.tokenize
        val tokenization = tokenize("ええ誕生日に友達のクラークさんにもらいました")
        assertThat(tokenization.tokens).hasSize(13)
        KuromojiIpadicCache.save()
    }

    @Test
    fun sentenceSkeletonFilter() {
        SentenceSkeletonFilter.filterOf
    }

    @Test
    fun grammarSummaryFilter() {
        GrammarSummaryFilter.filterOf
    }

    @Test
    fun wanikaniLevelFilter() {
        val sentencesOf = WanikaniLevelFilter.filterOf
        sentencesOf(42)
    }

    @Test
    fun vocabToSentenceFilter() {
        val vocabToSentence = VocabToSentenceFilter()
        vocabToSentence["友達"]
    }

    @Test
    fun sentenceSkeletonLevels() {
        val skeletonLevels = SentenceSkeletonLevels()
        skeletonLevels[5]
    }

    @Test
    fun waniKaniVocabLevels() {
        val levels = WanikaniVocabLevels()
        levels[42]
    }

    @Test
    fun brother() {
        val tokenize = KuromojiIpadicCache.tokenize
        val tokenization = tokenize("お兄さんの身長はいくつですか")
        assertThat(tokenization.tokens).hasSize(8)
        KuromojiIpadicCache.save()
    }

    @Test
    fun repro() {
        val filter = VocabToSentenceFilter()["一"]
        assertThat(filter.contains(4635)).isFalse()
    }

    @Test
    fun oneigaishimas() {
        val tokenize = KuromojiIpadicCache.tokenize
        val tokenization = tokenize("フライトのリコンファームをお願いします")
        assertThat(tokenization.tokens).hasSize(7)
        KuromojiIpadicCache.save()
    }

    @Test
    fun grammarSummaryLevels() {
        val levels = GrammarSummaryLevels()
        val level = levels[5]
        println("$level")
    }

    data class Score(
            var correct: Int = 0,
            var incorrect: Int = 0) {
        fun value() = correct - max(incorrect, 0)
        fun attempts() = correct + incorrect
    }

    enum class LadderKind(
            val levelProvider: LevelProvider) {
        WANIKANI_VOCAB_LADDER(WanikaniVocabLevels()),
        SENTENCE_SKELETON_LADDER(SentenceSkeletonLevels()),
        GRAMMAR_SUMMARY_LADDER(GrammarSummaryLevels());
    }

    enum class MezzoScore(
            private val scoreRange: IntRange) {
        APPRENTICE(0..5),
        GURU(6..10),
        MASTER(11..15),
        ENLIGHTENED(16..20),
        BURNED(21..Int.MAX_VALUE);

        companion object {
            fun fromScore(score: Score): MezzoScore {
                for (mezzo in values()) {
                    if (score.value() in mezzo.scoreRange) {
                        return mezzo
                    }
                }
                throw RuntimeException()
            }
        }
    }

    data class Scores(
            val currentLevel: Int = 0,
            val levels: MutableMap<Int, MutableMap<String, Score>> = mutableMapOf()) {
        operator fun get(level: Int, key: String): Score {
            while (level >= levels.size) {
                levels[level] = mutableMapOf()
            }
            val map = levels[level]!!
            return map.getsert(key) { Score() }
        }
    }

    data class ScoreCoordinate(
            val ladderKind : LadderKind,
            val level : Int,
            val key : String)

    data class Player(
            val sentencesStudying: IntSet = intSetOf(),
            val ladders: MutableMap<LadderKind, Scores> = mutableMapOf()) {

        fun levelMap(ladderKind: LadderKind) = ladders.getsert(ladderKind) {
            Scores()
        }

        fun sentenceScoreCoordinates(sentence : Int) : Set<ScoreCoordinate> {
            val (japanese, _) = TranslatedSentences.sentences.sentences[sentence]!!
            val tokenization = KuromojiIpadicCache.tokenize(japanese)
            val result = mutableSetOf<ScoreCoordinate>()
            for (ladderKind in LadderKind.values()) {
                val scores = ladders[ladderKind] ?: Scores()
                val levelProvider = ladderKind.levelProvider
                val levelKeysAndSentences = levelProvider[scores.currentLevel]
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

        fun getScore(coordinate : ScoreCoordinate) : Score {
            val ladder = ladders.getsert(coordinate.ladderKind) { Scores() }
            return ladder[coordinate.level, coordinate.key]
        }

        fun newSentenceValue(sentence : Int) : Int {
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

        fun scoreCorrect(sentence : Int) {
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
            var currentApprenticeLevelSentences = 0
            for (ladderKind in LadderKind.values()) {
                // Get a count of APPRENTICE item for each ladderKind
                val levelMap = levelMap(ladderKind)
                var apprenticeCount = 0
                for ((_, scores) in levelMap.levels) {
                    for ((_, score) in scores) {
                        if (score.attempts() > 0) {
                            val mezzoLevel = MezzoScore.fromScore(score)
                            if (mezzoLevel == MezzoScore.APPRENTICE) {
                                ++apprenticeCount
                            }
                        }
                    }
                }
                currentApprenticeLevelSentences = max(
                        apprenticeCount,
                        currentApprenticeLevelSentences)
            }


            val apprenticeLevelsNeeded = 10 - currentApprenticeLevelSentences
            if (apprenticeLevelsNeeded <= 0) {
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
                            val scores = ladders[ladderKind]
                            val levelProvider = ladderKind.levelProvider
                            val keySentencesForLevel = levelProvider[scores!!.currentLevel]
                            var combined = intSetOf()
                            for ((_, sentences) in keySentencesForLevel) {
                                combined = combined union sentences
                            }
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
                    .onEach {(ladderKinds, result) ->
                        println("$ladderKinds provides ${result.size} sentences")
                    }
                    .map { it.second }
                    .flatten()
                    .filter { !sentencesStudying.contains(it) }
                    .take(apprenticeLevelsNeeded * 20)
                    .distinctBy { it }
                    .map { Pair(newSentenceValue(it), it)}
                    .sortedByDescending { it.first }
                    .take(apprenticeLevelsNeeded)
                    .onEach { (value, sentence) ->
                        val tokenize = KuromojiIpadicCache.tokenize
                        val (japanese, english) = TranslatedSentences.sentences.sentences[sentence]!!
                        val tokenization = tokenize(japanese)
                        val skeleton = tokenization.particleSkeletonForm()
                        println("$value skeleton=$skeleton japanese=$japanese english=$english")
                    }
                    .map { it.second }

            sentencesStudying += sentences
        }
    }


    @Test
    fun player() {
        val player = Player()
        player.addNewSentencesIfNecessary()
        assertThat(player.sentencesStudying).hasSize(10)
        player.addNewSentencesIfNecessary()
        assertThat(player.sentencesStudying).hasSize(10)
    }

    @Test
    fun initialScoreCoordinates() {
        val player = Player()
        val coordinates = player.sentenceScoreCoordinates(50195)
    }

    @Test
    fun iteratePlayer() {
        val player = Player()
        fun scoreAllCorrect() {
            for (sentence in player.sentencesStudying) {
                player.scoreCorrect(sentence)
            }
        }
        player.addNewSentencesIfNecessary()
        scoreAllCorrect()
        assertThat(player.sentencesStudying).hasSize(10)
        player.addNewSentencesIfNecessary()
        assertThat(player.sentencesStudying).hasSize(13)
        scoreAllCorrect()
        player.addNewSentencesIfNecessary()
        assertThat(player.sentencesStudying).hasSize(15)
        scoreAllCorrect()
        player.addNewSentencesIfNecessary()
        assertThat(player.sentencesStudying).hasSize(18)
        scoreAllCorrect()
        player.addNewSentencesIfNecessary()
        assertThat(player.sentencesStudying).hasSize(21)
        scoreAllCorrect()
        player.addNewSentencesIfNecessary()
        assertThat(player.sentencesStudying).hasSize(25)
        scoreAllCorrect()
        player.addNewSentencesIfNecessary()
        assertThat(player.sentencesStudying).hasSize(33)
        scoreAllCorrect()
        player.addNewSentencesIfNecessary()
        assertThat(player.sentencesStudying).hasSize(43)
        scoreAllCorrect()
        player.addNewSentencesIfNecessary()
        assertThat(player.sentencesStudying).hasSize(53)
        scoreAllCorrect()
    }
}