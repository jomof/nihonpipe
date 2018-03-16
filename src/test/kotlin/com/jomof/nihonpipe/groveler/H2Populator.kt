package com.jomof.nihonpipe.groveler

import com.google.common.truth.Truth.assertThat
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
        val stats = summarize("ええ、誕生日に 友達の クラークさんに もらいました")
        assertThat(stats.waniKaniLevel.min).isEqualTo(2)
        assertThat(stats.waniKaniLevel.max).isEqualTo(18)
        SentenceStatisticsCache.save()
    }

    @Test
    fun tokenization() {
        val tokenize = KuromojiIpadicCache.tokenize
        val tokenization = tokenize("ええ、誕生日に 友達の クラークさんに もらいました")
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
        val sentencesOf = VocabToSentenceFilter.sentencesOf
        sentencesOf("友達")
    }

    @Test
    fun sentenceSkeletonLevels() {
        val skeletonLevels = SentenceSkeletonLevels()
        skeletonLevels[5]
    }

    @Test
    fun waniKaniVocabLevels() {
        val levels = WanikaniVocabLevels()
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
    fun player() {
        data class Score(
                val correct: Short = 0,
                val incorrect: Short = 0)

        class LevelMap(
                val currentLevel: Int = 0,
                val levels: List<Map<String, Score>> = listOf())

        data class Player(
                val wanikaniScores: LevelMap = LevelMap(),
                val sentenceSkeletonScores: LevelMap = LevelMap(),
                val grammarSummaryScores: LevelMap = LevelMap())

        val player = Player()
        val skeletonLevels = SentenceSkeletonLevels()
        val wanikaniVocabLevels = WanikaniVocabLevels()


    }
}