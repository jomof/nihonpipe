package com.jomof.nihonpipe.groveler

import java.io.File

class Index(val originalNext: Int, val map: MutableMap<String, Int>) {
    var next = originalNext
    fun getOrdinal(key: String): Int {
        val sha = getSHA256OfString(key)
        if (map.containsKey(sha)) {
            return map[sha]!!
        }
        map[sha] = next++
        return getOrdinal(key)
    }

    fun size(): Int {
        return originalNext
    }

    fun containsKey(key: String): Boolean {
        return map.containsKey(getSHA256OfString(key))
    }

    fun writeFile(file: File) {
        var sb = StringBuilder()
        sb.append("$next\r")
        for ((key, value) in map) {
            sb.append("$key $value\r")
        }
        file.writeText(sb.toString())
    }

    fun hasChanged(): Boolean {
        return originalNext != next
    }
}

fun readIndex(file: File): Index {
    val result = mutableMapOf<String, Int>()
    if (!file.isFile) {
        return Index(0, result)
    }
    val lines = file.readLines()
    val next = lines[0].toInt()
    for (i in 1 until lines.size) {
        val line = lines[i]
        val split = line.split(" ")
        result[split[0]] = split[1].toInt()
    }
    return Index(next, result)
}