package com.jomof.nihonpipe.datafiles

import com.google.common.truth.Truth
import org.junit.Test

class SentenceFrequencyLevelsTest {
    @Test
    fun iteratePlayer() {
        val expectedOrder = listOf(
                20997, //Did you notice him coming in?
                33109 // She is like a hen with one chicken.

        )
        var prior = 0L
        var priorSentence = 0
        expectedOrder.forEach { index ->
            val next = SentenceFrequencyLevels.frequencyOrder(index)
            Truth.assertThat(next)
                    .named("$priorSentence > $index")
                    .isGreaterThan(prior)
            prior = next
            priorSentence = index
        }

        val value = SentenceFrequencyLevels.frequencyOrder(33109)
    }

}