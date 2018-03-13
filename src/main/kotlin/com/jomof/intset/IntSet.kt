package com.jomof.intset

import java.io.Externalizable
import java.io.ObjectInput
import java.io.ObjectOutput

class IntSet(
        private var top: Node = EmptyNode.instance)
    : MutableSet<Int>, Externalizable {
    override val size: Int get() = top.size

    override fun readExternal(i: ObjectInput) {
        top = readNode(i)
    }

    override fun writeExternal(out: ObjectOutput) {
        top.writeExternal(out)
    }

    override fun add(element: Int): Boolean {
        return top.add(pageOf(element), offsetOf(element)) {
            top = it
        }
    }

    override fun clear() {
        top = EmptyNode.instance
    }

    override fun contains(element: Int) = top.contains(
            pageOf(element),
            offsetOf(element))

    fun pages(): Iterable<Page> = top.pages().asIterable()

    fun setPage(startPage: Int, elements: Long) {
        top.setPage(startPage, elements) { top = it }
    }

    fun copy(): IntSet {
        val copy = intSetOf()
        pages().forEach { node ->
            copy.setPage(node.number, node.elements)
        }
        return copy
    }

    fun maxDepth(): Int {
        return top.maxDepth()
    }

    fun pageCount(): Int {
        return top.pageCount()
    }

    override fun addAll(elements: Collection<Int>) = TODO("not implemented")
    override fun iterator() = IntSetMutableIterator(this)
    override fun remove(element: Int) = TODO("not implemented")
    override fun removeAll(elements: Collection<Int>) = TODO("not implemented")
    override fun retainAll(elements: Collection<Int>) = TODO("not implemented")
    override fun containsAll(elements: Collection<Int>) = TODO("not implemented")
    override fun isEmpty() = TODO("not implemented")
    override fun toString() = "IntSet(size = $size)"

    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        }
        if (other !is IntSet) {
            return false
        }
        var equal = true
        (this.pages() coiterate other.pages())
                .filter { equal }
                .forEach { (page, left, right) ->
                    equal = left == right
                }
        return equal
    }
}

