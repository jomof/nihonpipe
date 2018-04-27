package com.jomof.nihonpipe.datafiles

import com.jomof.algorithm.getsert
import com.jomof.nihonpipe.*
import com.jomof.nihonpipe.schema.TranslatedSentence
import org.h2.mvstore.MVStore
import java.io.File

private val db = MVStore.Builder()
        .fileName(translatedSentencesBin.absolutePath!!)
        .compress()
        .open()!!

private val indexToTranslated = db.openMap<Int, TranslatedSentence>("IndexToTranslated")!!
private val japaneseToIndex = db.openMap<String, Int>("JapaneseToIndex")!!
private val englishToIndexes = db.openMap<String, MutableSet<Int>>("EnglishToIndexes")!!
private val nextIndex = db.openMap<String, Int>("NextIndex")!!
private val seen = mutableSetOf<String>()

private fun addSentence(japanese: String, english: String) {
    val tokenized = tokenizeJapaneseSentence(japanese)
    val normalized = tokenized.normalized()

    if (japaneseToIndex.containsKey(normalized)) {
        return
    }
    val token = tokenized.reading() + "---" + tokenized.pronunciation()
    if (seen.contains(token)) {
        return
    }
    seen += token
    val next = nextIndex.getsert("index") { 0 }
    japaneseToIndex[japanese] = next
    englishToIndexes
            .getsert(english, { mutableSetOf() })
            .add(next)
    indexToTranslated[next] = TranslatedSentence(
            japanese = japanese,
            english = english)
    nextIndex["index"] = next + 1
}

private fun populate() {
    if (indexToTranslated.isEmpty()) {
        core10kSentences()
        translateTanakaCorpus()
        residueSentences()
        otherSentences(gapFillingSentencesFile)
        otherSentencesReversed(personalSentencesFile)
    }
    db.commit()
}

private fun otherSentences(sentences: File) {
    sentences
            .forEachLine { line ->
                when (line[0]) {
                    'A' -> {
                        val leftStripped = line.substring(3)
                        val split = leftStripped.split("\t")
                        addSentence(split[0], split[1])
                    }
                }
            }
}

private fun otherSentencesReversed(sentences: File) {
    sentences
            .forEachLine { line ->
                if (line.length > 3) when (line[0]) {
                    'A' -> {
                        val leftStripped = line.substring(3)
                        val split = leftStripped.split("\t")
                        addSentence(split[1], split[0])
                    }
                }
            }
}

private fun residueSentences() {
    tanakaWWWJDICExamplesResidueFile
            .forEachLine { line ->
                when (line[0]) {
                    'A' -> {
                        val leftStripped = line.substring(3)
                        val idPos = leftStripped.indexOf("#ID=")
                        val rightStripped = leftStripped.substring(0, idPos)
                        val split = rightStripped.split("\t")
                        addSentence(split[0], split[1])
                    }
                }
            }
}

private fun translateTanakaCorpus(file: File) {
    val lines = file.readLines()

    for (i in (0 until lines.size step 3)) {
        val japaneseLine = lines[i]
        val semsem = japaneseLine.indexOf(";;")
        val japanese = japaneseLine.substring(10, semsem)
        val english = lines[i + 1]
        addSentence(japanese, english)
    }
}

private fun translateTanakaCorpus() {
    if (!jacyDataTanakaDir.isDirectory) {
        throw RuntimeException(jacyDataTanakaDir.toString())
    }
    jacyDataTanakaDir.walkTopDown()
            .toList()
            .forEach { file ->
                if (file.isFile) {
                    translateTanakaCorpus(file)
                }
            }
}

private fun core10kSentences() {
    val lines = core2k6k10kFile.readLines()
    for (i in (1 until lines.size)) {
        val line = lines[i]
        val split = line.split("\t")
        val english = split[8]
        val japanese = split[9]
        addSentence(japanese, english)
    }
}

fun sentenceIndexRange(): IntRange {
    populate()
    return 0 until indexToTranslated.size
}

fun sentenceIndexToTranslatedSentence(sentenceIndex: Int): TranslatedSentence {
    populate()
    return indexToTranslated[sentenceIndex]!!
}

fun japaneseToSentenceIndex(japanese: String): Int {
    populate()
    return japaneseToIndex[japanese] ?: throw RuntimeException("'$japanese'")
}

fun englishToSentenceIndexes(english: String): Set<Int> {
    populate()
    return englishToIndexes[english] ?: throw RuntimeException("'$english'")
}
