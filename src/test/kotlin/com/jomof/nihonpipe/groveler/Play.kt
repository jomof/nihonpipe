package com.jomof.nihonpipe.groveler

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
        val player = Player(mutableMapOf())

        val translated = TranslatedSentences()
        (0..1000).forEach {
            val nextSentence = player.findNextSentence()
            val sentence = translated.sentences[nextSentence]
            player.addSentence(sentence!!.japanese)
            println("$sentence")
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
    fun analyzeSentence() {
        val target = "人工 着色 料 。"
        //val target = "オレ に 八つ 当たり する な よ 。"
        val found =
                TranslatedSentences()
                        .sentences
                        .filter { (key, sentence) ->
                            sentence.japanese == target
                        }
                        .entries
                        .toList()
                        .single()
        println("$found")
        val tokenization = KuromojiIpadicCache
                .tokenize(target)
        println("tokens = ${tokenization.tokens}")
        println("skeleton = ${tokenization.particleSkeletonForm()}")
        val skeletonSentences = SentenceSkeletonFilter
                .filterOf
                .skeletons[tokenization.particleSkeletonForm()]!!
        println("There are ${skeletonSentences.size} sentences with this skeleton")
        val index = found.key
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