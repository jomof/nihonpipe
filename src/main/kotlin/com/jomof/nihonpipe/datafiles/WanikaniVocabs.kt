package com.jomof.nihonpipe.datafiles

import com.jomof.nihonpipe.schema.WaniKaniVocab
import com.jomof.nihonpipe.wanikaniVocabFile
import com.jomof.nihonpipe.wanikaniVocabsBin
import org.h2.mvstore.MVStore

class WanikaniVocabs private constructor(
        file: String = wanikaniVocabsBin.absolutePath!!) {

    private val db = MVStore.Builder()
            .fileName(file)
            .compress()
            .open()!!

    private val wanikaniVocab = db.openMap<String, WaniKaniVocab>("WanikaniVocab")
    operator fun invoke(vocab: String) = wanikaniVocab[vocab]

    init {
        if (wanikaniVocab.isEmpty()) {
            translateWaniKaniVocab()
            save()
        }
    }

    private fun translateWaniKaniVocab() {
        val lines = wanikaniVocabFile.readLines()
        (0 until lines.size).map { n ->
            val fields = lines[n].split("\t")
            if (fields.size != 4) {
                throw RuntimeException(fields.size.toString())
            }
            val kana = fields[0].substring(1, fields[0].length - 1)
            val vocab = fields[1]
            val meaning = fields[2].substring(1, fields[2].length - 1)
            val level = fields[3].toInt()
            WaniKaniVocab(vocab, kana, meaning, level)
        }
                .groupBy { it.vocab }
                .forEach { vocab, vocabs ->
                    wanikaniVocab[vocab] = vocabs.single()
                }
    }

    companion object {
        private var instance: WanikaniVocabs? = null
        val vocabOf: WanikaniVocabs
            get() {
                if (instance != null) {
                    return instance!!
                }
                instance = WanikaniVocabs()
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