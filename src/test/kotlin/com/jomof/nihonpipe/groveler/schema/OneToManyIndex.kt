package com.jomof.nihonpipe.groveler.schema

import com.jomof.intset.IntSet
import com.jomof.intset.intSetOf
import com.jomof.intset.intersect
import com.jomof.intset.minus
import org.h2.mvstore.MVMap

interface OneToManyIndex<P : Any> {
    val name: String
    operator fun get(key: P): IntSet?
}

open class MutableOneToManyIndex<P : Any>(
        private val filterTable: FilterTable,
        private val table: MVMap<P, IntSet>) : OneToManyIndex<P> {
    override val name: String
        get() = table.name

    override fun get(key: P): IntSet? {
        return table[key]
    }

    open fun <F : Any> add(
            primary: P,
            foreignKey: Int,
            foreignTable: IndexedTable<F>) {
        val bf = table[primary] ?: intSetOf()
        bf += foreignKey
        table[primary] = bf
        filterTable.addOneToManyContains(this, foreignKey, foreignTable)
    }
}


fun FluentIndices<IntSet, OneIndexToManyIndex>.removeRowsContaining(
        other: IndexedTable<*>): FluentIndices<IntSet, OneIndexToManyIndex> {
    val reverse = filterTable.oneToManyValueContains(this.table, other)
    val subtracted = filter minus reverse
    return FluentIndices(subtracted, filterTable, table)
}

fun FluentIndices<IntSet, OneIndexToManyIndex>.keepOnlyRowsContaining(
        other: IndexedTable<*>): FluentIndices<IntSet, OneIndexToManyIndex> {
    val reverse = filterTable.oneToManyValueContains(this.table, other)
    val subtracted = filter intersect reverse
    return FluentIndices(subtracted, filterTable, table)
}


inline fun <reified T> FluentIndices<IntSet, OneIndexToManyIndex>.keepInstances(db: Store) =
        map { (row, indices) ->
            Row(row, db.getIndexed(indices)
                    .filterIsInstance<T>()
                    .single())
        }

interface OneIndexToManyIndex : IndexedTable<IntSet> {
    override fun toSequence(): FluentIndices<IntSet, OneIndexToManyIndex>
}

class MutableOneIndexToManyIndex(
        private val filterTable: FilterTable,
        private val table: MVMap<Int, IntSet>) :
        MutableOneToManyIndex<Int>(filterTable, table), OneIndexToManyIndex {
    override fun toSequence(): FluentIndices<IntSet, OneIndexToManyIndex> =
            FluentIndices(contains, filterTable, this)

    override val contains: IntSet
        get() = filterTable.tableContainsBitField(this)

    override val entries: Set<Map.Entry<Int, IntSet>>
        get() = table.entries
    override val keys: Set<Int>
        get() = table.keys
    override val size: Int
        get() = table.size
    override val values: Collection<IntSet>
        get() = table.values

    override fun containsKey(key: Int) = table.containsKey(key)

    override fun containsValue(value: IntSet) = table.containsValue(value)

    override fun get(key: Int) = table[key]

    override fun isEmpty() = table.isEmpty()

    override fun <F : Any> add(
            primary: Int,
            foreignKey: Int,
            foreignTable: IndexedTable<F>) {
        super.add(primary, foreignKey, foreignTable)
        assert(contains.contains(primary)) {
            "can't set foreign keys on element that doesn't exist"
        }
        filterTable.addOneToManyValueContains(this, primary, foreignTable)
    }

    fun add(primary: Int, foreignKey: Int) {
        assert(!contains.contains(primary)) { "already added" }
        table[primary] = intSetOf(foreignKey..foreignKey)
        filterTable.addTableContains(this, primary)
    }

    override fun toString() = contains.toString()
}

