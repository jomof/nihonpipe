package com.jomof.nihonpipe.groveler

import org.junit.Test
import java.io.File


class Grovel {

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
    fun deleteRaw() {
        File(indexedDir, "raw").deleteRecursively()
    }

    //@Test
    fun translateTanakaCorpus() {
        if (!jacyDataTanakaDir.isDirectory) {
            throw RuntimeException(jacyDataTanakaDir.toString())
        }
        jacyDataTanakaDir.walkTopDown()
                .toList()
                .take(90)
                .take(2)
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
    fun annotateSentencesWithMaxVocabIndex() {
        // For each sentence, find the word with the highest index value and record it.
        val vocabMap = mutableMapOf<String, String>()
        var maxOrder = ""
        db.forEach("vocab") { node ->
            fun format(key: String): String {
                val file = node.getValueTypeFile(key)
                if (!file.exists()) {
                    return "9999"
                }
                val value = file.readText()
                return when (value) {
                    "JLPT0" -> "0006"
                    "JLPT1" -> "0005"
                    "JLPT2" -> "0004"
                    "JLPT3" -> "0003"
                    "JLPT4" -> "0002"
                    "JLPT5" -> "0001"
                    else -> ("0000" + value.toInt().toString()).substring(value.length)
                }
            }

            val word = node.getValueTypeFile("key").readText()
            val r0 = format("wani-kani-level")
            val r1 = format("core-index-0")
            val r2 = format("new-opt-voc-index-0")
            val r3 = format("opt-sen-index-0")
            val r4 = format("opt-voc-index-0")
            val r5 = format("sent-ko-0")
            val r6 = format("vocab-ko-index-0")
            val r7 = format("jlpt-0")
            val order = "$r0$r1$r2$r3$r4$r5$r6$r7"
            if (order > maxOrder) {
                maxOrder = order
            }
            vocabMap[word] = order
        }

        db.forEach("japanese-sentence") { node ->
            val tokenized = node.getValueTypeFile("tokenized").readText()
            val stripped = tokenized
                    .replace("。", " ")
                    .replace("  ", " ")
            var max: String? = null
            for (word in stripped.split(" ")) {
                val lookup = vocabMap[word]
                if (lookup != null) {
                    if (max == null) {
                        max = lookup
                    } else {
                        if (lookup > max) {
                            max = lookup
                        }
                    }
                }
            }
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

    @Test
    fun addSomeMissingVocab() {
        db.withinKey("最善", "vocab")
                .write("JLPT1", "jlpt-0")
                .write("さいぜん", "kana-0")
        db.save()
    }

    //@Test

    //@Test
    fun translateWaniKaniVocab() {
        val lines = wanikaniVocabFile.readLines()
        for (n in 0 until lines.size) {
            val fields = lines[n].split("\t")
            if (fields.size != 4) {
                throw RuntimeException(fields.size.toString())
            }
            val kana = fields[0].substring(1, fields[0].length - 1)
            val vocab = fields[1]
            val english = fields[2].substring(1, fields[2].length - 1)
            val level = fields[3]
            db.withinKey(vocab, "vocab")
                    .write("$kana", "wani-kani-kana")
                    .write("$kana", "kana-0")
                    .write("$english", "wani-kani-english")
                    .write("$english", "english-0")
                    .write("$level", "wani-kani-level")
        }
        db.save()
    }
}