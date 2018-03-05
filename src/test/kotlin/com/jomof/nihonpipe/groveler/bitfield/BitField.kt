package com.jomof.nihonpipe.groveler.bitfield

import java.io.Serializable
import kotlin.coroutines.experimental.buildSequence

data class BitField(
        private var init: String = "",
        private var sizeField: Int = 0) : Serializable {

    val size: Int get() = sizeField
    val segments: Iterable<Segment>
        get() = getBitFieldSegmentsIterable(init)

    private fun copyFrom(segments: List<Segment>) {
        val bf = segments.toBitField()
        this.init = bf.init
        this.sizeField = bf.size
    }

    operator fun set(key: Int, value: Boolean) {
        val segments = segments.toMutableList()
        segments[key] = value
        copyFrom(segments)
    }

    operator fun get(key: Int): Boolean {
        return segments[key]
    }
}

fun BitField.cosegment(
        bitField: BitField,
        action: (range: IntRange, left: Boolean, right: Boolean) -> Unit) {
    val left = segments.iterator()
    val right = bitField.segments.iterator()
    fun nextOrNull(it: Iterator<Segment>): Segment? {
        return if (it.hasNext()) {
            it.next()
        } else {
            null
        }
    }

    var leftSegment = nextOrNull(left)
    var rightSegment = nextOrNull(right)
    while (leftSegment != null || rightSegment != null) {
        if (leftSegment == null && rightSegment == null) {
            return
        } else if (leftSegment == null) {
            action(rightSegment!!.range, false, rightSegment.set)
            rightSegment = nextOrNull(right)
        } else if (rightSegment == null) {
            action(leftSegment.range, leftSegment.set, false)
            leftSegment = nextOrNull(left)
        } else {

            val (leftRange, leftSet) = leftSegment
            val (rightRange, rightSet) = rightSegment
            assert(leftRange.start == rightRange.start)

            when {
                leftRange.last == rightRange.last -> {
                    action(leftRange, leftSet, rightSet)
                    leftSegment = nextOrNull(left)
                    rightSegment = nextOrNull(right)
                }
                leftRange.last > rightRange.last -> {
                    action(rightRange, leftSet, rightSet)
                    leftSegment = Segment(rightRange.last + 1..leftRange.last, leftSet)
                    rightSegment = nextOrNull(right)
                }
                else -> {
                    action(leftRange, leftSet, rightSet)
                    rightSegment = Segment(leftRange.last + 1..rightRange.last, rightSet)
                    leftSegment = nextOrNull(left)
                }
            }
        }
    }
}

infix fun BitField.and(bitField: BitField): BitField {
    val result = mutableListOf<Segment>()
    this.cosegment(bitField) { range, left, right ->
        result.add(Segment(range, left && right))
    }
    return result.toBitField()
}

infix fun BitField.minus(bitField: BitField): BitField {
    val result = mutableListOf<Segment>()
    this.cosegment(bitField) { range, left, right ->
        result.add(Segment(range, left && !right))
    }
    return result.toBitField()
}

fun BitField.toSetBitIndices(): Sequence<Int> = buildSequence {
    for ((range, set) in segments) {
        if (set) {
            for (index in range) {
                yield(index)
            }
        }
    }
}
