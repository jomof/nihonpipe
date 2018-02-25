package com.jomof.nihonpipe.groveler

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class BitFieldTest {
    @Test
    fun size() {
        val bf = createBitField(1000)
        assertThat(bf.size).isEqualTo(1000)
    }

    @Test
    fun set() {
        val bf = createBitField(1000).set(500, true)
        assertThat(bf.size).isEqualTo(1000)
        assertThat(bf[499]).isEqualTo(false)
        assertThat(bf[500]).isEqualTo(true)
        assertThat(bf[501]).isEqualTo(false)

    }

    @Test
    fun set2() {
        val bf = createBitField(1000).set(0, true)
        assertThat(bf.size).isEqualTo(1000)
        assertThat(bf[0]).isEqualTo(true)
        assertThat(bf[1]).isEqualTo(false)
    }

    @Test
    fun set3() {
        val bf = createBitField(1000).set(999, true)
        assertThat(bf.size).isEqualTo(1000)
        assertThat(bf[998]).isEqualTo(false)
        assertThat(bf[999]).isEqualTo(true)
    }
}