package com.jomof.intset

import java.io.Externalizable
import java.io.ObjectInput
import java.io.ObjectOutput

open class IntSet(
        internal var top: Node = EmptyNode.instance)
    : MutableSet<Int>, Externalizable {
    private var range = (0..Int.MAX_VALUE)
    private var readonly = false
    override val size: Int get() = top.size

    override fun readExternal(i: ObjectInput) {
        assert(!readonly)
        val byte = i.readByte()
        when (byte) {
            1.toByte() -> {
                val first = i.readInt()
                val last = i.readInt()
                range = first..last
            }
        }
        top = readNode(i)
    }

    override fun writeExternal(out: ObjectOutput) {
        if (range == (0..Int.MAX_VALUE)) {
            out.writeByte(0)
        } else {
            out.writeByte(1)
            out.writeInt(range.first)
            out.writeInt(range.last)
        }
        top.writeExternal(out)
    }

    override fun add(element: Int): Boolean {
        assert(!readonly)
        assert(element in range)
        return top.add(pageOf(element), offsetOf(element)) {
            top = it
        }
    }

    fun withRangeLimit(range: IntRange): IntSet {
        this.range = range
        return this
    }

    override fun clear() {
        assert(!readonly)
        top = EmptyNode.instance
    }

    override fun contains(element: Int): Boolean {
        assert(element in range)
        return top.contains(
                pageOf(element),
                offsetOf(element))
    }

    fun pages(): Iterable<Page> = top.pages().asIterable()

    fun setPage(startPage: Int, elements: Long) {
        assert(!readonly)
        assert(startPage * 64 in range)
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
    fun readonly(): IntSet {
        readonly = true
        return this
    }

    fun readwrite(): IntSet {
        readonly = false
        return this
    }

    fun doWhile(action: (Int) -> Boolean) = top.doWhile(action)
    fun serializationSize() = top.serializationSize()

    override fun addAll(elements: Collection<Int>): Boolean {
        assert(!readonly)
        if (elements is IntSet) {
            (pages() coiterate elements.pages())
                    .forEach { (page, left, right) ->
                        assert(page * 64 in range)
                        if (right != 0L && right != left) {
                            setPage(page, left or right)
                        }
                    }
            return false
        }
        for (element in elements) add(element)
        return false
    }

    override fun iterator() = IntSetMutableIterator(this)
    override fun remove(element: Int): Boolean {
        assert(!readonly)
        assert(element in range)
        val pageOf = pageOf(element)
        val offsetOf = offsetOf(element)
        val bitOf = bitOf(offsetOf)
        val negated = -1L xor bitOf
        val left = top.getPage(pageOf)
        val combined = left and negated
        if (combined != left) {
            setPage(pageOf, combined)
        }
        return (left and bitOf) != 0L
    }

    override fun removeAll(elements: Collection<Int>): Boolean {
        assert(!readonly)
        if (elements is IntSet) {
            (pages() coiterate elements.pages())
                    .forEach { (page, left, right) ->
                        val negated = -1L xor right
                        val combined = left and negated
                        if (combined != left) {
                            assert(page * 64 in range)
                            setPage(page, combined)
                        }
                    }
            return false
        }
        for (element in elements) remove(element)
        return false
    }

    override fun retainAll(elements: Collection<Int>) = TODO("not implemented")
    override fun containsAll(elements: Collection<Int>) = TODO("not implemented")
    override fun isEmpty() = size == 0
    override fun toString(): String {
        return if (size > 20) {
            "IntSet(size = $size $top)"
        } else {
            "IntSet(${joinToString()})"
        }
    }

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
                .forEach { (_, left, right) ->
                    equal = equal && (left == right)
                }
        return equal
    }

    companion object {
        private const val serialVersionUID = 6790746115289250686L
    }
}

