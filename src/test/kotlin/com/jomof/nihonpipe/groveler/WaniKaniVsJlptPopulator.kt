package com.jomof.nihonpipe.groveler

import com.jomof.nihonpipe.groveler.schema.Jlpt
import com.jomof.nihonpipe.groveler.schema.Jlpt.*
import com.jomof.nihonpipe.groveler.schema.Store
import com.jomof.nihonpipe.groveler.schema.WaniKaniVsJlptVocab
import java.io.File


fun translateWaniKaniVsJlpt(file: File, jlpt: Jlpt, map: MutableMap<String, WaniKaniVsJlptVocab>) {
    val lines = file.readLines()
    for (n in 1 until lines.size) {
        val fields = lines[n].split("\t")
        if (fields.size != 7) {
            throw RuntimeException(fields.size.toString())
        }
        val wanikani = fields[0].toInt()
        val vocab = fields[1]
        val kana = fields[2]
        val sense1 = fields[3]
        val sense2 = fields[4]
        val sense3 = fields[5]
        map[vocab] = WaniKaniVsJlptVocab(
                vocab = vocab,
                kana = kana,
                wanikaniLevel = wanikani,
                jlptLevel = jlpt,
                sense1 = sense1,
                sense2 = sense2,
                sense3 = sense3)
    }
}

fun translateWaniKaniVsJLPT(store: Store) {
    val map = mutableMapOf<String, WaniKaniVsJlptVocab>()
    translateWaniKaniVsJlpt(wanikanivsjlptJLPT5, JLPT5, map)
    translateWaniKaniVsJlpt(wanikanivsjlptJLPT4, JLPT4, map)
    translateWaniKaniVsJlpt(wanikanivsjlptJLPT3, JLPT3, map)
    translateWaniKaniVsJlpt(wanikanivsjlptJLPT2, JLPT2, map)
    translateWaniKaniVsJlpt(wanikanivsjlptJLPT1, JLPT1, map)

    map.values.forEach { vocab ->
        store.add(vocab)
    }
}