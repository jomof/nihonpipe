package com.jomof.nihonpipe.play

import com.jomof.intset.IntSet
import com.jomof.intset.intSetOf
import com.jomof.nihonpipe.datafiles.sentenceIndexRange
import com.jomof.nihonpipe.sentenceTransitionsBin
import org.h2.mvstore.MVStore

class LeastBurdenSentenceTransitions {

    fun getNextSentences(sentence : Int) : Pair<IntSet, Int> {
        val result = table[sentence]
        if (result != null) {
            return result
        }
        var leastBurdenSeen = Int.MAX_VALUE
        val nextSentence = mutableListOf<Int>()
        val fromCoordinates = ladderCoordinateIndexesOfSentence(sentence)
        for (ixTo in sentenceIndexRange()) {
            if (sentence == ixTo) {
                continue
            }
            val toCoordinates = ladderCoordinateIndexesOfSentence(ixTo)
            var burden = 0
            toCoordinates.doWhile { coordinate ->
                if (!fromCoordinates.contains(coordinate)) {
                    ++burden
                }
                burden <= leastBurdenSeen
            }
            when {
                burden == 0 -> {
                } // Ignore burden==0, these are backward transitions
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
        val bits = intSetOf()
        bits += nextSentence
        table[sentence] = Pair(bits.readonly(), leastBurdenSeen)
        return getNextSentences(sentence)
    }

    companion object {
        private val db = MVStore.Builder()
                .fileName(sentenceTransitionsBin.absolutePath)
                .compress()
                .open()!!
        private val table = db.openMap<Int, Pair<IntSet, Int>>("SentenceTransitions")
    }
}