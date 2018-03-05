package com.jomof.nihonpipe.groveler

import com.atilika.kuromoji.ipadic.Tokenizer
import com.jomof.nihonpipe.groveler.schema.*

fun populateKuromojiBatch(db: Store, batchSize: Int) {
    db.sentenceIndexToIndex
            .toSequence()
            .removeRowsContaining(db.kuromojiIpadicTokenization)
            .take(batchSize)
            .map { (row, indices) ->
                Row(row, db[indices]
                        .filterIsInstance<TanakaCorpusSentence>()
                        .first())
            }.map { (row, tanaka) ->
                Row(row, KuromojiIpadicTokenization(Tokenizer()
                        .tokenize(tanaka.japanese.replace(" ", ""))
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
                        }))
            }
            .toList()
            .forEach { (row, kuromoji) -> db.add(row, kuromoji) }
}