package com.jomof.nihonpipe.groveler.datafiles

import com.jomof.nihonpipe.groveler.grammarSummaryLevels
import com.jomof.nihonpipe.groveler.schema.KeySentences
import com.jomof.nihonpipe.groveler.schema.KuromojiIpadicTokenization
import com.jomof.nihonpipe.groveler.schema.grammarSummaryForm
import org.h2.mvstore.MVMap
import org.h2.mvstore.MVStore

class GrammarSummaryLevels : LevelProvider {
    override operator fun get(level: Int) = instance[level]!!
    override val size: Int get() = instance.size
    override fun keysOf(tokenization: KuromojiIpadicTokenization) =
            tokenization.grammarSummaryForm()

    companion object {
        private var theTable: MVMap<Int, List<KeySentences>>? = null
        private fun create() = MVStore.Builder()
                .fileName(grammarSummaryLevels.absolutePath!!)
                .compress()
                .open()!!
                .openMap<Int, List<KeySentences>>("GrammarSummaryLevels")

        private fun populate(table: MVMap<Int, List<KeySentences>>) {
            GrammarSummaryFilter
                    .filterOf
                    .grammarSummaries
                    .entries
                    .filter { (_, ix) -> ix.size > 5 }
                    .sortedByDescending { (_, ix) ->
                        ix.size
                    }
                    .chunked(2)
                    .forEachIndexed { index, summary ->
                        table[index] = summary
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