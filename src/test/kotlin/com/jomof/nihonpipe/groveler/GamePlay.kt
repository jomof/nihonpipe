package com.jomof.nihonpipe.groveler

import com.jomof.nihonpipe.groveler.schema.LevelType
import com.jomof.nihonpipe.groveler.schema.Store
import org.junit.Test
import java.io.Serializable

class GamePlay {

    data class SrsInfo(
            var right: Int,
            var wrong: Int,
            var level: Int,
            var nextPractice: Long) : Serializable

    data class Player(
            var sentenceLevels: MutableMap<Int, SrsInfo>
    ) : Serializable

    @Test
    fun simulate() {
        val db = Store(dataDatabaseBin)
        val skeletonLevelInfo = db.levels[LevelType.SENTENCE_SKELETON]!!
        val grammarLevelInfo = db.levels[LevelType.GRAMMAR_ELEMENT]!!
        val waniKaniLevelInfo = db.levels[LevelType.WANIKANI_LEVEL]!!
        val skeletonBitField = skeletonLevelInfo.sentencesByLevel[0].levelElements[0].sentenceIndex
        val grammarBitField = grammarLevelInfo.sentencesByLevel[0].levelElements[0].sentenceIndex
        val waniKaniBitField = waniKaniLevelInfo.sentencesByLevel[0].levelElements[0].sentenceIndex
        val intersect = skeletonBitField intersect grammarBitField
        println("skeleton = ${skeletonBitField.size}")
        println("grammarBitField = ${grammarBitField.size}")
        println("waniKaniBitField = ${waniKaniBitField.size}")
        println("common = ${intersect.size}")

        waniKaniLevelInfo.sentencesByLevel
//                .filter { (level, list) ->  (list[0].sentenceIndex and intersect).size > 0 }
//                .take(1)
                .onEach {
                    println("${it.level} = ${it.levelElements[0].sentenceIndex.size}")
                }
        db.close()
    }
}