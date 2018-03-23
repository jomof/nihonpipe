package com.jomof.nihonpipe.datafiles

import com.jomof.intset.IntSet
import com.jomof.intset.intSetOf
import com.jomof.nihonpipe.jlptVocabLadderBin
import com.jomof.nihonpipe.schema.Jlpt
import com.jomof.nihonpipe.schema.KeySentences
import org.h2.mvstore.MVMap
import org.h2.mvstore.MVStore

class JlptVocabLadder : LevelProvider {
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
        private fun create(): Pair<
                MVMap<Int, List<KeySentences>>,
                MVMap<Int, IntSet>> {
            val db = MVStore.Builder()
                    .fileName(jlptVocabLadderBin.absolutePath!!)
                    .compress()
                    .open()!!
            val keyLevels =
                    db.openMap<Int, List<KeySentences>>("JlptKeyLevels")
            val levels =
                    db.openMap<Int, IntSet>("JlptLevels")
            return Pair(keyLevels, levels)
        }

        private fun populate(
                keySentences: MVMap<Int, List<KeySentences>>,
                levels: MVMap<Int, IntSet>) {
            val vocabToSentenceFilter = VocabToSentenceFilter()
            val allSentences = intSetOf()
            val groupsOfJlptLevel =
                    WanikaniVsJlptVocabs
                            .vocabOf
                            .vocabs
                            .entries
                            .map { (vocab, info) ->
                                val sentences = vocabToSentenceFilter[vocab]
                                Triple(info.vocab, info.jlptLevel, sentences)
                            }
                            .filter { it.third.size > 0 }
                            .groupBy { (_, level, _) -> level }
                            .toList()
                            .sortedBy { it.first }
            val levelsPerJlptLevel = 60 / Jlpt.values().size
            groupsOfJlptLevel
                    .map { (jlpt, group) ->
                        group
                                .chunked(group.size / levelsPerJlptLevel)
                                .map { levelElements ->
                                    levelElements.map { (key, _, sentences) ->
                                        KeySentences(key, sentences)
                                    }

                                }
                    }
                    .flatten()
                    .mapIndexed { level, keySentences ->
                        Pair(level, keySentences)
                    }
                    .onEach { (level, list) ->
                        keySentences[level] = list
                    }
                    .onEach { (level, list) ->
                        val accumulatedLevels = intSetOf()
                        for ((_, sentences) in list) {
                            accumulatedLevels += sentences
                            allSentences += sentences
                        }
                        levels[level] = accumulatedLevels
                    }

            val unmappedSentences = intSetOf()
            (0 until TranslatedSentences().sentences.size).forEach {
                if (!allSentences.contains(it)) {
                    unmappedSentences += it
                }
            }
            if (unmappedSentences.size > 0) {
                // Put the rest of the sentences in a final level
                val catchAllLevel = levels.size
                levels[catchAllLevel] = unmappedSentences
                keySentences[catchAllLevel] = listOf(
                        KeySentences("everything-but-wanikani", unmappedSentences))
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