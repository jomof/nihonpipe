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
        val player = Player(mutableMapOf(
                "入口はどこですか。" to Score(100, 0),
                "私の日本語教師の犬には名刺があります。" to Score(60, 50),
                "日本語が分かりましたか。" to Score(75, 50)))
        val incomplete = player.incompleteLadderLevelKeys()
        val report = incomplete.entries.joinToString("\r\n")
        println("$report")
    }

    @Test
    fun analyzeSentence() {
        val target = "遅くなかったです。"
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