package com.jomof.nihonpipe.groveler.datafiles

import com.jomof.nihonpipe.groveler.jacyDataTanakaDir
import com.jomof.nihonpipe.groveler.schema.TanakaCorpusSentence
import com.jomof.nihonpipe.groveler.tanakaCorpusSentencesBin
import org.h2.mvstore.MVStore
import java.io.File

class TanakaCorpusSentences private constructor(
        file: String = tanakaCorpusSentencesBin.absolutePath!!) {

    private val db = MVStore.Builder()
            .fileName(file)
            .compress()
            .open()!!

    private val tanakaCorpus = db.openMap<Int, TanakaCorpusSentence>(
            "TanakaCorpusSentences")!!

    init {
        if (tanakaCorpus.isEmpty()) {
            translateTanakaCorpus()
            save()
        }
    }

    val sentences: Map<Int, TanakaCorpusSentence> = tanakaCorpus

    private fun translateTanakaCorpus(offset: Int, file: File): Int {
        var offset = offset
        val lines = file.readLines()

        for (i in (0 until lines.size step 3)) {
            val japaneseLine = lines[i]
            val code = japaneseLine.substring(1, 8)
            val semsem = japaneseLine.indexOf(";;")
            val japanese = japaneseLine.substring(10, semsem)
            val tid = japaneseLine.substring(semsem + 6)
            val english = lines[i + 1]
            tanakaCorpus[offset++] = TanakaCorpusSentence(
                    japanese,
                    code,
                    tid,
                    english,
                    file.name)
        }
        return offset
    }

    private fun translateTanakaCorpus() {
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
    }

    companion object {
        private var instance: TanakaCorpusSentences? = null
        val tanaka: TanakaCorpusSentences
            get() {
                if (instance != null) {
                    return instance!!
                }
                instance = TanakaCorpusSentences()
                return tanaka
            }

        fun save() {
            if (instance != null) {
                instance!!.db.close()
                instance = null
            }
        }
    }

}