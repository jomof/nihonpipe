package com.jomof.nihonpipe.groveler.datafiles

import com.jomof.intset.IntSet
import com.jomof.intset.intSetOf
import com.jomof.nihonpipe.groveler.vocabToSentenceFilter
import org.h2.mvstore.MVMap
import org.h2.mvstore.MVStore

class VocabToSentenceFilter {
    operator fun get(vocab: String) = VocabToSentenceFilter.instance[vocab] ?: intSetOf()

    companion object {
        private fun create() = MVStore.Builder()
                .fileName(vocabToSentenceFilter.absolutePath!!)
                .compress()
                .open()!!
                .openMap<String, IntSet>("VocabToSentenceFilter")

        private fun populate(table: MVMap<String, IntSet>) {
            val tanaka = TranslatedSentences.sentences
            val tokenize = KuromojiIpadicCache.tokenize
            val map = mutableMapOf<String, IntSet>()
            for ((index, sentence) in tanaka.sentences) {
                val tokenization = tokenize(sentence.japanese)
                for (token in tokenization.tokens) {
                    val filter = map[token.baseForm] ?: intSetOf()
                    filter += index
                    map[token.baseForm] = filter
                }
            }

            map.entries
                    .forEach { (vocab, ix) ->
                        table[vocab] = ix
                    }

            table.store.commit()
        }

        private var theTable: MVMap<String, IntSet>? = null
        private val instance: MVMap<String, IntSet>
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