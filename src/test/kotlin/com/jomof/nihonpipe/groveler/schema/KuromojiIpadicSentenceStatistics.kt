package com.jomof.nihonpipe.groveler.schema

import java.io.Serializable

data class KuromojiIpadicSentenceStatistics(
        val waniKaniLevel: Statistics,
        val jishoJlpt: Statistics,
        val optCore: Statistics,
        val optCoreVocabKoIndex: Statistics,
        val optCoreSentKoIndex: Statistics,
        val optCoreNewOptVocIndex: Statistics,
        val optCoreOptVocIndex: Statistics,
        val optCoreOptSenIndex: Statistics,
        val optCoreJlpt: Statistics) : Indexed, Serializable