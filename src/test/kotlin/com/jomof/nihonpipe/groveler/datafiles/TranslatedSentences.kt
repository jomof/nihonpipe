package com.jomof.nihonpipe.groveler.datafiles

import com.jomof.nihonpipe.groveler.gapFillingSentencesFile
import com.jomof.nihonpipe.groveler.jacyDataTanakaDir
import com.jomof.nihonpipe.groveler.schema.TranslatedSentence
import com.jomof.nihonpipe.groveler.tanakaWWWJDICExamplesResidueFile
import com.jomof.nihonpipe.groveler.translatedSentencesBin
import org.h2.mvstore.MVStore
import java.io.File

class TranslatedSentences private constructor(
        file: String = translatedSentencesBin.absolutePath!!) {

    private val db = MVStore.Builder()
            .fileName(file)
            .compress()
            .open()!!

    private val translatedSentences = db.openMap<Int, TranslatedSentence>(
            "TranslatedSentences")!!

    init {
        if (translatedSentences.isEmpty()) {
            var offset = translateTanakaCorpus()

            // Some additional tanaka sentences
            tanakaWWWJDICExamplesResidueFile
                    .forEachLine { line ->
                        when (line[0]) {
                            'A' -> {
                                val leftStripped = line.substring(3)
                                val idPos = leftStripped.indexOf("#ID=")
                                val rightStripped = leftStripped.substring(0, idPos)
                                val split = rightStripped.split("\t")
                                translatedSentences[offset++] = TranslatedSentence(
                                        japanese = split[0],
                                        english = split[1]
                                )
                            }
                        }
                    }
            gapFillingSentencesFile
                    .forEachLine { line ->
                        when (line[0]) {
                            'A' -> {
                                val leftStripped = line.substring(3)
                                val split = leftStripped.split("\t")
                                translatedSentences[offset++] = TranslatedSentence(
                                        japanese = split[0],
                                        english = split[1]
                                )
                            }
                        }
                    }
            db.compactRewriteFully()
        }
    }

    val sentences: Map<Int, TranslatedSentence> = translatedSentences

    private fun translateTanakaCorpus(offset: Int, file: File): Int {
        var localOffset = offset
        val lines = file.readLines()

        for (i in (0 until lines.size step 3)) {
            val japaneseLine = lines[i]
            val semsem = japaneseLine.indexOf(";;")
            val japanese = japaneseLine.substring(10, semsem)
            val english = lines[i + 1]
            translatedSentences[localOffset++] = TranslatedSentence(
                    japanese,
                    english)
        }
        return localOffset
    }

    private fun translateTanakaCorpus(): Int {
        if (!jacyDataTanakaDir.isDirectory) {
            throw RuntimeException(jacyDataTanakaDir.toString())
        }
        var offset = 0
        jacyDataTanakaDir.walkTopDown()
                .toList()
                //.take(90)
                .forEach { file ->
                    if (file.isFile) {
                        offset = translateTanakaCorpus(offset, file)
                    }
                }
        return offset
    }

    companion object {
        private var instance: TranslatedSentences? = null
        val tanaka: TranslatedSentences
            get() {
                if (instance != null) {
                    return instance!!
                }
                instance = TranslatedSentences()
                return instance!!
            }

        fun save() {
            if (instance != null) {
                instance!!.db.close()
                instance = null
            }
        }
    }

}