package com.jomof.nihonpipe.groveler.datafiles

import com.jomof.nihonpipe.groveler.schema.KeySentences
import com.jomof.nihonpipe.groveler.schema.KuromojiIpadicTokenization

interface LevelProvider {
    operator fun get(level: Int): List<KeySentences>
    val size: Int
    fun keysOf(tokenization : KuromojiIpadicTokenization) : Set<String>
}