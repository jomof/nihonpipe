package com.jomof.nihonpipe.groveler.schema

import com.jomof.nihonpipe.groveler.bitfield.BitField
import com.jomof.nihonpipe.groveler.bitfield.mutableBitFieldOf
import org.h2.mvstore.MVStore
import java.io.File

const val VOCAB_TO_INDEX = "vocab-to-index"
const val SENTENCE_INDEX_TO_INDEX = "sentence-index-to-index"
const val NEXT_INDEX = "next-index"
const val JISHO_VOCAB = "jisho-vocab"
const val OPTIMIZED_CORE_VOCAB = "optimized-core-vocab"
const val WANIKANI_VOCAB = "wanikani-vocab"
const val TANAKA_CORPUS_SENTENCE = "tanaka-corpus-sentence"

class Store(file: File) {
    private val store = MVStore.Builder()
            .fileName(file.absolutePath)
            .compress()
            .open()!!
    private val nextIndexTable = store.openMap<String, Int>(NEXT_INDEX)!!
    private var nextIndex = nextIndexTable[NEXT_INDEX] ?: 0
    private val filterTable = store.openMap<String, BitField>("filters")!!
    private val vocabToIndexTable = store.openMap<String, BitField>(VOCAB_TO_INDEX)!!
    private val sentenceIndexToIndex = store.openMap<Int, BitField>(SENTENCE_INDEX_TO_INDEX)!!
    private val jishoVocabTable = store.openMap<Int, JishoVocab>(JISHO_VOCAB)!!
    private val optimizedCoreVocabTable = store.openMap<Int, OptimizedKoreVocab>(OPTIMIZED_CORE_VOCAB)!!
    private val wanikaniVocabTable = store.openMap<Int, WaniKaniVocab>(WANIKANI_VOCAB)!!
    private val tanakaCorpusSentenceTable = store.openMap<Int, TanakaCorpusSentence>(TANAKA_CORPUS_SENTENCE)!!
    fun vocabToIndex(): Map<String, BitField> = vocabToIndexTable
    fun sentenceIndexToIndex(): Map<Int, BitField> = sentenceIndexToIndex
    fun tanakaCorpusSentence(): Map<Int, TanakaCorpusSentence> = tanakaCorpusSentenceTable

    private fun addVocabForeignKey(vocab: String, foreignKey: Int) {
        val bf = vocabToIndexTable[vocab] ?: mutableBitFieldOf()
        bf[foreignKey] = true
        vocabToIndexTable[vocab] = bf
    }

    private fun addToFilter(filterName: String, index: Int) {
        val bf = filterTable[filterName] ?: mutableBitFieldOf()
        bf[index] = true
        filterTable[filterName] = bf
    }

    fun add(vocab: JishoVocab) {
        addToFilter(JISHO_VOCAB, nextIndex)
        addVocabForeignKey(vocab.vocab, nextIndex)
        addVocabForeignKey(vocab.kana, nextIndex)
        jishoVocabTable[nextIndex] = vocab
        nextIndex++
    }

    fun add(vocab: OptimizedKoreVocab) {
        addToFilter(OPTIMIZED_CORE_VOCAB, nextIndex)
        addVocabForeignKey(vocab.vocab, nextIndex)
        addVocabForeignKey(vocab.kana, nextIndex)
        optimizedCoreVocabTable[nextIndex] = vocab
        nextIndex++
    }

    fun add(vocab: WaniKaniVocab) {
        addToFilter(WANIKANI_VOCAB, nextIndex)
        addVocabForeignKey(vocab.vocab, nextIndex)
        addVocabForeignKey(vocab.kana, nextIndex)
        wanikaniVocabTable[nextIndex] = vocab
        nextIndex++
    }

    fun add(sentence: TanakaCorpusSentence) {
        addToFilter(TANAKA_CORPUS_SENTENCE, nextIndex)
        tanakaCorpusSentenceTable[nextIndex] = sentence
        ++nextIndex
    }

    fun addSentenceIndex(indexOfSentence: Int) {
        addToFilter(SENTENCE_INDEX_TO_INDEX, nextIndex)
        val bf = sentenceIndexToIndex[nextIndex] ?: mutableBitFieldOf()
        bf[nextIndex] = true
        sentenceIndexToIndex[nextIndex] = bf
        ++nextIndex
    }

//    fun forEachIndexed(bitfield : BitField, action : (Indexed) -> Unit) {
//        bitfield.forEach { it ->
//            it.
//        }
//    }

    fun close() {
        nextIndexTable[NEXT_INDEX] = nextIndex
        store.close()
    }
}