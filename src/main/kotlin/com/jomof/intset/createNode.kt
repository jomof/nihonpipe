package com.jomof.intset

fun createPageNode(startPage: Int, elements: Long): Node {
    return when (java.lang.Long.bitCount(elements)) {
        0 -> EmptyNode.instance
        64 -> AllSetNode(PageRange(startPage, startPage))
        else -> LongPageNode(startPage, arrayOf(elements))
    }
}

fun createPairNode(first: Node, second: Node): Node {
    val (left, right) =
            if (first.pageRange.first > second.pageRange.first) {
                Pair(second, first)
            } else {
                Pair(first, second)
            }
    // If they're adjacent do something smarter
    if (left.pageRange.last == right.pageRange.first - 1) {
        if (left is LongPageNode && right is LongPageNode) {
            return LongPageNode(
                    left.pageRange.first,
                    left.elements + right.elements)
        }
        return if (left is AllSetNode && right is LongPageNode) {
            PairNode(1, left, right)
        } else if (left is AllSetNode && right is AllSetNode) {
            AllSetNode(PageRange(
                    left.pageRange.first,
                    right.pageRange.last))
        } else if (left is PairNode && right is PairNode) {
            PairNode(1, left, right)
        } else if (left.javaClass == right.javaClass) {
            throw RuntimeException()
        } else {
            PairNode(1, left, right)
        }
    }
    return PairNode(1, left, right)
}