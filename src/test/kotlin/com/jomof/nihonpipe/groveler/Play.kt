package com.jomof.nihonpipe.groveler

import com.google.common.truth.Truth.assertThat
import com.jomof.intset.intSetOf
import com.jomof.nihonpipe.datafiles.*
import com.jomof.nihonpipe.groveler.schema.particleSkeletonForm
import com.jomof.nihonpipe.play.*
import com.jomof.nihonpipe.sampleSentencesTsv
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
            println(report)
        }
    }

    @Test
    fun bestSeedSentence() {
        val sentenceSkeletonLadder = SentenceSkeletonLadder().getLevelSentences(0)
        val grammarSummaryLadder = GrammarSummaryLadder().getLevelSentences(0)
        val wanikaniVocabLadder = WanikaniVocabLadder().getLevelSentences(0)
        val jlptVocabLadder = JlptVocabLadder().getLevelSentences(0)

        val skeletonByGrammar = sentenceSkeletonLadder intersect grammarSummaryLadder
        val wanikaniByJlpt = wanikaniVocabLadder intersect jlptVocabLadder

        val total = skeletonByGrammar intersect  wanikaniByJlpt
        for (sentence in total) {
            val translated = TranslatedSentences().sentences[sentence]
            println("$sentence $translated")
        }
    }

    @Test
    fun repro() {
        val coordinateIndex = ScoreCoordinateIndex()
        val target1 = "頭 の 毛 は 灰色 だっ た 。"
        val target2 = "私 は 君 の 判断 を 尊重 する 。"
        val index1 = TranslatedSentences().sentenceToIndex(target1)!!
        val index2 = TranslatedSentences().sentenceToIndex(target2)!!
        val reasons1 = coordinateIndex.sentences[index1]
        val reasons2 = coordinateIndex.sentences[index2]
        val player = Player(mutableMapOf())
        assertThat(player.coordinates.size).isEqualTo(0)
        player.addSentence(target1)
        assertThat(player.coordinates).isEqualTo(reasons1)
        assertThat(coordinateIndex.sentences[index1]).isEqualTo(reasons1)
        assertThat(coordinateIndex.sentences[index2]).isEqualTo(reasons2)
        player.addSentence(target2)
        assertThat(player.coordinates).isNotEqualTo(reasons1)
        assertThat(player.coordinates).isNotEqualTo(reasons2)
        val combined = reasons1 union reasons2
        assertThat(player.coordinates).isEqualTo(combined)
    }

    @Test
    fun addNextSentence() {
        val player = Player(mutableMapOf())
        sampleSentencesTsv.delete()
        (0..5000).forEach {
            if ((it) % 50 == 51) {
                val incomplete =
                        player.incompleteLadderLevelKeys()
                val report = incomplete
                        .entries
                        .joinToString("\r\n") { (ladder, keySentence) ->
                            val keys = keySentence.joinToString { (key, sentences) -> "$key[${sentences.size}]" }
                            val coveredKeys = keySentence.count()
                            val totalKeys = ladder.first.levelProvider.getKeySentences(ladder.second).count()
                            "${ladder.first} level ${ladder.second} of " +
                                    "${ladder.first.levelProvider.size - 1} " +
                                    "with ${totalKeys - coveredKeys} of $totalKeys keys covered = $keys"
                        }
                println(report)
            }
            val sentence = player.addOneSentence()
            println(sentence.toDisplayString())
            sampleSentencesTsv.appendText("${sentence.toDisplayString()}\r\n")
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
        println(report)
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
        //val target = "彼 は 方向 音痴 だ 。"
        //val target = "もう 一つ ケーキ を 食べ て も いい です か 。"
        //val target = "ジム は 肩幅 が 広い 。"
        //val target = "君 は 外来 思想 に 偏見 を 抱い て いる よう だ 。"
        //val target = "頭 の 毛 は 灰色 だっ た 。"
        val target = "私 は 君 の 判断 を 尊重 する 。"
        //val target = "私と一緒に外に来て。"
        //val target = "これ は 本 です 。"
        //val target = "お母さん は どこ 。"
        //val target = "ここ は 今 乾期 です 。"
        //val target = "バラ は 今 満開 です 。"
        val index = TranslatedSentences().sentenceToIndex(target)!!
        val found = TranslatedSentences().sentences[index]

        println("$found")
        val tokenization = KuromojiIpadicCache
                .tokenize(target)
        println("sentence index = $index")
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
            val size = provider.size
            for (level in 0 until size) {
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

        val (sentences, burden) =  LeastBurdenSentenceTransitions().getNextSentences(index)
        println("The most similar sentences have burden $burden -> ")
        for (ix in sentences) {
            println("  $ix : ${TranslatedSentences().sentences[ix]}")
        }
    }

    @Test
    fun leastBurdenConnections() {
        val leastBurdenTransitions = LeastBurdenSentenceTransitions()
        val translated = TranslatedSentences().sentences
        val index = ScoreCoordinateIndex()
        for ((ixFrom, _) in index.sentences.withIndex()) {
            val (sentences, burden) = leastBurdenTransitions.getNextSentences(ixFrom)

            println("${translated[ixFrom]} with burden $burden -> ")
            for (ix in sentences) {
                println("  ${translated[ix]}")
            }
        }
    }
}