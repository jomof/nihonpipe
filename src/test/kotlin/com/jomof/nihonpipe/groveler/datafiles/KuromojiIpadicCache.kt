package com.jomof.nihonpipe.groveler.datafiles

import com.atilika.kuromoji.ipadic.Tokenizer
import com.jomof.nihonpipe.groveler.dataKuromojiBin
import com.jomof.nihonpipe.groveler.schema.KuromojiIpadicToken
import com.jomof.nihonpipe.groveler.schema.KuromojiIpadicTokenization
import org.h2.mvstore.MVStore

class KuromojiIpadicCache private constructor(
        file: String = dataKuromojiBin.absolutePath) {
    private val db = MVStore.Builder()
            .fileName(file)
            .compress()
            .open()!!
    private val kuromojiCache = db.openMap<String, KuromojiIpadicTokenization>(
            "KuromojiIpadic")

    private fun tokenize(sentence: String): KuromojiIpadicTokenization {
        val cleaned = sentence.replace(" ", "")
        val lookup = kuromojiCache[cleaned]
        if (lookup != null) {
            return lookup
        }
        kuromojiCache[cleaned] =
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
        return tokenize(sentence)
    }

    operator fun invoke(sentence: String) = tokenize(sentence)

    companion object {
        private var instance: KuromojiIpadicCache? = null
        val tokenize: KuromojiIpadicCache
            get() {
                if (instance != null) {
                    return instance!!
                }
                instance = KuromojiIpadicCache()
                return tokenize
            }

        fun save() {
            if (instance != null) {
                instance!!.db.close()
                instance = null
            }
        }
    }
}