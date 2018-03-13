package com.jomof.nihonpipe.groveler

import com.google.common.truth.Truth.assertThat
import com.jomof.intset.intSetOf
import com.jomof.intset.intersect
import com.jomof.nihonpipe.groveler.algorithm.cartesian
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
        val db = Store(dataDatabaseBin)
        populateKuromojiBatch(db, 100_000)
        populateKuromojiTokenSentenceStatistics(db)
        populateKuromojiTokenSentenceStructure(db)
        populateWaniKaniSentencesLevels(db)
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
            manyToOne.add(primary, foreign)
            manyToOne.add(primary, foreign, foreignTable)
            store.close()
        }

        fun test() {
            val db = MVStore.Builder().fileName(name).compress().open()!!
            val (manyToOne, foreign) = tables(db)
            assertThat(manyToOne.contains.contains(191)).isFalse()
            assertThat(manyToOne.contains.contains(192)).isTrue()
            assertThat(manyToOne.contains.contains(193)).isFalse()
            val rows = manyToOne.toSequence().toList()
            assertThat(rows).containsExactly(
                    Row(192, intSetOf(193)))
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
            val file = File(name)
            file.delete()
            val db = Store(file)
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

    @Test
    fun crossWaniKani() {
        val db = Store(dataDatabaseBin)
        try {
            val skeletonLevelInfo = db.levels[LevelType.SENTENCE_SKELETON]!!
            val grammarLevelInfo = db.levels[LevelType.GRAMMAR_ELEMENT]!!
            val waniKaniLevelInfo = db.levels[LevelType.WANIKANI_LEVEL]!!
            cartesian(
                    skeletonLevelInfo.sentencesByLevel,
                    grammarLevelInfo.sentencesByLevel,
                    waniKaniLevelInfo.sentencesByLevel) { sl, gl, wl ->
                cartesian(
                        sl.levelElements,
                        gl.levelElements,
                        wl.levelElements) { se, ge, we ->
                    val common = se.sentenceIndex intersect ge.sentenceIndex intersect we.sentenceIndex
//                    if (common.ones().count() == 0) {
//                        println("skeleton=${se.level} grammar=${ge.level} wanikani=${we.level}")
//                        println("skeleton ${se.key} doesn't have a corresponding grammar element ${ge.key}")
//                        db.sentenceIndexToIndex
//                                .toSequence()
//                                .keepOnlyRowsContaining(se.sentenceIndex)
//                                .keepInstances<TanakaCorpusSentence>(db)
//                                .sortedBy { (row, tanaka) -> tanaka.japanese.length }
//                                .take(3)
//                                .onEach { (row, tanaka) ->
//                                    println("${tanaka.japanese} / ${tanaka.english} ")
//                                }
//                                .count()
//                        throw RuntimeException()
//                    }
                }
            }
        } finally {
            db.close()
        }
    }
}