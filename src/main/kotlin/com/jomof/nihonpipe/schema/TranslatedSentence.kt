package com.jomof.nihonpipe.schema

import java.io.Serializable

data class TranslatedSentence(
        val japanese: String,
        val english: String) : Serializable
