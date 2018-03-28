package com.jomof.nihonpipe.groveler

import com.google.common.truth.Truth.assertThat
import com.jomof.nihonpipe.datafiles.*
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
    fun vocabToSentenceFilter() {
        val vocabToSentence = VocabToSentenceFilter()
        vocabToSentence["友達"]
    }

    @Test
    fun sentenceSkeletonLevels() {
        val skeletonLevels = SentenceSkeletonLadder()
        skeletonLevels.getKeySentences(5)
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
        val levels = GrammarSummaryLadder()
        val level = levels.getKeySentences(5)
        println("$level")
    }



}