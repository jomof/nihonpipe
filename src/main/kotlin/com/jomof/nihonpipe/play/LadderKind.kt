package com.jomof.nihonpipe.play

import com.jomof.nihonpipe.datafiles.GrammarSummaryLevels
import com.jomof.nihonpipe.datafiles.LevelProvider
import com.jomof.nihonpipe.datafiles.SentenceSkeletonLevels
import com.jomof.nihonpipe.datafiles.WanikaniVocabLevels

enum class LadderKind(
        val levelProvider: LevelProvider,
        val levelsPerMezzo: Int) {
    WANIKANI_VOCAB_LADDER(WanikaniVocabLevels(), 5),
    SENTENCE_SKELETON_LADDER(SentenceSkeletonLevels(), 15),
    GRAMMAR_SUMMARY_LADDER(GrammarSummaryLevels(), 20);
}