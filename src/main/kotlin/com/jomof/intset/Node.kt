package com.jomof.intset

import java.io.ObjectOutput

interface Node {
    val code: NodeCode
    val pageRange: PageRange
    fun add(page: Int, offset: Int, update: (Node) -> Unit): Boolean
    fun setPage(startPage: Int, elements: Long, update: (Node) -> Unit)
    fun contains(page: Int, offset: Int): Boolean
    val size: Int
    fun pages(): Sequence<Page>
    fun writeExternal(out: ObjectOutput)
}