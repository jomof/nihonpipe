package com.jomof.nihonpipe.groveler.schema

import java.io.Serializable

data class KuromojiIpadicTokenization(
        val tokens: List<KuromojiIpadicToken>) : Indexed, Serializable