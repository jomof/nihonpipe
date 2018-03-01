package com.jomof.nihonpipe.groveler

import com.jomof.nihonpipe.groveler.schema.Store
import com.jomof.nihonpipe.groveler.schema.WaniKaniVocab

fun translateWaniKaniVocab(store: Store) {
    val lines = wanikaniVocabFile.readLines()
    for (n in 0 until lines.size) {
        val fields = lines[n].split("\t")
        if (fields.size != 4) {
            throw RuntimeException(fields.size.toString())
        }
        val kana = fields[0].substring(1, fields[0].length - 1)
        val vocab = fields[1]
        val meaning = fields[2].substring(1, fields[2].length - 1)
        val level = fields[3].toInt()
        store.add(WaniKaniVocab(vocab, kana, meaning, level))
    }
}