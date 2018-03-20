package com.jomof.nihonpipe.play

import com.jomof.intset.IntSet
import com.jomof.nihonpipe.datafiles.*

enum class LadderKind(
        val levelProvider: LevelProvider,
        val levelsPerMezzo: Int) {
    WANIKANI_VOCAB_LADDER(WanikaniVocabLevels(), 5),
    SENTENCE_SKELETON_LADDER(SentenceSkeletonLevels(), 15),
    GRAMMAR_SUMMARY_LADDER(GrammarSummaryLevels(), 20),
    SENTENCE_FREQUENCY_LADDER(SentenceFrequencyLevels(), 100);

    companion object {

        fun forEachLadderLevel(
                action: (ladderKind: LadderKind, level: Int) -> Unit) {
            for (ladderKind in LadderKind.values()) {
                for (level in 0 until ladderKind.levelProvider.size) {
                    action(ladderKind, level)
                }
            }
        }

        internal fun forEachLadderCoordinate(
                action: (coordinate: ScoreCoordinate, sentences: IntSet) -> Unit) {
            for (ladderKind in LadderKind.values()) {
                for (level in 0 until ladderKind.levelProvider.size) {
                    for ((key, sentences) in ladderKind.levelProvider.getKeySentences(level)) {
                        val coordinate = ScoreCoordinate(ladderKind, level, key)
                        action(coordinate, sentences)
                    }
                }
            }
        }

    }
}