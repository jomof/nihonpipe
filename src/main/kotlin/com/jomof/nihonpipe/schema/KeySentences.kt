package com.jomof.nihonpipe.schema

import com.jomof.intset.IntSet
import java.io.Serializable

data class KeySentences(
        val key: String,
        val sentences: IntSet) : Serializable