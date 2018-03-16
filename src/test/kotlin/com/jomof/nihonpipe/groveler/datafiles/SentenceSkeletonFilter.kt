package com.jomof.nihonpipe.groveler.datafiles

import com.jomof.intset.IntSet
import com.jomof.intset.intSetOf
import com.jomof.nihonpipe.groveler.schema.particleSkeletonForm
import com.jomof.nihonpipe.groveler.sentenceSkeletonFilter
import org.h2.mvstore.MVStore

class SentenceSkeletonFilter {
    private val db = MVStore.Builder()
            .fileName(sentenceSkeletonFilter.absolutePath)
            .compress()
            .open()!!
    private val skeletonFilter = db.openMap<String, IntSet>(
            "SkeletonFilter")

    val skeletons: Map<String, IntSet> = skeletonFilter

    init {
        if (skeletonFilter.isEmpty()) {
            val tanaka = TranslatedSentences.tanaka
            val tokenize = KuromojiIpadicCache.tokenize
            val map = mutableMapOf<String, IntSet>()
            for ((index, sentence) in tanaka.sentences) {
                val tokenization = tokenize(sentence.japanese)
                val skeleton = tokenization.particleSkeletonForm()
                val filter = map[skeleton] ?: intSetOf()
                filter += index
                map[skeleton] = filter
            }
            map.entries
                    .forEach { (skeleton, ix) ->
                        skeletonFilter[skeleton] = ix
                    }

            db.compactRewriteFully()
            save()
            TranslatedSentences.save()
            KuromojiIpadicCache.save()
        }
    }

    companion object {
        private var instance: SentenceSkeletonFilter? = null
        val filterOf: SentenceSkeletonFilter
            get() {
                if (instance != null) {
                    return instance!!
                }
                instance = SentenceSkeletonFilter()
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