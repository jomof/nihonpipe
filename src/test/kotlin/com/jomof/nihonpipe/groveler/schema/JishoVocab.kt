package com.jomof.nihonpipe.groveler.schema

import java.io.Serializable

data class JishoVocab(
        val vocab: String,
        val kana: String,
        val jlptLevel: Jlpt,
        val meaning: String) : Serializable


