package com.jomof.nihonpipe

fun katakanaToRomaji(katakana: Char): String {
    if (katakana in 'a'..'z' || katakana in 'A'..'Z') {
        return katakana.toString()
    }
    if (katakana in 'A'..'Z') {
        return (katakana.toInt() - 'A'.toInt() + 'a'.toInt()).toChar().toString()
    }
    // Weird alphabet
    if (katakana.toInt() in 65313..65338) {
        return (katakana.toInt() - 65313 + 'a'.toInt()).toChar().toString()
    }
    return when (katakana) {
        'ミ' -> "mi"
        'ズ' -> "zu"
        'ヲ' -> "wo"
        'ク' -> "ku"
        'ダ' -> "da"
        'サ' -> "sa"
        'イ' -> "i"
        'オ' -> "o"
        'ト' -> "to"
        'ナ' -> "na"
        'ニ' -> "ni"
        'マ' -> "ma"
        'モ' -> "mo"
        'ヒ' -> "hi"
        'ツ' -> "tsu"
        'ケ' -> "ke"
        'キ' -> "ki"
        'タ' -> "ta"
        'ベ' -> "be"
        'テ' -> "te"
        'デ' -> "de"
        'ス' -> "su"
        'カ' -> "ka"
        'ン' -> "n"
        'ワ' -> "wa"
        'ド' -> "do"
        'コ' -> "ko"
        'ア' -> "a"
        'ハ' -> "ha"
        'ガ' -> "ga"
        'ラ' -> "ra"
        'シ' -> "shi"
        'ソ' -> "so"
        'ヘ' -> "he"
        'ル' -> "ru"
        'ヨ' -> "yo"
        'ウ' -> "u"
        'ホ' -> "ho"
        'ゴ' -> "go"
        'ジ' -> "ji"
        'ノ' -> "no"
        'バ' -> "ba"
        'リ' -> "ri"
        'セ' -> "se"
        'ヤ' -> "ya"
        'ネ' -> "ne"
        'ム' -> "mu"
        'メ' -> "me"
        'ブ' -> "bu"
        'フ' -> "fu"
        'ザ' -> "ze"
        'レ' -> "re"
        'ヅ' -> "dzu"
        'パ' -> "pa"
        'チ' -> "chi"
        'ゲ' -> "ge"
        'ビ' -> "bi"
        'ギ' -> "gi"
        'グ' -> "gu"
        'ロ' -> "ro"
        'エ' -> "e"
        'ゾ' -> "zo"
        'ユ' -> "yu"
        'ピ' -> "pi"
        'ゼ' -> "ze"
        'ボ' -> "bo"
        'ペ' -> "pe"
        'ポ' -> "po"
        'ヌ' -> "nu"
        'ぐ' -> "gu"
        'に' -> "ni"
        'や' -> "ya"
        'ら' -> "ra"
        'な' -> "na"
        'く' -> "ku"
        'プ' -> "pu"
        'あ' -> "a"
        'ざ' -> "za"
        '。' -> "."
        '、' -> ","
        '，' -> ","
        '？' -> "?"
        '！' -> "!"
        '・' -> "/"
        '〜' -> "~"
        '「' -> "["
        '」' -> "]"
        'Ｄ' -> "d"
        'V' -> "v"
        'Ｉ' -> "i"
        65334.toChar() -> "v"
        else ->
            throw RuntimeException("$katakana ${katakana.toInt()}")
    }
}

private fun prejoin(katakana: String): String {
    // https://www.tofugu.com/japanese/how-to-type-in-japanese/
    val result = katakana
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

data class RomajiState(
        var littleStu: Boolean = false
)

fun katakanaToRomaji(
        katakana: String,
        state: RomajiState): String {
    val katakana = prejoin(katakana)
    val sb = StringBuilder()

    for (char in katakana) {
        when (char) {
            'ッ' -> {
                assert(!state.littleStu)
                state.littleStu = true
            }
            'ー' -> {
                sb.append(sb.last())
            }
            else -> {
                var romaji = katakanaToRomaji(char)
                if (state.littleStu) {
                    romaji = romaji[0] + romaji
                    state.littleStu = false
                }
                sb.append(romaji)
            }
        }
    }
    return sb.toString()
}