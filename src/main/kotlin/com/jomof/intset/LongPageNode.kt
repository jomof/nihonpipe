package com.jomof.intset

import java.io.ObjectInput
import java.io.ObjectOutput

class LongPageNode(
        startPage: Int,
        var elements: Array<Long>,
        endPage: Int? = null) : Node {
    init {
//        assert(!elements.all { it == -1L })
//        assert(!elements.all { it == 0L })
//        assert(elements.map(
//                java.lang.Long::bitCount).sum() < elements.size * 64)
        if (endPage != null) {
            assert(endPage - startPage + 1 == elements.size)
        }
    }
    override val code: NodeCode
        get() = if (elements.size == 1) {
            NodeCode.LONG_PAGE
        } else {
            NodeCode.MULTI_LONG_PAGE
        }

    override fun writeExternal(out: ObjectOutput) {
        out.writeByte(code.ordinal)
        out.writeInt(pageRange.first)
        if (code == NodeCode.MULTI_LONG_PAGE) {
            out.writeInt(elements.size)
        }
        elements.forEach { out.writeLong(it) }
    }

    override val pageRange: PageRange = PageRange(
            startPage,
            startPage + elements.size - 1)

    override fun add(
            page: Int,
            offset: Int,
            update: (Node) -> Unit): Boolean {
        if (page !in pageRange) {
            update(createPairNode(
                    LongPageNode.of(page, offset),
                    this))
            return false
        }
        val index = page - pageRange.first
        val elements = elements[index]
        val bit = bitOf(offset)
        val prior = (elements and bit) != 0L
        val combined = elements or bit
        when (java.lang.Long.bitCount(combined)) {
            64 -> {
                var node: Node = AllSetNode(PageRange(page, page))
                val leftSize = page - pageRange.first
                val rightSize = pageRange.last - page
                val rightElementStart = page - pageRange.first + 1
                assert(leftSize >= 0)
                assert(rightSize >= 0)
                assert(leftSize + rightSize + 1 == pageRange.count)
                assert(leftSize + rightSize + 1 == this.elements.size)
                if (leftSize > 0) {
                    val array = Array(leftSize) { pageNumber ->
                        this.elements[pageNumber]
                    }
                    val left = createPageNode(
                            pageRange.first,
                            array,
                            page - 1)
                    node = createPairNode(left, node)
                }
                if (rightSize > 0) {
                    val array = Array(rightSize) { pageNumber ->
                        this.elements[rightElementStart + pageNumber]
                    }
                    val right = createPageNode(
                            page + 1,
                            array,
                            pageRange.last)
                    node = createPairNode(node, right)
                }
                assert(node.pageRange.count == pageRange.count)
                update(node)
            }
            else -> this.elements[index] = combined
        }
        return prior
    }

    override fun setPage(
            startPage: Int,
            elements: Long,
            update: (Node) -> Unit) {
        if (startPage in pageRange) {
            val index = startPage - pageRange.first
            this.elements[index] = elements
        } else {
            update(createPairNode(
                    this, createPageNode(
                    startPage, arrayOf(elements))))
        }
    }


    override fun contains(page: Int, offset: Int): Boolean {
        if (page !in pageRange) {
            return false
        }
        val index = page - pageRange.first
        return (elements[index] and bitOf(offset)) != 0L
    }

    override val size: Int
        get() =
            elements.map(java.lang.Long::bitCount).sum()

    override fun pages(): Sequence<Page> =
            elements
                    .mapIndexed { index, elements ->
                        Page(pageRange.first + index, elements)
                    }
                    .asSequence()

    override fun toString() = "$size in $pageRange"

    companion object {

        fun of(page: Int, offset: Int) =
                createPageNode(page, arrayOf(bitOf(offset)), page)

        fun of(code: NodeCode, i: ObjectInput): LongPageNode {
            val page = i.readInt()
            val elementCount =
                    if (code == NodeCode.MULTI_LONG_PAGE) {
                        i.readInt()
                    } else {
                        1
                    }
            val elements = (0 until elementCount)
                    .map { i.readLong() }
                    .toTypedArray()
            return LongPageNode(page, elements)
        }
    }
}