package com.jomof.nihonpipe.play

import com.jomof.nihonpipe.datafiles.*
import com.jomof.nihonpipe.schema.KeySentences

enum class LadderKind(
        val levelProvider: LevelProvider,
        val levelsPerMezzo: Int) {
    TOKEN_SURFACE_FREQUENCY_LADDER(TokenSurfaceFrequencyLadder(), 5),
    TOKEN_BASEFORM_FREQUENCY_LADDER(TokenBaseFormFrequencyLadder(), 5),
    SENTENCE_SKELETON_LADDER(SentenceSkeletonLadder(), 1000),
    GRAMMAR_SUMMARY_LADDER(GrammarSummaryLadder(), 1000);

    fun forEachKeySentence(level: Int, action: (KeySentences) -> Unit) {
        for (keySentence in levelProvider.getKeySentences(level)) {
            action(keySentence)
        }
    }
}