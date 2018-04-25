package com.jomof.nihonpipe

import com.google.common.truth.Truth.assertThat
import com.jomof.nihonpipe.datafiles.sentenceIndexRange
import com.jomof.nihonpipe.datafiles.sentenceIndexToTranslatedSentence
import com.jomof.nihonpipe.play.generateAnkiInfo
import org.junit.Test

class Test {
    @Test
    fun test() {
        for (sentenceIndex in sentenceIndexRange()) {
            val english = sentenceIndexToTranslatedSentence(sentenceIndex).english
            generateAnkiInfo(english)
        }
    }

    @Test
    fun testWeirdAlphabet() {
        val english = "IT is a major industry in India."
        val info = generateAnkiInfo(english)
        assertThat(info.romaji).isEqualTo("ＩＴ[it]  産業[sangyoo]  は[wa]  インド[indo]  " +
                "の[no]  主要[shuyoo]  産業[sangyoo]  よ[yo]  ね[ne]  。[.]")
    }
}