package com.jomof.nihonpipe.schema

import java.io.Serializable

data class OptimizedKoreVocab(
        val core: Int,
        val vocabKoIndex: Int,
        val sentKoIndex: Int,
        val newOptVocIndex: Int,
        val optVocIndex1: Int,
        val optSenIndex: Int,
        val jlpt: Jlpt,
        val vocab: String,
        val kana: String,
        val english: String,
        val pos: String) : Serializable