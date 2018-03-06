package com.jomof.nihonpipe.groveler.schema

import com.jomof.nihonpipe.groveler.bitfield.BitField
import com.jomof.nihonpipe.groveler.bitfield.and
import com.jomof.nihonpipe.groveler.bitfield.minus
import com.jomof.nihonpipe.groveler.bitfield.mutableBitFieldOf
import org.h2.mvstore.MVMap

interface OneToManyIndex<P : Any> {
    val name: String
    operator fun get(key: P): BitField?
}

open class MutableOneToManyIndex<P : Any>(
        private val filterTable: FilterTable,
        private val table: MVMap<P, BitField>) : OneToManyIndex<P> {
    override val name: String
        get() = table.name

    override fun get(key: P): BitField? {
        return table[key]
    }
    open fun <F : Any> add(
            primary: P,
            foreignKey: Int,
            foreignTable: IndexedTable<F>) {
        val bf = table[primary] ?: mutableBitFieldOf()
        bf[foreignKey] = true
        table[primary] = bf
        filterTable.addOneToManyContains(this, foreignKey, foreignTable)
    }
}


fun FluentIndices<BitField, OneIndexToManyIndex>.removeRowsContaining(
        other: IndexedTable<*>): FluentIndices<BitField, OneIndexToManyIndex> {
    val reverse = filterTable.oneToManyValueContains(this.table, other)
    val subtracted = filter minus reverse
    return FluentIndices(subtracted, filterTable, table)
}

fun FluentIndices<BitField, OneIndexToManyIndex>.keepOnlyRowsContaining(
        other: IndexedTable<*>): FluentIndices<BitField, OneIndexToManyIndex> {
    val reverse = filterTable.oneToManyValueContains(this.table, other)
    val subtracted = filter and reverse
    return FluentIndices(subtracted, filterTable, table)
}

inline fun <reified T> FluentIndices<BitField, OneIndexToManyIndex>.keepInstances(db: Store) = map { (row, indices) ->
    Row(row, db[indices]
            .filterIsInstance<T>()
            .takeOnly())
}

interface OneIndexToManyIndex : IndexedTable<BitField> {
    override fun toSequence(): FluentIndices<BitField, OneIndexToManyIndex>
}

class MutableOneIndexToManyIndex(
        private val filterTable: FilterTable,
        private val table: MVMap<Int, BitField>) :
        MutableOneToManyIndex<Int>(filterTable, table), OneIndexToManyIndex {
    override fun toSequence(): FluentIndices<BitField, OneIndexToManyIndex> =
            FluentIndices(contains, filterTable, this)

    override val contains: BitField
        get() = filterTable.tableContainsBitField(this)

    override val entries: Set<Map.Entry<Int, BitField>>
        get() = table.entries
    override val keys: Set<Int>
        get() = table.keys
    override val size: Int
        get() = table.size
    override val values: Collection<BitField>
        get() = table.values

    override fun containsKey(key: Int) = table.containsKey(key)

    override fun containsValue(value: BitField) = table.containsValue(value)

    override fun get(key: Int) = table[key]

    override fun isEmpty() = table.isEmpty()

    override fun <F : Any> add(
            primary: Int,
            foreignKey: Int,
            foreignTable: IndexedTable<F>) {
        super.add(primary, foreignKey, foreignTable)
        filterTable.addTableContains(this, primary)
        filterTable.addOneToManyValueContains(this, primary, foreignTable)
    }
}

