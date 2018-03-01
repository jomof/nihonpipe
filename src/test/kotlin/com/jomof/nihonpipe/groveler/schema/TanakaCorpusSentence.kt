package com.jomof.nihonpipe.groveler.schema

import java.io.Serializable

data class TanakaCorpusSentence(
        val japanese: String,
        val code: String,
        val tid: String,
        val english: String,
        val filename: String) : Serializable