package com.jomof.nihonpipe.play

import com.jomof.intset.IntSet
import com.jomof.intset.intSetOf
import com.jomof.nihonpipe.datafiles.TranslatedSentences

class ScoreCoordinateIndex {
    fun get(sentence: Int) = sentenceToScoreCoordinate[sentence]
    val sentences: Array<IntSet> get () = sentenceToScoreCoordinate
    val coordinates: List<ScoreCoordinate> get() = coordinateList
    val coordinateToIndex: Map<ScoreCoordinate, Int> = coordinateMap

    companion object {
        private val coordinateMap = mutableMapOf<ScoreCoordinate, Int>()
        private val coordinateList = mutableListOf<ScoreCoordinate>()
        private val sentenceToScoreCoordinate = Array(TranslatedSentences().sentences.keys.size) { _ ->
            intSetOf()
        }

        init {
            var scoreCoordinateIndex = 0
            LadderKind.forEachPossibleCoordinate { scoreCoordinate, sentences ->
                coordinateMap[scoreCoordinate] = scoreCoordinateIndex
                coordinateList.add(scoreCoordinate)
                for (sentence in sentences) {
                    val coordinates = sentenceToScoreCoordinate[sentence]
                    coordinates += scoreCoordinateIndex
                }
                ++scoreCoordinateIndex
            }
        }
    }
}