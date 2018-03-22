package com.jomof.nihonpipe.datafiles

import com.jomof.intset.IntSet
import com.jomof.intset.intSetOf
import com.jomof.nihonpipe.schema.Jlpt
import com.jomof.nihonpipe.schema.KeySentences
import com.jomof.nihonpipe.sentenceFrequencyLevelsBin
import org.h2.mvstore.MVMap
import org.h2.mvstore.MVStore
import java.lang.Character.UnicodeBlock.*
import kotlin.math.max

class SentenceFrequencyLevels : LevelProvider {

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
                    .fileName(sentenceFrequencyLevelsBin.absolutePath!!)
                    .compress()
                    .open()!!
            val keyLevels =
                    db.openMap<Int, List<KeySentences>>("SentenceFrequencyKeyLevels")
            val levels =
                    db.openMap<Int, IntSet>("SentenceFrequencyLevels")
            return Pair(keyLevels, levels)
        }

        fun frequencyOrder(index: Int): Long {
            val sentence = TranslatedSentences()
                    .sentences[index]!!
            val summarize = SentenceStatisticsCache.summarize
            val (japanese, english) = sentence
            val summary = summarize(japanese)
            var frequency = 0L

            // Things like Katakana won't have been analyzed for frequency
            // so make a rough estimate by counting the number of katakana
            // (and some others)
            frequency = frequency shl 8
            for (c in japanese) {
                val block = Character.UnicodeBlock.of(c)
                frequency += when (block) {
                // Kanji
                    CJK_UNIFIED_IDEOGRAPHS -> 0
                // Things like '㌘'
                    CJK_COMPATIBILITY -> 0
                // Hiragana
                    HIRAGANA -> 0
                // Punctuation
                    CJK_SYMBOLS_AND_PUNCTUATION -> 0
                // Katakana
                    KATAKANA -> 1
                // Things like '２'
                    HALFWIDTH_AND_FULLWIDTH_FORMS -> 1
                // Things like '−'
                    MATHEMATICAL_OPERATORS -> 1
                // Things like '―'
                    GENERAL_PUNCTUATION -> 1
                // Things like '○' (what the)
                    GEOMETRIC_SHAPES -> 128
                // Things like '♪' (what the)
                    MISCELLANEOUS_SYMBOLS -> 128
                // Things like '①' (what the)
                    ENCLOSED_ALPHANUMERICS -> 128
                // Things like '→' (what the)
                    ARROWS -> 128
                // Things like 'β' (what the)
                    GREEK -> 128
                // Things like '℃' (what the)
                    LETTERLIKE_SYMBOLS -> 128
                // Basic latin
                    BASIC_LATIN -> when (c) {
                        ' ' -> 0
                        else -> 1
                    }
                    else -> throw RuntimeException("'$c' = $block")
                }
            }

            frequency = (frequency shl 15) + if (summary.optCore.max < 0) {
                (1 shl 15) - 1
            } else {
                summary.optCore.max.toLong()
            }
            val jlptMax = max(summary.jishoJlpt.max, summary.optCoreJlpt.max)
            frequency = (frequency shl 3) + if (jlptMax < 0 || jlptMax > Jlpt.values().size) {
                (1 shl 3) - 1
            } else {
                val result = Jlpt.values().size - jlptMax
                result
            }
            val jlptMin = max(summary.jishoJlpt.min, summary.optCoreJlpt.min)
            frequency = (frequency shl 3) + if (jlptMin < 0 || jlptMin > Jlpt.values().size) {
                (1 shl 3) - 1
            } else {
                val result = Jlpt.values().size - jlptMin
                result
            }
            frequency = (frequency shl 15) + japanese.length
            frequency = (frequency shl 15) + english.length
            assert(frequency > 0)
            return frequency
        }

        private fun populate(
                table: MVMap<Int, List<KeySentences>>,
                levels: MVMap<Int, IntSet>) {
            val translatedSentences = TranslatedSentences().sentences
            val chunkSize = translatedSentences.size / 60
            translatedSentences
                    .entries
                    .map { (index, _) ->

                        Pair(index, frequencyOrder(index))
                    }
                    .sortedBy { (_, frequency) -> frequency }
                    .chunked(chunkSize)
                    .dropLast(1) // Drop the last level because it is proper shit
                    .mapIndexed { level, sentences ->
                        sentences
                                .chunked(sentences.size / 30) // Subchunks per level
                                .mapIndexed { chunkNumber, chunkSentences ->
                                    val all = intSetOf()
                                    for ((sentence, _) in chunkSentences) {
                                        all += sentence
                                    }
                                    KeySentences(
                                            "frequency-group-$level-$chunkNumber",
                                            all)
                                }
                    }
                    .forEachIndexed { level, keySentences ->
                        table[level] = keySentences
                        val accumulatedLevels = intSetOf()
                        keySentences.forEach { (_, sentences) ->
                            accumulatedLevels += sentences
                        }
                        levels[level] = accumulatedLevels
                    }
            println("levels = ${levels.size}")
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