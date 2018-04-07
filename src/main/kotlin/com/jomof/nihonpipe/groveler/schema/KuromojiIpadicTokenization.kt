package com.jomof.nihonpipe.groveler.schema

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
