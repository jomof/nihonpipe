package com.jomof.nihonpipe.groveler

import com.google.common.truth.Truth.assertThat
import com.jomof.intset.IntSet
import com.jomof.intset.intSetOf
import com.jomof.nihonpipe.groveler.algorithm.getsert
import com.jomof.nihonpipe.groveler.datafiles.*
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
        TranslatedSentences.tanaka
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
            val correct: Int = 0,
            val incorrect: Int = 0) {
        fun value() = correct - max(incorrect, 0)
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

    data class LevelMap(
            val currentLevel: Int = 0,
            val levels: List<Map<String, Score>> = listOf())

    class GameConstants {
        /**
         * The minimum number of apprentice levels needed.
         */
        val minApprenticeLevels = 10
    }

    data class Player(
            val sentencesStudying: IntSet = intSetOf(),
            val ladders: MutableMap<LadderKind, LevelMap> = mutableMapOf()) {

        fun levelMap(ladderKind: LadderKind) = ladders.getsert(ladderKind) {
            LevelMap()
        }

        fun newSentencesNeeded() {
            val apprenticeCounts = mutableMapOf<LadderKind, Int>()
            for (ladderKind in LadderKind.values()) {
                // Get a count of APPRENTICE item for each ladderKind
                val levelMap = levelMap(ladderKind)
                var apprenticeCount = 0
                for (level in levelMap.levels) {
                    for ((key, score) in level) {
                        val mezzoLevel = MezzoScore.fromScore(score)
                        if (mezzoLevel == MezzoScore.APPRENTICE) {
                            ++apprenticeCount
                        }
                    }
                }
                apprenticeCounts[ladderKind] = apprenticeCount
            }

            // Now we have a count of how many apprentice items there are for each ladder.
            // The number we *need* is (minApprenticeLevels - ladder apprentice levels)
            // How do we choose new sentences?
            // For each kind of ladder we look for sentences at the current level or lower
            // that satisfy
        }
    }


    @Test
    fun player() {
        val player = Player()
        player.newSentencesNeeded()
    }
}