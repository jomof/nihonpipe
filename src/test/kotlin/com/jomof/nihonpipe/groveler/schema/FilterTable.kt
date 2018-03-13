package com.jomof.nihonpipe.groveler.schema

import com.jomof.intset.IntSet
import com.jomof.intset.intSetOf
import org.h2.mvstore.MVMap

class FilterTable(
        private val filters: MVMap<String, IntSet>) {

    private fun set(name: String, key: Int) {
        val bf = get(name)
        bf += key
        filters[name] = bf
    }

    private fun get(name: String): IntSet =
            filters[name] ?: intSetOf()

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
            index: IndexedTable<IntSet>,
            primaryKey: Int,
            foreignTable: IndexedTable<F>) {
        set(oneToManyValueContainsName(index, foreignTable), primaryKey)
    }

    private fun <F> oneToManyValueContainsName(
            index: IndexedTable<IntSet>,
            foreignTable: IndexedTable<F>) = "${index.name}-value-contains-${foreignTable.name}"

    fun oneToManyValueContains(
            table: IndexedTable<IntSet>,
            other: IndexedTable<*>): IntSet {
        return get(oneToManyValueContainsName(table, other))
    }
}