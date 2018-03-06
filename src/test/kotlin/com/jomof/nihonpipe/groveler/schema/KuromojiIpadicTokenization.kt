package com.jomof.nihonpipe.groveler.schema

import java.io.Serializable

data class KuromojiIpadicTokenization(
        val tokens: List<KuromojiIpadicToken>) : Indexed, Serializable


fun KuromojiIpadicTokenization.particleSkeletonForm(): String {
    var lastWasX = false
    val result = tokens.joinToString("") { token ->
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
                    lastWasX = true
                    "x"
                }
            }
        }
    }
    return result
}
