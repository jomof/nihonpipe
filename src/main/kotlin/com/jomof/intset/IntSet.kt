package com.jomof.intset

import java.io.*

class IntSet(
        internal var top: Node = EmptyNode.instance)
    : MutableSet<Int>, Externalizable {
    private var readonly = false
    override val size: Int get() = top.size

    override fun readExternal(i: ObjectInput) {
        assert(!readonly)
        top = readNode(i)
    }

    fun check(): IntSet {
        fun serialize(set: IntSet): ByteArray {
            val bos = ByteArrayOutputStream()
            val out: ObjectOutput?
            val bytes: ByteArray
            try {
                out = ObjectOutputStream(bos)
                out.writeObject(set)
                out.flush()
                bytes = bos.toByteArray()
            } finally {
                try {
                    bos.close()
                } catch (ex: IOException) {
                    // ignore close exception
                }
            }
            return bytes
        }

        fun deserialize(bytes: ByteArray): IntSet {
            val bis = ByteArrayInputStream(bytes)
            var i: ObjectInput? = null
            try {
                i = ObjectInputStream(bis)
                return i.readObject() as IntSet
            } finally {
                try {
                    if (i != null) {
                        i.close()
                    }
                } catch (ex: IOException) {
                }
            }
        }

        val reserialized = deserialize(serialize(this))
        assert(this == reserialized) {
            deserialize(serialize(this))
            "booo"
        }
        return reserialized
    }

    override fun writeExternal(out: ObjectOutput) {
        top.writeExternal(out)
    }

    override fun add(element: Int): Boolean {
        assert(!readonly)
        return top.add(pageOf(element), offsetOf(element)) {
            top = it
        }
    }

    override fun clear() {
        assert(!readonly)
        top = EmptyNode.instance
    }

    override fun contains(element: Int) = top.contains(
            pageOf(element),
            offsetOf(element))

    fun pages(): Iterable<Page> = top.pages().asIterable()

    fun setPage(startPage: Int, elements: Long) {
        assert(!readonly)
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
                .forEach { (page, left, right) ->
                    equal = equal && (left == right)
                }
        return equal
    }

    companion object {
        private const val serialVersionUID = 6790746115289250686L
    }
}

