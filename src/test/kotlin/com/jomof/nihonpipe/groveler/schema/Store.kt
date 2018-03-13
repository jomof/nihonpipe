package com.jomof.nihonpipe.groveler.schema

import com.jomof.intset.IntSet
import org.h2.mvstore.MVStore
import java.io.File
import kotlin.reflect.KClass

const val VOCAB_TO_INDEX = "vocab-to-index"
const val SENTENCE_INDEX_TO_INDEX = "sentence-index-to-index"
const val NEXT_INDEX = "next-index"
const val KUROMOJI_STRUCTURE_TO_INDEX = "kuromoji-structure-to-index"

class Store(file: File) {
    private val db = MVStore.Builder()
            .fileName(file.absolutePath)
            .compress()
            .open()!!
    private val levelsDb = MVStore.Builder()
            .fileName(file.absoluteFile.parent + "/levels.bin")
            .compress()
            .open()!!
    private val nextIndexTable = db.openMap<String, Int>(NEXT_INDEX)!!
    private val levelsTable = levelsDb.openMap<LevelType, LevelInfo>("sentence-index-levels")!!
    private var nextIndex = nextIndexTable[NEXT_INDEX] ?: 0
    private val filterTable = FilterTable(
            db.openMap<String, IntSet>("filters"))
    private val vocabToIndexTable = oneToManyOf<String>(VOCAB_TO_INDEX)
    //private val kuromojiStructureToIndexTable = oneToManyOf<String>(KUROMOJI_STRUCTURE_TO_INDEX)
    private val sentenceIndexToIndexTable = oneIndexToManyOf(SENTENCE_INDEX_TO_INDEX)
    private val jishoVocabTable = tableOf(JishoVocab::class)
    private val optimizedCoreVocabTable = tableOf(OptimizedKoreVocab::class)
    private val wanikaniVocabTable = tableOf(WaniKaniVocab::class)
    private val waniKaniVsJlptVocabTable = tableOf(WaniKaniVsJlptVocab::class)
    private val tanakaCorpusSentenceTable = tableOf(TanakaCorpusSentence::class)
    private val kuromojiIpadicTokenizationTable = tableOf(KuromojiIpadicTokenization::class)
    private val kuromojiIpadicSentenceStatisticsTable = tableOf(KuromojiIpadicSentenceStatistics::class)
    val tanakaCorpusSentence: IndexedTable<TanakaCorpusSentence> = tanakaCorpusSentenceTable
    val sentenceIndexToIndex: OneIndexToManyIndex = sentenceIndexToIndexTable
    val kuromojiIpadicTokenization: IndexedTable<KuromojiIpadicTokenization> = kuromojiIpadicTokenizationTable
    val kuromojiIpadicSentenceStatistics: IndexedTable<KuromojiIpadicSentenceStatistics> = kuromojiIpadicSentenceStatisticsTable
    val vocabToIndex: OneToManyIndex<String> = vocabToIndexTable
    val levels: Map<LevelType, LevelInfo> = levelsTable

    private val indexedTables = arrayOf(
            jishoVocabTable,
            optimizedCoreVocabTable,
            wanikaniVocabTable,
            tanakaCorpusSentenceTable,
            kuromojiIpadicTokenizationTable,
            kuromojiIpadicSentenceStatisticsTable,
            waniKaniVsJlptVocabTable
    )

    operator fun get(index: Int): Indexed {
        for (table in indexedTables) {
            val value = table[index]
            if (value != null) {
                return value
            }
        }
        throw RuntimeException()
    }

    fun getIndexed(indices: IntSet): List<Indexed> = indices
                    .map { get(it) }

    fun add(vocab: JishoVocab) {
        jishoVocabTable[nextIndex] = vocab
        vocabToIndexTable.add(vocab.vocab, nextIndex, jishoVocabTable)
        vocabToIndexTable.add(vocab.kana, nextIndex, jishoVocabTable)
        nextIndex++
    }

    fun add(vocab: OptimizedKoreVocab) {
        optimizedCoreVocabTable[nextIndex] = vocab
        vocabToIndexTable.add(vocab.vocab, nextIndex, optimizedCoreVocabTable)
        vocabToIndexTable.add(vocab.kana, nextIndex, optimizedCoreVocabTable)
        nextIndex++
    }

    fun add(vocab: WaniKaniVocab) {
        wanikaniVocabTable[nextIndex] = vocab
        vocabToIndexTable.add(vocab.vocab, nextIndex, wanikaniVocabTable)
        vocabToIndexTable.add(vocab.kana, nextIndex, wanikaniVocabTable)
        nextIndex++
    }

    fun add(vocab: WaniKaniVsJlptVocab) {
        waniKaniVsJlptVocabTable[nextIndex] = vocab
        vocabToIndexTable.add(vocab.vocab, nextIndex, waniKaniVsJlptVocabTable)
        vocabToIndexTable.add(vocab.kana, nextIndex, waniKaniVsJlptVocabTable)
        nextIndex++
    }

    fun add(sentence: TanakaCorpusSentence) {
        tanakaCorpusSentenceTable[nextIndex] = sentence
        ++nextIndex
    }

    fun add(indexOfSentence: Int, tokenization: KuromojiIpadicTokenization) {
        kuromojiIpadicTokenizationTable[nextIndex] = tokenization
        sentenceIndexToIndexTable.add(indexOfSentence,
                nextIndex,
                kuromojiIpadicTokenizationTable)
        ++nextIndex
    }

    fun add(indexOfSentence: Int, statistics: KuromojiIpadicSentenceStatistics) {
        kuromojiIpadicSentenceStatisticsTable[nextIndex] = statistics
        sentenceIndexToIndexTable.add(indexOfSentence,
                nextIndex,
                kuromojiIpadicSentenceStatisticsTable)
        ++nextIndex
    }

    fun add(indexOfSentence: Int, sentence: TanakaCorpusSentence) {
        sentenceIndexToIndexTable.add(nextIndex, indexOfSentence)
        sentenceIndexToIndexTable.add(nextIndex,
                indexOfSentence,
                tanakaCorpusSentenceTable)
        ++nextIndex
    }

    fun set(levelType: LevelType, levelInfo: LevelInfo) {
        levelsTable[levelType] = levelInfo
    }

    private fun <T : Indexed> tableOf(clazz: KClass<T>): MutableIndexedTable<T> {
        val name = clazz.java.simpleName
        return MutableIndexedTable(filterTable, db.openMap(name))
    }

    private fun <P : Any> oneToManyOf(name: String): MutableOneToManyIndex<P> {
        return MutableOneToManyIndex(filterTable, db.openMap(name))
    }

    private fun oneIndexToManyOf(name: String): MutableOneIndexToManyIndex {
        return MutableOneIndexToManyIndex(filterTable, db.openMap(name))
    }

    fun close() {
        nextIndexTable[NEXT_INDEX] = nextIndex
        db.close()
        levelsDb.close()
    }
}