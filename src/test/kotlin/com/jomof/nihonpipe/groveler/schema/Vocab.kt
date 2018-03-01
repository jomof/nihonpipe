package com.jomof.nihonpipe.groveler.schema

import java.io.Serializable

data class JishoVocab(
        val kanji: String,
        val kana: String,
        val meaning: String,
        val jlptLevel: String,
        val jishoOrder: Int) : Serializable