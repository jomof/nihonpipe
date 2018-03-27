package com.jomof.nihonpipe.play

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SrsScheduleTest {

    @Test
    fun testHoursFromLevel1ToLevel2() {
        val hours = hoursUntilNextSrsLevel(1)
        assertThat(hours)
                .isWithin(.001)
                .of(1.5)
    }

    @Test
    fun testHoursUntilBurned() {
        // Level 21 should take six months
        val hours = hoursUntilSrsLevel(21)
        val days = hours / 24
        val years = days / 365.24
        val months = years * 12
        assertThat(months)
                .isWithin(.001)
                .of(6.0)
    }
}