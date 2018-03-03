package com.jomof.nihonpipe.groveler.bitfield

import java.io.Serializable

class BitField(
        private var init: String = "",
        private var sizeField: Int = 0) : Serializable {

    val size: Int get() = sizeField
    val segments: Iterable<Segment>
        get() = getBitFieldSegmentsIterable(init)

    private fun copyFrom(segments: List<Segment>) {
        val bf = segments.toBitField()
        this.init = bf.init
        this.sizeField = bf.size
    }

    operator fun set(key: Int, value: Boolean) {
        val segments = segments.toMutableList()
        segments[key] = value
        copyFrom(segments)
    }

    operator fun get(key: Int): Boolean {
        return segments[key]
    }
}

