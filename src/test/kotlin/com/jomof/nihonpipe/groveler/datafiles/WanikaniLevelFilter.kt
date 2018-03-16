package com.jomof.nihonpipe.groveler.datafiles

import com.jomof.intset.IntSet
import com.jomof.intset.intSetOf
import com.jomof.nihonpipe.groveler.wanikaniSummaryFilter
import org.h2.mvstore.MVStore

class WanikaniLevelFilter {

    private val db = MVStore.Builder()
            .fileName(wanikaniSummaryFilter.absolutePath)
            .compress()
            .open()!!

    private val wanikaniLevels = db.openMap<Int, IntSet>(
            "WanikaniSummaryFilter")

    operator fun invoke(level: Int) = wanikaniLevels[level]!!

    init {
        if (wanikaniLevels.isEmpty()) {
            val tanaka = TranslatedSentences.tanaka
            val tokenize = KuromojiIpadicCache.tokenize
            val vocabOf = WanikaniVsJlptVocabs.vocabOf
            val map = mutableMapOf<Int, IntSet>()
            for ((index, sentence) in tanaka.sentences) {
                val tokenization = tokenize(sentence.japanese)
                for (token in tokenization.tokens) {
                    val vocab = vocabOf(token.baseForm)
                    if (vocab != null) {
                        val level = vocab.wanikaniLevel
                        val filter = map[level] ?: intSetOf()
                        filter += index
                        map[level] = filter
                    }
                }
            }
            map.entries
                    .forEach { (level, ix) ->
                        wanikaniLevels[level] = ix
                    }

            db.compactRewriteFully()
            save()
            TranslatedSentences.save()
            KuromojiIpadicCache.save()
        }
    }

    companion object {
        private var instance: WanikaniLevelFilter? = null
        val filterOf: WanikaniLevelFilter
            get() {
                if (instance != null) {
                    return instance!!
                }
                instance = WanikaniLevelFilter()
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