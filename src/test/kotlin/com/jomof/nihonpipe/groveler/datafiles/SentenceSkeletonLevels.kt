package com.jomof.nihonpipe.groveler.datafiles

import com.jomof.nihonpipe.groveler.schema.KeySentences
import com.jomof.nihonpipe.groveler.sentenceSkeletonLevelsBin
import org.h2.mvstore.MVMap
import org.h2.mvstore.MVStore

class SentenceSkeletonLevels {
    operator fun get(level: Int) = instance[level]

    companion object {
        private var theTable: MVMap<Int, List<KeySentences>>? = null
        private fun create() = MVStore.Builder()
                .fileName(sentenceSkeletonLevelsBin.absolutePath!!)
                .compress()
                .open()!!
                .openMap<Int, List<KeySentences>>("SentenceSkeletonLevels")

        private fun populate(table: MVMap<Int, List<KeySentences>>) {
            SentenceSkeletonFilter
                    .filterOf
                    .skeletons
                    .entries
                    .filter { (skeleton, ix) -> ix.size > 5 }
                    .sortedByDescending { (skeleton, ix) ->
                        ix.size
                    }
                    .chunked(5)
                    .forEachIndexed { index, skeletons ->
                        table[index] = skeletons
                                .map { (vocab, sentences) -> KeySentences(vocab, sentences) }
                    }
            table.store.commit()
        }

        val instance: MVMap<Int, List<KeySentences>>
            get() {
                if (theTable == null) {
                    val table = create()
                    if (table.isEmpty()) {
                        populate(table)
                    }
                    theTable = table
                }
                return theTable!!
            }
    }
}