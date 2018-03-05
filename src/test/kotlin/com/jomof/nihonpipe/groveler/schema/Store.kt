package com.jomof.nihonpipe.groveler.schema

import com.jomof.nihonpipe.groveler.bitfield.BitField
import com.jomof.nihonpipe.groveler.bitfield.toSetBitIndices
import org.h2.mvstore.MVStore
import java.io.File
import kotlin.reflect.KClass

const val VOCAB_TO_INDEX = "vocab-to-index"
const val SENTENCE_INDEX_TO_INDEX = "sentence-index-to-index"
const val NEXT_INDEX = "next-index"

class Store(file: File) {

    private val store = MVStore.Builder()
            .fileName(file.absolutePath)
            .compress()
            .open()!!
    private val nextIndexTable = store.openMap<String, Int>(NEXT_INDEX)!!
    private var nextIndex = nextIndexTable[NEXT_INDEX] ?: 0
    private val filterTable = FilterTable(
            store.openMap<String, BitField>("filters"))
    private val vocabToIndexTable = oneToManyOf<String>(VOCAB_TO_INDEX)
    private val sentenceIndexToIndexTable = oneIndexToManyOf(SENTENCE_INDEX_TO_INDEX)
    private val jishoVocabTable = tableOf(JishoVocab::class)
    private val optimizedCoreVocabTable = tableOf(OptimizedKoreVocab::class)
    private val wanikaniVocabTable = tableOf(WaniKaniVocab::class)
    private val tanakaCorpusSentenceTable = tableOf(TanakaCorpusSentence::class)
    private val kuromojiIpadicTokenizationTable = tableOf(KuromojiIpadicTokenization::class)
    val tanakaCorpusSentence: IndexedTable<TanakaCorpusSentence> = tanakaCorpusSentenceTable
    val sentenceIndexToIndex: OneIndexToManyIndex = sentenceIndexToIndexTable
    val kuromojiIpadicTokenization: IndexedTable<KuromojiIpadicTokenization> = kuromojiIpadicTokenizationTable

    operator fun get(index: Int) =
            jishoVocabTable[index] ?: optimizedCoreVocabTable[index] ?: wanikaniVocabTable[index]
            ?: tanakaCorpusSentenceTable[index] ?: kuromojiIpadicTokenizationTable[index]

    operator fun get(indices: BitField) =
            indices
                    .toSetBitIndices()
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

    fun add(indexOfSentence: Int, sentence: TanakaCorpusSentence) {
        sentenceIndexToIndexTable.add(nextIndex,
                indexOfSentence,
                tanakaCorpusSentenceTable)
        ++nextIndex
    }

    private fun <T : Indexed> tableOf(clazz: KClass<T>): MutableIndexedTable<T> {
        val name = clazz.java.simpleName
        return MutableIndexedTable(filterTable, store.openMap(name))
    }

    private fun <P : Any> oneToManyOf(name: String): MutableOneToManyIndex<P> {
        return MutableOneToManyIndex(filterTable, store.openMap(name))
    }

    private fun oneIndexToManyOf(name: String): MutableOneIndexToManyIndex {
        return MutableOneIndexToManyIndex(filterTable, store.openMap(name))
    }

    fun close() {
        nextIndexTable[NEXT_INDEX] = nextIndex
        store.close()
    }
}