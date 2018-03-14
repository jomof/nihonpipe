package com.jomof.intset

import kotlin.math.max

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