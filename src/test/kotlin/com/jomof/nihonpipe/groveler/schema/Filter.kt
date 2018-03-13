package com.jomof.nihonpipe.groveler.schema

import com.jomof.intset.IntSet
import java.io.Serializable

data class Filter(val bitfield: IntSet) : Indexed, Serializable