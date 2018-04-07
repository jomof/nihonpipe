package com.jomof.nihonpipe.groveler

import com.google.common.truth.Truth.assertThat
import com.jomof.intset.intSetOf
import com.jomof.nihonpipe.datafiles.*
import com.jomof.nihonpipe.groveler.schema.particleSkeletonForm
import com.jomof.nihonpipe.play.*
import com.jomof.nihonpipe.play.LadderKind.*
import com.jomof.nihonpipe.play.io.StudyActionType.NOTHING
import com.jomof.nihonpipe.play.io.StudyActionType.SENTENCE_TEST
import com.jomof.nihonpipe.sampleSentencesTsv
import org.junit.Test
import java.util.*
import kotlin.math.min


class Play {
    @Test
    fun prepopulate() {
        println("populate translated sentences")
        for (ladderKind in values()) {
            println("$ladderKind")
            ladderKind.levelProvider.size
        }
        println("score coordinate index")
        ladderCoordinateIndexesOfSentence(0)
        println("least burden transitions")
        (0 until min(20, sentenceIndexRange().count())).map {
            if (it % 10 == 0) println("sentence $it")
            LeastBurdenSentenceTransitions().getNextSentences(it)
        }

    }

    @Test
    fun simplePlayer() {
        (0..1000).forEach {
            Player(sentenceScores = mutableMapOf(
                    "入口はどこですか。" to Score(100, 0),
                    "私の日本語教師の犬には名刺があります。" to Score(50, 50)))
        }
    }

    @Test
    fun reportMissingLevelKeys() {
        (0..0).forEach {
            val player = Player(sentenceScores = mutableMapOf(
                    "入口はどこですか。" to Score(100, 0),
                    "私の日本語教師の犬には名刺があります。" to Score(60, 50),
                    "日本語が分かりましたか。" to Score(75, 50),
                    "ばい菌だらけだ！" to Score(7, 0),
                    //"今こそ一気に取引をまとめるときだ。" to Score(12, 2),
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
    fun repro() {
        val target1 = "頭 の 毛 は 灰色 だっ た 。"
        val target2 = "トイレ は どこ です か 。"
        val index1 = japaneseToSentenceIndex(target1)
        val index2 = japaneseToSentenceIndex(target2)
        val reasons1 = ladderCoordinateIndexesOfSentence(index1).toSet()
        val reasons2 = ladderCoordinateIndexesOfSentence(index2).toSet()
        val player = Player(sentenceScores = mutableMapOf())
        assertThat(player.ladderKeysCovered.size).isEqualTo(0)
        player.addSentence(target1)
        assertThat(player.ladderKeysCovered).isEqualTo(reasons1)
        assertThat(ladderCoordinateIndexesOfSentence(index1)).isEqualTo(reasons1)
        assertThat(ladderCoordinateIndexesOfSentence(index2)).isEqualTo(reasons2)
        player.addSentence(target2)
        assertThat(player.ladderKeysCovered.toSet()).isNotEqualTo(reasons1)
        assertThat(player.ladderKeysCovered.toSet()).isNotEqualTo(reasons2)
        val combined = reasons1 union reasons2
        assertThat(player.ladderKeysCovered).isEqualTo(combined)
    }

    private val seedSentences = listOf(
            "トイレ は どこ です か 。",
            "寒いですね？",
            "ただいま！",
            "おやすみなさい。",
            "コンビニはどこですか？",
           // "乾杯！",
            "何時ですか？",
            "これは何ですか？",
            "それはいくらですか？",
            "東京駅はどこですか？",
            "水 を ください 。",
            "ありがとう。",
            "彼 は 眠り込ん だ 。")

    @Test
    fun playTheGame() {
        val player = Player(
                seedSentences = seedSentences,
                sentenceScores = mutableMapOf())
        var time = GregorianCalendar(2020, 1, 1)
        fun time(): String {
            val year = time.get(Calendar.YEAR)
            val month = time.get(Calendar.MONTH)
            val day = time.get(Calendar.DAY_OF_MONTH)
            val hour = time.get(Calendar.HOUR_OF_DAY)
            val minute = time.get(Calendar.MINUTE)
            return "$year-$month-$day $hour:$minute"
        }

        var questionsAnswer = 0.0
        var daysElapsed = 0.0
        var rand = Random(0)
        val chanceCorrect = .80
        var number = 1
        val set = mutableSetOf<String>()
        while (number < 5000) {
            //println("${time()}")
            val requestResponse =
                    player.requestStudyAction(time.timeInMillis)
            when (requestResponse.type) {
                NOTHING -> {
                    //println("sleeping for one day. Questions per day ${questionsAnswer / daysElapsed}")
                    time.add(Calendar.HOUR, 24)
                    ++daysElapsed
                }
                SENTENCE_TEST -> {
                    ++questionsAnswer
                    //println("question: ${requestResponse.english}")
                    //println("skeleton hint: ${requestResponse.hints.skeleton}")
                    val answer = if (rand.nextDouble() > chanceCorrect) {
                        "bob"
                    } else {
                        requestResponse.debug.pronunciation
                    }
                    val answerResponse = player.respondSentenceTest(
                            sentence = requestResponse.sentence,
                            answer = answer,
                            currentTime = time.timeInMillis
                    )
                    if (answerResponse.mezzoPromotion == MezzoScore.MASTER &&
                            !set.contains(requestResponse.english)) {
                        set += requestResponse.english
                        val achievementsUnlocked = answerResponse.achievementsUnlocked
                        val vocabSeen = mutableSetOf<String>()
                        vocabSeen += "。"
                        vocabSeen += "？"
                        val achievementElementsUnlocked = answerResponse
                                .achievementElementsUnlocked
                                .map { (vocab, flavors) ->
                                    vocabSeen += vocab
                                    "$vocab from ${flavors.joinToString(" and ")}"
                                }
                        val ladderKeysUnlocked = answerResponse.ladderKeyElementsUnlocked
                                .mapNotNull { coordinate ->
                                    val result =
                                            when (coordinate.ladderKind) {
                                                TOKEN_SURFACE_FREQUENCY_LADDER, TOKEN_BASEFORM_FREQUENCY_LADDER -> {
                                                    if (!vocabSeen.contains(coordinate.key)) {
                                                        vocabSeen += coordinate.key
                                                        "${coordinate.key} not from JLPT or Wanikani"
                                                    } else {
                                                        null
                                                    }
                                                }
                                                SENTENCE_SKELETON_LADDER -> "'${coordinate.key}' sentence pattern"
                                                GRAMMAR_SUMMARY_LADDER -> "${coordinate.key} grammar form"
                                                else -> throw RuntimeException("")
                                            }
                                    result
                                }
                        val unlocks = (ladderKeysUnlocked + achievementsUnlocked + achievementElementsUnlocked)
                                .joinToString()
                        println("#$number," +
                                "${time()}," +
                                "${answerResponse.japanese}," +
                                "${requestResponse.english}," +
                                "${answerResponse.pronunciation}," +
                                "${answerResponse.reading}," +
                                unlocks)
                        ++number

                    }
                    //println("$answerResponse")
                }
            }
            //println("Questions per day ${questionsAnswer / daysElapsed}")
            //println("Stats: ${player.requestUserStatistics()}")
        }
    }

    @Test
    fun addNextSentence() {

        val player = Player(
                seedSentences = seedSentences,
                sentenceScores = mutableMapOf())
        sampleSentencesTsv.delete()
        (0..2000).forEach {
            if ((it) % 50 == 49) {

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
                println("Disambiguation stats; ${player.disambiguationStats}")
                println("Postachievement stats; ${player.postAchievementStats}")
                println("Ladder elements covered; ${player.ladderKeysCovered.size}")
                println("Achievement elements covered; ${player.achievementsCovered.size}")
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
        val allSentenceCount = sentenceIndexRange().count()
        for (ladderKind in values()) {
            val sentences = intSetOf()
            for (level in 0 until ladderKind.levelProvider.size) {
                sentences += ladderKind.levelProvider.getLevelSentences(level)
            }
            if (sentences.size != allSentenceCount) {
                (0..allSentenceCount).forEach { index ->
                    if (!sentences.contains(index)) {
                        val sentence = sentenceIndexToTranslatedSentence(index)
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
        val target = "私に触らないで。"
        //val target = "私と一緒に外に来て。"
        //val target = "これ は 本 です 。"
        //val target = "お母さん は どこ 。"
        //val target = "ここ は 今 乾期 です 。"
        //val target = "バラ は 今 満開 です 。"
        val tokenization = tokenizeJapaneseSentence(target)
        println("tokens = \r\n${tokenization.tokens.joinToString("\r\n")}")
        val index = japaneseToSentenceIndex(target)
        val found = sentenceIndexToTranslatedSentence(index)

        println("$found")
        println("sentence index = $index")
        println("reading = ${tokenization.reading()}")
        println("skeleton = ${tokenization.particleSkeletonForm()}")
        val skeletonSentences = SentenceSkeletonFilter
                .filterOf
                .skeletons[tokenization.particleSkeletonForm()]!!
        println("There are ${skeletonSentences.size} sentences with this skeleton")
        fun locateInLevel(ladderKind: LadderKind) {
            val provider = ladderKind.levelProvider
            var foundSentence = false
            val size = provider.size
            for (level in 0 until size) {
                val keySentences = provider.getKeySentences(level)
                for (keySentence in keySentences) {
                    if (keySentence.sentences.contains(index)) {
                        println("$ladderKind level=$level of ${provider.size} key=${keySentence.key}")
                        foundSentence = true
                    }
                }
            }
            if (!foundSentence) {
                println("NOT FOUND in $ladderKind")
            }
        }
        for (ladderKind in values()) {
            locateInLevel(ladderKind)
        }

        val coordinates = ladderCoordinateIndexesOfSentence(index)
        for (coordinateIndex in coordinates) {
            val coordinate = ladderCoordinateOfLadderCoordinateIndex(
                    coordinateIndex)
            println("$coordinate")
        }

        val (sentences, burden) = LeastBurdenSentenceTransitions().getNextSentences(index)
        println("The most similar sentences have burden $burden -> ")
        for ((count, ix) in sentences.withIndex()) {
            println("  $ix : ${sentenceIndexToTranslatedSentence(ix)}")
            if (count > 20) break
        }
    }

    @Test
    fun testSomeBurdens() {
        val sentence = "はじめまして。"
        val index = japaneseToSentenceIndex(sentence)
        val back = sentenceIndexToTranslatedSentence(index)
        assertThat(back.japanese).isEqualTo(sentence)
        val coordinatesOfSentence = ladderCoordinateIndexesOfSentence(index)
        assertThat(coordinatesOfSentence).hasSize(4)
        absoluteBurdenOfSentence(index)
    }

    @Test
    fun leastBurdenConnections() {
        val leastBurdenTransitions = LeastBurdenSentenceTransitions()
        for (ixFrom in sentenceIndexRange()) {
            val (sentences, burden) = leastBurdenTransitions.getNextSentences(ixFrom)

            println("$burden : ${sentenceIndexToTranslatedSentence(ixFrom)} -> ")
            for (ix in sentences) {
                println("  ${sentenceIndexToTranslatedSentence(ix)}")
            }
            break
        }
    }
}