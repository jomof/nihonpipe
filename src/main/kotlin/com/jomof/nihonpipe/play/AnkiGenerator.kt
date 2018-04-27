package com.jomof.nihonpipe.play

import com.jomof.algorithm.getsert
import com.jomof.nihonpipe.datafiles.englishToSentenceIndexes
import com.jomof.nihonpipe.datafiles.sentenceIndexToTranslatedSentence
import com.jomof.nihonpipe.datafiles.tokenizeJapaneseSentence
import com.jomof.nihonpipe.datafiles.vocabInfo
import com.jomof.nihonpipe.groveler.schema.KuromojiIpadicToken
import com.jomof.nihonpipe.groveler.schema.KuromojiIpadicTokenization
import com.jomof.nihonpipe.groveler.schema.particleSkeletonForm
import com.jomof.nihonpipe.schema.Jlpt

data class AnkiInfo(
        val english: String,
        val romaji: String,
        val meanings: Set<String>,
        val vocab: Map<String, AnkiVocab>,
        val notes: List<String>
)

data class AnkiVocab(
        val token: KuromojiIpadicToken,
        val jlpt: Jlpt?,
        val wanikani: Int?,
        val optKoreIndex: Int?,
        val partOfSpeech: String?,
        val pronunciation: String,
        val definition: String
)

data class AnkiSentenceInfo(
        val japanese: String,
        val tokenization: KuromojiIpadicTokenization,
        val pronunciation: String = tokenization.pronunciation(),
        val reading: String = tokenization.reading(),
        val romajiSurfaceFuragana: String = tokenization.romajiSurfaceFurigana(),
        val romajiPronunciation: String = tokenization.romajiPronunciation(),
        val romajiReading: String = tokenization.romajiReading()
)

data class AnkiVocabBuilder(
        val token: KuromojiIpadicToken,
        var jlpt: Jlpt? = null,
        var wanikani: Int? = null,
        var optKoreIndex: Int? = null,
        var partOfSpeech: String? = null,
        var pronunciation: String? = null,
        var definition: String? = null
) {
    fun toVocab(): AnkiVocab {
        return AnkiVocab(
                token = token,
                jlpt = jlpt,
                wanikani = wanikani,
                optKoreIndex = optKoreIndex,
                partOfSpeech = partOfSpeech,
                pronunciation = pronunciation!!,
                definition = definition!!
        )
    }
}

data class AnkiInfoBuilder(
        val english: String,
        var romaji: String? = null,
        val sentenceIndexes: Set<Int> = mutableSetOf(),
        val sentences: MutableList<AnkiSentenceInfo> = mutableListOf(),
        val meanings: MutableSet<String> = mutableSetOf(),
        val vocabs: MutableMap<String, AnkiVocabBuilder> = mutableMapOf(),
        val notes: MutableList<String> = mutableListOf()
) {
    private fun chooseLonger(original: String?, proposed: String?): String? {
        if (proposed == null) {
            return original
        }
        if (original == null) {
            return proposed
        }
        if (original.length >= proposed.length) {
            return original
        }
        return proposed
    }

    private fun chooseShorter(original: String?, proposed: String?): String? {
        if (proposed == null) {
            return original
        }
        if (original == null) {
            return proposed
        }
        if (original.length <= proposed.length) {
            return original
        }
        return proposed
    }

    private fun chooseJlpt(original: Jlpt?, proposed: Jlpt?): Jlpt? {
        if (proposed == Jlpt.JLPT6 || proposed == Jlpt.JLPT0) {
            return original
        }
        if (original == null) {
            return proposed
        }
        return original
    }

    fun addSentence(sentenceIndex: Int) {
        val japanese = sentenceIndexToTranslatedSentence(sentenceIndex).japanese
        val tokenization = tokenizeJapaneseSentence(japanese)
        val sentenceInfo = AnkiSentenceInfo(
                japanese = japanese,
                tokenization = tokenization
        )
        romaji = chooseShorter(romaji, sentenceInfo.romajiSurfaceFuragana)
        meanings += sentenceInfo.romajiReading
        meanings += sentenceInfo.romajiPronunciation
        meanings += sentenceInfo.reading
        meanings += sentenceInfo.pronunciation
        notes += "Skeleton: ${tokenization.particleSkeletonForm()}"
        notes += "Pronunciation: ${sentenceInfo.pronunciation}/${sentenceInfo.romajiPronunciation}"
        notes += "Reading: ${sentenceInfo.reading}/${sentenceInfo.romajiReading}"

        for (token in tokenization.tokens) {
            val vocab = vocabs.getsert(token.surface, { AnkiVocabBuilder(token) })
            val vocabInfo = vocabInfo(token.surface)
            val waniKaniVsJlptVocab = vocabInfo.waniKaniVsJlptVocab
            vocab.pronunciation = token.pronunciation
            if (waniKaniVsJlptVocab != null) {
                vocab.jlpt = chooseJlpt(vocab.jlpt, waniKaniVsJlptVocab.jlptLevel)
                if (waniKaniVsJlptVocab.wanikaniLevel in 1..60) {
                    vocab.wanikani = vocab.wanikani ?: waniKaniVsJlptVocab.wanikaniLevel
                }
                vocab.definition = chooseLonger(vocab.definition, waniKaniVsJlptVocab.sense1)
            }
            for (optKore in vocabInfo.optimizedKoreVocabs) {
                vocab.jlpt = chooseJlpt(vocab.jlpt, optKore.jlpt)
                vocab.definition = chooseLonger(vocab.definition, optKore.english)
                vocab.partOfSpeech = chooseLonger(vocab.partOfSpeech, optKore.pos)
            }
        }
        sentences += sentenceInfo
    }

    fun toInfo(): AnkiInfo {
        val vocabs = mutableMapOf<String, AnkiVocab>()
        for ((key, value) in this.vocabs) {
            if (value.definition == null) {
                continue
            }
            if (!partOfSpeechAgrees(value.partOfSpeech, value.token)) {
                continue
            }
            vocabs[key] = value.toVocab()
        }

        return AnkiInfo(
                english = english,
                romaji = romaji!!,
                meanings = meanings,
                vocab = vocabs,
                notes = notes
        )
    }

    private fun partOfSpeechAgrees(
            partOfSpeech: String?,
            token: KuromojiIpadicToken): Boolean {
        if (partOfSpeech == null) {
            return true
        }
        // Noun != verb
        if (partOfSpeech == "Noun" && token.partOfSpeechLevel1 == "動詞") {
            return false
        }
        return true
    }
}

fun generateAnkiInfo(english: String): AnkiInfo {
    val sentenceIndexes = englishToSentenceIndexes(english)
    val builder = AnkiInfoBuilder(english = english)
    for (sentenceIndex in sentenceIndexes) {
        builder.addSentence(sentenceIndex)
    }
    return builder.toInfo()
}

fun AnkiInfo.htmlDefinitions(): String {
    if (this.vocab.isEmpty()) {
        return ""
    }
    val sb = StringBuilder()
    sb.append("<ul>")
    for ((word, vocab) in this.vocab) {
        sb.append("<li>")
        sb.append("<b>$word</b>/")
        sb.append("${vocab.pronunciation} ")
        if (vocab.partOfSpeech != null) {
            sb.append("<i>${vocab.partOfSpeech.toLowerCase()}</i> ")
        }
        sb.append("${vocab.definition} ")
        val levels = mutableListOf<String>()
        if (vocab.jlpt != null) {
            levels += "${vocab.jlpt}"
        }
        if (vocab.wanikani != null) {
            levels += "Wankikani Level ${vocab.wanikani}"
        }
        if (vocab.optKoreIndex != null) {
            levels += "Opt Kore Index ${vocab.optKoreIndex}"
        }
        if (levels.isNotEmpty()) {
            sb.append("[${levels.joinToString()}]")
        }
        sb.append("</li>")
    }
    sb.append("</ul>")
    return sb.toString()
}

fun AnkiInfo.htmlNotes(): String {
    if (this.notes.isEmpty()) {
        return ""
    }
    val sb = StringBuilder()
    sb.append("<ul>")
    for (note in notes) {
        sb.append("<li>")
        sb.append(note)
        sb.append("</li>")
    }
    sb.append("</ul>")
    return sb.toString()
}