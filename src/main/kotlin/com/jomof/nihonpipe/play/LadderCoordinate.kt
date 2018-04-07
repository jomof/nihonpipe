package com.jomof.nihonpipe.play

import com.jomof.nihonpipe.play.LadderKind.*
import java.io.Serializable

data class LadderCoordinate(
        val ladderKind: LadderKind,
        val level: Int,
        val key: String) : Serializable {

    override fun toString(): String {
        val description = when (ladderKind) {
            TOKEN_SURFACE_FREQUENCY_LADDER -> "token_surface"
            TOKEN_BASEFORM_FREQUENCY_LADDER -> "token_baseform"
            SENTENCE_SKELETON_LADDER -> "skeleton"
            GRAMMAR_SUMMARY_LADDER -> "grammar"
        }
        return "$description[$level]:$key"
    }

    companion object {
        private const val serialVersionUID = 6790756115289251686L
    }
}