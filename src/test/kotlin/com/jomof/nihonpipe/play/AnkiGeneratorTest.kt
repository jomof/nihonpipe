package com.jomof.nihonpipe.play

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AnkiGeneratorTest {
    @Test
    fun testExcludeNonNounAri() {
        val english = "The apple was not red."
        val info = generateAnkiInfo(english)
        assertThat(info.vocab).hasSize(2)
        assertThat(info.vocab.keys).doesNotContain("あり")
        println(info)
    }

    @Test
    fun testExcludeJlpt0() {
        val english = "The apple was not red."
        val info = generateAnkiInfo(english)
        assertThat(info.vocab).hasSize(2)
        assertThat(info.vocab.toString()).doesNotContain("JLPT0")
        println(info)
    }

    @Test
    fun thanks() {
        val english = "Thanks."
        val info = generateAnkiInfo(english)
        println(info)
    }

}