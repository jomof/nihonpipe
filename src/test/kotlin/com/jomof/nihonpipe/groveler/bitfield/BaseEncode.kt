package com.jomof.nihonpipe.groveler.bitfield

private const val base = Character.MAX_RADIX

fun baseEncode(num: Int): String {
    return java.lang.Integer.toString(num, base)
}

private fun baseDecode(c: Char): Int {
    return Integer.parseInt(c.toString(), base)
}

fun baseInsertDigit(original: Int, c: Char): Int {
    return original * base + baseDecode(c)
}