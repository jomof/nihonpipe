package com.jomof.nihonpipe.play

import com.jomof.intset.IntSet
import com.jomof.intset.intSetOf
import com.jomof.nihonpipe.datafiles.TranslatedSentences

class ScoreCoordinateIndex {
    fun get(sentence: Int) = arr[sentence]
    val sentences: Array<IntSet>
        get () = arr
    val coordinates: List<ScoreCoordinate>
        get() = coordinateList

    companion object {
        private var scoreCoordinateIndex = 0
        private val coordinateMap = mutableMapOf<ScoreCoordinate, Int>()
        private val coordinateList = mutableListOf<ScoreCoordinate>()
        private val arr = Array(TranslatedSentences().sentences.keys.size) { _ ->
            intSetOf()
        }

        init {
            LadderKind.forEachPossibleCoordinate { scoreCoordinate, sentences ->
                coordinateMap[scoreCoordinate] = scoreCoordinateIndex
                coordinateList.add(scoreCoordinate)
                for (sentence in sentences) {
                    val coordinates = arr[sentence]
                    coordinates += scoreCoordinateIndex
                }
                ++scoreCoordinateIndex
            }
        }
    }
}