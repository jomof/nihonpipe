package com.jomof.nihonpipe.play

import com.jomof.intset.IntSet
import com.jomof.nihonpipe.datafiles.KuromojiIpadicCache
import com.jomof.nihonpipe.datafiles.TranslatedSentences

data class SentenceRank(
        val rank: Int,
        val sourceSentence: Int?,
        val sentenceIndex: Int,
        val marginalScoreCoordinates: IntSet,
        val marginalBurden: Int) {

    fun toDisplayString(): String {
        fun labelOf(name: String, elements: List<String>): String {
            return if (elements.isEmpty()) {
                ""
            } else {
                "$name:" + elements.joinToString("\\")
            }
        }

        val scoreCoordinateIndex = ScoreCoordinateIndex()
        val sentence = TranslatedSentences().sentences[sentenceIndex]!!
        val coordinates = marginalScoreCoordinates.map { index ->
            scoreCoordinateIndex.getCoordinateFromCoordinateIndex(index)
        }
        val reading = KuromojiIpadicCache.tokenize.reading(sentence.japanese)
        val pronunciation = KuromojiIpadicCache.tokenize.pronunciation(sentence.japanese)

        val token = labelOf("vocab", coordinates
                .filter { it.ladderKind == LadderKind.TOKEN_FREQUENCY_LADDER }
                .map { it.key })
        val skeleton = labelOf("pattern", coordinates
                .filter { it.ladderKind == LadderKind.SENTENCE_SKELETON_LADDER }
                .map { it.key })
        val grammar = labelOf("grammar", coordinates
                .filter { it.ladderKind == LadderKind.GRAMMAR_SUMMARY_LADDER }
                .map { it.key })

        val sourceSentence = sourceSentence ?: "--"
        return "#$rank\t$sentenceIndex\t$sourceSentence\t" +
                "${sentence.japanese}\t${sentence.english}\t$reading\t" +
                "$pronunciation\t$marginalBurden\t$grammar\t$skeleton\t$token"
    }
}