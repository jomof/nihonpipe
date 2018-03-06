package com.jomof.nihonpipe.groveler.schema

import com.jomof.nihonpipe.groveler.bitfield.BitField
import com.jomof.nihonpipe.groveler.bitfield.mutableBitFieldOf
import org.h2.mvstore.MVMap


class FilterTable(
        private val filters: MVMap<String, BitField>) {

    private fun set(name: String, key: Int) {
        var bf = get(name)
        bf[key] = true
        filters[name] = bf
    }

    private fun get(name: String) =
            filters[name] ?: mutableBitFieldOf()

    private fun containsName(table: IndexedTable<*>) =
            "contains-${table.name}"

    fun tableContainsBitField(table: IndexedTable<*>) =
            get(containsName(table))

    fun addTableContains(table: IndexedTable<*>, key: Int) =
            set(containsName(table), key)

    fun <P : Any> addOneToManyContains(
            index: OneToManyIndex<P>,
            foreignKey: Int,
            foreignTable: IndexedTable<*>) {
        set("${index.name}-key-contains-${foreignTable.name}", foreignKey)
    }

    fun <F> addOneToManyValueContains(
            index: IndexedTable<BitField>,
            primaryKey: Int,
            foreignTable: IndexedTable<F>) {
        set(oneToManyValueContainsName(index, foreignTable), primaryKey)
    }

    private fun <F> oneToManyValueContainsName(
            index: IndexedTable<BitField>,
            foreignTable: IndexedTable<F>) = "${index.name}-value-contains-${foreignTable.name}"

    fun oneToManyValueContains(
            table: IndexedTable<BitField>,
            other: IndexedTable<*>): BitField {
        return get(oneToManyValueContainsName(table, other))
    }
}