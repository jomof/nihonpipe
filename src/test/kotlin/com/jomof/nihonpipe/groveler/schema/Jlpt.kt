package com.jomof.nihonpipe.groveler.schema

enum class Jlpt {
    JLPT6,
    JLPT5,
    JLPT4,
    JLPT3,
    JLPT2,
    JLPT1,
    JLPT0;

    companion object {
        fun of(value: String): Jlpt {
            return when (value) {
                "JLPT1", "jlpt1" -> JLPT1
                "JLPT2", "jlpt2" -> JLPT2
                "JLPT3", "jlpt3" -> JLPT3
                "JLPT4", "jlpt4" -> JLPT4
                "JLPT5", "jlpt5" -> JLPT5
                "JLPT6", "jlpt6" -> JLPT6
                "JLPT0", "jlpt0" -> JLPT0
                else -> throw RuntimeException(value)
            }
        }
    }
}