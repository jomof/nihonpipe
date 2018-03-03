package com.jomof.nihonpipe.groveler

import com.jomof.nihonpipe.groveler.schema.Store
import com.jomof.nihonpipe.groveler.schema.TanakaCorpusSentence
import java.io.File

private fun translateTanakaCorpus(file: File, store: Store) {
    val lines = file.readLines()

    for (i in 0 until lines.size step 3) {
        val japaneseLine = lines[i]
        val code = japaneseLine.substring(1, 8)
        val semsem = japaneseLine.indexOf(";;")
        val japanese = japaneseLine.substring(10, semsem)
        val tid = japaneseLine.substring(semsem + 6)
        val english = lines[i + 1]
        store.add(TanakaCorpusSentence(japanese, code, tid,
                english, file.name))
    }
}

fun translateTanakaCorpus(store: Store) {
    if (!jacyDataTanakaDir.isDirectory) {
        throw RuntimeException(jacyDataTanakaDir.toString())
    }
    jacyDataTanakaDir.walkTopDown()
            .toList()
            .take(90)
            .forEach { file ->
                if (file.isFile) {
                    translateTanakaCorpus(file, store)
                }
            }

    // Add an index for each sentence
    store.tanakaCorpusSentence().forEach { (index, sentence) ->
        store.addSentenceIndex(index)
    }
}
