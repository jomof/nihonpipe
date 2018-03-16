package com.jomof.nihonpipe.groveler

import com.google.common.truth.Truth.assertThat
import com.jomof.intset.IntSet
import com.jomof.intset.intSetOf
import com.jomof.nihonpipe.groveler.datafiles.*
import org.junit.Test

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

    enum class LadderKind {
        WANIKANI_VOCAB_LADDER,
        SENTENCE_SKELETON_LADDER,
        GRAMMAR_SUMMARY_LADDER;
    }

    @Test
    fun player() {
        data class Score(
                val correct: Short = 0,
                val incorrect: Short = 0)

        class LevelMap(
                val currentLevel: Int = 0,
                val levels: List<Map<String, Score>> = listOf())

        data class Player(
                val sentencesStudying: IntSet = intSetOf(),
                val ladders: Map<LadderKind, LevelMap> = mapOf())

        val player = Player()
        val skeletonLevels = SentenceSkeletonLevels()
        val wanikaniVocabLevels = WanikaniVocabLevels()


    }
}