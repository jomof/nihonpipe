package com.jomof.nihonpipe.groveler

import com.jomof.nihonpipe.groveler.schema.Jlpt
import com.jomof.nihonpipe.groveler.schema.OptimizedKoreVocab
import com.jomof.nihonpipe.groveler.schema.Store

fun translateOptimizedKore(store: Store) {
    val lines = optimizedKoreFile.readLines()
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

        store.add(OptimizedKoreVocab(coreIndex, vocabKoIndex, sentKoIndex, newOptVocIndex,
                optVocIndex, optSenIndex, Jlpt.of(jlpt), vocab, kana, english, pos))
    }
}
