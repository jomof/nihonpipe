package com.jomof.nihonpipe.groveler.datafiles

import com.jomof.nihonpipe.groveler.schema.SentenceStatistics
import com.jomof.nihonpipe.groveler.schema.Statistics
import com.jomof.nihonpipe.groveler.schema.plus
import com.jomof.nihonpipe.groveler.sentenceStatisticsCacheBin
import org.h2.mvstore.MVStore

class SentenceStatisticsCache private constructor(
        file: String = sentenceStatisticsCacheBin.absolutePath) {

    private val db = MVStore.Builder()
            .fileName(file)
            .compress()
            .open()!!

    private val sentenceStatisticsCache = db.openMap<String, SentenceStatistics>(
            "SentenceStatisticsCacheBin")

    operator fun invoke(sentence: String): SentenceStatistics {
        val cleaned = sentence.replace(" ", "")
        if (sentenceStatisticsCache.contains(cleaned)) {
            return sentenceStatisticsCache[cleaned]!!
        }
        val wanikaniVsJlptVocabs = WanikaniVsJlptVocabs.vocabOf
        val wanikaniVocabs = WanikaniVocabs.vocabOf
        val jishoJlptVocabs = JishoJlptVocabs.vocabOf
        val optimizedKoreVocabs = OptimizedKoreVocabs.vocabOf
        val kuromojiIpadicTokenization =
                KuromojiIpadicCache.tokenize(cleaned)
        var waniKaniLevel = Statistics()
        var jishoJlpt = Statistics()
        var optCore = Statistics()
        var optCoreVocabKoIndex = Statistics()
        var optCoreSentKoIndex = Statistics()
        var optCoreNewOptVocIndex = Statistics()
        var optCoreOptVocIndex = Statistics()
        var optCoreOptSenIndex = Statistics()
        var optCoreJlpt = Statistics()
        var waniKaniVsJlptWaniKaniLevel = Statistics()
        var waniKaniVsJlptJlptLevel = Statistics()
        kuromojiIpadicTokenization.tokens.forEach { token ->
            val wanikaniVsJlptVocab = wanikaniVsJlptVocabs(token.baseForm)
            val wanikaniVocab = wanikaniVocabs(token.baseForm)
            val jishoJlptVocab = jishoJlptVocabs(token.baseForm)

            if (wanikaniVsJlptVocab != null) {
                waniKaniVsJlptWaniKaniLevel += wanikaniVsJlptVocab.wanikaniLevel
                waniKaniVsJlptJlptLevel += wanikaniVsJlptVocab.jlptLevel.ordinal
            }

            if (wanikaniVocab != null) {
                waniKaniLevel += wanikaniVocab.level
            }

            jishoJlptVocabs(token.baseForm)?.forEach { vocab ->
                jishoJlpt += vocab.jlptLevel.ordinal
            }

            optimizedKoreVocabs(token.baseForm)?.forEach { vocab ->
                optCore += vocab.core
                optCoreVocabKoIndex += vocab.vocabKoIndex
                optCoreSentKoIndex += vocab.sentKoIndex
                optCoreNewOptVocIndex += vocab.newOptVocIndex
                optCoreOptVocIndex += vocab.optVocIndex1
                optCoreOptSenIndex += vocab.optSenIndex
                optCoreJlpt += vocab.jlpt.ordinal
            }
        }
        sentenceStatisticsCache[cleaned] = SentenceStatistics(
                waniKaniLevel = waniKaniLevel,
                jishoJlpt = jishoJlpt,
                optCore = optCore,
                optCoreVocabKoIndex = optCoreVocabKoIndex,
                optCoreSentKoIndex = optCoreSentKoIndex,
                optCoreNewOptVocIndex = optCoreNewOptVocIndex,
                optCoreOptVocIndex = optCoreOptVocIndex,
                optCoreOptSenIndex = optCoreOptSenIndex,
                optCoreJlpt = optCoreJlpt,
                waniKaniVsJlptWaniKaniLevel = waniKaniVsJlptWaniKaniLevel,
                waniKaniVsJlptJlptLevel = waniKaniVsJlptJlptLevel)
        return invoke(sentence)
    }

    companion object {
        private var instance: SentenceStatisticsCache? = null
        val summarize: SentenceStatisticsCache
            get() {
                if (instance != null) {
                    return instance!!
                }
                instance = SentenceStatisticsCache()
                return summarize
            }

        fun save() {
            if (instance != null) {
                KuromojiIpadicCache.save()
                instance!!.db.close()
                instance = null
            }
        }

    }
}