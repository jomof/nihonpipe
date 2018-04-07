package com.jomof.nihonpipe.datafiles

import com.atilika.kuromoji.ipadic.Tokenizer
import com.jomof.nihonpipe.dataKuromojiBin
import com.jomof.nihonpipe.groveler.schema.KuromojiIpadicToken
import com.jomof.nihonpipe.groveler.schema.KuromojiIpadicTokenization
import org.h2.mvstore.MVStore

private val db = MVStore.Builder()
        .fileName(dataKuromojiBin.absolutePath)
        .compress()
        .open()!!

private val kuromojiCache = db.openMap<String, KuromojiIpadicTokenization>(
        "KuromojiIpadic")

fun normalizationOfJapaneseSentence(japanese: String): String {
    val tokens = tokenizeJapaneseSentence(japanese)
    return tokens.normalized()
}

fun readingOfJapaneseSentence(japanese: String): String {
    val tokens = tokenizeJapaneseSentence(japanese)
    return tokens.reading()
}

fun pronunciationOfJapaneseSentence(japanese: String): String {
    val tokens = tokenizeJapaneseSentence(japanese)
    return tokens.pronunciation()
}

fun tokenizeJapaneseSentence(japanese: String): KuromojiIpadicTokenization {
    val cleaned = japanese.replace(" ", "")
    val lookup = kuromojiCache[cleaned]
    if (lookup != null) {
        return lookup
    }
    kuromojiCache[cleaned] =
            KuromojiIpadicTokenization(Tokenizer()
                    .tokenize(japanese)
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
    return tokenizeJapaneseSentence(japanese)
}