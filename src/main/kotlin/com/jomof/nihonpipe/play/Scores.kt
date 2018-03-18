package com.jomof.nihonpipe.play

import com.jomof.algorithm.getsert

data class Scores(
        val currentLevel: Int = 0,
        val levels: MutableMap<Int, MutableMap<String, Score>> = mutableMapOf()) {
    operator fun get(level: Int, key: String): Score {
        while (level >= levels.size) {
            levels[level] = mutableMapOf()
        }
        val map = levels[level]!!
        return map.getsert(key) { Score() }
    }
}