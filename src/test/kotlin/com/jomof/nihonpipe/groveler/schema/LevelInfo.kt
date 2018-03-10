package com.jomof.nihonpipe.groveler.schema

import com.jomof.nihonpipe.groveler.bitfield.BitField
import java.io.Serializable

/**
 * A kind of level. This is a key into the levels table.
 */
enum class LevelType {
    /**
     * Sentence skeleton level reduces sentences to a skeleton with just particles
     * and verbs, then sorts them from most frequent to least.
     */
    SENTENCE_SKELETON,

    /**
     * Grammar element level reduces sentences to a set of grammar pieces (like suru
     * verb), then sorts the from most frequent to least.
     */
    GRAMMAR_ELEMENT,


    WANIKANI_LEVEL
}

/**
 * Describes an achievement element of a level. The key describes the element, for example
 * a sentence skeleton, and the sentenceIndex is the set of sentences that match the key.
 */
data class LevelElement(
        val level: Int,
        val key: String,
        val sentenceIndex: BitField) : Serializable

/**
 * A single level containing several level elements (for example, sentence skeletons
 * to achieve).
 */
data class Level(
        val level: Int,
        val levelElements: List<LevelElement>) : Serializable

/**
 * Information about a particular type of level
 */
data class LevelInfo(
        val sentencesByLevel: List<Level>) : Serializable