package com.jomof.nihonpipe.groveler

import com.google.common.truth.Truth.assertThat
import com.jomof.nihonpipe.groveler.bitfield.BitField
import com.jomof.nihonpipe.groveler.bitfield.and
import com.jomof.nihonpipe.groveler.bitfield.bitFieldOf
import com.jomof.nihonpipe.groveler.bitfield.minus
import org.junit.Test

class BitFieldTest {

    @Test
    fun rangeInit() {
        val bf = bitFieldOf(0..5 to true)
        assertThat(bf.size).isEqualTo(6)
        assertThat(bf.segments.count()).isEqualTo(1)
        assertThat(bf[5]).isEqualTo(true)
    }

    @Test
    fun repro2() {
        val bf = BitField(init = "3jqa-3803+", sizeField = 315877)
        assertThat(bf[165543]).isTrue()
        assertThat(bf.size).isEqualTo(315877)
        bf[165543] = true
        assertThat(bf[165543]).isTrue()
        assertThat(bf.size).isEqualTo(315877)
    }

    @Test
    fun bitfieldof() {
        val bf1 = bitFieldOf(1..2 to true)
        assertThat(bf1[0]).isFalse()
        assertThat(bf1[1]).isTrue()
        assertThat(bf1[2]).isTrue()
    }

    @Test
    fun and() {
        val bf1 = bitFieldOf(0..5 to true)
        val bf2 = bitFieldOf(3..4 to true)
        val bf3 = bf1 and bf2
        println(bf3)
        assertThat(bf3[0]).isFalse()
        assertThat(bf3[1]).isFalse()
        assertThat(bf3[2]).isFalse()
        assertThat(bf3[3]).isTrue()
        assertThat(bf3[4]).isTrue()
        assertThat(bf3[5]).isFalse()
    }

    @Test
    fun size() {
        val bf = bitFieldOf()
        assertThat(bf.size).isEqualTo(0)
        assertThat(bf.segments.count()).isEqualTo(0)
    }

    @Test
    fun set0true() {
        val bf = bitFieldOf()
        bf[0] = true
        assertThat(bf.size).isEqualTo(1)
        assertThat(bf[0]).isEqualTo(true)
        assertThat(bf.segments.count()).isEqualTo(1)
    }

    @Test
    fun set0false() {
        val bf = bitFieldOf()
        bf[0] = false
        assertThat(bf.size).isEqualTo(0)
        assertThat(bf[0]).isEqualTo(false)
        assertThat(bf.segments.count()).isEqualTo(0)
    }

    @Test
    fun setMiddleOf3() {
        val bf = bitFieldOf()
        bf[1] = true
        assertThat(bf.size).isEqualTo(2)
        assertThat(bf[0]).isEqualTo(false)
        assertThat(bf[1]).isEqualTo(true)
        assertThat(bf[2]).isEqualTo(false)
        assertThat(bf.segments.count()).isEqualTo(2)
    }

    @Test
    fun set() {
        val bf = bitFieldOf()
        bf[500] = true
        assertThat(bf[499]).isEqualTo(false)
        assertThat(bf[500]).isEqualTo(true)
        assertThat(bf[501]).isEqualTo(false)
        assertThat(bf.size).isEqualTo(501)
        assertThat(bf.segments.count()).isEqualTo(2)
    }

    @Test
    fun set2() {
        val bf = bitFieldOf()
        bf[0] = true
        assertThat(bf.size).isEqualTo(1)
        assertThat(bf[0]).isEqualTo(true)
        assertThat(bf[1]).isEqualTo(false)
        assertThat(bf.segments.count()).isEqualTo(1)
    }

    @Test
    fun set3() {
        val bf = bitFieldOf()
        bf[999] = true
        assertThat(bf.size).isEqualTo(1000)
        assertThat(bf[998]).isEqualTo(false)
        assertThat(bf[999]).isEqualTo(true)
        assertThat(bf.segments.count()).isEqualTo(2)
    }

    @Test
    fun set3000() {
        val bf = bitFieldOf()
        bf[3000] = true
        assertThat(bf.size).isEqualTo(3001)
        assertThat(bf.segments.count()).isEqualTo(2)
    }

    @Test
    fun adjacent() {
        val bf = bitFieldOf()
        bf[0] = true
        bf[1] = true
        assertThat(bf.size).isEqualTo(2)
        assertThat(bf.segments.count()).isEqualTo(1)
    }

    @Test
    fun reset() {
        val bf = bitFieldOf()
        bf[0] = true
        bf[0] = false
        assertThat(bf[0]).isFalse()
        assertThat(bf.size).isEqualTo(0)
        assertThat(bf.segments.count()).isEqualTo(0)
    }

    @Test
    fun repro() {
        val bf = bitFieldOf()
        bf[3] = true
        bf[4] = false
    }

    @Test
    fun minus1() {
        val bf1 = bitFieldOf(0..100 to true)
        val bf2 = bitFieldOf(50..100 to true)
        val result = bf1 minus bf2
        assertThat(result[49]).isTrue()
        assertThat(result[50]).isFalse()
        assertThat(result[51]).isFalse()
    }

    @Test
    fun minus2() {
        val bf1 = bitFieldOf(0..100 to true)
        val bf2 = bitFieldOf()
        val result = bf1 minus bf2
        assertThat(result[49]).isTrue()
        assertThat(result[50]).isTrue()
        assertThat(result[51]).isTrue()
    }

    @Test
    fun minus3() {
        val filter = BitField("36qg-2v0c+", 282196)
        val other = BitField("", 0)
        val result = filter minus other
        assertThat(result).isEqualTo(filter)
    }

    @Test
    fun repro3() {
        val v = BitField(init = "1-1+1-2+", sizeField = 5)
        v[3] = false
        assertThat(v[3]).isEqualTo(false)
    }

    @Test
    fun several() {
        val v = bitFieldOf()
        for (a in 0..1) {
            println("v[0]=${a == 1}")
            v[0] = a == 1
            for (b in 0..1) {
                println("v[1]=${b == 1}")
                v[1] = b == 1
                for (c in 0..1) {
                    println("v[2]=${c == 1}")
                    v[2] = c == 1
                    for (d in 0..1) {
                        println("v[3]=${d == 1}")
                        v[3] = d == 1
                        for (e in 0..1) {
                            println("v[4]=${e == 1}")
                            v[4] = e == 1
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