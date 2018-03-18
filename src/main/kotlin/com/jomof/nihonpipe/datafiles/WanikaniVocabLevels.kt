package com.jomof.nihonpipe.datafiles

import com.jomof.intset.IntSet
import com.jomof.intset.intSetOf
import com.jomof.intset.intersect
import com.jomof.intset.union
import com.jomof.nihonpipe.groveler.schema.KuromojiIpadicTokenization
import com.jomof.nihonpipe.schema.KeySentences
import com.jomof.nihonpipe.wanikaniVocabLevelsBin
import org.h2.mvstore.MVMap
import org.h2.mvstore.MVStore

class WanikaniVocabLevels : LevelProvider {
    override fun getKeySentences(level: Int) = instance.first[level]!!
    override fun getSentences(level: Int) = instance.second[level]!!
    override val size: Int get() = instance.first.size
    override fun keysOf(tokenization: KuromojiIpadicTokenization): Set<String> {
        return tokenization.tokens
                .map { token -> token.baseForm }
                .toSet()
    }

    companion object {
        private fun create(): Pair<
                MVMap<Int, List<KeySentences>>,
                MVMap<Int, IntSet>> {
            val db = MVStore.Builder()
                    .fileName(wanikaniVocabLevelsBin.absolutePath!!)
                    .compress()
                    .open()!!
            val keyLevels =
                    db.openMap<Int, List<KeySentences>>("WanikaniKeyLevels")
            val levels =
                    db.openMap<Int, IntSet>("WanikaniLevels")
            return Pair(keyLevels, levels)
        }

        private fun populate(
                keySentences: MVMap<Int, List<KeySentences>>,
                levels: MVMap<Int, IntSet>) {
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
                    .onEach { (level, list) ->
                        keySentences[level] = list
                                .map { (vocab, _, sentences) ->
                                    KeySentences(vocab, sentences)
                                }
                    }
                    .onEach { (level, list) ->
                        var result = intSetOf()
                        for ((_, _, sentences) in list) {
                            result = result union sentences
                        }
                        levels[level] = result
                    }
            keySentences.store.commit()
        }

        private var theTable: Pair<
                MVMap<Int, List<KeySentences>>,
                MVMap<Int, IntSet>>? = null
        private val instance: Pair<
                MVMap<Int, List<KeySentences>>,
                MVMap<Int, IntSet>>
            get() {
                if (theTable == null) {
                    val tables = create()
                    val (keySentences, levels) = tables
                    if (levels.isEmpty()) {
                        populate(keySentences, levels)
                    }
                    theTable = tables
                }
                return theTable!!
            }
    }
}