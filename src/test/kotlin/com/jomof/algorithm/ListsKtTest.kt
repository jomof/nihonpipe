package com.jomof.algorithm

import com.google.common.truth.Truth.assertThat
import org.junit.Test


class ListsKtTest {

    @Test
    fun takeMaxBy() {
        val list = arrayListOf(1, 1, 0, 0)
        val max = list.takeMaxBy { it + 10000 }
        assertThat(max).isEqualTo(arrayListOf(1, 1))
    }

    @Test
    fun takeMaxBy2() {
        val list = arrayListOf(0, 1, 0, 1)
        val max = list.takeMaxBy { it + 1000 }
        assertThat(max).isEqualTo(arrayListOf(1, 1))
    }

}