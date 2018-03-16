package com.jomof.nihonpipe.groveler.datafiles

import com.jomof.intset.IntSet
import com.jomof.intset.intersect
import com.jomof.nihonpipe.groveler.schema.KeySentences
import com.jomof.nihonpipe.groveler.wanikaniVocabLevelsBin
import org.h2.mvstore.MVMap
import org.h2.mvstore.MVStore

class WanikaniVocabLevels {
    operator fun get(level: Int) = instance[level]

    companion object {
        private fun create() = MVStore.Builder()
                .fileName(wanikaniVocabLevelsBin.absolutePath!!)
                .compress()
                .open()!!
                .openMap<Int, List<KeySentences>>("WanikaniVocabLevelsBin")

        private fun populate(table: MVMap<Int, List<KeySentences>>) {
            val sentencesOf = VocabToSentenceFilter.sentencesOf
            val tokenize = KuromojiIpadicCache.tokenize
            WanikaniVsJlptVocabs
                    .vocabOf
                    .vocabs
                    .entries
                    .filter { (vocab, info) -> info.wanikaniLevel != 100 }
                    .map { (vocab, info) ->
                        val sentences = sentencesOf(vocab)
                        if (sentences.size > 0) {
                            Triple(info.vocab, info.wanikaniLevel, sentences)
                        } else {
                            // Try to parse the vocab
                            val tokenization = tokenize(info.vocab)
                            var acc: IntSet? = null
                            tokenization
                                    .tokens
                                    .map { sentencesOf(it.baseForm) }
                                    .forEach { set ->
                                        acc = (acc ?: set) intersect set
                                    }
                            // Some of these will have no matching sentences
                            Triple(info.vocab, info.wanikaniLevel, acc!!)
                        }
                    }
                    .groupBy { (vocab, level, sentences) -> level - 1 }
                    .forEach { (level, list) ->
                        table[level] = list
                                .map { (vocab, level, sentences) ->
                                    KeySentences(vocab, sentences)
                                }
                    }

            table.store.commit()
        }

        private var theTable: MVMap<Int, List<KeySentences>>? = null
        private val instance: MVMap<Int, List<KeySentences>>
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