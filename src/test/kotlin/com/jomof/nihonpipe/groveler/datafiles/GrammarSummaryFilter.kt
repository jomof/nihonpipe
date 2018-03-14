package com.jomof.nihonpipe.groveler.datafiles

import com.jomof.intset.intSetOf
import com.jomof.nihonpipe.groveler.grammarSummaryFilter
import com.jomof.nihonpipe.groveler.schema.grammarSummaryForm
import org.h2.mvstore.MVStore

class GrammarSummaryFilter {
    private val db = MVStore.Builder()
            .fileName(grammarSummaryFilter.absolutePath)
            .compress()
            .open()!!
    private val grammarSummary = db.openMap<String, Set<Int>>(
            "GrammarSummaryFilter")

    init {
        if (grammarSummary.isEmpty()) {
            val tanaka = TanakaCorpusSentences.tanaka
            val tokenize = KuromojiIpadicCache.tokenize
            val map = mutableMapOf<String, MutableSet<Int>>()
            for ((index, sentence) in tanaka.sentences) {
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
            TanakaCorpusSentences.save()
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
                return filterOf
            }

        fun save() {
            if (instance != null) {
                instance!!.db.close()
                instance = null
            }
        }
    }
}