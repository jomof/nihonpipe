package com.jomof.nihonpipe.play

import com.jomof.nihonpipe.datafiles.GrammarSummaryLadder
import com.jomof.nihonpipe.datafiles.LevelProvider
import com.jomof.nihonpipe.datafiles.SentenceSkeletonLadder
import com.jomof.nihonpipe.datafiles.TokenFrequencyLadder
import com.jomof.nihonpipe.schema.KeySentences

enum class LadderKind(
        val levelProvider: LevelProvider,
        val levelsPerMezzo: Int) {
    TOKEN_FREQUENCY_LADDER(TokenFrequencyLadder(), 5),
    SENTENCE_SKELETON_LADDER(SentenceSkeletonLadder(), 1000),
    GRAMMAR_SUMMARY_LADDER(GrammarSummaryLadder(), 1000);

    fun forEachKeySentence(level: Int, action: (KeySentences) -> Unit) {
        for (keySentence in levelProvider.getKeySentences(level)) {
            action(keySentence)
        }
    }
}