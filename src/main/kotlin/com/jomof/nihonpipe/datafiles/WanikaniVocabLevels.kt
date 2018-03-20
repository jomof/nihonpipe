package com.jomof.nihonpipe.datafiles

import com.jomof.intset.IntSet
import com.jomof.intset.intSetOf
import com.jomof.intset.union
import com.jomof.nihonpipe.groveler.schema.KuromojiIpadicTokenization
import com.jomof.nihonpipe.schema.KeySentences
import com.jomof.nihonpipe.wanikaniVocabLevelsBin
import org.h2.mvstore.MVMap
import org.h2.mvstore.MVStore

class WanikaniVocabLevels : LevelProvider {
    override fun getKeySentences(level: Int) = instance.first[level]!!
    override fun getLevelSentences(level: Int) = instance.second[level]!!
    override val size: Int get() = instance.first.size
    override fun keysOf(tokenization: KuromojiIpadicTokenization): Set<String> {
        return tokenization.tokens
                .map { token -> token.baseForm }
                .toSet()
    }

    override fun getLevelSizes(): List<Int> {
        return instance
                .first
                .entries
                .sortedBy { it.key }
                .map { it.value.size }
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
            WanikaniVsJlptVocabs
                    .vocabOf
                    .vocabs
                    .entries
                    .filter { (_, info) -> info.wanikaniLevel != 100 }
                    .map { (vocab, info) ->
                        val sentences = vocabToSentenceFilter[vocab]
                        Triple(info.vocab, info.wanikaniLevel, sentences)
                    }
                    .filter { it.third.size > 0 }
                    .groupBy { (_, level, _) -> level - 1 }
                    .onEach { (level, list) ->
                        keySentences[level] = list
                                .map { (vocab, _, sentences) ->
                                    KeySentences(vocab, sentences)
                                }
                    }
                    .onEach { (level, list) ->
                        var accumulatedLevels = intSetOf()
                        for ((_, _, sentences) in list) {
                            accumulatedLevels = accumulatedLevels union sentences
                        }
                        levels[level] = accumulatedLevels
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