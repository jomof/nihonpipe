package com.jomof.nihonpipe.schema

import java.io.Serializable

data class WaniKaniVocab(
        val vocab: String,
        val kana: String,
        val meaning: String,
        val level: Int) : Serializable