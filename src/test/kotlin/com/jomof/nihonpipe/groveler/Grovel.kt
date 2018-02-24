package com.jomof.nihonpipe.groveler

import org.junit.Test
import java.io.File


class Grovel {
    private val projectRootDir = File(".").absoluteFile.canonicalFile!!
    private val externalDir = File(projectRootDir, "external")
    private val jacyDir = File(externalDir, "jacy")
    private val jacyDataDir = File(jacyDir, "data")
    private val jacyDataTanakaDir = File(jacyDataDir, "tanaka")
    private val processedDir = File(projectRootDir, "processed")
    private val indexedDir = File(processedDir, "indexed")


    fun translateTanakaCorpus(file: File) {
        val lines = file.readLines()

        for (i in 0 until lines.size step 3) {
            val japaneseLine = lines[i]
            //println(japaneseLine)
            val code = japaneseLine.substring(1, 8)
            //println(code)
            val semsem = japaneseLine.indexOf(";;")
            //println(semsem)
            val japanese = japaneseLine.substring(10, semsem)
            //println("[$japanese]")
            val tid = japaneseLine.substring(semsem + 6)
            //println(tid)
            val english = lines[i + 1]
            //println(english)
            val key = japanese.replace(" ", "")
            db.withinKey(key, "japanese-sentence")
                    .write(code, "tanaka-code")
                    .write(japanese, "tokenized")
                    .write(tid, "tanaka-tid")
                    .write(english, "english")
                    .write(file.name, "tanaka-file-name")
        }
    }

    val db = Database(indexedDir)

    @Test
    fun readme() {
        db.withinKey("readme.txt", "raw")
                .write("Hello world", "text-file")
        db.save()
    }

    @Test
    fun translateTanakaCorpus() {
        if (!jacyDataTanakaDir.isDirectory) {
            throw RuntimeException(jacyDataTanakaDir.toString())
        }
        jacyDataTanakaDir.walkTopDown()
                .toList()
                .forEach { file ->
                    if (file.isFile) {
                        translateTanakaCorpus(file)
                        db.save()
                        println("saved tanaka corpus index...")
                    }
                }
        db.save()
    }
}