package com.jomof.nihonpipe.datafiles

import com.jomof.intset.IntSet
import com.jomof.intset.intSetOf
import com.jomof.nihonpipe.grammarSummaryFilter
import com.jomof.nihonpipe.groveler.schema.grammarSummaryForm
import org.h2.mvstore.MVStore

class GrammarSummaryFilter {
    private val db = MVStore.Builder()
            .fileName(grammarSummaryFilter.absolutePath)
            .compress()
            .open()!!
    private val grammarSummary = db.openMap<String, IntSet>(
            "GrammarSummaryFilter")

    val grammarSummaries: Map<String, IntSet> = grammarSummary

    init {
        if (grammarSummary.isEmpty()) {
            val tokenize = KuromojiIpadicCache.tokenize
            val map = mutableMapOf<String, IntSet>()
            for (index in sentenceIndexRange()) {
                val sentence = sentenceIndexToTranslatedSentence(index)
                val tokenization = tokenize(sentence.japanese)
                val summary = tokenization.grammarSummaryForm()
                for (element in summary) {
                    val filter = map[element] ?: intSetOf()
                    filter += index
                    map[element] = filter
                }
            }
            map.entries
                    .forEach { (skeleton, ix) ->
                        grammarSummary[skeleton] = ix
                    }

            db.compactRewriteFully()
            save()
            KuromojiIpadicCache.save()
        }
    }

    companion object {
        private var instance: GrammarSummaryFilter? = null
        val filterOf: GrammarSummaryFilter
            get() {
                if (instance != null) {
                    return instance!!
                }
                instance = GrammarSummaryFilter()
                return instance!!
            }

        fun save() {
            if (instance != null) {
                instance!!.db.close()
                instance = null
            }
        }
    }
}