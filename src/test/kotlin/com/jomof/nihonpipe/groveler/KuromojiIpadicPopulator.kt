package com.jomof.nihonpipe.groveler

import com.atilika.kuromoji.ipadic.Tokenizer
import com.jomof.nihonpipe.groveler.schema.*
import org.h2.mvstore.MVStore

fun populateKuromojiBatch(db: Store, batchSize: Int) {
    val kuromojiCacheStore = MVStore.Builder()
            .fileName(dataKuromojiBin.absolutePath)
            .compress()
            .open()!!
    val kuromojiCache = kuromojiCacheStore.openMap<String, KuromojiIpadicTokenization>("KuromojiIpadic")
    fun kuromoji(sentence: String): KuromojiIpadicTokenization {
        val lookup = kuromojiCache[sentence]
        if (lookup != null) {
            return lookup
        }
        kuromojiCache[sentence] =
                KuromojiIpadicTokenization(Tokenizer()
                        .tokenize(sentence)
                        .map { token ->
                            KuromojiIpadicToken(
                                    baseForm = token.baseForm!!,
                                    conjugationForm = token.conjugationForm!!,
                                    conjugationType = token.conjugationType!!,
                                    partOfSpeechLevel1 = token.partOfSpeechLevel1!!,
                                    partOfSpeechLevel2 = token.partOfSpeechLevel2!!,
                                    partOfSpeechLevel3 = token.partOfSpeechLevel3!!,
                                    partOfSpeechLevel4 = token.partOfSpeechLevel4!!,
                                    pronunciation = token.pronunciation!!,
                                    reading = token.reading!!)
                        })
        return kuromoji(sentence)
    }
    db.sentenceIndexToIndex
            .toSequence()
            .removeRowsContaining(db.kuromojiIpadicTokenization)
            .take(batchSize)
            .map { (row, indices) ->
                Row(row, db[indices]
                        .filterIsInstance<TanakaCorpusSentence>()
                        .first())
            }.map { (row, tanaka) ->
                Row(row, kuromoji(tanaka.japanese.replace(" ", "")))
            }
            .toList()
            .forEach { (row, kuromoji) ->
                db.add(row, kuromoji)
            }
    kuromojiCacheStore.close()
}