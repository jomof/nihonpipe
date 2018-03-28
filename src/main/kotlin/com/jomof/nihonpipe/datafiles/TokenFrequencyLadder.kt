package com.jomof.nihonpipe.datafiles

import com.jomof.algorithm.getsert
import com.jomof.intset.IntSet
import com.jomof.intset.intSetOf
import com.jomof.nihonpipe.groveler.schema.KuromojiIpadicToken
import com.jomof.nihonpipe.schema.KeySentences
import com.jomof.nihonpipe.tokenFrequencyLadderBin
import org.h2.mvstore.MVMap
import org.h2.mvstore.MVStore

class TokenFrequencyLadder : LevelProvider {
    override fun getKeySentences(level: Int) = instance.first[level]!!
    override fun getLevelSentences(level: Int) = instance.second[level]!!
    override val size: Int get() = instance.first.size

    override fun getLevelSizes(): List<Int> {
        return instance
                .first
                .entries
                .sortedBy { it.key }
                .map { it.value.size }
    }

    companion object {
        private var theTable: Pair<
                MVMap<Int, List<KeySentences>>,
                MVMap<Int, IntSet>>? = null

        private fun create(): Pair<
                MVMap<Int, List<KeySentences>>,
                MVMap<Int, IntSet>> {
            val db = MVStore.Builder()
                    .fileName(tokenFrequencyLadderBin.absolutePath!!)
                    .compress()
                    .open()!!
            val keyLevels =
                    db.openMap<Int, List<KeySentences>>("TokenFrequencyLadderKeyLevels")
            val levels =
                    db.openMap<Int, IntSet>("TokenFrequencyLadderLevels")
            return Pair(keyLevels, levels)
        }

        private fun populate(
                table: MVMap<Int, List<KeySentences>>,
                levels: MVMap<Int, IntSet>) {
            val map = mutableMapOf<KuromojiIpadicToken, IntSet>()
            val tokenize = KuromojiIpadicCache.tokenize
            for (index in sentenceIndexRange()) {
                val translated = sentenceIndexToTranslatedSentence(index)
                for (token in tokenize(translated.japanese).tokens) {
                    val value = map.getsert(token) { intSetOf() }
                    value += index
                }
            }

            map
                    .entries
                    .sortedByDescending { it.value.size }
                    .chunked(map.size / 60)
                    .forEachIndexed { level, keys ->
                        table[level] = keys
                                .map { (vocab, sentences) -> KeySentences(vocab.surface, sentences) }
                        val accumulatedLevels = intSetOf()
                        for ((_, sentencesBits) in keys) {
                            accumulatedLevels += sentencesBits
                        }
                        levels[level] = accumulatedLevels
                    }
            table.store.commit()
        }

        val instance: Pair<
                MVMap<Int, List<KeySentences>>,
                MVMap<Int, IntSet>>
            get() {
                if (theTable == null) {
                    val table = create()
                    val (keySentences, levels) = table
                    if (levels.isEmpty()) {
                        populate(keySentences, levels)
                    }
                    theTable = table
                }
                return theTable!!
            }
    }
}