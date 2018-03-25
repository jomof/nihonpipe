package com.jomof.nihonpipe.play

import com.jomof.intset.IntSet
import com.jomof.nihonpipe.datafiles.KuromojiIpadicCache
import com.jomof.nihonpipe.datafiles.TranslatedSentences

data class SentenceRank(
        val rank: Int,
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
        var coordinates = marginalScoreCoordinates.map { index ->
            scoreCoordinateIndex.coordinates[index]
        }
        val reading = KuromojiIpadicCache.tokenize.reading(sentence.japanese)
        val pronunciation = KuromojiIpadicCache.tokenize.pronunciation(sentence.japanese)

        val token = labelOf("frequency", coordinates
                .filter { it.ladderKind == LadderKind.TOKEN_FREQUENCY_LADDER }
                .map { it.key })
        val wanikani = labelOf("wanikani", coordinates
                .filter { it.ladderKind == LadderKind.WANIKANI_VOCAB_LADDER }
                .map { it.key })
        val jlpt = labelOf("jlpt", coordinates
                .filter { it.ladderKind == LadderKind.JLPT_VOCAB_LADDER }
                .map { it.key })
        val skeleton = labelOf("pattern", coordinates
                .filter { it.ladderKind == LadderKind.SENTENCE_SKELETON_LADDER }
                .map { it.key })
        val grammar = labelOf("grammar", coordinates
                .filter { it.ladderKind == LadderKind.GRAMMAR_SUMMARY_LADDER }
                .map { it.key })
        if (token.isEmpty() && grammar.isEmpty() && skeleton.isEmpty() && jlpt.isEmpty() && wanikani.isEmpty()) {
            println("huh")
        }
        return "#$rank\t${sentence.japanese}\t${sentence.english}\t$reading\t" +
                "$pronunciation\t$marginalBurden\t$grammar\t$skeleton\t$jlpt\t$wanikani\t$token"
    }
}