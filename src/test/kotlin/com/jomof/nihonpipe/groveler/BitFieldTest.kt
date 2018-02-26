package com.jomof.nihonpipe.groveler

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class BitFieldTest {
    @Test
    fun size() {
        val bf = createBitField()
        assertThat(bf.size()).isEqualTo(0)
        assertThat(bf.spans()).isEqualTo(0)
    }

    @Test
    fun set0true() {
        val bf = createBitField().set(0, true)
        assertThat(bf.size()).isEqualTo(1)
        assertThat(bf[0]).isEqualTo(true)
        assertThat(bf.spans()).isEqualTo(1)
    }

    @Test
    fun set0false() {
        val bf = createBitField().set(0, false)
        assertThat(bf.size()).isEqualTo(0)
        assertThat(bf[0]).isEqualTo(false)
        assertThat(bf.spans()).isEqualTo(0)
    }

    @Test
    fun setMiddleOf3() {
        val bf = createBitField().set(1, true)
        assertThat(bf.size()).isEqualTo(2)
        assertThat(bf[0]).isEqualTo(false)
        assertThat(bf[1]).isEqualTo(true)
        assertThat(bf[2]).isEqualTo(false)
        assertThat(bf.spans()).isEqualTo(2)
    }

    @Test
    fun set() {
        val bf = createBitField().set(500, true)
        assertThat(bf[499]).isEqualTo(false)
        assertThat(bf[500]).isEqualTo(true)
        assertThat(bf[501]).isEqualTo(false)
        assertThat(bf.size()).isEqualTo(501)
        assertThat(bf.spans()).isEqualTo(2)
    }

    @Test
    fun set2() {
        val bf = createBitField().set(0, true)
        assertThat(bf.size()).isEqualTo(1)
        assertThat(bf[0]).isEqualTo(true)
        assertThat(bf[1]).isEqualTo(false)
        assertThat(bf.spans()).isEqualTo(1)
    }

    @Test
    fun set3() {
        val bf = createBitField().set(999, true)
        assertThat(bf.size()).isEqualTo(1000)
        assertThat(bf[998]).isEqualTo(false)
        assertThat(bf[999]).isEqualTo(true)
        assertThat(bf.spans()).isEqualTo(2)
    }

    @Test
    fun set3000() {
        val bf = createBitField().set(3000, true)
        assertThat(bf.size()).isEqualTo(3001)
        assertThat(bf.spans()).isEqualTo(2)
    }

    @Test
    fun adjacent() {
        var bf = createBitField()
        bf = bf.set(0, true)
        bf = bf.set(1, true)
        assertThat(bf.size()).isEqualTo(2)
        assertThat(bf.spans()).isEqualTo(1)
    }

    @Test
    fun reset() {
        var bf = createBitField()
        bf = bf.set(0, true)
        bf = bf.set(0, false)
        assertThat(bf[0]).isFalse()
        assertThat(bf.size()).isEqualTo(0)
        assertThat(bf.spans()).isEqualTo(0)
    }

    @Test
    fun repro() {
        var v = createBitField()
        v = v.set(3, true) // 3-1+
        v = v.set(4, false)
    }

    @Test
    fun several() {
        var v = createBitField()
        for (a in 0..1) {
            v = v.set(0, a == 1)
            for (b in 0..1) {
                v = v.set(1, b == 1)
                for (c in 0..1) {
                    v = v.set(2, c == 1)
                    for (d in 0..1) {
                        v = v.set(3, d == 1)
                        for (e in 0..1) {
                            v = v.set(4, e == 1)
                            assertThat(v[4]).isEqualTo(e == 1)
                        }
                        assertThat(v[3]).isEqualTo(d == 1)
                    }
                    assertThat(v[2]).isEqualTo(c == 1)
                }
                assertThat(v[1]).isEqualTo(b == 1)
            }
            assertThat(v[0]).isEqualTo(a == 1)
        }
    }
}