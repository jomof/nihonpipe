package com.jomof.nihonpipe.groveler.schema

import com.jomof.nihonpipe.groveler.bitfield.BitField
import com.jomof.nihonpipe.groveler.bitfield.toSetBitIndices
import org.h2.mvstore.MVMap

data class Row<out V>(val index: Int, val value: V)

class FluentIndices<V, out T : IndexedTable<V>>(
        val filter: BitField,
        val filterTable: FilterTable,
        val table: T) : Sequence<Row<V>> {
    override fun iterator(): Iterator<Row<V>> = filter
            .toSetBitIndices()
            .map { index -> Row(index, table[index]!!) }
            .iterator()
}

fun <V : Indexed, T : IndexedTable<V>> FluentIndices<V, T>.count() =
        filter.toSetBitIndices().count()

interface IndexedTable<V> : Map<Int, V> {
    fun toSequence(): FluentIndices<V, IndexedTable<V>>
    val contains: BitField
    val name: String
}

class MutableIndexedTable<T : Indexed>(
        private val filterTable: FilterTable,
        private val table: MVMap<Int, T>) : IndexedTable<T> {

    override val contains: BitField
        get() = filterTable.tableContainsBitField(this)
    override val name = table.name!!

    override fun toSequence(): FluentIndices<T, MutableIndexedTable<T>> =
            FluentIndices(contains, filterTable, this)

    override val keys: Set<Int>
        get() = table.keys

    override val size: Int
        get() = table.size

    override val values: Collection<T>
        get() = table.values

    override fun containsKey(key: Int) = table.containsKey(key)

    override fun containsValue(value: T) = table.containsValue(value)

    override fun isEmpty() = table.isEmpty()

    override val entries: Set<Map.Entry<Int, T>>
        get() = table.entries

    operator fun set(key: Int, value: T) {
        table[key] = value
        filterTable.addTableContains(this, key)
    }

    override operator fun get(key: Int): T? {
        return table[key]
    }

    override fun toString() = "MutableIndexedTable(name=%s)"
}


