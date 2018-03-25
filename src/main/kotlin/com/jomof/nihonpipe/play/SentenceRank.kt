package com.jomof.nihonpipe.play

import com.jomof.intset.IntSet
import com.jomof.nihonpipe.datafiles.TranslatedSentences

data class SentenceRank(
        val rank: Int,
        val sentenceIndex: Int,
        val marginalScoreCoordinates: IntSet,
        val marginalBurden: Int) {

//    private fun englishGrammar(japaneseGrammar : String) : String {
//        return when(japaneseGrammar) {
//            "名詞" -> "noun"
//            "助詞" -> "particle"
//            "助動詞" -> "auxiliary verb"
//            "接尾" -> "suffix"
//            "特殊・デス" -> "desu"
//            "特殊・マス" -> "masu"
//            "数" -> "number"
//            "助数詞" -> "counter_suffix"
//            "自立" -> "independent"
//            "動詞" -> "verb"
//            "格助詞" -> "case particle"
//            "サ変接続" -> "irregular_connection"
//            "五段・ラ行" -> "godan_ru_verb"
//            "五段・サ行" -> "godan_su_verb"
//            "五段・マ行" -> "godan_mu_verb"
//            "五段・ラ行特殊" -> "godan_ru_special_verb"
//            "副詞" -> "adverb"
//            "名詞接続" -> "noun_connection"
//            "接頭詞" -> "prefix"
//            "接続詞" -> "conjunction"
//            "感動詞" -> "interjection"
//            "助詞類接続" -> "particle_type_connection"
//            "形容動詞語幹" -> "adjective_stem"
//            "副助詞" -> "adverbial_particle"
//            "五段・ワ行促音便" -> "godan_nasal"
//            "引用" -> "quote"
//            "不変化型" -> "non_change"
//            "読点" -> "comma"
//            "形容詞・イ段" -> "adjective_idan"
//            "係助詞" -> "dependency_marker"
//            "非自立" -> "not_independent"
//            "連体化" -> "pre_noun_adjectival_like"
//            "代名詞" -> "pronoun"
//            "サ変・スル" -> "sahen_suru"
//            "副詞可能" -> "potential_adverb"
//            "特殊・タ" -> "special_ta"
//            "特殊・ナイ" -> "special_nai"
//            "特殊・ダ" -> "special_da"
//            "一段" -> "ichidan_verb"
//            "一段・クレル" -> "ichidan_kureru"
//            "接続助詞" -> "conjunctive_particle"
//            "連体詞" -> "prenoun_adjectival"
//            "形容詞" -> "adjective"
//            "地域" -> "area"
//            "国" -> "country"
//            "特殊・ヌ" -> "special_nu"
//            "副詞化" -> "adverbification"
//            "数接続" -> "number_connection"
//            "固有名詞" -> "proper_noun"
//            "五段・カ行イ音便" -> "godan_verb_ku_euphonic"
//            "五段・カ行促音便" -> "godan_verb_ku_nasal"
//            "五段・タ行" -> "godan_verb_tsu"
//            "五段・バ行" -> "godan_verb_bu"
//            "ナイ形容詞語幹" -> "nai_adjective_stem"
//            "形容詞・アウオ段" -> "adjective_auodan"
//            "終助詞" -> "sentence_ending_particle"
//            "副助詞／並立助詞／終助詞" -> "adverbial_particle/parallel_marker/sentence_ending_particle"
//            else -> throw RuntimeException(japaneseGrammar)
//        }
//    }

    fun toDisplayString(): String {
        fun labelOf(name: String, elements: List<String>): String {
            return if (elements.isEmpty()) {
                ""
            } else {
                "$name:" + elements.joinToString("\\")
            }
        }

        val scoreCoordinateIndex = ScoreCoordinateIndex()
        val sentence = TranslatedSentences().sentences[sentenceIndex]!!
        var coordinates = marginalScoreCoordinates.map { index ->
            scoreCoordinateIndex.coordinates[index]
        }

        val token = labelOf("frequency", coordinates
                .filter { it.ladderKind == LadderKind.TOKEN_FREQUENCY_LADDER }
                .map { it.key })
        val wanikani = labelOf("wanikani", coordinates
                .filter { it.ladderKind == LadderKind.WANIKANI_VOCAB_LADDER }
                .map { it.key })
        val jlpt = labelOf("jlpt", coordinates
                .filter { it.ladderKind == LadderKind.JLPT_VOCAB_LADDER }
                .map { it.key })
        val skeleton = labelOf("pattern", coordinates
                .filter { it.ladderKind == LadderKind.SENTENCE_SKELETON_LADDER }
                .map { it.key })
        val grammar = labelOf("grammar", coordinates
                .filter { it.ladderKind == LadderKind.GRAMMAR_SUMMARY_LADDER }
                .map { it.key })
        if (token.isEmpty() && grammar.isEmpty() && skeleton.isEmpty() && jlpt.isEmpty() && wanikani.isEmpty()) {
            println("huh")
        }
        return "#$rank, ${sentence.japanese}, ${sentence.english}, " +
                "$marginalBurden, $grammar, $skeleton, $jlpt, $wanikani, $token"
    }
}