package com.jomof.nihonpipe.groveler

import com.google.common.truth.Truth.assertThat
import com.jomof.nihonpipe.groveler.bitfield.bitFieldOf
import com.jomof.nihonpipe.groveler.schema.*
import org.h2.mvstore.MVStore
import org.junit.Test
import java.io.File
import java.io.Serializable

class H2Populator {
    @Test
    fun populate() {
        dataDir.mkdirs()
        if (!dataDatabaseBin.exists()) {
            // Fast population steps
            val db = Store(dataDatabaseBin)
            translateJishoJLPT(db)
            translateOptimizedKore(db)
            translateWaniKaniVocab(db)
            translateTanakaCorpus(db)
            translateWaniKaniVsJLPT(db)
            db.close()
        }

        // Slow or incremental population steps
        var db = Store(dataDatabaseBin)
        populateKuromojiBatch(db, 1_000)
        populateKuromojiTokenSentenceStatistics(db)
        populateKuromojiTokenSentenceStructure(db)
        println("kuromoji size = ${db.kuromojiIpadicTokenization.count()}")
        println("tanaka corpus size = ${db.tanakaCorpusSentence.count()}")
        println("sentence index size = ${db.sentenceIndexToIndex.count()}")
        db.close()
    }

    @Test
    fun testManyToOne() {
        val name = "my-test-db-2.bin"

        data class TestClass(val value: String) : Serializable, Indexed

        fun tables(store: MVStore): Pair<MutableOneIndexToManyIndex, MutableIndexedTable<TestClass>> {
            val filters = FilterTable(store.openMap("filters"))
            val manyToOne = MutableOneIndexToManyIndex(
                    filters,
                    store.openMap("many-to-one"))
            val foreignTable = MutableIndexedTable<TestClass>(
                    filters,
                    store.openMap("foreign-table"))
            return Pair(manyToOne, foreignTable)
        }

        fun setup() {
            File(name).delete()
            val store = MVStore.Builder().fileName(name).compress().open()!!
            val (manyToOne, foreignTable) = tables(store)
            val primary = 192
            val foreign = 193
            foreignTable[foreign] = TestClass("bob")
            manyToOne.add(primary, foreign, foreignTable)
            store.close()
        }

        fun test() {
            val db = MVStore.Builder().fileName(name).compress().open()!!
            val (manyToOne, foreign) = tables(db)
            assertThat(manyToOne.contains[191]).isFalse()
            assertThat(manyToOne.contains[192]).isTrue()
            assertThat(manyToOne.contains[193]).isFalse()
            val rows = manyToOne.toSequence().toList()
            assertThat(rows).containsExactly(
                    Row(192, bitFieldOf(193..193 to true)))
            val subtracted =
                    manyToOne.toSequence()
                            .removeRowsContaining(foreign)
                            .toList()
            assertThat(subtracted.count()).isEqualTo(0)
            db.close()
        }
        setup()
        test()
        File(name).delete()
    }

    @Test
    fun testSentencesIndexed() {
        val name = "my-test-sentences-indexed.bin"
        fun setup() {
            val db = Store(File(name))
            val sentence = TanakaCorpusSentence(
                    "japanese",
                    "code",
                    "tid",
                    "english",
                    "filename")
            db.add(sentence)
            db.tanakaCorpusSentence.forEach { (index, sentence) ->
                db.add(index, sentence)
            }
            db.close()
        }
        setup()
        val db = Store(File(name))
        assertThat(db.sentenceIndexToIndex.toSequence().count()).isEqualTo(1)
        assertThat(db.sentenceIndexToIndex
                .toSequence()
                .removeRowsContaining(db.tanakaCorpusSentence)
                .count()).isEqualTo(0)
        db.close()
        File(name).delete()
    }
}