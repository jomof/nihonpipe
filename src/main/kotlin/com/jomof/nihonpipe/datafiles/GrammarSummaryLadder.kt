package com.jomof.nihonpipe.datafiles

import com.jomof.intset.IntSet
import com.jomof.intset.intSetOf
import com.jomof.nihonpipe.grammarSummaryLadderBin
import com.jomof.nihonpipe.groveler.schema.KuromojiIpadicTokenization
import com.jomof.nihonpipe.groveler.schema.grammarSummaryForm
import com.jomof.nihonpipe.schema.KeySentences
import org.h2.mvstore.MVMap
import org.h2.mvstore.MVStore

class GrammarSummaryLadder : LevelProvider {
    override fun getKeySentences(level: Int) = instance.first[level]!!
    override fun getLevelSentences(level: Int) = instance.second[level]!!
    override val size: Int get() = instance.first.size
    fun keysOf(tokenization: KuromojiIpadicTokenization) =
            tokenization.grammarSummaryForm()

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
                    .fileName(grammarSummaryLadderBin.absolutePath!!)
                    .compress()
                    .open()!!
            val keyLevels =
                    db.openMap<Int, List<KeySentences>>("GrammarSummaryKeyLevels")
            val levels =
                    db.openMap<Int, IntSet>("GrammarSummaryLadder")
            return Pair(keyLevels, levels)
        }

        private fun populate(
                table: MVMap<Int, List<KeySentences>>,
                levels: MVMap<Int, IntSet>) {

            GrammarSummaryFilter
                    .filterOf
                    .grammarSummaries
                    .entries
                    .filter { (_, ix) -> ix.size > 5 }
                    .sortedByDescending { (_, ix) ->
                        ix.size
                    }
                    .chunked(2)
                    .forEachIndexed { level, summary ->
                        table[level] = summary
                                .map { (vocab, sentences) -> KeySentences(vocab, sentences) }
                        val accumulatedLevels = intSetOf()
                        for ((_, sentences) in summary) {
                            accumulatedLevels += sentences
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