package com.jomof.intset

fun createPageNode(
        startPage: Int,
        elements: Array<Long>,
        endPage: Int? = null): Node {
    var bits = elements.sumBy { java.lang.Long.bitCount(it) }
    var result = when (bits) {
        0 -> EmptyNode.instance
        elements.size * 64 -> AllSetNode(
                PageRange(startPage, startPage + elements.size - 1))
        else -> LongPageNode(startPage, elements, endPage)
    }
    if (result !is EmptyNode) {
        assert(result.pageRange.count == elements.size)
    }
    return result
}

fun createPairNode(first: Node, second: Node): Node {
    val (left, right) =
            if (first.pageRange.first > second.pageRange.first) {
                Pair(second, first)
            } else {
                Pair(first, second)
            }
    when(left) {
        is EmptyNode -> return right
        is AllSetNode -> when(right) {
            is AllSetNode -> when {
                left adjacent right ->
                        return AllSetNode(left.pageRange union right.pageRange)
            }
            is LongPageNode -> when {
                right.full() -> return AllSetNode(left.pageRange union right.pageRange)
            }
            is PairNode -> when (right.left) {
                is AllSetNode -> when {
                    left adjacent right.left -> return createPairNode(
                            AllSetNode(left.pageRange union right.left.pageRange),
                            right.right)
                }
            }
        }
        is PairNode -> when {
            left.right adjacent right -> return when {
                left.right is AllSetNode && right is AllSetNode ->
                    createPairNode(
                        left.left,
                        AllSetNode(left.right.pageRange union right.pageRange))
                else ->
                    // Rotate
                    createPairNode(left.left, createPairNode(left.right, right))
            }
        }
        is LongPageNode -> when(right) {
                is PairNode -> when {
                    left.pageRange adjacent right.left.pageRange -> return when(right.left) {
                        is AllSetNode -> PairNode(left, right)
                        else -> createPairNode(createPairNode(left, right.left), right.right)
                    }
                }
                is EmptyNode -> return left
        }
    }

    // If they're adjacent do something smarter
    if (left.pageRange adjacent right.pageRange) {
        if (left.size == left.pageRange.count * 64
                && right.size == right.pageRange.count * 64) {
            return AllSetNode(PageRange(left.pageRange.first, right.pageRange.last))
        }
        if (left.size == left.pageRange.count * 64) {
            return PairNode(AllSetNode(left.pageRange), right)
        }
        if (right.size == right.pageRange.count * 64) {
            return PairNode(left, AllSetNode(right.pageRange))
        }

        val pages = PairNode(left, right).toAdjacentPages()
        return LongPageNode(left.pageRange.first, pages.toTypedArray())
    }
    return PairNode(left, right)
}