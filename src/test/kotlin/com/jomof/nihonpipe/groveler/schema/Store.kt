package com.jomof.nihonpipe.groveler.schema

import com.jomof.nihonpipe.groveler.BitField
import com.jomof.nihonpipe.groveler.createBitField
import com.jomof.nihonpipe.groveler.dataDatabaseBin
import com.jomof.nihonpipe.groveler.set
import org.h2.mvstore.MVStore

const val VOCAB_TO_DEFINITION = "vocab-to-definition"
const val VOCAB_NEXT_INDEX = "vocab-next-index"
const val JISHO_VOCAB = "jisho-vocab"
const val JISHO_VOCAB_TABLE_INDEX = 0
const val OPTIMIZED_CORE_VOCAB = "optimized-core-vocab"
const val OPTIMIZED_CORE_VOCAB_TABLE_INDEX = 1
const val WANIKANI_VOCAB = "wanikani-vocab"
const val WANIKANI_VOCAB_TABLE_INDEX = 2
const val TANAKA_CORPUS_SENTENCE = "tanaka-corpus-sentence"
//const val TANAKA_CORPUS_SENTENCE_TABLE_INDEX = 3

class Store {
    private val store = MVStore.Builder()
            .fileName(dataDatabaseBin.absolutePath)
            .compress()
            .open()!!
    private val nextIndexTable = store.openMap<String, Int>("next-index")!!
    private var nextIndex = nextIndexTable[VOCAB_NEXT_INDEX] ?: 0
    private val filterTable = store.openMap<String, BitField>("filters")!!
    private var jishoVocabFilter = filterTable[JISHO_VOCAB] ?: createBitField()
    private var optimizedKoreVocabFilter = filterTable[OPTIMIZED_CORE_VOCAB] ?: createBitField()
    private var wanikaniVocabFilter = filterTable[OPTIMIZED_CORE_VOCAB] ?: createBitField()
    private var tanakaCorpusSentenceFilter = filterTable[TANAKA_CORPUS_SENTENCE] ?: createBitField()
    private val vocabToIndexedTable = store.openMap<String, BitField>(VOCAB_TO_DEFINITION)!!
    private val jishoVocabTable = store.openMap<Int, JishoVocab>(JISHO_VOCAB)!!
    private val optimizedCoreVocabTable = store.openMap<Int, OptimizedKoreVocab>(OPTIMIZED_CORE_VOCAB)!!
    private val wanikaniVocabTable = store.openMap<Int, WaniKaniVocab>(WANIKANI_VOCAB)!!
    private val tanakaCorpusSentenceTable = store.openMap<Int, TanakaCorpusSentence>(TANAKA_CORPUS_SENTENCE)!!
    val vocabToIndex = vocabToIndexedTable.toMap<String, BitField>()
    val tanakaCorpusSentence = tanakaCorpusSentenceTable.toMap<Int, TanakaCorpusSentence>()

    private fun addVocabForeignKey(vocab: String, foreignKey: Int) {
        vocabToIndexedTable[vocab] =
                vocabToIndexedTable[vocab].set(foreignKey, true)
    }

    fun add(vocab: JishoVocab) {
        jishoVocabFilter = jishoVocabFilter.set(nextIndex, true)
        addVocabForeignKey(vocab.vocab, nextIndex)
        addVocabForeignKey(vocab.kana, nextIndex)
        jishoVocabTable[nextIndex] = vocab
        nextIndex++
    }

    fun add(vocab: OptimizedKoreVocab) {
        optimizedKoreVocabFilter = optimizedKoreVocabFilter.set(nextIndex, true)
        addVocabForeignKey(vocab.vocab, nextIndex)
        addVocabForeignKey(vocab.kana, nextIndex)
        optimizedCoreVocabTable[nextIndex] = vocab
        nextIndex++
    }

    fun add(vocab: WaniKaniVocab) {
        wanikaniVocabFilter = wanikaniVocabFilter.set(nextIndex, true)
        addVocabForeignKey(vocab.vocab, nextIndex)
        addVocabForeignKey(vocab.kana, nextIndex)
        wanikaniVocabTable[nextIndex] = vocab
        nextIndex++
    }

    fun add(sentence: TanakaCorpusSentence) {
        tanakaCorpusSentenceFilter = tanakaCorpusSentenceFilter.set(nextIndex, true)
        tanakaCorpusSentenceTable[nextIndex] = sentence
        ++nextIndex
    }

    fun close() {
        nextIndexTable[VOCAB_NEXT_INDEX] = nextIndex
        filterTable[JISHO_VOCAB] = jishoVocabFilter
        filterTable[OPTIMIZED_CORE_VOCAB] = optimizedKoreVocabFilter
        filterTable[WANIKANI_VOCAB] = wanikaniVocabFilter
        filterTable[TANAKA_CORPUS_SENTENCE] = tanakaCorpusSentenceFilter
        store.close()
    }
}