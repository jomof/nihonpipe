package com.jomof.nihonpipe.play

data class ScoreCoordinate(
        val ladderKind: LadderKind,
        val level: Int,
        val key: String)

fun ScoreCoordinate.toMezzoLevel(level: Int): MezzoScore {
    val mezzoOrdinal = level / ladderKind.levelsPerMezzo
    if (mezzoOrdinal > MezzoScore.BURNED.ordinal) {
        return MezzoScore.BURNED
    }
    return MezzoScore.values()[mezzoOrdinal]
}