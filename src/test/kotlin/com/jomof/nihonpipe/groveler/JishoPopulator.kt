package com.jomof.nihonpipe.groveler

import com.jomof.nihonpipe.groveler.schema.JishoVocab
import com.jomof.nihonpipe.groveler.schema.Store
import com.jomof.nihonpipe.groveler.schema.jlptToInt
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

fun translatJishoJLPT(store: Store) {
    val map = mutableMapOf<String, List<String>>()
    translateJishoJLPT(jishoJLPT5, 5, map)
    translateJishoJLPT(jishoJLPT4, 4, map)
    translateJishoJLPT(jishoJLPT3, 3, map)
    translateJishoJLPT(jishoJLPT2, 2, map)
    translateJishoJLPT(jishoJLPT1, 1, map)

    map.map { (english, values) ->
        listOf(values[0], values[1], values[2], english)
    }
            .sortedByDescending { it[2] }
            .map {
                JishoVocab(it[0], it[1], jlptToInt(it[2]), it[3])
            }
            .forEach { it ->
                store.add(it)
            }
}