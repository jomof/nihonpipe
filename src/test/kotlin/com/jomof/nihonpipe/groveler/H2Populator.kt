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
        TanakaCorpusSentences.tanaka
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
        WanikaniLevelFilter.filterOf.levels
                .toList()
                .sortedByDescending { (_, filter) ->
                    filter.size
                }
                .forEach { (level, filter) ->
                    println("$level = ${filter.size}")
                }
    }
}