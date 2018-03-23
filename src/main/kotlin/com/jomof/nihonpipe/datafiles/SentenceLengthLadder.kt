package com.jomof.nihonpipe.datafiles

import com.jomof.intset.IntSet
import com.jomof.intset.intSetOf
import com.jomof.nihonpipe.schema.KeySentences
import com.jomof.nihonpipe.sentenceLengthLadder
import org.h2.mvstore.MVMap
import org.h2.mvstore.MVStore

class SentenceLengthLadder : LevelProvider {
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
                    .fileName(sentenceLengthLadder.absolutePath!!)
                    .compress()
                    .open()!!
            val keyLevels =
                    db.openMap<Int, List<KeySentences>>("SentenceLengthKeyLevels")
            val levels =
                    db.openMap<Int, IntSet>("SentenceLengthLadder")
            return Pair(keyLevels, levels)
        }

        private fun populate(
                table: MVMap<Int, List<KeySentences>>,
                levels: MVMap<Int, IntSet>) {
            TranslatedSentences()
                    .sentences
                    .entries
                    .sortedBy { it.value.japanese.length }
                    .chunked(TranslatedSentences().sentences.size / 60)
                    .mapIndexed { level, sentences ->
                        val sentenceSet = intSetOf()
                        sentences.forEach { (index, _) ->
                            sentenceSet += index
                        }
                        val keySentences = KeySentences("length-$level", sentenceSet)
                        listOf(keySentences)
                    }
                    .forEachIndexed { level, summary ->
                        table[level] = summary
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