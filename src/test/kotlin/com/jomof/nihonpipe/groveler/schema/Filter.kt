package com.jomof.nihonpipe.groveler.schema

import com.jomof.nihonpipe.groveler.bitfield.BitField
import java.io.Serializable

data class Filter(val bitfield: BitField) : Indexed, Serializable