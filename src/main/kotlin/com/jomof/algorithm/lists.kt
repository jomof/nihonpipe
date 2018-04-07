package com.jomof.algorithm

import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

data class StatisticsAccumulator(
        var count: Double = 0.0,
        var min: Double = 0.0,
        var max: Double = 0.0,
        var sum: Double = 0.0,
        val all: MutableList<Double> = mutableListOf()
) {
    fun plus(value: Double) {
        ++count
        min = min(min, value)
        max = max(max, value)
        sum += value
        all += value
    }

    fun plus(value: Int) {
        plus(value.toDouble())
    }

    override fun toString(): String {
        all.sort()
        val median = all[(count / 2).toInt()]
        return "[n=$count min=$min median=$median avg=${round(sum / count)} max=$max]"
    }

}

fun <T> Iterable<T>.gatherListStatistics(accumulator: StatisticsAccumulator): List<T> {
    val list = toList()
    accumulator.plus(list.size)
    return list
}


fun <T, V : Comparable<V>> Iterable<T>.takeMinBy(value: (T) -> V): Iterable<T> {
    var v: V? = null
    return sortedBy(value)
            .takeWhile { it ->
                val current = value(it)
                if (v == null) {
                    v = current
                    true
                } else {
                    v!! == value
                }
            }
}

fun <T, V : Comparable<V>> Iterable<T>.takeMaxBy(value: (T) -> V): Iterable<T> {
    var v: V? = null
    val sorted = sortedByDescending(value)
    val result = sorted
            .takeWhile { it ->
                val current = value(it)
                if (v == null) {
                    v = current
                    true
                } else {
                    val same = v!! == current
                    same

                }
            }
    return result
}
