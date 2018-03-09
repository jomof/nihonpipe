package com.jomof.nihonpipe.groveler.schema

import java.io.Serializable

data class KuromojiIpadicTokenization(
        val tokens: List<KuromojiIpadicToken>) : Indexed, Serializable


fun KuromojiIpadicTokenization.grammarSummaryForm(): Set<String> {
    val set = mutableSetOf<String>()
    tokens.forEach { token ->
        set += token.conjugationType
        set += token.partOfSpeechLevel1
        set += token.partOfSpeechLevel2
        set += token.partOfSpeechLevel3
        set += token.partOfSpeechLevel4
    }

    set -= ""
    set -= "*"
    set -= "記号"
    set -= "句点"
    set -= "一般"

    return set
}

fun KuromojiIpadicTokenization.particleSkeletonForm(): String {
    var lastWasX = false
    return tokens.joinToString("") { token ->
        when {
            token.partOfSpeechLevel1.contains("助詞") -> {
                lastWasX = false
                token.surface
            }
            token.partOfSpeechLevel1.contains("動詞") -> {
                lastWasX = false
                token.surface
            }
            token.partOfSpeechLevel1.contains("記号") -> {
                lastWasX = false
                token.surface
            }
            else -> when (lastWasX) {
                true -> ""
                else -> {
                    lastWasX = false
                    "x"
                }
            }
        }
    }
}
