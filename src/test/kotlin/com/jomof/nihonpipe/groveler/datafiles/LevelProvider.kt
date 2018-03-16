package com.jomof.nihonpipe.groveler.datafiles

import com.jomof.nihonpipe.groveler.schema.KeySentences

interface LevelProvider {
    operator fun get(level: Int): List<KeySentences>
    val size: Int
}