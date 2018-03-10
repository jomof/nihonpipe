package com.jomof.nihonpipe.groveler

import com.jomof.nihonpipe.groveler.bitfield.and
import com.jomof.nihonpipe.groveler.bitfield.toSetBitIndices
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
        val intersect = skeletonBitField and grammarBitField
        println("skeleton = ${skeletonBitField.toSetBitIndices().count()}")
        println("grammarBitField = ${grammarBitField.toSetBitIndices().count()}")
        println("waniKaniBitField = ${waniKaniBitField.toSetBitIndices().count()}")
        println("common = ${intersect.toSetBitIndices().count()}")

        waniKaniLevelInfo.sentencesByLevel
//                .filter { (level, list) ->  (list[0].sentenceIndex and intersect).size > 0 }
//                .take(1)
                .onEach {
                    println("${it.level} = ${it.levelElements[0].sentenceIndex.toSetBitIndices().count()}")
                }
        db.close()

//        db.sentenceIndexToIndex
//                .toSequence()
//                .keepOnlyRowsContaining(intersect)
//                .keepInstances<TanakaCorpusSentence>(db)
//                .onEach { (row, sentence) ->
//                    println("${sentence.japanese} ${sentence.english}")
//                }
//                .count()


    }

}