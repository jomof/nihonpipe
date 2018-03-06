package com.jomof.nihonpipe.groveler

import com.atilika.kuromoji.ipadic.Tokenizer
import com.jomof.nihonpipe.groveler.schema.*
import org.h2.mvstore.MVStore

fun populateKuromojiBatch(db: Store, millis: Int) {
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
                                    surface = token.surface,
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

    val start = System.currentTimeMillis()
    db.sentenceIndexToIndex
            .toSequence()
            .removeRowsContaining(db.kuromojiIpadicTokenization)
            .map { (row, indices) ->
                Row(row, db[indices]
                        .filterIsInstance<TanakaCorpusSentence>()
                        .takeOnly())
            }.map { (row, tanaka) ->
                Row(row, kuromoji(tanaka.japanese.replace(" ", "")))
            }.takeWhile {
                (System.currentTimeMillis() - start) < millis
            }.toList()
            .forEach { (row, kuromoji) ->
                db.add(row, kuromoji)
            }
    kuromojiCacheStore.close()
}

fun getKuromojiTokenizationWithout(db: Store, table: IndexedTable<*>) =
        db.sentenceIndexToIndex
                .toSequence()
                .removeRowsContaining(table)
                .keepOnlyRowsContaining(db.kuromojiIpadicTokenization)
                .keepInstances<KuromojiIpadicTokenization>(db)
                .map { (row, indices) -> Row(row, indices.tokens) }
                .map { (row, tokens) ->
                    Row(row, tokens
                            .map { token -> Pair(token, db.vocabToIndex[token.baseForm]) }
                            .filter { (token, vocab) -> vocab != null })
                }.filter { (_, tokens) ->
                    tokens.isNotEmpty()
                }.map { (row, tokens) ->
                    Row(row, tokens
                            .map { (kuromoji, vocab) ->
                                Pair(kuromoji, db[vocab!!])
                            })
                }

fun populateKuromojiTokenSentenceStatistics(db: Store) {
    getKuromojiTokenizationWithout(db, db.kuromojiIpadicSentenceStatistics)
            .map { (row, tokens) ->
                var waniKaniLevel = Statistics()
                var jishoJlpt = Statistics()
                var optCore = Statistics()
                var optCoreVocabKoIndex = Statistics()
                var optCoreSentKoIndex = Statistics()
                var optCoreNewOptVocIndex = Statistics()
                var optCoreOptVocIndex = Statistics()
                var optCoreOptSenIndex = Statistics()
                var optCoreJlpt = Statistics()
                tokens.forEach { (kuromoji, vocabs) ->
                    vocabs.forEach { vocab ->
                        when (vocab) {
                            is WaniKaniVocab -> {
                                waniKaniLevel += vocab.level
                            }
                            is JishoVocab -> {
                                jishoJlpt += vocab.jlptLevel
                            }
                            is OptimizedKoreVocab -> {
                                optCore += vocab.core
                                optCoreVocabKoIndex += vocab.vocabKoIndex
                                optCoreSentKoIndex += vocab.sentKoIndex
                                optCoreNewOptVocIndex += vocab.newOptVocIndex
                                optCoreOptVocIndex += vocab.optVocIndex1
                                optCoreOptSenIndex += vocab.optSenIndex
                                optCoreJlpt += jlptToInt(vocab.jlpt)
                            }
                            else -> throw RuntimeException(vocab.toString())
                        }
                    }

                }
                Row(row, KuromojiIpadicSentenceStatistics(
                        waniKaniLevel,
                        jishoJlpt,
                        optCore,
                        optCoreVocabKoIndex,
                        optCoreSentKoIndex,
                        optCoreNewOptVocIndex,
                        optCoreOptVocIndex,
                        optCoreOptSenIndex,
                        optCoreJlpt))
            }.forEach { (row, statistics) ->
                db.add(row, statistics)
            }
}


fun populateKuromojiTokenSentenceStructure(db: Store) {
    var groups = db.sentenceIndexToIndex
            .toSequence()
            .keepOnlyRowsContaining(db.kuromojiIpadicTokenization)
            .keepInstances<KuromojiIpadicTokenization>(db)
            .map { (row, tokenization) ->
                val skeleton = tokenization.particleSkeletonForm()
                if (skeleton == "xはxだx") {
                    var rebuild = tokenization.tokens
                            .joinToString(" ") { it.surface }
                    tokenization.particleSkeletonForm()
                }
                skeleton
            }
            .toList()
            .groupBy { it }
            .map { (key, value) -> Pair(key, value) }
            .filter { (key, value) -> value.size > 1 }
            .sortedBy { (key, value) -> value.size }
            .onEach { (key, value) ->
                println("$key = ${value.size}")
            }.count()
    println("groups=$groups")
}