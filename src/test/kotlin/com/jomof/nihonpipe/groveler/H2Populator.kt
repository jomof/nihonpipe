package com.jomof.nihonpipe.groveler

import com.jomof.nihonpipe.groveler.schema.Store
import org.junit.Test

class H2Populator {
    @Test
    fun populate() {
        dataDir.mkdirs()
        if (dataDatabase1Bin.exists()) {
            return
        }
        dataDatabase1Bin.delete()
        val store = Store(dataDatabase1Bin)
        translatJishoJLPT(store)
        translateOptimizedKore(store)
        translateWaniKaniVocab(store)
        translateTanakaCorpus(store)
        store.close()
    }

    @Test
    fun matchSentenceToVocab() {
        if (!dataDatabase1Bin.exists()) {
            populate()
        }
        dataDatabase2Bin.delete()
        dataDatabase1Bin.copyTo(dataDatabase2Bin)
        val store = Store(dataDatabase2Bin)
        var n = 1
//        store.sentenceIndexToIndex().forEach { sentenceIndex, bitfield  ->
//            store.forEachIndexed(bitfield) { indexed -> }


//            if (n % 10 == 0) println("sentence $n")
//            ++n
//            val tokens = Tokenizer()
//                    .tokenize(sentence.japanese.replace(" ", ""))
//
//            for (token in tokens) {
//                var baseForm = token.baseForm
//                var index = store.vocabToIndex()[baseForm]
//                if (index == null) {
//                    val x = "baseForm=${token.baseForm} " +
//                            "conjugationForm=${token.conjugationForm} " +
//                            "conjugationType=${token.conjugationType} " +
//                            "partOfSpeechLevel1=${token.partOfSpeechLevel1} " +
//                            "partOfSpeechLevel2=${token.partOfSpeechLevel2} " +
//                            "partOfSpeechLevel3=${token.partOfSpeechLevel3} " +
//                            "partOfSpeechLevel4=${token.partOfSpeechLevel4} " +
//                            "pronunciation=${token.pronunciation} " +
//                            "reading=${token.reading}"
//                    val y = x
//                }
//            }
//        }
        store.close()
    }
}