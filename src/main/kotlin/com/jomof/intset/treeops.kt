package com.jomof.intset

import kotlin.math.max

infix fun Node.adjacent(other : Node) =
        this.pageRange.last == other.pageRange.first - 1
fun Node.full() : Boolean {
    return when (this) {
        is EmptyNode -> false
        is AllSetNode -> true
        is PairNode -> left.full() and right.full()
        is LongPageNode -> false
        else -> throw RuntimeException()
    }
}

fun Node.empty() : Boolean {
    return when (this) {
        is EmptyNode -> true
        is AllSetNode -> false
        is PairNode -> left.empty() and right.empty()
        is LongPageNode -> false
        else -> throw RuntimeException()
    }
}

fun Node.toAdjacentPages() : List<Long> {
    return when (this) {
        is EmptyNode -> listOf(0)
        is AllSetNode -> listOf(1)
        is PairNode -> {
            assert(left.pageRange.last == right.pageRange.first - 1) {
                "expected adjacent pages"
            }
            left.toAdjacentPages() + right.toAdjacentPages()
        }
        is LongPageNode -> elements.asList()
        else -> throw RuntimeException()
    }
}


fun Node.maxDepth(): Int {
    return when (this) {
        is EmptyNode -> 1
        is AllSetNode -> 1
        is PairNode -> {
            val leftDepth = left.maxDepth()
            val rightDepth = right.maxDepth()
            return 1 + max(leftDepth, rightDepth)
        }
        is LongPageNode -> 1
        else -> throw RuntimeException()
    }
}

fun Node.pageCount(): Int {
    return when (this) {
        is EmptyNode -> 0
        is AllSetNode -> 1
        is PairNode -> left.maxDepth() + right.maxDepth()
        is LongPageNode -> 1
        else -> throw RuntimeException()
    }
}

fun Node.serializationSize(): Int {
    return when (this) {
        is EmptyNode -> 1 // code
        is AllSetNode ->
            1 + // code
                    4 + // first page
                    4   // last page
        is PairNode ->
            1 + // code
                    left.serializationSize() +
                    right.serializationSize()
        is LongPageNode ->
            1 + // code
                    4 + // first page
                    4 + // array size
                    this.pageRange.count * 8 // elements
        else -> throw RuntimeException()
    }
}

fun Node.forEach(action: (Int) -> Unit) {
    return when (this) {
        is EmptyNode -> {
        }
        is AllSetNode -> {
            for (i in pageRange.first * 64 until (pageRange.last + 1) * 64) {
                action(i)
            }
        }
        is PairNode -> {
            left.forEach(action)
            right.forEach(action)
        }
        is LongPageNode -> {
            val firstBit = pageRange.first * 64
            for (bit in pageRange.first * 64 until ((pageRange.last + 1) * 64)) {
                val element = bit - firstBit
                val page = pageOf(element)
                val offset = offsetOf(element)
                val mask = bitOf(offset)
                if ((elements[page] and mask) != 0L) {
                    action(bit)
                }
            }
        }
        else -> throw RuntimeException()
    }
}