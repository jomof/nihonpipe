package com.jomof.nihonpipe.play

import com.jomof.algorithm.getsert
import com.jomof.intset.forEachElement

private val cognitiveBurdenMap = mutableMapOf<Int, Int>()

private fun calculateBurden(sentence: Int): Int {
    val counts = mutableMapOf<String, Int>()
    val costs = mutableMapOf<String, Int>()
    val coordinateIndex = ScoreCoordinateIndex()
    val allReasons = coordinateIndex.getCoordinatesFromSentence(sentence)
    val reasonList = mutableListOf<ScoreCoordinate>()
    allReasons.forEachElement { reason ->
        reasonList += coordinateIndex
                .getCoordinateFromCoordinateIndex(reason)
    }
    val grouped = reasonList.groupBy { it.ladderKind }
    assert(grouped.size == LadderKind.values().size) {
        "Each ladder should be represented"
    }
    reasonList.forEach { (ladder, level, key) ->
        val count = counts.getsert(key) { 0 }
        val cost = costs.getsert(key) { 0 }
        counts[key] = count + 1
        costs[key] = cost + (level + 1) * (level + 1) * ladder.levelsPerMezzo
    }
    return counts.keys.sumBy { key ->
        costs[key]!! / counts[key]!!
    }
}

/**
 * Calculate the absolute cognitive burden of the given sentence.
 */
fun absoluteBurdenOfSentence(sentence: Int) = cognitiveBurdenMap.getsert(sentence) {
    calculateBurden(sentence)
}
