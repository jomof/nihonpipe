package com.jomof.nihonpipe.datafiles

import com.jomof.nihonpipe.*
import com.jomof.nihonpipe.schema.JishoVocab
import com.jomof.nihonpipe.schema.Jlpt
import com.jomof.nihonpipe.schema.Jlpt.*
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
            level: Jlpt,
            map: MutableMap<String, List<String>>) {
        val lines = file.readLines()
        for (n in 1 until lines.size) {
            val fields = lines[n].split("\t")
            if (fields.size != 4) {
                throw RuntimeException(fields.size.toString())
            }
            val vocab = fields[0]
            val kana = fields[1]
            val english = fields[3]
            map[english] = listOf(vocab, kana, level.name)
        }
    }

    private fun translateJishoJLPT() {
        val map = mutableMapOf<String, List<String>>()
        translateJishoJLPT(jishoJLPT5, JLPT5, map)
        translateJishoJLPT(jishoJLPT4, JLPT4, map)
        translateJishoJLPT(jishoJLPT3, JLPT3, map)
        translateJishoJLPT(jishoJLPT2, JLPT2, map)
        translateJishoJLPT(jishoJLPT1, JLPT1, map)

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
                return instance!!
            }
    }
}