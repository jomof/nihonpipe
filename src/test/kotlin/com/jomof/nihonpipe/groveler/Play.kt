package com.jomof.nihonpipe.groveler

import com.google.common.truth.Truth.assertThat
import com.jomof.intset.intSetOf
import com.jomof.nihonpipe.datafiles.KuromojiIpadicCache
import com.jomof.nihonpipe.datafiles.SentenceSkeletonFilter
import com.jomof.nihonpipe.datafiles.TranslatedSentences
import com.jomof.nihonpipe.datafiles.VocabToSentenceFilter
import com.jomof.nihonpipe.groveler.schema.particleSkeletonForm
import com.jomof.nihonpipe.play.LadderKind
import com.jomof.nihonpipe.play.Player
import com.jomof.nihonpipe.play.Score
import org.junit.Test

class Play {

    @Test
    fun simplePlayer() {
        (0..1000).forEach {
            Player(mutableMapOf(
                    "入口はどこですか。" to Score(100, 0),
                    "私の日本語教師の犬には名刺があります。" to Score(50, 50)))
        }
    }

    @Test
    fun reportMezzoLevels() {
        val player = Player(mutableMapOf(
                "入口はどこですか。" to Score(100, 0),
                "私の日本語教師の犬には名刺があります。" to Score(60, 50),
                "日本語が分かりましたか。" to Score(75, 50)))
        player.reportMezzoLevels()
    }

    @Test
    fun reportMissingLevelKeys() {
        (0..0).forEach {
            val player = Player(mutableMapOf(
                    "入口はどこですか。" to Score(100, 0),
                    "私の日本語教師の犬には名刺があります。" to Score(60, 50),
                    "日本語が分かりましたか。" to Score(75, 50),
                    "ばい菌だらけだ！" to Score(7, 0),
                    "今こそ一気に取引をまとめるときだ。" to Score(12, 2),
                    "新聞を一つ下さい。" to Score(22, 2),
                    "入口はどこですか。" to Score(32, 9),
                    "りんごを七つ下さい。" to Score(64, 9)))
            val incomplete =
                    player.incompleteLadderLevelKeys()
            val report = incomplete
                    .entries
                    .joinToString("\r\n") { (ladder, keySentence) ->
                        val keys = keySentence.joinToString { (key, sentences) -> "$key[${sentences.size}]" }
                        "${ladder.first} ${ladder.second} of " +
                                "${ladder.first.levelProvider.size} = $keys"
                    }
            println("$report")
        }
    }

    @Test
    fun addNextSentence() {
        val player = Player(mutableMapOf(
                //  "お母さん は どこ 。" to Score(0,0)
        ))

        val translated = TranslatedSentences()
        (0..5000).forEach {
            val (nextSentence, reasons) = player.findNextSentence()
            val sentence = translated.sentences[nextSentence]
            player.addSentence(sentence!!.japanese)
            println("${sentence.japanese} ${sentence.english} $reasons")
//            assertThat(sentence.japanese.length)
//                    .named(sentence.toString())
//                    .isLessThan(50)
        }
        val incomplete =
                player.incompleteLadderLevelKeys()
        val report = incomplete
                .entries
                .joinToString("\r\n") { (ladder, keySentence) ->
                    val keys = keySentence.joinToString { (key, sentences) -> "$key[${sentences.size}]" }
                    "${ladder.first} ${ladder.second} of " +
                            "${ladder.first.levelProvider.size} = $keys"
                }
        println("$report")
    }

    @Test
    fun allSentencesAreCoveredByEachLadderLevel() {
        val allSentenceCount = TranslatedSentences().sentences.size
        for (ladderKind in LadderKind.values()) {
            val sentences = intSetOf()
            for (level in 0 until ladderKind.levelProvider.size) {
                sentences += ladderKind.levelProvider.getLevelSentences(level)
            }
            if (sentences.size != allSentenceCount) {
                (0..allSentenceCount).forEach { index ->
                    if (!sentences.contains(index)) {
                        val sentence = TranslatedSentences().sentences[index]
                        assertThat(sentences.contains(index))
                                .named("sentence $sentence is not covered by $ladderKind")
                                .isTrue()
                    }
                }
            }
        }
    }

    @Test
    fun analyzeSentence() {
        //val target = "大人 ２ 枚 ください 。"
        val target = "彼 は 方向 音痴 だ 。"
        //val target = "お母さん は どこ 。"
        val index = TranslatedSentences().sentenceToIndex(target)
        val found = TranslatedSentences().sentences[index]

        println("$found")
        val tokenization = KuromojiIpadicCache
                .tokenize(target)
        println("tokens = ${tokenization.tokens}")
        println("reading = ${tokenization.reading()}")
        println("skeleton = ${tokenization.particleSkeletonForm()}")
        val skeletonSentences = SentenceSkeletonFilter
                .filterOf
                .skeletons[tokenization.particleSkeletonForm()]!!
        println("There are ${skeletonSentences.size} sentences with this skeleton")
        fun locateInLevel(ladderKind: LadderKind) {
            val provider = ladderKind.levelProvider
            var found = false
            for (level in 0 until provider.size) {
                val keySentences = provider.getKeySentences(level)
                for (keySentence in keySentences) {
                    if (keySentence.sentences.contains(index)) {
                        println("$ladderKind level=$level of ${provider.size} key=${keySentence.key}")
                        found = true
                    }
                }
            }
            if (!found) {
                println("NOT FOUND in $ladderKind")
            }
        }
        for (ladderKind in LadderKind.values()) {
            locateInLevel(ladderKind)
        }

        val vocabToSentenceFilter = VocabToSentenceFilter()


    }
}