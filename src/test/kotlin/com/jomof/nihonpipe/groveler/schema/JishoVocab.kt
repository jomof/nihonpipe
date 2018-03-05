package com.jomof.nihonpipe.groveler.schema

import java.io.Serializable

data class JishoVocab(
        val vocab: String,
        val kana: String,
        val meaning: String,
        val jlptLevel: String) : Serializable, Indexed {
    companion object {
        val type = TableType(JishoVocab::class)
    }
}

