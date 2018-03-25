package com.jomof.intset

import java.io.Externalizable
import java.io.ObjectInput
import java.io.ObjectOutput

class IntSet(
        internal var top: Node = EmptyNode.instance)
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

    fun maxDepth() = top.maxDepth()
    fun pageCount() = top.pageCount()
    fun serializationSize() = top.serializationSize()

    override fun addAll(elements: Collection<Int>) : Boolean {
        if (elements is IntSet) {
            (pages() coiterate elements.pages())
                    .forEach { (page, left, right) ->
                        setPage(page, left or right)
                    }
        }
        for (element in elements) add(element)
        return false
    }
    override fun iterator() = IntSetMutableIterator(this)
    override fun remove(element: Int) = TODO("not implemented")
    override fun removeAll(elements: Collection<Int>) = TODO("not implemented")
    override fun retainAll(elements: Collection<Int>) = TODO("not implemented")
    override fun containsAll(elements: Collection<Int>) = TODO("not implemented")
    override fun isEmpty() = size == 0
    override fun toString() = "IntSet(size = $size, $top)"

    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        }
        if (other !is IntSet) {
            if (other is Set<*>) {
                for (obj in other) {
                    if (obj !is Int) {
                        return false
                    }
                    if (!contains(obj)) {
                        return false
                    }
                }
                return true
            }
            return false
        }
        var equal = true
        (this.pages() coiterate other.pages())
                .filter { equal }
                .forEach { (page, left, right) ->
                    equal = equal && (left == right)
                }
        return equal
    }

    companion object {
        private const val serialVersionUID = 6790746115289250686L
    }
}

