package com.jomof.nihonpipe.play

import com.jomof.intset.IntSet
import com.jomof.intset.intSetOf
import com.jomof.nihonpipe.datafiles.sentenceIndexRange
import com.jomof.nihonpipe.datafiles.sentenceIndexToTranslatedSentence

private val rangeOfCoordinates = 1_000_000 until 2_000_000
private val coordinateMap = mutableMapOf<LadderCoordinate, Int>()
private val coordinateList = mutableMapOf<Int, LadderCoordinate>()
private val sentenceToLadderScoreCoordinates = arrayOfNulls<IntSet?>(
        sentenceIndexRange().count())

private fun populate() {
    if (coordinateMap.isNotEmpty()) {
        return
    }
    var coordinateIndex = rangeOfCoordinates.start
    for (ladderKind in LadderKind.values()) {
        for (level in 0 until ladderKind.levelProvider.size) {
            for (keySentences in ladderKind.levelProvider.getKeySentences(level)) {
                val coordinate = LadderCoordinate(ladderKind, level, keySentences.key)
                coordinateList[coordinateIndex] = coordinate
                coordinateMap[coordinate] = coordinateIndex
                val sentencesSeen = mutableSetOf<Int>()
                for (sentence in keySentences.sentences) {
                    sentencesSeen.add(sentence)
                    val coordinateBits = sentenceToLadderScoreCoordinates[sentence]
                            ?: intSetOf().withRangeLimit(rangeOfCoordinates)
                    coordinateBits.readwrite()
                    coordinateBits.add(coordinateIndex)
                    coordinateBits.readonly()
                    sentenceToLadderScoreCoordinates[sentence] = coordinateBits
                }
                ++coordinateIndex
            }
        }
    }
}

private fun report(sentence: Int): IntSet {
    val sentenceText = sentenceIndexToTranslatedSentence(sentence)
    throw RuntimeException("unknown id=$sentence $sentenceText " +
            "out of size ${sentenceToLadderScoreCoordinates.size}")
}

fun ladderCoordinateIndexesOfSentence(sentence: Int): IntSet {
    assert(sentence !in rangeOfCoordinates)
    populate()
    return sentenceToLadderScoreCoordinates[sentence] ?: report(sentence)
}

fun ladderCoordinateOfLadderCoordinateIndex(coordinateIndex: Int): LadderCoordinate {
    assert(coordinateIndex in rangeOfCoordinates)
    populate()
    return coordinateList[coordinateIndex]!!
}