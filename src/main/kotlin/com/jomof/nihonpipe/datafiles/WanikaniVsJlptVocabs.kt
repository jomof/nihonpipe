package com.jomof.nihonpipe.datafiles

import com.jomof.nihonpipe.*
import com.jomof.nihonpipe.schema.Jlpt
import com.jomof.nihonpipe.schema.WaniKaniVsJlptVocab
import org.h2.mvstore.MVStore
import java.io.File

class WanikaniVsJlptVocabs private constructor(
        file: String = wanikaniVsJlptVocabsBin.absolutePath!!) {

    private val db = MVStore.Builder()
            .fileName(file)
            .compress()
            .open()!!

    private val wanikaniVsJlpt = db.openMap<String, WaniKaniVsJlptVocab>(
            "WanikaniVsJlptVocabs")

    val vocabs: Map<String, WaniKaniVsJlptVocab> = wanikaniVsJlpt

    operator fun invoke(vocab: String) = wanikaniVsJlpt[vocab]

    init {
        if (wanikaniVsJlpt.isEmpty()) {
            translateWaniKaniVsJLPT()
            save()
        }
    }

    private fun translateWaniKaniVsJlpt(
            file: File,
            jlpt: Jlpt,
            map: MutableMap<String, WaniKaniVsJlptVocab>) {
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

    private fun translateWaniKaniVsJLPT() {
        val map = mutableMapOf<String, WaniKaniVsJlptVocab>()
        translateWaniKaniVsJlpt(wanikanivsjlptJLPT5, Jlpt.JLPT5, map)
        translateWaniKaniVsJlpt(wanikanivsjlptJLPT4, Jlpt.JLPT4, map)
        translateWaniKaniVsJlpt(wanikanivsjlptJLPT3, Jlpt.JLPT3, map)
        translateWaniKaniVsJlpt(wanikanivsjlptJLPT2, Jlpt.JLPT2, map)
        translateWaniKaniVsJlpt(wanikanivsjlptJLPT1, Jlpt.JLPT1, map)
        map
                .toList()
                .groupBy { (vocab, _) -> vocab }
                .forEach { (vocab, values) ->
                    wanikaniVsJlpt[vocab] = values.map { it.second }.first()
                }
    }

    companion object {
        private var instance: WanikaniVsJlptVocabs? = null
        val vocabOf: WanikaniVsJlptVocabs
            get() {
                if (instance != null) {
                    return instance!!
                }
                instance = WanikaniVsJlptVocabs()
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