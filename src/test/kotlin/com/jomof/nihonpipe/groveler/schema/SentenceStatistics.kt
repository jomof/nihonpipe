package com.jomof.nihonpipe.groveler.schema

import java.io.Serializable

data class SentenceStatistics(
        val waniKaniLevel: Statistics,
        val jishoJlpt: Statistics,
        val optCore: Statistics,
        val optCoreVocabKoIndex: Statistics,
        val optCoreSentKoIndex: Statistics,
        val optCoreNewOptVocIndex: Statistics,
        val optCoreOptVocIndex: Statistics,
        val optCoreOptSenIndex: Statistics,
        val optCoreJlpt: Statistics,
        val waniKaniVsJlptWaniKaniLevel: Statistics,
        val waniKaniVsJlptJlptLevel: Statistics) : Serializable