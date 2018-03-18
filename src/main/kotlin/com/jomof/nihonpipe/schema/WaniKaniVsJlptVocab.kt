package com.jomof.nihonpipe.schema

import java.io.Serializable

data class WaniKaniVsJlptVocab(
        val vocab: String,
        val kana: String,
        val wanikaniLevel: Int,
        val jlptLevel: Jlpt,
        val sense1: String,
        val sense2: String,
        val sense3: String) : Serializable