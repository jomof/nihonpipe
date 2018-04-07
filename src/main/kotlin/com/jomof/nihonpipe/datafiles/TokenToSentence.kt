package com.jomof.nihonpipe.datafiles

import com.jomof.algorithm.getsert
import com.jomof.intset.IntSet
import com.jomof.intset.intSetOf
import com.jomof.nihonpipe.tokenToSentenceBin
import org.h2.mvstore.MVStore

private val db = MVStore.Builder()
        .fileName(tokenToSentenceBin.absolutePath)
        .compress()
        .open()!!

private val surfaceToSentence = db.openMap<String, IntSet>(
        "SurfaceToSentence")

private val baseformToSentence = db.openMap<String, IntSet>(
        "BaseformToSentence")

private fun populate() {
    if (surfaceToSentence.isEmpty()) {
        for (index in sentenceIndexRange()) {
            val sentence = sentenceIndexToTranslatedSentence(index)
            val tokens = tokenizeJapaneseSentence(sentence.japanese)
            for (token in tokens.tokens) {
                surfaceToSentence
                        .getsert(token.surface) { intSetOf() }
                        .add(index)
                baseformToSentence
                        .getsert(token.surface) { intSetOf() }
                        .add(index)
            }
        }
        db.commit()
        db.compactRewriteFully()
    }
}

fun surfaceTokenToSentences(token: String): IntSet {
    populate()
    return surfaceToSentence[token] ?: intSetOf()
}

fun baseformTokenToSentences(token: String): IntSet {
    populate()
    return baseformToSentence[token] ?: intSetOf()
}