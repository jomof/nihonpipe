package com.jomof.nihonpipe.datafiles

import com.jomof.nihonpipe.optimizedKoreFile
import com.jomof.nihonpipe.optimizedKoreVocabsBin
import com.jomof.nihonpipe.schema.Jlpt
import com.jomof.nihonpipe.schema.OptimizedKoreVocab
import org.h2.mvstore.MVStore

class OptimizedKoreVocabs private constructor(
        file: String = optimizedKoreVocabsBin.absolutePath!!) {

    private val db = MVStore.Builder()
            .fileName(file)
            .compress()
            .open()!!

    private val optimizedKore = db.openMap<String, List<OptimizedKoreVocab>>(
            "OptimizedKoreVocabs")

    operator fun invoke(vocab: String) = optimizedKore[vocab]

    init {
        if (optimizedKore.isEmpty()) {
            translateOptimizedKore()
            save()
        }
    }

    private fun translateOptimizedKore() {
        val lines = optimizedKoreFile.readLines()
        (2 until lines.size).map { n ->
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

            OptimizedKoreVocab(
                    coreIndex,
                    vocabKoIndex,
                    sentKoIndex,
                    newOptVocIndex,
                    optVocIndex,
                    optSenIndex,
                    Jlpt.of(jlpt),
                    vocab,
                    kana,
                    english,
                    pos)
        }
                .groupBy { kore -> kore.vocab }
                .forEach { vocab, kores ->
                    optimizedKore[vocab] = kores
                }
    }

    companion object {
        private var instance: OptimizedKoreVocabs? = null
        val vocabOf: OptimizedKoreVocabs
            get() {
                if (instance != null) {
                    return instance!!
                }
                instance = OptimizedKoreVocabs()
                return instance!!
            }

        fun save() {
            if (instance != null) {
                instance!!.db.close()
                instance = null
            }
        }
    }
}