package com.jomof.nihonpipe.datafiles

import com.jomof.algorithm.getsert
import com.jomof.nihonpipe.gapFillingSentencesFile
import com.jomof.nihonpipe.jacyDataTanakaDir
import com.jomof.nihonpipe.schema.TranslatedSentence
import com.jomof.nihonpipe.tanakaWWWJDICExamplesResidueFile
import com.jomof.nihonpipe.translatedSentencesBin
import org.h2.mvstore.MVStore
import java.io.File

class TranslatedSentences {
    val sentences: Map<Int, TranslatedSentence>
        get() = TranslatedSentences.sentences

    fun sentenceToIndex(japanese: String): Int {
        return TranslatedSentences.japaneseToIndex[japanese] ?: throw RuntimeException("'$japanese'")
    }

    companion object {
        private val db = MVStore.Builder()
                .fileName(translatedSentencesBin.absolutePath!!)
                .compress()
                .open()!!

        private val indexToTranslated = db.openMap<Int, TranslatedSentence>(
                "IndexToTranslated")!!

        private val japaneseToIndex = db.openMap<String, Int>(
                "JapaneseToIndex")!!

        private val nextIndex = db.openMap<String, Int>(
                "NextIndex")!!

        private val seen = mutableMapOf<String, TranslatedSentence>()

        private fun addSentence(japanese: String, english: String) {
            val tokenized = KuromojiIpadicCache.tokenize(japanese)
            val normalized = tokenized.normalized()

            if (japaneseToIndex.containsKey(normalized)) {
                return
            }
            val token = tokenized.reading() + "---" + tokenized.pronunciation()
            val lookup = seen[token]
            if (lookup != null) {
                return
            }
            seen[token] = TranslatedSentence(japanese, english)
            val next = nextIndex.getsert("index") { 0 }
            japaneseToIndex[japanese] = next
            indexToTranslated[next] = TranslatedSentence(japanese, english)
            nextIndex["index"] = next + 1
        }

        private val sentences: Map<Int, TranslatedSentence> = indexToTranslated

        init {
            if (indexToTranslated.isEmpty()) {
                translateTanakaCorpus()
                residueSentences()
                gapFillingSentences()
            }
            db.commit()
        }

        private fun gapFillingSentences() {
            gapFillingSentencesFile
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
    }
}