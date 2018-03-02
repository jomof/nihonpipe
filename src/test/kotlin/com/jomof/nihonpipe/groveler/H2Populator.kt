package com.jomof.nihonpipe.groveler

import com.jomof.nihonpipe.groveler.schema.Store
import org.junit.Test

class H2Populator {
    @Test
    fun populate() {
        dataDir.mkdirs()
        dataDatabaseBin.delete()
        val store = Store()
        translatJishoJLPT(store)
        translateOptimizedKore(store)
        translateWaniKaniVocab(store)
        translateTanakaCorpus(store)
        store.close()
    }

    //@Test
    fun matchSentenceToVocab() {
        val store = Store()
        store.tanakaCorpusSentence.forEach { index, sentence ->
            val unpunctuated = sentence.japanese
                    .replace("。", "")
                    .replace("、", "")
            val words = unpunctuated.split(" ")
            for (word in words) {
                val vocab = store.vocabToIndex[word]
                if (vocab == null) {
                    //org.atilika.kuromoji.Tokenizer()
                    println("$word : ${sentence.japanese}")
                }
            }
        }
    }
}