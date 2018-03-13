package com.jomof.intset

import java.io.ObjectOutput

class EmptyNode private constructor() : Node {
    override val code = NodeCode.EMPTY
    override val size = 0
    override val pageRange = PageRange(0, Int.MAX_VALUE)
    override fun add(
            page: Int,
            offset: Int,
            update: (Node) -> Unit): Boolean {
        update(LongPageNode.of(page, offset))
        return false
    }

    override fun setPage(startPage: Int, elements: Long, update: (Node) -> Unit) {
        update(createPageNode(startPage, elements))
    }

    override fun contains(page: Int, offset: Int) = false
    override fun writeExternal(out: ObjectOutput) {
        out.writeByte(code.ordinal)
    }

    override fun pages() = listOf<Page>().asSequence()

    companion object {
        val instance = EmptyNode()
    }
}