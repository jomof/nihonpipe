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
        val tokenization = tokenizeJapaneseSentence(
                "ええ誕生日に友達のクラークさんにもらいました")
        assertThat(tokenization.tokens).hasSize(13)
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
    fun sentenceSkeletonLevels() {
        val skeletonLevels = SentenceSkeletonLadder()
        skeletonLevels.getKeySentences(5)
    }

    @Test
    fun brother() {
        val tokenization = tokenizeJapaneseSentence(
                "お兄さんの身長はいくつですか")
        assertThat(tokenization.tokens).hasSize(8)
    }

    @Test
    fun oneigaishimas() {
        val tokenization = tokenizeJapaneseSentence(
                "フライトのリコンファームをお願いします")
        assertThat(tokenization.tokens).hasSize(7)
    }

    @Test
    fun grammarSummaryLevels() {
        val levels = GrammarSummaryLadder()
        val level = levels.getKeySentences(5)
        println("$level")
    }



}