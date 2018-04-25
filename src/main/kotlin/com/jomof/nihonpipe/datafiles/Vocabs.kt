package com.jomof.nihonpipe.datafiles

import com.jomof.nihonpipe.schema.OptimizedKoreVocab
import com.jomof.nihonpipe.schema.WaniKaniVocab
import com.jomof.nihonpipe.schema.WaniKaniVsJlptVocab

data class VocabInfo(
        val waniKani: WaniKaniVocab?,
        val waniKaniVsJlptVocab: WaniKaniVsJlptVocab?,
        val optimizedKoreVocabs: List<OptimizedKoreVocab>
)

fun vocabInfo(vocab: String): VocabInfo {
    return VocabInfo(
            waniKani = WanikaniVocabs.vocabOf(vocab),
            waniKaniVsJlptVocab = WanikaniVsJlptVocabs.vocabOf(vocab),
            optimizedKoreVocabs = OptimizedKoreVocabs.vocabOf(vocab) ?: mutableListOf()
    )
}