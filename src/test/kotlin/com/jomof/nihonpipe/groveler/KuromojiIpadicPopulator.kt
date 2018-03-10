package com.jomof.nihonpipe.groveler

import com.atilika.kuromoji.ipadic.Tokenizer
import com.jomof.nihonpipe.groveler.bitfield.BitField
import com.jomof.nihonpipe.groveler.bitfield.toSetBitIndices
import com.jomof.nihonpipe.groveler.schema.*
import org.h2.mvstore.MVStore

fun populateKuromojiBatch(db: Store, millis: Int) {
    val kuromojiCacheStore = MVStore.Builder()
            .fileName(dataKuromojiBin.absolutePath)
            .compress()
            .open()!!
    val kuromojiCache = kuromojiCacheStore.openMap<String, KuromojiIpadicTokenization>("KuromojiIpadic")
    println("cache size = ${kuromojiCache.size}")
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
    var after = 0
    var before = 0

    db.sentenceIndexToIndex
            .toSequence()
            .removeRowsContaining(db.kuromojiIpadicTokenization)
            .onEach {
                ++before
            }
            .takeWhile {
                (System.currentTimeMillis() - start) < millis
            }
            .onEach {
                ++after
            }
            .map { (row, indices) ->
                Row(row, db[indices]
                        .filterIsInstance<TanakaCorpusSentence>()
                        .takeOnly())
            }.map { (row, tanaka) ->
                Row(row, kuromoji(tanaka.japanese.replace(" ", "")))
            }
            .forEach { (row, kuromoji) ->
                db.add(row, kuromoji)
            }
    kuromojiCacheStore.close()
    println("before=$before after=$after")
}

fun populateKuromojiTokenSentenceStatistics(db: Store) {
    db.sentenceIndexToIndex
            .toSequence()
            .removeRowsContaining(db.kuromojiIpadicSentenceStatistics)
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
                var waniKaniVsJlptWaniKaniLevel = Statistics()
                var waniKaniVsJlptJlptLevel = Statistics()
                tokens.forEach { (kuromoji, vocabs) ->
                    vocabs.forEach { vocab ->
                        when (vocab) {
                            is WaniKaniVocab -> {
                                waniKaniLevel += vocab.level
                            }
                            is JishoVocab -> {
                                jishoJlpt += vocab.jlptLevel.ordinal
                            }
                            is OptimizedKoreVocab -> {
                                optCore += vocab.core
                                optCoreVocabKoIndex += vocab.vocabKoIndex
                                optCoreSentKoIndex += vocab.sentKoIndex
                                optCoreNewOptVocIndex += vocab.newOptVocIndex
                                optCoreOptVocIndex += vocab.optVocIndex1
                                optCoreOptSenIndex += vocab.optSenIndex
                                optCoreJlpt += vocab.jlpt.ordinal
                            }
                            is WaniKaniVsJlptVocab -> {
                                waniKaniVsJlptWaniKaniLevel += vocab.wanikaniLevel
                                waniKaniVsJlptJlptLevel += vocab.jlptLevel.ordinal
                            }
                            else -> throw RuntimeException(vocab.toString())
                        }
                    }

                }
                Row(row, KuromojiIpadicSentenceStatistics(
                        waniKaniLevel = waniKaniLevel,
                        jishoJlpt = jishoJlpt,
                        optCore = optCore,
                        optCoreVocabKoIndex = optCoreVocabKoIndex,
                        optCoreSentKoIndex = optCoreSentKoIndex,
                        optCoreNewOptVocIndex = optCoreNewOptVocIndex,
                        optCoreOptVocIndex = optCoreOptVocIndex,
                        optCoreOptSenIndex = optCoreOptSenIndex,
                        optCoreJlpt = optCoreJlpt,
                        waniKaniVsJlptWaniKaniLevel = waniKaniVsJlptWaniKaniLevel,
                        waniKaniVsJlptJlptLevel = waniKaniVsJlptJlptLevel))
            }.forEach { (row, statistics) ->
                db.add(row, statistics)
            }
}


fun populateKuromojiTokenSentenceStructure(db: Store) {
    if (db.levels.containsKey(LevelType.SENTENCE_SKELETON)) {
        return
    }
    val skeletonToSentence = mutableMapOf<String, BitField>()
    val grammarElementToSentence = mutableMapOf<String, BitField>()

    db.sentenceIndexToIndex
            .toSequence()
            .keepOnlyRowsContaining(db.kuromojiIpadicTokenization)
            .keepInstances<KuromojiIpadicTokenization>(db)
            .indexInto(skeletonToSentence) { it.particleSkeletonForm() }
            .indexEachInto(grammarElementToSentence) { it.grammarSummaryForm() }
            .count()

    db.set(LevelType.GRAMMAR_ELEMENT, LevelInfo(grammarElementToSentence
            .toList()
            .sortedWith(compareByDescending<Pair<String, BitField>>
            { it.second.toSetBitIndices().count() }
                    .thenBy { it.first.length })
            .chunked(2)
            .take(60)
            .mapIndexed { level, chunk ->
                Level(level, chunk.map {
                    LevelElement(
                            level = level,
                            key = it.first,
                            sentenceIndex = it.second)
                })
            }
            .onEach { level ->
                println("level = $level")
            }))

    db.set(LevelType.SENTENCE_SKELETON, LevelInfo(skeletonToSentence
            .toList()
            .sortedWith(compareByDescending<Pair<String, BitField>>
            { it.second.toSetBitIndices().count() }
                    .thenBy { it.first.length })
            .chunked(6)
            .take(60)
            .mapIndexed { level, chunk ->
                Level(level, chunk.map {
                    LevelElement(
                            level = level,
                            key = it.first,
                            sentenceIndex = it.second)
                })
            }
            .onEach { level ->
                println("level = $level")
            }))
}


