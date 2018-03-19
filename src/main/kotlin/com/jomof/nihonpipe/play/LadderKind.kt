package com.jomof.nihonpipe.play

import com.jomof.nihonpipe.datafiles.*

enum class LadderKind(
        val levelProvider: LevelProvider,
        val levelsPerMezzo: Int,
        val valueOfNewSentence: Int) {
    WANIKANI_VOCAB_LADDER(WanikaniVocabLevels(), 5, 10),
    SENTENCE_SKELETON_LADDER(SentenceSkeletonLevels(), 15, 100),
    GRAMMAR_SUMMARY_LADDER(GrammarSummaryLevels(), 20, 8),
    SENTENCE_FREQUENCY_LADDER(SentenceFrequencyLevels(), 100, 4);
}