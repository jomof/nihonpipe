package com.jomof.nihonpipe.datafiles

import com.jomof.intset.IntSet
import com.jomof.intset.intSetOf
import com.jomof.nihonpipe.groveler.schema.particleSkeletonForm
import com.jomof.nihonpipe.sentenceSkeletonFilter
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
            val map = mutableMapOf<String, IntSet>()
            for (index in sentenceIndexRange()) {
                val sentence = sentenceIndexToTranslatedSentence(index)
                val tokenization = tokenizeJapaneseSentence(sentence.japanese)
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