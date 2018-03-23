package com.jomof.nihonpipe.play

import com.jomof.intset.IntSet
import com.jomof.intset.forEachElement
import com.jomof.intset.intSetOf
import com.jomof.intset.minus

class LeastBurdenSentenceTransitions {

    fun getNextSentences(sentence : Int) : Pair<IntSet, Int> {
        if (sentenceToNextSentences.containsKey(sentence)) {
            return Pair(sentenceToNextSentences[sentence]!!,
                    sentenceToNextSentencesBurden[sentence]!!)
        }
        var leastBurdenSeen = Long.MAX_VALUE
        val nextSentence = intSetOf()
        val index = ScoreCoordinateIndex()
        val fromCoordinates = index.sentences[sentence]
        for ((ixTo, toCoordinates) in index.sentences.withIndex()) {
            if (sentence == ixTo) {
                continue
            }
            val leastBurdenSeenCoordinates = toCoordinates minus fromCoordinates
            var burden = 0L
            leastBurdenSeenCoordinates.forEachElement { coordinateIndex ->
                val raw = index.coordinates[coordinateIndex].level
                burden += raw * raw
            }
            when {
                burden == 0L -> {} // Ignore burden==0, these are backward transitions
                leastBurdenSeen > burden -> {
                    leastBurdenSeen = burden
                    nextSentence.clear()
                    nextSentence += ixTo
                }
                leastBurdenSeen == burden -> {
                    nextSentence += ixTo
                }
            }
        }
        sentenceToNextSentences[sentence] = nextSentence
        sentenceToNextSentencesBurden[sentence] = leastBurdenSeen.toInt()
        return getNextSentences(sentence)
    }

    companion object {
        private val sentenceToNextSentences = mutableMapOf<Int, IntSet>()
        private val sentenceToNextSentencesBurden = mutableMapOf<Int, Int>()
    }
}