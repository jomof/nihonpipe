package com.jomof.nihonpipe.play

import com.jomof.algorithm.getsert
import com.jomof.intset.IntSet
import com.jomof.intset.forEachElement
import com.jomof.intset.intSetOf
import com.jomof.intset.union
import com.jomof.nihonpipe.achievementsDir
import com.jomof.nihonpipe.datafiles.WanikaniVsJlptVocabs
import com.jomof.nihonpipe.datafiles.baseformTokenToSentences
import com.jomof.nihonpipe.datafiles.surfaceTokenToSentences
import com.jomof.nihonpipe.schema.Jlpt
import java.io.File


private val rangeOfAchievements = 2_000_000 until 3_000_000

private var nextAchievementElement = rangeOfAchievements.first

// key = sentence, value achievement element index
private val sentenceToAchievementMask = mutableMapOf<Int, IntSet>()

// key = achievement vocab, value = achievement index
private val achievementVocabs = mutableMapOf<String, Int>()

// key = achievement name, value = achievement indexes
private val achievementToIndexes = mutableMapOf<String, IntSet>()

// key = achievement index, value = vocab
private val achievementToVocab = mutableMapOf<Int, String>()

// key = achievement index, value = achievement names
private val achievementToFlavor = mutableMapOf<Int, MutableSet<String>>()

private fun addVocab(
        flavor: String,
        vocab: String,
        disregardMissing: Boolean = false) {
    val achievementElement =
            achievementVocabs.getsert(vocab)
            { nextAchievementElement++ }
    val baseformSentences = baseformTokenToSentences(vocab)
    val surfaceSentences = surfaceTokenToSentences(vocab)
    val allSentences = baseformSentences union surfaceSentences
    if (!disregardMissing) {
        assert(!allSentences.isEmpty()) {
            "achievement vocab $vocab not satisfied"
        }
    }
    achievementToVocab[achievementElement] = vocab
    allSentences.forEachElement { sentence ->
        sentenceToAchievementMask
                .getsert(sentence) { intSetOf() }
                .add(achievementElement)
        achievementToIndexes
                .getsert(flavor) { intSetOf() }
                .add(achievementElement)
        achievementToFlavor
                .getsert(achievementElement) { mutableSetOf() }
                .add(flavor)
    }
}

private fun parseAchievementsFile(file: File) {
    var flavor: String? = null
    file
            .forEachLine { line ->
                val code = line[0]
                val remainder = line.substring(3)
                val splits = remainder.split("\t")
                when (code) {
                    'A' -> {
                        assert(flavor == null)
                        assert(splits.size == 1)
                        flavor = splits[0]
                    }
                    'B' -> {
                        assert(splits.size == 2)
                        addVocab(flavor!!, splits[0])
                    }
                }
            }

}

private fun populate() {
    if (sentenceToAchievementMask.isEmpty()) {
        achievementsDir.walkTopDown()
                .toList()
                .forEach { file ->
                    if (file.isFile) {
                        parseAchievementsFile(file)
                    }
                }

        for ((word, vocab) in WanikaniVsJlptVocabs.vocabOf.vocabs) {
            if (vocab.wanikaniLevel != 100) {
                addVocab(
                        "Wanikani level ${vocab.wanikaniLevel}",
                        word,
                        disregardMissing = true
                )
            }
            if (vocab.jlptLevel != Jlpt.JLPT0 && vocab.jlptLevel != Jlpt.JLPT6) {
                addVocab(
                        "${vocab.jlptLevel}",
                        word,
                        disregardMissing = true
                )
            }
        }
    }
}

/**
 * Given a sentence index return the index mask of achievements
 */
fun achievementIndexesOfSentence(sentence: Int): IntSet {
    populate()
    return sentenceToAchievementMask[sentence] ?: intSetOf()
}

fun achievementNames(): List<String> {
    return achievementToIndexes.keys.toList()
}

fun achievementIndexes(achievement: String): IntSet {
    return achievementToIndexes[achievement]!!
}

fun achievementIndexToVocab(achievementIndex: Int): String {
    return achievementToVocab[achievementIndex]!!
}

fun achievementIndexToFlavor(achievementIndex: Int): Set<String> {
    assert(achievementIndex in rangeOfAchievements)
    return achievementToFlavor[achievementIndex]!!
}
