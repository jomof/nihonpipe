package com.jomof.nihonpipe.play

import com.jomof.nihonpipe.datafiles.*
import com.jomof.nihonpipe.schema.KeySentences

enum class LadderKind(
        val levelProvider: LevelProvider,
        val levelsPerMezzo: Int) {
    WANIKANI_VOCAB_LADDER(WanikaniVocabLevels(), 5),
    SENTENCE_SKELETON_LADDER(SentenceSkeletonLevels(), 15),
    GRAMMAR_SUMMARY_LADDER(GrammarSummaryLevels(), 20),
    SENTENCE_FREQUENCY_LADDER(SentenceFrequencyLevels(), 100);

    fun forEachKeySentence(level: Int, action: (KeySentences) -> Unit) {
        for (keySentence in levelProvider.getKeySentences(level)) {
            action(keySentence)
        }
    }

    companion object {
        fun forEachLadderLevel(
                action: (ladderKind: LadderKind, level: Int) -> Unit) {
            for (ladderKind in LadderKind.values()) {
                for (level in 0 until ladderKind.levelProvider.size) {
                    action(ladderKind, level)
                }
            }
        }

        fun forEachSentenceCoordinate(sentence: Int, action: (ScoreCoordinate) -> Unit) {
            forEachLadderLevel { ladderKind, level ->
                if (ladderKind.levelProvider.getLevelSentences(level).contains(sentence)) {
                    ladderKind.forEachKeySentence(level) { (key, sentences) ->
                        if (sentences.contains(sentence)) {
                            action(ScoreCoordinate(ladderKind, level, key))
                        }
                    }
                }
            }
        }
    }
}