package com.jomof.nihonpipe.groveler.schema

import com.jomof.nihonpipe.groveler.BitField
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
    private val filterTable = store.openMap<String, String>("filters")!!
    private var jishoVocabFilter = BitField(filterTable[JISHO_VOCAB] ?: "")
    private var optimizedKoreVocabFilter = BitField(filterTable[OPTIMIZED_CORE_VOCAB] ?: "")
    private var wanikaniVocabFilter = BitField(filterTable[OPTIMIZED_CORE_VOCAB] ?: "")
    private var tanakaCorpusSentenceFilter = BitField(filterTable[TANAKA_CORPUS_SENTENCE] ?: "")
    private val vocabToDefinition = store.openMap<String, List<ForeignKey>>(VOCAB_TO_DEFINITION)!!
    private val jishoVocab = store.openMap<Int, JishoVocab>(JISHO_VOCAB)!!
    private val optimizedCoreVocab = store.openMap<Int, OptimizedKoreVocab>(OPTIMIZED_CORE_VOCAB)!!
    private val wanikaniVocab = store.openMap<Int, WaniKaniVocab>(WANIKANI_VOCAB)!!
    private val tanakaCorpusSentence = store.openMap<Int, TanakaCorpusSentence>(TANAKA_CORPUS_SENTENCE)!!

    private fun addVocabForeignKey(vocab: String, foreignKey: ForeignKey) {
        val keys = (vocabToDefinition[vocab] ?: listOf())
                .toMutableList()
        keys.add(foreignKey)
        vocabToDefinition[vocab] = keys
    }

    fun add(vocab: JishoVocab) {
        jishoVocabFilter = jishoVocabFilter.set(nextIndex, true)
        val foreignKey = ForeignKey(JISHO_VOCAB_TABLE_INDEX, nextIndex)
        addVocabForeignKey(vocab.vocab, foreignKey)
        addVocabForeignKey(vocab.kana, foreignKey)
        jishoVocab[nextIndex] = vocab
        nextIndex++
    }

    fun add(vocab: OptimizedKoreVocab) {
        optimizedKoreVocabFilter = optimizedKoreVocabFilter.set(nextIndex, true)
        val foreignKey = ForeignKey(OPTIMIZED_CORE_VOCAB_TABLE_INDEX, nextIndex)
        addVocabForeignKey(vocab.vocab, foreignKey)
        addVocabForeignKey(vocab.kana, foreignKey)
        optimizedCoreVocab[nextIndex] = vocab
        nextIndex++
    }

    fun add(vocab: WaniKaniVocab) {
        wanikaniVocabFilter = wanikaniVocabFilter.set(nextIndex, true)
        val foreignKey = ForeignKey(WANIKANI_VOCAB_TABLE_INDEX, nextIndex)
        addVocabForeignKey(vocab.vocab, foreignKey)
        addVocabForeignKey(vocab.kana, foreignKey)
        wanikaniVocab[nextIndex] = vocab
        nextIndex++
    }

    fun add(sentence: TanakaCorpusSentence) {
        tanakaCorpusSentenceFilter = tanakaCorpusSentenceFilter.set(nextIndex, true)
        tanakaCorpusSentence[nextIndex] = sentence
        ++nextIndex
    }

    fun close() {
        nextIndexTable[VOCAB_NEXT_INDEX] = nextIndex
        filterTable[JISHO_VOCAB] = jishoVocabFilter.init
        filterTable[OPTIMIZED_CORE_VOCAB] = optimizedKoreVocabFilter.init
        filterTable[WANIKANI_VOCAB] = wanikaniVocabFilter.init
        filterTable[TANAKA_CORPUS_SENTENCE] = tanakaCorpusSentenceFilter.init
        store.close()
    }
}