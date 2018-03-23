package com.jomof.intset

import java.io.ObjectInput
import java.io.ObjectOutput
import kotlin.coroutines.experimental.buildSequence

class PairNode(
        first: Node,
        second: Node) : Node {
    var left: Node
    var right: Node
    override val pageRange : PageRange

    init {
        if (first.pageRange.first > second.pageRange.first) {
            left = second
            right = first
        } else {
            left = first
            right = second
        }
        if (left is AllSetNode && right is AllSetNode) {
            throw RuntimeException()
        }
        pageRange = PageRange(left.pageRange.first, right.pageRange.last)
    }

    override fun pages(): Sequence<Page> = buildSequence {
        yieldAll(left.pages())
        yieldAll(right.pages())
    }

    override val code = NodeCode.PAIR

    override fun writeExternal(out: ObjectOutput) {
        out.writeByte(code.ordinal)
        left.writeExternal(out)
        right.writeExternal(out)
    }

    private fun balanced(
            left: Node,
            middle: Node,
            right: Node): Node {
        return createPairNode(left, createPairNode(middle, right))
    }

    override fun add(
            page: Int,
            offset: Int,
            update: (Node) -> Unit): Boolean {
        return when {
            page in left.pageRange -> left.add(page, offset) {
                update(createPairNode(it, right))
            }
            page in right.pageRange -> right.add(page, offset) {
                update(createPairNode(left, it))
            }
            page > left.pageRange.last && page < right.pageRange.first -> {
                val new = LongPageNode.of(page, offset)
                update(balanced(left, new, right))
                false
            }
            page > right.pageRange.last -> {
                val new = LongPageNode.of(page, offset)
                update(balanced(left, right, new))
                false
            }
            page < left.pageRange.first -> {
                val new = LongPageNode.of(page, offset)
                update(balanced(new, left, right))
                false
            }
            else -> throw RuntimeException()
        }
    }

    override fun setPage(
            startPage: Int,
            elements: Long,
            update: (Node) -> Unit) {
        return when {
            startPage in left.pageRange -> left.setPage(startPage, elements) { left = it }
            startPage in right.pageRange -> right.setPage(startPage, elements) { right = it }
            startPage > left.pageRange.last && startPage < right.pageRange.first -> {
                update(balanced(
                        left,
                        LongPageNode(startPage, arrayOf(elements)),
                        right))
            }
            startPage > right.pageRange.last -> {
                update(balanced(
                        left,
                        right,
                        createPageNode(startPage, elements)))
            }
            startPage < left.pageRange.first -> {
                update(balanced(
                        createPageNode(startPage, elements),
                        left,
                        right))
            }
            else -> throw RuntimeException()
        }
    }

    override fun contains(page: Int, offset: Int): Boolean {
        return when (page) {
            in left.pageRange -> left.contains(page, offset)
            in right.pageRange -> right.contains(page, offset)
            else -> false
        }
    }

    override val size: Int
        get() = left.size + right.size

    override fun toString() = "$left, $right"

    companion object {
        fun of(i: ObjectInput): Node {
            val left = readNode(i)
            val right = readNode(i)
            return createPairNode(left, right)
        }
    }

}