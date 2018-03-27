package com.jomof.nihonpipe.play

import com.jomof.intset.IntSet
import com.jomof.intset.intSetOf
import com.jomof.nihonpipe.datafiles.TranslatedSentences
import com.jomof.nihonpipe.scoreCoordinateIndexBin
import org.h2.mvstore.MVStore

class ScoreCoordinateIndex {
    fun getCoordinatesFromSentence(sentence: Int): IntSet {
        populate()
        return sentenceToScoreCoordinate[sentence] ?: report(sentence)
    }

    fun getCoordinateFromCoordinateIndex(coordinateIndex: Int): ScoreCoordinate {
        populate()
        return coordinateList[coordinateIndex]!!
    }

    private fun report(sentence: Int): IntSet {
        val sentence = TranslatedSentences().sentences[sentence]
        println(sentence)
        throw RuntimeException("unknown $sentence " +
                "out of size ${sentenceToScoreCoordinate.size}")
    }
    companion object {
        private val db = MVStore.Builder()
                .fileName(scoreCoordinateIndexBin.absolutePath)
                .compress()
                .open()!!
        private val coordinateMap = mutableMapOf<ScoreCoordinate, Int>()
        private val coordinateList = mutableMapOf<Int, ScoreCoordinate>()
        private val sentenceToScoreCoordinate = arrayOfNulls<IntSet?>(
                TranslatedSentences().sentences.size)
        private var populating = false
        fun populate() {
            assert(!populating)
            if (coordinateMap.isNotEmpty()) {
                return
            }
            println("populating")
            populating = true
            var coordinateIndex = 0
            for (ladderKind in LadderKind.values()) {
                for (level in 0 until ladderKind.levelProvider.size) {
                    for (keySentences in ladderKind.levelProvider.getKeySentences(level)) {
                        var coordinate = ScoreCoordinate(ladderKind, level, keySentences.key)
                        coordinateList[coordinateIndex] = coordinate
                        coordinateMap[coordinate] = coordinateIndex
                        val sentencesSeen = mutableSetOf<Int>()
                        for (sentence in keySentences.sentences) {
                            assert(!sentencesSeen.contains(sentence))
                            sentencesSeen.add(sentence)
                            assert(sentencesSeen.contains(sentence))
                            val coordinateBits = sentenceToScoreCoordinate[sentence] ?: intSetOf()
                            coordinateBits.readwrite()
                            coordinateBits.add(coordinateIndex)
                            coordinateBits.readonly()
                            sentenceToScoreCoordinate[sentence] = coordinateBits
                        }
                        ++coordinateIndex
                    }
                }
            }
            println("done populating")
            populating = false
        }
    }
}