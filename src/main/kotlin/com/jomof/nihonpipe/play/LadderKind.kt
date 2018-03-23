package com.jomof.nihonpipe.play

import com.jomof.intset.IntSet
import com.jomof.nihonpipe.datafiles.*
import com.jomof.nihonpipe.schema.KeySentences

enum class LadderKind(
        val levelProvider: LevelProvider,
        val levelsPerMezzo: Int) {
    WANIKANI_VOCAB_LADDER(WanikaniVocabLadder(), 5),
    SENTENCE_SKELETON_LADDER(SentenceSkeletonLadder(), 15),
    GRAMMAR_SUMMARY_LADDER(GrammarSummaryLadder(), 20),
    JLPT_VOCAB_LADDER(JlptVocabLadder(), 5),
    SENTENCE_LENGTH_LADDER(SentenceLengthLadder(), 20),
    //SENTENCE_FREQUENCY_LADDER(SentenceFrequencyLadder(), 100)
    ;

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

        fun forEachPossibleCoordinate(action: (ScoreCoordinate, IntSet) -> Unit) {
            forEachLadderLevel { ladderKind, level ->
                ladderKind.forEachKeySentence(level) { (key, sentences) ->
                    action(ScoreCoordinate(ladderKind, level, key), sentences)
                }
            }
        }
    }
}