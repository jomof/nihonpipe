package com.jomof.intset

import java.io.ObjectInput
import java.io.ObjectOutput

class LongPageNode(
        startPage: Int,
        var elements: Array<Long>) : Node {
    init {
        assert(!elements.all { it == -1L })
        assert(!elements.all { it == 0L })
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
        this.elements[index] = elements or bit
        when (size) {
            0 -> update(EmptyNode.instance)
            64 * pageRange.count -> update(AllSetNode(pageRange))
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
                LongPageNode(page, arrayOf(bitOf(offset)))

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