package com.jomof.nihonpipe.groveler

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class BitFieldTest {
    @Test
    fun size() {
        val bf = createBitField(1000)
        assertThat(bf.size()).isEqualTo(1000)
        assertThat(bf.spans()).isEqualTo(1)
    }

    @Test
    fun set() {
        val bf = createBitField(1000).set(500, true)
        assertThat(bf.size()).isEqualTo(1000)
        assertThat(bf[499]).isEqualTo(false)
        assertThat(bf[500]).isEqualTo(true)
        assertThat(bf[501]).isEqualTo(false)
        assertThat(bf.spans()).isEqualTo(3)
    }

    @Test
    fun set2() {
        val bf = createBitField(1000).set(0, true)
        assertThat(bf.size()).isEqualTo(1000)
        assertThat(bf[0]).isEqualTo(true)
        assertThat(bf[1]).isEqualTo(false)
        assertThat(bf.spans()).isEqualTo(2)
    }

    @Test
    fun set3() {
        val bf = createBitField(1000).set(999, true)
        assertThat(bf.size()).isEqualTo(1000)
        assertThat(bf[998]).isEqualTo(false)
        assertThat(bf[999]).isEqualTo(true)
        assertThat(bf.spans()).isEqualTo(2)
    }

    @Test
    fun adjacent() {
        var bf = createBitField(1000)
        bf = bf.set(0, true)
        bf = bf.set(1, true)
        assertThat(bf.size()).isEqualTo(1000)
        assertThat(bf.spans()).isEqualTo(2)
    }
}