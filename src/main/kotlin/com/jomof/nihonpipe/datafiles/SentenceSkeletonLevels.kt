package com.jomof.nihonpipe.datafiles

import com.jomof.intset.IntSet
import com.jomof.intset.intSetOf
import com.jomof.intset.union
import com.jomof.nihonpipe.groveler.schema.KuromojiIpadicTokenization
import com.jomof.nihonpipe.groveler.schema.particleSkeletonForm
import com.jomof.nihonpipe.schema.KeySentences
import com.jomof.nihonpipe.sentenceSkeletonLevelsBin
import org.h2.mvstore.MVMap
import org.h2.mvstore.MVStore

class SentenceSkeletonLevels : LevelProvider {
    override fun getKeySentences(level: Int) = instance.first[level]!!
    override fun getSentences(level: Int) = instance.second[level]!!
    override val size: Int get() = instance.first.size
    override fun keysOf(tokenization: KuromojiIpadicTokenization) =
            setOf(tokenization.particleSkeletonForm())

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
            SentenceSkeletonFilter
                    .filterOf
                    .skeletons
                    .entries
                    .filter { (_, ix) -> ix.size > 5 }
                    .sortedByDescending { (_, ix) ->
                        ix.size
                    }
                    .chunked(5)
                    .forEachIndexed { level, skeletons ->
                        table[level] = skeletons
                                .map { (vocab, sentences)
                                    ->
                                    KeySentences(vocab, sentences)
                                }
                        var result = intSetOf()
                        for ((_, sentences) in skeletons) {
                            result = result union sentences
                        }
                        levels[level] = result
                    }

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