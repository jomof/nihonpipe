package com.jomof.nihonpipe.play

import com.jomof.intset.IntSet

data class ScoreCoordinate(
        val ladderKind: LadderKind,
        val level: Int,
        val key: String)

fun ScoreCoordinate.toSentences(): IntSet {
    return this
            .ladderKind
            .levelProvider
            .getKeySentences(level)
            .single { key == it.key }
            .sentences
}

fun ScoreCoordinate.toMezzoLevel(level: Int): MezzoScore {
    val mezzoOrdinal = level / ladderKind.levelsPerMezzo
    if (mezzoOrdinal > MezzoScore.BURNED.ordinal) {
        return MezzoScore.BURNED
    }
    return MezzoScore.values()[mezzoOrdinal]
}