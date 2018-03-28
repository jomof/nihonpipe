package com.jomof.nihonpipe.play

import com.jomof.intset.IntSet
import com.jomof.intset.intSetOf
import com.jomof.nihonpipe.datafiles.TranslatedSentences

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
        val sentenceText = TranslatedSentences().sentences[sentence]
        throw RuntimeException("unknown id=$sentence $sentenceText " +
                "out of size ${sentenceToScoreCoordinate.size}")
    }
    companion object {
        val rangeOfCoordinates = 1_000_000..2_000_000
        private val coordinateMap = mutableMapOf<ScoreCoordinate, Int>()
        private val coordinateList = mutableMapOf<Int, ScoreCoordinate>()
        private val sentenceToScoreCoordinate = arrayOfNulls<IntSet?>(
                TranslatedSentences().sentences.size)
        fun populate() {
            if (coordinateMap.isNotEmpty()) {
                return
            }
            var coordinateIndex = rangeOfCoordinates.start
            for (ladderKind in LadderKind.values()) {
                for (level in 0 until ladderKind.levelProvider.size) {
                    for (keySentences in ladderKind.levelProvider.getKeySentences(level)) {
                        var coordinate = ScoreCoordinate(ladderKind, level, keySentences.key)
                        coordinateList[coordinateIndex] = coordinate
                        coordinateMap[coordinate] = coordinateIndex
                        val sentencesSeen = mutableSetOf<Int>()
                        for (sentence in keySentences.sentences) {
                            sentencesSeen.add(sentence)
                            val coordinateBits = sentenceToScoreCoordinate[sentence]
                                    ?: intSetOf().withRangeLimit(rangeOfCoordinates)
                            coordinateBits.readwrite()
                            coordinateBits.add(coordinateIndex)
                            coordinateBits.readonly()
                            sentenceToScoreCoordinate[sentence] = coordinateBits
                        }
                        ++coordinateIndex
                    }
                }
            }
        }
    }
}