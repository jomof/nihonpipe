package com.jomof.nihonpipe

import com.google.common.truth.Truth.assertThat
import com.jomof.nihonpipe.datafiles.sentenceIndexRange
import com.jomof.nihonpipe.datafiles.sentenceIndexToTranslatedSentence
import com.jomof.nihonpipe.play.generateAnkiInfo
import org.junit.Test

class Test {
    //@Test
    fun test() {
        for ((index, sentenceIndex) in sentenceIndexRange().withIndex()) {
            if (index > 0 && index % 1000 == 0) {
                println("sentence $index")
            }
            val english = sentenceIndexToTranslatedSentence(sentenceIndex).english
            generateAnkiInfo(english)
        }
    }

    @Test
    fun guramu() {
        val english = "Born on 2006 Feb 23, at 2730g, our family's treasure."
        val info = generateAnkiInfo(english)
        assertThat(info.romaji).isEqualTo("２[ni]  ０[zero]  ０[zero]  ６[roku]  " +
                "年[nen]  ２月[nigatsu]  ２[ni]  ３[san]  日[nichi]  、[,]  " +
                "２[ni]  ７[nana]  ３[san]  ０[zero]  ㌘[guramu]  " +
                "で[de]  誕生[tanjoo]  し[shi]  た[ta]  我が家[wagaya]  " +
                "の[no]  宝物[hoomotsu]  。[.]")
    }

    @Test
    fun fiveMinutes() {
        val english = "It's OK to think of 'five minutes' as a noun phrase, right?"
        val info = generateAnkiInfo(english)
        assertThat(info.romaji).isEqualTo("’[']  fiveminutes[fiveminutes]  ’[']  って[tte]  名詞[meishi]  句[ku]  " +
                "で[de]  いい[ii]  ん[n]  です[desu]  よ[yo]  ね[ne]  ？[?]")
    }

    @Test
    fun theLongerWeWait() {
        val english = "The longer we waited, the more impatient we became."
        val info = generateAnkiInfo(english)
        assertThat(info.romaji).isEqualTo("待て[mate]  ば[ba]  待つ[matsu]  " +
                "ほど[hodo]  、[,]  私[watashi]  たち[tachi]  は[wa]  " +
                "苛[X]  々[X]  し[shi]  て[te]  き[ki]  た[ta]  。[.]")
    }

    @Test
    fun prototypeJs() {
        val english = "prototype.js - inserts 'what's new' data into the page when it is read."
        val info = generateAnkiInfo(english)
        assertThat(info.romaji).isEqualTo("ページ[peeji]  が[ga]  読み込ま[yomikoma]  " +
                "れ[re]  たら[tara]  更新[kooshin]  情報[joohoo]  " +
                "を[wo]  ページ[peeji]  内[nai]  に[ni]  流し込む[nagashikomu]  " +
                "　[ ]  prototype[prototype]  .[.]  js[js]")
    }

    @Test
    fun whinyThoughts() {
        val english = "Or rather, is it not just pathetic that I think such whiny thoughts like this?"
        val info = generateAnkiInfo(english)
        assertThat(info.romaji).isEqualTo("っ[]  てゆーか[tteyuuka]  、[,]  こう[koo]  " +
                "やっ[ya]  て[tte]  ぐじぐじ[gujiguji]  考える[kangaeru]  の[no]  " +
                "が[ga]  情けない[nasakenai]  ん[n]  じゃ[ja]  ない[nai]  の[no]  " +
                "か[ka]  ？[?]")
    }

    @Test
    fun heSawRightThroughMe() {
        val english = "Uh-oh. He knew I was lying - saw right through me. I didn't know what to say."
        val info = generateAnkiInfo(english)
        assertThat(info.romaji).isEqualTo("ウッ[u]  、[,]  嘘[uso]  を[wo]  つい[tsui]  て[te]  " +
                "いる[iru]  の[no]  を[wo]  見透かさ[misukasa]  れ[re]  て[te]  、[,]  " +
                "答え[kotae]  に[ni]  困っ[koma]  た[tta]  。[.]")
    }

    @Test
    fun testWeirdAlphabet() {
        val english = "IT is a major industry in India."
        val info = generateAnkiInfo(english)
        assertThat(info.romaji).isEqualTo("ＩＴ[it]  産業[sangyoo]  は[wa]  インド[indo]  " +
                "の[no]  主要[shuyoo]  産業[sangyoo]  よ[yo]  ね[ne]  。[.]")
    }

    @Test
    fun mySocksAreFallingApart() {
        val english = "My socks are falling apart."
        val info = generateAnkiInfo(english)
        assertThat(info.romaji).isEqualTo("靴下[kutsushita]  " +
                "が[ga]  綻[X]  ろ[ro]  びている[biteiru]")
    }

    @Test
    fun testHalfWidthHiragana() {
        val halfYaHiragana = 'ゃ'
        val fullYaHiragana = 'や'
        assertThat(isHiragana(halfYaHiragana)).isTrue()
        assertThat(isHiragana(fullYaHiragana)).isTrue()
        assertThat(toKatakana(halfYaHiragana)).isEqualTo('ャ')
        assertThat(toKatakana(fullYaHiragana)).isEqualTo('ヤ')
    }

    @Test
    fun testReproZaIssue() {
        assertThat(isHiragana('ざ')).isTrue()
        assertThat(toKatakana('ざ')).isEqualTo('ザ')
        val english = "It's pouring."
        val info = generateAnkiInfo(english)
        assertThat(info.romaji).isEqualTo("雨[ame]  が[ga]  " +
                "ざあざあ[zaazaa]  降っ[fu]  て[tte]  いる[iru]  。[.]")
    }

    @Test
    fun testReproWeirdDot() {
        val english = "She attends culture school."
        val info = generateAnkiInfo(english)
        assertThat(info.romaji).isEqualTo("彼女[kanojo]  は[wa]  " +
                "カルチャー[karuchaa]  ・[/]  スクール[sukuuru]  " +
                "に[ni]  通っ[kayo]  て[tte]  い[i]  ます[masu]")
    }


    @Test
    fun testReproEmptySoundContinuation() {
        val english = "Can I get two ice cream cones?"
        val info = generateAnkiInfo(english)
        assertThat(info.romaji).isEqualTo("ソフトクリーム[sofutokuriimu]  " +
                "を[wo]  二つ[futatsu]  ください[kudasai]  。[.]")
    }

    @Test
    fun testReproIsolatedDash() {
        val english = "I ate a slice of cheese."
        val info = generateAnkiInfo(english)
        assertThat(info.romaji).isEqualTo("ち[chi]  ー[i]  " +
                "ず[zu]  を[wo]  一[ichi]  切れ[kire]  " +
                "食べ[tabe]  まし[mashi]  た[ta]  。[.]")
    }

    @Test
    fun testReproAlphabetAfterSomething() {
        val english = "I bought a DVD in the sales section."
        val info = generateAnkiInfo(english)
        assertThat(info.romaji).isEqualTo("バーゲン[baagen]  " +
                "コーナー[koonaa]  で[de]  ＤＶＤ[dvd]  を[wo]  " +
                "買い[kai]  まし[mashi]  た[ta]  。[.]")
    }

    @Test
    fun testReproIsshoIssue() {
        val english = "I did my homework with my friend."
        val info = generateAnkiInfo(english)
        assertThat(info.romaji).isEqualTo("友達[tomodachi]  " +
                "と[to]  一緒[issho]  に[ni]  宿題[shukudai]  " +
                "を[wo]  し[shi]  た[ta]  。[.]")
    }

    @Test
    fun someJuicePlease() {
        val english = "Some juice, please."
        val info = generateAnkiInfo(english)
        assertThat(info.romaji).isEqualTo("ジュース[juusu]  " +
                "を[wo]  ください[kudasai]  。[.]")
    }

    @Test
    fun testReproContinuedSound() {
        var english = "I haven't finished my homework yet."
        val info = generateAnkiInfo(english)
        assertThat(info.romaji).isEqualTo("まだ[mada]  " +
                "宿題[shukudai]  が[ga]  " +
                "終わら[owara]  ない[nai]  。[.]")
    }

    @Test
    fun testLargeSmallKatakana() {
        assertThat(isHiragana('ョ')).isFalse()
        assertThat(isKatakana('ョ')).isTrue()
        assertThat(isLargeKatakana('ョ')).isFalse()
        assertThat(isSmallKatakana('ョ')).isTrue()
        assertThat(isSmallKatakana('ォ')).isTrue()

        assertThat(isLargeKatakana('ビ')).isTrue()
        assertThat(isSmallKatakana('ビ')).isFalse()
    }

    @Test
    fun buildUpRomajiState() {
        assertThat(katakanaToRomaji("ヤッヒ", RomajiState())).isEqualTo("yahhi")
        assertThat(katakanaToRomaji("ヤー", RomajiState())).isEqualTo("yaa")
        assertThat(katakanaToRomaji("ビョ", RomajiState())).isEqualTo("byo")
        assertThat(katakanaToRomaji("ヤ", RomajiState())).isEqualTo("ya")
    }

    @Test
    fun soundToRomaji() {
        val pairs = listOf(
                "ヂュ" to "dju",
                "ズィ" to "zui",
                "ヨォ" to "yoo",
                "ハァ" to "haa",
                "ビョ" to "byo",
                "サァ" to "saa",
                "リャ" to "rya",
                "ピュ" to "pyu",
                "ウェ" to "we",
                "ショ" to "sho",
                "ファ" to "fa"
        )
        for ((sound, romaji) in pairs) {
            assertThat(soundToRomaji(sound)).isEqualTo(romaji)
        }
    }

    @Test
    fun convertVariousToTokens() {
        val pairs = listOf(
                "アァァ" to "aaa",
                "アァ" to "aa",
                "ソフトクリーム" to "sofutokuriimu",
                "イッショ" to "issho",
                "サンギョー" to "sangyoo"
        )
        for ((sound, romaji) in pairs) {
            assertThat(katakanaToRomaji(sound, RomajiState())).isEqualTo(romaji)
        }
    }
}