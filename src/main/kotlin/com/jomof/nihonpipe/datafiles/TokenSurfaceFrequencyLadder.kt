package com.jomof.nihonpipe.datafiles

import com.jomof.algorithm.getsert
import com.jomof.intset.IntSet
import com.jomof.intset.intSetOf
import com.jomof.nihonpipe.groveler.schema.KuromojiIpadicToken
import com.jomof.nihonpipe.schema.KeySentences
import com.jomof.nihonpipe.tokenBaseformFrequencyLadderBin
import com.jomof.nihonpipe.tokenSurfaceFrequencyLadderBin
import org.h2.mvstore.MVMap
import org.h2.mvstore.MVStore
import java.io.File

abstract class TokenFrequencyLadder(database: File) : LevelProvider {
    private val keyLevels: MVMap<Int, List<KeySentences>>
    private val levels: MVMap<Int, IntSet>
    override fun getKeySentences(level: Int): List<KeySentences> {
        populate()
        return keyLevels[level]!!
    }

    override fun getLevelSentences(level: Int): IntSet {
        populate()
        return levels[level]!!
    }

    override val size: Int
        get() {
            populate()
            return keyLevels.size
        }
    override fun getLevelSizes(): List<Int> {
        populate()
        return keyLevels
                .entries
                .sortedBy { it.key }
                .map { it.value.size }
    }

    abstract fun summarizeToken(token: KuromojiIpadicToken): String
    abstract val tableNamePrefix: String

    private fun populate() {
        if (levels.isEmpty()) {
            val map = mutableMapOf<KuromojiIpadicToken, IntSet>()
            for (index in sentenceIndexRange()) {
                val translated = sentenceIndexToTranslatedSentence(index)
                for (token in tokenizeJapaneseSentence(translated.japanese).tokens) {
                    val value = map.getsert(token) { intSetOf() }
                    value += index
                }
            }

            map
                    .entries
                    .sortedByDescending { it.value.size }
                    .chunked(map.size / 60)
                    .forEachIndexed { level, keys ->
                        keyLevels[level] = keys
                                .map { (vocab, sentences) -> KeySentences(summarizeToken(vocab), sentences) }
                        val accumulatedLevels = intSetOf()
                        for ((_, sentencesBits) in keys) {
                            accumulatedLevels += sentencesBits
                        }
                        levels[level] = accumulatedLevels
                    }
            keyLevels.store.commit()
        }
    }

    init {
        val db = MVStore.Builder()
                .fileName(database.absolutePath!!)
                .compress()
                .open()!!
        keyLevels = db.openMap<Int, List<KeySentences>>("${tableNamePrefix}KeyLevels")
        levels = db.openMap<Int, IntSet>("${tableNamePrefix}Levels")
    }
}

class TokenSurfaceFrequencyLadder : TokenFrequencyLadder(tokenSurfaceFrequencyLadderBin) {
    override val tableNamePrefix = "Surface"
    override fun summarizeToken(token: KuromojiIpadicToken) = token.surface
}

class TokenBaseFormFrequencyLadder : TokenFrequencyLadder(tokenBaseformFrequencyLadderBin) {
    override val tableNamePrefix = "BaseForm"
    override fun summarizeToken(token: KuromojiIpadicToken) = token.baseForm
}