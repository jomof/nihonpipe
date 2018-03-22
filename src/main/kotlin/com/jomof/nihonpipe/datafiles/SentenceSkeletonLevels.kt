package com.jomof.nihonpipe.datafiles

import com.jomof.intset.IntSet
import com.jomof.intset.intSetOf
import com.jomof.nihonpipe.groveler.schema.KuromojiIpadicTokenization
import com.jomof.nihonpipe.groveler.schema.particleSkeletonForm
import com.jomof.nihonpipe.schema.KeySentences
import com.jomof.nihonpipe.sentenceSkeletonLevelsBin
import org.h2.mvstore.MVMap
import org.h2.mvstore.MVStore

class SentenceSkeletonLevels : LevelProvider {
    override fun getKeySentences(level: Int) = instance.first[level]!!
    override fun getLevelSentences(level: Int) = instance.second[level]!!
    override val size: Int get() = instance.first.size
    fun keysOf(tokenization: KuromojiIpadicTokenization) =
            setOf(tokenization.particleSkeletonForm())

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
                    .fileName(sentenceSkeletonLevelsBin.absolutePath!!)
                    .compress()
                    .open()!!
            val keyLevels =
                    db.openMap<Int, List<KeySentences>>("SentenceSkeletonKeyLevels")
            val levels =
                    db.openMap<Int, IntSet>("SentenceSkeletonLevels")
            return Pair(keyLevels, levels)
        }

        private fun populate(
                table: MVMap<Int, List<KeySentences>>,
                levels: MVMap<Int, IntSet>) {
            val accumulatedLevels = intSetOf()
            val sorted = SentenceSkeletonFilter
                    .filterOf
                    .skeletons
                    .entries
                    .sortedByDescending { (_, ix) ->
                        ix.size
                    }
            var totalSize = 0
            var level = 0
            var keySentences = mutableListOf<KeySentences>()
            var acceptableSize = 115.0
            val growthRate = 1.08
            for ((skeleton, sentences) in sorted) {
                accumulatedLevels += sentences
                keySentences.add(KeySentences(skeleton, sentences))
                totalSize += sentences.size
                if (totalSize > acceptableSize) {
                    table[level] = keySentences
                    levels[level] = accumulatedLevels.copy()
                    accumulatedLevels.clear()
                    keySentences = mutableListOf()
                    totalSize = 0
                    level++
                    acceptableSize *= growthRate
                }
            }
            table[level] = keySentences
            levels[level] = accumulatedLevels.copy()
            table.store.commit()
        }

        val instance: Pair<
                MVMap<Int, List<KeySentences>>,
                MVMap<Int, IntSet>>
            get() {
                if (theTable == null) {
                    val table = create()
                    val (keyLevels, levels) = table
                    if (levels.isEmpty()) {
                        populate(keyLevels, levels)
                    }
                    theTable = table
                }
                return theTable!!
            }
    }
}