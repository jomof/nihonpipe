package com.jomof.nihonpipe.groveler.schema

import com.jomof.nihonpipe.RomajiState
import com.jomof.nihonpipe.katakanaToRomaji
import com.jomof.nihonpipe.schema.Indexed
import java.io.Serializable

data class KuromojiIpadicTokenization(
        val tokens: List<KuromojiIpadicToken>) : Indexed, Serializable {

    fun reading(): String {
        val sb = StringBuilder()
        for (token in tokens) {
            when (token.reading) {
                "*" ->
                    sb.append(token.surface)
                else -> sb.append(token.reading)
            }
        }
        return sb.toString()
    }

    fun pronunciation(): String {
        val sb = StringBuilder()
        for (token in tokens) {
            when (token.pronunciation) {
                "*" ->
                    sb.append(token.surface)
                else -> sb.append(token.pronunciation)
            }
        }
        return sb.toString()
    }

    fun romajiPronunciation(): String {
        val sb = StringBuilder()
        val state = RomajiState()
        var lastToken: KuromojiIpadicToken? = null
        for (token in tokens) {
            if (!sb.isEmpty()) {
                sb.append(" ")
            }
            when (token.pronunciation) {
                "*" ->
                    sb.append(katakanaToRomaji(token.surface, state))
                "々" -> {
                    val katakana = katakanaToRomaji(lastToken!!.pronunciation, state)
                    sb.append("[$katakana]")
                }
                else -> sb.append(katakanaToRomaji(token.pronunciation, state))
            }
            lastToken = token
        }
        return sb.toString()
    }

    fun romajiReading(): String {
        val sb = StringBuilder()
        val state = RomajiState()
        var lastToken: KuromojiIpadicToken? = null
        for (token in tokens) {
            if (!sb.isEmpty()) {
                sb.append(" ")
            }
            when (token.reading) {
                "*" ->
                    sb.append(katakanaToRomaji(token.surface, state))
                "々" -> {
                    val katakana = katakanaToRomaji(lastToken!!.reading, state)
                    sb.append("[$katakana]")
                }
                else -> sb.append(katakanaToRomaji(token.reading, state))
            }
            lastToken = token
        }
        return sb.toString()
    }

    fun romajiSurfaceFurigana(): String {
        val sb = StringBuilder()
        var state = RomajiState()
        var lastToken: KuromojiIpadicToken? = null
        for (token in tokens) {
            if (!sb.isEmpty()) {
                sb.append("  ")
            }

            sb.append(token.surface)
            when (token.pronunciation) {
                "*" -> {
                    val romaji = katakanaToRomaji(token.surface, state)
                    sb.append("[$romaji]")
                }
                "々" -> {
                    val romaji = katakanaToRomaji(lastToken!!.pronunciation, state)
                    sb.append("[$romaji]")
                }
                else -> {
                    val romaji = katakanaToRomaji(token.pronunciation, state)
                    sb.append("[$romaji]")
                }
            }
            lastToken = token
        }
        return sb.toString()
    }

    fun normalized() = tokens.joinToString(" ") { it.surface }

    companion object {
        private const val serialVersionUID = -7094035836985017858L
    }
}


fun KuromojiIpadicTokenization.grammarSummaryForm(): List<String> {
    val list = mutableListOf<String>()
    tokens.forEach { token ->
        list += mutableListOf(
                token.conjugationType,
                token.partOfSpeechLevel1,
                token.partOfSpeechLevel2,
                token.partOfSpeechLevel3,
                token.partOfSpeechLevel4)
                .filter { it != "*" }
                .joinToString(" ")
    }
    return list
}

fun KuromojiIpadicTokenization.particleSkeletonForm(): String {
    return tokens.joinToString("") { token ->
        when {
            token.partOfSpeechLevel1.contains("助詞") -> {
                token.surface
            }
            token.partOfSpeechLevel1.contains("動詞") -> {
                token.surface
            }
            token.partOfSpeechLevel1.contains("記号") -> {
                token.surface
            }
            token.surface == "。" -> ""
            else -> "x"
        }
    }
}
