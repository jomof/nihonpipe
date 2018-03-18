package com.jomof.intset

import java.io.ObjectInput
import java.io.ObjectOutput

class AllSetNode(
        override val pageRange: PageRange) : Node {
    override fun setPage(
            startPage: Int,
            elements: Long,
            update: (Node) -> Unit) {
        val new = createPageNode(startPage, elements)
        if (startPage in pageRange) {
            update(new)
        } else {
            update(createPairNode(this, new))
        }
    }

    override val code: NodeCode
        get() =
            if (pageRange.count == 1) {
                NodeCode.ALL_SET
            } else {
                NodeCode.MULTI_PAGE_ALL_SET
            }

    override fun add(page: Int, offset: Int, update: (Node) -> Unit): Boolean {
        if (page !in pageRange) {
            update(createPairNode(this, LongPageNode.of(page, offset)))
            return false
        }
        return true
    }

    override fun contains(page: Int, offset: Int) = page in pageRange
    override val size: Int get() = pageRange.count * 64

    override fun writeExternal(out: ObjectOutput) {
        out.writeByte(code.ordinal)
        out.writeInt(pageRange.first)
        if (pageRange.count > 1) {
            out.writeInt(pageRange.last)
        }
    }

    override fun pages(): Sequence<Page> =
            (pageRange.first..pageRange.last)
                    .map { Page(it, -1) }
                    .asSequence()

    companion object {
        fun of(code: NodeCode, i: ObjectInput): AllSetNode {
            val startPage = i.readInt()
            if (code == NodeCode.MULTI_PAGE_ALL_SET) {
                val endPage = i.readInt()
                return AllSetNode(PageRange(startPage, endPage))
            }
            return AllSetNode(PageRange(startPage, startPage))
        }
    }


    override fun toString() = "all set in $pageRange"
}