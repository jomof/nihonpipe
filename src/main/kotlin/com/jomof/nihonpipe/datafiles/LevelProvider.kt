package com.jomof.nihonpipe.datafiles

import com.jomof.intset.IntSet
import com.jomof.nihonpipe.groveler.schema.KuromojiIpadicTokenization
import com.jomof.nihonpipe.schema.KeySentences

interface LevelProvider {
    fun getKeySentences(level: Int): List<KeySentences>
    fun getSentences(level: Int): IntSet
    val size: Int
    fun keysOf(tokenization: KuromojiIpadicTokenization): Set<String>
}