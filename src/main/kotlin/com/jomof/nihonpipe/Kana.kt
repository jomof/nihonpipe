package com.jomof.nihonpipe

import com.jomof.nihonpipe.RomajiStateKind.*

private fun reindexChar(c: Char, base: Int): Char {
    return (c.toInt() - base + 'a'.toInt()).toChar()
}

private fun katakanaToRomaji(katakana: Char): String {
    assert(!isHiragana(katakana)) {
        katakana
    }
    if (katakana in 'a'..'z' || katakana in 'A'..'Z') {
        return katakana.toString()
    }
    if (katakana in 'A'..'Z') {
        return reindexChar(katakana, 'A'.toInt()).toString()
    }
    // Weird alphabets
    if (katakana.toInt() in 65313..65338) {
        return reindexChar(katakana, 65313).toString()
    }
    if (katakana.toInt() in 65345..65370) {
        return reindexChar(katakana, 65345).toString()
    }
    if (isKatakana(katakana)) {
        return toRomaji(katakana)
    }
    return when (katakana) {
        '。' -> "."
        '、' -> ","
        '，' -> ","
        '？' -> "?"
        '！' -> "!"
        '・' -> "/"
        '〜' -> "~"
        '「' -> "["
        '」' -> "]"
        else ->
            throw RuntimeException("$katakana ${katakana.toInt()}")
    }
}

private fun prejoin(katakana: String): String {
    // https://www.tofugu.com/japanese/how-to-type-in-japanese/
    val sb = StringBuilder()
    for (c in katakana) {
        if (isHiragana(c)) {
            sb.append(toKatakana(c))
        } else {
            sb.append(c)
        }
    }

    val result = sb.toString()
            .replace("ビョ", "byo")
            .replace("チョ", "cho")
            .replace("ギョ", "gyo")
            .replace("ヒョ", "hyo")
            .replace("ジョ", "jo")
            .replace("キョ", "kyo")
            .replace("ミョ", "myo")
            .replace("ニョ", "nyo")
            .replace("ピョ", "pyo")
            .replace("リョ", "ryo")
            .replace("ショ", "sho")

            .replace("ビュ", "by")
            .replace("チュ", "chu")
            .replace("ギュ", "gyu")
            .replace("ヒュ", "hyu")
            .replace("ジュ", "ju")
            .replace("キュ", "kyu")
            .replace("ミュ", "myu")
            .replace("ニュ", "nyu")
            .replace("ピュ", "pyu")
            .replace("リュ", "ryu")
            .replace("シュ", "shu")

            .replace("ビャ", "bya")
            .replace("チャ", "cha")
            .replace("ギャ", "gya")
            .replace("ヒャ", "hya")
            .replace("ジャ", "ja")
            .replace("キャ", "kya")
            .replace("ミャ", "mya")
            .replace("ニャ", "nya")
            .replace("ピャ", "pya")
            .replace("リャ", "rya")
            .replace("シャ", "sha")

            .replace("ディ", "di")
            .replace("フィ", "fi")
            .replace("ティ", "ti")
            .replace("ウィ", "wi")
            .replace("ヴィ", "vi")

            .replace("フォ", "fo")
            .replace("ウォ", "who")

            .replace("チェ", "che")
            .replace("フェ", "fe")
            .replace("ジェ", "je")
            .replace("ウェ", "we")

            .replace("ファ", "fa")
            .replace("ヴァ", "va")
            .replace("ツァ", "tsa")

    if (result.contains("ョ")
            || result.contains("ュ")
            || result.contains("ャ")
            || result.contains("ィ")
            || result.contains("ァ")
            || result.contains("々")
            || result.contains("ェ")
            || result.contains("ォ")) {
        throw RuntimeException(katakana)
    }
    return result
}

enum class RomajiStateKind {
    start,
    hasSound,
    inContinuedSound,
    inAlphabet,
    sukuon, // Last seen was ッ
}

data class RomajiState(
        private var state: RomajiStateKind = start,
        private var builder: StringBuilder = StringBuilder(),
        var lastToken: String = "",
        private var thisToken: String = "",
        private var currentSound: String = "",
        private var hadFailure: Boolean = false) {
    fun appendChar(char: Char) {
        val converted = when (char) {
            '。' -> "."
            '、' -> ","
            '，' -> ","
            '？' -> "?"
            '！' -> "!"
            '・' -> "/"
            '〜' -> "~"
            '「' -> "["
            '」' -> "]"
            '+' -> "+"
            '&' -> "&"
            ';' -> ";"
            '/' -> "/"
            '%' -> "%"
            '!' -> "!"
            '（' -> "("
            '）' -> ")"
            '『' -> "["
            '』' -> "]"
            '．' -> "."
            '.' -> "."
            '＝' -> "="
            '【' -> "["
            '】' -> "]"
            '：' -> ":"
            '−' -> "-"
            '―' -> "-"
            '?' -> "?"
            '-' -> "-"
            '　' -> " "
            '○' -> "maru"
            '※' -> "kome"
            '♪' -> "ongaku"
            '㌘' -> "guramu" // This is a cool character
            '①' -> "(1)"
            '②' -> "(2)"
            '℃' -> "(c)"
            '→' -> "->"
            '｡' -> "."
            '／' -> "/"
            '’' -> "'"
            ',' -> ","
            '；' -> ":"
            ':' -> ":"
            '＊' -> "*"
            '”' -> "\""
            '"' -> "\""
            '*' -> "*"
            '>' -> ">"
            '＋' -> "+"
            '＃' -> "#"
            '〈' -> "<"
            '〉' -> ">"
            '､' -> ","
            '^' -> "^"
            '‘' -> "'"
            '＜' -> "<"
            '＞' -> ">"
            '［' -> "["
            '］' -> "]"
            '…' -> "..."
            '〔' -> "["
            '〕' -> "]"
            ' ' -> " "
            '｢' -> "["
            '｣' -> "]"
            '(' -> "("
            ')' -> ")"
            '[' -> "["
            ']' -> "]"
            else -> null
        }
        when {
            converted != null -> {
                when (state) {
                    inAlphabet, start -> {
                        thisToken += converted
                    }
                    hasSound, inContinuedSound -> {
                        thisToken += currentSoundToRomaji()
                        thisToken += converted
                        state = start
                    }
                    sukuon -> {
                        state = start
                        appendChar(char)
                    }
                }
            }
            char == 'ッ' -> {
                thisToken += currentSoundToRomaji()
                state = sukuon
            }
            char == 'ー' -> {
                when (state) {
                    inContinuedSound, hasSound -> {
                        thisToken += currentSoundToRomaji()
                        currentSound = when {
                            currentSound.isNotEmpty() -> currentSound.last().toString()
                            thisToken.isNotEmpty() -> thisToken.last().toString()
                            lastToken.isNotEmpty() -> lastToken.last().toString()
                            else -> throw RuntimeException(this.toString())
                        }
                    }
                    else ->
                        throw RuntimeException(state.toString())
                }
            }
            isSmallKatakana(char) -> {
                when (state) {
                    start, inContinuedSound, hasSound -> {
                        currentSound += char
                        state = inContinuedSound
                    }
                    else ->
                        throw RuntimeException(state.toString())
                }
            }
            isLargeKatakana(char) -> {
                when (state) {
                    inAlphabet, start -> {
                        currentSound = char.toString()
                        state = hasSound
                    }
                    inContinuedSound, hasSound -> {
                        thisToken += currentSoundToRomaji()
                        currentSound = char.toString()
                    }
                    sukuon -> {
                        thisToken += toRomaji(char)[0]
                        state = start
                        appendChar(char)
                    }
                }
            }
            isHiragana(char) -> appendChar(toKatakana(char))
            char.toInt() in 65296..65306 ->
                return appendChar(reindexChar(char, 65296))
            char.toInt() in 65313..65338 ->
                return appendChar(reindexChar(char, 65313))
            char.toInt() in 65345..65370 ->
                return appendChar(reindexChar(char, 65345))
            char in 'A'..'Z' ->
                return appendChar(reindexChar(char, 'A'.toInt()))
            char in 'a'..'z' || char in '0'..'9' -> {
                when (state) {
                    start -> {
                        thisToken += char
                        state = inAlphabet
                    }
                    inAlphabet -> {
                        thisToken += char
                    }
                    inContinuedSound, hasSound -> {
                        thisToken += currentSoundToRomaji()
                        thisToken += char
                    }
                    else ->
                        throw RuntimeException(state.toString())
                }
            }
            isKanji(char) -> {
                hadFailure = true
                thisToken += 'X'
            }
            else ->
                throw RuntimeException("'$char' ${char.toInt()}")
        }
    }

    private fun currentSoundToRomaji(): String {
        val result = soundToRomaji(currentSound)
        currentSound = ""
        return result
    }

    fun endToken(): String {
        thisToken += currentSoundToRomaji()
        builder.append(thisToken)
        lastToken = thisToken
        thisToken = ""
        return lastToken
    }

    override fun toString(): String {
        // toString shouldn't change state because it may be executed
        // from debugger.
        return builder.toString() + thisToken + soundToRomaji(currentSound)
    }
}

fun katakanaToRomaji(
        katakana: String,
        state: RomajiState): String {
    if (katakana == "々") {
        return state.lastToken
    }
    for (char in katakana) {
        state.appendChar(char)
    }
    return state.endToken()
}

/**
 * Determines if this character is a Japanese Kana.
 */
fun isKana(c: Char): Boolean {
    return isHiragana(c) || isKatakana(c)
}

/**
 * Determines if this character is one of the Japanese Hiragana.
 */
fun isHiragana(c: Char): Boolean {
    return c in '\u3041'..'\u309e'
}

/**
 * Determines if this character is one of the Japanese Katakana.
 */
fun isKatakana(c: Char): Boolean {
    return isHalfWidthKatakana(c) || isFullWidthKatakana(c)
}

/**
 * Determines if this character is a Half width Katakana.
 */
fun isHalfWidthKatakana(c: Char): Boolean {
    return c in '\uff66'..'\uff9d'
}

/**
 * Determines if this character is a Full width Katakana.
 */
fun isFullWidthKatakana(c: Char): Boolean {
    return c in '\u30a1'..'\u30fe'
}

/**
 * Determines if this character is a small full width Katakana.
 */
fun isSmallKatakana(c: Char): Boolean {
    return when (c) {
        '\u30a1', '\u30a3', '\u30a5', '\u30a7',
        '\u30a9', '\u30c3', '\u30e3', '\u30e5',
        '\u30e7', '\u30ee' -> true
        else -> false
    }
}

/**
 * Determines if this character is a large full width Katakana.
 */
fun isLargeKatakana(c: Char): Boolean {
    return isKatakana(c) && !isSmallKatakana(c)
}

/**
 * Determines if this character is a Kanji character.
 */
fun isKanji(c: Char): Boolean {
    if (c in '\u4e00'..'\u9fa5') {
        return true
    }
    return c in '\u3005'..'\u3007'
}

/**
 * Determines if this character could be used as part of
 * a romaji character.
 */
fun isRomaji(c: Char): Boolean {
    return if (c in '\u0041'..'\u0090')
        true
    else if (c in '\u0061'..'\u007a')
        true
    else if (c in '\u0021'..'\u003a')
        true
    else c in '\u0041'..'\u005a'
}

/**
 * Translates this character into the equivalent Katakana character.
 * The function only operates on Hiragana and returns either full
 * width or half width Katakana. If the character is outside the
 * Hiragana then the original character is returned.
 */
fun toKatakana(c: Char): Char {
    return if (isHiragana(c)) {
        (c.toInt() + 0x60).toChar()
    } else c
}

/**
 * Translates this character into the equivalent Hiragana character.
 * The function only operates on Katakana characters
 * If the character is outside the Full width or Half width
 * Katakana then the origianal character is returned.
 */
fun toHiragana(c: Char): Char {
    if (isFullWidthKatakana(c)) {
        return (c.toInt() - 0x60).toChar()
    } else if (isHalfWidthKatakana(c)) {
        return (c.toInt() - 0xcf25).toChar()
    }
    return c
}

/**
 * Translates this character into the equivalent Romaji character.
 * The function only operates on Hiragana and Katakana characters
 * If the character is outside the given range then
 * the origianal character is returned.
 *
 *
 * The resulting string is lowercase if the input was Hiragana and
 * UPPERCASE if the input was Katakana.
 */
fun toRomaji(c: Char): String {
    if (isHiragana(c)) {
        return lookupRomaji(c)
    } else if (isKatakana(c)) {
        return lookupRomaji(toHiragana(c))
    }
    return c.toString()
}

/**
 * Convert a full sound to romaji
 */
fun soundToRomaji(sound: String): String {
    if (sound.isEmpty()) {
        return ""
    }
    if (sound.length == 1) {
        return toRomaji(sound[0])
    }
    val first = sound[0]
    var result =
            when (first) {
                'ァ', 'ア' -> "a"
                'ビ' -> "b"
                'ベ' -> "be"
                'ブ' -> "bu"
                'チ' -> "c"
                'デ' -> "d"
                'ヂ' -> "dj"
                'ド' -> "do"
                'フ' -> "f"
                'ギ' -> "g"
                'ヒ' -> "h"
                'ハ' -> "ha"
                'ヘ' -> "he"
                'ジ' -> "j"
                'キ' -> "k"
                'カ' -> "ka"
                'ク' -> "ku"
                'ミ' -> "m"
                'マ' -> "ma"
                'メ' -> "me"
                'ニ' -> "n"
                'ネ' -> "ne"
                'ナ' -> "na"
                'ピ' -> "p"
                'リ' -> "r"
                'シ' -> "s"
                'サ' -> "sa"
                'テ' -> "t"
                'ト' -> "to"
                'ツ' -> "tsu"
                'ヴ' -> "v"
                'ウ' -> "w"
                'ワ' -> "wa"
                'ヤ' -> "ya"
                'ヨ' -> "yo"
                'ズ' -> "zu"
                else ->
                    throw RuntimeException(sound)
            }
    for (index in 1 until sound.length) {
        val current = sound[index]
        result += when (current) {
            'ョ' -> when (result.last()) {
                'c' -> "ho"
                'j' -> "o"
                's' -> "ho"
                else -> "yo"
            }
            'ャ' -> when (result.last()) {
                'c' -> "ha"
                'j' -> "a"
                's' -> "ha"
                else -> "ya"
            }
            'ュ' -> when (result.last()) {
                'c' -> "hu"
                'j' -> "u"
                's' -> "hu"
                else -> "yu"
            }
            'ィ' -> 'i'
            'ォ' -> 'o'
            'ェ' -> 'e'
            'ァ' -> 'a'
            'ゥ' -> 'u'
            else -> throw RuntimeException(sound)
        }
    }
    return result
}

/**
 * The array used to map hirgana to romaji.
 */
var romaji = arrayOf("a", "a", "i", "i", "u", "u", "e", "e", "o", "o",

        "ka", "ga", "ki", "gi", "ku", "gu", "ke", "ge", "ko", "go",

        "sa", "za", "shi", "ji", "su", "zu", "se", "ze", "so", "zo",

        "ta", "da", "chi", "ji", "tsu", "tsu", "zu", "te", "de", "to", "do",

        "na", "ni", "nu", "ne", "no",

        "ha", "ba", "pa", "hi", "bi", "pi", "fu", "bu", "pu", "he", "be", "pe", "ho", "bo", "po",

        "ma", "mi", "mu", "me", "mo",

        "a", "ya", "u", "yu", "o", "yo",

        "ra", "ri", "ru", "re", "ro",

        "wa", "wa", "wi", "we", "wo", "n",

        "v", "ka", "ke")

/**
 * Access the array to return the correct romaji string.
 */
private fun lookupRomaji(c: Char): String {
    val index = c.toInt() - 0x3041
    assert(index in 0 until romaji.size) {
        "lookup of '$c' is out of range. Was $index but array is size ${romaji.size}"
    }
    return romaji[index]
}