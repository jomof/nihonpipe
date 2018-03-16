package com.jomof.nihonpipe.groveler.datafiles

import com.jomof.intset.IntSet
import com.jomof.intset.intSetOf
import com.jomof.nihonpipe.groveler.vocabToSentenceFilter
import org.h2.mvstore.MVStore

class VocabToSentenceFilter {
    private val db = MVStore.Builder()
            .fileName(vocabToSentenceFilter.absolutePath)
            .compress()
            .open()!!

    private val vocabFilter = db.openMap<String, IntSet>(
            "VocabToSentenceFilter")

    operator fun invoke(vocab: String) = vocabFilter[vocab] ?: intSetOf()

    init {
        if (vocabFilter.isEmpty()) {
            val tanaka = TranslatedSentences.tanaka
            val tokenize = KuromojiIpadicCache.tokenize
            val map = mutableMapOf<String, IntSet>()
            for ((index, sentence) in tanaka.sentences) {
                val tokenization = tokenize(sentence.japanese)
                for (token in tokenization.tokens) {
                    val filter = map[token.baseForm] ?: intSetOf()
                    filter += index
                    map[token.baseForm] = filter
                }
            }

            map.entries
                    .forEach { (skeleton, ix) ->
                        vocabFilter[skeleton] = ix
                    }

            db.compactRewriteFully()
            save()
            TranslatedSentences.save()
            KuromojiIpadicCache.save()
        }
    }

    companion object {
        private var instance: VocabToSentenceFilter? = null
        val sentencesOf: VocabToSentenceFilter
            get() {
                if (instance != null) {
                    return instance!!
                }
                instance = VocabToSentenceFilter()
                return sentencesOf
            }

        fun save() {
            if (instance != null) {
                instance!!.db.close()
                instance = null
            }
        }
    }
}