package com.jomof.nihonpipe.groveler

import java.io.File

fun translateJishoJLPT(file: File, level: Int, map: MutableMap<String, List<String>>) {
    val lines = file.readLines()
    for (n in 1 until lines.size) {
        val fields = lines[n].split("\t")
        if (fields.size != 4) {
            throw RuntimeException(fields.size.toString())
        }
        val vocab = fields[0]
        val kana = fields[1]
        val jlpt = "JLPT$level"
        val english = fields[3]
        map[english] = listOf(vocab, kana, jlpt)
    }
}

fun translatJishoJLPT() {
    val map = mutableMapOf<String, List<String>>()
    translateJishoJLPT(jishoJLPT5, 5, map)
    translateJishoJLPT(jishoJLPT4, 4, map)
    translateJishoJLPT(jishoJLPT3, 3, map)
    translateJishoJLPT(jishoJLPT2, 2, map)
    translateJishoJLPT(jishoJLPT1, 1, map)
    var index = mutableMapOf<String, Int>()
    map.map { (english, values) ->
        listOf(values[0], values[1], values[2], english)
    }
            .sortedByDescending { it[2] }
            .map {
                val vocab = it[0]
                val last = index[vocab] ?: 0
                index[vocab] = last + 1
                listOf(it[0], it[1], it[2], it[3], last.toString())
            }
            .forEach { it ->
                val vocab = it[0]
                val kana = it[1]
                val jlpt = it[2]
                val meaning = it[3]
                val number = it[4]
//                db.withinKey(vocab, "jisho-vocab")
//                        .write("$kana", "kana")
//                        .write("$meaning", "english")
//                        .write("$jlpt", "jlpt")
//                        .write("$kana", "kana-$number")
//                        .write("$meaning", "english-$number")
//                        .write("$jlpt", "jlpt-$number")
            }
}