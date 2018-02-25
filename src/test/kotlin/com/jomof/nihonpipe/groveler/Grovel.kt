package com.jomof.nihonpipe.groveler

import org.junit.Test
import java.io.File


class Grovel {
    private val projectRootDir = File(".").absoluteFile.canonicalFile!!
    private val linuxScriptFile = File(projectRootDir, "make.sh")
    /**/private val externalDir = File(projectRootDir, "external")
    /*--*/private val optimizedKoreFile = File(externalDir, "optimized-kore.tsv")
    /*--*/private val jacyDir = File(externalDir, "jacy")
    /*----*/private val jacyAceDir = File(jacyDir, "ace")
    /*----*/private val jacyAceConfigTdlFile = File(jacyAceDir, "config.tdl")
    /*----*/private val jacyDataDir = File(jacyDir, "data")
    /*------*/private val jacyDataTanakaDir = File(jacyDataDir, "tanaka")
    /**/private val processedDir = File(projectRootDir, "processed")
    /*--*/private val indexedDir = File(processedDir, "indexed")
    /*--*/private val binDir = File(projectRootDir, "bin")
    /*----*/private val aceBinDir = File(binDir, "ace-0.9.26")
    /*----*/private val aceExecutableFile = File(aceBinDir, "ace")
    /**/private val grammarsDir = File(projectRootDir, "grammars")
    /*--*/private val grammarsJacyDir = File(grammarsDir, "jacy")
    /*--*/private val grammarsJacyDatFile = File(grammarsJacyDir, "jacy.dat")

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
                .take(80)
                .forEach { file ->
                    if (file.isFile) {
                        translateTanakaCorpus(file)
                        if (db.save()) {
                            println("saved ${file.name}...")
                        }
                    }
                    db.save()
                }
    }

    @Test
    fun generateIncrementalAceScript() {
        translateTanakaCorpus()
        val sb = StringBuilder()
        sb.appendln("$aceExecutableFile -g $jacyAceConfigTdlFile -G $grammarsJacyDatFile")
        var n = 1
        db.forEach("japanese-sentence") { node ->
            if (!node.hasValueType("tokenized")) {
                throw RuntimeException(node.keyTypeFolder.toString())
            }
            if (!node.hasValueType("jacy-parsed")) {
                val tokenized = node.getValueTypeFile("tokenized")
                val jacyParsed = node.getValueTypeFile("jacy-parsed")
                val jacyParsedStderr = node.getValueTypeFile("jacy-parsed-stderr")
                sb.appendln(
                        "cat $tokenized | $aceExecutableFile -1 -g $grammarsJacyDatFile " +
                                "> $jacyParsed " +
                                "2> $jacyParsedStderr")
                if (n % 50 == 0) {
                    sb.appendln("echo ${node.ordinal} of ${node.getIndexSize()} " +
                            "sentences processed")
                }
                ++n
            }
        }
        linuxScriptFile.writeText(sb.toString())
    }

    //@Test
    fun translateOptimizedKore() {
        val lines = optimizedKoreFile.readLines()
        val fieldNames = lines[1].split("\t")
        val count = mutableMapOf<String, Int>()
        for (n in 2 until lines.size) {
            val fields = lines[n].split("\t")
            if (fields.size != 20) {
                throw RuntimeException(fields.size.toString())
            }
            val coreIndex = fields[0].toInt()
            val vocabKoIndex = fields[1].toInt()
            val sentKoIndex = fields[2].toInt()
            val newOptVocIndex = fields[3].toInt()
            val optVocIndex = fields[4].toInt()
            val optSenIndex = fields[5].toInt()
            val jlpt = fields[6]
            val vocab = fields[7]
            val kana = fields[8]
            val english = fields[9]
            val pos = fields[11]
            val index = count[vocab] ?: 0
            count[vocab] = index + 1
            db.withinKey(vocab, "vocab")
                    .write("$coreIndex", "core-index-$index")
                    .write("$vocabKoIndex", "vocab-ko-index-$index")
                    .write("$sentKoIndex", "sent-ko-$index")
                    .write("$newOptVocIndex", "new-opt-voc-index-$index")
                    .write("$optVocIndex", "opt-voc-index-$index")
                    .write("$optSenIndex", "opt-sen-index-$index")
                    .write("$jlpt", "jlpt-$index")
                    .write("$kana", "kana-$index")
                    .write("$english", "english-$index")
                    .write("$pos", "pos-$index")
                    .overwrite("${index + 1}", "meaning-count")
        }
        db.save()
    }
}