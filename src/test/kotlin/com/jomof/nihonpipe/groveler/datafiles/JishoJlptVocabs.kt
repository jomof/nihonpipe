package com.jomof.nihonpipe.groveler.datafiles

import com.jomof.nihonpipe.groveler.*
import com.jomof.nihonpipe.groveler.schema.JishoVocab
import com.jomof.nihonpipe.groveler.schema.Jlpt
import com.jomof.nihonpipe.groveler.schema.jlptToInt
import org.h2.mvstore.MVStore
import java.io.File

class JishoJlptVocabs private constructor(
        file: String = jishoJlptVocabsBin.absolutePath!!) {

    private val db = MVStore.Builder()
            .fileName(file)
            .compress()
            .open()!!

    private val jishoJlpt = db.openMap<String, List<JishoVocab>>("JishoJlptVocabs")
    operator fun invoke(vocab: String) = jishoJlpt[vocab]

    init {
        if (jishoJlpt.isEmpty()) {
            translateJishoJLPT()
        }
    }

    private fun translateJishoJLPT(
            file: File,
            level: Int,
            map: MutableMap<String, List<String>>) {
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

    private fun translateJishoJLPT() {
        val map = mutableMapOf<String, List<String>>()
        translateJishoJLPT(jishoJLPT5, jlptToInt("jlpt5"), map)
        translateJishoJLPT(jishoJLPT4, jlptToInt("jlpt4"), map)
        translateJishoJLPT(jishoJLPT3, jlptToInt("jlpt3"), map)
        translateJishoJLPT(jishoJLPT2, jlptToInt("jlpt2"), map)
        translateJishoJLPT(jishoJLPT1, jlptToInt("jlpt1"), map)

        map.map { (english, values) ->
            listOf(values[0], values[1], values[2], english)
        }
                .sortedByDescending { it[2] }
                .map {
                    JishoVocab(it[0], it[1], Jlpt.of(it[2]), it[3])
                }
                .groupBy { it.vocab }
                .forEach { vocab, jisho ->
                    jishoJlpt[vocab] = jisho
                }
    }

    companion object {
        private var instance: JishoJlptVocabs? = null
        val vocabOf: JishoJlptVocabs
            get() {
                if (instance != null) {
                    return instance!!
                }
                instance = JishoJlptVocabs()
                return vocabOf
            }

        fun save() {
            if (instance != null) {
                instance!!.db.close()
                instance = null
            }
        }
    }
}