package com.jomof.nihonpipe.groveler.schema

import com.jomof.nihonpipe.groveler.BitField
import com.jomof.nihonpipe.groveler.dataDatabaseBin
import com.jomof.nihonpipe.groveler.set
import org.h2.mvstore.MVStore

const val VOCAB_TO_DEFINITION = "vocab-to-definition"
const val JISHO_VOCAB = "jisho-vocab"
const val JISHO_VOCAB_TABLE_INDEX = 0
const val OPTIMIZED_CORE_VOCAB = "optimized-core-vocab"
const val OPTIMIZED_CORE_VOCAB_TABLE_INDEX = 1
const val VOCAB_NEXT_INDEX = "vocab-next-index"


class Store {
    private val store = MVStore.Builder()
            .fileName(dataDatabaseBin.absolutePath)
            .compress()
            .open()!!
    private val nextIndexTable = store.openMap<String, Int>("next-index")!!
    private var nextVocabIndex = nextIndexTable[VOCAB_NEXT_INDEX] ?: 0
    private val filterTable = store.openMap<String, String>("filters")!!
    private var jishoVocabFilter = BitField(filterTable[JISHO_VOCAB] ?: "")
    private var optimizedKoreVocabFilter = BitField(filterTable[OPTIMIZED_CORE_VOCAB] ?: "")
    private val vocabToDefinition = store.openMap<String, List<ForeignKey>>(VOCAB_TO_DEFINITION)!!
    private val jishoVocab = store.openMap<Int, JishoVocab>(JISHO_VOCAB)!!
    private val optimizedCoreVocab = store.openMap<Int, OptimizedKoreVocab>(OPTIMIZED_CORE_VOCAB)!!

    private fun addVocabForeignKey(vocab: String, foreignKey: ForeignKey) {
        val keys = (vocabToDefinition[vocab] ?: listOf())
                .toMutableList()
        keys.add(foreignKey)
        vocabToDefinition[vocab] = keys
    }

    fun add(vocab: JishoVocab) {
        jishoVocabFilter = jishoVocabFilter.set(nextVocabIndex, true)
        val foreignKey = ForeignKey(JISHO_VOCAB_TABLE_INDEX, nextVocabIndex)
        addVocabForeignKey(vocab.vocab, foreignKey)
        addVocabForeignKey(vocab.kana, foreignKey)
        jishoVocab[nextVocabIndex] = vocab
        nextVocabIndex++
    }

    fun add(vocab: OptimizedKoreVocab) {
        optimizedKoreVocabFilter = optimizedKoreVocabFilter.set(nextVocabIndex, true)
        val foreignKey = ForeignKey(OPTIMIZED_CORE_VOCAB_TABLE_INDEX, nextVocabIndex)
        addVocabForeignKey(vocab.vocab, foreignKey)
        addVocabForeignKey(vocab.kana, foreignKey)
        optimizedCoreVocab[nextVocabIndex] = vocab
        nextVocabIndex++
    }

    fun close() {
        nextIndexTable[VOCAB_NEXT_INDEX] = nextVocabIndex
        filterTable[JISHO_VOCAB] = jishoVocabFilter.init
        filterTable[OPTIMIZED_CORE_VOCAB] = optimizedKoreVocabFilter.init
        store.close()
    }
}