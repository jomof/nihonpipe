package com.jomof.nihonpipe.play

import com.jomof.nihonpipe.play.LadderKind.*

data class ScoreCoordinate(
        val ladderKind: LadderKind,
        val level: Int,
        val key: String) {

    fun toMezzoLevel(level: Int): MezzoScore {
        val mezzoOrdinal = level / ladderKind.levelsPerMezzo
        if (mezzoOrdinal > MezzoScore.BURNED.ordinal) {
            return MezzoScore.BURNED
        }
        return MezzoScore.values()[mezzoOrdinal]
    }

    override fun toString(): String {
        val description = when (ladderKind) {
            TOKEN_FREQUENCY_LADDER -> "token"
            WANIKANI_VOCAB_LADDER -> "wanikani"
            SENTENCE_SKELETON_LADDER -> "skeleton"
            JLPT_VOCAB_LADDER -> "jlpt"
            GRAMMAR_SUMMARY_LADDER -> "grammar"
        //SENTENCE_LENGTH_LADDER -> "length"
        }
        return "$description[$level]:$key"
    }
}