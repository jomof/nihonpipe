package com.jomof.nihonpipe.groveler.datafiles

import com.jomof.intset.IntSet
import com.jomof.intset.intersect
import com.jomof.nihonpipe.groveler.schema.KeySentences
import com.jomof.nihonpipe.groveler.schema.KuromojiIpadicTokenization
import com.jomof.nihonpipe.groveler.wanikaniVocabLevelsBin
import org.h2.mvstore.MVMap
import org.h2.mvstore.MVStore

class WanikaniVocabLevels : LevelProvider {
    override operator fun get(level: Int) = instance[level]!!
    override val size: Int get() = instance.size
    override fun keysOf(tokenization : KuromojiIpadicTokenization): Set<String> {
        return tokenization.tokens
                .map { token -> token.baseForm }
                .toSet()
    }

    companion object {
        private fun create() = MVStore.Builder()
                .fileName(wanikaniVocabLevelsBin.absolutePath!!)
                .compress()
                .open()!!
                .openMap<Int, List<KeySentences>>("WanikaniVocabLevelsBin")

        private fun populate(table: MVMap<Int, List<KeySentences>>) {
            val vocabToSentenceFilter = VocabToSentenceFilter()
            val tokenize = KuromojiIpadicCache.tokenize
            WanikaniVsJlptVocabs
                    .vocabOf
                    .vocabs
                    .entries
                    .filter { (_, info) -> info.wanikaniLevel != 100 }
                    .map { (vocab, info) ->
                        val sentences = vocabToSentenceFilter[vocab]
                        if (sentences.size > 0) {
                            Triple(info.vocab, info.wanikaniLevel, sentences)
                        } else {
                            // Try to parse the vocab
                            val tokenization = tokenize(info.vocab)
                            var acc: IntSet? = null
                            tokenization
                                    .tokens
                                    .map { vocabToSentenceFilter[it.baseForm] }
                                    .forEach { set ->
                                        acc = (acc ?: set) intersect set
                                    }
                            // Some of these will have no matching sentences
                            Triple(info.vocab, info.wanikaniLevel, acc!!)
                        }
                    }
                    .groupBy { (_, level, _) -> level - 1 }
                    .forEach { (level, list) ->
                        table[level] = list
                                .map { (vocab, _, sentences) ->
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