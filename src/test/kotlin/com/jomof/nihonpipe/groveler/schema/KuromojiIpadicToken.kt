package com.jomof.nihonpipe.groveler.schema

import java.io.Serializable

data class KuromojiIpadicToken(
        val surface: String,
        val baseForm: String,
        val conjugationForm: String,
        val conjugationType: String,
        val partOfSpeechLevel1: String,
        val partOfSpeechLevel2: String,
        val partOfSpeechLevel3: String,
        val partOfSpeechLevel4: String,
        val reading: String,
        val pronunciation: String) : Serializable
