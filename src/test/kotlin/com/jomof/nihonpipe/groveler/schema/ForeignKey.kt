package com.jomof.nihonpipe.groveler.schema

import java.io.Serializable

data class ForeignKey(
        val table: Int,
        val index: Int) : Serializable